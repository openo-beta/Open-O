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
import ca.openosp.openo.commn.model.UAO;
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
     * Returns the provider security records linked to a ONE ID subject. Called only from the ONE ID
     * callback before a session exists, so it carries no privilege check; the trust boundary is the
     * OAuth state and nonce plus the id-token signature that produced the subject.
     *
     * @param subject String the ONE ID subject (sub), stored as the provider's oneIdKey
     * @return List&lt;Security&gt; the matching security records, empty when none are linked
     */
    List<Security> findProvidersByOneId(String subject);

    /**
     * Creates or updates the persisted ONE ID session for a provider. Called only from the ONE ID
     * callback while it establishes the session, so it carries no privilege check; the trust boundary
     * is the OAuth handshake that just authenticated the provider.
     *
     * @param oneIdSession OneIdSession the session row to store, keyed by provider number
     */
    void saveOneIdSession(OneIdSession oneIdSession);

    /**
     * Removes the persisted ONE ID session for a provider so it is no longer rehydrated on later
     * requests. No-op when no session exists.
     *
     * @param loggedInInfo LoggedInInfo the acting user, allowed as an admin or the owning provider
     * @param providerNo String the provider number
     */
    void removeOneIdSession(LoggedInInfo loggedInInfo, String providerNo);

    /**
     * Clears the ONE ID association (key and email) from a provider's security record.
     *
     * @param loggedInInfo LoggedInInfo the acting user, allowed as an admin or the owning provider
     * @param providerNo String the provider number
     * @return boolean true when a security record was found and cleared
     */
    boolean clearOneIdBinding(LoggedInInfo loggedInInfo, String providerNo);

    /**
     * Returns a provider's active UAO values, the default first.
     *
     * @param loggedInInfo LoggedInInfo the acting user, allowed as an admin or the owning provider
     * @param providerNo String the provider number
     * @return List&lt;UAO&gt; the active UAO values, default-first
     */
    List<UAO> findUaosByProvider(LoggedInInfo loggedInInfo, String providerNo);

    /**
     * Returns a single UAO value by id, or null when none exists.
     *
     * @param loggedInInfo LoggedInInfo the acting user, allowed as an admin or the value's owning provider
     * @param id Integer the UAO id
     * @return UAO the value, or null
     */
    UAO findUao(LoggedInInfo loggedInInfo, Integer id);

    /**
     * Persists a new UAO value.
     *
     * @param loggedInInfo LoggedInInfo the acting user, checked for the admin privilege
     * @param uao UAO the value to create
     */
    void createUao(LoggedInInfo loggedInInfo, UAO uao);

    /**
     * Updates an existing UAO value.
     *
     * @param loggedInInfo LoggedInInfo the acting user, checked for the admin privilege
     * @param uao UAO the value to update
     */
    void updateUao(LoggedInInfo loggedInInfo, UAO uao);

    /**
     * Makes one of a provider's UAO values the default and clears the flag on the others.
     *
     * @param loggedInInfo LoggedInInfo the acting user, allowed as an admin or the owning provider
     * @param uao UAO the value to make default
     * @param providerNo String the provider number
     */
    void setDefaultUao(LoggedInInfo loggedInInfo, UAO uao, String providerNo);

    /**
     * Attaches a UAO to the provider's persisted ONE ID session so it survives across requests and
     * every gateway call carries it. No-op when the provider has no ONE ID session.
     *
     * @param loggedInInfo LoggedInInfo the acting user, allowed as an admin or the owning provider
     * @param providerNo String the provider number
     * @param uaoValue String the UAO value sent to the gateway
     * @param uaoFriendlyName String the human-readable UAO label
     */
    void setSessionUao(LoggedInInfo loggedInInfo, String providerNo, String uaoValue, String uaoFriendlyName);
}
