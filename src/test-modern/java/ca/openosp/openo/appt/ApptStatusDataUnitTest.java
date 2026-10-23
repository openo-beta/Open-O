package ca.openosp.openo.appt;

import ca.openosp.openo.appt.status.service.impl.AppointmentStatusCacheExtension;
import ca.openosp.openo.appt.status.service.impl.AppointmentStatusMgrImpl;
import ca.openosp.openo.commn.dao.AppointmentStatusDao;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.test.unit.OscarPropertiesOverride;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for how {@link ApptStatusData} draws an appointment's status on the schedule, with
 * editable statuses. A signed or verified image status gets the image's S or V copy, while a glyph
 * status keeps its glyph and reports its sign-off separately. A disabled status keeps its look, and
 * clicking the icon moves the appointment on to the next active status, or from Confirmed to Here.
 *
 * @since 2026-10-07
 */
@DisplayName("ApptStatusData schedule status unit tests")
@Tag("unit")
@Tag("fast")
@Tag("appointment")
public class ApptStatusDataUnitTest extends OpenOUnitTestBase {

    @RegisterExtension
    final AppointmentStatusCacheExtension statusCache = new AppointmentStatusCacheExtension();

    @RegisterExtension
    final OscarPropertiesOverride properties = new OscarPropertiesOverride();

    @BeforeEach
    void setUp() {
        // The manager fills its DAO on class load, so a mock is registered before the class is touched.
        registerMock(AppointmentStatusDao.class, mock(AppointmentStatusDao.class));
        AppointmentStatus picked = status(3, "P", "picked.gif");
        picked.setDescription("Picked");
        picked.setColor("#FFBBFF");
        picked.setActive(0);
        statusCache.fill(List.of(status(4, "E", "empty.gif"), picked, status(2, "G", "glyphicon-home"), status(1, "H", "here.gif")));
        // ApptStatusData reads ENABLE_EDIT_APPT_STATUS when it is made; "yes" draws the statuses as edited.
        properties.set("ENABLE_EDIT_APPT_STATUS", "yes");
    }

    @ParameterizedTest
    @CsvSource({"H, here.gif, ''", "HS, Shere.gif, S", "HV, Vhere.gif, V"})
    @DisplayName("should draw an image status's own copy when signed or verified")
    void shouldUseImageCopy_whenImageStatus(String apptStatus, String icon, String signOff) {
        ApptStatusData data = apptStatusData(apptStatus);

        assertThat(data.getIcon()).isEqualTo(icon);
        assertThat(data.getSignOff()).isEqualTo(signOff);
    }

    @ParameterizedTest
    @CsvSource({"G, ''", "GS, S", "GV, V"})
    @DisplayName("should keep a glyph status's glyph and report the sign-off separately")
    void shouldKeepGlyph_whenGlyphStatus(String apptStatus, String signOff) {
        ApptStatusData data = apptStatusData(apptStatus);

        assertThat(data.getIcon()).isEqualTo("glyphicon-home");
        assertThat(data.getSignOff()).isEqualTo(signOff);
    }

    @Test
    @DisplayName("should draw a disabled status with its own colour, icon and name")
    void shouldKeepLook_whenStatusDisabled() {
        ApptStatusData data = apptStatusData("PS");

        assertThat(data.getBgColor()).isEqualTo("#FFBBFF");
        assertThat(data.getIcon()).isEqualTo("Spicked.gif");
        assertThat(data.getTitle()).isEqualTo("Picked/Signed");
    }

    @ParameterizedTest
    @CsvSource({
            "H, G",
            // Skips the disabled Picked
            "G, E",
            // A disabled status moves on too
            "P, E",
            "HS, GS",
            // Cancelled starts again at the first active status
            "C, H",
            // The last active status and Billed go no further
            "E, ''",
            "B, ''"})
    @DisplayName("should move a click on the icon to the next active status")
    void shouldGiveNextActiveStatus_whenIconClicked(String apptStatus, String nextStatus) {
        assertThat(apptStatusData(apptStatus).getNextStatus()).isEqualTo(nextStatus);
    }

    @Test
    @DisplayName("should give no next status for a code with no status")
    void shouldGiveNoNextStatus_whenCodeHasNoStatus() {
        assertThat(apptStatusData("Z").getNextStatus()).isNull();
    }

    @ParameterizedTest
    @CsvSource({"h, H", "hS, HS", "hV, HV"})
    @DisplayName("should move Confirmed on to Here, signed or verified too")
    void shouldMoveConfirmedToHere_whenHereActive(String apptStatus, String nextStatus) {
        assertThat(apptStatusData(apptStatus).getNextStatus()).isEqualTo(nextStatus);
    }

    @ParameterizedTest
    @CsvSource({"h, G", "hS, GS", "hV, GV"})
    @DisplayName("should move Confirmed on to where a click on Here would, when Here is disabled")
    void shouldMoveConfirmedPastHere_whenHereDisabled(String apptStatus, String nextStatus) {
        AppointmentStatusMgrImpl.getCachedStatuses().stream()
                .filter(status -> status.getStatus().equals("H"))
                .forEach(here -> here.setActive(0));

        assertThat(apptStatusData(apptStatus).getNextStatus()).isEqualTo(nextStatus);
    }

    @Test
    @DisplayName("should give no short-letter colour for a disabled status without one, so the schedule uses its own")
    void shouldGiveNoShortLetterColour_whenStatusHasNone() {
        // Picked is disabled and has no short-letter colour, like every status CDS import adds.
        assertThat(apptStatusData("P").getShortLetterColour()).isNull();
    }

    private static ApptStatusData apptStatusData(String apptStatus) {
        ApptStatusData data = new ApptStatusData();
        data.setApptStatus(apptStatus);
        return data;
    }

    private static AppointmentStatus status(int id, String code, String icon) {
        AppointmentStatus status = new AppointmentStatus();
        status.setId(id);
        status.setStatus(code);
        status.setIcon(icon);
        status.setActive(1);
        return status;
    }
}
