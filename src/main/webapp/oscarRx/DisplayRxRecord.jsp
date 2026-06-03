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
<%@page import="ca.openosp.openo.commn.model.DrugReason" %>
<%@page import="ca.openosp.openo.commn.dao.DrugReasonDao" %>
<%@page import="ca.openosp.openo.commn.model.PartialDate" %>
<%@page import="ca.openosp.openo.commn.dao.PartialDateDao" %>
<%@page import="ca.openosp.openo.managers.CodingSystemManager" %>
<%@page import="ca.openosp.openo.managers.PharmacyManager" %>
<%@page import="ca.openosp.openo.casemgmt.model.CaseManagementNoteLink" %>
<%@page import="ca.openosp.openo.casemgmt.service.CaseManagementManager" %>
<%@page import="ca.openosp.openo.commn.model.PharmacyInfo" %>
<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@page import="java.util.List" %>
<%@page import="org.apache.commons.lang3.StringUtils" %>
<%@page import="ca.openosp.openo.commn.dao.DrugDao,ca.openosp.openo.commn.model.Drug,ca.openosp.openo.utility.MiscUtils,ca.openosp.openo.utility.SpringUtils,ca.openosp.openo.PMmodule.dao.ProviderDao,ca.openosp.openo.commn.dao.DemographicDao" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_rx" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_rx");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<%
    String id = request.getParameter("id");
    LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
    DrugDao drugDao = (DrugDao) SpringUtils.getBean(DrugDao.class);
    ProviderDao providerDao = (ProviderDao) SpringUtils.getBean(ProviderDao.class);
    DemographicDao demographicDao = (DemographicDao) SpringUtils.getBean(DemographicDao.class);

    Integer drugId = Integer.parseInt(id);

    Drug drug = drugDao.find(drugId);

    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    PartialDateDao partialDateDao = SpringUtils.getBean(PartialDateDao.class);
    DrugReasonDao drugReasonDao = SpringUtils.getBean(DrugReasonDao.class);

/**
 * Define value for the ProblemCode field.
 */
    CodingSystemManager codingSystemManager = SpringUtils.getBean(CodingSystemManager.class);
    List<DrugReason> drugReasonList = drugReasonDao.getReasonsForDrugID(drug.getId(), true);
    StringBuilder stringBuilder = null;

    for (DrugReason dr : drugReasonList) {
        String codeSystem = dr.getCodingSystem();
        String code = dr.getCode();
        String codeDescription = codingSystemManager.getCodeDescription(codeSystem, code);

        if (stringBuilder == null) {
            stringBuilder = new StringBuilder("");
        }

        if (codeDescription == null || codeDescription.isEmpty()) {
            codeDescription = dr.getComments();
        }

        stringBuilder.append(codeDescription);

        if (codeSystem != null && !codeSystem.isEmpty() && code != null && !code.isEmpty()) {
            stringBuilder.append(" (");
            stringBuilder.append(codeSystem);
            stringBuilder.append(":");
            stringBuilder.append(code);
            stringBuilder.append(")");
        }

        if (drugReasonList.size() > 1) {
            stringBuilder.append("<br>");
        }
    }

    if (stringBuilder != null) {
        pageContext.setAttribute("ProblemCode", stringBuilder.toString());
    }

// Get the pharmacy information for this drug.
    PharmacyManager pharmacyManager = SpringUtils.getBean(PharmacyManager.class);
    PharmacyInfo pharmacy = null;
    if (drug.getPharmacyId() != null) {
        pharmacy = pharmacyManager.getPharmacy(loggedInInfo, drug.getPharmacyId());
    }
    pageContext.setAttribute("pharmacy", pharmacy);

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="ca.openosp.openo.utility.MiscUtils" %>
<%@ page import="ca.openosp.openo.util.UtilDateUtilities" %>
<%@ page import="org.owasp.encoder.Encode" %>
<html>
<head>
    <script type="text/javascript" src="<%= request.getContextPath()%>/js/global.js"></script>
    <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
    <title><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarRx.DisplayRxRecord.title"/></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/share/css/OscarStandardLayout.css">
    <link rel="stylesheet" type="text/css" media="all" href="<%= request.getContextPath() %>/share/css/extractedFromPages.css"/>
    <style>
        .label {
            font-weight: bold;
        }
    </style>
    <script>
        function openAnnotation() {
            ///annotation/annotation.jsp?display=Prescriptions&table_id=173&demo=185&drugSpecial=HYZAAR%2012.5MG/100MG%20take%201%20OD%20for%2030%20days%20Qty:30%20Repeats:0

        }

        function updateForm() {
            popup(250, 500, '<%=request.getContextPath()%>/oscarRx/updateForm.jsp?id=<%=Encode.forJavaScript(String.valueOf(drugId))%>', '<%=Encode.forJavaScript(String.valueOf(drugId))%>');
        }

        function popup(height, width, url, windowName) {
            return popup2(height, width, 0, 0, url, windowName);
        }


        function popup2(height, width, top, left, url, windowName) {
            var page = url;
            windowprops = "height=" + height + ",width=" + width + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=" + top + ",left=" + left;
            var popup = window.open(url, windowName, windowprops);
            if (popup != null) {
                if (popup.opener == null) {
                    popup.opener = self;
                }
            }
            popup.focus();
            return false;
        }


    </script>
</head>


<body>
<table width="100%" height="100%" border="0" cellspacing="0"
       cellpadding="0">
    <tr>
        <td valign="top">

            <table width="100%" border="0" cellspacing="0" cellpadding="3"
                   bgcolor="black">
                <tr>
                    <td width="66%" align="left" class="Cell">
                        <div style="color:white;margin-left:5px;" class="Field2"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarMDS.segmentDisplay.formDetailResults"/></div>
                    </td>
                    <td align="right">
                        <input type="button" value="<fmt:setBundle basename="oscarResources"/><fmt:message key="global.btnClose"/>" onClick="window.close()"/>
                        <input type="button" value="<fmt:setBundle basename="oscarResources"/><fmt:message key="global.btnPrint"/>" onClick="window.print()"/>
                    </td>
                </tr>
            </table>

            <br/>

            <table style="width:100%">
                <tr>
                    <td class="label">Provider:</td>
                    <td><%=Encode.forHtml(String.valueOf(providerDao.getProviderName(drug.getProviderNo())))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Patient:</td>
                    <td><%=Encode.forHtml(String.valueOf(demographicDao.getDemographic("" + drug.getDemographicId()).getDisplayName()))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Drug Name:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getDrugName())))%>
                    </td>
                </tr>
                <% if (drug.getBrandName() != null && !drug.getBrandName().equalsIgnoreCase("null")) { %>
                <tr>
                    <td class="label">Brand Name:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getBrandName()))%>
                    </td>
                </tr>
                <%}%>
                <% if (drug.getCustomName() != null && !drug.getCustomName().equalsIgnoreCase("null")) { %>
                <tr>
                    <td class="label">Drug Name:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getCustomName()))%>
                    </td>
                </tr>
                <%}%>
                <tr style="height:15px">

                </tr>
                <tr>
                    <td class="label">Rx Date:</td>
                    <td><%=Encode.forHtml(String.valueOf(partialDateDao.getDatePartial(UtilDateUtilities.DateToString(drug.getRxDate()), PartialDate.DRUGS, drug.getId(), PartialDate.DRUGS_STARTDATE)))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Rx End Date:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getEndDate()))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Written Date:</td>
                    <td><%=Encode.forHtml(String.valueOf(partialDateDao.getDatePartial(UtilDateUtilities.DateToString(drug.getWrittenDate()), PartialDate.DRUGS, drug.getId(), PartialDate.DRUGS_WRITTENDATE)))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Create Date:</td>
                    <td><%=Encode.forHtml(String.valueOf(dateTimeFormatter.format(drug.getCreateDate())))%>
                    </td>
                </tr>
                <tr style="height:15px">

                </tr>
                <tr>
                    <td class="label">Preferred Pharmacy:</td>
                    <td>
                        ${ pharmacy.name }
                    </td>
                </tr>
                <tr>
                    <td class="label">ATC:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getAtc())))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">DIN:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getRegionalIdentifier())))%>
                    </td>
                </tr>
                <tr style="height:15px">

                </tr>
                <tr>
                    <td class="label">Rx Text:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getSpecial()))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Special Instructions:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getSpecialInstruction()))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Dosage:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getDosageDisplay()))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Frequency:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getFreqCode()))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Duration:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getDuration()))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Duration Unit:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getDurUnit()))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Quantity:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getQuantity()))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Repeats:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getRepeat()))%>
                    </td>
                </tr>
                <tr style="height:15px">

                </tr>
                <tr>
                    <td class="label">Refill Duration:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getRefillDuration() != null ? drug.getRefillDuration() : ""))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Refill Quantity:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getRefillQuantity() != null ? drug.getRefillQuantity() : ""))%>
                    </td>
                </tr>
                <tr style="height:15px">

                </tr>
                <tr>
                    <td class="label">Dispense Interval:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getDispenseInterval()))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Pickup Date/Time:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getPickUpDateTime() != null ? dateTimeFormatter.format(drug.getPickUpDateTime()) : ""))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Unit:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getUnit())))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Method:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getMethod())))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Route:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getRoute())))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Form:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getDrugForm())))%> &nbsp; (<a href="javascript:void()"
                                                                                      onClick="updateForm();return false;">Update</a>)
                    </td>
                </tr>
                <tr>
                    <td class="label">Strength:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getDosage())))%>
                    </td>
                </tr>
                <% if (drug.getUnitName() != null && !drug.getUnitName().equalsIgnoreCase("null")) { %>
                <tr>
                    <td class="label">Strength:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getUnitName())))%>
                    </td>
                </tr>
                <%}%>
                <tr style="height:15px">

                </tr>
                <tr>
                    <td class="label">Long Term:</td>
                    <td>
                        <% if (drug.getLongTerm() == null) { %>
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="WriteScript.msgUnset"/>
                        <% } else if (drug.getLongTerm()) { %>
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="WriteScript.msgYes"/>
                        <% } else { %>
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="WriteScript.msgNo"/>
                        <% } %>
                    </td>
                </tr>
                <tr>
                    <td class="label">Short Term:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getShortTerm() != null ? drug.getShortTerm() : ""))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Past Medication:</td>
                    <td>
                        <% if (drug.getPastMed() == null) { %>
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="WriteScript.msgUnset"/>
                        <% } else if (drug.getPastMed()) { %>
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="WriteScript.msgYes"/>
                        <% } else { %>
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="WriteScript.msgNo"/>
                        <% } %>
                    </td>
                </tr>
                <tr>
                    <td class="label">Patient Compliance:</td>
                    <td>
                        <% if (drug.getPatientCompliance() == null) { %>
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="WriteScript.msgUnset"/>
                        <% } else if (drug.getPatientCompliance()) { %>
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="WriteScript.msgYes"/>
                        <% } else { %>
                        <fmt:setBundle basename="oscarResources"/><fmt:message key="WriteScript.msgNo"/>
                        <% } %>
                    </td>

                </tr>
                <tr>
                    <td class="label">Last Refill:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getLastRefillDate() != null ? dateTimeFormatter.format(drug.getLastRefillDate()) : ""))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">eTreatment:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getETreatmentType())))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Status:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getRxStatus())))%>
                    </td>
                </tr>
                <%if (drug.getOutsideProviderName() != null || drug.getOutsideProviderOhip() != null) { %>
                <tr style="height:15px">

                </tr>
                <tr>
                    <td class="label">Outside Provider Name:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getOutsideProviderName())))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Outside Provider OHIP:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getOutsideProviderOhip())))%>
                    </td>
                </tr>
                <% } %>
                <%if (drug.getArchivedDate() != null || drug.getArchivedReason() != null) { %>
                <tr style="height:15px">

                </tr>
                <tr>
                    <td class="label">Archived Date:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.getArchivedDate() != null ? dateTimeFormatter.format(drug.getArchivedDate()) : ""))%>
                    </td>
                </tr>
                <tr>
                    <td class="label">Archived Reason:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getArchivedReason())))%>
                    </td>
                </tr>
                <% } %>

                <tr>
                    <td class="label">Protocol Reference:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getProtocol())))%>
                    </td>
                </tr>


                <tr>
                    <td class="label" style="vertical-align:top;">Prior Prescription Reference:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getPriorRxProtocol())))%>
                    </td>
                </tr>


                <tr>
                    <td class="label">Non Authorative:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.isNonAuthoritative() != null ? drug.isNonAuthoritative().toString() : "")))%>
                    </td>
                </tr>

                <tr>
                    <td class="label">Substitution Not Allowed:</td>
                    <td><%=Encode.forHtml(String.valueOf(drug.isNoSubs()))%>
                    </td>
                </tr>

                <tr>
                    <td class="label">Problem Code:</td>
                    <td>
                        ${ ProblemCode }
                    </td>
                </tr>
                <tr style="height:15px">

                </tr>
                <tr>
                    <td class="label">Comment:</td>
                    <td><%=Encode.forHtml(String.valueOf(StringUtils.trimToEmpty(drug.getComment())))%>
                    </td>
                </tr>
            </table>

            <br/>

            <input type="button" value="Annotation" title="Annotation" class="ControlPushButton"
                   onclick="window.open('<%= request.getContextPath() %>/annotation/annotation.jsp?display=<%=Encode.forJavaScript(String.valueOf(ca.openosp.openo.casemgmt.model.CaseManagementNoteLink.DISP_PRESCRIP))%>&table_id=<%=Encode.forJavaScript(String.valueOf(drug.getId()))%>&demo=<%=Encode.forJavaScript(String.valueOf(drug.getDemographicId()))%>','anwin','width=400,height=500');">


            <%--
            Unused Items

            ID: <%=Encode.forHtml(String.valueOf(drug.getId()))%><br>
            Audit: <%=Encode.forHtml(String.valueOf(drug.getAuditString()))%><br>
            Full: <%=Encode.forHtml(String.valueOf(drug.getFullOutLine()))%><br>
            Remote Facility Name: <%=Encode.forHtml(String.valueOf(drug.getRemoteFacilityName()))%><br>
            Facility Id: <%=Encode.forHtml(String.valueOf(drug.getRemoteFacilityId()))%><br>
            Position: <%=Encode.forHtml(String.valueOf(drug.getPosition()))%><br>
            Start Date Unknown: <%=Encode.forHtml(String.valueOf(drug.getStartDateUnknown()))%><br>
            Script No: <%=Encode.forHtml(String.valueOf(drug.getScriptNo()))%><br>
            hide for cpp: <%=Encode.forHtml(String.valueOf(drug.getHideFromCpp()))%><br>
            GCN: <%=Encode.forHtml(String.valueOf(drug.getGcnSeqNo()))%><br>
            Gen Name: <%=Encode.forHtml(String.valueOf(drug.getGenericName()))%><br>
            Min: <%=Encode.forHtml(String.valueOf(drug.getTakeMin()))%><br>
            Max: <%=Encode.forHtml(String.valueOf(drug.getTakeMax()))%><br>
             --%>

        </td>
    </tr>
</table>
</body>
</html>
