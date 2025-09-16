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

package ca.openosp.openo.web;

import ca.openosp.openo.commn.dao.EFormDao;
import ca.openosp.openo.commn.dao.ProviderPreferenceDao;
import ca.openosp.openo.commn.model.EForm;
import ca.openosp.openo.commn.model.ProviderPreference;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

/**
 * UI Bean for appointment scheduling interface customization and form display utilities.
 * This class provides functionality for managing how clinical forms and links are displayed
 * on provider appointment screens, including length-limited form names and electronic form retrieval.
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Dynamic form name truncation based on provider preferences</li>
 *   <li>Electronic form retrieval for appointment scheduling interfaces</li>
 *   <li>Provider-specific display length customization</li>
 * </ul>
 *
 * <p>This utility supports the appointment scheduling workflow by ensuring that form links
 * are displayed in a consistent, space-efficient manner according to each provider's
 * interface preferences.</p>
 *
 * @since October 21, 2010
 * @see ProviderPreference
 * @see EForm
 */
public final class AppointmentProviderAdminDayUIBean {
    private static EFormDao eFormDao = (EFormDao) SpringUtils.getBean(EFormDao.class);
    private static ProviderPreferenceDao providerPreferenceDao = (ProviderPreferenceDao) SpringUtils.getBean(ProviderPreferenceDao.class);

    /**
     * Returns a truncated form name that respects the provider's display length preferences.
     * This method ensures that form links on appointment screens don't exceed the provider's
     * configured maximum display length, improving interface layout and usability.
     *
     * <p>The truncation behavior:</p>
     * <ul>
     *   <li>If form name is within length limit, returns original name</li>
     *   <li>If form name exceeds limit, truncates to (maxLength - 1) characters and adds "."</li>
     *   <li>Default maximum length is 3 characters if no preference is set</li>
     * </ul>
     *
     * @param loggedInInfo LoggedInInfo the current provider's session information
     * @param formName String the original form name to potentially truncate
     * @return String the truncated form name with ellipsis if shortened, or original name if within limit
     */
    public static String getLengthLimitedLinkName(LoggedInInfo loggedInInfo, String formName) {
        int maxLength = 3;

        ProviderPreference providerPreference = providerPreferenceDao.find(loggedInInfo.getLoggedInProviderNo());
        if (providerPreference != null) maxLength = providerPreference.getAppointmentScreenLinkNameDisplayLength();

        if (formName.length() <= maxLength) return (formName);
        else return (formName.substring(0, maxLength - 1) + ".");
    }

    /**
     * Retrieves an electronic form by its unique identifier.
     * This method is used to fetch eForm details for display on appointment scheduling
     * interfaces and clinical workflow screens.
     *
     * @param eformId Integer the unique identifier of the electronic form
     * @return EForm the electronic form record, or null if not found
     */
    public static EForm getEForms(Integer eformId) {
        return (eFormDao.find(eformId));
    }
}
