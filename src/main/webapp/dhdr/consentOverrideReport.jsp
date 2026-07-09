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
<%--
    DHDR Temporary Consent Unblock report (DHDR13.02).

    Purpose : Lists every temporary consent-unblock (override) decision recorded against the
              Digital Health Drug Repository, with the stored keys resolved to display values.
    Features: Per row - date/time of the override, EMR user's name, patient Unique ID, patient
              name, patient HCN, and the continue/refuse/cancel decision. Searchable by patient
              last name and/or patient Unique ID, over a date range.
    Data    : Populated by ConsentOverrideReport2Action (mapped as dhdr/consentOverrideReport.do),
              which sets the "rows" request attribute (List of Row) plus the echoed search inputs.
    Security: Rides the _rx security object (DHDR medication data); the action is the primary
              guard and this page re-asserts the same privilege defensively.
    Params  : searchLastName, searchUniqueId, dateFrom (yyyy-MM-dd), dateTo (yyyy-MM-dd) - all
              optional; the search form submits them back via GET (idempotent read).
    @since 2026-07-08
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%><security:oscarSec roleName="<%=roleName$%>" objectName="_rx" rights="r" reverse="<%=true%>">
    <%authed = false;%>
    <%response.sendRedirect("../securityError.jsp?type=_rx");%>
</security:oscarSec><%
    if (!authed) {
        return;
    }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>DHDR Temporary Consent Unblock Report</title>
    <link href="<%=request.getContextPath()%>/library/bootstrap/3.0.0/css/bootstrap.css" rel="stylesheet">
</head>
<body>
<div class="container" style="margin-top: 20px;">
    <h3>DHDR Temporary Consent Unblock Report</h3>
    <p class="text-muted">Record of temporary consent-unblock (override) requests for the Digital Health Drug Repository.</p>

    <form method="get" action="<%=request.getContextPath()%>/dhdr/consentOverrideReport.do" class="form-inline" style="margin-bottom: 15px;">
        <div class="form-group">
            <label for="searchLastName">Patient last name</label>
            <input type="text" class="form-control" id="searchLastName" name="searchLastName"
                   value="<c:out value='${searchLastName}'/>"/>
        </div>
        <div class="form-group">
            <label for="searchUniqueId">Patient Unique ID</label>
            <input type="text" class="form-control" id="searchUniqueId" name="searchUniqueId"
                   value="<c:out value='${searchUniqueId}'/>"/>
        </div>
        <div class="form-group">
            <label for="dateFrom">From</label>
            <input type="date" class="form-control" id="dateFrom" name="dateFrom"
                   value="<c:out value='${dateFrom}'/>"/>
        </div>
        <div class="form-group">
            <label for="dateTo">To</label>
            <input type="date" class="form-control" id="dateTo" name="dateTo"
                   value="<c:out value='${dateTo}'/>"/>
        </div>
        <button type="submit" class="btn btn-primary">Search</button>
    </form>

    <c:if test="${not empty dateWarning}">
        <div class="alert alert-warning"><c:out value="${dateWarning}"/></div>
    </c:if>

    <p><strong>${fn:length(rows)}</strong> override request(s) found.</p>

    <table class="table table-striped table-bordered">
        <thead>
        <tr>
            <th>Date / Time</th>
            <th>EMR User</th>
            <th>Patient Unique ID</th>
            <th>Patient Name</th>
            <th>HCN</th>
            <th>Choice</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${empty rows}">
                <tr>
                    <td colspan="6" class="text-center text-muted">No override requests match the current criteria.</td>
                </tr>
            </c:when>
            <c:otherwise>
                <c:forEach var="row" items="${rows}">
                    <tr>
                        <td><c:out value="${row.dateTime}"/></td>
                        <td><c:out value="${row.emrUser}"/></td>
                        <td><c:out value="${row.uniqueId}"/></td>
                        <td><c:out value="${row.patientName}"/></td>
                        <td><c:out value="${row.hcn}"/></td>
                        <td><c:out value="${row.choice}"/></td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>
</div>
</body>
</html>
