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


package ca.openosp.openo.prescript.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.DrugDao;
import ca.openosp.openo.commn.model.Drug;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for managing the "hide from CPP" (Canadian Patient Portal) setting
 * on prescription drug records.
 *
 * This action allows authorized users to toggle the visibility of prescription drugs
 * in the Canadian Patient Portal. When a drug is marked as "hidden from CPP", it will
 * not appear in the patient's online portal view, providing privacy control over
 * sensitive medications.
 *
 * The action requires the "_rx" privilege with update permission to modify drug
 * visibility settings. All changes are persisted directly to the database via
 * the DrugDao.
 *
 * @since 2011-06-20
 */
public class RxHideCpp2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for sending responses back to the client */
    HttpServletResponse response = ServletActionContext.getResponse();


    /** Logger instance for debugging and error tracking */
    private static final Logger logger = MiscUtils.getLogger();

    /** Data access object for managing prescription drug records */
    private DrugDao drugDao = (DrugDao) SpringUtils.getBean(DrugDao.class);

    /** Security manager for validating user privileges */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Main entry point for the action that delegates to the update method.
     *
     * @return String Result of the update operation, typically null for AJAX responses
     */
    public String execute() {
        return update();
    }

    /**
     * Updates the "hide from CPP" setting for a specific prescription drug.
     *
     * This method performs the following operations:
     * <ol>
     * <li>Validates user has "_rx" update privileges</li>
     * <li>Retrieves the drug record by prescription ID</li>
     * <li>Updates the hideFromCpp boolean flag</li>
     * <li>Persists changes to the database</li>
     * <li>Sends "ok" response to client</li>
     * </ol>
     *
     * @return String Always returns null as this is an AJAX endpoint
     * @throws RuntimeException if user lacks required "_rx" update privileges
     */
    public String update() {
        // Security check - user must have prescription update privileges
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_rx", "u", null)) {
            throw new RuntimeException("missing required sec object (_rx)");
        }

        // Extract parameters from request
        String prescriptId = request.getParameter("prescriptId");
        String value = request.getParameter("value");

        // Find and update the drug record
        Drug drug = drugDao.find(Integer.valueOf(prescriptId));
        if (drug != null) {
            drug.setHideFromCpp(Boolean.valueOf(value));
        }

        // Persist changes to database
        drugDao.merge(drug);

        // Send confirmation response for AJAX call
        try {
            response.getWriter().println("ok");
        } catch (IOException e) {
            logger.error("error", e);
        }
        return null;
    }
}
