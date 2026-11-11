package ca.openosp.openo.managers;

import java.util.Map;
import java.util.stream.Stream;

import ca.openosp.openo.appt.LocationList;
import ca.openosp.openo.commn.dao.LookupListDao;
import ca.openosp.openo.commn.dao.LookupListItemDao;
import ca.openosp.openo.commn.dao.OscarAppointmentDao;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.commn.dao.SystemPreferencesDao;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.SystemPreferences;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppointmentLocationManager}: the one place the Location List is resolved,
 * its Default Location and Location Requirement are stored, and a location's new name is carried to
 * its upcoming appointments, which it also counts.
 *
 * @since 2026-10-23
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentLocationManager unit tests")
@Tag("unit")
@Tag("fast")
@Tag("manager")
public class AppointmentLocationManagerUnitTest extends OpenOUnitTestBase {

    private static final int ITEM_ID = 7;
    private static final int LOCATION_LIST_ID = 5;
    private static final java.sql.Date TODAY = java.sql.Date.valueOf("2026-10-16");

    @Mock
    private LookupListDao lookupListDao;

    @Mock
    private LookupListItemDao lookupListItemDao;

    @Mock
    private SystemPreferencesDao systemPreferencesDao;

    @Mock
    private OscarAppointmentDao appointmentDao;

    @Mock
    private SecurityInfoManager securityInfoManager;

    @InjectMocks
    private AppointmentLocationManager manager;

    private final LoggedInInfo loggedInInfo = new LoggedInInfo();
    private MockedStatic<LogAction> logActionMock;
    private MockedStatic<LocationList> locationListMock;
    private LookupList locationList;
    private LookupListItem location;

    @BeforeEach
    void setUp() {
        // LogAction's static initializer looks up OscarLogDao, so register it before mocking LogAction
        registerMock(OscarLogDao.class, mock(OscarLogDao.class));
        logActionMock = mockStatic(LogAction.class);
        // Pin today, so a run across midnight can't give the manager another date than the test expects
        locationListMock = mockStatic(LocationList.class, CALLS_REAL_METHODS);
        locationListMock.when(LocationList::firstRenamedDate).thenReturn(TODAY);

        lenient().when(securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null))
                .thenReturn(true);

        locationList = new LookupList();
        locationList.setId(LOCATION_LIST_ID);
        lenient().when(lookupListDao.findByName("appointmentLocationCode")).thenReturn(locationList);

        location = new LookupListItem();
        location.setId(ITEM_ID);
        location.setLookupListId(LOCATION_LIST_ID);
        location.setLabel("Midtown");
        location.setActive(true);
        lenient().when(lookupListItemDao.find(ITEM_ID)).thenReturn(location);
    }

    @AfterEach
    void tearDown() {
        logActionMock.close();
        locationListMock.close();
    }

    /* The one system preference a Location tab setting saved. */
    private SystemPreferences saved() {
        ArgumentCaptor<SystemPreferences> preference = ArgumentCaptor.forClass(SystemPreferences.class);
        verify(systemPreferencesDao).saveEntity(preference.capture());
        return preference.getValue();
    }

    /** A change as the guard test applies it. */
    @FunctionalInterface
    interface Change {
        void apply(AppointmentLocationManager manager, LoggedInInfo loggedInInfo);
    }

    /** Every change that needs _admin update, named for the test report. */
    static Stream<Named<Change>> updates() {
        return Stream.of(
                Named.of("makeDefault", (Change) (m, info) -> m.makeDefault(info, ITEM_ID)),
                Named.of("removeDefault", (Change) (m, info) -> m.removeDefault(info, ITEM_ID)),
                Named.of("setRequired", (Change) (m, info) -> m.setRequired(info, true)),
                Named.of("rename", (Change) (m, info) -> m.rename(info, ITEM_ID, "Midtown Clinic")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("updates")
    @DisplayName("should refuse a change and save nothing without _admin update")
    void shouldDeny_whenUserLacksAdminUpdate(Change change) {
        when(securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null)).thenReturn(false);
        // removeDefault only saves, and so only checks, when the location is still the default
        lenient().when(systemPreferencesDao.findPreferenceByName(SystemPreferences.APPOINTMENT_LOCATION_KEYS.appointment_default_location))
                .thenReturn(new SystemPreferences("appointment_default_location", String.valueOf(ITEM_ID)));

        assertThatThrownBy(() -> change.apply(manager, loggedInInfo)).hasMessage("Access Denied");

        verify(lookupListItemDao, never()).find(anyInt());
        verify(systemPreferencesDao, never()).saveEntity(any());
        verify(lookupListItemDao, never()).merge(any());
        verify(appointmentDao, never()).renameLocationFrom(anyInt(), any(), any(), any());
    }

    @Nested
    @DisplayName("findLocationList")
    class LocationListResolver {

        @Test
        @DisplayName("should resolve the appointmentLocationCode list without a privilege check")
        void shouldFindLocationList_whenAnyUser() {
            assertThat(manager.findLocationList(loggedInInfo)).isSameAs(locationList);

            verify(securityInfoManager, never()).hasPrivilege(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("the Default Location")
    class LocationDefault {

        private final SystemPreferences.APPOINTMENT_LOCATION_KEYS key =
                SystemPreferences.APPOINTMENT_LOCATION_KEYS.appointment_default_location;

        private void storedDefault(String value) {
            when(systemPreferencesDao.findPreferenceByName(key)).thenReturn(new SystemPreferences(key.name(), value));
        }

        @Test
        @DisplayName("should find the default's id without a privilege check")
        void shouldFindDefault_whenAnyUser() {
            storedDefault("58");

            assertThat(manager.findDefault(loggedInInfo)).isEqualTo(58);

            verify(securityInfoManager, never()).hasPrivilege(any(), any(), any(), any());
        }

        @Test
        @DisplayName("should find no default when none was ever set")
        void shouldFindNoDefault_whenNeverSet() {
            assertThat(manager.findDefault(loggedInInfo)).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "abc", "-13", "0"})
        @DisplayName("should find no default when the stored value is cleared or not an id")
        void shouldFindNoDefault_whenValueNotAnId(String value) {
            storedDefault(value);

            assertThat(manager.findDefault(loggedInInfo)).isNull();
        }

        @Test
        @DisplayName("should store the first default as a new system preference")
        void shouldCreatePreference_whenNoDefaultYet() {
            manager.makeDefault(loggedInInfo, ITEM_ID);

            SystemPreferences preference = saved();
            assertThat(preference.getName()).isEqualTo("appointment_default_location");
            assertThat(preference.getValue()).isEqualTo(String.valueOf(ITEM_ID));
        }

        @Test
        @DisplayName("should replace the default in the existing preference and log the old and the new")
        void shouldReplaceDefault_whenOneIsSet() {
            SystemPreferences existing = new SystemPreferences(key.name(), "3");
            when(systemPreferencesDao.findPreferenceByName(key)).thenReturn(existing);

            manager.makeDefault(loggedInInfo, ITEM_ID);

            assertThat(saved()).isSameAs(existing);
            assertThat(existing.getValue()).isEqualTo(String.valueOf(ITEM_ID));
            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("AppointmentLocationManager.makeDefault"), contains("was [3], now [" + ITEM_ID + "]")));
        }

        @Test
        @DisplayName("should refuse to make an inactive location the default, since no new booking offers it")
        void shouldThrow_whenMakingInactiveDefault() {
            location.setActive(false);

            assertThatThrownBy(() -> manager.makeDefault(loggedInInfo, ITEM_ID))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(systemPreferencesDao, never()).saveEntity(any());
        }

        @Test
        @DisplayName("should refuse to make a missing location the default")
        void shouldThrow_whenMakingMissingDefault() {
            assertThatThrownBy(() -> manager.makeDefault(loggedInInfo, 99))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(systemPreferencesDao, never()).saveEntity(any());
        }

        @Test
        @DisplayName("should refuse to make another look-up list's item the default")
        void shouldThrow_whenMakingOtherListItemDefault() {
            location.setLookupListId(LOCATION_LIST_ID + 1);

            assertThatThrownBy(() -> manager.makeDefault(loggedInInfo, ITEM_ID))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(systemPreferencesDao, never()).saveEntity(any());
        }

        @Test
        @DisplayName("should remove the default while it is still this location, and log it")
        void shouldRemoveDefault_whenStillDefault() {
            SystemPreferences existing = new SystemPreferences(key.name(), String.valueOf(ITEM_ID));
            when(systemPreferencesDao.findPreferenceByName(key)).thenReturn(existing);

            manager.removeDefault(loggedInInfo, ITEM_ID);

            assertThat(saved()).isSameAs(existing);
            assertThat(existing.getValue()).isEmpty();
            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("AppointmentLocationManager.removeDefault"), contains("was [" + ITEM_ID + "], now []")));
        }

        @Test
        @DisplayName("should leave the default alone when the page asking to remove it is out of date")
        void shouldKeepDefault_whenAnotherLocationIsDefault() {
            storedDefault("3");

            manager.removeDefault(loggedInInfo, ITEM_ID);

            verify(systemPreferencesDao, never()).saveEntity(any());
        }

    }

    @Nested
    @DisplayName("the Location Requirement")
    class LocationRequirement {

        private final SystemPreferences.APPOINTMENT_LOCATION_KEYS key =
                SystemPreferences.APPOINTMENT_LOCATION_KEYS.appointment_location_required;

        @Test
        @DisplayName("should read the requirement without a privilege check")
        void shouldReadRequirement_whenAnyUser() {
            when(systemPreferencesDao.isReadBooleanPreference(key)).thenReturn(true);

            assertThat(manager.isRequired(loggedInInfo)).isTrue();

            verify(securityInfoManager, never()).hasPrivilege(any(), any(), any(), any());
        }

        @Test
        @DisplayName("should store the first change as a new system preference")
        void shouldCreatePreference_whenNeverSet() {
            manager.setRequired(loggedInInfo, true);

            SystemPreferences preference = saved();
            assertThat(preference.getName()).isEqualTo("appointment_location_required");
            assertThat(preference.getValue()).isEqualTo("true");
        }

        @Test
        @DisplayName("should turn the requirement off in the existing preference and log the old and the new")
        void shouldTurnOff_whenOn() {
            SystemPreferences existing = new SystemPreferences(key.name(), "true");
            when(systemPreferencesDao.findPreferenceByName(key)).thenReturn(existing);

            manager.setRequired(loggedInInfo, false);

            assertThat(saved()).isSameAs(existing);
            assertThat(existing.getValue()).isEqualTo("false");
            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("AppointmentLocationManager.setRequired"), contains("was [true], now [false]")));
        }
    }

    @Nested
    @DisplayName("rename")
    class Rename {

        @BeforeEach
        void loggedInProvider() {
            Provider provider = new Provider();
            provider.setProviderNo("999998");
            loggedInInfo.setLoggedInProvider(provider);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Midtown Clinic", "  Midtown Clinic  "})
        @DisplayName("should rename the location and its appointments from today on, as the user")
        void shouldRenameUpcomingAppointments_whenRenamed(String name) {
            when(appointmentDao.renameLocationFrom(ITEM_ID, "Midtown Clinic", TODAY, "999998")).thenReturn(3);

            assertThat(manager.rename(loggedInInfo, ITEM_ID, name)).isTrue();

            assertThat(location.getLabel()).isEqualTo("Midtown Clinic");
            verify(lookupListItemDao).merge(location);
            verify(appointmentDao).renameLocationFrom(ITEM_ID, "Midtown Clinic", TODAY, "999998");
            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("AppointmentLocationManager.rename"), contains("on 3 appointments")));
        }

        @Test
        @DisplayName("should log the old and the new name, the one change that loses information")
        void shouldLogBothNames_whenRenamed() {
            manager.rename(loggedInInfo, ITEM_ID, "Midtown East");

            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("AppointmentLocationManager.rename"), contains("was [Midtown], now [Midtown East]")));
        }

        @Test
        @DisplayName("should rename the appointments when only the name's case changes")
        void shouldRenameUpcomingAppointments_whenOnlyCaseChanges() {
            manager.rename(loggedInInfo, ITEM_ID, "MIDTOWN");

            verify(appointmentDao).renameLocationFrom(ITEM_ID, "MIDTOWN", TODAY, "999998");
        }

        @Test
        @DisplayName("should leave appointments alone when the name is unchanged")
        void shouldNotRenameAppointments_whenNameUnchanged() {
            assertThat(manager.rename(loggedInInfo, ITEM_ID, " Midtown ")).isTrue();

            verify(appointmentDao, never()).renameLocationFrom(anyInt(), any(), any(), any());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("should reject a blank name, which would leave an unnamed choice")
        void shouldThrow_whenBlank(String name) {
            assertThatThrownBy(() -> manager.rename(loggedInInfo, ITEM_ID, name))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(lookupListItemDao, never()).merge(any());
            verify(appointmentDao, never()).renameLocationFrom(anyInt(), any(), any(), any());
        }

        @Test
        @DisplayName("should keep a location's name short enough for an appointment's copy")
        void shouldThrow_whenLongerThanAppointmentColumn() {
            String tooLong = "x".repeat(LocationList.LABEL_MAX_LENGTH + 1);

            assertThatThrownBy(() -> manager.rename(loggedInInfo, ITEM_ID, tooLong))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(lookupListItemDao, never()).merge(any());
            verify(appointmentDao, never()).renameLocationFrom(anyInt(), any(), any(), any());
            assertThat(manager.rename(loggedInInfo, ITEM_ID, "x".repeat(LocationList.LABEL_MAX_LENGTH))).isTrue();
        }

        @Test
        @DisplayName("should return false when the location does not exist")
        void shouldReturnFalse_whenLocationMissing() {
            assertThat(manager.rename(loggedInInfo, 99, "Midtown")).isFalse();

            verify(lookupListItemDao, never()).merge(any());
        }

        @Test
        @DisplayName("should rename nothing when the item belongs to another look-up list")
        void shouldReturnFalse_whenItemOnOtherList() {
            location.setLookupListId(LOCATION_LIST_ID + 1);

            assertThat(manager.rename(loggedInInfo, ITEM_ID, "Midtown Clinic")).isFalse();

            assertThat(location.getLabel()).isEqualTo("Midtown");
            verify(lookupListItemDao, never()).merge(any());
            verify(appointmentDao, never()).renameLocationFrom(anyInt(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("countUpcomingAppointments")
    class CountUpcomingAppointments {

        @Test
        @DisplayName("should count each location's appointments from today on")
        void shouldCountFromToday_whenAsked() {
            Map<Integer, Long> counts = Map.of(ITEM_ID, 3L);
            when(securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.READ, null)).thenReturn(true);
            when(appointmentDao.countByLocationCodeFrom(TODAY)).thenReturn(counts);

            assertThat(manager.countUpcomingAppointments(loggedInInfo)).isSameAs(counts);
        }

        @Test
        @DisplayName("should refuse the count without _admin read")
        void shouldThrow_whenNoPrivilege() {
            assertThatThrownBy(() -> manager.countUpcomingAppointments(loggedInInfo))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Access Denied");

            verify(appointmentDao, never()).countByLocationCodeFrom(any());
        }
    }
}
