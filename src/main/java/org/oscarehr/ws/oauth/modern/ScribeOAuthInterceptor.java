package org.oscarehr.ws.oauth.modern;

import com.github.scribejava.core.model.OAuth1AccessToken;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.logging.log4j.Logger;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * OAuth 1.0a interceptor for REST services using ScribeJava.
 * Replaces legacy CXF-based OAuth 1.0a request filter.
 */
@Component("ModernOAuthInterceptor")
public class ScribeOAuthInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger logger = MiscUtils.getLogger();

    @Autowired
    private OAuthService oAuthService;

    public ScribeOAuthInterceptor() {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        // Skip OAuth validation if service is not configured
        if (!oAuthService.isOAuthConfigured()) {
            return;
        }

        HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        if (request == null) {
            return;
        }

        // Skip OAuth for certain endpoints
        String requestURI = request.getRequestURI();
        if (isExemptEndpoint(requestURI)) {
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new Fault(new Exception("No session found - authentication required"));
        }

        OAuth1AccessToken token = oAuthService.getAccessToken(session);
        if (token == null || !oAuthService.validateToken(token)) {
            throw new Fault(new Exception("Invalid or expired OAuth token"));
        }

        logger.debug("OAuth validation successful for request: " + requestURI);
    }

    private boolean isExemptEndpoint(String requestURI) {
        // Exempt OAuth endpoints and status endpoints from OAuth validation
        return requestURI.contains("/oauth/") || 
               requestURI.contains("/status") ||
               requestURI.contains("/csrf");
    }
}
