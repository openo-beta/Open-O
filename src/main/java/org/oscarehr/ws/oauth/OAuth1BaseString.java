/**
 * File: OAuth1BaseString.java
 *
 * Purpose:
 *   Utility for constructing the OAuth 1.0a signature base string
 *   used in HMAC-SHA1 and PLAINTEXT signature verification.
 *
 * Responsibilities:
 *   • Collect and normalize all request parameters, excluding oauth_signature.
 *   • Percent-encode keys/values per RFC 5849 (UTF-8, space as %20).
 *   • Sort parameters lexicographically by encoded key, then value.
 *   • Concatenate HTTP method, base URL, and normalized parameters into
 *     the final signature base string.
 *
 * Context / Why Added:
 *   Shared helper used by signature verifiers to ensure consistent,
 *   deterministic base string construction across endpoints.
 *
 * Notes:
 *   • Excludes oauth_signature during normalization.
 *   • Host, port, and protocol must exactly match the actual request
 *     to avoid false 401s during verification.
 *   • Percent-encoding follows OAuth 1.0a spec (slightly different from
 *     standard URL encoding).
 */


package org.oscarehr.ws.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public final class OAuth1BaseString {
    private OAuth1BaseString() {}

    public static String build(OAuth1Request r) {
        // 1) Collect all params excluding oauth_signature
        List<String[]> nv = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : r.params.entrySet()) {
            String k = e.getKey();
            if ("oauth_signature".equals(k)) continue;
            for (String v : e.getValue()) {
                nv.add(new String[]{ percentEncode(k), percentEncode(v) });
            }
        }
        // 2) Sort by key, then value (both already encoded)
        nv.sort((a,b) -> {
            int cmp = a[0].compareTo(b[0]);
            return cmp != 0 ? cmp : a[1].compareTo(b[1]);
        });

        // 3) Join as key=value&key=value...
        StringBuilder norm = new StringBuilder();
        for (int i=0;i<nv.size();i++) {
            if (i>0) norm.append('&');
            norm.append(nv.get(i)[0]).append('=').append(nv.get(i)[1]);
        }

        // 4) METHOD & baseURL & percentEncode(normalizedParamString)
        return percentEncode(r.method.toUpperCase()) + '&'
             + percentEncode(r.urlBase) + '&'
             + percentEncode(norm.toString());
    }

    public static String percentEncode(String s) {
        try {
            return URLEncoder.encode(s == null ? "" : s, "UTF-8")
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
