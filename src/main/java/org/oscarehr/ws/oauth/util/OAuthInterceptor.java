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

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import org.oscarehr.common.dao.AppDefinitionDao;
import org.oscarehr.common.model.AppDefinition;
import org.oscarehr.app.AppOAuth1Config;
import oscar.login.OscarOAuthDataProvider;
import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.common.model.Provider;
import org.oscarehr.util.LoggedInInfo;



public class OAuthInterceptor implements PhaseInterceptor<Message> {

    @Autowired
    private AppDefinitionDao appDefinitionDao;

    @Autowired
    private OscarOAuthDataProvider oauthDataProvider;

    @Autowired
    private ProviderDao providerDao;

    @Override
    public String getPhase() {
        return Phase.PRE_INVOKE;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        try {
            // 1) Grab the HTTP servlet request
            HttpServletRequest req =
                (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);

            // 2) Extract oauth_consumer_key, oauth_token, oauth_signature
            Map<String, String> oauthParams = extractOAuthParameters(req);
            String consumerKey = oauthParams.get(OAuthConstants.CONSUMER_KEY);
            String token       = oauthParams.get(OAuthConstants.TOKEN);
            String incomingSig = oauthParams.get(OAuthConstants.SIGNATURE);
            if (consumerKey == null || token == null || incomingSig == null) {
                // not an OAuth1 request → skip
                return;
            }

            // 3) Lookup AppDefinition by consumerKey
            AppDefinition appDef = appDefinitionDao.findByConsumerKey(consumerKey);
            if (appDef == null) {
                throw new IllegalArgumentException("Unknown consumer_key: " + consumerKey);
            }
            AppOAuth1Config cfg = AppOAuth1Config.fromDocument(appDef.getConfig());

            // 4) Build ScribeJava OAuth10aService
            OAuth10aService service = new ServiceBuilder(cfg.getConsumerKey())
                .apiSecret(cfg.getConsumerSecret())
                .build(new GenericOAuth10aApi(cfg.getBaseURL()));

            // 5) Fetch the stored token secret
            String tokenSecret = oauthDataProvider.getTokenSecret(token);
            if (tokenSecret == null) {
                throw new IllegalArgumentException("Unknown or expired token: " + token);
            }
            OAuth1AccessToken accessToken = new OAuth1AccessToken(token, tokenSecret);

            // 6) Recompute the signature on the exact same request
            String url = req.getRequestURL().toString();
            String qs  = req.getQueryString();
            if (qs != null) {
                url += "?" + qs;
            }
            OAuthRequest sreq = new OAuthRequest(Verb.valueOf(req.getMethod()), url);

            if ("POST".equalsIgnoreCase(req.getMethod())) {
                req.getParameterMap().forEach((k, v) -> {
                    if (!k.startsWith("oauth_")) {
                        for (String val : v) {
                            sreq.addParameter(k, val);
                        }
                    }
                });
            }

            // sign the request
            service.signRequest(accessToken, sreq);

            // pull out the single Authorization header value
            String authzHeader = sreq.getHeaders().get("Authorization");

            // parse out the oauth_signature (your helper will safely handle null/empty)
            String computedSig = extractSignatureFromHeader(authzHeader);

            // constant‐time signature comparison to avoid timing attacks
            byte[] incomingBytes = incomingSig != null
                ? incomingSig.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                : new byte[0];
            byte[] computedBytes = computedSig != null
                ? computedSig.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                : new byte[0];

            if (!java.security.MessageDigest.isEqual(incomingBytes, computedBytes)) {
                throw new IllegalArgumentException("Invalid OAuth1 signature");
            }



            // 7) Load OSCAR Provider by token
            String providerNo = oauthDataProvider.getProviderNoByToken(token);
            Provider provider = providerDao.getProvider(providerNo);
            if (provider == null) {
                throw new IllegalArgumentException("Unknown provider for token: " + token);
            }

            // 8) Attach LoggedInInfo to the request
            LoggedInInfo info = new LoggedInInfo();
            info.setLoggedInProvider(provider);
            req.setAttribute(info.LOGGED_IN_INFO_KEY, info);

        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    @Override
    public void handleFault(Message message) {
        // no-op
    }

    @Override
    public Set<String> getBefore() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getAfter() {
        return Collections.emptySet();
    }

    @Override
    public Collection<PhaseInterceptor<? extends Message>> getAdditionalInterceptors() {
        return null;
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    //——— Helper Methods ———

    /**
     * Extract all oauth_* params from the Authorization header or request parameters.
     */
    private Map<String,String> extractOAuthParameters(HttpServletRequest req) {
        Map<String,String> m = new HashMap<>();

        // from Authorization: OAuth ...
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("OAuth ")) {
            String[] parts = auth.substring(6).split(",");
            for (String kv : parts) {
                String[] pair = kv.trim().split("=", 2);
                if (pair.length == 2) {
                    String k = pair[0];
                    String v = pair[1].replaceAll("^\"|\"$", "");
                    m.put(k, urlDecode(v));
                }
            }
        }

        // also from query/form
        Enumeration<String> names = req.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith("oauth_")) {
                m.put(name, req.getParameter(name));
            }
        }
        return m;
    }

    /**
     * Pull the oauth_signature value back out of the signed Authorization header.
     */
    private String extractSignatureFromHeader(String authzHeader) {
        if (authzHeader == null || !authzHeader.startsWith("OAuth ")) {
            return null;
        }
        for (String kv : authzHeader.substring(6).split(",")) {
            kv = kv.trim();
            if (kv.startsWith("oauth_signature=")) {
                String v = kv.split("=", 2)[1].replaceAll("^\"|\"$", "");
                try {
                    return URLDecoder.decode(v, "UTF-8");
                } catch (IOException e) {
                    return v;
                }
            }
        }
        return null;
    }


    private String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (IOException e) {
            return input;
        }
    }


    /**
     * Minimal DefaultApi10a for ScribeJava, pointing at our provider endpoints.
     */
    private static class GenericOAuth10aApi extends DefaultApi10a {
        private final String baseUrl;
        GenericOAuth10aApi(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        @Override
        public String getRequestTokenEndpoint() {
            return baseUrl + "/oauth/request_token";
        }
        @Override
        public String getAccessTokenEndpoint() {
            return baseUrl + "/oauth/access_token";
        }
        @Override
        public String getAuthorizationBaseUrl() {
            return baseUrl + "/oauth/authorize";
        }
    }
}
