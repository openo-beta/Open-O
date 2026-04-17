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

<%@page import="java.net.URLEncoder" %>
<%@page import="java.nio.charset.StandardCharsets" %>
<%@page import="java.text.SimpleDateFormat, java.util.*,ca.openosp.openo.prevention.*,ca.openosp.openo.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="oscarResources"/>

<%@ taglib uri="/WEB-INF/caisi-tag.tld" prefix="caisi" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ page import="ca.openosp.openo.commn.model.PreventionsLotNrs" %>
<%@ page import="ca.openosp.openo.commn.dao.PreventionsLotNrsDao" %>
<%@ page import="ca.openosp.openo.utility.SpringUtils" %>
<%@ page import="java.util.*" %>
<%@ page import="ca.openosp.OscarProperties" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%
    String orderby = request.getParameter("orderby") != null ? request.getParameter("orderby") : "prevention_type";
    String deepcolor = "#CCCCFF", weakcolor = "#EEEEFF";
%>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="java.sql.*, java.util.*, ca.openosp.*" buffer="none"
         errorPage="/errorpage.jsp" %>
<jsp:useBean id="apptMainBean" class="ca.openosp.AppointmentMainBean"
             scope="session"/>

<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>

<security:oscarSec roleName="<%=roleName$%>" objectName="_admin" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_admin");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>


<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <title><fmt:message key="admin.lotnrsearchresults.title"/></title>
        <link rel="stylesheet" href="<%= request.getContextPath() %>/web.css"/>
        <script LANGUAGE="JavaScript">
            <!--
            function setfocus() {
                document.searchlotnr.keyword.focus();
                document.searchlotnr.keyword.select();
            }

            function onsub() {
                var keyword = document.searchlotnr.keyword.value;
                document.searchlotnr.keyword.value = keyword.toLowerCase();
            }

            //-->
        </script>
    </head>
    <body onLoad="setfocus()" topmargin="0" leftmargin="0" rightmargin="0">
    <center>
        <table border="0" cellspacing="0" cellpadding="0" width="100%">
            <tr bgcolor="<%=Encode.forHtmlAttribute(String.valueOf(deepcolor))%>">
                <th><fmt:message key="admin.lotnrsearchresults.description"/></th>
            </tr>
        </table>
        <table cellspacing="0" cellpadding="0" width="100%" border="0"
               BGCOLOR="<%=Encode.forHtmlAttribute(String.valueOf(weakcolor))%>">
            <form method="post" action="lotnrsearchresults.jsp" name="searchlotnr"
                  onsubmit="return onsub();">
                <tr valign="top">
                    <td rowspan="2" align="right" valign="middle"><font
                            face="Verdana" color="#0000FF"><b><i><fmt:message key="admin.search.formSearchCriteria"/></i></b></font></td>
                    <td nowrap><font size="1" face="Verdana" color="#0000FF">
                        <input type="radio"
                                <%=Encode.forHtml(request.getParameter("search_mode").equals("search_prev")?"checked":"")%>
                               name="search_mode" value="search_prev"
                               onclick="document.forms['searchlotnr'].keyword.focus();"><fmt:message key="admin.lotnrsearch.prevention"/></font></td>
                    <td valign="middle" rowspan="2" ALIGN="left"><input type="text"
                                                                        NAME="keyword" SIZE="17" MAXLENGTH="100"
                                                                        value="<%=Encode.forHtmlAttribute(request.getParameter("keyword") != null ? request.getParameter("keyword") : "")%>">
                        <INPUT
                                TYPE="hidden" NAME="orderby" VALUE="prevention_type"> <INPUT
                                TYPE="hidden" NAME="dboperation" VALUE="lotnr_search_prevention">
                        <INPUT TYPE="hidden" NAME="limit1" VALUE="0"> <INPUT
                                TYPE="hidden" NAME="limit2" VALUE="10"> <INPUT
                                TYPE="SUBMIT" NAME="button"
                                VALUE=
                                    <fmt:message key="admin.lotnrsearchresults.btnSubmit"/>
                                        SIZE="17"></td>
                </tr>
            </form>
        </table>

        <table width="100%" border="0">
            <tr>
                <td align="left"><i><fmt:message key="admin.search.keywords"/></i>
                    : <%=Encode.forHtml(request.getParameter("keyword"))%>
                </td>
            </tr>
        </table>

        <CENTER>
            <table width="100%" cellspacing="2" cellpadding="2" border="0"
                   bgcolor="ivory">
                <tr bgcolor="<%=Encode.forHtmlAttribute(String.valueOf(deepcolor))%>">
                    <TH align="center" width="25%"><b><fmt:message key="admin.lotnrsearchresults.prevention"/></b></TH>
                    <TH align="center" width="25%"><b><fmt:message key="admin.lotnrsearchresults.lotnr"/> </b></TH>
                </tr>


                <%
                    PreventionsLotNrsDao PreventionsLotNrsDao = (PreventionsLotNrsDao) SpringUtils.getBean(PreventionsLotNrsDao.class);
                    int nItems = 0;
                    boolean bodd = false;
                    String keyword = request.getParameter("keyword").trim();
                    String prevention = keyword + "%";
                    //find active lot number records only
                    List<PreventionsLotNrs> p = PreventionsLotNrsDao.findPagedData(prevention, false, Integer.parseInt(request.getParameter("limit1")), Integer.parseInt(request.getParameter("limit2")));
                    for (PreventionsLotNrs pRec : p) {
                        bodd = bodd ? false : true;
                        nItems++;
                %>
                <tr bgcolor="<%=bodd?"white":weakcolor%>">
                    <td><%=Encode.forHtml(String.valueOf(pRec.getPreventionType()))%>
                    </td>
                    <td><a
                            href="lotnrdeleterecordhtm.jsp?prevention=<%=Encode.forUriComponent(String.valueOf(pRec.getPreventionType()))%>&lotnr=<%=Encode.forUriComponent(String.valueOf(pRec.getLotNr()))%>"><%=Encode.forHtml(String.valueOf(pRec.getLotNr()))%>
                    </a></td>
                </tr>
                <% }
                %>

            </table>
            <br>
            <%
                int nLastPage = 0, nNextPage = 0;
                String strLimit1 = request.getParameter("limit1");
                String strLimit2 = request.getParameter("limit2");

                nNextPage = Integer.parseInt(strLimit2) + Integer.parseInt(strLimit1);
                nLastPage = Integer.parseInt(strLimit1) - Integer.parseInt(strLimit2);
                if (nLastPage >= 0) {
            %> <a
                href="lotnrsearchresults.jsp?keyword=<%=Encode.forUriComponent(request.getParameter("keyword"))%>&search_mode=<%=Encode.forUriComponent(request.getParameter("search_mode"))%>&limit1=<%=Encode.forUriComponent(String.valueOf(nLastPage))%>&limit2=<%=Encode.forUriComponent(String.valueOf(strLimit2))%>"><fmt:message key="admin.lotnrsearchresults.btnLastPage"/></a> | <%
            }
            if (nItems == Integer.parseInt(strLimit2)) {
        %> <a
                href="lotnrsearchresults.jsp?keyword=<%=Encode.forUriComponent(request.getParameter("keyword"))%>&search_mode=<%=Encode.forUriComponent(request.getParameter("search_mode"))%>&limit1=<%=Encode.forUriComponent(String.valueOf(nNextPage))%>&limit2=<%=Encode.forUriComponent(String.valueOf(strLimit2))%>"><fmt:message key="admin.lotnrsearchresults.btnNextPage"/></a> <%
            }
        %>
            <p><fmt:message key="admin.lotnrsearchresults.msgClickForEditing"/></p>
            <br/>
            <a href="lotnraddrecordhtm.jsp">Add new Lot #</a>
        </center>
    </body>
</html>
