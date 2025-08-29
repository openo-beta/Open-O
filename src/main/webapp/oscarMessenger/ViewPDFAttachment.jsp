<%--

    Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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

    This software was written for the
    Department of Family Medicine
    McMaster University
    Hamilton
    Ontario, Canada

--%>

<%@ page
        import="ca.openosp.openo.messenger.docxfer.send.*, ca.openosp.openo.messenger.docxfer.util.*, ca.openosp.openo.util.*" %>
<%@ page import="java.util.*, org.w3c.dom.*" %>
<%@ page import="ca.openosp.openo.util.Doc2PDF" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_msg" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect("../securityError.jsp?type=_msg");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>


<c:if test="${empty msgSessionBean}">
    <c:redirect url="index.jsp"/>
</c:if>
<c:if test="${not empty msgSessionBean}">
    <c:set var="bean" value="${msgSessionBean}" scope="session"/>
    <c:if test="${bean.valid == 'false'}">
        <c:redirect url="index.jsp"/>
    </c:if>
</c:if>

<link rel="stylesheet" type="text/css" href="encounterStyles.css">
<html>
<head>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>

    <%
        String pdfAttch = (String) request.getAttribute("PDFAttachment");

        session.setAttribute("PDFAttachment", pdfAttch);
    %>

    <title>Document Transfer</title>
</head>


<link rel="stylesheet" type="text/css" media="all" href="<%= request.getContextPath() %>/share/css/extractedFromPages.css"/>

<body class="BodyStyle" vlink="#0000FF">
<!--  -->

<table class="MainTable" id="scrollNumber1" name="encounterTable">
    <tr class="MainTableTopRow">
        <td class="MainTableTopRowLeftColumn">Oscar Messenger</td>
        <td class="MainTableTopRowRightColumn">
            <table class="TopStatusBar">
                <tr>
                    <td>OSCAR Messenger Attachment</td>
                    <td></td>
                    <td style="text-align: right"><a
                            href="javascript:popupStart(300,400,'About.jsp')">About</a> | <a
                            href="javascript:popupStart(300,400,'License.jsp')">License</a></td>
                </tr>
            </table>
        </td>
    </tr>


    <tr>
        <td class="MainTableBottomRowLeftColumn"></td>

        <form action="${pageContext.request.contextPath}/oscarMessenger/ViewPDFFile.do" method="post">
            <td class="MainTableBottomRowRightColumn">
                <table cellspacing=3>
                    <tr>
                        <td>
                            <table class=messButtonsA cellspacing=0 cellpadding=3>
                                <tr>
                                    <td class="messengerButtonsA"><a href="#"
                                                                     onclick="javascript:top.window.close()"
                                                                     class="messengerButtons">
                                        Close Attachment </a></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <table>

                            <% Vector attVector = Doc2PDF.getXMLTagValue(pdfAttch, "TITLE" ); %>
                            <% for ( int i = 0 ; i < attVector.size(); i++) { %>
                    <tr>
                        <td bgcolor="#DDDDFF"><%=(String) attVector.get(i)%>
                        </td>
                        <td bgcolor="#DDDDFF"><input type=submit
                                                     onclick=" document.forms[0].file_id.value = <%=i%>"
                                                     value="Download"/></td>
                    </tr>
                            <% }  %>
                        <input type="hidden" name="file_id" id="file_id"/>
                        <input type="hidden" name="attachment" id="attachment" value="<%=pdfAttch%>"/>

                    <table>
            </td>
        </form>
    </tr>
</table>
</body>
</html>
