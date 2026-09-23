<%--
    One Appointment Settings, Location tab change, as its own posted form: moving a location along
    the order, disabling it, or enabling it. Changes are posted because the CSRF guard
    only checks posts (see AppointmentSettingsAction).

    A glyph renders an icon button labelled for screen readers; without one the label is the button's
    text. A confirm message is asked before the form is submitted, by the page's script.

    Attributes:
      action    String the form's context-relative action, e.g. /appointment/apptLocationSetting.do
      dispatch  String the change to apply: moveUp, moveDown, deactivate or restore
      itemId    the location's LookupListItem id
      labelKey  String the message key naming the change
      glyph     String a glyphicon class for an icon button, or nothing for a text button
      disabled  Boolean true to show the button but refuse the change, e.g. at the end of the order
      confirm   String a question to ask before posting, or nothing to post straight away

    @since 2026-09-17
--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="dispatch" required="true" %>
<%@ attribute name="itemId" required="true" %>
<%@ attribute name="labelKey" required="true" %>
<%@ attribute name="glyph" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ attribute name="confirm" required="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
<%@ taglib prefix="csrf" uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" %>
<fmt:setBundle basename="oscarResources"/>
<fmt:message key="${labelKey}" var="label"/>
<form method="post" action="<c:url value='${action}'/>" class="d-inline"
      <c:if test="${not empty confirm}">data-confirm="${e:forHtmlAttribute(confirm)}"</c:if>>
    <input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue/>">
    <input type="hidden" name="dispatch" value="${e:forHtmlAttribute(dispatch)}">
    <input type="hidden" name="ID" value="${e:forHtmlAttribute(itemId)}">
    <c:choose>
        <c:when test="${not empty glyph}">
            <button type="submit" class="btn btn-sm btn-outline-secondary" title="${e:forHtmlAttribute(label)}"
                    aria-label="${e:forHtmlAttribute(label)}" ${disabled ? 'disabled' : ''}>
                <span class="glyphicon ${e:forHtmlAttribute(glyph)}" aria-hidden="true"></span>
            </button>
        </c:when>
        <c:otherwise>
            <button type="submit" class="btn btn-sm btn-outline-secondary" ${disabled ? 'disabled' : ''}>
                <c:out value="${label}"/>
            </button>
        </c:otherwise>
    </c:choose>
</form>
