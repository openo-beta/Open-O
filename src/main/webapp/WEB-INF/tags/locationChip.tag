<%--
    Location Chip: the small marker on a schedule appointment showing its Location List item's
    colour and icon, with the item's current label on hover and for screen readers. An item with no
    colour draws a see-through chip; one with no icon draws a map marker. The icon is dark or white,
    whichever reads better on the chip (ItemStyleColour.iconColour). Renders nothing when item is null.

    Needs the Bootstrap 3 glyphicon font, which the schedule already loads.

    Attributes:
      item  LookupListItem the appointment's Location List item, or null

    @since 2026-09-15
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" import="ca.openosp.openo.appt.ItemStyleColour" %>
<%@ attribute name="item" required="false" type="ca.openosp.openo.commn.model.LookupListItem" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
<c:if test="${not empty item}">
    <span class="location-chip" role="img" title="${e:forHtmlAttribute(item.label)}" aria-label="${e:forHtmlAttribute(item.label)}"
          style="display:inline-block;padding:0 2px;border-radius:2px;color:${ItemStyleColour.iconColour(item.colour)};<c:if test='${not empty item.colour}'>background-color:${e:forHtmlAttribute(item.colour)};</c:if>"><span
            class="glyphicon ${e:forHtmlAttribute(empty item.icon ? 'glyphicon-map-marker' : item.icon)}" aria-hidden="true"></span></span>
</c:if>
