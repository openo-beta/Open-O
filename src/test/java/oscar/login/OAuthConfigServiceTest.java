package oscar.login;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.oscarehr.app.AppOAuth1Config;
import org.oscarehr.common.dao.AppDefinitionDao;
import org.oscarehr.common.model.AppDefinition;

@RunWith(MockitoJUnitRunner.class)
public class OAuthConfigServiceTest {

    @Mock
    private AppDefinitionDao appDefinitionDao;

    @InjectMocks
    private OAuthConfigService configService;

    private AppDefinition testApp;
    private AppOAuth1Config testConfig;

    @Before
    public void setUp() {
        testApp = new AppDefinition();
        testApp.setId(1);
        testApp.setName("test-app");
        testApp.setConfig("<config><consumerKey>test-consumer</consumerKey><consumerSecret>test-secret</consumerSecret><baseURL>http://localhost:8080</baseURL></config>");

        testConfig = new AppOAuth1Config();
        // Note: AppOAuth1Config would need to be mocked or have proper fromDocument implementation
    }

    @Test
    public void testGetOAuthConfiguration_Found() {
        // Arrange
        when(appDefinitionDao.findByConsumerKey("test-consumer")).thenReturn(testApp);

        // Act
        AppOAuth1Config config = configService.getOAuthConfiguration("test-consumer");

        // Assert
        assertNotNull(config);
        verify(appDefinitionDao).findByConsumerKey("test-consumer");
    }

    @Test
    public void testGetOAuthConfiguration_NotFound() {
        // Arrange
        when(appDefinitionDao.findByConsumerKey("unknown-consumer")).thenReturn(null);

        // Act
        AppOAuth1Config config = configService.getOAuthConfiguration("unknown-consumer");

        // Assert
        assertNull(config);
        verify(appDefinitionDao).findByConsumerKey("unknown-consumer");
    }

    @Test
    public void testIsValidCallback_OobCallback() {
        // Arrange
        AppOAuth1Config config = new AppOAuth1Config();

        // Act & Assert
        assertTrue(configService.isValidCallback(config, "oob"));
    }

    @Test
    public void testIsValidCallback_NoRestrictionsConfigured() {
        // Arrange
        AppOAuth1Config config = new AppOAuth1Config();

        // Act & Assert
        assertTrue(configService.isValidCallback(config, "http://localhost:8080/callback"));
    }
}
