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
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Logger;
import org.oscarehr.common.dao.ServiceAccessTokenDao;
import org.oscarehr.common.dao.ServiceClientDao;
import org.oscarehr.common.dao.ServiceRequestTokenDao;
import org.oscarehr.common.model.ServiceAccessToken;
import org.oscarehr.common.model.ServiceClient;
import org.oscarehr.common.model.ServiceRequestToken;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;

/**
 * Provides OAuth 1.0a operations (request/access token handling, authorization)
 * using ScribeJava and persistence via DAO layer.
 */
@Component
@Transactional
public class OscarOAuthDataProvider {

    private static final Logger logger = MiscUtils.getLogger();

    @Autowired private ServiceRequestTokenDao serviceRequestTokenDao;
    @Autowired private ServiceAccessTokenDao serviceAccessTokenDao;
    @Autowired private ServiceClientDao serviceClientDao;

    @Value("${oauth.token.lifetime:3600}")
    private long requestTokenLifetime;

    /**
     * Helper API definition for dynamic OAuth endpoint binding.
     */
    private static class GenericOAuth10aApi extends DefaultApi10a {
        private final String requestTokenUrl;
        private final String accessTokenUrl;
        private final String authorizationUrl;

        public GenericOAuth10aApi(String baseUrl) {
            this.requestTokenUrl = baseUrl + "/oauth/request_token";
            this.accessTokenUrl = baseUrl + "/oauth/access_token";
            this.authorizationUrl = baseUrl + "/oauth/authorize";
        }

        @Override public String getRequestTokenEndpoint() { return requestTokenUrl; }
        @Override public String getAccessTokenEndpoint() { return accessTokenUrl; }
        @Override public String getAuthorizationBaseUrl() { return authorizationUrl; }
    }

    public ServiceClient getClient(String clientId) {
        logger.debug("getClient() called");
        return serviceClientDao.findByKey(clientId);
    }

    public OAuth1RequestToken createRequestToken(String clientKey, String callback, List<String> scopes) {
        logger.debug("createRequestToken() called");
        ServiceClient client = serviceClientDao.findByKey(clientKey);
        if (client == null) throw new RuntimeException("Client not found: " + clientKey);

        try {
            OAuth10aService service = new ServiceBuilder(client.getKey())
                .apiSecret(client.getSecret())
                .callback(callback != null ? callback : "oob")
                .build(new GenericOAuth10aApi(client.getUri()));

            OAuth1RequestToken token = service.getRequestToken();

            ServiceRequestToken srt = new ServiceRequestToken();
            srt.setCallback(callback);
            srt.setClientId(client.getId());
            srt.setDateCreated(new Date());
            srt.setTokenId(token.getToken());
            srt.setTokenSecret(token.getTokenSecret());
            srt.setScopes(scopes != null ? String.join(" ", scopes) : "");
            serviceRequestTokenDao.persist(srt);

            return token;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error creating request token", e);
            throw new RuntimeException("Failed to create request token", e);
        }
    }

    public OAuth1RequestToken getRequestToken(String tokenId) {
        ServiceRequestToken srt = serviceRequestTokenDao.findByTokenId(tokenId);
        if (srt == null) return null;

        long age = System.currentTimeMillis() - srt.getDateCreated().getTime();
        if (age > (requestTokenLifetime * 1000L)) {
            serviceRequestTokenDao.remove(srt);
            return null;
        }
        return new OAuth1RequestToken(srt.getTokenId(), srt.getTokenSecret());
    }

    public String finalizeAuthorization(String requestTokenId, String providerNo) {
        logger.debug("finalizeAuthorization() called");
        String verifier = UUID.randomUUID().toString();
        ServiceRequestToken srt = serviceRequestTokenDao.findByTokenId(requestTokenId);
        if (srt != null) {
            srt.setVerifier(verifier);
            srt.setProviderNo(providerNo);
            serviceRequestTokenDao.merge(srt);
        }
        return verifier;
    }

    public OAuth1AccessToken createAccessToken(String tokenId, String verifier) {
        logger.debug("createAccessToken() called");
        ServiceRequestToken srt = serviceRequestTokenDao.findByTokenId(tokenId);
        if (srt == null) throw new RuntimeException("Invalid request token.");
        if (!verifier.equals(srt.getVerifier())) throw new RuntimeException("Invalid verifier.");

        try {
            ServiceClient client = serviceClientDao.find(srt.getClientId());
            OAuth10aService service = new ServiceBuilder(client.getKey())
                .apiSecret(client.getSecret())
                .callback(srt.getCallback())
                .build(new GenericOAuth10aApi(client.getUri()));

            OAuth1AccessToken token = service.getAccessToken(
                new OAuth1RequestToken(srt.getTokenId(), srt.getTokenSecret()), verifier);

            ServiceAccessToken sat = new ServiceAccessToken();
            sat.setClientId(client.getId());
            sat.setDateCreated(new Date());
            sat.setIssued(System.currentTimeMillis() / 1000);
            sat.setLifetime(3600);
            sat.setTokenId(token.getToken());
            sat.setTokenSecret(token.getTokenSecret());
            sat.setProviderNo(srt.getProviderNo());
            sat.setScopes(srt.getScopes());
            serviceAccessTokenDao.persist(sat);

            serviceRequestTokenDao.remove(srt);
            return token;
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Error creating access token", e);
            throw new RuntimeException("Failed to create access token", e);
        }
    }

    public OAuth1AccessToken getAccessToken(String tokenId) {
        ServiceAccessToken sat = serviceAccessTokenDao.findByTokenId(tokenId);
        if (sat == null) throw new RuntimeException("Invalid access token.");
        return new OAuth1AccessToken(sat.getTokenId(), sat.getTokenSecret());
    }

    public void removeToken(String tokenId) {
        ServiceRequestToken srt = serviceRequestTokenDao.findByTokenId(tokenId);
        if (srt != null) serviceRequestTokenDao.remove(srt);
        ServiceAccessToken sat = serviceAccessTokenDao.findByTokenId(tokenId);
        if (sat != null) serviceAccessTokenDao.remove(sat);
    }

    public String getTokenSecret(String tokenId) {
        ServiceAccessToken sat = serviceAccessTokenDao.findByTokenId(tokenId);
        if (sat != null) return sat.getTokenSecret();
        ServiceRequestToken srt = serviceRequestTokenDao.findByTokenId(tokenId);
        return srt != null ? srt.getTokenSecret() : null;
    }

    public String getProviderNoByToken(String tokenId) {
        ServiceAccessToken sat = serviceAccessTokenDao.findByTokenId(tokenId);
        if (sat != null) return sat.getProviderNo();
        ServiceRequestToken srt = serviceRequestTokenDao.findByTokenId(tokenId);
        return srt != null ? srt.getProviderNo() : null;
    }

    public OAuth10aService buildService(String clientKey, String callbackUrl) {
        ServiceClient client = getClient(clientKey);
        ServiceBuilder builder = new ServiceBuilder(client.getKey()).apiSecret(client.getSecret());
        if (callbackUrl != null) builder.callback(callbackUrl);
        return builder.build(new GenericOAuth10aApi(client.getUri()));
    }

    public OAuth1AccessToken getStoredAccessToken(String tokenId) {
        return getAccessToken(tokenId);
    }
}
