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

import ca.openosp.openo.commn.model.UAO;
import ca.openosp.openo.integration.oneId.OneIdGatewayData;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.managers.EhrConnectivityManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Chooses the "Under Authority Of" (UAO) value the acting provider signs in under. After a ONE ID
 * login the provider is sent here: with no UAO values they proceed straight in, with exactly one it
 * is selected automatically, and with several they pick one. The chosen value is attached to the
 * ONE ID session so every gateway call carries it.
 *
 * @since 2026-07-02
 */
public class UaoSelectAction extends ActionSupport {

    private static final Logger logger = MiscUtils.getLogger();
    private static final String SEC_OBJECT = "_ehr.connectivity";

    private final HttpServletRequest request = ServletActionContext.getRequest();
    private final HttpServletResponse response = ServletActionContext.getResponse();

    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final EhrConnectivityManager ehrConnectivityManager = SpringUtils.getBean(EhrConnectivityManager.class);

    public String execute() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo, "r");
        String providerNo = loggedInInfo.getLoggedInProviderNo();

        List<UAO> uaoList = ehrConnectivityManager.findUaosByProvider(loggedInInfo, providerNo);
        if (uaoList == null || uaoList.isEmpty()) {
            redirectHome();
            return NONE;
        }
        if (uaoList.size() == 1) {
            applyUao(loggedInInfo, providerNo, uaoList.get(0));
            redirectHome();
            return NONE;
        }

        request.setAttribute("uaoList", uaoList);
        request.setAttribute("currentUao", currentUaoName(loggedInInfo));
        return "success";
    }

    public String select() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo, "w");
        String providerNo = loggedInInfo.getLoggedInProviderNo();

        Integer id = parseId(request.getParameter("id"));
        UAO uao = (id == null) ? null : ehrConnectivityManager.findUao(loggedInInfo, id);
        if (uao != null) {
            applyUao(loggedInInfo, providerNo, uao);
        }
        redirectHome();
        return NONE;
    }

    private void applyUao(LoggedInInfo loggedInInfo, String providerNo, UAO uao) {
        OneIdGatewayData gatewayData = loggedInInfo.getOneIdGatewayData();
        if (gatewayData != null) {
            gatewayData.setUao(uao.getName());
            gatewayData.setUaoFriendlyName(uao.getFriendlyName());
            gatewayData.setUpdateUAOInCMS(true);
        }
        ehrConnectivityManager.setSessionUao(loggedInInfo, providerNo, uao.getName(), uao.getFriendlyName());
        ehrConnectivityManager.setDefaultUao(loggedInInfo, uao, providerNo);
        LogAction.addLog(providerNo, LogConst.UPDATE, "UAO", providerNo + ":" + uao.getName(), request.getRemoteAddr());
    }

    private String currentUaoName(LoggedInInfo loggedInInfo) {
        OneIdGatewayData gatewayData = loggedInInfo.getOneIdGatewayData();
        return (gatewayData == null) ? null : gatewayData.getUaoFriendlyName();
    }

    private void redirectHome() {
        try {
            // A step-up sign-in leaves the page that started it in the session; return there,
            // otherwise land on the provider home page.
            HttpSession session = request.getSession(false);
            if (session != null) {
                String returnUrl = OneIdLoginAction.safeLocalPath(
                        (String) session.getAttribute(OneIdLoginAction.SESSION_RETURN_URL));
                session.removeAttribute(OneIdLoginAction.SESSION_RETURN_URL);
                if (returnUrl != null) {
                    response.sendRedirect(returnUrl);
                    return;
                }
            }
            response.sendRedirect(request.getContextPath() + "/provider/providercontrol.jsp");
        } catch (Exception e) {
            logger.error("Failed to redirect after UAO selection", e);
        }
    }

    private LoggedInInfo loggedInInfo() {
        return LoggedInInfo.getLoggedInInfoFromSession(request);
    }

    private void checkPrivilege(LoggedInInfo loggedInInfo, String right) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, SEC_OBJECT, right, null)) {
            throw new SecurityException("missing required sec object (" + SEC_OBJECT + ")");
        }
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
}
