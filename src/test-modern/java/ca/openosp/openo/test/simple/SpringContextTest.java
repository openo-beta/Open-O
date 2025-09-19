package ca.openosp.openo.test.simple;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify Spring context loading works
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:test-minimal-context.xml"})
@DisplayName("Spring Context Test")
public class SpringContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Test Spring context loads")
    public void testContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded");
    }

    @Test
    @DisplayName("Test DataSource is available")
    public void testDataSourceAvailable() {
        assertNotNull(dataSource, "DataSource should be autowired");
    }

    @Test
    @DisplayName("Test bean retrieval")
    public void testBeanRetrieval() {
        String testString = applicationContext.getBean("testString", String.class);
        assertEquals("Test String Bean", testString);
    }
}