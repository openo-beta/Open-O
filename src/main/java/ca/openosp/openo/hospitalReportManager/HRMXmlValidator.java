//CHECKSTYLE:OFF
package ca.openosp.openo.hospitalReportManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.openosp.openo.utility.MiscUtils;

/**
 * Validates HRM XML report files against the OntarioMD HRM XSD schema by performing
 * a SAX pre-pass to detect required elements that are present but empty.
 *
 * <p>The set of required elements is derived dynamically at class load time by parsing
 * the XSD schema files, so the validation stays in sync with schema changes without
 * requiring manual maintenance of a hardcoded list.
 *
 * <p>An element is flagged as invalid when it is present but empty AND it does not have
 * {@code minOccurs="0"} in its parent's schema definition. This covers elements in
 * {@code xs:sequence}, {@code xs:all}, and {@code xs:choice}: once a choice branch is
 * present in the document it must not be empty, just like a required sequence element.
 *
 * @since 2026-04-24
 */
public class HRMXmlValidator {

    private static final Logger logger = MiscUtils.getLogger();

    private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    private static final String MAIN_XSD = "/xsd/hrm/1.1.2/ontariomd_hrm.xsd";
    private static final String DT_XSD   = "/xsd/hrm/1.1.2/ontariomd_hrm_dt.xsd";

    /**
     * Maps XML element name → set of child element names that are required per the HRM 1.1.2 XSD.
     * Built once at class load time by parsing both schema files.
     */
    private static final Map<String, Set<String>> REQUIRED_CHILDREN = buildRequiredChildrenFromSchema();

    private HRMXmlValidator() {
    }

    /**
     * Performs a SAX pre-pass over the given HRM XML file and throws a {@link SAXException}
     * if any element that is required by the HRM schema is present but has empty content.
     *
     * <p>Optional elements (minOccurs="0") that appear empty are silently allowed, since the
     * schema permits their omission entirely.
     *
     * @param xmlFile the HRM XML report file to validate; must exist
     * @throws SAXException if a required element is present but empty
     * @throws IOException  if the file cannot be read
     */
    public static void validateNoRequiredElementsEmpty(File xmlFile) throws SAXException, IOException {
        if (REQUIRED_CHILDREN.isEmpty()) return;

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        try {
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            spf.newSAXParser().parse(xmlFile, new DefaultHandler() {
                private final Deque<Boolean> hasChildrenStack = new ArrayDeque<>();
                private final Deque<StringBuilder> textStack = new ArrayDeque<>();
                private final Deque<String> nameStack = new ArrayDeque<>();

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    if (!hasChildrenStack.isEmpty()) {
                        hasChildrenStack.pop();
                        hasChildrenStack.push(true);
                    }
                    hasChildrenStack.push(false);
                    textStack.push(new StringBuilder());
                    nameStack.push(localName);
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    textStack.peek().append(ch, start, length);
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    boolean hasChildren = hasChildrenStack.pop();
                    String text = textStack.pop().toString().trim();
                    nameStack.pop();
                    if (!hasChildren && text.isEmpty()) {
                        String parent = nameStack.isEmpty() ? null : nameStack.peek();
                        Set<String> requiredInParent = parent != null
                            ? REQUIRED_CHILDREN.getOrDefault(parent, Collections.emptySet())
                            : Collections.emptySet();
                        if (requiredInParent.contains(localName)) {
                            throw new SAXException("Invalid content found: required element <"
                                + localName + "> in <" + parent + "> is empty");
                        }
                    }
                }
            });
        } catch (ParserConfigurationException e) {
            throw new SAXException("XML parser configuration error", e);
        }
    }

    // -------------------------------------------------------------------------
    // Invalid placeholder-date normalization
    // -------------------------------------------------------------------------

    /** Namespace (cds_dt) the date leaf elements of {@code dateFullOrPartial} live in. */
    private static final String DT_NS = "cds_dt";

    /** The date-carrying leaf elements of the {@code dateFullOrPartial} type, by local name. */
    private static final String[] DATE_LEAVES = {"DateTime", "FullDate", "YearMonth", "YearOnly"};

    /**
     * Parent element whose placeholder date must NOT be silently substituted. A birthdate is a
     * clinically significant patient identifier, so a placeholder value is left in place and instead
     * rejected by the dedicated DateOfBirth validation rather than having a fabricated date assigned.
     */
    private static final String DOB_PARENT = "DateOfBirth";

    /**
     * Parses the HRM XML file and replaces any invalid placeholder date value (such as the
     * {@code 0-00-00T00:00:00} some sending facilities emit to mean "no date") with today's date in
     * the format expected by the element's type, so the report passes XSD validation instead of being
     * rejected outright. A warning naming the affected element is recorded for each substitution.
     *
     * <p>{@code DateOfBirth} is intentionally excluded so a placeholder birthdate is still rejected by
     * the stricter DateOfBirth validation rather than silently replaced.
     *
     * @param xmlFile  the HRM XML report file to normalize; must exist
     * @param warnings collector that receives one human-readable warning per substituted date
     * @return the parsed (and possibly modified) DOM, ready to be unmarshalled
     * @throws SAXException if the file cannot be parsed
     * @throws IOException  if the file cannot be read
     */
    public static Document normalizeInvalidDates(File xmlFile, List<String> warnings)
            throws SAXException, IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setExpandEntityReferences(false);
            Document doc = dbf.newDocumentBuilder().parse(xmlFile);

            DatatypeFactory dtf = DatatypeFactory.newInstance();
            for (String leafName : DATE_LEAVES) {
                normalizeDateLeaves(doc, leafName, dtf, warnings);
            }
            return doc;
        } catch (ParserConfigurationException | DatatypeConfigurationException e) {
            throw new SAXException("XML parser configuration error", e);
        }
    }

    private static void normalizeDateLeaves(Document doc, String leafLocalName,
                                            DatatypeFactory dtf, List<String> warnings) {
        NodeList nodes = doc.getElementsByTagNameNS(DT_NS, leafLocalName);
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Element leaf = (org.w3c.dom.Element) nodes.item(i);

            Node parentNode = leaf.getParentNode();
            String parentName = parentNode != null ? parentNode.getLocalName() : leafLocalName;
            if (DOB_PARENT.equals(parentName)) continue;

            String value = leaf.getTextContent() == null ? "" : leaf.getTextContent().trim();
            if (value.isEmpty() || isValidDateValue(value, dtf)) continue;

            String replacement = todaysValueFor(leafLocalName);
            leaf.setTextContent(replacement);
            warnings.add("<" + parentName + "> contained an invalid date '" + value
                + "'; using today's date (" + replacement + ") instead.");
            logger.warn("HRM report <{}> had invalid date '{}'; substituted today's date '{}'",
                parentName, value, replacement);
        }
    }

    /**
     * Returns {@code true} when {@code value} is a lexically valid date/dateTime with in-range month
     * and day. Placeholder values such as {@code 0-00-00T00:00:00} fail because the lexical form is
     * rejected (or the month/day are zero).
     */
    private static boolean isValidDateValue(String value, DatatypeFactory dtf) {
        try {
            XMLGregorianCalendar cal = dtf.newXMLGregorianCalendar(value);
            int year = cal.getYear();
            if (year != DatatypeConstants.FIELD_UNDEFINED && year <= 0) return false;
            int month = cal.getMonth();
            if (month != DatatypeConstants.FIELD_UNDEFINED && (month < 1 || month > 12)) return false;
            int day = cal.getDay();
            if (day != DatatypeConstants.FIELD_UNDEFINED && (day < 1 || day > 31)) return false;
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String todaysValueFor(String leafLocalName) {
        Date now = Calendar.getInstance().getTime();
        switch (leafLocalName) {
            case "FullDate":  return new SimpleDateFormat("yyyy-MM-dd").format(now);
            case "YearMonth": return new SimpleDateFormat("yyyy-MM").format(now);
            case "YearOnly":  return new SimpleDateFormat("yyyy").format(now);
            case "DateTime":
            default:          return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(now);
        }
    }

    // -------------------------------------------------------------------------
    // Schema introspection — builds REQUIRED_CHILDREN from the XSD files
    // -------------------------------------------------------------------------

    private static Map<String, Set<String>> buildRequiredChildrenFromSchema() {
        Map<String, Set<String>> result = new HashMap<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document mainDoc = db.parse(new ClassPathResource(MAIN_XSD).getInputStream());
            Document dtDoc   = db.parse(new ClassPathResource(DT_XSD).getInputStream());

            // Pass 1: named xs:complexType declarations → required child element names
            Map<String, Set<String>> typeChildren = new HashMap<>();
            collectNamedComplexTypeChildren(dtDoc,   typeChildren);
            collectNamedComplexTypeChildren(mainDoc, typeChildren);

            // Pass 2: every xs:element declaration → required children via inline type or type ref
            collectElementRequiredChildren(mainDoc, typeChildren, result);
            collectElementRequiredChildren(dtDoc,   typeChildren, result);

        } catch (Exception e) {
            logger.error("Failed to build HRM required-children map from schema; empty-element check will be skipped", e);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Reads every named {@code xs:complexType} in the document and records the names of its
     * required child elements (those without {@code minOccurs="0"}) into {@code typeMap}.
     */
    private static void collectNamedComplexTypeChildren(Document doc, Map<String, Set<String>> typeMap) {
        NodeList types = doc.getElementsByTagNameNS(XSD_NS, "complexType");
        for (int i = 0; i < types.getLength(); i++) {
            org.w3c.dom.Element ct = (org.w3c.dom.Element) types.item(i);
            String typeName = ct.getAttribute("name");
            if (typeName.isEmpty()) continue;
            Set<String> required = requiredNamesFromGroup(ct);
            if (!required.isEmpty()) {
                typeMap.merge(typeName, required, (a, b) -> { a.addAll(b); return a; });
            }
        }
    }

    /**
     * For every {@code xs:element} declaration in the document, resolves its required child
     * element names from its inline anonymous complex type and/or its named type reference,
     * then merges the result into {@code result} keyed by the element's own XML name.
     */
    private static void collectElementRequiredChildren(Document doc, Map<String, Set<String>> typeMap,
                                                       Map<String, Set<String>> result) {
        NodeList elements = doc.getElementsByTagNameNS(XSD_NS, "element");
        for (int i = 0; i < elements.getLength(); i++) {
            org.w3c.dom.Element elem = (org.w3c.dom.Element) elements.item(i);
            String elemName = elem.getAttribute("name");
            if (elemName.isEmpty()) continue;

            Set<String> required = new HashSet<>();

            // Resolve named type reference (e.g. type="cdsd:healthCard")
            String typeRef = elem.getAttribute("type");
            if (!typeRef.isEmpty()) {
                String localType = typeRef.contains(":") ? typeRef.substring(typeRef.indexOf(':') + 1) : typeRef;
                Set<String> fromType = typeMap.get(localType);
                if (fromType != null) required.addAll(fromType);
            }

            // Inline anonymous xs:complexType (direct child node of this xs:element)
            NodeList children = elem.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child instanceof org.w3c.dom.Element
                        && "complexType".equals(((org.w3c.dom.Element) child).getLocalName())) {
                    required.addAll(requiredNamesFromGroup((org.w3c.dom.Element) child));
                    break;
                }
            }

            if (!required.isEmpty()) {
                result.merge(elemName, required, (a, b) -> { a.addAll(b); return a; });
            }
        }
    }

    /**
     * Recursively walks {@code xs:sequence}, {@code xs:all}, and {@code xs:choice} groups and
     * returns the names of all {@code xs:element} declarations that have no {@code minOccurs="0"}.
     *
     * <p>Choice elements are included because their {@code minOccurs} attribute reflects whether
     * the element is allowed to be absent — not whether it was selected. Once a choice branch IS
     * present in the document, an empty value is just as invalid as it would be in a sequence.
     * For example, {@code <TextContent/>} inside {@code <Content>} (a choice) should fail if
     * empty, even though {@code Content} itself is optional in the parent.
     */
    private static Set<String> requiredNamesFromGroup(org.w3c.dom.Element parent) {
        Set<String> required = new HashSet<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (!(child instanceof org.w3c.dom.Element)) continue;
            org.w3c.dom.Element childEl = (org.w3c.dom.Element) child;
            String tag = childEl.getLocalName();
            if ("sequence".equals(tag) || "all".equals(tag) || "choice".equals(tag)) {
                required.addAll(requiredNamesFromGroup(childEl));
            } else if ("element".equals(tag) && !"0".equals(childEl.getAttribute("minOccurs"))) {
                String name = childEl.getAttribute("name");
                if (!name.isEmpty()) required.add(name);
            }
        }
        return required;
    }
}
