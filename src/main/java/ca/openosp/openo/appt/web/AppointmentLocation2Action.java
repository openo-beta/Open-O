package ca.openosp.openo.appt.web;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import ca.openosp.openo.appt.LocationList;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.managers.LookupListManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Appointment Settings, Location tab: lists the places an appointment can be booked, the active ones
 * in the order the booking screens offer them and then the inactive ones, and manages them. New locations are added in the Look-Up List
 * Manager, which the page links to; everything else happens here.
 *
 * <p>Routed on the {@code dispatch} parameter, each change acting on item {@code ID}:</p>
 * <ul>
 *   <li>{@code updateColour}, {@code updateIcon} and {@code updateDescription} set the item's
 *       colour, icon or name to {@code value}; the item style editor on the page posts them, and a
 *       blank value clears a colour or an icon.</li>
 *   <li>{@code restore} enables an inactive location, {@code deactivate} disables an active one,
 *       and {@code moveUp} and {@code moveDown} change where an active one sits in the order.</li>
 *   <li>Anything else renders the page.</li>
 * </ul>
 *
 * <p>Changes need update on {@code _admin}, which {@link LookupListManager} requires, except
 * disabling, which needs delete because it is the manager's remove. All must be posted (see
 * {@link AppointmentSettingsAction}). A location's name must be at most
 * {@link LocationList#LABEL_MAX_LENGTH} characters, because a booking saves a copy of it, and may
 * not be one another active location already has.</p>
 *
 * @since 2026-09-15
 */
public class AppointmentLocation2Action extends AppointmentSettingsAction {

    private LookupListManager lookupListManager = SpringUtils.getBean(LookupListManager.class);

    private String anchor = "";

    /**
     * Renders the Location List's items, or applies the change named by {@code dispatch}.
     *
     * @return String {@code success} to render the list, or {@code saved} to redirect back to it
     * @throws SecurityException if the user lacks the privilege, or a change was not posted
     */
    public String execute() {
        String dispatch = Objects.toString(request.getParameter("dispatch"), "view");
        return switch (dispatch) {
            case "updateColour" -> changeLocation((locations, item) ->
                    lookupListManager.updateLookupListItemColour(getLoggedInInfo(), item.getId(), value()));
            case "updateIcon" -> changeLocation((locations, item) ->
                    lookupListManager.updateLookupListItemIcon(getLoggedInInfo(), item.getId(), value()));
            case "updateDescription" -> changeLocation(this::rename);
            case "restore" -> changeLocation(this::restore);
            case "deactivate" -> changeLocation(this::deactivate);
            case "moveUp" -> changeLocation((locations, item) ->
                    lookupListManager.moveLookupListItem(getLoggedInInfo(), item.getId(), true));
            case "moveDown" -> changeLocation((locations, item) ->
                    lookupListManager.moveLookupListItem(getLoggedInInfo(), item.getId(), false));
            default -> show();
        };
    }

    @Override
    protected boolean canChange() {
        return hasAnyPrivilege(List.of("_admin"), SecurityInfoManager.UPDATE);
    }

    /**
     * Whether the current user may disable a location. Disabling is the Look-Up List Manager's
     * remove, which needs delete rather than update, so the page offers it separately.
     *
     * @return boolean true if the user holds delete on _admin
     */
    public boolean isCanDeactivate() {
        return hasAnyPrivilege(List.of("_admin"), SecurityInfoManager.DELETE);
    }

    /**
     * The fragment the redirect after a saved change lands on, so the location just changed stays in
     * view.
     *
     * @return String {@code #location-<id>}, or blank before a change has named its location
     */
    public String getAnchor() {
        return anchor;
    }

    @Override
    protected String view() {
        LookupList list = lookupListManager.findAppointmentLocationList(getLoggedInInfo());
        LocationList locations = LocationList.of(list);
        request.setAttribute("locationListName", list == null ? null : list.getName());
        request.setAttribute("locations", locations.getItemsActiveFirst());
        request.setAttribute("activeLocationCount", locations.getActiveItems().size());
        request.setAttribute("canChange", canChange());
        request.setAttribute("canDeactivate", isCanDeactivate());
        return SUCCESS;
    }

    /*
     * Every change acts on one location. The manager takes any lookup list's item, so the posted id
     * is resolved against the Location List, loaded once, before the change sees it.
     */
    private String changeLocation(BiPredicate<LocationList, LookupListItem> change) {
        return change(() -> {
            int id = intParameter("ID");
            LocationList locations = LocationList.of(lookupListManager.findAppointmentLocationList(getLoggedInInfo()));
            LookupListItem item = locations.find(id);
            if (item == null) {
                throw new IllegalArgumentException("not a Location List item: " + id);
            }
            anchor = "#location-" + id;
            return change.test(locations, item);
        });
    }

    /* Renames a location, keeping active names distinct and short enough for a booking. */
    private boolean rename(LocationList locations, LookupListItem item) {
        String name = value().trim();
        if (name.length() > LocationList.LABEL_MAX_LENGTH) {
            throw new ChangeRefusedException("admin.appt.location.msg.nameTooLong",
                    String.valueOf(LocationList.LABEL_MAX_LENGTH));
        }
        requireNameFree(locations, name, item);

        return lookupListManager.updateLookupListItemLabel(getLoggedInInfo(), item.getId(), name);
    }

    /* Enables a location, unless an active one already has its name. */
    private boolean restore(LocationList locations, LookupListItem item) {
        requireNameFree(locations, item.getLabel(), item);

        return lookupListManager.restoreLookupListItem(getLoggedInInfo(), item.getId());
    }

    /* Disables a location, which needs delete, unlike the rest of this page. */
    private boolean deactivate(LocationList locations, LookupListItem item) {
        if (!isCanDeactivate()) {
            throw new SecurityException("missing the privilege to disable a location");
        }

        return lookupListManager.removeLookupListItem(getLoggedInInfo(), item.getId());
    }

    private static void requireNameFree(LocationList locations, String name, LookupListItem item) {
        if (locations.isNameTaken(name, item)) {
            throw new ChangeRefusedException("admin.appt.location.msg.nameInUse", name.trim());
        }
    }

    private String value() {
        return Objects.toString(request.getParameter("value"), "");
    }
}
