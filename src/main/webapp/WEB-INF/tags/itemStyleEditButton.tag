<%--
    Pencil button that opens the item style editor (js/appointment/itemStyleEditor.js) for one
    item and kind. The data attributes are the editor's trigger contract; this tag is where pages
    get them from.

    Attributes:
      kind     description, colour or icon
      itemId   the id the editor posts as ID
      label    String the button's tooltip, when the page calls the value something of its own,
               such as a location's Name; defaults to the kind's own wording
      current  the item's current value for this kind

    @since 2026-09-15
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="kind" required="true" %>
<%@ attribute name="itemId" required="true" %>
<%@ attribute name="current" required="false" %>
<%@ attribute name="label" required="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
<fmt:setBundle basename="oscarResources"/>
<c:if test="${empty label}">
    <fmt:message key="appointment.itemStyleEditor.title.${kind}" var="label"/>
</c:if>
<button type="button" class="btn btn-link btn-sm p-0 ms-1" title="${e:forHtmlAttribute(label)}" aria-label="${e:forHtmlAttribute(label)}"
        data-item-style-edit="${e:forHtmlAttribute(kind)}" data-item-id="${e:forHtmlAttribute(itemId)}"
        data-current="${e:forHtmlAttribute(current)}"><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></button>
