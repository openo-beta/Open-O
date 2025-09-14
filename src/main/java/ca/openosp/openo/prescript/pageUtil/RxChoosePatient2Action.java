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
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import ca.openosp.openo.commn.dao.UserPropertyDAO;
import ca.openosp.openo.commn.model.UserProperty;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ca.openosp.OscarProperties;
import ca.openosp.openo.prescript.data.RxPatientData;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for patient selection and prescription interface initialization.
 * <p>
 * This action handles the selection of a patient for prescription management
 * and initializes the prescription session with patient demographics and
 * user preferences. It determines the appropriate prescription interface
 * version (RX3, RX4, or legacy) based on system properties and user settings.
 * <p>
 * The action creates an RxSessionBean to maintain prescription session state,
 * loads patient information, and configures prescription profile view settings
 * based on user preferences. It also handles user authentication validation
 * and redirects to appropriate prescription interfaces.
 *
 * @since 2008
 */
public final class RxChoosePatient2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters and session */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for handling the response */
    HttpServletResponse response = ServletActionContext.getResponse();

    /** DAO for accessing user property settings */
    private static UserPropertyDAO userPropertyDAO = SpringUtils.getBean(UserPropertyDAO.class);

    /** Security manager for validating user permissions */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Debug logging method for single string values.
     * <p>
     * Logs debug messages to help with troubleshooting prescription
     * interface initialization and patient selection processes.
     *
     * @param s String the debug message to log
     */
    public void p(String s) {
        MiscUtils.getLogger().debug(s);
    }

    /**
     * Debug logging method for key-value pairs.
     * <p>
     * Logs debug messages in key=value format for parameter tracking
     * during prescription session setup.
     *
     * @param s String the parameter name or key
     * @param s2 String the parameter value
     */
    public void p(String s, String s2) {
        MiscUtils.getLogger().debug(s + "=" + s2);
    }

    /**
     * Main execution method for patient selection and prescription session initialization.
     * <p>
     * This method performs the following operations:
     * 1. Validates user has demographic read permissions
     * 2. Checks for active user session
     * 3. Creates and configures RxSessionBean with provider and patient information
     * 4. Loads patient data and sets session attributes
     * 5. Determines appropriate prescription interface (RX3/RX4/legacy)
     * 6. Configures user prescription profile view preferences
     * 7. Redirects to the appropriate prescription interface
     * <p>
     * The method supports multiple prescription interface versions and
     * user-specific configuration settings for prescription display preferences.
     *
     * @return String the result status indicating which prescription interface to use
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     * @throws RuntimeException if user lacks required demographic permissions
     */
    public String execute() throws IOException, ServletException {
        // Validate user has permission to access patient demographic information
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_demographic", "r", null)) {
            throw new RuntimeException("missing required sec object (_demoraphic)");
        }

        // Validate user session exists
        if (request.getSession().getAttribute("user") == null) {
            return "Logout";
        }

        // Initialize redirect target and extract user information
        String redirect = "error.html";
        String user_no = (String) request.getSession().getAttribute("user");

        // Create and configure prescription session bean
        RxSessionBean bean = new RxSessionBean();
        bean.setProviderNo(user_no);
        bean.setDemographicNo(Integer.parseInt(this.getDemographicNo()));
        request.getSession().setAttribute("RxSessionBean", bean);

        // Load patient data for the selected demographic
        RxPatientData.Patient patient = RxPatientData.getPatient(loggedInInfo, bean.getDemographicNo());

        // Determine prescription interface version based on user preferences
        String provider = (String) request.getSession().getAttribute("user");
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
        userPropertyDAO = (UserPropertyDAO) ctx.getBean(UserPropertyDAO.class);
        boolean providerUseRx3 = false;
        UserProperty propUseRx3 = userPropertyDAO.getProp(provider, UserProperty.RX_USE_RX3);

        if (propUseRx3 != null) {
            providerUseRx3 = BooleanUtils.toBoolean(propUseRx3.getValue());
        }

        if (patient != null) {
            // Determine which prescription interface to use
            if (OscarProperties.getInstance().getBooleanProperty("RX3", "yes") || providerUseRx3) {
                redirect = "successRX3";
            }
            // Placeholder for future RX4 interface
            // else if (OscarProperties.getInstance().getBooleanProperty("ENABLE_RX4", "yes")) {
            //     redirect = "successRX4";
            // }
            else {
                redirect = "success";
            }

            // Configure prescription profile view settings based on user preferences
            UserProperty prop = userPropertyDAO.getProp(provider, UserProperty.RX_PROFILE_VIEW);
            if (prop != null) {
                try {
                    String propValue = prop.getValue();
                    HashMap hm = new HashMap();

                    // Parse profile view preferences - order is important for string replacement
                    String[] viewOptions = {"show_current", "show_all", "longterm_acute_inactive_external",
                                          "inactive", "active", "all", "longterm_acute"};
                    for (int i = 0; i < viewOptions.length; i++) {
                        if (propValue.contains(viewOptions[i])) {
                            propValue = propValue.replace(viewOptions[i], "");
                            hm.put(viewOptions[i].trim(), true);
                        } else {
                            hm.put(viewOptions[i].trim(), false);
                        }
                    }
                    request.getSession().setAttribute("profileViewSpec", hm);
                } catch (Exception e) {
                    MiscUtils.getLogger().error("Error processing prescription profile view settings", e);
                }
            }

            // Set patient data in session for prescription interface use
            request.getSession().setAttribute("Patient", patient);
        }

        return redirect;

    }

    /** Provider number for the current user */
    private String providerNo = null;

    /** Demographic number of the selected patient */
    private String demographicNo = null;

    /**
     * Gets the provider number.
     *
     * @return String the provider number
     */
    public String getProviderNo() {
        return (this.providerNo);
    }

    /**
     * Sets the provider number.
     *
     * @param RHS String the provider number to set
     */
    public void setProviderNo(String RHS) {
        this.providerNo = RHS;
    }

    /**
     * Gets the demographic number of the selected patient.
     *
     * @return String the demographic number
     */
    public String getDemographicNo() {
        return (this.demographicNo);
    }

    /**
     * Sets the demographic number of the patient for prescription management.
     *
     * @param demographicNo String the patient's demographic number
     */
    public void setDemographicNo(String demographicNo) {
        this.demographicNo = demographicNo;
    }
}
