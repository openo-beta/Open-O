/**
 * File: AccessTokenResource.java
 *
 * Purpose:
 *   JAX-RS resource handling the OAuth 1.0a "access token" exchange step
 *   (request token → access token). Implements the server-side logic
 *   required by RFC 5849 Section 2.3.
 *
 * Responsibilities:
 *   • Parse the incoming OAuth1 request using OAuth1ParamParser.
 *   • Validate the client’s consumer key, request token, and verifier.
 *   • Verify the request signature using OAuth1SignatureVerifier.
 *   • Delegate token issuance to the OscarOAuthDataProvider.
 *   • Return a form-encoded access token response containing
 *     oauth_token and oauth_token_secret.
 *
 * Context / Why Added:
 *   Part of the CXF → ScribeJava migration. This explicit resource class
 *   replaces CXF’s built-in OAuth handlers with a more transparent,
 *   testable implementation.
 *
 * Notes:
 *   • Logs invalid attempts at WARN level but never logs secrets.
 *   • Request tokens are invalidated/consumed once exchanged by the provider.
 *   • Optional timestamp/nonce replay checks may be added in the provider.
 *   • Always responds with "application/x-www-form-urlencoded" as required
 *     by the OAuth 1.0a spec.
 */

package org.oscarehr.ws.oauth;

import oscar.login.OscarOAuthDataProvider;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.oscarehr.ws.oauth.util.*;
import oscar.login.AppOAuth1Config;

public class AccessTokenResource {

    private static final org.apache.logging.log4j.Logger logger =
            org.apache.logging.log4j.LogManager.getLogger(AccessTokenResource.class);

    private final OscarOAuthDataProvider provider;
    private final OAuth1ParamParser parser;
    private final OAuth1SignatureVerifier verifier;

    @Context
    private HttpServletRequest req;

    public AccessTokenResource(OscarOAuthDataProvider provider,
                               OAuth1ParamParser parser,
                               OAuth1SignatureVerifier verifier) {
        this.provider = provider;
        this.parser = parser;
        this.verifier = verifier;
    }

    @POST
    @Path("/token")
    @Produces("application/x-www-form-urlencoded")
    public Response exchange() {
        OAuth1Request oreq = parser.parseFromRequest(req);

        // 1) Load client + request token
        Client client = provider.getClient(oreq.consumerKey);
        if (client == null) {
            logger.warn("Access token request failed: invalid consumer key [{}]", oreq.consumerKey);
            return Response.status(401).entity("invalid_consumer").build();
        }

        RequestToken rt = provider.getRequestToken(oreq.token);
        if (rt == null) {
            logger.warn("Access token request failed: invalid request token [{}]", oreq.token);
            return Response.status(401).entity("invalid_request_token").build();
        }

        // 2) Verify oauth_verifier
        if (oreq.verifier == null || !oreq.verifier.equals(rt.getVerifier())) {
            logger.warn("Access token request failed: invalid verifier [{}] for token [{}]", oreq.verifier, oreq.token);
            return Response.status(401).entity("invalid_verifier").build();
        }

        // 3) Verify signature using existing verifier API
        try {
            String baseUrl = req.getScheme() + "://" + req.getServerName()
                    + (((req.getScheme().equals("http") && req.getServerPort() == 80)
                    || (req.getScheme().equals("https") && req.getServerPort() == 443))
                    ? "" : (":" + req.getServerPort()));

            AppOAuth1Config cfg = new AppOAuth1Config();
            cfg.setConsumerKey(client.getConsumerKey());
            cfg.setConsumerSecret(client.getSecret());

            // No applicationURI needed here
            String tokenFromSig = verifier.verifySignature(req, cfg);

            if (!oreq.token.equals(tokenFromSig)) {
                logger.warn("Access token request failed: token mismatch (req [{}] vs sig [{}])",
                        oreq.token, tokenFromSig);
                return Response.status(401).entity("invalid_signature").build();
            }            
        } catch (Exception e) {
            logger.warn("Access token request failed: signature verification error for token [{}]", oreq.token, e);
            return Response.status(401).entity("invalid_signature").build();
        }

        // (optional) timestamp/nonce checks if you have them in provider/parser
        // provider.checkTimestampAndNonce(oreq);

        // 4) Create access token (provider will handle marking request token as used)
        AccessToken at = provider.createAccessToken(rt);
        logger.debug("Access token [{}] issued for client [{}]",
                at.getTokenKey(), client.getConsumerKey());

        // 5) Respond form-encoded
        String body = "oauth_token=" + enc(at.getTokenKey())
                    + "&oauth_token_secret=" + enc(at.getTokenSecret());
        return Response.ok(body, "application/x-www-form-urlencoded").build();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
