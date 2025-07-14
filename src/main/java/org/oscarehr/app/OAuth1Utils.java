//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package org.oscarehr.app; 

import java.io.IOException; 
import java.util.List; 
import java.util.Map; 
import java.util.concurrent.ExecutionException; 

import org.apache.cxf.jaxrs.provider.json.JSONProvider; 
import org.apache.logging.log4j.Logger; 
import org.oscarehr.common.model.AppDefinition; 
import org.oscarehr.common.model.AppUser; 
import org.oscarehr.common.model.ServiceClient; 
import org.oscarehr.util.LoggedInInfo; 
import org.oscarehr.util.MiscUtils; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Component; 

import com.fasterxml.jackson.core.JsonProcessingException; 
import com.fasterxml.jackson.databind.ObjectMapper; 

import com.github.scribejava.core.model.OAuth1AccessToken; 
import com.github.scribejava.core.model.OAuthRequest; 
import com.github.scribejava.core.model.Verb; 
import com.github.scribejava.core.oauth.OAuth10aService; 

import oscar.log.LogAction; 
import oscar.login.OscarOAuthDataProvider;

@Component
public class OAuth1Utils {
    // Shared logger instance for diagnostic and error reporting
    private static final Logger logger = MiscUtils.getLogger();
    // Jackson mapper used to serialize POST payloads to JSON
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // The dataProvider supplies consumer secrets, service construction, and token lookup
    private static OscarOAuthDataProvider dataProvider;

    @Autowired
    public void setDataProvider(OscarOAuthDataProvider dp) {
        OAuth1Utils.dataProvider = dp;
    }

    /**
     * Provide a CXF JSONProvider that drops root elements, for web client usage.
     */
    public static List<Object> getProviderK2A() {
        JSONProvider<Object> provider = new JSONProvider<>();
        provider.setDropRootElement(true);
        return List.of(provider);
    }

    /**
     * Perform a signed HTTP GET to an OAuth1-protected resource. Logs and returns
     * the response body, or null on failure.
     */
    public static String getOAuthGetResponse(
        LoggedInInfo info,
        AppDefinition app,
        AppUser user,
        String requestURI,
        String baseRequestURI
    ) {
        try {
            // Parse OAuth config (consumer key/secret, endpoints)
            AppOAuth1Config cfg = AppOAuth1Config.fromDocument(app.getConfig());
            // Build a service instance with ScribeJava
            OAuth10aService service = dataProvider.buildService(cfg.getConsumerKey(), null);
            // Lookup stored token secret for this user
            Map<String,String> authData = AppOAuth1Config.getKeySecret(user.getAuthenticationData());
            OAuth1AccessToken token = dataProvider.getStoredAccessToken(authData.get("key"));

            // Construct full URL and sign request
            ServiceClient client = dataProvider.getClient(cfg.getConsumerKey());
            String fullUrl = client.getUri() + requestURI;
            OAuthRequest req = new OAuthRequest(Verb.GET, fullUrl);
            service.signRequest(token, req);

            // Execute and inspect response
            com.github.scribejava.core.model.Response resp = service.execute(req);
            int code = resp.getCode();
            if (code < 200 || code >= 300) {
                logger.warn("GET {} -> status {} (baseURI={})", fullUrl, code, baseRequestURI);
            }
            String body = resp.getBody();

            // Audit log the entire GET operation
            LogAction.addLog(
                info,
                "oauth1.GET, AppId=" + app.getId() + ", AppUser=" + user.getId(),
                client.getUri() + baseRequestURI,
                "AppUser=" + user.getId(),
                null,
                body
            );
            return body;

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Network or signing error during GET", e);
        } catch (Exception e) {
            logger.error("Configuration error during GET", e);
        }
        return null;
    }

    /**
     * Perform a signed HTTP POST with optional JSON body. Returns response body.
     */
    public static String getOAuthPostResponse(
        LoggedInInfo info,
        AppDefinition app,
        AppUser user,
        String requestURI,
        String baseRequestURI,
        List<Object> providers,
        Object obj
    ) {
        try {
            // Load and build service as above
            AppOAuth1Config cfg = AppOAuth1Config.fromDocument(app.getConfig());
            OAuth10aService service = dataProvider.buildService(cfg.getConsumerKey(), null);
            Map<String,String> authData = AppOAuth1Config.getKeySecret(user.getAuthenticationData());
            OAuth1AccessToken token = dataProvider.getStoredAccessToken(authData.get("key"));

            // Prepare POST request
            ServiceClient client = dataProvider.getClient(cfg.getConsumerKey());
            String fullUrl = client.getUri() + requestURI;
            OAuthRequest req = new OAuthRequest(Verb.POST, fullUrl);

            // Attach JSON payload if provided
            if (obj != null) {
                req.addHeader("Content-Type", "application/json; charset=UTF-8");
                req.addHeader("Accept", "application/json, text/plain, */*");
                try {
                    String payload = objectMapper.writeValueAsString(obj);
                    req.setPayload(payload);
                } catch (JsonProcessingException ex) {
                    logger.error("Failed to serialize POST payload", ex);
                    throw ex;
                }
            }

            // Sign, execute, and log
            service.signRequest(token, req);
            com.github.scribejava.core.model.Response resp = service.execute(req);
            String body = resp.getBody();
            LogAction.addLog(
                info,
                "oauth1.POST, AppId=" + app.getId() + ", AppUser=" + user.getId(),
                client.getUri() + baseRequestURI,
                "AppUser=" + user.getId(),
                null,
                body
            );
            return body;

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Network or signing error during POST", e);
        } catch (Exception e) {
            logger.error("Configuration error during POST", e);
        }
        return null;
    }

    /**
     * Perform a signed HTTP DELETE. Logs status and response body; preserves void signature.
     */
    public static void getOAuthDeleteResponse(
        AppDefinition app,
        AppUser user,
        String requestURI,
        String auditURI
    ) {
        try {
            // Build service and token
            AppOAuth1Config cfg = AppOAuth1Config.fromDocument(app.getConfig());
            OAuth10aService service = dataProvider.buildService(cfg.getConsumerKey(), null);
            Map<String,String> authData = AppOAuth1Config.getKeySecret(user.getAuthenticationData());
            OAuth1AccessToken token = dataProvider.getStoredAccessToken(authData.get("key"));

            // Construct and sign DELETE request
            ServiceClient client = dataProvider.getClient(cfg.getConsumerKey());
            String fullUrl = client.getUri() + requestURI;
            OAuthRequest req = new OAuthRequest(Verb.DELETE, fullUrl);
            service.signRequest(token, req);
            com.github.scribejava.core.model.Response resp = service.execute(req);

            // Log outcome
            int code = resp.getCode();
            String body = resp.getBody();
            logger.info("DELETE {} → {}: {}", fullUrl, code, body);
            if (code < 200 || code >= 300) {
                logger.error("DELETE request failed (status={}): {}", code, body);
            }
        } catch (Exception e) {
            logger.error("Error executing DELETE {}: {}", requestURI, e.getMessage(), e);
        }
    }
}
