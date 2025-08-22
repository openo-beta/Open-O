/**
 * File: Timing.java
 *
 * Purpose:
 *   Security utility for performing constant-time operations to prevent
 *   timing attacks when comparing secrets (e.g., OAuth signatures).
 *
 * Responsibilities:
 *   • Provide a constant-time string equality check.
 *   • Avoid leaking information about matching prefixes/characters through
 *     branch timing.
 *
 * Context / Why Added:
 *   Used by signature verifiers (e.g., HMAC-SHA1) to compare the computed
 *   signature with the client-provided one safely.
 *
 * Notes:
 *   • All methods are static and side-effect free.
 *   • Returns false if either input is null.
 *   • Compare full length and character values to ensure timing is uniform.
 */


package org.oscarehr.ws.oauth;

public final class Timing {
    private Timing() {}

    /**
     * Compare two strings in constant time to mitigate timing attacks.
     * Returns true only if both are non-null and equal.
     */
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
