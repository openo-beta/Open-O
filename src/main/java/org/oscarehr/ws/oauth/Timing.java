/**
 * Purpose: Represents the authenticated end-user (resource owner) identity.
 * Contains: user id/username and optional roles/attributes.
 * Used by: Linking approved tokens to a user during authorization.
 * Notes:
 *   • Do not place sensitive PII beyond what’s needed to bind tokens.
 */

package org.oscarehr.ws.oauth;

public final class Timing {
    private Timing() {}
    public static boolean safeEquals(String a, String b) {
        if (a == null || b == null) return false;
        int len = Math.max(a.length(), b.length());
        int result = 0;
        for (int i = 0; i < len; i++) {
            char ca = i < a.length() ? a.charAt(i) : 0;
            char cb = i < b.length() ? b.charAt(i) : 0;
            result |= ca ^ cb;
        }
        return result == 0 && a.length() == b.length();
    }
}
