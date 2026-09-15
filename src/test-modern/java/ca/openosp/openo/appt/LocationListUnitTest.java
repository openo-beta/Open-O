package ca.openosp.openo.appt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.openosp.OscarProperties;
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

    private final LookupListItem room1 = item(11, "Room 1", true);
    private final LookupListItem retired = item(12, "Old Annex", false);
    private final LookupListItem room2 = item(13, "Room 2", true);

    private LookupList locationList;

    @BeforeEach
    void setUp() {
        registerMock(LookupListManager.class, lookupListManager);
        locationList = new LookupList();
        locationList.setId(LOCATION_LIST_ID);
        locationList.setName("appointmentLocationCode");
        locationList.setItems(List.of(room1, retired, room2));
        lenient().when(lookupListManager.findAppointmentLocationList(any())).thenReturn(locationList);
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
        @DisplayName("should offer a booking's text as its Legacy Location only when its code is not an item")
        void shouldOfferLegacyLocation_whenCodeNotAnItem() {
            LocationList locations = LocationList.of(locationList);

            assertThat(locations.getLegacyLocation(null, "Room 5 (old)")).isEqualTo("Room 5 (old)");
            assertThat(locations.getLegacyLocation(99, "Room 5 (old)")).isEqualTo("Room 5 (old)");
            assertThat(locations.getLegacyLocation(13, "Room 2")).isNull();
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

        private static final List<String> KEYS = List.of("multisites", "scheduleSiteID", "ModuleNames", "useProgramLocation");
        private final Map<String, String> saved = new HashMap<>();
        private final OscarProperties properties = OscarProperties.getInstance();

        @BeforeEach
        void clearModeProperties() {
            for (String key : KEYS) {
                String value = (String) properties.remove(key);
                if (value != null) {
                    saved.put(key, value);
                }
            }
        }

        @AfterEach
        void restoreModeProperties() {
            KEYS.forEach(properties::remove);
            properties.putAll(saved);
        }

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
        @DisplayName("should save the typed location and leave the code when the form offered no Location List")
        void shouldSaveTextKeepCode_whenNoLocationCodeField() {
            request.setParameter("location", "Front desk");

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocation()).isEqualTo("Front desk");
            assertThat(appointment.getLocationCode()).isEqualTo(7);
            verifyNoInteractions(lookupListManager);
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
        void shouldClearCodeKeepText_whenBlank() {
            request.setParameter("location", "Room 5 (old)");
            request.setParameter("locationCode", "");

            LocationList.applyPostedLocation(appointment, request);

            assertThat(appointment.getLocationCode()).isNull();
            assertThat(appointment.getLocation()).isEqualTo("Room 5 (old)");
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
