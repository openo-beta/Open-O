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

package ca.openosp.openo.appt.status.web;

import java.util.List;
import java.util.Objects;

import ca.openosp.openo.appt.status.service.AppointmentStatusMgr;
import ca.openosp.openo.appt.status.service.impl.AppointmentStatusMgrImpl;
import ca.openosp.openo.appt.web.AppointmentSettingsAction;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.managers.SecurityInfoManager;

/**
 * Appointment Status Manager: lists the appointment statuses and applies changes to them.
 *
 * <p>Routed on the {@code dispatch} parameter. Anything other than a change renders the list. A
 * change needs update on one of the Appointment Settings objects, the same ones read opens the page
 * with, and must be posted (see {@link AppointmentSettingsAction}):</p>
 * <ul>
 *   <li>{@code reset}: puts every editable status back to its seeded description, colour and icon</li>
 *   <li>{@code changestatus}: enables or disables {@code statusID} ({@code iActive} 1 or 0)</li>
 *   <li>{@code updateDescription}, {@code updateColour}, {@code updateIcon}: sets status {@code ID}
 *       to {@code value}; posted by the item style editor on the list page</li>
 * </ul>
 *
 * @since 2024-12-06
 */
public class AppointmentStatus2Action extends AppointmentSettingsAction {

    private AppointmentStatusMgr appointmentStatusMgr = new AppointmentStatusMgrImpl();

    /**
     * Renders the status list, or applies the change named by {@code dispatch}.
     *
     * @return String {@code success} to render the list, or {@code saved} to redirect back to it
     * @throws SecurityException if the user lacks the privilege, or a change was not posted
     */
    public String execute() {
        // The admin menu links here with no dispatch, and a switch on null throws.
        String dispatch = Objects.toString(request.getParameter("dispatch"), "view");
        return switch (dispatch) {
            case "reset" -> change(() -> {
                appointmentStatusMgr.reset();
                return true;
            });
            case "changestatus" -> change(() -> {
                appointmentStatusMgr.changeStatus(intParameter("statusID"), intParameter("iActive"));
                return true;
            });
            case "updateDescription" -> change(() -> appointmentStatusMgr.updateDescription(intParameter("ID"), request.getParameter("value")));
            case "updateColour" -> change(() -> appointmentStatusMgr.updateColour(intParameter("ID"), request.getParameter("value")));
            case "updateIcon" -> change(() -> appointmentStatusMgr.updateIcon(intParameter("ID"), request.getParameter("value")));
            default -> show();
        };
    }

    @Override
    protected boolean canChange() {
        return hasAnyPrivilege(SEC_OBJECTS, SecurityInfoManager.UPDATE);
    }

    @Override
    protected String view() {
        List<AppointmentStatus> allStatus = appointmentStatusMgr.getAllStatus();
        request.setAttribute("allStatus", allStatus);
        request.setAttribute("iconSet", AppointmentStatusMgr.ICON_SET);
        request.setAttribute("descriptionMaxLength", AppointmentStatusMgr.DESCRIPTION_MAX_LENGTH);
        int iUseStatus = appointmentStatusMgr.checkStatusUsuage(allStatus);
        if (iUseStatus > 0) {
            request.setAttribute("useStatus", appointmentStatusMgr.getStatus(iUseStatus + 1).getStatus());
        }
        return SUCCESS;
    }
}
