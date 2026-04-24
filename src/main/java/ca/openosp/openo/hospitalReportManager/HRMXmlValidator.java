//CHECKSTYLE:OFF
package ca.openosp.openo.hospitalReportManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
