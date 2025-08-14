<%--


    Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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

    This software was written for
    Centre for Research on Inner City Health, St. Michael's Hospital,
    Toronto, Ontario, Canada

--%>
<%@page import="org.oscarehr.common.model.Demographic" %>
<%@page import="org.oscarehr.common.model.Provider" %>
<%@page import="org.oscarehr.util.LoggedInInfo" %>

<%
    LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
    Demographic demographic = null;

    String strProgramId = request.getParameter("programId");
    int programId = 0;
    try {
        programId = Integer.parseInt(strProgramId);
    } catch (NumberFormatException e) {
    }

    int clientId = 0;
    if (programId == 0) {
        //error has occured
    } else {
        demographic = org.oscarehr.PMmodule.web.CreateAnonymousClientAction.generatePEClient(loggedInInfo.getLoggedInProviderNo(), programId);
    }

    Provider provider = loggedInInfo.getLoggedInProvider();
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
    String curDate = sdf.format(new java.util.Date());
    java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("kk:mm");
    String curTime = sdf2.format(new java.util.Date());

    response.sendRedirect("../oscarEncounter/IncomingEncounter.do?providerNo=" + provider.getProviderNo() + "&appointmentNo=0&demographicNo=" + demographic.getDemographicNo() + "&curProviderNo=" + provider.getProviderNo() + "&reason=&encType=Telephone+Encounter+with+client&userName=" + provider.getFormattedName() + "&curDate=" + curDate + "&appointmentDate=" + curDate + "&startTime=" + curTime + "&status=T&source=cm");

%>