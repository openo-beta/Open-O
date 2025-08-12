/**
 * Purpose: Lightweight identity for the OAuth “resource owner” bound to a token/request.
 *
 * What it represents:
 *  • The authenticated user/principal (e.g., provider/staff/patient) that approved the token.
 *  • Stable identifiers only (e.g., userId/providerNo, optional tenant/clinic/site id).
 *  • Optional display fields (username/displayName) and role hints.
 *
 * Used by:
 *  • Authorization step to attach an approver to a RequestToken.
 *  • AccessToken to carry the subject across signed requests.
 *  • Downstream services to resolve permissions/audit without re-querying session state.
 *
 * Design notes:
 *  • Keep it IMMUTABLE where possible (final fields; no setters) for thread safety.
 *  • equals/hashCode should rely on stable IDs (e.g., subjectId + tenant) – never on names.
 *  • Do NOT include secrets or volatile data (passwords, hashes, session ids).
 *  • Minimize PII; avoid logging full objects—log only non-sensitive ids.
 *  • If multi-tenant, include tenant/clinic id and treat it as part of identity.
 *
 * Serialization:
 *  • Safe to serialize as part of token persistence; version carefully if fields change.
 */

package org.oscarehr.ws.oauth;

import java.util.List;

public class UserSubject {

    private String loginName;
    private List<String> roles;

    public UserSubject(String loginName, List<String> roles) {
        this.loginName = loginName;
        this.roles = roles;
    }

    public String getLoginName() {
        return loginName;
    }

    public List<String> getRoles() {
        return roles;
    }
}
