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

// Package declaration
package org.oscarehr.app; 

// Import necessary libraries and classes
import java.io.IOException; // For handling IO exceptions
import java.util.List; // For the List interface
import java.util.Map; // For Map interface
import java.util.concurrent.ExecutionException; // For handling concurrent execution exceptions
import java.util.Collections; // For using Collections utility methods

// apache CXF libraries for JAX-RS and JSON processing
import org.apache.cxf.jaxrs.provider.json.JSONProvider; // JSON provider for JAX-RS
import org.apache.logging.log4j.Logger; // Logging facility

// Oscar libraries for application and user models
import org.oscarehr.common.model.AppDefinition; // Application definition model
import org.oscarehr.common.model.AppUser; // Application user model
import org.oscarehr.util.LoggedInInfo; // Information about the logged in user
import org.oscarehr.util.MiscUtils; // Miscellaneous utilities, including logging

// OAuth 1.0a libraries from ScribeJava
import com.github.scribejava.core.builder.ServiceBuilder; // Builder for OAuth service
import com.github.scribejava.core.builder.api.DefaultApi10a; // Default OAuth 1.0a API
import com.github.scribejava.core.model.OAuth1AccessToken; // OAuth 1.0a Access Token model
import com.github.scribejava.core.model.OAuthRequest; // Represents an OAuth request
import com.github.scribejava.core.model.Response; // Represents an OAuth response
import com.github.scribejava.core.model.Verb; // HTTP verb (GET, POST, DELETE etc.)
import com.github.scribejava.core.oauth.OAuth10aService; // OAuth 1.0a service interface

import oscar.log.LogAction; // Log action utility

public class OAuth1Utils { // Utility class for OAuth 1.0a operations
    private static final Logger logger = MiscUtils.getLogger(); // Logger instance for debugging and errors

    /**
     * A generic OAuth 1.0a API implementation for any provider sharing the standard
     * /oauth/request_token, /oauth/access_token, and /oauth/authorize endpoints.
     */
    private static class GenericOAuth10aApi extends DefaultApi10a {
        private final String requestTokenUrl;     // Endpoint to obtain a request token
        private final String accessTokenUrl;      // Endpoint to exchange for an access token
        private final String authorizationUrl;    // Endpoint to redirect users for authorization

        /**
         * Constructs the OAuth1 endpoints based on a base URL.
         *
         * @param baseUrl The root URL of the OAuth1 provider (e.g., https://api.example.com)
         */
        public GenericOAuth10aApi(String baseUrl) {
            this.requestTokenUrl  = baseUrl + "/oauth/request_token";
            this.accessTokenUrl   = baseUrl + "/oauth/access_token";
            this.authorizationUrl = baseUrl + "/oauth/authorize";
        }

        @Override
        public String getRequestTokenEndpoint() {
            return requestTokenUrl;
        }

        @Override
        public String getAccessTokenEndpoint() {
            return accessTokenUrl;
        }

        @Override
        public String getAuthorizationBaseUrl() {
            return authorizationUrl;
        }
    }

    /**
     * Provides a minimal set of CXF providers configured for JSON processing.
     * <p>
     * This method returns a list containing a single Jackson JSONProvider
     * that is set up to drop the root element wrapper, allowing clean
     * serialization/deserialization of payloads without extra nesting.
     *
     * @return a List of CXF providers for JSON marshalling/unmarshalling
     */
    public static List<Object> getProviderK2A() {
        // 1. Instantiate the Jackson JSON provider
        JSONProvider<Object> jsonProvider = new JSONProvider<Object>();
        
        // 2. Configure it to ignore JSON root elements (no extra wrapper objects)
        jsonProvider.setDropRootElement(true);
        
        // 3. Return the configured provider in a singleton list
        return Collections.<Object>singletonList(jsonProvider);
    }


    /**
     * Performs an OAuth1-signed HTTP GET request to a protected API.
     *
     * @param loggedInInfo   User session context for logging/auditing.
     * @param app            AppDefinition containing OAuth1 consumer credentials XML.
     * @param user           AppUser holding stored OAuth1 access token credentials.
     * @param requestURI     Resource path to append to the base URL (e.g., "/resource/123").
     * @param baseRequestURI Secondary path used purely for logging context.
     * @return               The response body as a String, or null if an error occurs.
     */
    public static String getOAuthGetResponse(LoggedInInfo loggedInInfo,
                                            AppDefinition app,
                                            AppUser user,
                                            String requestURI,
                                            String baseRequestURI) {
        try {
            // 1. Parse consumer key/secret and API base URL from the app’s config document
            AppOAuth1Config config = AppOAuth1Config.fromDocument(app.getConfig());

            // 2. Extract the user's OAuth1 access token and secret
            Map<String,String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            // 3. Initialize the OAuth1 service with consumer credentials
            OAuth10aService service = new ServiceBuilder(config.getConsumerKey())
                .apiSecret(config.getConsumerSecret())
                .build(new GenericOAuth10aApi(config.getBaseURL()));

            // 4. Construct the OAuth1 access token for signing
            OAuth1AccessToken accessToken =
                new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            // 5. Build the full GET URL
            String fullUrl = config.getBaseURL() + requestURI;

            // 6. Create and sign the request
            OAuthRequest request = new OAuthRequest(Verb.GET, fullUrl);
            service.signRequest(accessToken, request);

            // 7. Execute the request and read the response body
            Response response = service.execute(request);
            String responseBody = response.getBody();

            // 8. Log the GET action for auditing
            String action    = "oauth1.GET, AppId=" + app.getId() + ", AppUser=" + user.getId();
            String content   = config.getBaseURL() + baseRequestURI;
            String contentId = "AppUser=" + user.getId();
            LogAction.addLog(loggedInInfo, action, content, contentId, null, responseBody);
            logger.debug("Logged action: " + action);

            return responseBody;

        } catch (IOException | InterruptedException | ExecutionException e) {
            // Network, I/O, or ScribeJava errors
            logger.error("Failed to perform OAuth1 GET request", e);
            return null;
        } catch (Exception e) {
            // Configuration or unexpected errors
            logger.error("OAuth1 GET configuration error", e);
            return null;
        }
    }
    /**
     * Executes an OAuth1-signed HTTP POST against a protected API endpoint.
     *
     * @param loggedInInfo     Contextual information for the current user session, used for logging.
     * @param app              The application definition containing OAuth1 consumer credentials and base URL.
     * @param user             The application user holding their stored OAuth1 access token credentials.
     * @param requestURI       The endpoint path (relative to the base URL) to which the POST will be sent.
     * @param baseRequestURI   A secondary endpoint path used for logging context (e.g., resource identifier).
     * @param providers        A list of CXF providers for marshalling/unmarshalling (not used in ScribeJava).
     * @param obj              An optional payload object to be serialized as JSON and included in the POST body.
     * @return                 The raw response body as a String if the request succeeds; null on failure.
     * @throws IOException            If an I/O error occurs during request execution.
     * @throws InterruptedException   If the request thread is interrupted.
     * @throws ExecutionException     If the OAuth1 signing or execution fails.
     */
    public static String getOAuthPostResponse(LoggedInInfo loggedInInfo,
                                          AppDefinition app,
                                          AppUser user,
                                          String requestURI,
                                          String baseRequestURI,
                                          List<Object> providers,
                                          Object obj) {
        try {
            // 1. Parse consumer key/secret and base URL from the app’s XML configuration.
            AppOAuth1Config config = AppOAuth1Config.fromDocument(app.getConfig());

            // 2. Extract the user’s saved token key & secret from their authentication data.
            Map<String,String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            // 3. Initialize the ScribeJava OAuth1 service with consumer credentials.
            OAuth10aService service = new ServiceBuilder(config.getConsumerKey())
                .apiSecret(config.getConsumerSecret())
                .build(new GenericOAuth10aApi(config.getBaseURL()));

            // 4. Create an access token object for signing requests.
            OAuth1AccessToken accessToken =
                new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            // 5. Construct the full target URL for the POST.
            String fullUrl = config.getBaseURL() + requestURI;

            // 6. Build the OAuthRequest and attach JSON headers if a payload is provided.
            OAuthRequest request = new OAuthRequest(Verb.POST, fullUrl);
            if (obj != null) {
                // NOTE: Consider using a dedicated JSON serializer rather than toString().
                request.addHeader("Content-Type", "application/json; charset=utf-8");
                request.addHeader("Accept", "application/json, text/plain, */*");
                request.setPayload(obj.toString());
            }

            // 7. Sign the request (computes HMAC-SHA1 Authorization header).
            service.signRequest(accessToken, request);

            // 8. Execute and retrieve the response body as a String.
            Response response = service.execute(request);
            String responseBody = response.getBody();

            // 9. Log the action for auditing and debugging.
            String action    = "oauth1.POST, AppId=" + app.getId() + ", AppUser=" + user.getId();
            String content   = config.getBaseURL() + baseRequestURI;
            String contentId = "AppUser=" + user.getId();
            LogAction.addLog(loggedInInfo, action, content, contentId, null, responseBody);

            return responseBody;

        } catch (IOException | InterruptedException | ExecutionException e) {
            // Network or execution error: log and return failure.
            logger.error("Failed to execute OAuth1 POST request", e);
            return null;
        } catch (Exception e) {
            // Configuration or other unexpected error.
            logger.error("OAuth1 POST configuration error", e);
            return null;
        }
    }


    /**
     * Sends an OAuth1-signed DELETE request to a protected resource.
     *
     * @param app            The application definition, containing OAuth1 consumer config as XML.
     * @param user           The application user, holding their stored OAuth1 access token.
     * @param requestURI     The API endpoint path to DELETE (appended to base URL).
     * @param baseRequestURI A secondary URI used solely for logging context.
     */
    public static void getOAuthDeleteResponse(AppDefinition app,
                                            AppUser user,
                                            String requestURI,
                                            String baseRequestURI) {
        try {
            // 1) Parse the application's OAuth1 configuration (consumer key/secret + base URL)
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());

            // 2) Extract the user's saved access token and secret from their auth data
            Map<String, String> keySecret =
                    AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            // 3) Build the ScribeJava OAuth1 service using the consumer credentials
            OAuth10aService service = new ServiceBuilder(appAuthConfig.getConsumerKey())
                    .apiSecret(appAuthConfig.getConsumerSecret())
                    .build(new GenericOAuth10aApi(appAuthConfig.getBaseURL()));

            // 4) Create an access token object with the user's token credentials
            OAuth1AccessToken accessToken =
                    new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            // 5) Construct the full URL by joining the base URL and the request path
            String fullUrl = appAuthConfig.getBaseURL() + requestURI;

            // 6) Initialize an OAuthRequest for the DELETE verb
            OAuthRequest request = new OAuthRequest(Verb.DELETE, fullUrl);

            // 7) Apply the OAuth1 signature (HMAC-SHA1) to the request
            service.signRequest(accessToken, request);

            // 8) Execute the DELETE request; we do not process any response body
            service.execute(request);

        } catch (IOException | InterruptedException | ExecutionException e) {
            // Network, I/O, or ScribeJava execution errors
            logger.error("Failed to execute OAuth1 DELETE request", e);
        } catch (Exception e) {
            // Any other unexpected errors (e.g., parsing config)
            logger.error("OAuth1 DELETE configuration error", e);
        }
    }
}
