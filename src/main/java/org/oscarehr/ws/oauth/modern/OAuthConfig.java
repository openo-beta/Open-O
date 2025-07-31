package org.oscarehr.ws.oauth.modern;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.apis.TwitterApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Modern OAuth 1.0a configuration using ScribeJava
 * Replaces legacy CXF OAuth 1.0a implementation
 */
@Configuration
public class OAuthConfig {

    @Value("${oauth.consumer.key:}")
    private String consumerKey;

    @Value("${oauth.consumer.secret:}")
    private String consumerSecret;

    @Value("${oauth.callback.url:http://localhost:8080/oscar/ws/oauth/callback}")
    private String callbackUrl;

    @Bean
    public OAuth10aService oAuth10aService() {
        if (consumerKey == null || consumerKey.isEmpty() || consumerSecret == null || consumerSecret.isEmpty()) {
            // Return a null service if OAuth is not configured
            return null;
        }

        return new ServiceBuilder(consumerKey)
                .apiSecret(consumerSecret)
                .callback(callbackUrl)
                .build(TwitterApi.instance()); // Using Twitter API as OAuth 1.0a example
    }
}   
