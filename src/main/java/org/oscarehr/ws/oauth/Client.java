/**
 * Purpose: Domain model for an OAuth 1.0a client/application.
 * Contains: consumerKey/secret, app name, callback/application URIs, status.
 * Used by: Lookup during signature verification and token issuance.
 * Notes:
 *   • consumerSecret must be stored securely and never logged.
 *   • Consider rotating secrets and tracking last-used timestamps.
 */

package org.oscarehr.ws.oauth;

public class Client {
    private String consumerKey;
    private String secret;
    private String name;
    private String uri;
    private String loginName; // optional, used for storing providerNo

    public Client(String consumerKey, String secret, String name, String uri) {
        this.consumerKey = consumerKey;
        this.secret = secret;
        this.name = name;
        this.uri = uri;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getSecret() {
        return secret;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
