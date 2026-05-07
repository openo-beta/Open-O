<%--

    Copyright (c) 2006-. OSCARservice, OpenSoft System. All Rights Reserved.
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

--%>
<%

    String curUser_no = (String) session.getAttribute("user");
%>
<%@ page import="java.math.*, java.util.*, java.sql.*, ca.openosp.*, java.net.*" errorPage="/errorpage.jsp" %>
<%@page import="ca.openosp.openo.utility.SpringUtils" %>
<%@page import="ca.openosp.openo.commn.dao.BillingServiceDao" %>
<%@page import="ca.openosp.openo.commn.model.BillingService" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%
    BillingServiceDao billingServiceDao = SpringUtils.getBean(BillingServiceDao.class);
%>
<html>
<head>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
    <title>Billing Summary</title>
    <script LANGUAGE="JavaScript">
        <!--
        function CodeAttach(File0, File1, File2) {

            <%
            // formIndex and elementName together identify a form field. They are validated
            // against strict allowlists so the access path below is emitted from a fixed
            // server-side template — no caller-supplied text reaches the JS source.
            String formIndex = request.getParameter("formIndex");
            String elementName = request.getParameter("elementName");
            boolean hasStructuredTarget = ("0".equals(formIndex) || "1".equals(formIndex))
                    && elementName != null && elementName.matches("^[a-zA-Z_$][a-zA-Z0-9_$]*$");
            if (hasStructuredTarget) {
            %>
            self.opener.document.forms[<%=formIndex%>].elements['<%=Encode.forJavaScript(elementName)%>'].value = File0;
            <% } else { %>
            self.opener.document.serviceform.xml_other1.value = File0;
            self.opener.document.serviceform.xml_other2.value = File1;
            self.opener.document.serviceform.xml_other3.value = File2;
            <% } %>
            self.close();
        }

        -->
    </script>

</head>
<body>
<%
    if (request.getParameter("update").equals("Confirm")) {


        String temp = "";
        String[] param = new String[10];
        param[0] = "";
        param[1] = "";
        param[2] = "";

        int Count = 0;

        for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); ) {
            temp = e.nextElement().toString();
            if (temp.indexOf("code_") == -1) continue;
            param[Count] = temp.substring(5).toUpperCase(); // + " |" + request.getParameter("codedesc_" + temp.substring(5));
            Count = Count + 1;

        }

        if (Count == 1) {
            param[1] = "";
            param[2] = "";
        }
        if (Count == 2) {
            param[2] = "";

        }

        if (Count == 0) {
%>
<p>No input selected</p>
<input type="button" name="back" value="back"
       onClick="javascript:history.go(-1);return false;">
<%
} else {
%>
<script LANGUAGE="JavaScript">
    <!--
    CodeAttach('<%=Encode.forJavaScript(String.valueOf(param[0]))%>', '<%=Encode.forJavaScript(String.valueOf(param[1]))%>', '<%=Encode.forJavaScript(String.valueOf(param[2]))%>');
    -->

</script>
<%
    }
} else {
%>
<%

    String code = request.getParameter("update");
    code = code.substring(code.length() - 5);

    int rowsAffected = 0;

    for (BillingService bs : billingServiceDao.findByServiceCode(code)) {
        bs.setDescription(request.getParameter(code));
        billingServiceDao.merge(bs);
        rowsAffected++;
    }
%>
<%
%>
<p>
<h1>Successful Addition of a billing Record.</h1>
</p>
<script LANGUAGE="JavaScript">
    history.go(-1);
    return false;
    self.opener.refresh();
</script>
<% } %>

</body>
</html>
