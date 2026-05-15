<%--

    Copyright (c) 2008-2012 Indivica Inc.

    This software is made available under the terms of the
    GNU General Public License, Version 2, 1991 (GPLv2).
    License details are available via "indivica.ca/gplv2"
    and "gnu.org/licenses/gpl-2.0.html".

--%>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="ca.openosp.openo.olis.model.OLISParticipatingLab" %>
<%@ include file="/casemgmt/taglibs.jsp" %>

<%
    String curUser_no;
    curUser_no = (String) session.getAttribute("user");

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>
<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.olisPrefs"/></title>

        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/oscarEncounter/encounterStyles.css">
        <link rel="stylesheet" type="text/css" media="all" href="<c:out value="${ctx}"/>/share/calendar/calendar.css"
              title="win2k-cold-1">
        <script src="<c:out value="${ctx}"/>/share/javascript/prototype.js" type="text/javascript"></script>
        <script src="<c:out value="${ctx}"/>/share/javascript/scriptaculous.js" type="text/javascript"></script>
        <script src="<c:out value="${ctx}"/>/js/jquery.js"></script>
        <script>
            jQuery.noConflict();
        </script>


    </head>

    <body class="BodyStyle" vlink="#0000FF">

    <table class="MainTable" id="scrollNumber1" name="encounterTable">
        <tr class="MainTableTopRow">
            <td class="MainTableTopRowLeftColumn"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.setNoteStaleDate.msgPrefs"/></td>
            <td style="color: white" class="MainTableTopRowRightColumn"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.olisPrefs"/></td>
        </tr>
        <tr>
            <td class="MainTableLeftColumn">&nbsp;</td>
            <td class="MainTableRightColumn">
                <!-- form starts here -->
                <form action="<c:out value="${ctx}"/>/provider/OlisPreferences.do?method=save" method="post">
                    <table width="100%" border="1">
                        <tr>
                            <th width="20%">Default Reporting Laboratory</th>
                            <td colspan="3">
                                <%
                                    String val1 = (String) request.getAttribute("reportingLaboratory");
                                    if (val1 == null) val1 = "";
                                %>
                                <select id="reportingLaboratory" name="reportingLaboratory">
                                    <option value="" <%=(val1.equals("") ? "selected=\"selected\"" : "") %>></option>
                                    <%
                                        for (OLISParticipatingLab olisLab : OLISParticipatingLab.values()) {
                                    %>
                                    <option value="<%=olisLab.getLabNo()%>" <%=(val1.equals(olisLab.getLabNo()) ? "selected=\"selected\"" : "") %>><%=Encode.forHtml(olisLab.getDisplayName())%></option>
                                    <%
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <th width="20%">Default Exclude Reporting Laboratory</th>
                            <td width="30%">
                                <%
                                    val1 = (String) request.getAttribute("excludeReportingLaboratory");
                                    if (val1 == null) val1 = "";
                                %>
                                <select id="excludeReportingLaboratory" name="excludeReportingLaboratory">
                                    <option value="" <%=(val1.equals("") ? "selected=\"selected\"" : "") %>></option>
                                    <%
                                        for (OLISParticipatingLab olisLab : OLISParticipatingLab.values()) {
                                    %>
                                    <option value="<%=olisLab.getLabNo()%>" <%=(val1.equals(olisLab.getLabNo()) ? "selected=\"selected\"" : "") %>><%=Encode.forHtml(olisLab.getDisplayName())%></option>
                                    <%
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <th width="20%">Automated Polling Frequency (in minutes)</th>
                            <td width="30%">
                                <%
                                    val1 = (String) request.getAttribute("pollingFrequency");
                                    if (val1 == null) val1 = "";
                                %>
                                <input type="text" id="pollingFrequency" name="pollingFrequency" value="<%=Encode.forHtmlAttribute(String.valueOf(val1))%>">

                            </td>
                        </tr>

                        <tr>
                            <th width="20%">Start Time (for polling):</th>
                            <td width="30%">
                                <%
                                    val1 = (String) request.getAttribute("olis_provider_start_time");
                                    if (val1 == null) val1 = "";
                                %>
                                <input type="text" id="providerStartTime" name="providerStartTime" value="<%=Encode.forHtmlAttribute(String.valueOf(val1))%>">
                                (YYYY-MM-DD hh:mm:ss [-/+]ZZZZ)
                                <br>
                                <h6 style="color:red">note: this field will be auto-updated by the system</h6>
                            </td>
                        </tr>

                        <tr>
                            <th width="20%">Unmatched Patient Results</th>
                            <td width="30%">
                                <%
                                    Boolean filterPatientsPref = (Boolean) request.getAttribute("filterPatients");
                                    String filterPatientsVal = (filterPatientsPref == null) ? "" : filterPatientsPref.toString();
                                %>
                                <select id="filterPatients" name="filterPatients">
                                    <option value="" <%=("".equals(filterPatientsVal) ? "selected=\"selected\"" : "")%>>Use system default</option>
                                    <option value="true" <%=("true".equals(filterPatientsVal) ? "selected=\"selected\"" : "")%>>Filter &mdash; send to unclaimed worklist</option>
                                    <option value="false" <%=("false".equals(filterPatientsVal) ? "selected=\"selected\"" : "")%>>Send to my inbox</option>
                                </select>
                                <br>
                                <h6>where OLIS results that don't match a patient in this system are routed; overrides the system-level setting</h6>
                            </td>
                        </tr>


                    </table>
                    <input type="submit" value="Save Changes"/>
                </form>
                <!-- end of form -->
            </td>
        </tr>
        <tr>
            <td class="MainTableBottomRowLeftColumn"></td>
            <td class="MainTableBottomRowRightColumn"></td>
        </tr>
    </table>
    </body>
</html>
