// src/main/java/oscar/login/OAuthServiceFactory.java
package oscar.login;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.oscarehr.util.MiscUtils;
import org.springframework.stereotype.Component;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.oauth.OAuth10aService;

@Component
public class OAuthServiceFactory {
    private static final Logger logger = MiscUtils.getLogger();

    /**
     * Build an OAuth10aService for this app/config + callback.
     * We inline the three OAuth endpoints based on the baseUrl.
     */
    public OAuth10aService buildService(AppOAuth1Config cfg, 
                                        String callbackUrl, 
                                        HttpServletResponse resp) 
                                        throws IOException {
        try {
            return new ServiceBuilder(cfg.getConsumerKey())
                .apiSecret(cfg.getConsumerSecret())
                .callback(callbackUrl)
                .build(new DefaultApi10a() {
                    @Override
                    public String getRequestTokenEndpoint() {
                        return cfg.getBaseUrl() + "/oauth/request_token";
                    }
                    @Override
                    public String getAccessTokenEndpoint() {
                        return cfg.getBaseUrl() + "/oauth/access_token";
                    }
                    @Override
                    public String getAuthorizationBaseUrl() {
                        return cfg.getBaseUrl() + "/oauth/authorize";
                    }
                });
        } catch (Exception e) {
            logger.error("Failed to build OAuth service: {}", e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           "OAuth service initialization error");
            return null;
        }
    }
}
