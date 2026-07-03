package ca.openosp.openo.integration.dhdr;
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */

import ca.openosp.OscarProperties;
import ca.openosp.openo.commn.dao.OMDGatewayTransactionLogDao;
import ca.openosp.openo.commn.dao.SystemPreferencesDao;
import ca.openosp.openo.commn.model.OMDGatewayTransactionLog;
import ca.openosp.openo.commn.model.SystemPreferences;

import ca.openosp.openo.integration.fhircast.Event;
import ca.openosp.openo.integration.ohcms.CMSManager;
import ca.openosp.openo.integration.oneId.OneIDTokenUtils;
import ca.openosp.openo.integration.oneId.OneIdGatewayData;
import ca.openosp.openo.integration.oneId.OneIdSession;
import ca.openosp.openo.integration.oneId.OneIdSessionDao;
import ca.openosp.openo.integration.oneId.TokenExpiredException;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PKCEUtils;
import ca.openosp.openo.utility.PathUtils;
import ca.openosp.openo.utility.SpringUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

public class OmdGateway {

	private final String KEYSTORE_FILE = "keystore_file";
	private final String KEYSTORE_PATH = "/opt/labs/olis/oneid.jks";
	private static final Logger logger = MiscUtils.getLogger();
	
	protected OMDGatewayTransactionLogDao transactionLogDao = SpringUtils.getBean(OMDGatewayTransactionLogDao.class);
	private SystemPreferencesDao systemPreferencesDao = SpringUtils.getBean(SystemPreferencesDao.class);
    private OneIdSessionDao oneIdSessionDao = SpringUtils.getBean(OneIdSessionDao.class);

	public enum ToolbarKeys {
		FHIR_ISS("FHIR_iss"),
		HUB_URL("hub.url"),
		CMS_URL("cms_url");

		public final String key;

		ToolbarKeys(String key) {
			this.key = key;
		}
	}

	public static OMDGatewayTransactionLog getOMDGatewayTransactionLog(LoggedInInfo loggedInInfo, Integer demographicNo, String externalSystem, String transactionType) {
		OMDGatewayTransactionLog omdGatewayTransactionLog = new OMDGatewayTransactionLog();
		OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
		if(oneIdGatewayData != null) {
			logger.error("oneIdGatewayData.howLongUntilAccessTokenIsExpired() "+oneIdGatewayData.howLongUntilAccessTokenIsExpired());
			omdGatewayTransactionLog.setSecondsLeft(oneIdGatewayData.howLongUntilAccessTokenIsExpired());
			omdGatewayTransactionLog.setUao(oneIdGatewayData.getUao());
			omdGatewayTransactionLog.setContextSessionId(oneIdGatewayData.getCtxSessionId());
			omdGatewayTransactionLog.setUniqueSessionId(oneIdGatewayData.getUniqueSessionId());
		}

		omdGatewayTransactionLog.setDemographicNo(demographicNo);
		omdGatewayTransactionLog.setExternalSystem(externalSystem);
		omdGatewayTransactionLog.setInitiatingProviderNo(loggedInInfo.getLoggedInProviderNo());
		omdGatewayTransactionLog.setOscarSessionId(loggedInInfo.getSession().getId());
		omdGatewayTransactionLog.setStarted(new Date());
		omdGatewayTransactionLog.setTransactionType(transactionType);

		return omdGatewayTransactionLog;
	}

	protected static void completeLog(OMDGatewayTransactionLog log, Response response2) {
		completeLog(log, response2, true);
	}

	/**
	 * Records the outcome of a gateway call on its transaction log row.
	 *
	 * @param storeResponseBody when false the response body is not stored (used for token
	 *                          endpoints, whose body carries access/refresh tokens)
	 */
	protected static void completeLog(OMDGatewayTransactionLog log, Response response2, boolean storeResponseBody) {
		log.setResultCode(response2.getStatus());
		log.setEnded(new Date());

		// Buffer the entity so the body can be read here and again by the caller
		// (a CXF response stream is otherwise consumable only once).
		String body = "";
		try {
			response2.bufferEntity();
			body = response2.readEntity(String.class);
		} catch (Exception e) {
			logger.warn("Could not read gateway response body: " + e.getMessage());
		}

		String xRequestId = response2.getHeaderString("X-Request-Id");
		if (xRequestId != null) {
			log.setxRequestId(xRequestId);
		}
		String xLobTxId = response2.getHeaderString("X-LobTxId");
		if (xLobTxId != null) {
			log.setxLobTxId(xLobTxId);
		}
		String xCorrelationId = response2.getHeaderString("X-Correlation-Id");
		if (xCorrelationId != null) {
			log.setxCorrelationId(xCorrelationId);
		}

		if (response2.getStatus() >= 300) {
			log.setSuccess(false);
			log.setError(body);
		} else {
			log.setSuccess(true);
			if (storeResponseBody) {
				log.setDataRecieved(body);
			}
		}

		StringBuilder headers = new StringBuilder();
		for (String headerName : response2.getHeaders().keySet()) {
			headers.append(headerName).append(":").append(response2.getHeaderString(headerName)).append("\n");
		}
		log.setHeaders(headers.toString());
	}

	/** Generates a unique X-Request-Id for a single gateway transaction. */
	protected static String newRequestId() {
		return UUID.randomUUID().toString();
	}

	protected List<OperationOutcome> hasOperationOutcome(Bundle bundle)  {
		List<OperationOutcome> result = new ArrayList<OperationOutcome>();

		for(BundleEntryComponent comp : bundle.getEntry()) {
			Resource resource = comp.getResource();
			if(resource.getResourceType() == ResourceType.OperationOutcome) {
				OperationOutcome oo = (OperationOutcome)resource;
				result.add(oo);
			}
		}
		return result;
	}
	
	public boolean hasGatewayPropertiesSet(LoggedInInfo loggedInInfo) throws Exception{
		String clientId = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_id).getValue();
		String clientSecret =
				systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_secret).getValue();
		Path keystorePath = Paths.get(systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_path).getValue());
		String keystorePassword = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_password).getValue();
		OneIdSession oneIdSession = oneIdSessionDao.find(loggedInInfo.getLoggedInProviderNo());
		String endPoint = oneIdSession == null ? "" : oneIdSession.getUrlFromToolbar(ToolbarKeys.FHIR_ISS.key);

		StringBuilder sb = new StringBuilder();

		logger.debug("clientId" + clientId + " clientSecret " + clientSecret + " publicKeyStore " + keystorePath
				+ " keystorePassword " + keystorePassword + " endPoint " + endPoint);

		if(clientId == null || clientId.trim().isEmpty()) {
			sb.append("Client Id has not been configured. Use OSCAR property 'oneid.consumerKey' to configure.\n");
		}



		if(keystorePath.toString().trim().isEmpty()) {
			sb.append("Public Keystore has not been configured. Use OSCAR property 'oneid.gateway.keystore' to configure.\n");
		}
		try {
			if(Files.notExists(keystorePath)) {
				sb.append("Public Keystore can not be found at: ").append(keystorePath).append("\n");
			}
		}catch(Exception e) {
			sb.append("Public Keystore can not be found at: ").append(keystorePath).append("\n");
		}

		if(keystorePassword == null || keystorePassword.trim().isEmpty()) {
			sb.append("Keystore password has not been configured. Use OSCAR property 'oneid.gateway.keystore.password' to configure.\n");
		}

		if(endPoint == null || endPoint.trim().isEmpty()) {
			sb.append("Gateway endPoint has not been configured. Use OSCAR property 'oneid.gateway.url' to configure.\n");
		}


		if(sb.length() > 0) {
			OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, null, "GATEWAY" , "Configuration Error");
			omdGatewayTransactionLog.setStarted(new Date());
			omdGatewayTransactionLog.setError(sb.toString());
			transactionLogDao.persist(omdGatewayTransactionLog);
			throw(new Exception("Gateway Configuration Error"));
		}
    logger.info("has props out " + sb);
		return true;
	}
	
	public void logError(LoggedInInfo loggedInInfo,String externalSystem, String transactionType,String error) {
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, null, externalSystem, transactionType);
		omdGatewayTransactionLog.setStarted(new Date());
		omdGatewayTransactionLog.setSuccess(Boolean.FALSE);
		omdGatewayTransactionLog.setError(error);
		transactionLogDao.persist(omdGatewayTransactionLog);
	}
	
	public void logDataReceived(LoggedInInfo loggedInInfo,String externalSystem, String transactionType,String dataReceived,Integer demographicNo) {
		logDataReceived( loggedInInfo, externalSystem,  transactionType, dataReceived, demographicNo,null) ;
	}
	
	public void logDataReceived(LoggedInInfo loggedInInfo,String externalSystem, String transactionType,String dataReceived,Integer demographicNo,String uniqueToken) {
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, null, externalSystem, transactionType);
		omdGatewayTransactionLog.setStarted(new Date());
		omdGatewayTransactionLog.setSuccess(Boolean.TRUE);
		if(demographicNo != null) {
			omdGatewayTransactionLog.setDemographicNo(demographicNo);
		}
		omdGatewayTransactionLog.setDataRecieved(dataReceived);
		if(uniqueToken != null) {
			omdGatewayTransactionLog.setxCorrelationId(uniqueToken);
		}
		transactionLogDao.persist(omdGatewayTransactionLog);
	}

	public WebClient getWebClientWholeURL(LoggedInInfo loggedInInfo,String url) throws Exception {
		hasGatewayPropertiesSet(loggedInInfo);
		WebClient wc = WebClient.create(url);
		WebClient.getConfig(wc).getHttpConduit().setTlsClientParameters(getTLSClientParameters(loggedInInfo));
		return wc;
	}
	
	public WebClient getWebClient(LoggedInInfo loggedInInfo,String resource) throws Exception {
			hasGatewayPropertiesSet(loggedInInfo);
			String gatewayUrl = getEndpointURL(loggedInInfo.getLoggedInProviderNo());
			String fullURL = gatewayUrl+resource;

			WebClient wc = WebClient.create(fullURL);
			WebClient.getConfig(wc).getHttpConduit().setTlsClientParameters(getTLSClientParameters(loggedInInfo));
			long timeoutMillis = getTimeoutMillis();
			WebClient.getConfig(wc).getHttpConduit().getClient().setConnectionTimeout(timeoutMillis);
			WebClient.getConfig(wc).getHttpConduit().getClient().setReceiveTimeout(timeoutMillis);

			return wc;
		}

	/** Gateway connection/receive timeout in milliseconds, from the configurable timeout preference (in seconds). */
	protected long getTimeoutMillis() {
		long seconds = 30;
		SystemPreferences pref = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.timeout);
		if (pref != null && !pref.getValue().trim().isEmpty()) {
			try {
				seconds = Long.parseLong(pref.getValue().trim());
			} catch (NumberFormatException e) {
				logger.warn("Invalid ONE ID gateway timeout '" + pref.getValue() + "'; using " + seconds + "s");
			}
		}
		return seconds * 1000;
	}

	protected TLSClientParameters getTLSClientParameters(LoggedInInfo loggedInInfo) throws Exception {
			hasGatewayPropertiesSet(loggedInInfo);
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load( new FileInputStream(
                    Paths.get(systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_path).getValue()).toFile()
                ),
                systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_password).getValue().toCharArray()
                );
			SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(ks, systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_password).getValue().toCharArray()).build();
			sslcontext.getDefaultSSLParameters().setNeedClientAuth(true);
			sslcontext.getDefaultSSLParameters().setWantClientAuth(true);

			TLSClientParameters tlsParams = new TLSClientParameters();
			tlsParams.setSSLSocketFactory(sslcontext.getSocketFactory());
			// Hostname (CN) verification stays on; disable it only for local development.
			if ("true".equalsIgnoreCase(OscarProperties.getInstance().getProperty("oneid.disable_tls_cn_check"))) {
				tlsParams.setDisableCNCheck(true);
			}

			return tlsParams;
		}

	public Response doGet(LoggedInInfo loggedInInfo, WebClient wc) throws TokenExpiredException {
		return doGet(loggedInInfo,wc,null);
	}

	protected String getConsumerKey() {
		SystemPreferences consumerKey = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_id);
		return consumerKey == null ? null : consumerKey.getValue();
	}

	protected String getConsumerSecret() {
		SystemPreferences consumerSecret = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_secret);
		return consumerSecret == null ? null : consumerSecret.getValue();
	}
	
	protected String getEndpointURL(String providerNumber) {
		OneIdSession oneIdSession = oneIdSessionDao.find(providerNumber);
		return oneIdSession != null && oneIdSession.getUrlFromToolbar(ToolbarKeys.FHIR_ISS.key) != null
				? oneIdSession.getUrlFromToolbar(ToolbarKeys.FHIR_ISS.key)
				: "";
	}
	
	public Response doGet(LoggedInInfo loggedInInfo, WebClient wc, AuditInfo auditInfo) throws TokenExpiredException {
		OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
		// Refresh the access token if it has expired (throws when the refresh token is dead too).
		OneIDTokenUtils.verifyAccessTokenIsValid(loggedInInfo, oneIdGatewayData);
		String consumerKey = getConsumerKey();
		String consumerSecret = getConsumerSecret();
		String accessToken = oneIdGatewayData.getAccessToken();

		Integer demographicNo = null;
		String externalSystem = null;
		String transactionType = null;
		if(auditInfo != null) {
			demographicNo = auditInfo.getDemographicNo();
			externalSystem = auditInfo.getExternalSystem();
			transactionType = auditInfo.getTransactionType();
		}

		String requestId = newRequestId();
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, demographicNo, externalSystem, transactionType);
		omdGatewayTransactionLog.setDataSent(wc.getCurrentURI().toASCIIString());
		omdGatewayTransactionLog.setxGtwyClientId(consumerKey);
		omdGatewayTransactionLog.setxRequestId(requestId);
		transactionLogDao.persist(omdGatewayTransactionLog);

		Response response2;
		try {
			response2 = wc.header("Authorization", "Bearer " + accessToken).header("X-Gtwy-Client-Id", consumerKey).header("X-Gtwy-Client-Secret", consumerSecret).header("X-Request-Id", requestId).get();
			completeLog(omdGatewayTransactionLog,response2);
			transactionLogDao.merge(omdGatewayTransactionLog);
		}catch(Exception e) {
			logger.error("ERROR OMD Gateway GET",e);
			omdGatewayTransactionLog.setError(e.getLocalizedMessage());
			transactionLogDao.merge(omdGatewayTransactionLog);
			throw(e);
		}
		return response2;
	}

	public String getConsentViewletURL(LoggedInInfo loggedInInfo, int demographicNo, String target,String uniqueToken) throws Exception {
		CMSManager.consentTargetChange(loggedInInfo, demographicNo,target);
		OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
		String url = oneIdGatewayData.getPcoiUrl()+"?launch="+oneIdGatewayData.getHubTopic()+"&iss="+oneIdGatewayData.getFhirIss()+"&inheritanceID="+oneIdGatewayData.getAuthorizationId();
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, demographicNo, "PCOI", "consentViewletLaunch");
		omdGatewayTransactionLog.setDataSent(url);
		omdGatewayTransactionLog.setxCorrelationId(uniqueToken);
		transactionLogDao.persist(omdGatewayTransactionLog);
		return url;
	}
	
	public Response doPost(LoggedInInfo loggedInInfo, WebClient wc, Event fhirCastEvent) throws Exception {
		// Context submission must carry the acting authority; block it when no UAO is selected.
		OneIdGatewayData gatewayData = loggedInInfo.getOneIdGatewayData();
		if (gatewayData == null || gatewayData.getUao() == null || gatewayData.getUao().trim().isEmpty()) {
			throw new IllegalStateException("A ONE ID Under Authority Of (UAO) value must be selected before submitting context to the gateway.");
		}
		String consumerKey = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_id).getValue();
		String consumerSecret =systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_secret).getValue();
		// Refresh the access token if it has expired (throws when the refresh token is dead too).
		OneIDTokenUtils.verifyAccessTokenIsValid(loggedInInfo, loggedInInfo.getOneIdGatewayData());
		String accessToken = loggedInInfo.getOneIdGatewayData().getAccessToken();
		Integer demographicNo = null;
		String externalSystem = null;
		String transactionType = null;
		if(fhirCastEvent != null) {
			externalSystem = "CMS";
			transactionType = fhirCastEvent.getHubEvent();
		}
		String requestId = newRequestId();
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, demographicNo, externalSystem, transactionType);
		omdGatewayTransactionLog.setDataSent(fhirCastEvent.getFhirCastEvent());
		omdGatewayTransactionLog.setxGtwyClientId(consumerKey);
		omdGatewayTransactionLog.setxRequestId(requestId);
		transactionLogDao.persist(omdGatewayTransactionLog);
		Response response2 = null;
		try {
			response2 = wc.header("Authorization", "Bearer " + accessToken).header("X-Gtwy-Client-Id", consumerKey)
				.header("X-Gtwy-Client-Secret", consumerSecret).header("X-Request-Id", requestId)
				.header("X-Correlation-Id", fhirCastEvent.getId()).header("X-LobTxId", fhirCastEvent.getId())
				.header("Content-Type", "application/json").post(fhirCastEvent.getFhirCastEvent());
		completeLog(omdGatewayTransactionLog,response2);
		transactionLogDao.merge(omdGatewayTransactionLog);
		}catch(Exception e) {
			e.getMessage();
			omdGatewayTransactionLog.setError(e.getLocalizedMessage());
			transactionLogDao.merge(omdGatewayTransactionLog);
			throw(e);
		}
		return response2;
	}

	public Response getTokens(LoggedInInfo loggedInInfo,String code,String clientId, String codeVerifier,String jwt)  {
		String externalSystem = "OIDC";
		String transactionType = "TOKENS";
		String tokenUrl = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_access_token).getValue();
		String callbackUrl = PathUtils.addTrailingSlash(OscarProperties.getInstance().getProperty("clinic.url"))
				+ systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_callback).getValue();

		String requestId = newRequestId();
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, null, externalSystem, transactionType);
		omdGatewayTransactionLog.setDataSent(null);
		omdGatewayTransactionLog.setxRequestId(requestId);
		transactionLogDao.persist(omdGatewayTransactionLog);
		Response response2 = null;
		try {
			WebClient wc = WebClient.create(tokenUrl);
			wc.query("grant_type", "authorization_code");
			wc.query("client_assertion_type", "urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer");

			wc.query("code", code);
			wc.query("redirect_uri", callbackUrl);
			wc.query("client_id", clientId);

			wc.query("code_verifier",codeVerifier);
			wc.query("client_assertion", jwt);


			response2 = wc.header("X-Request-Id", requestId).header("Content-Type", "application/x-www-form-urlencoded").post(null);


		completeLog(omdGatewayTransactionLog,response2,false);
		transactionLogDao.merge(omdGatewayTransactionLog);
		}catch(Exception e) {
			e.getMessage();
			omdGatewayTransactionLog.setError(e.getLocalizedMessage());
			transactionLogDao.merge(omdGatewayTransactionLog);
			throw(e);
		}
		return response2;
	}

	/**
	 * Builds the private_key_jwt client assertion used to authenticate to the token endpoint,
	 * signed with the configured keystore key.
	 *
	 * @param loggedInInfo LoggedInInfo the current session context
	 * @return String the signed client-assertion JWT
	 * @throws Exception when the keystore cannot be read or holds no usable private key
	 */
	protected String buildClientAssertion(LoggedInInfo loggedInInfo) throws Exception {
		String clientId = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_id).getValue();
		String audURL = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_audience).getValue();
		String alias = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_alias).getValue();
		String keystoreLocation = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_path).getValue();
		String keystorePassword = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_password).getValue();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 10);
		Date expiryDate = cal.getTime();

		try (FileInputStream is = new FileInputStream(keystoreLocation)) {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, keystorePassword.toCharArray());
			Key key = keystore.getKey(alias, keystorePassword.toCharArray());
			if (!(key instanceof PrivateKey)) {
				throw new IllegalStateException("Keystore alias does not hold a private key");
			}
			Certificate cert = keystore.getCertificate(alias);
			return JWT.create().withSubject(clientId).withAudience(audURL).withExpiresAt(expiryDate).withIssuer(clientId)
					.sign(Algorithm.RSA256((RSAPublicKey) cert.getPublicKey(), (RSAPrivateKey) key));
		}
	}

	/**
	 * Exchanges an authorization code for tokens using PKCE and a private_key_jwt client assertion.
	 *
	 * @param loggedInInfo LoggedInInfo the current session context
	 * @param code String the authorization code returned to the callback
	 * @param codeVerifier String the PKCE code verifier generated at login
	 * @return Response the raw token-endpoint response
	 * @throws Exception when the client assertion cannot be built or the call fails
	 */
	public Response exchangeCodeForTokens(LoggedInInfo loggedInInfo, String code, String codeVerifier) throws Exception {
		String clientId = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_id).getValue();
		return getTokens(loggedInInfo, code, clientId, codeVerifier, buildClientAssertion(loggedInInfo));
	}

	/**
	 * Builds the OAuth2 authorize URL to redirect the browser to, including the requested scope,
	 * the PKCE code challenge, the state, and the nonce.
	 *
	 * @param oneIdGatewayData OneIdGatewayData carries the requested scope and profile
	 * @param state String the anti-forgery state stored in the session
	 * @param nonce String the nonce stored in the session for id-token validation
	 * @param verifier String the PKCE code verifier stored in the session
	 * @return String the fully-built authorize URL
	 * @throws Exception when the PKCE challenge cannot be generated
	 */
	public String buildAuthorizeUrl(OneIdGatewayData oneIdGatewayData, String state, String nonce, String verifier) throws Exception {
		String authorizeUrl = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_authorize).getValue();
		String callbackUrl = PathUtils.addTrailingSlash(OscarProperties.getInstance().getProperty("clinic.url"))
				+ systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_callback).getValue();
		String clientId = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_id).getValue();
		String aud = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_audience).getValue();
		String challenge = PKCEUtils.generateChallengeS256(verifier);

		UriBuilder uriBuilder = UriBuilder.fromUri(authorizeUrl)
				.queryParam("response_type", "code")
				.queryParam("scope", oneIdGatewayData.getScope())
				.queryParam("code_challenge_method", "S256")
				.queryParam("code_challenge", challenge)
				.queryParam("redirect_uri", callbackUrl)
				.queryParam("client_id", clientId)
				.queryParam("state", state)
				.queryParam("nonce", nonce);
		if (oneIdGatewayData.get_profile() != null && !oneIdGatewayData.get_profile().isEmpty()) {
			uriBuilder.queryParam("_profile", oneIdGatewayData.get_profile());
		}
		if (aud != null && !aud.isEmpty()) {
			uriBuilder.queryParam("aud", aud);
		}
		if (oneIdGatewayData.getUao() != null) {
			uriBuilder.queryParam("uao", oneIdGatewayData.getUao());
		}
		return uriBuilder.build().toString();
	}

	public String generateVerifier() {
	    byte[] array = new byte[50];
	    new Random().nextBytes(array);
	    String generatedString = RandomStringUtils.randomAlphabetic(50);

	    String verifier = PKCEUtils.encodeBase64NoPadding(generatedString);
	    logger.debug("verifier = "+verifier);
	    return verifier;
	}
	
	public Response callAuthorize(LoggedInInfo loggedInInfo,OneIdGatewayData oneIdGatewayData,String state,String verifier) {
		logger.info("OAUTH2 Login started oneIdGatewayData null ?"+ (oneIdGatewayData == null)+ " loggedInInfo "+(loggedInInfo.getOneIdGatewayData() == null));
		if(oneIdGatewayData == null ){
			oneIdGatewayData = new OneIdGatewayData();
		}
	    String challenge = null;
	    try {
	    	challenge = PKCEUtils.generateChallengeS256(verifier);
	    } catch(Exception e) {
	    	logger.error("Error",e);
	    }
	    logger.debug("challenge = "+challenge);


		String authorizeUrl = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_authorize).getValue();
		String callbackUrl = PathUtils.addTrailingSlash(OscarProperties.getInstance().getProperty("clinic.url"))
				+ systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_callback).getValue();
		String clientId = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_id).getValue();

		String aud = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_audience).getValue();

		WebClient wc = WebClient.create(authorizeUrl);

		wc.query("response_type", "code");

		wc.query("scope", OneIDTokenUtils.urlEncode(oneIdGatewayData.getScope()));

		if(oneIdGatewayData.get_profile() != null && oneIdGatewayData.get_profile().length() != 0) {
			wc.query("_profile",OneIDTokenUtils.urlEncode(oneIdGatewayData.get_profile()));
		}

		wc.query("code_challenge_method", "S256");

		wc.query("code_challenge", challenge);
		wc.query("redirect_uri", callbackUrl);
		wc.query("client_id", clientId);
		wc.query("state", state);
		if(aud != null){
			wc.query("aud",aud);
		}
		if(oneIdGatewayData.getUao() != null) {
			wc.query("uao",oneIdGatewayData.getUao());
		}

		OMDGatewayTransactionLog omdGatewayTransactionLog = OmdGateway.getOMDGatewayTransactionLog(loggedInInfo, null, "Auth", "AUTHORIZE");
		transactionLogDao.persist(omdGatewayTransactionLog);
		Response response2 = null;
		try {
			response2 = wc.header("Content-Type", "application/x-www-form-urlencoded").get();
			completeLog(omdGatewayTransactionLog,response2);
			transactionLogDao.merge(omdGatewayTransactionLog);
			logger.info("Response Status from /Authorize =" + response2.getStatus());
		}catch(Exception e) {
			logger.error("Error calling Authorize "+omdGatewayTransactionLog,e);
			omdGatewayTransactionLog.setError(ExceptionUtils.getStackTrace(e));
			omdGatewayTransactionLog.setSuccess(false);
			transactionLogDao.merge(omdGatewayTransactionLog);
		}
		return response2;
	}
	
	public void refreshToken(LoggedInInfo loggedInInfo,OneIdGatewayData oneIdGatewayData) throws TokenExpiredException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 10);
		Date expiryDate = cal.getTime();

		String tokenUrl = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_access_token).getValue();
		String audURL = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_audience).getValue();

		String clientId = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_id).getValue();
		String alias = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_alias).getValue();
		String keystoreLocation = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_path).getValue();
		String keystorePassword= systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.keystore_password).getValue();

		Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "refresh_token");
		params.put("client_id", clientId);
		params.put("client_assertion_type", "urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer");
		params.put("refresh_token", oneIdGatewayData.getRefreshTokenString());

		try (FileInputStream is = new FileInputStream(keystoreLocation);) {

			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, keystorePassword.toCharArray());

			Key key = keystore.getKey(alias, keystorePassword.toCharArray());

			if (key instanceof PrivateKey) {
				Certificate cert = keystore.getCertificate(alias);

				JWTCreator.Builder builder = JWT.create().withSubject(clientId).withAudience(audURL).withExpiresAt(expiryDate).withIssuer(clientId);
				String jwt = builder.sign(Algorithm.RSA256((RSAPublicKey) cert.getPublicKey(), (RSAPrivateKey) key));
				params.put("client_assertion", jwt);
			}

			WebClient wc = WebClient.create(tokenUrl);
			for (Entry<String, String> entry : params.entrySet()) {
				wc.query(entry.getKey(), entry.getValue());
			}
			String requestId = newRequestId();
			OMDGatewayTransactionLog omdGatewayTransactionLog = OmdGateway.getOMDGatewayTransactionLog(loggedInInfo, null, "Auth", "REFRESH");
			omdGatewayTransactionLog.setxRequestId(requestId);
			transactionLogDao.persist(omdGatewayTransactionLog);
			Response response2 = wc.header("X-Request-Id", requestId).header("Content-Type", "application/x-www-form-urlencoded").post(null);
			completeLog(omdGatewayTransactionLog,response2,false);
			transactionLogDao.merge(omdGatewayTransactionLog);

			if(response2.getStatus() == 200) {
				String body = response2.readEntity(String.class);
				JSONObject respObj = new JSONObject(body);
				String accessToken = respObj.getString("access_token");
				oneIdGatewayData.processAccessToken(accessToken);

			} else {
				logger.error("ONE ID token refresh failed (HTTP " + response2.getStatus() + ")");
				throw new TokenExpiredException();
			}

		}catch(TokenExpiredException e) {
			throw e;
		}catch(Exception e) {
			logger.error("ONE ID token refresh failed", e);
			throw new TokenExpiredException();
		}
	}

	/**
	 * Revokes the ONE ID tokens for the acting provider at the revocation endpoint. Revoking one
	 * token revokes the whole grant.
	 *
	 * @param loggedInInfo LoggedInInfo the current session context
	 * @param oneIdGatewayData OneIdGatewayData the gateway data holding the access token
	 * @throws Exception when the client assertion cannot be built or the call fails
	 */
	public void revokeToken(LoggedInInfo loggedInInfo, OneIdGatewayData oneIdGatewayData) throws Exception {
		String revokeUrl = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_revocation).getValue();
		String clientId = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oag_client_id).getValue();
		String jwt = buildClientAssertion(loggedInInfo);
		String requestId = newRequestId();
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, null, "Auth", "REVOKE");
		omdGatewayTransactionLog.setxRequestId(requestId);
		transactionLogDao.persist(omdGatewayTransactionLog);
		try {
			WebClient wc = WebClient.create(revokeUrl);
			wc.query("token", oneIdGatewayData.getAccessTokenStr());
			wc.query("client_id", clientId);
			wc.query("client_assertion_type", "urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer");
			wc.query("client_assertion", jwt);
			Response response2 = wc.header("X-Request-Id", requestId).header("Content-Type", "application/x-www-form-urlencoded").post(null);
			completeLog(omdGatewayTransactionLog, response2, false);
			transactionLogDao.merge(omdGatewayTransactionLog);
		} catch (Exception e) {
			omdGatewayTransactionLog.setError(e.getLocalizedMessage());
			transactionLogDao.merge(omdGatewayTransactionLog);
			throw e;
		}
	}

	/**
	 * Builds the OpenID Connect End Session URL to redirect the browser to at logout.
	 *
	 * @param idTokenHint String the id token that hints the session being ended, or null
	 * @return String the End Session URL
	 */
	public String buildEndSessionUrl(String idTokenHint) {
		String endSessionUrl = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.endpoint_end_session).getValue();
		String postLogout = PathUtils.addTrailingSlash(OscarProperties.getInstance().getProperty("clinic.url")) + "oneIdLoggedOut.jsp";
		UriBuilder uriBuilder = UriBuilder.fromUri(endSessionUrl).queryParam("post_logout_redirect_uri", postLogout);
		if (idTokenHint != null && !idTokenHint.isEmpty()) {
			uriBuilder.queryParam("id_token_hint", idTokenHint);
		}
		return uriBuilder.build().toString();
	}
}
