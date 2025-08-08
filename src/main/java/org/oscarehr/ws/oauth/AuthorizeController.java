package org.oscarehr.ws.oauth;

import oscar.login.OscarOAuthDataProvider;
import org.apache.cxf.rs.security.oauth.data.AuthorizationInput;
import org.apache.cxf.rs.security.oauth.data.RequestToken;
import org.apache.cxf.rs.security.oauth.data.UserSubject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping // servlet mapping handles /ws/oauth/*
public class AuthorizeController {

    private final OscarOAuthDataProvider provider;

    // If OAuthSessionMerger is a Spring bean, inject it; if it's static utils, call statically.
    private final org.oscarehr.ws.oauth.util.OAuthSessionMerger sessionMerger;

    public AuthorizeController(OscarOAuthDataProvider provider,
                               org.oscarehr.ws.oauth.util.OAuthSessionMerger sessionMerger) {
        this.provider = provider;
        this.sessionMerger = sessionMerger;
    }

    @GetMapping("/authorize")
    public String showConsent(@RequestParam("oauth_token") String token, Model model) {
        RequestToken rt = provider.getRequestToken(token);
        if (rt == null) throw new OAuth1Exception(400, "invalid_request_token");
        model.addAttribute("requestToken", rt);
        // Renders the existing consent/login JSP
        return "/login/3rdpartyLogin";
    }

    // The JSP "Approve" should POST oauth_token (and any UI inputs)
    @PostMapping("/authorize")
    public String approve(@RequestParam("oauth_token") String token,
                          HttpServletRequest req) {
        RequestToken rt = provider.getRequestToken(token);
        if (rt == null) throw new OAuth1Exception(400, "invalid_request_token");

        // 1) Preserve existing behavior: merge session -> sets providerNo onto ServiceRequestToken
        //    (This mirrors what LoginAction used to do.)
        sessionMerger.mergeSession(token, req.getSession());

        // 2) Tell provider to finalize authorization (creates/stores verifier)
        //    We also pass a subject so the provider could use it if it ever needs to.
        var subject = loginToSubject(lookupProviderNo(req));
        var verifier = provider.finalizeAuthorization(new AuthorizationInput(rt, subject, true));

        // 3) Redirect back to callback (or show OOB verifier if callback was "oob")
        String callback = rt.getCallback();
        if (callback == null || "oob".equalsIgnoreCase(callback)) {
            // show a simple page that displays the verifier (optional JSP you create), or reuse login page
            req.setAttribute("oauth_verifier", verifier);
            return "/login/authorized"; // make /login/authorized.jsp to display the verifier
        }
        String sep = callback.contains("?") ? "&" : "?";
        return "redirect:" + callback + sep + "oauth_token=" + token + "&oauth_verifier=" + verifier;
    }

    private String lookupProviderNo(HttpServletRequest req) {
        // pull it from the same session place LoginAction uses.
        // Example: LoggedInInfo or similar – adjust if your app uses a different key.
        Object li = req.getSession().getAttribute("loggedInInfo");
        if (li instanceof org.oscarehr.util.LoggedInInfo) {
            return ((org.oscarehr.util.LoggedInInfo) li).getLoggedInProviderNo();
        }
        return null; // sessionMerger will have already persisted it; this is only for subject cosmetics.
    }

    private static UserSubject loginToSubject(String providerNo) {
        return (providerNo == null) ? null : new UserSubject(providerNo);
    }
}
