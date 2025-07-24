package org.oscarehr.ws.oauth;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;

import oscar.login.OscarOAuthDataProvider;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * RESTful OAuth 1.0a endpoints implemented using ScribeJava.
 * This class exposes request token generation, token authorization,
 * and access token exchange endpoints.
 */
@Path("/oauth")
public class ScribeOAuthService {

    /**
     * Injected OAuth 1.0a data provider for handling token storage and validation.
     */
    @Inject
    private OscarOAuthDataProvider oAuthDataProvider;

    /**
     * Generates a request token for a given client key and optional callback URL.
     * @param clientKey the OAuth client key
     * @param callback the callback URL (can be "oob" for out-of-band)
     * @return JSON response containing token and secret
     */
    @GET
    @Path("/request_token")
    @Produces("application/json")
    public Response getRequestToken(@QueryParam("client_key") String clientKey,
                                    @QueryParam("callback") String callback) throws IOException, InterruptedException, ExecutionException {
        OAuth1RequestToken requestToken = oAuthDataProvider.createRequestToken(clientKey, callback, null);
        return Response.ok("{" +
                "\"token\": \"" + requestToken.getToken() + "\"," +
                "\"secret\": \"" + requestToken.getTokenSecret() + "\"}").build();
    }

    /**
     * Finalizes the authorization step by generating a verifier tied to a provider number.
     * @param token the request token previously issued
     * @param providerNo the OSCAR providerNo authorizing the request
     * @return response containing the generated verifier string
     */
    @GET
    @Path("/authorize")
    public Response getAuthorizationUrl(@QueryParam("token") String token,
                                        @QueryParam("providerNo") String providerNo) {
        String verifier = oAuthDataProvider.finalizeAuthorization(token, providerNo);
        // Typically you would redirect to a JSP page that uses the verifier
        return Response.ok("Verifier generated: " + verifier).build();
    }

    /**
     * Exchanges a request token and verifier for a permanent access token.
     * @param oauthToken the temporary request token
     * @param verifier the verifier code generated during authorization
     * @return JSON response containing access token and secret
     */
    @POST
    @Path("/access_token")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response getAccessToken(@FormParam("oauth_token") String oauthToken,
                                   @FormParam("oauth_verifier") String verifier) throws IOException, InterruptedException, ExecutionException {
        OAuth1AccessToken accessToken = oAuthDataProvider.createAccessToken(oauthToken, verifier);
        return Response.ok("{" +
                "\"token\": \"" + accessToken.getToken() + "\"," +
                "\"secret\": \"" + accessToken.getTokenSecret() + "\"}").build();
    }
}
