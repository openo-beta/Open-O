<%--
    Location Chip: the small marker on a schedule appointment showing its Location List item's
    colour and icon, with the location's name on hover and for screen readers. An item with no
    colour, or a value that isn't one (ItemStyleColour.isColour), draws a see-through chip; one
    with no icon draws a map marker. A glyph icon is dark or white, whichever reads better on the
    chip (ItemStyleColour.iconColour); an image icon keeps its own colours and is drawn 10px tall,
    like the status icon beside it. Renders nothing when item is null.

    Needs the Bootstrap 3 glyphicon font, which the schedule already loads.

    Attributes:
      item  LookupListItem the appointment's Location List item, or null
      name  String the name to show, such as the appointment's saved location name
            (LocationList.getDisplayName); defaults to the item's current label

    @since 2026-09-15
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" import="ca.openosp.openo.appt.IconSet,ca.openosp.openo.appt.ItemStyleColour" %>
<%@ attribute name="item" required="false" type="ca.openosp.openo.commn.model.LookupListItem" %>
<%@ attribute name="name" required="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
<c:if test="${not empty item}">
    <c:set var="icon" value="${empty item.icon ? 'glyphicon-map-marker' : item.icon}"/>
    <c:set var="chipName" value="${empty name ? item.label : name}"/>
    <%-- Spaces inside the chip don't show: an inline block drops them at the start and end of its line. --%>
    <span class="location-chip" role="img" title="${e:forHtmlAttribute(chipName)}" aria-label="${e:forHtmlAttribute(chipName)}"
          style="display:inline-block;padding:0 2px;border-radius:2px;color:${ItemStyleColour.iconColour(item.colour)};<c:if test='${ItemStyleColour.isColour(item.colour)}'>background-color:${e:forHtmlAttribute(item.colour)};</c:if>">
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
