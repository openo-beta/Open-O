<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="ca.openosp.openo.olis.OLISNomenclatureImport2Action,
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

    OLISNomenclatureImport2Action.ImportReport resultReport = (OLISNomenclatureImport2Action.ImportReport)
            request.getAttribute("resultReport");
    OLISNomenclatureImport2Action.ImportReport requestReport = (OLISNomenclatureImport2Action.ImportReport)
            request.getAttribute("requestReport");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String xlsxFileName = (String) request.getAttribute("xlsxFileName");
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>OLIS — Import Nomenclature</title>
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

<h2>OLIS — Import Nomenclature</h2>
<p class="subtitle">Refresh the local OLIS result + request nomenclature tables from an official eHealth Ontario XLSX distribution.</p>

<% if (errorMessage != null) { %>
<div class="panel error">
    <strong>Import failed.</strong><br/>
    <%= Encode.forHtml(errorMessage) %>
</div>
<% } %>

<% if (resultReport != null && requestReport != null) { %>
<div class="panel success">
    <strong>Import complete.</strong>
    <% if (xlsxFileName != null) { %>
        <span style="color: #666;">— from <code><%= Encode.forHtml(xlsxFileName) %></code></span>
    <% } %>
    <table class="summary">
        <thead>
        <tr><th></th><th>Added</th><th>Updated</th><th>Deprecated</th><th>Total touched</th></tr>
        </thead>
        <tbody>
        <tr>
            <th>Test Result codes</th>
            <td class="num"><%= resultReport.getAdded() %></td>
            <td class="num"><%= resultReport.getUpdated() %></td>
            <td class="num"><%= resultReport.getDeprecated() %></td>
            <td class="num"><strong><%= resultReport.getTotal() %></strong></td>
        </tr>
        <tr>
            <th>Test Request codes</th>
            <td class="num"><%= requestReport.getAdded() %></td>
            <td class="num"><%= requestReport.getUpdated() %></td>
            <td class="num"><%= requestReport.getDeprecated() %></td>
            <td class="num"><strong><%= requestReport.getTotal() %></strong></td>
        </tr>
        </tbody>
    </table>
</div>
<% } %>

<div class="panel">
    <form action="<%= request.getContextPath() %>/olis/NomenclatureImport.do" method="POST"
          enctype="multipart/form-data">
        <p>
            <label for="nomenclatureFile"><strong>Official OLIS Nomenclatures distribution (.xlsx):</strong></label><br/>
            <input id="nomenclatureFile" type="file" name="nomenclatureFile" accept=".xlsx" required/>
        </p>
        <p class="note">
            Download the latest release from
            <a href="https://ehealthontario.on.ca/en/OLIS-nomenclature/" target="_blank" rel="noopener noreferrer">eHealth Ontario OLIS Nomenclature</a>.
            The file looks like <code>OLIS Nomenclatures V{X.YY}_PROD.xlsx</code>.
            <br/><br/>
            <strong>Each release has a ~7-day "review and remap by" deadline</strong> before non-conforming senders begin to have OLIS messages rejected. Run this import once per release.
            <br/><br/>
            The import upserts both Test Result and Test Request nomenclature tables. Codes whose <em>Validation Status Indicator</em> or <em>Workflow Status Indicator</em> is no longer ACTIVE / RELEASED are marked deprecated; the autocomplete on OLIS Search hides deprecated codes automatically.
        </p>
        <p>
            <input type="submit" value="Import"/>
        </p>
    </form>
</div>

</body>
</html>
