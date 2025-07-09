package org.oscarehr.oauth;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.oscarehr.util.MiscUtils;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;

/**
 * JAX-RS filter for OAuth 1.0a authentication
 * Uses javax.ws.rs (pre-Jakarta) for compatibility with CXF < 3.5
 */
@Provider
public class OAuth1Filter implements ContainerRequestFilter {
    
    private static final Logger logger = MiscUtils.getLogger();
    
    private final OAuth10aService oauthService;
    private final OAuth1AccessToken accessToken;
    
    public OAuth1Filter(OAuth10aService oauthService, OAuth1AccessToken accessToken) {
        this.oauthService = oauthService;
        this.accessToken = accessToken;
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            // Get request details
            String method = requestContext.getMethod();
            URI uri = requestContext.getUriInfo().getRequestUri();
            
            // Create OAuth request for signing
            Verb verb = Verb.valueOf(method.toUpperCase());
            OAuthRequest oauthRequest = new OAuthRequest(verb, uri.toString());
            
            // Sign the request
            oauthService.signRequest(accessToken, oauthRequest);
            
            // Add OAuth headers to the JAX-RS request
            String authHeader = oauthRequest.getHeaders().get("Authorization");
            if (authHeader != null) {
                requestContext.getHeaders().add("Authorization", authHeader);
                logger.debug("Added OAuth 1.0a Authorization header");
            }
            
        } catch (Exception e) {
            logger.error("Error applying OAuth 1.0a signature", e);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("OAuth 1.0a authentication failed")
                    .build()
            );
        }
    }
}
