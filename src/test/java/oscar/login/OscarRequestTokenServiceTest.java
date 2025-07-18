package oscar.login;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.oscarehr.app.AppOAuth1Config;

@RunWith(MockitoJUnitRunner.class)
public class OscarRequestTokenServiceTest {

    @Mock
    private OscarOAuthDataProvider dataProvider;

    @Mock
    private OAuthConfigService configService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private OscarRequestTokenService tokenService;

    private AppOAuth1Config testConfig;

    @Before
    public void setUp() {
        testConfig = new AppOAuth1Config();
        // Note: Would need proper setup of AppOAuth1Config
    }

    @Test
    public void testGetRequestToken_MissingConsumerKey() {
        // Arrange
        when(request.getParameter("oauth_consumer_key")).thenReturn(null);
        when(request.getParameter("oauth_callback")).thenReturn("http://localhost:8080/callback");

        // Act
        Response result = tokenService.getRequestToken(request, response);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatus());
        assertTrue(result.getEntity().toString().contains("parameter_absent"));
    }

    @Test
    public void testGetRequestToken_MissingCallback() {
        // Arrange
        when(request.getParameter("oauth_consumer_key")).thenReturn("test-consumer");
        when(request.getParameter("oauth_callback")).thenReturn(null);

        // Act
        Response result = tokenService.getRequestToken(request, response);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatus());
        assertTrue(result.getEntity().toString().contains("parameter_absent"));
    }

    @Test
    public void testGetRequestToken_UnknownConsumer() {
        // Arrange
        when(request.getParameter("oauth_consumer_key")).thenReturn("unknown-consumer");
        when(request.getParameter("oauth_callback")).thenReturn("http://localhost:8080/callback");
        when(configService.getOAuthConfiguration("unknown-consumer")).thenReturn(null);

        // Act
        Response result = tokenService.getRequestToken(request, response);

        // Assert
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), result.getStatus());
        assertTrue(result.getEntity().toString().contains("consumer_key_unknown"));
    }
}
