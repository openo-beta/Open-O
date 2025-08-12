/**
 * Purpose: Represents a granted scope/permission within OAuth.
 * Contains: scope identifier and optional description/constraints.
 * Used by: RequestTokenRegistration to capture requested scopes.
 * Notes:
 *   • Keep canonical scope names; validate unknown scopes early.
 */

package org.oscarehr.ws.oauth;

public class OAuth1Permission {
    private String permission;
    private String description;

    public OAuth1Permission(String permission, String description) {
        this.permission = permission;
        this.description = description;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }
}
