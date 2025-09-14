//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package ca.openosp.openo.web.admin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;
import ca.openosp.openo.commn.dao.EFormDao;
import ca.openosp.openo.commn.dao.EncounterFormDao;
import ca.openosp.openo.commn.dao.ProviderPreferenceDao;
import ca.openosp.openo.commn.model.EForm;
import ca.openosp.openo.commn.model.EncounterForm;
import ca.openosp.openo.commn.model.ProviderPreference;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import ca.openosp.openo.utility.WebUtils;

/**
 * UI Bean for managing healthcare provider preference settings in the OpenO EMR system.
 * This class handles the complex logic for updating, retrieving, and managing provider-specific
 * configurations that customize the clinical workflow experience for individual healthcare providers.
 *
 * <p>Provider preference categories include:</p>
 * <ul>
 *   <li>Clinical workflow settings (tickler warnings, scheduling preferences)</li>
 *   <li>Billing preferences (default codes, service types, billing locations)</li>
 *   <li>Appointment screen customization (forms, eforms, quick links)</li>
 *   <li>Electronic prescription integration (eRx settings)</li>
 *   <li>CAISI integration settings for program management</li>
 *   <li>Display and interface preferences (color templates, form name lengths)</li>
 * </ul>
 *
 * <p>The class provides both read and write operations for preferences, with automatic
 * preference record creation for new providers. It supports session-based fallback values
 * and handles complex form collections for appointment screen customization.</p>
 *
 * @since September 5, 2010
 * @see ProviderPreference
 * @see EForm
 * @see EncounterForm
 */
public final class ProviderPreferencesUIBean {

    private static final ProviderPreferenceDao providerPreferenceDao = (ProviderPreferenceDao) SpringUtils.getBean(ProviderPreferenceDao.class);
    private static final EFormDao eFormDao = (EFormDao) SpringUtils.getBean(EFormDao.class);
    private static final EncounterFormDao encounterFormDao = (EncounterFormDao) SpringUtils.getBean(EncounterFormDao.class);

    /**
     * Updates or creates provider preferences based on HTTP request parameters and session attributes.
     * This method processes a wide range of preference settings including clinical workflow,
     * billing defaults, appointment screen customization, and external system integrations.
     *
     * <p>Processed preference categories:</p>
     * <ul>
     *   <li>Tickler warning settings for clinical alerts</li>
     *   <li>CAISI program management module settings</li>
     *   <li>Billing preferences (service types, locations, diagnosis codes)</li>
     *   <li>Scheduling preferences (hours, intervals, color templates)</li>
     *   <li>Appointment screen forms and electronic forms selection</li>
     *   <li>Electronic prescription (eRx) integration settings</li>
     *   <li>Prescription QR code printing preferences</li>
     * </ul>
     *
     * @param request HttpServletRequest containing preference parameters from the admin interface
     * @return ProviderPreference the updated or newly created provider preference record
     */
    public static final ProviderPreference updateOrCreateProviderPreferences(HttpServletRequest request) {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();

        ProviderPreference providerPreference = getProviderPreference(providerNo);

        // Update preferences based on request parameters and session fallback values
        String temp;
        HttpSession session = request.getSession();

        boolean updatePreferences = Boolean.parseBoolean(request.getParameter("updatePreference"));

        // Configure tickler warning window preferences for clinical alerts
        temp = StringUtils.trimToNull(request.getParameter("new_tickler_warning_window"));
        if (temp != null) {
            providerPreference.setNewTicklerWarningWindow(temp);
        } else {
            temp = StringUtils.trimToNull((String) session.getAttribute("newticklerwarningwindow"));
            if (temp != null) providerPreference.setNewTicklerWarningWindow(temp);
        }

        // Configure default Program Management Module (PMM) settings for CAISI integration
        temp = StringUtils.trimToNull(request.getParameter("default_pmm"));
        if (temp != null) {
            providerPreference.setDefaultCaisiPmm(temp);
        } else {
            temp = StringUtils.trimToNull((String) session.getAttribute("default_pmm"));
            if (temp == null) providerPreference.setDefaultCaisiPmm("disabled");
            else providerPreference.setDefaultCaisiPmm(temp);
        }

        // Configure billing record handling preferences (edit vs delete behavior)
        temp = StringUtils.trimToNull(request.getParameter("caisiBillingPreferenceNotDelete"));
        if (temp != null) {
            try {
                providerPreference.setDefaultDoNotDeleteBilling(Integer.parseInt(temp));
            } catch (NumberFormatException e) {
                MiscUtils.getLogger().error("Error", e);
            }
        } else {
            temp = StringUtils.trimToNull(String.valueOf(session.getAttribute("caisiBillingPreferenceNotDelete")));
            if (temp == null)
                providerPreference.setDefaultDoNotDeleteBilling(0);
            else {
                int defBilling = 0;
                try {
                    defBilling = Integer.parseInt(temp);
                } catch (NumberFormatException e) {
                    Log.warn("warning", e);
                }
                providerPreference.setDefaultDoNotDeleteBilling(defBilling);
            }
        }

        // Configure default diagnosis code for billing entries
        temp = StringUtils.trimToNull(request.getParameter("dxCode"));
        if (temp != null) providerPreference.setDefaultDxCode(temp);

        try {
            Integer startHour = Integer.parseInt(StringUtils.trimToNull(request.getParameter("start_hour")));
            Integer endHour = Integer.parseInt(StringUtils.trimToNull(request.getParameter("end_hour")));
            if (startHour < endHour) {
                if (startHour >= 0 && startHour <= 23) {
                    providerPreference.setStartHour(startHour);
                }
                if (endHour >= 0 && endHour <= 23) {
                    providerPreference.setEndHour(endHour);
                }
            }


        } catch (Exception e) {
            MiscUtils.getLogger().warn("user entered invalid values");
        }
        // Configure scheduling and display preferences

        temp = StringUtils.trimToNull(request.getParameter("every_min"));
        if (temp != null) providerPreference.setEveryMin(Integer.parseInt(temp));

        temp = StringUtils.trimToNull(request.getParameter("mygroup_no"));
        if (temp != null) providerPreference.setMyGroupNo(temp);

        temp = StringUtils.trimToNull(request.getParameter("default_servicetype"));
        if (temp != null) providerPreference.setDefaultServiceType(temp);

        temp = StringUtils.trimToNull(request.getParameter("default_location"));
        if (temp != null) providerPreference.setDefaultBillingLocation(temp);


        temp = StringUtils.trimToNull(request.getParameter("color_template"));
        if (temp != null) providerPreference.setColourTemplate(temp);

        providerPreference.setPrintQrCodeOnPrescriptions(WebUtils.isChecked(request, "prescriptionQrCodes"));

        // Configure encounter forms displayed on appointment scheduling screen
        temp = StringUtils.trimToNull(request.getParameter("appointmentScreenFormsNameDisplayLength"));
        if (temp != null) providerPreference.setAppointmentScreenLinkNameDisplayLength(Integer.parseInt(temp));

        String[] formNames = request.getParameterValues("encounterFormName");
        Collection<String> formNamesList = providerPreference.getAppointmentScreenForms();

        formNamesList.clear();
        if (formNames != null) {
            for (String formName : formNames) {
                formNamesList.add(formName);
            }
        }

        // Configure electronic forms (eForms) displayed on appointment scheduling screen
        // Each eForm is stored with both ID and name for display purposes in the schedule interface
        String[] formIds = request.getParameterValues("eformId");
        Collection<ProviderPreference.EformLink> eFormsIdsList = providerPreference.getAppointmentScreenEForms();
        eFormsIdsList.clear();
        if (formIds != null) {
            for (String formId : formIds) {
                Integer formIdInteger = Integer.parseInt(formId);
                EForm eForm = eFormDao.find(formIdInteger);
                if (eForm != null) {
                    eFormsIdsList.add(new ProviderPreference.EformLink(formIdInteger, eForm.getFormName()));
                } else {
                    MiscUtils.getLogger().warn("EForm not found for id of:" + formIdInteger);
                }
            }
        }

        // Configure external electronic prescription (eRx) system integration settings
        providerPreference.setERxEnabled(WebUtils.isChecked(request, "erx_enable"));

        temp = StringUtils.trimToNull(request.getParameter("erx_username"));
        if (temp != null) providerPreference.setERxUsername(temp);

        temp = StringUtils.trimToNull(request.getParameter("erx_password"));
        if (temp != null) providerPreference.setERxPassword(temp);

        temp = StringUtils.trimToNull(request.getParameter("erx_facility"));
        if (temp != null) providerPreference.setERxFacility(temp);

        providerPreference.setERxTrainingMode(WebUtils.isChecked(request, "erx_training_mode"));

        temp = StringUtils.trimToNull(request.getParameter("erx_sso_url"));
        if (temp != null) providerPreference.setERx_SSO_URL(temp);

        providerPreferenceDao.merge(providerPreference);

        return (providerPreference);
    }

    /**
     * Retrieves provider preferences for the specified provider, creating default preferences if none exist.
     * This method ensures that every provider has a preference record available, automatically creating
     * one with default values if the provider doesn't have preferences configured yet.
     *
     * <p>This lazy initialization approach handles the common scenario where providers are created
     * but preferences are not immediately configured, ensuring the application doesn't fail when
     * accessing preference-dependent features.</p>
     *
     * @param providerNo String the unique provider identifier
     * @return ProviderPreference the existing or newly created provider preferences
     */
    public static ProviderPreference getProviderPreference(String providerNo) {

        ProviderPreference providerPreference = providerPreferenceDao.find(providerNo);

        if (providerPreference == null) {
            providerPreference = new ProviderPreference();
            providerPreference.setProviderNo(providerNo);
            providerPreferenceDao.persist(providerPreference);
        }

        return providerPreference;
    }

    /**
     * Retrieves all active electronic forms (eForms) available in the system, sorted by form name.
     * These eForms can be configured to appear on provider appointment screens for quick access
     * during patient encounters and scheduling workflows.
     *
     * @return List<EForm> all active eForms sorted alphabetically by form name
     */
    public static List<EForm> getAllEForms() {
        List<EForm> results = eFormDao.findAll(true);
        Collections.sort(results, EForm.FORM_NAME_COMPARATOR);
        return (results);
    }

    /**
     * Retrieves all encounter forms available in the system, sorted by form name.
     * Encounter forms are clinical templates used during patient visits and can be
     * configured to appear on provider appointment screens for quick access.
     *
     * @return List<EncounterForm> all encounter forms sorted alphabetically by form name
     */
    public static List<EncounterForm> getAllEncounterForms() {
        List<EncounterForm> results = encounterFormDao.findAll();
        Collections.sort(results, EncounterForm.FORM_NAME_COMPARATOR);
        return (results);
    }

    /**
     * Retrieves the encounter form names selected by a provider for display on their appointment screen.
     * These forms will appear as quick-access links during scheduling and patient encounters.
     *
     * @param providerNo String the unique provider identifier
     * @return Collection<String> the names of encounter forms configured for the provider's appointment screen
     */
    public static Collection<String> getCheckedEncounterFormNames(String providerNo) {
        ProviderPreference providerPreference = getProviderPreference(providerNo);
        return (providerPreference.getAppointmentScreenForms());
    }

    /**
     * Retrieves the electronic form links selected by a provider for display on their appointment screen.
     * Each link contains both the eForm ID and display name for presentation in the scheduling interface.
     *
     * @param providerNo String the unique provider identifier
     * @return Collection<ProviderPreference.EformLink> the eForm links configured for the provider's appointment screen
     */
    public static Collection<ProviderPreference.EformLink> getCheckedEFormIds(String providerNo) {
        ProviderPreference providerPreference = getProviderPreference(providerNo);
        return (providerPreference.getAppointmentScreenEForms());
    }

    /**
     * Retrieves provider preferences by provider number without automatic creation.
     * Unlike getProviderPreference(), this method returns null if no preferences exist
     * rather than creating default preferences.
     *
     * @param providerNo String the unique provider identifier
     * @return ProviderPreference the existing provider preferences, or null if none exist
     */
    public static ProviderPreference getProviderPreferenceByProviderNo(String providerNo) {
        return providerPreferenceDao.find(providerNo);
    }

    /**
     * Retrieves the quick links configured by a provider for display on their appointment screen.
     * Quick links provide fast access to frequently used external websites, resources, or
     * internal system functions during clinical workflows.
     *
     * @param providerNo String the unique provider identifier
     * @return Collection<ProviderPreference.QuickLink> the quick links configured for the provider's appointment screen
     */
    public static Collection<ProviderPreference.QuickLink> getQuickLinks(String providerNo) {
        ProviderPreference providerPreference = getProviderPreference(providerNo);

        return (providerPreference.getAppointmentScreenQuickLinks());
    }

    /**
     * Adds a new quick link to the provider's appointment screen configuration.
     * Quick links enable providers to access frequently used resources directly from
     * the scheduling interface, improving clinical workflow efficiency.
     *
     * @param providerNo String the unique provider identifier
     * @param name String the display name for the quick link
     * @param url String the URL or resource location for the quick link
     */
    public static void addQuickLink(String providerNo, String name, String url) {
        ProviderPreference providerPreference = getProviderPreference(providerNo);

        Collection<ProviderPreference.QuickLink> quickLinks = providerPreference.getAppointmentScreenQuickLinks();

        ProviderPreference.QuickLink quickLink = new ProviderPreference.QuickLink();
        quickLink.setName(name);
        quickLink.setUrl(url);

        quickLinks.add(quickLink);

        providerPreferenceDao.merge(providerPreference);
    }

    /**
     * Removes a quick link from the provider's appointment screen configuration.
     * This method searches for a quick link by name and removes the first matching entry.
     *
     * @param providerNo String the unique provider identifier
     * @param name String the display name of the quick link to remove
     */
    public static void removeQuickLink(String providerNo, String name) {
        ProviderPreference providerPreference = getProviderPreference(providerNo);

        Collection<ProviderPreference.QuickLink> quickLinks = providerPreference.getAppointmentScreenQuickLinks();

        for (ProviderPreference.QuickLink quickLink : quickLinks) {
            if (name.equals(quickLink.getName())) {
                // Safe to modify collection during iteration since we break immediately after removal
                quickLinks.remove(quickLink);
                break;
            }
        }

        providerPreferenceDao.merge(providerPreference);
    }
}
