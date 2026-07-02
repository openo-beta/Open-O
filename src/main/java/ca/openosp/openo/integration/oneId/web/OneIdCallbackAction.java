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

import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.integration.dhdr.OmdGateway;
import ca.openosp.openo.integration.oneId.IdTokenValidationException;
import ca.openosp.openo.integration.oneId.OneIdJwksProvider;
import ca.openosp.openo.integration.oneId.OneIdSession;
import ca.openosp.openo.integration.oneId.web.ConnectivityErrorHelper.ConnectivityError;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.managers.EhrConnectivityManager;
import ca.openosp.openo.managers.SsoAuthenticationManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Pre-authentication callback for the ONE ID OAuth2/OIDC login. It verifies the anti-forgery state,
 * exchanges the authorization code for tokens, validates the id_token signature and claims, resolves
 * the linked provider, establishes a fully authenticated session, and persists the ONE ID session.
 * An unlinked identity is sent to the login page to bind once. There is no privilege check because
 * this is what establishes the session; trust comes from the state, nonce, and id-token validation.
 *
 * @since 2026-07-02
 */
public class OneIdCallbackAction extends ActionSupport {

    private static final Logger logger = MiscUtils.getLogger();

    private final HttpServletRequest request = ServletActionContext.getRequest();
    private final HttpServletResponse response = ServletActionContext.getResponse();

    private final OneIdJwksProvider oneIdJwksProvider = SpringUtils.getBean(OneIdJwksProvider.class);
    private final EhrConnectivityManager ehrConnectivityManager = SpringUtils.getBean(EhrConnectivityManager.class);
    private final SsoAuthenticationManager ssoAuthenticationManager = SpringUtils.getBean(SsoAuthenticationManager.class);

    public String execute() {
        HttpSession session = request.getSession(false);
        try {
            if (session == null) {
                return fail("Your sign-in session could not be found.", "Please try signing in again.");
            }

            String expectedState = (String) session.getAttribute(OneIdLoginAction.SESSION_STATE);
            String nonce = (String) session.getAttribute(OneIdLoginAction.SESSION_NONCE);
            String verifier = (String) session.getAttribute(OneIdLoginAction.SESSION_VERIFIER);
            session.removeAttribute(OneIdLoginAction.SESSION_STATE);
            session.removeAttribute(OneIdLoginAction.SESSION_NONCE);
            session.removeAttribute(OneIdLoginAction.SESSION_VERIFIER);

            String returnedState = request.getParameter("state");
            String code = request.getParameter("code");

            if (expectedState == null || returnedState == null || !expectedState.equals(returnedState)) {
                return fail("Your sign-in could not be verified.", "Please try signing in again.");
            }
            if (code == null || code.isEmpty()) {
                return fail("Ontario Health did not return an authorization code.", "Please try signing in again.");
            }

            LoggedInInfo loggedInInfo = new LoggedInInfo();
            loggedInInfo.setSession(session);

            OmdGateway omdGateway = new OmdGateway();
            Response tokenResponse = omdGateway.exchangeCodeForTokens(loggedInInfo, code, verifier);
            if (tokenResponse.getStatus() != 200) {
                return failWith(ConnectivityErrorHelper.mapStatus(tokenResponse.getStatus()));
            }

            JSONObject tokens = new JSONObject(tokenResponse.readEntity(String.class));
            String idToken = tokens.optString("id_token", null);
            if (idToken == null) {
                return fail("Ontario Health did not return an identity token.", "Please try signing in again.");
            }

            String subject = oneIdJwksProvider.verifyIdToken(idToken, nonce);

            List<Security> matches = ehrConnectivityManager.findProvidersByOneId(subject);
            if (matches == null || matches.isEmpty()) {
                // Not linked yet: keep the verified subject and send the provider to log in once to bind.
                session.setAttribute("oneIdSubject", subject);
                response.sendRedirect(request.getContextPath() + "/index.jsp?oneIdLink=true");
                return NONE;
            }
            if (matches.size() > 1) {
                return fail("More than one account is linked to this ONE ID.", "Contact your administrator.");
            }

            Map<String, Object> sessionData = ssoAuthenticationManager.checkOneIdLogin(subject);
            if (sessionData == null || sessionData.isEmpty()) {
                return fail("Could not sign you in with ONE ID.", "Please try again or contact your administrator.");
            }

            session.invalidate();
            HttpSession newSession = request.getSession(true);
            newSession.setMaxInactiveInterval(7200);
            newSession.setAttribute("oscar_context_path", request.getContextPath());
            newSession.setAttribute("fullSite", "true");
            for (String key : sessionData.keySet()) {
                newSession.setAttribute(key, sessionData.get(key));
            }

            String providerNo = (String) newSession.getAttribute("user");
            LogAction.addLog(providerNo, LogConst.LOGIN, LogConst.CON_LOGIN, "", request.getRemoteAddr());
            ehrConnectivityManager.saveOneIdSession(buildOneIdSession(providerNo, subject, tokens));

            response.sendRedirect(request.getContextPath() + "/uaoSelect.do");
            return NONE;
        } catch (IdTokenValidationException e) {
            logger.warn("ONE ID id-token validation failed");
            return fail("Your sign-in could not be verified.", "Please try signing in again.");
        } catch (Exception e) {
            logger.error("ONE ID callback failed (" + e.getClass().getSimpleName() + ")");
            return failWith(ConnectivityErrorHelper.map(e));
        }
    }

    private OneIdSession buildOneIdSession(String providerNo, String subject, JSONObject tokens) {
        OneIdSession oneIdSession = new OneIdSession();
        oneIdSession.setProviderNo(providerNo);
        oneIdSession.setAccessToken(tokens.optString("access_token", null));
        oneIdSession.setRefreshToken(tokens.optString("refresh_token", null));
        oneIdSession.setIdToken(tokens.optString("id_token", null));
        oneIdSession.setSubject(subject);
        oneIdSession.setEmail(tokens.optString("email", null));
        oneIdSession.setToolbar(tokens.optString("toolbar", null));
        oneIdSession.setAuthorizationId(tokens.optString("authzid", null));
        oneIdSession.setLastKeptActive(new Date());
        oneIdSession.setTimestamp(System.currentTimeMillis());
        oneIdSession.setSso(true);
        return oneIdSession;
    }

    private String fail(String message, String nextStep) {
        return redirectToLoginFailed(message + " " + nextStep);
    }

    private String failWith(ConnectivityError error) {
        return redirectToLoginFailed(error.getUserMessage() + " " + error.getNextStep());
    }

    private String redirectToLoginFailed(String message) {
        LogAction.addLog("", LogConst.LOGIN, "failed", message, request.getRemoteAddr());
        try {
            response.sendRedirect(request.getContextPath() + "/loginfailed.jsp?errormsg="
                    + URLEncoder.encode(message, "UTF-8"));
        } catch (Exception e) {
            logger.error("Failed to redirect to the login-failed page", e);
        }
        return NONE;
    }
}
