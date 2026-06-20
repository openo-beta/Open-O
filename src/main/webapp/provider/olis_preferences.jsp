<%--

    Copyright (c) 2008-2012 Indivica Inc.

    This software is made available under the terms of the
    GNU General Public License, Version 2, 1991 (GPLv2).
    License details are available via "indivica.ca/gplv2"
    and "gnu.org/licenses/gpl-2.0.html".

--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="ca.openosp.openo.olis.dao.OLISFacilityDao" %>
<%@ page import="ca.openosp.openo.olis.model.OLISFacility" %>
<%@ page import="ca.openosp.openo.utility.SpringUtils" %>
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
        <link rel="stylesheet" href="<c:out value="${ctx}"/>/library/jquery/jquery-ui-1.12.1.min.css">
        <script src="<c:out value="${ctx}"/>/library/jquery/jquery-3.6.4.min.js"></script>
        <script src="<c:out value="${ctx}"/>/library/jquery/jquery-ui-1.12.1.min.js"></script>
        <style>
            .facility-chips { margin-top: 6px; max-width: 320px; }
            .facility-chip {
                display: inline-block; padding: 2px 6px; margin: 2px;
                background: #e0e0e0; border-radius: 3px; font-size: 12px; line-height: 1.4;
            }
            .facility-chip-remove { color: #666; text-decoration: none; margin-left: 4px; font-weight: bold; }
            .facility-chip-remove:hover { color: #000; }
            /* Scrollable autocomplete dropdown — without max-height jQuery UI
               just keeps growing the suggestion list off-screen. */
            .ui-autocomplete { max-height: 400px; overflow-y: auto; overflow-x: hidden; }
        </style>


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
                        <%
                            OLISFacilityDao facilityDao = SpringUtils.getBean(OLISFacilityDao.class);

                            String repVal = (String) request.getAttribute("reportingLaboratory");
                            if (repVal == null) repVal = "";
                            String repLabel = "";
                            if (!repVal.isEmpty()) {
                                OLISFacility f = facilityDao.findByClassAndLicence(OLISFacility.CLASS_LAB, repVal);
                                if (f != null) repLabel = f.getName() + " [" + f.getLicenceNumber() + "]";
                            }

                            String exRepVal = (String) request.getAttribute("excludeReportingLaboratory");
                            if (exRepVal == null) exRepVal = "";
                            String exRepLabel = "";
                            if (!exRepVal.isEmpty()) {
                                OLISFacility f = facilityDao.findByClassAndLicence(OLISFacility.CLASS_LAB, exRepVal);
                                if (f != null) exRepLabel = f.getName() + " [" + f.getLicenceNumber() + "]";
                            }
                        %>
                        <tr>
                            <th width="20%">Default Reporting Laboratory</th>
                            <td colspan="3">
                                <input type="text" id="reportingLaboratoryAC" autocomplete="off"
                                       placeholder="Type 2+ characters to search..." style="width: 280px;"
                                       data-preload-label="<%=Encode.forHtmlAttribute(repLabel)%>"/>
                                <input type="hidden" id="reportingLaboratoryAC_hidden" name="reportingLaboratory"
                                       value="<%=Encode.forHtmlAttribute(repVal)%>"/>
                                <div id="reportingLaboratoryAC_chip" class="facility-chips"></div>
                            </td>
                        </tr>
                        <tr>
                            <th width="20%">Default Exclude Reporting Laboratory</th>
                            <td width="30%">
                                <input type="text" id="excludeReportingLaboratoryAC" autocomplete="off"
                                       placeholder="Type 2+ characters to search..." style="width: 280px;"
                                       data-preload-label="<%=Encode.forHtmlAttribute(exRepLabel)%>"/>
                                <input type="hidden" id="excludeReportingLaboratoryAC_hidden" name="excludeReportingLaboratory"
                                       value="<%=Encode.forHtmlAttribute(exRepVal)%>"/>
                                <div id="excludeReportingLaboratoryAC_chip" class="facility-chips"></div>
                            </td>
                        </tr>

                        <tr>
                            <th width="20%">Automated Polling Frequency (in minutes)</th>
                            <td width="30%">
                                <%
                                    String val1 = (String) request.getAttribute("pollingFrequency");
                                    if (val1 == null) val1 = "";
                                %>
                                <input type="text" id="pollingFrequency" name="pollingFrequency" value="<%=Encode.forHtmlAttribute(String.valueOf(val1))%>">

                            </td>
                        </tr>

                        <tr>
                            <th width="20%">Start Time (for polling):</th>
                            <td width="30%">
                                <%
                                    String val2 = (String) request.getAttribute("olis_provider_start_time");
                                    if (val2 == null) val2 = "";
                                %>
                                <input type="text" id="providerStartTime" name="providerStartTime" value="<%=Encode.forHtmlAttribute(String.valueOf(val2))%>">
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
                                    <option value="true" <%=("true".equals(filterPatientsVal) ? "selected=\"selected\"" : "")%>>Filter to unclaimed worklist</option>
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
                    if (!licence) { $input.show(); return; }
                    var chip = document.createElement('span');
                    chip.className = 'facility-chip';
                    chip.appendChild(document.createTextNode(label + ' '));
                    var remove = document.createElement('a');
                    remove.href = '#';
                    remove.className = 'facility-chip-remove';
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

            makeFacilityPicker('reportingLaboratoryAC', 'LAB');
            makeFacilityPicker('excludeReportingLaboratoryAC', 'LAB');
        })(jQuery);
    </script>
    </body>
</html>
