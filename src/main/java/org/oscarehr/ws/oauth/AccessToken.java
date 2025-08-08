package org.oscarehr.ws.oauth;

import java.util.ArrayList;
import java.util.List;

public class AccessToken {

    private Client client;
    private String tokenKey;
    private String tokenSecret;
    private long lifetime;
    private long issuedAt;
    private UserSubject subject;
    private List<OAuth1Permission> scopes = new ArrayList<>();

    public AccessToken(Client client, String tokenKey, String tokenSecret, long lifetime, long issuedAt) {
        this.client = client;
        this.tokenKey = tokenKey;
        this.tokenSecret = tokenSecret;
        this.lifetime = lifetime;
        this.issuedAt = issuedAt;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getTokenKey() {
        return tokenKey;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public long getLifetime() {
        return lifetime;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public UserSubject getSubject() {
        return subject;
    }

    public void setSubject(UserSubject subject) {
        this.subject = subject;
    }

    public List<OAuth1Permission> getScopes() {
        return scopes;
    }

    public void setScopes(List<OAuth1Permission> scopes) {
        this.scopes = scopes;
    }
}
