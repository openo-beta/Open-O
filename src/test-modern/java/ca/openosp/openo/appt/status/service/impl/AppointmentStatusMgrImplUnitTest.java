package ca.openosp.openo.appt.status.service.impl;

import ca.openosp.openo.appt.status.service.AppointmentStatusMgr;
import ca.openosp.openo.commn.dao.AppointmentStatusDao;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the status updates in {@link AppointmentStatusMgrImpl}: {@code updateDescription},
 * {@code updateColour} and {@code updateIcon}.
 *
 * <p>The colour and icon end up in style and src attributes on the schedule, so the tests pin the
 * rules: a valid value is saved, an invalid one is rejected before the status is loaded, and a
 * locked status (editable=0) is never changed.</p>
 *
 * @since 2026-09-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentStatusMgrImpl update unit tests")
@Tag("unit")
@Tag("fast")
@Tag("update")
public class AppointmentStatusMgrImplUnitTest extends OpenOUnitTestBase {

    private static final int STATUS_ID = 3;
    private static final String ICON_SET_SOURCE =
            "ca.openosp.openo.appt.status.service.impl.AppointmentStatusMgrImplUnitTest#iconSet";

    static List<String> iconSet() {
        return AppointmentStatusMgr.ICON_SET;
    }

    @Mock
    private AppointmentStatusDao appointmentStatusDao;

    private AppointmentStatusDao originalDao;
    private AppointmentStatusMgrImpl manager;
    private AppointmentStatus status;

    @BeforeEach
    void setUp() {
        // The DAO is a static field filled on class load, so swap it in and put the original back after.
        registerMock(AppointmentStatusDao.class, appointmentStatusDao);
        originalDao = (AppointmentStatusDao) ReflectionTestUtils.getField(AppointmentStatusMgrImpl.class, "appointStatusDao");
        ReflectionTestUtils.setField(AppointmentStatusMgrImpl.class, "appointStatusDao", appointmentStatusDao);
        manager = new AppointmentStatusMgrImpl();

        status = new AppointmentStatus();
        status.setId(STATUS_ID);
        status.setDescription("Here");
        status.setColor("#00ee00");
        status.setIcon("here.gif");
        status.setEditable(1);
        lenient().when(appointmentStatusDao.find(STATUS_ID)).thenReturn(status);
    }

    @AfterEach
    void restoreDao() {
        ReflectionTestUtils.setField(AppointmentStatusMgrImpl.class, "appointStatusDao", originalDao);
    }

    @Nested
    @DisplayName("updateDescription")
    class Description {

        @ParameterizedTest
        @ValueSource(strings = {"Waiting room", "  Waiting room  "})
        @DisplayName("should save the trimmed description")
        void shouldSaveTrimmedDescription_whenNotBlank(String description) {
            assertThat(manager.updateDescription(STATUS_ID, description)).isTrue();

            assertThat(status.getDescription()).isEqualTo("Waiting room");
            verify(appointmentStatusDao).merge(status);
        }

        @Test
        @DisplayName("should save a description that fills the column")
        void shouldSaveDescription_whenExactlyColumnWidth() {
            String description = "x".repeat(AppointmentStatusMgr.DESCRIPTION_MAX_LENGTH);

            assertThat(manager.updateDescription(STATUS_ID, description)).isTrue();

            assertThat(status.getDescription()).isEqualTo(description);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"})
        @DisplayName("should reject a blank or over-long description and load nothing")
        void shouldRejectDescription_whenBlankOrTooLong(String description) {
            assertThatThrownBy(() -> manager.updateDescription(STATUS_ID, description))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(appointmentStatusDao, never()).find(anyInt());
            verify(appointmentStatusDao, never()).merge(any());
        }
    }

    @Nested
    @DisplayName("updateColour")
    class Colour {

        @ParameterizedTest
        @ValueSource(strings = {"#1a2B3c", "  #1a2B3c  "})
        @DisplayName("should save the trimmed colour when it is #rrggbb")
        void shouldSaveColour_whenHexColour(String colour) {
            assertThat(manager.updateColour(STATUS_ID, colour)).isTrue();

            assertThat(status.getColor()).isEqualTo("#1a2B3c");
            verify(appointmentStatusDao).merge(status);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"red", "#fff", "112233", "#12345g", "#1122334", "#112233;background:url(x)"})
        @DisplayName("should reject the colour and load nothing when it is not #rrggbb")
        void shouldRejectColour_whenNotHexColour(String colour) {
            assertThatThrownBy(() -> manager.updateColour(STATUS_ID, colour))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(appointmentStatusDao, never()).find(anyInt());
            verify(appointmentStatusDao, never()).merge(any());
        }
    }

    @Nested
    @DisplayName("updateIcon")
    class Icon {

        @ParameterizedTest
        @MethodSource(ICON_SET_SOURCE)
        @DisplayName("should save any icon in the icon set")
        void shouldSaveIcon_whenInIconSet(String icon) {
            assertThat(manager.updateIcon(STATUS_ID, icon)).isTrue();

            assertThat(status.getIcon()).isEqualTo(icon);
            verify(appointmentStatusDao).merge(status);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"thumb.png", "Shere.gif", "../here.gif", " here.gif", "here.gif\" onerror=\"x"})
        @DisplayName("should reject the icon and load nothing when it is not in the icon set")
        void shouldRejectIcon_whenNotInIconSet(String icon) {
            assertThatThrownBy(() -> manager.updateIcon(STATUS_ID, icon))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(appointmentStatusDao, never()).find(anyInt());
            verify(appointmentStatusDao, never()).merge(any());
        }

        @ParameterizedTest
        @MethodSource(ICON_SET_SOURCE)
        @DisplayName("should list only images that have signed and verified variants")
        void shouldHaveSignedAndVerifiedVariants_whenInIconSet(String icon) {
            Path images = Path.of("src/main/webapp/images");

            assertThat(images.resolve(icon)).exists();
            assertThat(images.resolve("S" + icon)).exists();
            assertThat(images.resolve("V" + icon)).exists();
        }
    }

    @Nested
    @DisplayName("shared rules")
    class SharedRules {

        @Test
        @DisplayName("should change nothing when the status is locked")
        void shouldReturnFalse_whenStatusLocked() {
            status.setEditable(0);

            assertThat(manager.updateDescription(STATUS_ID, "Renamed")).isFalse();
            assertThat(manager.updateColour(STATUS_ID, "#445566")).isFalse();
            assertThat(manager.updateIcon(STATUS_ID, "1.gif")).isFalse();

            assertThat(status.getDescription()).isEqualTo("Here");
            assertThat(status.getColor()).isEqualTo("#00ee00");
            assertThat(status.getIcon()).isEqualTo("here.gif");
            verify(appointmentStatusDao, never()).merge(any());
        }

        @Test
        @DisplayName("should return false when the status does not exist")
        void shouldReturnFalse_whenStatusMissing() {
            when(appointmentStatusDao.find(99)).thenReturn(null);

            assertThat(manager.updateColour(99, "#445566")).isFalse();

            verify(appointmentStatusDao, never()).merge(any());
        }

        @Test
        @DisplayName("should leave the other fields alone")
        void shouldKeepOtherFields_whenOneFieldUpdated() {
            manager.updateIcon(STATUS_ID, "1.gif");

            assertThat(status.getDescription()).isEqualTo("Here");
            assertThat(status.getColor()).isEqualTo("#00ee00");
        }
    }
}
