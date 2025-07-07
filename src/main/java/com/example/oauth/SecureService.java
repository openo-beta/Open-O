package com.example.oauth;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.rs.security.oauth2.common.AccessToken;
import org.apache.cxf.rs.security.oauth2.common.OAuthContext;
import org.apache.cxf.rs.security.oauth2.filters.OAuthRequestFilter;
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
    public SecureService() {
        // OAuth2 components will be injected via Spring or configured programmatically
    }

    public void setDataProvider(OAuthDataProvider dataProvider) {
        this.dataProvider = dataProvider;
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

            // Get the subject from OAuth context
            String subjectId = oauthContext.getSubject() != null ? 
                oauthContext.getSubject().getId() : "unknown";
            
            // For now, return basic info since AccessToken interface is different in OAuth2
            String scopes = "read"; // Default scope
            String clientId = "unknown";

            return Response.ok()
                .entity(String.format(
                    "{ \"message\": \"This is protected data\", \"subject_id\": \"%s\", \"scopes\": \"%s\" }",
                    subjectId, scopes))
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Internal server error\"}")
                .build();
        }
    }
}
