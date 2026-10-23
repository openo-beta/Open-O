package ca.openosp.openo.appt.web;

import java.util.HashMap;
import java.util.List;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.test.unit.OscarPropertiesOverride;
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
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for {@link AppointmentSettingsAction}, the request handling both Appointment Settings
 * pages share: who may see a page, who may change it and how, and what a saved or refused change
 * gives back. A small page stands in for the Status and Location tabs, whose own tests cover only
 * what they route where.
 *
 * @since 2026-10-23
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentSettingsAction unit tests")
@Tag("unit")
@Tag("fast")
@Tag("web")
public class AppointmentSettingsActionUnitTest extends OpenOUnitTestBase {

    @Mock
    private SecurityInfoManager securityInfoManager;

    @RegisterExtension
    final OscarPropertiesOverride properties = new OscarPropertiesOverride();

    private final LoggedInInfo loggedInInfo = new LoggedInInfo();
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private TestPage action;

    /** A settings page whose changes, which need update on _admin, are named by the dispatch. */
    private static class TestPage extends AppointmentSettingsAction {

        int views;

        @Override
        public String execute() {
            return switch (String.valueOf(request.getParameter("dispatch"))) {
                case "save" -> change(() -> true);
                case "refuse" -> change(() -> false);
                case "reject" -> change(() -> {
                    throw new IllegalArgumentException("not a colour");
                });
                case "readId" -> change(() -> intParameter("ID") > 0);
                case "refuseWithReason" -> change(() -> {
                    throw new ChangeRefusedException("admin.appt.location.msg.nameInUse", "Midtown");
                });
                default -> show();
            };
        }

        @Override
        protected boolean canChange() {
            return hasAnyPrivilege(List.of("_admin"), SecurityInfoManager.UPDATE);
        }

        @Override
        protected String view() {
            views++;
            return SUCCESS;
        }
    }

    @BeforeEach
    void setUp() {
        registerMock(SecurityInfoManager.class, securityInfoManager);

        request = new MockHttpServletRequest("POST", "/appointment/apptSetting.do");
        response = new MockHttpServletResponse();
        LoggedInInfo.setLoggedInInfoIntoSession(request.getSession(), loggedInInfo);
        ActionContext context = new ActionContext(new HashMap<>());
        context.put(ServletActionContext.HTTP_REQUEST, request);
        context.put(ServletActionContext.HTTP_RESPONSE, response);
        ActionContext.setContext(context);

        action = new TestPage();
    }

    @AfterEach
    void clearActionContext() {
        ActionContext.setContext(null);
    }

    private void grant(String object, String privilege) {
        lenient().when(securityInfoManager.hasPrivilege(loggedInInfo, object, privilege, null)).thenReturn(true);
    }

    private String post(String dispatch) {
        request.setParameter("dispatch", dispatch);
        return action.execute();
    }

    @Nested
    @DisplayName("showing the page")
    class Showing {

        @BeforeEach
        void get() {
            request.setMethod("GET");
        }

        @ParameterizedTest
        @ValueSource(strings = {"_admin", "_admin.userAdmin", "_admin.schedule"})
        @DisplayName("should show the page with read on any one of the Appointment Settings objects")
        void shouldShowPage_whenReadOnAnyObject(String object) {
            grant(object, SecurityInfoManager.READ);

            assertThat(action.execute()).isEqualTo("success");

            assertThat(action.views).isEqualTo(1);
            assertThat(request.getAttribute("canChange")).isEqualTo(false);
        }

        @Test
        @DisplayName("should refuse the page without read on any Appointment Settings object")
        void shouldThrow_whenNoReadPrivilege() {
            assertThatThrownBy(() -> action.execute()).isInstanceOf(SecurityException.class);

            assertThat(action.views).isZero();
        }

        @Test
        @DisplayName("should offer the change controls to a user the page lets change it")
        void shouldOfferChanges_whenPageAllows() {
            grant("_admin", SecurityInfoManager.READ);
            grant("_admin", SecurityInfoManager.UPDATE);

            action.execute();

            assertThat(request.getAttribute("canChange")).isEqualTo(true);
        }

        @Test
        @DisplayName("should show the Status tab only when the schedule uses editable statuses")
        void shouldShowStatusTab_whenEditableStatusesOn() {
            grant("_admin", SecurityInfoManager.READ);
            properties.remove("ENABLE_EDIT_APPT_STATUS");
            action.execute();
            assertThat(request.getAttribute("statusTabEnabled")).isEqualTo(false);

            properties.set("ENABLE_EDIT_APPT_STATUS", "yes");
            action.execute();
            assertThat(request.getAttribute("statusTabEnabled")).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("refusing a change before it runs")
    class Refusing {

        @Test
        @DisplayName("should refuse a change from a user the page doesn't let change it")
        void shouldThrow_whenPageDisallows() {
            grant("_admin.schedule", SecurityInfoManager.UPDATE);

            assertThatThrownBy(() -> post("save")).isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("should refuse a change that is not posted, which the CSRF guard would not check")
        void shouldThrow_whenNotPosted() {
            grant("_admin", SecurityInfoManager.UPDATE);
            request.setMethod("GET");

            assertThatThrownBy(() -> post("save")).isInstanceOf(SecurityException.class);
        }
    }

    @Nested
    @DisplayName("the outcome of a change")
    class Outcomes {

        @BeforeEach
        void allowChanges() {
            grant("_admin", SecurityInfoManager.UPDATE);
        }

        @Test
        @DisplayName("should redirect back to the page once the change is saved")
        void shouldRedirect_whenSaved() {
            assertThat(post("save")).isEqualTo("saved");

            assertThat(action.views).isZero();
        }

        @ParameterizedTest
        @ValueSource(strings = {"refuse", "reject"})
        @DisplayName("should show the page again with status 400 when the change is refused or its value rejected")
        void shouldShowPageWith400_whenNotSaved(String dispatch) {
            assertThat(post(dispatch)).isEqualTo("success");

            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(request.getAttribute("saveFailed")).isEqualTo(true);
            assertThat(request.getAttribute("saveFailedKey")).isNull();
            assertThat(action.views).isEqualTo(1);
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", ""})
        @DisplayName("should show the page again with status 400 when the id is not a number")
        void shouldShowPageWith400_whenIdMalformed(String id) {
            request.setParameter("ID", id);

            assertThat(post("readId")).isEqualTo("success");

            assertThat(response.getStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("should say why when the change is refused for a reason the user can act on")
        void shouldShowReason_whenRefusedWithReason() {
            assertThat(post("refuseWithReason")).isEqualTo("success");

            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(request.getAttribute("saveFailedKey")).isEqualTo("admin.appt.location.msg.nameInUse");
            assertThat(request.getAttribute("saveFailedParam")).isEqualTo("Midtown");
        }
    }
}
