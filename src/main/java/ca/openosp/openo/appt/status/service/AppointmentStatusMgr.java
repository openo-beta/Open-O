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
import ca.openosp.openo.utility.LoggedInInfo;

/**
 * @author toby
 */
public interface AppointmentStatusMgr {

    /**
     * Width of the appointment_status.description column.
     *
     * @since 2026-09-15
     */
    int DESCRIPTION_MAX_LENGTH = 30;

    public List<AppointmentStatus> getAllStatus();

    public List<AppointmentStatus> getAllActiveStatus();

    /**
     * The statuses an appointment's edit screen offers: the active ones, and its own status if that
     * was disabled, so the screen shows what the appointment has and saving it changes nothing.
     *
     * @param statusCode String the appointment's status code, without its signed or verified mark
     * @return List<AppointmentStatus> the active statuses and the appointment's own, by id
     * @since 2026-10-19
     */
    public List<AppointmentStatus> getStatusChoices(String statusCode);

    public AppointmentStatus getStatus(int ID);

    /**
     * Enables or disables an editable status. Locked statuses (editable=0) stay as seeded, as they
     * do for every other change.
     *
     * @param loggedInInfo LoggedInInfo the user making the change, whom its audit row names
     * @param id int the appointment_status id
     * @param active int 1 to enable the status, 0 to disable it
     * @return boolean true when saved; false when there is no such status or it is locked (editable=0)
     * @throws IllegalArgumentException if active is neither 0 nor 1
     * @since 2026-09-23
     */
    public boolean changeStatus(LoggedInInfo loggedInInfo, int id, int active);

    /**
     * Renames an editable status.
     *
     * @param loggedInInfo LoggedInInfo the user making the change, whom its audit row names
     * @param id int the appointment_status id
     * @param description String the new description; surrounding whitespace is trimmed
     * @return boolean true when saved; false when there is no such status or it is locked (editable=0)
     * @throws IllegalArgumentException if the trimmed description is blank or longer than
     *     {@link #DESCRIPTION_MAX_LENGTH}
     * @since 2026-09-15
     */
    public boolean updateDescription(LoggedInInfo loggedInInfo, int id, String description);

    /**
     * Sets an editable status's background colour.
     *
     * @param loggedInInfo LoggedInInfo the user making the change, whom its audit row names
     * @param id int the appointment_status id
     * @param colour String the new colour as #rrggbb, or #rrggbbaa when see-through; surrounding
     *     whitespace is trimmed
     * @return boolean true when saved; false when there is no such status or it is locked (editable=0)
     * @throws IllegalArgumentException if the colour is neither #rrggbb nor #rrggbbaa
     * @since 2026-09-15
     */
    public boolean updateColour(LoggedInInfo loggedInInfo, int id, String colour);

    /**
     * Sets an editable status's icon.
     *
     * @param loggedInInfo LoggedInInfo the user making the change, whom its audit row names
     * @param id int the appointment_status id
     * @param icon String the new icon, one of {@link ca.openosp.openo.appt.IconSet#ICONS}: an image or a glyph
     * @return boolean true when saved; false when there is no such status or it is locked (editable=0)
     * @throws IllegalArgumentException if the icon is not in the Icon Set
     * @since 2026-09-15
     */
    public boolean updateIcon(LoggedInInfo loggedInInfo, int id, String icon);

    /**
     * Puts every editable status (editable=1) back to its Default Status Style: the description,
     * colour and icon it is seeded with. Statuses are matched by status code, not id, because ids
     * differ between installs. Locked statuses, codes without a default, and whether a status is
     * active are left alone, and no missing status is added. All changes are saved together.
     *
     * @param loggedInInfo LoggedInInfo the user making the change, whom its audit row names
     * @since 2026-09-21
     */
    public void reset(LoggedInInfo loggedInInfo);
}
