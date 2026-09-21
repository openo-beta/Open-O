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
    Appointment Status Manager.

    Lists every appointment status. An editable status gets pencil buttons that open the item
    style editor (js/appointment/itemStyleEditor.js) for its description, colour and icon, and an
    Enable or Disable button. A locked status (editable=0) is read-only. Reset, after a confirm,
    puts every editable status back to its seeded description, colour and icon. Every change posts
    back to AppointmentStatus2Action.

    Request attributes, set by AppointmentStatus2Action (appointment/apptStatusSetting.do):
      allStatus             List<AppointmentStatus> every status
      iconSet               List<String> the images a status may use, under /images
      descriptionMaxLength  Integer the description column's width
      useStatus             String code of a disabled status still used by appointments, if any
      saveFailed            Boolean true when the last change was rejected
      statusTabEnabled      Boolean whether to show the Status tab

    @since 2008-04-21
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="appt" %>
<%@ taglib prefix="csrf" uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" %>

<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_admin,_admin.userAdmin,_admin.schedule" rights="r" reverse="<%=true%>">
    <c:redirect url="/logout.jsp"/>
</security:oscarSec>

<fmt:setBundle basename="oscarResources"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="statusAction" value="/appointment/apptStatusSetting.do"/>
<c:set var="activeTab" value="status"/>
<c:set var="itemStyleEditorAction" value="${statusAction}"/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><fmt:message key="admin.appt.status.mgr.title"/></title>
    <link href="${ctx}/library/bootstrap/5.0.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="${ctx}/css/glyphicons-standalone.css" rel="stylesheet">
    <link href="${ctx}/css/itemStyleEditor.css" rel="stylesheet">
    <script src="${ctx}/js/appointment/itemStyleEditor.js"></script>
    <style>
        .status-swatch {
            display: inline-block;
            width: 1rem;
            height: 1rem;
            vertical-align: middle;
            border: 1px solid var(--bs-gray-500, #adb5bd);
        }
    </style>
</head>
<body class="p-3">
<%@ include file="appointmentSettingsNav.jspf" %>

<div class="d-flex align-items-center mb-3">
    <h1 class="h5 mb-0 me-auto"><fmt:message key="admin.appt.status.mgr.title"/></h1>
    <fmt:message key="admin.appt.status.mgr.msg.confirmReset" var="confirmReset"/>
    <form method="post" action="<c:url value='${statusAction}'/>" data-confirm="${fn:escapeXml(confirmReset)}">
        <input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue/>">
        <input type="hidden" name="dispatch" value="reset">
        <button type="submit" class="btn btn-sm btn-outline-secondary"><fmt:message key="global.reset"/></button>
    </form>
</div>

<c:if test="${saveFailed}">
    <div class="alert alert-danger" role="alert"><fmt:message key="admin.appt.settings.msg.saveFailed"/></div>
</c:if>
<c:if test="${not empty useStatus}">
    <div class="alert alert-warning" role="alert">
        <fmt:message key="admin.appt.status.mgr.msg.usedBefore">
            <fmt:param value="${useStatus}"/>
        </fmt:message>
    </div>
</c:if>

<fmt:message key="admin.appt.status.mgr.label.lockedTitle" var="lockedTitle"/>

<div class="table-responsive">
    <table id="statusTable" class="table table-sm table-striped align-middle"
           data-icon-base="${fn:escapeXml(ctx)}/images/"
           data-icon-names="<c:forEach items='${iconSet}' var='icon' varStatus='loop'>${fn:escapeXml(icon)}${loop.last ? '' : ' '}</c:forEach>"
           data-description-max-length="${fn:escapeXml(descriptionMaxLength)}">
        <thead>
        <tr>
            <th scope="col"><fmt:message key="admin.appt.status.mgr.label.status"/></th>
            <th scope="col"><fmt:message key="admin.appt.status.mgr.label.desc"/></th>
            <th scope="col"><fmt:message key="admin.appt.status.mgr.label.color"/></th>
            <th scope="col"><fmt:message key="admin.appt.status.mgr.label.icon"/></th>
            <th scope="col"><fmt:message key="admin.appt.status.mgr.label.enable"/></th>
            <th scope="col"><fmt:message key="admin.appt.status.mgr.label.active"/></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${allStatus}" var="status">
            <c:set var="editable" value="${status.editable == 1}"/>
            <tr>
                <td class="text-nowrap"><c:out value="${status.status}"/></td>
                <td class="text-nowrap">
                    <c:out value="${status.description}"/>
                    <c:if test="${editable}">
                        <appt:itemStyleEditButton kind="description" itemId="${status.id}" current="${status.description}"/>
                    </c:if>
                </td>
                <td class="text-nowrap">
                    <span class="status-swatch" data-colour="${fn:escapeXml(status.color)}"></span>
                    <c:out value="${status.color}"/>
                    <c:if test="${editable}">
                        <appt:itemStyleEditButton kind="colour" itemId="${status.id}" current="${status.color}"/>
                    </c:if>
                </td>
                <td class="text-nowrap">
                    <img src="${fn:escapeXml(ctx)}/images/${fn:escapeXml(status.icon)}" alt="${fn:escapeXml(status.icon)}">
                    <c:if test="${editable}">
                        <appt:itemStyleEditButton kind="icon" itemId="${status.id}" current="${status.icon}"/>
                    </c:if>
                </td>
                <td><fmt:message key="${status.active > 0 ? 'global.yes' : 'global.no'}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${editable}">
                            <form method="post" action="<c:url value='${statusAction}'/>">
                                <input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue/>">
                                <input type="hidden" name="dispatch" value="changestatus">
                                <input type="hidden" name="statusID" value="${fn:escapeXml(status.id)}">
                                <input type="hidden" name="iActive" value="${status.active > 0 ? 0 : 1}">
                                <button type="submit" class="btn btn-sm btn-outline-primary">
                                    <fmt:message key="${status.active > 0 ? 'admin.appt.status.mgr.btn.disable' : 'admin.appt.status.mgr.btn.enable'}"/>
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-secondary" title="${fn:escapeXml(lockedTitle)}">
                                <fmt:message key="admin.appt.status.mgr.label.locked"/>
                            </span>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="itemStyleEditorDialog.jspf" %>
<script>
    (function () {
        const table = document.getElementById('statusTable');

        // Colours are only ever applied as #rrggbb, so a stored value cannot inject other CSS.
        table.querySelectorAll('.status-swatch').forEach(function (swatch) {
            if (/^#[0-9a-fA-F]{6}$/.test(swatch.dataset.colour)) {
                swatch.style.backgroundColor = swatch.dataset.colour;
            }
        });

        // Reset overwrites every editable status's description, colour and icon, with no undo.
        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (!window.confirm(form.dataset.confirm)) {
                    event.preventDefault();
                }
            });
        });

        ItemStyleEditor.init({
            description: {maxLength: Number(table.dataset.descriptionMaxLength)},
            colour: {clearable: false},
            icon: {
                clearable: false,
                iconSet: {kind: 'image', base: table.dataset.iconBase, names: table.dataset.iconNames.split(' ')}
            }
        });
    })();
</script>
</body>
</html>
