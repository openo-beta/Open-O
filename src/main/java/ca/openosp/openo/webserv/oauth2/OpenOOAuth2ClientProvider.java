/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package ca.openosp.openo.webserv.oauth2;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Abstract base provider for configuring a {@link WebClient} with the OAuth2 Client Credentials grant flow.
 * <p>
 * This class encapsulates the boilerplate required to set up an {@code OAuth2AuthorizedClientManager}
 * and provides lifecycle hooks for customizing token request parameters, authentication methods,
 * and registration identifiers.
 * </p>
 * <p>Subclasses should implement {@link #getCustomTokenParameters()} if the authorization server requires extra fields.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * import org.springframework.beans.factory.annotation.Value;
 * import org.springframework.context.annotation.Bean;
 * import org.springframework.context.annotation.Configuration;
 * import org.springframework.util.LinkedMultiValueMap;
 * import org.springframework.util.MultiValueMap;
 * import org.springframework.web.reactive.function.client.WebClient;
 *
 * @Configuration
 * public class CustomWebClientConfig extends OpenOOAuth2ClientProvider {
 *
 *     private final String userId;
 *
 *     public CustomWebClientConfig(
 *             @Value("${custom3rdParty.oauth2.client.id}") String clientId,
 *             @Value("${custom3rdParty.oauth2.client.secret}") String clientSecret,
 *             @Value("${custom3rdParty.oauth2.client.token_uri}") String tokenUri,
 *             @Value("${custom3rdParty.oauth2.client.user_id}") String userId) {
 *
 *         super(clientId, clientSecret, tokenUri);
 *         this.userId = userId;
 *     }
 *
 *     @Override
 *     protected MultiValueMap<String, String> getCustomTokenParameters() {
 *         MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
 *         params.add("user_id", this.userId);
 *         return params;
 *     }
 *
 *    @Override
 *    protected HttpHeaders getCustomTokenHeaders() {
 *          HttpHeaders headers = new HttpHeaders();
 *          headers.add("X-API-Key", "super-secret-gateway-key");
 *          headers.add("User-Agent", "My-Spring-App-V1");
 *          headers.add("X-Correlation-ID", java.util.UUID.randomUUID().toString());
 *          return headers;
 *    }
 *
 *     @Bean
 *     public WebClient getCustomClient() {
 *         return buildClient();
 *     }
 * }
 * }</pre>
 */
public abstract class OpenOOAuth2ClientProvider {

    private final String clientId;
    private final String clientSecret;
    private final String tokenUri;

    /**
     * Constructs the provider with core OAuth2 credentials.
     *
     * @param clientId     the OAuth2 client ID
     * @param clientSecret the OAuth2 client secret
     * @param tokenUri     the URI of the authorization server's token endpoint
     */
    protected OpenOOAuth2ClientProvider(String clientId, String clientSecret, String tokenUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUri = tokenUri;
    }

    /**
     * Returns the registration ID used for the OAuth2 client configuration.
     * Defaults to the simple name of the concrete subclass, safely unwrapping Spring CGLIB proxies.
     *
     * @return a unique string identifying this client registration
     */
    protected String getRegistrationId() {
        return ClassUtils.getUserClass(this.getClass()).getSimpleName();
    }

    /**
     * Provides custom parameters to be included in the body of the access token request.
     * Override this if the provider requires non-standard fields (e.g., {@code user_id} or {@code resource}).
     *
     * @return a map of additional parameters, defaults to an empty map
     */
    protected MultiValueMap<String, String> getCustomTokenParameters() {
        return new LinkedMultiValueMap<>(); // Default: empty
    }

    /**
     * Provides custom headers to be included in the access token request.
     * Override this if the provider requires non-standard headers.
     *
     * @return a collection of additional headers, defaults to empty
     */
    protected HttpHeaders getCustomTokenHeaders() {
        return new HttpHeaders(); // Default: empty
    }

    /**
     * Defines the method used to authenticate the client with the authorization server.
     * Defaults to {@link ClientAuthenticationMethod#CLIENT_SECRET_POST}.
     *
     * @return the authentication method to use
     */
    protected ClientAuthenticationMethod getClientAuthenticationMethod() {
        return ClientAuthenticationMethod.CLIENT_SECRET_POST; // Default: POST
    }

    /**
     * Builds and configures a {@link WebClient} instance pre-configured with an OAuth2 exchange filter.
     * The resulting client will automatically manage token acquisition, caching, and renewal
     * using the Client Credentials grant type.
     *
     * @return a configured {@link WebClient}
     */
    protected WebClient buildClient() {
        ClientRegistration registration = getClientRegistration();
        ClientRegistrationRepository clientRegistrationRepo = new InMemoryClientRegistrationRepository(registration);

        DefaultClientCredentialsTokenResponseClient tokenResponseClient = getClientCredentialsTokenResponseClient();

        OAuth2AuthorizedClientProvider authorizedClientProvider = getAuthorizedClientProvider(tokenResponseClient);
        OAuth2AuthorizedClientService authorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepo);

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepo, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        ExchangeFilterFunction oauth2Filter = getExchangeFilterFunction(authorizedClientManager);

        return WebClient.builder().filter(oauth2Filter).build();
    }

    /**
     * Configures the client credentials token response client.
     * This includes setting up custom parameter and header converters.
     *
     * @return a configured {@link DefaultClientCredentialsTokenResponseClient}
     */
    private @NonNull DefaultClientCredentialsTokenResponseClient getClientCredentialsTokenResponseClient() {
        DefaultClientCredentialsTokenResponseClient tokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
        OAuth2ClientCredentialsGrantRequestEntityConverter requestEntityConverter = new OAuth2ClientCredentialsGrantRequestEntityConverter();

        requestEntityConverter.addParametersConverter(grantRequest -> getCustomTokenParameters());
        requestEntityConverter.addHeadersConverter(grantRequest -> getCustomTokenHeaders());
        tokenResponseClient.setRequestEntityConverter(requestEntityConverter);
        return tokenResponseClient;
    }

    /**
     * Configures the authorized client provider with the custom token response client.
     *
     * @param tokenResponseClient the client responsible for executing the token request
     * @return a configured {@link OAuth2AuthorizedClientProvider}
     */
    private @NonNull OAuth2AuthorizedClientProvider getAuthorizedClientProvider(DefaultClientCredentialsTokenResponseClient tokenResponseClient) {
        return OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(configurer -> configurer.accessTokenResponseClient(tokenResponseClient))
                .build();
    }

    /**
     * Creates an {@link ExchangeFilterFunction} that intercepts WebClient requests to inject the Bearer token.
     *
     * @param authorizedClientManager the manager responsible for authorizing the client
     * @return the filter function
     */
    private @NonNull ExchangeFilterFunction getExchangeFilterFunction(AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {
        return (request, next) -> {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(getRegistrationId())
                    .principal(getRegistrationId() + "-principal")
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            ClientRequest newRequest = ClientRequest.from(request).headers(headers -> {
                if (authorizedClient != null) {
                    headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
                }
            }).build();

            return next.exchange(newRequest);
        };
    }

    /**
     * Constructs the {@link ClientRegistration} object based on the provider's configuration and hooks.
     *
     * @return the client registration details
     */
    private @NonNull ClientRegistration getClientRegistration() {
        return ClientRegistration
                .withRegistrationId(getRegistrationId())
                .tokenUri(this.tokenUri)
                .clientId(this.clientId)
                .clientSecret(this.clientSecret)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientAuthenticationMethod(getClientAuthenticationMethod())
                .build();
    }
}
