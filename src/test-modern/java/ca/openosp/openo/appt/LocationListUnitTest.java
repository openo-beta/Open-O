package ca.openosp.openo.appt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import ca.openosp.openo.commn.IsPropertiesOn;
import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.PMmodule.service.ProgramManager;
import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.managers.AppointmentLocationManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.test.unit.OscarPropertiesOverride;
import ca.openosp.openo.utility.LoggedInInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link LocationList}: item lookup, the choices a booking offers, the Default
 * Location it starts on and whether the Location Requirement applies, the Location Mode precedence,
 * how a booking form's posted choice is saved, and the saved name an appointment shows.
 *
 * @since 2026-09-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LocationList unit tests")
@Tag("unit")
@Tag("fast")
@Tag("appointment")
public class LocationListUnitTest extends OpenOUnitTestBase {

    private static final int LOCATION_LIST_ID = 5;

    @Mock
    private AppointmentLocationManager locationManager;

    @Mock
    private ProgramManager programManager;

    private final LookupListItem room1 = item(11, "Room 1", true);
    private final LookupListItem retired = item(12, "Old Annex", false);
    private final LookupListItem room2 = item(13, "Room 2", true);

    /** The properties that decide the Location Mode; each test starts with none set. */
    private static final String[] MODE_KEYS = {"multisites", "scheduleSiteID", "ModuleNames", "useProgramLocation"};

    @RegisterExtension
    final OscarPropertiesOverride properties = new OscarPropertiesOverride();

    private LookupList locationList;

    /** The site setups, each with the properties that turn it on and the Location Mode it gives. */
    enum SiteSetup {
        MULTISITES(LocationList.LocationMode.SITES, Map.of("multisites", "on")),
        SCHEDULE_SITES(LocationList.LocationMode.SCHEDULE_SITES, Map.of("scheduleSiteID", "site1|site2")),
        PROGRAMS(LocationList.LocationMode.PROGRAMS, Map.of("ModuleNames", "HRM,Caisi", "useProgramLocation", "true"));

        final LocationList.LocationMode mode;
        private final Map<String, String> properties;

        SiteSetup(LocationList.LocationMode mode, Map<String, String> properties) {
            this.mode = mode;
            this.properties = properties;
        }

        void turnOn(OscarPropertiesOverride override) {
            properties.forEach(override::set);
        }
    }

    @BeforeEach
    void clearModeProperties() {
        properties.remove(MODE_KEYS);
    }

    @BeforeEach
    void setUp() {
        registerMock(AppointmentLocationManager.class, locationManager);
        registerMock(ProgramManager.class, programManager);
        locationList = new LookupList();
        locationList.setId(LOCATION_LIST_ID);
        locationList.setName("appointmentLocationCode");
        locationList.setItems(List.of(room1, retired, room2));
        lenient().when(locationManager.findLocationList(any())).thenReturn(locationList);
    }

    private void programLocations() {
        SiteSetup.PROGRAMS.turnOn(properties);
    }

    private static Program program(String name, String location) {
        Program program = new Program();
        program.setName(name);
        program.setLocation(location);
        return program;
    }

    private static LookupListItem item(int id, String label, boolean active) {
        LookupListItem item = new LookupListItem();
        item.setId(id);
        item.setLookupListId(LOCATION_LIST_ID);
        item.setLabel(label);
        item.setActive(active);
        return item;
    }

    @Nested
    @DisplayName("items")
    class Items {

        @Test
        @DisplayName("should find any item of the list, active or not")
        void shouldFindItem_whenInList() {
            LocationList locations = LocationList.of(locationList);

            assertThat(locations.find(11)).isSameAs(room1);
            assertThat(locations.find(12)).isSameAs(retired);
        }

        @Test
        @DisplayName("should find nothing for a null id or one outside the list")
        void shouldFindNothing_whenIdNullOrElsewhere() {
            LocationList locations = LocationList.of(locationList);

            assertThat(locations.find(null)).isNull();
            assertThat(locations.find(99)).isNull();
        }

        @Test
        @DisplayName("should offer only active items to a new booking, in display order")
        void shouldListActiveItems_whenNewBooking() {
            assertThat(LocationList.of(locationList).getActiveItems()).containsExactly(room1, room2);
        }

        @Test
        @DisplayName("should be empty when the list is missing")
        void shouldBeEmpty_whenListMissing() {
            LocationList locations = LocationList.of(null);

            assertThat(locations.getActiveItems()).isEmpty();
            assertThat(locations.find(11)).isNull();
            assertThat(locations.isLocationMode()).isFalse();
        }
    }

    @Nested
    @DisplayName("names")
    class Names {

        @Test
        @DisplayName("should list the active locations in order, then the inactive ones by name")
        void shouldListActiveFirstThenInactiveByName_whenListed() {
            LookupListItem annexB = item(14, "annex B", false);
            LookupListItem clinicA = item(15, "Clinic A", false);
            locationList.setItems(List.of(room1, clinicA, retired, annexB, room2));

            assertThat(LocationList.of(locationList).getItemsActiveFirst())
                    .containsExactly(room1, room2, annexB, clinicA, retired);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Room 2", "room 2", "  ROOM 2  "})
        @DisplayName("should call a name taken when another location in use has it, ignoring case and spaces")
        void shouldBeTaken_whenAnotherActiveHasIt(String name) {
            assertThat(LocationList.of(locationList).isNameTaken(name, room1)).isTrue();
        }

        @Test
        @DisplayName("should let a location keep its own name, in any case")
        void shouldBeFree_whenOnlyItsOwn() {
            assertThat(LocationList.of(locationList).isNameTaken("ROOM 1", room1)).isFalse();
        }

        @Test
        @DisplayName("should not count an inactive location, which no new booking offers")
        void shouldBeFree_whenOnlyInactiveHasIt() {
            assertThat(LocationList.of(locationList).isNameTaken("Old Annex", room1)).isFalse();
        }

        @Test
        @DisplayName("should catch the clash when an inactive location would be enabled under an active name")
        void shouldBeTaken_whenRestoringOntoActiveName() {
            LookupListItem oldRoom2 = item(16, "room 2", false);
            locationList.setItems(List.of(room1, room2, oldRoom2));

            assertThat(LocationList.of(locationList).isNameTaken(oldRoom2.getLabel(), oldRoom2)).isTrue();
        }
    }

    @Nested
    @DisplayName("parseCode")
    class ParseCode {

        @Test
        @DisplayName("should read a posted item id")
        void shouldReadId_whenNumeric() {
            assertThat(LocationList.parseCode("13")).isEqualTo(13);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"abc", "0", "-3", "99999999999", "1.5"})
        @DisplayName("should read nothing from a blank, invalid or non-positive value")
        void shouldReadNothing_whenNotAnId(String posted) {
            assertThat(LocationList.parseCode(posted)).isNull();
        }
    }

    @Nested
    @DisplayName("getMode")
    class Modes {

        private LocationList.LocationMode mode() {
            return LocationList.of(locationList).getMode();
        }

        @Test
        @DisplayName("should use the list when it has an active item and no site setup applies")
        void shouldBeLocationList_whenActiveItemAndNoSites() {
            assertThat(mode()).isEqualTo(LocationList.LocationMode.LOCATION_LIST);
            assertThat(LocationList.of(locationList).isLocationMode()).isTrue();
        }

        @Test
        @DisplayName("should stay free text while the list has no active item")
        void shouldBeFreeText_whenNoActiveItem() {
            locationList.setItems(List.of(retired));

            assertThat(mode()).isEqualTo(LocationList.LocationMode.FREE_TEXT);
            assertThat(LocationList.of(locationList).isLocationMode()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(SiteSetup.class)
        @DisplayName("should give way to a site setup")
        void shouldBeSiteSetup_whenOneIsOn(SiteSetup setup) {
            setup.turnOn(properties);

            assertThat(mode()).isEqualTo(setup.mode);
            assertThat(mode().isSiteSetup()).isTrue();
        }

        @Test
        @DisplayName("should still use the list when CAISI is on without program locations")
        void shouldBeLocationList_whenCaisiWithoutProgramLocations() {
            properties.set("ModuleNames", "Caisi");

            assertThat(mode()).isEqualTo(LocationList.LocationMode.LOCATION_LIST);
        }

        @Test
        @DisplayName("should put sites before programs, and programs before schedule sites, as the booking screens do")
        void shouldFollowBookingScreenOrder_whenSetupsOverlap() {
            SiteSetup.PROGRAMS.turnOn(properties);
            SiteSetup.SCHEDULE_SITES.turnOn(properties);
            assertThat(mode()).isEqualTo(LocationList.LocationMode.PROGRAMS);

            SiteSetup.MULTISITES.turnOn(properties);
            assertThat(mode()).isEqualTo(LocationList.LocationMode.SITES);
        }
    }

    @Nested
    @DisplayName("getDisplayName")
    class DisplayNames {

        private final Appointment appointment = new Appointment();

        @Test
        @DisplayName("should use the name saved with the appointment, not the item's current name")
        void shouldUseSavedName_whenCodeIsItem() {
            appointment.setLocationCode(13);
            appointment.setLocation("Room Two");

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo("Room Two");
        }

        @Test
        @DisplayName("should not fall back to the item's name when the appointment saved none")
        void shouldBeBlank_whenItemButNoSavedName() {
            appointment.setLocationCode(12);

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEmpty();
        }

        @Test
        @DisplayName("should use the saved text when the code is not an item of the list")
        void shouldUseText_whenCodeNotItem() {
            appointment.setLocationCode(99);
            appointment.setLocation(" Front desk ");

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo("Front desk");
        }

        @ParameterizedTest
        @EnumSource(value = SiteSetup.class, names = {"MULTISITES", "SCHEDULE_SITES"})
        @DisplayName("should use the saved text, the site, when the schedule shows sites")
        void shouldUseText_whenSites(SiteSetup setup) {
            appointment.setLocationCode(13);
            appointment.setLocation("site1");
            setup.turnOn(properties);

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo("site1");
        }

        @Test
        @DisplayName("should name the program's location when a program-location booking saved its id")
        void shouldUseProgramLocation_whenProgramSetup() {
            programLocations();
            appointment.setLocationCode(13);
            appointment.setLocation("10034");
            when(programManager.getProgram(10034)).thenReturn(program("Shelter", "East Wing"));

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo("East Wing");
        }

        @Test
        @DisplayName("should name the program when it has no location, as the booking dropdown does")
        void shouldUseProgramName_whenProgramHasNoLocation() {
            programLocations();
            appointment.setLocation("10034");
            when(programManager.getProgram(10034)).thenReturn(program("Shelter", " "));

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo("Shelter");
        }

        @ParameterizedTest
        @ValueSource(strings = {"10099", "Front desk"})
        @DisplayName("should use the saved text when it names no program")
        void shouldUseText_whenNoProgramMatches(String text) {
            programLocations();
            appointment.setLocation(text);

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo(text);
        }

        @Test
        @DisplayName("should look each program up once per list")
        void shouldLookUpProgramOnce_whenNamedTwice() {
            programLocations();
            appointment.setLocation("10034");
            when(programManager.getProgram(10034)).thenReturn(program("Shelter", "East Wing"));
            LocationList locations = LocationList.of(locationList);

            locations.getDisplayName(appointment);
            locations.getDisplayName(appointment);

            verify(programManager, times(1)).getProgram(10034);
        }

        @Test
        @DisplayName("should use the saved text, not a program, when sites and programs are both on")
        void shouldUseText_whenMultisitesAndProgramLocations() {
            programLocations();
            SiteSetup.MULTISITES.turnOn(properties);
            appointment.setLocation("10034");

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo("10034");
            verifyNoInteractions(programManager);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"null", "  "})
        @DisplayName("should be blank when the appointment has no location")
        void shouldBeBlank_whenNoLocation(String text) {
            appointment.setLocation(text);

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getChipItem")
    class ChipItem {

        private final Appointment appointment = new Appointment();

        @Test
        @DisplayName("should give the appointment's item, active or not, when no site setup applies")
        void shouldGiveItem_whenNoSiteSetup() {
            appointment.setLocationCode(12);

            assertThat(LocationList.of(locationList).getChipItem(appointment)).isSameAs(retired);
        }

        @ParameterizedTest
        @EnumSource(SiteSetup.class)
        @DisplayName("should give no chip in a site setup, for a code saved before the switch")
        void shouldGiveNoChip_whenSiteSetup(SiteSetup setup) {
            setup.turnOn(properties);
            appointment.setLocationCode(13);

            assertThat(LocationList.of(locationList).getChipItem(appointment)).isNull();
        }

        @Test
        @DisplayName("should read the site setup once per list, however many appointments ask")
        void shouldReadSiteSetupOnce_whenManyAppointmentsAsk() {
            LocationList locations = LocationList.of(locationList);
            try (MockedStatic<IsPropertiesOn> propertiesOn = mockStatic(IsPropertiesOn.class)) {
                locations.getChipItem(appointment);
                locations.getDisplayName(appointment);
                locations.isLocationMode();

                propertiesOn.verify(IsPropertiesOn::isMultisitesEnable, times(1));
            }
        }
    }

    @Nested
    @DisplayName("setLocationText")
    class SetLocationText {

        private final Appointment appointment = new Appointment();

        @BeforeEach
        void codedAppointment() {
            appointment.setLocation("Room 1");
            appointment.setLocationCode(11);
        }

        @Test
        @DisplayName("should keep the code when the text is unchanged")
        void shouldKeepCode_whenTextUnchanged() {
            LocationList.setLocationText(appointment, "Room 1");

            assertThat(appointment.getLocation()).isEqualTo("Room 1");
            assertThat(appointment.getLocationCode()).isEqualTo(11);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Room 12", "room 1", "Room 1 "})
        @DisplayName("should save the text and clear the code when the text changed")
        void shouldClearCode_whenTextChanged(String text) {
            LocationList.setLocationText(appointment, text);

            assertThat(appointment.getLocation()).isEqualTo(text);
            assertThat(appointment.getLocationCode()).isNull();
        }

        @Test
        @DisplayName("should treat no saved text and blank text as the same text")
        void shouldKeepCode_whenNullBecomesBlank() {
            appointment.setLocation(null);

            LocationList.setLocationText(appointment, "");

            assertThat(appointment.getLocationCode()).isEqualTo(11);
        }
    }

    @Nested
    @DisplayName("dropdown choice")
    class Choices {

        private LocationList locations;

        @BeforeEach
        void wrapList() {
            locations = LocationList.of(locationList);
        }

        @Test
        @DisplayName("should choose a saved booking's item, active or not")
        void shouldChooseItem_whenCodeIsItem() {
            assertThat(locations.choiceFor(13, "Room 2")).isEqualTo(new LocationList.Choice("13", null));
            assertThat(locations.choiceFor(12, "Old Annex")).isEqualTo(new LocationList.Choice("12", null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Room 2", " room 2 ", "ROOM 2"})
        @DisplayName("should choose the active item a typed location names, ignoring case and surrounding spaces")
        void shouldChooseNamedItem_whenTypedTextNamesActiveItem(String typed) {
            assertThat(locations.choiceFor(null, typed)).isEqualTo(new LocationList.Choice("13", null));
        }

        @Test
        @DisplayName("should keep typed text as the Legacy Location when it names only an inactive item")
        void shouldChooseLegacy_whenTypedTextNamesInactiveItem() {
            assertThat(locations.choiceFor(null, "Old Annex")).isEqualTo(new LocationList.Choice(LocationList.LEGACY_VALUE, "Old Annex"));
        }

        @Test
        @DisplayName("should offer and choose the Legacy Location when the code is not an item and the text names none")
        void shouldChooseLegacy_whenTextNamesNoItem() {
            assertThat(locations.choiceFor(null, "Room 5 (old)")).isEqualTo(new LocationList.Choice(LocationList.LEGACY_VALUE, "Room 5 (old)"));
            assertThat(locations.choiceFor(99, "Room 5 (old)")).isEqualTo(new LocationList.Choice(LocationList.LEGACY_VALUE, "Room 5 (old)"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        @DisplayName("should choose the blank option when the booking has no location")
        void shouldChooseBlankOption_whenNoLocation(String location) {
            assertThat(locations.choiceFor(null, location)).isEqualTo(new LocationList.Choice("", null));
        }

        @Test
        @DisplayName("should name an item's code only when an item is chosen")
        void shouldGiveCode_whenItemChosen() {
            assertThat(new LocationList.Choice("13", null).code()).isEqualTo(13);
            assertThat(new LocationList.Choice(LocationList.LEGACY_VALUE, "Room 5 (old)").code()).isNull();
            assertThat(new LocationList.Choice("", null).code()).isNull();
            assertThat(new LocationList.Choice(null, null).code()).isNull();
        }
    }

    @Nested
    @DisplayName("dropdowns")
    class Dropdowns {

        private final LoggedInInfo loggedInInfo = new LoggedInInfo();
        private final Appointment saved = new Appointment();
        private LocationList locations;

        @BeforeEach
        void wrapList() {
            locations = LocationList.of(locationList);
        }

        private List<LookupListItem> offered(LocationList.Dropdown dropdown) {
            return dropdown.options().stream().map(LocationList.Option::item).toList();
        }

        @Test
        @DisplayName("should offer a new booking the active locations under their own names")
        void shouldOfferActiveItems_whenNewBooking() {
            LocationList.Dropdown dropdown = locations.newBookingDropdown(loggedInInfo, null);

            assertThat(dropdown.options()).containsExactly(new LocationList.Option(room1, "Room 1"), new LocationList.Option(room2, "Room 2"));
            assertThat(dropdown.legacy()).isNull();
        }

        @Test
        @DisplayName("should keep the choice a patient search posted back over the Default Location")
        void shouldKeepPostedChoice_whenNewBookingRedisplayed() {
            lenient().when(locationManager.findDefault(loggedInInfo)).thenReturn(13);

            assertThat(locations.newBookingDropdown(loggedInInfo, "11").selected()).isEqualTo("11");
            assertThat(locations.newBookingDropdown(loggedInInfo, "").selected()).isEmpty();
        }

        @Test
        @DisplayName("should make a new booking's dropdown required under the Location Requirement")
        void shouldBeRequired_whenRequirementOn() {
            when(locationManager.isRequired(loggedInInfo)).thenReturn(true);

            assertThat(locations.newBookingDropdown(loggedInInfo, null).required()).isTrue();
        }

        @Test
        @DisplayName("should start an edit on the saved booking's choice, offering its deactivated location in place")
        void shouldStartOnSavedChoice_whenEditFirstDisplayed() {
            saved.setLocationCode(12);
            saved.setLocation("Old Annex");

            LocationList.Dropdown dropdown = locations.editDropdown(saved, null);

            assertThat(dropdown.selected()).isEqualTo("12");
            assertThat(offered(dropdown)).containsExactly(room1, retired, room2);
            assertThat(dropdown.required()).isFalse();
        }

        @Test
        @DisplayName("should offer only active locations when the booking has none or an active one")
        void shouldOfferActiveItems_whenSavedActiveOrNone() {
            assertThat(offered(locations.editDropdown(saved, null))).containsExactly(room1, room2);

            saved.setLocationCode(13);
            saved.setLocation("Room 2");
            assertThat(offered(locations.editDropdown(saved, null))).containsExactly(room1, room2);
        }

        @Test
        @DisplayName("should show the booking's own location under the name it was saved with")
        void shouldShowSavedName_whenItemRenamedSince() {
            saved.setLocationCode(13);
            saved.setLocation("Second Room");

            assertThat(locations.editDropdown(saved, null).options())
                    .containsExactly(new LocationList.Option(room1, "Room 1"), new LocationList.Option(room2, "Second Room"));
        }

        @Test
        @DisplayName("should still offer the saved deactivated location after a patient search posted another")
        void shouldKeepSavedItem_whenPostedAnother() {
            saved.setLocationCode(12);
            saved.setLocation("Old Annex");

            LocationList.Dropdown dropdown = locations.editDropdown(saved, new LocationList.Choice("11", null));

            assertThat(dropdown.selected()).isEqualTo("11");
            assertThat(offered(dropdown)).containsExactly(room1, retired, room2);
        }

        @Test
        @DisplayName("should keep a posted Legacy Location, and offer no deactivated location the booking doesn't have")
        void shouldKeepPostedLegacy_whenRedisplayed() {
            saved.setLocation("Room 5 (old)");

            LocationList.Dropdown dropdown = locations.editDropdown(saved,
                    new LocationList.Choice(LocationList.LEGACY_VALUE, "Room 5 (old)"));

            assertThat(dropdown.selected()).isEqualTo(LocationList.LEGACY_VALUE);
            assertThat(dropdown.legacy()).isEqualTo("Room 5 (old)");
            assertThat(offered(dropdown)).containsExactly(room1, room2);
        }

        @Test
        @DisplayName("should offer what was posted when the booking is gone since the page opened")
        void shouldOfferPostedChoice_whenSavedBookingGone() {
            LocationList.Dropdown dropdown = locations.editDropdown(null, new LocationList.Choice("12", null));

            assertThat(dropdown.selected()).isEqualTo("12");
            assertThat(offered(dropdown)).containsExactly(room1, retired, room2);
        }
    }

    @Nested
    @DisplayName("Default Location")
    class DefaultLocation {

        private final LoggedInInfo loggedInInfo = new LoggedInInfo();
        private LocationList locations;

        @BeforeEach
        void wrapList() {
            locations = LocationList.of(locationList);
        }

        private void defaultSetTo(Integer id) {
            when(locationManager.findDefault(loggedInInfo)).thenReturn(id);
        }

        @Test
        @DisplayName("should start a new booking on the Default Location while it is active")
        void shouldStartOnDefault_whenDefaultActive() {
            defaultSetTo(13);

            assertThat(locations.getDefault(loggedInInfo)).isSameAs(room2);
            assertThat(locations.newBookingDropdown(loggedInInfo, null).selected()).isEqualTo("13");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(ints = {12, 99})
        @DisplayName("should start on the blank option when no default is set, or the one set is inactive or no item of the list")
        void shouldHaveNoDefault_whenNoneOrInactiveOrElsewhere(Integer id) {
            defaultSetTo(id);

            assertThat(locations.getDefault(loggedInInfo)).isNull();
            assertThat(locations.newBookingDropdown(loggedInInfo, null).selected()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Location Requirement")
    class LocationRequirement {

        private final LoggedInInfo loggedInInfo = new LoggedInInfo();

        @Test
        @DisplayName("should require a location on a new booking when the requirement is on")
        void shouldRequire_whenRequirementOn() {
            when(locationManager.isRequired(loggedInInfo)).thenReturn(true);

            assertThat(LocationList.of(locationList).isRequiredForNewBooking(loggedInInfo)).isTrue();
        }

        @Test
        @DisplayName("should not require a location when the requirement is off")
        void shouldNotRequire_whenRequirementOff() {
            when(locationManager.isRequired(loggedInInfo)).thenReturn(false);

            assertThat(LocationList.of(locationList).isRequiredForNewBooking(loggedInInfo)).isFalse();
        }

        @Test
        @DisplayName("should ignore the requirement when no location is active, since bookings take a typed one")
        void shouldNotRequire_whenNoActiveItem() {
            locationList.setItems(List.of(retired));
            lenient().when(locationManager.isRequired(loggedInInfo)).thenReturn(true);

            assertThat(LocationList.of(locationList).isRequiredForNewBooking(loggedInInfo)).isFalse();
        }

        @ParameterizedTest
        @EnumSource(SiteSetup.class)
        @DisplayName("should ignore the requirement in a site setup, which offers its own places")
        void shouldNotRequire_whenSiteSetup(SiteSetup setup) {
            setup.turnOn(properties);
            lenient().when(locationManager.isRequired(loggedInInfo)).thenReturn(true);

            assertThat(LocationList.of(locationList).isRequiredForNewBooking(loggedInInfo)).isFalse();
        }
    }

    @Nested
    @DisplayName("applyPostedLocation")
    class ApplyPostedLocation {

        private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/appointment/appointmentcontrol.jsp");
        private final Appointment appointment = new Appointment();

        @BeforeEach
        void existingAppointment() {
            appointment.setLocation("Before");
            appointment.setLocationCode(7);
            request.setParameter("location", "");
        }

        @Test
        @DisplayName("should keep the code when the form offered no Location List and the text is unchanged")
        void shouldKeepCode_whenNoLocationCodeFieldAndTextUnchanged() {
            request.setParameter("location", "Before");

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocation()).isEqualTo("Before");
            assertThat(appointment.getLocationCode()).isEqualTo(7);
            verifyNoInteractions(locationManager);
        }

        @Test
        @DisplayName("should not read the site setup to save a chosen item, since a repeat booking saves many")
        void shouldNotReadSiteSetup_whenItemChosen() {
            request.setParameter("locationCode", "13");
            try (MockedStatic<IsPropertiesOn> propertiesOn = mockStatic(IsPropertiesOn.class)) {
                LocationList.applyPostedLocation(appointment, request);

                propertiesOn.verifyNoInteractions();
            }
            assertThat(appointment.getLocationCode()).isEqualTo(13);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Front desk", "before", "Before ", ""})
        @DisplayName("should save the typed location and clear the code when the form offered no Location List and the text changed")
        void shouldClearCode_whenNoLocationCodeFieldAndTextChanged(String typed) {
            request.setParameter("location", typed);

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocation()).isEqualTo(typed);
            assertThat(appointment.getLocationCode()).isNull();
            verifyNoInteractions(locationManager);
        }

        @Test
        @DisplayName("should treat no saved text and a blank post as the same text")
        void shouldKeepCode_whenNoSavedTextAndBlankPosted() {
            appointment.setLocation(null);

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocationCode()).isEqualTo(7);
        }

        @Test
        @DisplayName("should save the chosen item's id and label")
        void shouldSaveIdAndLabel_whenItemChosen() {
            request.setParameter("locationCode", "13");

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocationCode()).isEqualTo(13);
            assertThat(appointment.getLocation()).isEqualTo("Room 2");
        }

        @Test
        @DisplayName("should keep a deactivated item an edited booking still carries")
        void shouldSaveItem_whenDeactivated() {
            request.setParameter("locationCode", "12");

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocationCode()).isEqualTo(12);
            assertThat(appointment.getLocation()).isEqualTo("Old Annex");
        }

        @Test
        @DisplayName("should cut a long label to the location column's width")
        void shouldTruncateLabel_whenLongerThanColumn() {
            room2.setLabel("x".repeat(LocationList.LABEL_MAX_LENGTH + 20));
            request.setParameter("locationCode", "13");

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocation()).hasSize(LocationList.LABEL_MAX_LENGTH);
        }

        @Test
        @DisplayName("should clear the code and keep the Legacy Location when it stays chosen")
        void shouldKeepLegacyText_whenLegacyChosen() {
            request.setParameter("location", "Room 5 (old)");
            request.setParameter("locationCode", LocationList.LEGACY_VALUE);

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocationCode()).isNull();
            assertThat(appointment.getLocation()).isEqualTo("Room 5 (old)");
            verifyNoInteractions(locationManager);
        }

        @Test
        @DisplayName("should clear the location when the blank option is chosen, even with a Legacy Location on offer")
        void shouldClearLocation_whenBlankOptionChosen() {
            request.setParameter("location", "Room 5 (old)");
            request.setParameter("locationCode", "");

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocationCode()).isNull();
            assertThat(appointment.getLocation()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"99", "abc"})
        @DisplayName("should not save an id that is not an item of the Location List")
        void shouldClearCode_whenNotAnItemOfList(String posted) {
            request.setParameter("locationCode", posted);

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocationCode()).isNull();
            assertThat(appointment.getLocation()).isEmpty();
        }

        @Nested
        @DisplayName("saved name, by appointment date")
        class SavedNameByDate {

            private final LocalDate today = LocalDate.of(2026, 10, 16);
            private MockedStatic<LocationList> pinnedToday;

            @BeforeEach
            void pinToday() {
                // Pin today, so a run across midnight can't move it between the test's dates and the save's
                pinnedToday = mockStatic(LocationList.class, CALLS_REAL_METHODS);
                pinnedToday.when(LocationList::firstRenamedDate).thenReturn(java.sql.Date.valueOf(today));
            }

            @AfterEach
            void unpinToday() {
                pinnedToday.close();
            }

            /* Saves the appointment, dated days from today and saved at code under name, as the form posts posted. */
            private void save(int days, Integer code, String name, String posted) {
                // A date read from the database is a java.sql.Date, whose toInstant() throws.
                appointment.setAppointmentDate(java.sql.Date.valueOf(today.plusDays(days)));
                appointment.setLocationCode(code);
                appointment.setLocation(name);
                request.setParameter("locationCode", posted);

                LocationList.applyPostedLocation(appointment, request);
            }

            @Test
            @DisplayName("should keep the saved name when an appointment before today stays at its renamed item")
            void shouldKeepSavedName_whenPastAppointmentKeepsItem() {
                save(-1, 13, "Room Two", "13");

                assertThat(appointment.getLocationCode()).isEqualTo(13);
                assertThat(appointment.getLocation()).isEqualTo("Room Two");
            }

            @ParameterizedTest
            @ValueSource(ints = {0, 7})
            @DisplayName("should take the item's current name when an appointment from today on stays at its item")
            void shouldTakeCurrentName_whenAppointmentFromTodayKeepsItem(int daysAhead) {
                save(daysAhead, 13, "Room Two", "13");

                assertThat(appointment.getLocation()).isEqualTo("Room 2");
            }

            @Test
            @DisplayName("should take the current name when an appointment before today moves to another item")
            void shouldTakeCurrentName_whenPastAppointmentChangesItem() {
                save(-1, 11, "Room One", "13");

                assertThat(appointment.getLocationCode()).isEqualTo(13);
                assertThat(appointment.getLocation()).isEqualTo("Room 2");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = " ")
            @DisplayName("should take the current name when an appointment before today saved no name")
            void shouldTakeCurrentName_whenPastAppointmentHasNoSavedName(String saved) {
                save(-1, 13, saved, "13");

                assertThat(appointment.getLocation()).isEqualTo("Room 2");
            }
        }
    }
}
