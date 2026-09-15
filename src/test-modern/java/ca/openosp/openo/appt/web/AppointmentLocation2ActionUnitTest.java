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
 * Unit tests for {@link AppointmentLocation2Action}: who may view and change Location List item
 * styles, which items a change may touch, and the saved/rejected outcomes.
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

            assertThat(request.getAttribute("locationItems")).isEqualTo(List.of(room));
            assertThat(request.getAttribute("locationListName")).isEqualTo("appointmentLocationCode");
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
        @ValueSource(strings = {"updateColour", "updateIcon"})
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
            @DisplayName("should save the colour and redirect to the list")
            void shouldRedirect_whenColourSaved() {
                when(lookupListManager.updateLookupListItemColour(loggedInInfo, 11, "#445566")).thenReturn(true);

                assertThat(post("updateColour", "11", "#445566")).isEqualTo("saved");
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
                assertThat(request.getAttribute("locationItems")).isEqualTo(List.of(room));
            }

            @Test
            @DisplayName("should show the list with an error when the id is not a number")
            void shouldShowError_whenIdMalformed() {
                assertThat(post("updateColour", "abc", "#445566")).isEqualTo("success");

                assertThat(response.getStatus()).isEqualTo(400);
            }
        }
    }
}
