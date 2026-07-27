<%--


    Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
    This software is published under the GPL GNU General Public License.
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

    This software was written for
    Centre for Research on Inner City Health, St. Michael's Hospital,
    Toronto, Ontario, Canada

--%>


<%@ include file="/casemgmt/taglibs.jsp" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_casemgmt.notes" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_casemgmt.notes");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>
<%
    String caseNote_history = (String) request.getAttribute("caseNote_history");
    if (caseNote_history == null) {
        caseNote_history = "";
    }
%>

<html>
<head>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
    <title>Note History</title>
    <c:set var="ctx" value="${pageContext.request.contextPath}"
           scope="request"/>
    <link rel="stylesheet" href="<c:out value="${ctx}"/>/css/casemgmt.css"
          type="text/css">

</head>
<body bgcolor="#eeeeff">
<form action="<%= request.getContextPath() %>/CaseManagementEntry.do">
<br>
<b>Archived Note Update History</b>
<br>
<br>
Client name:
<I> <c:if test="${not empty requestScope.demoName}">
    <%=request.getAttribute("demoName") == null ? "" : Encode.forHtml(String.valueOf(request.getAttribute("demoName")))%>
</c:if>
<c:if test="${empty requestScope.demoName}">
    <%=request.getParameter("demoName") == null ? "" : Encode.forHtml(request.getParameter("demoName"))%>
</c:if> </I>
<br>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Age:
<I> <c:if test="${not empty requestScope.demoName}">
    <%=request.getAttribute("demoAge") == null ? "" : Encode.forHtml(String.valueOf(request.getAttribute("demoAge")))%>
</c:if>
<c:if test="${empty requestScope.demoName}">
    <%=request.getParameter("demoAge") == null ? "" : Encode.forHtml(request.getParameter("demoAge"))%>
</c:if> </I>
<br>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; DOB:
<I> <c:if test="${not empty requestScope.demoName}">
    <%=request.getAttribute("demoDOB") == null ? "" : Encode.forHtml(String.valueOf(request.getAttribute("demoDOB")))%>
</c:if>
<c:if test="${empty requestScope.demoName}">
    <%=request.getParameter("demoDOB") == null ? "" : Encode.forHtml(request.getParameter("demoDOB"))%>
</c:if> </I>
<br>
<br>


<input type="button" value=" Close This Page " onclick="self.close()">
<br>
<table width="400" border="0">
    <tr>
        <td class="fieldValue">
            <textarea name="caseNote_history" cols="107" rows="29" wrap="soft">
                <%= Encode.forHtml(caseNote_history) %>                       
            </textarea>
        </td>
    </tr>
    <br>

    </form>
</body>
</html>
