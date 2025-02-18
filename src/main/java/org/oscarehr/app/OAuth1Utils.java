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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.oscarehr.ws.oauth.OAuth1Client;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import org.oscarehr.common.model.AppDefinition;
import org.oscarehr.common.model.AppUser;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;

import oscar.log.LogAction;

public class OAuth1Utils {
    private static final Logger logger = MiscUtils.getLogger();

    public static String getOAuthGetResponse(LoggedInInfo loggedInInfo, AppDefinition app, AppUser user, String requestURI, String baseRequestURI) {
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        BufferedReader bufferedReader = null;
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            OAuth1Client oauthClient = new OAuth1Client(appAuthConfig.getConsumerKey(), appAuthConfig.getConsumerSecret(), null);
            OAuth1AccessToken accessToken = new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            OAuthRequest request = new OAuthRequest(Verb.GET, appAuthConfig.getBaseURL() + requestURI);
            oauthClient.getService().signRequest(accessToken, request);

            Response response = oauthClient.getService().execute(request);
            in = response.getStream();
            bufferedReader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            String action = "oauth1.GET, AppId=" + app.getId() + ", AppUser=" + user.getId();
            String content = appAuthConfig.getBaseURL() + baseRequestURI;
            String contentId = "AppUser=" + user.getId();
            String demographicNo = null;
            String data = sb.toString();
            LogAction.addLog(loggedInInfo, action, content, contentId, demographicNo, data);
            logger.debug("logaction " + action);

            return data;
        } catch (Exception e) {
            logger.error("Error getting information from OAuth Service", e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    public static String getOAuthPostResponse(LoggedInInfo loggedInInfo, AppDefinition app, AppUser user, String requestURI, String baseRequestURI, List<Object> providers, Object obj) {
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        BufferedReader bufferedReader = null;
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            OAuth1Client oauthClient = new OAuth1Client(appAuthConfig.getConsumerKey(), appAuthConfig.getConsumerSecret(), null);
            OAuth1AccessToken accessToken = new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            OAuthRequest request = new OAuthRequest(Verb.POST, appAuthConfig.getBaseURL() + requestURI);
            request.addHeader("Content-Type", "application/json;charset=utf-8");
            request.setPayload(obj.toString()); 

            oauthClient.getService().signRequest(accessToken, request);

            Response response = oauthClient.getService().execute(request);
            in = response.getStream();
            bufferedReader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String action = "oauth1.POST, AppId=" + app.getId() + ", AppUser=" + user.getId();
            String content = appAuthConfig.getBaseURL() + baseRequestURI;
            String contentId = "AppUser=" + user.getId();
            String demographicNo = null;
            String data = sb.toString();
            LogAction.addLog(loggedInInfo, action, content, contentId, demographicNo, data);

            return data;
        } catch (Exception e) {
            logger.error("Error posting information to OAuth Service", e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    public static void getOAuthDeleteResponse(AppDefinition app, AppUser user, String requestURI, String baseRequestURI) {
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        BufferedReader bufferedReader = null;
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            OAuth1Client oauthClient = new OAuth1Client(appAuthConfig.getConsumerKey(), appAuthConfig.getConsumerSecret(), null);
            OAuth1AccessToken accessToken = new OAuth1AccessToken(keySecret.get("key"), keySecret.get("secret"));

            OAuthRequest request = new OAuthRequest(Verb.DELETE, appAuthConfig.getBaseURL() + requestURI);
            oauthClient.getService().signRequest(accessToken, request);

            oauthClient.getService().execute(request);
            logger.debug("OAuth1 DELETE request successful: " + requestURI);
        } catch (Exception e) {
            logger.error("Error getting information from OAuth Service", e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(bufferedReader);
        }
    }
}
