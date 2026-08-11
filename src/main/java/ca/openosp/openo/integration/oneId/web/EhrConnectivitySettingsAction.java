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
package ca.openosp.openo.integration.oneId.web;

import ca.openosp.openo.commn.model.SystemPreferences;
import ca.openosp.openo.commn.model.SystemPreferences.ONEID_KEYS;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.managers.EhrConnectivityManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin screen for the ONE ID gateway settings: client credentials, endpoints, keystore, and the
 * JWKS / issuer / public-key values. Values are stored as {@code SystemPreferences} rows. Secret
 * fields are never rendered back and are only overwritten when a new value is entered, and every
 * add/update of a value is written to the audit log.
 *
 * @since 2026-07-01
 */
public class EhrConnectivitySettingsAction extends ActionSupport {

    private final HttpServletRequest request = ServletActionContext.getRequest();

    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final EhrConnectivityManager ehrConnectivityManager = SpringUtils.getBean(EhrConnectivityManager.class);

    private static final String SEC_OBJECT = "_admin.ehrConnectivity";

    /** Keys whose value is a secret: blanked on display, only written when a new value is entered. */
    private static final List<String> SECRET_KEYS = Arrays.asList(
            ONEID_KEYS.oag_client_secret.name(),
            ONEID_KEYS.keystore_password.name());

    /** The keys this screen manages, in display order. */
    private static final ONEID_KEYS[] EDITABLE_KEYS = {
            ONEID_KEYS.oag_client_id, ONEID_KEYS.oag_client_secret, ONEID_KEYS.oag_public_key,
            ONEID_KEYS.keystore_path, ONEID_KEYS.keystore_alias, ONEID_KEYS.keystore_password,
            ONEID_KEYS.endpoint_authorize, ONEID_KEYS.endpoint_access_token,
            ONEID_KEYS.endpoint_callback, ONEID_KEYS.endpoint_audience,
            ONEID_KEYS.endpoint_jwks, ONEID_KEYS.oneid_issuer,
            ONEID_KEYS.endpoint_end_session, ONEID_KEYS.endpoint_revocation,
            ONEID_KEYS.pcoi_key, ONEID_KEYS.timeout, ONEID_KEYS.viewlet_timeout
    };

    @Override
    public String execute() {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), SEC_OBJECT, "r", null)) {
            throw new SecurityException("missing required sec object (" + SEC_OBJECT + ")");
        }
        loadSettingsForDisplay();
        return "success";
    }

    public String save() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, SEC_OBJECT, "w", null)) {
            throw new SecurityException("missing required sec object (" + SEC_OBJECT + ")");
        }

        String providerNo = loggedInInfo.getLoggedInProviderNo();
        String ip = request.getRemoteAddr();

        for (ONEID_KEYS key : EDITABLE_KEYS) {
            String name = key.name();
            String submitted = request.getParameter(name);
            if (submitted == null) {
                continue;
            }
            submitted = submitted.trim();
            boolean secret = SECRET_KEYS.contains(name);

            // A blank secret means "leave the stored value unchanged".
            if (secret && submitted.isEmpty()) {
                continue;
            }
            if (key == ONEID_KEYS.keystore_path && !submitted.isEmpty()) {
                validateKeystorePath(submitted);
            }

            SystemPreferences pref = ehrConnectivityManager.getConfig(key);
            String previous = pref == null ? "" : pref.getValue();
            if (previous.equals(submitted)) {
                continue;
            }

            ehrConnectivityManager.saveConfig(loggedInInfo, key, submitted);
            auditChange(providerNo, ip, name, previous, submitted, secret);
        }

        request.setAttribute("saved", Boolean.TRUE);
        loadSettingsForDisplay();
        return "success";
    }

    private void loadSettingsForDisplay() {
        List<Map<String, String>> settings = new ArrayList<>();
        for (ONEID_KEYS key : EDITABLE_KEYS) {
            boolean secret = SECRET_KEYS.contains(key.name());
            SystemPreferences pref = ehrConnectivityManager.getConfig(key);
            Map<String, String> setting = new LinkedHashMap<>();
            setting.put("key", key.name());
            setting.put("label", labelFor(key));
            setting.put("type", secret ? "secret" : (key == ONEID_KEYS.oag_public_key ? "textarea" : "text"));
            setting.put("value", secret || pref == null ? "" : pref.getValue());
            settings.add(setting);
        }
        request.setAttribute("settings", settings);
    }

    private String labelFor(ONEID_KEYS key) {
        switch (key) {
            case oag_client_id: return "OAG Client ID";
            case oag_client_secret: return "OAG Client Secret";
            case oag_public_key: return "OAG Public Key";
            case keystore_path: return "Keystore Path";
            case keystore_alias: return "Keystore Alias";
            case keystore_password: return "Keystore Password";
            case endpoint_authorize: return "Authorize Endpoint";
            case endpoint_access_token: return "Token Endpoint";
            case endpoint_callback: return "Callback Path";
            case endpoint_audience: return "Audience";
            case endpoint_jwks: return "JWKS Endpoint";
            case oneid_issuer: return "Expected Issuer";
            case endpoint_end_session: return "End-Session Endpoint";
            case endpoint_revocation: return "Revocation Endpoint";
            case pcoi_key: return "Consent Viewlet Key";
            case timeout: return "Gateway Timeout (seconds)";
            case viewlet_timeout: return "Viewlet Response Timeout (seconds)";
            default: return key.name();
        }
    }

    private void validateKeystorePath(String path) {
        if (path.indexOf('\0') >= 0) {
            throw new SecurityException("Invalid keystore path");
        }
        try {
            Paths.get(path);
        } catch (RuntimeException e) {
            throw new SecurityException("Invalid keystore path");
        }
    }

    private void auditChange(String providerNo, String ip, String key, String previous, String current, boolean secret) {
        String data = secret
                ? key + ": secret value changed"
                : key + ": '" + previous + "' -> '" + current + "'";
        LogAction.addLog(providerNo, LogConst.UPDATE, "ehrConnectivitySettings", key, ip, null, data);
    }
}
