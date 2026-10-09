package ca.openosp.openo.commn.dao;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.AppointmentArchive;
import ca.openosp.openo.test.base.OpenOTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AppointmentArchiveDao#archiveAppointment}: an edited appointment's
 * archived copy keeps its Location List choice.
 *
 * @since 2026-09-15
 */
@DisplayName("AppointmentArchiveDao integration tests")
@Tag("integration")
@Tag("database")
@Tag("dao")
@Tag("create")
public class AppointmentArchiveDaoIntegrationTest extends OpenOTestBase {

    @Autowired
    private AppointmentArchiveDao appointmentArchiveDao;

    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    @Test
    @DisplayName("should copy the location code and label when archiving")
    void shouldCopyLocationCode_whenArchiving() {
        Appointment appointment = new Appointment();
        appointment.setProviderNo("999998");
        appointment.setAppointmentDate(new Date());
        appointment.setStartTime(new Date());
        appointment.setEndTime(new Date());
        appointment.setName("Test,Patient");
        appointment.setStatus("t");
        appointment.setLocation("Room 2");
        appointment.setLocationCode(42);
        entityManager.persist(appointment);

        AppointmentArchive archived = appointmentArchiveDao.archiveAppointment(appointment);
        entityManager.flush();
        entityManager.clear();

        AppointmentArchive reloaded = entityManager.find(AppointmentArchive.class, archived.getId());
        assertThat(reloaded.getAppointmentNo()).isEqualTo(appointment.getId());
        assertThat(reloaded.getLocationCode()).isEqualTo(42);
        assertThat(reloaded.getLocation()).isEqualTo("Room 2");
    }
}
