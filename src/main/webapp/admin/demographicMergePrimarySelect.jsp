<%--
    Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
    This software is published under the GPL GNU General Public License.

    demographicMergePrimarySelect.jsp
    Purpose: Step 2 of the demographic merge workflow. Renders the list of selected
             patients (loaded by DemographicMergeAction) and lets the user pick which
             one is the primary. Submits to DemographicMerge.do?method=merge.
    Request attributes (set by DemographicMergeAction):
        demographics - List<Demographic> the selected patients to choose from
        selectedIds  - List<Integer> the demographic_no values that were selected
    @since 2026-03-25
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>

<security:oscarSec roleName="${sessionScope.userrole},${sessionScope.user}" objectName="_demographic" rights="w" reverse="true">
    <c:redirect url="/securityError.jsp?type=_demographic"/>
</security:oscarSec>
<security:oscarSec roleName="${sessionScope.userrole},${sessionScope.user}" objectName="_admin" rights="w" reverse="true">
    <c:redirect url="/securityError.jsp?type=_admin"/>
</security:oscarSec>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Select Primary Record — Demographic Merge</title>
    <link href="${pageContext.request.contextPath}/library/bootstrap/5.0.2/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { padding-top: 1rem; }
        .primary-row { background-color: #cfe2ff !important; }
        .table th { white-space: nowrap; }
        .legend { font-size: .85rem; color: #6c757d; margin-bottom: .75rem; }
    </style>
</head>
<body>
<div class="container-fluid" style="max-width:1100px;">

    <h4 class="mt-3 mb-1">Select Primary Record</h4>
    <p class="legend">
        Choose which record will be the <strong>primary (A)</strong>. Its identity fields (name, DOB, HIN, address) will be used for the new merged record. All clinical data from every record below will be copied into the merged record.
    </p>

    <form method="post" action="${pageContext.request.contextPath}/admin/DemographicMerge.do"
          id="mergeForm" onsubmit="return buildMergeForm()">
        <input type="hidden" name="method" value="merge">

        <table class="table table-sm table-striped table-bordered" id="selectionTable">
            <thead class="table-light">
            <tr>
                <th class="text-center">Primary</th>
                <th>ID</th>
                <th>Last Name</th>
                <th>First Name</th>
                <th>DOB</th>
                <th>Sex</th>
                <th>Roster</th>
                <th>HIN</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="demo" items="${demographics}">
                <tr data-demo-no="${demo.demographicNo}">
                    <td class="text-center">
                        <input type="radio" name="primaryRadio" value="${demo.demographicNo}"
                               onchange="highlightPrimary(this)">
                    </td>
                    <td><e:forHtml value="${demo.demographicNo}"/></td>
                    <td><e:forHtml value="${demo.lastName}"/></td>
                    <td><e:forHtml value="${demo.firstName}"/></td>
                    <td><e:forHtml value="${demo.formattedDob}"/></td>
                    <td><e:forHtml value="${demo.sex}"/></td>
                    <td><e:forHtml value="${demo.rosterStatus}"/></td>
                    <td><e:forHtml value="${demo.hin}"/></td>
                    <td><e:forHtml value="${demo.patientStatus}"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <div class="d-flex gap-2 mt-3">
            <button type="submit" class="btn btn-danger">Merge Records</button>
            <a href="${pageContext.request.contextPath}/admin/DemographicMerge.do" class="btn btn-outline-secondary">
                &larr; Back
            </a>
        </div>
    </form>

</div>

<script>
    // All selected IDs passed from action — used to build secondary list on submit
    const allSelectedIds = [
        <c:forEach var="id" items="${selectedIds}" varStatus="s">${id}<c:if test="${!s.last}">,</c:if></c:forEach>
    ];

    function highlightPrimary(radio) {
        document.querySelectorAll('#selectionTable tbody tr').forEach(function (row) {
            row.classList.remove('primary-row');
        });
        radio.closest('tr').classList.add('primary-row');
    }

    function buildMergeForm() {
        const selected = document.querySelector('input[name="primaryRadio"]:checked');
        if (!selected) {
            alert('Please select a primary record.');
            return false;
        }

        if (!confirm('Merge these records? The original records will be marked as MERGED.')) {
            return false;
        }

        const form   = document.getElementById('mergeForm');
        const primary = selected.value;

        const primaryInput   = document.createElement('input');
        primaryInput.type    = 'hidden';
        primaryInput.name    = 'primaryDemographicNo';
        primaryInput.value   = primary;
        form.appendChild(primaryInput);

        allSelectedIds.forEach(function (id) {
            if (String(id) !== String(primary)) {
                const input = document.createElement('input');
                input.type  = 'hidden';
                input.name  = 'secondaryDemographicNo';
                input.value = id;
                form.appendChild(input);
            }
        });

        return true;
    }
</script>
<script src="${pageContext.request.contextPath}/library/bootstrap/5.0.2/js/bootstrap.bundle.min.js"></script>
</body>
</html>
