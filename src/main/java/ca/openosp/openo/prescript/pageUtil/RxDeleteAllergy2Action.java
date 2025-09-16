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


package ca.openosp.openo.prescript.pageUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.prescript.data.RxPatientData;


import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for deleting or activating patient allergies in the prescription module.
 * <p>
 * This action handles the deletion and activation of patient allergies within
 * the prescription interface. It supports both permanent deletion and activation
 * of previously archived allergies. All operations are logged for audit purposes
 * with detailed information about the allergy changes.
 * <p>
 * The action validates security permissions for allergy management and integrates
 * with the patient data model to maintain allergy records. It supports different
 * operations based on the action parameter (activate vs delete).
 *
 * @since 2008
 */
public final class RxDeleteAllergy2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters and session */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for handling the response */
    HttpServletResponse response = ServletActionContext.getResponse();

    /** Security manager for validating user permissions */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Main execution method for processing allergy deletion or activation requests.
     * <p>
     * This method:
     * 1. Validates user permissions for allergy updates
     * 2. Retrieves the patient object from the session
     * 3. Processes the allergy operation (delete or activate)
     * 4. Logs the operation for audit purposes
     * 5. Sets demographic number as request attribute if provided
     * <p>
     * The method supports two operations:
     * - Delete: Marks the allergy as deleted
     * - Activate: Reactivates a previously archived allergy
     *
     * @return String the result status (SUCCESS) to continue with the workflow
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     * @throws RuntimeException if user lacks required allergy permissions
     */
    public String execute()
            throws IOException, ServletException {

        // Validate user has permission to update allergy information
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_allergy", "u", null)) {
            throw new RuntimeException("missing required sec object (_allergy)");
        }


        // Extract request parameters
        int id = Integer.parseInt(request.getParameter("ID"));
        String demographicNo = request.getParameter("demographicNo");
        String action = request.getParameter("action");

        // Retrieve patient object from session
        RxPatientData.Patient patient = (RxPatientData.Patient) request.getSession().getAttribute("Patient");

        // Get the allergy record for audit logging
        Allergy allergy = patient.getAllergy(id);
        String ip = request.getRemoteAddr();
        String userId = (String) request.getSession().getAttribute("user");

        // Process the requested operation
        if (action != null && action.equals("activate")) {
            // Reactivate a previously archived allergy
            patient.activateAllergy(id);
            LogAction.addLog(userId, "Activate", LogConst.CON_ALLERGY, "" + id, ip,
                           "" + patient.getDemographicNo(), allergy.getAuditString());
        } else {
            // Delete (archive) the allergy
            patient.deleteAllergy(id);
            LogAction.addLog(userId, LogConst.DELETE, LogConst.CON_ALLERGY, "" + id, ip,
                           "" + patient.getDemographicNo(), allergy.getAuditString());
        }

        // Set demographic number as request attribute for downstream processing
        if (demographicNo != null) {
            request.setAttribute("demographicNo", demographicNo);
        }

        return SUCCESS;
    }
}
