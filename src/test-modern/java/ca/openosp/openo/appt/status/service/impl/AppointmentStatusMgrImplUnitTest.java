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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the status changes in {@link AppointmentStatusMgrImpl}: {@code updateDescription},
 * {@code updateColour}, {@code updateIcon} and {@code reset}.
 *
 * <p>The colour and icon end up in style and src attributes on the schedule, so the tests pin the
 * rules: a valid value is saved, an invalid one is rejected before the status is loaded, and a
 * locked status (editable=0) is never changed. Reset is checked on both table layouts found in the
 * field, and against the seed it restores.</p>
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
        @ValueSource(strings = {"lts.gif", "Shere.gif", "../here.gif", " here.gif", "here.gif\" onerror=\"x"})
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

    @Nested
    @DisplayName("reset")
    class Reset {

        /* One seeded appointment_status row: (id,'code','description','#colour','icon',active,editable,... */
        private static final Pattern SEED_ROW =
                Pattern.compile("\\((\\d+),\\s*'(\\w)',\\s*'([^']*)',\\s*'([^']*)',\\s*'([^']*)',\\s*(\\d),\\s*(\\d)");

        private AppointmentStatus row(int id, String code, int editable) {
            AppointmentStatus row = new AppointmentStatus();
            row.setId(id);
            row.setStatus(code);
            row.setDescription("Custom " + code);
            row.setColor("#123456");
            row.setIcon("16.gif");
            row.setActive(1);
            row.setEditable(editable);
            return row;
        }

        private List<AppointmentStatus> seedRows() throws IOException {
            String seed = Files.readString(Path.of("database/mysql/oscardata.sql"));
            String insert = seed.substring(seed.indexOf("INSERT INTO `appointment_status` VALUES"));
            insert = insert.substring(0, insert.indexOf(';'));
            List<AppointmentStatus> rows = new ArrayList<>();
            Matcher m = SEED_ROW.matcher(insert);
            while (m.find()) {
                AppointmentStatus seeded = new AppointmentStatus();
                seeded.setId(Integer.parseInt(m.group(1)));
                seeded.setStatus(m.group(2));
                seeded.setDescription(m.group(3));
                seeded.setColor(m.group(4));
                seeded.setIcon(m.group(5));
                seeded.setActive(Integer.parseInt(m.group(6)));
                seeded.setEditable(Integer.parseInt(m.group(7)));
                rows.add(seeded);
            }
            return rows;
        }

        @SuppressWarnings("unchecked")
        private List<AppointmentStatus> resetAndCaptureSaved(List<AppointmentStatus> table) {
            when(appointmentStatusDao.findAll()).thenReturn(table);
            ArgumentCaptor<List<AppointmentStatus>> saved = ArgumentCaptor.forClass(List.class);

            manager.reset();

            verify(appointmentStatusDao).mergeAll(saved.capture());
            return saved.getValue();
        }

        @Test
        @DisplayName("should restore every editable status to its seeded style by code on the 15-row layout")
        void shouldRestoreSeededStyleByCode_whenFifteenRowLayout() throws IOException {
            List<AppointmentStatus> seed = seedRows();
            assertThat(seed).hasSize(15);
            List<AppointmentStatus> table = new ArrayList<>();
            for (AppointmentStatus seeded : seed) {
                // Every row unlocked, so each default is written and can be compared with the seed.
                table.add(row(seeded.getId(), seeded.getStatus(), 1));
            }

            List<AppointmentStatus> saved = resetAndCaptureSaved(table);

            assertThat(saved).hasSize(15);
            for (int i = 0; i < seed.size(); i++) {
                assertThat(table.get(i))
                        .as("status %s", seed.get(i).getStatus())
                        .extracting(AppointmentStatus::getDescription, AppointmentStatus::getColor, AppointmentStatus::getIcon)
                        .containsExactly(seed.get(i).getDescription(), seed.get(i).getColor(), seed.get(i).getIcon());
            }
        }

        @Test
        @DisplayName("should restore only icons in the icon set and descriptions that fit the column")
        void shouldRestoreValidStyles_whenEveryCodeReset() throws IOException {
            List<AppointmentStatus> table = new ArrayList<>();
            for (AppointmentStatus seeded : seedRows()) {
                table.add(row(seeded.getId(), seeded.getStatus(), 1));
            }

            resetAndCaptureSaved(table);

            assertThat(table).allSatisfy(status -> {
                assertThat(AppointmentStatusMgr.ICON_SET).contains(status.getIcon());
                assertThat(status.getDescription()).hasSizeLessThanOrEqualTo(AppointmentStatusMgr.DESCRIPTION_MAX_LENGTH);
                assertThat(status.getColor()).matches("#[0-9a-fA-F]{6}");
            });
        }

        @Test
        @DisplayName("should match statuses by code on the older 13-row layout, where ids 11-13 are N, C and B")
        void shouldMatchByCode_whenThirteenRowLayout() {
            AppointmentStatus here = row(3, "H", 1);
            AppointmentStatus custom5 = row(10, "e", 1);
            AppointmentStatus noShow = row(11, "N", 1);
            AppointmentStatus billed = row(13, "B", 1);

            resetAndCaptureSaved(List.of(here, custom5, noShow, billed));

            assertThat(here.getDescription()).isEqualTo("Here");
            assertThat(here.getIcon()).isEqualTo("here.gif");
            assertThat(custom5.getDescription()).isEqualTo("Customized 5");
            assertThat(noShow.getDescription()).isEqualTo("No Show");
            assertThat(noShow.getColor()).isEqualTo("#cccccc");
            assertThat(noShow.getIcon()).isEqualTo("noshow.gif");
            assertThat(billed.getDescription()).isEqualTo("Billed");
        }

        @Test
        @DisplayName("should leave locked statuses and unknown codes alone")
        void shouldSkipLockedAndUnknown_whenResetting() {
            AppointmentStatus here = row(3, "H", 1);
            AppointmentStatus todo = row(1, "t", 0);
            AppointmentStatus clinicOwn = row(16, "z", 1);

            List<AppointmentStatus> saved = resetAndCaptureSaved(List.of(here, todo, clinicOwn));

            assertThat(saved).containsExactly(here);
            assertThat(todo.getDescription()).isEqualTo("Custom t");
            assertThat(todo.getIcon()).isEqualTo("16.gif");
            assertThat(clinicOwn.getDescription()).isEqualTo("Custom z");
        }

        @Test
        @DisplayName("should keep whether a status is active or editable")
        void shouldKeepActiveAndEditable_whenResetting() {
            AppointmentStatus custom3 = row(8, "c", 1);
            custom3.setActive(0);

            resetAndCaptureSaved(List.of(custom3));

            assertThat(custom3.getDescription()).isEqualTo("Customized 3");
            assertThat(custom3.getActive()).isZero();
            assertThat(custom3.getEditable()).isEqualTo(1);
        }

        @Test
        @DisplayName("should save all changes in one call and nothing one by one")
        void shouldSaveTogether_whenResetting() {
            resetAndCaptureSaved(List.of(row(3, "H", 1), row(4, "P", 1)));

            verify(appointmentStatusDao, never()).merge(any());
        }

        @Test
        @DisplayName("should save an empty batch when no status is editable")
        void shouldSaveNothing_whenNoStatusEditable() {
            assertThat(resetAndCaptureSaved(List.of(row(1, "t", 0)))).isEmpty();
        }
    }
}
