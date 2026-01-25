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

package ca.openosp.openo.encounter.pageUtil;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.EFormDao;
import ca.openosp.openo.commn.dao.EncounterFormDao;
import ca.openosp.openo.commn.model.EForm;
import ca.openosp.openo.commn.model.EncounterForm;
import ca.openosp.openo.commn.model.ProviderPreference;
import ca.openosp.openo.encounter.data.EctFormData;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import ca.openosp.openo.util.StringUtils;
import ca.openosp.openo.web.admin.ProviderPreferencesUIBean;
import ca.openosp.openo.provider.web.CppPreferencesUIBean;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Displays provider-selected preferred forms (encounter forms and eForms) in the patient chart left navbar.
 * Providers configure their preferred forms via the Provider Preferences page.
 *
 * @since 2026-01-24
 */
public class EctDisplayPreferredForms2Action extends EctDisplayAction {

    private static Logger logger = MiscUtils.getLogger();
    private static final String BGCOLOUR = "4B0082"; // Indigo color to distinguish from Forms (gold) and eForms (green)
    private String cmd = "preferredForms";

    @Override
    public boolean getInfo(EctSessionBean bean, HttpServletRequest request, NavBarDisplayDAO Dao) {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

        // Check if provider has access to forms or eForms
        boolean hasFormAccess = securityInfoManager.hasPrivilege(loggedInInfo, "_form", "r", null);
        boolean hasEFormAccess = securityInfoManager.hasPrivilege(loggedInInfo, "_eform", "r", null);

        if (!hasFormAccess && !hasEFormAccess) {
            return true; // No access, don't show anything
        }

        try {
            String providerNo = loggedInInfo.getLoggedInProviderNo();

            // Check CPP preference - if hidden, don't show the section
            CppPreferencesUIBean cppPrefs = new CppPreferencesUIBean(providerNo);
            cppPrefs.loadValues();
            if (!"SHOW".equals(cppPrefs.getPreferredFormsDisplay())) {
                return true; // Section is hidden by preference
            }

            ProviderPreference providerPreference = ProviderPreferencesUIBean.getProviderPreference(providerNo);

            Collection<String> preferredFormNames = providerPreference.getChartForms();
            Collection<ProviderPreference.EformLink> preferredEForms = providerPreference.getChartEForms();

            String appointmentNo = bean.appointmentNo;
            if (appointmentNo == null && request.getSession().getAttribute("cur_appointment_no") != null) {
                appointmentNo = (String) request.getSession().getAttribute("cur_appointment_no");
            }

            // Set heading
            Dao.setLeftHeading(getText("oscarEncounter.Index.msgPreferredForms"));
            Dao.setLeftURL("return false;"); // No pop-up for header click

            Dao.setRightHeadingID(cmd);
            Dao.setRightURL("return false;");

            // If no preferred forms configured, show a message
            boolean hasPreferredForms = (preferredFormNames != null && !preferredFormNames.isEmpty()) ||
                                        (preferredEForms != null && !preferredEForms.isEmpty());
            if (!hasPreferredForms) {
                NavBarDisplayDAO.Item item = NavBarDisplayDAO.Item();
                item.setTitle("(none configured)");
                item.setColour("808080");
                item.setURL("return false;");
                Dao.addItem(item);
                return true;
            }

            StringBuilder javascript = new StringBuilder("<script type=\"text/javascript\">");
            String js;
            String key;
            int hash;
            String headingColour = request.getParameter("hC");
            if (headingColour == null) {
                headingColour = BGCOLOUR;
            }

            EncounterFormDao encounterFormDao = SpringUtils.getBean(EncounterFormDao.class);
            EFormDao eFormDao = SpringUtils.getBean(EFormDao.class);

            // Process preferred encounter forms
            if (hasFormAccess && preferredFormNames != null) {
                for (String formName : preferredFormNames) {
                    java.util.List<EncounterForm> forms = encounterFormDao.findByFormName(formName);
                    if (forms == null || forms.isEmpty()) {
                        continue;
                    }
                    EncounterForm encounterForm = forms.get(0);
                    if (encounterForm.isHidden()) {
                        continue;
                    }

                    String table = encounterForm.getFormTable();
                    String winName = encounterForm.getFormName() + bean.demographicNo;

                    if (!table.equalsIgnoreCase("")) {
                        EctFormData.PatientForm[] pforms = EctFormData.getPatientFormsFromLocalAndRemote(loggedInInfo, bean.demographicNo, table);

                        if (pforms.length > 0) {
                            // Form has been started for this patient
                            NavBarDisplayDAO.Item item = NavBarDisplayDAO.Item();
                            EctFormData.PatientForm pfrm = pforms[0];

                            DateFormat formatter = new SimpleDateFormat(EctFormData.DATETIME_FORMAT);
                            String serviceDateStr = pfrm.getEdited();
                            Date date = null;

                            try {
                                date = formatter.parse(serviceDateStr);
                            } catch (ParseException ex) {
                                logger.debug("EctDisplayPreferredForms2Action: Error parsing date " + ex.getMessage());
                            }

                            item.setDate(date);

                            String fullTitle = "[F] " + encounterForm.getFormName();
                            String strTitle = StringUtils.maxLenString(fullTitle, MAX_LEN_TITLE, CROP_LEN_TITLE, ELLIPSES);

                            winName = winName + serviceDateStr;
                            hash = Math.abs(winName.hashCode());
                            String url = "popupPage(700,960,'" + hash + "started', '" +
                                    request.getContextPath() +
                                    "/form/forwardshortcutname.do?formname=" +
                                    encounterForm.getFormName() +
                                    "&demographic_no=" + bean.demographicNo +
                                    (pfrm.getRemoteFacilityId() != null ? "&remoteFacilityId=" + pfrm.getRemoteFacilityId() : "") +
                                    (appointmentNo != null ? "&appointmentNo=" + appointmentNo : "") +
                                    "&formId=latest" + "');";

                            key = StringUtils.maxLenString(fullTitle, MAX_LEN_KEY, CROP_LEN_KEY, ELLIPSES) + "(" + serviceDateStr + ")";
                            key = StringEscapeUtils.escapeEcmaScript(key);

                            js = "itemColours['" + key + "'] = '" + headingColour + "'; autoCompList.push('" + key + "'); autoCompleted['" + key + "'] = \"" + url + "\";";
                            javascript.append(js);

                            item.setTitle(strTitle);
                            item.setURL(url + "return false;");
                            item.setLinkTitle(fullTitle + " " + serviceDateStr);
                            Dao.addItem(item);
                        } else {
                            // Form not yet started - add as "new" option
                            NavBarDisplayDAO.Item item = NavBarDisplayDAO.Item();
                            String fullTitle = "[F] " + encounterForm.getFormName() + " (new)";
                            String strTitle = StringUtils.maxLenString(fullTitle, MAX_LEN_TITLE, CROP_LEN_TITLE, ELLIPSES);

                            hash = Math.abs(winName.hashCode());
                            String url = "popupPage(700,960,'" + hash + "new', '" + encounterForm.getFormValue() +
                                    bean.demographicNo + "&formId=0&provNo=" + bean.providerNo +
                                    "&parentAjaxId=" + cmd +
                                    ((appointmentNo != null) ? "&appointmentNo=" + appointmentNo : "") + "');";

                            key = StringUtils.maxLenString(fullTitle, MAX_LEN_KEY, CROP_LEN_KEY, ELLIPSES);
                            key = StringEscapeUtils.escapeEcmaScript(key);

                            js = "itemColours['" + key + "'] = '" + headingColour + "'; autoCompList.push('" + key + "'); autoCompleted['" + key + "'] = \"" + url + "\";";
                            javascript.append(js);

                            item.setTitle(strTitle);
                            item.setURL(url + "return false;");
                            item.setLinkTitle(fullTitle);
                            item.setColour("808080"); // Gray color for new/unstarted forms
                            Dao.addItem(item);
                        }
                    }
                }
            }

            // Process preferred eForms
            if (hasEFormAccess && preferredEForms != null) {
                for (ProviderPreference.EformLink eformLink : preferredEForms) {
                    int eformId = eformLink.getAppointmentScreenEForm();
                    EForm eform = eFormDao.find(eformId);
                    if (eform == null) {
                        continue;
                    }

                    NavBarDisplayDAO.Item item = NavBarDisplayDAO.Item();
                    String fullTitle = "[E] " + eform.getFormName() + " (new)";
                    String strTitle = StringUtils.maxLenString(fullTitle, MAX_LEN_TITLE, CROP_LEN_TITLE, ELLIPSES);

                    String winName = eform.getFormName() + bean.demographicNo;
                    hash = Math.abs(winName.hashCode());
                    String url = "popupPage(700,800,'" + hash + "','" + request.getContextPath() +
                            "/eform/efmformadd_data.jsp?fid=" + eformId +
                            "&demographic_no=" + bean.demographicNo +
                            "&appointment=" + bean.appointmentNo +
                            "&parentAjaxId=" + cmd + "','" + eformId + "_" + bean.demographicNo + "');";

                    key = StringUtils.maxLenString(fullTitle, MAX_LEN_KEY, CROP_LEN_KEY, ELLIPSES);
                    key = StringEscapeUtils.escapeEcmaScript(key);

                    js = "itemColours['" + key + "'] = '" + headingColour + "'; autoCompList.push('" + key + "'); autoCompleted['" + key + "'] = \"" + url + "\";";
                    javascript.append(js);

                    item.setTitle(strTitle);
                    item.setURL(url + "return false;");
                    item.setLinkTitle(eform.getFormName());
                    item.setColour("808080"); // Gray for new eForms
                    Dao.addItem(item);
                }
            }

            javascript.append("</script>");
            Dao.setJavaScript(javascript.toString());

            // Sort by date
            Dao.sortItems(NavBarDisplayDAO.DATESORT_ASC);

            return true;
        } catch (Exception e) {
            logger.error("EctDisplayPreferredForms2Action error:", e);
            return false;
        }
    }

    @Override
    public String getCmd() {
        return cmd;
    }
}
