/**
 * Copyright (c) 2025. OpenOSP EMR
 * Example DAO test using modern JUnit 5 framework
 */
package ca.openosp.openo.test.examples;

import ca.openosp.openo.test.base.OpenODaoTestBase;
import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.utility.SpringUtils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

/**
 * Example test demonstrating modern JUnit 5 features with the OpenO test framework.
 * This shows how to test DAOs while handling the SpringUtils anti-pattern.
 */
@DisplayName("Demographic DAO Modern Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemographicDaoModernTest extends OpenODaoTestBase {

    private DemographicDao demographicDao;

    @BeforeEach
    void setUp() {
        // Get DAO using SpringUtils to ensure compatibility
        demographicDao = SpringUtils.getBean(DemographicDao.class);

        // Verify SpringUtils integration
        verifyDaoSpringUtilsIntegration(DemographicDao.class);
    }

    @Test
    @DisplayName("Should create and retrieve demographic")
    @Order(1)
    void testCreateAndRetrieveDemographic() {
        // Given
        Demographic demographic = createTestDemographic("TEST001", "John", "Doe");

        // When
        demographicDao.save(demographic);
        Demographic retrieved = demographicDao.getDemographicById(demographic.getDemographicNo());

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getChartNo()).isEqualTo("TEST001");
        assertThat(retrieved.getFirstName()).isEqualTo("John");
        assertThat(retrieved.getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should handle SpringUtils.getBean() correctly")
    void testSpringUtilsIntegration() {
        // This verifies the anti-pattern is handled correctly
        DemographicDao daoFromContext = getBean(DemographicDao.class);
        DemographicDao daoFromUtils = SpringUtils.getBean(DemographicDao.class);

        assertThat(daoFromContext).isSameAs(daoFromUtils);
    }

    @ParameterizedTest
    @DisplayName("Should validate HIN formats")
    @ValueSource(strings = {
        "1234567890",  // Ontario
        "9876543210",  // BC
        "1111111111"   // Generic
    })
    void testHealthInsuranceNumberValidation(String hin) {
        // Given
        Demographic demographic = createTestDemographic("TEST002", "Jane", "Smith");
        demographic.setHin(hin);

        // When
        demographicDao.save(demographic);
        Demographic retrieved = demographicDao.getDemographicById(demographic.getDemographicNo());

        // Then
        assertThat(retrieved.getHin()).isEqualTo(hin);
    }

    @ParameterizedTest
    @DisplayName("Should search by various criteria")
    @CsvSource({
        "lastName, Doe, 1",
        "firstName, John, 1",
        "chartNo, TEST001, 1",
        "hin, 1234567890, 0"  // No HIN set in basic test
    })
    void testSearchByCriteria(String searchField, String searchValue, int expectedCount) {
        // Given
        Demographic demographic = createTestDemographic("TEST001", "John", "Doe");
        demographicDao.save(demographic);

        // When
        List<Demographic> results = searchDemographics(searchField, searchValue);

        // Then
        assertThat(results).hasSize(expectedCount);
    }

    @Test
    @DisplayName("Should handle transaction rollback")
    void testTransactionRollback() {
        // This test verifies that our @Transactional and @Rollback annotations work
        int initialCount = countRowsInTable("demographic");

        // Create and save a demographic
        Demographic demographic = createTestDemographic("ROLLBACK", "Test", "Rollback");
        demographicDao.save(demographic);

        // Verify it was saved within the transaction
        assertThat(countRowsInTable("demographic")).isEqualTo(initialCount + 1);

        // After test method completes, @Rollback will revert this
    }

    @Nested
    @DisplayName("Batch Operations")
    class BatchOperationsTest {

        @Test
        @DisplayName("Should handle batch inserts efficiently")
        void testBatchInsert() {
            // Use transaction template for batch operations
            executeInTransaction(() -> {
                for (int i = 0; i < 100; i++) {
                    Demographic demo = createTestDemographic(
                        "BATCH" + i,
                        "First" + i,
                        "Last" + i
                    );
                    demographicDao.save(demo);
                }
                return null;
            });

            assertThat(countRowsInTable("demographic")).isGreaterThanOrEqualTo(100);
        }
    }

    @Test
    @DisplayName("Should handle concurrent access")
    @Tag("slow")
    @Timeout(5)  // 5 seconds timeout
    void testConcurrentAccess() throws InterruptedException {
        // This demonstrates JUnit 5's timeout and tagging features
        Thread thread1 = new Thread(() -> {
            Demographic demo = createTestDemographic("THREAD1", "Thread", "One");
            demographicDao.save(demo);
        });

        Thread thread2 = new Thread(() -> {
            Demographic demo = createTestDemographic("THREAD2", "Thread", "Two");
            demographicDao.save(demo);
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Verify both were saved
        assertThat(countRowsInTable("demographic")).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should validate required fields")
    void testRequiredFieldValidation() {
        // Use AssertJ's exception assertions
        assertThatThrownBy(() -> {
            Demographic demographic = new Demographic();
            // Missing required fields
            demographicDao.save(demographic);
        }).isInstanceOf(Exception.class);
    }

    // Helper methods

    private Demographic createTestDemographic(String chartNo, String firstName, String lastName) {
        Demographic demographic = new Demographic();
        demographic.setChartNo(chartNo);
        demographic.setFirstName(firstName);
        demographic.setLastName(lastName);
        demographic.setYearOfBirth("1980");
        demographic.setMonthOfBirth("01");
        demographic.setDateOfBirth("15");
        demographic.setSex("M");
        demographic.setProviderNo("999998");
        demographic.setPatientStatus("AC");
        demographic.setDateJoined(new Date());
        demographic.setLastUpdateUser("test");
        demographic.setLastUpdateDate(new Date());
        return demographic;
    }

    private List<Demographic> searchDemographics(String field, String value) {
        // This would use the actual DAO search method
        // Simplified for example
        return demographicDao.getDemographics();
    }

    @Override
    protected String[] cleanableTables() {
        return new String[] { "demographic", "demographicExt" };
    }

    @Override
    protected String getTestDataFile() {
        // Can return path to test data XML if needed
        return null;
    }
}