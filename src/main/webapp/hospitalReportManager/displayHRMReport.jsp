<%--

    Copyright (c) 2008-2012 Indivica Inc.

    This software is made available under the terms of the
    GNU General Public License, Version 2, 1991 (GPLv2).
    License details are available via "indivica.ca/gplv2"
    and "gnu.org/licenses/gpl-2.0.html".

--%>
<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@page import="org.apache.commons.lang3.StringUtils,ca.openosp.openo.log.*" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@ page import="ca.openosp.OscarProperties" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="/WEB-INF/oscar-tag.tld" prefix="oscar" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_hrm" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_hrm");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
    Logger logger = MiscUtils.getLogger();
    HRMDocumentDao hrmDocumentDao = (HRMDocumentDao) SpringUtils.getBean(HRMDocumentDao.class);
    HRMDocumentToDemographicDao hrmDocumentToDemographicDao = (HRMDocumentToDemographicDao) SpringUtils.getBean(HRMDocumentToDemographicDao.class);
    HRMDocumentToProviderDao hrmDocumentToProviderDao = (HRMDocumentToProviderDao) SpringUtils.getBean(HRMDocumentToProviderDao.class);
    HRMDocumentSubClassDao hrmDocumentSubClassDao = (HRMDocumentSubClassDao) SpringUtils.getBean(HRMDocumentSubClassDao.class);
    HRMSubClassDao hrmSubClassDao = (HRMSubClassDao) SpringUtils.getBean(HRMSubClassDao.class);
    HRMCategoryDao hrmCategoryDao = (HRMCategoryDao) SpringUtils.getBean(HRMCategoryDao.class);
    HRMDocumentCommentDao hrmDocumentCommentDao = (HRMDocumentCommentDao) SpringUtils.getBean(HRMDocumentCommentDao.class);
    HRMProviderConfidentialityStatementDao hrmProviderConfidentialityStatementDao = (HRMProviderConfidentialityStatementDao) SpringUtils.getBean(HRMProviderConfidentialityStatementDao.class);
    HRMSendingFacilityDao hrmSendingFacilityDao = SpringUtils.getBean(HRMSendingFacilityDao.class);
%>

<%@page import="ca.openosp.openo.hospitalReportManager.*, ca.openosp.openo.hospitalReportManager.model.*, ca.openosp.openo.utility.SpringUtils, ca.openosp.openo.PMmodule.dao.ProviderDao" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="ca.openosp.openo.utility.MiscUtils" %>
<%@ page import="ca.openosp.openo.hospitalReportManager.dao.*" %>
<%@ page import="ca.openosp.openo.encounter.data.EctFormData" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.owasp.csrfguard.CsrfGuard" %>
<%@ page import="ca.openosp.openo.log.LogAction" %>
<%@ page import="ca.openosp.openo.log.LogConst" %>
<%@ page import="ca.openosp.openo.hospitalReportManager.HRMDisplayReport2Action" %>
<%@ page import="ca.openosp.openo.hospitalReportManager.HRMReportParser" %>
<%@ page import="ca.openosp.openo.hospitalReportManager.HRMReport" %>
<%@ page import="ca.openosp.openo.hospitalReportManager.model.*" %>
<%@ page import="ca.openosp.openo.hospitalReportManager.dao.*" %>
<%@ page import="ca.openosp.openo.hospitalReportManager.model.HRMReportCriteria" %>
<!DOCTYPE html>

<%
    Integer hrmReportId = Integer.parseInt(request.getParameter("id"));
    // Access ModelDriven criteria object from request attributes
    HRMReportCriteria criteria =
        (ca.openosp.openo.hospitalReportManager.model.HRMReportCriteria) request.getAttribute("criteria");
    boolean isListView = criteria != null && criteria.getListView() != null ? criteria.getListView() : false;
    String hrmReportTime = "";
    Integer hrmDuplicateNum = null;
    LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
    HRMDocument document = hrmDocumentDao.findById(hrmReportId).get(0);
    HRMReport hrmReport = null;
    Map<Integer, Date> dupReportDates = new HashMap<Integer, Date>();
    Map<Integer, Date> dupTimeReceived = new HashMap<Integer, Date>();
    HRMDocumentToDemographic demographicLink = null;
    List<HRMDocumentToDemographic> demographicLinkList = null;
    List<HRMCategory> hrmCategories = null;
    List<HRMDocumentToProvider> providerLinkList = null;
    List<HRMDocumentSubClass> subClassList;
    HRMDocumentToProvider thisProviderLink;
    HRMCategory category = null;
    List<HRMDocument> allDocumentsWithRelationship = null;
    List<HRMDocument> children = null;
    List<HRMDocumentComment> documentComments = null;
    String confidentialityStatement = null;


    if (document != null) {
        logger.debug("reading repotFile : " + document.getReportFile());
        hrmReport = HRMReportParser.parseReport(loggedInInfo, document.getReportFile());

        if (hrmReport != null) {
            hrmCategories = hrmCategoryDao.findAll();
            hrmReportTime = document.getTimeReceived().toString();
            hrmDuplicateNum = document.getNumDuplicatesReceived();

            demographicLinkList = hrmDocumentToDemographicDao.findByHrmDocumentId(document.getId());
            demographicLink = (demographicLinkList.size() > 0 ? demographicLinkList.get(0) : null);

            providerLinkList = hrmDocumentToProviderDao.findByHrmDocumentIdNoSystemUser(document.getId());


            subClassList = hrmDocumentSubClassDao.getSubClassesByDocumentId(document.getId());


            thisProviderLink = hrmDocumentToProviderDao.findByHrmDocumentIdAndProviderNo(document.getId(), loggedInInfo.getLoggedInProviderNo());


            if (thisProviderLink != null) {
                thisProviderLink.setViewed(1);
                hrmDocumentToProviderDao.merge(thisProviderLink);
            }

            HRMDocumentSubClass hrmDocumentSubClass = null;
            if (subClassList != null) {
                for (HRMDocumentSubClass temp : subClassList) {
                    if (temp.isActive()) {
                        hrmDocumentSubClass = temp;
                        break;
                    }
                }
            }

            if (document.getHrmCategoryId() != null) {
                List<HRMCategory> categoryResults = hrmCategoryDao.findById(document.getHrmCategoryId());
                if (categoryResults != null && !categoryResults.isEmpty()) {
                    category = categoryResults.get(0);
                }
            } else if (hrmDocumentSubClass != null) {
                String subClassName = hrmDocumentSubClass.getSubClass();
                String subClasMnemonic = hrmDocumentSubClass.getSubClassMnemonic();
                category = hrmCategoryDao.findBySubClassNameMnemonic(hrmDocumentSubClass.getSendingFacilityId(), subClassName + ':' + subClasMnemonic);

                if (category == null) {
                    HRMSubClass subClass = hrmSubClassDao.findApplicableSubClassMapping(document.getReportType(), subClassName, subClasMnemonic, hrmDocumentSubClass.getSendingFacilityId());
                    category = (subClass != null) ? subClass.getHrmCategory() : null;
                }
            } else {
                category = hrmCategoryDao.findBySubClassNameMnemonic("DEFAULT");
            }

            // Get all the other HRM documents that are either a child, sibling, or parent
            allDocumentsWithRelationship = hrmDocumentDao.findAllDocumentsWithRelationship(document.getId());
            request.setAttribute("allDocumentsWithRelationship", allDocumentsWithRelationship);

            // Get all the other HRM documents that are a child of this document
            children = hrmDocumentDao.getAllChildrenOf(document.getId());
            request.setAttribute("children", children);

            documentComments = hrmDocumentCommentDao.getCommentsForDocument(hrmReportId);


            confidentialityStatement = hrmProviderConfidentialityStatementDao.getConfidentialityStatementForProvider(loggedInInfo.getLoggedInProviderNo());


            String duplicateLabIdsString = StringUtils.trimToNull(request.getParameter("duplicateLabIds"));


            if (duplicateLabIdsString != null) {
                String[] duplicateLabIdsStringSplit = duplicateLabIdsString.split(",");
                for (String tempId : duplicateLabIdsStringSplit) {
                    HRMDocument doc = hrmDocumentDao.find(Integer.parseInt(tempId));
                    dupReportDates.put(Integer.parseInt(tempId), doc.getReportDate());
                    dupTimeReceived.put(Integer.parseInt(tempId), doc.getTimeReceived());
                }

            }
        }
    }

    ProviderDao providerDao = (ProviderDao) SpringUtils.getBean(ProviderDao.class);

    if (demographicLink != null) {
        LogAction.addLog((String) session.getAttribute("user"), LogConst.READ, LogConst.CON_HRM, "" + hrmReportId, request.getRemoteAddr(), "" + demographicLink.getDemographicNo());
    } else {
        LogAction.addLog((String) session.getAttribute("user"), LogConst.READ, LogConst.CON_HRM, "" + hrmReportId, request.getRemoteAddr());
    }

    String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    boolean obgynShortcuts = OscarProperties.getInstance().getProperty("show_obgyn_shortcuts", "false").equalsIgnoreCase("true");
    String formId = "0";


    String btnDisabled = "disabled";
    String demographicNo = "";
    if (demographicLink != null) {
        btnDisabled = "";
        demographicNo = demographicLink.getDemographicNo().toString();

        if (obgynShortcuts) {
            List<EctFormData.PatientForm> formsONAREnhanced = Arrays.asList(EctFormData.getPatientFormsFromLocalAndRemote(LoggedInInfo.getLoggedInInfoFromSession(request), demographicNo, "formONAREnhancedRecord", true));
            if (formsONAREnhanced != null && !formsONAREnhanced.isEmpty()) {
                formId = formsONAREnhanced.get(0).getFormId();
            }
        }
    }
    String csrfTokenJs = "{'" + Encode.forJavaScript(CsrfGuard.getInstance().getTokenName()) + "': '" + Encode.forJavaScript(CsrfGuard.getInstance().getTokenValue(request)) + "'}";

%>


<html>
<head>
    <title>HRM Report</title>
    <script src="${pageContext.request.contextPath}/csrfguard"></script>

    <script type="text/javascript"
            src="${pageContext.request.contextPath}/library/jquery/jquery-1.12.0.min.js"></script>
    <script type="text/javascript"
            src="${pageContext.request.contextPath}/library/jquery/jquery-ui-1.12.1.min.js"></script>
    <script language="javascript" type="text/javascript"
            src="${pageContext.request.contextPath}/share/javascript/Oscar.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/share/javascript/prototype.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/share/javascript/effects.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/share/javascript/controls.js"></script>

    <script type="text/javascript" src="${pageContext.request.contextPath}/share/yui/js/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/share/yui/js/connection-min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/share/yui/js/animation-min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/share/yui/js/datasource-min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/share/yui/js/autocomplete-min.js"></script>
    <script type="text/javascript"
            src="${pageContext.request.contextPath}/js/demographicProviderAutocomplete.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/hospitalReportManager/hrmActions.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/global.js"></script>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/library/jquery/jquery-ui-1.12.1.min.css"
          type="text/css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/share/yui/css/fonts-min.css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/share/yui/css/autocomplete.css"/>
    <link rel="stylesheet" type="text/css" media="all"
          href="${pageContext.request.contextPath}/share/css/demographicProviderAutocomplete.css"/>

    <style>


        table {
            width: 100%;
            border: none;
            margin: 0;
            padding: 0;
        }

        textarea {
            width: 100%;
        }

        div[id^='hrmdoc'] {
            display: flex;
            flex-direction: column;
            align-items: stretch;
        }

        #buttonBox {
            order: 1;
        }

        #reportViewer {
            display: flex;
            order: 2;
        }

        #hrmReportContent, #descriptionBox, #commentBox,
        #metadataBox, #infoBox, #duplicateAndSimilarBox, #duplicatesMessage {
            padding: 25px;
            margin: 10px;
            border: 1px solid black;
        }

        #hrmReportContent {
            flex-grow: 2;
            max-width: 100%;
            height: auto;
            width: auto \9;
            vertical-align: middle;
            box-shadow: 0px 1px 3px #333333;
            -webkit-box-shadow: 0px 1px 3px #333333;
            -moz-box-shadow: 0px 1px 3px #333333;
        }

        #descriptionBox {
            order: 4;

        }

        #commentBox {
            order: 5;
        }

        #metadataBox {
            order: 6;
        }

        #duplicateAndSimilarBox {
            order: 3;
        }

        #duplicatesMessage {
            order: 7;
        }

        #duplicatesMessage td, #duplicateAndSimilarBox td {
            text-align: center;
        }

        #infoBox {
            flex-grow: 1;
        }

        #infoBox th {
            text-align: right;
            vertical-align: top;
        }

        #hrmNotice {
            border-bottom: 1px solid black;
            padding-bottom: 15px;
            margin-bottom: 15px;
            font-style: italic;
        }

        .documentLink_statusC {
            background-color: red;
        }

        .documentComment {
            border: 1px solid black;
            margin: 10px;
        }


        #metadataBox th {
            text-align: right;
        }

        .boxButton {
            margin-top: 10px;
        }

        @media print {
            #infoBox {
                display: none;
            }

            .boxButton {
                display: none;
            }

            #hrmHeader {
                display: block;
            }
        }


    </style>

    <%
        // check for errors printing
        if (request.getAttribute("printError") != null && (Boolean) request.getAttribute("printError")) {
    %>
    <script language="JavaScript">
        alert("The HRM Report could not be printed due to an error.");
    </script>
    <% } %>

    <script type="text/javascript">
        var contextpath = "<%=request.getContextPath()%>";

        function popupPatient(height, width, url, windowName, docId, d) {
            if (!d) {
                d = $('demofind' + docId + 'hrm').value;
            }
            urlNew = url + d;
            return popup2(height, width, 0, 0, urlNew, windowName);
        }

        function popupPatientTickler(height, width, url, windowName, docId, d, n) {
            if (!d) {
                d = $('demofind' + docId + 'hrm').value;
            }
            urlNew = url + "method=edit&tickler.demographic_webName=" + n + "&tickler.demographicNo=" + d + "&docType=HRM&docId=" + docId;
            return popup2(height, width, 0, 0, urlNew, windowName);
        }

        function popupPage(vheight, vwidth, varpage) { //open a new popup window
            var page = "" + varpage;
            windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0";//360,680
            var popup = window.open(page, "groupno", windowprops);
            if (popup != null) {
                if (popup.opener == null) {
                    popup.opener = self;
                }
                popup.focus();
            }
        }

        function openReport(id) {
            popupPage(700, 1200, 'Display.do?id=' + id);

        }


    </script>
</head>
<body>

<% if (hrmReport == null) { %>
<h1>HRM report not found! Please check the file location.</h1>
<% return;
} %>

<div id="hrmdoc_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>">
    <div id="buttonBox">
        <input type="button" id="msgBtn_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>" value="Msg"
               onclick="popupPatient(700,960,'<%= request.getContextPath() %>/messenger/SendDemoMessage.do?demographic_no=','msg', '<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>','<%=Encode.forJavaScript(String.valueOf(demographicNo))%>')" <%=Encode.forHtmlAttribute(String.valueOf(btnDisabled))%>/>
        <input type="button" id="mainTickler_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>" value="Tickler"
               onClick="popupPatient(710, 1024,'<%= request.getContextPath() %>/tickler/ForwardDemographicTickler.do?docType=HRM&docId=<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>&demographic_no=', 'Tickler','<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>','<%=Encode.forJavaScript(String.valueOf(demographicNo))%>')" <%=Encode.forHtmlAttribute(String.valueOf(btnDisabled))%>>
        <input type="button" id="mainEchart_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>"
               value=" <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarMDS.segmentDisplay.btnEChart"/> "
               onClick="popupPatient(710, 1024,'<%= request.getContextPath() %>/oscarEncounter/IncomingEncounter.do?updateParent=false&reason=
               <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarMDS.segmentDisplay.labResults"/>&curDate=<%=Encode.forHtml(String.valueOf(currentDate))%>>&appointmentNo=&appointmentDate=&startTime=&status=&demographicNo=', 'encounter', '<%=Encode.forHtml(String.valueOf(hrmReportId))%>','<%=Encode.forHtml(String.valueOf(demographicNo))%>')" <%=Encode.forHtmlAttribute(String.valueOf(btnDisabled))%>>
        <input type="button" id="mainMaster_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>"
               value=" <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarMDS.segmentDisplay.btnMaster"/>"
               onClick="popupPatient(710,1024,'<%= request.getContextPath() %>/demographic/demographiccontrol.jsp?displaymode=edit&dboperation=search_detail&demographic_no=','master','<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>','<%=Encode.forJavaScript(String.valueOf(demographicNo))%>')" <%=Encode.forHtmlAttribute(String.valueOf(btnDisabled))%>>
        <input type="button" id="mainApptHistory_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>"
               value=" <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarMDS.segmentDisplay.btnApptHist"/>"
               onClick="popupPatient(710,1024,'<%= request.getContextPath() %>/demographic/demographiccontrol.jsp?orderby=appttime&displaymode=appt_history&dboperation=appt_history&limit1=0&limit2=25&demographic_no=','ApptHist','<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>','<%=Encode.forJavaScript(String.valueOf(demographicNo))%>')" <%=Encode.forHtmlAttribute(String.valueOf(btnDisabled))%>>
        <% if (obgynShortcuts && demographicLink != null) {%>
        <input type="button" value="AR1-ILI"
               onClick="popupONAREnhanced(290, 625, '<%=request.getContextPath()%>/form/formonarenhancedForm.jsp?demographic_no=<%=Encode.forJavaScript(String.valueOf(demographicNo))%>&formId=<%=Encode.forJavaScript(String.valueOf(formId))%>&section='+this.value)"/>
        <input type="button" value="AR1-PGI"
               onClick="popupONAREnhanced(225, 590,'<%=request.getContextPath()%>/form/formonarenhancedForm.jsp?demographic_no=<%=Encode.forJavaScript(String.valueOf(demographicNo))%>&formId=<%=Encode.forJavaScript(String.valueOf(formId))%>&section='+this.value)"/>
        <input type="button" value="AR2-US"
               onClick="popupONAREnhanced(395, 655, '<%=request.getContextPath()%>/form/formonarenhancedForm.jsp?demographic_no=<%=Encode.forJavaScript(String.valueOf(demographicNo))%>&formId=<%=Encode.forJavaScript(String.valueOf(formId))%>&section='+this.value)"/>
        <input type="button" value="AR2-ALI"
               onClick="popupONAREnhanced(375, 430, '<%=request.getContextPath()%>/form/formonarenhancedForm.jsp?demographic_no=<%=Encode.forJavaScript(String.valueOf(demographicNo))%>&formId=<%=Encode.forJavaScript(String.valueOf(formId))%>&section='+this.value)"/>
        <input type="button" value="AR2"
               onClick="popupPage(700, 1024, '<%=request.getContextPath()%>/form/formonarenhancedpg2.jsp?demographic_no=<%=Encode.forJavaScript(String.valueOf(demographicNo))%>&formId=<%=Encode.forJavaScript(String.valueOf(formId))%>&update=true')"/>
        <% } %>
    </div>

    <div id="reportViewer">
        <div id="hrmReportContent">
            <div id="hrmHeader"><b>Demographic Info:</b><br/>
                <%=Encode.forHtml(String.valueOf(hrmReport.getLegalName()))%> <br/>
                <%=Encode.forHtml(String.valueOf(hrmReport.getHCN()))%> &nbsp; <%=Encode.forHtml(String.valueOf(hrmReport.getHCNVersion()))%> &nbsp; <%=Encode.forHtml(String.valueOf(hrmReport.getGender()))%><br/>
                <b>DOB:</b><%=Encode.forHtml(String.valueOf(hrmReport.getDateOfBirthAsString()))%>
            </div>


            <div id="hrmNotice">
                This report was received from the Hospital Report Manager (HRM) at <%=Encode.forHtml(String.valueOf((String) hrmReportTime))%>.
                <% if (hrmDuplicateNum != null && (hrmDuplicateNum > 0)) { %><br/><i>OSCAR has
                received <%=Encode.forHtml(String.valueOf(String.valueOf(hrmDuplicateNum)))%> duplicates of this report.</i><% } %>
                <%
                    allDocumentsWithRelationship = (List<HRMDocument>) request.getAttribute("allDocumentsWithRelationship");
                    if (allDocumentsWithRelationship != null && allDocumentsWithRelationship.size() > 1) {
                %>
                <span id="similarNotice">OSCAR has also detected that the following reports are similar:
		<%
            List<Integer> seenBefore = new LinkedList<Integer>();
            for (HRMDocument relationshipDocument : allDocumentsWithRelationship) {
                if (!seenBefore.contains(relationshipDocument.getId().intValue())) { %>
			<span class="documentLink_status<%=Encode.forHtmlAttribute(String.valueOf(relationshipDocument.getReportStatus()))%>"
                  title="<%=Encode.forHtmlAttribute(String.valueOf(relationshipDocument.getReportDate().toString()))%>">
			<% if (relationshipDocument.getId().intValue() != hrmReportId.intValue()) { %><a
                    href="<%=request.getContextPath() %>/hospitalReportManager/Display.do?id=<%=Encode.forUriComponent(String.valueOf(relationshipDocument.getId()))%>&segmentId=<%=Encode.forUriComponent(String.valueOf(relationshipDocument.getId()))%> "><% } %>[<%=Encode.forHtml(String.valueOf(relationshipDocument.getId()))%>]<% if (relationshipDocument.getId().intValue() != hrmReportId.intValue()) { %></a><% } %>
			</span>&nbsp;&nbsp;
		<% seenBefore.add(relationshipDocument.getId().intValue());
        }
        } %>
		 <div class="boxButton">
		   <input type="button" onClick="makeIndependent('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>')"
                  value="Mark this report as not similar to the other report(s)"/>
		 </div>  
		</span>
                <% } %>
            </div>

            <%
                if (hrmReport.isBinary()) {
                    // Use the hash stored in the database instead of recalculating it
                    // This ensures compatibility with reports uploaded before UTF-8 encoding migration
                    String noMessageIdHash = document.getReportHash();

                    if (hrmReport.getFileExtension() != null && (".gif".equals(hrmReport.getFileExtension()) || ".jpg".equals(hrmReport.getFileExtension()) || ".png".equals(hrmReport.getFileExtension()))) {
            %><img
                src="<%=request.getContextPath() %>/hospitalReportManager/HRMDownloadFile.do?hash=<%=Encode.forUriComponent(String.valueOf(noMessageIdHash))%>"/><br/><%
            }
            if (hrmReport.getFileExtension() != null && ".pdf".equals(hrmReport.getFileExtension())) {
        %>
            <object data="<%=request.getContextPath() %>/hospitalReportManager/HRMDownloadFile.do?hash=<%=Encode.forHtmlAttribute(String.valueOf(noMessageIdHash))%>"
                    width="100%" height="600" type="application/pdf">
                <p>(Your browser could not display the pdf)</p>
            </object>
            <br/>
            <%
                }
            %><a
                href="<%=request.getContextPath() %>/hospitalReportManager/HRMDownloadFile.do?hash=<%=Encode.forUriComponent(String.valueOf(noMessageIdHash))%>"><%=Encode.forHtml(String.valueOf((hrmReport.getLegalLastName() + "-" + hrmReport.getLegalFirstName() + "-" + hrmReport.getFirstReportClass() + hrmReport.getFileExtension()).replaceAll("\\s", "_")))%>
        </a>&nbsp;&nbsp;
            <br/>
            <%
                if (hrmReport.getFileExtension() != null && (".pdf".equals(hrmReport.getFileExtension()) || ".gif".equals(hrmReport.getFileExtension()) || ".jpg".equals(hrmReport.getFileExtension()) || ".png".equals(hrmReport.getFileExtension()))) {
            %>
            <span>(Please use the link above to download the attachement.)</span>
            <%
            } else {
            %>
            <span style="color:red">(This report contains an attachment which cannot be viewed in your browser. Please use the link above to view/download the content contained within.)</span>
            <%
                }


            } else {

            %>
            <%=Encode.forHtml(hrmReport.getFirstReportTextContent()).replaceAll("\n", "<br />")%>

            <% } %>

            <%
                if (confidentialityStatement != null && confidentialityStatement.trim().length() > 0) {
            %>
            <hr/>
            <em><strong>Provider Confidentiality Statement</strong><br/><%=Encode.forHtml(String.valueOf(confidentialityStatement))%>
            </em>
            <% } %>
        </div>

        <div id="infoBox">
            <table>
                <tr>
                    <th>Report Date:</th>
                    <td><%=Encode.forHtml(String.valueOf((hrmReport.getFirstReportEventTime() != null ? hrmReport.getFirstReportEventTime().getTime().toString() :
                            hrmReport.getFirstAccompanyingSubClassDateTime())))%>
                    </td>
                </tr>
                <tr>
                    <th>Demographic Info:</th>
                    <td>
                        <%=Encode.forHtml(String.valueOf(hrmReport.getLegalName()))%><br/>
                        <%=Encode.forHtml(String.valueOf(hrmReport.getAddressLine1()))%><br/>
                        <%=Encode.forHtml(String.valueOf(hrmReport.getAddressLine2() != null ? hrmReport.getAddressLine2() : ""))%><br/>
                        <%=Encode.forHtml(String.valueOf(hrmReport.getAddressCity()))%>
                    </td>
                </tr>

                <tr>
                    <th>Report Class:</th>
                    <td><%=Encode.forHtml(String.valueOf(hrmReport.getFirstReportClass()))%>
                    </td>
                </tr>
                <% if (hrmReport.getFirstReportClass().equalsIgnoreCase("Diagnostic Imaging Report") || hrmReport.getFirstReportClass().equalsIgnoreCase("Cardio Respiratory Report")) { %>
                <tr>
                    <th>Accompanying Subclass:</th>
                    <td>
                        <%
                            List<List<Object>> subClassListFromReport = hrmReport.getAccompanyingSubclassList();
                            List<HRMDocumentSubClass> subClassListFromDb = (List<HRMDocumentSubClass>) request.getAttribute("subClassList");

                            if (subClassListFromReport.size() > 0) {
                        %>
                        <i>From the Report</i><br/>
                        <% for (List<Object> subClass : subClassListFromReport) { %>
                        <abbr title="Type: <%=Encode.forHtmlAttribute(String.valueOf((String) subClass.get(0)))%>; Date of Observation: <%=Encode.forHtmlAttribute(String.valueOf(((Date) subClass.get(3)).toString()))%>">(<%=Encode.forHtml(String.valueOf((String) subClass.get(1)))%>
                            ) <%=Encode.forHtml(String.valueOf((String) subClass.get(2)))%>
                        </abbr><br/>
                        <% }
                        } %><br/>
                        <%
                            if (subClassListFromDb != null && subClassListFromDb.size() > 0) { %>
                        <i>Stored in Database</i><br/>
                        <div id="subclassstatus<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>"></div>
                        <% for (HRMDocumentSubClass subClass : subClassListFromDb) { %>
                        <abbr title="Type: <%=Encode.forHtmlAttribute(String.valueOf(subClass.getSubClass()))%>; Date of Observation: <%=Encode.forHtmlAttribute(String.valueOf(subClass.getSubClassDateTime().toString()))%>">(<%=Encode.forHtml(String.valueOf(subClass.getSubClassMnemonic()))%>
                            ) <%=Encode.forHtml(String.valueOf(subClass.getSubClassDescription()))%>
                        </abbr>
                        <% if (!subClass.isActive()) { %> (<a href="#"
                                                              onclick="makeActiveSubClass('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>', '<%=Encode.forJavaScript(String.valueOf(subClass.getId()))%>')">make
                        active</a>)<% } %><br/>
                        <% }
                        } %>
                    </td>
                </tr>
                <% } else { %>
                <tr>
                    <th>Subclass:</th>
                    <td>
                        <%
                            String[] subClassFromReport = hrmReport.getFirstReportSubClass().split("\\^");
                            if (subClassFromReport.length == 2) {
                        %>
                        <abbr title="<%=Encode.forHtmlAttribute(String.valueOf(subClassFromReport[0]))%>"><%=Encode.forHtml(String.valueOf(subClassFromReport[1]))%>
                        </abbr>
                        <% } else {%>
                        <abbr><%=Encode.forHtml(String.valueOf(subClassFromReport[0]))%>
                        </abbr>
                        <% } %>
                    </td>
                </tr>
                <% } %>

                <th>Source Author(s):</th>
                <td>

                    <%
                        for (String author : hrmReport.getFirstReportAuthorPhysician()) {
                    %>
                    <%=Encode.forHtml(String.valueOf(author))%>&nbsp;
                    <%} %>

                </td>
                </tr>

                <tr>
                    <td colspan=2>
                        <hr/>
                    </td>
                </tr>

                <tr>
                    <th>Linked with Demographic:</th>
                    <td>
                        <div id="demostatus<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>">
                            <% if (demographicLink != null) { %>
                            <oscar:nameage demographicNo="<%=Encode.forHtmlAttribute(String.valueOf(demographicLink.getDemographicNo().toString()))%>"/> <br/>
                            <a href="#" onclick="removeDemoFromHrm('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>', <%=csrfTokenJs%>)">(remove)</a>
                            <% } else { %>
                            <i>Not currently linked</i>
                            <% } %>
                        </div>
                        <input type="hidden" id="demofind<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>hrm" value="<%=Encode.forHtmlAttribute(String.valueOf(demographicNo))%>"/>
                        <input type="hidden" id="demofind<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>hrm" value=""/>
                        <input type="hidden" id="routetodemo<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>hrm" value=""/>
                        <input type="checkbox" id="activeOnly<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>hrm" name="activeOnly" checked="checked"
                               value="true" onclick="setupHrmDemoAutoCompletion('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>', <%=csrfTokenJs%>)">Active
                        Only<br>
                        <input type="text" id="autocompletedemo<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>hrm"
                               onchange="checkSave('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>hrm')" name="demographicKeyword"
                               style="display:<%=(demographicLink != null) ? "none" : "block"%> "/>
                    </td>
                </tr>
                <tr>
                    <th>Assigned Providers:</th>
                    <td>
                        <div id="provstatus<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>"></div>
                        <% if (providerLinkList != null && providerLinkList.size() > 0) {
                            for (HRMDocumentToProvider p : providerLinkList) {
                                if (!p.getProviderNo().equalsIgnoreCase("-1")) { %>
                        <%=Encode.forHtml(providerDao.getProviderName(p.getProviderNo()))%> <%=p.getSignedOff() != null && p.getSignedOff() == 1 ? "<abbr title='" + Encode.forHtmlAttribute(String.valueOf(p.getSignedOffTimestamp())) + "'>(Signed-Off " + Encode.forHtml(String.valueOf(p.getSignedOffTimestamp())) + ")</abbr>" : ""%>
                        <a href="#"
                           onclick="removeProvFromHrm('<%=Encode.forJavaScript(String.valueOf(p.getId()))%>', '<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>')">(remove)</a><br/>
                        <% }
                        }
                        } else { %>
                        <i>No providers currently assigned</i><br/>
                        <% } %>
                        <% if (document.getUnmatchedProviders() != null && document.getUnmatchedProviders().trim().length() >= 1) {
                            String[] unmatchedProviders = document.getUnmatchedProviders().substring(1).split("\\|");
                            for (String unmatchedProvider : unmatchedProviders) { %>
                        <i><abbr title="From the HRM document"><%=Encode.forHtml(String.valueOf(unmatchedProvider))%>
                        </abbr></i><br/>
                        <% }
                        } %>
                        <div id="providerList<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>hrm"></div>
                        <input type="hidden" name="provi" id="provfind<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>hrm"/>
                        <input type="text" id="autocompleteprov<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>hrm" name="demographicKeyword"/>
                        <div id="autocomplete_choicesprov<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>hrm" class="autocomplete"></div>
                    </td>
                </tr>
                <tr>
                    <td colspan=2>
                        <hr/>
                    </td>
                </tr>
                <tr>
                    <th>Report Class:</th>
                    <td><%=Encode.forHtml(String.valueOf(hrmReport.getFirstReportClass()))%>
                    </td>
                </tr>
                <% if (hrmReport.getFirstReportClass().equalsIgnoreCase("Diagnostic Imaging Report") || hrmReport.getFirstReportClass().equalsIgnoreCase("Cardio Respiratory Report")) { %>
                <tr>
                    <th>Accompanying Subclass:</th>
                    <td>
                        <%
                            List<List<Object>> subClassListFromReport = hrmReport.getAccompanyingSubclassList();
                            List<HRMDocumentSubClass> subClassListFromDb = (List<HRMDocumentSubClass>) request.getAttribute("subClassList");

                            if (subClassListFromReport.size() > 0) {
                        %>
                        <i>From the Report</i><br/>
                        <% for (List<Object> subClass : subClassListFromReport) { %>
                        <abbr title="Date of Observation: <%=Encode.forHtmlAttribute(String.valueOf(((String)subClass.get(4))))%>">(<%=Encode.forHtml(String.valueOf((String) subClass.get(0)))%>
                            : <%=Encode.forHtml(String.valueOf((String) subClass.get(1)))%>) <%=Encode.forHtml(String.valueOf((String) subClass.get(2)))%>
                        </abbr><br/>
                        <% }
                        } %><br/>
                        <%
                            if (subClassListFromDb != null && subClassListFromDb.size() > 0) { %>
                        <i>Stored in Database</i><br/>
                        <div id="subclassstatus<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>"></div>
                        <% for (HRMDocumentSubClass subClass : subClassListFromDb) { %>
                        <abbr title="Date of Observation: <%=Encode.forHtmlAttribute(String.valueOf(subClass.getSubClassDateTime().toString()))%>">(<%=Encode.forHtml(String.valueOf(subClass.getSubClass()))%>
                            : <%=Encode.forHtml(String.valueOf(subClass.getSubClassMnemonic()))%>) <%=Encode.forHtml(String.valueOf(subClass.getSubClassDescription()))%>
                        </abbr>
                        <% if (!subClass.isActive()) { %> (<a href="#"
                                                              onclick="makeActiveSubClass('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>', '<%=Encode.forJavaScript(String.valueOf(subClass.getId()))%>')">make
                        active</a>)<% } %><br/>
                        <% }
                        } %>
                    </td>
                </tr>
                <% } else { %>
                <tr>
                    <th>Subclass:</th>
                    <td>
                        <%
                            String[] subClassFromReport = hrmReport.getFirstReportSubClass().split("\\^");
                            if (subClassFromReport.length == 2) {
                        %>
                        <abbr title="Subclass: <%=Encode.forHtmlAttribute(String.valueOf(subClassFromReport[0]))%>"><%=Encode.forHtml(String.valueOf(subClassFromReport[1]))%>
                        </abbr>
                        <% } %>
                    </td>
                </tr>
                <% } %>
                <tr>
                    <th>Categorization:</th>
                    <td>
				<span id="chooseCategory_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>" onchange="updateCategory('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>');"
                      style="display:none">
					<select id="selectedCategory_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>">
						<% for (HRMCategory hrmCategory : hrmCategories) { %>
						<option value="<%=Encode.forHtmlAttribute(String.valueOf(hrmCategory.getId()))%>" <%=(category != null && category.getId().equals(hrmCategory.getId())) ? "selected" : ""%>><%=Encode.forHtml(String.valueOf(hrmCategory.getCategoryName()))%></option>
						<%}%>
					</select>
				</span>

                        <span id="showCategory_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>">
					<span id="hrmCategory_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>">
						<%
                            if (category != null) {
                        %>
						<%=Encode.forHtml(category.getCategoryName())%>
						<% }%>
					</span>

					<a href="javascript:void(0)" onclick="editCategory('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>');">(edit)</a>
				</span>

                    </td>
                </tr>
                <tr>
                    <td colspan=2>
                        <form action="<%=request.getContextPath() %>/hospitalReportManager/PrintHRMReport.do">
                            <input type="hidden" value="<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>" name="hrmReportId"/>
                            <% if (request.getRequestURI().contains("oscarMDS/Page.jsp")) {%>
                            <input type="button" value="Print" onclick="printHrm('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>')"/>
                            <%} else { %>
                            <input type="submit" value="Print"/>
                            <% }%>

                            <input type="button" style="display: none;" value="Save" id="save<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>hrm"/>
                            <%
                                HRMDocumentToProvider hrmDocumentToProvider = HRMDisplayReport2Action.getHRMDocumentFromProvider(loggedInInfo.getLoggedInProviderNo(), hrmReportId);
                                if (hrmDocumentToProvider != null && hrmDocumentToProvider.getSignedOff() != null && hrmDocumentToProvider.getSignedOff() == 1) {
                            %>
                            <input type="button" id="signoff<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>" value="Revoke Sign-Off"
                                   onClick="revokeSignOffHrm('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>')"/>
                            <%
                            } else {
                            %>
                            <input type="button" id="signoff<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>" value="Sign-Off"
                                   onClick="signOffHrm('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>', <%=Encode.forJavaScript(String.valueOf(isListView))%>)" <%=Encode.forHtmlAttribute(String.valueOf(btnDisabled))%>/>
                            <%
                                }
                            %>
                            <input type="button" value="Annotations"
                                   onClick="popupPage(500, 400, '<%=request.getContextPath() %>/annotation/annotation.jsp?display=HRM&table_id=<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>&demo=<%=Encode.forJavaScript(String.valueOf(demographicNo))%>')"/>
                        </form>
                    </td>
                </tr>

            </table>
        </div>
    </div>

    <div class="aBox" id="duplicateAndSimilarBox">

        <% if (request.getAttribute("hrmDuplicateNum") != null && ((Integer) request.getAttribute("hrmDuplicateNum")) > 0) { %>
        Duplicates Received by HRM:  <%=Encode.forHtml(String.valueOf(request.getAttribute("hrmDuplicateNum")))%>.<br/>
        <% } else { %>
        Duplicates Received by HRM: 0.<br/>
        <% } %>

        <br/>
        <%
            children = (List<HRMDocument>) request.getAttribute("children");

            if (children != null && children.size() > 0) {
        %>
        NOTE: This report might <b style="color:red">not be the most current report available</b>. Similar reports have
        been received as follows:<br/><br/>
        <table>
            <tr>
                <th>Id</th>
                <th>Report Date</th>
                <th>Received Date</th>
            </tr>
            <%for (HRMDocument child : children) { %>
            <tr>
                <td><a href="javascript:void(0)" onClick="openReport('<%=Encode.forJavaScript(String.valueOf(child.getId()))%>')"><%=Encode.forHtml(String.valueOf(child.getId()))%>
                </a></td>
                <td><%=Encode.forHtml(String.valueOf(child.getReportDate()))%>
                </td>
                <td><%=Encode.forHtml(String.valueOf(child.getTimeReceived()))%>
                </td>
            </tr>
            <% } %>
        </table>
        <%
            }
        %>

    </div>
    <div id="descriptionBox">

        Add a description:
        <input type="text" id="descriptionField_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>_hrm" size="100"
               value="<%=Encode.forHtml(document.getDescription())%>"/><br/>

        <div class="boxButton">
            <input type="button" onClick="setDescription('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>')" value="Set Description"/><span
                id="descriptionstatus<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>"></span><br/><br/>
        </div>

    </div>

    <div id="commentBox">
        Add a comment:
        <textarea rows="10" cols="50" id="commentField_<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>_hrm"></textarea>

        <div class="boxButton">
            <input type="button" onClick="addComment('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>')" value="Add Comment"/><span
                id="commentstatus<%=Encode.forHtmlAttribute(String.valueOf(hrmReportId))%>"></span><br/><br/>
        </div>
        <%

            if (documentComments != null) {
        %>Displaying <%=documentComments.size() %> comment<%=documentComments.size() != 1 ? "s" : "" %><br/>
        <% for (HRMDocumentComment comment : documentComments) {
            String commentTime = comment.getCommentTime() != null ? " on " + comment.getCommentTime().toString() : ""; %>
        <div class="documentComment">
            <strong><%=Encode.forHtml(providerDao.getProviderName(comment.getProviderNo())) %><%=Encode.forHtml(String.valueOf(commentTime))%>
                wrote...</strong><br/>
            <%=Encode.forHtml(comment.getComment()) %><br/>
            <a href="#" onClick="deleteComment('<%=Encode.forJavaScript(String.valueOf(comment.getId()))%>', '<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>'); return false;">(Delete
                this comment)</a></div>
        <% }
        }
        %>
    </div>

    <div id="metadataBox">
        <table>
            <tr>

                <th>Media</th>
                <td><%=Encode.forHtml(String.valueOf(hrmReport.getMediaType()))%>
                </td>
            </tr>
            <tr>
                <th>Message Unique ID</th>
                <td><%=Encode.forHtml(String.valueOf(hrmReport.getMessageUniqueId()))%>
                </td>
            </tr>
            <tr>
                <th>Sending Author</th>
                <td><%=Encode.forHtml(String.valueOf(hrmReport.getSendingAuthor()))%>
                </td>
            </tr>
            <tr>
                <th>Sending Facility</th>

                <td>
                    <%
                        String sfId = hrmReport.getSendingFacilityId();
                        HRMSendingFacility sf = (sfId != null && !sfId.isEmpty())
                                ? hrmSendingFacilityDao.findBySendingFacilityId(sfId) : null;
                        if (sf != null) {
                    %>
                        <%=Encode.forHtml(sf.getFacilityName())%>
                        (<%=Encode.forHtml(sf.getSendingFacilityId())%>)
                    <% } else if (sfId != null && !sfId.isEmpty()) { %>
                        <%=Encode.forHtml(sfId)%>
                        <span style="background-color: #f0ad4e; color: #fff; padding: 2px 6px;
                                     border-radius: 3px; font-size: 0.85em; margin-left: 6px;
                                     font-weight: bold;"
                              title="This Sending Facility is not in the HRM Sending Facilities registry. Add it via Admin → Integration → Hospital Report Manager (HRM) Sending Facilities to display a facility name.">
                            Unregistered
                        </span>
                    <% } %>
                </td>
            </tr>
            <tr>
                <th>Sending Facility Report No.</th>
                <td><%=Encode.forHtml(String.valueOf(hrmReport.getSendingFacilityReportNo()))%>
                </td>
            </tr>
            <tr>

                <th>Date and Time of Report</th>
                <td><%=Encode.forHtml(String.valueOf(HRMReportParser.getAppropriateDateStringFromReport(hrmReport)))%>
                </td>

            </tr>
            <tr>
                <th>Result Status</th>
                <td><%=(hrmReport.getResultStatus() != null && hrmReport.getResultStatus().equalsIgnoreCase("C")) ? "Cancelled" : "Signed by the responsible author and Released by health records"  %>
                </td>
            </tr>
        </table>
    </div>


    <script type="text/javascript">
        jQuery(setupHrmDemoAutoCompletion(<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>, <%=csrfTokenJs%>));

        YAHOO.example.BasicRemote = function () {
            var url = "<%= request.getContextPath() %>/provider/SearchProvider.do";
            var oDS = new YAHOO.util.XHRDataSource(url, {connMethodPost: true, connXhrMode: 'ignoreStaleResponses'});
            oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;// Set the responseType
            // Define the schema of the delimited resultsTEST, PATIENT(1985-06-15)
            oDS.responseSchema = {
                resultsList: "results",
                fields: ["providerNo", "firstName", "lastName"]
            };
            // Enable caching
            oDS.maxCacheEntries = 0;
            // Instantiate the AutoComplete
            var oAC = new YAHOO.widget.AutoComplete("autocompleteprov<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>hrm", "autocomplete_choicesprov<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>hrm", oDS);
            oAC.queryMatchSubset = true;
            oAC.minQueryLength = 3;
            oAC.maxResultsDisplayed = 25;
            oAC.formatResult = resultFormatter3;
            oAC.queryMatchContains = true;
            oAC.itemSelectEvent.subscribe(function (type, args) {
                var myAC = args[0];
                var str = myAC.getInputEl().id.replace("autocompleteprov", "provfind");
                var oData = args[2];
                $(str).value = args[2][0];//li.id;
                myAC.getInputEl().value = args[2][2] + "," + args[2][1];
                var adoc = document.createElement('div');
                adoc.appendChild(document.createTextNode(oData[2] + " " + oData[1]));
                var idoc = document.createElement('input');
                idoc.setAttribute("type", "hidden");
                idoc.setAttribute("name", "flagproviders");
                idoc.setAttribute("value", oData[0]);
                adoc.appendChild(idoc);

                var providerList = $('providerList<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>hrm');
                providerList.appendChild(adoc);

                myAC.getInputEl().value = '';//;oData.fname + " " + oData.lname ;

                addProvToHrm('<%=Encode.forJavaScript(String.valueOf(hrmReportId))%>', args[2][0]);
            });


            return {
                oDS: oDS,
                oAC: oAC
            };
        }();
    </script>

    <%
        String duplicateLabIdsString = StringUtils.trimToNull(request.getParameter("duplicateLabIds"));
        if (duplicateLabIdsString != null) {

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
    %>
    <div id="duplicatesMessage">
        Duplicate Report History:<br/><br/>

        <table>
            <tr>
                <th>ID</th>
                <th>Report Date</th>
                <th>Date Received</th>
                <th></th>
            </tr>
            <%
                //need datetime of report.
                String[] duplicateLabIdsStringSplit = duplicateLabIdsString.split(",");
                for (String tempId : duplicateLabIdsStringSplit) {
            %>
            <tr>
                <td><%=Encode.forHtml(String.valueOf(tempId))%>
                </td>
                <td><%=Encode.forHtml(String.valueOf(formatter.format(dupReportDates.get(Integer.parseInt(tempId)))))%>
                </td>
                <td><%=Encode.forHtml(String.valueOf(formatter.format(dupTimeReceived.get(Integer.parseInt(tempId)))))%>
                </td>
                <td><input type="button" value="Open Report"
                           onclick="window.open('?id=<%=Encode.forJavaScript(String.valueOf(tempId))%>&segmentId=<%=Encode.forJavaScript(String.valueOf(tempId))%>&providerNo=<%=Encode.forJavaScript(request.getParameter("providerNo"))%>&searchProviderNo=<%=Encode.forJavaScript(request.getParameter("searchProviderNo"))%>&status=<%=Encode.forJavaScript(request.getParameter("status"))%>&demoName=<%=Encode.forHtml(request.getParameter("demoName"))%>', null)"/>
                </td>
            </tr>

            <%
                }

            %></table>
    </div>
    <%
        }
    %>


</div>
</body>
</html>
