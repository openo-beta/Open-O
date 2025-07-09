//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package oscar.login;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.oscarehr.common.model.ServiceClient;
import org.oscarehr.util.MiscUtils;

import com.github.scribejava.core.model.OAuth1RequestToken;

public class OscarRequestTokenHandler {

    private static final Logger logger = MiscUtils.getLogger();
    private static final String[] REQUIRED_PARAMETERS = {
        "oauth_consumer_key",
        "oauth_signature_method", 
        "oauth_signature",
        "oauth_timestamp",
        "oauth_nonce",
        "oauth_callback"
    };

    private long tokenLifetime = 3600L;
    private String defaultScope;
    private OscarOAuthDataProvider dataProvider;
    
    public OscarRequestTokenHandler(OscarOAuthDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public Response handle(MessageContext mc) {
        try {
            HttpServletRequest request = mc.getHttpServletRequest();
            
            // Validate required parameters
            for (String param : REQUIRED_PARAMETERS) {
                if (request.getParameter(param) == null) {
                    return Response.status(HttpServletResponse.SC_BAD_REQUEST)
                            .entity("Missing required parameter: " + param)
                            .build();
                }
            }

            String consumerKey = request.getParameter("oauth_consumer_key");
            String callback = request.getParameter("oauth_callback");
            
            // Get client
            ServiceClient client = dataProvider.getClient(consumerKey);
            if (client == null) {
                return Response.status(HttpServletResponse.SC_UNAUTHORIZED)
                        .entity("Unknown consumer key")
                        .build();
            }

            // Validate callback URL
            if (!validateCallbackURL(client, callback)) {
                return Response.status(HttpServletResponse.SC_BAD_REQUEST)
                        .entity("Invalid callback URL")
                        .build();
            }

            // Parse scopes
            String scopeParam = request.getParameter("scope");
            List<String> scopes = scopeParam != null ? 
                Arrays.asList(scopeParam.split(" ")) : 
                (defaultScope != null ? Arrays.asList(defaultScope) : null);

            // Create request token
            OAuth1RequestToken requestToken = dataProvider.createRequestToken(consumerKey, callback, scopes);

            logger.debug("Created request token: " + requestToken.getToken());

            // Create response
            Map<String, Object> responseParams = new HashMap<>();
            responseParams.put("oauth_token", requestToken.getToken());
            responseParams.put("oauth_token_secret", requestToken.getTokenSecret());
            responseParams.put("oauth_callback_confirmed", "true");

            StringBuilder responseBody = new StringBuilder();
            for (Map.Entry<String, Object> entry : responseParams.entrySet()) {
                if (responseBody.length() > 0) {
                    responseBody.append("&");
                }
                responseBody.append(entry.getKey()).append("=").append(entry.getValue());
            }

            return Response.ok(responseBody.toString())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

        } catch (Exception e) {
            logger.error("Error handling request token request", e);
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .entity("Internal server error")
                    .build();
        }
    }

    protected boolean validateCallbackURL(ServiceClient client, String oauthCallback) {
        // Allow "oob" for out-of-band callback
        if ("oob".equals(oauthCallback)) {
            return true;
        }
        
        // The callback must not be empty or null, and it should either match
        // the pre-registered callback URI or have the common root with the
        // the pre-registered application URI
        if (oauthCallback != null && !oauthCallback.isEmpty()) {
            String clientCallback = client.getUri(); // Assuming this is the callback URI
            if (clientCallback != null && (oauthCallback.equals(clientCallback) || 
                oauthCallback.startsWith(clientCallback))) {
                return true;
            }
        }
        
        return false;
    }

    public void setTokenLifetime(long tokenLifetime) {
        this.tokenLifetime = tokenLifetime;
    }

    public void setDefaultScope(String defaultScope) {
        this.defaultScope = defaultScope;
    }

}
