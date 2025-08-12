/**
 * Purpose: Authorization endpoint (OAuth 1.0a step 2) where a user approves
 * a client and the server generates an oauth_verifier.
 * Responsibilities:
 *   • Display/handle user consent.
 *   • On approval, redirect to the registered callback with oauth_verifier.
 * Notes:
 *   • Supports "oob" flows by showing the verifier on-screen when no callback.
 *   • Do not leak client secrets or token secrets in views/logs.
 */

package org.oscarehr.ws.oauth;

import oscar.login.OscarOAuthDataProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping // servlet mapping handles /ws/oauth/*
public class AuthorizeController {

    private final OscarOAuthDataProvider provider;

    public AuthorizeController(OscarOAuthDataProvider provider) {
        this.provider = provider;
    }

    @GetMapping("/authorize")
    public String showConsent(@RequestParam("oauth_token") String token,
                            HttpServletRequest req, Model model) {
        RequestToken rt = provider.getRequestToken(token);
        if (rt == null) throw new OAuth1Exception(400, "invalid_request_token");

        oscar.login.OAuthData od = new oscar.login.OAuthData();
        od.setOauthToken(token);

        // Use your Client POJO accessors
        Client c = rt.getClient();
        od.setApplicationName(c != null ? c.getName() : null);
        od.setApplicationURI(c != null ? c.getUri() : null);

        // POST target for approval
        od.setReplyTo(req.getContextPath() + "/ws/oauth/authorize");

        // Permissions -> strings
        od.setPermissions(
            rt.getScopes() == null ? java.util.Collections.emptyList()
                : rt.getScopes().stream()
                    .map(org.oscarehr.ws.oauth.OAuth1Permission::getPermission)
                    .toList()
        );

        model.addAttribute("oauthData", od);
        return "forward:/login/3rdpartyLogin.jsp";

    }

    @PostMapping("/authorize")
    public String approve(@RequestParam("oauth_token") String token,
                        HttpServletRequest req) {
        RequestToken rt = provider.getRequestToken(token);
        if (rt == null) throw new OAuth1Exception(400, "invalid_request_token");

        // Keep your existing behavior: set providerNo on the request token
        oscar.login.OAuthSessionMerger.mergeSession(req);

        String verifier = provider.finalizeAuthorization(rt, /* providerNo is set by merger */ null);

        String callback = rt.getCallback();
        if (callback == null || "oob".equalsIgnoreCase(callback)) {
            req.setAttribute("oauth_verifier", verifier);
            // show a simple page with the verifier; create this JSP if you don’t have it
            return "forward:/login/authorized.jsp";
        }
        String sep = callback.contains("?") ? "&" : "?";
        return "redirect:" + callback + sep + "oauth_token=" + token + "&oauth_verifier=" + verifier;
    }
}
