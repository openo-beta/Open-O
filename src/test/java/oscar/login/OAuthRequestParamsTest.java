package oscar.login;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;

import oscar.login.OAuthRequestParams.OAuthParameterException;

public class OAuthRequestParamsTest {

    @Test
    public void testParseAndValidate_Success() throws OAuthParameterException {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("oauth_consumer_key")).thenReturn("test-consumer");
        when(request.getParameter("oauth_callback")).thenReturn("http://localhost:8080/callback");

        // Act
        OAuthRequestParams params = OAuthRequestParams.parseAndValidate(request);

        // Assert
        assertNotNull(params);
        assertEquals("test-consumer", params.getConsumerKey());
        assertEquals("http://localhost:8080/callback", params.getCallbackUrl());
    }

    @Test(expected = OAuthParameterException.class)
    public void testParseAndValidate_MissingConsumerKey() throws OAuthParameterException {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("oauth_consumer_key")).thenReturn(null);
        when(request.getParameter("oauth_callback")).thenReturn("http://localhost:8080/callback");

        // Act
        OAuthRequestParams.parseAndValidate(request);
    }

    @Test(expected = OAuthParameterException.class)
    public void testParseAndValidate_EmptyConsumerKey() throws OAuthParameterException {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("oauth_consumer_key")).thenReturn("");
        when(request.getParameter("oauth_callback")).thenReturn("http://localhost:8080/callback");

        // Act
        OAuthRequestParams.parseAndValidate(request);
    }

    @Test(expected = OAuthParameterException.class)
    public void testParseAndValidate_MissingCallback() throws OAuthParameterException {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("oauth_consumer_key")).thenReturn("test-consumer");
        when(request.getParameter("oauth_callback")).thenReturn(null);

        // Act
        OAuthRequestParams.parseAndValidate(request);
    }

    @Test
    public void testOAuthParameterException_Properties() {
        // Arrange & Act
        OAuthParameterException exception = new OAuthParameterException(
            Response.Status.BAD_REQUEST, "parameter_absent", "oauth_consumer_key");

        // Assert
        assertEquals(Response.Status.BAD_REQUEST, exception.getStatus());
        assertEquals("parameter_absent", exception.getProblem());
        assertEquals("oauth_consumer_key", exception.getAdvice());
        assertTrue(exception.getMessage().contains("parameter_absent"));
    }
}
