<%--
    Status Icon: the icon marking an appointment's status on the schedule, 10px tall like the status
    images. An image is drawn as given, so a signed or verified appointment shows the image's own S or
    V copy. A glyph is drawn dark, like the appointment text, on a white square when signed and an
    orange one when verified: the backgrounds of the S and V copies.

    Needs the Bootstrap 3 glyphicon font, which the schedule already loads.

    Attributes:
      icon     String ApptStatusData.getIcon(): an image under /images, or a glyph class
      signOff  String ApptStatusData.getSignOff(): S, V or blank
      label    String the status's name, read out in place of the icon

    @since 2026-10-07
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" import="ca.openosp.openo.appt.IconSet,ca.openosp.openo.appt.ItemStyleColour" %>
<%@ attribute name="icon" required="true" %>
<%@ attribute name="signOff" required="false" %>
<%@ attribute name="label" required="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
<c:choose>
    <c:when test="${IconSet.isGlyph(icon)}">
        <%-- #fb7204 is the orange behind the V copies (Vhere.gif). --%>
        <c:set var="square" value="${signOff eq 'S' ? '#ffffff' : signOff eq 'V' ? '#fb7204' : 'transparent'}"/>
        <span role="img" aria-label="${e:forHtmlAttribute(label)}"
              style="display:inline-block;padding:0 1px;color:${ItemStyleColour.DARK};background-color:${square}"><span
                class="glyphicon ${e:forHtmlAttribute(icon)}" style="font-size:10px" aria-hidden="true"></span></span>
    </c:when>
    <c:otherwise>
        <img src="${e:forHtmlAttribute(pageContext.request.contextPath)}/images/${e:forHtmlAttribute(icon)}" border="0" height="10"
             alt="${e:forHtmlAttribute(label)}">
    </c:otherwise>
</c:choose>
