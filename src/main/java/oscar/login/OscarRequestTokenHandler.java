//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package oscar.login;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;

/**
 * OAuth 1.0a Request Token Handler using ScribeJava library.
 * 
 * This class handles the first step of the OAuth 1.0a flow:
 * 1. Obtains a request token from the OAuth provider
 * 2. Stores the temporary token and secret
 * 3. Redirects the user to the authorization URL
 * 
 * Migrated from Apache CXF to ScribeJava for OAuth implementation.
 */
@Component
public class OscarRequestTokenHandler {

    private static final Logger logger = MiscUtils.getLogger();
    
    @Autowired
    private OscarOAuthDataProvider dataProvider;
    
    private long tokenLifetime = 3600L;
    private String defaultScope;

    /**
     * Handles the OAuth 1.0a request token flow.
     * 
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @param app Application configuration containing OAuth settings
     * @param callbackUrl The callback URL for OAuth flow
     * @throws IOException if there's an I/O error during the OAuth process
     * @throws InterruptedException if the OAuth request is interrupted
     * @throws ExecutionException if there's an execution error during OAuth
     */
    public void handleRequestToken(HttpServletRequest request, 
                                 HttpServletResponse response,
                                 Object app, 
                                 String callbackUrl) 
            throws IOException, InterruptedException, ExecutionException {
        
        try {
            logger.info("Starting OAuth 1.0a request token flow");
            
            // Load OAuth configuration from application config
            AppOAuth1Config config = AppOAuth1Config.fromDocument(getAppConfig(app));
            
            if (config == null || config.getConsumerKey() == null || config.getConsumerSecret() == null) {
                logger.error("OAuth configuration is missing or incomplete");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth configuration error");
                return;
            }

            // Validate callback URL
            validateCallbackURL(config, callbackUrl);

            // Build OAuth 1.0a service using ScribeJava
            OAuth10aService service = new ServiceBuilder(config.getConsumerKey())
                    .apiSecret(config.getConsumerSecret())
                    .callback(callbackUrl)
                    .build(new GenericOAuth10aApi(config.getBaseUrl()));

            logger.info("Requesting OAuth token from provider");
            
            // Get request token from OAuth provider
            OAuth1RequestToken requestToken = service.getRequestToken();
            
            if (requestToken == null) {
                logger.error("Failed to obtain request token from OAuth provider");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to obtain request token");
                return;
            }

            // Store the request token and secret in data provider
            storeRequestToken(requestToken, callbackUrl);

            // Build authorization URL and redirect user
            String authorizationUrl = service.getAuthorizationUrl(requestToken);
            
            logger.info("Redirecting user to authorization URL: {}", authorizationUrl);
            response.sendRedirect(authorizationUrl);

        } catch (IOException e) {
            logger.error("I/O error during OAuth request token handling: {}", e.getMessage(), e);
            throw e;
        } catch (InterruptedException e) {
            logger.error("OAuth request was interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException e) {
            logger.error("Execution error during OAuth request: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during OAuth request token handling: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth processing error");
        }
    }

    /**
     * Validates the callback URL against the configured application settings.
     * 
     * @param config OAuth configuration
     * @param callbackUrl The callback URL to validate
     * @throws IllegalArgumentException if the callback URL is invalid
     */
    protected void validateCallbackURL(AppOAuth1Config config, String callbackUrl) 
            throws IllegalArgumentException {
        
        if (callbackUrl == null || callbackUrl.trim().isEmpty()) {
            logger.error("Callback URL is null or empty");
            throw new IllegalArgumentException("Callback URL cannot be null or empty");
        }

        // Check if callback URL matches pre-registered callback URI
        if (config.getCallbackURI() != null && !config.getCallbackURI().isEmpty()) {
            if (callbackUrl.equals(config.getCallbackURI())) {
                logger.debug("Callback URL matches pre-registered callback URI");
                return;
            }
        }

        // Check if callback URL has common root with application URI
        if (config.getApplicationURI() != null && !config.getApplicationURI().isEmpty()) {
            if (callbackUrl.startsWith(config.getApplicationURI())) {
                logger.debug("Callback URL has valid application URI prefix");
                return;
            }
        }

        logger.error("Invalid callback URL: {}", callbackUrl);
        throw new IllegalArgumentException("Invalid callback URL: " + callbackUrl);
    }

    /**
     * Stores the request token and associated information in the data provider.
     * 
     * @param requestToken The OAuth request token to store
     * @param callbackUrl The callback URL
     */
    private void storeRequestToken(OAuth1RequestToken requestToken, String callbackUrl) {
        try {
            // Prepare scopes list
            List<String> scopes = null;
            if (defaultScope != null && !defaultScope.trim().isEmpty()) {
                scopes = Arrays.asList(defaultScope.split(","));
            }

            // Store in data provider using the new signature
            dataProvider.createRequestToken(
                requestToken.getToken(), 
                requestToken.getTokenSecret(), 
                scopes
            );
            
            logger.info("Successfully stored request token: {}", requestToken.getToken());
            
        } catch (Exception e) {
            logger.error("Failed to store request token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to store request token", e);
        }
    }

    /**
     * Extracts the application configuration from the app object.
     * 
     * @param app The application object
     * @return The configuration document
     */
    private Object getAppConfig(Object app) {
        try {
            // Use reflection to call getConfig() method on app object
            return app.getClass().getMethod("getConfig").invoke(app);
        } catch (Exception e) {
            logger.error("Failed to get application config: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Sets the token lifetime in seconds.
     * 
     * @param tokenLifetime Token lifetime in seconds
     */
    public void setTokenLifetime(long tokenLifetime) {
        this.tokenLifetime = tokenLifetime;
    }

    /**
     * Sets the default OAuth scope.
     * 
     * @param defaultScope Default scope string
     */
    public void setDefaultScope(String defaultScope) {
        this.defaultScope = defaultScope;
    }

    /**
     * Inner class to represent OAuth configuration.
     */
    public static class AppOAuth1Config {
        private String consumerKey;
        private String consumerSecret;
        private String baseUrl;
        private String callbackURI;
        private String applicationURI;

        /**
         * Creates an OAuth configuration from a configuration document.
         * 
         * @param configDoc The configuration document
         * @return AppOAuth1Config instance
         */
        public static AppOAuth1Config fromDocument(Object configDoc) {
            // Implementation would parse the configuration document
            // This is a placeholder - actual implementation depends on config format
            AppOAuth1Config config = new AppOAuth1Config();
            // Parse configDoc and populate config fields
            return config;
        }

        // Getters and setters
        public String getConsumerKey() { return consumerKey; }
        public void setConsumerKey(String consumerKey) { this.consumerKey = consumerKey; }
        
        public String getConsumerSecret() { return consumerSecret; }
        public void setConsumerSecret(String consumerSecret) { this.consumerSecret = consumerSecret; }
        
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public String getCallbackURI() { return callbackURI; }
        public void setCallbackURI(String callbackURI) { this.callbackURI = callbackURI; }
        
        public String getApplicationURI() { return applicationURI; }
        public void setApplicationURI(String applicationURI) { this.applicationURI = applicationURI; }
    }

    /**
     * Generic OAuth 1.0a API implementation for ScribeJava.
     * Points to OAuth endpoints at <baseUrl>/oauth/...
     */
    public static class GenericOAuth10aApi extends DefaultApi10a {
        private final String baseUrl;

        /**
         * Constructor for GenericOAuth10aApi.
         * 
         * @param baseUrl The base URL for OAuth endpoints
         */
        public GenericOAuth10aApi(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        @Override
        public String getRequestTokenEndpoint() {
            return baseUrl + "/oauth/request_token";
        }

        @Override
        public String getAccessTokenEndpoint() {
            return baseUrl + "/oauth/access_token";
        }

        @Override
        public String getAuthorizationBaseUrl() {
            return baseUrl + "/oauth/authorize";
        }
    }
}
