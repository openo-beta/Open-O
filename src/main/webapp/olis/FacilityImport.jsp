<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="ca.openosp.openo.olis.OLISFacilityImport2Action,
                 ca.openosp.openo.utility.LoggedInInfo,
                 ca.openosp.openo.managers.SecurityInfoManager,
                 ca.openosp.openo.utility.SpringUtils,
                 org.owasp.encoder.Encode" %>
<%
    LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
    SecurityInfoManager sim = SpringUtils.getBean(SecurityInfoManager.class);
    if (!sim.hasPrivilege(loggedInInfo, "_admin", "w", null)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }

    OLISFacilityImport2Action.ImportReport labReport = (OLISFacilityImport2Action.ImportReport)
            request.getAttribute("labReport");
    OLISFacilityImport2Action.ImportReport sccReport = (OLISFacilityImport2Action.ImportReport)
            request.getAttribute("sccReport");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String xlsxFileName = (String) request.getAttribute("xlsxFileName");
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>OLIS Import Lab/SCC Roster</title>
    <link rel="stylesheet" type="text/css"
          href="<%= request.getContextPath() %>/share/css/OscarStandardLayout.css">
    <style>
        body { font-family: arial, sans-serif; padding: 20px; max-width: 800px; }
        h2 { margin-bottom: 4px; }
        .subtitle { color: #666; margin-top: 0; }
        .panel { border: 1px solid #ccc; border-radius: 4px; padding: 16px; margin: 16px 0; background: #fafafa; }
        .panel.success { border-color: #4a7; background: #f3faf5; }
        .panel.error { border-color: #c33; background: #fef5f5; }
        table.summary { border-collapse: collapse; margin-top: 8px; }
        table.summary th, table.summary td { padding: 4px 12px; text-align: left; border-bottom: 1px solid #ddd; font-size: 14px; }
        table.summary th { background: #eaeaea; }
        td.num { text-align: right; font-variant-numeric: tabular-nums; }
        .note { color: #666; font-size: 13px; line-height: 1.4; }
    </style>
</head>
<body>

<jsp:include page="/images/spinner.jsp" flush="true"/>

<h2>OLIS Import Lab/SCC Roster</h2>
<p class="subtitle">Refresh the local OLIS Laboratory + Specimen Collection Centre roster from the official eHealth Ontario Lab/SCC Extract.</p>

<% if (errorMessage != null) { %>
<div class="panel error">
    <strong>Import failed.</strong><br/>
    <%= Encode.forHtml(errorMessage) %>
</div>
<% } %>

<% if (labReport != null && sccReport != null) { %>
<div class="panel success">
    <strong>Import complete.</strong>
    <% if (xlsxFileName != null) { %>
        <span style="color: #666;">from <code><%= Encode.forHtml(xlsxFileName) %></code></span>
    <% } %>
    <table class="summary">
        <thead>
        <tr><th></th><th>Added</th><th>Updated</th><th>Total touched</th></tr>
        </thead>
        <tbody>
        <tr>
            <th>Laboratories</th>
            <td class="num"><%= labReport.getAdded() %></td>
            <td class="num"><%= labReport.getUpdated() %></td>
            <td class="num"><strong><%= labReport.getTotal() %></strong></td>
        </tr>
        <tr>
            <th>Specimen Collection Centres</th>
            <td class="num"><%= sccReport.getAdded() %></td>
            <td class="num"><%= sccReport.getUpdated() %></td>
            <td class="num"><strong><%= sccReport.getTotal() %></strong></td>
        </tr>
        </tbody>
    </table>
    <p class="note" style="margin-top: 8px;">
        Rows present before the import but absent from this file are now marked INACTIVE; they no longer surface in the OLIS Search and OLIS Preferences pickers.
    </p>
</div>
<% } %>

<div class="panel">
    <form action="<%= request.getContextPath() %>/olis/FacilityImport.do" method="POST"
          enctype="multipart/form-data" onsubmit="ShowSpin(true);">
        <p>
            <label for="facilityFile"><strong>Official Lab/SCC Extract (.xlsx):</strong></label><br/>
            <input id="facilityFile" type="file" name="facilityFile" accept=".xlsx" required/>
        </p>
        <p class="note">
            Download the latest extract from
            <a href="https://ehealthontario.on.ca/en/support/lab-results/olis-whats-new/olis-client-support" target="_blank" rel="noopener noreferrer">eHealth Ontario OLIS Client Support</a>.
            The file looks like <code>Lab and SCC extract.xlsx</code>.
            <br/><br/>
            The extract is the canonical list of Ontario laboratories and specimen-collection centres OLIS will accept in query parameters such as Reporting Laboratory (@ZBR.4), Performing Laboratory (@ZBR.6), and Specimen Collector (@ZBR.3). A typical extract contains ~270 Laboratories and ~1,000 Specimen Collection Centres.
            <br/><br/>
            Each row is upserted on the natural key (facility class, licence number). Rows present before this import but absent from the new file are marked INACTIVE, so the OLIS Search and OLIS Preferences typeahead pickers stop suggesting them.
            <br/><br/>
            Imports usually complete in a few seconds. Don't navigate away while the import is running.
        </p>
        <p>
            <input type="submit" value="Import"/>
        </p>
    </form>
</div>

</body>
</html>
