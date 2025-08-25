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

package oscar.appt.status.service;

import java.util.List;

import org.oscarehr.common.model.AppointmentStatus;

/**
 * @author toby
 */
public interface AppointmentStatusMgr {
    List<AppointmentStatus> getAllStatus();

    List<AppointmentStatus> getAllActiveStatus();

    AppointmentStatus getStatus(int ID);

    void changeStatus(int ID, int iActive);

    void modifyStatus(int ID, String strDesc, String strColor);

    int checkStatusUsuage(List<AppointmentStatus> allStatus);

    void reset();
}
