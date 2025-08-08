package org.oscarehr.ws.oauth;

import java.util.ArrayList;
import java.util.List;

public class RequestToken {

    private Client client;
    private String tokenKey;
    private String tokenSecret;
    private List<OAuth1Permission> scopes = new ArrayList<>();
    private String callback;
    private String verifier;

    public RequestToken(Client client, String tokenKey, String tokenSecret) {
        this.client = client;
        this.tokenKey = tokenKey;
        this.tokenSecret = tokenSecret;
    }

    public Client getClient() {
        return client;
    }

    public String getTokenKey() {
        return tokenKey;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public List<OAuth1Permission> getScopes() {
        return scopes;
    }

    public void setScopes(List<OAuth1Permission> scopes) {
        this.scopes = scopes;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getVerifier() {
        return verifier;
    }

    public void setVerifier(String verifier) {
        this.verifier = verifier;
    }
}
