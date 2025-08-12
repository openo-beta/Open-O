package org.oscarehr.ws.oauth;

import oscar.login.OscarOAuthDataProvider;
import oscar.login.AppOAuth1Config;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.oscarehr.ws.oauth.util.OAuth1ParamParser;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping // servlet mapping handles /ws/oauth/*
public class AccessTokenController {

    private static final org.apache.logging.log4j.Logger logger =
            org.apache.logging.log4j.LogManager.getLogger(AccessTokenController.class);

    private final OscarOAuthDataProvider provider;
    private final OAuth1ParamParser parser;
    private final OAuth1SignatureVerifier verifier;

    public AccessTokenController(OscarOAuthDataProvider provider,
                                 OAuth1ParamParser parser,
                                 OAuth1SignatureVerifier verifier) {
        this.provider = provider;
        this.parser = parser;
        this.verifier = verifier;
    }

    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String exchange(HttpServletRequest req) {
        OAuth1Request oreq = parser.parseFromRequest(req);

        // 1) Load client + request token
        Client client = provider.getClient(oreq.consumerKey);
        if (client == null) {
            logger.warn("Access token request failed: invalid consumer key [{}]", oreq.consumerKey);
            throw new OAuth1Exception(401, "invalid_consumer");
        }

        RequestToken rt = provider.getRequestToken(oreq.token);
        if (rt == null) {
            logger.warn("Access token request failed: invalid request token [{}]", oreq.token);
            throw new OAuth1Exception(401, "invalid_request_token");
        }

        // 2) Verify oauth_verifier
        if (oreq.verifier == null || !oreq.verifier.equals(rt.getVerifier())) {
            logger.warn("Access token request failed: invalid verifier [{}] for token [{}]", oreq.verifier, oreq.token);
            throw new OAuth1Exception(401, "invalid_verifier");
        }

        // 3) Verify HMAC-SHA1 signature using the verifier’s (req, cfg) API
        try {
            // baseUrl here is not used for signing endpoints; any host base is OK
            String baseUrl = req.getScheme() + "://" + req.getServerName()
                    + (((req.getScheme().equals("http") && req.getServerPort()==80)
                       || (req.getScheme().equals("https") && req.getServerPort()==443))
                       ? "" : (":" + req.getServerPort()));
            AppOAuth1Config cfg = new AppOAuth1Config();
            cfg.setConsumerKey(client.getConsumerKey());
            cfg.setConsumerSecret(client.getSecret());
            cfg.setApplicationURI(baseUrl);

            String tokenFromSig = verifier.verifySignature(req, cfg);
            // optional sanity check: the signed token should match the request token we loaded
            if (!oreq.token.equals(tokenFromSig)) {
                logger.warn("Access token request failed: token mismatch (req [{}] vs sig [{}])",
                        oreq.token, tokenFromSig);
                throw new OAuth1Exception(401, "invalid_signature");
            }
        } catch (Exception e) {
            logger.warn("Access token request failed: signature verification error for token [{}]", oreq.token, e);
            throw new OAuth1Exception(401, "invalid_signature");
        }

        // 4) Create access token
        AccessToken at = provider.createAccessToken(rt);
        logger.debug("Access token [{}] issued for client [{}]", at.getTokenKey(), client.getConsumerKey());

        // 5) Respond form-encoded
        return "oauth_token=" + enc(at.getTokenKey())
             + "&oauth_token_secret=" + enc(at.getTokenSecret());
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
