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
import ca.openosp.openo.integration.ohcms.CMSException;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.struts2.ServletActionContext;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ProcessingException;
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
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	 * @param storeResponseDetail when false neither the response body nor the response headers are
	 *                            stored. Used for the OAuth calls: a token response carries access
	 *                            and refresh tokens in its body, and an authorize response carries
	 *                            the authorization code in its Location header. The correlation
	 *                            headers are still lifted into their own columns above, so a call
	 *                            excluded here is still traceable.
	 */
	protected static void completeLog(OMDGatewayTransactionLog log, Response response2, boolean storeResponseDetail) {
		log.setResultCode(response2.getStatus());
		log.setEnded(new Date());

		// Buffer the entity so the body can be read here and again by the caller
		// (a CXF response stream is otherwise consumable only once).
		String body = "";
		try {
			response2.bufferEntity();
			body = response2.readEntity(String.class);
		} catch (Exception e) {
			logger.warn("Could not read gateway response body (" + e.getClass().getSimpleName() + ")");
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

		// Stored for any status: a refusal carries the code that explains it, and a 200 can still
		// carry one, for example the notice that a temporary consent unblock is in effect.
		log.setEhrResultCode(extractEhrResultCode(body));

		if (response2.getStatus() >= 300) {
			log.setSuccess(false);
			// A CMS refusal body quotes back the patient context just sent, so it is not stored.
			// The outcome code is lifted into its own column above, which is what an auditor
			// filters on. A refusal from any other system carries an OperationOutcome instead.
			if (!CMS_EXTERNAL_SYSTEM.equals(log.getExternalSystem())) {
				log.setError(body);
			}
		} else {
			log.setSuccess(true);
			if (storeResponseDetail) {
				log.setDataRecieved(body);
			}
		}

		// Headers are withheld on the same terms as the body, because an OAuth response carries
		// secrets in both: an authorize response returns the authorization code in Location. The
		// correlation ids are lifted into their own columns above, so a call excluded here is
		// still traceable.
		if (storeResponseDetail) {
			StringBuilder headers = new StringBuilder();
			for (String headerName : response2.getHeaders().keySet()) {
				headers.append(headerName).append(":").append(response2.getHeaderString(headerName)).append("\n");
			}
			log.setHeaders(headers.toString());
		}
	}

	/** Generates a unique X-Request-Id for a single gateway transaction. */
	protected static String newRequestId() {
		return UUID.randomUUID().toString();
	}

	/** The externalSystem recorded on a context call to the Ontario Health CMS. */
	protected static final String CMS_EXTERNAL_SYSTEM = "CMS";

	/** How many nested causes to render before stopping. */
	private static final int MAX_CAUSE_DEPTH = 10;

	/**
	 * Renders a throwable as its class names and stack frames, with every message dropped.
	 *
	 * <p>A failed gateway call raises an exception whose message embeds the request URI, and a
	 * search URI carries the patient's health card number and date of birth. A stack frame holds
	 * only a class, method, file and line, so it cannot carry request data; only the message can.
	 * Keeping the frames and dropping the messages leaves the diagnostic value in the application
	 * log and takes the patient data out of it.
	 *
	 * @param throwable Throwable the exception to render, which may be null
	 * @return String the class names and stack frames, or "(none)" when there is no exception
	 * @since 2026-08-05
	 */
	public static String stackTraceWithoutMessages(Throwable throwable) {
		if (throwable == null) {
			return "(none)";
		}
		StringBuilder rendered = new StringBuilder();
		Throwable current = throwable;
		for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
			if (depth > 0) {
				rendered.append("Caused by: ");
			}
			rendered.append(current.getClass().getName()).append('\n');
			for (StackTraceElement frame : current.getStackTrace()) {
				rendered.append("\tat ").append(frame).append('\n');
			}
			// A throwable may report itself as its own cause; stop rather than loop.
			current = current.getCause() == current ? null : current.getCause();
		}
		return rendered.toString();
	}

	private static final ObjectMapper auditObjectMapper = new ObjectMapper();
	private static final int MAX_EHR_RESULT_CODE_LENGTH = 255;

	/**
	 * Reads the EHR service's own outcome codes out of a response body, for example IN_0045 or
	 * CONSENT_EXISTS. They arrive inside an OperationOutcome, either on its own or as an entry in a
	 * returned Bundle, and they are the only thing that says why a call was refused: the HTTP status
	 * records that it was.
	 *
	 * @param body String the raw response body, which may be empty or may not be JSON at all
	 * @return String the codes found, comma separated, or null when the body carries none
	 * @since 2026-08-04
	 */
	static String extractEhrResultCode(String body) {
		if (body == null || body.trim().isEmpty()) {
			return null;
		}
		try {
			List<String> codes = new ArrayList<String>();
			collectOutcomeCodes(auditObjectMapper.readTree(body), codes);
			if (codes.isEmpty()) {
				return null;
			}
			String joined = String.join(",", codes);
			return joined.length() > MAX_EHR_RESULT_CODE_LENGTH
					? joined.substring(0, MAX_EHR_RESULT_CODE_LENGTH) : joined;
		} catch (Exception e) {
			// A body that is not JSON, or not shaped like FHIR, simply has no code to record.
			return null;
		}
	}

	/**
	 * Walks a parsed body for OperationOutcome resources and collects the codes on their issues.
	 *
	 * <p>The search stops at each OperationOutcome instead of collecting every coding in the body.
	 * A returned Bundle is full of clinical codings, drug identifiers among them, and sweeping
	 * those into an audit column would put patient data in a field meant to hold a status.
	 */
	private static void collectOutcomeCodes(JsonNode node, List<String> codes) {
		if (node == null) {
			return;
		}
		if (node.isArray()) {
			for (JsonNode child : node) {
				collectOutcomeCodes(child, codes);
			}
			return;
		}
		if (!node.isObject()) {
			return;
		}
		JsonNode resourceType = node.get("resourceType");
		if (resourceType != null && "OperationOutcome".equals(resourceType.asText())) {
			JsonNode issues = node.get("issue");
			if (issues != null && issues.isArray()) {
				for (JsonNode issue : issues) {
					JsonNode details = issue.get("details");
					JsonNode coding = details == null ? null : details.get("coding");
					if (coding == null || !coding.isArray()) {
						continue;
					}
					for (JsonNode entry : coding) {
						JsonNode code = entry.get("code");
						if (code != null && code.isTextual() && !code.asText().trim().isEmpty()
								&& !codes.contains(code.asText().trim())) {
							codes.add(code.asText().trim());
						}
					}
				}
			}
			return;
		}
		for (JsonNode child : node) {
			collectOutcomeCodes(child, codes);
		}
	}

	/**
	 * Resolves the base URL of this OpenO instance, used to compose the OAuth2 redirect URI and the
	 * post-logout redirect URI. The configured {@code clinic.url} property wins whenever it holds a
	 * value; when it is absent the address is derived from the request being served, so a stock
	 * installation works without the property being set.
	 *
	 * @return String the base URL ending in a slash, for example {@code https://emr.example.ca/oscar/}
	 * @throws IllegalStateException when {@code clinic.url} is unset and there is no request to derive from
	 * @since 2026-08-04
	 */
	static String resolveBaseUrl() {
		String configured = OscarProperties.getInstance().getProperty("clinic.url");
		if (configured != null && !configured.trim().isEmpty()) {
			return PathUtils.addTrailingSlash(configured.trim());
		}
		HttpServletRequest request = currentRequest();
		if (request == null) {
			throw new IllegalStateException("The OpenO address could not be determined: clinic.url is not "
					+ "configured and this call is not serving a request. Set clinic.url in the properties file.");
		}
		return PathUtils.addTrailingSlash(baseUrlFromRequest(request));
	}

	/** Returns the request being served, or null outside a request. */
	private static HttpServletRequest currentRequest() {
		try {
			return ServletActionContext.getRequest();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Builds the base URL from the address the servlet container reports for the request. The
	 * X-Forwarded headers are deliberately not read here: they are set by the client and would let a
	 * crafted request choose the redirect URI. A proxied installation supplies its public address one
	 * of two ways, both of which land in the values read below: configure the container's
	 * {@code RemoteIpValve} (or {@code RemoteIpFilter}), which resolves those headers only for
	 * requests arriving from a trusted proxy, or set {@code clinic.url}, which takes precedence over
	 * this method entirely.
	 *
	 * @param request HttpServletRequest the request being served
	 * @return String the scheme, authority and context path, with no trailing slash
	 */
	private static String baseUrlFromRequest(HttpServletRequest request) {
		String scheme = request.getScheme();
		int port = request.getServerPort();
		StringBuilder url = new StringBuilder(scheme).append("://").append(request.getServerName());
		if (port > 0 && port != defaultPort(scheme)) {
			url.append(':').append(port);
		}
		return url.append(request.getContextPath()).toString();
	}

	private static int defaultPort(String scheme) {
		return "https".equalsIgnoreCase(scheme) ? 443 : 80;
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
		String clientId = getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_id);
		String keystoreLocation = getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_path);
		Path keystorePath = keystoreLocation == null || keystoreLocation.trim().isEmpty()
				? null
				: Paths.get(keystoreLocation);
		String keystorePassword = getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_password);
		OneIdSession oneIdSession = oneIdSessionDao.find(loggedInInfo.getLoggedInProviderNo());
		String endPoint = oneIdSession == null ? "" : oneIdSession.getUrlFromToolbar(ToolbarKeys.FHIR_ISS.key);

		StringBuilder sb = new StringBuilder();

		logger.debug("clientId " + clientId + " publicKeyStore " + keystorePath + " endPoint " + endPoint);

		if(clientId == null || clientId.trim().isEmpty()) {
			sb.append("Client Id has not been configured. Use OSCAR property 'oneid.consumerKey' to configure.\n");
		}



		if(keystorePath == null) {
			sb.append("Public Keystore has not been configured. Use OSCAR property 'oneid.gateway.keystore' to configure.\n");
		} else {
			try {
				if(Files.notExists(keystorePath)) {
					sb.append("Public Keystore can not be found at: ").append(keystorePath).append("\n");
				}
			}catch(Exception e) {
				sb.append("Public Keystore can not be found at: ").append(keystorePath).append("\n");
			}
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
		logError(loggedInInfo, externalSystem, transactionType, error, null, null);
	}

	public void logError(LoggedInInfo loggedInInfo,String externalSystem, String transactionType,String error,Integer demographicNo,String uniqueToken) {
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, null, externalSystem, transactionType);
		omdGatewayTransactionLog.setStarted(new Date());
		omdGatewayTransactionLog.setSuccess(Boolean.FALSE);
		omdGatewayTransactionLog.setError(error);
		if(demographicNo != null) {
			omdGatewayTransactionLog.setDemographicNo(demographicNo);
		}
		if(uniqueToken != null) {
			omdGatewayTransactionLog.setxCorrelationId(uniqueToken);
		}
		transactionLogDao.persist(omdGatewayTransactionLog);
	}
	
	public void logDataReceived(LoggedInInfo loggedInInfo,String externalSystem, String transactionType,String dataReceived,Integer demographicNo) {
		logDataReceived( loggedInInfo, externalSystem,  transactionType, dataReceived, demographicNo,null) ;
	}
	
	public void logDataReceived(LoggedInInfo loggedInInfo,String externalSystem, String transactionType,String dataReceived,Integer demographicNo,String uniqueToken) {
		logDataReceived(loggedInInfo, externalSystem, transactionType, dataReceived, demographicNo, uniqueToken, Boolean.TRUE);
	}

	/**
	 * Records data received from an external system, with the outcome stated by the caller.
	 *
	 * <p>The success column is what an auditor filters on to answer whether an access happened, so
	 * it has to mean one thing. The overloads above record a success because that is all they are
	 * used for; a caller that records a mix of outcomes through this method must pass the real one,
	 * otherwise a row describing a failed operation still reads as successful.
	 *
	 * @param loggedInInfo   LoggedInInfo the acting provider session
	 * @param externalSystem String the system the data came from
	 * @param transactionType String the transaction being recorded
	 * @param dataReceived   String the payload to store on the row
	 * @param demographicNo  Integer the patient the data concerns, or null
	 * @param uniqueToken    String the correlation id tying this row to its request, or null
	 * @param success        Boolean the real outcome; null when it is genuinely not known
	 * @since 2026-08-04
	 */
	public void logDataReceived(LoggedInInfo loggedInInfo,String externalSystem, String transactionType,String dataReceived,Integer demographicNo,String uniqueToken,Boolean success) {
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, null, externalSystem, transactionType);
		omdGatewayTransactionLog.setStarted(new Date());
		omdGatewayTransactionLog.setSuccess(success);
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
		long timeoutMillis = getTimeoutMillis();
		WebClient.getConfig(wc).getHttpConduit().getClient().setConnectionTimeout(timeoutMillis);
		WebClient.getConfig(wc).getHttpConduit().getClient().setReceiveTimeout(timeoutMillis);
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
		long seconds = 65;
		String configured = getPreferenceValue(SystemPreferences.ONEID_KEYS.timeout);
		if (configured != null && !configured.trim().isEmpty()) {
			try {
				seconds = Long.parseLong(configured.trim());
			} catch (NumberFormatException e) {
				logger.warn("Invalid ONE ID gateway timeout '" + configured + "'; using " + seconds + "s");
			}
		}
		return seconds * 1000;
	}

	protected TLSClientParameters getTLSClientParameters(LoggedInInfo loggedInInfo) throws Exception {
			hasGatewayPropertiesSet(loggedInInfo);
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load( new FileInputStream(
                    Paths.get(getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_path)).toFile()
                ),
                getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_password).toCharArray()
                );
			SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(ks, getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_password).toCharArray()).build();
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

	/**
	 * Reads a ONE ID gateway setting.
	 *
	 * @param key ONEID_KEYS the setting to read
	 * @return String the configured value, or null where the setting has no row or holds no value
	 */
	private String getPreferenceValue(SystemPreferences.ONEID_KEYS key) {
		SystemPreferences preference = systemPreferencesDao.findPreferenceByName(key);
		return preference == null ? null : preference.getValue();
	}

	protected String getConsumerKey() {
		return getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_id);
	}

	protected String getConsumerSecret() {
		return getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_secret);
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
			logger.error("OMD Gateway GET failed\n" + stackTraceWithoutMessages(e));
			// The call threw before any response arrived, so completeLog never ran and the outcome
			// is set here instead. A row left with a null success reads as one whose call is still
			// in flight.
			omdGatewayTransactionLog.setSuccess(Boolean.FALSE);
			omdGatewayTransactionLog.setError(e.getLocalizedMessage());
			transactionLogDao.merge(omdGatewayTransactionLog);
			throw(e);
		}
		return response2;
	}

	/**
	 * Builds the launch URL for the consent Viewlet: sets the consent target in CMS context (with
	 * the bounded retry), takes the PCOI service address resolved from the ONE ID toolbar, and
	 * appends the launch topic, FHIR issuer and authorization reference. One transaction-log row is
	 * written per launch.
	 *
	 * @param loggedInInfo  LoggedInInfo the acting provider session
	 * @param demographicNo int the patient the Viewlet is launched for
	 * @param target        String the consent target the override applies to
	 * @param uniqueToken   String correlation token recorded on the transaction-log row
	 * @return String the consent Viewlet launch URL
	 * @throws Exception CMSException when the context is not acknowledged or the toolbar carries no
	 *                   PCOI address
	 */
	public String getConsentViewletURL(LoggedInInfo loggedInInfo, int demographicNo, String target,String uniqueToken) throws Exception {
		setContextWithRetry(() -> CMSManager.consentTargetChange(loggedInInfo, demographicNo, target));
		OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
		String pcoiUrl = oneIdGatewayData.getPcoiUrl();
		if (pcoiUrl == null || pcoiUrl.trim().isEmpty()) {
			throw new CMSException("No consent service address was found in the ONE ID toolbar. Check the PCOI Key setting names a registered Viewlet.");
		}
		String url = pcoiUrl+"?launch="+oneIdGatewayData.getHubTopic()+"&iss="+oneIdGatewayData.getFhirIss()+"&inheritanceID="+oneIdGatewayData.getAuthorizationId();
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, demographicNo, "PCOI", "consentViewletLaunch");
		omdGatewayTransactionLog.setDataSent(url);
		omdGatewayTransactionLog.setxCorrelationId(uniqueToken);
		omdGatewayTransactionLog.setSuccess(Boolean.TRUE);
		transactionLogDao.persist(omdGatewayTransactionLog);
		return url;
	}

	/**
	 * Builds the launch URL for a configured Viewlet: puts the patient in CMS context (with the
	 * bounded retry), resolves the Viewlet's service address from the ONE ID toolbar by its key,
	 * and appends the launch topic, FHIR issuer and authorization reference. One transaction-log
	 * row is written per launch.
	 *
	 * @param loggedInInfo  LoggedInInfo the acting provider session
	 * @param demographicNo int the patient the Viewlet is launched for
	 * @param viewletKey    String the Viewlet's toolbar key
	 * @param uniqueToken   String correlation token recorded on the transaction-log row
	 * @return String the Viewlet launch URL
	 * @throws Exception CMSException when the context is not acknowledged or the toolbar carries no
	 *                   address for the key
	 */
	public String getViewletLaunchURL(LoggedInInfo loggedInInfo, int demographicNo, String viewletKey, String uniqueToken) throws Exception {
		setContextWithRetry(() -> CMSManager.patientChange(loggedInInfo, demographicNo));
		OneIdSession oneIdSession = oneIdSessionDao.find(loggedInInfo.getLoggedInProviderNo());
		String serviceUrl = oneIdSession == null ? null : oneIdSession.getUrlFromToolbar(viewletKey);
		if (serviceUrl == null || serviceUrl.trim().isEmpty()) {
			throw new CMSException("No service address was found in the ONE ID toolbar for key " + viewletKey + ".");
		}
		OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
		String url = serviceUrl+"?launch="+oneIdGatewayData.getHubTopic()+"&iss="+oneIdGatewayData.getFhirIss()+"&inheritanceID="+oneIdGatewayData.getAuthorizationId();
		OMDGatewayTransactionLog omdGatewayTransactionLog = getOMDGatewayTransactionLog(loggedInInfo, demographicNo, viewletKey, "viewletLaunch");
		omdGatewayTransactionLog.setDataSent(url);
		omdGatewayTransactionLog.setxCorrelationId(uniqueToken);
		omdGatewayTransactionLog.setSuccess(Boolean.TRUE);
		transactionLogDao.persist(omdGatewayTransactionLog);
		return url;
	}

	private static final int MAX_SET_CONTEXT_ATTEMPTS = 3;

	private interface ContextCall {
		void run() throws Exception;
	}

	/**
	 * Runs a CMS set-context call, retrying a bounded number of times when the context is not
	 * acknowledged: either the CMS returns a non-2xx ({@link CMSException}) or no response comes
	 * back at all ({@link ProcessingException}, e.g. a connection or read timeout). Once the
	 * attempts are exhausted the last failure is propagated so the caller can surface it and leave a
	 * re-attempt available. A missing UAO or any other error propagates immediately.
	 *
	 * @param contextCall ContextCall the set-context call to run
	 * @throws Exception the last failure when the context is not acknowledged after the bounded attempts
	 */
	private void setContextWithRetry(ContextCall contextCall) throws Exception {
		for (int attempt = 1; attempt <= MAX_SET_CONTEXT_ATTEMPTS; attempt++) {
			try {
				contextCall.run();
				return;
			} catch (CMSException | ProcessingException e) {
				logger.warn("CMS set-context not acknowledged (attempt {} of {})", attempt, MAX_SET_CONTEXT_ATTEMPTS);
				if (attempt == MAX_SET_CONTEXT_ATTEMPTS) {
					throw e;
				}
			}
		}
	}
	
	public Response doPost(LoggedInInfo loggedInInfo, WebClient wc, Event fhirCastEvent) throws Exception {
		// Context submission must carry the acting authority; block it when no UAO is selected.
		OneIdGatewayData gatewayData = loggedInInfo.getOneIdGatewayData();
		if (gatewayData == null || gatewayData.getUao() == null || gatewayData.getUao().trim().isEmpty()) {
			throw new IllegalStateException("A ONE ID Under Authority Of (UAO) value must be selected before submitting context to the gateway.");
		}
		String consumerKey = getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_id);
		String consumerSecret =getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_secret);
		// Refresh the access token if it has expired (throws when the refresh token is dead too).
		OneIDTokenUtils.verifyAccessTokenIsValid(loggedInInfo, loggedInInfo.getOneIdGatewayData());
		String accessToken = loggedInInfo.getOneIdGatewayData().getAccessToken();
		Integer demographicNo = null;
		String externalSystem = null;
		String transactionType = null;
		if(fhirCastEvent != null) {
			externalSystem = CMS_EXTERNAL_SYSTEM;
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
			// The call threw before any response arrived, so completeLog never ran and the outcome
			// is set here instead. A row left with a null success reads as one whose call is still
			// in flight.
			omdGatewayTransactionLog.setSuccess(Boolean.FALSE);
			omdGatewayTransactionLog.setError(e.getLocalizedMessage());
			transactionLogDao.merge(omdGatewayTransactionLog);
			throw(e);
		}
		return response2;
	}

	public Response getTokens(LoggedInInfo loggedInInfo,String code,String clientId, String codeVerifier,String jwt)  {
		String externalSystem = "OIDC";
		String transactionType = "TOKENS";
		String tokenUrl = getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_access_token);
		String callbackUrl = resolveBaseUrl()
				+ getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_callback);

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
			// The call threw before any response arrived, so completeLog never ran and the outcome
			// is set here instead. A row left with a null success reads as one whose call is still
			// in flight.
			omdGatewayTransactionLog.setSuccess(Boolean.FALSE);
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
		String clientId = getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_id);
		String audURL = getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_audience);
		String alias = getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_alias);
		String keystoreLocation = getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_path);
		String keystorePassword = getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_password);

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
		String clientId = getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_id);
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
		String authorizeUrl = getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_authorize);
		String callbackUrl = resolveBaseUrl()
				+ getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_callback);
		String clientId = getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_id);
		String aud = getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_audience);
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
	    byte[] randomBytes = new byte[32];
	    new SecureRandom().nextBytes(randomBytes);
	    return PKCEUtils.encodeBase64NoPadding(randomBytes);
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


		String authorizeUrl = getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_authorize);
		String callbackUrl = resolveBaseUrl()
				+ getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_callback);
		String clientId = getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_id);

		String aud = getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_audience);

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
			completeLog(omdGatewayTransactionLog,response2,false);
			transactionLogDao.merge(omdGatewayTransactionLog);
			logger.info("Response Status from /Authorize =" + response2.getStatus());
		}catch(Exception e) {
			logger.error("Authorize call failed\n" + stackTraceWithoutMessages(e));
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

		String tokenUrl = getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_access_token);
		String audURL = getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_audience);

		String clientId = getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_id);
		String alias = getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_alias);
		String keystoreLocation = getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_path);
		String keystorePassword= getPreferenceValue(SystemPreferences.ONEID_KEYS.keystore_password);

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
				String rotatedRefreshToken = respObj.optString("refresh_token", null);
				if (rotatedRefreshToken != null && !rotatedRefreshToken.isEmpty()) {
					oneIdGatewayData.setRefreshTokenStr(rotatedRefreshToken);
					try {
						oneIdGatewayData.processRefreshToken(rotatedRefreshToken);
					} catch (Exception e) {
						logger.warn("Could not decode the rotated refresh token; keeping the raw value");
					}
				}
				persistRefreshedTokens(loggedInInfo, accessToken, rotatedRefreshToken);

			} else {
				logger.error("ONE ID token refresh failed (HTTP " + response2.getStatus() + ")");
				throw new TokenExpiredException();
			}

		}catch(TokenExpiredException e) {
			throw e;
		}catch(Exception e) {
			logger.error("ONE ID token refresh failed\n" + stackTraceWithoutMessages(e));
			throw new TokenExpiredException();
		}
	}

	/**
	 * Writes freshly refreshed tokens onto the provider's persisted ONE ID session. Without this
	 * the session filter restores the replaced tokens from the row on the next request, forcing a
	 * refresh on every call and losing a rotated refresh token entirely. Best-effort: the tokens
	 * already refreshed in memory must keep serving the current request even when the row cannot
	 * be written.
	 *
	 * @param loggedInInfo LoggedInInfo the current session context
	 * @param accessToken String the new access token
	 * @param refreshToken String the rotated refresh token, or null when the broker kept the old one
	 */
	private void persistRefreshedTokens(LoggedInInfo loggedInInfo, String accessToken, String refreshToken) {
		try {
			if (loggedInInfo == null || loggedInInfo.getLoggedInProviderNo() == null) {
				return;
			}
			OneIdSession oneIdSession = oneIdSessionDao.find(loggedInInfo.getLoggedInProviderNo());
			if (oneIdSession == null) {
				return;
			}
			oneIdSession.setAccessToken(accessToken);
			if (refreshToken != null && !refreshToken.isEmpty()) {
				oneIdSession.setRefreshToken(refreshToken);
			}
			oneIdSessionDao.merge(oneIdSession);
		} catch (Exception e) {
			logger.warn("Could not persist the refreshed ONE ID tokens (" + e.getClass().getSimpleName() + ")");
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
		String revokeUrl = getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_revocation);
		String clientId = getPreferenceValue(SystemPreferences.ONEID_KEYS.oag_client_id);
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
			// The call threw before any response arrived, so completeLog never ran and the outcome
			// is set here instead. A row left with a null success reads as one whose call is still
			// in flight.
			omdGatewayTransactionLog.setSuccess(Boolean.FALSE);
			omdGatewayTransactionLog.setError(e.getLocalizedMessage());
			transactionLogDao.merge(omdGatewayTransactionLog);
			throw e;
		}
	}

	/**
	 * Builds the OpenID Connect End Session URL to redirect the browser to at logout. Users with EHR
	 * service access land on the residual-PHI notice page; others land on the login page.
	 *
	 * @param idTokenHint       String the id token that hints the session being ended, or null
	 * @param showPrivacyNotice boolean whether to land on the residual-PHI notice page
	 * @return String the End Session URL
	 */
	public String buildEndSessionUrl(String idTokenHint, boolean showPrivacyNotice) {
		String endSessionUrl = getPreferenceValue(SystemPreferences.ONEID_KEYS.endpoint_end_session);
		String landingPage = showPrivacyNotice ? "oneIdLoggedOut.jsp" : "index.jsp";
		String postLogout = resolveBaseUrl() + landingPage;
		UriBuilder uriBuilder = UriBuilder.fromUri(endSessionUrl).queryParam("post_logout_redirect_uri", postLogout);
		if (idTokenHint != null && !idTokenHint.isEmpty()) {
			uriBuilder.queryParam("id_token_hint", idTokenHint);
		}
		return uriBuilder.build().toString();
	}
}
