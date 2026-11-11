package ca.openosp.openo.appt.web;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.openosp.openo.appt.LocationList;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.managers.AppointmentLocationManager;
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
 *       blank value clears a colour or an icon. A new name also renames the location on its
 *       appointments from today on.</li>
 *   <li>{@code restore} enables an inactive location, {@code deactivate} disables an active one
 *       and stops it being the default, and {@code moveUp} and {@code moveDown} change where an
 *       active one sits in the order.</li>
 *   <li>{@code makeDefault} makes an active location the Default Location, the one new bookings
 *       start on, in place of any other; {@code removeDefault} leaves no default.</li>
 *   <li>{@code updateRequirement} acts on the whole list, not an item: it turns the Location
 *       Requirement on when {@code value} is {@code true} and off otherwise, as the page's switch
 *       posts {@code value} only while it is on.</li>
 *   <li>{@code upcomingCount} changes nothing: it answers {@code {"count": n}}, location
 *       {@code ID}'s appointments from today on as of now, which a rename's confirmation asks for
 *       because the counts drawn with the page miss anything booked since.</li>
 *   <li>Anything else renders the page.</li>
 * </ul>
 *
 * <p>Changes need update on {@code _admin}, which {@link LookupListManager} and
 * {@link AppointmentLocationManager} require, except disabling, which needs delete because it is
 * the Look-Up List Manager's remove. The link to add locations
 * needs write, because the manager's Add does. All changes must be posted (see
 * {@link AppointmentSettingsAction}). A location's name must be at most
 * {@link LocationList#LABEL_MAX_LENGTH} characters, because a booking saves a copy of it, and may
 * not be one another active location already has.</p>
 *
 * @since 2026-09-15
 */
public class AppointmentLocation2Action extends AppointmentSettingsAction {

    /** Location changes are Look-Up List edits, which the managers check against _admin. */
    private static final List<String> ADMIN = List.of("_admin");

    private static final ObjectMapper JSON = new ObjectMapper();

    private LookupListManager lookupListManager = SpringUtils.getBean(LookupListManager.class);
    private AppointmentLocationManager locationManager = SpringUtils.getBean(AppointmentLocationManager.class);

    private String anchor = "";

    /**
     * Renders the Location List's items, or applies the change named by {@code dispatch}.
     *
     * @return String {@code success} to render the list, {@code saved} to redirect back to it, or
     *         null once {@code upcomingCount} has written its answer
     * @throws SecurityException if the user lacks the privilege, or a change was not posted
     */
    @Override
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
            case "makeDefault" -> changeLocation(this::makeDefault);
            case "removeDefault" -> changeLocation(this::removeDefault);
            case "updateRequirement" -> change(this::updateRequirement);
            case "upcomingCount" -> upcomingCount();
            default -> show();
        };
    }

    @Override
    protected boolean canChange() {
        return hasAnyPrivilege(ADMIN, SecurityInfoManager.UPDATE);
    }

    /**
     * Whether the current user may disable a location. Disabling is the Look-Up List Manager's
     * remove, which needs delete rather than update, so the page offers it separately.
     *
     * @return boolean true if the user holds delete on _admin
     */
    public boolean isCanDeactivate() {
        return hasAnyPrivilege(ADMIN, SecurityInfoManager.DELETE);
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
        LookupList list = locationManager.findLocationList(getLoggedInInfo());
        LocationList locations = LocationList.of(list);
        request.setAttribute("locationListName", list == null ? null : list.getName());
        request.setAttribute("locations", locations.getItemsActiveFirst());
        request.setAttribute("activeLocationCount", locations.getActiveItems().size());
        request.setAttribute("defaultLocation", locations.getDefault(getLoggedInInfo()));
        request.setAttribute("locationRequired", locationManager.isRequired(getLoggedInInfo()));
        request.setAttribute("canDeactivate", isCanDeactivate());
        // A rename asks first when it would also rename upcoming appointments, so the page needs their count.
        if (canChange()) {
            request.setAttribute("upcomingCounts", locationManager.countUpcomingAppointments(getLoggedInInfo()));
        }
        // The Add link opens the Look-Up List Manager, whose Add needs write, not update.
        request.setAttribute("canAdd", hasAnyPrivilege(ADMIN, SecurityInfoManager.WRITE));
        return SUCCESS;
    }

    /*
     * Every change acts on one location. The manager takes any lookup list's item, so the posted id
     * is resolved against the Location List, loaded once, before the change sees it.
     */
    private String changeLocation(BiPredicate<LocationList, LookupListItem> change) {
        return change(() -> {
            int id = intParameter("ID");
            LocationList locations = LocationList.of(locationManager.findLocationList(getLoggedInInfo()));
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

        return locationManager.rename(getLoggedInInfo(), item.getId(), name);
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

    /* Makes a location the default; the manager refuses an inactive one. */
    private boolean makeDefault(LocationList locations, LookupListItem item) {
        locationManager.makeDefault(getLoggedInInfo(), item.getId());
        return true;
    }

    /* Removes the default, which the manager does only while it is still this location. */
    private boolean removeDefault(LocationList locations, LookupListItem item) {
        locationManager.removeDefault(getLoggedInInfo(), item.getId());
        return true;
    }

    /*
     * Posts the switch's new state rather than a toggle, so a page left open can't flip it back. An
     * unchecked checkbox posts nothing, which turns the requirement off.
     */
    private boolean updateRequirement() {
        locationManager.setRequired(getLoggedInInfo(), Boolean.parseBoolean(value()));
        return true;
    }

    /*
     * Answers a rename's confirmation with the location's count as of now. It changes nothing, so a
     * GET will do, but only users who may rename are told, as only they see the counts on the page.
     */
    private String upcomingCount() {
        if (!canChange()) {
            throw new SecurityException("missing the privilege to change appointment settings");
        }
        int id;
        try {
            id = intParameter("ID");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        long count = locationManager.countUpcomingAppointments(getLoggedInInfo()).getOrDefault(id, 0L);
        response.setContentType("application/json");
        try {
            response.getWriter().write(JSON.writeValueAsString(Map.of("count", count)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return null;
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
