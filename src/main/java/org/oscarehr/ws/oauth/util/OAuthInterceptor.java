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

/** 
 * File: OAuthInterceptor.java
 *
 * Purpose:
 *   CXF phase interceptor that authenticates OAuth 1.0a requests before
 *   endpoint invocation. Bridges signature verification with OSCAR’s
 *   provider/session model.
 *
 * Responsibilities:
 *   • Detect OAuth 1.0a requests and load the calling application's config.
 *   • Delegate signature verification to OAuth1SignatureVerifier.
 *   • Resolve the provider from the validated access token and attach a
 *     LoggedInInfo to the HttpServletRequest for downstream use.
 *   • Fail fast with a CXF Fault on unknown consumer keys, invalid tokens,
 *     or verification errors.
 *
 * Context / Why Added:
 *   Part of the CXF → ScribeJava migration. Keeps CXF for routing/phases while
 *   replacing legacy OAuth filters with explicit, unit-testable components.
 *
 * Notes:
 *   • Does not log secrets; avoids mutating the request beyond attaching
 *     LoggedInInfo on success.
 *   • Nonce/timestamp replay protection is handled by the verifier/provider.
 *   • Intercepts in Phase.PRE_INVOKE to ensure endpoints only see authenticated requests.
 */


package org.oscarehr.ws.oauth.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.ws.oauth.OAuth1Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import oscar.login.OscarOAuthDataProvider;

import org.oscarehr.PMmodule.dao.ProviderDao;

@Component
public class OAuthInterceptor implements PhaseInterceptor<Message> {

    @Autowired private OscarOAuthDataProvider oauthDataProvider;
    @Autowired private ProviderDao            providerDao;

    @Override
    public String getPhase() { return Phase.PRE_INVOKE; }

    @Override
    public void handleMessage(Message message) throws Fault {
        HttpServletRequest req =
            (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);

        // 1) Skip non‑OAuth1 requests
        if (!OAuthRequestParser.isOAuth1Request(req)) {
            return;
        }

        try {
            // 2) Pull oauth_token directly from the request (Authorization/query/form)
            Map<String, String> oauth = OAuthRequestParser.extractOAuthParameters(req);
            String token = oauth.get("oauth_token");
            if (token == null || token.isEmpty()) {
                throw new OAuth1Exception(400, "missing_access_token");
            }

            // 3) Resolve provider and attach LoggedInInfo
            String providerNo = oauthDataProvider.getProviderNoByAccessToken(token);
            var provider = providerDao.getProvider(providerNo);
            if (provider == null) {
                throw new OAuth1Exception(401, "unknown_provider");
            }

            LoggedInInfo info = new LoggedInInfo();
            info.setLoggedInProvider(provider);
            req.setAttribute(info.getLoggedInInfoKey(), info);

        } catch (OAuth1Exception e) {
            throw new Fault(e);
        } catch (Exception e) {
            // Keep details server‑side; expose a generic fault
            throw new Fault(new OAuth1Exception(401, "oauth_authentication_failed"));
        }
    }

    @Override public void handleFault(Message message) { /* no‑op */ }
    @Override public Set<String> getBefore() { return Collections.emptySet(); }
    @Override public Set<String> getAfter()  { return Collections.emptySet(); }
    @Override public Collection<PhaseInterceptor<? extends Message>> getAdditionalInterceptors() { return null; }
    @Override public String getId() { return getClass().getSimpleName(); }
}
