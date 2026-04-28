package ca.openosp.openo.consultation.dao;

import ca.openosp.openo.commn.dao.ProfessionalSpecialistDao;
import ca.openosp.openo.commn.model.ProfessionalSpecialist;
import ca.openosp.openo.consultation.dto.SpecialistListDTO;
import ca.openosp.openo.test.base.OpenOTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProfessionalSpecialistDao#findAllListDTOs()}.
 *
 * <p>Validates the JPQL constructor projection that provides lightweight
 * specialist data for list views, avoiding full entity hydration.</p>
 *
 * @since 2026-03-11
 * @see ProfessionalSpecialistDao
 * @see SpecialistListDTO
 */
@DisplayName("ProfessionalSpecialist DAO DTO Projection Integration Tests")
@Tag("integration")
@Tag("database")
@Tag("dao")
@Tag("slow")
@Tag("read")
@Tag("dto")
@Transactional
public class ProfessionalSpecialistDaoDTOIntegrationTest extends OpenOTestBase {

    @Autowired
    private ProfessionalSpecialistDao professionalSpecialistDao;

    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        ProfessionalSpecialist spec1 = new ProfessionalSpecialist();
        spec1.setFirstName("Alice");
        spec1.setLastName("Smith");
        spec1.setProfessionalLetters("MD, FRCPC");
        spec1.setStreetAddress("123 Main St");
        spec1.setPhoneNumber("555-1234");
        spec1.setFaxNumber("555-5678");
        entityManager.persist(spec1);

        ProfessionalSpecialist spec2 = new ProfessionalSpecialist();
        spec2.setFirstName("Bob");
        spec2.setLastName("Jones");
        spec2.setStreetAddress("456 Oak Ave");
        spec2.setPhoneNumber("555-9999");
        entityManager.persist(spec2);

        ProfessionalSpecialist spec3 = new ProfessionalSpecialist();
        spec3.setFirstName("Carol");
        spec3.setLastName("Adams");
        entityManager.persist(spec3);

        entityManager.flush();
    }

    @Test
    @DisplayName("should return all specialists as DTOs")
    void shouldReturnAllSpecialistsAsDTOs() {
        List<SpecialistListDTO> results = professionalSpecialistDao.findAllListDTOs();

        assertThat(results).hasSize(3);
    }

    @Test
    @Tag("query")
    @DisplayName("should order by lastName then firstName")
    void shouldOrderByLastNameThenFirstName() {
        List<SpecialistListDTO> results = professionalSpecialistDao.findAllListDTOs();

        assertThat(results).extracting(SpecialistListDTO::getLastName)
                .containsExactly("Adams", "Jones", "Smith");
    }

    @Test
    @Tag("query")
    @DisplayName("should populate all DTO fields correctly")
    void shouldPopulateAllDTOFields_correctly() {
        List<SpecialistListDTO> results = professionalSpecialistDao.findAllListDTOs();

        SpecialistListDTO alice = results.stream()
                .filter(dto -> "Smith".equals(dto.getLastName()))
                .findFirst().orElse(null);

        assertThat(alice).isNotNull();
        assertThat(alice.getId()).isNotNull();
        assertThat(alice.getFirstName()).isEqualTo("Alice");
        assertThat(alice.getLastName()).isEqualTo("Smith");
        assertThat(alice.getProfessionalLetters()).isEqualTo("MD, FRCPC");
        assertThat(alice.getStreetAddress()).isEqualTo("123 Main St");
        assertThat(alice.getPhoneNumber()).isEqualTo("555-1234");
        assertThat(alice.getFaxNumber()).isEqualTo("555-5678");
    }

    @Test
    @Tag("query")
    @DisplayName("should handle null optional fields gracefully")
    void shouldHandleNullOptionalFields_gracefully() {
        List<SpecialistListDTO> results = professionalSpecialistDao.findAllListDTOs();

        SpecialistListDTO carol = results.stream()
                .filter(dto -> "Adams".equals(dto.getLastName()))
                .findFirst().orElse(null);

        assertThat(carol).isNotNull();
        assertThat(carol.getProfessionalLetters()).isNull();
        assertThat(carol.getStreetAddress()).isNull();
        assertThat(carol.getPhoneNumber()).isNull();
        assertThat(carol.getFaxNumber()).isNull();
    }

    @Test
    @DisplayName("should return empty list when no specialists exist")
    void shouldReturnEmptyList_whenNoSpecialistsExist() {
        entityManager.createQuery("DELETE FROM ProfessionalSpecialist").executeUpdate();
        entityManager.flush();

        List<SpecialistListDTO> results = professionalSpecialistDao.findAllListDTOs();

        assertThat(results).isEmpty();
    }
}
