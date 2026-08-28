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
package ca.openosp.openo.integration.oneId.web;

import ca.openosp.openo.commn.model.SystemPreferences;
import ca.openosp.openo.integration.dhdr.OmdGateway;
import ca.openosp.openo.integration.ohcms.CMSException;
import ca.openosp.openo.integration.ohcms.CMSManager;
import ca.openosp.openo.integration.oneId.OneIdViewlet;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.managers.EhrConnectivityManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.UUID;

/**
 * Browser endpoints for the eChart Viewlet launch. {@code launch} puts the patient in CMS context
 * and returns the Viewlet's launch URL as JSON; {@code patientClose} clears the patient from the
 * CMS context when the Viewlet window or dialog is closed. A launch failure returns HTTP 268 with
 * a readable message instead of a URL.
 *
 * @since 2026-07-06
 */
public class ViewletLaunchAction extends ActionSupport {

    private static final Logger logger = MiscUtils.getLogger();
    private static final String SEC_OBJECT = "_ehr.connectivity";
    private static final int STATUS_LAUNCH_FAILED = 268;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpServletRequest request = ServletActionContext.getRequest();
    private final HttpServletResponse response = ServletActionContext.getResponse();

    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final EhrConnectivityManager ehrConnectivityManager = SpringUtils.getBean(EhrConnectivityManager.class);

    public String launch() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo);
        if (rejectNonPost()) {
            return NONE;
        }
        Integer demographicNo = parseId(request.getParameter("demographicNo"));
        String key = request.getParameter("key");
        if (demographicNo == null || key == null || key.trim().isEmpty()) {
            writeFailure("The patient and EHR service for the launch were not provided.");
            return NONE;
        }
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_demographic", "r", String.valueOf(demographicNo))) {
            throw new SecurityException("missing required sec object (_demographic)");
        }
        if (loggedInInfo.getOneIdGatewayData() == null) {
            // Only a missing ONE ID sign-in is recoverable in place; the step-up flag lets the
            // page offer the mid-session sign-in instead of a dead-end message.
            ObjectNode node = objectMapper.createObjectNode();
            node.put("summary", "Sign in with ONE ID to use EHR services.");
            node.put("stepUp", true);
            writeJson(STATUS_LAUNCH_FAILED, node);
            return NONE;
        }
        OneIdViewlet viewlet = ehrConnectivityManager.findActiveViewletByKey(loggedInInfo, key.trim());
        if (viewlet == null) {
            writeFailure("This EHR service is not configured.");
            return NONE;
        }
        String uniqueToken = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        try {
            String url = new OmdGateway().getViewletLaunchURL(loggedInInfo, demographicNo, viewlet.getKeyValue(), uniqueToken);
            ObjectNode node = objectMapper.createObjectNode();
            node.put("viewletUrl", url);
            node.put("uuid", uniqueToken);
            node.put("timeoutMillis", viewletTimeoutMillis());
            writeJson(HttpServletResponse.SC_OK, node);
        } catch (CMSException | IllegalStateException e) {
            // When the CMS refuses a context change it is its response body that becomes the
            // exception message, and that body quotes back the patient just sent, so neither the
            // audit row nor the reply repeats it. The correlation id ties this row to the launch.
            new OmdGateway().logError(loggedInInfo, viewlet.getKeyValue(), "viewletLaunch",
                    "The EHR service did not accept the patient context.", demographicNo, uniqueToken);
            writeFailure("The EHR service could not be launched. Please try again.");
        } catch (Exception e) {
            logger.error("Viewlet launch failed\n" + OmdGateway.stackTraceWithoutMessages(e));
            new OmdGateway().logError(loggedInInfo, viewlet.getKeyValue(), "viewletLaunch", "The EHR service could not be launched.", demographicNo, uniqueToken);
            writeFailure("The EHR service could not be launched. Please try again.");
        }
        return NONE;
    }

    public String result() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo);
        if (rejectNonPost()) {
            return NONE;
        }
        Integer demographicNo = parseId(request.getParameter("demographicNo"));
        String key = request.getParameter("key");
        if (demographicNo == null || key == null || key.trim().isEmpty()) {
            writeFailure("The patient and EHR service for the result were not provided.");
            return NONE;
        }
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_demographic", "r", String.valueOf(demographicNo))) {
            throw new SecurityException("missing required sec object (_demographic)");
        }
        String uniqueToken = emptyToNull(request.getParameter("uuid"));
        String message = bounded(request.getParameter("message"));
        String status = request.getParameter("status");
        boolean success = "success".equalsIgnoreCase(status);

        // The window closed without saying anything, so the outcome is unknown. It is still
        // recorded, and never as a success: the audit row must not claim an outcome nobody
        // observed. The row says plainly that no response came back, so anyone reading the log
        // later knows to confirm the result from the EHR service's own data.
        boolean noResponse = "noresponse".equalsIgnoreCase(status);

        // The EHR service answered, and the answer confirmed the Viewlet call but not the service
        // the clinician launched for. A consent Viewlet acting for several services can accept the
        // consent call while the drug override behind it never happens. The launch did not fail,
        // and it did not do what was asked, so it gets its own outcome.
        boolean partial = "partial".equalsIgnoreCase(status);
        
        try {
            OmdGateway omdGateway = new OmdGateway();
            if (success) {
                omdGateway.logDataReceived(loggedInInfo, key.trim(), "viewletResult", message, demographicNo, uniqueToken);
            } else if (noResponse) {
                omdGateway.logError(loggedInInfo, key.trim(), "viewletResultNoResponse",
                        "The EHR service window closed with no response, so the outcome is unknown. "
                                + "Confirm the result in the EHR service's own data. "
                                + (message == null ? "" : message),
                        demographicNo, uniqueToken);
            } else if (partial) {
                omdGateway.logError(loggedInInfo, key.trim(), "viewletResultPartial",
                        "The EHR service replied without confirming the requested service, so the "
                                + "outcome is unconfirmed. Confirm the result in the EHR service's own data. "
                                + (message == null ? "" : message),
                        demographicNo, uniqueToken);
            } else {
                omdGateway.logError(loggedInInfo, key.trim(), "viewletResult",
                        message == null ? "The EHR service did not return a successful response." : message,
                        demographicNo, uniqueToken);
            }
            ObjectNode node = objectMapper.createObjectNode();
            node.put("status", "ok");
            writeJson(HttpServletResponse.SC_OK, node);
        } catch (Exception e) {
            logger.error("Failed to record the viewlet result", e);
            writeFailure("The EHR service result could not be recorded.");
        }
        return NONE;
    }

    public String noticeSetting() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo, "r");
        ObjectNode node = objectMapper.createObjectNode();
        node.put("enabled", ehrConnectivityManager.isMultiWindowNoticeEnabled(
                loggedInInfo, loggedInInfo.getLoggedInProviderNo()));
        writeJson(HttpServletResponse.SC_OK, node);
        return NONE;
    }

    public String noticeToggle() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo, "w");
        if (rejectNonPost()) {
            return NONE;
        }
        String value = request.getParameter("enabled");
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            writeFailure("The warning setting to apply was not provided.");
            return NONE;
        }
        boolean enabled = Boolean.parseBoolean(value);
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        boolean previous = ehrConnectivityManager.setMultiWindowNoticeEnabled(loggedInInfo, providerNo, enabled);
        LogAction.addLog(providerNo, LogConst.UPDATE, "viewlet-multi-window-notice", providerNo,
                request.getRemoteAddr(), null, "enabled: " + previous + " -> " + enabled);
        ObjectNode node = objectMapper.createObjectNode();
        node.put("status", "ok");
        node.put("enabled", enabled);
        writeJson(HttpServletResponse.SC_OK, node);
        return NONE;
    }

    public String patientClose() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo);
        if (rejectNonPost()) {
            return NONE;
        }
        Integer demographicNo = parseId(request.getParameter("demographicNo"));
        try {
            if (demographicNo != null
                    && loggedInInfo.getOneIdGatewayData() != null
                    && String.valueOf(demographicNo).equals(loggedInInfo.getOneIdGatewayData().getCmsPatientInContext())) {
                CMSManager.patientClose(loggedInInfo, demographicNo);
            }
            ObjectNode node = objectMapper.createObjectNode();
            node.put("status", "ok");
            writeJson(HttpServletResponse.SC_OK, node);
        } catch (Exception e) {
            logger.error("CMS patient close failed\n" + OmdGateway.stackTraceWithoutMessages(e));
            writeFailure("The patient could not be removed from the EHR context.");
        }
        return NONE;
    }

    private void writeFailure(String message) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("summary", message == null ? "The EHR service could not be launched." : message);
        writeJson(STATUS_LAUNCH_FAILED, node);
    }

    private void writeJson(int status, ObjectNode node) {
        try {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(node.toString());
        } catch (Exception e) {
            logger.error("Failed to write the viewlet launch response", e);
        }
    }

    private LoggedInInfo loggedInInfo() {
        return LoggedInInfo.getLoggedInInfoFromSession(request);
    }

    private void checkPrivilege(LoggedInInfo loggedInInfo) {
        checkPrivilege(loggedInInfo, "r");
    }

    /**
     * Rejects any non-POST request to a state-changing method, so these endpoints stay inside
     * the CSRF filter's protected-method set and cannot be driven by a crafted link or image.
     *
     * @return boolean true when the request was rejected and a failure reply written
     */
    private boolean rejectNonPost() {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        writeFailure("This EHR service endpoint only accepts POST requests.");
        return true;
    }

    private void checkPrivilege(LoggedInInfo loggedInInfo, String right) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, SEC_OBJECT, right, null)) {
            throw new SecurityException("missing required sec object (" + SEC_OBJECT + ")");
        }
    }

    private long viewletTimeoutMillis() {
        long seconds = 65;
        String value = ehrConnectivityManager.getConfigValue(SystemPreferences.ONEID_KEYS.viewlet_timeout, "65");
        if (value != null && !value.trim().isEmpty()) {
            try {
                seconds = Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                seconds = 65;
            }
        }
        return seconds * 1000L;
    }

    private static Integer parseId(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String emptyToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    private static String bounded(String value) {
        String trimmed = emptyToNull(value);
        if (trimmed == null) {
            return null;
        }
        return trimmed.length() > 2000 ? trimmed.substring(0, 2000) : trimmed;
    }
}
