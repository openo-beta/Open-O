package ca.openosp.openo.webserv.transfer_objects;

import ca.openosp.openo.commn.model.Appointment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for how {@link AppointmentTransfer#copyTo} saves a SOAP client's location: the
 * location code is read-only, and a changed location text clears it.
 *
 * @since 2026-09-22
 */
@DisplayName("AppointmentTransfer unit tests")
@Tag("unit")
@Tag("fast")
public class AppointmentTransferUnitTest {

    private final Appointment appointment = new Appointment();
    private final AppointmentTransfer transfer = new AppointmentTransfer();

    @BeforeEach
    void codedAppointment() {
        appointment.setLocation("Clinic A");
        appointment.setLocationCode(5);
        transfer.setLocation("Clinic A");
        transfer.setLocationCode(5);
    }

    @Test
    @DisplayName("should keep the code when the location text is unchanged")
    void shouldKeepCode_whenLocationUnchanged() {
        transfer.copyTo(appointment);

        assertThat(appointment.getLocation()).isEqualTo("Clinic A");
        assertThat(appointment.getLocationCode()).isEqualTo(5);
    }

    @Test
    @DisplayName("should save the new location text and clear the code when the text changed")
    void shouldClearCode_whenLocationChanged() {
        transfer.setLocation("Room 12");

        transfer.copyTo(appointment);

        assertThat(appointment.getLocation()).isEqualTo("Room 12");
        assertThat(appointment.getLocationCode()).isNull();
    }

    @Test
    @DisplayName("should ignore a location code sent by the client")
    void shouldIgnoreCode_whenClientSendsOne() {
        transfer.setLocationCode(99);

        transfer.copyTo(appointment);

        assertThat(appointment.getLocationCode()).isEqualTo(5);
    }
}
