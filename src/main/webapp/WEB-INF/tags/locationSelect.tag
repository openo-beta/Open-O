<%--
    Location List dropdown for the booking screens, shown in place of the free-text location when
    ca.openosp.openo.appt.LocationList#isLocationMode() is true. It posts locationCode, and a
    hidden location holding the Legacy Location, if any; LocationList.applyPostedLocation saves them.

    The first choice is "Not specified", or the Legacy Location when the appointment has one: its
    location text with no item, which saving unchanged keeps.

    Attributes:
      choices   List<LookupListItem> the items to offer, in display order
      selected  Integer the chosen item's id, or null
      legacy    String the Legacy Location to offer, or blank

    @since 2026-09-15
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="choices" required="true" type="java.util.List" %>
<%@ attribute name="selected" required="false" type="java.lang.Integer" %>
<%@ attribute name="legacy" required="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setBundle basename="oscarResources"/>
<select name="locationCode" class="form-control" tabindex="4">
    <c:choose>
        <c:when test="${not empty legacy}">
            <option value="" selected><c:out value="${legacy}"/></option>
        </c:when>
        <c:otherwise>
            <option value=""><fmt:message key="appointment.location.notSpecified"/></option>
        </c:otherwise>
    </c:choose>
    <c:forEach items="${choices}" var="item">
        <option value="${fn:escapeXml(item.id)}" ${item.id == selected ? 'selected' : ''}><c:out value="${item.label}"/></option>
    </c:forEach>
</select>
<input type="hidden" name="location" value="${fn:escapeXml(legacy)}">
