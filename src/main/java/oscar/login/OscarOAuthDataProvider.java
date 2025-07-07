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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.oscarehr.common.dao.ServiceRequestTokenDaoImpl;

import org.apache.cxf.rs.security.oauth2.common.AccessToken;
import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.common.OAuthPermission;
import org.apache.cxf.rs.security.oauth2.common.ServerAccessToken;
import org.apache.cxf.rs.security.oauth2.common.UserSubject;
import org.apache.cxf.rs.security.oauth2.provider.OAuthDataProvider;
import org.apache.cxf.rs.security.oauth2.provider.OAuthServiceException;
import org.apache.cxf.rs.security.oauth2.tokens.bearer.BearerAccessToken;
import org.apache.logging.log4j.Logger;
import org.oscarehr.common.dao.ServiceAccessTokenDao;
import org.oscarehr.common.dao.ServiceClientDao;
import org.oscarehr.common.dao.ServiceRequestTokenDao;
import org.oscarehr.common.model.ServiceAccessToken;
import org.oscarehr.common.model.ServiceClient;
import org.oscarehr.common.model.ServiceRequestToken;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.oscarehr.util.SpringUtils;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// @EnableJpaRepositories("org.oscarehr.common.model")
@Component
@Transactional
public class OscarOAuthDataProvider implements OAuthDataProvider {

    Logger logger = MiscUtils.getLogger();

    // @Autowired
    // private ServiceClientDao serviceClientDao ;
    @Autowired
    private ServiceRequestTokenDao serviceRequestTokenDao;
    @Autowired
    private ServiceAccessTokenDao serviceAccessTokenDao;
    @Autowired
    private ServiceClientDao serviceClientDao;
    //private ServiceClientDao serviceClientDao = SpringUtils.getBean(ServiceClientDao.class);

    @Override
    public Client getClient(String clientId) {
        logger.debug("getClient() called");
        ServiceClient sc = serviceClientDao.findByKey(clientId);
        if (sc != null) {
            Client client = new Client(sc.getKey(), sc.getSecret(), true);
            client.setApplicationName(sc.getName());
            client.setApplicationURI(sc.getUri());
            return client;
        }
        return null;
    }

    @Override
    public ServerAccessToken createAccessToken(AccessToken token) throws OAuthServiceException {
        logger.debug("createAccessToken() called");
        String accessTokenString = UUID.randomUUID().toString();
        long issuedAt = System.currentTimeMillis() / 1000;
        BearerAccessToken bearerToken = new BearerAccessToken(token.getClient(), 3600L);
        bearerToken.setTokenKey(accessTokenString);
        bearerToken.setIssuedAt(issuedAt);
        bearerToken.setSubject(token.getSubject());
        bearerToken.setScopes(token.getScopes());
        
        ServiceAccessToken sat = new ServiceAccessToken();
        ServiceClient sc = serviceClientDao.findByKey(token.getClient().getClientId());
        sat.setClientId(sc.getId());
        sat.setDateCreated(new Date());
        sat.setIssued(issuedAt);
        sat.setLifetime(3600);
        sat.setTokenId(accessTokenString);
        sat.setTokenSecret(""); // OAuth2 doesn't use token secrets
        if (token.getSubject() != null) {
            sat.setProviderNo(token.getSubject().getId());
        }
        if (token.getScopes() != null && !token.getScopes().isEmpty()) {
            sat.setScopes(String.join(" ", token.getScopes()));
        }
        serviceAccessTokenDao.persist(sat);
        return bearerToken;
    }

    @Override
    public ServerAccessToken getAccessToken(String accessToken) throws OAuthServiceException {
        ServiceAccessToken sat = serviceAccessTokenDao.findByTokenId(accessToken);
        if (sat == null) {
            return null;
        }
        ServiceClient sc = serviceClientDao.find(sat.getClientId());
        Client c = getClient(sc.getKey());
        BearerAccessToken bearerToken = new BearerAccessToken(c, sat.getLifetime());
        bearerToken.setTokenKey(sat.getTokenId());
        bearerToken.setIssuedAt(sat.getIssued());
        
        if (sat.getProviderNo() != null) {
            UserSubject subject = new UserSubject(sat.getProviderNo());
            bearerToken.setSubject(subject);
        }
        
        if (sat.getScopes() != null && !sat.getScopes().trim().isEmpty()) {
            List<String> scopes = new ArrayList<>();
            String[] scopeArray = sat.getScopes().split(" ");
            for (String scope : scopeArray) {
                scopes.add(scope.trim());
            }
            bearerToken.setScopes(scopes);
        }
        
        return bearerToken;
    }

    @Override
    public ServerAccessToken getPreauthorizedToken(Client client, List<String> requestedScopes, 
                                                   UserSubject subject, String grantType) throws OAuthServiceException {
        String accessTokenString = UUID.randomUUID().toString();
        long issuedAt = System.currentTimeMillis() / 1000;
        BearerAccessToken bearerToken = new BearerAccessToken(client, 3600L);
        bearerToken.setTokenKey(accessTokenString);
        bearerToken.setIssuedAt(issuedAt);
        bearerToken.setSubject(subject);
        bearerToken.setScopes(requestedScopes);
        
        ServiceAccessToken sat = new ServiceAccessToken();
        ServiceClient sc = serviceClientDao.findByKey(client.getClientId());
        sat.setClientId(sc.getId());
        sat.setDateCreated(new Date());
        sat.setIssued(issuedAt);
        sat.setLifetime(3600);
        sat.setTokenId(accessTokenString);
        sat.setTokenSecret("");
        if (subject != null) {
            sat.setProviderNo(subject.getId());
        }
        if (requestedScopes != null && !requestedScopes.isEmpty()) {
            sat.setScopes(String.join(" ", requestedScopes));
        }
        serviceAccessTokenDao.persist(sat);
        return bearerToken;
    }

    @Override
    public void removeAccessToken(ServerAccessToken token) throws OAuthServiceException {
        ServiceAccessToken sat = serviceAccessTokenDao.findByTokenId(token.getTokenKey());
        if (sat != null) {
            serviceAccessTokenDao.remove(sat);
        }
    }

    @Override
    public List<OAuthPermission> convertScopeToPermissions(Client client, List<String> requestedScopes) {
        return Collections.emptyList();
    }

    @Override
    public void revokeToken(Client client, String token, String tokenTypeHint) throws OAuthServiceException {
        ServiceAccessToken sat = serviceAccessTokenDao.findByTokenId(token);
        if (sat != null) {
            serviceAccessTokenDao.remove(sat);
        }
    }

    @Override
    public List<ServerAccessToken> getAccessTokens(Client client, UserSubject subject) {
        return Collections.emptyList();
    }

    @Override
    public List<ServerAccessToken> getRefreshTokens(Client client, UserSubject subject) {
        return Collections.emptyList();
    }

    @Override
    public ServerAccessToken refreshAccessToken(Client client, String refreshToken, List<String> requestedScopes) throws OAuthServiceException {
        return null;
    }
}
