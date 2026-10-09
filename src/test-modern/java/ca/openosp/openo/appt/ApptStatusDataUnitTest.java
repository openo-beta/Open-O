package ca.openosp.openo.appt;

import ca.openosp.OscarProperties;
import ca.openosp.openo.appt.status.service.impl.AppointmentStatusMgrImpl;
import ca.openosp.openo.commn.dao.AppointmentStatusDao;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the status icon {@link ApptStatusData} gives the schedule, with editable statuses:
 * a signed or verified image status gets the image's S or V copy, while a glyph status keeps its
 * glyph and reports its sign-off separately.
 *
 * @since 2026-10-07
 */
@DisplayName("ApptStatusData status icon unit tests")
@Tag("unit")
@Tag("fast")
public class ApptStatusDataUnitTest extends OpenOUnitTestBase {

    private List<AppointmentStatus> originalCache;
    private boolean originalCacheIsDirty;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // The manager fills its DAO on class load, so a mock is registered before the class is touched.
        registerMock(AppointmentStatusDao.class, mock(AppointmentStatusDao.class));
        originalCache = (List<AppointmentStatus>) ReflectionTestUtils.getField(AppointmentStatusMgrImpl.class, "cachedActiveStatuses");
        originalCacheIsDirty = AppointmentStatusMgrImpl.isCacheIsDirty();
        AppointmentStatusMgrImpl.setCachedActiveStatuses(new ArrayList<>(List.of(status(1, "H", "here.gif"), status(2, "G", "glyphicon-home"))));
        AppointmentStatusMgrImpl.setCacheIsDirty(false);
    }

    @AfterEach
    void restoreCache() {
        ReflectionTestUtils.setField(AppointmentStatusMgrImpl.class, "cachedActiveStatuses", originalCache);
        AppointmentStatusMgrImpl.setCacheIsDirty(originalCacheIsDirty);
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

    /* ApptStatusData reads ENABLE_EDIT_APPT_STATUS when it is made; "yes" draws the statuses as edited. */
    private static ApptStatusData apptStatusData(String apptStatus) {
        try (MockedStatic<OscarProperties> properties = mockStatic(OscarProperties.class)) {
            OscarProperties editable = mock(OscarProperties.class);
            when(editable.getProperty("ENABLE_EDIT_APPT_STATUS")).thenReturn("yes");
            properties.when(OscarProperties::getInstance).thenReturn(editable);
            ApptStatusData data = new ApptStatusData();
            data.setApptStatus(apptStatus);
            return data;
        }
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
