package org.oscarehr.ws.oauth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HmacSha1SignatureVerifier implements OAuth1SignatureVerifier {

    @Override
    public void verifySignature(OAuth1Request req, String consumerSecret, String tokenSecret) {
        if (!"HMAC-SHA1".equalsIgnoreCase(req.signatureMethod)) {
            throw new OAuth1Exception(400, "unsupported_signature_method");
        }
        try {
            String base = OAuth1BaseString.build(req);
            String key = OAuth1BaseString.percentEncode(consumerSecret) + '&'
                       + OAuth1BaseString.percentEncode(tokenSecret == null ? "" : tokenSecret);

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA1"));
            String calc = Base64.getEncoder().encodeToString(mac.doFinal(base.getBytes("UTF-8")));

            if (!Timing.safeEquals(calc, req.signature)) {
                throw new OAuth1Exception(401, "invalid_signature");
            }
        } catch (OAuth1Exception e) {
            throw e;
        } catch (Exception e) {
            throw new OAuth1Exception(400, "signature_error");
        }
    }
}
