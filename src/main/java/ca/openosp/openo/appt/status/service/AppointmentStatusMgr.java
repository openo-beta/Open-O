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

package ca.openosp.openo.appt.status.service;

import java.util.List;

import ca.openosp.openo.commn.model.AppointmentStatus;

/**
 * @author toby
 */
public interface AppointmentStatusMgr {

    /**
     * The images, under /images, that a status may use as its icon. The day view shows a signed or
     * verified appointment with the S- or V-prefixed copy, so only images that have both are listed.
     *
     * @since 2026-09-15
     */
    List<String> ICON_SET = List.of(
            "starbill.gif", "todo.gif", "here.gif", "picked.gif", "empty.gif", "noshow.gif", "cancel.gif", "billed.gif",
            "thumb.png",
            "1.gif", "2.gif", "3.gif", "4.gif", "5.gif", "6.gif", "7.gif", "8.gif",
            "9.gif", "10.gif", "11.gif", "12.gif", "13.gif", "14.gif", "15.gif", "16.gif");

    /**
     * Width of the appointment_status.description column.
     *
     * @since 2026-09-15
     */
    int DESCRIPTION_MAX_LENGTH = 30;

    public List<AppointmentStatus> getAllStatus();

    public List<AppointmentStatus> getAllActiveStatus();

    public AppointmentStatus getStatus(int ID);

    public void changeStatus(int ID, int iActive);

    /**
     * Renames an editable status.
     *
     * @param id int the appointment_status id
     * @param description String the new description; surrounding whitespace is trimmed
     * @return boolean true when saved; false when there is no such status or it is locked (editable=0)
     * @throws IllegalArgumentException if the trimmed description is blank or longer than
     *     {@link #DESCRIPTION_MAX_LENGTH}
     * @since 2026-09-15
     */
    public boolean updateDescription(int id, String description);

    /**
     * Sets an editable status's background colour.
     *
     * @param id int the appointment_status id
     * @param colour String the new colour as #rrggbb; surrounding whitespace is trimmed
     * @return boolean true when saved; false when there is no such status or it is locked (editable=0)
     * @throws IllegalArgumentException if the colour is not #rrggbb
     * @since 2026-09-15
     */
    public boolean updateColour(int id, String colour);

    /**
     * Sets an editable status's icon.
     *
     * @param id int the appointment_status id
     * @param icon String the new icon, one of {@link #ICON_SET}
     * @return boolean true when saved; false when there is no such status or it is locked (editable=0)
     * @throws IllegalArgumentException if the icon is not in {@link #ICON_SET}
     * @since 2026-09-15
     */
    public boolean updateIcon(int id, String icon);

    public int checkStatusUsuage(List<AppointmentStatus> allStatus);

    /**
     * Puts every editable status (editable=1) back to its Default Status Style: the description,
     * colour and icon it is seeded with. Statuses are matched by status code, not id, because ids
     * differ between installs. Locked statuses, codes without a default, and whether a status is
     * active are left alone, and no missing status is added. All changes are saved together.
     *
     * @since 2026-09-21
     */
    public void reset();
}
