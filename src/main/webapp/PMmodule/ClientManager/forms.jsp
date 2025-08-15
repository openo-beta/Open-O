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

<%@ include file="/taglibs.jsp" %>

<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%@page import="org.oscarehr.PMmodule.web.ClientManagerAction" %>
<%@page import="org.oscarehr.common.model.CdsClientForm" %>
<%@page import="org.oscarehr.common.model.OcanStaffForm" %>
<%@page import="org.oscarehr.common.model.OcanClientForm" %>
<%@page import="org.oscarehr.common.model.Demographic" %>
<%@page import="java.util.Enumeration" %>
<%@ page import="org.oscarehr.PMmodule.service.ProgramManager" %>
<%@ page import="org.oscarehr.PMmodule.model.Program" %>
<%@page import="org.oscarehr.util.SpringUtils" %>

<input type="hidden" name="clientId" value=""/>
<input type="hidden" name="formId" value=""/>
<input type="hidden" id="formInstanceId" value="0"/>

<%
    Demographic currentDemographic = (Demographic) request.getAttribute("client");
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean programEnableOcan = false;
    String currentProgram = (String) session.getAttribute(org.oscarehr.util.SessionConstants.CURRENT_PROGRAM_ID);
    if (currentProgram != null) {
        ProgramManager pm = SpringUtils.getBean(ProgramManager.class);
        Program program = pm.getProgram(currentProgram);
        programEnableOcan = program.isEnableOCAN();
    }
%>

<script>
</script>


<br/>
<br/>
<!--
<div class="tabs">
<table cellpadding="3" cellspacing="0" border="0">
<tr>
<th title="Programs">Program Intakes</th>
</tr>
</table>
</div>
<table class="simple" cellspacing="2" cellpadding="3">
<thead>
<tr>
<th>Date</th>
<th>Staff</th>
<th>Actions</th>
</tr>
</thead>
<c:forEach var="intake" items="${programIntakes}">
    <tr>
    <td width="20%"><c:out value="${intake.createdOnStr}"/></td>
    <td><c:out value="${intake.staffName}"/></td>
    <td>
    <input type="button" value="Print Preview" onclick="printProgramIntake('<c:out value="${client.demographicNo}"/>', '<c:out
        value="${intake.programId}"/>')" />
    <input type="button" value="Update" onclick="updateProgramIntake('<c:out value="${client.demographicNo}"/>', '<c:out
        value="${intake.programId}"/>')" />
    </td>
    </tr>
</c:forEach>
<tr>
<td colspan="3">
<select name="programWithIntakeId" id="programWithIntakeId">
<c:forEach var="program" items="${programsWithIntake}">
    <option value="${program.id}">
    ${program.name}
    </option>
</c:forEach>
</select>
<input type="button" value="Update" onclick="updateProgramIntake('<c:out value="${client.demographicNo}"/>')" />
</td>
</tr>
</table>
<br />
<br />
-->

<div class="tabs">
    <table cellpadding="3" cellspacing="0" border="0">
        <tr>
            <th title="Programs">Consent History</th>
        </tr>
    </table>
</div>
<table class="simple" cellspacing="2" cellpadding="3">
    <thead>
    <tr>
        <th>Date</th>
        <th>Provider</th>
        <th></th>
    </tr>
    </thead>
    <c:forEach var="form" items="${consents}">
        <tr>
            <td><c:out value="${form.createdDate}"/></td>
            <td><c:out value="${form.provider}"/></td>
            <td>
                <a href="ClientManager/manage_consent.jsp?viewConsentId=<c:out value="${form.consentId}" />&demographicId=<%=request.getAttribute("id")%>">details</a>
            </td>
        </tr>
    </c:forEach>
</table>
<br/>
<br/>

<div class="tabs">
    <table cellpadding="3" cellspacing="0" border="0">
        <tr>
            <th title="Programs">CDS History</th>
        </tr>
    </table>
</div>
<table class="simple" cellspacing="2" cellpadding="3">
    <thead>
    <tr>
        <th>Date</th>
        <th>Provider</th>
        <th>Signed</th>
        <th>Admission</th>
        <th>Actions</th>
    </tr>
    </thead>
    <c:forEach var="form" items="${cdsForms}">
        <tr>
            <c:set var="form" value="${form}" scope="request"/>
            <%
                CdsClientForm cdsForm = (CdsClientForm) request.getAttribute("form");
                String admissionString = ClientManagerAction.getEscapedAdmissionSelectionDisplay(cdsForm.getAdmissionId());
            %>
            <td><%=ClientManagerAction.getEscapedDateDisplay(cdsForm.getCreated())%>
            </td>
            <td><%=ClientManagerAction.getEscapedProviderDisplay(cdsForm.getProviderNo())%>
            </td>
            <td><%=cdsForm.isSigned() ? "signed" : "unsigned"%>
            </td>
            <td><%=admissionString%>
            </td>
            <%
                String cdsFormUrl = "ClientManager/cds_form_4.jsp?cdsFormId=" + cdsForm.getId();
            %>
            <td><a href="<%=cdsFormUrl%>">update cds data</a> <input type="button" value="Print Preview"
                                                                     onclick="document.location='<%=cdsFormUrl+"&print=true"%>'"/>
            </td>
        </tr>
    </c:forEach>
</table>
<br/>
<br/>

<% if (programEnableOcan) { %>

<div class="tabs">
    <table cellpadding="3" cellspacing="0" border="0">
        <tr>
            <th title="Programs">CBI Form History</th>
        </tr>
    </table>
</div>
<table class="simple" cellspacing="2" cellpadding="3">
    <thead>
    <tr>
        <th>Form ID</th>
        <th>Date</th>
        <th>Provider</th>
        <th>Signed</th>
        <th>Admission</th>
        <th>Actions</th>
    </tr>
    </thead>
    <c:forEach var="form" items="${cbiForms}">
        <tr>
            <c:set var="form" value="${form}" scope="request"/>
            <%
                OcanStaffForm cbiForm = (OcanStaffForm) request.getAttribute("form");
                String admissionString = ClientManagerAction.getEscapedAdmissionSelectionDisplay(cbiForm.getAdmissionId());
            %>
            <td><%=cbiForm.getId()%>
            </td>
            <td><%=ClientManagerAction.getEscapedDateDisplay(cbiForm.getCreated())%>
            </td>
            <td><%=ClientManagerAction.getEscapedProviderDisplay(cbiForm.getProviderNo())%>
            </td>
            <td><%=cbiForm.isSigned() ? "signed" : "unsigned"%>
            </td>
            <td><%=admissionString%>
            </td>
            <%

                String cbiFormUrl = "ClientManager/cbi_form.jsp?view=history&ocanType=CBI&demographicId=" + currentDemographic.getDemographicNo() + "&ocanStaffFormId=" + cbiForm.getId();

            %>
            <td><a href="<%=cbiFormUrl%>">View CBI Data</a></td>
        </tr>
    </c:forEach>
</table>
<br/>
<br/>

<div class="tabs">
    <table cellpadding="3" cellspacing="0" border="0">
        <tr>
            <th title="Programs">FULL Ocan Assessment History</th>
        </tr>
    </table>
</div>
<table class="simple" cellspacing="2" cellpadding="3">
    <thead>
    <tr>
        <th>Form ID</th>
        <th>Assessment ID</th>
        <th>Creation Date</th>
        <th>Start Date</th>
        <th>Completion Date</th>
        <th>Staff</th>
        <th>Status</th>
        <th>(Self)Creation Date</th>
        <th>(Self)Start Date</th>
        <th>(Self)Completion Date</th>
        <th>(Self)Staff</th>
        <th>Actions</th>
    </tr>
    </thead>
    <c:forEach var="form" items="${ocanStaffForms}">
        <tr>
            <td><c:out value="${form.id}"/></td>
            <td><c:out value="${form.assessmentId}"/></td>
            <td width="10%"><c:out value="${form.created}"/></td>
            <td width="10%"><c:out value="${form.formattedStartDate}"/></td>
            <td width="10%"><c:out value="${form.formattedCompletionDate}"/></td>
            <td><c:out value="${form.providerName}"/></td>
            <td><c:out value="${form.assessmentStatus}"/></td>
            <td width="10%"><c:out value="${form.clientFormCreated}"/></td>
            <td width="10%"><c:out value="${form.formattedClientStartDate}"/></td>
            <td width="10%"><c:out value="${form.formattedClientCompletionDate}"/></td>
            <td><c:out value="${form.clientFormProviderName}"/></td>
            <c:set var="form" value="${form}" scope="request"/>
            <%
                OcanStaffForm ocanStaffForm = (OcanStaffForm) request.getAttribute("form");
                String fullOcanStaffFormUrl = "ClientManager/ocan_form.jsp?ocanType=FULL&demographicId=" + currentDemographic.getDemographicNo() + "&ocanStaffFormId=" + ocanStaffForm.getId();
                String fullOcanClientFormUrl = "ClientManager/ocan_client_form.jsp?ocanType=FULL&demographicId=" + currentDemographic.getDemographicNo() + "&ocanStaffFormId=" + ocanStaffForm.getId();
                boolean completed = ocanStaffForm.getAssessmentStatus() != null ? "Completed".equalsIgnoreCase(ocanStaffForm.getAssessmentStatus()) : false;
            %>

            <!--
			<td><input type="button" value="Print Preview" onclick="printOcanStaffForm('<c:out value="${client.demographicNo}" />','<c:out value="${form.id}" />')" /></td>	
		-->

            <td>
                <% if (completed) { %>
                <security:oscarSec roleName="<%=roleName$%>" objectName="_pmm_clientForms.updateCompletedOcan"
                                   rights="r">
                    <a href="<%=fullOcanStaffFormUrl%>">Update Staff Assessment</a>
                </security:oscarSec>
                <%} else {%>
                <a href="<%=fullOcanStaffFormUrl%>">Update Staff Assessment</a>
                <%} %>
                <input type="button" value="Print Preview"
                       onclick="document.location='<%=fullOcanStaffFormUrl+"&print=true"%>'"/></td>

            <td>
                <% if (completed) { %>
                <security:oscarSec roleName="<%=roleName$%>" objectName="_pmm_clientForms.updateCompletedOcan"
                                   rights="r">
                    <a href="<%=fullOcanClientFormUrl%>">Update Consumer Self-Assessment</a>
                </security:oscarSec>
                <%} else { %>
                <a href="<%=fullOcanClientFormUrl%>">Update Consumer Self-Assessment</a>
                <%} %>
                <input type="button" value="Print Preview"
                       onclick="document.location='<%=fullOcanClientFormUrl+"&print=true"%>'"/></td>

        </tr>
    </c:forEach>
</table>
<br/>
<br/>


<div class="tabs">
    <table cellpadding="3" cellspacing="0" border="0">
        <tr>
            <th title="Programs">SELF+CORE Ocan Assessment History</th>
        </tr>
    </table>
</div>
<table class="simple" cellspacing="2" cellpadding="3">
    <thead>
    <tr>
        <th>Form ID</th>
        <th>Assessment ID</th>
        <th>Creation Date</th>
        <th>Start Date</th>
        <th>Completion Date</th>
        <th>Staff</th>
        <th>Status</th>
        <th>(Self)Creation Date</th>
        <th>(Self)Start Date</th>
        <th>(Self)Completion Date</th>
        <th>(Self)Staff</th>
        <th>Actions</th>
    </tr>
    </thead>
    <c:forEach var="form" items="${selfOcanStaffForms}">
        <tr>
            <td><c:out value="${form.id}"/></td>
            <td><c:out value="${form.assessmentId}"/></td>
            <td width="10%"><c:out value="${form.created}"/></td>
            <td width="10%"><c:out value="${form.formattedStartDate}"/></td>
            <td width="10%"><c:out value="${form.formattedCompletionDate}"/></td>
            <td><c:out value="${form.providerName}"/></td>
            <td><c:out value="${form.assessmentStatus}"/></td>
            <td width="10%"><c:out value="${form.clientFormCreated}"/></td>
            <td width="10%"><c:out value="${form.formattedClientStartDate}"/></td>
            <td width="10%"><c:out value="${form.formattedClientCompletionDate}"/></td>
            <td><c:out value="${form.clientFormProviderName}"/></td>
            <c:set var="form" value="${form}" scope="request"/>
            <%
                OcanStaffForm ocanStaffForm = (OcanStaffForm) request.getAttribute("form");
                String selfOcanStaffFormUrl = "ClientManager/ocan_form.jsp?ocanType=SELF&demographicId=" + currentDemographic.getDemographicNo() + "&ocanStaffFormId=" + ocanStaffForm.getId();
                String selfOcanClientFormUrl = "ClientManager/ocan_client_form.jsp?ocanType=SELF&demographicId=" + currentDemographic.getDemographicNo() + "&ocanStaffFormId=" + ocanStaffForm.getId();
                boolean completed = ocanStaffForm.getAssessmentStatus() != null ? "Completed".equalsIgnoreCase(ocanStaffForm.getAssessmentStatus()) : false;
            %>

            <td>
                <% if (completed) { %>
                <security:oscarSec roleName="<%=roleName$%>" objectName="_pmm_clientForms.updateCompletedOcan"
                                   rights="r">
                    <a href="<%=selfOcanStaffFormUrl%>">Update Staff Assessment</a>
                </security:oscarSec>
                <%} else {%>
                <a href="<%=selfOcanStaffFormUrl%>">Update Staff Assessment</a>
                <%} %>
                <input type="button" value="Print Preview"
                       onclick="document.location='<%=selfOcanStaffFormUrl+"&print=true"%>'"/></td>
            <td>
                <% if (completed) { %>
                <security:oscarSec roleName="<%=roleName$%>" objectName="_pmm_clientForms.updateCompletedOcan"
                                   rights="r">
                    <a href="<%=selfOcanClientFormUrl%>">Update Consumer Self-Assessment</a>
                </security:oscarSec>
                <%} else { %>
                <a href="<%=selfOcanClientFormUrl%>">Update Consumer Self-Assessment</a>
                <%} %>
                <input type="button" value="Print Preview"
                       onclick="document.location='<%=selfOcanClientFormUrl+"&print=true"%>'"/></td>

        </tr>
    </c:forEach>
</table>
<br/>
<br/>


<div class="tabs">
    <table cellpadding="3" cellspacing="0" border="0">
        <tr>
            <th title="Programs">CORE Ocan Assessment History</th>
        </tr>
    </table>
</div>
<table class="simple" cellspacing="2" cellpadding="3">
    <thead>
    <tr>
        <th>Form ID</th>
        <th>Assessment ID</th>
        <th>Creation Date</th>
        <th>Start Date</th>
        <th>Completion Date</th>
        <th>Staff</th>
        <th>Status</th>
        <th>Actions</th>
    </tr>
    </thead>
    <c:forEach var="form" items="${coreOcanStaffForms}">
        <tr>
            <td><c:out value="${form.id}"/></td>
            <td><c:out value="${form.assessmentId}"/></td>
            <td width="10%"><c:out value="${form.created}"/></td>
            <td width="10%"><c:out value="${form.formattedStartDate}"/></td>
            <td width="10%"><c:out value="${form.formattedCompletionDate}"/></td>
            <td><c:out value="${form.providerName}"/></td>

            <td><c:out value="${form.assessmentStatus}"/></td>
            <c:set var="form" value="${form}" scope="request"/>
            <%
                OcanStaffForm ocanStaffForm = (OcanStaffForm) request.getAttribute("form");
                String coreOcanStaffFormUrl = "ClientManager/ocan_form.jsp?ocanType=CORE&demographicId=" + currentDemographic.getDemographicNo() + "&ocanStaffFormId=" + ocanStaffForm.getId();
                boolean completed = ocanStaffForm.getAssessmentStatus() != null ? "Completed".equalsIgnoreCase(ocanStaffForm.getAssessmentStatus()) : false;
            %>

            <td>
                <% if (completed) { %>
                <security:oscarSec roleName="<%=roleName$%>" objectName="_pmm_clientForms.updateCompletedOcan"
                                   rights="r">
                    <a href="<%=coreOcanStaffFormUrl%>">Update CORE OCAN</a>
                </security:oscarSec>
                <%} else { %>
                <a href="<%=coreOcanStaffFormUrl%>">Update CORE OCAN</a>
                <%} %><input type="button" value="Print Preview"
                             onclick="document.location='<%=coreOcanStaffFormUrl+"&print=true"%>'"/></td>

        </tr>
    </c:forEach>
</table>


<% } %>
<br/>
<br/>
