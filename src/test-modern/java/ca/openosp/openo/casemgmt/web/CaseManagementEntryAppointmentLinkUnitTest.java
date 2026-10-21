package ca.openosp.openo.casemgmt.web;

import ca.openosp.openo.commn.dao.OscarAppointmentDao;
import ca.openosp.openo.commn.model.Appointment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CaseManagementEntry2Action#appointmentNoForNote(String, String, OscarAppointmentDao)},
 * which picks the appointment a note saved from the encounter is linked to.
 *
 * <p>Signing or verifying a linked note adds the S or V to that appointment's status on the
 * schedule. The appointment number comes from the encounter window's own form, not from the
 * session, so two encounters open at once can't mark each other's appointments. A posted
 * appointment is only linked when it belongs to the patient whose note is being saved.</p>
 */
@DisplayName("Encounter note save links the note to its own appointment")
@Tag("unit")
@Tag("fast")
@Tag("casemgmt")
class CaseManagementEntryAppointmentLinkUnitTest {

    private static final int APPOINTMENT_NO = 531;
    private static final int PATIENT = 2;
    private static final int OTHER_PATIENT = 12;

    private OscarAppointmentDao appointmentDao;

    @BeforeEach
    void setUp() {
        appointmentDao = mock(OscarAppointmentDao.class);
    }

    @Test
    @DisplayName("should link the posted appointment when it belongs to the note's patient")
    void shouldLinkPostedAppointment_whenItBelongsToThePatient() {
        when(appointmentDao.find(APPOINTMENT_NO)).thenReturn(appointmentFor(PATIENT));

        int linked = CaseManagementEntry2Action.appointmentNoForNote(String.valueOf(APPOINTMENT_NO), String.valueOf(PATIENT), appointmentDao);

        assertThat(linked).isEqualTo(APPOINTMENT_NO);
    }

    @Test
    @DisplayName("should not link the posted appointment when it belongs to another patient")
    void shouldNotLink_whenAppointmentBelongsToAnotherPatient() {
        when(appointmentDao.find(APPOINTMENT_NO)).thenReturn(appointmentFor(OTHER_PATIENT));

        int linked = CaseManagementEntry2Action.appointmentNoForNote(String.valueOf(APPOINTMENT_NO), String.valueOf(PATIENT), appointmentDao);

        assertThat(linked).isZero();
    }

    @Test
    @DisplayName("should not link when the posted appointment does not exist")
    void shouldNotLink_whenAppointmentNotFound() {
        when(appointmentDao.find(APPOINTMENT_NO)).thenReturn(null);

        int linked = CaseManagementEntry2Action.appointmentNoForNote(String.valueOf(APPOINTMENT_NO), String.valueOf(PATIENT), appointmentDao);

        assertThat(linked).isZero();
    }

    @Test
    @DisplayName("should not link or look anything up when the encounter has no appointment")
    void shouldNotLink_whenNoAppointmentPosted() {
        for (String posted : new String[]{null, "", "0", "null", "-5", "abc"}) {
            assertThat(CaseManagementEntry2Action.appointmentNoForNote(posted, String.valueOf(PATIENT), appointmentDao))
                    .as("posted appointmentNo %s", posted)
                    .isZero();
        }
        verify(appointmentDao, never()).find(anyInt());
    }

    private static Appointment appointmentFor(int demographicNo) {
        Appointment appointment = new Appointment();
        appointment.setId(APPOINTMENT_NO);
        appointment.setDemographicNo(demographicNo);
        return appointment;
    }
}
