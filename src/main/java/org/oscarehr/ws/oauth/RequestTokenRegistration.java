/**
 * Purpose: Time/constant-time helpers for security-sensitive comparisons.
 * Responsibilities:
 *   • Constant-time equals for signatures/secrets.
 *   • Clock-skew checks for OAuth timestamp validation.
 * Notes:
 *   • Keep methods static and side-effect free.
 *   • Unit test with edge values (past, future, boundary skew).
 */

package org.oscarehr.ws.oauth;

public class RequestTokenRegistration {
    private Client client;
    private String callback;
    private String[] scopes;

    public RequestTokenRegistration(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String[] getScopes() {
        return scopes;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }
}
