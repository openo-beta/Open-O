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

<%@ page errorPage="/errorpage.jsp"
         import="java.util.*, ca.openosp.openo.report.data.*" %>
<%@ page import="ca.openosp.openo.login.*" %>
<%@ page import="org.apache.commons.lang3.*" %>
<%@ page import="ca.openosp.openo.report.data.RptReportItem" %>
<%@ page import="ca.openosp.openo.report.data.RptTableFieldNameCaption" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%
    String reportId = request.getParameter("id") != null ? request.getParameter("id") : "0";
    String tableName = request.getParameter("tableName") != null ? request.getParameter("tableName") : "";
    String formTableName = request.getParameter("formTableName") != null ? request.getParameter("formTableName") : tableName;
    String configTableName = request.getParameter("configTableName") != null ? request.getParameter("configTableName") : formTableName;

// get form name
    String reportName = (new RptReportItem()).getReportName(reportId);

// get form parameters
    RptTableFieldNameCaption tableObj = new RptTableFieldNameCaption();

// add/delete action 
    if (request.getParameter("submit") != null && request.getParameter("submit").equals(" Add ")) {
        String strName = request.getParameter("name") != null ? request.getParameter("name") : "";
        String strCaption = request.getParameter("caption") != null ? request.getParameter("caption") : "";
        tableObj.setTable_name(tableName);
        tableObj.setName(strName);
        tableObj.setCaption(strCaption);
        tableObj.insertRecord();
    }
    if (request.getParameter("submit") != null && request.getParameter("submit").equals("Update")) {
        String strName = request.getParameter("name") != null ? request.getParameter("name") : "";
        String strCaption = request.getParameter("caption") != null ? request.getParameter("caption") : "";
        tableObj.setTable_name(tableName);
        tableObj.setName(strName);
        tableObj.setCaption(strCaption);
        tableObj.updateRecord();
    }

// get display data
    Vector vecTableField = new Vector();
    vecTableField = tableObj.getTableNameCaption(tableName);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <title>Report List</title>
        <LINK REL="StyleSheet" HREF="<%= request.getContextPath() %>/web.css" TYPE="text/css">
        <script language="JavaScript">

            <!--
            function setfocus() {
                this.focus();
                //document.forms[0].service_code.focus();
            }

            function onDelete() {
                ret = confirm("Are you sure you want to delete it?");
                return ret;
            }

            function onRestore() {
                ret = confirm("Are you sure you want to restore it?");
                return ret;
            }

            function goCaption() {
                //self.location.href = "reportFormCaption.jsp?id=<%=Encode.forUriComponent(String.valueOf(reportId))%>&tableName=<%=Encode.forUriComponent(String.valueOf(tableName))%>";
            }

            function goPage(id) {
                self.location.href = "reportFilter.jsp?id=" + id;
            }

            //-->

        </script>
    </head>
    <body bgcolor="ivory" onLoad="setfocus()" topmargin="0" leftmargin="0"
          rightmargin="0">
    <center></center>
    <table BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="100%">
        <tr BGCOLOR="#CCCCFF">
            <td><%=Encode.forHtml(String.valueOf(reportName))%> Caption</td>
            <td width="10%" align="right" nowrap>
                <% if ("demographic".equals(tableName)) {%> <a
                    href="reportFormDemoConfig.jsp?id=<%=Encode.forUriComponent(String.valueOf(reportId))%>&tableName=<%=Encode.forUriComponent(String.valueOf(tableName))%>&formTableName=<%=Encode.forUriComponent(String.valueOf(formTableName))%>&configTableName=<%=Encode.forUriComponent(String.valueOf(configTableName))%>">Back
                to the Configuration</a> <% } else {%> <a
                    href="reportFormConfig.jsp?id=<%=Encode.forUriComponent(String.valueOf(reportId))%>&tableName=<%=Encode.forUriComponent(String.valueOf(tableName))%>">Back
                to the Configuration</a> <% }%>
            </td>
        </tr>
    </table>

    <table width="100%" border="0" cellspacing="2" cellpadding="2">
        <tr>
            <td width="70%">

                <table width="100%" border="0" cellspacing="1" cellpadding="2">
                    <%
                        for (int i = 0; i < vecTableField.size(); i++) {
                            String color = i % 2 == 0 ? "#EEEEFF" : "";
                            String captionName = (String) vecTableField.get(i);
                            String[] strTemp = captionName.split("\\|");
                            String fieldName = "";
                            String fieldCaption = "";
                            String action = " Add ";
                            if (strTemp.length > 1) {
                                fieldName = strTemp[1];
                                fieldCaption = strTemp[0].trim();
                            }
                            if (fieldCaption.length() > 1) {
                                color = "gold";
                                action = "Update";
                            }
                    %>
                    <form method="post" name="baseurl<%=i%>"
                          action="reportFormCaption.jsp">
                        <tr bgcolor="<%=Encode.forHtmlAttribute(String.valueOf(color))%>">
                            <td width="50%"><input type="text" name="caption"
                                                   value="<%=Encode.forHtmlAttribute(String.valueOf(fieldCaption))%>" size="36"/></td>
                            <td width="30%" nowrap><%=Encode.forHtml(String.valueOf(fieldName))%>
                            </td>
                            <td align="center"><input type="submit" name="submit"
                                                      value="<%=Encode.forHtmlAttribute(String.valueOf(action))%>"/></td>
                            <input type="hidden" name="name" value="<%=Encode.forHtmlAttribute(String.valueOf(fieldName))%>">
                            <input type="hidden" name="id" value="<%=Encode.forHtmlAttribute(String.valueOf(reportId))%>">
                            <input type="hidden" name="tableName" value="<%=Encode.forHtmlAttribute(String.valueOf(tableName))%>">
                            <input type="hidden" name="formTableName" value="<%=Encode.forHtmlAttribute(String.valueOf(formTableName))%>">
                            <input type="hidden" name="configTableName"
                                   value="<%=Encode.forHtmlAttribute(String.valueOf(configTableName))%>">
                        </tr>
                    </form>
                    <% } %>
                </table>
            </td>
            <td></td>
        </tr>
    </table>


    </body>
</html>
