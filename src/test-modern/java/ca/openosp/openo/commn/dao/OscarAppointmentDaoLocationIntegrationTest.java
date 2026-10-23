package ca.openosp.openo.commn.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.AppointmentArchive;
import ca.openosp.openo.test.base.OpenOTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Integration tests for {@link OscarAppointmentDao#renameLocationFrom} and
 * {@link OscarAppointmentDao#countByLocationCodeFrom}: a renamed Location List location's
 * appointments from a date on take its new name, each archived first exactly as an edit archives
 * it, and are counted beforehand.
 *
 * @since 2026-10-16
 */
@DisplayName("OscarAppointmentDao location rename integration tests")
@Tag("integration")
@Tag("database")
@Tag("dao")
@Tag("update")
public class OscarAppointmentDaoLocationIntegrationTest extends OpenOTestBase {

    private static final int MIDTOWN = 41;
    private static final int RIVERSIDE = 42;
    private static final LocalDate RENAMED_ON = LocalDate.of(2026, 10, 20);

    @Autowired
    private OscarAppointmentDao appointmentDao;

    @Autowired
    private AppointmentArchiveDao appointmentArchiveDao;

    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    private Appointment appointment(LocalDate date, Integer locationCode, String location) {
        Appointment appointment = new Appointment();
        appointment.setProviderNo("999998");
        appointment.setAppointmentDate(java.sql.Date.valueOf(date));
        appointment.setStartTime(new Date());
        appointment.setEndTime(new Date());
        appointment.setName("Test,Patient");
        appointment.setStatus("t");
        appointment.setLocation(location);
        appointment.setLocationCode(locationCode);
        appointment.setLastUpdateUser("111111");
        entityManager.persist(appointment);
        return appointment;
    }

    private Appointment reload(Appointment appointment) {
        return entityManager.find(Appointment.class, appointment.getId());
    }

    private List<AppointmentArchive> archived(Appointment appointment) {
        return entityManager.createQuery("SELECT a FROM AppointmentArchive a WHERE a.appointmentNo = ?1", AppointmentArchive.class)
                .setParameter(1, appointment.getId())
                .getResultList();
    }

    @Test
    @DisplayName("should rename the location's appointments from the date on, as the user, after archiving each")
    void shouldRenameAndArchive_whenFromDateOrLater() {
        Appointment onTheDay = appointment(RENAMED_ON, MIDTOWN, "Midtown");
        Appointment later = appointment(RENAMED_ON.plusDays(7), MIDTOWN, "Midtown");
        entityManager.flush();
        Date before = new Date(System.currentTimeMillis() - 1000);

        int renamed = appointmentDao.renameLocationFrom(MIDTOWN, "Midtown Clinic", java.sql.Date.valueOf(RENAMED_ON), "999998");
        entityManager.clear();

        assertThat(renamed).isEqualTo(2);
        for (Appointment appointment : List.of(onTheDay, later)) {
            Appointment reloaded = reload(appointment);
            assertThat(reloaded.getLocation()).isEqualTo("Midtown Clinic");
            assertThat(reloaded.getLocationCode()).isEqualTo(MIDTOWN);
            assertThat(reloaded.getLastUpdateUser()).isEqualTo("999998");
            assertThat(reloaded.getUpdateDateTime()).isAfter(before);

            assertThat(archived(appointment)).singleElement().satisfies(archive -> {
                assertThat(archive.getLocation()).isEqualTo("Midtown");
                assertThat(archive.getLocationCode()).isEqualTo(MIDTOWN);
                assertThat(archive.getLastUpdateUser()).isEqualTo("111111");
                assertThat(archive.getName()).isEqualTo("Test,Patient");
            });
        }
    }

    @Test
    @DisplayName("should archive what archiveAppointment archives, so a column added to both tables can't be missed")
    void shouldArchiveLikeAnEdit_whenRenamed() {
        Appointment appointment = appointment(RENAMED_ON, MIDTOWN, "Midtown");
        appointment.setDemographicNo(5);
        appointment.setProgramId(6);
        appointment.setNotes("notes");
        appointment.setReason("reason");
        appointment.setResources("resources");
        appointment.setType("type");
        appointment.setStyle("style");
        appointment.setBilling("billing");
        appointment.setImportedStatus("imported");
        appointment.setCreator("creator");
        appointment.setRemarks("remarks");
        appointment.setUrgency("critical");
        appointment.setCreatorSecurityId(7);
        appointment.setBookingSource(Appointment.BookingSource.OSCAR);
        entityManager.flush();
        Integer byEdit = appointmentArchiveDao.archiveAppointment(appointment).getId();
        entityManager.flush();

        appointmentDao.renameLocationFrom(MIDTOWN, "Midtown Clinic", java.sql.Date.valueOf(RENAMED_ON), "999998");
        entityManager.clear();

        List<AppointmentArchive> archives = archived(appointment);
        assertThat(archives).hasSize(2);
        AppointmentArchive edit = archives.stream().filter(a -> a.getId().equals(byEdit)).findFirst().orElseThrow();
        AppointmentArchive rename = archives.stream().filter(a -> !a.getId().equals(byEdit)).findFirst().orElseThrow();
        assertThat(nullFields(edit)).as("archive fields the fixture leaves empty, where a missing column would go unseen").isEmpty();
        assertThat(edit.getLocationCode()).isEqualTo(MIDTOWN);
        assertThat(edit.getLocation()).isEqualTo("Midtown");
        assertThat(rename).usingRecursiveComparison().ignoringFields("id").isEqualTo(edit);
    }

    /* The archive's fields that hold nothing; primitives always hold a value. */
    private static List<String> nullFields(AppointmentArchive archive) {
        return Arrays.stream(AppointmentArchive.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(field -> ReflectionTestUtils.getField(archive, field.getName()) == null)
                .map(Field::getName)
                .toList();
    }

    @Test
    @DisplayName("should leave earlier appointments and other locations as they were")
    void shouldLeaveAlone_whenEarlierOrElsewhere() {
        Appointment earlier = appointment(RENAMED_ON.minusDays(1), MIDTOWN, "Midtown");
        Appointment elsewhere = appointment(RENAMED_ON.plusDays(1), RIVERSIDE, "Riverside");
        Appointment typed = appointment(RENAMED_ON.plusDays(1), null, "Midtown");
        entityManager.flush();

        int renamed = appointmentDao.renameLocationFrom(MIDTOWN, "Midtown Clinic", java.sql.Date.valueOf(RENAMED_ON), "999998");
        entityManager.clear();

        assertThat(renamed).isZero();
        assertThat(reload(earlier).getLocation()).isEqualTo("Midtown");
        assertThat(reload(elsewhere).getLocation()).isEqualTo("Riverside");
        assertThat(reload(typed).getLocation()).isEqualTo("Midtown");
        assertThat(reload(earlier).getLastUpdateUser()).isEqualTo("111111");
        for (Appointment appointment : List.of(earlier, elsewhere, typed)) {
            assertThat(archived(appointment)).isEmpty();
        }
    }

    @Test
    @DisplayName("should count each location's appointments from the date on, leaving out typed locations")
    void shouldCountByLocation_whenFromDateOrLater() {
        appointment(RENAMED_ON.minusDays(1), MIDTOWN, "Midtown");
        appointment(RENAMED_ON, MIDTOWN, "Midtown");
        appointment(RENAMED_ON.plusDays(30), MIDTOWN, "Midtown");
        appointment(RENAMED_ON.plusDays(1), RIVERSIDE, "Riverside");
        appointment(RENAMED_ON.plusDays(1), null, "Front desk");
        entityManager.flush();

        assertThat(appointmentDao.countByLocationCodeFrom(java.sql.Date.valueOf(RENAMED_ON)))
                .containsOnly(entry(MIDTOWN, 2L),
                        entry(RIVERSIDE, 1L));
    }
}
