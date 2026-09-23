<%--
    Location List dropdown for the booking screens, shown in place of the free-text location when
    ca.openosp.openo.appt.LocationList#isLocationMode() is true. It posts locationCode, and a
    hidden location holding the Legacy Location, if any; LocationList.applyPostedLocation saves them.

    The first choice is "Not specified", which saves no location. An appointment with a Legacy
    Location (location text with no item) also gets that text as a choice, posted as
    LocationList.LEGACY_VALUE and marked data-legacy, so saving it unchanged keeps the text.

    Attributes:
      choices   List<LookupListItem> the items to offer, in display order
      selected  String the chosen option's value (see LocationList.Choice), or blank for "Not specified"
      legacy    String the Legacy Location to offer, or blank

    @since 2026-09-15
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="choices" required="true" type="java.util.List" %>
<%@ attribute name="selected" required="false" %>
<%@ attribute name="legacy" required="false" %>
<%@ tag import="ca.openosp.openo.appt.LocationList" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
<fmt:setBundle basename="oscarResources"/>
<select name="locationCode" class="form-control" tabindex="4">
    <option value=""><fmt:message key="appointment.location.notSpecified"/></option>
    <c:if test="${not empty legacy}">
        <c:set var="legacyValue"><%= LocationList.LEGACY_VALUE %></c:set>
        <option value="${legacyValue}" data-legacy ${legacyValue eq selected ? 'selected' : ''}><c:out value="${legacy}"/></option>
    </c:if>
    <%-- A saved location that is no longer offered stays on the list, marked, so saving keeps it.
         The option's text is kept free of stray whitespace, and an inactive one is marked, because
         js/appointment/locationSelect.js matches a location by name. --%>
    <c:forEach items="${choices}" var="item">
        <c:choose>
            <c:when test="${item.active}"><c:set var="optionLabel" value="${item.label}"/></c:when>
            <c:otherwise>
                <fmt:message key="appointment.location.inactiveItem" var="optionLabel">
                    <fmt:param value="${item.label}"/>
                </fmt:message>
            </c:otherwise>
        </c:choose>
        <%-- A body-set var is a String, so the comparison is text, like the posted value. --%>
        <c:set var="optionValue">${item.id}</c:set>
        <option value="${e:forHtmlAttribute(optionValue)}" ${optionValue eq selected ? 'selected' : ''} ${item.active ? '' : 'data-inactive'}><c:out value="${optionLabel}"/></option>
    </c:forEach>
</select>
<input type="hidden" name="location" value="${e:forHtmlAttribute(legacy)}">
