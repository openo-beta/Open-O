/**
 * Purpose: Immutable view of normalized OAuth 1.0a request parameters.
 * Contains: consumerKey, token, signatureMethod, signature, timestamp, nonce,
 *           callback, verifier, httpMethod, baseUri, normalized params.
 * Used by: Signature verification and token endpoints.
 * Notes:
 *   • Produced by the parser; do not mutate.
 *   • Avoid logging secrets or raw signatures.
 */

package org.oscarehr.ws.oauth;

import java.util.*;

public class OAuth1Request {
    public String method;
    public String urlBase; // scheme://host[:port]/path (no query)
    public SortedMap<String, List<String>> params = new TreeMap<>();
    public String signature;        // oauth_signature
    public String signatureMethod;  // HMAC-SHA1
    public String consumerKey;      // oauth_consumer_key
    public String token;            // oauth_token (may be null for initiate)
    public String timestamp;        // oauth_timestamp
    public String nonce;            // oauth_nonce
    public String callback;         // oauth_callback (initiate)
    public String verifier;         // oauth_verifier (token)
    public String scopesCsv;        // non-standard, if your clients pass scope

    public void addParam(String k, String v) {
        params.computeIfAbsent(k, _k -> new ArrayList<>()).add(v);
    }
}
