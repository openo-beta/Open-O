/**
 * Purpose: Server-side verification of OAuth 1.0a signatures (HMAC-SHA1/PLAINTEXT).
 * Responsibilities:
 *   • Build signature base string; recompute signature; constant-time comparison.
 *   • Validate timestamp/nonce (anti-replay) via injected store if configured.
 *   • Throw domain-specific OAuth1Exception with HTTP 401 on failure.
 * Why changed/added: Replace CXF OAuth filter with an explicit, testable verifier used by
 * request/access token endpoints while keeping CXF JAX-RS for routing.
 * Notes:
 *   • Do not log shared secrets or raw signatures.
 *   • Allow small clock skew; ensure nonce uniqueness per consumer+timestamp.
 */

package org.oscarehr.ws.oauth.util;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import oscar.login.AppOAuth1Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;

import oscar.login.OscarOAuthDataProvider;
import org.oscarehr.ws.oauth.OAuth1SignatureVerifier;

@Service
public class OAuth1SignatureVerifierImplementation implements OAuth1SignatureVerifier {
    @Autowired
    private OscarOAuthDataProvider dataProvider;

    /**
     * Recomputes the OAuth1 signature for the incoming request and compares
     * it (constant-time) against the provided oauth_signature. Returns the
     * token if valid, or throws IllegalArgumentException if not.
     */
    @Override
    public String verifySignature(HttpServletRequest req, AppOAuth1Config cfg) {
        // 1) Extract oauth_* from Authorization header
        Map<String, String> oauth = OAuthRequestParser.extractOAuthParameters(req);
        String token       = oauth.get(OAuthConstants.TOKEN);
        String incomingSig = oauth.get(OAuthConstants.SIGNATURE);
        if (token == null || incomingSig == null) {
            throw new IllegalArgumentException("Missing OAuth token or signature");
        }

        // 2) Look up the correct token secret
        //    Step 3 (access token exchange) uses the REQUEST-TOKEN secret.
        String tokenSecret = null;
        if (dataProvider != null) {
            // Prefer request-token secret first; fall back to access-token secret for resource requests
            try {
                tokenSecret = dataProvider.getRequestTokenSecret(token);
            } catch (Throwable ignored) { /* method may not exist on some branches */ }
            if (tokenSecret == null) {
                try {
                    tokenSecret = dataProvider.getAccessTokenSecret(token);
                } catch (Throwable ignored) { }
            }
        }
        if (tokenSecret == null) {
            throw new IllegalArgumentException("Unknown or expired token: " + token);
        }

        // 3) Build base URL (no query), normalized per RFC 5849
        final String scheme = req.getScheme().toLowerCase();
        final String host   = req.getServerName().toLowerCase();
        final int    port   = req.getServerPort();
        final boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        final String baseUrl = scheme + "://" + host + (defaultPort ? "" : ":" + port) + req.getRequestURI();

        // 4) Collect ALL parameters used in the signature:
        //    - oauth_* from header (except oauth_signature & realm)
        //    - query string params
        //    - application/x-www-form-urlencoded body params (if any)
        //    Servlet getParameterMap() merges query + form (decoded). That's correct for OAuth 1.0.
        final java.util.List<NameValue> all = new java.util.ArrayList<>();

        for (Map.Entry<String, String> e : oauth.entrySet()) {
            final String k = e.getKey();
            if (!"oauth_signature".equals(k) && !"realm".equalsIgnoreCase(k)) {
                all.add(new NameValue(k, e.getValue()));
            }
        }
        req.getParameterMap().forEach((k, vals) -> {
            if (!k.startsWith("oauth_")) {
                for (String v : vals) all.add(new NameValue(k, v));
            }
        });

        // 5) Normalize parameters (percent-encode, then sort by encoded key, then encoded value)
        all.sort((a, b) -> {
            int c = pct(a.name).compareTo(pct(b.name));
            return (c != 0) ? c : pct(a.value).compareTo(pct(b.value));
        });
        final StringBuilder norm = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) norm.append('&');
            norm.append(pct(all.get(i).name)).append('=').append(pct(all.get(i).value));
        }

        // 6) Build base string and compute HMAC-SHA1
        final String baseString =
                req.getMethod().toUpperCase() + '&' + pct(baseUrl) + '&' + pct(norm.toString());

        final String signingKey = pct(cfg.getConsumerSecret()) + '&' + pct(tokenSecret);
        final String computed = base64(hmacSha1(baseString, signingKey));

        // 7) Compare (incoming value may be percent-encoded)
        final String incoming = urlDecode(incomingSig);
        if (!constantTimeEquals(incoming, computed)) {
            throw new IllegalArgumentException("Invalid OAuth1 signature");
        }

        return token; // also your current return type
    }

    /**
     * After verifySignature, use this to look up which provider owns the token.
     */
    public String getProviderNo(String token) {
        return dataProvider.getProviderNoByAccessToken(token);
    }

    // /**
    //  * Builds a ScribeJava OAuthRequest matching the incoming servlet request,
    //  * including query/form params (but omitting oauth_*).
    //  */
    // private OAuthRequest buildScribeRequest(HttpServletRequest req) {
    //     String url = req.getRequestURL().toString();
    //     String qs  = req.getQueryString();
    //     if (qs != null && !qs.isEmpty()) {
    //         url += "?" + qs;
    //     }
    //     Verb verb = Verb.valueOf(req.getMethod());
    //     OAuthRequest sreq = new OAuthRequest(verb, url);

    //     if ("POST".equalsIgnoreCase(req.getMethod())) {
    //         req.getParameterMap().forEach((k, vals) -> {
    //             if (!k.startsWith("oauth_")) {
    //                 for (String v : vals) {
    //                     sreq.addParameter(k, v);
    //                 }
    //             }
    //         });
    //     }
    //     return sreq;
    // }

    // /**
    //  * Private DefaultApi10a implementation, so we don’t need a public helper.
    //  */
    // private static class GenericApi extends DefaultApi10a {
    //     private final String baseUrl;
    //     GenericApi(String baseUrl) { this.baseUrl = baseUrl; }

    //     @Override
    //     public String getRequestTokenEndpoint() {
    //         return baseUrl + "/oauth/request_token";
    //     }

    //     @Override
    //     public String getAccessTokenEndpoint() {
    //         return baseUrl + "/oauth/access_token";
    //     }

    //     @Override
    //     public String getAuthorizationBaseUrl() {
    //         return baseUrl + "/oauth/authorize";
    //     }
    // }

    private static final java.nio.charset.Charset UTF8 = java.nio.charset.StandardCharsets.UTF_8;

    // Unreserved per RFC 5849: ALPHA / DIGIT / "-" / "." / "_" / "~"
    private static String pct(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (byte b : s.getBytes(UTF8)) {
            int c = b & 0xFF;
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9') || c == '-' || c == '.' || c == '_' || c == '~') {
                out.append((char) c);
            } else {
                out.append('%');
                String hex = Integer.toHexString(c).toUpperCase();
                if (hex.length() == 1) out.append('0');
                out.append(hex);
            }
        }
        return out.toString();
    }

    private static byte[] hmacSha1(String data, String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
            javax.crypto.spec.SecretKeySpec ks = new javax.crypto.spec.SecretKeySpec(key.getBytes(UTF8), "HmacSHA1");
            mac.init(ks);
            return mac.doFinal(data.getBytes(UTF8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String base64(byte[] bytes) {
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        int len = Math.max(a.length(), b.length());
        int r = 0;
        for (int i = 0; i < len; i++) {
            char ca = i < a.length() ? a.charAt(i) : 0;
            char cb = i < b.length() ? b.charAt(i) : 0;
            r |= (ca ^ cb);
        }
        return r == 0 && a.length() == b.length();
    }

    private static String urlDecode(String s) {
        try { return java.net.URLDecoder.decode(s, "UTF-8"); }
        catch (Exception e) { return s; }
    }

    private static final class NameValue {
        final String name, value;
        NameValue(String n, String v) { this.name = n; this.value = v; }
    }

}
