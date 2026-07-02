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

/**
 * Removes the ONE ID association from the acting provider's own account and ends the ONE ID session
 * for that provider. Clearing the stored key means a later ONE ID login no longer resolves to this
 * account until it is bound again. The provider stays signed into OpenO.
 *
 * @since 2026-07-02
 */
public class OneIdUnlinkAction extends ActionSupport {

    private static final Logger logger = MiscUtils.getLogger();
    private static final String SEC_OBJECT = "_ehr.connectivity";

    private final HttpServletRequest request = ServletActionContext.getRequest();
    private final HttpServletResponse response = ServletActionContext.getResponse();

    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final EhrConnectivityManager ehrConnectivityManager = SpringUtils.getBean(EhrConnectivityManager.class);

    public String execute() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, SEC_OBJECT, "w", null)) {
            throw new SecurityException("missing required sec object (" + SEC_OBJECT + ")");
        }

        String providerNo = loggedInInfo.getLoggedInProviderNo();
        if (ehrConnectivityManager.clearOneIdBinding(loggedInInfo, providerNo)) {
            LogAction.addLog(providerNo, LogConst.UNLINK, "ONE ID", "", request.getRemoteAddr());
        }

        ehrConnectivityManager.removeOneIdSession(loggedInInfo, providerNo);
        if (loggedInInfo.getOneIdGatewayData() != null) {
            loggedInInfo.getOneIdGatewayData().clearGatewayData();
        }

        try {
            response.sendRedirect(request.getContextPath() + "/provider/providerpreference.jsp");
        } catch (Exception e) {
            logger.error("Failed to redirect after ONE ID unlink", e);
        }
        return NONE;
    }
}
