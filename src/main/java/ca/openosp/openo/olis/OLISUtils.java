//CHECKSTYLE:OFF
package ca.openosp.openo.olis;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.Hl7TextInfoDao;
import ca.openosp.openo.commn.model.Hl7TextInfo;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.OscarAuditLogger;
import ca.openosp.openo.utility.SpringUtils;
import org.xml.sax.InputSource;

import ca.ssha._2005.hial.Response;
import ca.openosp.OscarProperties;
import ca.openosp.openo.lab.ca.all.parsers.Factory;
import ca.openosp.openo.lab.ca.all.parsers.OLISHL7Handler;

/**
 * Utility class for OLIS (Ontario Laboratories Information System) response processing and duplicate detection.
 * <p>
 * This class provides essential utilities for handling OLIS laboratory results, including:
 * - XML response parsing and content extraction
 * - Duplicate laboratory result detection across different laboratory providers
 * - Laboratory-specific accession number normalization
 * - Integration with existing HL7 lab result storage system
 * <p>
 * The duplicate detection system handles laboratory-specific accession number formats for:
 * - Canadian Medical Laboratories (CML)
 * - Gamma Dynacare Medical Laboratories (GDML)
 * - LifeLabs Medical Laboratory Services
 * - Alpha Laboratories
 * <p>
 * Each laboratory has unique accession number formatting that requires normalization
 * to match against existing direct lab interface results stored in the system.
 *
 * @since 2008
 */
public class OLISUtils {
    /** Logger for OLIS utility operations and duplicate detection */
    static Logger logger = MiscUtils.getLogger();

    /** DAO for accessing HL7 text info records to check for duplicates */
    static Hl7TextInfoDao hl7TextInfoDao = SpringUtils.getBean(Hl7TextInfoDao.class);

    /** OID identifier for Canadian Medical Laboratories */
    static final public String CMLIndentifier = "2.16.840.1.113883.3.59.1:5407";
    /** OID identifier for Gamma Dynacare Medical Laboratories */
    static final public String GammaDyancareIndentifier = "2.16.840.1.113883.3.59.1:5552";
    /** OID identifier for LifeLabs Medical Laboratory Services */
    static final public String LifeLabsIndentifier = "2.16.840.1.113883.3.59.1:5687";
    /** OID identifier for Alpha Laboratories */
    static final public String AlphaLabsIndetifier = "2.16.840.1.113883.3.59.1:5254";

    /**
     * Extracts the content section from an OLIS XML response message.
     * <p>
     * This method processes OLIS XML responses by:
     * 1. Normalizing XML namespace declarations
     * 2. Validating against OLIS response schema (if configured)
     * 3. Unmarshalling XML to Response object using JAXB
     * 4. Extracting and returning the content portion
     * <p>
     * The response XML typically contains nested Content and Error sections that need
     * special namespace handling for proper parsing.
     *
     * @param response String the raw OLIS XML response
     * @return String the extracted content portion of the response
     * @throws Exception if XML parsing, validation, or unmarshalling fails
     */
    public static String getOLISResponseContent(String response) throws Exception {
        response = response.replaceAll("<Content", "<Content xmlns=\"\" ");
        response = response.replaceAll("<Errors", "<Errors xmlns=\"\" ");

        DocumentBuilderFactory.newInstance().newDocumentBuilder();
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        InputStream is = OLISPoller.class.getResourceAsStream("/org/oscarehr/olis/response.xsd");

        Source schemaFile = new StreamSource(is);

        if (OscarProperties.getInstance().getProperty("olis_response_schema") != null) {
            schemaFile = new StreamSource(new File(OscarProperties.getInstance().getProperty("olis_response_schema")));
        }

        factory.newSchema(schemaFile);

        JAXBContext jc = JAXBContext.newInstance("ca.ssha._2005.hial");
        Unmarshaller u = jc.createUnmarshaller();
        @SuppressWarnings("unchecked")
        Response root = ((JAXBElement<Response>) u.unmarshal(new InputSource(new StringReader(response)))).getValue();

        return root.getContent();
    }


    /**
     * Checks if an OLIS HL7 message is a duplicate of an existing laboratory result.
     * <p>
     * This method creates an OLIS HL7 handler from the message and delegates to the
     * handler-based duplicate check method.
     *
     * @param loggedInInfo LoggedInInfo the current user session information
     * @param msg String the HL7 message content to check
     * @return boolean true if this is a duplicate result, false otherwise
     */
    public static boolean isDuplicate(LoggedInInfo loggedInInfo, String msg) {
        OLISHL7Handler h = (OLISHL7Handler) Factory.getHandler("OLIS_HL7", msg);
        return isDuplicate(loggedInInfo, h, msg);
    }

    /**
     * Checks if an OLIS HL7 message from a file is a duplicate of an existing laboratory result.
     * <p>
     * This method reads the file content and delegates to the string-based duplicate check.
     *
     * @param loggedInInfo LoggedInInfo the current user session information
     * @param file File the file containing HL7 message content to check
     * @return boolean true if this is a duplicate result, false otherwise
     * @throws FileNotFoundException if the specified file cannot be found
     * @throws IOException if there is an error reading the file
     */
    public static boolean isDuplicate(LoggedInInfo loggedInInfo, File file) throws FileNotFoundException, IOException {
        String msg = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            msg = IOUtils.toString(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return isDuplicate(loggedInInfo, msg);
    }


    /**
     * Performs duplicate detection using an OLIS HL7 handler to extract key identifiers.
     * <p>
     * This method extracts laboratory identifiers from the HL7 handler and delegates
     * to the identifier-based duplicate check method.
     *
     * @param loggedInInfo LoggedInInfo the current user session information
     * @param h OLISHL7Handler the HL7 message handler containing parsed lab data
     * @param msg String the original HL7 message content for logging
     * @return boolean true if this is a duplicate result, false otherwise
     */
    public static boolean isDuplicate(LoggedInInfo loggedInInfo, OLISHL7Handler h, String msg) {

        String sendingFacility = h.getPlacerGroupNumber();//getPerformingFacilityNameOnly();
        logger.debug("SENDING FACILITY: " + sendingFacility);
        String accessionNumber = h.getAccessionNum();
        String hin = h.getHealthNum();
        String collectionDate = h.getCollectionDateTime(0);
        collectionDate = collectionDate.substring(0, 10).replaceAll("-", "");

        return isDuplicate(loggedInInfo, sendingFacility, accessionNumber, msg, hin, collectionDate);
    }


    public static boolean isDuplicate(LoggedInInfo loggedInInfo, String sendingFacility, String olisAccessionNum, String msg, String hin, String olisCollectionDate) {
        logger.debug("Facility " + sendingFacility + " Accession # " + olisAccessionNum);

        //CML
        if (sendingFacility != null && sendingFacility.equals(CMLIndentifier)) {
            //OLIS ACCESSION NUM LOOKS LIKE Q18OPUT-1215, CML ONE LOOKS LIKE Q18OPUT
            olisAccessionNum = olisAccessionNum.indexOf("-") != -1 ? olisAccessionNum.split("-")[0] : olisAccessionNum;

            for (Hl7TextInfo dupResult : hl7TextInfoDao.searchByAccessionNumber(olisAccessionNum)) {
                String cmlAccessionNum = dupResult.getAccessionNumber();
                cmlAccessionNum = cmlAccessionNum.indexOf("-1") != -1 ? cmlAccessionNum.split("-")[0] : cmlAccessionNum;

                if (hin.equals(dupResult.getHealthNumber())) {
                    String collectionDate = dupResult.getObrDate().substring(0, 10).replaceAll("-", "");
                    if (!StringUtils.isEmpty(collectionDate) && olisCollectionDate.equals(collectionDate)) {
                        OscarAuditLogger.getInstance().log(loggedInInfo, "Lab", "Skip", "Duplicate CML lab skipped - accession " + olisAccessionNum + "\n" + msg);
                        return true;
                    }
                }
            }
        }
        //LIFELABS
        else if (sendingFacility != null && sendingFacility.equals(LifeLabsIndentifier)) {
            //OLIS ACCESSION LOOKS LIKE 2015-Q20OUTPUT, DIRECT LOOKS LIKE 16660-Q20OUTPUT-1 (hl7TextInfo.accession would be Q20OUTPUT)
            olisAccessionNum = olisAccessionNum.substring(5);
            for (Hl7TextInfo dupResult : hl7TextInfoDao.searchByAccessionNumber(olisAccessionNum)) {
                logger.debug("LIFELABS " + dupResult.getAccessionNumber() + " " + olisAccessionNum + " == " + dupResult.getAccessionNumber().equals(olisAccessionNum.substring(5)));

                if (hin.equals(dupResult.getHealthNumber())) {
                    String collectionDate = dupResult.getObrDate().substring(0, 10).replaceAll("-", "");
                    if (!StringUtils.isEmpty(collectionDate) && olisCollectionDate.equals(collectionDate)) {
                        OscarAuditLogger.getInstance().log(loggedInInfo, "Lab", "Skip", "Duplicate LifeLabs lab skipped - accession " + olisAccessionNum + "\n" + msg);
                        return true;
                    }
                }
            }
        }
        //GDML
        else if (sendingFacility != null && sendingFacility.equals(GammaDyancareIndentifier)) {
            //OLIS ACCNUM LOOKS LIKE 201512Q19OUPUT and direct looks like 12-Q19OUPUT
            olisAccessionNum = olisAccessionNum.substring(4);

            List<Hl7TextInfo> dupResults = new ArrayList<Hl7TextInfo>();
            String directAcc = null;
            try {
                directAcc = olisAccessionNum.substring(0, 2) + "-" + olisAccessionNum.substring(2);
                dupResults = hl7TextInfoDao.searchByAccessionNumber(directAcc);
            } catch (Exception e) {

            }

            for (Hl7TextInfo dupResult : dupResults) {
                logger.debug(dupResult.getAccessionNumber() + " == " + directAcc + " " + dupResult.getAccessionNumber().equals(directAcc));

                if (dupResult.getAccessionNumber().equals(directAcc)) {
                    if (hin.equals(dupResult.getHealthNumber())) {
                        String collectionDate = dupResult.getObrDate().substring(0, 10).replaceAll("-", "");
                        if (!StringUtils.isEmpty(collectionDate) && olisCollectionDate.equals(collectionDate)) {
                            OscarAuditLogger.getInstance().log(loggedInInfo, "Lab", "Skip", "Duplicate GAMMA lab skipped - accession " + olisAccessionNum + "\n" + msg);
                            return true;
                        }
                    }
                }
            }
        }
        //ALPHA
        else if (sendingFacility != null && sendingFacility.equals(AlphaLabsIndetifier)) {
            List<Hl7TextInfo> dupResults = hl7TextInfoDao.searchByAccessionNumber(olisAccessionNum.substring(5));
            for (Hl7TextInfo dupResult : dupResults) {
                logger.debug("AlphaLabs " + dupResult.getAccessionNumber() + " " + olisAccessionNum + " == " + dupResult.getAccessionNumber().equals(olisAccessionNum.substring(5)));

                if (dupResult.getAccessionNumber().equals(olisAccessionNum.substring(5))) {
                    if (hin.equals(dupResult.getHealthNumber())) {
                        OscarAuditLogger.getInstance().log(loggedInInfo, "Lab", "Skip", "Duplicate AlphaLabs lab skipped - accession " + olisAccessionNum + "\n" + msg);
                        return true;
                    }
                }
            }

        }


        return false;
    }


}
