package org.oscarehr.oauth;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.Response;
import org.oscarehr.util.MiscUtils;
import org.apache.logging.log4j.Logger;

/**
 * Example usage of OAuth 1.0a service
 */
public class OAuth1Example {
    
    private static final Logger logger = MiscUtils.getLogger();
    
    public void demonstrateOAuth1Flow() {
        try {
            // Initialize OAuth service
            String apiKey = "your-api-key";
            String apiSecret = "your-api-secret";
            String callbackUrl = "http://localhost:8080/oauth/callback";
            
            OAuth1Service oauthService = new OAuth1Service(apiKey, apiSecret, callbackUrl);
            
            // Step 1: Get request token
            OAuth1RequestToken requestToken = oauthService.getRequestToken();
            logger.info("Request token: " + requestToken.getToken());
            
            // Step 2: Redirect user to authorization URL
            String authUrl = oauthService.getAuthorizationUrl(requestToken);
            logger.info("Redirect user to: " + authUrl);
            
            // Step 3: After user authorizes, exchange for access token
            // (In real implementation, you'd get the verifier from callback)
            String verifier = "user-provided-verifier";
            OAuth1AccessToken accessToken = oauthService.getAccessToken(requestToken, verifier);
            logger.info("Access token: " + accessToken.getToken());
            
            // Step 4: Make authenticated API calls
            String apiUrl = "https://api.example.com/user/profile";
            Response response = oauthService.get(accessToken, apiUrl);
            
            logger.info("API Response: " + response.getCode());
            logger.info("Response body: " + response.getBody());
            
        } catch (Exception e) {
            logger.error("OAuth 1.0a flow failed", e);
        }
    }
}
