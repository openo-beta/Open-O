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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/casemgmt/taglibs.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    if (session.getValue("user") == null)
        response.sendRedirect(request.getContextPath() + "/logout.htm");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>
<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.setInTabs.title"/></title>

        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/oscarEncounter/encounterStyles.css">
    </head>

    <body class="BodyStyle" vlink="#0000FF">

    <table class="MainTable" id="scrollNumber1" name="encounterTable">
        <tr class="MainTableTopRow">
            <td class="MainTableTopRowLeftColumn">
                <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.setInTabs.preferences"/>
            </td>
            <td style="color: white" class="MainTableTopRowRightColumn">
                <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.setInTabs.header"/>
            </td>
        </tr>
        <tr>
            <td class="MainTableLeftColumn">&nbsp;</td>
            <td class="MainTableRightColumn">
                <%if (request.getAttribute("status") == null) {%>
                <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.setInTabs.description"/>

                <form action="${pageContext.request.contextPath}/setProviderStaleDate.do" method="post">
                    <input type="hidden" name="method" value="<c:out value="${method}"/>">
                    <br/>
                    <label>
                        <input type="checkbox" name="openInTabs.checked" <c:if test="${openInTabs.value == 'true'}">checked</c:if> />
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.setInTabs.enableLabel"/>
                    </label>
                    <br/><br/>
                    <p style="font-size: 11px; color: #666;">
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.setInTabs.hint"/>
                    </p>
                    <br/>
                    <input type="submit" name="btnApply" value="<fmt:setBundle basename="oscarResources"/><fmt:message key="global.btnSave"/>" />
                </form>

                <%} else {%>
                <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.setInTabs.saved"/> <br>
                <a href="javascript:window.close();"><fmt:setBundle basename="oscarResources"/><fmt:message key="global.btnClose"/></a>
                <%}%>
            </td>
        </tr>
        <tr>
            <td class="MainTableBottomRowLeftColumn"></td>
            <td class="MainTableBottomRowRightColumn"></td>
        </tr>
    </table>
    </body>
</html>
