<%--

    Copyright (c) 2006-. OSCARservice, OpenSoft System. All Rights Reserved.
    This software is published under the GPL GNU General Public License.
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

--%>
<%--
    Appointment Settings entry point, linked from the admin menus. Opens the Status tab when
    ENABLE_EDIT_APPT_STATUS is on, otherwise the Location tab.

    @since 2008-04-21
--%>
<%@ page import="ca.openosp.openo.appt.web.AppointmentSettingsAction" %>
<%
    String tab = AppointmentSettingsAction.isStatusTabEnabled() ? "/appointment/apptStatusSetting.do" : "/appointment/apptLocationSetting.do";
    response.sendRedirect(request.getContextPath() + tab);
%>
