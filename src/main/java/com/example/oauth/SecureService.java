package com.example.oauth;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.rs.security.oauth2.common.AccessToken;
import org.apache.cxf.rs.security.oauth2.common.OAuthContext;
import org.apache.cxf.rs.security.oauth2.filters.OAuthRequestFilter;
import org.apache.cxf.rs.security.oauth2.provider.AccessTokenValidator;
import org.apache.cxf.rs.security.oauth2.provider.OAuthDataProvider;
import org.apache.cxf.rs.security.oauth2.utils.OAuthContextUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/secure")
public class SecureService {

    @Context
    private MessageContext messageContext;

    private OAuthDataProvider dataProvider;
    private AccessTokenValidator tokenValidator;

    public SecureService() {
        // OAuth2 components will be injected via Spring or configured programmatically
    }

    public void setDataProvider(OAuthDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public void setTokenValidator(AccessTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProtectedResource() {
        try {
            // Extract OAuth context from the message context
            OAuthContext oauthContext = OAuthContextUtils.getContext(messageContext);
            
            if (oauthContext == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"No OAuth context found\"}")
                    .build();
            }

            // Get the access token
            AccessToken accessToken = oauthContext.getToken();
            
            if (accessToken == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"No access token found\"}")
                    .build();
            }

            // Validate token is not expired
            if (accessToken.getExpiresIn() > 0 && 
                System.currentTimeMillis() / 1000 > accessToken.getIssuedAt() + accessToken.getExpiresIn()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Access token expired\"}")
                    .build();
            }

            // Extract scopes or claims if needed
            String scopes = accessToken.getScopes() != null ? 
                String.join(",", accessToken.getScopes()) : "none";
            
            String clientId = accessToken.getClient() != null ? 
                accessToken.getClient().getClientId() : "unknown";

            return Response.ok()
                .entity(String.format(
                    "{ \"message\": \"This is protected data\", \"client_id\": \"%s\", \"scopes\": \"%s\" }",
                    clientId, scopes))
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Internal server error\"}")
                .build();
        }
    }
}
