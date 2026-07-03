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

package ca.openosp.openo.login;

import ca.openosp.openo.integration.dhdr.OmdGateway;
import ca.openosp.openo.integration.ohcms.CMSManager;
import ca.openosp.openo.integration.oneId.OneIdGatewayData;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.managers.EhrConnectivityManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class Logout2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static final Logger logger = MiscUtils.getLogger();
    private final EhrConnectivityManager ehrConnectivityManager = SpringUtils.getBean(EhrConnectivityManager.class);


    public String execute() {
        String method = request.getParameter("method");

        if ("ssoLogout".equals(method)) {
            return ssoLogout();
        } else if ("sessionTimeout".equals(method)) {
            return sessionTimeout();
        } else if ("failure".equals(method)) {
            return failure();
        }

        // Default to logout method if unspecified
        return logout();
    }

    public String ssoLogout() {
        logout();
        return "logoutsso";
    }

    public String sessionTimeout() {
        return logout();
    }

    public String failure() {
        return logout();
    }

    public String logout() {

        HttpSession session = request.getSession(false);
        String login = request.getParameter("login");
        String errorMessage = request.getParameter("errorMessage");
        String nameId = request.getParameter("nameId");

        // End the ONE ID session (revoke + remove) while the tokens are still readable.
        String oneIdEndSessionUrl = endOneIdSession(session);

        if (session != null) {
            String user = (String) session.getAttribute("user");
            session.invalidate();
            if (user != null) {
                LogAction.addLog(user, LogConst.LOGOUT, LogConst.CON_LOGIN, "", request.getRemoteAddr());
            }
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Sanitize cookie name to prevent HTTP response splitting
                String cookieName = cookie.getName().replaceAll("[\\r\\n]", "");
                Cookie expiredCookie = new Cookie(cookieName, "");
                expiredCookie.setMaxAge(0);
                expiredCookie.setPath("/");
                response.addCookie(expiredCookie);
            }
        }

        if (oneIdEndSessionUrl != null) {
            try {
                response.sendRedirect(oneIdEndSessionUrl);
            } catch (Exception e) {
                logger.error("Failed to redirect to the ONE ID End Session endpoint", e);
            }
            return NONE;
        }
        return SUCCESS;
    }

    private String endOneIdSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(session);
        OneIdGatewayData gatewayData = (loggedInInfo == null) ? null : loggedInInfo.getOneIdGatewayData();
        if (gatewayData == null) {
            return null;
        }
        String idTokenHint = gatewayData.getIdTokenStr();
        OmdGateway omdGateway = new OmdGateway();
        try {
            CMSManager.userLogout(loggedInInfo);
        } catch (Exception e) {
            logger.error("ONE ID CMS context clear on logout failed", e);
        }
        try {
            omdGateway.revokeToken(loggedInInfo, gatewayData);
        } catch (Exception e) {
            logger.error("ONE ID token revoke failed", e);
        }
        try {
            ehrConnectivityManager.removeOneIdSession(loggedInInfo, loggedInInfo.getLoggedInProviderNo());
        } catch (Exception e) {
            logger.error("ONE ID session removal failed", e);
        }
        gatewayData.clearGatewayData();
        try {
            return omdGateway.buildEndSessionUrl(idTokenHint);
        } catch (Exception e) {
            logger.error("Failed to build the ONE ID End Session URL", e);
            return null;
        }
    }
}
