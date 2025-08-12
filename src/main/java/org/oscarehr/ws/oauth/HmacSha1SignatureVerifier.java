/**
 * Purpose: Build the OAuth 1.0a signature base string.
 * Responsibilities:
 *   • Normalize HTTP method, base URI, and parameters per RFC 5849.
 *   • Percent-encode components correctly and sort params lexicographically.
 * Notes:
 *   • Exclude "oauth_signature" from parameter normalization.
 *   • Ensure host/port/protocol match the actual request to avoid 401s.
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

        // For access-token exchange, we must use the **request token secret**
        String tokenSecret = provider.getRequestTokenSecret(r.token);
        if (tokenSecret == null) {
            throw new OAuth1Exception(401, "invalid_request_token");
        }

        verifySignature(r, cfg.getConsumerSecret(), tokenSecret);
        return r.token; // return the token we validated
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
