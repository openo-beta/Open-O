package org.oscarehr.ws.oauth.modern;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.apache.logging.log4j.Logger;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Modern OAuth 1.0a service implementation using ScribeJava
 * Provides OAuth authentication and token management
 */
@Service
public class OAuthService {

    private static final Logger logger = MiscUtils.getLogger();
    private static final String ACCESS_TOKEN_SESSION_KEY = "oauth_access_token";
    private static final String REQUEST_TOKEN_SESSION_KEY = "oauth_request_token";

    @Value("${oauth.token.validation.url}")
    private String validationUrl;

    @Autowired(required = false)
    private OAuth10aService oAuth10aService;

    /**
     * Generate authorization URL for OAuth 1.0a flow
     */
    public String getAuthorizationUrl(HttpSession session) {
        if (oAuth10aService == null) {
            logger.warn("OAuth service not configured");
            return null;
        }

        try {
            OAuth1RequestToken requestToken = oAuth10aService.getRequestToken();
            session.setAttribute(REQUEST_TOKEN_SESSION_KEY, requestToken);
            
            return oAuth10aService.getAuthorizationUrl(requestToken);
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error generating authorization URL", e);
            return null;
        }
    }

    /**
     * Exchange OAuth verifier for access token
     */
    public OAuth1AccessToken exchangeVerifierForToken(String oauthVerifier, HttpSession session) {
        if (oAuth10aService == null) {
            logger.warn("OAuth service not configured");
            return null;
        }

        OAuth1RequestToken requestToken = (OAuth1RequestToken) session.getAttribute(REQUEST_TOKEN_SESSION_KEY);
        if (requestToken == null) {
            logger.error("No request token found in session");
            return null;
        }

        try {
            OAuth1AccessToken accessToken = oAuth10aService.getAccessToken(requestToken, oauthVerifier);
            session.setAttribute(ACCESS_TOKEN_SESSION_KEY, accessToken);
            session.removeAttribute(REQUEST_TOKEN_SESSION_KEY);
            return accessToken;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error exchanging verifier for token", e);
            return null;
        }
    }

    /**
     * Get stored access token from session
     */
    public OAuth1AccessToken getAccessToken(HttpSession session) {
        return (OAuth1AccessToken) session.getAttribute(ACCESS_TOKEN_SESSION_KEY);
    }

    /**
     * Validate access token by making a test API call
     */
    public boolean validateToken(OAuth1AccessToken token) {
        if (oAuth10aService == null || token == null) {
            return false;
        }

        try {
            // Use a simple verification endpoint - this would depend on your OAuth provider
            OAuthRequest request = new OAuthRequest(Verb.GET, validationUrl);
            oAuth10aService.signRequest(token, request);
            
            Response response = oAuth10aService.execute(request);
            return response.isSuccessful();
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error validating token", e);
            return false;
        }
    }

    /**
     * Clear OAuth session data
     */
    public void clearSession(HttpSession session) {
        session.removeAttribute(ACCESS_TOKEN_SESSION_KEY);
        session.removeAttribute(REQUEST_TOKEN_SESSION_KEY);
    }

    /**
     * Check if OAuth is configured and available
     */
    public boolean isOAuthConfigured() {
        return oAuth10aService != null;
    }
}

