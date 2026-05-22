<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_admin,_admin.misc" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_admin&type=_admin.misc");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<%@ page import="java.util.List" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="ca.openosp.openo.hospitalReportManager.model.HRMSendingFacility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    HRMSendingFacility editing = (HRMSendingFacility) request.getAttribute("editing");
    @SuppressWarnings("unchecked")
    List<HRMSendingFacility> facilities = (List<HRMSendingFacility>) request.getAttribute("facilities");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String actionUrl = request.getContextPath() + "/hospitalReportManager/HRMSendingFacility.do";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>HRM Sending Facilities</title>
    <link rel="stylesheet" type="text/css"
          href="<%=request.getContextPath()%>/library/bootstrap/3.0.0/css/bootstrap.min.css">
    <link rel="stylesheet"
          href="<%=request.getContextPath()%>/library/DataTables-1.10.12/media/css/jquery.dataTables.min.css">
    <script src="<%=request.getContextPath()%>/library/jquery/jquery-3.6.4.min.js"></script>
    <script src="<%=request.getContextPath()%>/library/DataTables/datatables.min.js"></script>
    <script>
        jQuery(document).ready(function () {
            jQuery('#tblFacility').DataTable({
                "order": [],
                "bPaginate": false
            });
        });
    </script>
</head>
<body>
<div class="table-responsive">
    <div class="col-sm-12">
        <h4>HRM Sending Facilities</h4>
        <p>
            Maps the <code>&lt;SendingFacility&gt;</code> ID in incoming HRM reports
            to a human-readable facility name. Used when displaying reports.
        </p>

        <% if (errorMessage != null) { %>
            <div class="alert alert-danger"><%=Encode.forHtml(errorMessage)%></div>
        <% } %>

        <form method="post" action="<%=actionUrl%>">
            <input type="hidden" name="method" value="save"/>
            <input type="hidden" name="id"
                   value="<%=editing != null ? Encode.forHtmlAttribute(String.valueOf(editing.getId())) : ""%>"/>
            <fieldset>
                <div class="control-group">
                    <label class="control-label">Sending Facility ID:</label>
                    <div class="controls">
                        <input type="text" name="sendingFacilityId" class="form-control input-normal"
                               maxlength="50" required
                               value="<%=editing != null ? Encode.forHtmlAttribute(editing.getSendingFacilityId()) : ""%>"/>
                        <span class="help-block">
                            The exact string used in the HRM XML <code>&lt;SendingFacility&gt;</code> element
                            (e.g. <code>MIS1</code>, <code>0911</code>).
                        </span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">Facility Name:</label>
                    <div class="controls">
                        <input type="text" name="facilityName" class="form-control"
                               maxlength="255" required
                               value="<%=editing != null ? Encode.forHtmlAttribute(editing.getFacilityName()) : ""%>"/>
                        <span class="help-block">
                            Human-readable name shown alongside the ID on HRM reports.
                        </span>
                    </div>
                </div>
                <div class="control-group">
                    <input type="submit" class="btn btn-primary"
                           value="<%=editing != null ? "Save" : "Add"%>"/>
                    <% if (editing != null) { %>
                        <a class="btn btn-default"
                           href="<%=actionUrl%>">Cancel</a>
                    <% } %>
                </div>
            </fieldset>
        </form>

        <hr/>

        <table id="tblFacility" class="table table-striped table-hover table-condensed">
            <thead>
            <tr>
                <th></th>
                <th>ID</th>
                <th>Sending Facility ID</th>
                <th>Facility Name</th>
            </tr>
            </thead>
            <tbody>
            <% if (facilities != null) {
                for (HRMSendingFacility f : facilities) { %>
                <tr>
                    <td>
                        <a href="<%=actionUrl%>?method=delete&id=<%=Encode.forHtmlAttribute(String.valueOf(f.getId()))%>"
                           onclick="return confirm('Delete sending facility <%=Encode.forJavaScript(f.getSendingFacilityId())%>?');">
                            <img src="<%=request.getContextPath()%>/images/icons/101.png" alt="delete">
                        </a>
                    </td>
                    <td>
                        <a href="<%=actionUrl%>?id=<%=Encode.forHtmlAttribute(String.valueOf(f.getId()))%>">
                            <%=Encode.forHtml(String.valueOf(f.getId()))%>
                        </a>
                    </td>
                    <td><%=Encode.forHtml(f.getSendingFacilityId())%></td>
                    <td><%=Encode.forHtml(f.getFacilityName())%></td>
                </tr>
            <%  }
            } %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
