package org.oscarehr.ws.oauth.modern;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.apis.GoogleApi20;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Modern OAuth 2.0 configuration using ScribeJava
 * Replaces legacy CXF OAuth 1.0a implementation
 */
@Configuration
public class OAuthConfig {

    @Value("${oauth.client.id:}")
    private String clientId;

    @Value("${oauth.client.secret:}")
    private String clientSecret;

    @Value("${oauth.callback.url:http://localhost:8080/oscar/ws/oauth/callback}")
    private String callbackUrl;

    @Value("${oauth.scope:openid profile email}")
    private String scope;

    @Bean
    public OAuth20Service oAuth20Service() {
        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            // Return a null service if OAuth is not configured
            return null;
        }

        return new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .defaultScope(scope)
                .callback(callbackUrl)
                .build(GoogleApi20.instance()); // Can be configured for other providers
    }
}
