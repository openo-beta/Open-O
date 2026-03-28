<%--
    Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
    This software is published under the GPL GNU General Public License.

    demographicMergeRecord.jsp
    Purpose: Single-page demographic merge/unmerge workflow.
             Merge mode   — results table shows a checkbox (select) and a Primary radio per row;
                            form submits directly to DemographicMerge.do?method=merge.
             Unmerge mode — results table shows a single radio per row;
                            form submits to DemographicMerge.do?method=unmerge.
             Checkbox and radio are suppressed for records with patientStatus=MERGED or IN
             (not eligible to participate in a new merge).
    Request attributes (set by DemographicMergeAction):
        demoList        - List<Demographic> search results (null = form not yet submitted)
        keyword         - String search term (null = form not yet submitted; "" = search-all)
        searchMode      - String search_name | search_phone | search_dob | search_address | search_hin
        mode            - String merge | unmerge
        unmergeMode     - Boolean true when in unmerge mode
        outcome         - String success | failure | successUnMerge (from prior redirect)
        mergedDemoNo    - String demographic_no of newly created merged record C (on success redirect)
        offset          - int pagination offset
        limit           - int page size
        resultCount     - int number of results
        mergeEventMap   - Map<Integer,DemographicMergeEvent> keyed by merged demographic_no (unmerge mode only)
        mergeSourcesMap - Map<Integer,List<Demographic>> keyed by merged demographic_no; primary first, then secondaries (unmerge mode only)
    @since 2026-03-25
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
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
    <title><fmt:setBundle basename="oscarResources"/><fmt:message key="admin.admin.mergeRec"/></title>
    <link href="${pageContext.request.contextPath}/library/bootstrap/5.0.2/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { padding-top: 1rem; }
        a { text-decoration: none; }
        a:hover { text-decoration: underline; }
        .search-bar { background: #f8f9fa; border-radius: 6px; padding: 1rem 1.25rem; margin-bottom: 1.25rem; }
        .results-header { font-size: .85rem; color: #6c757d; margin-bottom: .5rem; }
        .table th { white-space: nowrap; }
        .table th.sortable { cursor: pointer; user-select: none; }
        .table th.sortable:hover { background-color: #e9ecef; }
        .sort-icon { font-size: .75rem; margin-left: 3px; color: #adb5bd; }
        .sort-icon.active { color: #0d6efd; }
        .pagination-bar { margin-top: .75rem; }
        .ineligible-row { color: #adb5bd; font-style: italic; }
        /* Bootstrap collapse sets display:block, which breaks <tr> layout — override */
        tr.collapse.show { display: table-row; }
        .history-toggle { transition: transform 0.2s; }
        .history-toggle[aria-expanded="true"] { transform: rotate(90deg); }
    </style>
</head>
<body>
<div class="container-fluid" style="max-width:1200px;">

    <h4 class="mt-3 mb-3">
        <fmt:setBundle basename="oscarResources"/>
        <fmt:message key="admin.admin.mergeRec"/>
    </h4>

    <%-- Outcome alerts --%>
    <c:choose>
        <c:when test="${outcome eq 'success'}">
            <%-- BS5 success modal — auto-opened by JS below --%>
            <div class="modal fade" id="mergeSuccessModal" tabindex="-1" aria-labelledby="mergeSuccessModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header bg-success text-white">
                            <h5 class="modal-title" id="mergeSuccessModalLabel">Merge Successful</h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <p>The patient records have been successfully merged into a new combined record.</p>
                            <c:if test="${not empty mergedDemoNo}">
                                <p class="mb-0">
                                    <strong>New patient ID:</strong> <e:forHtml value="${mergedDemoNo}"/>
                                </p>
                            </c:if>
                        </div>
                        <div class="modal-footer">
                            <c:if test="${not empty mergedDemoNo}">
                                <button type="button" class="btn btn-success"
                                        onclick="popupWindow('${pageContext.request.contextPath}/demographic/demographiccontrol.jsp?demographic_no=<e:forUriComponent value='${mergedDemoNo}'/>&amp;displaymode=edit&amp;dboperation=search_detail'); document.getElementById('mergeSuccessModal').querySelector('[data-bs-dismiss=modal]').click();">
                                    Open New Patient Chart
                                </button>
                            </c:if>
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        </c:when>
        <c:when test="${outcome eq 'successUnMerge'}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                Record unmerged successfully.
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:when>
        <c:when test="${outcome eq 'failure'}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                Operation failed. Please try again.
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:when>
    </c:choose>

    <%-- Mode tabs --%>
    <ul class="nav nav-tabs mb-3">
        <li class="nav-item">
            <a class="nav-link ${not unmergeMode ? 'active' : ''}"
               href="${pageContext.request.contextPath}/admin/DemographicMerge.do?mode=merge&search_mode=<e:forUriComponent value='${searchMode}'/>">
                Find Records to Merge
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${unmergeMode ? 'active' : ''}"
               href="${pageContext.request.contextPath}/admin/DemographicMerge.do?mode=unmerge&search_mode=<e:forUriComponent value='${searchMode}'/>">
                Find Merged Records (Unmerge)
            </a>
        </li>
    </ul>

    <%-- Search form --%>
    <div class="search-bar">
        <form method="get" action="${pageContext.request.contextPath}/admin/DemographicMerge.do" class="row g-2 align-items-center">
            <input type="hidden" name="mode" value="<e:forHtmlAttribute value='${mode}'/>">
            <div class="col-auto">
                <div class="btn-group btn-group-sm" role="group">
                    <input type="radio" class="btn-check" name="search_mode" id="sm_name"    value="search_name"    <c:if test="${searchMode eq 'search_name' or empty searchMode}">checked</c:if>>
                    <label class="btn btn-outline-secondary" for="sm_name">Name</label>

                    <input type="radio" class="btn-check" name="search_mode" id="sm_phone"   value="search_phone"   <c:if test="${searchMode eq 'search_phone'}">checked</c:if>>
                    <label class="btn btn-outline-secondary" for="sm_phone">Phone</label>

                    <input type="radio" class="btn-check" name="search_mode" id="sm_dob"     value="search_dob"     <c:if test="${searchMode eq 'search_dob'}">checked</c:if>>
                    <label class="btn btn-outline-secondary" for="sm_dob">DOB</label>

                    <input type="radio" class="btn-check" name="search_mode" id="sm_address" value="search_address" <c:if test="${searchMode eq 'search_address'}">checked</c:if>>
                    <label class="btn btn-outline-secondary" for="sm_address">Address</label>

                    <input type="radio" class="btn-check" name="search_mode" id="sm_hin"     value="search_hin"     <c:if test="${searchMode eq 'search_hin'}">checked</c:if>>
                    <label class="btn btn-outline-secondary" for="sm_hin">HIN</label>
                </div>
            </div>
            <div class="col">
                <input type="text" class="form-control form-control-sm" name="keyword"
                       value="<e:forHtmlAttribute value='${keyword}'/>" maxlength="100" autofocus>
            </div>
            <div class="col-auto">
                <button type="submit" class="btn btn-primary btn-sm">Search</button>
            </div>
        </form>
    </div>

    <%-- Results — only shown after the form has been submitted (keyword != null) --%>
    <c:if test="${keyword != null}">

        <p class="results-header">
            <c:choose>
                <c:when test="${not empty keyword}">Results for: <strong><e:forHtml value="${keyword}"/></strong> &nbsp;</c:when>
                <c:otherwise>All records &nbsp;</c:otherwise>
            </c:choose>
            (${resultCount} record<c:if test="${resultCount != 1}">s</c:if>)
        </p>

        <c:choose>

            <%-- ════════════════════════════════════════════════════════════
                 UNMERGE MODE — radio per row + collapsible merge-history accordion.
                 Sorting is intentionally omitted: reordering rows would detach each
                 main row from its paired accordion row.
                 ════════════════════════════════════════════════════════════ --%>
            <c:when test="${unmergeMode}">
                <form id="unmergeForm" method="post" action="${pageContext.request.contextPath}/admin/DemographicMerge.do">
                    <input type="hidden" name="method" value="unmerge">
                    <table class="table table-sm table-bordered">
                        <thead class="table-light">
                        <tr>
                            <th title="Select record to unmerge">Select</th>
                            <th title="Show merge history">History</th>
                            <th>ID</th>
                            <th>Last Name</th>
                            <th>First Name</th>
                            <th>Age</th>
                            <th>DOB</th>
                            <th>Sex</th>
                            <th>HIN</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="demo" items="${demoList}">
                            <%-- Main result row --%>
                            <tr>
                                <td class="text-center">
                                    <input type="radio" name="mergedDemographicNo" value="${demo.demographicNo}">
                                </td>
                                <td class="text-center">
                                    <button type="button" class="btn btn-link btn-sm p-0 history-toggle"
                                            data-bs-toggle="collapse"
                                            data-bs-target="#history-${demo.demographicNo}"
                                            aria-expanded="false"
                                            title="Show merge history">&#9654;</button>
                                </td>
                                <td>
                                    <a href="javascript:popupWindow('${pageContext.request.contextPath}/demographic/demographiccontrol.jsp?demographic_no=${demo.demographicNo}&amp;displaymode=edit&amp;dboperation=search_detail')">
                                        <e:forHtml value="${demo.demographicNo}"/>
                                    </a>
                                </td>
                                <td><e:forHtml value="${demo.lastName}"/></td>
                                <td><e:forHtml value="${demo.firstName}"/></td>
                                <td><e:forHtml value="${demo.age}"/></td>
                                <td><e:forHtml value="${demo.formattedDob}"/></td>
                                <td><e:forHtml value="${demo.sex}"/></td>
                                <td><e:forHtml value="${demo.hin}"/></td>
                            </tr>
                            <%-- Accordion row — hidden until toggle is clicked --%>
                            <tr class="collapse" id="history-${demo.demographicNo}">
                                <td colspan="9" class="p-0">
                                    <div class="bg-light border-bottom px-3 py-2">
                                        <c:set var="sources" value="${mergeSourcesMap[demo.demographicNo]}"/>
                                        <c:set var="event"   value="${mergeEventMap[demo.demographicNo]}"/>
                                        <c:choose>
                                            <c:when test="${not empty sources}">
                                                <strong class="d-block mb-2">Merged from:</strong>
                                                <table class="table table-sm table-bordered mb-0">
                                                    <thead class="table-secondary">
                                                    <tr>
                                                        <th>Role</th>
                                                        <th>ID</th>
                                                        <th>Last Name</th>
                                                        <th>First Name</th>
                                                        <th>DOB</th>
                                                        <th>HIN</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    <c:forEach var="src" items="${sources}">
                                                        <tr>
                                                            <td>
                                                                <c:choose>
                                                                    <c:when test="${src.demographicNo == event.primaryDemographicNo}">
                                                                        <span class="badge bg-primary">Primary</span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="badge bg-secondary">Secondary</span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td>
                                                                <a href="javascript:popupWindow('${pageContext.request.contextPath}/demographic/demographiccontrol.jsp?demographic_no=${src.demographicNo}&amp;displaymode=edit&amp;dboperation=search_detail')">
                                                                    <e:forHtml value="${src.demographicNo}"/>
                                                                </a>
                                                            </td>
                                                            <td><e:forHtml value="${src.lastName}"/></td>
                                                            <td><e:forHtml value="${src.firstName}"/></td>
                                                            <td><e:forHtml value="${src.formattedDob}"/></td>
                                                            <td><e:forHtml value="${src.hin}"/></td>
                                                        </tr>
                                                    </c:forEach>
                                                    </tbody>
                                                </table>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted fst-italic">Merge history not available.</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty demoList}">
                            <tr><td colspan="9" class="text-center text-muted">No merged records found.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                    <c:if test="${not empty demoList}">
                        <button type="button" class="btn btn-warning" onclick="showUnmergeConfirm()">
                            Unmerge Selected
                        </button>
                    </c:if>
                </form>
            </c:when>

            <%-- ════════════════════════════════════════════════════════════
                 MERGE MODE — checkbox (select) + radio (primary) in same row.
                 Records with patientStatus MERGED or IN show no checkbox/radio.
                 ════════════════════════════════════════════════════════════ --%>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/admin/DemographicMerge.do"
                      id="mergeForm" onsubmit="return buildAndSubmit()">
                    <input type="hidden" name="method" value="merge">
                    <table class="table table-sm table-striped table-bordered" id="mergeTable">
                        <thead class="table-light">
                        <tr>
                            <th title="Select records to merge">Select</th>
                            <th title="Choose which record is the primary (its identity will be used for the merged record)">Primary</th>
                            <th class="sortable" data-col="2">ID <span class="sort-icon">&#8597;</span></th>
                            <th class="sortable" data-col="3">Last Name <span class="sort-icon">&#8597;</span></th>
                            <th class="sortable" data-col="4">First Name <span class="sort-icon">&#8597;</span></th>
                            <th class="sortable" data-col="5">Age <span class="sort-icon">&#8597;</span></th>
                            <th class="sortable" data-col="6">DOB <span class="sort-icon">&#8597;</span></th>
                            <th class="sortable" data-col="7">Sex <span class="sort-icon">&#8597;</span></th>
                            <th class="sortable" data-col="8">Roster <span class="sort-icon">&#8597;</span></th>
                            <th class="sortable" data-col="9">HIN <span class="sort-icon">&#8597;</span></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="demo" items="${demoList}">
                            <c:set var="eligible" value="${demo.patientStatus ne 'MERGED' and demo.patientStatus ne 'IN'}"/>
                            <tr class="${not eligible ? 'ineligible-row' : ''}">
                                <td class="text-center">
                                    <c:if test="${eligible}">
                                        <input type="checkbox" class="demo-check" name="demographicNo" value="${demo.demographicNo}">
                                    </c:if>
                                </td>
                                <td class="text-center">
                                    <c:if test="${eligible}">
                                        <input type="radio" class="primary-radio" name="primaryRadio" value="${demo.demographicNo}">
                                    </c:if>
                                </td>
                                <td>
                                    <a href="javascript:popupWindow('${pageContext.request.contextPath}/demographic/demographiccontrol.jsp?demographic_no=${demo.demographicNo}&amp;displaymode=edit&amp;dboperation=search_detail')">
                                        <e:forHtml value="${demo.demographicNo}"/>
                                    </a>
                                </td>
                                <td><e:forHtml value="${demo.lastName}"/></td>
                                <td><e:forHtml value="${demo.firstName}"/></td>
                                <td><e:forHtml value="${demo.age}"/></td>
                                <td><e:forHtml value="${demo.formattedDob}"/></td>
                                <td><e:forHtml value="${demo.sex}"/></td>
                                <td><e:forHtml value="${demo.rosterStatus}"/></td>
                                <td><e:forHtml value="${demo.hin}"/></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty demoList}">
                            <tr><td colspan="10" class="text-center text-muted">No records found.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                    <c:if test="${not empty demoList}">
                        <button type="submit" class="btn btn-primary">Merge Selected Records</button>
                    </c:if>
                </form>
            </c:otherwise>
        </c:choose>

        <%-- Pagination --%>
        <div class="pagination-bar">
            <c:if test="${offset > 0}">
                <a class="btn btn-outline-secondary btn-sm"
                   href="${pageContext.request.contextPath}/admin/DemographicMerge.do?mode=<e:forUriComponent value='${mode}'/>&keyword=<e:forUriComponent value='${keyword}'/>&search_mode=<e:forUriComponent value='${searchMode}'/>&limit1=${offset - limit}&limit2=${limit}">
                    &laquo; Previous
                </a>
            </c:if>
            <c:if test="${resultCount >= limit}">
                <a class="btn btn-outline-secondary btn-sm"
                   href="${pageContext.request.contextPath}/admin/DemographicMerge.do?mode=<e:forUriComponent value='${mode}'/>&keyword=<e:forUriComponent value='${keyword}'/>&search_mode=<e:forUriComponent value='${searchMode}'/>&limit1=${offset + limit}&limit2=${limit}">
                    Next &raquo;
                </a>
            </c:if>
        </div>

    </c:if><%-- end keyword != null --%>

    <c:if test="${keyword == null}">
        <p class="text-center text-muted mt-4">Enter a search term above to find patient records, or click Search to show all.</p>
    </c:if>

    <%-- ── BS5 modals ─────────────────────────────────────────────────────── --%>

    <%-- Validation error modal --%>
    <div class="modal fade" id="mergeValidationModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-warning">
                    <h5 class="modal-title">Cannot Merge</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body" id="mergeValidationMessage"></div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">OK</button>
                </div>
            </div>
        </div>
    </div>

    <%-- Merge confirmation modal --%>
    <div class="modal fade" id="mergeConfirmModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Confirm Merge</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    You are about to merge duplicate patient records. This action is irreversible. Do you want to proceed?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-primary" id="mergeConfirmBtn">Merge Records</button>
                </div>
            </div>
        </div>
    </div>

    <%-- Unmerge confirmation modal --%>
    <div class="modal fade" id="unmergeConfirmModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Confirm Unmerge</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    Unmerge the selected record? This will restore the original patients.
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-warning" id="unmergeConfirmBtn">Unmerge</button>
                </div>
            </div>
        </div>
    </div>

</div>

<jsp:include page="/images/spinner.jsp" flush="true"/>

<script>
    // ── Popup to patient chart ────────────────────────────────────────────────
    function popupWindow(url) {
        const props = "height=660,width=960,location=no,scrollbars=yes,menubar=no,toolbar=no,resizable=yes,top=0,left=0";
        const popup = window.open(url, "patientchart", props);
        if (popup) popup.focus();
    }

    // ── Merge form: validate then show confirmation modal ────────────────────
    // Returns false always — the actual submit is triggered by the modal confirm button.
    function buildAndSubmit() {
        const checked = Array.from(document.querySelectorAll('input.demo-check:checked'));
        if (checked.length < 2) {
            showMergeValidationError('Please select at least 2 records to merge.');
            return false;
        }

        const primaryRadio = document.querySelector('input.primary-radio:checked');
        if (!primaryRadio) {
            showMergeValidationError('Please select a Primary record (the record whose identity will be used for the merged record).');
            return false;
        }

        const checkedValues = checked.map(function (cb) { return cb.value; });
        if (checkedValues.indexOf(primaryRadio.value) === -1) {
            showMergeValidationError('The Primary record must be one of the selected (checked) records.');
            return false;
        }

        // Validation passed — open the confirmation modal; submit handled by modal button
        new bootstrap.Modal(document.getElementById('mergeConfirmModal')).show();
        return false;
    }

    function showMergeValidationError(msg) {
        document.getElementById('mergeValidationMessage').textContent = msg;
        new bootstrap.Modal(document.getElementById('mergeValidationModal')).show();
    }

    // ── Unmerge: show confirmation modal ─────────────────────────────────────
    function showUnmergeConfirm() {
        new bootstrap.Modal(document.getElementById('unmergeConfirmModal')).show();
    }

    // ── Wire modal confirm buttons + auto-check on primary radio change ───────
    document.addEventListener('DOMContentLoaded', function () {

        // Merge confirm button: build hidden inputs then submit
        const mergeConfirmBtn = document.getElementById('mergeConfirmBtn');
        if (mergeConfirmBtn) {
            mergeConfirmBtn.addEventListener('click', function () {
                const form = document.getElementById('mergeForm');
                const primaryRadio = document.querySelector('input.primary-radio:checked');
                const checked = Array.from(document.querySelectorAll('input.demo-check:checked'));

                const pi   = document.createElement('input');
                pi.type  = 'hidden';
                pi.name  = 'primaryDemographicNo';
                pi.value = primaryRadio.value;
                form.appendChild(pi);

                checked.forEach(function (cb) {
                    if (cb.value !== primaryRadio.value) {
                        const si   = document.createElement('input');
                        si.type  = 'hidden';
                        si.name  = 'secondaryDemographicNo';
                        si.value = cb.value;
                        form.appendChild(si);
                    }
                });

                ShowSpin(true);
                form.submit();
            });
        }

        // Unmerge confirm button: submit the unmerge form
        const unmergeConfirmBtn = document.getElementById('unmergeConfirmBtn');
        if (unmergeConfirmBtn) {
            unmergeConfirmBtn.addEventListener('click', function () {
                ShowSpin(true);
                document.getElementById('unmergeForm').submit();
            });
        }

        // Auto-check a row when its Primary radio is selected
        document.querySelectorAll('input.primary-radio').forEach(function (radio) {
            radio.addEventListener('change', function () {
                const row = radio.closest('tr');
                const cb  = row ? row.querySelector('input.demo-check') : null;
                if (cb && !cb.checked) cb.checked = true;
            });
        });
    });

    // ── Client-side column sort ───────────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('th.sortable').forEach(function (th) {
            th.addEventListener('click', function () {
                const table = th.closest('table');
                const tbody = table.querySelector('tbody');
                const col   = parseInt(th.getAttribute('data-col'), 10);
                const asc   = th.getAttribute('data-asc') !== 'true';
                th.setAttribute('data-asc', asc ? 'true' : 'false');

                table.querySelectorAll('th.sortable .sort-icon').forEach(function (ic) {
                    ic.classList.remove('active');
                    ic.textContent = '\u2195';
                });
                const icon = th.querySelector('.sort-icon');
                icon.classList.add('active');
                icon.textContent = asc ? '\u2191' : '\u2193';

                const rows = Array.from(tbody.querySelectorAll('tr'));
                rows.sort(function (a, b) {
                    const at  = ((a.querySelectorAll('td')[col] || {}).textContent || '').trim();
                    const bt  = ((b.querySelectorAll('td')[col] || {}).textContent || '').trim();
                    const n1  = parseFloat(at);
                    const n2  = parseFloat(bt);
                    const cmp = (!isNaN(n1) && !isNaN(n2)) ? (n1 - n2) : at.localeCompare(bt);
                    return asc ? cmp : -cmp;
                });
                rows.forEach(function (r) { tbody.appendChild(r); });
            });
        });
    });
</script>
<script src="${pageContext.request.contextPath}/library/bootstrap/5.0.2/js/bootstrap.bundle.min.js"></script>
<c:if test="${outcome eq 'success'}">
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const el = document.getElementById('mergeSuccessModal');
        if (el) { new bootstrap.Modal(el).show(); }
    });
</script>
</c:if>
</body>
</html>
