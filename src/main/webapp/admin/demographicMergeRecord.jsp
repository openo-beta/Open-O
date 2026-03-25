<%--
    Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
    This software is published under the GPL GNU General Public License.

    demographicMergeRecord.jsp
    Purpose: Step 1 of the demographic merge/unmerge workflow. Renders search results
             from request attributes populated by DemographicMergeAction. For merge,
             selected patients are forwarded to demographicMergePrimarySelect.jsp via
             DemographicMerge.do?method=selectPrimary. For unmerge, the selected merged
             record is submitted directly to DemographicMerge.do?method=unmerge.
    Request attributes (set by DemographicMergeAction):
        demoList    - List<Demographic> search results (null if no search yet)
        keyword     - String search term
        searchMode  - String search_name | search_phone | search_dob | search_address | search_hin
        mode        - String merge | unmerge
        unmergeMode - Boolean true when in unmerge mode
        outcome     - String success | failure | successUnMerge (from prior redirect)
        offset      - int pagination offset
        limit       - int page size
        resultCount - int number of results
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
        .search-bar { background: #f8f9fa; border-radius: 6px; padding: 1rem 1.25rem; margin-bottom: 1.25rem; }
        .results-header { font-size: .85rem; color: #6c757d; margin-bottom: .5rem; }
        .table th { white-space: nowrap; }
        .pagination-bar { margin-top: .75rem; }
    </style>
</head>
<body>
<div class="container-fluid" style="max-width:1100px;">

    <h4 class="mt-3 mb-3">
        <fmt:setBundle basename="oscarResources"/>
        <fmt:message key="admin.admin.mergeRec"/>
    </h4>

    <%-- Outcome alert --%>
    <c:choose>
        <c:when test="${outcome eq 'success'}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                Records merged successfully.
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
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
               href="${pageContext.request.contextPath}/DemographicMerge.do?mode=merge&search_mode=<e:forUriComponent value='${searchMode}'/>">
                Find Records to Merge
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${unmergeMode ? 'active' : ''}"
               href="${pageContext.request.contextPath}/DemographicMerge.do?mode=unmerge&search_mode=<e:forUriComponent value='${searchMode}'/>">
                Find Merged Records (Unmerge)
            </a>
        </li>
    </ul>

    <%-- Search form --%>
    <div class="search-bar">
        <form method="get" action="${pageContext.request.contextPath}/DemographicMerge.do" class="row g-2 align-items-center">
            <input type="hidden" name="mode" value="<e:forHtmlAttribute value='${mode}'/>">
            <div class="col-auto">
                <div class="btn-group btn-group-sm" role="group">
                    <input type="radio" class="btn-check" name="search_mode" id="sm_name"    value="search_name"    <c:if test="${searchMode eq 'search_name'}">checked</c:if>>
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
            <input type="hidden" name="limit1" value="0">
            <input type="hidden" name="limit2" value="<e:forHtmlAttribute value='${limit}'/>">
            <div class="col-auto">
                <button type="submit" class="btn btn-primary btn-sm">Search</button>
            </div>
        </form>
    </div>

    <%-- Results --%>
    <c:if test="${not empty keyword}">
        <p class="results-header">
            Results for: <strong><e:forHtml value="${keyword}"/></strong>
            &nbsp;(${resultCount} record<c:if test="${resultCount != 1}">s</c:if>)
        </p>

        <c:choose>
            <%-- UNMERGE MODE --%>
            <c:when test="${unmergeMode}">
                <form method="post" action="${pageContext.request.contextPath}/DemographicMerge.do">
                    <input type="hidden" name="method" value="unmerge">
                    <table class="table table-sm table-striped table-bordered">
                        <thead class="table-light">
                        <tr>
                            <th></th>
                            <th>ID</th>
                            <th>Last Name</th>
                            <th>First Name</th>
                            <th>DOB</th>
                            <th>Sex</th>
                            <th>HIN</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="demo" items="${demoList}">
                            <tr>
                                <td class="text-center">
                                    <input type="radio" name="mergedDemographicNo" value="${demo.demographicNo}">
                                </td>
                                <td><e:forHtml value="${demo.demographicNo}"/></td>
                                <td><e:forHtml value="${demo.lastName}"/></td>
                                <td><e:forHtml value="${demo.firstName}"/></td>
                                <td><e:forHtml value="${demo.formattedDob}"/></td>
                                <td><e:forHtml value="${demo.sex}"/></td>
                                <td><e:forHtml value="${demo.hin}"/></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty demoList}">
                            <tr><td colspan="7" class="text-center text-muted">No merged records found.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                    <c:if test="${not empty demoList}">
                        <button type="submit" class="btn btn-warning"
                                onclick="return confirm('Unmerge the selected record? This will restore the original patients.')">
                            Unmerge Selected
                        </button>
                    </c:if>
                </form>
            </c:when>

            <%-- MERGE MODE --%>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/DemographicMerge.do"
                      onsubmit="return validateMergeSelection()">
                    <input type="hidden" name="method" value="selectPrimary">
                    <input type="hidden" name="search_mode" value="<e:forHtmlAttribute value='${searchMode}'/>">
                    <input type="hidden" name="keyword"     value="<e:forHtmlAttribute value='${keyword}'/>">
                    <table class="table table-sm table-striped table-bordered">
                        <thead class="table-light">
                        <tr>
                            <th></th>
                            <th>ID</th>
                            <th>Last Name</th>
                            <th>First Name</th>
                            <th>DOB</th>
                            <th>Sex</th>
                            <th>Roster</th>
                            <th>HIN</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="demo" items="${demoList}">
                            <tr>
                                <td class="text-center">
                                    <input type="checkbox" name="demographicNo" value="${demo.demographicNo}">
                                </td>
                                <td><e:forHtml value="${demo.demographicNo}"/></td>
                                <td><e:forHtml value="${demo.lastName}"/></td>
                                <td><e:forHtml value="${demo.firstName}"/></td>
                                <td><e:forHtml value="${demo.formattedDob}"/></td>
                                <td><e:forHtml value="${demo.sex}"/></td>
                                <td><e:forHtml value="${demo.rosterStatus}"/></td>
                                <td><e:forHtml value="${demo.hin}"/></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty demoList}">
                            <tr><td colspan="8" class="text-center text-muted">No records found.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                    <c:if test="${not empty demoList}">
                        <button type="submit" class="btn btn-primary">
                            Continue to Select Primary &rarr;
                        </button>
                    </c:if>
                </form>
            </c:otherwise>
        </c:choose>

        <%-- Pagination --%>
        <div class="pagination-bar">
            <c:if test="${offset > 0}">
                <a class="btn btn-outline-secondary btn-sm"
                   href="${pageContext.request.contextPath}/DemographicMerge.do?mode=<e:forUriComponent value='${mode}'/>&keyword=<e:forUriComponent value='${keyword}'/>&search_mode=<e:forUriComponent value='${searchMode}'/>&limit1=${offset - limit}&limit2=${limit}">
                    &laquo; Previous
                </a>
            </c:if>
            <c:if test="${resultCount >= limit}">
                <a class="btn btn-outline-secondary btn-sm"
                   href="${pageContext.request.contextPath}/DemographicMerge.do?mode=<e:forUriComponent value='${mode}'/>&keyword=<e:forUriComponent value='${keyword}'/>&search_mode=<e:forUriComponent value='${searchMode}'/>&limit1=${offset + limit}&limit2=${limit}">
                    Next &raquo;
                </a>
            </c:if>
        </div>
    </c:if>

    <c:if test="${empty keyword}">
        <p class="text-center text-muted mt-4">Enter a search term above to find patient records.</p>
    </c:if>

</div>

<script>
    function validateMergeSelection() {
        const checked = document.querySelectorAll('input[name="demographicNo"]:checked');
        if (checked.length < 2) {
            alert('Please select at least 2 records to merge.');
            return false;
        }
        return true;
    }
</script>
<script src="${pageContext.request.contextPath}/library/bootstrap/5.0.2/js/bootstrap.bundle.min.js"></script>
</body>
</html>
