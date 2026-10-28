package ca.openosp.openo.managers;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.openosp.openo.appt.LocationList;
import ca.openosp.openo.commn.dao.LookupListDao;
import ca.openosp.openo.commn.dao.LookupListItemDao;
import ca.openosp.openo.commn.dao.OscarAppointmentDao;
import ca.openosp.openo.commn.dao.SystemPreferencesDao;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.commn.model.SystemPreferences;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.utility.LoggedInInfo;

/**
 * The Location List: the lookup list whose items are the places an appointment can be booked, with
 * its Default Location and Location Requirement. Its items are edited through
 * {@link LookupListManager} like any list's, which also clears the default when it disables that
 * location; this manager holds what only locations have: renaming one also renames it on its
 * upcoming appointments, and the two clinic-wide settings, which are system preferences.
 *
 * @since 2026-10-23
 */
@Service
public class AppointmentLocationManager {

    /** The name of the Location List; {@link #findLocationList} is how code should find the list. */
    public static final String LOCATION_LIST = "appointmentLocationCode";

    private static final SystemPreferences.APPOINTMENT_LOCATION_KEYS DEFAULT =
            SystemPreferences.APPOINTMENT_LOCATION_KEYS.appointment_default_location;
    private static final SystemPreferences.APPOINTMENT_LOCATION_KEYS REQUIRED =
            SystemPreferences.APPOINTMENT_LOCATION_KEYS.appointment_location_required;

    @Autowired
    private LookupListDao lookupListDao;
    @Autowired
    private LookupListItemDao lookupListItemDao;
    @Autowired
    private SystemPreferencesDao systemPreferencesDao;
    @Autowired
    private OscarAppointmentDao appointmentDao;
    @Autowired
    private SecurityInfoManager securityInfoManager;

    /**
     * Finds the Location List. Every appointment screen resolves the list here; the Look-Up List
     * Manager recognises it by {@link #LOCATION_LIST}. It needs no privilege, because booking screens
     * load it for every user.
     *
     * @param loggedInInfo LoggedInInfo the current user
     * @return LookupList the Location List with all its items, active or not, or null if it is missing
     * @since 2026-09-15
     */
    public LookupList findLocationList(LoggedInInfo loggedInInfo) {
        return lookupListDao.findByName(LOCATION_LIST);
    }

    /**
     * Finds the Default Location: the Location List item new bookings start on. It is stored apart
     * from the item, so a caller must still check the item is active. It needs no privilege, because
     * booking screens read it for every user.
     *
     * @param loggedInInfo LoggedInInfo the current user
     * @return Integer the item's id, or null when none is set or the stored value is not an id
     * @since 2026-09-30
     */
    public Integer findDefault(LoggedInInfo loggedInInfo) {
        SystemPreferences preference = systemPreferencesDao.findPreferenceByName(DEFAULT);
        int id = preference == null ? 0 : NumberUtils.toInt(preference.getValue(), 0);
        return id > 0 ? id : null;
    }

    /**
     * Makes an active location the Default Location, in place of any other. An inactive one can't
     * be, because no new booking offers it.
     *
     * @param loggedInInfo LoggedInInfo the current user, who needs _admin update
     * @param locationId int the location to make the default
     * @throws RuntimeException if the user lacks _admin update
     * @throws IllegalArgumentException if there is no such location in the Location List or it is inactive
     * @since 2026-09-30
     */
    public void makeDefault(LoggedInInfo loggedInInfo, int locationId) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null)) {
            throw new RuntimeException("Access Denied");
        }

        LookupListItem location = findLocation(loggedInInfo, locationId);
        if (location == null || !location.isActive()) {
            throw new IllegalArgumentException("only an active location can be the default: " + locationId);
        }
        saveSetting(loggedInInfo, DEFAULT, String.valueOf(locationId), "makeDefault", "Default Location lookupListItem Id");
    }

    /**
     * Leaves no Default Location, but only while it is still this location: a page left open while
     * another location was made the default must not remove that one. Disabling a location calls it
     * too ({@link LookupListManager#removeLookupListItem}), so enabling the location again doesn't
     * bring the default back.
     *
     * @param loggedInInfo LoggedInInfo the current user, who needs _admin update
     * @param locationId int the location that should stop being the default
     * @throws RuntimeException if the user lacks _admin update and the location is the default
     * @since 2026-09-30
     */
    @Transactional
    public void removeDefault(LoggedInInfo loggedInInfo, int locationId) {
        if (Objects.equals(findDefault(loggedInInfo), locationId)) {
            saveSetting(loggedInInfo, DEFAULT, "", "removeDefault", "Default Location lookupListItem Id");
        }
    }

    /**
     * Whether the Location Requirement is on: a new booking can't be saved with a Blank Location. It
     * is off until an administrator turns it on, and needs no privilege, because booking screens
     * read it for every user. Whether it applies to a booking screen is for the caller to decide.
     *
     * @param loggedInInfo LoggedInInfo the current user
     * @return boolean true if new bookings must choose a location
     * @since 2026-09-30
     */
    public boolean isRequired(LoggedInInfo loggedInInfo) {
        return systemPreferencesDao.isReadBooleanPreference(REQUIRED);
    }

    /**
     * Turns the Location Requirement on or off.
     *
     * @param loggedInInfo LoggedInInfo the current user, who needs _admin update
     * @param required boolean true if new bookings must choose a location
     * @throws RuntimeException if the user lacks _admin update
     * @since 2026-09-30
     */
    public void setRequired(LoggedInInfo loggedInInfo, boolean required) {
        saveSetting(loggedInInfo, REQUIRED, String.valueOf(required), "setRequired", "Location Requirement");
    }

    /**
     * Renames a location, and renames it on its appointments from
     * {@link LocationList#firstRenamedDate()} on, in the same transaction. Earlier appointments keep
     * the name they were saved with: a location records where a visit happened, while an upcoming
     * appointment says where the patient is going. Each renamed appointment is archived first and
     * recorded as updated by the user (see {@link OscarAppointmentDao#renameLocationFrom}); saving
     * the name it already has renames none.
     *
     * @param loggedInInfo LoggedInInfo the current user, who needs _admin update
     * @param locationId int the location to rename
     * @param name String the new name, which is trimmed
     * @return boolean true if the location exists and was renamed, false if there is no such location
     *         in the Location List
     * @throws RuntimeException if the user lacks _admin update
     * @throws IllegalArgumentException if name is blank, which would leave an unnamed choice, or
     *         longer than {@link LocationList#LABEL_MAX_LENGTH}, an appointment's copy of it
     * @since 2026-09-17
     */
    @Transactional
    public boolean rename(LoggedInInfo loggedInInfo, int locationId, String name) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null)) {
            throw new RuntimeException("Access Denied");
        }

        String label = name == null ? "" : name.trim();
        if (label.isEmpty() || label.length() > LocationList.LABEL_MAX_LENGTH) {
            throw new IllegalArgumentException("location name must be 1-" + LocationList.LABEL_MAX_LENGTH + " characters");
        }

        LookupListItem location = findLocation(loggedInInfo, locationId);
        if (location == null) {
            return false;
        }

        String previous = location.getLabel();
        location.setLabel(label);
        lookupListItemDao.merge(location);
        LogAction.addLogSynchronous(loggedInInfo, "AppointmentLocationManager.rename",
                "Renamed lookupListItem Id: " + locationId + ", was [" + previous + "], now [" + label + "]");

        if (!label.equals(previous)) {
            Date from = LocationList.firstRenamedDate();
            int renamed = appointmentDao.renameLocationFrom(locationId, label, from, loggedInInfo.getLoggedInProviderNo());
            LogAction.addLogSynchronous(loggedInInfo, "AppointmentLocationManager.rename",
                    "Renamed lookupListItem Id: " + locationId + " on " + renamed + " appointments dated on or after " + from);
        }

        return true;
    }

    /**
     * Counts each location's appointments from {@link LocationList#firstRenamedDate()} on, whatever
     * their status: the ones renaming it would rename.
     *
     * @param loggedInInfo LoggedInInfo the current user, who needs _admin read
     * @return Map&lt;Integer, Long&gt; the count by location id; a location with none is left out
     * @throws RuntimeException if the user lacks _admin read
     * @since 2026-10-16
     */
    public Map<Integer, Long> countUpcomingAppointments(LoggedInInfo loggedInInfo) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.READ, null)) {
            throw new RuntimeException("Access Denied");
        }

        return appointmentDao.countByLocationCodeFrom(LocationList.firstRenamedDate());
    }

    /* Finds a location by id, or null if there is none or the item belongs to another look-up list. */
    private LookupListItem findLocation(LoggedInInfo loggedInInfo, int locationId) {
        LookupList locationList = findLocationList(loggedInInfo);
        LookupListItem item = lookupListItemDao.find(locationId);
        return locationList != null && item != null && Objects.equals(locationList.getId(), item.getLookupListId()) ? item : null;
    }

    /* Saves one Location tab setting, creating its preference on first use, and audits the change. */
    private void saveSetting(LoggedInInfo loggedInInfo, SystemPreferences.APPOINTMENT_LOCATION_KEYS key,
                             String value, String change, String logName) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null)) {
            throw new RuntimeException("Access Denied");
        }

        SystemPreferences preference = Objects.requireNonNullElseGet(systemPreferencesDao.findPreferenceByName(key),
                () -> new SystemPreferences(key.name()));
        String previous = preference.getValue();
        preference.setValue(value);
        preference.setUpdateDate(new Date());
        systemPreferencesDao.saveEntity(preference);
        LogAction.addLogSynchronous(loggedInInfo, "AppointmentLocationManager." + change,
                logName + " was [" + previous + "], now [" + value + "]");
    }
}
