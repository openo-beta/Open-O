//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import ca.openosp.openo.commn.dao.LookupListDao;
import ca.openosp.openo.commn.dao.LookupListItemDao;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.openosp.openo.log.LogAction;

@Service
public class LookupListManager {

    private static final Pattern COLOUR = Pattern.compile("#[0-9a-fA-F]{6}");
    private static final Pattern ICON = Pattern.compile("glyphicon-[a-z0-9-]+");
    private static final String APPOINTMENT_LOCATION_LIST = "appointmentLocationCode";

    /** The width of LookupListItem.label; a screen with a narrower store of its own caps it lower. */
    private static final int LABEL_MAX_LENGTH = 255;

    @Autowired
    private LookupListDao lookupListDao;
    @Autowired
    private LookupListItemDao lookupListItemDao;
    @Autowired
    SecurityInfoManager securityInfoManager;

    public List<LookupList> findAllActiveLookupLists(LoggedInInfo loggedInInfo) {
        return lookupListDao.findAllActive();
    }

    public LookupList findLookupListById(LoggedInInfo loggedInInfo, int id) {
        return lookupListDao.find(id);
    }

    public LookupList findLookupListByName(LoggedInInfo loggedInInfo, String name) {
        return lookupListDao.findByName(name);
    }

    /**
     * Finds the Location List: the one lookup list whose items are the places an appointment can be
     * booked. Every appointment screen resolves the list here, so this is the only place that knows
     * which list it is. Like {@link #findLookupListByName}, it needs no privilege, because booking
     * screens load it for every user.
     *
     * @param loggedInInfo LoggedInInfo the current user
     * @return LookupList the Location List with all its items, active or not, or null if it is missing
     * @since 2026-09-15
     */
    public LookupList findAppointmentLocationList(LoggedInInfo loggedInInfo) {
        return findLookupListByName(loggedInInfo, APPOINTMENT_LOCATION_LIST);
    }

    public LookupList addLookupList(LoggedInInfo loggedInInfo, LookupList lookupList) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.WRITE, null)) {
            throw new RuntimeException("Access Denied");
        }

        lookupListDao.persist(lookupList);
        LogAction.addLogSynchronous(loggedInInfo, "LookupListManager.addLookupList", "id=" + lookupList.getId());

        return (lookupList);
    }


    /**
     * Add a new lookupListItem
     * Ensure that the lookupListItem is associated to a list entry of the LookupList table.
     */
    public LookupListItem addLookupListItem(LoggedInInfo loggedInInfo, LookupListItem lookupListItem) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.WRITE, null)) {
            throw new RuntimeException("Access Denied");
        }

        lookupListItemDao.persist(lookupListItem);
        LogAction.addLogSynchronous(loggedInInfo, "LookupListManager.addLookupListItem", "id=" + lookupListItem.getId());

        return (lookupListItem);
    }

    /**
     * Retrieve all the active select list option items by the lookUpList.id
     */
    public List<LookupListItem> findLookupListItemsByLookupListId(LoggedInInfo loggedInInfo, int lookupListId) {
        return lookupListItemDao.findActiveByLookupListId(lookupListId);
    }

    public LookupListItem findLookupListItemByLookupListIdAndValue(LoggedInInfo loggedInInfo, int lookupListId, String value) {
        return lookupListItemDao.findByLookupListIdAndValue(lookupListId, value);
    }


    /**
     * Retrieve all the active select list option items by the lookupList.name
     */
    public List<LookupListItem> findLookupListItemsByLookupListName(LoggedInInfo loggedInInfo, String lookupListName) {

        LookupList lookupList = findLookupListByName(loggedInInfo, lookupListName);
        List<LookupListItem> lookupListItems = null;

        if (lookupList != null) {
            lookupListItems = findLookupListItemsByLookupListId(loggedInInfo, lookupList.getId());
        }

        return lookupListItems;
    }


    /**
     * Find a specific lookupListItem by it's id
     */
    public LookupListItem findLookupListItemById(LoggedInInfo loggedInInfo, int lookupListItemId) {

        LookupListItem lookupListItem = null;
        if (lookupListItemId > 0) {
            lookupListItem = lookupListItemDao.find(lookupListItemId);
        }
        return lookupListItem;
    }

    /**
     * Update a lookupListItem that has been edited.
     */
    public Integer updateLookupListItem(LoggedInInfo loggedInInfo, LookupListItem lookupListItem) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null)) {
            throw new RuntimeException("Access Denied");
        }

        lookupListItemDao.merge(lookupListItem);
        Integer id = lookupListItem.getId();
        LogAction.addLogSynchronous(loggedInInfo, "LookupListManager.updateLookupListItem", "Merged LookupListItem Id: " + id);

        return id;
    }

    /**
     * Remove a lookupListItem by it's id.
     */
    public boolean removeLookupListItem(LoggedInInfo loggedInInfo, int lookupListItemId) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.DELETE, null)) {
            throw new RuntimeException("Access Denied");
        }

        LookupListItem lookupListItem = findLookupListItemById(loggedInInfo, lookupListItemId);
        Integer id = null;

        if (lookupListItem != null) {
            lookupListItem.setActive(Boolean.FALSE);
            id = updateLookupListItem(loggedInInfo, lookupListItem);
        }
        LogAction.addLogSynchronous(loggedInInfo, "LookupListManager.removeLookupListItem", "Removed lookupListItem Id: " + id);

        return (id == lookupListItemId);
    }

    /**
     * Change the display order sequence of this lookupListItem
     *
     * @param lookupListItemId
     */
    public boolean updateLookupListItemDisplayOrder(LoggedInInfo loggedInInfo, int lookupListItemId, int lookupListItemDisplayOrder) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null)) {
            throw new RuntimeException("Access Denied");
        }

        LookupListItem lookupListItem = findLookupListItemById(loggedInInfo, lookupListItemId);
        Integer id = null;

        if (lookupListItem != null) {
            lookupListItem.setDisplayOrder(lookupListItemDisplayOrder);
            id = updateLookupListItem(loggedInInfo, lookupListItem);
        }

        LogAction.addLogSynchronous(loggedInInfo, "LookupListManager.updateLookupListItemDisplayOrder",
                "Changed display order for lookupListItem Id: " + id + " To: " + lookupListItemDisplayOrder);

        return (id == lookupListItemId);
    }

    /**
     * Sets or clears the colour a lookupListItem is drawn in.
     *
     * @param loggedInInfo LoggedInInfo the current user, who needs _admin update
     * @param lookupListItemId int the item to change
     * @param colour String a #rrggbb hex colour, or null or blank to clear it
     * @return boolean true if the item exists and was updated, false if there is no such item
     * @throws RuntimeException if the user lacks _admin update
     * @throws IllegalArgumentException if colour is neither blank nor #rrggbb
     * @since 2026-09-15
     */
    public boolean updateLookupListItemColour(LoggedInInfo loggedInInfo, int lookupListItemId, String colour) {
        return updateLookupListItemStyle(loggedInInfo, lookupListItemId, colour, COLOUR, LookupListItem::setColour);
    }

    /**
     * Sets or clears the icon drawn for a lookupListItem.
     *
     * @param loggedInInfo LoggedInInfo the current user, who needs _admin update
     * @param lookupListItemId int the item to change
     * @param icon String a glyphicon class name such as glyphicon-home, or null or blank to clear it
     * @return boolean true if the item exists and was updated, false if there is no such item
     * @throws RuntimeException if the user lacks _admin update
     * @throws IllegalArgumentException if icon is neither blank nor a glyphicon class name
     * @since 2026-09-15
     */
    public boolean updateLookupListItemIcon(LoggedInInfo loggedInInfo, int lookupListItemId, String icon) {
        return updateLookupListItemStyle(loggedInInfo, lookupListItemId, icon, ICON, LookupListItem::setIcon);
    }

    /**
     * Renames a lookupListItem.
     *
     * <p>Screens that saved the old name keep it: an appointment stores the location's name at
     * booking time, not a reference to it.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user, who needs _admin update
     * @param lookupListItemId int the item to rename
     * @param label String the new name, which is trimmed
     * @return boolean true if the item exists and was renamed, false if there is no such item
     * @throws RuntimeException if the user lacks _admin update
     * @throws IllegalArgumentException if label is blank or longer than the column
     * @since 2026-09-17
     */
    public boolean updateLookupListItemLabel(LoggedInInfo loggedInInfo, int lookupListItemId, String label) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null)) {
            throw new RuntimeException("Access Denied");
        }

        String name = label == null ? "" : label.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("lookupListItem label must not be blank");
        }
        if (name.length() > LABEL_MAX_LENGTH) {
            throw new IllegalArgumentException("lookupListItem label must be at most " + LABEL_MAX_LENGTH + " characters");
        }

        LookupListItem lookupListItem = findLookupListItemById(loggedInInfo, lookupListItemId);
        if (lookupListItem == null) {
            return false;
        }

        String previous = lookupListItem.getLabel();
        lookupListItem.setLabel(name);
        updateLookupListItem(loggedInInfo, lookupListItem);
        LogAction.addLogSynchronous(loggedInInfo, "LookupListManager.updateLookupListItemLabel",
                "Renamed lookupListItem Id: " + lookupListItemId + ", was [" + previous + "], now [" + name + "]");

        return true;
    }

    /**
     * Makes a deactivated lookupListItem a choice again, at the end of its list.
     *
     * <p>The reverse of {@link #removeLookupListItem}. An item that is already active is left
     * where it is.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user, who needs _admin update
     * @param lookupListItemId int the item to restore
     * @return boolean true if the item exists and is now active, false if there is no such item
     * @throws RuntimeException if the user lacks _admin update
     * @since 2026-09-17
     */
    public boolean restoreLookupListItem(LoggedInInfo loggedInInfo, int lookupListItemId) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null)) {
            throw new RuntimeException("Access Denied");
        }

        LookupListItem lookupListItem = findLookupListItemById(loggedInInfo, lookupListItemId);
        if (lookupListItem == null) {
            return false;
        }

        if (!lookupListItem.isActive()) {
            List<LookupListItem> active = findLookupListItemsByLookupListId(loggedInInfo, lookupListItem.getLookupListId());
            int last = active.isEmpty() ? 0 : active.get(active.size() - 1).getDisplayOrder();

            lookupListItem.setActive(Boolean.TRUE);
            lookupListItem.setDisplayOrder(last + 1);
            updateLookupListItem(loggedInInfo, lookupListItem);
            LogAction.addLogSynchronous(loggedInInfo, "LookupListManager.restoreLookupListItem",
                    "Restored lookupListItem Id: " + lookupListItemId + " at display order " + (last + 1));
        }

        return true;
    }

    /**
     * Moves an active lookupListItem one place along its list's display order.
     *
     * @param loggedInInfo LoggedInInfo the current user, who needs _admin update
     * @param lookupListItemId int the item to move
     * @param up boolean true to move it towards the start of the list, false towards the end
     * @return boolean true if the item moved, false if there is no such active item or it is
     *         already at that end of the list
     * @throws RuntimeException if the user lacks _admin update
     * @since 2026-09-17
     */
    @Transactional
    public boolean moveLookupListItem(LoggedInInfo loggedInInfo, int lookupListItemId, boolean up) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null)) {
            throw new RuntimeException("Access Denied");
        }

        LookupListItem lookupListItem = findLookupListItemById(loggedInInfo, lookupListItemId);
        if (lookupListItem == null || !lookupListItem.isActive()) {
            return false;
        }

        List<LookupListItem> ordered =
                new ArrayList<>(findLookupListItemsByLookupListId(loggedInInfo, lookupListItem.getLookupListId()));
        int from = indexOfItem(ordered, lookupListItemId);
        int to = up ? from - 1 : from + 1;
        if (from < 0 || to < 0 || to >= ordered.size()) {
            return false;
        }

        Collections.swap(ordered, from, to);
        renumber(loggedInInfo, ordered);
        LogAction.addLogSynchronous(loggedInInfo, "LookupListManager.moveLookupListItem",
                "Moved lookupListItem Id: " + lookupListItemId + " " + (up ? "up" : "down")
                        + " to display order " + (to + 1));

        return true;
    }

    private static int indexOfItem(List<LookupListItem> items, int lookupListItemId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() != null && items.get(i).getId() == lookupListItemId) {
                return i;
            }
        }
        return -1;
    }

    /*
     * Numbers the items 1..n, writing only the ones that move. Display orders can repeat, because
     * an item added while another is deactivated takes an order the deactivated one still holds,
     * so swapping two stored orders would not always reorder anything.
     */
    private void renumber(LoggedInInfo loggedInInfo, List<LookupListItem> ordered) {
        for (int i = 0; i < ordered.size(); i++) {
            LookupListItem item = ordered.get(i);
            if (item.getDisplayOrder() != i + 1) {
                item.setDisplayOrder(i + 1);
                updateLookupListItem(loggedInInfo, item);
            }
        }
    }

    /*
     * Style values are later written into class and style attributes, so only
     * values matching the pattern are stored; blank means clear.
     */
    private boolean updateLookupListItemStyle(LoggedInInfo loggedInInfo, int lookupListItemId, String value,
                                              Pattern pattern, BiConsumer<LookupListItem, String> setter) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null)) {
            throw new RuntimeException("Access Denied");
        }

        String style = value == null || value.isBlank() ? null : value.trim();
        if (style != null && !pattern.matcher(style).matches()) {
            throw new IllegalArgumentException("lookupListItem style value must match " + pattern.pattern());
        }

        LookupListItem lookupListItem = findLookupListItemById(loggedInInfo, lookupListItemId);
        if (lookupListItem == null) {
            return false;
        }

        setter.accept(lookupListItem, style);
        updateLookupListItem(loggedInInfo, lookupListItem);
        return true;
    }
}
