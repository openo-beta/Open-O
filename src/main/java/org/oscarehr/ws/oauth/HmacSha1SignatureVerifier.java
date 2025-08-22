/**
 * File: HmacSha1SignatureVerifier.java
 *
 * Purpose:
 *   Implements OAuth 1.0a signature verification for the HMAC-SHA1 method.
 *   Responsible for building the RFC 5849-compliant base string and
 *   comparing the client-provided signature against a locally computed one.
 *
 * Responsibilities:
 *   • Parse and validate OAuth1 parameters from the incoming request.
 *   • Build the normalized signature base string from method, URI, and params.
 *   • Compute the HMAC-SHA1 signature using the consumer secret and token secret.
 *   • Compare the computed signature against the client’s using constant-time
 *     equality checks.
 *
 * Context / Why Added:
 *   Replaces CXF’s built-in OAuth verifier with an explicit, Spring-managed
 *   service. Part of the CXF → ScribeJava migration to make OAuth request
 *   verification more deterministic and testable.
 *
 * Notes:
 *   • Throws OAuth1Exception with HTTP-like codes on invalid/unsupported input.
 *   • Delegates provider lookups (request/access token secrets, providerNo)
 *     to OscarOAuthDataProvider.
 *   • Relies on OAuth1BaseString for normalization and Timing.safeEquals
 *     for constant-time comparison.
 */


package org.oscarehr.ws.oauth;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import oscar.login.AppOAuth1Config;
import oscar.login.OscarOAuthDataProvider;
import org.oscarehr.ws.oauth.util.OAuth1ParamParser;

@Service
@Primary
public class HmacSha1SignatureVerifier implements OAuth1SignatureVerifier {

    @Autowired private OAuth1ParamParser parser;
    @Autowired private OscarOAuthDataProvider provider;

    // Interface method – wraps your low-level verifier
    @Override
    public String verifySignature(HttpServletRequest req, AppOAuth1Config cfg) {
        OAuth1Request r = parser.parseFromRequest(req);

        if (!"HMAC-SHA1".equalsIgnoreCase(r.signatureMethod)) {
            throw new OAuth1Exception(400, "unsupported_signature_method");
        }

        // For /oauth/initiate there is NO oauth_token -> tokenSecret must be ""
        String tokenSecret = "";
        if (r.token != null && !r.token.isEmpty()) {
            // Only needed for access-token exchange or signed resource calls
            tokenSecret = provider.getRequestTokenSecret(r.token);
            if (tokenSecret == null) {
                throw new OAuth1Exception(401, "invalid_request_token");
            }
        }

        verifySignature(r, cfg.getConsumerSecret(), tokenSecret);
        return r.token; // will be null for initiate, which is fine
    }


    // Interface method
    @Override
    public String getProviderNo(String token) {
        return provider.getProviderNoByAccessToken(token);
    }

    // Your original low-level verifier
    public void verifySignature(OAuth1Request req, String consumerSecret, String tokenSecret) {
        try {
            String base = OAuth1BaseString.build(req);
            String key  = OAuth1BaseString.percentEncode(nullToEmpty(consumerSecret)) + '&'
                        + OAuth1BaseString.percentEncode(nullToEmpty(tokenSecret));

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            String calc = Base64.getEncoder().encodeToString(mac.doFinal(base.getBytes(StandardCharsets.UTF_8)));

            if (!Timing.safeEquals(calc, req.signature)) {
                throw new OAuth1Exception(401, "invalid_signature");
            }
        } catch (OAuth1Exception e) {
            throw e;
        } catch (Exception e) {
            throw new OAuth1Exception(400, "signature_error");
        }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
