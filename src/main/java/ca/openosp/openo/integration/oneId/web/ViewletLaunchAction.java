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
        try {
            String uniqueToken = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
            String url = new OmdGateway().getViewletLaunchURL(loggedInInfo, demographicNo, viewlet.getKeyValue(), uniqueToken);
            ObjectNode node = objectMapper.createObjectNode();
            node.put("viewletUrl", url);
            node.put("uuid", uniqueToken);
            node.put("timeoutMillis", viewletTimeoutMillis());
            writeJson(HttpServletResponse.SC_OK, node);
        } catch (CMSException | IllegalStateException e) {
            new OmdGateway().logError(loggedInInfo, viewlet.getKeyValue(), "viewletLaunch", e.getMessage(), demographicNo, null);
            writeFailure(e.getMessage());
        } catch (Exception e) {
            logger.error("Viewlet launch failed", e);
            new OmdGateway().logError(loggedInInfo, viewlet.getKeyValue(), "viewletLaunch", "The EHR service could not be launched.", demographicNo, null);
            writeFailure("The EHR service could not be launched. Please try again.");
        }
        return NONE;
    }

    public String result() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo);
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
        boolean success = "success".equalsIgnoreCase(request.getParameter("status"));
        try {
            OmdGateway omdGateway = new OmdGateway();
            if (success) {
                omdGateway.logDataReceived(loggedInInfo, key.trim(), "viewletResult", message, demographicNo, uniqueToken);
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

    public String patientClose() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo);
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
            logger.error("CMS patient close failed", e);
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
        if (!securityInfoManager.hasPrivilege(loggedInInfo, SEC_OBJECT, "r", null)) {
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
