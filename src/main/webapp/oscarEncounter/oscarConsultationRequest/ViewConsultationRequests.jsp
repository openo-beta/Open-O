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
<security:oscarSec roleName="<%=roleName$%>" objectName="_con" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_con");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@page import="ca.openosp.openo.commn.dao.ConsultationRequestDao" %>
<%@page import="ca.openosp.openo.commn.model.ProfessionalSpecialist" %>
<%@page import="ca.openosp.openo.commn.model.Provider" %>

<%@ page import="ca.openosp.openo.encounter.pageUtil.*,java.text.*,java.util.*" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page
        import="ca.openosp.openo.commn.dao.UserPropertyDAO, ca.openosp.openo.commn.model.UserProperty, org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="ca.openosp.openo.utility.SpringUtils" %>

<%@ page import="ca.openosp.openo.commn.model.Site" %>
<%@ page import="ca.openosp.openo.commn.dao.SiteDao" %>

<%@ page import="ca.openosp.openo.commn.model.ProviderData" %>
<%@ page import="ca.openosp.openo.commn.dao.ProviderDataDao" %>

<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="ca.openosp.openo.encounter.oscarConsultationRequest.pageUtil.EctConsultationFormRequestUtil" %>
<%@ page import="ca.openosp.openo.encounter.oscarConsultationRequest.pageUtil.EctViewConsultationRequestsUtil" %>
<%@ page import="ca.openosp.openo.commn.IsPropertiesOn" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    String curProvider_no = (String) session.getAttribute("user");

    boolean isSiteAccessPrivacy = false;
    boolean isTeamAccessPrivacy = false;
    boolean bMultisites = IsPropertiesOn.isMultisitesEnable();
    List<String> mgrSite = new ArrayList<String>();

    ProviderDataDao providerDataDao = SpringUtils.getBean(ProviderDataDao.class);

    String strLimit = request.getParameter("limit");
    String strOffset = request.getParameter("offset");

    Integer limit = ConsultationRequestDao.DEFAULT_CONSULT_REQUEST_RESULTS_LIMIT;
    Integer offset = 0;

    try {
        offset = Integer.parseInt(strOffset);
    } catch (NumberFormatException e) {
        offset = 0;
    }

    try {
        limit = Integer.parseInt(strLimit);
    } catch (NumberFormatException e) {
        limit = 100;
    }
%>
<security:oscarSec objectName="_site_access_privacy" roleName="<%=roleName$%>" rights="r"
                   reverse="false"><%isSiteAccessPrivacy = true; %></security:oscarSec>
<security:oscarSec objectName="_team_access_privacy" roleName="<%=roleName$%>" rights="r"
                   reverse="false"><%isTeamAccessPrivacy = true; %></security:oscarSec>

<%
    List<ProviderData> pdList = null;
    HashMap<String, String> providerMap = new HashMap<String, String>();

//multisites function
    if (isSiteAccessPrivacy || isTeamAccessPrivacy) {

        if (isSiteAccessPrivacy)
            pdList = providerDataDao.findByProviderSite(curProvider_no);

        if (isTeamAccessPrivacy)
            pdList = providerDataDao.findByProviderTeam(curProvider_no);

        for (ProviderData providerData : pdList) {
            providerMap.put(providerData.getId(), "true");
        }
    }
%>

<%
    //multi-site office , save all bgcolor to Hashmap
    HashMap<String, String> siteBgColor = new HashMap<String, String>();
    HashMap<String, String> siteShortName = new HashMap<String, String>();
    if (bMultisites) {
        SiteDao siteDao = (SiteDao) WebApplicationContextUtils.getWebApplicationContext(application).getBean(SiteDao.class);

        List<Site> sites = siteDao.getAllSites();
        for (Site st : sites) {
            siteBgColor.put(st.getName(), st.getBgColor());
            siteShortName.put(st.getName(), st.getShortName());
        }
        List<Site> providerSites = siteDao.getActiveSitesByProviderNo(curProvider_no);
        for (Site st : providerSites) {
            mgrSite.add(st.getName());
        }
    }
%>

<!DOCTYPE html>
<html>

    <%

        String team = (String) request.getAttribute("teamVar");
        if (team == null) {
            team = new String();
        }

        Boolean includeBool = (Boolean) request.getAttribute("includeCompleted");
        boolean includeCompleted = false;
        if (includeBool != null) {
            includeCompleted = "on".equals(request.getParameter("includeCompleted"));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // Getting startDate attribute of the consultation request and ensuring that it is of type "Date" before casting
        Object startDateObj = request.getAttribute("startDate");
        Date startDate = null;
        String formattedStartDate = "";
        if (startDateObj instanceof Date) {
            startDate = (Date) startDateObj;
            formattedStartDate = sdf.format(startDateObj);
        }

        // Getting endDate attribute of the consultation request and ensuring that it is of type "Date" before casting
        Object endDateObj = request.getAttribute("endDate");
        Date endDate = null;
        String formattedEndDate = "";
        if (endDateObj instanceof Date) {
            endDate = (Date) endDateObj;
            formattedEndDate = sdf.format(endDateObj);
        }

        // Getting orderby, description, and searchDate attributes of the consultation request
        String orderby = (String) request.getAttribute("orderby");
        String desc = (String) request.getAttribute("desc");
        String searchDate = (String) request.getAttribute("searchDate");

        // Setting defaults to match consultation request in struts 1
        if (searchDate == null) {
            searchDate = "0";
        }

        // New consultant and provider filter params
        Integer consultantId = (Integer) request.getAttribute("consultantId");
        String filterProviderNo = (String) request.getAttribute("filterProviderNo");
        if (filterProviderNo == null) filterProviderNo = "";

        ConsultationRequestDao consultReqDaoForFilters = SpringUtils.getBean(ConsultationRequestDao.class);
        List<ProfessionalSpecialist> availableConsultants = consultReqDaoForFilters.getDistinctConsultants();
        List<Provider> availableProviders = consultReqDaoForFilters.getDistinctConsultProviders();
        if (isSiteAccessPrivacy || isTeamAccessPrivacy) {
            List<Provider> filteredProviders = new ArrayList<>();
            for (Provider pv : availableProviders) {
                if (providerMap.containsKey(pv.getProviderNo())) {
                    filteredProviders.add(pv);
                }
            }
            availableProviders = filteredProviders;
        }

        EctConsultationFormRequestUtil consultUtil;
        consultUtil = new EctConsultationFormRequestUtil();

        if (isTeamAccessPrivacy) {
            consultUtil.estTeamsByTeam(curProvider_no);
        } else if (isSiteAccessPrivacy) {
            consultUtil.estTeamsBySite(curProvider_no);
        } else {
            consultUtil.estTeams();
        }


        ArrayList tickerList = new ArrayList();

        // Compute initial display labels for the search-ahead fields
        String selectedConsultantLabel = "";
        if (consultantId != null) {
            for (ProfessionalSpecialist cons : availableConsultants) {
                if (consultantId.equals(cons.getId())) {
                    selectedConsultantLabel = cons.getLastName() + ", " + cons.getFirstName();
                    break;
                }
            }
        }
        String selectedProviderLabel = "";
        if (!filterProviderNo.isEmpty()) {
            for (Provider pv : availableProviders) {
                if (filterProviderNo.equals(pv.getProviderNo())) {
                    selectedProviderLabel = pv.getFormattedName();
                    break;
                }
            }
        }
    %>


    <head>
        <title>
            <fmt:setBundle basename="oscarResources"/><fmt:message key="ectViewConsultationRequests.title"/>
        </title>


        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">

        <link rel="stylesheet" type="text/css" media="all" href="<%= request.getContextPath() %>/share/calendar/calendar.css"
              title="win2k-cold-1"/>
        <script type="text/javascript" src="<%= request.getContextPath() %>/share/calendar/calendar.js"></script>
        <script type="text/javascript"
                src="<%= request.getContextPath() %>/share/calendar/lang/<fmt:setBundle basename="oscarResources"/><fmt:message key="global.javascript.calendar"/>"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/share/calendar/calendar-setup.js"></script>
        <!--META HTTP-EQUIV="Refresh" CONTENT="20;"-->

        <style type="text/css">
            td.stat1 {
                background-color: #eeeeFF;
            }

            th, td.stat2 {
                background-color: #ccccFF;
            }

            td.stat3 {
                background-color: #B8B8FF;
            }

            td.stat4 {
                background-color: #eeeeff;
            }
td.stat5 {
background-color:rgb(212, 212, 254);
}

            th.VCRheads {
                background-color: #ddddff;
                color: black;
            }

        </style>


    <script type="text/javascript">
        function BackToOscar() {
            window.close();
        }

        function popupOscarRx(vheight, vwidth, varpage) {
            var windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0";
            var popup = window.open(varpage, "oscarConsultationRequest", windowprops);
            if (popup != null && popup.opener == null) {
                popup.opener = self;
            }
        }

        function popupOscarConsultationConfig(vheight, vwidth, varpage) {
            var windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0";
            var popup = window.open(varpage, "OscarConsultationConfig", windowprops);
            if (popup != null && popup.opener == null) {
                popup.opener = self;
            }
        }

        function setOrder(val) {
            var frm = document.forms[0];
            if (frm.orderby.value === val) {
                frm.desc.value = frm.desc.value === '1' ? '0' : '1';
            } else {
                frm.orderby.value = val;
                frm.desc.value = '0';
            }
            frm.submit();
        }

        function gotoPage(next) {
            var frm = document.forms[0];
            frm.limit.value = <%=limit%>;
            frm.offset.value = next ? <%=limit + offset%> : <%=offset - limit%>;
            frm.submit();
        }
    </script>

    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/oscarEncounter/encounterStyles.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/library/jquery/jquery-ui-1.12.1.min.css">
    <script type="text/javascript" src="<%= request.getContextPath() %>/library/jquery/jquery-3.6.4.min.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/library/jquery/jquery-ui-1.12.1.min.js"></script>
    <script type="text/javascript">
        var consultantOptions = [
            <%
                for (ProfessionalSpecialist cons : availableConsultants) {
                    String consLabel = cons.getLastName() + ", " + cons.getFirstName();
                    String consIdVal = cons.getId() != null ? cons.getId().toString() : "";
            %>
            { label: "<%=Encode.forJavaScript(consLabel)%>", value: "<%=Encode.forJavaScript(consIdVal)%>" },
            <%
                }
            %>
        ];
        var providerOptions = [
            <%
                for (Provider pv : availableProviders) {
                    String pvLabel = pv.getFormattedName();
                    String pvNoVal = pv.getProviderNo() != null ? pv.getProviderNo() : "";
            %>
            { label: "<%=Encode.forJavaScript(pvLabel)%>", value: "<%=Encode.forJavaScript(pvNoVal)%>" },
            <%
                }
            %>
        ];

        jQuery(document).ready(function () {
            jQuery("#consultantSearch").autocomplete({
                source: consultantOptions,
                minLength: 0,
                delay: 0,
                focus: function (event, ui) {
                    event.preventDefault();
                    jQuery("#consultantSearch").val(ui.item.label);
                },
                select: function (event, ui) {
                    event.preventDefault();
                    jQuery("#consultantSearch").val(ui.item.label);
                    jQuery("#consultantId").val(ui.item.value);
                },
                change: function (event, ui) {
                    if (!ui.item) {
                        jQuery("#consultantSearch").val("");
                        jQuery("#consultantId").val("");
                    }
                }
            });
            jQuery("#consultantSearch").on("focus", function () {
                jQuery(this).autocomplete("search", jQuery(this).val());
            });

            jQuery("#providerSearch").autocomplete({
                source: providerOptions,
                minLength: 0,
                delay: 0,
                focus: function (event, ui) {
                    event.preventDefault();
                    jQuery("#providerSearch").val(ui.item.label);
                },
                select: function (event, ui) {
                    event.preventDefault();
                    jQuery("#providerSearch").val(ui.item.label);
                    jQuery("#filterProviderNo").val(ui.item.value);
                },
                change: function (event, ui) {
                    if (!ui.item) {
                        jQuery("#providerSearch").val("");
                        jQuery("#filterProviderNo").val("");
                    }
                }
            });
            jQuery("#providerSearch").on("focus", function () {
                jQuery(this).autocomplete("search", jQuery(this).val());
            });

            // Before form submit, ensure text field / hidden field are consistent.
            jQuery("form").on("submit", function () {
                var consultText = jQuery("#consultantSearch").val().trim();
                if (consultText === "") {
                    jQuery("#consultantId").val("");
                } else {
                    var consultMatched = consultantOptions.some(function(o) { return o.label === consultText; });
                    if (!consultMatched) {
                        jQuery("#consultantSearch").val("");
                        jQuery("#consultantId").val("");
                    }
                }
                var provText = jQuery("#providerSearch").val().trim();
                if (provText === "") {
                    jQuery("#filterProviderNo").val("");
                } else {
                    var provMatched = providerOptions.some(function(o) { return o.label === provText; });
                    if (!provMatched) {
                        jQuery("#providerSearch").val("");
                        jQuery("#filterProviderNo").val("");
                    }
                }
            });
        });
    </script>
    </head>
    <body class="BodyStyle" vlink="#0000FF">
    <!--  -->
    <table class="MainTable" id="scrollNumber1" name="encounterTable">
        <tr class="MainTableTopRow">
            <td class="MainTableTopRowLeftColumn">
                Consultation
            </td>
            <td class="MainTableTopRowRightColumn">
                <table class="TopStatusBar">
                    <tr>
                        <td class="Header" NOWRAP>
                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msfConsReqForTeam"/>
                            =
                            <%
                                if (team.equals("-1")) {
                            %>
                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.formTeamNotApplicable"/>
                            <% } else if (team.isEmpty()) { %>
                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.formViewAll"/>
                            <% } else { %>
                            <%= Encode.forHtml(team) %>
                            <% } %>
                        </td>
                        <td>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr style="vertical-align:top">
            <td class="MainTableLeftColumn">
                <table>
                    <tr>
                        <td NOWRAP>
                            <a href="javascript:popupOscarConsultationConfig(700,960,'<%=request.getContextPath()%>/oscarEncounter/oscarConsultationRequest/config/ShowAllServices.jsp')"
                               class="consultButtonsActive">
                                <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgEditSpecialists"/>
                            </a>
                        </td>
                    </tr>
                </table>
            </td>
            <td class="MainTableRightColumn">
                <table width="100%">
                    <tr>
                        <td style="margin: 0; padding: 0;">
                            <form action="${pageContext.request.contextPath}/oscarEncounter/ViewConsultation.do" method="get">
                                <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.formSelectTeam"/>:
                                <select name="sendTo">
                                    <option value=""><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.formViewAll"/></option>
                                    <%
                                        if (team.equals("-1")) { %>
                                    <option value="-1" selected><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.formTeamNotApplicable"/></option>
                                    <% } else {
                                    %>
                                    <option value="-1"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.formTeamNotApplicable"/></option>
                                    <% }
                                        for (int i = 0; i < consultUtil.teamVec.size(); i++) {
                                            String te = (String) consultUtil.teamVec.get(i);
                                            if (te.equals(team)) {
                                    %>
                                    <option value="<%=Encode.forHtmlAttribute(te)%>" selected><%=Encode.forHtml(te)%>
                                    </option>
                                    <%} else {%>
                                    <option value="<%=Encode.forHtmlAttribute(te)%>"><%=Encode.forHtml(te)%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                                <input type="text" id="consultantSearch" autocomplete="off"
                                       value="<%=Encode.forHtmlAttribute(selectedConsultantLabel)%>"
                                       placeholder="All Consultants" size="22">
                                <input type="hidden" name="consultantId" id="consultantId"
                                       value="<%=Encode.forHtmlAttribute(consultantId != null ? consultantId.toString() : "")%>">
                                <input type="text" id="providerSearch" autocomplete="off"
                                       value="<%=Encode.forHtmlAttribute(selectedProviderLabel)%>"
                                       placeholder="All Providers" size="22">
                                <input type="hidden" name="filterProviderNo" id="filterProviderNo"
                                       value="<%=Encode.forHtmlAttribute(filterProviderNo)%>">
                                <input type="submit"
                                       value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.btnConsReq"/>"/>
                                <div style="margin: 0; padding: 0; ">
                                    <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgStart"/>:
                                    <input type="text" name="startDate" size="8" id="startDate" value="<%=Encode.forHtmlAttribute(String.valueOf(formattedStartDate))%>" /><a id="SCal"><img
                                        title="Calendar" src="<%= request.getContextPath() %>/images/cal.gif" alt="Calendar" border="0"/></a>
                                    <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgEnd"/>:
                                    <input type="text" name="endDate" size="8" id="endDate" value="<%=Encode.forHtmlAttribute(String.valueOf(formattedEndDate))%>" /><a id="ECal"><img
                                        title="Calendar" src="<%= request.getContextPath() %>/images/cal.gif" alt="Calendar" border="0"/></a>
                                    <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgIncludeCompleted"/>:
                                    <input type="checkbox" name="includeCompleted" <%= includeCompleted ? "checked" : "" %> />
                                    <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgSearchon"/>
                                    <input type="radio" name="searchDate" value="0" titleKey="Search on Referal Date"
                                        <%= "0".equals(searchDate) ? "checked" : "" %> />
                                    <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgApptDate"/>
                                    <input type="radio" name="searchDate" value="1" titleKey="Search on Appt. Date"
                                        <%= "1".equals(searchDate) ? "checked" : "" %> />
                                    <input type="hidden" name="currentTeam" id="currentTeam" value="<%= Encode.forHtmlAttribute(team != null ? team : "") %>"/>
                                    <input type="hidden" name="orderby" id="orderby" value="<%= Encode.forHtmlAttribute(orderby != null ? orderby : "") %>"/>
                                    <input type="hidden" name="desc" id="desc" value="<%= Encode.forHtmlAttribute(desc != null ? desc : "") %>"/>
                                    <input type="hidden" name="offset" id="offset" value="<%= Encode.forHtmlAttribute(String.valueOf(offset)) %>"/>
                                    <input type="hidden" name="limit" id="limit" value="<%= Encode.forHtmlAttribute(String.valueOf(limit)) %>"/>
                                </div>
                            </form>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <table border="0" width="90%" cellspacing="1" style="border: thin solid #C0C0C0;">
                                <tr>
                                    <th align="left" class="VCRheads" width="10%">
                                        <a href="javascript:void(0)" onclick="setOrder('1'); return false;">
                                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgStatus"/>
                                        </a>
                                    </th>
                                    <th align="left" class="VCRheads" width="10%">
                                        <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgUrgency"/>
                                    </th>
                                    <th align="left" class="VCRheads">
                                        <a href="javascript:void(0)" onclick="setOrder('2'); return false;">
                                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgTeam"/>
                                        </a>
                                    </th>
                                    <th align="left" class="VCRheads" width="75">
                                        <a href="javascript:void(0)" onclick="setOrder('3'); return false;">
                                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgPatient"/>
                                        </a>
                                    </th>
                                    <th align="left" class="VCRheads">
                                        <a href="javascript:void(0)" onclick="setOrder('4'); return false;">
                                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgProvider"/>
                                        </a>
                                    </th>
                                    <th align="left" class="VCRheads">
                                        <a href="javascript:void(0)" onclick="setOrder('5'); return false;">
                                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgService"/>
                                        </a>
                                    </th>
                                    <th align="left" class="VCRheads">
                                        <a href="javascript:void(0)" onclick="setOrder('6'); return false;">
                                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgConsultant"/>
                                        </a>
                                    </th>
                                    <th align="left" class="VCRheads">
                                        <a href="javascript:void(0)" onclick="setOrder('7'); return false;">
                                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgRefDate"/>
                                        </a>
                                    </th>
                                    <th align="left" class="VCRheads">
                                        <a href="javascript:void(0)" onclick="setOrder('8'); return false;">
                                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgAppointmentDate"/>
                                        </a>
                                    </th>
                                    <th align="left" class="VCRheads">
                                        <a href="javascript:void(0)" onclick="setOrder('9'); return false;">
                                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgFollowUpDate"/>
                                        </a>
                                    </th>
                                    <% if (bMultisites) { %>
                                    <th align="left" class="VCRheads">
                                        <a href="javascript:void(0)" onclick="setOrder('10'); return false;">
                                            <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgSiteName"/>
                                        </a>
                                    </th>
                                    <%} %>
                                </tr>
                                <%
                                    EctViewConsultationRequestsUtil theRequests;
                                    theRequests = new EctViewConsultationRequestsUtil();
                                    theRequests.estConsultationVecByTeam(LoggedInInfo.getLoggedInInfoFromSession(request), team, includeCompleted, startDate, endDate, orderby, desc, searchDate, offset, limit, consultantId, filterProviderNo);
                                    boolean overdue;
                                    UserPropertyDAO pref = (UserPropertyDAO) WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext()).getBean(UserPropertyDAO.class);
                                    String user = (String) session.getAttribute("user");
                                    UserProperty up = pref.getProp(user, UserProperty.CONSULTATION_TIME_PERIOD_WARNING);
                                    String timeperiod = null;
                                    int countback;

                                    if (up != null && up.getValue() != null && !up.getValue().trim().equals("")) {
                                        timeperiod = up.getValue();
                                    }

                                    for (int i = 0; i < theRequests.ids.size(); i++) {
                                        //multisites. skip record if not belong to same site/team
                                        if (isSiteAccessPrivacy || isTeamAccessPrivacy) {
                                            if (providerMap.get(theRequests.providerNo.get(i)) == null) continue;
                                        }

                                        String id = theRequests.ids.get(i);
                                        String status = theRequests.status.get(i);
                                        String patient = theRequests.patient.get(i);
                                        String provide = theRequests.provider.get(i);
                                        String service = theRequests.service.get(i);
                                        boolean eReferral = theRequests.eReferral.get(i);
                                        String date = theRequests.date.get(i);
                                        String demo = theRequests.demographicNo.get(i);
                                        String appt = theRequests.apptDate.get(i);
                                        String patBook = theRequests.patientWillBook.get(i);
                                        String urgency = theRequests.urgency.get(i);
                                        String sendTo = theRequests.teams.get(i);
                                        if (sendTo == null) sendTo = "-1";
                                        String specialist = theRequests.vSpecialist.get(i);
                                        String followUpDate = theRequests.followUpDate.get(i);
                                        String siteName = "";
                                        if (bMultisites) {
                                            siteName = theRequests.siteName.get(i);
                                        }
                                        if (status.equals("1") && dateGreaterThan(date, Calendar.WEEK_OF_YEAR, -1)) {
                                            tickerList.add(demo);
                                        }

                                        //multisites. skip record if not belong to same site
                                        if (isSiteAccessPrivacy || isTeamAccessPrivacy) {
                                            if (!mgrSite.contains(siteName)) continue;
                                        }
                                        overdue = false;

                                        if (timeperiod != null) {
                                            countback = Integer.parseInt(timeperiod);
                                            countback = countback * -1;


                                            if ((status.equals("1") || status.equals("2") || status.equals("3")) && dateGreaterThan(date, Calendar.MONTH, countback)) {
                                                overdue = true;
                                            }
                                        } else {
                                            countback = -7;  //7 days
                                            if ((status.equals("1") || status.equals("3")) && dateGreaterThan(date, Calendar.DAY_OF_YEAR, countback)) {
                                                overdue = true;
                                            }

                                            countback = -30;  //30 days
                                            if (status.equals("2") && dateGreaterThan(date, Calendar.DAY_OF_YEAR, countback)) {
                                                overdue = true;
                                            }
                                        }


                                %>
                                <tr <%=overdue ? "style='color:red;'" : ""%>>
                                    <td class="stat<%=Encode.forHtmlAttribute(status)%>">
                                        <% if (status.equals("1")) { %>
                                        <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgND"/>
                                        <% } else if (status.equals("2")) { %>
                                        <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgSR"/>
                                        <% } else if (status.equals("3")) { %>
                                        <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgPR"/>
                                        <% } else if (status.equals("4")) { %>
                                        <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgDONE"/>
                                        <% }else if(status.equals("5")) { %>
                                        <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.oscarConsultationRequest.ViewConsultationRequests.msgBC"/>
                                        <%}%>
                                    </td>
                                    <td class="stat<%=Encode.forHtmlAttribute(status)%>">
                                        <% if (urgency.equals("1")) { %>
                                        <div style="color:red;"> Urgent</div>
                                        <% } else if (urgency.equals("2")) { %>
                                        Non-Urgent
                                        <% } else if (urgency.equals("3")) { %>
                                        Return
                                        <% } %>


                                    </td>
                                    <td class="stat<%=Encode.forHtmlAttribute(status)%>">
                                        <a href="javascript:popupOscarRx(700,960,'<%=request.getContextPath()%>/oscarEncounter/ViewRequest.do?requestId=<%=Encode.forUriComponent(id)%>')">
                                            <%=sendTo.equals("-1") ? "N/A" : Encode.forHtml(sendTo)%>
                                        </a>
                                    </td>
                                    <td class="stat<%=Encode.forHtmlAttribute(status)%>">
                                        <a href="javascript:popupOscarRx(700,960,'<%=request.getContextPath()%>/oscarEncounter/ViewRequest.do?requestId=<%=Encode.forUriComponent(id)%>')">
                                            <%=Encode.forHtml(patient)%>
                                        </a>
                                    </td>
                                    <td class="stat<%=Encode.forHtmlAttribute(status)%>">
                                        <%=Encode.forHtml(provide)%>
                                    </td>
                                    <td class="stat<%=Encode.forHtmlAttribute(status)%>">
                                        <a href="javascript:popupOscarRx(700,960,'<%=request.getContextPath()%>/oscarEncounter/ViewRequest.do?requestId=<%=Encode.forUriComponent(id)%>')">
                                            <%=Encode.forHtml(service)%>
                                        </a>

                                    </td>
                                    <td class="stat<%=Encode.forHtmlAttribute(status)%>">
                                        <a href="javascript:popupOscarRx(700,960,'<%=request.getContextPath()%>/oscarEncounter/ViewRequest.do?requestId=<%=Encode.forUriComponent(id)%>')">
                                            <%=Encode.forHtml(specialist)%>
                                        </a>
                                    <% if (eReferral) { %>
                                    <span>(via OCEAN)</span>
                                    <%} %>
                                    </td>
                                    <td class="stat<%=Encode.forHtmlAttribute(status)%>">
                                        <%=Encode.forHtml(date)%>
                                    </td>
                                    <td class="stat<%=Encode.forHtmlAttribute(status)%>">
                                        <% if (patBook != null && patBook.trim().equals("1")) {%>
                                        Patient will book
                                        <%} else {%>
                                        <%=Encode.forHtml(appt)%>
                                        <%}%>
                                    </td>
                                    <td class="stat<%=Encode.forHtmlAttribute(status)%>">
                                        <a href="javascript:popupOscarRx(700,960,'<%=request.getContextPath()%>/oscarEncounter/ViewRequest.do?requestId=<%=Encode.forUriComponent(id)%>')">
                                            <%=Encode.forHtml(followUpDate)%>
                                        </a>

                                    </td>
                                    <% if (bMultisites) { %>
                                    <td bgcolor="<%=Encode.forHtmlAttribute(siteBgColor.get(siteName)==null || siteBgColor.get(siteName).length()== 0 ? "#FFFFFF" : siteBgColor.get(siteName))%>">
                                        <%=Encode.forHtml(siteShortName.get(siteName))%>
                                    </td>
                                    <%} %>
                                </tr>
                                <%}%>
                            </table>

                        </td>
                    </tr>
                </table>


                <%
                    if (offset > 0) {
//                		String queryString = getNewQueryString(request.getQueryString(),offset-limit,limit);
                %><input type="button" value="Prev" onClick="gotoPage(false);"/><%
                }
                if (theRequests.ids.size() == limit) {
//                		String queryString = getNewQueryString(request.getQueryString(),offset+limit,limit);
            %><input type="button" value="Next" onClick="gotoPage(true);"/><%
                }
            %>

            </td>
        </tr>
        <tr>
            <td class="MainTableBottomRowLeftColumn">

            </td>
            <td class="MainTableBottomRowRightColumn">
                <% if (tickerList.size() > 0) {
                    String queryStr = "";
                    for (int i = 0; i < tickerList.size(); i++) {
                        String demo = (String) tickerList.get(i);
                        if (i == 0) {
                            queryStr += "demo=" + demo;
                        } else {
                            queryStr += "&demo=" + demo;
                        }
                    }%>
                <a target="_blank"
                   href="<%= request.getContextPath() %>/tickler/AddTickler.do?<%=Encode.forUriComponent(String.valueOf(queryStr))%>&message=<%=Encode.forUriComponent(String.valueOf("Patient has Consultation Letter with a status of 'Nothing Done' for over one week"))%>">Add
                    Tickler for Consults with ND for more than one week</a>
                <%}%>
            </td>
        </tr>
    </table>
    <script type="text/javascript">
        Calendar.setup({
            inputField: "startDate",
            ifFormat: "%Y-%m-%d",
            showsTime: false,
            button: "SCal",
            singleClick: true,
            step: 1
        });
        Calendar.setup({
            inputField: "endDate",
            ifFormat: "%Y-%m-%d",
            showsTime: false,
            button: "ECal",
            singleClick: true,
            step: 1
        });
    </script>
    </body>

</html>
<%!
    /*
    String getNewQueryString(String queryString,Integer offset, Integer limit) {

        String result = "";
        List<String> resultParts = new ArrayList<String>();

        String[] parts = queryString.split("&");
        for(String part:parts) {

            if(!part.startsWith("offset=") && !part.startsWith("limit=")) {
                resultParts.add(part);
            }
        }

        resultParts.add("offset=" + (offset!=null?offset:0));
        resultParts.add("limit=" + (limit != null?limit:ConsultationRequestDao.DEFAULT_CONSULT_REQUEST_RESULTS_LIMIT));
        for(int x=0;x<resultParts.size();x++) {
            if(x>0)
                result += "&";
            result += resultParts.get(x);
        }

        return result;
    }
    */

    boolean dateGreaterThan(String dateStr, int unit, int period) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date prevDate = null;
        try {
            prevDate = formatter.parse(dateStr);
        } catch (Exception e) {
            return false;
        }

        Calendar bonusEl = Calendar.getInstance();
        bonusEl.add(unit, period);
        Date bonusStartDate = bonusEl.getTime();

        return bonusStartDate.after(prevDate);
    }

%>
