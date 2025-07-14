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

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import oscar.log.LogAction;
import oscar.login.OscarOAuthDataProvider;

/**
 * OAuth1Utils
 *
 * A static-method facade for signing and executing OAuth1 requests using ScribeJava.
 * Internally delegates client lookup and token management to OscarOAuthDataProvider.
 * Maintains original static signatures so existing callers do not break.
 */
@Component
public class OAuth1Utils {
    private static final Logger logger = MiscUtils.getLogger();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Spring-injected provider for all token/client operations
    private static OscarOAuthDataProvider dataProvider;

    @Autowired
    public void setDataProvider(OscarOAuthDataProvider dp) {
        OAuth1Utils.dataProvider = dp;
    }

    /**
     * Return a configured CXF JSON provider that drops root elements.
     */
    public static List<Object> getProviderK2A() {
        JSONProvider<Object> provider = new JSONProvider<>();
        provider.setDropRootElement(true);
        return List.of(provider);
    }

    /**
     * Perform a signed HTTP GET to a protected OAuth1 resource.
     * @return response body, or null on error
     */
    public static String getOAuthGetResponse(LoggedInInfo info,
                                             AppDefinition app,
                                             AppUser user,
                                             String requestURI,
                                             String baseRequestURI) {
        try {
            String consumerKey = AppOAuth1Config.fromDocument(app.getConfig())
                                                .getConsumerKey();
            OAuth10aService service = dataProvider.buildService(consumerKey, null);
            Map<String,String> authData = AppOAuth1Config.getKeySecret(user.getAuthenticationData());
            OAuth1AccessToken token = dataProvider.getStoredAccessToken(authData.get("key"));

            ServiceClient client = dataProvider.getClient(consumerKey);
            String fullUrl = client.getUri() + requestURI;

            OAuthRequest req = new OAuthRequest(Verb.GET, fullUrl);
            service.signRequest(token, req);
            Response resp = service.execute(req);

            int code = resp.getCode();
            if (code < 200 || code >= 300) {
                logger.warn("GET {} -> status {} (baseRequestURI={})", fullUrl, code, baseRequestURI);
            }

            String body = resp.getBody();
            String action = "oauth1.GET, AppId=" + app.getId() + ", AppUser=" + user.getId();
            String content = client.getUri() + baseRequestURI;
            String contentId = "AppUser=" + user.getId();
            LogAction.addLog(info, action, content, contentId, null, body);

            return body;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Network or signing error during GET", e);
        } catch (Exception e) {
            logger.error("Configuration error during GET", e);
        }
        return null;
    }

    /**
     * Perform a signed HTTP POST to a protected OAuth1 resource, with optional JSON body.
     * Original callers pass providers list which is ignored internally.
     */
    public static String getOAuthPostResponse(LoggedInInfo info,
                                              AppDefinition app,
                                              AppUser user,
                                              String requestURI,
                                              String baseRequestURI,
                                              List<Object> providers,
                                              Object obj) {
        try {
            String consumerKey = AppOAuth1Config.fromDocument(app.getConfig())
                                                .getConsumerKey();
            OAuth10aService service = dataProvider.buildService(consumerKey, null);
            Map<String,String> authData = AppOAuth1Config.getKeySecret(user.getAuthenticationData());
            OAuth1AccessToken token = dataProvider.getStoredAccessToken(authData.get("key"));

            ServiceClient client = dataProvider.getClient(consumerKey);
            String fullUrl = client.getUri() + requestURI;
            OAuthRequest req = new OAuthRequest(Verb.POST, fullUrl);

            if (obj != null) {
                req.addHeader("Content-Type", "application/json; charset=UTF-8");
                req.addHeader("Accept", "application/json, text/plain, */*");
                try {
                    req.setPayload(objectMapper.writeValueAsString(obj));
                } catch (JsonProcessingException ex) {
                    logger.error("Failed to serialize POST payload", ex);
                    throw ex;
                }
            }

            service.signRequest(token, req);
            Response resp = service.execute(req);
            String body = resp.getBody();

            String action = "oauth1.POST, AppId=" + app.getId() + ", AppUser=" + user.getId();
            String content = client.getUri() + baseRequestURI;
            String contentId = "AppUser=" + user.getId();
            LogAction.addLog(info, action, content, contentId, null, body);

            return body;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Network or signing error during POST", e);
        } catch (Exception e) {
            logger.error("Configuration error during POST", e);
        }
        return null;
    }

    /**
     * Perform a signed HTTP DELETE to a protected OAuth1 resource.
     * Does not return the body (void signature preserved).
     */
    public static void getOAuthDeleteResponse(AppDefinition app,
                                              AppUser user,
                                              String requestURI,
                                              String baseRequestURI) {
        try {
            String consumerKey = AppOAuth1Config.fromDocument(app.getConfig())
                                                .getConsumerKey();
            OAuth10aService service = dataProvider.buildService(consumerKey, null);
            Map<String,String> authData = AppOAuth1Config.getKeySecret(user.getAuthenticationData());
            OAuth1AccessToken token = dataProvider.getStoredAccessToken(authData.get("key"));

            ServiceClient client = dataProvider.getClient(consumerKey);
            String fullUrl = client.getUri() + requestURI;
            OAuthRequest req = new OAuthRequest(Verb.DELETE, fullUrl);
            service.signRequest(token, req);

            Response resp = service.execute(req);
            int code = resp.getCode();
            if (code < 200 || code >= 300) {
                logger.warn("DELETE {} -> status {} (baseRequestURI={})", fullUrl, code, baseRequestURI);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Network or signing error during DELETE", e);
        } catch (Exception e) {
            logger.error("Configuration error during DELETE", e);
        }
    }
}
