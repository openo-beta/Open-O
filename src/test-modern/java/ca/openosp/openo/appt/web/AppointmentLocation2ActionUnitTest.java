package ca.openosp.openo.appt.web;

import java.util.List;

import ca.openosp.OscarProperties;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.managers.LookupListManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;

import com.opensymphony.xwork2.ActionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppointmentLocation2Action}: who may view and change locations, which
 * items a change may touch, the rules on a location's name, and the saved/rejected outcomes.
 *
 * @since 2026-09-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentLocation2Action unit tests")
@Tag("unit")
@Tag("fast")
@Tag("web")
public class AppointmentLocation2ActionUnitTest extends OpenOUnitTestBase {

    @Mock
    private SecurityInfoManager securityInfoManager;

    @Mock
    private LookupListManager lookupListManager;

    private final LoggedInInfo loggedInInfo = new LoggedInInfo();
    private final LookupListItem room = item(11, true);
    private final LookupListItem retired = item(12, false);
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AppointmentLocation2Action action;

    @BeforeEach
    void setUp() {
        registerMock(SecurityInfoManager.class, securityInfoManager);
        registerMock(LookupListManager.class, lookupListManager);

        LookupList locationList = new LookupList();
        locationList.setName("appointmentLocationCode");
        locationList.setItems(List.of(room, retired));
        lenient().when(lookupListManager.findAppointmentLocationList(any())).thenReturn(locationList);

        request = new MockHttpServletRequest("POST", "/appointment/apptLocationSetting.do");
        response = new MockHttpServletResponse();
        LoggedInInfo.setLoggedInInfoIntoSession(request.getSession(), loggedInInfo);
        ActionContext.bind(ActionContext.of().withServletRequest(request).withServletResponse(response));

        action = new AppointmentLocation2Action();
    }

    @AfterEach
    void clearActionContext() {
        ActionContext.clear();
    }

    private static LookupListItem item(int id, boolean active) {
        LookupListItem item = new LookupListItem();
        item.setId(id);
        item.setLabel("Room " + id);
        item.setActive(active);
        return item;
    }

    private void grant(String object, String privilege) {
        lenient().when(securityInfoManager.hasPrivilege(loggedInInfo, object, privilege, null)).thenReturn(true);
    }

    @Nested
    @DisplayName("viewing")
    class Viewing {

        @BeforeEach
        void get() {
            request.setMethod("GET");
        }

        @ParameterizedTest
        @ValueSource(strings = {"_admin", "_admin.userAdmin", "_admin.schedule"})
        @DisplayName("should list the active items with read on any Appointment Settings object")
        void shouldListActiveItems_whenReadOnAnyObject(String object) {
            grant(object, SecurityInfoManager.READ);

            assertThat(action.execute()).isEqualTo("success");

            assertThat(request.getAttribute("locations")).isEqualTo(List.of(room, retired));
            assertThat(request.getAttribute("activeLocationCount")).isEqualTo(1);
            assertThat(request.getAttribute("locationListName")).isEqualTo("appointmentLocationCode");
        }

        @Test
        @DisplayName("should list the inactive locations after the active ones, for enabling one")
        void shouldListInactiveItemsLast_whenRead() {
            grant("_admin", SecurityInfoManager.READ);
            LookupListItem annex = item(13, false);
            LookupListItem hall = item(14, true);
            LookupList locationList = new LookupList();
            locationList.setItems(List.of(annex, room, retired, hall));
            when(lookupListManager.findAppointmentLocationList(any())).thenReturn(locationList);

            action.execute();

            assertThat(request.getAttribute("locations")).isEqualTo(List.of(room, hall, retired, annex));
            assertThat(request.getAttribute("activeLocationCount")).isEqualTo(2);
        }

        @Test
        @DisplayName("should offer disabling a location only with delete on _admin")
        void shouldOfferDeactivate_whenAdminDelete() {
            grant("_admin", SecurityInfoManager.READ);
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
            grant("_admin", SecurityInfoManager.READ);
            grant("_admin", SecurityInfoManager.UPDATE);
            action.execute();
            assertThat(request.getAttribute("canAdd")).isEqualTo(false);

            grant("_admin", SecurityInfoManager.WRITE);
            action.execute();
            assertThat(request.getAttribute("canAdd")).isEqualTo(true);
        }

        @Test
        @DisplayName("should refuse the page without read on any Appointment Settings object")
        void shouldThrow_whenNoReadPrivilege() {
            assertThatThrownBy(() -> action.execute()).isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("should offer changes only with update on _admin")
        void shouldOfferChanges_whenAdminUpdate() {
            grant("_admin.schedule", SecurityInfoManager.READ);
            grant("_admin.schedule", SecurityInfoManager.UPDATE);
            action.execute();
            assertThat(request.getAttribute("canChange")).isEqualTo(false);

            grant("_admin", SecurityInfoManager.UPDATE);
            action.execute();
            assertThat(request.getAttribute("canChange")).isEqualTo(true);
        }

        @Test
        @DisplayName("should show the Status tab only when the schedule uses editable statuses")
        void shouldShowStatusTab_whenEditableStatusesOn() {
            grant("_admin", SecurityInfoManager.READ);
            OscarProperties properties = OscarProperties.getInstance();
            Object saved = properties.get("ENABLE_EDIT_APPT_STATUS");
            try {
                properties.remove("ENABLE_EDIT_APPT_STATUS");
                action.execute();
                assertThat(request.getAttribute("statusTabEnabled")).isEqualTo(false);

                properties.setProperty("ENABLE_EDIT_APPT_STATUS", "yes");
                action.execute();
                assertThat(request.getAttribute("statusTabEnabled")).isEqualTo(true);
            } finally {
                properties.remove("ENABLE_EDIT_APPT_STATUS");
                if (saved != null) {
                    properties.put("ENABLE_EDIT_APPT_STATUS", saved);
                }
            }
        }
    }

    @Nested
    @DisplayName("changes")
    class Changes {

        private String post(String dispatch, String id, String value) {
            request.setParameter("dispatch", dispatch);
            request.setParameter("ID", id);
            request.setParameter("value", value);
            return action.execute();
        }

        @Test
        @DisplayName("should refuse a change with update on another Appointment Settings object only")
        void shouldThrow_whenNoAdminUpdate() {
            grant("_admin.schedule", SecurityInfoManager.UPDATE);

            assertThatThrownBy(() -> post("updateColour", "11", "#445566")).isInstanceOf(SecurityException.class);

            verify(lookupListManager, never()).updateLookupListItemColour(any(), anyInt(), any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"updateColour", "updateIcon", "updateDescription", "restore", "deactivate",
                "moveUp", "moveDown"})
        @DisplayName("should refuse a change that is not posted")
        void shouldThrow_whenChangeNotPosted(String dispatch) {
            grant("_admin", SecurityInfoManager.UPDATE);
            request.setMethod("GET");

            assertThatThrownBy(() -> post(dispatch, "11", "")).isInstanceOf(SecurityException.class);

            verify(lookupListManager, never()).findAppointmentLocationList(any());
        }

        @Nested
        @DisplayName("with update on _admin")
        class WithAdminUpdate {

            @BeforeEach
            void allowUpdate() {
                grant("_admin", SecurityInfoManager.UPDATE);
            }

            @Test
            @DisplayName("should save the colour and return to the row that changed")
            void shouldRedirect_whenColourSaved() {
                when(lookupListManager.updateLookupListItemColour(loggedInInfo, 11, "#445566")).thenReturn(true);

                assertThat(post("updateColour", "11", "#445566")).isEqualTo("saved");
                assertThat(action.getAnchor()).isEqualTo("#location-11");
            }

            @Test
            @DisplayName("should save the icon and redirect to the list")
            void shouldRedirect_whenIconSaved() {
                when(lookupListManager.updateLookupListItemIcon(loggedInInfo, 11, "glyphicon-home")).thenReturn(true);

                assertThat(post("updateIcon", "11", "glyphicon-home")).isEqualTo("saved");
            }

            @Test
            @DisplayName("should pass a blank value on, which clears the style")
            void shouldClear_whenValueBlank() {
                when(lookupListManager.updateLookupListItemIcon(loggedInInfo, 12, "")).thenReturn(true);

                assertThat(post("updateIcon", "12", "")).isEqualTo("saved");
            }

            @Test
            @DisplayName("should refuse an item from another lookup list")
            void shouldShowError_whenItemNotInLocationList() {
                assertThat(post("updateColour", "99", "#445566")).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
                assertThat(request.getAttribute("saveFailed")).isEqualTo(true);
                verify(lookupListManager, never()).updateLookupListItemColour(any(), anyInt(), anyString());
            }

            @Test
            @DisplayName("should show the list with an error when the value is invalid")
            void shouldShowError_whenValueInvalid() {
                when(lookupListManager.updateLookupListItemIcon(loggedInInfo, 11, "home"))
                        .thenThrow(new IllegalArgumentException("not a glyphicon"));

                assertThat(post("updateIcon", "11", "home")).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
                assertThat(request.getAttribute("locations")).isEqualTo(List.of(room, retired));
            }

            @Test
            @DisplayName("should show the list with an error when the id is not a number")
            void shouldShowError_whenIdMalformed() {
                assertThat(post("updateColour", "abc", "#445566")).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
            }
        }
    }

    @Nested
    @DisplayName("managing locations")
    class Managing {

        @BeforeEach
        void allowUpdate() {
            grant("_admin", SecurityInfoManager.UPDATE);
        }

        private String post(String dispatch, String id, String value) {
            request.setParameter("dispatch", dispatch);
            request.setParameter("ID", id);
            request.setParameter("value", value);
            return action.execute();
        }

        private void locations(LookupListItem... items) {
            LookupList list = new LookupList();
            list.setName("appointmentLocationCode");
            list.setItems(List.of(items));
            lenient().when(lookupListManager.findAppointmentLocationList(any())).thenReturn(list);
        }

        private LookupListItem named(int id, String label, boolean active) {
            LookupListItem item = item(id, active);
            item.setLabel(label);
            return item;
        }

        @Nested
        @DisplayName("renaming")
        class Renaming {

            @Test
            @DisplayName("should save the trimmed name and redirect to the list")
            void shouldRedirect_whenRenamed() {
                when(lookupListManager.updateLookupListItemLabel(loggedInInfo, 11, "Midtown")).thenReturn(true);

                assertThat(post("updateDescription", "11", "  Midtown  ")).isEqualTo("saved");
            }

            @Test
            @DisplayName("should refuse a name longer than a booking can store")
            void shouldRefuse_whenNameTooLong() {
                assertThat(post("updateDescription", "11", "x".repeat(81))).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
                assertThat(request.getAttribute("saveFailedKey")).isEqualTo("admin.appt.location.msg.nameTooLong");
                verify(lookupListManager, never()).updateLookupListItemLabel(any(), anyInt(), anyString());
            }

            @Test
            @DisplayName("should keep a name that is exactly as long as a booking can store")
            void shouldSave_whenNameExactlyMaxLength() {
                String name = "x".repeat(80);
                when(lookupListManager.updateLookupListItemLabel(loggedInInfo, 11, name)).thenReturn(true);

                assertThat(post("updateDescription", "11", name)).isEqualTo("saved");
            }

            @Test
            @DisplayName("should refuse a name another location in use already has, whatever its case")
            void shouldRefuse_whenNameInUse() {
                locations(named(11, "Midtown", true), named(13, "Eastside", true));

                assertThat(post("updateDescription", "11", "EASTSIDE")).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
                assertThat(request.getAttribute("saveFailedKey")).isEqualTo("admin.appt.location.msg.nameInUse");
                assertThat(request.getAttribute("saveFailedParam")).isEqualTo("EASTSIDE");
                verify(lookupListManager, never()).updateLookupListItemLabel(any(), anyInt(), anyString());
            }

            @Test
            @DisplayName("should allow a location to change the case of its own name")
            void shouldSave_whenOnlyItsOwnCaseChanges() {
                locations(named(11, "midtown", true), named(13, "Eastside", true));
                when(lookupListManager.updateLookupListItemLabel(loggedInInfo, 11, "Midtown")).thenReturn(true);

                assertThat(post("updateDescription", "11", "Midtown")).isEqualTo("saved");
            }

            @Test
            @DisplayName("should allow a name that only an inactive location has")
            void shouldSave_whenNameOnlyHeldByInactive() {
                locations(named(11, "Midtown", true), named(12, "Eastside", false));
                when(lookupListManager.updateLookupListItemLabel(loggedInInfo, 11, "Eastside")).thenReturn(true);

                assertThat(post("updateDescription", "11", "Eastside")).isEqualTo("saved");
            }

            @Test
            @DisplayName("should refuse an item from another lookup list")
            void shouldRefuse_whenItemNotInLocationList() {
                assertThat(post("updateDescription", "99", "Midtown")).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
                assertThat(request.getAttribute("saveFailedKey")).isNull();
            }
        }

        @Nested
        @DisplayName("enabling")
        class Restoring {

            @Test
            @DisplayName("should enable the location and redirect to the list")
            void shouldRedirect_whenRestored() {
                when(lookupListManager.restoreLookupListItem(loggedInInfo, 12)).thenReturn(true);

                assertThat(post("restore", "12", "")).isEqualTo("saved");
            }

            @Test
            @DisplayName("should refuse enabling a location whose name an active one has")
            void shouldRefuse_whenNameInUse() {
                locations(named(11, "Midtown", true), named(12, "midtown", false));

                assertThat(post("restore", "12", "")).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
                assertThat(request.getAttribute("saveFailedKey")).isEqualTo("admin.appt.location.msg.nameInUse");
                verify(lookupListManager, never()).restoreLookupListItem(any(), anyInt());
            }
        }

        @Nested
        @DisplayName("disabling")
        class Deactivating {

            @Test
            @DisplayName("should refuse without delete on _admin, which the Look-Up List Manager requires")
            void shouldThrow_whenNoAdminDelete() {
                assertThatThrownBy(() -> post("deactivate", "11", "")).isInstanceOf(SecurityException.class);

                verify(lookupListManager, never()).removeLookupListItem(any(), anyInt());
            }

            @Test
            @DisplayName("should disable the location with delete on _admin")
            void shouldRedirect_whenDeactivated() {
                grant("_admin", SecurityInfoManager.DELETE);
                when(lookupListManager.removeLookupListItem(loggedInInfo, 11)).thenReturn(true);

                assertThat(post("deactivate", "11", "")).isEqualTo("saved");
            }
        }

        @Nested
        @DisplayName("moving")
        class Moving {

            @Test
            @DisplayName("should move the location and return to the row that moved")
            void shouldRedirectToRow_whenMoved() {
                when(lookupListManager.moveLookupListItem(loggedInInfo, 11, true)).thenReturn(true);

                assertThat(post("moveUp", "11", "")).isEqualTo("saved");
                assertThat(action.getAnchor()).isEqualTo("#location-11");
            }

            @Test
            @DisplayName("should move the location the other way")
            void shouldMoveDown_whenAsked() {
                when(lookupListManager.moveLookupListItem(loggedInInfo, 11, false)).thenReturn(true);

                assertThat(post("moveDown", "11", "")).isEqualTo("saved");
            }

            @Test
            @DisplayName("should show the list with an error at the end of the order")
            void shouldShowError_whenAlreadyAtTheEnd() {
                assertThat(post("moveUp", "11", "")).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
            }
        }
    }
}
