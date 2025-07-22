// src/main/java/oscar/login/OAuthConfigService.java
package oscar.login;

import java.io.IOException;
import java.net.URI;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.oscarehr.app.AppOAuth1Config;
import org.oscarehr.common.dao.AppDefinitionDao;
import org.oscarehr.common.model.AppDefinition;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OAuthConfigService {
    private static final Logger logger = MiscUtils.getLogger();

    @Autowired
    private AppDefinitionDao appDefinitionDao;

    /**
     * Retrieves OAuth application configuration for the given consumer key.
     * 
     * @param consumerKey The OAuth consumer key
     * @return AppOAuth1Config or null if not found
     */
    public AppOAuth1Config getOAuthConfiguration(String consumerKey) {
        try {
            AppDefinition app = appDefinitionDao.findByConsumerKey(consumerKey);
            if (app == null) {
                logger.warn("No OAuth configuration found for consumer key: {}", consumerKey);
                return null;
            }
            
            String configStr = app.getConfig();
            if (configStr == null) {
                logger.error("Configuration document missing for app id {}", app.getId());
                return null;
            }
            
            AppOAuth1Config cfg;
            try {
                cfg = AppOAuth1Config.fromDocument(configStr);
            } catch (Exception e) {
                logger.error("Failed to parse OAuth config for app id {}: {}", app.getId(), e.getMessage());
                return null;
            }
            if (cfg == null
             || cfg.getConsumerKey() == null
             || cfg.getConsumerSecret() == null
             || cfg.getBaseURL() == null) {
                logger.error("Invalid OAuth config in app id {}", app.getId());
                return null;
            }
            
            return cfg;
        } catch (Exception e) {
            logger.error("Error retrieving OAuth configuration for consumer key: {}", consumerKey, e);
            return null;
        }
    }

    public AppOAuth1Config loadConfig(AppDefinition app, HttpServletResponse resp) throws IOException {
        String configStr = app.getConfig();
        if (configStr == null) {
            logger.error("Configuration document missing for app id {}", app.getId());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Configuration retrieval error");
            return null;
        }
        AppOAuth1Config cfg;
        try {
            cfg = AppOAuth1Config.fromDocument(configStr);
        } catch (Exception e) {
            logger.error("Failed to parse OAuth config for app id {}: {}", app.getId(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Configuration parsing error");
            return null;
        }
        if (cfg == null
         || cfg.getConsumerKey() == null
         || cfg.getConsumerSecret() == null
         || cfg.getBaseURL() == null) {
            logger.error("Invalid OAuth config in app id {}", app.getId());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid OAuth configuration");
            return null;
        }
        return cfg;
    }

    /**
     * Validates the callback URL against the registered application configuration.
     * 
     * @param config OAuth application configuration
     * @param callbackUrl The callback URL to validate
     * @return true if the callback URL is valid, false otherwise
     */
    public boolean isValidCallback(AppOAuth1Config config, String callbackUrl) {
        // Allow "oob" (out-of-band) callback for desktop applications
        if ("oob".equals(callbackUrl)) {
            return true;
        }
        
        // If no restrictions are configured, allow any callback
        if (!hasConfiguredCallbackRestrictions(config)) {
            return true;
        }
        
        // Check if callback URL matches pre-registered callback URI
        if (config.getCallbackURI() != null && !config.getCallbackURI().isEmpty()) {
            if (callbackUrl.equals(config.getCallbackURI())) {
                return true;
            }
        }

        // Check if callback URL has common root with application URI
        if (config.getApplicationURI() != null && !config.getApplicationURI().isEmpty()) {
            if (callbackUrl.startsWith(config.getApplicationURI())) {
                return true;
            }
        }

        return false;
    }

    public boolean validateRegisteredCallback(AppOAuth1Config cfg, String callbackUrl, HttpServletResponse resp) throws IOException {
        if (isValidCallback(cfg, callbackUrl)) {
            return true;
        }
        logger.error("Callback URL '{}' is not allowed by config (registered='{}', appBase='{}')",
                     callbackUrl, cfg.getCallbackURI(), cfg.getApplicationURI());
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid callback URL");
        return false;
    }

    public boolean validateCallbackFormat(String callbackUrl, HttpServletResponse resp) throws IOException {
        try {
            URI uri = new URI(callbackUrl);
            uri.toURL();
            return true;
        } catch (Exception e) {
            logger.error("Invalid callback URL: {}", callbackUrl);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid callback URL");
            return false;
        }
    }

    public boolean validateApp(AppDefinition app, HttpServletResponse resp) throws IOException {
        if (app == null) {
            logger.error("AppDefinition is null");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing application configuration");
            return false;
        }
        return true;
    }
}
