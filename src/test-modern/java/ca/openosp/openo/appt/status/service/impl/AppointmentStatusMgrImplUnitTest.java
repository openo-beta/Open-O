package ca.openosp.openo.appt.status.service.impl;

import ca.openosp.openo.appt.IconSet;
import ca.openosp.openo.appt.status.service.AppointmentStatusMgr;
import ca.openosp.openo.commn.dao.AppointmentStatusDao;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the status changes in {@link AppointmentStatusMgrImpl}: {@code updateDescription},
 * {@code updateColour}, {@code updateIcon} and {@code reset}, and for {@code getStatusChoices}, the
 * edit screen's list, and the schedule's status cache.
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
@Tag("manager")
public class AppointmentStatusMgrImplUnitTest extends OpenOUnitTestBase {

    private static final int STATUS_ID = 3;
    @Mock
    private AppointmentStatusDao appointmentStatusDao;

    @RegisterExtension
    final AppointmentStatusCacheExtension statusCache = new AppointmentStatusCacheExtension();

    private AppointmentStatusMgrImpl manager;
    private AppointmentStatus status;
    private final LoggedInInfo loggedInInfo = new LoggedInInfo();
    private MockedStatic<LogAction> logActionMock;

    @BeforeEach
    void setUp() {
        // The DAO is a static field filled on class load, so register it first, then swap it in.
        registerMock(AppointmentStatusDao.class, appointmentStatusDao);
        // LogAction's static initializer looks up OscarLogDao, so register it before mocking LogAction
        registerMock(OscarLogDao.class, mock(OscarLogDao.class));
        logActionMock = mockStatic(LogAction.class);
        statusCache.useDao(appointmentStatusDao);
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
    void closeLogAction() {
        logActionMock.close();
    }

    /** A change to one status, as the shared-rule tests apply it. */
    @FunctionalInterface
    interface StatusChange {
        boolean apply(AppointmentStatusMgrImpl manager, LoggedInInfo loggedInInfo, int id);
    }

    /** Every change to one status, named for the test report. */
    static Stream<Named<StatusChange>> changes() {
        return Stream.of(
                Named.of("updateDescription", (StatusChange) (m, info, id) -> m.updateDescription(info, id, "Renamed")),
                Named.of("updateColour", (StatusChange) (m, info, id) -> m.updateColour(info, id, "#445566")),
                Named.of("updateIcon", (StatusChange) (m, info, id) -> m.updateIcon(info, id, "1.gif")),
                Named.of("changeStatus", (StatusChange) (m, info, id) -> m.changeStatus(info, id, 0)));
    }

    @Nested
    @DisplayName("updateDescription")
    class Description {

        @ParameterizedTest
        @ValueSource(strings = {"Waiting room", "  Waiting room  "})
        @DisplayName("should save the trimmed description")
        void shouldSaveTrimmedDescription_whenNotBlank(String description) {
            assertThat(manager.updateDescription(loggedInInfo, STATUS_ID, description)).isTrue();

            assertThat(status.getDescription()).isEqualTo("Waiting room");
            verify(appointmentStatusDao).merge(status);
        }

        @Test
        @DisplayName("should save a description that fills the column")
        void shouldSaveDescription_whenExactlyColumnWidth() {
            String description = "x".repeat(AppointmentStatusMgr.DESCRIPTION_MAX_LENGTH);

            assertThat(manager.updateDescription(loggedInInfo, STATUS_ID, description)).isTrue();

            assertThat(status.getDescription()).isEqualTo(description);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"})
        @DisplayName("should reject a blank or over-long description and load nothing")
        void shouldRejectDescription_whenBlankOrTooLong(String description) {
            assertThatThrownBy(() -> manager.updateDescription(loggedInInfo, STATUS_ID, description))
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
            assertThat(manager.updateColour(loggedInInfo, STATUS_ID, colour)).isTrue();

            assertThat(status.getColor()).isEqualTo("#1a2B3c");
            verify(appointmentStatusDao).merge(status);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"#112233;background:url(x)"})
        @DisplayName("should reject no colour, or one ItemStyleColour refuses, and load nothing")
        void shouldRejectColour_whenNotHexColour(String colour) {
            assertThatThrownBy(() -> manager.updateColour(loggedInInfo, STATUS_ID, colour))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(appointmentStatusDao, never()).find(anyInt());
            verify(appointmentStatusDao, never()).merge(any());
        }
    }

    @Nested
    @DisplayName("updateIcon")
    class Icon {

        @ParameterizedTest
        @ValueSource(strings = {"here.gif", "glyphicon-home"})
        @DisplayName("should save an icon in the Icon Set, image or glyph")
        void shouldSaveIcon_whenInIconSet(String icon) {
            assertThat(manager.updateIcon(loggedInInfo, STATUS_ID, icon)).isTrue();

            assertThat(status.getIcon()).isEqualTo(icon);
            verify(appointmentStatusDao).merge(status);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"here.gif\" onerror=\"x"})
        @DisplayName("should reject no icon, or one outside the Icon Set, and load nothing")
        void shouldRejectIcon_whenNotInIconSet(String icon) {
            assertThatThrownBy(() -> manager.updateIcon(loggedInInfo, STATUS_ID, icon))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(appointmentStatusDao, never()).find(anyInt());
            verify(appointmentStatusDao, never()).merge(any());
        }
    }

    @Nested
    @DisplayName("enable and disable")
    class Active {

        @ParameterizedTest
        @ValueSource(ints = {0, 1})
        @DisplayName("should save the active flag when the status is editable")
        void shouldSaveActive_whenStatusEditable(int active) {
            status.setActive(1 - active);

            assertThat(manager.changeStatus(loggedInInfo, STATUS_ID, active)).isTrue();

            assertThat(status.getActive()).isEqualTo(active);
            verify(appointmentStatusDao).merge(status);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 2})
        @DisplayName("should reject an active flag other than 0 or 1")
        void shouldReject_whenActiveNotZeroOrOne(int active) {
            assertThatThrownBy(() -> manager.changeStatus(loggedInInfo, STATUS_ID, active)).isInstanceOf(IllegalArgumentException.class);

            verify(appointmentStatusDao, never()).merge(any());
        }
    }

    @Nested
    @DisplayName("shared rules")
    class SharedRules {

        @Test
        @DisplayName("should write an audit row naming the user, the status and its new value")
        void shouldAudit_whenSaved() {
            manager.updateColour(loggedInInfo, STATUS_ID, " #445566 ");
            manager.changeStatus(loggedInInfo, STATUS_ID, 0);

            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("AppointmentStatusMgr.updateColour"), eq("appointment_status id 3: colour is now [#445566]")));
            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("AppointmentStatusMgr.changeStatus"), eq("appointment_status id 3: active is now 0")));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("ca.openosp.openo.appt.status.service.impl.AppointmentStatusMgrImplUnitTest#changes")
        @DisplayName("should change nothing, and audit nothing, when the status is locked")
        void shouldReturnFalse_whenStatusLocked(StatusChange change) {
            status.setEditable(0);
            status.setActive(1);

            assertThat(change.apply(manager, loggedInInfo, STATUS_ID)).isFalse();

            assertThat(status.getDescription()).isEqualTo("Here");
            assertThat(status.getColor()).isEqualTo("#00ee00");
            assertThat(status.getIcon()).isEqualTo("here.gif");
            assertThat(status.getActive()).isEqualTo(1);
            verify(appointmentStatusDao, never()).merge(any());
            logActionMock.verifyNoInteractions();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("ca.openosp.openo.appt.status.service.impl.AppointmentStatusMgrImplUnitTest#changes")
        @DisplayName("should return false when the status does not exist")
        void shouldReturnFalse_whenStatusMissing(StatusChange change) {
            assertThat(change.apply(manager, loggedInInfo, 99)).isFalse();

            verify(appointmentStatusDao, never()).merge(any());
        }

        @Test
        @DisplayName("should leave the other fields alone")
        void shouldKeepOtherFields_whenOneFieldUpdated() {
            manager.updateIcon(loggedInInfo, STATUS_ID, "1.gif");

            assertThat(status.getDescription()).isEqualTo("Here");
            assertThat(status.getColor()).isEqualTo("#00ee00");
        }
    }

    @Nested
    @DisplayName("getStatusChoices")
    class StatusChoices {

        private final AppointmentStatus here = choice(3, "H", 1);
        private final AppointmentStatus picked = choice(4, "P", 0);
        private final AppointmentStatus emptyRoom = choice(5, "E", 1);

        @BeforeEach
        void allStatusesOutOfOrder() {
            when(appointmentStatusDao.findAll()).thenReturn(List.of(emptyRoom, picked, here));
        }

        @Test
        @DisplayName("should offer only the active statuses, by id, when the appointment's status is active")
        void shouldOfferActiveStatuses_whenOwnStatusActive() {
            assertThat(manager.getStatusChoices("H")).containsExactly(here, emptyRoom);
        }

        @Test
        @DisplayName("should also offer the appointment's own status, in its place, when it was disabled")
        void shouldOfferOwnStatus_whenOwnStatusDisabled() {
            assertThat(manager.getStatusChoices("P")).containsExactly(here, picked, emptyRoom);
        }

        @Test
        @DisplayName("should not offer a disabled status whose code differs only in case")
        void shouldLeaveOutDisabledStatus_whenCodeDiffersInCase() {
            assertThat(manager.getStatusChoices("p")).containsExactly(here, emptyRoom);
        }

        private AppointmentStatus choice(int id, String code, int active) {
            AppointmentStatus choice = new AppointmentStatus();
            choice.setId(id);
            choice.setStatus(code);
            choice.setActive(active);
            return choice;
        }
    }

    @Nested
    @DisplayName("status cache")
    class StatusCache {

        @BeforeEach
        void emptyCache() {
            statusCache.empty();
        }

        @Test
        @DisplayName("should load the statuses once, by id, and once more after a status is saved")
        void shouldReloadOnce_whenStatusSaved() {
            AppointmentStatus here = status(3, "H");
            AppointmentStatus toDo = status(1, "t");
            when(appointmentStatusDao.findAll()).thenReturn(new ArrayList<>(List.of(here, toDo)), new ArrayList<>(List.of(toDo)));

            AppointmentStatusMgrImpl.getCachedStatuses();
            assertThat(AppointmentStatusMgrImpl.getCachedStatuses()).containsExactly(toDo, here);

            here.on_jpa_update();
            AppointmentStatusMgrImpl.getCachedStatuses();
            assertThat(AppointmentStatusMgrImpl.getCachedStatuses()).containsExactly(toDo);

            verify(appointmentStatusDao, times(2)).findAll();
        }

        @Test
        @DisplayName("should reload once more when the save commits, after a lookup made before the commit")
        void shouldReloadAgain_whenSaveCommits() {
            AppointmentStatus hereBeforeSave = status(3, "H");
            hereBeforeSave.setActive(1);
            AppointmentStatus hereDisabled = status(3, "H");
            hereDisabled.setActive(0);
            when(appointmentStatusDao.findAll()).thenReturn(new ArrayList<>(List.of(hereBeforeSave)), new ArrayList<>(List.of(hereDisabled)));
            TransactionSynchronizationManager.initSynchronization();
            try {
                hereDisabled.on_jpa_update();
                // A lookup before the commit still reads Here as active.
                assertThat(AppointmentStatusMgrImpl.getCachedStatuses()).extracting(AppointmentStatus::getActive).containsExactly(1);

                TransactionSynchronizationUtils.triggerAfterCommit();

                assertThat(AppointmentStatusMgrImpl.getCachedStatuses()).extracting(AppointmentStatus::getActive).containsExactly(0);
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
            }
        }

        @Test
        @DisplayName("should try the reload again on the next lookup when it fails")
        void shouldRetryReload_whenReloadFails() {
            AppointmentStatus here = status(3, "H");
            AppointmentStatus toDo = status(1, "t");
            when(appointmentStatusDao.findAll())
                    .thenReturn(new ArrayList<>(List.of(here)))
                    .thenThrow(new PersistenceException("database unavailable"))
                    .thenReturn(new ArrayList<>(List.of(toDo)));
            AppointmentStatusMgrImpl.getCachedStatuses();
            here.on_jpa_update();

            assertThatThrownBy(AppointmentStatusMgrImpl::getCachedStatuses).isInstanceOf(PersistenceException.class);

            assertThat(AppointmentStatusMgrImpl.getCachedStatuses()).containsExactly(toDo);
        }

        @Test
        @DisplayName("should list the active statuses without touching the cache")
        void shouldLeaveCache_whenActiveStatusesListed() {
            AppointmentStatus here = status(3, "H");
            AppointmentStatus toDo = status(1, "t");
            when(appointmentStatusDao.findAll()).thenReturn(new ArrayList<>(List.of(here)), new ArrayList<>(List.of(toDo)));
            AppointmentStatusMgrImpl.getCachedStatuses();
            toDo.on_jpa_update();

            manager.getAllActiveStatus();

            verify(appointmentStatusDao).findActive();
            verify(appointmentStatusDao, times(1)).findAll();
            // The save is still pending, so the schedule's next lookup reloads.
            assertThat(AppointmentStatusMgrImpl.getCachedStatuses()).containsExactly(toDo);
        }

        private AppointmentStatus status(int id, String code) {
            AppointmentStatus status = new AppointmentStatus();
            status.setId(id);
            status.setStatus(code);
            return status;
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

            manager.reset(loggedInInfo);

            verify(appointmentStatusDao).mergeAll(saved.capture());
            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo), eq("AppointmentStatusMgr.reset"), any()));
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
        @DisplayName("should restore only status images and descriptions that fit the column")
        void shouldRestoreValidStyles_whenEveryCodeReset() throws IOException {
            List<AppointmentStatus> table = new ArrayList<>();
            for (AppointmentStatus seeded : seedRows()) {
                table.add(row(seeded.getId(), seeded.getStatus(), 1));
            }

            resetAndCaptureSaved(table);

            assertThat(table).allSatisfy(status -> {
                assertThat(IconSet.IMAGES).contains(status.getIcon());
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
