<%--

    Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
    This software is published under the GPL GNU General Public License.
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

    This software was written for the
    Department of Family Medicine
    McMaster University
    Hamilton
    Ontario, Canada

--%>
<%@page import="com.github.scribejava.core.model.OAuth1RequestToken,java.util.*,java.net.*,org.oscarehr.common.dao.*,org.oscarehr.common.model.*,org.oscarehr.util.*,org.oscarehr.app.*" %>
<%

    AppUserDao appUserDao = SpringUtils.getBean(AppUserDao.class);
    AppDefinitionDao appDefinitionDao = SpringUtils.getBean(AppDefinitionDao.class);

    Integer appId = null;
    if (request.getParameter("id") != null) {
        try {
            appId = Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            if ("K2A".equals(request.getParameter("id"))) {
                AppDefinition k2aApp = appDefinitionDao.findByName("K2A");
                if (k2aApp != null) {
                    appId = k2aApp.getId();
                }
            }
        }
    } else if (session.getAttribute("appId") != null) {
        appId = (Integer) session.getAttribute("appId");
    }

    if (appId == null) {
        response.sendRedirect("close.jsp");
    }

    AppDefinition appDef = appDefinitionDao.find(appId);

    AppOAuth1Config oauth1config = (AppOAuth1Config) AppOAuth1Config.fromDocument(appDef.getConfig());

    if (request.getParameter("oauth_verifier") == null) {      //need to request a token
        try {
            // Request token handling is now done by the OAuth service layer
            // The JSP should redirect to the authorization URL provided by the service
            OAuth1RequestToken requestToken = (OAuth1RequestToken) request.getAttribute("requestToken");
            if (requestToken != null) {
                session.setAttribute("requestToken", requestToken);
                session.setAttribute("appId", appId);
                String authUrl = (String) request.getAttribute("authorizationUrl");
                response.sendRedirect(authUrl);
            } else {
                throw new Exception("No request token available");
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error getting Request Token from app " + appId + " for user " + (String) session.getAttribute("user"), e);
            session.setAttribute("oauthMessage", "Error requesting token from app");
            response.sendRedirect("close.jsp");
        }
    } else {
        try {
            String oauthVerifier = request.getParameter("oauth_verifier");
            OAuth1RequestToken requestToken = (OAuth1RequestToken) session.getAttribute("requestToken");
            
            // Access token handling is now done by the OAuth service layer
            // The access token should be available as a request attribute
            String accessTokenString = (String) request.getAttribute("accessToken");
            String accessTokenSecret = (String) request.getAttribute("accessTokenSecret");

            //appUserDao
            AppUser appuser = new AppUser();
            appuser.setAppId(appId);
            appuser.setProviderNo((String) session.getAttribute("user"));

            String authenticationData = AppOAuth1Config.getTokenXML(accessTokenString, accessTokenSecret);
            appuser.setAuthenticationData(authenticationData);

            appuser.setAdded(new Date());
            appUserDao.saveEntity(appuser);
        } catch (Exception e) {
            session.setAttribute("oauthMessage", "Error with verifing authentication");
            MiscUtils.getLogger().error("Error returning from app " + appId + " for user " + (String) session.getAttribute("user"), e);
            response.sendRedirect("close.jsp");
        }

        session.setAttribute("oauthMessage", "Success");
        session.removeAttribute("requestToken");
        session.removeAttribute("appId");
        response.sendRedirect("close.jsp");
    }
%>
