/**
 * OAuth 1.0a Authorize Endpoint (Step 2) — CXF JAX-RS only.
 * GET  /ws/oauth/authorize?oauth_token=...  -> consent page
 * POST /ws/oauth/authorize                  -> approve -> redirect with oauth_verifier
 * If callback == "oob", returns the verifier in the body.
 */
package org.oscarehr.ws.oauth;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import oscar.login.OscarOAuthDataProvider;
import oscar.login.OAuthData; // model used by 3rdpartyLogin.jsp

@Produces(MediaType.TEXT_HTML)
public class AuthorizeResource {

    @Context private HttpServletRequest  request;
    @Context private HttpServletResponse response;

    @Inject  private OscarOAuthDataProvider provider;

    private String getLoggedInProviderNo() {
        Object u = request.getSession().getAttribute("user");
        return u != null ? u.toString() : null;
    }

    /** GET /ws/oauth/authorize?oauth_token=... — show consent UI */
    @GET
    @Path("/authorize")
    public void showConsent(@QueryParam("oauth_token") String tokenId) throws Exception {
        System.out.println("showConsent() called with tokenId: " + tokenId);
        if (tokenId == null || tokenId.isEmpty()) {
            response.sendError(400, "Missing oauth_token");
            return;
        }

        // Use YOUR RequestToken type
        RequestToken rt = provider.getRequestToken(tokenId);
        System.out.println("RequestToken retrieved: " + rt);
        if (rt == null) {
            response.sendError(400, "Invalid oauth_token");
            return;
        }

        Client c = rt.getClient();
        OAuthData od = new OAuthData();
        od.setOauthToken(tokenId);
        od.setReplyTo(request.getContextPath() + "/ws/oauth/authorize");
        System.out.println("OAuthData prepared: " + od);
        if (c != null) {
            od.setApplicationName(c.getName());
            od.setApplicationURI(c.getUri());
        }
        List<String> scopes = (rt.getScopes() == null)
                ? java.util.Collections.emptyList()
                : rt.getScopes().stream().map(OAuth1Permission::getPermission).collect(Collectors.toList());
        od.setPermissions(scopes);

        request.setAttribute("oauthData", od);

        // Correct servlet forward
        RequestDispatcher rd = request.getRequestDispatcher("/login/3rdpartyLogin.jsp");
        rd.forward(request, response); // response committed by forward
    }

    /** POST /ws/oauth/authorize — approve & redirect (or OOB) */
    @POST
    @Path("/authorize")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response approve(@FormParam("oauth_token") String tokenId) {
        if (tokenId == null || tokenId.isEmpty()) {
            return Response.status(400).entity("missing oauth_token").type(MediaType.TEXT_PLAIN).build();
        }

        RequestToken rt = provider.getRequestToken(tokenId);
        if (rt == null) {
            return Response.status(400).entity("invalid_request_token").type(MediaType.TEXT_PLAIN).build();
        }

        String providerNo = getLoggedInProviderNo();
        if (providerNo == null || providerNo.isEmpty()) {
            return Response.status(401).entity("login_required").type(MediaType.TEXT_PLAIN).build();
        }

        // Let the provider set & persist the verifier + providerNo
        String verifier = provider.finalizeAuthorization(rt);

        String cb = rt.getCallback();
        if (cb != null && !"oob".equalsIgnoreCase(cb)) {
            String sep = cb.contains("?") ? "&" : "?";
            String loc = cb + sep + "oauth_token=" + enc(tokenId) + "&oauth_verifier=" + enc(verifier);
            return Response.seeOther(URI.create(loc)).build(); // 303 redirect
        }

        // OOB: show verifier
        return Response.ok("oauth_verifier=" + enc(verifier)).type(MediaType.TEXT_PLAIN).build();
    }

    private static String enc(String v) { return URLEncoder.encode(v, StandardCharsets.UTF_8); }
}
