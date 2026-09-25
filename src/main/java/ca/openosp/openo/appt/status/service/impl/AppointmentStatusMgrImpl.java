//CHECKSTYLE:OFF
/**
 * Copyright (c) 2006-. OSCARservice, OpenSoft System. All Rights Reserved.
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
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.openosp.openo.appt.status.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ca.openosp.openo.commn.dao.AppointmentStatusDao;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.appt.status.service.AppointmentStatusMgr;

/**
 * @author toby
 */

public class AppointmentStatusMgrImpl implements AppointmentStatusMgr {

    private static AppointmentStatusDao appointStatusDao = SpringUtils.getBean(AppointmentStatusDao.class);

    private static final Pattern COLOUR = Pattern.compile("#[0-9a-fA-F]{6}");

    private record StatusStyle(String description, String colour, String icon) {
    }

    /*
     * Each status's Default Status Style, as seeded by database/mysql/oscardata.sql, keyed by status
     * code: row ids differ between installs (older databases have 13 rows and no Confirmed).
     */
    private static final Map<String, StatusStyle> DEFAULT_STYLES = Map.ofEntries(
            Map.entry("t", new StatusStyle("To Do", "#FDFEC7", "starbill.gif")),
            Map.entry("T", new StatusStyle("Daysheet Printed", "#FDFEC7", "todo.gif")),
            Map.entry("H", new StatusStyle("Here", "#00ee00", "here.gif")),
            Map.entry("P", new StatusStyle("Picked", "#FFBBFF", "picked.gif")),
            Map.entry("E", new StatusStyle("Empty Room", "#FFFF33", "empty.gif")),
            Map.entry("a", new StatusStyle("Customized 1", "#897DF8", "1.gif")),
            Map.entry("b", new StatusStyle("Customized 2", "#897DF8", "2.gif")),
            Map.entry("c", new StatusStyle("Customized 3", "#897DF8", "3.gif")),
            Map.entry("d", new StatusStyle("Customized 4", "#897DF8", "4.gif")),
            Map.entry("e", new StatusStyle("Customized 5", "#897DF8", "5.gif")),
            Map.entry("f", new StatusStyle("Customized 6", "#897DF8", "5.gif")),
            Map.entry("h", new StatusStyle("Confirmed", "#2fcccf", "thumb.png")),
            Map.entry("N", new StatusStyle("No Show", "#cccccc", "noshow.gif")),
            Map.entry("C", new StatusStyle("Cancelled", "#999999", "cancel.gif")),
            Map.entry("B", new StatusStyle("Billed", "#3ea4e1", "billed.gif")));

    private static List<AppointmentStatus> cachedActiveStatuses = null;
    private static boolean cacheIsDirty = false;

    public static List<AppointmentStatus> getCachedActiveStatuses() {
        if (cachedActiveStatuses == null || cacheIsDirty) {
            cachedActiveStatuses = appointStatusDao.findActive();
        }
        return cachedActiveStatuses;
    }

    @SuppressWarnings("unchecked")
    public static synchronized void setCachedActiveStatuses(List<AppointmentStatus> cachedActiveStatuses) {
        Collections.sort(cachedActiveStatuses, Comparator.comparing(AppointmentStatus::getId));
        AppointmentStatusMgrImpl.cachedActiveStatuses = cachedActiveStatuses;
    }


    public static boolean isCacheIsDirty() {
        return cacheIsDirty;
    }

    public static void setCacheIsDirty(boolean cacheIsDirty) {
        AppointmentStatusMgrImpl.cacheIsDirty = cacheIsDirty;
    }

    public List<AppointmentStatus> getAllStatus() {
        return appointStatusDao.findAll();
    }

    public List<AppointmentStatus> getAllActiveStatus() {
        if (cacheIsDirty) {
            setCachedActiveStatuses(appointStatusDao.findActive());
            cacheIsDirty = false;
        }
        return appointStatusDao.findActive();
    }

    public AppointmentStatus getStatus(int ID) {
        return appointStatusDao.find(ID);
    }

    public boolean changeStatus(int id, int active) {
        if (active != 0 && active != 1) {
            throw new IllegalArgumentException("appointment status active must be 0 or 1");
        }
        return updateEditable(id, active, AppointmentStatus::setActive);
    }

    public boolean updateDescription(int id, String description) {
        String value = StringUtils.trimToEmpty(description);
        if (value.isEmpty() || value.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("appointment status description must be 1-" + DESCRIPTION_MAX_LENGTH + " characters");
        }
        return updateEditable(id, value, AppointmentStatus::setDescription);
    }

    public boolean updateColour(int id, String colour) {
        String value = StringUtils.trimToEmpty(colour);
        if (!COLOUR.matcher(value).matches()) {
            throw new IllegalArgumentException("appointment status colour must match " + COLOUR.pattern());
        }
        return updateEditable(id, value, AppointmentStatus::setColor);
    }

    public boolean updateIcon(int id, String icon) {
        if (icon == null || !ICON_SET.contains(icon)) {
            throw new IllegalArgumentException("appointment status icon must be one of " + ICON_SET);
        }
        return updateEditable(id, icon, AppointmentStatus::setIcon);
    }

    /* Locked statuses (editable=0) keep their seeded look: neither an edit nor Reset writes them. */
    private static boolean isEditable(AppointmentStatus status) {
        return status.getEditable() == 1;
    }

    private <T> boolean updateEditable(int id, T value, BiConsumer<AppointmentStatus, T> setter) {
        AppointmentStatus status = appointStatusDao.find(id);
        if (status == null || !isEditable(status)) {
            return false;
        }
        setter.accept(status, value);
        appointStatusDao.merge(status);
        return true;
    }

    public int checkStatusUsuage(List<AppointmentStatus> allStatus) {
        return appointStatusDao.checkStatusUsuage(allStatus);
    }

    public void reset() {
        List<AppointmentStatus> changed = new ArrayList<>();
        for (AppointmentStatus status : appointStatusDao.findAll()) {
            StatusStyle style = DEFAULT_STYLES.get(status.getStatus());
            if (style != null && isEditable(status)) {
                status.setDescription(style.description());
                status.setColor(style.colour());
                status.setIcon(style.icon());
                changed.add(status);
            }
        }
        appointStatusDao.mergeAll(changed);
    }
}
