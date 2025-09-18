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

<%@page import="ca.openosp.openo.commn.model.Provider" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_con" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect("../../securityError.jsp?type=_con");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<%@page
        import="ca.openosp.openo.encounter.pageUtil.*,ca.openosp.openo.encounter.data.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="ca.openosp.openo.demographic.data.DemographicData" %>
<%@ page import="ca.openosp.openo.encounter.oscarConsultationRequest.pageUtil.EctConsultationFormRequestUtil" %>
<%@ page import="ca.openosp.openo.encounter.oscarConsultationRequest.pageUtil.EctViewConsultationRequestsUtil" %>
<%@ page import="ca.openosp.openo.providers.data.ProviderData" %>
<%@ page import="ca.openosp.openo.commn.model.Demographic" %>

<%
    String demo = request.getParameter("de");
    String proNo = (String) session.getAttribute("user");
    DemographicData demoData = null;
    Demographic demographic = null;

    ProviderData pdata = new ProviderData(proNo);
    String team = pdata.getTeam();

    if (demo != null) {
        demoData = new DemographicData();
        demographic = demoData.getDemographic(LoggedInInfo.getLoggedInInfoFromSession(request), demo);
    } else
        response.sendRedirect("../error.jsp");

    EctConsultationFormRequestUtil consultUtil;
    consultUtil = new EctConsultationFormRequestUtil();
    consultUtil.estPatient(LoggedInInfo.getLoggedInInfoFromSession(request), demo);

    EctViewConsultationRequestsUtil theRequests;
    theRequests = new EctViewConsultationRequestsUtil();
    theRequests.estConsultationVecByDemographic(LoggedInInfo.getLoggedInInfoFromSession(request), demo);
%>

<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.title"/>
        </title>
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">

        <!--META HTTP-EQUIV="Refresh" CONTENT="20;"-->

        <link rel="stylesheet" type="text/css" media="all" href="<%= request.getContextPath() %>/share/css/extractedFromPages.css"/>


    </head>
    <script language="javascript">
        function BackToOscar() {
            window.close();
        }

        function popupOscarRx(vheight, vwidth, varpage) { //open a new popup window
            var page = varpage;
            windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0";
            var popup = window.open(varpage, "<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgConsReq"/>", windowprops);
            //if (popup != null) {
            //  if (popup.opener == null) {
            //    popup.opener = self;
            //  }
            //}
        }

        function popupOscarConS(vheight, vwidth, varpage) { //open a new popup window
            var page = varpage;
            windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0";
            var popup = window.open(varpage, "<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ConsultChoice.oscarConS"/>", windowprops);
            window.close();
        }
    </script>

    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/oscarEncounter/encounterStyles.css">
    <body class="BodyStyle" vlink="#0000FF" onload="window.focus()">
    <!--  -->
    <table class="MainTable" id="scrollNumber1" name="encounterTable">
        <tr class="MainTableTopRow">
            <td class="MainTableTopRowLeftColumn">Consultation</td>
            <td class="MainTableTopRowRightColumn">
                <table class="TopStatusBar">
                    <tr>
                        <td class="Header" NOWRAP><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgConsReqFor"/>
                            <%=demographic.getLastName() %>, <%=demographic.getFirstName()%> <%=demographic.getSex()%>
                            <%=demographic.getAge()%>
                        </td>
                        <td></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr style="vertical-align: top">
            <td class="MainTableLeftColumn">
                <table>
                    <tr>
                        <td NOWRAP><a
                                href="javascript:popupOscarRx(700,960,'oscarEncounter/oscarConsultationRequest/ConsultationFormRequest.jsp?de=<%=demo%>&teamVar=<%=team%>')">
                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ConsultChoice.btnNewCon"/></a>
                        </td>
                    </tr>
                </table>
            </td>
            <td class="MainTableRightColumn">
                <table width="100%">
                    <tr>
                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgClickLink"/>
                        </td>
                    </tr>
                    <tr>
                        <td>

                            <table border="0" width="80%" cellspacing="1">
                                <tr>
                                    <th align="left" class="VCRheads" width="75"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgStatus"/>
                                    </th>
                                    <th align="left" class="VCRheads"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgPat"/>
                                    </th>
                                    <th align="left" class="VCRheads"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgMRP"/>
                                    </th>
                                    <th align="left" class="VCRheads"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgProvider"/>
                                    </th>
                                    <th align="left" class="VCRheads"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgService"/>
                                    </th>
                                    <th align="left" class="VCRheads"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgRefDate"/>
                                    </th>
                                </tr>
                                <%
                                    for (int i = 0; i < theRequests.ids.size(); i++) {
                                        String id = (String) theRequests.ids.elementAt(i);
                                        String status = (String) theRequests.status.elementAt(i);
                                        String patient = (String) theRequests.patient.elementAt(i);
                                        String provide = (String) theRequests.provider.elementAt(i);
                                        String service = (String) theRequests.service.elementAt(i);
                                        String date = (String) theRequests.date.elementAt(i);
                                        Provider cProv = (Provider) theRequests.consultProvider.elementAt(i);
                                %>
                                <tr>
                                    <td class="stat<%=status%>" width="75">
                                        <% if ("1".equals(status)) { %>
                                            <fmt:setBundle basename="oscarResources"/>
                                            <fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgNothingDone"/>
                                        <% } else if ("2".equals(status)) { %>
                                            <fmt:setBundle basename="oscarResources"/>
                                            <fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgSpecialistCall"/>
                                        <% } else if ("3".equals(status)) { %>
                                            <fmt:setBundle basename="oscarResources"/>
                                            <fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgPatCall"/>
                                        <% } else if ("4".equals(status)) { %>
                                            <fmt:setBundle basename="oscarResources"/>
                                            <fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgAppMade"/>
                                        <% } else if ("5".equals(status)) { %>
                                            <fmt:setBundle basename="oscarResources"/>
                                            <fmt:message key="oscarEncounter.oscarConsultationRequest.DisplayDemographicConsultationRequests.msgBookCon"/>
                                        <% } %>
                                    <td class="stat<%=status%>"><a
                                            href="javascript:popupOscarRx(700,960,'<%= request.getContextPath() %>/oscarEncounter/ViewRequest.do?de=<%=demo%>&requestId=<%=id%>')">
                                        <%=patient%>
                                    </a></td>
                                    <td class="stat<%=status%>"><%=provide%>
                                    </td>
						<td class="stat<%=status%>"><a
							href="javascript:popupOscarRx(700,960,'<%=request.getContextPath()%>/oscarEncounter/ViewRequest.do?de=<%=demo%>&requestId=<%=id%>')">
						<%=patient%> </a></td>
						<td class="stat<%=status%>"><%=provide%></td>
						<td class="stat<%=status%>"><%= (cProv != null) ? cProv.getFormattedName() : "" %></td>
                                    <td class="stat<%=status%>">
                                        <a href="javascript:popupOscarRx(700,960,'<%= request.getContextPath() %>/oscarEncounter/ViewRequest.do?de=<%=demo%>&requestId=<%=id%>')">
                                            <%=StringUtils.trimToEmpty(service)%>
                                        </a>
                                    </td>
                                    <td class="stat<%=status%>"><%=date%>
                                    </td>
                                </tr>
                                <%}%>
                            </table>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="MainTableBottomRowLeftColumn"></td>
            <td class="MainTableBottomRowRightColumn"></td>
        </tr>
    </table>
    </body>
</html>
