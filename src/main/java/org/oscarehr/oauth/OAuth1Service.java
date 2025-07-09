package org.oscarehr.oauth;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.oscarehr.util.MiscUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * OAuth 1.0a service implementation using ScribeJava
 * Compatible with pre-3.5 CXF (javax.ws.rs) environment
 */
public class OAuth1Service {
    
    private static final Logger logger = MiscUtils.getLogger();
    
    private final OAuth10aService service;
    private final String apiKey;
    private final String apiSecret;
    private final String callbackUrl;
    
    public OAuth1Service(String apiKey, String apiSecret, String callbackUrl) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.callbackUrl = callbackUrl;
        
        // Build OAuth 1.0a service - using Twitter API as example
        // Replace TwitterApi.instance() with your specific OAuth provider
        this.service = new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .callback(callbackUrl)
                .build(TwitterApi.instance());
    }
    
    /**
     * Step 1: Obtain request token
     */
    public OAuth1RequestToken getRequestToken() throws IOException, InterruptedException, ExecutionException {
        logger.info("Obtaining OAuth 1.0a request token");
        return service.getRequestToken();
    }
    
    /**
     * Step 2: Get authorization URL
     */
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        String authUrl = service.getAuthorizationUrl(requestToken);
        logger.info("Authorization URL: " + authUrl);
        return authUrl;
    }
    
    /**
     * Step 3: Exchange request token for access token
     */
    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String verifier) 
            throws IOException, InterruptedException, ExecutionException {
        logger.info("Exchanging request token for access token");
        return service.getAccessToken(requestToken, verifier);
    }
    
    /**
     * Step 4: Make signed API requests
     */
    public Response makeSignedRequest(OAuth1AccessToken accessToken, String url, Verb httpMethod) 
            throws IOException, InterruptedException, ExecutionException {
        
        OAuthRequest request = new OAuthRequest(httpMethod, url);
        service.signRequest(accessToken, request);
        
        logger.info("Making signed OAuth 1.0a request to: " + url);
        return service.execute(request);
    }
    
    /**
     * Convenience method for GET requests
     */
    public Response get(OAuth1AccessToken accessToken, String url) 
            throws IOException, InterruptedException, ExecutionException {
        return makeSignedRequest(accessToken, url, Verb.GET);
    }
    
    /**
     * Convenience method for POST requests
     */
    public Response post(OAuth1AccessToken accessToken, String url) 
            throws IOException, InterruptedException, ExecutionException {
        return makeSignedRequest(accessToken, url, Verb.POST);
    }
}
