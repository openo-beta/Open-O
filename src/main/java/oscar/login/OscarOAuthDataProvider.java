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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import org.oscarehr.common.dao.ServiceAccessTokenDao;
import org.oscarehr.common.dao.ServiceClientDao;
import org.oscarehr.common.dao.ServiceRequestTokenDao;
import org.oscarehr.common.model.ServiceAccessToken;
import org.oscarehr.common.model.ServiceClient;
import org.oscarehr.common.model.ServiceRequestToken;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.ws.oauth.OAuth1Client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;

@Component
@Transactional
public class OscarOAuthDataProvider {

    Logger logger = MiscUtils.getLogger();

    @Autowired
    private ServiceRequestTokenDao serviceRequestTokenDao;
    @Autowired
    private ServiceAccessTokenDao serviceAccessTokenDao;
    @Autowired
    private ServiceClientDao serviceClientDao;

    private OAuth1Client oauthClient;

    /**
     * Initialize the OAuth1Client with API credentials.
     */
    public void initializeOAuthClient(String apiKey, String apiSecret, String callbackUrl) {
        this.oauthClient = new OAuth1Client(apiKey, apiSecret, callbackUrl);
    }

    /**
     * Get client information by client ID.
     */
    public ServiceClient getClient(String clientId) {
        logger.debug("getClient() called");
        return serviceClientDao.findByKey(clientId);
    }

    /**
     * Create a new OAuth 1.0a request token.
     */
    public OAuth1RequestToken createRequestToken(String clientId, String callbackUrl, List<String> scopes) {
        logger.debug("createRequestToken() called");

        // Generate token and secret
        String token = UUID.randomUUID().toString();
        String secret = UUID.randomUUID().toString();

        // Create a new OAuth1RequestToken
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, secret);

        // Save the request token to the database
        ServiceRequestToken srt = new ServiceRequestToken();
        srt.setTokenId(token);
        srt.setTokenSecret(secret);
        srt.setCallback(callbackUrl);
        srt.setClientId(serviceClientDao.findByKey(clientId).getId());
        srt.setDateCreated(new Date());
        srt.setScopes(String.join(" ", scopes)); // Store scopes as a space-separated string
        serviceRequestTokenDao.persist(srt);

        return requestToken;
    }

    /**
     * Retrieve a request token by its token ID.
     */
    public OAuth1RequestToken getRequestToken(String tokenId) {
        logger.debug("getRequestToken() called");

        ServiceRequestToken serviceToken = serviceRequestTokenDao.findByTokenId(tokenId);
        if (serviceToken != null) {
            // Check if the token is expired (1 hour validity)
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, -1);
            Date oneHourAgo = cal.getTime();
            if (serviceToken.getDateCreated().before(oneHourAgo)) {
                serviceRequestTokenDao.remove(serviceToken);
                return null;
            }

            // Return the request token
            return new OAuth1RequestToken(serviceToken.getTokenId(), serviceToken.getTokenSecret());
        }
        return null;
    }

    /**
     * Finalize the authorization process by generating a verifier.
     */
    public String finalizeAuthorization(String tokenId) {
        logger.debug("finalizeAuthorization() called");

        // Generate a verifier
        String verifier = UUID.randomUUID().toString();

        // Update the request token with the verifier
        ServiceRequestToken srt = serviceRequestTokenDao.findByTokenId(tokenId);
        if (srt != null) {
            srt.setVerifier(verifier);
            serviceRequestTokenDao.merge(srt);
        }

        return verifier;
    }

    /**
     * Create a new OAuth 1.0a access token.
     */
    public OAuth1AccessToken createAccessToken(String tokenId, String verifier) {
        logger.debug("createAccessToken() called");

        // Retrieve the request token
        ServiceRequestToken srt = serviceRequestTokenDao.findByTokenId(tokenId);
        if (srt == null) {
            throw new RuntimeException("Invalid request token.");
        }

        // Verify the verifier
        if (!verifier.equals(srt.getVerifier())) {
            throw new RuntimeException("Invalid verifier.");
        }

        // Use OAuth1Client to exchange the request token for an access token
        OAuth1RequestToken requestToken = new OAuth1RequestToken(srt.getTokenId(), srt.getTokenSecret());
        OAuth1AccessToken accessToken;
        try {
            accessToken = oauthClient.getAccessToken(requestToken, verifier);
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain access token.", e);
        }

        // Save the access token to the database
        ServiceAccessToken sat = new ServiceAccessToken();
        sat.setTokenId(accessToken.getToken());
        sat.setTokenSecret(accessToken.getTokenSecret());
        sat.setClientId(srt.getClientId());
        sat.setProviderNo(srt.getProviderNo());
        sat.setDateCreated(new Date());
        sat.setScopes(srt.getScopes());
        serviceAccessTokenDao.persist(sat);

        // Remove the request token
        serviceRequestTokenDao.remove(srt);

        return accessToken;
    }

    /**
     * Retrieve an access token by its token ID.
     */
    public OAuth1AccessToken getAccessToken(String tokenId) {
        ServiceAccessToken sat = serviceAccessTokenDao.findByTokenId(tokenId);
        if (sat == null) {
            throw new RuntimeException("Invalid access token.");
        }
        return new OAuth1AccessToken(sat.getTokenId(), sat.getTokenSecret());
    }

    /**
     * Remove a token (either request or access token).
     */
    public void removeToken(String tokenId) {
        ServiceRequestToken srt = serviceRequestTokenDao.findByTokenId(tokenId);
        if (srt != null) {
            serviceRequestTokenDao.remove(srt);
        }

        ServiceAccessToken sat = serviceAccessTokenDao.findByTokenId(tokenId);
        if (sat != null) {
            serviceAccessTokenDao.remove(sat);
        }
    }

    /**
     * Send a signed request using the access token.
     */
    public String sendSignedRequest(OAuth1AccessToken accessToken, String url) {
        try {
            return oauthClient.sendSignedRequest(accessToken, url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send signed request.", e);
        }
    }
}
