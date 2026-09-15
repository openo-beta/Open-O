<%--
    Location Chip: the small marker on a schedule appointment showing its Location List item's
    colour and icon, with the item's current label on hover. An item with no colour or no icon falls
    back to neutral grey or a map marker, each on its own. Renders nothing when item is null.

    Needs the Bootstrap 3 glyphicon font, which the schedule already loads.

    Attributes:
      item  LookupListItem the appointment's Location List item, or null

    @since 2026-09-15
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="item" required="false" type="ca.openosp.openo.commn.model.LookupListItem" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:if test="${not empty item}">
    <span class="location-chip" title="${fn:escapeXml(item.label)}"
          style="display:inline-block;padding:0 2px;border-radius:2px;color:#fff;background-color:${fn:escapeXml(empty item.colour ? '#6c757d' : item.colour)};"><span
            class="glyphicon ${fn:escapeXml(empty item.icon ? 'glyphicon-map-marker' : item.icon)}" aria-hidden="true"></span></span>
</c:if>
