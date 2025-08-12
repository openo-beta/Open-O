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
 * 
 * Migrated from Apache CXF to ScribeJava OAuth1 implementation.
 */
package oscar.login;

import org.oscarehr.ws.oauth.OAuth1Request;
import org.oscarehr.ws.oauth.OAuth1SignatureVerifier;
import org.oscarehr.ws.oauth.OAuth1Exception;
import org.oscarehr.ws.oauth.Client;
import org.oscarehr.ws.oauth.RequestTokenRegistration;
import org.oscarehr.ws.oauth.RequestToken;
import org.oscarehr.ws.oauth.util.OAuth1ParamParser;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping // servlet handles /ws/oauth/*
public class OscarRequestTokenService {

    private final OscarOAuthDataProvider dataProvider;
    private final OAuth1ParamParser parser;
    private final OAuth1SignatureVerifier verifier;

    public OscarRequestTokenService(OscarOAuthDataProvider dataProvider,
                                    OAuth1ParamParser parser,
                                    OAuth1SignatureVerifier verifier) {
        this.dataProvider = dataProvider;
        this.parser = parser;
        this.verifier = verifier;
    }

    @RequestMapping(
        value = "/initiate",
        method = {RequestMethod.GET, RequestMethod.POST},
        produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public String initiate(HttpServletRequest req) {
        OAuth1Request oreq = parser.parseFromRequest(req);

        Client client = dataProvider.getClient(oreq.consumerKey);
        if (client == null) {
            throw new OAuth1Exception(401, "invalid_consumer");
        }

        // Build config expected by the verifier
        // Use whatever constructor/setters your AppOAuth1Config provides.
        AppOAuth1Config cfg = new AppOAuth1Config();
        cfg.setConsumerKey(client.getConsumerKey());
        cfg.setConsumerSecret(client.getSecret());
        cfg.setApplicationURI(req.getRequestURL().toString());

        
        try {
            // ignore the return value for /initiate; you just need it to not throw
            verifier.verifySignature(req, cfg);
        } catch (org.oscarehr.ws.oauth.OAuth1Exception e) {
            // pass through your domain error
            throw e;
        } catch (Exception e) {
            // normalize any other failure
            throw new org.oscarehr.ws.oauth.OAuth1Exception(401, "invalid_signature");
        }
        RequestTokenRegistration reg = new RequestTokenRegistration(client);
        reg.setCallback(oreq.callback); // may be "oob"
        if (oreq.scopesCsv != null && !oreq.scopesCsv.isBlank()) {
            reg.setScopes(oreq.scopesCsv.split("\\s+"));
        }
        RequestToken rt = dataProvider.createRequestToken(reg);

        return "oauth_token=" + enc(rt.getTokenKey())
             + "&oauth_token_secret=" + enc(rt.getTokenSecret())
             + "&oauth_callback_confirmed=true";
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
