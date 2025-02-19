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

package org.oscarehr.ws.oauth.util;


import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import org.springframework.beans.factory.annotation.Autowired;

import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.common.model.Provider;
import org.oscarehr.PMmodule.dao.ProviderDao;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.oauth.OAuth10aService;

public class OAuthInterceptor implements PhaseInterceptor<Message> {

    @Autowired
    protected ProviderDao providerDao;


    private final OAuth10aService service;

    public OAuthInterceptor(OAuth10aService service) {
        this.service = service;
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        /*
         * Setup a LoggedInInfo and throw it onto the Request for use elsewhere.
         * All we have is a providerNo so we access the ProviderDao directly.
         * There (likely) may be a better way to do this.
         *
         */

        // Create a new LoggedInInfo
        LoggedInInfo loggedInInfo = new LoggedInInfo();

        OAuth1AccessToken accessToken = getOAuthAccessTokenFromMessage(message);

        if (accessToken == null) {
            return;
        }

        String providerNo = accessToken.getToken(); 
        Provider provider = providerDao.getProvider(providerNo);
        loggedInInfo.setLoggedInProvider(provider);

        /* NOTE:
         * A LoggedInInfo object from OAuth will NOT have the following:
         * - session (no active session -- OAuth requests are stateless)
         * - loggedInSecurity (the logged in user is OAuth, so no actual username/password security)
         * - currentFacility (this could change, I'm not sure what it's for)
         * - initiatingCode (this could change, I'm not sure what it's for)
         * - locale (this could change, I'm not sure what it's for)
         */

        // Throw our new loggedInInfo onto the request for future use.
        HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        request.setAttribute(new LoggedInInfo().LOGGED_IN_INFO_KEY, loggedInInfo);

        return;
    }

    private OAuth1AccessToken getOAuthAccessTokenFromMessage(Message message) {
        String token = (String) message.get("oauth_token"); 
        String tokenSecret = (String) message.get("oauth_token_secret"); 

        if (token != null && tokenSecret != null) {
            return new OAuth1AccessToken(token, tokenSecret);
        }

        return null;
    }

    public Collection<PhaseInterceptor<? extends Message>> getAdditionalInterceptors() {
        return null;
    }

    public Set<String> getAfter() {
        return Collections.emptySet();
    }

    public Set<String> getBefore() {
        return Collections.emptySet();
    }

    public String getId() {
        return getClass().getName();
    }

    public String getPhase() {
        return Phase.PRE_INVOKE;
    }

    public void handleFault(Message message) {
    }
}
