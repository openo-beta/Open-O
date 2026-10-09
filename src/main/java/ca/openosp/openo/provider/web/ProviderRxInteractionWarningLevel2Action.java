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


package ca.openosp.openo.provider.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.UserPropertyDAO;
import ca.openosp.openo.commn.model.UserProperty;
import ca.openosp.openo.commn.model.enumerator.UserPropertyKey;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class ProviderRxInteractionWarningLevel2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();


    private static final Logger logger = MiscUtils.getLogger();

    private UserPropertyDAO propertyDao = (UserPropertyDAO) SpringUtils.getBean(UserPropertyDAO.class);
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public String execute() {
        return update();
    }

    public String update() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_pref", SecurityInfoManager.UPDATE, null)) {
            throw new SecurityException("missing required sec object (_pref)");
        }

        // POST only, so CSRFGuard checks the token
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }

        // Only the levels offered on the preference page: 0 not specified, 1 low, 2 medium, 3 high, 4 none
        String value = request.getParameter("value");
        if (value == null || !value.matches("[0-4]")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        String providerNo = loggedInInfo.getLoggedInProviderNo();
        UserProperty prop = propertyDao.getProp(providerNo, UserPropertyKey.RX_INTERACTION_WARNING_LEVEL);
        if (prop == null) {
            prop = new UserProperty();
            prop.setName(UserPropertyKey.RX_INTERACTION_WARNING_LEVEL.getName());
            prop.setProviderNo(providerNo);
        }
        prop.setValue(value);
        propertyDao.saveProp(prop);


        try {
            response.getWriter().println("ok");
        } catch (IOException e) {
            logger.error("error", e);
        }
        return null;
    }
}
