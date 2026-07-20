package ca.openosp.openo.integration.oneId;

import ca.openosp.openo.commn.dao.SystemPreferencesDao;
import ca.openosp.openo.commn.model.SystemPreferences;
import ca.openosp.openo.integration.dhdr.OmdGateway;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SessionConstants;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.logging.log4j.Logger;

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
        String loggedInUser =
            (session != null) ? (String) session.getAttribute("user") : null;

        if (loggedInUser != null) {
            try {
                rehydrateOneIdSession(session, loggedInUser);
            } catch (Exception e) {
                // A failure restoring the ONE ID session must never break the request chain;
                // local (non-ONE ID) login has to keep working regardless.
                logger.warn("OneIdFilter: skipping ONE ID session rehydration - " + e.getMessage());
            }
        }
        // If no ONE ID session exists, the user is accessing the EMR locally with no intent of
        // authenticating with ONE ID; the request proceeds unchanged.
        chain.doFilter(request, response);
    }

    /**
     * Restores the ONE ID gateway data onto the HTTP session for a logged-in provider that has a
     * persisted {@link OneIdSession}. No-op when the provider has no ONE ID session.
     *
     * @param session      HttpSession the current session (non-null for a logged-in provider)
     * @param loggedInUser String the logged-in provider number
     */
    private void rehydrateOneIdSession(HttpSession session, String loggedInUser) {
        OneIdSession oneIdSession = oneIdSessionDao.find(loggedInUser);
        if (oneIdSession == null) {
            session.removeAttribute(LoggedInInfo.OH_GATEWAY_DATA);
            return;
        }
        OneIdGatewayData gatewayData = (OneIdGatewayData) session.getAttribute(LoggedInInfo.OH_GATEWAY_DATA);
        if (gatewayData != null && ((gatewayData.getUao() != null
                && !gatewayData.getUao().equals(oneIdSession.getUaoUpi())) ||
                (!gatewayData.getLastKeptActive().equals(oneIdSession.getLastKeptActive())) ||
                (!gatewayData.getAccessTokenStr().equals(oneIdSession.getAccessToken())))) {
            gatewayData = null;
        }
        if (gatewayData == null) {
            // If the session exists and is valid, add the data to the OSCAR session

            // Get One ID session info here and fill OMDGatewayData
            String pcoiKey = getPcoiKey();
            OneIdGatewayData oneIdGatewayData = new OneIdGatewayData();
            oneIdGatewayData.setAccessTokenStr(oneIdSession.getAccessToken());
            oneIdGatewayData.processAccessToken(oneIdSession.getAccessToken());
            oneIdGatewayData.setRefreshTokenStr(oneIdSession.getRefreshToken());
            oneIdGatewayData.processRefreshToken(oneIdSession.getRefreshToken());
            oneIdGatewayData.setIdTokenStr(oneIdSession.getIdToken());
            oneIdGatewayData.processIdToken(oneIdSession.getIdToken());
            oneIdGatewayData.setAuthorizationId(oneIdSession.getAuthorizationId());
            oneIdGatewayData.setHubTopic(oneIdSession.getHubTopic());
            oneIdGatewayData.setCtxSessionId(oneIdSession.getHubTopic());
            oneIdGatewayData.setUao(oneIdSession.getUaoUpi());
            oneIdGatewayData.setUaoFriendlyName(oneIdSession.getUaoName());
            oneIdGatewayData.setCmsUrl(resolveCmsEndpoint(oneIdSession));
            oneIdGatewayData.setPcoiUrl(oneIdSession.getUrlFromToolbar(pcoiKey));
            oneIdGatewayData.setFhirIss(oneIdSession.getUrlFromToolbar(OmdGateway.ToolbarKeys.FHIR_ISS.key));
            oneIdGatewayData.setLastKeptActive(oneIdSession.getLastKeptActive());
            oneIdGatewayData.setSso(oneIdSession.isSso());

            session.setAttribute(LoggedInInfo.OH_GATEWAY_DATA, oneIdGatewayData);
            LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(session);
            loggedInInfo.setOneIdGatewayData(oneIdGatewayData);
            LoggedInInfo.setLoggedInInfoIntoSession(session, loggedInInfo);
        }
    }

    /**
     * Resolves the CMS server endpoint from the ONE ID toolbar, preferring the "hub.url" key and
     * falling back to "cms_url". When neither key is present the endpoint is empty and the toolbar
     * keys that are present are logged.
     *
     * @param oneIdSession OneIdSession the persisted ONE ID session carrying the toolbar
     * @return String the CMS endpoint URL, or an empty string when the toolbar carries neither key
     */
    private String resolveCmsEndpoint(OneIdSession oneIdSession) {
        String endpoint = oneIdSession.getUrlFromToolbar(OmdGateway.ToolbarKeys.HUB_URL.key);
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = oneIdSession.getUrlFromToolbar(OmdGateway.ToolbarKeys.CMS_URL.key);
        }
        if (endpoint == null || endpoint.isEmpty()) {
            logger.warn("ONE ID toolbar carried no CMS endpoint under '"
                + OmdGateway.ToolbarKeys.HUB_URL.key + "' or '" + OmdGateway.ToolbarKeys.CMS_URL.key
                + "'; toolbar keys present: " + oneIdSession.getToolbarKeys());
        }
        return endpoint;
    }

    private String getPcoiKey() {
        OneIdViewlet oneIdViewlet = oneIdViewletDao.queryOneIdViewletForKey(
            systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.pcoi_key).getName()
        );
        return oneIdViewlet == null ? "" : oneIdViewlet.getKeyValue();
    }
}
