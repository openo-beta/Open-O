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
import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.commn.model.PartialDate;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.prescript.data.RxDrugData;
import ca.openosp.openo.prescript.data.RxPatientData;


import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for adding allergies in the prescription module.
 *
 * This action handles the creation of new allergy records for patients,
 * including drug allergies, non-drug allergies, and environmental allergies.
 * It supports partial date entry for onset dates and manages archiving of
 * replaced allergy records.
 *
 * The action validates security privileges, processes allergy data including
 * severity and reaction information, and logs all changes for audit purposes.
 * It integrates with the drug reference system to link allergies to specific
 * medications when applicable.
 *
 * @since 2008-11-20
 */
public final class RxAddAllergy2Action extends ActionSupport {
    /**
     * HTTP request object.
     */
    HttpServletRequest request = ServletActionContext.getRequest();

    /**
     * HTTP response object.
     */
    HttpServletResponse response = ServletActionContext.getResponse();

    /**
     * Security manager for privilege checking.
     */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Executes the add allergy action.
     *
     * Processes allergy form data, creates new allergy record, and
     * optionally archives a replaced allergy. Validates security
     * privileges and logs all actions for audit trail.
     *
     * @return String SUCCESS on successful addition
     * @throws IOException if I/O error occurs
     * @throws ServletException if servlet error occurs
     * @throws RuntimeException if user lacks required privileges
     */
    public String execute() throws IOException, ServletException {
        // Verify user has write permission for allergies
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_allergy", "w", null)) {
            throw new RuntimeException("missing required sec object (_allergy)");
        }

        // Get allergy identifier (drug reference ID or ATC code)
        String id = request.getParameter("ID");
        if (id != null && "null".equals(id)) {
            id = "";
        }

        String name = request.getParameter("name");
        String type = request.getParameter("type");
        String description = request.getParameter("reactionDescription");

        String startDate = request.getParameter("startDate");
        String ageOfOnset = request.getParameter("ageOfOnset");
        String severityOfReaction = request.getParameter("severityOfReaction");
        String onSetOfReaction = request.getParameter("onSetOfReaction");
        String lifeStage = request.getParameter("lifeStage");
        String allergyToArchive = request.getParameter("allergyToArchive");

        String nonDrug = request.getParameter("nonDrug");

        // Retrieve patient from session and create new allergy
        RxPatientData.Patient patient = (RxPatientData.Patient) request.getSession().getAttribute("Patient");
        Allergy allergy = new Allergy();

        // Set drug reference ID for type 13 (drug allergy)
        if (type != null && "13".equals(type)) {
            allergy.setDrugrefId(id);
        }

        // Set ATC code for type 8 (ATC-based allergy)
        if (type != null && "8".equals(type)) {
            allergy.setAtc(id);
        }
        allergy.setDescription(name);
        allergy.setTypeCode(Integer.parseInt(type));
        allergy.setReaction(description);

        // Handle partial date entry (full date, year-month, or year only)
        if (startDate.length() >= 8 && getCharOccur(startDate, '-') == 2) {
            allergy.setStartDate(RxUtil.StringToDate(startDate, "yyyy-MM-dd"));
        } else if (startDate.length() >= 6 && getCharOccur(startDate, '-') >= 1) {
            allergy.setStartDate(RxUtil.StringToDate(startDate, "yyyy-MM"));
            allergy.setStartDateFormat(PartialDate.YEARMONTH);
        } else if (startDate.length() >= 4) {
            allergy.setStartDate(RxUtil.StringToDate(startDate, "yyyy"));
            allergy.setStartDateFormat(PartialDate.YEARONLY);
        }
        allergy.setAgeOfOnset(ageOfOnset);
        allergy.setSeverityOfReaction(severityOfReaction);
        allergy.setOnsetOfReaction(onSetOfReaction);
        allergy.setLifeStage(lifeStage);

        // Set non-drug allergy flag
        if (nonDrug != null && "on".equals(nonDrug)) {
            allergy.setNonDrug(true);

        } else if (nonDrug != null && "off".equals(nonDrug)) {
            allergy.setNonDrug(false);
        }


        // Link allergy to drug monograph for drug allergies
        if (type != null && type.equals("13")) {
            RxDrugData drugData = new RxDrugData();
            try {
                RxDrugData.DrugMonograph f = drugData.getDrug("" + id);
                allergy.setRegionalIdentifier(f.regionalIdentifier);
            } catch (Exception e) {
                MiscUtils.getLogger().error("Error", e);
            }
        }

        allergy.setDemographicNo(patient.getDemographicNo());
        allergy.setArchived(false);

        // Add allergy to patient record
        patient.addAllergy(RxUtil.Today(), allergy);

        // Log the addition for audit trail
        String ip = request.getRemoteAddr();
        LogAction.addLog((String) request.getSession().getAttribute("user"), LogConst.ADD, LogConst.CON_ALLERGY, "" + allergy.getAllergyId(), ip, "" + patient.getDemographicNo(), allergy.getAuditString());

        // Archive replaced allergy if specified
        if (allergyToArchive != null && !allergyToArchive.isEmpty() && !"null".equals(allergyToArchive)) {
            patient.deleteAllergy(Integer.parseInt(allergyToArchive));
            LogAction.addLog((String) request.getSession().getAttribute("user"), LogConst.ARCHIVE, LogConst.CON_ALLERGY, "" + allergyToArchive, ip, "" + patient.getDemographicNo(), null);
        }

        return SUCCESS;
    }

    /**
     * Counts occurrences of a character in a string.
     *
     * Used to determine date format based on delimiter count.
     *
     * @param str String to search
     * @param ch char character to count
     * @return int number of occurrences
     */
    private int getCharOccur(String str, char ch) {
        int occurence = 0, from = 0;
        while (str.indexOf(ch, from) >= 0) {
            occurence++;
            from = str.indexOf(ch, from) + 1;
        }
        return occurence;
    }
}
