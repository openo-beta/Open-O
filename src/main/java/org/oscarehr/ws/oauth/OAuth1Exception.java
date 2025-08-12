/**
 * Purpose: Exception type for OAuth 1.0a failures.
 * Encodes an HTTP status and a short error code/message suitable for clients.
 * Used by: Signature/nonce/timestamp/callback validation and token state checks.
 * Notes:
 *   • Prefer 401 for auth failures, 400 for malformed requests, 500 for server errors.
 *   • Avoid including secrets in exception messages.
 */

package org.oscarehr.ws.oauth;

public class OAuth1Exception extends RuntimeException {
    private final int httpCode;
    public OAuth1Exception(int httpCode, String message) {
        super(message);
        this.httpCode = httpCode;
    }
    public int getHttpCode() { return httpCode; }
}
