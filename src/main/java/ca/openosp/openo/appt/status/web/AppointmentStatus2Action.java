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
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import ca.openosp.openo.appt.status.service.AppointmentStatusMgr;
import ca.openosp.openo.appt.status.service.impl.AppointmentStatusMgrImpl;
import ca.openosp.openo.appt.web.AppointmentSettingsAction;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.log.LogAction;
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
 * <p>Locked statuses (editable=0) can't be changed: edits and Enable/Disable refuse them, and Reset
 * skips them. Each saved change writes an audit log row.</p>
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
            case "reset" -> changeAudited(dispatch, () -> {
                appointmentStatusMgr.reset();
                return true;
            }, () -> "every editable status restored to its default description, colour and icon");
            case "changestatus" -> changeAudited(dispatch,
                    () -> appointmentStatusMgr.changeStatus(intParameter("statusID"), intParameter("iActive")),
                    () -> "appointment_status id " + intParameter("statusID") + ": active set to " + intParameter("iActive"));
            case "updateDescription" -> changeStyle(dispatch, "description", appointmentStatusMgr::updateDescription);
            case "updateColour" -> changeStyle(dispatch, "colour", appointmentStatusMgr::updateColour);
            case "updateIcon" -> changeStyle(dispatch, "icon", appointmentStatusMgr::updateIcon);
            default -> show();
        };
    }

    /* Sets status ID's description, colour or icon to the posted value. */
    private String changeStyle(String dispatch, String field, BiPredicate<Integer, String> update) {
        String value = request.getParameter("value");
        return changeAudited(dispatch, () -> update.test(intParameter("ID"), value),
                () -> "appointment_status id " + intParameter("ID") + ": " + field + " set to [" + StringUtils.trimToEmpty(value) + "]");
    }

    /*
     * Applies a change and, once it is saved, writes its audit row, as the Location tab's manager
     * does for its changes. A refused change writes none.
     */
    private String changeAudited(String dispatch, BooleanSupplier change, Supplier<String> data) {
        return change(() -> {
            boolean saved = change.getAsBoolean();
            if (saved) {
                LogAction.addLogSynchronous(getLoggedInInfo(), "AppointmentStatus2Action." + dispatch, data.get());
            }
            return saved;
        });
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
            request.setAttribute("useStatus", allStatus.get(iUseStatus).getStatus());
        }
        return SUCCESS;
    }
}
