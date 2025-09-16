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

//home/marc/t/oscar/src/main/java/oscar/oscarLab/ca/all/upload/handlers/IHAHandler.java
//Created on December 8, 2009. Modified from DefaultHandler.java

package ca.openosp.openo.lab.ca.all.upload.handlers;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ca.openosp.openo.utility.MiscUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.hl7.v2.oscar_to_oscar.DynamicHapiLoaderUtils;
import ca.openosp.openo.utility.LoggedInInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ca.openosp.openo.lab.ca.all.parsers.DefaultGenericHandler;
import ca.openosp.openo.lab.ca.all.upload.MessageUploader;
import ca.openosp.OscarProperties;

@Deprecated
/**
 * @Deprecated use IHAPOIHandler
 *
 */
public class IHAHandler extends DefaultGenericHandler implements MessageHandler {
    Logger logger = MiscUtils.getLogger();
    String hl7Type = null;
    String proNo, UserID, Password, Alias;
    ArrayList<String> headerList = null;
    Object terser;
    Object msg = null;


    String getHl7Type() {
        return "IHA";
    }

    @Override
    public String getMsgType() {
        return ("IHA");
    }

    @Override
    public ArrayList<String> getHeaders() {
        headerList = new ArrayList<String>();

        for (int i = 0; i < getOBRCount(); i++) {
            headerList.add(getOBRName(i));
            logger.debug("ADDING to header " + getOBRName(i));
        }
        return headerList;
    }

    @Override
    public String getObservationHeader(int i, int j) {
        return headerList.get(i);
    }

    @Override
    public String getOBXReferenceRange(int i, int j) {
        return (getOBXField(i, j, 7, 0, 3));
    }

    @Override
    public String getAccessionNum() {
        try {
            String accessionNum = getString(DynamicHapiLoaderUtils.terserGet(terser, "/.MSH-10-1"));
            int firstDash = accessionNum.indexOf("-");
            int secondDash = accessionNum.indexOf("-", firstDash + 1);
            return (accessionNum.substring(firstDash + 1, secondDash));
        } catch (Exception e) {
            return ("");
        }
    }

    public void setParameters(String proNo, String UserID, String Password, String Alias) {
        this.proNo = proNo;
        this.UserID = UserID;
        this.Password = Password;
        this.Alias = Alias;
    }

    @Override
    public String parse(LoggedInInfo loggedInInfo, String serviceName, String fileName, int fileId, String ipAddr) {
        Document xmlDoc = getXML(fileName);
        Node node;
        Element element;
        NamedNodeMap nnm = null;
        String msgId = null;
        String result = null;

        if (xmlDoc != null) {
            String hl7Body = null;
            String attrName = null;
            try {
                msgId = null;
                NodeList allNodes = xmlDoc.getElementsByTagNameNS("*", "*");

                for (int i = 1; i < allNodes.getLength(); i++) {
                    try {
                        element = (Element) allNodes.item(i);
                        nnm = element.getAttributes();
                        if (nnm != null) {
                            for (int j = 0; j < nnm.getLength(); j++) {
                                node = nnm.item(j);
                                attrName = node.getNodeName();
                                if (attrName.equals("msgId")) {
                                    msgId = node.getNodeValue();
                                }
                            }
                        }
                        hl7Body = allNodes.item(i).getFirstChild().getTextContent();

                        logger.debug("MESSAGE ID: " + msgId);
                        logger.debug("MESSAGE: ");
                        logger.debug(hl7Body);

                        if (hl7Body != null && hl7Body.indexOf("\nPID|") > 0) {
                            logger.info("using xml HL7 Type " + getHl7Type());
                            MessageUploader.routeReport(loggedInInfo, serviceName, "IHA", hl7Body, fileId);
                            result += "success:" + msgId + ",";
                        }

                    } catch (Exception e) {

                        logger.debug("NESTED EXCEPTION RESULT: " + result);

                        result += "fail:" + msgId + ",";
                    }

                    // The exception handling here is very dangerous.
                }
            } catch (Exception e) {

                logger.debug("EXCEPTION RESULT: " + result);

                MessageUploader.clean(fileId);
                logger.error("ERROR:", e);
                return null;
            }
        }

        logger.debug("FINAL RESULT: " + result);

        return (result);
    }

    /*
     *  Return the message as an xml document if it is in the xml format
     */
    private Document getXML(String fileName) {
        try {
            // Validate the file path to prevent path traversal attacks
            File file = new File(fileName);
            
            // Get the canonical path to resolve any relative path components
            String canonicalPath = file.getCanonicalPath();
            
            // Ensure the file exists and is a regular file
            if (!file.exists() || !file.isFile()) {
                logger.error("File does not exist or is not a regular file: " + fileName);
                return null;
            }
            
            // Additional validation: ensure the file is within the expected document directory
            OscarProperties props = OscarProperties.getInstance();
            String documentDir = props.getProperty("DOCUMENT_DIR");
            if (documentDir != null && !documentDir.isEmpty()) {
                File docDir = new File(documentDir).getCanonicalFile();
                if (!canonicalPath.startsWith(docDir.getCanonicalPath() + File.separator)) {
                    logger.error("Attempted to access file outside document directory: " + canonicalPath);
                    throw new SecurityException("Access denied: file outside permitted directory");
                }
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            try {
                // Disable external entities to prevent XXE attacks
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            
            } catch (ParserConfigurationException e) {
                MiscUtils.getLogger().error("Failed to set XML parser features to prevent XXE attacks", e);
                throw new RuntimeException(e);
            }
            
            // Use the validated file object instead of creating a new FileInputStream with the raw path
            Document doc = factory.newDocumentBuilder().parse(file);
            return (doc);

            // Ignore exceptions and return false
        } catch (Exception e) {
            logger.error("Error parsing XML file: " + fileName, e);
            return (null);
        }
    }
}
