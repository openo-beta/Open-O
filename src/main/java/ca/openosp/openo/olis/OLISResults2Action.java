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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.Misc;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.OLISResultsDao;
import ca.openosp.openo.commn.model.OLISResults;
import ca.openosp.openo.commn.model.OscarLog;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.olis1.Driver;
import ca.openosp.openo.olis1.queries.Query;
import ca.openosp.openo.olis1.queries.QueryType;
import ca.openosp.openo.olis1.queries.Z01Query;

import ca.openosp.OscarProperties;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.lab.ca.all.parsers.Factory;
import ca.openosp.openo.lab.ca.all.parsers.MessageHandler;
import ca.openosp.openo.lab.ca.all.parsers.OLISHL7Handler;
import ca.openosp.openo.lab.ca.all.upload.MessageUploader;
import ca.openosp.openo.lab.ca.all.util.Utilities;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for processing and displaying OLIS query results.
 *
 * <p>This action handles the processing of laboratory results returned from OLIS queries,
 * including parsing HL7 messages, duplicate detection, patient matching, and result storage.
 * It supports both live OLIS responses and simulation mode for testing. The action implements
 * sophisticated duplicate detection to prevent redundant lab results from being imported.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Processes HL7 messages from OLIS query responses</li>
 *   <li>Performs multi-level duplicate detection (OLIS and community labs)</li>
 *   <li>Automatic patient demographic matching based on identifiers</li>
 *   <li>Stores unique results in the database for later retrieval</li>
 *   <li>Maintains audit logs for rejected duplicate results</li>
 *   <li>Supports simulation mode for testing without OLIS connection</li>
 * </ul>
 * </p>
 *
 * <p>The duplicate detection algorithm checks:
 * <ol>
 *   <li>Whether the result already exists in the OLIS results table</li>
 *   <li>Whether the same lab exists from community lab uploads</li>
 *   <li>Provider-specific and accession number-based uniqueness</li>
 * </ol>
 * </p>
 *
 * <p>This action requires laboratory read privileges (_lab) to execute and maintains
 * detailed audit logs for compliance and troubleshooting purposes.</p>
 *
 * @since 2012-01-01
 * @see OLISUtils
 * @see OLISHL7Handler
 * @see OLISResults
 */
public class OLISResults2Action extends ActionSupport {
    /**
     * HTTP request object for retrieving OLIS response content and parameters.
     */
    HttpServletRequest request = ServletActionContext.getRequest();

    /**
     * HTTP response object for sending responses.
     * Currently unused but maintained for potential future enhancements.
     */
    HttpServletResponse response = ServletActionContext.getResponse();

    /**
     * Static cache of parsed HL7 handlers mapped by UUID.
     * Allows efficient access to parsed lab results without re-parsing.
     * Shared across all instances for the duration of the application session.
     */
    public static HashMap<String, OLISHL7Handler> searchResultsMap = new HashMap<String, OLISHL7Handler>();
    /**
     * Security manager for validating laboratory access privileges.
     * Ensures only authorized users can view OLIS results.
     */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * DAO for managing OLIS result storage and retrieval.
     * Handles persistence of unique lab results from OLIS queries.
     */
    protected OLISResultsDao olisResultsDao = SpringUtils.getBean(OLISResultsDao.class);

    /**
     * DAO for accessing provider information.
     * Used for logging and audit trail purposes.
     */
    protected ProviderDao providerDao = SpringUtils.getBean(ProviderDao.class);

    /**
     * Processes OLIS query results and prepares them for display.
     *
     * <p>This method performs the complete workflow for handling OLIS results:
     * <ol>
     *   <li>Validates user has laboratory read privileges</li>
     *   <li>Retrieves OLIS response content from request or session</li>
     *   <li>Handles simulation mode for testing environments</li>
     *   <li>Separates multiple HL7 messages from the response</li>
     *   <li>Performs duplicate detection at multiple levels</li>
     *   <li>Matches results to patient demographics</li>
     *   <li>Stores unique results in the database</li>
     *   <li>Prepares result list for display</li>
     * </ol>
     * </p>
     *
     * <p>Request parameters/attributes:
     * <ul>
     *   <li><b>olisResponseContent</b> - Raw HL7 response content from OLIS</li>
     *   <li><b>olisXmlResponse</b> - XML-wrapped response for parsing</li>
     *   <li><b>olisResponseQuery</b> - Original query object from session</li>
     * </ul>
     * </p>
     *
     * <p>Request attributes set:
     * <ul>
     *   <li><b>resultList</b> - List of UUIDs for parsed results</li>
     * </ul>
     * </p>
     *
     * @return String "results" - forwards to results display page
     * @throws SecurityException if user lacks laboratory read privileges
     */
    @Override
    public String execute() {
        // Validate laboratory access privileges
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_lab", "r", null)) {
            throw new SecurityException("missing required sec object (_lab)");
        }

        try {
            // Retrieve OLIS response content from request or session
            String olisResultString = (String) request.getAttribute("olisResponseContent");
            if (olisResultString != null) {
                olisResultString = (String) request.getSession().getAttribute("olisResponseContent");
            }
            // Handle non-simulation mode (production environment)
            if (olisResultString == null && "no".equals(OscarProperties.getInstance().getProperty("olis_simulate", "no"))) {
                olisResultString = Misc.getStr(request.getParameter("olisResponseContent"), "");
                request.setAttribute("olisResponseContent", olisResultString);

                // Process XML-wrapped response if no direct HL7 content
                String olisXmlResponse = Misc.getStr(request.getParameter("olisXmlResponse"), "");
                if (olisResultString.trim().equalsIgnoreCase("")) {
                    if (!olisXmlResponse.trim().equalsIgnoreCase("")) {
                        // Parse XML response and extract HL7 content
                        Driver.readResponseFromXML(LoggedInInfo.getLoggedInInfoFromSession(request), request, olisXmlResponse);
                    }

                    // Return empty result list if no content to process
                    List<String> resultList = new LinkedList<String>();
                    request.setAttribute("resultList", resultList);
                    return "results";
                }
            }

            // Generate unique identifier for this response batch
            UUID uuid = UUID.randomUUID();

            // Retrieve original query object for context
            Query query = (Query) request.getSession().getAttribute("olisResponseQuery");

            // Write response to temporary file for processing
            File tempFile = new File(System.getProperty("java.io.tmpdir") + "/olis_" + uuid.toString() + ".response");
            FileUtils.writeStringToFile(tempFile, olisResultString);


            // Separate multiple HL7 messages from the response
            // OLIS may return multiple lab results in a single response
            @SuppressWarnings("unchecked")
            ArrayList<String> messages = Utilities.separateMessages(System.getProperty("java.io.tmpdir") + "/olis_" + uuid.toString() + ".response");

            List<String> resultList = new LinkedList<String>();

            // Process each HL7 message individually
            if (messages != null) {
                for (String message : messages) {

                    // Skip empty messages
                    if (StringUtils.isEmpty(message)) {
                        continue;
                    }

                    // Generate unique identifier for this individual result
                    String resultUuid = UUID.randomUUID().toString();

                    // Save individual message to file
                    tempFile = new File(System.getProperty("java.io.tmpdir") + "/olis_" + resultUuid.toString() + ".response");
                    FileUtils.writeStringToFile(tempFile, message);

                    // Parse HL7 message to extract metadata
                    MessageHandler h = Factory.getHandler("OLIS_HL7", message);


                    // First level duplicate check - OLIS results database
                    // Check if this exact result already exists for this provider
                    if (!olisResultsDao.hasExistingResult(query.getRequestingHICProviderNo() != null ? query.getRequestingHICProviderNo() : LoggedInInfo.getLoggedInInfoFromSession(request).getLoggedInProviderNo(), query.getQueryType().toString(), h.getAccessionNum())) {

                        // Second level duplicate check - community lab uploads
                        // Check if this lab already exists from direct lab uploads
                        boolean dup2 = OLISUtils.isDuplicate(LoggedInInfo.getLoggedInInfoFromSession(request), new File(System.getProperty("java.io.tmpdir") + "/olis_" + resultUuid + ".response"));
                        if (!dup2) {
                            // Not a duplicate - create new result record
                            OLISResults result = new OLISResults();
                            result.setHash(h.getAccessionNum());  // Use accession number as unique identifier
                            result.setProviderNo(LoggedInInfo.getLoggedInInfoFromSession(request).getLoggedInProviderNo());
                            result.setQuery(query.getQueryHL7String());  // Store original query for audit
                            result.setQueryType(query.getQueryType().toString());
                            result.setResults(message);  // Store complete HL7 message
                            result.setUuid(resultUuid);
                            // Set requesting provider (may differ from logged-in provider)
                            if (query.getRequestingHICProviderNo() != null)
                                result.setRequestingHICProviderNo(query.getRequestingHICProviderNo());
                            else
                                result.setRequestingHICProviderNo(result.getProviderNo());


                            // Attempt to match lab to existing patient demographic
                            // Uses name, sex, DOB, and health number for matching
                            Integer demId = MessageUploader.willOLISLabReportMatch(LoggedInInfo.getLoggedInInfoFromSession(request), h.getLastName(), h.getFirstName(), h.getSex(), h.getDOB(), h.getHealthNum());
                            if (demId != null) {
                                result.setDemographicNo(demId);
                            }
                            result.setQueryUuid(query.getUuid());  // Link to original query
                            olisResultsDao.persist(result);
                        } else {
                            // Duplicate from community lab already in system
                            // Log for audit but don't import
                            logOLISDuplicate(LoggedInInfo.getLoggedInInfoFromSession(request), query, message, uuid.toString());
                        }
                    } else {
                        // Duplicate from previous OLIS query - auto-reject
                        logOLISDuplicate(LoggedInInfo.getLoggedInInfoFromSession(request), query, message, uuid.toString());
                    }

                }

                // Build result list from database for display
                // Includes all non-duplicate results for this provider and query type
                List<OLISResults> results = olisResultsDao.getResultList(query.getRequestingHICProviderNo() != null ? query.getRequestingHICProviderNo() : LoggedInInfo.getLoggedInInfoFromSession(request).getLoggedInProviderNo(), query.getQueryType().toString());
                for (OLISResults result : results) {
                    // Parse and cache each result for efficient display
                    MessageHandler h = Factory.getHandler("OLIS_HL7", result.getResults());
                    searchResultsMap.put(result.getUuid(), (OLISHL7Handler) h);
                    resultList.add(result.getUuid());
                }

                // Clear session and set result list for display
                request.getSession().setAttribute("olisResponseContent", null);
                request.setAttribute("resultList", resultList);
            }

        } catch (Exception e) {
            // Log error but still return to results page
            MiscUtils.getLogger().error("Can't pull out messages from OLIS response.", e);
        }
        return "results";
    }

    /**
     * Logs detailed information about duplicate lab results for audit purposes.
     *
     * <p>This method creates comprehensive audit logs when duplicate lab results are detected
     * and rejected by the system. The log includes query details, provider information,
     * lab metadata, and the reason for rejection. This is critical for compliance and
     * troubleshooting duplicate detection issues.</p>
     *
     * <p>The log entry includes:
     * <ul>
     *   <li>Query execution date and type</li>
     *   <li>Requesting HIC and initiating provider details</li>
     *   <li>Automatic rejection timestamp and reason</li>
     *   <li>Lab accession number and test details</li>
     *   <li>Collection and update dates</li>
     *   <li>Patient demographic linkage if applicable</li>
     * </ul>
     * </p>
     *
     * @param loggedInInfo LoggedInInfo the current user's authentication context
     * @param query Query the original OLIS query that returned the duplicate
     * @param message String the HL7 message content of the duplicate lab
     * @param resultUuid String the UUID assigned to this result for tracking
     */
    public void logOLISDuplicate(LoggedInInfo loggedInInfo, Query query, String message, String resultUuid) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Build detailed audit log entry
        StringBuilder data = new StringBuilder();
        data.append("Query Date:" + formatter.format(query.getQueryExecutionDate()) + "\n");
        data.append("Query Type:" + query.getQueryType().toString() + "\n");

        // Include requesting Healthcare Information Custodian details
        if (!StringUtils.isEmpty(query.getRequestingHICProviderNo())) {
            Provider reqHic = providerDao.getProviderByPractitionerNo(query.getRequestingHICProviderNo());
            if (reqHic != null) {
                data.append("Requesting HIC:" + reqHic.getFormattedName() + "\n");
            }
        }
        // Record provider and rejection details
        data.append("Initiating Provider: " + providerDao.getProvider(query.getInitiatingProviderNo()).getFormattedName() + "\n");
        data.append("Rejecting User: System (automatic)" + "\n");
        data.append("Rejection Date: " + formatter.format(new Date()) + "\n");
        data.append("Rejection Reason: Duplicate\n");
        data.append("Rejection Type: System\n");


        // Extract and log lab-specific details
        OLISHL7Handler h = (OLISHL7Handler) Factory.getHandler("OLIS_HL7", message);
        if (h != null) {
            data.append("Accession: " + h.getAccessionNum() + "\n");
            data.append("Test Request(s): " + h.getTestList(",") + "\n");

            // Log collection dates for all test segments
            for (int x = 0; x < h.getOBRCount(); x++) {
                data.append("Collection Date:" + h.getTimeStamp(x, 1) + "\n");
            }

            // Log last update dates for tracking
            for (int x = 0; x < h.getOBRCount(); x++) {
                data.append("LastUpdate Date:" + h.getLastUpdateDate(x, 1) + "\n");
            }
        }

        // Create audit log entry
        OscarLog oscarLog = new OscarLog();
        oscarLog.setAction("OLIS");
        oscarLog.setContent("DUPLICATE (OLIS)");
        oscarLog.setContentId(resultUuid);
        oscarLog.setProviderNo(loggedInInfo.getLoggedInProviderNo());
        oscarLog.setData(data.toString());

        // Link to patient demographic for Z01 (patient-specific) queries
        if (query.getQueryType() == QueryType.Z01) {
            String demographicNo = ((Z01Query) query).getDemographicNo();
            if (!StringUtils.isEmpty(demographicNo)) {
                oscarLog.setDemographicId(Integer.parseInt(demographicNo));
            }
        }

        // Persist log entry synchronously for immediate audit trail
        LogAction.addLogSynchronous(oscarLog);

    }
}
