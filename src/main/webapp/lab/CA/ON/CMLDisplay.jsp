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

<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_lab" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_lab");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<%@page import="ca.openosp.openo.commn.model.PatientLabRouting" %>
<%@page import="ca.openosp.openo.util.ConversionUtils" %>
<%@page import="ca.openosp.openo.commn.dao.PatientLabRoutingDao" %>
<%@page errorPage="/errorpage.jsp" %>
<%@ page
        import="java.util.*, ca.openosp.openo.mds.data.*,ca.openosp.openo.lab.ca.on.CML.*,ca.openosp.openo.lab.LabRequestReportLink,ca.openosp.openo.db.*,java.sql.*,ca.openosp.openo.log.*,ca.openosp.openo.utility.SpringUtils,ca.openosp.openo.casemgmt.service.CaseManagementManager,ca.openosp.openo.casemgmt.model.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="oscarResources"/>
<%@ taglib uri="/WEB-INF/oscar-tag.tld" prefix="oscar" %>
<%

    String segmentID = request.getParameter("segmentID");
    MDSSegmentData mDSSegmentData = new MDSSegmentData();

    CMLLabTest lab = new CMLLabTest();
    lab.populateLab(segmentID);

    Long reqIDL = LabRequestReportLink.getIdByReport("labPatientPhysicianInfo", Long.valueOf(segmentID));
    String reqID = reqIDL == null ? "" : reqIDL.toString();
    reqIDL = LabRequestReportLink.getRequestTableIdByReport("labPatientPhysicianInfo", Long.valueOf(segmentID));
    String reqTableID = reqIDL == null ? "" : reqIDL.toString();

    String annotation_display = CaseManagementNoteLink.DISP_LABTEST2;
    CaseManagementManager caseManagementManager = (CaseManagementManager) SpringUtils.getBean(CaseManagementManager.class);

%>
<%

    PatientLabRoutingDao dao = SpringUtils.getBean(PatientLabRoutingDao.class);
    PatientLabRouting routing = dao.findByLabNo(ConversionUtils.fromIntString(segmentID));

    String demographicID = "";
    if (routing != null) {
        demographicID = ConversionUtils.toIntString(routing.getDemographicNo());
    }

    if (lab.demographicNo != null && !lab.demographicNo.equals("null")) {
        LogAction.addLog((String) session.getAttribute("user"), LogConst.READ, LogConst.CON_HL7_LAB, segmentID, request.getRemoteAddr(), lab.demographicNo);
    } else {
        LogAction.addLog((String) session.getAttribute("user"), LogConst.READ, LogConst.CON_HL7_LAB, segmentID, request.getRemoteAddr());
    }
%>
<%
    /*
    String ackStatus = request.getParameter("status");
    if ( request.getParameter("searchProviderNo") == null || request.getParameter("searchProviderNo").equals("") ) {
        ackStatus = "U";
    } */
//mDSSegmentData.populateMDSSegmentData(segmentID);

//PatientData.Patient pd = new PatientData().getPatient(segmentID);
    String AbnFlag = "";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="ca.openosp.openo.utility.MiscUtils" %>
<%@ page import="ca.openosp.openo.log.LogAction" %>
<%@ page import="ca.openosp.openo.log.LogConst" %>
<%@ page import="ca.openosp.openo.lab.ca.on.CML.CMLLabTest" %>
<%@ page import="ca.openosp.openo.mds.data.ReportStatus" %>
<%@ page import="ca.openosp.openo.mds.data.MDSSegmentData" %>
<%@ page import="ca.openosp.openo.casemgmt.model.CaseManagementNoteLink" %>
<%@ page import="ca.openosp.openo.casemgmt.model.CaseManagementNote" %>
<%@ page import="org.owasp.encoder.Encode" %>
<html>
<head>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
    <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
    <title><%=Encode.forHtml(String.valueOf(lab.pLastName))%>, <%=Encode.forHtml(String.valueOf(lab.pFirstName))%> <fmt:message key="oscarMDS.segmentDisplay.title"/></title>
    <script language="javascript" type="text/javascript"
            src="<%= request.getContextPath() %>/share/javascript/Oscar.js"></script>
    <link rel="stylesheet" type="text/css"
          href="<%= request.getContextPath() %>/share/css/OscarStandardLayout.css">
    <link rel="stylesheet" type="text/css" media="all" href="<%= request.getContextPath() %>/share/css/extractedFromPages.css"/>
</head>

<script language="JavaScript">
    function getComment() {
        var ret = true;
        var commentVal = prompt('<fmt:message key="oscarMDS.segmentDisplay.msgComment"/>', '');

        if (commentVal == null)
            ret = false;
        else
            document.acknowledgeForm.comment.value = commentVal;

        return ret;
    }

    function popupStart(vheight, vwidth, varpage, windowname) {
        var page = varpage;
        windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes";
        var popup = window.open(varpage, windowname, windowprops);
    }

    function linkreq(rptId, reqId) {
        var link = "<%= request.getContextPath() %>/lab/LinkReq.jsp?table=labPatientPhysicianInfo&rptid=" + rptId + "&reqid=" + reqId + "<%=Encode.forJavaScript(String.valueOf(demographicID != null ? "&demographicNo=" + demographicID : ""))%>";
        window.open(link, "linkwin", "width=500, height=200");
    }
</script>

<body>
<!-- form forwarding of the lab -->
<form name="reassignForm" method="post" action="<%= request.getContextPath() %>/lab/CA/ON/Forward.do"><input
        type="hidden" name="flaggedLabs"
        value="<%=Encode.forHtmlAttribute(String.valueOf(segmentID))%>"/> <input
        type="hidden" name="selectedProviders" value=""/>
    <input type="hidden" name="favorites" value=""/>
    <input type="hidden" name="labType" value="CML"/> <input type="hidden"
                                                             name="labType<%=Encode.forHtmlAttribute(String.valueOf(segmentID))%>CML"
                                                             value="imNotNull"/> <input type="hidden" name="providerNo"
                                                                                        value="<%= Encode.forHtmlAttribute(request.getParameter("providerNo")) %>"/>
</form>
<form name="acknowledgeForm" method="post"
      action="<%=request.getContextPath()%>/oscarMDS/UpdateStatus.do">

    <table width="100%" height="100%" border="0" cellspacing="0"
           cellpadding="0">
        <tr>
            <td valign="top">
                <table width="100%" border="0" cellspacing="0" cellpadding="3">
                    <tr>
                        <td align="left" class="MainTableTopRowRightColumn" width="100%">
                            <input type="hidden" name="segmentID"
                                   value="<%=Encode.forHtmlAttribute(String.valueOf(segmentID))%>"/> <input
                                type="hidden" name="providerNo"
                                value="<%= Encode.forHtmlAttribute(request.getParameter("providerNo")) %>"/> <input
                                type="hidden" name="status" value="A"/> <input type="hidden"
                                                                               name="comment" value=""/> <input
                                type="hidden" name="labType"
                                value="CML"/> <% if (request.getParameter("providerNo") != null /*&& ! mDSSegmentData.getAcknowledgedStatus(request.getParameter("providerNo")) */) { %>
                            <input type="submit"
                                   value="<fmt:message key="oscarMDS.segmentDisplay.btnAcknowledge"/>"
                                   onclick="return getComment();"> <% } %> <input type="button"
                                                                                  class="smallButton"
                                                                                  value="<fmt:message key="oscarMDS.index.btnForward"/>"
                                                                                  onClick="popupStart(397, 700, '<%= request.getContextPath() %>/oscarMDS/SelectProvider.jsp', 'providerselect')">
                            <input type="button" value=" <fmt:message key="global.btnClose"/> "
                                   onClick="window.close()"> <input type="button"
                                                                    value=" <fmt:message key="global.btnPrint"/> "
                                                                    onClick="window.print()"> <% if (lab.getDemographicNo() != null && !lab.getDemographicNo().equals("") && !lab.getDemographicNo().equalsIgnoreCase("null")) { %>
                            <input type="button" value="Msg"
                                   onclick="popup(700,960,'${pageContext.request.contextPath}/messenger/SendDemoMessage.do?demographic_no=<%=Encode.forJavaScript(String.valueOf(lab.getDemographicNo()))%>','msg')"/>
                            <input type="button" value="Tickler"
                                   onclick="popup(450,600,'${pageContext.request.contextPath}/tickler/ForwardDemographicTickler.do?demographic_no=<%=Encode.forJavaScript(String.valueOf(lab.getDemographicNo()))%>','tickler')"/>
                            <% } %> <% if (request.getParameter("searchProviderNo") != null) { // we were called from e-chart %>
                            <input type="button"
                                   value=" <fmt:message key="oscarMDS.segmentDisplay.btnEChart"/> "
                                   onClick="popupStart(360, 680, '${pageContext.request.contextPath}/oscarMDS/SearchPatient.do?labType=CML&segmentID=<%=Encode.forJavaScript(String.valueOf(segmentID))%>&name=<%=Encode.forJavaScript(String.valueOf(java.net.URLEncoder.encode(lab.pLastName+", "+lab.pFirstName )))%>', 'searchPatientWindow')">
                            <% } %>
                            <input type="button" value="Req# <%=Encode.forHtmlAttribute(String.valueOf(reqTableID))%>" title="Link to Requisition"
                                   onclick="linkreq('<%=Encode.forJavaScript(String.valueOf(segmentID))%>','<%=Encode.forJavaScript(String.valueOf(reqID))%>');"/>
                            <span class="Field2"><i>Next Appointment: <oscar:nextAppt
                                    demographicNo="<%=Encode.forHtmlAttribute(String.valueOf(lab.getDemographicNo()))%>"/></i></span></td>
                    </tr>
                </table>


                <table width="100%" border="1" cellspacing="0" cellpadding="3"
                       bgcolor="#9999CC" bordercolordark="#bfcbe3">
                    <%
                        if (lab.multiLabId != null) {
                            String[] multiID = lab.multiLabId.split(",");
                            if (multiID.length > 1) {
                    %>
                    <tr>
                        <td class="Cell" colspan="2" align="middle">
                            <div class="Field2">Version:&#160;&#160; <%
                                for (int i = 0; i < multiID.length; i++) {
                                    if (multiID[i].equals(segmentID)) {
                            %>v<%=Encode.forHtml(String.valueOf(i + 1))%>&#160;<%
                            } else {
                                if (request.getParameter("searchProviderNo") != null) { // null if we were called from e-chart
                            %><a
                                    href="CMLDisplay.jsp?segmentID=<%=Encode.forUriComponent(String.valueOf(multiID[i]))%>&multiID=<%=Encode.forUriComponent(String.valueOf(lab.multiLabId))%>&providerNo=<%=Encode.forUriComponent(request.getParameter("providerNo"))%>&searchProviderNo=<%=Encode.forUriComponent(request.getParameter("searchProviderNo"))%>">v<%=Encode.forHtml(String.valueOf(i + 1))%>
                            </a>&#160;<%
                            } else {
                            %><a
                                    href="CMLDisplay.jsp?segmentID=<%=Encode.forUriComponent(String.valueOf(multiID[i]))%>&multiID=<%=Encode.forUriComponent(String.valueOf(lab.multiLabId))%>&providerNo=<%=Encode.forUriComponent(request.getParameter("providerNo"))%>">v<%=Encode.forHtml(String.valueOf(i + 1))%>
                            </a>&#160;<%
                                        }
                                    }
                                }
                            %>
                            </div>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                    <tr>
                        <td width="66%" align="middle" class="Cell">
                            <div class="Field2"><fmt:message key="oscarMDS.segmentDisplay.formDetailResults"/></div>
                        </td>
                        <td width="33%" align="middle" class="Cell">
                            <div class="Field2"><fmt:message key="oscarMDS.segmentDisplay.formResultsInfo"/></div>
                        </td>
                    </tr>
                    <tr>
                        <td bgcolor="white" valign="top">
                            <table valign="top" border="0" cellpadding="2" cellspacing="0"
                                   width="100%">
                                <tr valign="top">
                                    <td valign="top" width="33%" align="left">
                                        <table width="100%" border="0" cellpadding="2" cellspacing="0"
                                               valign="top">
                                            <tr>
                                                <td valign="top" align="left">
                                                    <table valign="top" border="0" cellpadding="3" cellspacing="0"
                                                           width="100%">
                                                        <tr>
                                                            <td colspan="2" nowrap>
                                                                <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formPatientName"/>: </strong>
                                                                </div>
                                                            </td>
                                                            <td colspan="2" nowrap>
                                                                <div class="FieldData" nowrap="nowrap">
                                                                    <% if (request.getParameter("searchProviderNo") == null) { // we were called from e-chart %>
                                                                    <a href="javascript:window.close()"><%=Encode.forHtml(String.valueOf(lab.pLastName))%>
                                                                        , <%=Encode.forHtml(String.valueOf(lab.pFirstName))%>
                                                                    </a>
                                                                    <% } else { // we were called from lab module %>
                                                                    <a
                                                                            href="javascript:popupStart(360, 680, '${pageContext.request.contextPath}/oscarMDS/SearchPatient.do?labType=CML&segmentID=<%=Encode.forUriComponent(String.valueOf(segmentID))%>&name=<%=Encode.forUriComponent(String.valueOf(lab.pLastName+", "+lab.pFirstName))%>', 'searchPatientWindow')">
                                                                        <%=Encode.forHtml(String.valueOf(lab.pLastName))%>, <%=Encode.forHtml(String.valueOf(lab.pFirstName))%>
                                                                    </a> <% } %></div>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td colspan="2" nowrap>
                                                                <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formDateBirth"/>: </strong>
                                                                </div>
                                                            </td>
                                                            <td colspan="2" nowrap>
                                                                <div class="FieldData" nowrap="nowrap"><%=Encode.forHtml(String.valueOf(lab.pDOB))%>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td colspan="2" nowrap>
                                                                <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formAge"/>: </strong><%=Encode.forHtml(String.valueOf(lab.getAge()))%> <%
                                                                    try {
                                                                        lab.getAge();
                                                                    } catch (Exception e) {
                                                                        MiscUtils.getLogger().error("Error", e);
                                                                    }

                                                                %>
                                                                </div>
                                                            </td>
                                                            <td colspan="2" nowrap>
                                                                <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formSex"/>: </strong><%=Encode.forHtml(String.valueOf(lab.pSex))%>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td colspan="2" nowrap>
                                                                <div class="FieldData"><strong>
                                                                    <% if (!lab.pHealthNum.startsWith("X")) {%>
                                                                    <fmt:message key="oscarMDS.segmentDisplay.formHealthNumber"/> <%} else {%>
                                                                    <fmt:message key="oscarMDS.segmentDisplay.formMDSIDNumber"/>
                                                                    <%}%></strong></div>
                                                            </td>
                                                            <td colspan="2" nowrap>
                                                                <div class="FieldData"
                                                                     nowrap="nowrap"><%=Encode.forHtml(String.valueOf(lab.pHealthNum))%>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                                <td width="33%" valign="top">
                                                    <table valign="top" border="0" cellpadding="3" cellspacing="0"
                                                           width="100%">
                                                        <tr>
                                                            <td nowrap>
                                                                <div align="left" class="FieldData">
                                                                    <strong><fmt:message key="oscarMDS.segmentDisplay.formHomePhone"/>: </strong>
                                                                </div>
                                                            </td>
                                                            <td nowrap>
                                                                <div align="left" class="FieldData"
                                                                     nowrap="nowrap"><%=Encode.forHtml(String.valueOf(lab.pPhone))%>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td nowrap>
                                                                <div align="left" class="FieldData">
                                                                    <strong><fmt:message key="oscarMDS.segmentDisplay.formWorkPhone"/>: </strong>
                                                                </div>
                                                            </td>
                                                            <td nowrap>
                                                                <div align="left" class="FieldData" nowrap="nowrap">
                                                                    &nbsp;
                                                                </div>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td nowrap>
                                                                <div align="left" class="FieldData"
                                                                     nowrap="nowrap"></div>
                                                            </td>
                                                            <td nowrap>
                                                                <div align="left" class="FieldData"
                                                                     nowrap="nowrap"></div>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td nowrap>
                                                                <div align="left" class="FieldData">
                                                                    <strong><fmt:message key="oscarMDS.segmentDisplay.formPatientLocation"/>: </strong>
                                                                </div>
                                                            </td>
                                                            <td nowrap>
                                                                <div align="left" class="FieldData"
                                                                     nowrap="nowrap"><%=Encode.forHtml(String.valueOf(""/*not sure on what goes here*/))%>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td bgcolor="white" valign="top">
                            <table width="100%" border="0" cellspacing="0" cellpadding="1">
                                <tr>
                                    <td>
                                        <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formDateService"/>:</strong></div>
                                    </td>
                                    <td>
                                        <div class="FieldData" nowrap="nowrap"><%=Encode.forHtml(String.valueOf(lab.serviceDate))%>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formReportStatus"/>:</strong></div>
                                    </td>
                                    <td>
                                        <div class="FieldData"
                                             nowrap="nowrap"><%=Encode.forHtml(String.valueOf(((String) (lab.status.equals("F") ? "Final" : "Partial"))))%>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td></td>
                                </tr>
                                <tr>
                                    <td nowrap>
                                        <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formClientRefer"/>:</strong></div>
                                    </td>
                                    <td nowrap>
                                        <div class="FieldData" nowrap="nowrap"><%=Encode.forHtml(String.valueOf(lab.docNum))%>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formAccession"/>:</strong></div>
                                    </td>
                                    <td>
                                        <div class="FieldData" nowrap="nowrap"><%=Encode.forHtml(String.valueOf(lab.accessionNum))%>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="FieldData"><strong>&nbsp;</strong></div>
                                    </td>
                                    <td>
                                        <div class="FieldData" nowrap="nowrap">&nbsp;</div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td bgcolor="white" colspan="2">
                            <table width="100%" border="0" cellpadding="0" cellspacing="0"
                                   bordercolor="#CCCCCC">
                                <tr>
                                    <td bgcolor="white">
                                        <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formRequestingClient"/>: </strong> <%=Encode.forHtml(String.valueOf(lab.docName))%>
                                        </div>
                                    </td>
                                    <td bgcolor="white">
                                        <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formReportToClient"/>: </strong> <%=Encode.forHtml(String.valueOf(""/*mDSSegmentData.providers.admittingDoctor not sure*/))%>
                                        </div>
                                    </td>
                                    <td bgcolor="white" align="right">
                                        <div class="FieldData"><strong><fmt:message key="oscarMDS.segmentDisplay.formCCClient"/>: </strong> <%=Encode.forHtml(String.valueOf("" /* mDSSegmentData.providers.consultingDoctor*/))%>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <% if (!lab.status.equals("U")) { %>
                    <tr>
                        <td align="center" bgcolor="white" colspan="2">
                            <%
                                String[] multiID = lab.multiLabId.split(",");
                                boolean startFlag = false;
                                for (int j = multiID.length - 1; j >= 0; j--) {
                                    if (multiID[j].equals(segmentID))
                                        startFlag = true;
                                    if (startFlag) {
                                        ArrayList statusArray = lab.getStatusArray(multiID[j]);
                                        if (statusArray.size() > 0) {
                            %>

                            <table width="100%" height="20" cellpadding="2" cellspacing="2">
                                <tr>
                                    <% if (multiID.length > 1) { %>
                                    <td align="center" bgcolor="white" width="20%" valign="top">
                                        <div class="FieldData"><b>Version:</b> v<%=Encode.forHtml(String.valueOf(j + 1))%>
                                        </div>
                                    </td>
                                    <td align="left" bgcolor="white" width="80%" valign="top">
                                            <% }else{ %>

                                    <td align="center" bgcolor="white">
                                        <% } %>
                                        <div class="FieldData">
                                            <!--center--> <% for (int i = 0; i < statusArray.size(); i++) {
                                            ReportStatus report = (ReportStatus) statusArray.get(i); %>
                                            <%=Encode.forHtml(String.valueOf(report.getProviderName()))%> : <font
                                                color="red"><%=Encode.forHtml(String.valueOf(report.getStatus()))%>
                                        </font>
                                            <% if (report.getStatus().equals("Acknowledged")) { %> <%=Encode.forHtml(String.valueOf(report.getTimestamp()))%>
                                            ,
                                            <%=Encode.forHtml(String.valueOf((report.getComment().equals("") ? "no comment" : "comment : " + report.getComment())))%>
                                            <% } %> <br>
                                            <% }
                                                if (statusArray.size() == 0) {
                                            %><font
                                                color="red">N/A</font>
                                            <%
                                                }
                                            %> <!--/center-->
                                        </div>
                                    </td>
                                </tr>
                            </table>
                            <%
                                        }
                                    }
                                }
                            %>
                        </td>
                    </tr>
                    <% } %>
                </table>


                <% int linenum = 0;
                    String highlight = "#E0E0FF";

                    ArrayList groupLabs = lab.getGroupResults(lab.labResults);

                    for (int i = 0; i < groupLabs.size(); i++) {
                        linenum = 0;
                        CMLLabTest.GroupResults gResults = (CMLLabTest.GroupResults) groupLabs.get(i);
                %>
                <table style="page-break-inside: avoid;" bgcolor="#003399" border="0"
                       cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td colspan="4" height="7">&nbsp;</td>
                    </tr>
                    <tr>
                        <td bgcolor="#FFCC00" width="200" height="22" valign="bottom">
                            <div class="Title2"><%=Encode.forHtml(String.valueOf(gResults.groupName))%>
                            </div>
                        </td>
                        <td align="right" bgcolor="#FFCC00" width="100">&nbsp;</td>
                        <td width="9">&nbsp;</td>
                        <td width="*">&nbsp;</td>
                    </tr>
                </table>

                <table width="100%" border="0" cellspacing="0" cellpadding="2"
                       bgcolor="#CCCCFF" bordercolor="#9966FF" bordercolordark="#bfcbe3"
                       name="tblDiscs" id="tblDiscs">
                    <tr class="Field2" style="font-weight:bold;">
                        <td width="25%" align="middle" valign="bottom" class="Cell"><fmt:message key="oscarMDS.segmentDisplay.formTestName"/></td>
                        <td width="15%" align="middle" valign="bottom" class="Cell"><fmt:message key="oscarMDS.segmentDisplay.formResult"/></td>
                        <td width="5%" align="middle" valign="bottom" class="Cell"><fmt:message key="oscarMDS.segmentDisplay.formAbn"/></td>
                        <td width="15%" align="middle" valign="bottom" class="Cell"><fmt:message key="oscarMDS.segmentDisplay.formReferenceRange"/></td>
                        <td width="10%" align="middle" valign="bottom" class="Cell"><fmt:message key="oscarMDS.segmentDisplay.formUnits"/></td>
                        <td width="15%" align="middle" valign="bottom" class="Cell"><fmt:message key="oscarMDS.segmentDisplay.formDateTimeCompleted"/></td>
                        <td width="5%" align="middle" valign="bottom" class="Cell"><fmt:message key="oscarMDS.segmentDisplay.formTestLocation"/></td>
                        <td width="5%" align="middle" valign="bottom" class="Cell"><fmt:message key="oscarMDS.segmentDisplay.formNew"/></td>
                        <td width="5%" align="middle" valign="bottom" class="Cell"><fmt:message key="oscarMDS.segmentDisplay.formAnnotate"/></td>
                    </tr>

                    <%
                        //int linenum = 1;
                        ArrayList labs = gResults.getLabResults();
                        for (int l = 0; l < labs.size(); l++) {

                            boolean isPrevAnnotation = false;
                            CaseManagementNoteLink cml = caseManagementManager.getLatestLinkByTableId(CaseManagementNoteLink.LABTEST2, Long.valueOf(segmentID), i + "-" + l);
                            CaseManagementNote p_cmn = null;
                            if (cml != null) {
                                p_cmn = caseManagementManager.getNote(cml.getNoteId().toString());
                            }
                            if (p_cmn != null) {
                                isPrevAnnotation = true;
                            }

                            CMLLabTest.LabResult thisResult = (CMLLabTest.LabResult) labs.get(l);
                            String lineClass = "NormalRes";
                            if (thisResult.abn != null && thisResult.abn.equals("A")) {
                                lineClass = "AbnormalRes";
                            }
                            if (thisResult.isLabResult()) {
                    %>

                    <tr bgcolor="<%=Encode.forHtmlAttribute(String.valueOf((linenum % 2 == 1 ? highlight : "")))%>"
                        class="<%=Encode.forHtmlAttribute(String.valueOf(lineClass))%>">
                        <td valign="top" align="left"><a
                                href="labValues.jsp?testName=<%=Encode.forUriComponent(String.valueOf(thisResult.testName))%>&demo=<%=Encode.forUriComponent(String.valueOf(lab.getDemographicNo()))%>&labType=CML"><%=Encode.forHtml(String.valueOf(thisResult.testName))%>
                        </a></td>
                        <td align="center"><%=Encode.forHtml(String.valueOf(thisResult.result))%>
                        </td>
                        <td align="center"><%=Encode.forHtml(String.valueOf(thisResult.abn))%>
                        </td>
                        <td align="center"><%=Encode.forHtml(String.valueOf(thisResult.getReferenceRange()))%>
                        </td>
                        <td align="center"><%=Encode.forHtml(String.valueOf(thisResult.units))%>
                        </td>
                        <td align="center"><%=Encode.forHtml(String.valueOf(lab.collectionDate))%>
                        </td>
                        <td align="center"><%=Encode.forHtml(String.valueOf(thisResult.locationId))%>
                        </td>
                        <td align="center"><%=Encode.forHtml(String.valueOf(""/*thisResult.resultStatus*/))%>
                        </td>
                        <td align="center" valign="top">
                            <a href="javascript:void(0);" title="Annotation"
                               onclick="window.open('<%=request.getContextPath()%>/annotation/annotation.jsp?display=<%=Encode.forJavaScript(String.valueOf(annotation_display))%>&amp;table_id=<%=Encode.forJavaScript(String.valueOf(segmentID))%>&amp;demo=<%=Encode.forJavaScript(String.valueOf(lab.getDemographicNo()))%>&amp;other_id=<%=Encode.forJavaScript(String.valueOf(String.valueOf(i) + "-" + String.valueOf(l)))%>','anwin','width=400,height=500');">
                                <%if (!isPrevAnnotation) { %><img src="<%= request.getContextPath() %>/images/notes.gif" alt="rxAnnotation"
                                                                  height="16" width="13" border="0"/><%} else { %><img
                                    src="<%= request.getContextPath() %>/images/filledNotes.gif" alt="rxAnnotation" height="16" width="13"
                                    border="0"/> <%} %>
                            </a>
                        </td>
                    </tr>
                    <% } else {%>
                    <tr bgcolor="<%=Encode.forHtmlAttribute(String.valueOf((linenum % 2 == 1 ? highlight : "")))%>"
                        class="<%=Encode.forHtmlAttribute(String.valueOf(lineClass))%>">
                        <td valign="top" align="left" colspan="8"><pre
                                style="margin-left: 100px;"><%=Encode.forHtml(String.valueOf(thisResult.description))%></pre>
                        </td>

                    </tr>

                    <% }%>

                    <%}/*for lab.size*/%>
                </table>
                <% //} // end if microbiology or not microbiology
                }  // for i=0... (headers) %> <!-- <table border="0" width="100%" cellpadding="5" cellspacing="0" bgcolor="white">
                <tr class="Field2">
                    <td width="20%" class="Cell2">
                        <div class="Field2" align="left">
                            <font color="white"></font>
                        </div>
                    </td>
                    <td width="60%" class="Cell2" valign="center" align="middle"><font color="white"><i>END
                        OF REPORT</i></font></td>
                    <td width="20%" class="Cell2" valign="center" align="right">&nbsp;</td>
                </tr>
            </table> -->
                <table width="100%" border="0" cellspacing="0" cellpadding="3"
                       class="MainTableBottomRowRightColumn" bgcolor="#003399">
                    <tr>
                        <td align="left" width="50%">
                            <% if (request.getParameter("providerNo") != null /*&& ! mDSSegmentData.getAcknowledgedStatus(request.getParameter("providerNo")) */) { %>
                            <input type="submit"
                                   value="<fmt:message key="oscarMDS.segmentDisplay.btnAcknowledge"/>"
                                   onclick="getComment()"> <% } %> <input type="button"
                                                                          class="smallButton"
                                                                          value="<fmt:message key="oscarMDS.index.btnForward"/>"
                                                                          onClick="popupStart(397, 700, '<%= request.getContextPath() %>/oscarMDS/SelectProvider.jsp', 'providerselect')">
                            <input type="button" value=" <fmt:message key="global.btnClose"/> "
                                   onClick="window.close()"> <input type="button"
                                                                    value=" <fmt:message key="global.btnPrint"/> "
                                                                    onClick="window.print()"> <% if (lab.getDemographicNo() != null && !lab.getDemographicNo().equals("") && !lab.getDemographicNo().equalsIgnoreCase("null")) { %>
                            <input type="button" value="Msg"
                                   onclick="popup(700,960,'<%=request.getContextPath()%>/messenger/SendDemoMessage.do?demographic_no=<%=Encode.forJavaScript(String.valueOf(lab.getDemographicNo()))%>','msg')"/>
                            <input type="button" value="Tickler"
                                   onclick="popup(450,600,'${pageContext.request.contextPath}/tickler/ForwardDemographicTickler.do?demographic_no=<%=Encode.forJavaScript(String.valueOf(lab.getDemographicNo()))%>','tickler')"/>
                            <% } %> <% if (request.getParameter("searchProviderNo") != null) { // we were called from e-chart %>
                            <input type="button"
                                   value=" <fmt:message key="oscarMDS.segmentDisplay.btnEChart"/> "
                                   onClick="popupStart(360, 680, '${pageContext.request.contextPath}/oscarMDS/SearchPatient.do?labType=CML&segmentID=<%=Encode.forJavaScript(String.valueOf(segmentID))%>&name=<%=Encode.forJavaScript(String.valueOf(java.net.URLEncoder.encode(lab.pLastName+", "+lab.pFirstName )))%>', 'searchPatientWindow')">
                            <% } %>
                        </td>
                        <td width="50%" valign="center" align="left"><span
                                class="Field2"><i><fmt:message key="oscarMDS.segmentDisplay.msgReportEnd"/></i></span></td>

                    </tr>
                </table>
            </td>
        </tr>
    </table>

</form>

</body>
</html>
