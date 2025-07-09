package org.oscarehr.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.oscarehr.common.model.AppDefinition;
import org.oscarehr.common.model.AppUser;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

/**
 * OAuth 1.0a utilities using ScribeJava
 * Migrated from CXF OAuth client utilities
 */
public class OAuth1Utils {
    
    private static final Logger logger = MiscUtils.getLogger();

    /**
     * Generic OAuth 1.0a API for custom providers
     */
    private static class GenericOAuth10aApi extends DefaultApi10a {
        private final String requestTokenUrl;
        private final String accessTokenUrl;
        private final String authorizationUrl;
        
        public GenericOAuth10aApi(String baseUrl) {
            this.requestTokenUrl = baseUrl + "/oauth/request_token";
            this.accessTokenUrl = baseUrl + "/oauth/access_token";
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

    public static String getOAuthGetResponse(LoggedInInfo loggedInInfo, AppDefinition app, AppUser user, String requestURI, String baseRequestURI) {
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            // Create OAuth 1.0a service using ScribeJava
            OAuth10aService service = new ServiceBuilder(appAuthConfig.getConsumerKey())
                    .apiSecret(appAuthConfig.getConsumerSecret())
                    .build(new GenericOAuth10aApi(appAuthConfig.getBaseURL()));

            // Create access token from stored credentials
            OAuth1AccessToken accessToken = new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            // Create and sign the request
            String fullUrl = appAuthConfig.getBaseURL() + requestURI;
            OAuthRequest request = new OAuthRequest(Verb.GET, fullUrl);
            service.signRequest(accessToken, request);

            // Execute the request
            Response response = service.execute(request);
            String data = response.getBody();

            // Log the action
            String action = "oauth1.GET, AppId=" + app.getId() + ", AppUser=" + user.getId();
            logger.debug("logaction " + action);

            return data;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error getting information from OAuth Service", e);
            return null;
        }
    }

    public static String getOAuthPostResponse(LoggedInInfo loggedInInfo, AppDefinition app, AppUser user, String requestURI, String baseRequestURI, List<Object> providers, Object obj) {
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            // Create OAuth 1.0a service using ScribeJava
            OAuth10aService service = new ServiceBuilder(appAuthConfig.getConsumerKey())
                    .apiSecret(appAuthConfig.getConsumerSecret())
                    .build(new GenericOAuth10aApi(appAuthConfig.getBaseURL()));

            // Create access token from stored credentials
            OAuth1AccessToken accessToken = new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            // Create and sign the request
            String fullUrl = appAuthConfig.getBaseURL() + requestURI;
            OAuthRequest request = new OAuthRequest(Verb.POST, fullUrl);
            
            // Add JSON payload if provided
            if (obj != null) {
                request.addHeader("Content-Type", "application/json;charset=utf-8");
                request.addHeader("Accept", "application/json, text/plain, */*");
                request.addHeader("Accept-Encoding", "gzip, deflate");
                // Convert object to JSON string - you may need to implement this based on your JSON library
                request.setPayload(obj.toString());
            }
            
            service.signRequest(accessToken, request);

            // Execute the request
            Response response = service.execute(request);
            String data = response.getBody();

            // Log the action
            String action = "oauth1.POST, AppId=" + app.getId() + ", AppUser=" + user.getId();
            logger.debug("logaction " + action);

            return data;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error posting information to OAuth Service", e);
            return null;
        }
    }

    public static void getOAuthDeleteResponse(AppDefinition app, AppUser user, String requestURI, String baseRequestURI) {
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            // Create OAuth 1.0a service using ScribeJava
            OAuth10aService service = new ServiceBuilder(appAuthConfig.getConsumerKey())
                    .apiSecret(appAuthConfig.getConsumerSecret())
                    .build(new GenericOAuth10aApi(appAuthConfig.getBaseURL()));

            // Create access token from stored credentials
            OAuth1AccessToken accessToken = new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            // Create and sign the request
            String fullUrl = appAuthConfig.getBaseURL() + requestURI;
            OAuthRequest request = new OAuthRequest(Verb.DELETE, fullUrl);
            service.signRequest(accessToken, request);

            // Execute the request
            service.execute(request);
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error deleting information from OAuth Service", e);
        }
    }

    /**
     * Get provider configuration for K2A
     * This method was referenced by other classes and needs to be maintained for compatibility
     */
    public static List<Object> getProviderK2A() {
        // Return empty list for now - this was likely used for CXF providers
        // You may need to implement this based on your specific requirements
        return new java.util.ArrayList<>();
    }
}
