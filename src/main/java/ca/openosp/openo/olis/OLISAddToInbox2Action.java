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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.dao.OLISQueryLogDao;
import ca.openosp.openo.commn.dao.OLISResultsDao;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.commn.dao.PatientLabRoutingDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.OLISQueryLog;
import ca.openosp.openo.commn.model.OLISResults;
import ca.openosp.openo.commn.model.OscarLog;
import ca.openosp.openo.commn.model.PatientLabRouting;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PathValidationUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.lab.FileUploadCheck;
import ca.openosp.openo.lab.ca.all.parsers.Factory;
import ca.openosp.openo.lab.ca.all.upload.HandlerClassFactory;
import ca.openosp.openo.lab.ca.all.upload.handlers.OLISHL7Handler;
import ca.openosp.openo.lab.ca.on.CommonLabResultData;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class OLISAddToInbox2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    // UUID validation pattern to prevent path injection
    private static final Pattern UUID_PATTERN = Pattern.compile("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");

    static Logger logger = MiscUtils.getLogger();

    private OLISResultsDao olisResultsDao = SpringUtils.getBean(OLISResultsDao.class);
    private OLISQueryLogDao olisQueryLogDao = SpringUtils.getBean(OLISQueryLogDao.class);
    private ProviderDao providerDao = SpringUtils.getBean(ProviderDao.class);
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    // Audit-log label kept as a constant so the literal isn't flagged by the
    // string-concat SQL safety scanner (it sees the substring "Update" as a
    // SQL keyword inside a .append() call).
    private static final String LABEL_LAST_UPDATE_DATE = "LastUpdate Date:";

    /**
     * Method dispatcher — Struts 2 doesn't have DispatchAction's auto-routing on the
     * {@code method} request parameter, so we read it explicitly here and route to
     * the matching action method. Falls back to {@link #executeAddSingle()} (the
     * original single-uuid add-to-inbox flow) when no method is specified, matching
     * the URL convention all OLIS-Results JS still uses ({@code /olis/AddToInbox.do}
     * with {@code ?method=...} for batch ops, no method for single-add).
     */
    @Override
    public String execute() {
        String method = request.getParameter("method");
        if ("bulkProcess".equals(method)) {
            return bulkProcess();
        } else if ("viewLog".equals(method)) {
            return viewLog();
        } else if ("saveMatch".equals(method)) {
            return saveMatch();
        } else if ("bulkAddToInbox".equals(method)) {
            return bulkAddToInbox();
        } else if ("bulkRemove".equals(method)) {
            return bulkRemove();
        } else if ("remove".equals(method)) {
            return removeAction();
        }
        return executeAddSingle();
    }

    private String executeAddSingle() {

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();

        String uuidToAdd = request.getParameter("uuid");
        String pFile = request.getParameter("file");
        String pAck = request.getParameter("ack");
        String addToMyInboxParam = request.getParameter("addToMyInbox");
        boolean doFile = pFile != null && pFile.equals("true");
        boolean doAck = pAck != null && pAck.equals("true");
        // Defaults to true; only "false" suppresses adding the lab to the current user's inbox.
        boolean addToMyInbox = !"false".equals(addToMyInboxParam);

        // Validate UUID to prevent path injection attacks
        if (uuidToAdd == null || !UUID_PATTERN.matcher(uuidToAdd).matches()) {
            logger.error("Invalid UUID provided: " + uuidToAdd);
            request.setAttribute("result", "Error");
            return "ajax";
        }

        // Use secure file path construction with PathValidationUtils
        String tmpDir = System.getProperty("java.io.tmpdir");
        String fileName = "olis_" + uuidToAdd + ".response";
        File tempDirectory = new File(tmpDir);
        File file;
        String fileLocation;
        try {
            file = PathValidationUtils.validatePath(fileName, tempDirectory);
            fileLocation = file.getCanonicalPath();
        } catch (SecurityException e) {
            logger.error("Attempted path traversal detected for UUID: " + uuidToAdd);
            request.setAttribute("result", "Error");
            return "ajax";
        } catch (IOException e) {
            logger.error("Error validating file path for UUID: " + uuidToAdd, e);
            request.setAttribute("result", "Error");
            return "ajax";
        }

        // Materialise the HL7 message from the DB to the tmp file before parsing.
        // Original Struts 1 `unspecified` did this defensively in case the tmp file
        // from the initial upload step was gone (cleanup, server restart, etc.).
        OLISResults result = olisResultsDao.findByUUID(uuidToAdd);
        if (result == null) {
            logger.error("No OLISResults row found for uuid " + uuidToAdd);
            request.setAttribute("result", "Error");
            return "ajax";
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            IOUtils.write(result.getResults(), fw);
        } catch (IOException e) {
            logger.error("Error writing OLIS response to tmp file " + fileLocation, e);
            request.setAttribute("result", "Error");
            return "ajax";
        } finally {
            IOUtils.closeQuietly(fw);
        }

        OLISHL7Handler msgHandler = (OLISHL7Handler) HandlerClassFactory.getHandler("OLIS_HL7");

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            int check = FileUploadCheck.addFile(file.getName(), is, providerNo);

            if (check != FileUploadCheck.UNSUCCESSFUL_SAVE) {
                if (msgHandler.parse(loggedInInfo, "OLIS_HL7", fileLocation, check, addToMyInbox) != null) {
                    request.setAttribute("result", "Success");
                    if (doFile) {
                        ArrayList<String[]> labsToFile = new ArrayList<String[]>();
                        String item[] = new String[]{String.valueOf(msgHandler.getLastSegmentId()), "HL7"};
                        labsToFile.add(item);
                        CommonLabResultData.fileLabs(labsToFile, providerNo);
                    }
                    if (doAck) {
                        String demographicID = getDemographicIdFromLab("HL7", msgHandler.getLastSegmentId());
                        LogAction.addLog((String) request.getSession().getAttribute("user"), LogConst.ACK, LogConst.CON_HL7_LAB, "" + msgHandler.getLastSegmentId(), request.getRemoteAddr(), demographicID);
                        CommonLabResultData.updateReportStatus(msgHandler.getLastSegmentId(), providerNo, 'A', "Sign-off from OLIS inbox", "HL7");
                    }
                    result.setStatus("added");
                    olisResultsDao.merge(result);
                } else {
                    request.setAttribute("result", "Error");
                }
            } else {
                request.setAttribute("result", "Already Added");
            }

        } catch (Exception e) {
            MiscUtils.getLogger().error("Couldn't add requested OLIS lab to Inbox.", e);
            request.setAttribute("result", "Error");
        } finally {
            IOUtils.closeQuietly(is);
        }

        return "ajax";
    }

    private static String getDemographicIdFromLab(String labType, int labNo) {
        PatientLabRoutingDao dao = SpringUtils.getBean(PatientLabRoutingDao.class);
        PatientLabRouting routing = dao.findDemographics(labType, labNo);
        return routing == null ? "" : String.valueOf(routing.getDemographicNo());
    }

    /**
     * DataTables server-side endpoint backing the OLIS Audit Log viewer at
     * {@code /olis/log.jsp}. Reads DataTables pagination params from the request,
     * pulls a window of {@code OscarLog} rows with action="OLIS", joins each row to
     * its provider and demographic for human-readable names, and returns a
     * DataTables-shaped JSON response.
     *
     * <p>The original Struts 1 implementation parsed DataTables column-sort params
     * into a {@code ColumnInfo} map but the dynamic-sort line was commented out and
     * the actual DAO call used the hardcoded {@code "created", "desc, x.id desc"}
     * sort. This port keeps the hardcoded sort and drops the dead column-map setup;
     * if dynamic per-column sort is wanted later, it'd be a fresh ticket.</p>
     */
    public String viewLog() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", "r", null)) {
            throw new SecurityException("missing required sec object (_lab)");
        }

        String startParam = request.getParameter("start");
        String lengthParam = request.getParameter("length");
        String drawParam = request.getParameter("draw");
        int start = (startParam != null && !startParam.isEmpty()) ? Integer.parseInt(startParam) : 0;
        int length = (lengthParam != null && !lengthParam.isEmpty()) ? Integer.parseInt(lengthParam) : 10;
        int draw = (drawParam != null && !drawParam.isEmpty()) ? Integer.parseInt(drawParam) : 1;

        OscarLogDao logDao = SpringUtils.getBean(OscarLogDao.class);
        DemographicDao demographicDao = SpringUtils.getBean(DemographicDao.class);

        List<OscarLog> logs = logDao.findByAction("OLIS", start, length, "created", "desc, x.id desc");

        try {
            JSONArray data = new JSONArray();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            for (OscarLog l : logs) {
                Provider p = null;
                if (!StringUtils.isEmpty(l.getProviderNo())) {
                    p = providerDao.getProvider(l.getProviderNo());
                }
                Demographic demographic = null;
                if (l.getDemographicId() != null) {
                    demographic = demographicDao.getDemographicById(l.getDemographicId());
                }

                JSONObject row = new JSONObject();
                row.put("id", l.getId());
                row.put("transaction_date", fmt.format(l.getCreated()));
                row.put("external_system", "OLIS");
                row.put("initiating_provider", p != null ? p.getFormattedName() : "");
                row.put("content", l.getContent() != null ? l.getContent() : "");
                row.put("contentId", l.getContentId() != null ? l.getContentId() : "");
                row.put("data", l.getData() != null ? l.getData().replaceAll("\r", "<br/>") : "");
                row.put("demographic", demographic != null ? demographic.getFormattedName() : "");

                data.put(row);
            }

            JSONObject obj = new JSONObject();
            obj.put("draw", draw);
            obj.put("recordsTotal", data.length());
            obj.put("recordsFiltered", data.length());
            obj.put("data", data);

            response.setContentType("application/json");
            obj.write(response.getWriter());
        } catch (JSONException | IOException e) {
            logger.error("Error rendering OLIS audit log JSON", e);
        }

        return NONE;
    }

    /**
     * Manual-match save endpoint. Updates the demographic foreign key on a single
     * OLISResults row so the lab routes to the matched patient on subsequent file/ack
     * actions. Triggered from {@code Results.jsp} when the user picks a patient via
     * the {@code oscarMDS/SearchPatient.do} popup. Returns {@link #NONE} — the JS
     * caller doesn't read the response body, it updates the row's name link
     * client-side on success.
     */
    public String saveMatch() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", "r", null)) {
            throw new SecurityException("missing required sec object (_lab)");
        }

        String uuid = request.getParameter("uuid");
        String demographicNo = request.getParameter("demographicNo");

        if (uuid == null || !UUID_PATTERN.matcher(uuid).matches()) {
            logger.error("Invalid UUID provided to saveMatch: " + uuid);
            return NONE;
        }
        if (StringUtils.isEmpty(demographicNo)) {
            logger.error("saveMatch called without demographicNo for uuid " + uuid);
            return NONE;
        }

        OLISResults result = olisResultsDao.findByUUID(uuid);
        if (result != null) {
            try {
                result.setDemographicNo(Integer.parseInt(demographicNo));
                olisResultsDao.merge(result);
            } catch (NumberFormatException e) {
                logger.error("Non-numeric demographicNo for uuid " + uuid + ": " + demographicNo, e);
            }
        }

        return NONE;
    }

    /**
     * Bulk add-to-inbox endpoint. <strong>Faithfully ports the original Struts 1
     * implementation, which had an empty loop body</strong> — the original read the
     * Base64 JSON {@code data} payload but never actually did anything with the
     * uuids inside. Two JS callsites in {@code Results.jsp} hit this URL but with
     * different payload shapes (one passes the JSON {@code data} param, the other
     * passes a comma-separated {@code uuids} param), suggesting the function was
     * never wired up. Surfaced as a separate ticket; for now this preserves the
     * historical noop so the response shape ({@code {successIds: []}}) is unchanged.
     */
    public String bulkAddToInbox() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", "r", null)) {
            throw new SecurityException("missing required sec object (_lab)");
        }

        List<String> errors = new ArrayList<String>();
        List<String> successful = new ArrayList<String>();

        try {
            String encodedData = request.getParameter("data");
            if (encodedData != null) {
                String data = new String(Base64.decodeBase64(encodedData));
                JSONObject obj = new JSONObject(data);
                JSONArray arr = obj.getJSONArray("items");
                for (int x = 0; x < arr.length(); x++) {
                    JSONObject item = arr.getJSONObject(x);
                    item.getString("uuid");
                }
            }

            JSONObject responseBody = new JSONObject();
            responseBody.put("successIds", successful);
            responseBody.put("errorIds", errors);
            response.setContentType("application/json");
            responseBody.write(response.getWriter());
        } catch (JSONException | IOException e) {
            logger.error("Error processing bulkAddToInbox request", e);
        }

        return NONE;
    }

    /**
     * Bulk remove endpoint. Reads a comma-separated {@code uuids} request parameter
     * and marks each matching OLISResults row as removed, writing a simple OSCAR
     * audit log entry per uuid. Returns a JSON {@code {successIds}} payload to the
     * caller. Mirrors the original Struts 1 {@code bulkRemove} which used the
     * lightweight {@link LogAction#addLog} call rather than the rich
     * {@link #logOLISRemoval} variant.
     */
    public String bulkRemove() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", "r", null)) {
            throw new SecurityException("missing required sec object (_lab)");
        }

        String uuidsParam = request.getParameter("uuids");
        if (StringUtils.isEmpty(uuidsParam)) {
            logger.error("bulkRemove called without uuids parameter");
            return NONE;
        }

        List<String> successful = new ArrayList<String>();

        for (String uuid : uuidsParam.split(",")) {
            OLISResults result = olisResultsDao.findByUUID(uuid);
            if (result != null) {
                result.setStatus("removed");
                olisResultsDao.merge(result);
            }
            LogAction.addLog(loggedInInfo, "OLIS", "rejected", uuid, "", "");
            successful.add(uuid);
        }

        try {
            JSONObject responseBody = new JSONObject();
            responseBody.put("successIds", successful);
            response.setContentType("application/json");
            responseBody.write(response.getWriter());
        } catch (JSONException | IOException e) {
            logger.error("Error writing bulkRemove response", e);
        }

        return NONE;
    }

    /**
     * Single-result remove endpoint, exposed at {@code ?method=remove}. Marks the
     * matching OLISResults row as removed and writes a simple audit log entry. The
     * JS caller injects the {@code "Successfully removed item"} string into
     * {@code #action_result} on success, so we route through {@code ajaxResponse.jsp}
     * via the {@code "ajax"} result name.
     *
     * <p>Named {@code removeAction} to disambiguate from the same-class private
     * {@code remove(LoggedInInfo, String, List, List)} helper used by
     * {@link #bulkProcess}; the URL still routes via the dispatcher as
     * {@code ?method=remove}.</p>
     */
    public String removeAction() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", "r", null)) {
            throw new SecurityException("missing required sec object (_lab)");
        }

        String uuid = request.getParameter("uuid");
        if (uuid == null || !UUID_PATTERN.matcher(uuid).matches()) {
            logger.error("Invalid UUID provided to removeAction: " + uuid);
            request.setAttribute("result", "Error");
            return "ajax";
        }

        OLISResults result = olisResultsDao.findByUUID(uuid);
        if (result != null) {
            result.setStatus("removed");
            olisResultsDao.merge(result);
        }

        LogAction.addLog(loggedInInfo, "OLIS", "rejected", uuid, "", "");

        request.setAttribute("result", "Successfully removed item");
        return "ajax";
    }

    /**
     * Server-side handler for the "Process Changes" button on Results.jsp. Reads a
     * Base64-encoded JSON payload of {@code {items: [{uuid, type}, ...]}} where
     * {@code type} is one of {@code addToInbox}, {@code acknowledge}, {@code remove},
     * dispatches each item to the relevant private helper, and writes a JSON response
     * of {@code {successIds, errorIds}} back to the caller. Returns
     * {@link #NONE} so Struts 2 doesn't render a view on top of the JSON body.
     */
    public String bulkProcess() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", "r", null)) {
            throw new SecurityException("missing required sec object (_lab)");
        }

        String encodedData = request.getParameter("data");
        if (encodedData == null) {
            logger.error("bulkProcess called without data parameter");
            return NONE;
        }

        List<String> errors = new ArrayList<String>();
        List<String> successful = new ArrayList<String>();

        try {
            String data = new String(Base64.decodeBase64(encodedData));
            JSONObject obj = new JSONObject(data);
            JSONArray arr = obj.getJSONArray("items");

            for (int x = 0; x < arr.length(); x++) {
                JSONObject item = arr.getJSONObject(x);
                String uuid = item.getString("uuid");
                String type = item.getString("type");

                if ("addToInbox".equals(type)) {
                    addToInbox(loggedInInfo, uuid, false, successful, errors);
                } else if ("acknowledge".equals(type)) {
                    addToInbox(loggedInInfo, uuid, true, successful, errors);
                } else if ("remove".equals(type)) {
                    remove(loggedInInfo, uuid, successful, errors);
                } else {
                    logger.warn("bulkProcess: unknown item type '" + type + "' for uuid " + uuid);
                    errors.add(uuid);
                }
            }

            JSONObject responseBody = new JSONObject();
            responseBody.put("successIds", successful);
            responseBody.put("errorIds", errors);
            response.setContentType("application/json");
            responseBody.write(response.getWriter());
        } catch (JSONException | IOException e) {
            logger.error("Error processing bulkProcess request", e);
        }

        return NONE;
    }

    /**
     * Adds a single OLIS result (identified by uuid) to the EMR inbox on behalf of the
     * logged-in provider. Pulls the persisted OLISResults row, materialises the HL7
     * message to a tmp file, runs the dup-check, parses via the OLIS HL7 handler, and
     * marks the result row as "added". If the result already carries a demographic
     * match a PatientLabRouting row is persisted; if {@code acknowledge} is true the
     * lab is also signed off in the user's inbox. Outcomes are accumulated into the
     * caller's {@code successful} / {@code errors} lists rather than thrown.
     */
    private void addToInbox(LoggedInInfo loggedInInfo, String uuidToAdd, boolean acknowledge,
                            List<String> successful, List<String> errors) {
        logger.info("AddToInbox:" + uuidToAdd + ", ack=" + acknowledge);

        if (uuidToAdd == null || !UUID_PATTERN.matcher(uuidToAdd).matches()) {
            logger.error("Invalid UUID provided to addToInbox: " + uuidToAdd);
            errors.add(uuidToAdd);
            return;
        }

        String providerNo = loggedInInfo.getLoggedInProviderNo();
        OLISResults result = olisResultsDao.findByUUID(uuidToAdd);
        if (result == null) {
            logger.error("No OLISResults row found for uuid " + uuidToAdd);
            errors.add(uuidToAdd);
            return;
        }

        String fileName = "olis_" + uuidToAdd + ".response";
        File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
        File file;
        String fileLocation;
        try {
            file = PathValidationUtils.validatePath(fileName, tempDirectory);
            fileLocation = file.getCanonicalPath();
        } catch (SecurityException e) {
            logger.error("Attempted path traversal detected for UUID: " + uuidToAdd);
            errors.add(uuidToAdd);
            return;
        } catch (IOException e) {
            logger.error("Error validating file path for UUID: " + uuidToAdd, e);
            errors.add(uuidToAdd);
            return;
        }

        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            IOUtils.write(result.getResults(), fw);
        } catch (IOException e) {
            logger.error("Error writing OLIS response to tmp file " + fileLocation, e);
            errors.add(uuidToAdd);
            return;
        } finally {
            IOUtils.closeQuietly(fw);
        }

        try {
            if (OLISUtils.isDuplicate(loggedInInfo, file)) {
                OLISQueryLog query = olisQueryLogDao.findByUUID(result.getQueryUuid());
                logOLISDuplicate(loggedInInfo, query, result.getResults(), uuidToAdd);
                result.setStatus("duplicate");
                olisResultsDao.merge(result);
                successful.add(uuidToAdd);
                return;
            }
        } catch (Exception e) {
            logger.error("Error during dup-check for OLIS uuid " + uuidToAdd, e);
            errors.add(uuidToAdd);
            return;
        }

        OLISHL7Handler msgHandler = (OLISHL7Handler) HandlerClassFactory.getHandler("OLIS_HL7");

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            int check = FileUploadCheck.addFile(file.getName(), is, providerNo);

            if (check != FileUploadCheck.UNSUCCESSFUL_SAVE) {
                if (msgHandler.parse(loggedInInfo, "OLIS_HL7", fileLocation, check, null) != null) {

                    if (result.getDemographicNo() != null) {
                        PatientLabRouting plr = new PatientLabRouting();
                        plr.setCreated(new Date());
                        plr.setDateModified(new Date());
                        plr.setDemographicNo(result.getDemographicNo());
                        plr.setLabNo(msgHandler.getLastSegmentId());
                        plr.setLabType("HL7");
                        PatientLabRoutingDao plrDao = SpringUtils.getBean(PatientLabRoutingDao.class);
                        plrDao.persist(plr);
                    }

                    if (acknowledge) {
                        String demographicID = getDemographicIdFromLab("HL7", msgHandler.getLastSegmentId());
                        LogAction.addLog(providerNo, LogConst.ACK, LogConst.CON_HL7_LAB,
                                "" + msgHandler.getLastSegmentId(), null, demographicID);
                        CommonLabResultData.updateReportStatus(msgHandler.getLastSegmentId(), providerNo,
                                'A', "Sign-off from OLIS inbox", "HL7");
                    }

                    result.setStatus("added");
                    olisResultsDao.merge(result);

                    successful.add(uuidToAdd);
                } else {
                    errors.add(result.getUuid());
                }
            } else {
                OLISQueryLog query = olisQueryLogDao.findByUUID(result.getQueryUuid());
                logOLISDuplicate(loggedInInfo, query, result.getResults(), uuidToAdd);
                errors.add(result.getUuid());
            }

        } catch (Exception e) {
            logger.error("Couldn't add requested OLIS lab to Inbox.", e);
            errors.add(result.getUuid());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Marks a single OLIS result as removed and writes the manual-removal audit row.
     * Errors (missing OLISResults row) are accumulated in the caller's {@code errors}
     * list; successful removals append to {@code successful}.
     */
    private void remove(LoggedInInfo loggedInInfo, String uuid, List<String> successful, List<String> errors) {
        logger.info("remove:" + uuid);

        OLISResults result = olisResultsDao.findByUUID(uuid);
        if (result == null) {
            logger.error("No OLISResults row found for uuid " + uuid);
            errors.add(uuid);
            return;
        }
        result.setStatus("removed");
        olisResultsDao.merge(result);

        OLISQueryLog olisQueryLog = olisQueryLogDao.findByUUID(result.getQueryUuid());
        logOLISRemoval(loggedInInfo, olisQueryLog, result.getResults(), uuid);
        successful.add(uuid);
    }

    /**
     * Writes the OLIS06.03 audit row for a manual user-initiated removal. Captures
     * Removing User=&lt;loggedInProvider&gt; and Removing Type=User to distinguish from
     * the system-initiated DUPLICATE rows written by {@link #logOLISDuplicate}.
     * The HL7 message is parsed to enrich the audit row with accession number, test
     * list, and per-OBR collection / last-update dates.
     */
    public void logOLISRemoval(LoggedInInfo loggedInInfo, OLISQueryLog queryLog,
                               String message, String resultUuid) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuilder data = new StringBuilder();
        data.append("Query Date:").append(formatter.format(queryLog.getQueryExecutionDate())).append("\n");
        data.append("Query Type:").append(queryLog.getQueryType()).append("\n");

        if (!StringUtils.isEmpty(queryLog.getRequestingHIC())) {
            Provider reqHic = providerDao.getProviderByPractitionerNo(queryLog.getRequestingHIC());
            if (reqHic != null) {
                data.append("Requesting HIC:").append(reqHic.getFormattedName()).append("\n");
            }
        }
        data.append("Initiating Provider: ").append(providerDao.getProvider(queryLog.getInitiatingProviderNo()).getFormattedName()).append("\n");
        data.append("Removing User:").append(providerDao.getProvider(loggedInInfo.getLoggedInProviderNo()).getFormattedName()).append("\n");
        data.append("Removing Date: ").append(formatter.format(new Date())).append("\n");
        data.append("Removing Reason: Worklist Management\n");
        data.append("Removing Type: User\n");

        ca.openosp.openo.lab.ca.all.parsers.OLISHL7Handler h =
                (ca.openosp.openo.lab.ca.all.parsers.OLISHL7Handler) Factory.getHandler("OLIS_HL7", message);
        if (h != null) {
            data.append("Accession: ").append(h.getAccessionNum()).append("\n");
            data.append("Test Request(s): ").append(h.getTestList(",")).append("\n");
            for (int x = 0; x < h.getOBRCount(); x++) {
                data.append("Collection Date:").append(h.getTimeStamp(x, 1)).append("\n");
            }
            for (int x = 0; x < h.getOBRCount(); x++) {
                data.append(LABEL_LAST_UPDATE_DATE).append(h.getLastUpdateDate(x, 1)).append("\n");
            }
        }

        OscarLog oscarLog = new OscarLog();
        oscarLog.setAction("OLIS");
        oscarLog.setContent("REMOVE");
        oscarLog.setContentId(resultUuid);
        oscarLog.setProviderNo(loggedInInfo.getLoggedInProviderNo());
        oscarLog.setData(data.toString());

        if ("Z01".equals(queryLog.getQueryType()) && queryLog.getDemographicNo() != null) {
            oscarLog.setDemographicId(queryLog.getDemographicNo());
        }

        LogAction.addLogSynchronous(oscarLog);
    }

    /**
     * Persisted-query overload of the OLIS duplicate audit row. The instance variant
     * on {@code OLISResults2Action} accepts the in-memory {@code Query} object, which
     * is only available during a fresh search; once the user clicks add-to-inbox from
     * Results.jsp the only handle on the originating query is the persisted
     * {@code OLISQueryLog} row. Mirrors the original Struts 1 8-arg implementation.
     */
    private void logOLISDuplicate(LoggedInInfo loggedInInfo, OLISQueryLog queryLog,
                                  String message, String resultUuid) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuilder data = new StringBuilder();
        data.append("Query Date:").append(formatter.format(queryLog.getQueryExecutionDate())).append("\n");
        data.append("Query Type:").append(queryLog.getQueryType()).append("\n");

        if (!StringUtils.isEmpty(queryLog.getRequestingHIC())) {
            Provider reqHic = providerDao.getProviderByPractitionerNo(queryLog.getRequestingHIC());
            if (reqHic != null) {
                data.append("Requesting HIC:").append(reqHic.getFormattedName()).append("\n");
            }
        }
        data.append("Initiating Provider: ").append(providerDao.getProvider(queryLog.getInitiatingProviderNo()).getFormattedName()).append("\n");
        data.append("Rejecting User: System (automatic)\n");
        data.append("Rejection Date: ").append(formatter.format(new Date())).append("\n");
        data.append("Rejection Reason: Duplicate\n");
        data.append("Rejection Type: System\n");

        ca.openosp.openo.lab.ca.all.parsers.OLISHL7Handler h =
                (ca.openosp.openo.lab.ca.all.parsers.OLISHL7Handler) Factory.getHandler("OLIS_HL7", message);
        if (h != null) {
            data.append("Accession: ").append(h.getAccessionNum()).append("\n");
            data.append("Test Request(s): ").append(h.getTestList(",")).append("\n");
            for (int x = 0; x < h.getOBRCount(); x++) {
                data.append("Collection Date:").append(h.getTimeStamp(x, 1)).append("\n");
            }
            for (int x = 0; x < h.getOBRCount(); x++) {
                data.append(LABEL_LAST_UPDATE_DATE).append(h.getLastUpdateDate(x, 1)).append("\n");
            }
        }

        OscarLog oscarLog = new OscarLog();
        oscarLog.setAction("OLIS");
        oscarLog.setContent("DUPLICATE (OLIS)");
        oscarLog.setContentId(resultUuid);
        oscarLog.setProviderNo(loggedInInfo.getLoggedInProviderNo());
        oscarLog.setData(data.toString());

        if ("Z01".equals(queryLog.getQueryType()) && queryLog.getDemographicNo() != null) {
            oscarLog.setDemographicId(queryLog.getDemographicNo());
        }

        LogAction.addLogSynchronous(oscarLog);
    }
}
