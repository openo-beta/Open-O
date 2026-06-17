<%--

    Copyright (c) 2008-2012 Indivica Inc.

    This software is made available under the terms of the
    GNU General Public License, Version 2, 1991 (GPLv2).
    License details are available via "indivica.ca/gplv2"
    and "gnu.org/licenses/gpl-2.0.html".

--%>
<%@page import="org.apache.commons.lang3.StringUtils" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="java.util.*,
                ca.openosp.openo.commn.dao.DemographicDao,
                ca.openosp.openo.commn.model.Demographic,
                ca.openosp.openo.PMmodule.dao.ProviderDao,
                ca.openosp.openo.commn.model.Provider,
                ca.openosp.openo.olis.dao.OLISFacilityDao,
                ca.openosp.openo.olis.model.OLISFacility,
                ca.openosp.openo.utility.SpringUtils" %>
<%@page import="ca.openosp.openo.commn.dao.UserPropertyDAO" %>
<%@page import="ca.openosp.openo.commn.model.UserProperty" %>
<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/oscar-tag.tld" prefix="oscar" %>

<%
    if (session.getValue("user") == null) response.sendRedirect(request.getContextPath() + "/logout.jsp");
    LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
    DemographicDao demographicDao = SpringUtils.getBean(DemographicDao.class);

    String demographicNoParam = request.getParameter("demographicNo");
    String demographicNo = null;
    String demographicKeyword = null;
    if (!StringUtils.isEmpty(demographicNoParam)) {
        Demographic demographic = demographicDao.getDemographic(demographicNoParam);
        if (demographic != null) {
            demographicNo = demographic.getDemographicNo().toString();
            demographicKeyword = demographic.getFormattedName() + "(" + demographic.getBirthDayAsString() + ")";
        }
    }
%>


<%
    String outcome = (String) request.getAttribute("outcome");
    if (outcome != null) {
        if (outcome.equalsIgnoreCase("success")) {
%>
<script type="text/javascript">alert("Lab uploaded successfully");
opener.refreshView();</script>
<%
} else if (outcome.equalsIgnoreCase("uploaded previously")) {
%>
<script type="text/javascript">alert("Lab has already been uploaded");</script>
<%
} else if (outcome.equalsIgnoreCase("exception")) {
%>
<script type="text/javascript">alert("Exception uploading the lab");</script>
<%
} else {
%>
<script type="text/javascript">alert("Failed to upload lab");</script>
<%
        }
    }

%>


<html>
<head>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.js"></script>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><fmt:setBundle basename="oscarResources"/><fmt:message key="olis.olisSearch"/></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/share/css/OscarStandardLayout.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/share/css/OscarStandardLayout.css">
    <script type="text/javascript" src="<%= request.getContextPath() %>/share/javascript/Oscar.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/share/javascript/Oscar.js"></script>

    <script type="text/javascript" src="<%= request.getContextPath() %>/share/yui/js/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/share/yui/js/connection-min.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/share/yui/js/animation-min.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/share/yui/js/datasource-min.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/share/yui/js/autocomplete-min.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/demographicProviderAutocomplete.js"></script>

    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/share/yui/css/fonts-min.css"/>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/share/yui/css/autocomplete.css"/>

    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/library/jquery/jquery-ui-1.12.1.min.css"/>
    <script type="text/javascript" src="<%= request.getContextPath() %>/library/jquery/jquery-3.6.4.min.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/library/jquery/jquery-ui-1.12.1.min.js"></script>


    <script type="text/javascript">
        function selectOther() {
            if (document.UPLOAD.type.value == "OTHER")
                document.getElementById('OTHER').style.visibility = "visible";
            else
                document.getElementById('OTHER').style.visibility = "hidden";
        }

        function checkInput() {
            if (document.UPLOAD.lab.value == "") {
                alert("Please select a lab for upload");
                return false;
            } else if (document.UPLOAD.type.value == "OTHER" && document.UPLOAD.otherType.value == "") {
                alert("Please specify the other message type");
                return false;
            } else {
                var lab = document.UPLOAD.lab.value;
                var ext = lab.substring((lab.length - 3), lab.length);
                if (ext != 'hl7' && ext != 'xml') {
                    alert("Error: The lab must be either a .xml or .hl7 file");
                    return false;
                }
            }
            return true;
        }

        function checkBlockedConsent(form) {
            const field = document.forms[form + "_form"].blockedInformationConsent;
            const value = field ? field.value : null;
            if (value != null && value == "Z") {
                return confirm("You have chosen to view blocked information.  This action is recorded in the audit log.  Are you sure?")
            }
            return true;
        }

        $(document).ready(function () {

            $("[name='requestingHic']").each(function () {
                $(this).val('<%=Encode.forJavaScript(String.valueOf(loggedInInfo.getLoggedInProviderNo()))%>');
            });

            <%if(demographicNo != null && demographicKeyword != null) {%>
            $("[name='demographic']").each(function () {
                $(this).val('<%=Encode.forJavaScript(String.valueOf(demographicNo))%>');
            });
            $("[name='demographicKeyword']").each(function () {
                $(this).val('<%=Encode.forJavaScript(String.valueOf(demographicKeyword))%>');
            });
            <% } %>

        });
    </script>

    <style type="text/css">
        table {
            font-size: 12px;
            width: 1000px;
        }

        table.innerTable {
            width: 600px;
        }

        table.smallTable {
            width: 300px;
        }

        th {
            text-align: right;
            font-size: 14px;
        }

        td span {
            font-size: 14px;
            font-weight: bold;
        }

        input {
            width: 120px;
        }

        input.checkbox {
            width: auto;
        }
    </style>
    <style type="text/css">
        #myAutoComplete {
            width: 15em; /* set width here or else widget will expand to fit its container */
            padding-bottom: 2em;
        }


        .yui-ac {
            position: relative;
            font-family: arial;
            font-size: 100%;
        }

        /* styles for input field */
        .yui-ac-input {
            position: relative;
            width: 100%;
        }

        /* styles for results container */
        .yui-ac-container {
            position: absolute;
            top: 0em;
            width: 100%;
        }

        /* styles for header/body/footer wrapper within container */
        .yui-ac-content {
            position: absolute;
            width: 100%;
            border: 1px solid #808080;
            background: #fff;
            overflow: hidden;
            z-index: 9050;
        }

        /* styles for container shadow */
        .yui-ac-shadow {
            position: absolute;
            margin: .0em;
            width: 100%;
            background: #000;
            -moz-opacity: 0.10;
            opacity: .10;
            filter: alpha(opacity=10);
            z-index: 9049;
        }

        /* styles for results list */
        .yui-ac-content ul {
            margin: 0;
            padding: 0;
            width: 100%;
        }

        /* styles for result item */
        .yui-ac-content li {
            margin: 0;
            padding: 0px 0px;
            cursor: default;
            white-space: nowrap;
        }

        /* styles for prehighlighted result item */
        .yui-ac-content li.yui-ac-prehighlight {
            background: #B3D4FF;
        }

        /* styles for highlighted result item */
        .yui-ac-content li.yui-ac-highlight {
            background: #426FD9;
            color: #FFF;
        }

        .nomenclature-chips {
            margin-top: 6px;
            max-width: 300px;
        }

        .nomenclature-chip {
            display: inline-block;
            padding: 2px 6px;
            margin: 2px;
            background: #e0e0e0;
            border-radius: 3px;
            font-size: 12px;
            line-height: 1.4;
        }

        .nomenclature-chip-remove {
            color: #666;
            text-decoration: none;
            margin-left: 4px;
            font-weight: bold;
        }

        .nomenclature-chip-remove:hover {
            color: #000;
        }

        /* Scrollable autocomplete dropdown — without max-height jQuery UI just
           keeps growing the suggestion list off-screen. 400px is ~16 rows tall;
           browse-by-scroll for large result sets, type-to-narrow for precision. */
        .ui-autocomplete {
            max-height: 400px;
            overflow-y: auto;
            overflow-x: hidden;
        }
    </style>

</head>

<body>

<table style="width:600px;" class="MainTable" align="left">
    <tbody>
    <tr class="MainTableTopRow">
        <td class="MainTableTopRowLeftColumn" width="175">OLIS</td>
        <td class="MainTableTopRowRightColumn">
            <table class="TopStatusBar">
                <tbody>
                <tr>
                    <td>Search</td>
                    <td>&nbsp;</td>
                    <td style="text-align: right"><a href="javascript:popupStart(300,400,'Help.jsp')"><u>H</u>elp</a> |
                        <a href="javascript:popupStart(300,400,'About.jsp')">About</a> | <a
                                href="javascript:popupStart(300,400,'License.jsp')">License</a></td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="2">


            <script type="text/javascript">
                var currentQuery = "Z01";

                function displaySearch(selectBox) {
                    queryType = document.getElementById("queryType").value;
                    if (document.getElementById(queryType + "_query") != null) {
                        document.getElementById(currentQuery + "_query").style.display = "none";
                        document.getElementById(queryType + "_query").style.display = "block";
                        currentQuery = queryType;
                    }

                }

            </script>


            <%
                ProviderDao providerDao = (ProviderDao) SpringUtils.getBean(ProviderDao.class);
                List<Provider> allProvidersList = providerDao.getActiveProviders(true);
            %>


            <select id="queryType" onchange="displaySearch(this)" style="margin-left:30px;">
                <option value="Z01">Z01 - Retrieve Laboratory Information for Patient</option>
                <!--
                <option value="Z02">Z02 - Retrieve Laboratory Information for Order ID</option>
                -->
                <option value="Z04">Z04 - Retrieve Laboratory Information Updates for Practitioner</option>
                <!--
                <option value="Z05">Z05 - Retrieve Laboratory Information Updates for Destination Laboratory</option>
                <option value="Z06">Z06 - Retrieve Laboratory Information Updates for Ordering Facility</option>
                <option value="Z07">Z07 - Retrieve Test Results Reportable to Public Health</option>
                <option value="Z08">Z08 - Retrieve Test Results Reportable to Cancer Care Ontario</option>
                <option value="Z50">Z50 - Identify Patient by Name, Sex, and Date of Birth</option>
                -->
            </select>

            <form action="<%=request.getContextPath() %>/olis/Search.do" method="POST"
                  onSubmit="return checkBlockedConsent('Z01')" name="Z01_form">
                <input type="hidden" name="queryType" value="Z01"/>
                <table id="Z01_query">
                    <tbody>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    <tr>
                        <th width="20%">Date &amp; Time Period to Search<br/>(yyyy-mm-dd)</th>
                        <td width="30%"><input style="width:150px" type="text" name="startTimePeriod"
                                               id="startTimePeriod" value=""> to <input style="width:150px"
                                                                                        name="endTimePeriod" type="text"
                                                                                        id="endTimePeriod"></td>
                    </tr>
                    <tr>
                        <th width="20%">Observation Date &amp; Time Period<br/>(yyyy-mm-dd)</th>
                        <td width="30%"><input style="width:150px;" type="text" name="observationStartTimePeriod"
                                               id="observationStartTimePeriod"> to <input style="width:150px"
                                                                                          name="obsevationEndTimePeriod"
                                                                                          type="text"
                                                                                          id="observationEndTimePeriod">
                        </td>
                    </tr>
                    <tr>
                        <th width="20%"><input class="checkbox" type="checkbox" name="quantityLimitedQuery"
                                               id="quantityLimitedQuery"> Quantity Limit?
                        </th>
                        <td width="30%">Quantity<br><input type="text" id="quantityLimit" name="quantityLimit"></td>
                    </tr>
                    <tr>
                        <th width="20%">Consent to View Blocked Information?</th>
                        <td width="30%"><select id="blockedInformationConsent" name="blockedInformationConsent"><option value="">(none)</option>
                        <option value="Z">Temporary </option>
                        </select>
                        &nbsp;&nbsp;Authorized by: <select name="blockedInformationIndividual" id="blockedInformationIndividual">
                        <option value="patient">Patient</option><option value="substitute">Substitute Decision Maker</option><option value="">Neither</option>
                        </select>
                        </td>
                    </tr>
                    <tr>
                        <td width="20%" colspan=4><span><input class="checkbox" type="checkbox" name="consentBlockAllIndicator" id="consentBlockAllIndicator"> Enable Patient Consent Block-All Indicator?</span></td>
                    </tr>
                    <tr>
                        <th width="20%">Specimen Collector</th>
                        <td width="30%">
                            <input type="text" id="specimenCollectorAC" autocomplete="off"
                                   placeholder="Type 2+ characters to search..." style="width: 250px;"/>
                            <input type="hidden" id="specimenCollectorAC_hidden" name="specimenCollector"/>
                            <div id="specimenCollectorAC_chip" class="nomenclature-chips"></div>
                        </td>
                    </tr>
                    <tr>
                        <th width="20%">Performing Laboratory</th>
                        <td width="30%">
                            <input type="text" id="performingLaboratoryAC" autocomplete="off"
                                   placeholder="Type 2+ characters to search..." style="width: 250px;"/>
                            <input type="hidden" id="performingLaboratoryAC_hidden" name="performingLaboratory"/>
                            <div id="performingLaboratoryAC_chip" class="nomenclature-chips"></div>
                        </td>
                    </tr>
                    <tr>
                        <th width="20%">Exclude Performing Laboratory</th>
                        <td width="30%">
                            <input type="text" id="excludePerformingLaboratoryAC" autocomplete="off"
                                   placeholder="Type 2+ characters to search..." style="width: 250px;"/>
                            <input type="hidden" id="excludePerformingLaboratoryAC_hidden" name="excludePerformingLaboratory"/>
                            <div id="excludePerformingLaboratoryAC_chip" class="nomenclature-chips"></div>
                        </td>
                    </tr>
                    <%
                        UserPropertyDAO upDao = (UserPropertyDAO) SpringUtils.getBean(UserPropertyDAO.class);
                        String providerNo = loggedInInfo.getLoggedInProviderNo();
                        UserProperty repLabProp = upDao.getProp(providerNo, "olis_reportingLab");
                        UserProperty exRepLabProp = upDao.getProp(providerNo, "olis_exreportingLab");

                        String reportingLabVal = (repLabProp != null) ? repLabProp.getValue() : "";
                        String exReportingLabVal = (exRepLabProp != null) ? exRepLabProp.getValue() : "";

                        OLISFacilityDao facilityDao = SpringUtils.getBean(OLISFacilityDao.class);
                        String reportingLabLabel = "";
                        if (!reportingLabVal.isEmpty()) {
                            OLISFacility f = facilityDao.findByClassAndLicence(OLISFacility.CLASS_LAB, reportingLabVal);
                            if (f != null) reportingLabLabel = f.getName() + " [" + f.getLicenceNumber() + "]";
                        }
                        String exReportingLabLabel = "";
                        if (!exReportingLabVal.isEmpty()) {
                            OLISFacility f = facilityDao.findByClassAndLicence(OLISFacility.CLASS_LAB, exReportingLabVal);
                            if (f != null) exReportingLabLabel = f.getName() + " [" + f.getLicenceNumber() + "]";
                        }
                    %>
                    <tr>
                        <th width="20%">Reporting Laboratory</th>
                        <td colspan="3">
                            <input type="text" id="reportingLaboratoryAC" autocomplete="off"
                                   placeholder="Type 2+ characters to search..." style="width: 250px;"
                                   data-preload-label="<%=Encode.forHtmlAttribute(reportingLabLabel)%>"/>
                            <input type="hidden" id="reportingLaboratoryAC_hidden" name="reportingLaboratory"
                                   value="<%=Encode.forHtmlAttribute(reportingLabVal)%>"/>
                            <div id="reportingLaboratoryAC_chip" class="nomenclature-chips"></div>
                        </td>
                    </tr>
                    <tr>
                        <th width="20%">Exclude Reporting Laboratory</th>
                        <td width="30%">
                            <input type="text" id="excludeReportingLaboratoryAC" autocomplete="off"
                                   placeholder="Type 2+ characters to search..." style="width: 250px;"
                                   data-preload-label="<%=Encode.forHtmlAttribute(exReportingLabLabel)%>"/>
                            <input type="hidden" id="excludeReportingLaboratoryAC_hidden" name="excludeReportingLaboratory"
                                   value="<%=Encode.forHtmlAttribute(exReportingLabVal)%>"/>
                            <div id="excludeReportingLaboratoryAC_chip" class="nomenclature-chips"></div>
                        </td>
                    </tr>
                    <tr>
                        <td colspan=4>
                            <hr/>
                        </td>
                    </tr>
                    <tr>
                        <td><span>Patient</span></td>
                        <td>
                            <%String currentDocId = "1"; %>
                            <input type="hidden" name="demographic" id="demofind<%=Encode.forHtmlAttribute(String.valueOf(currentDocId))%>"/>
                            <input type="text" id="autocompletedemo<%=Encode.forHtmlAttribute(String.valueOf(currentDocId))%>"
                                   onchange="checkSave('<%=Encode.forJavaScript(String.valueOf(currentDocId))%>')" name="demographicKeyword"/>
                            <div id="autocomplete_choices<%=Encode.forHtmlAttribute(String.valueOf(currentDocId))%>" class="autocomplete"></div>

                            <script type="text/javascript">       <%-- testDemocomp2.jsp    --%>


                            YAHOO.example.BasicRemote = function () {
                                var url = "<%=request.getContextPath()%>/demographic/SearchDemographic.do";
                                var oDS = new YAHOO.util.XHRDataSource(url, {
                                    connMethodPost: true,
                                    connXhrMode: 'ignoreStaleResponses'
                                });
                                oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;// Set the responseType
                                // Define the schema of the delimited resultsTEST, PATIENT(1985-06-15)
                                oDS.responseSchema = {
                                    resultsList: "results",
                                    fields: ["formattedName", "fomattedDob", "demographicNo", "status"]
                                };
                                // Enable caching
                                oDS.maxCacheEntries = 100;
                                //oDS.connXhrMode ="cancelStaleRequests";

                                // Instantiate the AutoComplete
                                var oAC = new YAHOO.widget.AutoComplete("autocompletedemo<%=Encode.forJavaScript(String.valueOf(currentDocId))%>", "autocomplete_choices<%=Encode.forJavaScript(String.valueOf(currentDocId))%>", oDS);
                                oAC.queryMatchSubset = true;
                                oAC.minQueryLength = 3;
                                oAC.maxResultsDisplayed = 25;
                                oAC.formatResult = resultFormatter2;
                                //oAC.typeAhead = true;
                                oAC.queryMatchContains = true;
                                oAC.itemSelectEvent.subscribe(function (type, args) {
                                    var str = args[0].getInputEl().id.replace("autocompletedemo", "demofind");
                                    document.getElementById(str).value = args[2][2];//li.id;
                                    args[0].getInputEl().value = args[2][0] + "(" + args[2][1] + ")";
                                    selectedDemos.push(args[0].getInputEl().value);

                                });


                                return {
                                    oDS: oDS,
                                    oAC: oAC
                                };
                            }();


                            </script>

                        </td>
                    </tr>
                    <tr>
                        <td colspan=4>
                            <hr/>
                        </td>
                    </tr>
                    <tr>
                        <td><span>Requesting HIC</span></td>
                        <td>
                            <select name="requestingHic" id="requestingHic">

                                <option value=""></option>
                                <%
                                    for (Provider provider : allProvidersList) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(String.valueOf(provider.getProviderNo()))%>">[<%=Encode.forHtml(String.valueOf(provider.getProviderNo()))%>
                                    ] <%=Encode.forHtml(String.valueOf(provider.getLastName()))%>, <%=Encode.forHtml(String.valueOf(provider.getFirstName()))%>
                                </option>
                                <%
                                    }
                                %>
                            </select></td>
                    </tr>

                    <tr>
                        <td>
                            <hr>
                        </td>
                    </tr>
                    <tr>
                        <th width="20%">Ordering Practitioner</th>
                        <td>
                            <select name="orderingPractitioner" id="orderingPractitioner">
                                <option value=""></option>
                                <%
                                    for (Provider provider : allProvidersList) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(String.valueOf(provider.getProviderNo()))%>">[<%=Encode.forHtml(String.valueOf(provider.getProviderNo()))%>
                                    ] <%=Encode.forHtml(String.valueOf(provider.getLastName()))%>, <%=Encode.forHtml(String.valueOf(provider.getFirstName()))%>
                                </option>
                                <%
                                    }
                                %>
                            </select></td>
                    </tr>
                    <tr>
                        <th width="20%">Copied-to Practitioner</th>
                        <td>
                            <select name="copiedToPractitioner" id="copiedToPractitioner">
                                <option value=""></option>
                                <%
                                    for (Provider provider : allProvidersList) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(String.valueOf(provider.getProviderNo()))%>">[<%=Encode.forHtml(String.valueOf(provider.getProviderNo()))%>
                                    ] <%=Encode.forHtml(String.valueOf(provider.getLastName()))%>, <%=Encode.forHtml(String.valueOf(provider.getFirstName()))%>
                                </option>
                                <%
                                    }
                                %>
                            </select></td>
                    </tr>
                    <tr>
                        <th width="20%">Attending Practitioner</th>
                        <td>
                            <select name="attendingPractitioner" id="attendingPractitioner">
                                <option value=""></option>
                                <%
                                    for (Provider provider : allProvidersList) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(String.valueOf(provider.getProviderNo()))%>">[<%=Encode.forHtml(String.valueOf(provider.getProviderNo()))%>
                                    ] <%=Encode.forHtml(String.valueOf(provider.getLastName()))%>, <%=Encode.forHtml(String.valueOf(provider.getFirstName()))%>
                                </option>
                                <%
                                    }
                                %>
                            </select></td>
                    </tr>
                    <tr>
                        <th width="20%">Admitting Practitioner</th>
                        <td>
                            <select name="admittingPractitioner" id="admittingPractitioner">
                                <option value=""></option>
                                <%
                                    for (Provider provider : allProvidersList) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(String.valueOf(provider.getProviderNo()))%>">[<%=Encode.forHtml(String.valueOf(provider.getProviderNo()))%>
                                    ] <%=Encode.forHtml(String.valueOf(provider.getLastName()))%>, <%=Encode.forHtml(String.valueOf(provider.getFirstName()))%>
                                </option>
                                <%
                                    }
                                %>
                            </select></td>
                    </tr>
                    <tr>
                        <th width="20%">Test Request Placer</th>
                        <td>
                            <input type="text" id="testRequestPlacerAC" autocomplete="off"
                                   placeholder="Type 2+ characters to search..." style="width: 250px;"/>
                            <input type="hidden" id="testRequestPlacerAC_hidden" name="testRequestPlacer"/>
                            <div id="testRequestPlacerAC_chip" class="nomenclature-chips"></div>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="4">
                            <table>
                                <tbody>
                                <tr>
                                    <th width="20%">Test Request Status (max. 15)</th>
                                    <td><select multiple="multiple" id="testRequestStatus" name="testRequestStatus">
                                        <option value=""></option>
                                        <option value="O"> Order Received</option>
                                        <option value="I"> No results</option>
                                        <option value="P"> Preliminary</option>
                                        <option value="A"> Partial</option>
                                        <option value="F"> Final</option>
                                        <option value="C"> Correction</option>
                                        <option value="X"> Cancelled</option>
                                        <option value="E"> Expired</option>
                                    </select></td>
                                    <th width="20%">Test Result Code (max. 200)</th>
                                    <td>
                                        <input type="text" id="testResultCodeAC" autocomplete="off"
                                               placeholder="Type 2+ characters to search..."
                                               style="width: 290px;"/>
                                        <div id="testResultCodeChips" class="nomenclature-chips"></div>
                                    </td>
                                    <th width="20%">Test Request Code (max. 100)</th>
                                    <td>
                                        <input type="text" id="testRequestCodeAC" autocomplete="off"
                                               placeholder="Type 2+ characters to search..."
                                               style="width: 290px;"/>
                                        <div id="testRequestCodeChips" class="nomenclature-chips"></div>
                                        <script type="text/javascript">
                                            (function ($) {
                                                var ctxPath = '<%=Encode.forJavaScript(request.getContextPath())%>';
                                                var endpoint = ctxPath + '/olis/NomenclatureSearch.do';

                                                function addChip(chipsId, fieldName, code, label) {
                                                    var container = document.getElementById(chipsId);
                                                    if (!container) return;
                                                    var existing = container.querySelectorAll('input[name="' + fieldName + '"]');
                                                    for (var i = 0; i < existing.length; i++) {
                                                        if (existing[i].value === code) return;
                                                    }
                                                    var chip = document.createElement('span');
                                                    chip.className = 'nomenclature-chip';
                                                    chip.appendChild(document.createTextNode(label + ' '));
                                                    var remove = document.createElement('a');
                                                    remove.href = '#';
                                                    remove.className = 'nomenclature-chip-remove';
                                                    remove.appendChild(document.createTextNode('×'));
                                                    remove.onclick = function (e) {
                                                        e.preventDefault();
                                                        container.removeChild(chip);
                                                        return false;
                                                    };
                                                    chip.appendChild(remove);
                                                    var hidden = document.createElement('input');
                                                    hidden.type = 'hidden';
                                                    hidden.name = fieldName;
                                                    hidden.value = code;
                                                    chip.appendChild(hidden);
                                                    container.appendChild(chip);
                                                }

                                                function makeAC(inputSelector, chipsId, fieldName, type) {
                                                    $(inputSelector).autocomplete({
                                                        minLength: 2,
                                                        source: function (request, response) {
                                                            $.getJSON(endpoint, {query: request.term, type: type}, function (data) {
                                                                response($.map(data.results || [], function (item) {
                                                                    return {label: item.name, value: item.name, code: item.code};
                                                                }));
                                                            });
                                                        },
                                                        select: function (event, ui) {
                                                            addChip(chipsId, fieldName, ui.item.code, ui.item.label);
                                                            $(this).val('');
                                                            return false;
                                                        },
                                                        focus: function () {
                                                            return false;
                                                        }
                                                    });
                                                }

                                                makeAC('#testResultCodeAC', 'testResultCodeChips', 'testResultCode', 'result');
                                                makeAC('#testRequestCodeAC', 'testRequestCodeChips', 'testRequestCode', 'request');
                                            })(jQuery);
                                        </script>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    </tbody>
                </table>
            </form>


            <form action="<%=request.getContextPath() %>/olis/Search.do" method="POST"
                  onSubmit="return checkBlockedConsent('Z02')" name="Z02_form">
                <input type="hidden" name="queryType" value="Z02"/>
                <table id="Z02_query" style="display: none;">
                    <tbody>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    <tr>
                        <td width="50%" colspan=2><span><input class="checkbox" type="checkbox"
                                                               name="retrieveAllResults" id="retrieveAllResults"> Retrieve All Test Results?</span>
                        </td>
                        <th width="20%">Consent to View Blocked Information?</th>
                        <td width="30%"><select id="blockedInformationConsent" name="blockedInformationConsent">
                            <option value="">(none)</option>
                            <option value="Z">Temporary</option>
                        </select>
                            <br/>Authorized by: <select name="blockedInformationIndividual"
                                                        id="blockedInformationIndividual">
                                <option value="patient">Patient</option>
                                <option value="substitute">Substitute Decision Maker</option>
                                <option value="">Neither</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td width="20%" colspan=4><span><input class="checkbox" type="checkbox"
                                                               name="consentBlockAllIndicator"
                                                               id="consentBlockAllIndicator"> Enable Patient Consent Block-All Indicator?</span>
                        </td>
                    </tr>
                    <tr>
                        <td colspan=4>
                            <hr/>
                        </td>
                    </tr>
                    <tr>
                        <td width="20%"><span>Patient</span></td>
                        <td>
                            <%currentDocId = "2"; %>
                            <input type="hidden" name="demographic" id="demofind<%=Encode.forHtmlAttribute(String.valueOf(currentDocId))%>"/>
                            <input type="text" id="autocompletedemo<%=Encode.forHtmlAttribute(String.valueOf(currentDocId))%>"
                                   onchange="checkSave('<%=Encode.forJavaScript(String.valueOf(currentDocId))%>')" name="demographicKeyword"/>
                            <div id="autocomplete_choices<%=Encode.forHtmlAttribute(String.valueOf(currentDocId))%>" class="autocomplete"></div>

                            <script type="text/javascript">       <%-- testDemocomp2.jsp    --%>


                            YAHOO.example.BasicRemote = function () {
                                var url = "<%=request.getContextPath()%>/demographic/SearchDemographic.do";
                                var oDS = new YAHOO.util.XHRDataSource(url, {
                                    connMethodPost: true,
                                    connXhrMode: 'ignoreStaleResponses'
                                });
                                oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;// Set the responseType
                                // Define the schema of the delimited resultsTEST, PATIENT(1985-06-15)
                                oDS.responseSchema = {
                                    resultsList: "results",
                                    fields: ["formattedName", "fomattedDob", "demographicNo", "status"]
                                };
                                // Enable caching
                                oDS.maxCacheEntries = 100;

                                // Instantiate the AutoComplete
                                var oAC = new YAHOO.widget.AutoComplete("autocompletedemo<%=Encode.forJavaScript(String.valueOf(currentDocId))%>", "autocomplete_choices<%=Encode.forJavaScript(String.valueOf(currentDocId))%>", oDS);
                                oAC.queryMatchSubset = true;
                                oAC.minQueryLength = 3;
                                oAC.maxResultsDisplayed = 25;
                                oAC.formatResult = resultFormatter2;
                                //oAC.typeAhead = true;
                                oAC.queryMatchContains = true;
                                oAC.itemSelectEvent.subscribe(function (type, args) {
                                    var str = args[0].getInputEl().id.replace("autocompletedemo", "demofind");

                                    document.getElementById(str).value = args[2][2];//li.id;
                                    args[0].getInputEl().value = args[2][0] + "(" + args[2][1] + ")";
                                    selectedDemos.push(args[0].getInputEl().value);

                                });


                                return {
                                    oDS: oDS,
                                    oAC: oAC
                                };
                            }();


                            </script>

                        </td>
                    </tr>
                    <tr>
                        <td colspan=4>
                            <hr/>
                        </td>
                    </tr>
                    <tr>
                        <td width="20%"><span>Requesting HIC</span></td>
                        <td><select name="requestingHic" id="requestingHic">

                            <option value=""></option>
                            <%
                                for (Provider provider : allProvidersList) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(String.valueOf(provider.getProviderNo()))%>">[<%=Encode.forHtml(String.valueOf(provider.getProviderNo()))%>
                                ] <%=Encode.forHtml(String.valueOf(provider.getLastName()))%>, <%=Encode.forHtml(String.valueOf(provider.getFirstName()))%>
                            </option>
                            <%
                                }
                            %>
                        </select></td>
                    </tr>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    </tbody>
                </table>
            </form>


            <form action="<%=request.getContextPath() %>/olis/Search.do" method="POST">
                <input type="hidden" name="queryType" value="Z04"/>
                <table id="Z04_query" style="display: none;">
                    <tbody>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    <tr>
                        <th width="20%">Date &amp; Time Period to Search<br/>(yyyy-mm-dd)</th>
                        <td width="30%"><input style="width:150px" type="text" name="startTimePeriod"
                                               id="startTimePeriod" value=""> to <input style="width:150px"
                                                                                        name="endTimePeriod" type="text"
                                                                                        id="endTimePeriod"></td>
                        <th width="20%"><input class="checkbox" type="checkbox" name="quantityLimitedQuery"
                                               id="quantityLimitedQuery"> Quantity Limit?
                        </th>
                        <td width="30%">Quantity<br><input type="text" id="quantityLimit" name="quantityLimit"></td>
                    </tr>
                    <tr>
                        <td colspan=4>
                            <hr/>
                        </td>
                    </tr>
                    <tr>
                        <td width="20%"><span>Requesting HIC</span></td>
                        <td><select multiple="multiple" name="requestingHic" id="requestingHic">

                            <option value=""></option>
                            <%
                                for (Provider provider : allProvidersList) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(String.valueOf(provider.getProviderNo()))%>">[<%=Encode.forHtml(String.valueOf(provider.getProviderNo()))%>
                                ] <%=Encode.forHtml(String.valueOf(provider.getLastName()))%>, <%=Encode.forHtml(String.valueOf(provider.getFirstName()))%>
                            </option>
                            <%
                                }
                            %>
                        </select></td>
                    </tr>

                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    </tbody>
                </table>
            </form>


            <form action="<%=request.getContextPath() %>/olis/Search.do" method="POST">
                <input type="hidden" name="queryType" value="Z05"/>
                <table id="Z05_query" style="display: none;">
                    <tbody>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    <tr>
                        <th width="20%">Date &amp; Time Period to Search<br/>(yyyy-mm-dd)</th>
                        <td width="30%"><input style="width:150px" type="text" name="startTimePeriod"
                                               id="startTimePeriod" value=""> to <input style="width:150px"
                                                                                        name="endTimePeriod" type="text"
                                                                                        id="endTimePeriod"></td>
                        <th width="20%"><input class="checkbox" type="checkbox" name="quantityLimitedQuery"
                                               id="quantityLimitedQuery"> Quantity Limit?
                        </th>
                        <td width="30%">Quantity<br><input type="text" id="quantityLimit" name="quantityLimit"></td>
                    </tr>
                    <tr>
                        <th width="20%">Destination Laboratory</th>
                        <td width="30%">
                            <input type="text" id="destinationLaboratoryAC" autocomplete="off"
                                   placeholder="Type 2+ characters to search..." style="width: 250px;"/>
                            <input type="hidden" id="destinationLaboratoryAC_hidden" name="destinationLaboratory"/>
                            <div id="destinationLaboratoryAC_chip" class="nomenclature-chips"></div>
                        </td>
                    </tr>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    </tbody>
                </table>
            </form>


            <form action="<%=request.getContextPath() %>/olis/Search.do" method="POST">
                <input type="hidden" name="queryType" value="Z06"/>
                <table id="Z06_query" style="display: none;">
                    <tbody>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    <tr>
                        <th width="20%">Date &amp; Time Period to Search<br/>(yyyy-mm-dd)</th>
                        <td width="30%"><input style="width:150px" type="text" name="startTimePeriod"
                                               id="startTimePeriod" value=""> to <input style="width:150px"
                                                                                        name="endTimePeriod" type="text"
                                                                                        id="endTimePeriod"></td>
                        <th width="20%"><input class="checkbox" type="checkbox" name="quantityLimitedQuery"
                                               id="quantityLimitedQuery"> Quantity Limit?
                        </th>
                        <td width="30%">Quantity<br><input type="text" id="quantityLimit" name="quantityLimit"></td>
                    </tr>
                    <tr>
                        <th width="20%">Ordering Facility</th>
                        <td width="30%">
                            <input type="text" id="orderingFacilityAC" autocomplete="off"
                                   placeholder="Type 2+ characters to search..." style="width: 250px;"/>
                            <input type="hidden" id="orderingFacilityAC_hidden" name="orderingFacility"/>
                            <div id="orderingFacilityAC_chip" class="nomenclature-chips"></div>
                        </td>
                    </tr>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    </tbody>
                </table>
            </form>


            <form action="<%=request.getContextPath() %>/olis/Search.do" method="POST">
                <input type="hidden" name="queryType" value="Z07"/>
                <table id="Z07_query" style="display: none;">
                    <tbody>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    <tr>
                        <th width="20%">Date &amp; Time Period to Search<br/>(yyyy-mm-dd)</th>
                        <td width="30%"><input style="width:150px" type="text" name="startTimePeriod"
                                               id="startTimePeriod" value=""> to <input style="width:150px"
                                                                                        name="endTimePeriod" type="text"
                                                                                        id="endTimePeriod"></td>
                        <th width="20%"><input class="checkbox" type="checkbox" name="quantityLimitedQuery"
                                               id="quantityLimitedQuery"> Quantity Limit?
                        </th>
                        <td width="30%">Quantity<br><input type="text" id="quantityLimit" name="quantityLimit"></td>
                    </tr>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    </tbody>
                </table>
            </form>


            <form action="<%=request.getContextPath() %>/olis/Search.do" method="POST">
                <input type="hidden" name="queryType" value="Z08"/>
                <table id="Z08_query" style="display: none;">
                    <tbody>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    <tr>
                        <th width="20%">Date &amp; Time Period to Search<br/>(yyyy-mm-dd)</th>
                        <td width="30%"><input style="width:150px" type="text" name="startTimePeriod"
                                               id="startTimePeriod" value=""> to <input style="width:150px"
                                                                                        name="endTimePeriod" type="text"
                                                                                        id="endTimePeriod"></td>
                        <th width="20%"><input class="checkbox" type="checkbox" name="quantityLimitedQuery"
                                               id="quantityLimitedQuery"> Quantity Limit?
                        </th>
                        <td width="30%">Quantity<br><input type="text" id="quantityLimit" name="quantityLimit"></td>
                    </tr>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    </tbody>
                </table>
            </form>


            <form action="<%=request.getContextPath() %>/olis/Search.do" method="POST">
                <input type="hidden" name="queryType" value="Z50"/>
                <table id="Z50_query" style="display: none;">
                    <tbody>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    <tr>
                        <th width="20%">First Name</th>
                        <td width="30%"><input type="text" id="z50firstName" name="z50firstName"></td>
                        <th width="20%">Last Name</th>
                        <td width="30%"><input type="text" id="z50lastName" name="z50lastName"></td>
                    </tr>
                    <tr>
                        <th width="20%">Sex</th>
                        <td width="30%"><select name="z50sex">
                            <option value="M">M</option>
                            <option value="F">F</option>
                        </select></td>
                        <th width="20%">Date of Birth</th>
                        <td width="30%"><input type="text" id="z50dateOfBirth" name="z50dateOfBirth"></td>
                    </tr>
                    <tr>
                        <td colspan=2><input type="submit" name="submit" value="Search"/></td>
                    </tr>
                    </tbody>
                </table>
            </form>


            <oscar:oscarPropertiesCheck value="yes" property="olis_simulate">

                <iframe src="Simulate.jsp" width="500" heigh="300" frameborder="0" scrolling="no"></iframe>

            </oscar:oscarPropertiesCheck>

        </td>
    </tr>
    </tbody>
</table>

<script type="text/javascript">
    (function ($) {
        var ctxPath = '<%=Encode.forJavaScript(request.getContextPath())%>';
        var endpoint = ctxPath + '/olis/FacilitySearch.do';

        function clearChildren(node) {
            while (node.firstChild) node.removeChild(node.firstChild);
        }

        function makeFacilityPicker(inputId, facilityClass) {
            var $input = $('#' + inputId);
            var hiddenInput = document.getElementById(inputId + '_hidden');
            var chipDiv = document.getElementById(inputId + '_chip');
            if (!hiddenInput || !chipDiv) return;

            function showChip(licence, label) {
                clearChildren(chipDiv);
                hiddenInput.value = licence || '';
                if (!licence) {
                    $input.show();
                    return;
                }
                var chip = document.createElement('span');
                chip.className = 'nomenclature-chip';
                chip.appendChild(document.createTextNode(label + ' '));
                var remove = document.createElement('a');
                remove.href = '#';
                remove.className = 'nomenclature-chip-remove';
                remove.appendChild(document.createTextNode('×'));
                remove.onclick = function (e) {
                    e.preventDefault();
                    showChip('', '');
                    $input.val('').focus();
                    return false;
                };
                chip.appendChild(remove);
                chipDiv.appendChild(chip);
                $input.hide();
            }

            $input.autocomplete({
                minLength: 2,
                source: function (req, response) {
                    $.getJSON(endpoint, {query: req.term, 'class': facilityClass}, function (data) {
                        response($.map(data.results || [], function (item) {
                            var label = item.name;
                            if (item.addressLine1) label = label + ', ' + item.addressLine1;
                            if (item.city) label = label + ', ' + item.city;
                            label = label + ' [' + item.licence + ']';
                            return {label: label, value: label, item: item};
                        }));
                    });
                },
                select: function (event, ui) {
                    showChip(ui.item.item.licence, ui.item.label);
                    $(this).val('');
                    return false;
                },
                focus: function () { return false; }
            });

            var preloadLicence = hiddenInput.value;
            var preloadAttr = $input.attr('data-preload-label');
            if (preloadLicence) {
                showChip(preloadLicence, preloadAttr || preloadLicence);
            }
        }

        makeFacilityPicker('specimenCollectorAC', 'SCC');
        makeFacilityPicker('performingLaboratoryAC', 'LAB');
        makeFacilityPicker('excludePerformingLaboratoryAC', 'LAB');
        makeFacilityPicker('reportingLaboratoryAC', 'LAB');
        makeFacilityPicker('excludeReportingLaboratoryAC', 'LAB');
        makeFacilityPicker('testRequestPlacerAC', 'LAB');
        makeFacilityPicker('destinationLaboratoryAC', 'LAB');
        makeFacilityPicker('orderingFacilityAC', 'ANY');
    })(jQuery);
</script>

</body>
</html>
