package oscar.login;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * Handles parsing and validation of OAuth request parameters.
 */
public class OAuthRequestParams {
    private final String consumerKey;
    private final String callbackUrl;

    private OAuthRequestParams(String consumerKey, String callbackUrl) {
        this.consumerKey = consumerKey;
        this.callbackUrl = callbackUrl;
    }

    /**
     * Parses and validates OAuth parameters from the HTTP request.
     * 
     * @param request The HTTP servlet request
     * @return OAuthRequestParams if valid, null if validation fails
     * @throws OAuthParameterException if required parameters are missing or invalid
     */
    public static OAuthRequestParams parseAndValidate(HttpServletRequest request) 
            throws OAuthParameterException {
        String consumerKey = request.getParameter("oauth_consumer_key");
        String callbackUrl = request.getParameter("oauth_callback");

        // Validate required parameters
        if (consumerKey == null || consumerKey.trim().isEmpty()) {
            throw new OAuthParameterException(Response.Status.BAD_REQUEST, 
                "parameter_absent", "oauth_consumer_key");
        }

        if (callbackUrl == null || callbackUrl.trim().isEmpty()) {
            throw new OAuthParameterException(Response.Status.BAD_REQUEST, 
                "parameter_absent", "oauth_callback");
        }

        return new OAuthRequestParams(consumerKey, callbackUrl);
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Exception thrown when OAuth parameter validation fails.
     */
    public static class OAuthParameterException extends Exception {
        private final Response.Status status;
        private final String problem;
        private final String advice;

        public OAuthParameterException(Response.Status status, String problem, String advice) {
            super("OAuth parameter validation failed: " + problem);
            this.status = status;
            this.problem = problem;
            this.advice = advice;
        }

        public Response.Status getStatus() { return status; }
        public String getProblem() { return problem; }
        public String getAdvice() { return advice; }
    }
}
