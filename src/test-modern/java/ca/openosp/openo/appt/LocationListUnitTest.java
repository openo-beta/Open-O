package ca.openosp.openo.appt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.openosp.OscarProperties;
import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.PMmodule.service.ProgramManager;
import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.managers.LookupListManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link LocationList}: item lookup, the choices a booking offers, the Location Mode
 * precedence, and how a booking form's posted choice is saved.
 *
 * @since 2026-09-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LocationList unit tests")
@Tag("unit")
@Tag("fast")
public class LocationListUnitTest extends OpenOUnitTestBase {

    private static final int LOCATION_LIST_ID = 5;

    @Mock
    private LookupListManager lookupListManager;

    @Mock
    private ProgramManager programManager;

    private final LookupListItem room1 = item(11, "Room 1", true);
    private final LookupListItem retired = item(12, "Old Annex", false);
    private final LookupListItem room2 = item(13, "Room 2", true);

    /** The site setups that decide the Location Mode; each test starts with none and ends as it found them. */
    private static final List<String> MODE_KEYS = List.of("multisites", "scheduleSiteID", "ModuleNames", "useProgramLocation");
    private final Map<String, String> savedModeProperties = new HashMap<>();
    private final OscarProperties properties = OscarProperties.getInstance();

    private LookupList locationList;

    @BeforeEach
    void clearModeProperties() {
        for (String key : MODE_KEYS) {
            String value = (String) properties.remove(key);
            if (value != null) {
                savedModeProperties.put(key, value);
            }
        }
    }

    @AfterEach
    void restoreModeProperties() {
        MODE_KEYS.forEach(properties::remove);
        properties.putAll(savedModeProperties);
    }

    @BeforeEach
    void setUp() {
        registerMock(LookupListManager.class, lookupListManager);
        registerMock(ProgramManager.class, programManager);
        locationList = new LookupList();
        locationList.setId(LOCATION_LIST_ID);
        locationList.setName("appointmentLocationCode");
        locationList.setItems(List.of(room1, retired, room2));
        lenient().when(lookupListManager.findAppointmentLocationList(any())).thenReturn(locationList);
    }

    private void programLocations() {
        properties.setProperty("ModuleNames", "HRM,Caisi");
        properties.setProperty("useProgramLocation", "true");
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
        @DisplayName("should keep a booking's deactivated item among its choices, in place")
        void shouldIncludeCurrentItem_whenDeactivated() {
            assertThat(LocationList.of(locationList).getChoices(12)).containsExactly(room1, retired, room2);
        }

        @Test
        @DisplayName("should offer only active items when the booking has no or an active item")
        void shouldListActiveItems_whenCurrentActiveOrNone() {
            LocationList locations = LocationList.of(locationList);

            assertThat(locations.getChoices(null)).containsExactly(room1, room2);
            assertThat(locations.getChoices(13)).containsExactly(room1, room2);
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
        void shouldListActiveFirstThenInactiveByName() {
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
    @DisplayName("isLocationMode")
    class LocationMode {

        @Test
        @DisplayName("should use the list when it has an active item and no site setup applies")
        void shouldBeLocationMode_whenActiveItemAndNoSites() {
            assertThat(LocationList.of(locationList).isLocationMode()).isTrue();
        }

        @Test
        @DisplayName("should not use the list when it has no active item")
        void shouldNotBeLocationMode_whenNoActiveItem() {
            locationList.setItems(List.of(retired));

            assertThat(LocationList.of(locationList).isLocationMode()).isFalse();
        }

        @Test
        @DisplayName("should give way to multisite sites")
        void shouldNotBeLocationMode_whenMultisites() {
            properties.setProperty("multisites", "on");

            assertThat(LocationList.of(locationList).isLocationMode()).isFalse();
        }

        @Test
        @DisplayName("should give way to schedule sites")
        void shouldNotBeLocationMode_whenScheduleSites() {
            properties.setProperty("scheduleSiteID", "Main|Annex");

            assertThat(LocationList.of(locationList).isLocationMode()).isFalse();
        }

        @Test
        @DisplayName("should give way to CAISI program locations")
        void shouldNotBeLocationMode_whenProgramLocations() {
            properties.setProperty("ModuleNames", "HRM,Caisi");
            properties.setProperty("useProgramLocation", "true");

            assertThat(LocationList.of(locationList).isLocationMode()).isFalse();
        }

        @Test
        @DisplayName("should still use the list when CAISI is on without program locations")
        void shouldBeLocationMode_whenCaisiWithoutProgramLocations() {
            properties.setProperty("ModuleNames", "Caisi");

            assertThat(LocationList.of(locationList).isLocationMode()).isTrue();
        }
    }

    @Nested
    @DisplayName("getDisplayName")
    class DisplayNames {

        private final Appointment appointment = new Appointment();

        @Test
        @DisplayName("should name the item's current name when the code is an item of the list")
        void shouldUseCurrentName_whenCodeIsItem() {
            appointment.setLocationCode(13);
            appointment.setLocation("Room 2 (booked name)");

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo("Room 2");
        }

        @Test
        @DisplayName("should name an inactive item without marking it")
        void shouldUseName_whenItemInactive() {
            appointment.setLocationCode(12);

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo("Old Annex");
        }

        @Test
        @DisplayName("should use the saved text when the code is not an item of the list")
        void shouldUseText_whenCodeNotItem() {
            appointment.setLocationCode(99);
            appointment.setLocation(" Front desk ");

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo("Front desk");
        }

        @Test
        @DisplayName("should use the saved text, the site, when the schedule shows sites")
        void shouldUseText_whenMultisites() {
            appointment.setLocationCode(13);
            appointment.setLocation("Downtown");
            properties.setProperty("multisites", "on");

            assertThat(LocationList.of(locationList).getDisplayName(appointment)).isEqualTo("Downtown");
        }

        @Test
        @DisplayName("should use the saved text, the site, when the schedule shows schedule sites")
        void shouldUseText_whenScheduleSites() {
            appointment.setLocationCode(13);
            appointment.setLocation("site1");
            properties.setProperty("scheduleSiteID", "site1|site2");

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
            properties.setProperty("multisites", "on");
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
        @ValueSource(strings = {"multisites", "scheduleSiteID", "programs"})
        @DisplayName("should give no chip in a site setup, for a code saved before the switch")
        void shouldGiveNoChip_whenSiteSetup(String setup) {
            switch (setup) {
                case "multisites" -> properties.setProperty("multisites", "on");
                case "scheduleSiteID" -> properties.setProperty("scheduleSiteID", "site1|site2");
                default -> programLocations();
            }
            appointment.setLocationCode(13);

            assertThat(LocationList.of(locationList).getChipItem(appointment)).isNull();
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
        @DisplayName("should choose Not specified when the booking has no location")
        void shouldChooseNotSpecified_whenNoLocation(String location) {
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
            verifyNoInteractions(lookupListManager);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Front desk", "before", "Before ", ""})
        @DisplayName("should save the typed location and clear the code when the form offered no Location List and the text changed")
        void shouldClearCode_whenNoLocationCodeFieldAndTextChanged(String typed) {
            request.setParameter("location", typed);

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocation()).isEqualTo(typed);
            assertThat(appointment.getLocationCode()).isNull();
            verifyNoInteractions(lookupListManager);
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
            verifyNoInteractions(lookupListManager);
        }

        @Test
        @DisplayName("should clear the location when Not specified is chosen, even with a Legacy Location on offer")
        void shouldClearLocation_whenNotSpecifiedChosen() {
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
    }
}
