// src/main/java/oscar/login/AppOAuth1Config.java
package oscar.login;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuration class for OAuth 1.0a applications.
 */
public class AppOAuth1Config {
    private String consumerKey;
    private String consumerSecret;
    private String baseUrl;
    private String callbackURI;
    private String applicationURI;
    private List<String> scopes;

    public String getConsumerKey() { return consumerKey; }
    public void setConsumerKey(String consumerKey) { this.consumerKey = consumerKey; }
    
    public String getConsumerSecret() { return consumerSecret; }
    public void setConsumerSecret(String consumerSecret) { this.consumerSecret = consumerSecret; }
    
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    
    public String getCallbackURI() { return callbackURI; }
    public void setCallbackURI(String callbackURI) { this.callbackURI = callbackURI; }
    
    public String getApplicationURI() { return applicationURI; }
    public void setApplicationURI(String applicationURI) { this.applicationURI = applicationURI; }
    
    public List<String> getScopes() { return scopes; }
    public void setScopes(List<String> scopes) { this.scopes = scopes; }

    /**
     * Creates an AppOAuth1Config from a configuration document.
     * 
     * @param doc The configuration document (JSON string)
     * @return AppOAuth1Config instance or null if parsing fails
     */
    public static AppOAuth1Config fromDocument(Object doc) {
        if (doc == null || !(doc instanceof String)) {
            return null;
        }
        
        String json = (String) doc;
        if (json.trim().isEmpty()) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, AppOAuth1Config.class);
        } catch (IOException e) {
            // Log the error, but for now, we just return null
            e.printStackTrace();
            return null;
        }
    }
}