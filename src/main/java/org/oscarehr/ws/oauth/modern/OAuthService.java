package org.oscarehr.ws.oauth.modern;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.apache.logging.log4j.Logger;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Modern OAuth 2.0 service implementation using ScribeJava
 * Provides OAuth authentication and token management
 */
@Service
public class OAuthService {

    private static final Logger logger = MiscUtils.getLogger();
    private static final String ACCESS_TOKEN_SESSION_KEY = "oauth_access_token";
    private static final String STATE_SESSION_KEY = "oauth_state";

    @Autowired(required = false)
    private OAuth20Service oAuth20Service;

    /**
     * Generate authorization URL for OAuth flow
     */
    public String getAuthorizationUrl(HttpSession session) {
        if (oAuth20Service == null) {
            logger.warn("OAuth service not configured");
            return null;
        }

        try {
            String state = generateState();
            session.setAttribute(STATE_SESSION_KEY, state);
            
            return oAuth20Service.createAuthorizationUrlBuilder()
                    .state(state)
                    .build();
        } catch (Exception e) {
            logger.error("Error generating authorization URL", e);
            return null;
        }
    }

    /**
     * Exchange authorization code for access token
     */
    public OAuth2AccessToken exchangeCodeForToken(String code, String state, HttpSession session) {
        if (oAuth20Service == null) {
            logger.warn("OAuth service not configured");
            return null;
        }

        // Verify state parameter
        String sessionState = (String) session.getAttribute(STATE_SESSION_KEY);
        if (sessionState == null || !sessionState.equals(state)) {
            logger.error("Invalid state parameter");
            return null;
        }

        try {
            OAuth2AccessToken accessToken = oAuth20Service.getAccessToken(code);
            session.setAttribute(ACCESS_TOKEN_SESSION_KEY, accessToken);
            session.removeAttribute(STATE_SESSION_KEY);
            return accessToken;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error exchanging code for token", e);
            return null;
        }
    }

    /**
     * Get stored access token from session
     */
    public OAuth2AccessToken getAccessToken(HttpSession session) {
        return (OAuth2AccessToken) session.getAttribute(ACCESS_TOKEN_SESSION_KEY);
    }

    /**
     * Validate access token by making a test API call
     */
    public boolean validateToken(OAuth2AccessToken token) {
        if (oAuth20Service == null || token == null) {
            return false;
        }

        try {
            OAuthRequest request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/oauth2/v1/userinfo");
            oAuth20Service.signRequest(token, request);
            
            Response response = oAuth20Service.execute(request);
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
        session.removeAttribute(STATE_SESSION_KEY);
    }

    /**
     * Check if OAuth is configured and available
     */
    public boolean isOAuthConfigured() {
        return oAuth20Service != null;
    }

    private String generateState() {
        return java.util.UUID.randomUUID().toString();
    }
}
