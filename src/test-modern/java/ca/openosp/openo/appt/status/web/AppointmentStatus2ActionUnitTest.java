package ca.openosp.openo.appt.status.web;

import ca.openosp.openo.appt.status.service.AppointmentStatusMgr;
import ca.openosp.openo.commn.dao.AppointmentStatusDao;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;

import com.opensymphony.xwork2.ActionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppointmentStatus2Action}: the privilege and POST checks in front of every
 * change, routing of the item style editor's dispatch values, the saved/rejected outcomes, and the
 * audit row each saved change writes.
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
    private MockedStatic<LogAction> logActionMock;

    @BeforeEach
    void setUp() {
        registerMock(SecurityInfoManager.class, securityInfoManager);
        // Creating the action creates the real manager, whose class load looks up the DAO.
        registerMock(AppointmentStatusDao.class, mock(AppointmentStatusDao.class));
        // LogAction's static initializer looks up OscarLogDao, so register it before mocking LogAction
        registerMock(OscarLogDao.class, mock(OscarLogDao.class));
        logActionMock = mockStatic(LogAction.class);

        request = new MockHttpServletRequest("POST", "/appointment/apptStatusSetting.do");
        response = new MockHttpServletResponse();
        LoggedInInfo.setLoggedInInfoIntoSession(request.getSession(), loggedInInfo);
        ActionContext.bind(ActionContext.of().withServletRequest(request).withServletResponse(response));

        action = new AppointmentStatus2Action();
        ReflectionTestUtils.setField(action, "appointmentStatusMgr", appointmentStatusMgr);
    }

    @AfterEach
    void clearActionContext() {
        ActionContext.clear();
        logActionMock.close();
    }

    private void grant(String object, String privilege) {
        lenient().when(securityInfoManager.hasPrivilege(loggedInInfo, object, privilege, null)).thenReturn(true);
    }

    @Nested
    @DisplayName("privileges")
    class Privileges {

        @ParameterizedTest
        @ValueSource(strings = {"_admin", "_admin.userAdmin", "_admin.schedule"})
        @DisplayName("should show the list with read on any one of the page's objects")
        void shouldShowList_whenReadOnAnyObject(String object) {
            grant(object, SecurityInfoManager.READ);
            request.setMethod("GET");

            assertThat(action.execute()).isEqualTo("success");

            assertThat(request.getAttribute("allStatus")).isNotNull();
            assertThat(request.getAttribute("iconSet")).isEqualTo(AppointmentStatusMgr.ICON_SET);
        }

        @Test
        @DisplayName("should offer the change controls with update")
        void shouldSetCanChange_whenUpdatePrivilege() {
            grant("_admin.schedule", SecurityInfoManager.READ);
            grant("_admin.schedule", SecurityInfoManager.UPDATE);
            request.setMethod("GET");

            action.execute();

            assertThat(request.getAttribute("canChange")).isEqualTo(true);
        }

        @Test
        @DisplayName("should hide the change controls from a user who can only read")
        void shouldClearCanChange_whenReadOnly() {
            grant("_admin.schedule", SecurityInfoManager.READ);
            request.setMethod("GET");

            action.execute();

            assertThat(request.getAttribute("canChange")).isEqualTo(false);
        }

        @Test
        @DisplayName("should refuse the list without read on any of the page's objects")
        void shouldThrow_whenNoReadPrivilege() {
            request.setMethod("GET");

            assertThatThrownBy(() -> action.execute()).isInstanceOf(SecurityException.class);

            verifyNoInteractions(appointmentStatusMgr);
        }

        @Test
        @DisplayName("should refuse a change with read but not update")
        void shouldThrow_whenChangeWithReadOnly() {
            grant("_admin.schedule", SecurityInfoManager.READ);
            request.setParameter("dispatch", "updateColour");
            request.setParameter("ID", "3");
            request.setParameter("value", "#445566");

            assertThatThrownBy(() -> action.execute()).isInstanceOf(SecurityException.class);

            verifyNoInteractions(appointmentStatusMgr);
        }

        @ParameterizedTest
        @ValueSource(strings = {"reset", "changestatus", "updateDescription", "updateColour", "updateIcon"})
        @DisplayName("should refuse a change that is not posted")
        void shouldThrow_whenChangeNotPosted(String dispatch) {
            grant("_admin", SecurityInfoManager.UPDATE);
            request.setMethod("GET");
            request.setParameter("dispatch", dispatch);

            assertThatThrownBy(() -> action.execute()).isInstanceOf(SecurityException.class);

            verifyNoInteractions(appointmentStatusMgr);
        }
    }

    @Nested
    @DisplayName("changes")
    class Changes {

        @BeforeEach
        void allowUpdate() {
            grant("_admin.schedule", SecurityInfoManager.UPDATE);
        }

        private String post(String dispatch, String id, String value) {
            request.setParameter("dispatch", dispatch);
            request.setParameter("ID", id);
            request.setParameter("value", value);
            return action.execute();
        }

        @Test
        @DisplayName("should save the description and redirect to the list")
        void shouldRedirect_whenDescriptionSaved() {
            when(appointmentStatusMgr.updateDescription(3, "Waiting")).thenReturn(true);

            assertThat(post("updateDescription", "3", "Waiting")).isEqualTo("saved");
        }

        @Test
        @DisplayName("should save the colour and redirect to the list")
        void shouldRedirect_whenColourSaved() {
            when(appointmentStatusMgr.updateColour(3, "#445566")).thenReturn(true);

            assertThat(post("updateColour", "3", "#445566")).isEqualTo("saved");
        }

        @Test
        @DisplayName("should save the icon and redirect to the list")
        void shouldRedirect_whenIconSaved() {
            when(appointmentStatusMgr.updateIcon(3, "1.gif")).thenReturn(true);

            assertThat(post("updateIcon", "3", "1.gif")).isEqualTo("saved");
        }

        @Test
        @DisplayName("should enable or disable the posted status")
        void shouldChangeActive_whenChangeStatusPosted() {
            when(appointmentStatusMgr.changeStatus(8, 1)).thenReturn(true);
            request.setParameter("dispatch", "changestatus");
            request.setParameter("statusID", "8");
            request.setParameter("iActive", "1");

            assertThat(action.execute()).isEqualTo("saved");

            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("AppointmentStatus2Action.changestatus"), contains("id 8: active set to 1")));
        }

        @Test
        @DisplayName("should show the list with an error when the status to enable or disable is locked")
        void shouldShowError_whenChangeStatusRefused() {
            when(appointmentStatusMgr.changeStatus(11, 0)).thenReturn(false);
            request.setParameter("dispatch", "changestatus");
            request.setParameter("statusID", "11");
            request.setParameter("iActive", "0");

            assertThat(action.execute()).isEqualTo("success");

            assertThat(response.getStatus()).isEqualTo(400);
            logActionMock.verifyNoInteractions();
        }

        @Test
        @DisplayName("should reset the statuses")
        void shouldReset_whenResetPosted() {
            request.setParameter("dispatch", "reset");

            assertThat(action.execute()).isEqualTo("saved");

            verify(appointmentStatusMgr).reset();
            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("AppointmentStatus2Action.reset"), anyString()));
        }

        @Test
        @DisplayName("should write an audit row naming the status and its new value")
        void shouldAudit_whenStyleSaved() {
            when(appointmentStatusMgr.updateColour(3, "#445566")).thenReturn(true);

            post("updateColour", "3", "#445566");

            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("AppointmentStatus2Action.updateColour"), eq("appointment_status id 3: colour set to [#445566]")));
        }

        @Test
        @DisplayName("should show the list with an error when the manager refuses the change")
        void shouldShowError_whenStatusLockedOrMissing() {
            when(appointmentStatusMgr.updateColour(1, "#445566")).thenReturn(false);

            assertThat(post("updateColour", "1", "#445566")).isEqualTo("success");

            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(request.getAttribute("saveFailed")).isEqualTo(true);
            logActionMock.verifyNoInteractions();
        }

        @Test
        @DisplayName("should show the list with an error when the value is invalid")
        void shouldShowError_whenValueInvalid() {
            when(appointmentStatusMgr.updateIcon(3, "thumb.png")).thenThrow(new IllegalArgumentException("not in set"));

            assertThat(post("updateIcon", "3", "thumb.png")).isEqualTo("success");

            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(request.getAttribute("saveFailed")).isEqualTo(true);
        }

        @Test
        @DisplayName("should show the list with an error when the id is not a number")
        void shouldShowError_whenIdMalformed() {
            assertThat(post("updateColour", "abc", "#445566")).isEqualTo("success");

            assertThat(response.getStatus()).isEqualTo(400);
            verify(appointmentStatusMgr, never()).updateColour(anyInt(), anyString());
        }
    }

    @Test
    @DisplayName("should show the list for an unknown dispatch, as before")
    void shouldShowList_whenDispatchUnknown() {
        grant("_admin", SecurityInfoManager.READ);
        request.setParameter("dispatch", "modify");

        assertThat(action.execute()).isEqualTo("success");

        assertThat(request.getAttribute("allStatus")).isNotNull();
        verify(appointmentStatusMgr, never()).reset();
        verify(appointmentStatusMgr, never()).changeStatus(anyInt(), anyInt());
        logActionMock.verifyNoInteractions();
    }
}
