<%--
    Location Chip: the small marker on a schedule appointment showing its Location List item's
    colour and icon, with the item's current label on hover and for screen readers. An item with no
    colour draws a see-through chip; one with no icon draws a map marker. A glyph icon is dark or
    white, whichever reads better on the chip (ItemStyleColour.iconColour); an image icon keeps its
    own colours and is drawn 10px tall, like the status icon beside it. Renders nothing when item
    is null.

    Needs the Bootstrap 3 glyphicon font, which the schedule already loads.

    Attributes:
      item  LookupListItem the appointment's Location List item, or null

    @since 2026-09-15
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" import="ca.openosp.openo.appt.IconSet,ca.openosp.openo.appt.ItemStyleColour" %>
<%@ attribute name="item" required="false" type="ca.openosp.openo.commn.model.LookupListItem" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
<c:if test="${not empty item}">
    <c:set var="icon" value="${empty item.icon ? 'glyphicon-map-marker' : item.icon}"/>
    <%-- Spaces inside the chip don't show: an inline block drops them at the start and end of its line. --%>
    <span class="location-chip" role="img" title="${e:forHtmlAttribute(item.label)}" aria-label="${e:forHtmlAttribute(item.label)}"
          style="display:inline-block;padding:0 2px;border-radius:2px;color:${ItemStyleColour.iconColour(item.colour)};<c:if test='${not empty item.colour}'>background-color:${e:forHtmlAttribute(item.colour)};</c:if>">
        <c:choose>
            <c:when test="${IconSet.isGlyph(icon)}">
                <span class="glyphicon ${e:forHtmlAttribute(icon)}" aria-hidden="true"></span>
            </c:when>
            <c:otherwise>
                <img src="${e:forHtmlAttribute(pageContext.request.contextPath)}/images/${e:forHtmlAttribute(icon)}" alt=""
                     height="10">
            </c:otherwise>
        </c:choose>
    </span>
</c:if>
