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

<%
    if (session.getValue("user") == null) response.sendRedirect("../../logout.jsp");
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="/WEB-INF/oscar-tag.tld" prefix="oscar" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%
    String demo = ((Integer) request.getAttribute("demographicNo")).toString();
%>
<!DOCTYPE html>
<html>

    <head>
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.Index.oldMeasurements"/>
        </title>
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">

        <script src="<%= request.getContextPath() %>/js/global.js"></script>

        <link href="<%=request.getContextPath() %>/css/bootstrap.css" rel="stylesheet" type="text/css">
        <link href="<%=request.getContextPath() %>/css/DT_bootstrap.css" rel="stylesheet" type="text/css">
        <script src="<%=request.getContextPath() %>/library/jquery/jquery-3.6.4.min.js"></script>
        <script src="<%=request.getContextPath() %>/library/DataTables/datatables.min.js">//1.13.4</script>

        <script>
            // NOTE
            // if 404 ie no translation available at URL eg https://localhost:8443/oscar/library/DataTables/i18n/en-GB.json
            // the data table display will resort to a default of american english
            jQuery(document).ready(function () {
                jQuery('#tblDiscs').DataTable({
                    "order": [],
                    "bPaginate": false,
                    "searching": false,
                    "language": {
                        "url": "<%=request.getContextPath() %>/library/DataTables/i18n/<fmt:setBundle basename="oscarResources"/><fmt:message key="global.i18nLanguagecode"/>.json"
                    }
                });
            });
        </script>

        <style type="text/css" media="print">
            .DoNotPrint {
                display: none;
            }
        </style>
    </head>
    <body
            onload="window.focus();">
    <% 
    java.util.List<String> actionErrors = (java.util.List<String>) request.getAttribute("actionErrors");
    if (actionErrors != null && !actionErrors.isEmpty()) {
%>
    <div class="action-errors">
        <ul>
            <% for (String error : actionErrors) { %>
                <li><%= error %></li>
            <% } %>
        </ul>
    </div>
<% } %>
    <form action="${pageContext.request.contextPath}/oscarEncounter/oscarMeasurements/DeleteData.do" method="post">

        <table style="border-width: 2px; width: 100%; border-spacing: 0px; ">
            <c:if test="${not empty messages}">
                <tr>
                    <td>
                        <c:forEach var="msg" items="${messages}">
                            <div class="alert">
                                ${msg}
                            </div>
                        </c:forEach>
                    </td>
                </tr>
            </c:if>
            <tr>
                <td>

                    <table style="border-width: 1px; width: 100%; border-spacing: 0px; border-color:black; border-style: solid; ">
                        <tr>
                            <td style="width: 100%; text-align: center;" class="Cell">
                                <div class="Field2"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarMDS.segmentDisplay.formDetailResults"/></div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <table style="width: 100%; border-spacing: 0px;">
                                    <tr>
                                        <td>

                                            <table style="width: 66%; border-spacing: 0px;">
                                                <tr>
                                                    <td>
                                                        <div class="FieldData"><strong><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarMDS.segmentDisplay.formPatientName"/>: </strong>
                                                            <oscar:nameage demographicNo="<%=demo%>"/></div>

                                                    </td>
                                                    <td>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                    </td>
                                                    <td>
                                                    </td>
                                                </tr>
                                            </table>

                                        </td>
                                        <td style="width: 33%;"></td>
                                    </tr>
                                </table>

                            </td>
                        </tr>
                    </table>

                </td>
            </tr>
            <tr>
                <td>

                    <table id="tblDiscs" class="table table-condensed table-striped">
                        <thead>
                        <tr>
                            <th class="Header"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.displayHistory.headingType"/>
                            </th>
                            <th class="Header"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.displayHistory.headingProvider"/>
                            </th>
                            <th class="Header"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.displayHistory.headingMeasuringInstruction"/>
                            </th>
                            <th class="Header"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.displayHistory.headingData"/>
                            </th>
                            <th class="Header"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.displayHistory.headingComments"/>
                            </th>
                            <th class="Header"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.displayHistory.headingObservationDate"/>
                            </th>
                            <th class="Header"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.displayHistory.headingDateEntered"/>
                            </th>
                            <security:oscarSec roleName="<%=roleName$%>" objectName="_flowsheet" rights="w">
                                <th class="Header DoNotPrint"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.displayHistory.headingDelete"/>
                                </th>
                            </security:oscarSec>

                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="data" items="${measurementsData.measurementsDataVector}" varStatus="ctr">
                            <c:choose>
                                <c:when test="${not empty data.remoteFacility}">
                                    <tr class="data" style="background-color:#ffcccc">
                                </c:when>
                                <c:otherwise>
                                    <tr class="data">
                                </c:otherwise>
                            </c:choose>

                            <td>
                                <a title="${data.typeDescription}">${data.type}</a>
                            </td>

                            <td>
                                    ${data.providerFirstName} ${data.providerLastName}
                                <c:if test="${not empty data.remoteFacility}">
                                    <br/><span style="color:#990000"> @: ${data.remoteFacility}</span>
                                </c:if>
                            </td>

                            <td>
                                <c:choose>
                                    <c:when test="${data.measuringInstrc == 'NULL'}">&nbsp;</c:when>
                                    <c:otherwise>${data.measuringInstrc}</c:otherwise>
                                </c:choose>
                            </td>

                            <td title="data">${data.dataField}</td>
                            <td title="comments">${data.comments}</td>
                            <td title="observed date"><fmt:formatDate value="${data.dateObservedAsDate}" pattern="yyyy-MM-dd"/></td>
                            <td title="entered date"><fmt:formatDate value="${data.dateEnteredAsDate}" pattern="yyyy-MM-dd"/></td>

                            <security:oscarSec roleName="${roleName$}" objectName="_flowsheet" rights="w">
                                <td class="DoNotPrint">
                                    <c:if test="${empty data.remoteFacility}">
                                        <input type="checkbox" name="deleteCheckbox" value="${data.id}">
                                    </c:if>
                                </td>
                            </security:oscarSec>
                            </tr>
                        </c:forEach>

                        </tbody>
                    </table>
            <tr>
                <td>
                    <table>
                        <tr>
                            <td><input type="button" name="Button" class="btn DoNotPrint"
                                       value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.oldmesurementindex"/>"
                                       onClick="popupPage(300,800,'oscarEncounter/oscarMeasurements/SetupHistoryIndex.do')"></td>
                            <td><input type="button" name="Button" class="btn DoNotPrint"
                                       value="<fmt:setBundle basename="oscarResources"/><fmt:message key="global.btnPrint"/>"
                                       onClick="window.print()"></td>
                            <td><input type="button" name="Button" class="btn DoNotPrint"
                                       value="<fmt:setBundle basename="oscarResources"/><fmt:message key="global.btnClose"/>"
                                       onClick="window.close()"></td>
                            <security:oscarSec roleName="<%=roleName$%>" objectName="_flowsheet" rights="w">
                                <td><input type="button" name="Button" class="btn DoNotPrint"
                                           value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.displayHistory.headingDelete"/>"
                                           onclick="submit();"></td>
                            </security:oscarSec>
                            <c:if test="${not empty data.canPlot}">
                                <td><input type="button" name="Button" class="btn DoNotPrint"
                                           value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarMeasurements.displayHistory.plot"/>"
                                           onClick="popupPage(600,1000,'../../oscarEncounter/GraphMeasurements.do?demographic_no=<%=demo%>&type=
                                               ${type}')">
                                </td>
                            </c:if>
                        </tr>
                    </table>

                </td>
            </tr>
            </tbody>
        </table>
        <c:if test="${not empty type}">
            <input type="hidden" name="type" value="${type}">
        </c:if>
    </form>
    </body>
</html>
