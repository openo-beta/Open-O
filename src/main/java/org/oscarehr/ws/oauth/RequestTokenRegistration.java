package org.oscarehr.ws.oauth;

public class RequestTokenRegistration {
    private Client client;
    private String callback;
    private String[] scopes;

    public RequestTokenRegistration(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String[] getScopes() {
        return scopes;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }
}
