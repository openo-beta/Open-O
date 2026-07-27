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
    String roleName2$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName2$%>" objectName="_form" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_form");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<%@ page import="ca.openosp.openo.util.*, ca.openosp.openo.form.*, ca.openosp.openo.form.data.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@ page import="ca.openosp.openo.form.FrmRecord" %>
<%@ page import="ca.openosp.openo.form.FrmRecordFactory" %>
<%@ page import="org.owasp.encoder.Encode" %>


<%
    String formClass = "CostQuestionnaire";
    String formLink = "formcostquestionnaire.jsp";

    int demoNo = Integer.parseInt(request.getParameter("demographic_no"));
    int formId = Integer.parseInt(request.getParameter("formId"));
    int provNo = Integer.parseInt((String) session.getAttribute("user"));
    FrmRecord rec = (new FrmRecordFactory()).factory(formClass);
    java.util.Properties props = rec.getFormRecord(LoggedInInfo.getLoggedInInfoFromSession(request), demoNo, formId);

    //FrmData fd = new FrmData();    String resource = fd.getResource(); resource = resource + "ob/riskinfo/";

    //get project_home
    String project_home = request.getContextPath().substring(1);
%>
<%
    boolean bView = false;
    if (request.getParameter("view") != null && request.getParameter("view").equals("1")) bView = true;
%>
<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <title>Cost Questionnaire</title>
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
        <link rel="stylesheet" type="text/css" media="all" href="<%= request.getContextPath() %>/share/css/extractedFromPages.css"/>
    </head>


    <script type="text/javascript" language="Javascript">
        var choiceFormat = new Array(6, 7, 10, 11, 30, 31, 34, 35, 37, 38, 42, 43);
        var allNumericField = new Array(8, 9, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 23, 25, 26, 28, 29, 32, 33, 36, 39, 40, 41, 44, 46, 48, 50);
        var allMatch = null;
        var action = "/<%=Encode.forJavaScript(String.valueOf(project_home))%>/form/formname.do";

        function backToPage1() {
            document.getElementById('page1').style.display = 'block';
            document.getElementById('page2').style.display = 'none';
            document.getElementById('page3').style.display = 'none';
        }

        function goToPage2() {
            var checkboxes = new Array(6, 7, 10, 11);
            var a = new Array(0, 9999, 23, 26, 29);
            var allInputs = new Array(a);
            var numericFields = new Array(8, 9, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 23, 25, 26, 28, 29);
            if (is1CheckboxChecked(0, checkboxes) == true && allAreNumeric(0, numericFields) == true && areInRange(0, allInputs) == true && isFormCompleted(6, 19, 2, 8) == true) {
                document.getElementById('page1').style.display = 'none';
                document.getElementById('page2').style.display = 'block';
                document.getElementById('page3').style.display = 'none';

            }
        }

        function backToPage2() {
            document.getElementById('page1').style.display = 'none';
            document.getElementById('page2').style.display = 'block';
            document.getElementById('page3').style.display = 'none';
        }

        function goToPage3() {
            var checkboxes = new Array(30, 31, 34, 35, 37, 38, 42, 43);
            var numericFields = new Array(32, 33, 36, 39, 40, 41, 44, 46, 48, 50);
            if (is1CheckboxChecked(0, checkboxes) == true && allAreNumeric(0, numericFields) == true && isFormCompleted(30, 50, 4, 0) == true) {
                document.getElementById('page1').style.display = 'none';
                document.getElementById('page2').style.display = 'none';
                document.getElementById('page3').style.display = 'block';

            }
        }

        function checkBeforeSave() {
            if (document.getElementById('page3').style.display == 'block') {
                if (isFormCompleted(51, 64, 1, 0) == true)
                    return true;
            } else {
                if (isFormCompleted(6, 19, 2, 8) == true && isFormCompleted(31, 50, 4, 0) == true && isFormCompleted(51, 64, 1, 0) == true)
                    return true;
            }

            return false;
        }

        function roundCost(index) {
            var originalNb = document.forms[0].elements[index].value;
            var newNb = Math.round(originalNb);
            document.forms[0].elements[index].value = newNb;
        }
    </script>
    <script type="text/javascript" src="formScripts.js">
    </script>


    <body bgproperties="fixed" topmargin="0" leftmargin="0" rightmargin="0"
          onload="window.resizeTo(768,768)">
    <!--
    @oscar.formDB Table="formAdf"
    @oscar.formDB Field="ID" Type="int(10)" Null="NOT NULL" Key="PRI" Default="" Extra="auto_increment"
    @oscar.formDB Field="demographic_no" Type="int(10)" Null="NOT NULL" Default="'0'"
    @oscar.formDB Field="provider_no" Type="int(10)" Null="" Default="NULL"
    @oscar.formDB Field="formCreated" Type="date" Null="" Default="NULL"
    @oscar.formDB Field="formEdited" Type="timestamp"
    -->
    <form action="${pageContext.request.contextPath}/form/formname.do" method="post">

    <input type="hidden" name="demographic_no"
               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("demographic_no", "0")))%>"/>
        <input type="hidden" name="formCreated"
               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("formCreated", "")))%>"/>
        <input type="hidden" name="form_class" value="<%=formClass%>"/>
        <input type="hidden" name="form_link" value="<%=formLink%>"/>
        <input type="hidden" name="formId" value="<%=Encode.forHtmlAttribute(String.valueOf(formId))%>"/>
        <input type="hidden" name="submit" value="exit"/>

        <table border="0" cellspacing="0" cellpadding="0" width="740px"
               height="95%">
            <tr>
                <td>
                    <table border="0" cellspacing="0" cellpadding="0" width="740px"
                           height="10%">
                        <tr>
                            <th class="subject">Cost Questionnaire</th>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td valign="top">
                    <table border="0" cellspacing="0" cellpadding="0" height="85%"
                           width="740px" id="page1">
                        <tr>
                            <td colspan="2">
                                <table width="740px" height="615px" border="0" cellspacing="0"
                                       cellpadding="0">
                                    <tr class="title">
                                        <th colspan="7">Health and social service provider visits</th>
                                    </tr>
                                    <tr>
                                        <td colspan="7">Have you seen a doctor in the last 9 months?
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td width="5%"></td>
                                        <td width="10%">Yes <input type="checkbox" class="checkbox"
                                                                   name="seenDoctorY" <%=Encode.forHtml(String.valueOf(props.getProperty("seenDoctorY", "")))%> />
                                        </td>
                                        <td width="10%">No <input type="checkbox" class="checkbox"
                                                                  name="seenDoctorN" <%=Encode.forHtml(String.valueOf(props.getProperty("seenDoctorN", "")))%> />
                                        </td>
                                        <td width="10%">If <font style="font-weight: bold">YES</font>:</td>
                                        <td width="25%">Family Physician</td>
                                        <td width="20%">Number of Visits</td>
                                        <td width="20%"><input type="text" name="familyPhyVisits"
                                                               size="5" class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("familyPhyVisits", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="4"></td>
                                        <td width="25%">Specialist</td>
                                        <td width="20%">Number of Visits</td>
                                        <td width="20%"><input type="text" name="specialistVisits"
                                                               size="5" class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("specialistVisits", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="7">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td colspan="7">Have you seen any other Health and Social
                                            Service providers in the last months?
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td></td>
                                        <td width="10%">Yes <input type="checkbox" class="checkbox"
                                                                   name="otherProviderY"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("otherProviderY", "")))%> /></td>
                                        <td width="10%">No <input type="checkbox" class="checkbox"
                                                                  name="otherProviderN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("otherProviderN", "")))%> /></td>
                                        <td width="10%">If <font style="font-weight: bold">YES</font>:</td>
                                        <td colspan="3"></td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="4"></td>
                                        <td colspan="3">
                                            <table width="100%">
                                                <tr>
                                                    <td width="50%" align="left"><font
                                                            style="font-weight: bold; text-decoration: underline">Service
                                                        provider visited</font></td>
                                                    <td width="40%" align="center"><font
                                                            style="font-weight: bold; text-decoration: underline">Number
                                                        of visits</font></td>
                                                </tr>
                                                <tr>
                                                    <td>Visiting Nurse (VON, Para med)</td>
                                                    <td align="center"><input type="text" name="visitNurse"
                                                                              size="5" class="textbox"
                                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("visitNurse", "")))%>"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>Home maker (Home care)</td>
                                                    <td align="center"><input type="text" name="homeMaker"
                                                                              size="5" class="textbox"
                                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("homeMaker", "")))%>"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>Physiotherapist</td>
                                                    <td align="center"><input type="text"
                                                                              name="physiotherapist" size="5"
                                                                              class="textbox"
                                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("physiotherapist", "")))%>"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>Occupational therapist</td>
                                                    <td align="center"><input type="text" name="therapist"
                                                                              size="5" class="textbox"
                                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("therapist", "")))%>"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>Psychologist</td>
                                                    <td align="center"><input type="text" name="psychologist"
                                                                              size="5" class="textbox"
                                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("psychologist", "")))%>"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>Social worker</td>
                                                    <td align="center"><input type="text" name="socialWorker"
                                                                              size="5" class="textbox"
                                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("socialWorker", "")))%>"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>Support Group</td>
                                                    <td align="center"><input type="text" name="supportGroup"
                                                                              size="5" class="textbox"
                                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("supportGroup", "")))%>"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>Meal on Wheels</td>
                                                    <td align="center"><input type="text" name="mealOnWheels"
                                                                              size="5" class="textbox"
                                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("mealOnWheels", "")))%>"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>Other</td>
                                                    <td align="center"><input type="text" name="other"
                                                                              size="5" class="textbox"
                                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("other", "")))%>"/>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="7">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td colspan="7">Have you used any paid services in the last
                                            <font style="font-weight: bold"> months</font>? (e.g. cleaning,
                                            transportation)
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td></td>
                                        <td colspan="6">
                                            <table width="100%">
                                                <tr>
                                                    <td width="40%"><input type="text" name="paidService1"
                                                                           size="40" class="textbox"
                                                                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("paidService1", "")))%>"/>
                                                    </td>
                                                    <td width="25%"># of hours <input type="text"
                                                                                      name="paidServiceHour1" size="5"
                                                                                      class="textbox"
                                                                                      value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("paidServiceHour1", "")))%>"/>
                                                    </td>
                                                    <td width="35%">Cost $<input type="text"
                                                                                 onchange="javascript:roundCost('23')"
                                                                                 name="paidServiceCost1"
                                                                                 size="20" class="textbox"
                                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("paidServiceCost1", "")))%>"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><input type="text" name="paidService2" size="40"
                                                               class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("paidService2", "")))%>"/>
                                                    </td>
                                                    <td># of hours <input type="text" name="paidServiceHour2"
                                                                          size="5" class="textbox"
                                                                          value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("paidServiceHour2", "")))%>"/>
                                                    </td>
                                                    <td>Cost $<input type="text"
                                                                     onchange="javascript:roundCost('26')"
                                                                     name="paidServiceCost2"
                                                                     size="20" class="textbox"
                                                                     value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("paidServiceCost2", "")))%>"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><input type="text" name="paidService3" size="40"
                                                               class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("paidService3", "")))%>"/>
                                                    </td>
                                                    <td># of hours <input type="text" name="paidServiceHour3"
                                                                          size="5" class="textbox"
                                                                          value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("paidServiceHour3", "")))%>"/>
                                                    </td>
                                                    <td>Cost $<input type="text"
                                                                     onchange="javascript:roundCost('29')"
                                                                     name="paidServiceCost3"
                                                                     size="20" class="textbox"
                                                                     value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("paidServiceCost3", "")))%>"/>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    <tr>
                                        <td colspan="7">
                                            <table height="30">
                                                <tr>
                                                    <td>&nbsp;</td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr class="subject">
                            <td></td>
                            <td align="right"><a href="javascript: goToPage2();">Next
                                Page >></a></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td valign="top">
                    <table border="0" cellspacing="0" cellpadding="0"
                           style="display: none" width="740px" height="85%" id="page2">
                        <tr>
                            <td colspan="2">
                                <table width="740px" height="615px" border="0" cellspacing="0"
                                       cellpadding="0">
                                    <tr class="title">
                                        <th colspan="6">Use of hospitals and emergency services</th>
                                    </tr>
                                    <tr>
                                        <td colspan="6" class="subtitle">Planned Hospitalizations</td>
                                    </tr>
                                    <tr>
                                        <td colspan="6">In the last <font style="font-weight: bold">9
                                            months</font>, did you ever spend one or more nights in the hospital due
                                            to a <font style="font-weight: bold">planned
                                                hospitalization</font> (e.g. hernia operation, knee surgery)
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td width="5%"></td>
                                        <td width="10%">No <input type="checkbox" class="checkbox"
                                                                  name="plannedHospN" <%=Encode.forHtml(String.valueOf(props.getProperty("plannedHospN", "")))%> />
                                        </td>
                                        <td width="10%">Yes <input type="checkbox" class="checkbox"
                                                                   name="plannedHospY" <%=Encode.forHtml(String.valueOf(props.getProperty("plannedHospY", "")))%> />
                                        </td>
                                        <td width="10%">If Yes</td>
                                        <td width="45%">How many times were you admitted?</td>
                                        <td width="25%"><input type="text"
                                                               name="plannedHospAdmitted" size="5" class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("plannedHospAdmitted", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="4"></td>
                                        <td width="45%">Total number of days in the hospital?</td>
                                        <td width="25%"><input type="text" name="plannedHospDays"
                                                               size="5" class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("plannedHospDays", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="6">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td colspan="6" class="subtitle">Nursing Home</td>
                                    </tr>
                                    <tr>
                                        <td colspan="6">In the last <font style="font-weight: bold">9
                                            months</font>, did you ever spend 1 or more nights in a nursing home?
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td width="5%"></td>
                                        <td width="10%">No <input type="checkbox" class="checkbox"
                                                                  name="nursingHomeN" <%=Encode.forHtml(String.valueOf(props.getProperty("nursingHomeN", "")))%> />
                                        </td>
                                        <td width="10%">Yes <input type="checkbox" class="checkbox"
                                                                   name="nursingHomeY" <%=Encode.forHtml(String.valueOf(props.getProperty("nursingHomeY", "")))%> />
                                        </td>
                                        <td width="10%">If Yes</td>
                                        <td width="45%">Total number of days?</td>
                                        <td width="25%"><input type="text" name="nursingHomeDays"
                                                               size="5" class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("nursingHomeDays", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="6">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td colspan="6" class="subtitle">Emergency Services</td>
                                    </tr>
                                    <tr>
                                        <td colspan="6">In the last <font style="font-weight: bold">9
                                            months</font>, did you ever have to go to a hospital emergency room for
                                            treatment?
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td width="5%"></td>
                                        <td width="10%">No <input type="checkbox" class="checkbox"
                                                                  name="emergencyN" <%=Encode.forHtml(String.valueOf(props.getProperty("emergencyN", "")))%> />
                                        </td>
                                        <td width="10%">Yes <input type="checkbox" class="checkbox"
                                                                   name="emergencyY" <%=Encode.forHtml(String.valueOf(props.getProperty("emergencyY", "")))%> />
                                        </td>
                                        <td width="10%">If Yes</td>
                                        <td width="45%">How many times did you call 911?</td>
                                        <td width="25%"><input type="text" name="emergency911"
                                                               size="5" class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("emergency911", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="4"></td>
                                        <td width="45%">How many times did you go to Emergency?</td>
                                        <td width="25%"><input type="text" name="emergency" size="5"
                                                               class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("emergency", "")))%>"/></td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="4"></td>
                                        <td width="45%">How many times did you need to go by
                                            ambulance?
                                        </td>
                                        <td width="25%"><input type="text" name="emergencyAmbulance"
                                                               size="5" class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("emergencyAmbulance", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="6">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td colspan="6" class="subtitle">Walk-In Clinics</td>
                                    </tr>
                                    <tr>
                                        <td colspan="6">In the last <font style="font-weight: bold">9
                                            months</font>, did you ever have to go to a walk-in clinic for
                                            treatment?
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td width="5%"></td>
                                        <td width="10%">No <input type="checkbox" class="checkbox"
                                                                  name="walkinN" <%=Encode.forHtml(String.valueOf(props.getProperty("walkinN", "")))%> />
                                        </td>
                                        <td width="10%">Yes <input type="checkbox" class="checkbox"
                                                                   name="walkinY" <%=Encode.forHtml(String.valueOf(props.getProperty("walkinY", "")))%> />
                                        </td>
                                        <td width="10%">If Yes</td>
                                        <td width="45%">Total number of days?</td>
                                        <td width="25%"><input type="text" name="walkin" size="5"
                                                               class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("walkin", "")))%>"/></td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td colspan="6">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td colspan="6" class="subtitle">Specialty Items Purchased</td>
                                    </tr>
                                    <tr>
                                        <td colspan="6">Have you purchased any supplies, aides or
                                            special devices to help you with your daily activities in the <font
                                                    style="font-weight: bold">last 9 months</font>? (e.g.
                                            wheelchair, walker)
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td></td>
                                        <td colspan="5">
                                            <table width="100%">
                                                <tr>
                                                    <td width="35%"><font
                                                            style="font-weight: bold; text-decoration: underline">Item
                                                        Purchased</font></td>
                                                    <td width="40%"><font
                                                            style="font-weight: bold; text-decoration: underline">Cost</font>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><input type="text" name="itemPurchased1" size="40"
                                                               class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("itemPurchased1", "")))%>"/>
                                                    </td>
                                                    <td>$<input type="text" name="itemCost1" size="10"
                                                                class="textbox"
                                                                value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("itemCost1", "")))%>"/></td>
                                                </tr>
                                                <tr>
                                                    <td><input type="text" name="itemPurchased2" size="40"
                                                               class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("itemPurchased2", "")))%>"/>
                                                    </td>
                                                    <td>$<input type="text" name="itemCost2" size="10"
                                                                class="textbox"
                                                                value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("itemCost2", "")))%>"/></td>
                                                </tr>
                                                <tr>
                                                    <td><input type="text" name="itemPurchased3" size="40"
                                                               class="textbox"
                                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("itemPurchased3", "")))%>"/>
                                                    </td>
                                                    <td>$<input type="text" name="itemCost3" size="10"
                                                                class="textbox"
                                                                value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("itemCost3", "")))%>"/></td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr class="subject">
                            <td align="left"><a href="javascript: backToPage1();"><<
                                Previous Page</a></td>
                            <td align="right"><a href="javascript: goToPage3();">Next
                                Page >></a></td>
                        </tr>
                    </table>
            <tr>
                <td valign="top">
                    <table border="0" cellspacing="0" cellpadding="0"
                           style="display: none" width="740px" height="85%" id="page3">
                        <tr>
                            <td colspan="2">
                                <table width="740px" height="615px" border="0" cellspacing="0"
                                       cellpadding="0">
                                    <tr class="title">
                                        <th colspan="2">Employment Information</th>
                                    </tr>
                                    <tr>
                                        <td colspan="2">Please provide your <font
                                                style="font-weight: bold">current</font> employment status by
                                            checking <font style="font-weight: bold">all</font> of the
                                            options that apply to you.
                                        </td>
                                    </tr>
                                    <tr bgcolor="white">
                                        <td width="5%"></td>
                                        <td>
                                            <table width="80%" class="smallTable" border="0" cellspacing="0"
                                                   cellpadding="0">
                                                <tr>
                                                    <td width="30%" class="row"><input type="checkbox"
                                                                                       class="checkbox" name="employed"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("employed", "")))%> /> Employed:
                                                    </td>
                                                    <td width="20%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="employedFullTime"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("employedFullTime", "")))%> /> Full
                                                        Time
                                                    </td>
                                                    <td width="20%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="employedPartTime"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("employedPartTime", "")))%> /> Part
                                                        Time
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td width="30%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="selfEmployed"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("selfEmployed", "")))%> />
                                                        Self-employed:
                                                    </td>
                                                    <td width="20%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="selfEmployedFullTime"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("selfEmployedFullTime", "")))%> /> Full
                                                        Time
                                                    </td>
                                                    <td width="20%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="selfEmployedPartTime"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("selfEmployedPartTime", "")))%> /> Part
                                                        Time
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td width="30%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="unemployed"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("unemployed", "")))%> /> Unemployed:
                                                    </td>
                                                    <td width="20%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="unemployedAble"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("unemployedAble", "")))%> /> Able to
                                                        work
                                                    </td>
                                                    <td width="20%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="unemployedUnable"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("unemployedUnable", "")))%> /> Unable
                                                        to
                                                        work
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td width="30%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="disability"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("disability", "")))%> /> On Disability:
                                                    </td>
                                                    <td width="20%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="disabilityShortTerm"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("disabilityShortTerm", "")))%> /> Short
                                                        Term
                                                    </td>
                                                    <td width="20%" class="row"><input type="checkbox"
                                                                                       class="checkbox"
                                                                                       name="disabilityLongTerm"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("disabilityLongTerm", "")))%> /> Long
                                                        Term
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td width="30%" class="row"><input type="checkbox"
                                                                                       class="checkbox" name="retired"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("retired", "")))%> /> Retired
                                                    </td>
                                                    <td colspan="2" class="row">&nbsp;</td>
                                                </tr>
                                                <tr>
                                                    <td width="30%"><input type="checkbox" class="checkbox"
                                                                           name="homemakerWithOutPaid"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("homemakerWithOutPaid", "")))%> />
                                                        Homemaker (without pay)
                                                    </td>
                                                    <td colspan="2"></td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <table height="300">
                                                <tr>
                                                    <td></td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr class="subject">
                            <td align="left"><a href="javascript: backToPage2();"><<
                                Previous Page</a></td>
                            <td align="right"></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td valign="top">
                    <table class="Head" class="hidePrint" height="5%">
                        <tr>
                            <td align="left">
                                <%
                                    if (!bView) {
                                %> <input type="submit" value="Save"
                                          onclick="javascript: return onSave();"/> <input type="submit"
                                                                                          value="Save and Exit"
                                                                                          onclick="javascript:if(checkBeforeSave()==true) return onSaveExit(); else return false;"/>
                                <%
                                    }
                                %> <input type="button" value="Exit"
                                          onclick="javascript:return onExit();"/> <input type="button"
                                                                                         value="Print"
                                                                                         onclick="javascript:window.print();"/>
                            </td>
                            <td align="right">Study ID: <%=Encode.forHtml(String.valueOf(props.getProperty("studyID", "N/A")))%>
                                <input type="hidden" name="studyID"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("studyID", "N/A")))%>"/></td>
                        </tr>
                        <tr>
                            <td><font style="font-size: 70%">Adapted from SLU cost
                                questionnaire. May 27, 2002</font></td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </form>
    </body>
</html>
