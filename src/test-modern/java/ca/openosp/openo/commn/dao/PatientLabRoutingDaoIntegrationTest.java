package ca.openosp.openo.commn.dao;

import java.util.Arrays;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ca.openosp.openo.commn.model.PatientLabRouting;
import ca.openosp.openo.test.base.OpenOTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Integration tests for {@link PatientLabRoutingDao#findByLabNos}: labs only, never a document
 * routed under the same number.
 *
 * <p>The shared test context mocks {@link PatientLabRoutingDao} for the tickler action tests, so
 * these tests build the real DAO on the test's EntityManager.</p>
 *
 * @since 2026-11-24
 */
@DisplayName("PatientLabRoutingDao integration tests")
@Tag("integration")
@Tag("database")
@Tag("dao")
@Tag("read")
@Tag("query")
public class PatientLabRoutingDaoIntegrationTest extends OpenOTestBase {

    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    private PatientLabRoutingDao patientLabRoutingDao;

    @BeforeEach
    void setUp() {
        PatientLabRoutingDaoImpl dao = new PatientLabRoutingDaoImpl();
        ReflectionTestUtils.setField(dao, "entityManager", entityManager);
        patientLabRoutingDao = dao;
    }

    private void routing(int labNo, String labType, Integer demographicNo) {
        entityManager.persist(new PatientLabRouting(labNo, labType, demographicNo));
    }

    @Test
    @DisplayName("should return only the lab when a document has the same number")
    void shouldReturnOnlyLab_whenDocumentSharesLabNumber() {
        // The document is saved first, so it would be the first row returned without the filter
        routing(3, "DOC", 5);
        routing(3, "HL7", 1);
        entityManager.flush();
        entityManager.clear();

        assertThat(patientLabRoutingDao.findByLabNos(Collections.singletonList(3)))
                .extracting(PatientLabRouting::getLabNo, PatientLabRouting::getLabType)
                .containsExactly(tuple(3, "HL7"));
    }

    @Test
    @DisplayName("should return the labs asked for and leave out document-only numbers")
    void shouldReturnRequestedLabs_whenSeveralLabNumbersGiven() {
        routing(3, "HL7", 1);
        routing(55, "MDS", 1);
        routing(79, "DOC", 3);
        routing(80, "HL7", 1);
        entityManager.flush();
        entityManager.clear();

        assertThat(patientLabRoutingDao.findByLabNos(Arrays.asList(3, 55, 79)))
                .extracting(PatientLabRouting::getLabNo, PatientLabRouting::getLabType)
                .containsExactlyInAnyOrder(tuple(3, "HL7"), tuple(55, "MDS"));
    }

    @Test
    @DisplayName("should return no rows when no lab numbers are given")
    void shouldReturnEmptyList_whenNoLabNumbersGiven() {
        routing(3, "HL7", 1);
        entityManager.flush();

        assertThat(patientLabRoutingDao.findByLabNos(Collections.emptyList())).isEmpty();
        assertThat(patientLabRoutingDao.findByLabNos(null)).isEmpty();
    }
}
