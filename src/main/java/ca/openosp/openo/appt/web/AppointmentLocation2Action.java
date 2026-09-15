package ca.openosp.openo.appt.web;

import java.util.List;
import java.util.Objects;

import ca.openosp.openo.appt.LocationList;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.managers.LookupListManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Appointment Settings, Location tab: lists the Location List's active items and sets their Item
 * Style. Items are added, renamed and deactivated in the Look-Up List Manager, which the page links
 * to.
 *
 * <p>Routed on the {@code dispatch} parameter. {@code updateColour} and {@code updateIcon} set
 * item {@code ID}'s colour or icon to {@code value}, and a blank value clears it; the item style
 * editor on the page posts them. Anything else renders the list. Changes need update on
 * {@code _admin}, which {@link LookupListManager} requires, and must be posted (see
 * {@link AppointmentSettingsAction}).</p>
 *
 * @since 2026-09-15
 */
public class AppointmentLocation2Action extends AppointmentSettingsAction {

    private LookupListManager lookupListManager = SpringUtils.getBean(LookupListManager.class);

    /**
     * Renders the Location List's items, or applies the change named by {@code dispatch}.
     *
     * @return String {@code success} to render the list, or {@code saved} to redirect back to it
     * @throws SecurityException if the user lacks the privilege, or a change was not posted
     */
    public String execute() {
        String dispatch = Objects.toString(request.getParameter("dispatch"), "view");
        return switch (dispatch) {
            case "updateColour" -> change(() -> updateStyle(lookupListManager::updateLookupListItemColour));
            case "updateIcon" -> change(() -> updateStyle(lookupListManager::updateLookupListItemIcon));
            default -> show();
        };
    }

    @Override
    protected boolean canChange() {
        return hasAnyPrivilege(List.of("_admin"), SecurityInfoManager.UPDATE);
    }

    @Override
    protected String view() {
        LookupList list = lookupListManager.findAppointmentLocationList(getLoggedInInfo());
        request.setAttribute("locationListName", list == null ? null : list.getName());
        request.setAttribute("locationItems", LocationList.of(list).getActiveItems());
        request.setAttribute("canChange", canChange());
        return SUCCESS;
    }

    /* The manager takes any lookup list's item, so only the Location List's are passed on. */
    private boolean updateStyle(StyleUpdate update) {
        int id = intParameter("ID");
        LoggedInInfo loggedInInfo = getLoggedInInfo();
        return LocationList.of(lookupListManager.findAppointmentLocationList(loggedInInfo)).find(id) != null
                && update.apply(loggedInInfo, id, request.getParameter("value"));
    }

    @FunctionalInterface
    private interface StyleUpdate {
        boolean apply(LoggedInInfo loggedInInfo, int lookupListItemId, String value);
    }
}
