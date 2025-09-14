//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


package ca.openosp.openo.prescript.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ca.openosp.openo.utility.MiscUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Utility class providing XML document creation, manipulation, and parsing functionality.
 * This class offers a simplified interface for common XML operations including document
 * creation, node manipulation, XML serialization, and parsing of XML strings.
 *
 * <p>The class provides static methods for:</p>
 * <ul>
 * <li>Creating new XML documents</li>
 * <li>Adding elements and text nodes to XML structures</li>
 * <li>Converting DOM documents to XML strings</li>
 * <li>Parsing XML strings into DOM documents</li>
 * <li>Extracting text content from XML nodes</li>
 * </ul>
 *
 * <p>This utility class is particularly useful for generating XML content for
 * web service communications and processing XML responses from external services.</p>
 *
 * <p><b>Note:</b> Class name follows legacy naming convention (lowercase) and should
 * be considered for refactoring to follow Java naming standards.</p>
 *
 * @since 2007-04-16
 */
public class xml {

    /**
     * Creates a new empty XML Document instance.
     *
     * @return Document a new empty XML document, or null if creation fails
     */
    public static Document newDocument() {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            return document;
        } catch (Exception e) {
            Document document1 = null;
            return document1;
        }
    }

    /**
     * Adds a new element node with the specified name to the parent node.
     * This is a convenience method that calls addNode(parentNode, name, null).
     *
     * @param parentNode Node the parent node to add the new element to
     * @param name String the name of the new element
     * @return Element the newly created and added element
     */
    public static Element addNode(Node parentNode, String name) {
        return addNode(parentNode, name, null);
    }

    /**
     * Adds a new element node with the specified name and text content to the parent node.
     *
     * @param parentNode Node the parent node to add the new element to
     * @param name String the name of the new element
     * @param value String the text content for the new element, or null for no text content
     * @return Element the newly created and added element
     */
    public static Element addNode(Node parentNode, String name, String value) {
        Element node = null;
        // Check if parent is a Document node (type 9) or an Element node
        if (parentNode.getNodeType() == 9)
            node = ((Document) parentNode).createElement(name);
        else
            node = parentNode.getOwnerDocument().createElement(name);

        // Add text content if provided
        if (value != null)
            node.appendChild(node.getOwnerDocument().createTextNode(value));

        return (Element) parentNode.appendChild(node);
    }

    /**
     * Converts an XML Document to its string representation.
     *
     * @param xmlDoc Document the XML document to serialize
     * @return String the XML document as a string, or an empty string if transformation fails
     */
    public static String toXML(Document xmlDoc) {
        StringWriter ret = new StringWriter();
        DOMSource src = new DOMSource(xmlDoc);
        StreamResult rslt = new StreamResult(ret);
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(src, rslt);
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        }
        return ret.toString();
    }

    /**
     * Parses an XML string into a Document object.
     *
     * @param xmlInput String the XML content to parse
     * @return Document the parsed XML document, or null if parsing fails
     */
    public static Document parseXML(String xmlInput) {
        Document document;
        try {
            InputSource is = new InputSource(new StringReader(xmlInput));
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            Document document1 = doc;
            return document1;
        } catch (Exception e) {
            document = null;
        }
        return document;
    }

    /**
     * Extracts all text content from a node by concatenating text from all text node children.
     * Only processes child nodes of type TEXT_NODE (type 3).
     *
     * @param node Node the node to extract text content from
     * @return String the concatenated text content, or empty string if no text nodes found
     */
    public static String getText(Node node) {
        String ret = "";
        if (node.hasChildNodes()) {
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                Node sub = node.getChildNodes().item(i);
                // Check if child node is a text node (type 3 = TEXT_NODE)
                if (sub.getNodeType() == 3)
                    ret = String.valueOf(ret) + String.valueOf(sub.getNodeValue());
            }
        }
        return ret;
    }
}
