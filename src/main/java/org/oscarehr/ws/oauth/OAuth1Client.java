package org.oscarehr.ws.oauth;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;

public class OAuth1Client {
    private final OAuth10aService service;

    public OAuth1Client(String apiKey, String apiSecret, String callbackUrl) {
        // Initialize the OAuth 1.0a service
        this.service = new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .callback(callbackUrl) 
                .build(TwitterApi.instance()); 
    }

    public OAuth10aService getService() {
        return this.service;
    }

    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier) throws Exception {
        return service.getAccessToken(requestToken, oauthVerifier);
    }

    public String sendSignedRequest(OAuth1AccessToken accessToken, String url) throws Exception {
        // Create the OAuth 1.0a request
        OAuthRequest request = new OAuthRequest(Verb.GET, url);
        service.signRequest(accessToken, request); // Use the access token to sign the request

        // Execute the request
        Response response = service.execute(request);
        if (response.getCode() == 200) {
            return response.getBody();
        } else {
            throw new RuntimeException("Request failed, the status code: " + response.getCode());
        }
    }

    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return service.getAuthorizationUrl(requestToken);
    }
}
