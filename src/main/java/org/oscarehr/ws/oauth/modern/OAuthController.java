package org.oscarehr.ws.oauth.modern;

import com.github.scribejava.core.model.OAuth2AccessToken;
import org.apache.logging.log4j.Logger;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Modern OAuth 2.0 controller for handling authorization flow
 * Replaces legacy OAuth 1.0a endpoints
 */
@Controller
@RequestMapping("/ws/oauth")
public class OAuthController {

    private static final Logger logger = MiscUtils.getLogger();

    @Autowired
    private OAuthService oAuthService;

    /**
     * Initiate OAuth authorization flow
     */
    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public void authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!oAuthService.isOAuthConfigured()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "OAuth not configured");
            return;
        }

        String authUrl = oAuthService.getAuthorizationUrl(request.getSession());
        if (authUrl != null) {
            response.sendRedirect(authUrl);
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate authorization URL");
        }
    }

    /**
     * Handle OAuth callback
     */
    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    public void callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession();

        if (error != null) {
            logger.error("OAuth error: " + error);
            response.sendRedirect("/oscar/login.jsp?oauth_error=" + error);
            return;
        }

        if (code == null) {
            logger.error("No authorization code received");
            response.sendRedirect("/oscar/login.jsp?oauth_error=no_code");
            return;
        }

        OAuth2AccessToken token = oAuthService.exchangeCodeForToken(code, state, session);
        if (token != null) {
            logger.info("OAuth authentication successful");
            response.sendRedirect("/oscar/provider/providercontrol.jsp");
        } else {
            logger.error("Failed to exchange code for token");
            response.sendRedirect("/oscar/login.jsp?oauth_error=token_exchange_failed");
        }
    }

    /**
     * OAuth logout endpoint
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        oAuthService.clearSession(request.getSession());
        response.sendRedirect("/oscar/logout.jsp");
    }

    /**
     * OAuth status endpoint
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public void status(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        OAuth2AccessToken token = oAuthService.getAccessToken(session);
        
        response.setContentType("application/json");
        if (token != null && oAuthService.validateToken(token)) {
            response.getWriter().write("{\"authenticated\": true, \"token_type\": \"" + token.getTokenType() + "\"}");
        } else {
            response.getWriter().write("{\"authenticated\": false}");
        }
    }
}
