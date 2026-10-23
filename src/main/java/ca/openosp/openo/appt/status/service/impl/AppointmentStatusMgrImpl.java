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

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.apache.commons.beanutils.BeanComparator;
import ca.openosp.openo.appt.IconSet;
import ca.openosp.openo.appt.ItemStyleColour;
import ca.openosp.openo.commn.dao.AppointmentStatusDao;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.appt.status.service.AppointmentStatusMgr;

/**
 * @author toby
 */

public class AppointmentStatusMgrImpl implements AppointmentStatusMgr {

    private static AppointmentStatusDao appointStatusDao = SpringUtils.getBean(AppointmentStatusDao.class);

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

    private static List<AppointmentStatus> cachedStatuses = null;
    // Set by markCacheDirty on every status save, from whichever thread saves it.
    private static volatile boolean cacheIsDirty = false;

    /**
     * Every status, disabled ones too, in id order: the schedule still draws an appointment whose
     * status was disabled, and moves it on to the next active one. Reloaded after a status is saved.
     *
     * @return List<AppointmentStatus> every status, by id
     */
    public static synchronized List<AppointmentStatus> getCachedStatuses() {
        if (cachedStatuses == null || cacheIsDirty) {
            // Cleared before the read, so a save made during it brings another reload.
            cacheIsDirty = false;
            try {
                setCachedStatuses(appointStatusDao.findAll());
            } catch (RuntimeException e) {
                // Still stale, so the next lookup tries again.
                cacheIsDirty = true;
                throw e;
            }
        }
        return cachedStatuses;
    }

    /**
     * Marks the cache stale after a status is saved: at once, and again when the saving transaction
     * commits, since a lookup in between would reload the statuses as they were before the save.
     *
     * @since 2026-10-20
     */
    public static void markCacheDirty() {
        cacheIsDirty = true;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cacheIsDirty = true;
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized void setCachedStatuses(List<AppointmentStatus> cachedStatuses) {
        Collections.sort(cachedStatuses, new BeanComparator("id"));
        AppointmentStatusMgrImpl.cachedStatuses = cachedStatuses;
    }

    public List<AppointmentStatus> getAllStatus() {
        return appointStatusDao.findAll();
    }

    public List<AppointmentStatus> getAllActiveStatus() {
        return appointStatusDao.findActive();
    }

    @Override
    public List<AppointmentStatus> getStatusChoices(String statusCode) {
        return appointStatusDao.findAll().stream()
                .filter(status -> status.getActive() == 1 || status.getStatus().equals(statusCode))
                .sorted(Comparator.comparing(AppointmentStatus::getId))
                .toList();
    }

    public AppointmentStatus getStatus(int ID) {
        return appointStatusDao.find(ID);
    }

    @Override
    public boolean changeStatus(LoggedInInfo loggedInInfo, int id, int active) {
        if (active != 0 && active != 1) {
            throw new IllegalArgumentException("appointment status active must be 0 or 1");
        }
        return updateEditable(loggedInInfo, id, active, AppointmentStatus::setActive, "changeStatus", "active is now " + active);
    }

    @Override
    public boolean updateDescription(LoggedInInfo loggedInInfo, int id, String description) {
        String value = StringUtils.trimToEmpty(description);
        if (value.isEmpty() || value.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("appointment status description must be 1-" + DESCRIPTION_MAX_LENGTH + " characters");
        }
        return updateEditable(loggedInInfo, id, value, AppointmentStatus::setDescription, "updateDescription", "description is now [" + value + "]");
    }

    @Override
    public boolean updateColour(LoggedInInfo loggedInInfo, int id, String colour) {
        String value = StringUtils.trimToEmpty(colour);
        if (!ItemStyleColour.isColour(value)) {
            throw new IllegalArgumentException("appointment status colour must be #rrggbb or #rrggbbaa");
        }
        return updateEditable(loggedInInfo, id, value, AppointmentStatus::setColor, "updateColour", "colour is now [" + value + "]");
    }

    @Override
    public boolean updateIcon(LoggedInInfo loggedInInfo, int id, String icon) {
        if (!IconSet.contains(icon)) {
            throw new IllegalArgumentException("appointment status icon must be in the Icon Set");
        }
        return updateEditable(loggedInInfo, id, icon, AppointmentStatus::setIcon, "updateIcon", "icon is now [" + icon + "]");
    }

    /* Locked statuses (editable=0) keep their seeded look: neither an edit nor Reset writes them. */
    private static boolean isEditable(AppointmentStatus status) {
        return status.getEditable() == 1;
    }

    /* Saves one change to an editable status and audits it; change names it on the audit row. */
    private <T> boolean updateEditable(LoggedInInfo loggedInInfo, int id, T value, BiConsumer<AppointmentStatus, T> setter,
                                       String change, String result) {
        AppointmentStatus status = appointStatusDao.find(id);
        if (status == null || !isEditable(status)) {
            return false;
        }
        setter.accept(status, value);
        appointStatusDao.merge(status);
        LogAction.addLogSynchronous(loggedInInfo, "AppointmentStatusMgr." + change, "appointment_status id " + id + ": " + result);
        return true;
    }

    @Override
    public void reset(LoggedInInfo loggedInInfo) {
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
        LogAction.addLogSynchronous(loggedInInfo, "AppointmentStatusMgr.reset",
                "every editable status restored to its default description, colour and icon");
    }
}
