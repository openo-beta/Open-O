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
import java.util.stream.Collectors;

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
     */
    public void handleRequestToken(HttpServletRequest request, 
                                 HttpServletResponse response,
                                 Object app, 
                                 String callbackUrl) 
            throws IOException {
        
        // Validate input parameters
        if (request == null || response == null) {
            throw new IllegalArgumentException("Request and response cannot be null");
        }
        
        if (app == null) {
            logger.error("Application configuration is null");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing application configuration");
            return;
        }
        
        try {
            logger.info("Starting OAuth 1.0a request token flow for callback: {}", callbackUrl);
            
            // Load OAuth configuration from application config
            Object configDoc = getAppConfig(app);
            if (configDoc == null) {
                logger.error("Failed to retrieve application configuration document");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Configuration retrieval error");
                return;
            }
            
            AppOAuth1Config config = AppOAuth1Config.fromDocument(configDoc);
            if (config == null) {
                logger.error("Failed to parse OAuth configuration from document");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth configuration parsing error");
                return;
            }
            
            if (config.getConsumerKey() == null || config.getConsumerSecret() == null) {
                logger.error("OAuth configuration is missing required fields: consumerKey={}, consumerSecret={}",
                    config.getConsumerKey() != null ? "[PRESENT]" : "[MISSING]",
                    config.getConsumerSecret() != null ? "[PRESENT]" : "[MISSING]");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Incomplete OAuth configuration");
                return;
            }

            // Validate callback URL
            try {
                validateCallbackURL(config, callbackUrl);
            } catch (IllegalArgumentException e) {
                logger.error("Callback URL validation failed: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid callback URL");
                return;
            }

            // Build OAuth 1.0a service using ScribeJava
            OAuth10aService service;
            try {
                service = new ServiceBuilder(config.getConsumerKey())
                        .apiSecret(config.getConsumerSecret())
                        .callback(callbackUrl)
                        .build(new GenericOAuth10aApi(config.getBaseUrl()));
            } catch (Exception e) {
                logger.error("Failed to build OAuth service: {}", e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth service initialization error");
                return;
            }

            logger.info("Requesting OAuth token from provider at: {}", config.getBaseUrl());
            
            // Get request token from OAuth provider
            OAuth1RequestToken requestToken;
            try {
                requestToken = service.getRequestToken();
            } catch (Exception e) {
                logger.error("Failed to obtain request token from OAuth provider: {}", e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "OAuth provider communication error");
                return;
            }
            
            if (requestToken == null || requestToken.getToken() == null) {
                logger.error("Received null or invalid request token from OAuth provider");
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Invalid response from OAuth provider");
                return;
            }

            // Store the request token and secret in data provider
            try {
                storeRequestToken(requestToken, callbackUrl);
            } catch (Exception e) {
                logger.error("Failed to store request token: {}", e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Token storage error");
                return;
            }

            // Build authorization URL and redirect user
            String authorizationUrl;
            try {
                authorizationUrl = service.getAuthorizationUrl(requestToken);
            } catch (Exception e) {
                logger.error("Failed to build authorization URL: {}", e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authorization URL generation error");
                return;
            }
            
            if (authorizationUrl == null || authorizationUrl.trim().isEmpty()) {
                logger.error("Generated authorization URL is null or empty");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid authorization URL");
                return;
            }
            
            logger.info("Successfully generated authorization URL, redirecting user");
            logger.debug("Authorization URL: {}", authorizationUrl);
            response.sendRedirect(authorizationUrl);

        } catch (IOException e) {
            logger.error("I/O error during OAuth request token handling: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during OAuth request token handling: {}", e.getMessage(), e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth processing error");
            }
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
     * @throws RuntimeException if token storage fails
     */
    private void storeRequestToken(OAuth1RequestToken requestToken, String callbackUrl) {
        if (requestToken == null) {
            throw new IllegalArgumentException("Request token cannot be null");
        }
        
        if (requestToken.getToken() == null || requestToken.getToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Request token key cannot be null or empty");
        }
        
        if (requestToken.getTokenSecret() == null) {
            throw new IllegalArgumentException("Request token secret cannot be null");
        }
        
        if (dataProvider == null) {
            throw new IllegalStateException("OAuth data provider is not available");
        }
        
        try {
            // Prepare scopes list
            List<String> scopes = null;
            if (defaultScope != null && !defaultScope.trim().isEmpty()) {
                scopes = Arrays.asList(defaultScope.split(","));
                // Remove empty strings from scopes
                scopes = scopes.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
                
                if (scopes.isEmpty()) {
                    scopes = null;
                }
            }

            logger.debug("Storing request token with key: {} and {} scopes", 
                requestToken.getToken(), 
                scopes != null ? scopes.size() : 0);

            // Store in data provider using the new signature
            dataProvider.createRequestToken(
                requestToken.getToken(), 
                requestToken.getTokenSecret(), 
                scopes
            );
            
            logger.info("Successfully stored request token: {}", requestToken.getToken());
            
        } catch (Exception e) {
            logger.error("Failed to store request token '{}': {}", requestToken.getToken(), e.getMessage(), e);
            throw new RuntimeException("Failed to store request token: " + e.getMessage(), e);
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
         * @param configDoc The configuration document (expected to be a Map or Properties-like object)
         * @return AppOAuth1Config instance or null if parsing fails
         */
        public static AppOAuth1Config fromDocument(Object configDoc) {
            if (configDoc == null) {
                logger.warn("Configuration document is null");
                return null;
            }
            
            try {
                AppOAuth1Config config = new AppOAuth1Config();
                
                // Handle different configuration document types
                if (configDoc instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> configMap = (java.util.Map<String, Object>) configDoc;
                    
                    config.setConsumerKey(getStringValue(configMap, "oauth.consumer.key"));
                    config.setConsumerSecret(getStringValue(configMap, "oauth.consumer.secret"));
                    config.setBaseUrl(getStringValue(configMap, "oauth.base.url"));
                    config.setCallbackURI(getStringValue(configMap, "oauth.callback.uri"));
                    config.setApplicationURI(getStringValue(configMap, "oauth.application.uri"));
                    
                } else if (configDoc instanceof java.util.Properties) {
                    java.util.Properties props = (java.util.Properties) configDoc;
                    
                    config.setConsumerKey(props.getProperty("oauth.consumer.key"));
                    config.setConsumerSecret(props.getProperty("oauth.consumer.secret"));
                    config.setBaseUrl(props.getProperty("oauth.base.url"));
                    config.setCallbackURI(props.getProperty("oauth.callback.uri"));
                    config.setApplicationURI(props.getProperty("oauth.application.uri"));
                    
                } else {
                    // Try reflection for generic objects with getter methods
                    config = parseViaReflection(configDoc);
                }
                
                // Validate required fields
                if (config.getConsumerKey() == null || config.getConsumerSecret() == null) {
                    logger.error("Missing required OAuth configuration: consumer key or secret");
                    return null;
                }
                
                if (config.getBaseUrl() == null) {
                    logger.error("Missing required OAuth configuration: base URL");
                    return null;
                }
                
                logger.debug("Successfully parsed OAuth configuration");
                return config;
                
            } catch (Exception e) {
                logger.error("Failed to parse OAuth configuration: {}", e.getMessage(), e);
                return null;
            }
        }
        
        /**
         * Helper method to safely extract string values from a map.
         */
        private static String getStringValue(java.util.Map<String, Object> map, String key) {
            Object value = map.get(key);
            return value != null ? value.toString() : null;
        }
        
        /**
         * Attempts to parse configuration using reflection.
         */
        private static AppOAuth1Config parseViaReflection(Object configDoc) {
            AppOAuth1Config config = new AppOAuth1Config();
            
            try {
                Class<?> clazz = configDoc.getClass();
                
                // Try common getter method names
                config.setConsumerKey(invokeGetter(configDoc, clazz, "getConsumerKey", "getOauthConsumerKey"));
                config.setConsumerSecret(invokeGetter(configDoc, clazz, "getConsumerSecret", "getOauthConsumerSecret"));
                config.setBaseUrl(invokeGetter(configDoc, clazz, "getBaseUrl", "getOauthBaseUrl"));
                config.setCallbackURI(invokeGetter(configDoc, clazz, "getCallbackURI", "getOauthCallbackUri"));
                config.setApplicationURI(invokeGetter(configDoc, clazz, "getApplicationURI", "getOauthApplicationUri"));
                
            } catch (Exception e) {
                logger.warn("Failed to parse configuration via reflection: {}", e.getMessage());
            }
            
            return config;
        }
        
        /**
         * Helper method to invoke getter methods via reflection.
         */
        private static String invokeGetter(Object obj, Class<?> clazz, String... methodNames) {
            for (String methodName : methodNames) {
                try {
                    java.lang.reflect.Method method = clazz.getMethod(methodName);
                    Object result = method.invoke(obj);
                    if (result != null) {
                        return result.toString();
                    }
                } catch (Exception e) {
                    // Continue to next method name
                }
            }
            return null;
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
