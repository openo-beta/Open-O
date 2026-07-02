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
package ca.openosp.openo.managers;

import ca.openosp.openo.commn.model.OMDGatewayTransactionLog;
import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.commn.model.SystemPreferences;
import ca.openosp.openo.integration.oneId.OneIdSession;
import ca.openosp.openo.utility.LoggedInInfo;

import java.util.List;

/**
 * Shared service for the ONE ID / EHR connectivity feature. Provides the gateway configuration
 * (stored as {@code SystemPreferences} rows) and read access to the gateway transaction log, for use
 * by the connectivity admin and provider actions.
 *
 * @since 2026-07-01
 */
public interface EhrConnectivityManager {

    /**
     * Returns the stored configuration row for a gateway setting.
     *
     * @param key Enum the configuration key
     * @return SystemPreferences the stored row, or null when the setting has never been saved
     */
    SystemPreferences getConfig(Enum<?> key);

    /**
     * Returns the stored value for a gateway setting.
     *
     * @param key Enum the configuration key
     * @return String the stored value, or null when the setting has never been saved
     */
    String getConfigValue(Enum<?> key);

    /**
     * Returns the stored value for a gateway setting, or a default when it is missing or blank.
     *
     * @param key Enum the configuration key
     * @param defaultValue String the value to return when the setting is missing or blank
     * @return String the stored value, or defaultValue
     */
    String getConfigValue(Enum<?> key, String defaultValue);

    /**
     * Returns a boolean gateway setting, treating the literal "true" as true.
     *
     * @param key Enum the configuration key
     * @param defaultValue boolean the value to return when the setting is missing or blank
     * @return boolean the stored flag, or defaultValue
     */
    boolean getConfigFlag(Enum<?> key, boolean defaultValue);

    /**
     * Creates or updates the stored value for a gateway setting.
     *
     * @param loggedInInfo LoggedInInfo the acting user, checked for the admin privilege
     * @param key Enum the configuration key
     * @param value String the value to store
     * @return SystemPreferences the saved row
     */
    SystemPreferences saveConfig(LoggedInInfo loggedInInfo, Enum<?> key, String value);

    /**
     * Returns the most recent gateway transaction log rows, optionally filtered by provider or,
     * when no provider is given, by external system.
     *
     * @param loggedInInfo LoggedInInfo the acting user, checked for the admin privilege
     * @param providerNo String restrict to one provider, or null
     * @param externalSystem String restrict to one external system when providerNo is null, or null
     * @param maxRows int the maximum number of rows to return
     * @return List&lt;OMDGatewayTransactionLog&gt; the matching rows, newest first, capped at maxRows
     */
    List<OMDGatewayTransactionLog> getRecentLogs(LoggedInInfo loggedInInfo, String providerNo, String externalSystem, int maxRows);

    /**
     * Returns the gateway transaction log rows for one provider, newest first.
     *
     * @param loggedInInfo LoggedInInfo the acting user, checked for the admin privilege
     * @param providerNo String the provider number
     * @return List&lt;OMDGatewayTransactionLog&gt; the matching rows
     */
    List<OMDGatewayTransactionLog> findLogsByProviderNo(LoggedInInfo loggedInInfo, String providerNo);

    /**
     * Returns the gateway transaction log rows for one external system, newest first.
     *
     * @param loggedInInfo LoggedInInfo the acting user, checked for the admin privilege
     * @param externalSystem String the external system identifier
     * @return List&lt;OMDGatewayTransactionLog&gt; the matching rows
     */
    List<OMDGatewayTransactionLog> findLogsByExternalSystem(LoggedInInfo loggedInInfo, String externalSystem);

    /**
     * Returns the provider security records linked to a ONE ID subject.
     *
     * @param subject String the ONE ID subject (sub), stored as the provider's oneIdKey
     * @return List&lt;Security&gt; the matching security records, empty when none are linked
     */
    List<Security> findProvidersByOneId(String subject);

    /**
     * Creates or updates the persisted ONE ID session for a provider.
     *
     * @param oneIdSession OneIdSession the session row to store, keyed by provider number
     */
    void saveOneIdSession(OneIdSession oneIdSession);

    /**
     * Removes the persisted ONE ID session for a provider so it is no longer rehydrated on later
     * requests. No-op when no session exists.
     *
     * @param providerNo String the provider number
     */
    void removeOneIdSession(String providerNo);
}
