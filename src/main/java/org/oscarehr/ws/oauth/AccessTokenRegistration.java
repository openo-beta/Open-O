package org.oscarehr.ws.oauth;

public class AccessTokenRegistration {

    private RequestToken requestToken;

    public AccessTokenRegistration(RequestToken requestToken) {
        this.requestToken = requestToken;
    }

    public RequestToken getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(RequestToken requestToken) {
        this.requestToken = requestToken;
    }
}
