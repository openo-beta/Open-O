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

/*
 * Written by Brandon Aubie <brandon@aubie.ca>
 */

package org.oscarehr.ws.oauth;

import jakarta.servlet.http.HttpServletRequest;

import org.oscarehr.common.model.Provider;
import org.oscarehr.util.LoggedInInfo;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.oauth.OAuth10aService;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Base class for OAuth web services
 */
public abstract class AbstractServiceImpl {

    private OAuth10aService oauthService;

    /**
     * Set the OAuth10aService instance.
     */
    public void setOAuthService(OAuth10aService oauthService) {
        this.oauthService = oauthService;
    }

    /**
     * Get the current HTTP servlet request.
     */
    protected HttpServletRequest getHttpServletRequest() {
        // Get the current request from Spring's RequestContextHolder
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No current request is available.");
        }
        return attributes.getRequest();
    }

    /**
     * Get the OAuth1AccessToken from the current request.
     */
    protected OAuth1AccessToken getOAuthAccessToken() {
        HttpServletRequest request = getHttpServletRequest();
        String token = request.getParameter("oauth_token");
        String tokenSecret = request.getParameter("oauth_token_secret");
        if (token == null || tokenSecret == null) {
            throw new IllegalStateException("OAuth access token is not available in the request.");
        }
        return new OAuth1AccessToken(token, tokenSecret);
    }

    /**
     * Get the current provider (user) from the logged-in info.
     */
    protected Provider getCurrentProvider() {
        LoggedInInfo loggedInInfo = getLoggedInInfo();
        return loggedInInfo.getLoggedInProvider();
    }

    /**
     * Get the logged-in info from the current request.
     */
    protected LoggedInInfo getLoggedInInfo() {
        HttpServletRequest request = getHttpServletRequest();
        LoggedInInfo info = LoggedInInfo.getLoggedInInfoFromRequest(request);
        if (info == null) {
            throw new IllegalStateException("Authentication info is not available.");
        }
        return info;
    }
}
