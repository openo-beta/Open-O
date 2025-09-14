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

import ca.openosp.openo.prescript.util.RxUtil;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.prescript.data.RxDrugData;
import ca.openosp.openo.prescript.data.RxPrescriptionData;


import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for drug selection and prescription creation in the prescription module.
 * <p>
 * This action handles the selection of a drug from the drug database and creates
 * a new prescription entry with default values. It processes both brand name (BN)
 * and drug ID parameters to create prescriptions with complete drug information
 * including ATC codes, dosage information, and drug components.
 * <p>
 * The action integrates with the RxSessionBean to manage prescription state
 * and adds the new prescription to the user's prescription stash. It handles
 * both database drugs (with drug ID) and custom entries (without drug ID).
 * Security validation ensures only authorized users can access prescription
 * functionality.
 *
 * @since 2008
 */
public final class RxChooseDrug2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters and session */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for handling the response */
    HttpServletResponse response = ServletActionContext.getResponse();

    /** Security manager for validating user permissions */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Debug print method (currently disabled).
     * <p>
     * This method was used for debugging purposes but is now empty.
     * Left in place to maintain compatibility with existing debug calls.
     *
     * @param s String to print (currently ignored)
     */
    public void p(String s) {
        // Debug method - currently disabled
    }

    /**
     * Main execution method for processing drug selection and prescription creation.
     * <p>
     * This method:
     * 1. Validates user permissions for prescription access
     * 2. Retrieves the active RxSessionBean from the session
     * 3. Creates a new prescription with the selected drug information
     * 4. Populates prescription details from the drug database
     * 5. Sets default prescription parameters (frequency, duration, etc.)
     * 6. Adds the prescription to the user's prescription stash
     * <p>
     * The method handles both database drugs (identified by drugId) and
     * custom drug entries. For database drugs, it retrieves complete
     * information including ATC codes, components, and dosage details.
     *
     * @return String the result status (SUCCESS) to continue with prescription workflow
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     * @throws RuntimeException if user lacks required prescription permissions
     */
    public String execute() throws IOException, ServletException {

        // Validate user has permission to access prescription functionality
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_rx", "r", null)) {
            throw new RuntimeException("missing required sec object (_rx)");
        }

        // Retrieve the active prescription session bean
        RxSessionBean bean = (RxSessionBean) request.getSession().getAttribute("RxSessionBean");
        if (bean == null) {
            response.sendRedirect("error.html");
            return null;
        }

        try {
            // Initialize data access objects for prescriptions and drugs
            RxPrescriptionData rxData = new RxPrescriptionData();
            RxDrugData drugData = new RxDrugData();

            // Create a new prescription instance for this provider and patient
            RxPrescriptionData.Prescription rx =
                    rxData.newPrescription(bean.getProviderNo(), bean.getDemographicNo());


            // Extract drug selection parameters from the request
            String BN = request.getParameter("BN");
            String drugId = request.getParameter("drugId");


            // Set the brand name from the request parameter
            rx.setBrandName(BN);
            try {
                // Parse drug ID and retrieve complete drug information from database
                rx.setGCN_SEQNO(Integer.parseInt(drugId));
                RxDrugData.DrugMonograph f = drugData.getDrug(drugId);
                String genName = f.name;
                rx.setAtcCode(f.atc);
                rx.setBrandName(f.product);
                rx.setRegionalIdentifier(f.regionalIdentifier);

                // Set drug components for display and build dosage string
                request.setAttribute("components", f.components);
                String dosage = "";
                for (int c = 0; c < f.components.size(); c++) {
                    RxDrugData.DrugMonograph.DrugComponent dc = (RxDrugData.DrugMonograph.DrugComponent) f.components.get(c);
                    if (c == (f.components.size() - 1)) {
                        dosage += dc.strength + " " + dc.unit;
                    } else {
                        dosage += dc.strength + " " + dc.unit + " / ";
                    }
                }
                rx.setDosage(dosage);
                // Build comprehensive component string for generic name
                StringBuilder compString = null;
                if (f.components != null) {
                    compString = new StringBuilder();
                    for (int c = 0; c < f.components.size(); c++) {
                        RxDrugData.DrugMonograph.DrugComponent dc = (RxDrugData.DrugMonograph.DrugComponent) f.components.get(c);
                        compString.append(dc.name + " " + dc.strength + " " + dc.unit + " ");
                    }
                }

                // Set generic name based on component information or fallback to drug name
                if (compString != null) {
                    MiscUtils.getLogger().debug("Setting generic name from components: " + compString.toString());
                    rx.setGenericName(compString.toString());
                } else {
                    rx.setGenericName(genName);
                }
            } catch (java.lang.NumberFormatException numEx) {
                // Handle custom drug entry (no drug ID from database)
                rx.setBrandName(null);
                rx.setCustomName("");
                rx.setGCN_SEQNO(0);
            }

            // Set default prescription parameters
            rx.setRxDate(RxUtil.Today());
            rx.setEndDate(null);
            rx.setTakeMin(1);
            rx.setTakeMax(1);
            rx.setFrequencyCode("OID");  // Once daily
            rx.setDuration("30");
            rx.setDurationUnit("D");     // Days
            bean.addAttributeName(rx.getAtcCode() + "-" + String.valueOf(bean.getStashIndex()));


            // Add the prescription to the user's prescription stash
            bean.setStashIndex(bean.addStashItem(loggedInInfo, rx));
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error creating prescription for drug selection", e);
        }

        return SUCCESS;
    }
}
