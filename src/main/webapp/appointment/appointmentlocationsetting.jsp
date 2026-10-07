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

    Lists the places an appointment can be booked in one table, as the Status tab lists statuses:
    the active locations in the order the booking screens offer them, then the inactive ones by
    name, each with the chip it shows on the schedule. The Default Location is marked, and a switch
    above the table shows whether the Location Requirement is on, for every user. A user who may
    change locations gets the switch enabled, saving on click; pencil buttons that open the item
    style editor (js/appointment/itemStyleEditor.js) for the name, colour and icon; buttons to move
    an active location along the order; a button to make an active location the default or to
    remove the default; an Enable or Disable button on each row; and a link to the Look-Up List
    Manager, where locations are added. Changes post back to AppointmentLocation2Action.

    Request attributes, set by AppointmentLocation2Action (appointment/apptLocationSetting.do):
      locations              List<LookupListItem> the active locations in order, then the inactive by name
      activeLocationCount    Integer how many of locations are active, all of them first
      defaultLocation        LookupListItem the Default Location, always active, or null for none
      locationRequired       Boolean whether the Location Requirement is on
      locationListName       String the Location List's name, for the Look-Up List Manager link
      canChange              Boolean whether the user may change locations
      canDeactivate          Boolean whether the user may disable one
      canAdd                 Boolean whether the user may add one in the Look-Up List Manager
      saveFailed             Boolean true when the last change was rejected
      saveFailedKey          String the message for a change refused with a reason, if any
      saveFailedParam        String that message's parameter, if it takes one
      statusTabEnabled       Boolean whether to show the Status tab

    @since 2026-09-15
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
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
<c:set var="activeTab" value="location"/>
<c:set var="locationAction" value="/appointment/apptLocationSetting.do"/>
<c:set var="itemStyleEditorAction" value="${locationAction}"/>
<c:set var="itemStyleEditorDescriptionTitleKey" value="admin.appt.location.label.editName"/>
<fmt:message key="admin.appt.location.label.editName" var="editNameLabel"/>
<fmt:message key="admin.appt.location.msg.confirmLastDisable" var="confirmLastDisable"/>
<%-- An unstyled location draws the default chip; its empty Colour and Icon cells say so on hover. --%>
<fmt:message key="admin.appt.location.label.defaultStyle" var="defaultStyleLabel"/>
<fmt:message key="admin.appt.location.label.default" var="defaultBadge"/>
<fmt:message key="admin.appt.location.msg.default" var="defaultHint"/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><fmt:message key="admin.appt.location.title"/></title>
    <link href="${ctx}/library/bootstrap/5.0.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="${ctx}/css/glyphicons-standalone.css" rel="stylesheet">
    <link href="${ctx}/library/coloris/0.25.0/coloris.min.css" rel="stylesheet">
    <link href="${ctx}/css/itemStyleEditor.css" rel="stylesheet">
    <script src="${ctx}/library/coloris/0.25.0/coloris.min.js"></script>
    <script src="${ctx}/js/appointment/itemStyleEditor.js"></script>
    <style>
        /* The action cells fit their buttons, so the table's spare width goes to the other columns. */
        .location-actions {
            width: 1%;
        }
    </style>
</head>
<body class="p-3" data-name-max-length="<%= ca.openosp.openo.appt.LocationList.LABEL_MAX_LENGTH %>">
<%@ include file="appointmentSettingsNav.jspf" %>

<div class="d-flex align-items-center mb-3">
    <h1 class="h5 mb-0 me-auto"><fmt:message key="admin.appt.location.title"/></h1>
    <c:if test="${canAdd and not empty locationListName}">
        <c:url var="manageItemsUrl" value="/lookupListManagerAction.do">
            <c:param name="method" value="manageSingle"/>
            <c:param name="listName" value="${locationListName}"/>
        </c:url>
        <a class="btn btn-sm btn-outline-secondary" href="${e:forHtmlAttribute(manageItemsUrl)}">
            <fmt:message key="admin.appt.location.btn.manageItems"/>
        </a>
    </c:if>
</div>

<c:if test="${saveFailed}">
    <div class="alert alert-danger" role="alert">
        <fmt:message key="${empty saveFailedKey ? 'admin.appt.settings.msg.saveFailed' : saveFailedKey}">
            <c:if test="${not empty saveFailedParam}"><fmt:param value="${e:forHtml(saveFailedParam)}"/></c:if>
        </fmt:message>
    </div>
</c:if>

<%-- The list takes over booking as soon as one location is active, so say so while none is. --%>
<c:if test="${activeLocationCount == 0 and not empty locations}">
    <div class="alert alert-info" role="alert"><fmt:message key="admin.appt.location.msg.noneActive"/></div>
</c:if>

<c:choose>
    <c:when test="${empty locations}">
        <p class="text-muted"><fmt:message key="admin.appt.location.msg.empty"/></p>
    </c:when>
    <c:otherwise>
        <%-- Saves on click, with no confirmation: one more click undoes it, and nothing booked changes. --%>
        <form method="post" action="<c:url value='${locationAction}'/>" class="mb-3">
            <input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue/>">
            <input type="hidden" name="dispatch" value="updateRequirement">
            <div class="form-check form-switch">
                <input class="form-check-input" type="checkbox" role="switch" id="locationRequirement" name="value"
                       value="true" aria-describedby="locationRequirementHint"
                       ${locationRequired ? 'checked' : ''} ${canChange ? '' : 'disabled'}>
                <label class="form-check-label" for="locationRequirement">
                    <fmt:message key="admin.appt.location.label.requirement"/>
                </label>
                <div id="locationRequirementHint" class="form-text">
                    <fmt:message key="admin.appt.location.msg.requirementHint"/>
                </div>
            </div>
        </form>
        <div class="table-responsive">
            <table class="table table-sm table-striped align-middle">
                <thead>
                <tr>
                    <th scope="col"><fmt:message key="admin.appt.location.label.name"/></th>
                    <th scope="col"><fmt:message key="admin.appt.location.label.colour"/></th>
                    <th scope="col"><fmt:message key="admin.appt.location.label.icon"/></th>
                    <th scope="col"><fmt:message key="admin.appt.location.label.chip"/></th>
                    <th scope="col"><fmt:message key="admin.appt.location.label.active"/></th>
                    <c:if test="${canChange}">
                        <%-- One cell per kind of change, so each kind lines up down the table. --%>
                        <th scope="col" colspan="3"><fmt:message key="admin.appt.location.label.actions"/></th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${locations}" var="item" varStatus="loop">
                    <tr id="location-${e:forHtmlAttribute(item.id)}" class="${item.active ? '' : 'text-muted'}">
                        <c:set var="isDefault" value="${item.id eq defaultLocation.id}"/>
                        <td>
                            <c:out value="${item.label}"/>
                            <c:if test="${isDefault}">
                                <span class="badge bg-secondary" title="${e:forHtmlAttribute(defaultHint)}">${e:forHtml(defaultBadge)}<span class="visually-hidden"> ${e:forHtml(defaultHint)}</span></span>
                            </c:if>
                            <c:if test="${canChange}">
                                <appt:itemStyleEditButton kind="description" itemId="${item.id}"
                                                          current="${item.label}" label="${editNameLabel}"/>
                            </c:if>
                        </td>
                        <td class="text-nowrap">
                            <c:choose>
                                <c:when test="${not empty item.colour}">
                                    <span class="item-style-swatch" data-colour="${e:forHtmlAttribute(item.colour)}"></span>
                                    <c:out value="${item.colour}"/>
                                </c:when>
                                <c:otherwise><span class="text-muted" role="img" title="${e:forHtmlAttribute(defaultStyleLabel)}" aria-label="${e:forHtmlAttribute(defaultStyleLabel)}">&mdash;</span></c:otherwise>
                            </c:choose>
                            <c:if test="${canChange}">
                                <appt:itemStyleEditButton kind="colour" itemId="${item.id}" current="${item.colour}"/>
                            </c:if>
                        </td>
                        <td class="text-nowrap">
                            <c:choose>
                                <c:when test="${not empty item.icon}">
                                    <span class="item-style-icon" data-icon="${e:forHtmlAttribute(item.icon)}"></span>
                                </c:when>
                                <c:otherwise><span class="text-muted" role="img" title="${e:forHtmlAttribute(defaultStyleLabel)}" aria-label="${e:forHtmlAttribute(defaultStyleLabel)}">&mdash;</span></c:otherwise>
                            </c:choose>
                            <c:if test="${canChange}">
                                <appt:itemStyleEditButton kind="icon" itemId="${item.id}" current="${item.icon}"/>
                            </c:if>
                        </td>
                        <%-- Inactive locations still draw their chip on the appointments booked with them. --%>
                        <td><appt:locationChip item="${item}"/></td>
                        <td><fmt:message key="${item.active ? 'global.yes' : 'global.no'}"/></td>
                        <c:if test="${canChange}">
                            <td class="text-nowrap location-actions">
                                <c:if test="${item.active and activeLocationCount > 1}">
                                    <appt:locationActionButton action="${locationAction}" dispatch="moveUp"
                                                               itemId="${item.id}" labelKey="admin.appt.location.btn.moveUp"
                                                               glyph="glyphicon-chevron-up" disabled="${loop.first}"/>
                                    <appt:locationActionButton action="${locationAction}" dispatch="moveDown"
                                                               itemId="${item.id}" labelKey="admin.appt.location.btn.moveDown"
                                                               glyph="glyphicon-chevron-down"
                                                               disabled="${loop.index == activeLocationCount - 1}"/>
                                </c:if>
                            </td>
                            <td class="text-nowrap location-actions">
                                <c:if test="${item.active}">
                                    <appt:locationActionButton action="${locationAction}" dispatch="${isDefault ? 'removeDefault' : 'makeDefault'}"
                                                               itemId="${item.id}"
                                                               labelKey="${isDefault ? 'admin.appt.location.btn.removeDefault' : 'admin.appt.location.btn.makeDefault'}"/>
                                </c:if>
                            </td>
                            <td class="text-nowrap location-actions">
                                <c:choose>
                                    <c:when test="${not item.active}">
                                        <appt:locationActionButton action="${locationAction}" dispatch="restore"
                                                                   itemId="${item.id}" labelKey="admin.appt.location.btn.enable"/>
                                    </c:when>
                                    <c:when test="${canDeactivate}">
                                        <appt:locationActionButton action="${locationAction}" dispatch="deactivate"
                                                                   itemId="${item.id}" labelKey="admin.appt.location.btn.disable"
                                                                   confirm="${activeLocationCount == 1 ? confirmLastDisable : ''}"/>
                                    </c:when>
                                </c:choose>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <c:if test="${canChange}">
            <p class="text-muted small"><fmt:message key="admin.appt.location.msg.renameNote"/></p>
        </c:if>
    </c:otherwise>
</c:choose>

<%@ include file="itemStyleEditorDialog.jspf" %>
<script>
    (function () {
        // The switch is disabled for a user who can't change it, so it never fires for them.
        const requirement = document.getElementById('locationRequirement');
        if (requirement) {
            requirement.addEventListener('change', function () {
                requirement.form.submit();
            });
        }

        // Disabling the last active location sends every booking screen back to a typed box.
        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (!window.confirm(form.dataset.confirm)) {
                    event.preventDefault();
                }
            });
        });

        ItemStyleEditor.init({
            description: {maxLength: Number(document.body.dataset.nameMaxLength)},
            colour: {clearable: true},
            icon: {clearable: true}
        });
    })();
</script>
</body>
</html>
