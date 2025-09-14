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


package ca.openosp.openo.mds.pageUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.lab.ca.on.CommonLabResultData;


import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for matching unmatched laboratory results to patient demographics in MDS system.
 * <p>
 * This action handles the critical workflow of linking laboratory results that could not be
 * automatically matched to patient demographics due to name variations, missing health numbers,
 * or data entry discrepancies. It updates the patient routing table to establish the proper
 * connection between lab results and patient records.
 * <p>
 * Key functionality includes:
 * <ul>
 * <li>Security validation requiring lab write privileges</li>
 * <li>Patient-lab result linking via demographic number</li>
 * <li>Integration with patient routing system</li>
 * <li>Redirection to patient electronic chart after successful matching</li>
 * <li>Error handling for failed matching operations</li>
 * </ul>
 * This action is essential for ensuring that laboratory results reach the correct patient
 * records and are available to healthcare providers for clinical decision-making.
 *
 * @since February 4, 2004
 */
public class PatientMatch2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters and session */
    HttpServletRequest request = ServletActionContext.getRequest();
    /** HTTP response object for handling redirects and output */
    HttpServletResponse response = ServletActionContext.getResponse();

    /** Security manager for validating user privileges and access control */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Default constructor for PatientMatch2Action.
     */
    public PatientMatch2Action() {
    }

    /**
     * Executes the patient matching operation for unmatched laboratory results.
     * <p>
     * This method performs the core patient matching workflow:
     * <ul>
     * <li>Validates user has lab write privileges</li>
     * <li>Extracts lab identification and demographic parameters</li>
     * <li>Updates patient lab routing to establish the connection</li>
     * <li>Redirects to patient chart on success or error page on failure</li>
     * </ul>
     * The method ensures that laboratory results are properly linked to patient
     * demographics, enabling them to appear in the correct patient charts.
     *
     * @return String Struts result constant (NONE for redirect)
     * @throws ServletException if servlet processing fails
     * @throws IOException if I/O operations fail during redirect
     */
    public String execute()
            throws ServletException, IOException {

        // Validate user has laboratory write privileges for patient matching operations
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_lab", "w", null)) {
            throw new SecurityException("missing required sec object (_lab)");
        }

        // Extract parameters for patient matching operation
        String demographicNo = request.getParameter("demographicNo");
        String labNo = request.getParameter("labNo");
        String labType = request.getParameter("labType");

        String newURL = "";

        try {
            // Update patient lab routing to link lab result with patient demographic
            CommonLabResultData.updatePatientLabRouting(labNo, demographicNo, labType);
            // Redirect to patient electronic chart showing the newly matched lab result
            newURL = request.getContextPath() + "/oscarMDS/OpenEChart.jsp";
            newURL = newURL + "?demographicNo=" + demographicNo;
        } catch (Exception e) {
            // Log error and redirect to error page if matching operation fails
            MiscUtils.getLogger().debug("exception in PatientMatch2Action:" + e);
            newURL = "/errorpage.jsp";
        }

        // Perform redirect to target page (patient chart or error page)
        response.sendRedirect(newURL);
        return NONE;
    }
}
