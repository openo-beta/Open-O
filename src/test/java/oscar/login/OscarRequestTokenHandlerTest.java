package oscar.login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.scribejava.core.model.OAuth1RequestToken;

/**
 * Unit tests for OscarRequestTokenHandler.
 */
@ExtendWith(MockitoExtension.class)
class OscarRequestTokenHandlerTest {

    @Mock
    private OscarOAuthDataProvider dataProvider;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @InjectMocks
    private OscarRequestTokenHandler handler;
    
    private Object mockApp;
    private Map<String, Object> validConfig;
    
    @BeforeEach
    void setUp() {
        // Create mock app with getConfig method
        mockApp = new Object() {
            public Map<String, Object> getConfig() {
                return validConfig;
            }
        };
        
        // Set up valid configuration
        validConfig = new HashMap<>();
        validConfig.put("oauth.consumer.key", "test-consumer-key");
        validConfig.put("oauth.consumer.secret", "test-consumer-secret");
        validConfig.put("oauth.base.url", "https://example.com");
        validConfig.put("oauth.callback.uri", "https://myapp.com/callback");
        validConfig.put("oauth.application.uri", "https://myapp.com");
    }
    
    @Test
    void testHandleRequestToken_NullRequest_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            handler.handleRequestToken(null, response, mockApp, "https://myapp.com/callback");
        });
    }
    
    @Test
    void testHandleRequestToken_NullResponse_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            handler.handleRequestToken(request, null, mockApp, "https://myapp.com/callback");
        });
    }
    
    @Test
    void testHandleRequestToken_NullApp_SendsError() throws IOException {
        handler.handleRequestToken(request, response, null, "https://myapp.com/callback");
        
        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing application configuration");
    }
    
    @Test
    void testHandleRequestToken_MissingConsumerKey_SendsError() throws IOException {
        validConfig.remove("oauth.consumer.key");
        
        handler.handleRequestToken(request, response, mockApp, "https://myapp.com/callback");
        
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Incomplete OAuth configuration"));
    }
    
    @Test
    void testHandleRequestToken_MissingConsumerSecret_SendsError() throws IOException {
        validConfig.remove("oauth.consumer.secret");
        
        handler.handleRequestToken(request, response, mockApp, "https://myapp.com/callback");
        
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Incomplete OAuth configuration"));
    }
    
    @Test
    void testHandleRequestToken_InvalidCallbackUrl_SendsError() throws IOException {
        handler.handleRequestToken(request, response, mockApp, "invalid-url");
        
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("Invalid callback URL"));
    }
    
    @Test
    void testHandleRequestToken_EmptyCallbackUrl_SendsError() throws IOException {
        handler.handleRequestToken(request, response, mockApp, "");
        
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("Invalid callback URL"));
    }
    
    @Test
    void testHandleRequestToken_NullCallbackUrl_SendsError() throws IOException {
        handler.handleRequestToken(request, response, mockApp, null);
        
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("Invalid callback URL"));
    }
    
    @Test
    void testValidateCallbackURL_ValidCallback_NoException() {
        OscarRequestTokenHandler.AppOAuth1Config config = new OscarRequestTokenHandler.AppOAuth1Config();
        config.setCallbackURI("https://myapp.com/callback");
        
        assertDoesNotThrow(() -> {
            handler.validateCallbackURL(config, "https://myapp.com/callback");
        });
    }
    
    @Test
    void testValidateCallbackURL_ValidApplicationUriPrefix_NoException() {
        OscarRequestTokenHandler.AppOAuth1Config config = new OscarRequestTokenHandler.AppOAuth1Config();
        config.setApplicationURI("https://myapp.com");
        
        assertDoesNotThrow(() -> {
            handler.validateCallbackURL(config, "https://myapp.com/oauth/callback");
        });
    }
    
    @Test
    void testValidateCallbackURL_InvalidCallback_ThrowsException() {
        OscarRequestTokenHandler.AppOAuth1Config config = new OscarRequestTokenHandler.AppOAuth1Config();
        config.setCallbackURI("https://myapp.com/callback");
        config.setApplicationURI("https://myapp.com");
        
        assertThrows(IllegalArgumentException.class, () -> {
            handler.validateCallbackURL(config, "https://malicious.com/callback");
        });
    }
    
    @Test
    void testAppOAuth1Config_FromDocument_ValidMap() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("oauth.consumer.key", "test-key");
        configMap.put("oauth.consumer.secret", "test-secret");
        configMap.put("oauth.base.url", "https://example.com");
        
        OscarRequestTokenHandler.AppOAuth1Config config = 
            OscarRequestTokenHandler.AppOAuth1Config.fromDocument(configMap);
        
        assertNotNull(config);
        assertEquals("test-key", config.getConsumerKey());
        assertEquals("test-secret", config.getConsumerSecret());
        assertEquals("https://example.com", config.getBaseUrl());
    }
    
    @Test
    void testAppOAuth1Config_FromDocument_MissingRequiredFields_ReturnsNull() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("oauth.consumer.key", "test-key");
        // Missing consumer secret
        
        OscarRequestTokenHandler.AppOAuth1Config config = 
            OscarRequestTokenHandler.AppOAuth1Config.fromDocument(configMap);
        
        assertNull(config);
    }
    
    @Test
    void testAppOAuth1Config_FromDocument_NullDocument_ReturnsNull() {
        OscarRequestTokenHandler.AppOAuth1Config config = 
            OscarRequestTokenHandler.AppOAuth1Config.fromDocument(null);
        
        assertNull(config);
    }
    
    @Test
    void testStoreRequestToken_DataProviderFailure_ThrowsException() throws Exception {
        OAuth1RequestToken requestToken = new OAuth1RequestToken("token", "secret");
        
        doThrow(new RuntimeException("Database error")).when(dataProvider)
            .createRequestToken(anyString(), anyString(), any());
        
        assertThrows(RuntimeException.class, () -> {
            handler.storeRequestToken(requestToken, "https://myapp.com/callback");
        });
    }
    
    @Test
    void testSetTokenLifetime() {
        handler.setTokenLifetime(7200L);
        // No direct way to verify, but method should not throw
    }
    
    @Test
    void testSetDefaultScope() {
        handler.setDefaultScope("read,write");
        // No direct way to verify, but method should not throw
    }
}
