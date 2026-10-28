package ca.openosp.openo.appt;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
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
import ca.openosp.openo.managers.AppointmentLocationManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

/**
 * The Location List as the appointment screens use it: the places an appointment can be booked.
 *
 * <p>When the Location List is the clinic's Location Mode (see {@link #getMode()}), the
 * booking screens offer its items in place of the free-text location. Choosing an item saves its id
 * as the appointment's location code and a snapshot of its label as the location text, so screens
 * that only read the text keep working. Every screen shows that saved name: renaming a location
 * also renames it on its appointments from today on
 * ({@link AppointmentLocationManager#rename}), and earlier
 * appointments keep the name they were saved with.</p>
 *
 * <p>It also decides how the schedule marks and names an appointment's location, which depends on
 * whether the clinic books by a site setup (sites, schedule sites or CAISI programs) instead.</p>
 *
 * @since 2026-09-15
 */
public final class LocationList {

    /** The width of appointment.location, which holds the chosen item's label. */
    public static final int LABEL_MAX_LENGTH = 80;

    /** The dropdown value that keeps a booking's Legacy Location, which has no item to name. */
    public static final String LEGACY_VALUE = "legacy";

    /**
     * A booking's location dropdown, as the form posts it: the option chosen, and the Legacy Location
     * offered besides the blank option and the items.
     *
     * @param value String the chosen option's value: an item's id, {@link #LEGACY_VALUE}, or blank for
     *              a Blank Location
     * @param legacy String the Legacy Location offered, or blank when there is none
     * @since 2026-09-23
     */
    public record Choice(String value, String legacy) {

        /**
         * @return Integer the chosen item's id, or null for a Blank Location or the Legacy Location
         */
        public Integer code() {
            return parseCode(value);
        }
    }

    /**
     * A booking's location dropdown, as WEB-INF/tags/locationSelect.tag draws it: the blank option,
     * the Legacy Location if there is one, then the locations. Built by {@link #newBookingDropdown}
     * and {@link #editDropdown}.
     *
     * @param options List&lt;Option&gt; the locations offered, in display order
     * @param selected String the chosen option's value: an item's id, {@link #LEGACY_VALUE}, or blank
     *                 for a Blank Location
     * @param legacy String the Legacy Location offered, or null when there is none
     * @param required boolean true to make the browser refuse a save on the blank option
     * @since 2026-10-23
     */
    public record Dropdown(List<Option> options, String selected, String legacy, boolean required) {
    }

    /**
     * A location a dropdown offers, and the name it shows.
     *
     * @param item LookupListItem the location, active or not
     * @param name String the item's name, or for an edited booking's own location the name the booking
     *             was saved with
     * @since 2026-10-23
     */
    public record Option(LookupListItem item, String name) {
    }

    private final List<LookupListItem> items;

    /** Program names already looked up, by program id; a day view names the same few programs many times. */
    private final Map<Integer, Optional<String>> programNames = new HashMap<>();

    /**
     * How the booking screens offer a location. A site setup comes first, and shows the clinic's own
     * places instead of the Location List: multisite sites, then CAISI program locations, then
     * schedule sites ({@code scheduleSiteID}). Without one, the booking screens offer the Location List
     * once it has an active location, and free text before that, so there is no separate switch to
     * leave half-set.
     *
     * @since 2026-10-23
     */
    public enum LocationMode {
        SITES, PROGRAMS, SCHEDULE_SITES, LOCATION_LIST, FREE_TEXT;

        /**
         * @return boolean true for a site setup, whose schedule shows its own location and no chip
         */
        public boolean isSiteSetup() {
            return this == SITES || this == PROGRAMS || this == SCHEDULE_SITES;
        }
    }

    /**
     * The clinic's Location Mode, read once per list and only when first asked: a day view asks for
     * every appointment, a save never asks, and each missing site setup property logs a warning.
     */
    private LocationMode mode;

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
        AppointmentLocationManager locationManager = SpringUtils.getBean(AppointmentLocationManager.class);
        return of(locationManager.findLocationList(loggedInInfo));
    }

    /**
     * Wraps a Location List that was already loaded.
     *
     * @param list LookupList the list from {@link AppointmentLocationManager#findLocationList}, or null
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
     *   <li>{@link #LEGACY_VALUE}: the location code is cleared and the posted {@code location} text,
     *       the Legacy Location the edit screen offered, is kept.</li>
     *   <li>An item of the list, active or not: its id becomes the location code and its label,
     *       cut to the column width, the location text. An appointment dated before today that
     *       stays at its item keeps its saved name, so editing it doesn't give it a name the
     *       location only took later.</li>
     *   <li>Blank, or anything else: the location code and text are cleared, leaving a Blank Location.</li>
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

        if (LEGACY_VALUE.equals(posted)) {
            appointment.setLocationCode(null);
            appointment.setLocation(request.getParameter("location"));
            return;
        }

        LookupListItem item = load(LoggedInInfo.getLoggedInInfoFromSession(request)).find(parseCode(posted));
        if (item == null) {
            appointment.setLocationCode(null);
            appointment.setLocation("");
        } else if (!keepsSavedName(appointment, item)) {
            appointment.setLocationCode(item.getId());
            appointment.setLocation(StringUtils.left(item.getLabel(), LABEL_MAX_LENGTH));
        }
    }

    /*
     * Whether an appointment staying at its item keeps the name it was saved with: it is dated before
     * the first date a rename reaches. The date is the one about to be saved, so an appointment moved
     * to that date or later takes the item's current name, as the rename would have given it.
     */
    private static boolean keepsSavedName(Appointment appointment, LookupListItem item) {
        Date date = appointment.getAppointmentDate();
        return item.getId().equals(appointment.getLocationCode())
                && StringUtils.isNotBlank(appointment.getLocation())
                && date != null && date.before(firstRenamedDate());
    }

    /**
     * The first appointment date a location's rename reaches: today. Appointments from it on take
     * the new name ({@link AppointmentLocationManager#rename}); earlier ones keep the name
     * they were saved with, and an edit keeps it too ({@link #applyPostedLocation}).
     *
     * @return Date today at midnight, as the appointment date column stores a date
     * @since 2026-10-16
     */
    public static Date firstRenamedDate() {
        return java.sql.Date.valueOf(LocalDate.now());
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
     * Works out the choice an edit screen's dropdown starts on for a saved booking: its item, when
     * its location code is one; otherwise the active item its location text names, ignoring case and
     * surrounding spaces, so a location typed before the list existed is linked on the next save;
     * otherwise its location text as a Legacy Location, chosen; or blank when it is a Blank Location.
     *
     * @param code Integer the booking's location code, or null
     * @param location String the booking's location text, or null
     * @return Choice the dropdown's starting choice
     * @since 2026-09-23
     */
    public Choice choiceFor(Integer code, String location) {
        if (find(code) != null) {
            return new Choice(String.valueOf(code), null);
        }
        return getActiveItems().stream()
                .filter(item -> isSameName(item.getLabel(), location))
                .findFirst()
                .map(item -> new Choice(String.valueOf(item.getId()), null))
                .orElseGet(() -> StringUtils.isBlank(location) ? new Choice("", null) : new Choice(LEGACY_VALUE, location));
    }

    /**
     * Builds a new booking's dropdown: the active locations. It starts on the Default Location, or on
     * the blank option for a Blank Location when there is none, and a patient search keeps staff's
     * choice. It is required under the Location Requirement ({@link #isRequiredForNewBooking}).
     *
     * @param loggedInInfo LoggedInInfo the current user; no privilege is needed
     * @param posted String the {@code locationCode} a patient search posted back, or null on first display
     * @return Dropdown the dropdown to draw
     * @since 2026-10-23
     */
    public Dropdown newBookingDropdown(LoggedInInfo loggedInInfo, String posted) {
        List<Option> options = getActiveItems().stream().map(item -> new Option(item, item.getLabel())).toList();
        String selected = posted != null ? posted : getNewBookingValue(loggedInInfo);
        return new Dropdown(options, selected, null, isRequiredForNewBooking(loggedInInfo));
    }

    /* The Default Location's id, or blank for a Blank Location when there is none. */
    private String getNewBookingValue(LoggedInInfo loggedInInfo) {
        LookupListItem item = getDefault(loggedInInfo);
        return item != null ? String.valueOf(item.getId()) : "";
    }

    /**
     * Builds an edit screen's dropdown for a saved booking. It offers the active locations, plus the
     * location chosen and the one the booking was saved with if either has since been disabled, so
     * saving it unchanged keeps it, and staff who moved off it can still go back after a patient
     * search. The saved location shows the name the booking was saved with ({@link #getDisplayName}):
     * one dated before a rename keeps its old name, and saving it keeps it. It is never required,
     * because a Blank Location is the honest answer when staff aren't sure.
     *
     * @param saved Appointment the booking as saved; after a patient search, null if it is gone since
     *              the page was opened
     * @param posted Choice what a patient search posted back, or null on first display, which starts
     *               on {@link #choiceFor} the saved booking
     * @return Dropdown the dropdown to draw
     * @since 2026-10-23
     */
    public Dropdown editDropdown(Appointment saved, Choice posted) {
        Choice choice = posted != null ? posted : choiceFor(saved.getLocationCode(), saved.getLocation());
        LookupListItem chosen = find(choice.code());
        LookupListItem savedItem = saved == null ? null : find(saved.getLocationCode());
        String savedName = savedItem == null ? null : getDisplayName(saved);
        List<Option> options = items.stream()
                .filter(item -> item.isActive() || item == chosen || item == savedItem)
                .map(item -> new Option(item, item == savedItem && StringUtils.isNotEmpty(savedName) ? savedName : item.getLabel()))
                .toList();
        return new Dropdown(options, choice.value(), choice.legacy(), false);
    }

    /**
     * Finds the Default Location, which is always active. Disabling a location clears the default,
     * but the setting is stored apart from the item, so one naming an inactive item reads as none.
     * It is read on every call.
     *
     * @param loggedInInfo LoggedInInfo the current user; no privilege is needed
     * @return LookupListItem the item an administrator made the default, or null when there is none,
     *         it is inactive, or it is no item of this list
     * @since 2026-09-30
     */
    public LookupListItem getDefault(LoggedInInfo loggedInInfo) {
        AppointmentLocationManager locationManager = SpringUtils.getBean(AppointmentLocationManager.class);
        LookupListItem item = find(locationManager.findDefault(loggedInInfo));
        return item != null && item.isActive() ? item : null;
    }

    /**
     * Whether a new booking must choose a location: the Location Requirement is on and this list is
     * the clinic's Location Mode. A site setup, or a list with no active location, ignores it. An edit
     * screen never asks, because a Blank Location is the honest answer when staff aren't sure.
     *
     * @param loggedInInfo LoggedInInfo the current user; no privilege is needed
     * @return boolean true if a new booking can't be saved with a Blank Location
     * @since 2026-09-30
     */
    public boolean isRequiredForNewBooking(LoggedInInfo loggedInInfo) {
        AppointmentLocationManager locationManager = SpringUtils.getBean(AppointmentLocationManager.class);
        return isLocationMode() && locationManager.isRequired(loggedInInfo);
    }

    /**
     * Names an appointment's location as the schedule shows it: the saved location text, which for
     * a Location List item is its name on the appointment's date, as reports and integrations read
     * it. With CAISI program locations it is the program's location, or its name when it has none,
     * since a booking saves the program's id.
     *
     * @param appointment Appointment the appointment shown
     * @return String the location's name, or blank when the appointment has none
     * @since 2026-09-21
     */
    public String getDisplayName(Appointment appointment) {
        String name = getMode() == LocationMode.PROGRAMS ? programName(appointment.getLocation()) : appointment.getLocation();
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
        return getMode().isSiteSetup() ? null : find(appointment.getLocationCode());
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
     * Works out the clinic's Location Mode from its site setup and this list.
     *
     * @return LocationMode how the booking screens offer a location
     * @since 2026-10-23
     */
    public LocationMode getMode() {
        if (mode == null) {
            OscarProperties properties = OscarProperties.getInstance();
            if (IsPropertiesOn.isMultisitesEnable()) {
                mode = LocationMode.SITES;
            } else if (StringUtils.containsIgnoreCase(properties.getProperty("ModuleNames"), "Caisi")
                    && "true".equals(properties.getProperty("useProgramLocation"))) {
                mode = LocationMode.PROGRAMS;
            } else if (!properties.getProperty("scheduleSiteID", "").isEmpty()) {
                mode = LocationMode.SCHEDULE_SITES;
            } else {
                mode = items.stream().anyMatch(LookupListItem::isActive) ? LocationMode.LOCATION_LIST : LocationMode.FREE_TEXT;
            }
        }
        return mode;
    }

    /**
     * Whether the booking screens offer the Location List ({@link LocationMode#LOCATION_LIST}).
     *
     * @return boolean true if the booking screens offer the list's active items
     */
    public boolean isLocationMode() {
        return getMode() == LocationMode.LOCATION_LIST;
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
        return items.stream()
                .filter(LookupListItem::isActive)
                .filter(other -> !Objects.equals(other.getId(), item.getId()))
                .anyMatch(other -> isSameName(other.getLabel(), name));
    }

    /* Location names match ignoring case and surrounding spaces. */
    private static boolean isSameName(String one, String other) {
        return one != null && other != null && one.trim().equalsIgnoreCase(other.trim());
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
}
