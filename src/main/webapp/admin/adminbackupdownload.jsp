<!DOCTYPE html>
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

<%@ page contentType="text/html; charset=UTF-8" errorPage="/errorpage.jsp" %>
<%@ page import="
    java.util.*,
    oscar.*,
    java.io.*,
    java.net.*,
    oscar.util.*,
    org.apache.commons.io.FileUtils"
    errorPage="/errorpage.jsp"
%>

<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    // --- Security check with null-safe session attributes ---
    String userRole = String.valueOf(session.getAttribute("userrole"));
    String userName = String.valueOf(session.getAttribute("user"));
    String roleName$ = userRole + "," + userName;
    boolean authed = true;
%>
<security:oscarSec
    roleName="<%= roleName$ %>"
    objectName="_admin,_admin.backup"
    rights="r"
    reverse="true">
  <% 
      authed = false;
      response.sendRedirect("../securityError.jsp?type=_admin&type=_admin.backup");
  %>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<%
    // --- Load backup_path from OscarProperties singleton ---
    Properties oscarVariables = OscarProperties.getInstance();
    String backuppath = oscarVariables.getProperty("backup_path");
    session.setAttribute("backupfilepath", backuppath);

    File dir = new File(backuppath);
    boolean exists = dir.exists();
%>

<html>
  <head>
    <title>Admin Backup Download</title>
    <link href="<%= request.getContextPath() %>/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/css/font-awesome.min.css" rel="stylesheet">
  </head>
  <body>
    <h3>
      <fmt:setBundle basename="oscarResources"/>
      <fmt:message key="admin.admin.btnAdminBackupDownload"/>
    </h3>

    <% if (exists) { %>
      <div class="well">
        <table class="table table-striped table-condensed">
          <thead>
            <tr>
              <th>File Name</th>
              <th>Size</th>
            </tr>
          </thead>
          <tbody>
          <%
              // Validate backup_path
              if (backuppath == null || backuppath.isEmpty()) {
                  request.setAttribute("errorMessage",
                      "backup_path is missing in properties; please configure it.");
                  request.getRequestDispatcher("/error.jsp").forward(request, response);
                  return;
              }

              // List, sort & display
              File[] contents = dir.listFiles();
              if (contents == null || contents.length == 0) {
                  throw new Exception(
                    "No files found in directory " + backuppath +
                    ". Please correct backup_path if necessary."
                  );
              }
              Arrays.sort(contents, new FileSortByDate());

              for (File f : contents) {
                  String name = f.getName();
                  if (f.isDirectory() ||
                      name.equals("BackupClient.class") ||
                      name.startsWith(".")) {
                      continue;
                  }

                  String encoded = URLEncoder.encode(name, "UTF-8");
                  long bytes = f.length();
                  String displaySize = FileUtils.byteCountToDisplaySize(bytes);

                  out.println(
                    "<tr>" +
                      "<td>" +
                        "<a href=\"" + request.getContextPath() +
                          "/servlet/BackupDownload?filename=" + encoded + "\">" +
                          name +
                        "</a>" +
                      "</td>" +
                      "<td align=\"right\" title=\"" + bytes + " bytes\">" +
                        displaySize +
                      "</td>" +
                    "</tr>"
                  );
              }
          %>
          </tbody>
        </table>
      </div>

    <% } else { %>
      <div class="alert alert-error">
        <strong>Warning!</strong>
        Your backup directory does not exist or path is invalid.
        Please check <i>backup_path</i> in your properties file.
      </div>
    <% } %>

  </body>
</html>