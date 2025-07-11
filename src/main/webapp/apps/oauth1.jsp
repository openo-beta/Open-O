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

    Migrated from Apache CXF to ScribeJava OAuth1 implementation.
--%>

<%@ page import="com.github.scribejava.core.model.OAuth1RequestToken" %>
<%@ page import="org.oscarehr.common.dao.AppUserDao" %>
<%@ page import="org.oscarehr.common.dao.AppDefinitionDao" %>
<%@ page import="org.oscarehr.common.model.AppUser" %>
<%@ page import="org.oscarehr.common.model.AppDefinition" %>
<%@ page import="org.oscarehr.app.AppOAuth1Config" %>
<%@ page import="org.oscarehr.util.SpringUtils" %>
<%@ page import="org.oscarehr.util.MiscUtils" %>
<%@ page import="java.util.Date" %>
<%@ page import="javax.servlet.http.*" %>

<%
    // Lookup DAOs
    AppUserDao appUserDao      = SpringUtils.getBean(AppUserDao.class);
    AppDefinitionDao appDefDao = SpringUtils.getBean(AppDefinitionDao.class);

    // Determine which app we’re handling
    Integer appId = null;
    String idParam = request.getParameter("id");
    if (idParam != null) {
        try {
            appId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            if ("K2A".equals(idParam)) {
                AppDefinition k2a = appDefDao.findByName("K2A");
                if (k2a != null) {
                    appId = k2a.getId();
                }
            }
        }
    } else if (session.getAttribute("appId") != null) {
        appId = (Integer) session.getAttribute("appId");
    }

    if (appId == null) {
        response.sendRedirect("close.jsp");
        return;
    }

    // Load our OAuth1 app configuration
    AppDefinition   appDef      = appDefDao.find(appId);
    AppOAuth1Config oauthConfig = AppOAuth1Config.fromDocument(appDef.getConfig());

    // Check if we’re in the “request token” phase or “access token” phase
    String oauthVerifier = request.getParameter("oauth_verifier");
    if (oauthVerifier == null) {
        // --- Phase 1: redirect client to provider’s auth URL ---
        try {
            OAuth1RequestToken requestToken =
                (OAuth1RequestToken) request.getAttribute("requestToken");
            if (requestToken == null) {
                throw new IllegalStateException("No requestToken in request");
            }
            // Persist for phase 2
            session.setAttribute("requestToken", requestToken);
            session.setAttribute("appId", appId);

            // Authorization URL was prepared by the service layer
            String authUrl = (String) request.getAttribute("authorizationUrl");
            response.sendRedirect(authUrl);
            return;

        } catch (Exception e) {
            MiscUtils.getLogger().error("Error obtaining request token for app " + appId, e);
            session.setAttribute("oauthMessage", "Error requesting token");
            response.sendRedirect("close.jsp");
            return;
        }
    } else {
        // --- Phase 2: handling callback, exchange for access token ---
        try {
            OAuth1RequestToken requestToken =
                (OAuth1RequestToken) session.getAttribute("requestToken");
            if (requestToken == null) {
                throw new IllegalStateException("No requestToken in session");
            }

            // These were set by your service layer
            String accessTokenString = (String) request.getAttribute("accessToken");
            String accessTokenSecret = (String) request.getAttribute("accessTokenSecret");

            // Persist the authorized user
            AppUser appUser = new AppUser();
            appUser.setAppId(appId);
            appUser.setProviderNo((String) session.getAttribute("user"));

            // Build the XML blob of token & secret
            String authData = AppOAuth1Config.getTokenXML(accessTokenString, accessTokenSecret);
            appUser.setAuthenticationData(authData);
            appUser.setAdded(new Date());

            appUserDao.saveEntity(appUser);

        } catch (Exception e) {
            MiscUtils.getLogger().error("Error verifying authentication for app " + appId, e);
            session.setAttribute("oauthMessage", "Error verifying authentication");
            response.sendRedirect("close.jsp");
            return;
        }

        // Success!
        session.setAttribute("oauthMessage", "Success");
        session.removeAttribute("requestToken");
        session.removeAttribute("appId");
        response.sendRedirect("close.jsp");
        return;
    }
%>