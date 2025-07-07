package com.example.oauth;

import org.apache.cxf.rs.security.oauth2.filters.OAuthRequestFilter;
import org.apache.cxf.rs.security.oauth2.provider.AccessTokenValidator;
import org.apache.cxf.rs.security.oauth2.provider.OAuthDataProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuth2Config {

    @Bean
    public OAuthDataProvider oauthDataProvider() {
        // You'll need to implement this or use a provided implementation
        // For example, you might use JPA-based provider or in-memory provider
        return new CustomOAuthDataProvider();
    }

    @Bean
    public AccessTokenValidator accessTokenValidator() {
        // Use the basic AccessTokenValidator implementation
        AccessTokenValidator validator = new AccessTokenValidator();
        validator.setDataProvider(oauthDataProvider());
        return validator;
    }

    @Bean
    public OAuthRequestFilter oauthRequestFilter() {
        OAuthRequestFilter filter = new OAuthRequestFilter();
        filter.setDataProvider(oauthDataProvider());
        return filter;
    }

    @Bean
    public SecureService secureService() {
        SecureService service = new SecureService();
        service.setDataProvider(oauthDataProvider());
        service.setTokenValidator(accessTokenValidator());
        return service;
    }
}
