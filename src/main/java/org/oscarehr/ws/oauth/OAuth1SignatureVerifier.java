package org.oscarehr.ws.oauth;

public interface OAuth1SignatureVerifier {
    void verifySignature(OAuth1Request req, String consumerSecret, String tokenSecret);
}
