package ca.openosp.openo.tickler.manager;

import ca.openosp.openo.test.base.OpenOTestBase;
import ca.openosp.openo.managers.TicklerManager;
import ca.openosp.openo.commn.dao.TicklerDao;
import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.model.Tickler;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.utility.LoggedInInfo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

/**
 * Simple integration test for TicklerManager using real Spring context.
 */
@DisplayName("Simple TicklerManager Integration Tests")
@Transactional
@ContextConfiguration(locations = {"classpath:test-context-mock-security.xml"})
class SimpleTicklerManagerTest extends OpenOTestBase {

    @Autowired
    private TicklerManager ticklerManager;

    @Autowired
    private TicklerDao ticklerDao;

    @Autowired
    private DemographicDao demographicDao;

    @Test
    @DisplayName("should load Spring context with all required beans")
    void shouldLoadSpringContextWithAllRequiredBeans() {
        assertThat(ticklerManager).isNotNull();
        assertThat(ticklerDao).isNotNull();
        assertThat(demographicDao).isNotNull();
    }


    /**
     * Tests the addition of a new tickler through the TicklerManager.
     *
     * <p>Validates the complete workflow of creating a tickler:</p>
     * <ol>
     *   <li>Create a new Tickler entity with required fields</li>
     *   <li>Set up LoggedInInfo with provider context</li>
     *   <li>Persist through TicklerManager.addTickler()</li>
     *   <li>Verify the tickler receives a generated ID</li>
     * </ol>
     *
     * @see TicklerManager#addTickler(LoggedInInfo, Tickler)
     */
    @Test
    @DisplayName("should add tickler successfully when valid data provided")
    void shouldAddTicklerSuccessfully_whenValidDataProvided() {
        // Given: Create a new tickler with required fields
        Tickler tickler = new Tickler();
        tickler.setDemographicNo(12345);
        tickler.setMessage("Test tickler message");
        tickler.setPriority(Tickler.PRIORITY.Normal);
        tickler.setTaskAssignedTo("999998");
        tickler.setCreator("999998");
        tickler.setServiceDate(new Date());
        tickler.setStatus(Tickler.STATUS.A);

        LoggedInInfo loggedInInfo = new LoggedInInfo();
        Provider provider = new Provider();
        provider.setProviderNo("999998");
        loggedInInfo.setLoggedInProvider(provider);

        // When
        boolean result = ticklerManager.addTickler(loggedInInfo, tickler);

        // Then
        assertTrue(result);
        assertThat(tickler.getId()).isNotNull();
    }
}