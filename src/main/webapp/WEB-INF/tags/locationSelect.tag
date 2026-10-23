<%--
    Location List dropdown for the booking screens, shown in place of the free-text location when
    ca.openosp.openo.appt.LocationList#isLocationMode() is true. It posts locationCode, and a
    hidden location holding the Legacy Location, if any; LocationList.applyPostedLocation saves them.
    Each location's option carries its Location Chip in data-chip, which the locationmenu widget in
    js/appointment/locationSelect.js draws beside its name.

    The first choice is blank and gives a Blank Location, as in the Type and Reason dropdowns. An
    appointment with a Legacy Location (location text with no item) also gets that text as a choice,
    posted as LocationList.LEGACY_VALUE and marked data-legacy, so saving it unchanged keeps the text.

    Attributes:
      dropdown  LocationList.Dropdown the dropdown to draw, from LocationList.newBookingDropdown or
                LocationList.editDropdown

    @since 2026-09-15
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="dropdown" required="true" type="ca.openosp.openo.appt.LocationList.Dropdown" %>
<%@ tag import="ca.openosp.openo.appt.LocationList" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="appt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
<fmt:setBundle basename="oscarResources"/>
<select name="locationCode" class="form-control" tabindex="4" ${dropdown.required() ? 'required' : ''}>
    <option value=""></option>
    <c:if test="${not empty dropdown.legacy()}">
        <c:set var="legacyValue"><%= LocationList.LEGACY_VALUE %></c:set>
        <option value="${legacyValue}" data-legacy ${legacyValue eq dropdown.selected() ? 'selected' : ''}><c:out value="${dropdown.legacy()}"/></option>
    </c:if>
    <%-- A saved location that is no longer offered stays on the list, marked, so saving keeps it.
         The option's text is kept free of stray whitespace, and an inactive one is marked, because
         js/appointment/locationSelect.js matches a location by name. --%>
    <c:forEach items="${dropdown.options()}" var="option">
        <c:set var="item" value="${option.item()}"/>
        <c:choose>
            <c:when test="${item.active}"><c:set var="optionLabel" value="${option.name()}"/></c:when>
            <c:otherwise>
                <fmt:message key="appointment.location.inactiveItem" var="optionLabel">
                    <fmt:param value="${option.name()}"/>
                </fmt:message>
            </c:otherwise>
        </c:choose>
        <%-- A body-set var is a String, so the comparison is text, like the posted value. --%>
        <c:set var="optionValue">${item.id}</c:set>
        <c:set var="chip"><appt:locationChip item="${item}" name="${option.name()}"/></c:set>
        <option value="${e:forHtmlAttribute(optionValue)}" ${optionValue eq dropdown.selected() ? 'selected' : ''} ${item.active ? '' : 'data-inactive'} data-chip="${e:forHtmlAttribute(chip)}"><c:out value="${optionLabel}"/></option>
    </c:forEach>
</select>
<input type="hidden" name="location" value="${e:forHtmlAttribute(dropdown.legacy())}">
