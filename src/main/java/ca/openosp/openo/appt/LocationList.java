package ca.openosp.openo.appt;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import ca.openosp.OscarProperties;
import ca.openosp.openo.commn.IsPropertiesOn;
import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.managers.LookupListManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

/**
 * The Location List as the appointment screens use it: the places an appointment can be booked.
 *
 * <p>When the Location List is the clinic's Location Mode (see {@link #isLocationMode()}), the
 * booking screens offer its items in place of the free-text location. Choosing an item saves its id
 * as the appointment's location code and a snapshot of its label as the location text, so screens
 * that only read the text keep working and a renamed item doesn't rewrite past appointments.</p>
 *
 * @since 2026-09-15
 */
public final class LocationList {

    /** The width of appointment.location, which holds the chosen item's label. */
    public static final int LABEL_MAX_LENGTH = 80;

    private final List<LookupListItem> items;

    private LocationList(List<LookupListItem> items) {
        this.items = items;
    }

    /**
     * Loads the Location List.
     *
     * @param loggedInInfo LoggedInInfo the current user; no privilege is needed
     * @return LocationList the list, which is empty if the list is missing
     */
    public static LocationList load(LoggedInInfo loggedInInfo) {
        LookupListManager lookupListManager = SpringUtils.getBean(LookupListManager.class);
        return of(lookupListManager.findAppointmentLocationList(loggedInInfo));
    }

    /**
     * Wraps a Location List that was already loaded.
     *
     * @param list LookupList the list from {@link LookupListManager#findAppointmentLocationList}, or null
     * @return LocationList the list, which is empty if list is null
     */
    public static LocationList of(LookupList list) {
        return new LocationList(list == null ? List.of() : list.getItems());
    }

    /**
     * Saves the location a booking form posted. Every booking form writer saves the location
     * through here, so a location code is never saved without its label.
     *
     * <ul>
     *   <li>No {@code locationCode} field: the form offered sites, programs or free text, so the
     *       posted {@code location} text is saved and the location code left as it is.</li>
     *   <li>An item of the list, active or not: its id becomes the location code and its label,
     *       cut to the column width, the location text.</li>
     *   <li>Blank, or anything else: the location code is cleared and the posted {@code location}
     *       text saved. That text is the Legacy Location the edit screen offered, or blank.</li>
     * </ul>
     *
     * @param appointment Appointment the appointment about to be saved
     * @param request HttpServletRequest the booking form's request
     */
    public static void applyPostedLocation(Appointment appointment, HttpServletRequest request) {
        appointment.setLocation(request.getParameter("location"));
        String posted = request.getParameter("locationCode");
        if (posted == null) {
            return;
        }

        LookupListItem item = load(LoggedInInfo.getLoggedInInfoFromSession(request)).find(parseCode(posted));
        appointment.setLocationCode(item == null ? null : item.getId());
        if (item != null) {
            appointment.setLocation(StringUtils.left(item.getLabel(), LABEL_MAX_LENGTH));
        }
    }

    /**
     * Finds the Legacy Location a booking offers to keep: its location text, when its location code
     * is not an item of this list.
     *
     * @param code Integer the booking's location code, or null
     * @param location String the booking's location text, or null
     * @return String location, or null when code is an item of this list
     */
    public String getLegacyLocation(Integer code, String location) {
        return find(code) == null ? location : null;
    }

    /**
     * Reads a location code as a booking form posts it.
     *
     * @param posted String the {@code locationCode} field's value, or null
     * @return Integer the LookupListItem id, or null if posted is blank or not a positive number
     */
    public static Integer parseCode(String posted) {
        int id = NumberUtils.toInt(posted, 0);
        return id > 0 ? id : null;
    }

    /**
     * Whether the booking screens offer the Location List. Site setups come first: multisite sites,
     * then schedule sites ({@code scheduleSiteID}), then CAISI program locations. The list only
     * replaces the free-text location, and only once it has an active item, so there is no separate
     * switch to leave half-set.
     *
     * @return boolean true if the booking screens offer the list's active items
     */
    public boolean isLocationMode() {
        OscarProperties properties = OscarProperties.getInstance();
        boolean programLocations = StringUtils.containsIgnoreCase(properties.getProperty("ModuleNames"), "Caisi")
                && "true".equals(properties.getProperty("useProgramLocation"));
        return !IsPropertiesOn.isMultisitesEnable()
                && properties.getProperty("scheduleSiteID", "").isEmpty()
                && !programLocations
                && items.stream().anyMatch(LookupListItem::isActive);
    }

    /**
     * Finds an item of this list, active or not.
     *
     * @param id Integer the LookupListItem id, such as an appointment's location code
     * @return LookupListItem the item, or null if id is null or not an item of this list
     */
    public LookupListItem find(Integer id) {
        return items.stream().filter(item -> Objects.equals(item.getId(), id)).findFirst().orElse(null);
    }

    /**
     * Lists the items a new booking may choose.
     *
     * @return List&lt;LookupListItem&gt; the active items, in display order
     */
    public List<LookupListItem> getActiveItems() {
        return items.stream().filter(LookupListItem::isActive).toList();
    }

    /**
     * Whether another active location already has this name, ignoring case and surrounding
     * spaces. Two locations with one name can't be told apart in the booking dropdown or in a
     * patient's history. Inactive locations are not compared: no new booking offers them, so a
     * clash with one is caught when it is enabled.
     *
     * @param name String the name wanted
     * @param item LookupListItem the location that would take the name, which may keep its own
     * @return boolean true if a different active item already has the name
     * @since 2026-09-17
     */
    public boolean isNameTaken(String name, LookupListItem item) {
        String wanted = name.trim();
        return items.stream()
                .filter(LookupListItem::isActive)
                .filter(other -> !Objects.equals(other.getId(), item.getId()))
                .anyMatch(other -> other.getLabel().trim().equalsIgnoreCase(wanted));
    }

    /**
     * Lists every item as the Location tab shows them: the active items in display order, then the
     * inactive ones by name. An inactive item's stored display order says nothing: a location added
     * later takes the order an inactive one still holds.
     *
     * @return List&lt;LookupListItem&gt; the active items in display order, then the inactive items by name
     * @since 2026-09-17
     */
    public List<LookupListItem> getItemsActiveFirst() {
        Stream<LookupListItem> inactive = items.stream()
                .filter(item -> !item.isActive())
                .sorted(Comparator.comparing(LookupListItem::getLabel, String.CASE_INSENSITIVE_ORDER));
        return Stream.concat(getActiveItems().stream(), inactive).toList();
    }

    /**
     * Lists the items a booking may choose when it already has a location code: the active items,
     * plus its current item if that has since been deactivated, so saving it unchanged keeps it.
     *
     * @param current Integer the booking's location code, or null
     * @return List&lt;LookupListItem&gt; the choices, in display order
     */
    public List<LookupListItem> getChoices(Integer current) {
        LookupListItem currentItem = find(current);
        return items.stream().filter(item -> item.isActive() || item == currentItem).toList();
    }
}
