//CHECKSTYLE:OFF
/**
 * Copyright (c) 2006-. OSCARservice, OpenSoft System. All Rights Reserved.
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
 */

package oscar.oscarEncounter.oscarMeasurements.hl7;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.oscarehr.common.dao.MeasurementDao;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.Measurement;
import org.oscarehr.managers.DemographicManager;
import org.oscarehr.managers.SecurityInfoManager;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.SpringUtils;

import oscar.OscarProperties;
import oscar.oscarLab.ca.all.pageUtil.LabUpload2Action;
import oscar.oscarLab.ca.all.util.Utilities;
import ca.uhn.hl7v2.model.v23.datatype.CE;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_RESPONSE;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * External API endpoint for uploading patient measurements via HL7 ORU^R01 messages.
 * 
 * <p>This action provides an HTTP interface for external systems to submit patient 
 * measurement data in HL7 v2.3 format. The measurements are parsed from the HL7 
 * message and stored in the OpenO measurements table.</p>
 * 
 * <h3>Endpoint:</h3>
 * <pre>POST /MeasurementHL7Uploader.do</pre>
 * 
 * <h3>Authentication:</h3>
 * <ul>
 *   <li>Requires valid OpenO session with _measurement write privileges</li>
 *   <li>Optional password protection via oscar.measurements.hl7.password property</li>
 *   <li>Supports RSA encryption for secure transmission when password is not used</li>
 * </ul>
 * 
 * <h3>Request Parameters:</h3>
 * <ul>
 *   <li><b>importFile</b> - Multipart file upload containing HL7 message</li>
 *   <li><b>hl7msg</b> - Alternative: HL7 message as plain text string</li>
 *   <li><b>password</b> - Optional password if configured</li>
 *   <li><b>signature</b> - RSA signature for encrypted messages</li>
 *   <li><b>key</b> - Symmetric key for encrypted messages</li>
 *   <li><b>service</b> - Public key ID for encrypted messages</li>
 * </ul>
 * 
 * <h3>Configuration Properties:</h3>
 * <ul>
 *   <li><b>oscar.measurements.hl7.defaultProviderNo</b> - Default provider if not specified in HL7 (default: 999998)</li>
 *   <li><b>oscar.measurements.hl7.password</b> - Optional password for authentication</li>
 *   <li><b>oscar.measurements.hl7.datetime.format</b> - Date format for parsing HL7 timestamps (default: yyyyMMddHHmmss)</li>
 * </ul>
 * 
 * @see <a href="/docs/actions/MeasurementHL7Uploader.md">Full API Documentation</a>
 * @since OpenO 2006
 * @author OSCARservice, OpenSoft System
 */
public class MeasurementHL7Uploader2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static Logger logger = org.oscarehr.util.MiscUtils.getLogger();
    
    /** Date formatter for parsing HL7 observation timestamps */
    private static SimpleDateFormat sdf = new SimpleDateFormat(OscarProperties.getInstance().getProperty("oscar.measurements.hl7.datetime.format", "yyyyMMddHHmmss"));

    private static MeasurementDao measurementsDao = SpringUtils.getBean(MeasurementDao.class);
    private static SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /** Default provider number used when not specified in HL7 message. Configurable via oscar.properties */
    private String defaultProviderNo = OscarProperties.getInstance().getProperty("oscar.measurements.hl7.defaultProviderNo", "999998");
    
    /** Optional password for additional security. When set, must be included in MSH-8 field */
    private String hl7UploadPassword = OscarProperties.getInstance().getProperty("oscar.measurements.hl7.password");

    /**
     * Sets the HL7 upload password for authentication.
     * 
     * @param uploadPassword Password to be matched against MSH-8 field in HL7 message
     */
    public void setHl7UploadPassword(String uploadPassword) {
        this.hl7UploadPassword = uploadPassword;
    }

    /**
     * Sets the default provider number for measurements when not specified in HL7.
     * 
     * @param defaultProviderNo Provider number to use as default
     */
    public void setDefaultProviderNo(String defaultProviderNo) {
        this.defaultProviderNo = defaultProviderNo;
    }

    /**
     * Main entry point for Struts action execution.
     * 
     * @return null (writes directly to response)
     */
    public String execute() {
        return upload();
    }

    /**
     * Processes the HL7 ORU^R01 message upload and creates measurements.
     * 
     * <p>This method performs the following operations:</p>
     * <ol>
     *   <li>Validates user permissions</li>
     *   <li>Extracts HL7 message from request (encrypted or plain text)</li>
     *   <li>Parses HL7 ORU^R01 message structure</li>
     *   <li>Validates message type and optional password</li>
     *   <li>Extracts patient information from PID segment</li>
     *   <li>Matches patient to OpenO demographic record</li>
     *   <li>Processes each OBX observation segment</li>
     *   <li>Creates measurement records in database</li>
     * </ol>
     * 
     * @return null - Response is written directly to HttpServletResponse
     * @throws SecurityException if user lacks _measurement write privileges
     */
    public String upload() {

        // Validate user has permission to write measurements
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_measurement", "w", null)) {
            throw new SecurityException("missing required security object (_measurement)");
        }

        Date dateEntered = new Date();
        String hl7msg = null;

        try {
            boolean checkPassword = StringUtils.isNotBlank(hl7UploadPassword);

            // Extract HL7 message - use plain text if password is configured, otherwise decrypt using RSA
            hl7msg = checkPassword ? IOUtils.toString(Files.newInputStream(importFile.toPath())) : extractEncryptedMessage(importFile, request);

            // Validate password strength if configured
            if (checkPassword && hl7UploadPassword.length() < 16)
                throw new RuntimeException("Upload password length is too weak, please check oscar property file and make sure it's more than 16 letters.");

            // Parse HL7 message using HAPI parser
            Parser p = new GenericParser();
            p.setValidationContext(new NoValidation());
            ORU_R01 msg = (ORU_R01) p.parse(hl7msg);

            // Validate message type is ORU^R01 (Observation Result)
            MSH msh = msg.getMSH();
            String msgType = msh.getMessageType().getMessageType().getValue() + "_" + msh.getMessageType().getTriggerEvent().getValue();

            if (!"ORU_R01".equals(msgType)) throw new RuntimeException("Message type is not ORU_R01 - " + msgType);

            // Verify password matches if password protection is enabled
            if (checkPassword && !hl7UploadPassword.equals(msh.getSecurity().getValue()))
                throw new RuntimeException("Password in MSH is invalid.");

            String sender = msh.getSendingApplication().getNamespaceID().getValue();
            String receiver = msh.getReceivingApplication().getNamespaceID().getValue();

            // Log message receipt for audit trail
            String msgId = msh.getMessageControlID().getValue();
            logger.info("HL7 message [" + msgId + "] received from: " + sender + " to: " + receiver + " on " + dateEntered);

            // Extract patient response from HL7 message
            // TODO: handle multiple responses in one upload, right now only assumes 1 per upload
            ORU_R01_RESPONSE resp = msg.getRESPONSE();

            // Extract patient identification from PID segment
            PID patient = resp.getPATIENT().getPID();

            String hcn = patient.getPatientIDInternalID(0).getID().getValue();
            String hcnType = patient.getPatientIDInternalID(0).getAssigningAuthority().getNamespaceID().getValue();
            
            // Match patient to OpenO demographic record using health card number
            DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
            List<Demographic> demos = demographicManager.getActiveDemosByHealthCardNo(loggedInInfo, hcn, hcnType);
            if (demos == null || demos.size() == 0)
                throw new RuntimeException("There is no active patient with the supplied health card number: " + hcn + " " + hcnType);

            // Extract provider from PV1 segment, use default if not specified
            String providerNo = resp.getPATIENT().getVISIT().getPV1().getConsultingDoctor(0).getIDNumber().getValue();
            if (providerNo == null) providerNo = defaultProviderNo;

            // Process observation request (OBR) segment
            ORU_R01_ORDER_OBSERVATION obr = resp.getORDER_OBSERVATION();

            CE univId = obr.getOBR().getUniversalServiceIdentifier();

            // Process all OBX (observation) segments containing measurement data
            int len = obr.getOBSERVATIONReps();
            for (int i = 0; i < len; i++) {
                logger.info("Processing OBX no." + i);
                OBX obx = obr.getOBSERVATION(i).getOBX();
                
                // Parse observation timestamp
                Date dateObserved = sdf.parse(obx.getDateTimeOfTheObservation().getTimeOfAnEvent().getValue());
                
                // Extract measurement type from OBX-3 (Observation Identifier)
                CE obvId = obx.getObservationIdentifier();
                String measurementType = obvId.getIdentifier().getValue(); // First part is the unique short name

                // Extract units and reference range from OBX-6
                String unit = obx.getUnits().getIdentifier().getValue();
                if (unit == null) unit = ""; // OpenO requires non-null unit field
                
                // Append reference range to unit if provided
                String range = obx.getReferencesRange().getValue();
                if (range != null) unit += " Range:" + range;

                // Extract observation value from OBX-5
                String data = obx.getObservationValue(0).getData().toString();

                // Extract abnormal flags from OBX-8
                String abnormal = StringUtils.join(obx.getAbnormalFlags(), "|");
                if (StringUtils.isNotEmpty(abnormal)) abnormal = " Abnormal:" + abnormal;

                logger.info(measurementType + " : " + data + " : " + unit + " : " + dateObserved + " : " + abnormal);

                // Create measurement records for all matching demographics
                // (OpenO may have duplicate patient records with same HCN)
                for (Demographic demo : demos) {
                    Integer demographicNo = demo.getDemographicNo();

                    // Create and persist measurement record
                    Measurement m = new Measurement();
                    m.setComments(abnormal + " by " + sender);
                    m.setDataField(data);
                    m.setDateObserved(dateObserved);
                    m.setDemographicId(demographicNo);
                    m.setMeasuringInstruction(unit);
                    m.setProviderNo(providerNo);
                    m.setType(measurementType);

                    measurementsDao.persist(m);
                }
            }

        } catch (Exception e) {
            // Log error and return appropriate HTTP error response
            logger.error("Failed to parse HL7 ORU_R01 messages:\n" + hl7msg, e);
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            try {
                response.getWriter().println("Invalid HL7 ORU_R01 format/request: " + e.getMessage());
            } catch (IOException e1) {
                logger.error("Error writing error response", e1);
            }
            return null;
        }

        // Return success response
        response.setStatus(HttpStatus.SC_OK);
        return null;
    }

    /**
     * Extracts and decrypts an encrypted HL7 message from the HTTP request.
     * 
     * <p>This method handles RSA-encrypted messages when password authentication is not used.
     * The encryption scheme uses:</p>
     * <ul>
     *   <li>RSA public/private key pairs for key exchange</li>
     *   <li>Symmetric encryption for the actual message content</li>
     *   <li>MD5WithRSA digital signature for message integrity</li>
     * </ul>
     * 
     * <p>Required request parameters for encrypted messages:</p>
     * <ul>
     *   <li><b>signature</b> - Message signature created with MD5WithRSA</li>
     *   <li><b>key</b> - Symmetric key encrypted with client's private key</li>
     *   <li><b>service</b> - ID of client's public key in OpenO's publicKeys table</li>
     * </ul>
     * 
     * <p>All cryptographic parameters must be Base64 encoded.</p>
     * 
     * @param importFile The uploaded file containing the encrypted HL7 message
     * @param request HTTP request containing encryption parameters
     * @return Decrypted HL7 message as plain text string
     * @throws RuntimeException if decryption fails or signature is invalid
     */
    private String extractEncryptedMessage(File importFile, HttpServletRequest request) {

        // Extract encryption parameters from request
        String signature = request.getParameter("signature");
        String key = request.getParameter("key");
        String service = request.getParameter("service");

        // Retrieve client's public key from database
        ArrayList<Object> clientInfo = LabUpload2Action.getClientInfo(service);
        PublicKey clientKey = (PublicKey) clientInfo.get(0);
        String type = (String) clientInfo.get(1);

        try {
            // Decrypt the message using the symmetric key
            InputStream is = LabUpload2Action.decryptMessage(Files.newInputStream(importFile.toPath()), key, clientKey);
            String fileName = importFile.getName();
            String filePath = Utilities.saveFile(is, fileName);

            File file = new File(filePath);

            // Validate message signature to ensure integrity
            if (LabUpload2Action.validateSignature(clientKey, signature, file)) {
                return FileUtils.readFileToString(file);
            } else throw new RuntimeException("Invalid signature, upload rejected.");

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt upload file stream", e);
        }
    }

    /** The uploaded file containing the HL7 message */
    private File importFile;

    /**
     * Gets the uploaded file containing the HL7 message.
     * 
     * @return File object containing HL7 message data
     */
    public File getImportFile() {
        return importFile;
    }

    /**
     * Sets the uploaded file containing the HL7 message.
     * Called by Struts framework for multipart file uploads.
     * 
     * @param importFile File uploaded via multipart form data
     */
    public void setImportFile(File importFile) {
        this.importFile = importFile;
    }
}
