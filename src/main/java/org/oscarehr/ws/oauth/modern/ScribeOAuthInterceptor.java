package org.oscarehr.ws.oauth.modern;

import com.github.scribejava.core.model.OAuth1AccessToken;

import org.oscarehr.ws.oauth.util.OAuth1SignatureVerifier;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.logging.log4j.Logger;
import org.oscarehr.common.model.AppDefinition;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.ws.oauth.util.OAuthRequestParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.oscarehr.common.dao.AppDefinitionDao;
import org.oscarehr.common.model.Provider;
import org.oscarehr.PMmodule.dao.ProviderDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oscar.login.OscarOAuthDataProvider;
import oscar.login.AppOAuth1Config;

/**
 * OAuth 1.0a interceptor for REST services using ScribeJava.
 * Replaces legacy CXF-based OAuth 1.0a request filter.
 */
@Component("modernOAuthInterceptor")
public class ScribeOAuthInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger logger = MiscUtils.getLogger();

    @Autowired private OAuthService oAuthService;
    @Autowired private AppDefinitionDao appDefinitionDao;
    @Autowired private OscarOAuthDataProvider oauthDataProvider;
    @Autowired private OAuth1SignatureVerifier signatureVerifier;
    @Autowired private ProviderDao providerDao;

    public ScribeOAuthInterceptor() {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        HttpServletRequest req = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        if (req == null || !OAuthRequestParser.isOAuth1Request(req)) {
            return;
        }

        try {
            // 1) Legacy CXF signature flow
            String consumerKey = OAuthRequestParser.getConsumerKey(req);
            AppDefinition appDef = appDefinitionDao.findByConsumerKey(consumerKey);
            AppOAuth1Config cfg = AppOAuth1Config.fromDocument(appDef.getConfig());
            String rawToken = signatureVerifier.verifySignature(req, cfg);

            // 2) Attach LoggedInInfo
            String providerNo = oauthDataProvider.getProviderNoByToken(rawToken);
            Provider provider = providerDao.getProvider(providerNo);
            LoggedInInfo info = new LoggedInInfo();
            info.setLoggedInProvider(provider);
            req.setAttribute(info.getLoggedInInfoKey(), info);
        } catch (Exception e) {
            throw new Fault(e);
        }

        // 3) Your ScribeJava session/token check
        if (!oAuthService.isOAuthConfigured()) {
            return;
        }
        HttpSession session = req.getSession(false);
        if (session == null) {
            throw new Fault(new Exception("No session found - authentication required"));
        }
        OAuth1AccessToken token = oAuthService.getAccessToken(session);
        if (token == null || !oAuthService.validateToken(token)) {
            throw new Fault(new Exception("Invalid or expired OAuth token"));
        }

        logger.debug("OAuth validation successful for request: " + req.getRequestURI());
    }
}
