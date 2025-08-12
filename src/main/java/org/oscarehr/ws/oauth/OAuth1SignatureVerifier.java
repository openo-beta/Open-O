/**
 * Purpose: High-level server-side verifier for OAuth 1.0a requests.
 * Responsibilities:
 *   • Validate timestamp/nonce (anti-replay).
 *   • Build base string and verify signature (HMAC-SHA1/plaintext).
 *   • Throw OAuth1Exception(401, ...) on failure.
 * Notes:
 *   • Delegates crypto to HmacSha1SignatureVerifier.
 *   • Keep constant-time comparisons for signatures.
 */
package org.oscarehr.ws.oauth;

import javax.servlet.http.HttpServletRequest;
import oscar.login.AppOAuth1Config;

public interface OAuth1SignatureVerifier {
    String verifySignature(HttpServletRequest req, AppOAuth1Config cfg);
    String getProviderNo(String token);
}
