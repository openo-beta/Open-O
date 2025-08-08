package org.oscarehr.ws.oauth;

/**
 * Simple replacement for CXF's OAuthPermission.
 * Represents a single scope/permission.
 */
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
