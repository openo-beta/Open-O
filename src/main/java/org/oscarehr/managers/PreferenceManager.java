//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
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
 * <p>
 * Modifications made by Magenta Health in 2024.
 */
package org.oscarehr.managers;

import java.text.SimpleDateFormat;
import java.util.List;

import org.oscarehr.casemgmt.model.CaseManagementNoteExt;
import org.oscarehr.common.model.Property;
import org.oscarehr.util.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


public interface PreferenceManager {
    String SOCHX = "SocHistory";
    String MEDHX = "MedHistory";
    String CONCERNS = "Concerns";
    String REMINDERS = "Reminders";
    String CUSTOM_SUMMARY_ENABLE = "cpp.pref.enable";
    String OLD_SOCIAL_HISTORY_POS = "cpp.social_hx.position";
    String OLD_MEDICAL_HISTORY_POS = "cpp.medical_hx.position";
    String OLD_ONGOING_CONCERNS_POS = "cpp.ongoing_concerns.position";
    String OLD_REMINDERS_POS = "cpp.reminders.position";
    String SOC_HX_POS = "summary.item.social_hx.position";
    String MED_HX_POS = "summary.item.med_hx.position";
    String ONGOING_POS = "summary.item.ongoing_concerns.position";
    String REMINDERS_POS = "summary.item.reminders.position";
    String SOC_HX_START_DATE = "cpp.social_hx.start_date";
    String SOC_HX_RES_DATE = "cpp.social_hx.res_date";
    String MED_HX_START_DATE = "cpp.med_hx.start_date";
    String MED_HX_RES_DATE = "cpp.med_hx.res_date";
    String MED_HX_TREATMENT = "cpp.med_hx.treatment";
    String MED_HX_PROCEDURE_DATE = "cpp.med_hx.procedure_date";
    String ONGOING_START_DATE = "cpp.ongoing_concerns.start_date";
    String ONGOING_RES_DATE = "cpp.ongoing_concerns.res_date";
    String ONGOING_PROBLEM_STATUS = "cpp.ongoing_concerns.problem_status";
    String REMINDERS_START_DATE = "cpp.reminders.start_date";
    String REMINDERS_RES_DATE = "cpp.reminders.res_date";
    String PREVENTION_POS = "summary.item.prevention.position";
    String FAM_HX_POS = "summary.item.famhx.position";
    String RISK_FACTORS_POS = "summary.item.riskfactors.position";
    String ALLERGIES_POS = "summary.item.allergies.position";
    String MEDS_POS = "summary.item.meds.position";
    String OTHER_MEDS_POS = "summary.item.othermeds.position";
    String ASSESSMENTS_POS = "summary.item.assessments.position";
    String INCOMING_POS = "summary.item.incoming.position";
    String DS_SUPPORT_POS = "summary.item.dssupport.position";

    boolean displaySummaryItem(LoggedInInfo loggedInInfo, String item);

    //TODO: look at appending the spaces
    String getCppExtsItem(LoggedInInfo loggedInInfo, List<CaseManagementNoteExt> noteExtList, String issueCode);


    boolean isCppItem(String issueCode);

    boolean isCustomSummaryEnabled(LoggedInInfo loggedInInfo);

    String getProviderPreference(LoggedInInfo loggedInInfo, String propertyName);

}
