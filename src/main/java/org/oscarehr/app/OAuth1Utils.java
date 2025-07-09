import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.apache.cxf.rs.security.oauth.client.OAuthClientUtils;
import org.apache.logging.log4j.Logger;
import org.oscarehr.common.model.AppDefinition;
import org.oscarehr.common.model.AppUser;
	// @@ -56,113 +58,22 @@ public static List<Object> getProviderK2A() {
    // }

    public static String getOAuthGetResponse(LoggedInInfo loggedInInfo, AppDefinition app, AppUser user, String requestURI, String baseRequestURI) {
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        BufferedReader bufferedReader = null;
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            OAuthClientUtils.Consumer consumer = new OAuthClientUtils.Consumer(appAuthConfig.getConsumerKey(), appAuthConfig.getConsumerSecret());
            OAuthClientUtils.Token accessToken = new OAuthClientUtils.Token(keySecret.get("key"), keySecret.get("secret"));

            requestURI = appAuthConfig.getBaseURL() + requestURI;
            WebClient webclient = WebClient.create(requestURI);

            webclient = webclient.replaceHeader("Authorization", OAuthClientUtils.createAuthorizationHeader(consumer, accessToken, "GET", appAuthConfig.getBaseURL() + baseRequestURI));

            javax.ws.rs.core.Response reps = webclient.get();

            in = (InputStream) reps.getEntity();
            bufferedReader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            String action = "oauth1.GET, AppId=" + app.getId() + ", AppUser=" + user.getId();
            String content = appAuthConfig.getBaseURL() + baseRequestURI;
            String contentId = "AppUser=" + user.getId();
            String demographicNo = null;
            String data = sb.toString();
            LogAction.addLog(loggedInInfo, action, content, contentId, demographicNo, data);
            logger.debug("logaction " + action);

            return data;
        } catch (Exception e) {
            logger.error("Error getting information from OAuth Service", e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    public static String getOAuthPostResponse(LoggedInInfo loggedInInfo, AppDefinition app, AppUser user, String requestURI, String baseRequestURI, List<Object> providers, Object obj) {
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        BufferedReader bufferedReader = null;
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            OAuthClientUtils.Consumer consumer = new OAuthClientUtils.Consumer(appAuthConfig.getConsumerKey(), appAuthConfig.getConsumerSecret());
            OAuthClientUtils.Token accessToken = new OAuthClientUtils.Token(keySecret.get("key"), keySecret.get("secret"));

            requestURI = appAuthConfig.getBaseURL() + requestURI;
            WebClient webclient = WebClient.create(requestURI, providers);

            webclient = webclient.replaceHeader("Authorization", OAuthClientUtils.createAuthorizationHeader(consumer, accessToken, "POST", appAuthConfig.getBaseURL() + baseRequestURI));

            javax.ws.rs.core.Response reps = webclient.accept("application/json, text/plain, */*").acceptEncoding("gzip, deflate").type("application/json;charset=utf-8").post(obj);

            in = (InputStream) reps.getEntity();
            bufferedReader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String action = "oauth1.POST, AppId=" + app.getId() + ", AppUser=" + user.getId();
            String content = appAuthConfig.getBaseURL() + baseRequestURI;
            String contentId = "AppUser=" + user.getId();
            String demographicNo = null;
            String data = sb.toString();
            LogAction.addLog(loggedInInfo, action, content, contentId, demographicNo, data);


            return data;
        } catch (Exception e) {
            logger.error("Error posting information to OAuth Service", e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    public static void getOAuthDeleteResponse(AppDefinition app, AppUser user, String requestURI, String baseRequestURI) {
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        BufferedReader bufferedReader = null;
        try {
            AppOAuth1Config appAuthConfig = AppOAuth1Config.fromDocument(app.getConfig());
            Map<String, String> keySecret = AppOAuth1Config.getKeySecret(user.getAuthenticationData());

            OAuthClientUtils.Consumer consumer = new OAuthClientUtils.Consumer(appAuthConfig.getConsumerKey(), appAuthConfig.getConsumerSecret());
            OAuthClientUtils.Token accessToken = new OAuthClientUtils.Token(keySecret.get("key"), keySecret.get("secret"));

            requestURI = appAuthConfig.getBaseURL() + requestURI;
            WebClient webclient = WebClient.create(requestURI);

            webclient = webclient.replaceHeader("Authorization", OAuthClientUtils.createAuthorizationHeader(consumer, accessToken, "DELETE", appAuthConfig.getBaseURL() + baseRequestURI));
            webclient.delete();
        } catch (Exception e) {
            logger.error("Error getting information from OAuth Service", e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(bufferedReader);
        }
    }
}