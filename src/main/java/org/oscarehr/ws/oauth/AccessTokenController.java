package org.oscarehr.ws.oauth;

import oscar.login.OscarOAuthDataProvider;
import org.oscarehr.ws.oauth.AccessToken;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping // servlet mapping handles /ws/oauth/*
public class AccessTokenController {

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
        if (client == null) throw new OAuth1Exception(401, "invalid_consumer");

        RequestToken rt = provider.getRequestToken(oreq.token);
        if (rt == null) throw new OAuth1Exception(401, "invalid_request_token");

        // 2) Verify oauth_verifier
        if (oreq.verifier == null || !oreq.verifier.equals(rt.getVerifier())) {
            throw new OAuth1Exception(401, "invalid_verifier");
        }

        // 3) Verify HMAC-SHA1 signature using consumerSecret + requestTokenSecret
        verifier.verifySignature(oreq, client.getSecret(), rt.getTokenSecret());

        // 4) Create access token
        AccessToken at = provider.createAccessToken(rt);

        // 5) Respond form-encoded
        return "oauth_token=" + enc(at.getTokenKey())
             + "&oauth_token_secret=" + enc(at.getTokenSecret());
    }

    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}
