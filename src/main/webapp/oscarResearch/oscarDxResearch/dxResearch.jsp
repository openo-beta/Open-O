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

<%@ page import="oscar.oscarResearch.oscarDxResearch.util.dxResearchCodingSystem" %>
<%@ page import="com.quatro.service.security.SecurityManager" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="/WEB-INF/oscar-tag.tld" prefix="oscar" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_dxresearch" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_dxresearch");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }

    boolean disable;
    SecurityManager sm = new SecurityManager();

    // Check to see if the currently logged in role has write access, if so, disable input fields present in the page
    disable = !sm.hasWriteAccess("_dx.code", roleName$);

    // Set a String based on the "disable" boolean for easy access to use html functionality of "disabled" attribute
    String disabled = disable ? "disabled" : "";

    boolean showQuicklist = sm.hasWriteAccess("_dx.quicklist", roleName$);

    String user_no = (String) session.getAttribute("user");
    String color = "";
    int Count = 0;

    pageContext.setAttribute("showQuicklist", showQuicklist);
    pageContext.setAttribute("disable", disable);
    pageContext.setAttribute("disabled", disabled);
%>

<!DOCTYPE html>
<html>
    <head>
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarResearch.oscarDxResearch.dxResearch.title"/></title>

        <script type="text/javascript" src="${pageContext.servletContext.contextPath}/js/global.js"></script>
        <script type="text/javascript"
                src="${pageContext.servletContext.contextPath}/share/javascript/prototype.js"></script>

        <script type="text/javascript"
                src="${pageContext.servletContext.contextPath}/library/jquery/jquery-1.12.0.min.js"></script>
        <script type="text/javascript"
                src="${pageContext.servletContext.contextPath}/library/jquery/jquery-ui-1.12.1.min.js"></script>
        <script type="text/javascript"
                src="${pageContext.servletContext.contextPath}/library/bootstrap/3.0.0/js/bootstrap.min.js"></script>
        <script type="text/javascript"
                src="${pageContext.servletContext.contextPath}/library/oscar-modal-dialog.js"></script>

        <link rel="stylesheet" type="text/css" media="all"
              href="${pageContext.servletContext.contextPath}/library/jquery/jquery-ui-1.12.1.min.css"/>
        <link rel="stylesheet" type="text/css" media="all"
              href="${pageContext.servletContext.contextPath}/library/bootstrap/3.0.0/css/bootstrap.min.css"/>
        <link rel="stylesheet" type="text/css" media="all"
              href="${pageContext.servletContext.contextPath}/library/bootstrap/3.0.0/css/bootstrap-theme.min.css"/>
        <link rel="stylesheet" type="text/css" href="dxResearch.css">

        <script type="text/javascript">
            //<!--

            jQuery.noConflict();

            function setfocus() {
                document.forms[0].xml_research1.focus();
                document.forms[0].xml_research1.select();
            }

            var remote = null;

            function rs(n, u, w, h, x) {
                args = "width=" + w + ",height=" + h + ",resizable=yes,scrollbars=yes,status=0,top=60,left=30";
                remote = window.open(u, n, args);
                if (remote != null) {
                    if (remote.opener == null)
                        remote.opener = self;
                }
                if (x == 1) {
                    return remote;
                }
            }

            function popPage(url) {
                awnd = rs('', url, 400, 200, 1);
                awnd.focus();
            }

            var awnd = null;

            function ResearchScriptAttach() {
                var t0 = escape(document.forms[0].xml_research1.value);
                var t1 = escape(document.forms[0].xml_research2.value);
                var t2 = escape(document.forms[0].xml_research3.value);
                var t3 = escape(document.forms[0].xml_research4.value);
                var t4 = escape(document.forms[0].xml_research5.value);
                var codeType = document.forms[0].selectedCodingSystem.value;
                var demographicNo = escape(document.forms[0].demographicNo.value);

                awnd = rs('att', 'dxResearchCodeSearch.do?codeType=' + codeType + '&xml_research1=' + t0 + '&xml_research2=' + t1 + '&xml_research3=' + t2 + '&xml_research4=' + t3 + '&xml_research5=' + t4 + '&demographicNo=' + demographicNo, 600, 600, 1);
                awnd.focus();
            }

            function submitform(target, sysCode) {
                document.forms[0].forward.value = target;

                if (sysCode != '')
                    document.forms[0].selectedCodingSystem.value = sysCode;

                document.forms[0].submit()
            }

            function set(target) {
                document.forms[0].forward.value = target;
            }


            function openNewPage(vheight, vwidth, varpage) {
                var page = varpage;
                windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=no,menubars=no,toolbars=no,resizable=no,screenX=0,screenY=0,top=0,left=0";
                var popup = window.open(varpage, "<fmt:setBundle basename="oscarResources"/><fmt:message key="global.oscarComm"/>", windowprops);
                popup.focus();
            }

            document.onkeypress = processKey;

            function processKey(e) {
                if (e == null) {
                    e = window.event;
                } else if (e.keyCode == 13) {
                    ResearchScriptAttach();
                }
            }

            function showdatebox(x) {
                document.getElementById("startdatenew" + x).show();
                document.getElementById("startdate1st" + x).hide();
            }

            function update_date(did, demoNo, provNo) {
                var startdate = document.getElementById("startdatenew" + did).value;
                window.location.href = "dxResearchUpdate.do?startdate=" + startdate + "&did=" + did + "&demographicNo=" + demoNo + "&providerNo=" + provNo;
            }

            //-->
        </script>

    </head>

    <body onLoad="setfocus();">
    <div class="wrapper">

        <div id="page-header">
            <table id="oscarDxHeader">
                <tr>
                    <td id="oscarDxHeaderLeftColumn"><h1><fmt:setBundle basename="oscarResources"/><fmt:message key="global.disease"/></h1></td>

                    <td id="oscarDxHeaderCenterColumn">
                        <oscar:nameage demographicNo="${ demographicNo }"/>
                    </td>
                    <td id="oscarDxHeaderRightColumn" align=right>
					<span class="HelpAboutLogout"> 
						<a style="font-size: 10px; font-style: normal;" href="${ ctx }oscarEncounter/About.jsp"
                           target="_new">About</a>
						<a style="font-size: 10px; font-style: normal;" target="_blank"
                           href="http://www.oscarmanual.org/search?SearchableText=&Title=Chart+Interface&portal_type%3Alist=Document">Help</a>
					</span>
                    </td>
                </tr>
            </table>
        </div>

        <table>
            <tr>
                <td><form action="${pageContext.request.contextPath}/oscarResearch/oscarDxResearch/dxResearch.do" method="post">
                    <table>
                        <% 
    java.util.List<String> actionErrors = (java.util.List<String>) request.getAttribute("actionErrors");
    if (actionErrors != null && !actionErrors.isEmpty()) {
%>
    <div class="action-errors">
        <ul>
            <% for (String error : actionErrors) { %>
                <li><%= error %></li>
            <% } %>
        </ul>
    </div>
<% } %>
                        <tr>
                            <td id="codeSelectorTable">

                                <table>
                                    <tr>
                                        <td>
                                            <div class="input-group">
								<span class="input-group-addon" id="basic-addon3">
									<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarResearch.oscarDxResearch.codingSystem"/>
								</span>

                                                <select class="form-control" name="selectedCodingSystem"
                                                            <%=disabled%>>
                                                    <c:forEach var="codingSys" items="${codingSystem.codingSystems}">
                                                        <option value="${codingSys}">
                                                            <c:out value="${codingSys}"/>
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><input type="text" class="form-control" name="xml_research1"
                                                    <%=disabled%> />
                                            <input type="hidden" name="demographicNo"
                                                   value="<c:out value="${demographicNo}"/>">
                                            <input type="hidden" name="providerNo"
                                                   value="<c:out value="${providerNo}"/>"></td>
                                    </tr>
                                    <tr>
                                        <td><input type="text" class="form-control" name="xml_research2"
                                                       <%=disabled%>/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="text" class="form-control" name="xml_research3"
                                                       <%=disabled%>/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="text" class="form-control" name="xml_research4"
                                                       <%=disabled%>/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="text" class="form-control" name="xml_research5"
                                                       <%=disabled%>/></td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <input type="hidden" name="forward" value="none"/>
                                            <%if (!disable) { %>
                                            <input type="button" name="codeSearch" class="btn btn-primary"
                                                   value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarResearch.oscarDxResearch.btnCodeSearch"/>"
                                                   onClick="ResearchScriptAttach();" )>

                                            <input type="button" name="codeAdd" class="btn btn-primary"
                                                   value="<fmt:setBundle basename="oscarResources"/><fmt:message key="ADD"/>"
                                                   onClick="submitform('','');">

                                            <% } else { %>

                                            <input type="button" name="button" class="btn btn-primary"
                                                   value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarResearch.oscarDxResearch.btnCodeSearch"/>"
                                                   onClick="ResearchScriptAttach();" )
                                                   <%=disabled%>">

                                            <input type="button" name="button" class="btn btn-primary"
                                                   value="<fmt:setBundle basename="oscarResources"/><fmt:message key="ADD"/>"
                                                   onClick="submitform('','');" <%=disabled%>">
                                            <% } %>
                                        </td>
                                    </tr>

                                        <%-- DX QUICK LIST - returns a table --%>
                                    <c:if test="${showQuicklist == true}">
                                        <tr>
                                            <td>
                                                <jsp:include page="dxQuickList.jsp">
                                                    <jsp:param value="false" name="disable"/>
                                                    <jsp:param value="${ param.quickList }" name="quickList"/>
                                                    <jsp:param value="${ demographicNo }" name="demographicNo"/>
                                                    <jsp:param value="${ providerNo }" name="providerNo"/>
                                                </jsp:include>
                                            </td>
                                        </tr>
                                    </c:if>
                                        <%-- DX QUICK LIST --%>

                                </table>

                            </td>
                            <td id="displayDxCodeTable">

                                <table>
                                    <tr>
                                        <th>System</th>
                                        <th class="heading"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarResearch.oscarDxResearch.dxResearch.msgCode"/></th>
                                        <th class="heading"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarResearch.oscarDxResearch.dxResearch.msgDiagnosis"/></th>
                                        <th class="heading"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarResearch.oscarDxResearch.dxResearch.msgFirstVisit"/></th>
                                        <th class="heading"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarResearch.oscarDxResearch.dxResearch.msgLastVisit"/></th>
                                        <% if (!disable) { %>
                                        <th class="heading"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarResearch.oscarDxResearch.dxResearch.msgAction"/></th>
                                        <%} %>
                                    </tr>
                                    <c:forEach var="diagnotics" items="${allDiagnostics.dxResearchBeanVector}" varStatus="ctr">
                                        <c:choose>
                                            <c:when test="${diagnotics.status == 'A'}">
                                                <tr>
                                                    <td><c:out value="${diagnotics.type}"/></td>
                                                    <td class="notResolved"><c:out value="${diagnotics.dxSearchCode}"/></td>
                                                    <td class="notResolved"><c:out value="${diagnotics.description}"/></td>
                                                    <td class="notResolved">
                                                        <a href="#" onclick="showdatebox(${diagnotics.dxResearchNo});">
                                                            <div id="startdate1st${diagnotics.dxResearchNo}">
                                                                <c:out value="${diagnotics.start_date}"/>
                                                            </div>
                                                            <input class="form-control" id="startdatenew${diagnotics.dxResearchNo}"
                                                                   type="text" name="start_date" size="8"
                                                                   value="${diagnotics.start_date}" style="display:none"/>
                                                        </a>
                                                    </td>
                                                    <td class="notResolved"><c:out value="${diagnotics.end_date}"/></td>
                                                    <c:if test="${not disable}">
                                                        <td class="notResolved">
                                                            <a href="dxResearchUpdate.do?status=C&did=${diagnotics.dxResearchNo}&demographicNo=${demographicNo}&providerNo=${providerNo}">
                                                                <fmt:message key="oscarResearch.oscarDxResearch.dxResearch.btnResolve"/>
                                                            </a>
                                                            <a href="dxResearchUpdate.do?status=D&did=${diagnotics.dxResearchNo}&demographicNo=${demographicNo}&providerNo=${providerNo}"
                                                               onclick="return confirm('Are you sure you would like to delete: ${diagnotics.description} ?')">
                                                                <fmt:message key="oscarResearch.oscarDxResearch.dxResearch.btnDelete"/>
                                                            </a>
                                                            <a href="#" onclick="update_date(${diagnotics.dxResearchNo}, ${demographicNo}, ${providerNo});">
                                                                <fmt:message key="oscarResearch.oscarDxResearch.dxResearch.btnUpdate"/>
                                                            </a>
                                                        </td>
                                                    </c:if>
                                                </tr>
                                            </c:when>
                                            <c:when test="${diagnotics.status == 'C'}">
                                                <tr>
                                                    <td><c:out value="${diagnotics.dxSearchCode}"/></td>
                                                    <td><c:out value="${diagnotics.description}"/></td>
                                                    <td><c:out value="${diagnotics.start_date}"/></td>
                                                    <td><c:out value="${diagnotics.end_date}"/></td>
                                                    <c:if test="${not disable}">
                                                        <td>
                                                            <fmt:message key="oscarResearch.oscarDxResearch.dxResearch.btnResolve"/> |
                                                            <a href="dxResearchUpdate.do?status=D&did=${diagnotics.dxResearchNo}&demographicNo=${demographicNo}&providerNo=${providerNo}"
                                                               onclick="return confirm('Are you sure you would like to delete this?')">
                                                                <fmt:message key="oscarResearch.oscarDxResearch.dxResearch.btnDelete"/>
                                                            </a>
                                                        </td>
                                                    </c:if>
                                                </tr>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>

                                </table>

                            </td>
                        </tr>
                    </table>
                </form>
                </td>
            </tr>
        </table>
    </div>
    </body>
</html>

