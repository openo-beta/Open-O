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
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_report,_admin.reporting" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_report&type=_admin.reporting");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<%
    String curUser_no = (String) session.getAttribute("user");
    String deepcolor = "#CCCCFF", weakcolor = "#EEEEFF";

    String strLimit1 = "0";
    String strLimit2 = "5000";
    if (request.getParameter("limit1") != null) strLimit1 = request.getParameter("limit1");
    if (request.getParameter("limit2") != null) strLimit2 = request.getParameter("limit2");

    String startDate = null, endDate = null;
    if (request.getParameter("startDate") != null) startDate = request.getParameter("startDate");
    if (request.getParameter("endDate") != null) endDate = request.getParameter("endDate");
%>
<%@ page import="java.util.*, java.sql.*" errorPage="/errorpage.jsp" %>

<jsp:useBean id="providerNameBean" class="java.util.Properties" scope="page"/>
<%@ page import="ca.openosp.openo.utility.SpringUtils" %>
<%@ page import="ca.openosp.openo.PMmodule.dao.ProviderDao" %>
<%@ page import="ca.openosp.openo.commn.model.Provider" %>
<%@ page import="ca.openosp.openo.commn.dao.forms.FormsDao" %>
<%@ page import="ca.openosp.openo.util.ConversionUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%
    ProviderDao providerDao = SpringUtils.getBean(ProviderDao.class);
    FormsDao formsDao = SpringUtils.getBean(FormsDao.class);
%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.title"/></title>
        <link rel="stylesheet" href="<%= request.getContextPath() %>/css/receptionistapptstyle.css">
        <script language="JavaScript">
            <!--

            //-->
        </SCRIPT>
        <!--base target="pt_srch_main"-->
    </head>
    <body onLoad="setfocus()" topmargin="0" leftmargin="0" rightmargin="0">

    <table border="0" cellspacing="0" cellpadding="0" width="100%">
        <tr bgcolor="<%=Encode.forHtmlAttribute(String.valueOf(deepcolor))%>">
            <th><font face="Helvetica"><fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.msgEDDList"/></font></th>
        </tr>
        <tr>
            <td align="right"><input type="button" name="Button"
                                     value="<fmt:setBundle basename="oscarResources"/><fmt:message key="global.btnPrint"/>"
                                     onClick="window.print()"> <input type="button" name="Button"
                                                                      value="<fmt:setBundle basename="oscarResources"/><fmt:message key="global.btnCancel"/>"
                                                                      onClick="window.close()">
                </th>
        </tr>
    </table>

    <CENTER>
        <table width="100%" border="0" bgcolor="silver" cellspacing="2"
               cellpadding="2">
            <tr bgcolor='<%=Encode.forHtmlAttribute(String.valueOf(deepcolor))%>'>
                <TH align="center" width="6%" nowrap><b><fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.msgEDD"/></b></TH>
                <TH align="center" width="20%"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.msgName"/> </b></TH>
                <!--TH align="center" width="20%"><b>Demog' No </b></TH-->
                <TH align="center" width="9%"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.msgDOB"/></b></TH>
                <TH align="center" width="5%"><b>G</b><font size="-2">ravida</font></TH>
                <TH align="center" width="5%"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.msgTerm"/></b></TH>
                <TH align="center" width="10%"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.msgPhone"/></b></TH>
                <TH align="center" width="10%"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.msLanguage"/></b></TH>
                <TH align="center" width="8%"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.msPHN"/></b></TH>
                <TH align="center" width="20%"><b>Doula</b></TH>
                <TH align="center"><b>Doula#</b></TH>
            </tr>
            <%
                for (Provider p : providerDao.getActiveProviders()) {
                    providerNameBean.setProperty(p.getProviderNo(), new String(p.getFormattedName()));
                }

                Properties demoProp = new Properties();


                boolean bodd = false;
                int nItems = 0;

                for (Object[] result : formsDao.selectBcFormAr(startDate, endDate, Integer.parseInt(strLimit1), Integer.parseInt(strLimit2))) {

                    String demographicNo = ((Integer) result[0]).toString();
                    String cEDD = ConversionUtils.toDateString((java.util.Date) result[1]);
                    String surname = (String) result[2];
                    String givenName = (String) result[3];
                    String pg1_ageAtEDD = (String) result[4];
                    String dob = (String) result[5];
                    String langPref = (String) result[6];
                    String phn = (String) result[7];
                    String gravida = (String) result[8];
                    String term = (String) result[9];
                    String phone = (String) result[10];
                    String doula = (String) result[12];
                    String doulaNo = (String) result[13];

                    if (demoProp.containsKey(demographicNo)) continue;
                    else demoProp.setProperty(demographicNo, "1");
                    bodd = bodd ? false : true; //for the color of rows
                    nItems++;
            %>
            <tr bgcolor="<%=bodd?weakcolor:"white"%>">
                <td align="center" nowrap><%=Encode.forHtml(String.valueOf(cEDD != null ? cEDD.replace('-', '/') : "----/--/--"))%>
                </td>
                <td><%=Encode.forHtml(String.valueOf(surname + ", " + givenName))%>
                </td>
                <!--td align="center" ><%=Encode.forHtml(String.valueOf(demographicNo))%> </td-->
                <td><%=Encode.forHtml(String.valueOf(dob != null ? dob : ""))%>
                </td>
                <td><%=Encode.forHtml(String.valueOf(gravida != null ? gravida : ""))%>
                </td>
                <td><%=Encode.forHtml(String.valueOf(term != null ? term : ""))%>
                </td>
                <td nowrap><%=Encode.forHtml(String.valueOf(phone))%>
                </td>
                <!--td><%--=reportMainBean.getString(rs,"c_phyMid")--%><%--=providerNameBean.getProperty(reportMainBean.getString(rs,"provider_no"), "")--%></td-->
                <td><%=Encode.forHtml(String.valueOf(langPref))%>
                </td>
                <td><%=Encode.forHtml(String.valueOf(phn))%>
                </td>
                <td><%=Encode.forHtml(String.valueOf(doula))%>
                </td>
                <td><%=Encode.forHtml(String.valueOf(doulaNo))%>
                </td>
            </tr>
            <%
                }
            %>

        </table>
        <br>
                <%
  int nLastPage=0,nNextPage=0;
  nNextPage=Integer.parseInt(strLimit2)+Integer.parseInt(strLimit1);
  nLastPage=Integer.parseInt(strLimit1)-Integer.parseInt(strLimit2);
  if(nLastPage>=0) {
%> <a
            href="reportbcedblist.jsp?startDate=<%=Encode.forUriComponent(request.getParameter("startDate"))%>&endDate=<%=Encode.forUriComponent(request.getParameter("endDate"))%>&limit1=<%=Encode.forUriComponent(String.valueOf(nLastPage))%>&limit2=<%=Encode.forUriComponent(String.valueOf(strLimit2))%>"><fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.msgLastPage"/></a> | <%
  }
  if(nItems==Integer.parseInt(strLimit2)) {
%> <a
            href="reportbcedblist.jsp?startDate=<%=Encode.forUriComponent(request.getParameter("startDate"))%>&endDate=<%=Encode.forUriComponent(request.getParameter("endDate"))%>&limit1=<%=Encode.forUriComponent(String.valueOf(nNextPage))%>&limit2=<%=Encode.forUriComponent(String.valueOf(strLimit2))%>">
        <fmt:setBundle basename="oscarResources"/><fmt:message key="report.reportnewdblist.msgNextPage"/></a> <%
}
%>

    </body>
</html>
