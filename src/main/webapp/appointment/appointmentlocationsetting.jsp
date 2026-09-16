<%--

    Copyright (c) 2015-2019. The Pharmacists Clinic, Faculty of Pharmaceutical Sciences, University of British Columbia. All Rights Reserved.
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
    The Pharmacists Clinic
    Faculty of Pharmaceutical Sciences
    University of British Columbia
    Vancouver, British Columbia, Canada

--%>
<%--
    Appointment Settings, Location tab.

    Lists the Location List's active items, the places an appointment can be booked, with the chip
    each shows on the schedule. A user who may change them gets pencil buttons that open the item
    style editor (js/appointment/itemStyleEditor.js) for the colour and icon, and a link to the
    Look-Up List Manager, where items are added, renamed and deactivated. Changes post back to
    AppointmentLocation2Action.

    Request attributes, set by AppointmentLocation2Action (appointment/apptLocationSetting.do):
      locationItems     List<LookupListItem> the active items, in display order
      locationListName  String the Location List's name, for the Look-Up List Manager link
      canChange         Boolean whether the user may change item styles
      saveFailed        Boolean true when the last change was rejected
      statusTabEnabled  Boolean whether to show the Status tab

    @since 2026-09-15
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="appt" %>

<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_admin,_admin.userAdmin,_admin.schedule" rights="r" reverse="<%=true%>">
    <c:redirect url="/logout.jsp"/>
</security:oscarSec>

<fmt:setBundle basename="oscarResources"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeTab" value="location"/>
<c:set var="itemStyleEditorAction" value="/appointment/apptLocationSetting.do"/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><fmt:message key="admin.appt.location.title"/></title>
    <link href="${ctx}/library/bootstrap/5.0.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="${ctx}/css/glyphicons-standalone.css" rel="stylesheet">
    <link href="${ctx}/css/itemStyleEditor.css" rel="stylesheet">
    <script src="${ctx}/js/appointment/itemStyleEditor.js"></script>
</head>
<body class="p-3">
<%@ include file="appointmentSettingsNav.jspf" %>

<div class="d-flex align-items-center mb-3">
    <h1 class="h5 mb-0 me-auto"><fmt:message key="admin.appt.location.title"/></h1>
    <c:if test="${canChange and not empty locationListName}">
        <c:url var="manageItemsUrl" value="/lookupListManagerAction.do">
            <c:param name="method" value="manageSingle"/>
            <c:param name="listName" value="${locationListName}"/>
        </c:url>
        <a class="btn btn-sm btn-outline-secondary" href="${fn:escapeXml(manageItemsUrl)}">
            <fmt:message key="admin.appt.location.btn.manageItems"/>
        </a>
    </c:if>
</div>

<c:if test="${saveFailed}">
    <div class="alert alert-danger" role="alert"><fmt:message key="admin.appt.settings.msg.saveFailed"/></div>
</c:if>

<c:choose>
    <c:when test="${empty locationItems}">
        <p class="text-muted"><fmt:message key="admin.appt.location.msg.empty"/></p>
    </c:when>
    <c:otherwise>
        <div class="table-responsive">
            <table class="table table-sm table-striped align-middle">
                <thead>
                <tr>
                    <th scope="col"><fmt:message key="admin.appt.location.label.name"/></th>
                    <th scope="col"><fmt:message key="admin.appt.location.label.colour"/></th>
                    <th scope="col"><fmt:message key="admin.appt.location.label.icon"/></th>
                    <th scope="col"><fmt:message key="admin.appt.location.label.chip"/></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${locationItems}" var="item">
                    <tr>
                        <td><c:out value="${item.label}"/></td>
                        <td class="text-nowrap">
                            <c:out value="${item.colour}"/>
                            <c:if test="${canChange}">
                                <appt:itemStyleEditButton kind="colour" itemId="${item.id}" current="${item.colour}"/>
                            </c:if>
                        </td>
                        <td class="text-nowrap">
                            <c:out value="${item.icon}"/>
                            <c:if test="${canChange}">
                                <appt:itemStyleEditButton kind="icon" itemId="${item.id}" current="${item.icon}"/>
                            </c:if>
                        </td>
                        <td><appt:locationChip item="${item}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="itemStyleEditorDialog.jspf" %>
<script>
    ItemStyleEditor.init({
        colour: {clearable: true},
        icon: {clearable: true, iconSet: {kind: 'glyphicon', names: ItemStyleEditor.GLYPHICONS}}
    });
</script>
</body>
</html>
