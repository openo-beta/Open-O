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



package ca.openosp.openo.providers.pageUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.openo.commn.exception.ProviderSignatureException;
import ca.openosp.openo.managers.ProviderSignatureManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.managers.model.SignatureChoice;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Adds, edits and deletes the signatures in the logged-in provider's list, and picks their default.
 *
 * <p>The {@code method} parameter picks the change: {@code save} (adds when there is no
 * {@code signatureId}, otherwise edits), {@code delete} or {@code makeDefault} (no
 * {@code signatureId} makes the prescriber name the default).
 *
 * @since 2003-07-22
 */
public class ProEditSignature2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final ProviderSignatureManager providerSignatureManager = SpringUtils.getBean(ProviderSignatureManager.class);

    /**
     * Applies one change to the logged-in provider's signature list.
     *
     * @return String "success" to go back to the list, "input" to show the edit page again with
     *         the problem, or null when the request isn't a POST or its signature id isn't a number
     * @throws SecurityException when the user can't change their preferences, or the signature isn't theirs
     */
    @Override
    public String execute() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_pref", SecurityInfoManager.UPDATE, null)) {
            throw new SecurityException("missing required sec object (_pref)");
        }

        // POST only, so CSRFGuard checks the token
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }

        Integer signatureId;
        try {
            signatureId = SignatureChoice.parsePostedId(request.getParameter("signatureId"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        String method = request.getParameter("method");
        if ("delete".equals(method)) {
            providerSignatureManager.deleteSignature(loggedInInfo, signatureId);
        } else if ("makeDefault".equals(method)) {
            providerSignatureManager.makeDefault(loggedInInfo, signatureId);
        } else {
            String text = request.getParameter("signature");
            try {
                if (signatureId == null) {
                    providerSignatureManager.addSignature(loggedInInfo, text);
                } else {
                    providerSignatureManager.editSignature(loggedInInfo, signatureId, text);
                }
            } catch (ProviderSignatureException e) {
                // Shown above the text box
                request.setAttribute("errorKey", e.getMessageKey());
                return INPUT;
            }
        }
        return SUCCESS;
    }
}
