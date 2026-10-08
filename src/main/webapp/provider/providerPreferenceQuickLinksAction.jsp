<%--

    Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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

    This software was written for the
    Department of Family Medicine
    McMaster University
    Hamilton
    Ontario, Canada

--%>

<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@page import="ca.openosp.openo.web.admin.ProviderPreferencesUIBean" %>
<%@page import="ca.openosp.openo.utility.WebUtils" %>
<%@page import="ca.openosp.openo.utility.MiscUtils" %>
<%@page import="ca.openosp.openo.utility.SpringUtils" %>
<%@page import="ca.openosp.openo.managers.SecurityInfoManager" %>
<%@page import="org.apache.commons.lang3.StringUtils" %>
<%--
    Adds or removes one of the logged-in provider's quick links (the links shown on the appointment screen).
    Called by POST from providerpreference.jsp with action=add (name, url) or action=remove (name).
    Answers 204 when done, 400 for a bad request; the preference page reloads itself.
--%>
<%
    LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
    SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    if (!securityInfoManager.hasPrivilege(loggedInInfo, "_pref", SecurityInfoManager.UPDATE, null)) {
        throw new SecurityException("missing required sec object (_pref)");
    }

    // POST only, so CSRFGuard checks the token
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String providerNo = loggedInInfo.getLoggedInProviderNo();
    String action = request.getParameter("action");
    String name = StringUtils.trimToNull(request.getParameter("name"));
    String url = StringUtils.trimToNull(request.getParameter("url"));

    if ("add".equals(action) && name != null && url != null) {
        ProviderPreferencesUIBean.addQuickLink(providerNo, name, url);
    } else if ("remove".equals(action) && name != null) {
        ProviderPreferencesUIBean.removeQuickLink(providerNo, name);
    } else {
        MiscUtils.getLogger().error("Missing action case. action=" + action);
        WebUtils.dumpParameters(request);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
    }

    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
%>
