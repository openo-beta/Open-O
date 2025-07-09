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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.apache.logging.log4j.Logger;
import org.oscarehr.common.model.AppDefinition;
import org.oscarehr.common.model.AppUser;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import oscar.log.LogAction;

public class OAuth1Utils {
    private static final Logger logger = MiscUtils.getLogger();

    /**
     * Generic OAuth 1.0a API for custom providers
     */
    private static class GenericOAuth10aApi extends DefaultApi10a {
        private final String requestTokenUrl;
        private final String accessTokenUrl;
        private final String authorizationUrl;
        
        public GenericOAuth10aApi(String baseUrl) {
            this.requestTokenUrl = baseUrl + "/oauth/request_token";
            this.accessTokenUrl = baseUrl + "/oauth/access_token";
            this.authorizationUrl = baseUrl + "/oauth/authorize";
        }
        
        @Override
        public String getRequestTokenEndpoint() {
            return requestTokenUrl;
        }
        
        @Override
        public String getAccessTokenEndpoint() {
            return accessTokenUrl;
        }
        
        @Override
        public String getAuthorizationBaseUrl() {
            return authorizationUrl;
        }
    }

    public static List<Object> getProviderK2A() {
        List<Object> providers = new ArrayList<Object>();
        JSONProvider jsonProvider = new JSONProvider();
        jsonProvider.setDropRootElement(true);
        providers.add(jsonProvider);

        return providers;
    }

    public static String getOAuthGetResponse(LoggedInInfo loggedInInfo, AppDefinition app, AppUser user, String requestURI, String baseRequestURI) {
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            // Create OAuth 1.0a service using ScribeJava
            OAuth10aService service = new ServiceBuilder(appAuthConfig.getConsumerKey())
                    .apiSecret(appAuthConfig.getConsumerSecret())
                    .build(new GenericOAuth10aApi(appAuthConfig.getBaseURL()));

            // Create access token from stored credentials
            OAuth1AccessToken accessToken = new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            // Create and sign the request
            String fullUrl = appAuthConfig.getBaseURL() + requestURI;
            OAuthRequest request = new OAuthRequest(Verb.GET, fullUrl);
            service.signRequest(accessToken, request);

            // Execute the request
            Response response = service.execute(request);
            String data = response.getBody();

            // Log the action
            String action = "oauth1.GET, AppId=" + app.getId() + ", AppUser=" + user.getId();
            String content = appAuthConfig.getBaseURL() + baseRequestURI;
            String contentId = "AppUser=" + user.getId();
            String demographicNo = null;
            LogAction.addLog(loggedInInfo, action, content, contentId, demographicNo, data);
            logger.debug("logaction " + action);

            return data;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error getting information from OAuth Service", e);
            return null;
        } catch (Exception e) {
            logger.error("Error with OAuth configuration", e);
            return null;
        }
    }

    public static String getOAuthPostResponse(LoggedInInfo loggedInInfo, AppDefinition app, AppUser user, String requestURI, String baseRequestURI, List<Object> providers, Object obj) {
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            // Create OAuth 1.0a service using ScribeJava
            OAuth10aService service = new ServiceBuilder(appAuthConfig.getConsumerKey())
                    .apiSecret(appAuthConfig.getConsumerSecret())
                    .build(new GenericOAuth10aApi(appAuthConfig.getBaseURL()));

            // Create access token from stored credentials
            OAuth1AccessToken accessToken = new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            // Create and sign the request
            String fullUrl = appAuthConfig.getBaseURL() + requestURI;
            OAuthRequest request = new OAuthRequest(Verb.POST, fullUrl);
            
            // Add JSON payload if provided
            if (obj != null) {
                request.addHeader("Content-Type", "application/json;charset=utf-8");
                request.addHeader("Accept", "application/json, text/plain, */*");
                request.addHeader("Accept-Encoding", "gzip, deflate");
                request.setPayload(obj.toString());
            }
            
            service.signRequest(accessToken, request);

            // Execute the request
            Response response = service.execute(request);
            String data = response.getBody();

            // Log the action
            String action = "oauth1.POST, AppId=" + app.getId() + ", AppUser=" + user.getId();
            String content = appAuthConfig.getBaseURL() + baseRequestURI;
            String contentId = "AppUser=" + user.getId();
            String demographicNo = null;
            LogAction.addLog(loggedInInfo, action, content, contentId, demographicNo, data);

            return data;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error posting information to OAuth Service", e);
            return null;
        } catch (Exception e) {
            logger.error("Error with OAuth configuration", e);
            return null;
        }
    }

    public static void getOAuthDeleteResponse(AppDefinition app, AppUser user, String requestURI, String baseRequestURI) {
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            // Create OAuth 1.0a service using ScribeJava
            OAuth10aService service = new ServiceBuilder(appAuthConfig.getConsumerKey())
                    .apiSecret(appAuthConfig.getConsumerSecret())
                    .build(new GenericOAuth10aApi(appAuthConfig.getBaseURL()));

            // Create access token from stored credentials
            OAuth1AccessToken accessToken = new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            // Create and sign the request
            String fullUrl = appAuthConfig.getBaseURL() + requestURI;
            OAuthRequest request = new OAuthRequest(Verb.DELETE, fullUrl);
            service.signRequest(accessToken, request);

            // Execute the request
            service.execute(request);
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error deleting information from OAuth Service", e);
        } catch (Exception e) {
            logger.error("Error with OAuth configuration", e);
        }
    }
}
