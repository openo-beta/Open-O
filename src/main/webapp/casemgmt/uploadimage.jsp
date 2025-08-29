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
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_demographic" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_demographic");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>


<%@ page import="ca.openosp.openo.casemgmt.model.*" %>
<%@ page import="ca.openosp.openo.casemgmt.web.formbeans.*" %>

<%
    if (application.getAttribute("javax.servlet.context.tempdir") == null) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        application.setAttribute("javax.servlet.context.tempdir", new java.io.File(tmpDir));
    }
%>
<head>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/global.js"></script>
    <title>Client Image Manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/web.css"/>
    <script>
        // If page is successful, reload the client E-Chart and close the profile picture upload page
        function init_page() {
            <%
                if(request.getAttribute("success") != null)
                {
                    %>
            opener.location.reload();
            self.close();
            <%
        }
    %>
        }

        // Makes sure that a picture has been uploaded to the submission box
        function onPicUpload() {
            var file = document.getElementById("clientImage").files[0];
            if (!file) {
                alert("Please specify a picture path and name for the upload.");
                return false;
            }
            return true;
        }
    </script>
</head>
<body bgcolor="#C4D9E7" bgproperties="fixed"
      onLoad="self.focus();init_page();" topmargin="0" leftmargin="0"
      rightmargin="0">
<table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr bgcolor="#486ebd">
        <th align=CENTER NOWRAP><font face="Helvetica" color="#FFFFFF">Client
            Image Manager</font></th>
    </tr>
</table>
<table BORDER="0" CELLPADDING="1" CELLSPACING="0" WIDTH="100%"
       BGCOLOR="#C4D9E7">

    <form action="${pageContext.request.contextPath}/ClientImage.do" enctype="multipart/form-data"
               method="post" onsubmit="return onPicUpload();">
        <input type="hidden" name="method" value="saveImage"/>
        <%
            request.getSession().setAttribute("clientId", request.getParameter("demographicNo"));
        %>
        <tr valign="top">
            <td rowspan="2" ALIGN="right" valign="middle"><font
                    face="Verdana" color="#0000FF"><b><i>Add Image </i></b></font></td>


            <td valign="middle" rowspan="2" ALIGN="left">
                <input type="file" name="clientImage" id="clientImage" size="30" accept="*.gif,*.jpg"/><br>
                <input type="submit" value="Upload" /></td>
        </tr>
    </form>
</table>
<br>
Attention:
<br>
Only gif and jpg image type are allowed for the client photo uploading.
<br>
<br>


<form><input type="button" name="Button" value="cancel"
             onclick="self.close();"/></form>


</body>
