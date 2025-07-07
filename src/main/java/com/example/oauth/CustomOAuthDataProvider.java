package com.example.oauth;

import org.apache.cxf.rs.security.oauth2.common.AccessToken;
import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.common.OAuthPermission;
import org.apache.cxf.rs.security.oauth2.common.ServerAccessToken;
import org.apache.cxf.rs.security.oauth2.common.UserSubject;
import org.apache.cxf.rs.security.oauth2.provider.OAuthDataProvider;
import org.apache.cxf.rs.security.oauth2.tokens.bearer.BearerAccessToken;
import org.apache.cxf.rs.security.oauth2.tokens.refresh.RefreshToken;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomOAuthDataProvider implements OAuthDataProvider {

    private Map<String, Client> clients = new ConcurrentHashMap<>();
    private Map<String, ServerAccessToken> accessTokens = new ConcurrentHashMap<>();

    public CustomOAuthDataProvider() {
        // Initialize with a default client for testing
        Client client = new Client("test-client", "test-secret", true);
        client.setRedirectUris(Collections.singletonList("http://localhost:8080/callback"));
        clients.put("test-client", client);
    }

    @Override
    public Client getClient(String clientId) {
        return clients.get(clientId);
    }

    public ServerAccessToken createAccessToken(Client client, UserSubject subject, List<String> scopes) {
        BearerAccessToken bearerToken = new BearerAccessToken(client, 3600L);
        bearerToken.setSubject(subject);
        bearerToken.setScopes(scopes);
        accessTokens.put(bearerToken.getTokenKey(), bearerToken);
        return bearerToken;
    }

    @Override
    public ServerAccessToken getAccessToken(String accessToken) {
        return accessTokens.get(accessToken);
    }

    @Override
    public ServerAccessToken getPreauthorizedToken(Client client, List<String> requestedScopes, 
                                                   UserSubject subject, String grantType) {
        BearerAccessToken token = new BearerAccessToken(client, 3600L);
        token.setSubject(subject);
        token.setScopes(requestedScopes);
        accessTokens.put(token.getTokenKey(), token);
        return token;
    }

    @Override
    public void removeAccessToken(ServerAccessToken token) {
        accessTokens.remove(token.getTokenKey());
    }

    @Override
    public List<OAuthPermission> convertScopeToPermissions(Client client, List<String> requestedScopes) {
        return Collections.emptyList();
    }

    @Override
    public void revokeToken(Client client, String token, String tokenTypeHint) {
        accessTokens.remove(token);
    }

    @Override
    public List<ServerAccessToken> getAccessTokens(Client client, UserSubject subject) {
        return Collections.emptyList();
    }

    @Override
    public List<ServerAccessToken> getRefreshTokens(Client client, UserSubject subject) {
        return Collections.emptyList();
    }

    @Override
    public ServerAccessToken refreshAccessToken(Client client, String refreshToken, List<String> requestedScopes) {
        return null;
    }
}
