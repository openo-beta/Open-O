<%--
    Fax popup error page.

    Shown in an OSCAR Fax popup when the item can't be faxed, for example a document that is
    password-protected or no longer linked to the patient. It alerts the message and closes the
    popup, as the OSCAR Email window does for its errors. The message also stays on the page in
    case the browser suppresses the alert.

    Request attributes:
      errorMessage (String) - the plain-text reason shown to staff

    @since 2026-11-24
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>

<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_fax" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_fax");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <title>OSCAR Fax</title>
    <c:set var="ctx" value="${ pageContext.request.contextPath }" scope="page"/>
    <link rel="stylesheet" href="<e:forHtmlAttribute value='${ ctx }' />/library/bootstrap/3.0.0/css/bootstrap.min.css" type="text/css"/>
</head>
<body>
<div class="container">
    <div class="alert alert-danger" role="alert" style="margin-top: 15px; white-space: pre-line;"><c:out value="${ errorMessage }"/></div>
    <input type="button" class="btn btn-default pull-right" value="Close" onclick="window.close();"/>
</div>
<script type="text/javascript">
    alert("<e:forJavaScript value='${ errorMessage }' />");
    window.close();
</script>
</body>
</html>
