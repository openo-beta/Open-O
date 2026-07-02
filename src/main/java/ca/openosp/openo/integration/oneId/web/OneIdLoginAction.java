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

import ca.openosp.openo.integration.dhdr.OmdGateway;
import ca.openosp.openo.integration.oneId.OneIdGatewayData;
import ca.openosp.openo.utility.MiscUtils;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * Pre-authentication entry point that starts the ONE ID OAuth2/OIDC login. It generates the
 * anti-forgery state, the id-token nonce, and a PKCE code verifier, keeps them server-side in the
 * session, and redirects the browser to the Ontario Health authorize endpoint. No privilege check
 * runs here because there is no logged-in session yet; the handshake's own state, nonce, and
 * id-token signature checks establish trust on the callback.
 *
 * @since 2026-07-02
 */
public class OneIdLoginAction extends ActionSupport {

    private static final Logger logger = MiscUtils.getLogger();

    static final String SESSION_STATE = "oneIdState";
    static final String SESSION_NONCE = "oneIdNonce";
    static final String SESSION_VERIFIER = "oneIdVerifier";

    private final HttpServletRequest request = ServletActionContext.getRequest();
    private final HttpServletResponse response = ServletActionContext.getResponse();

    public String execute() {
        try {
            OmdGateway omdGateway = new OmdGateway();
            String state = UUID.randomUUID().toString();
            String nonce = UUID.randomUUID().toString();
            String verifier = omdGateway.generateVerifier();

            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_STATE, state);
            session.setAttribute(SESSION_NONCE, nonce);
            session.setAttribute(SESSION_VERIFIER, verifier);

            OneIdGatewayData gatewayData = new OneIdGatewayData();
            gatewayData.hasScope(OneIdGatewayData.fullScope);

            String authorizeUrl = omdGateway.buildAuthorizeUrl(gatewayData, state, nonce, verifier);
            response.sendRedirect(authorizeUrl);
            return NONE;
        } catch (Exception e) {
            logger.error("Failed to start ONE ID login", e);
            ConnectivityErrorHelper.ConnectivityError error = ConnectivityErrorHelper.map(e);
            try {
                response.sendRedirect(request.getContextPath() + "/loginfailed.jsp?errormsg="
                        + URLEncoder.encode(error.getUserMessage() + " " + error.getNextStep(), "UTF-8"));
            } catch (Exception ignored) {
                // response already committed
            }
            return NONE;
        }
    }
}
