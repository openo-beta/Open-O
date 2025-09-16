//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.olis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.PatientLabRoutingDao;
import ca.openosp.openo.commn.model.PatientLabRouting;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.lab.FileUploadCheck;
import ca.openosp.openo.lab.ca.all.upload.HandlerClassFactory;
import ca.openosp.openo.lab.ca.all.upload.handlers.OLISHL7Handler;
import ca.openosp.openo.lab.ca.on.CommonLabResultData;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for adding OLIS (Ontario Laboratories Information System) lab results to provider inbox.
 *
 * <p>This action handles the import of HL7 formatted laboratory results from OLIS into the EMR system.
 * It processes temporary lab result files stored on the server, validates them for duplicates,
 * and optionally files them to a patient chart or acknowledges their receipt. The action follows
 * the 2Action pattern for Struts2 migration with security and audit logging built in.</p>
 *
 * <p>The workflow involves:
 * <ol>
 *   <li>Retrieving a temporary OLIS response file by UUID</li>
 *   <li>Checking for duplicate uploads to prevent redundant data</li>
 *   <li>Parsing the HL7 message and storing it in the database</li>
 *   <li>Optionally filing the lab to a patient demographic</li>
 *   <li>Optionally acknowledging the lab result</li>
 * </ol>
 * </p>
 *
 * @since 2012-01-01
 * @see OLISHL7Handler
 * @see CommonLabResultData
 */
public class OLISAddToInbox2Action extends ActionSupport {
    /**
     * HTTP response object for sending responses back to the client.
     * Currently not actively used but maintained for potential future enhancements.
     */
    HttpServletResponse response = ServletActionContext.getResponse();

    // UUID validation pattern to prevent path injection
    private static final Pattern UUID_PATTERN = Pattern.compile("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");

    /**
     * Logger instance for recording errors and debugging information.
     * Utilizes the centralized logging configuration from MiscUtils.
     */
    static Logger logger = MiscUtils.getLogger();

    /**
     * Processes the OLIS lab result and adds it to the provider's inbox.
     *
     * <p>This method handles the complete workflow of importing an OLIS lab result:
     * <ul>
     *   <li>Retrieves the lab file from temporary storage using the provided UUID</li>
     *   <li>Validates that the file hasn't been previously uploaded (duplicate check)</li>
     *   <li>Parses the HL7 message content and persists it to the database</li>
     *   <li>Optionally files the lab to the patient's chart if requested</li>
     *   <li>Optionally marks the lab as acknowledged with audit logging</li>
     * </ul>
     * </p>
     *
     * <p>Request parameters:
     * <ul>
     *   <li><b>uuid</b> - Unique identifier for the temporary OLIS response file</li>
     *   <li><b>file</b> - "true" to automatically file the lab to the patient's chart</li>
     *   <li><b>ack</b> - "true" to acknowledge the lab result with audit logging</li>
     * </ul>
     * </p>
     *
     * <p>Request attributes set on completion:
     * <ul>
     *   <li><b>result</b> - "Success", "Error", or "Already Added" indicating the operation outcome</li>
     * </ul>
     * </p>
     *
     * @return String "ajax" - Always returns "ajax" to indicate AJAX response handling
     * @throws SecurityException if the user lacks required privileges (handled internally)
     */
    @Override
    public String execute() {
        // Retrieve logged-in provider information from the session for security and audit purposes
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();

        // Extract request parameters for lab processing
        String uuidToAdd = request.getParameter("uuid");
        String pFile = request.getParameter("file");
        String pAck = request.getParameter("ack");

        // Parse boolean flags for filing and acknowledgment operations
        boolean doFile = false, doAck = false;
        if (pFile != null && pFile.equals("true")) {
            doFile = true;
        }
        if (pAck != null && pAck.equals("true")) {
            doAck = true;
        }

        // Validate UUID to prevent path injection attacks
        if (uuidToAdd == null || !UUID_PATTERN.matcher(uuidToAdd).matches()) {
            logger.error("Invalid UUID provided: " + uuidToAdd);
            request.setAttribute("result", "Error");
            return "ajax";
        }

        // Use secure file path construction with FilenameUtils
        String tmpDir = System.getProperty("java.io.tmpdir");
        String fileName = "olis_" + uuidToAdd + ".response";
        File tempDirectory = new File(tmpDir);
        File file = new File(tempDirectory, FilenameUtils.getName(fileName));
        
        // Ensure the file is within the temp directory (canonical path check)
        String fileLocation;
        try {
            fileLocation = file.getCanonicalPath();
            String canonicalTmpDir = tempDirectory.getCanonicalPath();
            if (!fileLocation.startsWith(canonicalTmpDir + File.separator)) {
                logger.error("Attempted path traversal detected for UUID: " + uuidToAdd);
                request.setAttribute("result", "Error");
                return "ajax";
            }
        } catch (IOException e) {
            logger.error("Error validating file path for UUID: " + uuidToAdd, e);
            request.setAttribute("result", "Error");
            return "ajax";
        }
        
        OLISHL7Handler msgHandler = (OLISHL7Handler) HandlerClassFactory.getHandler("OLIS_HL7");

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            int check = FileUploadCheck.addFile(file.getName(), is, providerNo);

            if (check != FileUploadCheck.UNSUCCESSFUL_SAVE) {
                // Parse the HL7 message and store it in the database
                // The parser returns null if parsing fails
                if (msgHandler.parse(loggedInInfo, "OLIS_HL7", fileLocation, check, true) != null) {
                    request.setAttribute("result", "Success");

                    // File the lab to the patient's chart if requested
                    if (doFile) {
                        // Create a list structure required by the filing method
                        // Format: [segment_id, lab_type] where lab_type is always "HL7" for OLIS
                        ArrayList<String[]> labsToFile = new ArrayList<String[]>();
                        String item[] = new String[]{String.valueOf(msgHandler.getLastSegmentId()), "HL7"};
                        labsToFile.add(item);

                        // Perform the actual filing operation
                        CommonLabResultData.fileLabs(labsToFile, providerNo);
                    }

                    // Acknowledge the lab result if requested
                    if (doAck) {
                        // Retrieve the patient's demographic ID for audit logging
                        String demographicID = getDemographicIdFromLab("HL7", msgHandler.getLastSegmentId());

                        // Log the acknowledgment action for audit compliance
                        LogAction.addLog((String) request.getSession().getAttribute("user"), LogConst.ACK, LogConst.CON_HL7_LAB, "" + msgHandler.getLastSegmentId(), request.getRemoteAddr(), demographicID);

                        // Update the lab status to 'A' (Acknowledged) in the database
                        CommonLabResultData.updateReportStatus(msgHandler.getLastSegmentId(), providerNo, 'A', "comment", "HL7");
                    }
                } else {
                    // Parsing failed - likely due to invalid HL7 format
                    request.setAttribute("result", "Error");
                }
            } else {
                // File was already previously uploaded
                request.setAttribute("result", "Already Added");
            }

        } catch (Exception e) {
            // Log any unexpected errors during lab processing
            MiscUtils.getLogger().error("Couldn't add requested OLIS lab to Inbox.", e);
            request.setAttribute("result", "Error");
        } finally {
            // Ensure the input stream is properly closed to prevent resource leaks
            try {
                is.close();
            } catch (IOException e) {
                // Safe to ignore IOException on close as we're already in finally block
            }
        }

        return "ajax";
    }

    /**
     * Retrieves the demographic (patient) ID associated with a specific lab result.
     *
     * <p>This utility method queries the patient-lab routing table to find which patient
     * a lab result is associated with. The routing table maintains the many-to-many relationship
     * between lab results and patient demographics, allowing a single lab to be associated
     * with multiple patients (e.g., for infectious disease reporting) or vice versa.</p>
     *
     * <p>The method is designed to handle cases where no routing exists gracefully by
     * returning an empty string rather than null, preventing NullPointerExceptions in
     * calling code that expects a string value for logging or display purposes.</p>
     *
     * @param labType String the type of lab result (e.g., "HL7", "CML", "MDS")
     * @param labNo int the unique identifier of the lab result within its type category
     * @return String the demographic number as a string, or empty string if no routing exists
     * @see PatientLabRouting
     * @see PatientLabRoutingDao
     */
    private static String getDemographicIdFromLab(String labType, int labNo) {
        // Obtain the DAO from Spring context for database operations
        PatientLabRoutingDao dao = SpringUtils.getBean(PatientLabRoutingDao.class);

        // Query for the patient-lab routing record
        PatientLabRouting routing = dao.findDemographics(labType, labNo);

        // Return empty string if no routing exists, otherwise return the demographic number
        // Empty string is preferred over null for safer string operations downstream
        return routing == null ? "" : String.valueOf(routing.getDemographicNo());
    }
}
