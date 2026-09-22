package ca.openosp.openo.appt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import ca.openosp.OscarProperties;
import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.PMmodule.service.ProgramManager;
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
 * <p>It also decides how the schedule marks and names an appointment's location, which depends on
 * whether the clinic books by a site setup (sites, schedule sites or CAISI programs) instead.</p>
 *
 * @since 2026-09-15
 */
public final class LocationList {

    /** The width of appointment.location, which holds the chosen item's label. */
    public static final int LABEL_MAX_LENGTH = 80;

    private final List<LookupListItem> items;

    /** Program names already looked up, by program id; a day view names the same few programs many times. */
    private final Map<Integer, Optional<String>> programNames = new HashMap<>();

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
     *       posted {@code location} text is saved through {@link #setLocationText}, which keeps the
     *       code only while the text is unchanged (as when the list was switched off since booking).</li>
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
        String posted = request.getParameter("locationCode");
        if (posted == null) {
            setLocationText(appointment, request.getParameter("location"));
            return;
        }

        LookupListItem item = load(LoggedInInfo.getLoggedInInfoFromSession(request)).find(parseCode(posted));
        appointment.setLocationCode(item == null ? null : item.getId());
        appointment.setLocation(item == null ? request.getParameter("location")
                : StringUtils.left(item.getLabel(), LABEL_MAX_LENGTH));
    }

    /**
     * Saves a location text that wasn't chosen from the Location List: typed on a booking form, or
     * sent by an integration. The location code is kept while the text is unchanged (null and blank
     * count as the same) and cleared once it changes, because the new text is no longer that item's
     * name. Use it wherever a saved appointment's location text changes outside the Location List,
     * booking forms and integrations alike, so a code never outlives its label.
     *
     * @param appointment Appointment the appointment about to be saved
     * @param text String the new location text, or null
     * @since 2026-09-22
     */
    public static void setLocationText(Appointment appointment, String text) {
        if (!StringUtils.defaultString(appointment.getLocation()).equals(StringUtils.defaultString(text))) {
            appointment.setLocationCode(null);
        }
        appointment.setLocation(text);
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
     * Names an appointment's location as the schedule shows it: what the booking screen offered.
     * Outside site setups, a location code that is an item of this list shows the item's current
     * name, as its chip does. In a site setup, the saved location text: the site's name, or with
     * CAISI program locations the program's location, or its name when it has none, since a
     * booking saves the program's id.
     *
     * @param appointment Appointment the appointment shown
     * @return String the location's name, or blank when the appointment has none
     * @since 2026-09-21
     */
    public String getDisplayName(Appointment appointment) {
        LookupListItem item = getChipItem(appointment);
        String name = item != null ? item.getLabel()
                : isProgramSetup() ? programName(appointment.getLocation()) : appointment.getLocation();
        return name == null || "null".equals(name) ? "" : name.trim();
    }

    /**
     * Finds the item whose chip marks an appointment on the schedule. Site setups show their own
     * location, so a code saved before the clinic switched to one draws no chip.
     *
     * @param appointment Appointment the appointment shown
     * @return LookupListItem the appointment's item, active or not, or null for no chip
     * @since 2026-09-22
     */
    public LookupListItem getChipItem(Appointment appointment) {
        return isSiteSetup() ? null : find(appointment.getLocationCode());
    }

    /**
     * Names the program a program-location booking saved by id, as the booking dropdown names it.
     * Any program the id matches is named, active or not; other text is returned as it is.
     */
    private String programName(String location) {
        Integer id = parseCode(StringUtils.trim(location));
        if (id == null) {
            return location;
        }
        return programNames.computeIfAbsent(id, key -> {
            ProgramManager programManager = SpringUtils.getBean(ProgramManager.class);
            Program program = programManager.getProgram(key);
            return Optional.ofNullable(program)
                    .map(p -> StringUtils.isBlank(p.getLocation()) ? p.getName() : p.getLocation());
        }).orElse(location);
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
        return !isSiteSetup() && items.stream().anyMatch(LookupListItem::isActive);
    }

    /** Whether the booking screens offer a site setup: multisite sites, schedule sites or CAISI programs. */
    private static boolean isSiteSetup() {
        return IsPropertiesOn.isMultisitesEnable()
                || !OscarProperties.getInstance().getProperty("scheduleSiteID", "").isEmpty()
                || isProgramSetup();
    }

    /** Whether the booking screens offer CAISI programs; sites come first when both are on. */
    private static boolean isProgramSetup() {
        OscarProperties properties = OscarProperties.getInstance();
        return !IsPropertiesOn.isMultisitesEnable()
                && StringUtils.containsIgnoreCase(properties.getProperty("ModuleNames"), "Caisi")
                && "true".equals(properties.getProperty("useProgramLocation"));
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
