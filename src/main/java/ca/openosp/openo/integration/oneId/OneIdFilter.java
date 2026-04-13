package ca.openosp.openo.integration.oneId;

import lombok.val;
import org.apache.log4j.Logger;
import org.oscarehr.common.dao.SystemPreferencesDao;
import org.oscarehr.integration.dhdr.OmdGateway;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SessionConstants;
import org.oscarehr.util.SpringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class OneIdFilter implements Filter {

    private static final Logger logger = MiscUtils.getLogger();
  private final SystemPreferencesDao systemPreferencesDao =
      SpringUtils.getBean(SystemPreferencesDao.class);
  private final OneIdSessionDao oneIdSessionDao = SpringUtils.getBean(OneIdSessionDao.class);
  private final OneIdViewletDao oneIdViewletDao = SpringUtils.getBean(OneIdViewletDao.class);

    public void init(FilterConfig config) throws ServletException {
        logger.info("Starting OneID Filter");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);
        String loggedInUser = null;

        if (httpRequest.getSession().getAttribute("user") != null) {
            loggedInUser = (String) httpRequest.getSession().getAttribute("user");
        }
        if (loggedInUser == null) {
            chain.doFilter(request, response);
            return;
        }
        OneIdSession oneIdSession = oneIdSessionDao.find(loggedInUser);
        if (oneIdSession == null) {
            session.removeAttribute(SessionConstants.OH_GATEWAY_DATA);
            chain.doFilter(request, response);
            return;
        }
        OneIdGatewayData gatewayData = (OneIdGatewayData) session.getAttribute(SessionConstants.OH_GATEWAY_DATA);
        if (gatewayData != null && ((gatewayData.getUao() != null
                && !gatewayData.getUao().equals(oneIdSession.getUaoUpi())) ||
                (!gatewayData.getLastKeptActive().equals(oneIdSession.getLastKeptActive())) ||
                (!gatewayData.getAccessTokenStr().equals(oneIdSession.getAccessToken())))) {
            gatewayData = null;
        }
        if (gatewayData == null) {
            // If the session exists and is valid, add the data to the OSCAR session

            // Get One ID session info here and fill OMDGatewayData
            val pcoiKey = getPcoiKey();
            OneIdGatewayData oneIdGatewayData = new OneIdGatewayData();
            oneIdGatewayData.setAccessTokenStr(oneIdSession.getAccessToken());
            oneIdGatewayData.processAccessToken(oneIdSession.getAccessToken());
            oneIdGatewayData.setRefreshTokenStr(oneIdSession.getRefreshToken());
            oneIdGatewayData.processRefreshToken(oneIdSession.getRefreshToken());
            oneIdGatewayData.setIdTokenStr(oneIdSession.getIdToken());
            oneIdGatewayData.processIdToken(oneIdSession.getIdToken());
            oneIdGatewayData.setAuthorizationId(oneIdSession.getAuthorizationId());
            oneIdGatewayData.setHubTopic(oneIdSession.getHubTopic());
            oneIdGatewayData.setUao(oneIdSession.getUaoUpi());
            oneIdGatewayData.setUaoFriendlyName(oneIdSession.getUaoName());
            oneIdGatewayData.setCmsUrl(oneIdSession.getUrlFromToolbar(OmdGateway.ToolbarKeys.CMS_URL.key));
            oneIdGatewayData.setPcoiUrl(oneIdSession.getUrlFromToolbar(pcoiKey));
            oneIdGatewayData.setFhirIss(oneIdSession.getUrlFromToolbar(OmdGateway.ToolbarKeys.FHIR_ISS.key));
            oneIdGatewayData.setLastKeptActive(oneIdSession.getLastKeptActive());
            oneIdGatewayData.setSso(oneIdSession.isSso());

            session.setAttribute(SessionConstants.OH_GATEWAY_DATA, oneIdGatewayData);
            LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(session);
            loggedInInfo.setOneIdGatewayData(oneIdGatewayData);
            LoggedInInfo.setLoggedInInfoIntoSession(session, loggedInInfo);
        }
        // If a session does not exist at all, we have not attempted to log into ONE ID through OSCAR Pro and
        // can assume the user is accessing the EMR locally with no intent of authenticating with ONE ID
        chain.doFilter(request, response);
    }

    private String getPcoiKey() {
        val oneIdViewlet = oneIdViewletDao.queryOneIdViewletForKey(
            systemPreferencesDao.getPreferenceValueByName("oneid.pcoi.key", "")
        );
        return oneIdViewlet == null ? "" : oneIdViewlet.getKeyValue();
    }
}
