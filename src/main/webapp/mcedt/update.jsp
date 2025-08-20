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

<%@ page errorPage="error.jsp" %>

<!DOCTYPE html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>



<html>
    <head>

        <jsp:include page="head-includes.jsp"/>

        <script language="javascript">
            function cancel(control) {
                return submitForm('cancel', control);
            }

            function modifyResource(resourceId) {
                window.location.href = "updateUpload.jsp?resourceId=" + resourceId;
                return false;
            }

            function modify(control) {
                return submitForm('sendUpdateRequest', control);
            }

            function submitForm(methodType, control) {
                if (control) {
                    control.disabled = true;
                }

                var method = jQuery("#method");
                method.val(methodType);

                var form = jQuery("form");
                form.submit();
                return true;
            }
        </script>


        <title>MCEDT: Updates</title>

        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
    </head>

    <body>
    <div class="container-fluid">
        <div class="row-fluid">

            <h2>Update Resource</h2>

            <form action="${pageContext.request.contextPath}/mcedt/update.do" method="post" id="form">

                <jsp:include page="messages.jsp"/>

                <input id="method" name="method" type="hidden" value="cancel"/>

                <table class="table table-striped  table-condensed">
                    <c:forEach var="d" items="${mcedtUploadDetails.data}" varStatus="i">
                        <tr>
                            <td><c:out value="${d.resourceID}"/> <c:out
                                    value="${d.resourceType}"/> <c:out value="${d.description}"/>
                            </td>
                            <td><c:choose>
                                <c:when test="${empty d.modifyTimestamp}">
                                    <button class="btn" onclick="return modifyResource(${d.resourceID})">Modify</button>
                                </c:when>
                                <c:otherwise>
                                    <button class="btn" disabled="disabled">Modify</button>
                                </c:otherwise>
                            </c:choose></td>
                        </tr>
                    </c:forEach>
                </table>

                <div>
                    <button class="btn btn-primary" onclick="modify(this)">Save Changes</button>
                    <button class="btn" onclick="return cancel(this)">Cancel</button>
                </div>

            </form>
        </div>
    </div>
    </body>
</html>