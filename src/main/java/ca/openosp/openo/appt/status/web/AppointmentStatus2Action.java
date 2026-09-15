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
import java.util.function.BooleanSupplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.appt.status.service.AppointmentStatusMgr;
import ca.openosp.openo.appt.status.service.impl.AppointmentStatusMgrImpl;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Appointment Status Manager: lists the appointment statuses and applies changes to them.
 *
 * <p>Routed on the {@code dispatch} parameter. Anything other than a change renders the list and
 * needs read on one of {@link #SEC_OBJECTS}. A change needs update on one of them and must be
 * posted, since the CSRF guard only checks posts:</p>
 * <ul>
 *   <li>{@code reset}: restores the seeded descriptions and colours</li>
 *   <li>{@code changestatus}: enables or disables {@code statusID} ({@code iActive} 1 or 0)</li>
 *   <li>{@code updateDescription}, {@code updateColour}, {@code updateIcon}: sets status {@code ID}
 *       to {@code value}; posted by the item style editor on the list page</li>
 * </ul>
 * <p>A saved change redirects back to the list. A rejected one renders the list with status 400
 * and {@code saveFailed} set.</p>
 *
 * @since 2024-12-06
 */
public class AppointmentStatus2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static final Logger logger = MiscUtils.getLogger();

    /** Holding read (to view) or update (to change) on any one of these opens the page; the JSP gate matches. */
    private static final List<String> SEC_OBJECTS = List.of("_admin", "_admin.userAdmin", "_admin.schedule");

    private static final String SAVED = "saved";

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
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
            default -> {
                requirePrivilege(SecurityInfoManager.READ);
                yield view();
            }
        };
    }

    /*
     * Runs one change for a user with update, and only when posted, since the CSRF guard only checks
     * posts. The change returns false, or throws IllegalArgumentException, when it is refused.
     */
    private String change(BooleanSupplier change) {
        requirePrivilege(SecurityInfoManager.UPDATE);
        if (!"POST".equals(request.getMethod())) {
            throw new SecurityException("appointment status changes must be posted");
        }

        boolean saved;
        try {
            saved = change.getAsBoolean();
        } catch (IllegalArgumentException e) {
            // Also catches NumberFormatException from a malformed id; the page never posts one.
            logger.warn("Rejected appointment status change: {}", e.getMessage());
            saved = false;
        }
        if (saved) {
            return SAVED;
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("saveFailed", true);
        return view();
    }

    private int intParameter(String name) {
        return Integer.parseInt(request.getParameter(name));
    }

    private void requirePrivilege(String privilege) {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (SEC_OBJECTS.stream().noneMatch(object -> securityInfoManager.hasPrivilege(loggedInInfo, object, privilege, null))) {
            throw new SecurityException("missing required sec object (" + String.join(" or ", SEC_OBJECTS) + ")");
        }
    }

    private String view() {
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
