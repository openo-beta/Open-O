package ca.openosp.openo.appt.status.web;

import java.util.HashMap;
import java.util.List;

import ca.openosp.openo.appt.status.service.AppointmentStatusMgr;
import ca.openosp.openo.commn.dao.AppointmentStatusDao;
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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppointmentStatus2Action}: who may change statuses, and which manager call
 * each of the page's dispatch values makes, for the current user. The page handling it shares with
 * the Location tab is tested in {@link ca.openosp.openo.appt.web.AppointmentSettingsActionUnitTest},
 * and the rules and audit rows in the manager's tests.
 *
 * @since 2026-09-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentStatus2Action unit tests")
@Tag("unit")
@Tag("fast")
@Tag("web")
public class AppointmentStatus2ActionUnitTest extends OpenOUnitTestBase {

    @Mock
    private SecurityInfoManager securityInfoManager;

    @Mock
    private AppointmentStatusMgr appointmentStatusMgr;

    private final LoggedInInfo loggedInInfo = new LoggedInInfo();
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AppointmentStatus2Action action;

    /** Every change the page posts. */
    static List<String> changes() {
        return List.of("reset", "changestatus", "updateDescription", "updateColour", "updateIcon");
    }

    @BeforeEach
    void setUp() {
        registerMock(SecurityInfoManager.class, securityInfoManager);
        // Creating the action creates the real manager, whose class load looks up the DAO.
        registerMock(AppointmentStatusDao.class, mock(AppointmentStatusDao.class));

        request = new MockHttpServletRequest("POST", "/appointment/apptStatusSetting.do");
        response = new MockHttpServletResponse();
        LoggedInInfo.setLoggedInInfoIntoSession(request.getSession(), loggedInInfo);
        ActionContext context = new ActionContext(new HashMap<>());
        context.put(ServletActionContext.HTTP_REQUEST, request);
        context.put(ServletActionContext.HTTP_RESPONSE, response);
        ActionContext.setContext(context);

        action = new AppointmentStatus2Action();
        ReflectionTestUtils.setField(action, "appointmentStatusMgr", appointmentStatusMgr);
    }

    @AfterEach
    void clearActionContext() {
        ActionContext.setContext(null);
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
        void get() {
            request.setMethod("GET");
        }

        @Test
        @DisplayName("should list every status")
        void shouldShowList_whenRead() {
            grant("_admin.schedule", SecurityInfoManager.READ);

            assertThat(action.execute()).isEqualTo("success");

            assertThat(request.getAttribute("allStatus")).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"_admin", "_admin.userAdmin", "_admin.schedule"})
        @DisplayName("should offer the change controls with update on any one of the page's objects")
        void shouldOfferChanges_whenUpdateOnAnyObject(String object) {
            grant(object, SecurityInfoManager.READ);
            grant(object, SecurityInfoManager.UPDATE);

            action.execute();

            assertThat(request.getAttribute("canChange")).isEqualTo(true);
        }

        @Test
        @DisplayName("should show the list for an unknown dispatch, as before")
        void shouldShowList_whenDispatchUnknown() {
            grant("_admin", SecurityInfoManager.READ);
            request.setParameter("dispatch", "modify");

            assertThat(action.execute()).isEqualTo("success");

            verify(appointmentStatusMgr).getAllStatus();
            verify(appointmentStatusMgr, never()).reset(any());
            verify(appointmentStatusMgr, never()).changeStatus(any(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("refusing a change before it runs")
    class Refusing {

        @ParameterizedTest
        @MethodSource("ca.openosp.openo.appt.status.web.AppointmentStatus2ActionUnitTest#changes")
        @DisplayName("should refuse every change from a user who can only read")
        void shouldThrow_whenReadOnly(String dispatch) {
            grant("_admin.schedule", SecurityInfoManager.READ);

            assertThatThrownBy(() -> post(dispatch, "3", "#445566")).isInstanceOf(SecurityException.class);

            verifyNoInteractions(appointmentStatusMgr);
        }

        @ParameterizedTest
        @MethodSource("ca.openosp.openo.appt.status.web.AppointmentStatus2ActionUnitTest#changes")
        @DisplayName("should refuse every change that is not posted")
        void shouldThrow_whenNotPosted(String dispatch) {
            grant("_admin", SecurityInfoManager.UPDATE);
            request.setMethod("GET");

            assertThatThrownBy(() -> post(dispatch, "3", "#445566")).isInstanceOf(SecurityException.class);

            verifyNoInteractions(appointmentStatusMgr);
        }
    }

    @Nested
    @DisplayName("with update")
    class Changes {

        @BeforeEach
        void allowUpdate() {
            grant("_admin.schedule", SecurityInfoManager.UPDATE);
        }

        @Test
        @DisplayName("should save the description, colour and icon for the current user and redirect to the list")
        void shouldSaveStyle_whenPosted() {
            when(appointmentStatusMgr.updateDescription(loggedInInfo, 3, "Waiting")).thenReturn(true);
            when(appointmentStatusMgr.updateColour(loggedInInfo, 3, "#445566")).thenReturn(true);
            when(appointmentStatusMgr.updateIcon(loggedInInfo, 3, "1.gif")).thenReturn(true);

            assertThat(post("updateDescription", "3", "Waiting")).isEqualTo("saved");
            assertThat(post("updateColour", "3", "#445566")).isEqualTo("saved");
            assertThat(post("updateIcon", "3", "1.gif")).isEqualTo("saved");
        }

        @ParameterizedTest
        @CsvSource({"8, 1", "10, 0"})
        @DisplayName("should enable or disable the posted status, whether or not appointments use it")
        void shouldChangeStatus_whenPosted(int id, int active) {
            when(appointmentStatusMgr.changeStatus(loggedInInfo, id, active)).thenReturn(true);
            request.setParameter("dispatch", "changestatus");
            request.setParameter("statusID", String.valueOf(id));
            request.setParameter("iActive", String.valueOf(active));

            assertThat(action.execute()).isEqualTo("saved");
        }

        @Test
        @DisplayName("should reset the statuses")
        void shouldReset_whenResetPosted() {
            request.setParameter("dispatch", "reset");

            assertThat(action.execute()).isEqualTo("saved");

            verify(appointmentStatusMgr).reset(loggedInInfo);
        }

        @Test
        @DisplayName("should show the list with an error when the manager refuses a locked or missing status")
        void shouldShowError_whenManagerRefuses() {
            assertThat(post("updateColour", "1", "#445566")).isEqualTo("success");

            assertThat(response.getStatus()).isEqualTo(400);
            verify(appointmentStatusMgr).updateColour(loggedInInfo, 1, "#445566");
        }
    }
}
