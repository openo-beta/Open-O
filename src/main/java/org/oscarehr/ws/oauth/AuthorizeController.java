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
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

// JAX-RS + Servlet
import javax.ws.rs.core.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    @POST
    @Path("/authorize")
    @Produces(MediaType.TEXT_PLAIN)
    public Response authorize(@Context HttpServletRequest req,
                            @QueryParam("oauth_token") String token,
                            @QueryParam("oauth_callback") String callback) {
        if (token == null || token.isEmpty()) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity("missing oauth_token").build();
        }

        // ensure the request token exists
        RequestToken rt = provider.getRequestToken(token);
        if (rt == null) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity("invalid_request_token").build();
        }

        // TODO: fetch the logged-in user/providerNo from your session
        String providerNo = "system";

        // generate & persist verifier (implement this in your provider if needed)
        String verifier = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        // e.g.: dataProvider.approveRequestToken(token, providerNo, verifier);

        // prefer the stored callback on the token; fall back to query param
        String cb = rt.getCallback();
        if (cb == null || cb.isEmpty()) cb = callback;

        if (cb != null && !"oob".equalsIgnoreCase(cb)) {
            String sep = cb.contains("?") ? "&" : "?";
            String loc = cb + sep + "oauth_token=" + enc(token) + "&oauth_verifier=" + enc(verifier);
            return Response.seeOther(java.net.URI.create(loc)).build(); // 303
        }

        // out-of-band: show the verifier
        return Response.ok("oauth_verifier=" + verifier).build();
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
