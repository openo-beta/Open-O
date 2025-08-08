package org.oscarehr.ws.oauth;

public class OAuth1Exception extends RuntimeException {
    private final int httpCode;
    public OAuth1Exception(int httpCode, String message) {
        super(message);
        this.httpCode = httpCode;
    }
    public int getHttpCode() { return httpCode; }
}
