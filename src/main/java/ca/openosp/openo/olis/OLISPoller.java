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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.openosp.Misc;
import ca.openosp.openo.lab.ca.all.upload.HandlerClassFactory;
import ca.openosp.openo.lab.ca.all.upload.handlers.MessageHandler;
import org.apache.commons.io.FileUtils;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.UserPropertyDAO;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.UserProperty;
import ca.openosp.openo.olis.dao.OLISProviderPreferencesDao;
import ca.openosp.openo.olis.dao.OLISSystemPreferencesDao;
import ca.openosp.openo.olis.model.OLISProviderPreferences;
import ca.openosp.openo.olis.model.OLISSystemPreferences;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.lab.FileUploadCheck;
import ca.openosp.openo.lab.ca.all.parsers.Factory;
import ca.openosp.openo.lab.ca.all.parsers.OLISHL7Handler;
import ca.openosp.openo.lab.ca.all.util.Utilities;

import ca.openosp.openo.olis1.Driver;
import ca.openosp.openo.olis1.parameters.OBR22;
import ca.openosp.openo.olis1.parameters.ORC21;
import ca.openosp.openo.olis1.parameters.ZRP1;
import ca.openosp.openo.olis1.queries.Z04Query;
import ca.openosp.openo.olis1.queries.Z06Query;

/**
 * Automated polling service for retrieving laboratory results from OLIS (Ontario Laboratories Information System).
 *
 * <p>This class implements the core polling mechanism for fetching new lab results from OLIS using HL7 messaging.
 * It performs automated queries on behalf of individual providers and facilities, processes the returned HL7 messages,
 * and imports them into the EMR system. The poller maintains state to ensure incremental fetching of new results
 * since the last successful poll.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Provider-specific polling using Z04 queries with practitioner credentials</li>
 *   <li>Facility-wide polling using Z06 queries for organization-level results</li>
 *   <li>Automatic tracking of last fetch timestamps to prevent duplicate retrievals</li>
 *   <li>Parsing and separation of multiple HL7 messages from bulk responses</li>
 *   <li>Integration with the lab upload framework for consistent processing</li>
 * </ul>
 * </p>
 *
 * <p>The polling process uses the OLIS Driver API to submit queries and handle responses in the
 * SSHA (Smart Systems for Health Agency) XML format. Results are temporarily stored as files
 * for processing through the standard lab import pipeline.</p>
 *
 * @since 2012-01-01
 * @see Driver
 * @see Z04Query
 * @see Z06Query
 * @see OLISHL7Handler
 */
public class OLISPoller {
    /**
     * Logger instance for tracking polling operations and debugging issues.
     * Uses the centralized MiscUtils logger configuration.
     */
    private static final Logger logger = MiscUtils.getLogger();

    /**
     * Default constructor for the OLIS poller.
     * Initializes a new instance ready for polling operations.
     */
    public OLISPoller() {
        super();
    }

    /**
     * Initiates automatic fetching of new OLIS lab results for all active providers and the facility.
     *
     * <p>This method performs a comprehensive poll of OLIS for new lab results by:
     * <ol>
     *   <li>Iterating through all active providers and querying for their specific results</li>
     *   <li>Performing a facility-wide query for results not assigned to specific providers</li>
     *   <li>Tracking the last successful fetch timestamp for each provider/facility</li>
     *   <li>Processing and importing all retrieved results into the EMR</li>
     * </ol>
     * </p>
     *
     * <p>The method uses stored preferences to determine the time range for queries, defaulting to
     * system-wide settings if provider-specific preferences don't exist. Each successful poll updates
     * the start time for the next incremental fetch.</p>
     *
     * <p>Error handling ensures that failures for individual providers don't stop the entire
     * polling process, allowing other providers to still receive their results.</p>
     *
     * @param loggedInInfo LoggedInInfo the logged-in user context containing authentication and facility information
     * @throws RuntimeException if critical system components cannot be accessed
     */
    public static void startAutoFetch(LoggedInInfo loggedInInfo) {
        OLISPoller olisPoller = new OLISPoller();
        logger.info("===== OLIS FETCH INITIATED ...");

        // Date format used by OLIS for timestamp parameters (YYYYMMDDHHmmssZ)
        String[] dateFormat = new String[]{"yyyyMMddHHmmssZ"};

        // Retrieve all active providers who may have OLIS results
        ProviderDao providerDao = (ProviderDao) SpringUtils.getBean(ProviderDao.class);
        List<Provider> allProvidersList = providerDao.getActiveProviders();

        // Load system-wide OLIS polling preferences for default time ranges
        OLISSystemPreferencesDao olisSystemPreferencesDao = (OLISSystemPreferencesDao) SpringUtils.getBean(OLISSystemPreferencesDao.class);
        OLISSystemPreferences olisSystemPreferences = olisSystemPreferencesDao.getPreferences();

        // DAO for managing provider-specific polling preferences
        OLISProviderPreferencesDao olisProviderPreferencesDao = (OLISProviderPreferencesDao) SpringUtils.getBean(OLISProviderPreferencesDao.class);
        OLISProviderPreferences olisProviderPreferences;

        // Extract default time range from system preferences
        // These are used when provider-specific preferences don't exist
        String defaultStartTime = Misc.getStr(olisSystemPreferences.getStartTime(), "").trim();
        String defaultEndTime = Misc.getStr(olisSystemPreferences.getEndTime(), "").trim();

        // Process each active provider individually
        Z04Query providerQuery;
        UserPropertyDAO userPropertyDAO = (UserPropertyDAO) SpringUtils.getBean(UserPropertyDAO.class);
        for (Provider provider : allProvidersList) {
            try {
                // Create a new Z04 query for provider-specific lab results
                providerQuery = new Z04Query();
                // Retrieve or initialize provider-specific polling preferences
                olisProviderPreferences = olisProviderPreferencesDao.findById(provider.getProviderNo());

                // Create OBR22 segment for specifying the time range of the query
                // OBR22 contains the ResultsRptStatusChngDateTime field
                OBR22 obr22 = new OBR22();
                List<Date> dateList = new LinkedList<Date>();
                if (olisProviderPreferences != null) {
                    // Use provider's last fetch time, or default if not set
                    if (olisProviderPreferences.getStartTime() == null) {
                        olisProviderPreferences.setStartTime(defaultStartTime);
                    }
                    obr22.setValue(DateUtils.parseDate(olisProviderPreferences.getStartTime(), dateFormat));
                } else {
                    // First time polling for this provider - use system defaults
                    if (defaultEndTime.equals("")) {
                        // Single timestamp - fetch all results from this time forward
                        obr22.setValue(DateUtils.parseDate(defaultStartTime, dateFormat));
                    } else {
                        // Time range - fetch results between start and end times
                        dateList.add(DateUtils.parseDate(defaultStartTime, dateFormat));
                        dateList.add(DateUtils.parseDate(defaultEndTime, dateFormat));
                        obr22.setValue(dateList);
                    }

                    // Initialize preferences for first-time provider
                    olisProviderPreferences = new OLISProviderPreferences();
                    olisProviderPreferences.setProviderId(provider.getProviderNo());
                }
                providerQuery.setStartEndTimestamp(obr22);

                // Configure Healthcare Information Custodian (HIC) identification for the query
                // Uses official provider credentials stored in user properties
                ZRP1 zrp1 = new ZRP1(
                        provider.getPractitionerNo(),
                        userPropertyDAO.getStringValue(provider.getProviderNo(), UserProperty.OFFICIAL_OLIS_IDTYPE),
                        "ON",  // Province code for Ontario
                        "HL70347",  // HL7 identifier type code
                        userPropertyDAO.getStringValue(provider.getProviderNo(), UserProperty.OFFICIAL_LAST_NAME),
                        userPropertyDAO.getStringValue(provider.getProviderNo(), UserProperty.OFFICIAL_FIRST_NAME),
                        userPropertyDAO.getStringValue(provider.getProviderNo(), UserProperty.OFFICIAL_SECOND_NAME)
                );
                providerQuery.setRequestingHic(zrp1);
                // Submit the query to OLIS and receive the response
                String response = Driver.submitOLISQuery(loggedInInfo, null, providerQuery);

                // Validate response format - must be properly wrapped SSHA XML
                if (!response.matches("<Request xmlns=\"http://www.ssha.ca/2005/HIAL\"><Content><![CDATA[.*]]></Content></Request>")) {
                    // Invalid response format - skip to next provider
                    break;
                }
                // Parse the response to extract individual lab result messages
                List<String> resultsList = olisPoller.parsePollResults(response);
                if (resultsList.size() == 0) {
                    // No new results for this provider - continue to next
                    continue;
                }

                // Import all retrieved results into the EMR system
                olisPoller.importResults(loggedInInfo, resultsList);

                // Extract the timestamp of the last result for incremental polling
                // Pattern matches: @OBR.22^YYYYMMDDHHMMSS-ZZZZ~
                Pattern p = Pattern.compile("@OBR\\.22\\^(\\d{14}-\\d{4})~");
                Matcher matcher = p.matcher(response);
                if (matcher.find()) {
                    // Update start time for next poll to avoid duplicate retrieval
                    olisProviderPreferences.setStartTime(matcher.group(1));
                }
                // Persist the updated polling preferences
                if (olisProviderPreferences.getId() != null) {
                    // Update existing preferences
                    olisProviderPreferencesDao.merge(olisProviderPreferences);
                } else {
                    // Create new preferences record
                    olisProviderPreferencesDao.persist(olisProviderPreferences);
                }
            } catch (Exception e) {
                // Log error but continue processing other providers
                logger.error("Error polling OLIS for provider " + provider.getProviderNo(), e);
            }
        }

        // Process facility-wide query for results not assigned to specific providers
        try {
            // Z06 query is used for facility/organization level polling
            Z06Query facilityQuery = new Z06Query();

            // Use provider ID "-1" as a convention for facility-wide preferences
            olisProviderPreferences = olisProviderPreferencesDao.findById("-1");
            // Configure time range for facility query (same logic as provider queries)
            OBR22 obr22 = new OBR22();
            List<Date> dateList = new LinkedList<Date>();
            if (olisProviderPreferences != null) {
                if (olisProviderPreferences.getStartTime() == null) {
                    olisProviderPreferences.setStartTime(defaultStartTime);
                }
                obr22.setValue(DateUtils.parseDate(olisProviderPreferences.getStartTime(), dateFormat));
            } else {
                if (defaultEndTime.equals("")) {
                    obr22.setValue(DateUtils.parseDate(defaultStartTime, dateFormat));
                } else {
                    dateList.add(DateUtils.parseDate(defaultStartTime, dateFormat));
                    dateList.add(DateUtils.parseDate(defaultEndTime, dateFormat));
                    obr22.setValue(dateList);
                }

                olisProviderPreferences = new OLISProviderPreferences();
                olisProviderPreferences.setProviderId("-1");
            }
            facilityQuery.setStartEndTimestamp(obr22);

            // Configure ordering facility identification using X500 distinguished name
            // This identifies the EMR system to OLIS for facility-level access
            ORC21 orc21 = new ORC21();
            orc21.setValue(6, 2, "CN=EMR.MCMUN2.CST,OU=Applications,OU=eHealthUsers,OU=Subscribers,DC=subscribers,DC=ssh");
            orc21.setValue(6, 3, "X500");  // X500 directory service identifier type
            facilityQuery.setOrderingFacilityId(orc21);

            // Submit facility query to OLIS
            String response = Driver.submitOLISQuery(loggedInInfo, null, facilityQuery);

            // Validate response format
            if (!response.matches("<Request xmlns=\"http://www.ssha.ca/2005/HIAL\"><Content><![CDATA[.*]]></Content></Request>")) {
                // Invalid response - exit polling
                return;
            }
            List<String> resultsList = olisPoller.parsePollResults(response);
            if (resultsList.size() == 0) {
                return;
            }
            olisPoller.importResults(loggedInInfo, resultsList);

            Pattern p = Pattern.compile("@OBR\\.22\\^(\\d{14}-\\d{4})~");
            Matcher matcher = p.matcher(response);
            if (matcher.find()) {
                olisProviderPreferences.setStartTime(matcher.group(1));
            }
            if (olisProviderPreferences.getId() != null) {
                olisProviderPreferencesDao.merge(olisProviderPreferences);
            } else {
                olisProviderPreferencesDao.persist(olisProviderPreferences);
            }
        } catch (Exception e) {
            // Log facility polling errors but don't throw - polling should be resilient
            logger.error("Error polling OLIS for facility", e);
        }
    }

    /**
     * Temporary storage map for parsed HL7 handlers during polling operations.
     * Maps UUID strings to their corresponding parsed OLISHL7Handler instances.
     * This allows results to be parsed once and referenced multiple times during import.
     */
    public static HashMap<String, OLISHL7Handler> pollResultsMap = new HashMap<String, OLISHL7Handler>();

    /**
     * Parses the OLIS response to extract individual HL7 messages.
     *
     * <p>The OLIS response may contain multiple HL7 messages concatenated together.
     * This method separates them into individual messages, parses each one, and
     * stores the parsed handlers for later import processing.</p>
     *
     * <p>Each message is assigned a unique UUID for tracking through the import
     * pipeline. The parsed handlers are cached in pollResultsMap to avoid
     * re-parsing during import.</p>
     *
     * @param response String the raw OLIS response containing one or more HL7 messages
     * @return List<String> list of UUIDs corresponding to parsed messages, or null if parsing fails
     */
    private List<String> parsePollResults(String response) {
        try {
            // Generate unique identifier for this polling response
            UUID uuid = UUID.randomUUID();

            // Write response to temporary file for processing
            File tempFile = new File(System.getProperty("java.io.tmpdir") + "/olis_" + uuid.toString() + ".response");
            FileUtils.writeStringToFile(tempFile, response);

            // Separate concatenated HL7 messages into individual messages
            @SuppressWarnings("unchecked")
            ArrayList<String> messages = Utilities.separateMessages(System.getProperty("java.io.tmpdir") + "/olis_" + uuid.toString() + ".response");

            List<String> resultList = new LinkedList<String>();

            // Process each separated message if valid messages exist
            if (messages != null && !(messages.size() == 1 && messages.get(0).trim().equals(""))) {
                for (String message : messages) {

                    logger.info("message:" + message);

                    // Parse the HL7 message using the appropriate handler
                    ca.openosp.openo.lab.ca.all.parsers.MessageHandler h = Factory.getHandler("OLIS_HL7", message);

                    // Generate unique identifier for this individual message
                    String resultUuid = UUID.randomUUID().toString();

                    // Cache the parsed handler for use during import
                    pollResultsMap.put(resultUuid, (OLISHL7Handler) h);
                    resultList.add(resultUuid);
                }
                return resultList;
            }

        } catch (Exception e) {
            // Log parsing errors and return null to indicate failure
            MiscUtils.getLogger().error("Can't pull out messages from OLIS response.", e);
        }
        return null;
    }

    /**
     * Imports parsed OLIS lab results into the EMR system.
     *
     * <p>This method processes each lab result identified by UUID, performing:
     * <ul>
     *   <li>Duplicate checking to prevent re-importing existing labs</li>
     *   <li>File-based processing through the standard lab upload pipeline</li>
     *   <li>Parsing and storage in the database</li>
     *   <li>Logging of success/failure for each lab</li>
     * </ul>
     * </p>
     *
     * <p>The import uses provider "0" to indicate system-imported labs rather than
     * manually uploaded ones. Each lab is processed independently so failures
     * don't affect other labs in the batch.</p>
     *
     * @param loggedInInfo LoggedInInfo the authentication context for the import operation
     * @param resultList List<String> list of UUIDs identifying labs to import
     */
    private void importResults(LoggedInInfo loggedInInfo, List<String> resultList) {
        for (String uuidToAdd : resultList) {

            // Construct path to temporary file containing the lab message
            String fileLocation = System.getProperty("java.io.tmpdir") + "/olis_" + uuidToAdd + ".response";
            File file = new File(fileLocation);

            // Get appropriate message handler for OLIS HL7 format
            MessageHandler msgHandler = HandlerClassFactory.getHandler("OLIS_HL7");

            try {
                InputStream is = new FileInputStream(fileLocation);

                // Check for duplicate uploads (provider "0" indicates system import)
                int check = FileUploadCheck.addFile(file.getName(), is, "0");

                if (check != FileUploadCheck.UNSUCCESSFUL_SAVE) {
                    // Not a duplicate - proceed with parsing and storage
                    if (msgHandler.parse(loggedInInfo, "OLIS_HL7", fileLocation, check, null) != null) {
                        logger.info("Lab successfully added.");
                    } else {
                        logger.info("Error adding lab.");
                    }
                } else {
                    // Lab already exists in the system
                    logger.info("Lab already in system.");
                }
                is.close();

            } catch (Exception e) {
                // Log import errors but continue processing remaining labs
                MiscUtils.getLogger().error("Couldn't add requested OLIS lab to Inbox.", e);
            }
        }
    }
}
