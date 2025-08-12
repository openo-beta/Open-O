//src/main/java/org/oscarehr/ws/oauth/OAuthSignatureVerifier.java
package org.oscarehr.ws.oauth;

import javax.servlet.http.HttpServletRequest;
import oscar.login.AppOAuth1Config;

public interface OAuth1SignatureVerifier {
    String verifySignature(HttpServletRequest req, AppOAuth1Config cfg);
    String getProviderNo(String token);
}
