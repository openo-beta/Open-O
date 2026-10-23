package ca.openosp.openo.appt.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.managers.AppointmentLocationManager;
import ca.openosp.openo.managers.LookupListManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;

import com.opensymphony.xwork2.ActionContext;
import org.apache.struts2.ServletActionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppointmentLocation2Action}: what the Location tab shows, that every change
 * needs update on _admin and acts only on a Location List item, and which manager each change goes
 * to. The page handling it shares with the Status tab is tested in {@link AppointmentSettingsActionUnitTest},
 * and the rules on names, order and the Default Location in the LocationList and manager tests.
 *
 * @since 2026-09-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentLocation2Action unit tests")
@Tag("unit")
@Tag("fast")
@Tag("web")
public class AppointmentLocation2ActionUnitTest extends OpenOUnitTestBase {

    /** Every change the page posts on item ID. */
    private static final List<String> ITEM_CHANGES = List.of("updateColour", "updateIcon", "updateDescription",
            "restore", "deactivate", "moveUp", "moveDown", "makeDefault", "removeDefault");

    static List<String> itemChanges() {
        return ITEM_CHANGES;
    }

    /** Every change the page posts: the item changes, and the Location Requirement's switch. */
    static List<String> changes() {
        return Stream.concat(ITEM_CHANGES.stream(), Stream.of("updateRequirement")).toList();
    }

    @Mock
    private SecurityInfoManager securityInfoManager;

    @Mock
    private LookupListManager lookupListManager;

    @Mock
    private AppointmentLocationManager locationManager;

    private final LoggedInInfo loggedInInfo = new LoggedInInfo();
    private final LookupListItem room = item(11, "Room 11", true);
    private final LookupListItem retired = item(12, "Room 12", false);
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AppointmentLocation2Action action;

    @BeforeEach
    void setUp() {
        registerMock(SecurityInfoManager.class, securityInfoManager);
        registerMock(LookupListManager.class, lookupListManager);
        registerMock(AppointmentLocationManager.class, locationManager);
        locations(room, retired);

        request = new MockHttpServletRequest("POST", "/appointment/apptLocationSetting.do");
        response = new MockHttpServletResponse();
        LoggedInInfo.setLoggedInInfoIntoSession(request.getSession(), loggedInInfo);
        ActionContext context = new ActionContext(new HashMap<>());
        context.put(ServletActionContext.HTTP_REQUEST, request);
        context.put(ServletActionContext.HTTP_RESPONSE, response);
        ActionContext.setContext(context);

        action = new AppointmentLocation2Action();
    }

    @AfterEach
    void clearActionContext() {
        ActionContext.setContext(null);
    }

    private static LookupListItem item(int id, String label, boolean active) {
        LookupListItem item = new LookupListItem();
        item.setId(id);
        item.setLabel(label);
        item.setActive(active);
        return item;
    }

    private void locations(LookupListItem... items) {
        LookupList list = new LookupList();
        list.setName("appointmentLocationCode");
        list.setItems(List.of(items));
        lenient().when(locationManager.findLocationList(any())).thenReturn(list);
    }

    private void grant(String object, String privilege) {
        lenient().when(securityInfoManager.hasPrivilege(loggedInInfo, object, privilege, null)).thenReturn(true);
    }

    private String post(String dispatch, String id, String value) {
        request.setParameter("dispatch", dispatch);
        request.setParameter("ID", id);
        request.setParameter("value", value);
        return action.execute();
    }

    @Nested
    @DisplayName("viewing")
    class Viewing {

        @BeforeEach
        void readOnly() {
            request.setMethod("GET");
            grant("_admin.schedule", SecurityInfoManager.READ);
        }

        @Test
        @DisplayName("should list the locations, the Default Location and the Location Requirement")
        void shouldShowLocations_whenRead() {
            when(locationManager.findDefault(loggedInInfo)).thenReturn(11);
            when(locationManager.isRequired(loggedInInfo)).thenReturn(true);

            assertThat(action.execute()).isEqualTo("success");

            assertThat(request.getAttribute("locations")).isEqualTo(List.of(room, retired));
            assertThat(request.getAttribute("activeLocationCount")).isEqualTo(1);
            assertThat(request.getAttribute("locationListName")).isEqualTo("appointmentLocationCode");
            assertThat(request.getAttribute("defaultLocation")).isSameAs(room);
            assertThat(request.getAttribute("locationRequired")).isEqualTo(true);
        }

        @Test
        @DisplayName("should offer changes only with update on _admin, which the managers check")
        void shouldOfferChanges_whenAdminUpdate() {
            grant("_admin.schedule", SecurityInfoManager.UPDATE);
            action.execute();
            assertThat(request.getAttribute("canChange")).isEqualTo(false);

            grant("_admin", SecurityInfoManager.UPDATE);
            action.execute();
            assertThat(request.getAttribute("canChange")).isEqualTo(true);
        }

        @Test
        @DisplayName("should offer disabling a location only with delete on _admin")
        void shouldOfferDeactivate_whenAdminDelete() {
            grant("_admin", SecurityInfoManager.UPDATE);
            action.execute();
            assertThat(request.getAttribute("canDeactivate")).isEqualTo(false);

            grant("_admin", SecurityInfoManager.DELETE);
            action.execute();
            assertThat(request.getAttribute("canDeactivate")).isEqualTo(true);
        }

        @Test
        @DisplayName("should offer adding locations only with write on _admin, which the manager's Add needs")
        void shouldOfferAdd_whenAdminWrite() {
            grant("_admin", SecurityInfoManager.UPDATE);
            action.execute();
            assertThat(request.getAttribute("canAdd")).isEqualTo(false);

            grant("_admin", SecurityInfoManager.WRITE);
            action.execute();
            assertThat(request.getAttribute("canAdd")).isEqualTo(true);
        }

        @Test
        @DisplayName("should count each location's upcoming appointments, which a rename asks about, only for a user who may change them")
        void shouldCountUpcomingAppointments_whenAdminUpdate() {
            Map<Integer, Long> counts = Map.of(11, 3L);
            when(locationManager.countUpcomingAppointments(loggedInInfo)).thenReturn(counts);
            action.execute();
            assertThat(request.getAttribute("upcomingCounts")).isNull();

            grant("_admin", SecurityInfoManager.UPDATE);
            action.execute();
            assertThat(request.getAttribute("upcomingCounts")).isSameAs(counts);
        }
    }

    @Nested
    @DisplayName("refusing a change before it runs")
    class Refusing {

        @ParameterizedTest
        @MethodSource("ca.openosp.openo.appt.web.AppointmentLocation2ActionUnitTest#changes")
        @DisplayName("should refuse every change with update on another Appointment Settings object only")
        void shouldThrow_whenNoAdminUpdate(String dispatch) {
            grant("_admin.schedule", SecurityInfoManager.UPDATE);
            grant("_admin", SecurityInfoManager.DELETE);

            assertThatThrownBy(() -> post(dispatch, "11", "true")).isInstanceOf(SecurityException.class);

            verifyNoInteractions(lookupListManager, locationManager);
        }

        @ParameterizedTest
        @MethodSource("ca.openosp.openo.appt.web.AppointmentLocation2ActionUnitTest#changes")
        @DisplayName("should refuse every change that is not posted")
        void shouldThrow_whenNotPosted(String dispatch) {
            grant("_admin", SecurityInfoManager.UPDATE);
            grant("_admin", SecurityInfoManager.DELETE);
            request.setMethod("GET");

            assertThatThrownBy(() -> post(dispatch, "11", "true")).isInstanceOf(SecurityException.class);

            verifyNoInteractions(lookupListManager, locationManager);
        }
    }

    @Nested
    @DisplayName("with update on _admin")
    class Changes {

        @BeforeEach
        void allowUpdate() {
            grant("_admin", SecurityInfoManager.UPDATE);
        }

        @ParameterizedTest
        @MethodSource("ca.openosp.openo.appt.web.AppointmentLocation2ActionUnitTest#itemChanges")
        @DisplayName("should refuse every change to an item from another lookup list")
        void shouldRefuse_whenItemNotInLocationList(String dispatch) {
            grant("_admin", SecurityInfoManager.DELETE);

            assertThat(post(dispatch, "99", "Midtown")).isEqualTo("success");

            assertThat(response.getStatus()).isEqualTo(400);
            verifyNoInteractions(lookupListManager);
            verify(locationManager, never()).rename(any(), anyInt(), anyString());
            verify(locationManager, never()).makeDefault(any(), anyInt());
            verify(locationManager, never()).removeDefault(any(), anyInt());
        }

        @Nested
        @DisplayName("colour and icon")
        class Style {

            @Test
            @DisplayName("should save the colour and return to the row that changed")
            void shouldRedirectToRow_whenColourSaved() {
                when(lookupListManager.updateLookupListItemColour(loggedInInfo, 11, "#445566")).thenReturn(true);

                assertThat(post("updateColour", "11", "#445566")).isEqualTo("saved");
                assertThat(action.getAnchor()).isEqualTo("#location-11");
            }

            @Test
            @DisplayName("should pass a blank icon on, which clears it, even on a disabled location")
            void shouldClearIcon_whenValueBlank() {
                when(lookupListManager.updateLookupListItemIcon(loggedInInfo, 12, "")).thenReturn(true);

                assertThat(post("updateIcon", "12", "")).isEqualTo("saved");
            }
        }

        @Nested
        @DisplayName("renaming")
        class Renaming {

            @Test
            @DisplayName("should save the trimmed name, which also renames upcoming appointments")
            void shouldRename_whenNameFree() {
                when(locationManager.rename(loggedInInfo, 11, "Midtown")).thenReturn(true);

                assertThat(post("updateDescription", "11", "  Midtown  ")).isEqualTo("saved");
            }

            @Test
            @DisplayName("should say so when the name is longer than a booking can store, and keep one that fits")
            void shouldRefuse_whenNameTooLong() {
                assertThat(post("updateDescription", "11", "x".repeat(81))).isEqualTo("success");

                assertThat(request.getAttribute("saveFailedKey")).isEqualTo("admin.appt.location.msg.nameTooLong");
                verify(locationManager, never()).rename(any(), anyInt(), anyString());

                when(locationManager.rename(loggedInInfo, 11, "x".repeat(80))).thenReturn(true);
                assertThat(post("updateDescription", "11", "x".repeat(80))).isEqualTo("saved");
            }

            @Test
            @DisplayName("should say so when another location in use already has the name")
            void shouldRefuse_whenNameInUse() {
                locations(item(11, "Midtown", true), item(13, "Eastside", true));

                assertThat(post("updateDescription", "11", "EASTSIDE")).isEqualTo("success");

                assertThat(request.getAttribute("saveFailedKey")).isEqualTo("admin.appt.location.msg.nameInUse");
                assertThat(request.getAttribute("saveFailedParam")).isEqualTo("EASTSIDE");
                verify(locationManager, never()).rename(any(), anyInt(), anyString());
            }
        }

        @Nested
        @DisplayName("enabling and disabling")
        class Lifecycle {

            @Test
            @DisplayName("should enable a location")
            void shouldRestore_whenNameFree() {
                when(lookupListManager.restoreLookupListItem(loggedInInfo, 12)).thenReturn(true);

                assertThat(post("restore", "12", "")).isEqualTo("saved");
            }

            @Test
            @DisplayName("should say so when enabling a location whose name an active one has")
            void shouldRefuse_whenRestoringOntoActiveName() {
                locations(item(11, "Midtown", true), item(12, "midtown", false));

                assertThat(post("restore", "12", "")).isEqualTo("success");

                assertThat(request.getAttribute("saveFailedKey")).isEqualTo("admin.appt.location.msg.nameInUse");
                verify(lookupListManager, never()).restoreLookupListItem(any(), anyInt());
            }

            @Test
            @DisplayName("should refuse disabling without delete on _admin, which the Look-Up List Manager requires")
            void shouldThrow_whenNoAdminDelete() {
                assertThatThrownBy(() -> post("deactivate", "11", "")).isInstanceOf(SecurityException.class);

                verify(lookupListManager, never()).removeLookupListItem(any(), anyInt());
            }

            @Test
            @DisplayName("should disable a location with delete on _admin")
            void shouldDeactivate_whenAdminDelete() {
                grant("_admin", SecurityInfoManager.DELETE);
                when(lookupListManager.removeLookupListItem(loggedInInfo, 11)).thenReturn(true);

                assertThat(post("deactivate", "11", "")).isEqualTo("saved");
            }
        }

        @Nested
        @DisplayName("moving")
        class Moving {

            @Test
            @DisplayName("should move the location either way and return to its row")
            void shouldMove_whenAsked() {
                when(lookupListManager.moveLookupListItem(loggedInInfo, 11, true)).thenReturn(true);
                when(lookupListManager.moveLookupListItem(loggedInInfo, 11, false)).thenReturn(true);

                assertThat(post("moveUp", "11", "")).isEqualTo("saved");
                assertThat(post("moveDown", "11", "")).isEqualTo("saved");
                assertThat(action.getAnchor()).isEqualTo("#location-11");
            }

            @Test
            @DisplayName("should show the list with an error at the end of the order")
            void shouldShowError_whenAlreadyAtTheEnd() {
                assertThat(post("moveUp", "11", "")).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
            }
        }

        @Nested
        @DisplayName("the Default Location")
        class Default {

            @Test
            @DisplayName("should make the location the default and return to its row")
            void shouldMakeDefault_whenAsked() {
                assertThat(post("makeDefault", "11", "")).isEqualTo("saved");

                verify(locationManager).makeDefault(loggedInInfo, 11);
                assertThat(action.getAnchor()).isEqualTo("#location-11");
            }

            @Test
            @DisplayName("should show the list with an error when the manager refuses the default")
            void shouldRefuse_whenManagerRefusesDefault() {
                doThrow(new IllegalArgumentException("inactive")).when(locationManager).makeDefault(loggedInInfo, 12);

                assertThat(post("makeDefault", "12", "")).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
            }

            @Test
            @DisplayName("should ask the manager to remove the default, even while that location is inactive")
            void shouldRemoveDefault_whenAsked() {
                assertThat(post("removeDefault", "12", "")).isEqualTo("saved");

                verify(locationManager).removeDefault(loggedInInfo, 12);
            }
        }

        @Nested
        @DisplayName("the Location Requirement")
        class Requirement {

            /* Posts the switch as the page does: value=true while it is on, nothing once it is off. */
            private String switchTo(boolean on) {
                request.setParameter("dispatch", "updateRequirement");
                if (on) {
                    request.setParameter("value", "true");
                }
                return action.execute();
            }

            @Test
            @DisplayName("should turn the requirement on and return to the top of the page")
            void shouldTurnOn_whenSwitchedOn() {
                assertThat(switchTo(true)).isEqualTo("saved");

                verify(locationManager).setRequired(loggedInInfo, true);
                assertThat(action.getAnchor()).isEmpty();
            }

            @Test
            @DisplayName("should turn the requirement off when the switch posts nothing, as an unchecked one does")
            void shouldTurnOff_whenSwitchedOff() {
                assertThat(switchTo(false)).isEqualTo("saved");

                verify(locationManager).setRequired(loggedInInfo, false);
            }
        }
    }

    @Nested
    @DisplayName("counting a location's upcoming appointments for a rename's confirmation")
    class UpcomingCount {

        @BeforeEach
        void asked() {
            request.setMethod("GET");
            request.setParameter("dispatch", "upcomingCount");
        }

        @Test
        @DisplayName("should answer the location's count as of now as JSON, on a GET")
        void shouldAnswerCount_whenAdminUpdate() throws Exception {
            grant("_admin", SecurityInfoManager.UPDATE);
            when(locationManager.countUpcomingAppointments(loggedInInfo)).thenReturn(Map.of(11, 3L));
            request.setParameter("ID", "11");

            assertThat(action.execute()).isNull();

            assertThat(response.getContentType()).startsWith("application/json");
            assertThat(response.getContentAsString()).isEqualTo("{\"count\":3}");
        }

        @Test
        @DisplayName("should answer 0 for a location with no upcoming appointments")
        void shouldAnswerZero_whenNoneUpcoming() throws Exception {
            grant("_admin", SecurityInfoManager.UPDATE);
            when(locationManager.countUpcomingAppointments(loggedInInfo)).thenReturn(Map.of(11, 3L));
            request.setParameter("ID", "12");

            action.execute();

            assertThat(response.getContentAsString()).isEqualTo("{\"count\":0}");
        }

        @Test
        @DisplayName("should tell a user who may not rename nothing")
        void shouldThrow_whenNoAdminUpdate() {
            grant("_admin.schedule", SecurityInfoManager.UPDATE);
            grant("_admin", SecurityInfoManager.READ);
            request.setParameter("ID", "11");

            assertThatThrownBy(() -> action.execute()).isInstanceOf(SecurityException.class);

            verifyNoInteractions(locationManager);
        }

        @Test
        @DisplayName("should answer 400 for a malformed id, which the page never sends")
        void shouldAnswerBadRequest_whenIdMalformed() {
            grant("_admin", SecurityInfoManager.UPDATE);
            request.setParameter("ID", "abc");

            assertThat(action.execute()).isNull();

            assertThat(response.getStatus()).isEqualTo(400);
            verifyNoInteractions(locationManager);
        }
    }
}
