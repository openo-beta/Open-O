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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.casemgmt.service.CaseManagementManager;
import ca.openosp.openo.commn.dao.DrugDao;
import ca.openosp.openo.commn.model.Drug;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for reordering prescription drugs within a patient's medication list.
 *
 * This action enables healthcare providers to change the display order of a patient's
 * active prescriptions by swapping the positions of two drugs. This functionality is
 * useful for organizing medications by importance, frequency, or other clinical preferences.
 *
 * The action performs position swapping by:
 * <ul>
 * <li>Retrieving the patient's current prescription list</li>
 * <li>Finding the two drugs to be swapped by their IDs</li>
 * <li>Exchanging their position values in the database</li>
 * <li>Persisting the changes via DrugDao</li>
 * </ul>
 *
 * Requires "_rx" update privileges to modify prescription ordering.
 *
 * @since 2011-06-27
 */
public class RxReorder2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for sending responses back to the client */
    HttpServletResponse response = ServletActionContext.getResponse();


    /** Logger instance for debugging and error tracking */
    private static final Logger logger = MiscUtils.getLogger();

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
     * Swaps the display positions of two prescription drugs for a specific patient.
     *
     * This method performs the following operations:
     * <ol>
     * <li>Validates user has "_rx" update privileges</li>
     * <li>Extracts patient demographic number and drug IDs from request</li>
     * <li>Retrieves the patient's current prescription list</li>
     * <li>Locates the two drugs to be swapped within the list</li>
     * <li>Exchanges their position values</li>
     * <li>Persists the changes to the database</li>
     * <li>Sends "ok" response to confirm success</li>
     * </ol>
     *
     * If either drug cannot be found in the patient's prescription list, a warning
     * is logged but no exception is thrown.
     *
     * @return String Always returns null as this is an AJAX endpoint
     * @throws RuntimeException if user lacks required "_rx" update privileges
     */
    public String update() {
        // Security check - user must have prescription update privileges
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_rx", "u", null)) {
            throw new RuntimeException("missing required sec object (_rx)");
        }

        // Extract request parameters
        String demographicNo = request.getParameter("demographicNo");
        int drugId = Integer.parseInt(request.getParameter("drugId"));
        int swapDrugId = Integer.parseInt(request.getParameter("swapDrugId"));

        // Get patient's current prescription list
        CaseManagementManager caseManagementManager = (CaseManagementManager) SpringUtils.getBean(CaseManagementManager.class);
        List<Drug> drugs = caseManagementManager.getPrescriptions(demographicNo, true);
        DrugDao drugDao = (DrugDao) SpringUtils.getBean(DrugDao.class);

        Drug myDrug = null;
        Drug swapDrug = null;

        // Find the two drugs to be swapped in the prescription list
        for (Drug drug : drugs) {
            if (drug.getId().intValue() == drugId) {
                myDrug = drug;
            }
            if (drug.getId().intValue() == swapDrugId) {
                swapDrug = drug;
            }
        }

        // Perform position swap if both drugs were found
        if (myDrug == null || swapDrug == null) {
            MiscUtils.getLogger().warn("Couldn't find the drugs to swap!");
        } else {
            // Exchange the position values
            int myPosition = myDrug.getPosition();
            int swapPosition = swapDrug.getPosition();
            myDrug.setPosition(swapPosition);
            swapDrug.setPosition(myPosition);

            // Persist changes to database
            drugDao.merge(myDrug);
            drugDao.merge(swapDrug);
        }

        // Send confirmation response for AJAX call
        try {
            response.getWriter().println("ok");
        } catch (IOException e) {
            logger.error("error", e);
        }
        return null;
    }
}
