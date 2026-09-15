package ca.openosp.openo.appt.web;

import java.util.List;
import java.util.function.BooleanSupplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import ca.openosp.OscarProperties;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Request handling shared by the Appointment Settings pages (Status and Location).
 *
 * <p>{@link #show()} renders a page for a user with read on one of {@link #SEC_OBJECTS}.
 * {@link #change} applies one change for a user the page's {@link #canChange()} allows, and only
 * when posted, since the CSRF guard only checks posts. A saved change redirects back to the page
 * ({@link #SAVED}); a refused one renders it with status 400 and {@code saveFailed} set. Every
 * render sets {@code statusTabEnabled} for the pages' tabs.</p>
 *
 * @since 2026-09-15
 */
public abstract class AppointmentSettingsAction extends ActionSupport {

    /** Read on any one of these opens an Appointment Settings page; the pages' JSP gates match. */
    protected static final List<String> SEC_OBJECTS = List.of("_admin", "_admin.userAdmin", "_admin.schedule");

    /** The result that redirects back to the page after a saved change. */
    protected static final String SAVED = "saved";

    private static final Logger logger = MiscUtils.getLogger();

    protected final HttpServletRequest request = ServletActionContext.getRequest();
    protected final HttpServletResponse response = ServletActionContext.getResponse();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Whether the Status tab is offered. {@code ENABLE_EDIT_APPT_STATUS=yes} switches the schedule
     * from its built-in statuses to the editable ones, so without it status edits change nothing.
     *
     * @return boolean true if ENABLE_EDIT_APPT_STATUS is yes
     */
    public static boolean isStatusTabEnabled() {
        return "yes".equalsIgnoreCase(OscarProperties.getInstance().getProperty("ENABLE_EDIT_APPT_STATUS"));
    }

    /**
     * Whether the current user may change this page's settings.
     *
     * @return boolean true if the user holds the privilege changes need
     */
    protected abstract boolean canChange();

    /**
     * Sets the request attributes the page's JSP renders.
     *
     * @return String {@code success}
     */
    protected abstract String view();

    /**
     * Renders the page.
     *
     * @return String {@code success}
     * @throws SecurityException if the user lacks read on all of {@link #SEC_OBJECTS}
     */
    protected final String show() {
        if (!hasAnyPrivilege(SEC_OBJECTS, SecurityInfoManager.READ)) {
            throw new SecurityException("missing required sec object (" + String.join(" or ", SEC_OBJECTS) + ")");
        }
        return render();
    }

    /**
     * Applies one change.
     *
     * @param change BooleanSupplier the change, which returns false or throws IllegalArgumentException
     *               when it is refused
     * @return String {@link #SAVED} to redirect back to the page, or {@code success} to render it
     *         with status 400 when the change was refused
     * @throws SecurityException if {@link #canChange()} is false, or the change was not posted
     */
    protected final String change(BooleanSupplier change) {
        if (!canChange()) {
            throw new SecurityException("missing the privilege to change appointment settings");
        }
        if (!"POST".equals(request.getMethod())) {
            throw new SecurityException("appointment settings changes must be posted");
        }

        boolean saved;
        try {
            saved = change.getAsBoolean();
        } catch (IllegalArgumentException e) {
            // Also catches NumberFormatException from a malformed id; the pages never post one.
            logger.warn("Rejected appointment settings change: {}", e.getMessage());
            saved = false;
        }
        if (saved) {
            return SAVED;
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("saveFailed", true);
        return render();
    }

    private String render() {
        request.setAttribute("statusTabEnabled", isStatusTabEnabled());
        return view();
    }

    /**
     * Checks the current user's privilege on several objects, since privileges don't inherit.
     *
     * @param objects List&lt;String&gt; the security objects, any one of which will do
     * @param privilege String a {@link SecurityInfoManager} privilege such as {@code UPDATE}
     * @return boolean true if the user holds privilege on at least one of objects
     */
    protected final boolean hasAnyPrivilege(List<String> objects, String privilege) {
        LoggedInInfo loggedInInfo = getLoggedInInfo();
        return objects.stream().anyMatch(object -> securityInfoManager.hasPrivilege(loggedInInfo, object, privilege, null));
    }

    /**
     * Reads a required integer parameter.
     *
     * @param name String the parameter name
     * @return int the parameter's value
     * @throws NumberFormatException if the parameter is missing or not an integer, which
     *                               {@link #change} treats as a refused change
     */
    protected final int intParameter(String name) {
        return Integer.parseInt(request.getParameter(name));
    }

    /**
     * Gets the current user.
     *
     * @return LoggedInInfo the current user's session details
     */
    protected final LoggedInInfo getLoggedInInfo() {
        return LoggedInInfo.getLoggedInInfoFromSession(request);
    }
}
