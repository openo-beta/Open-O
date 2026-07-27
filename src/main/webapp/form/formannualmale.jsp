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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<%@ page import="ca.openosp.openo.form.*" %>
<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@ page import="ca.openosp.openo.form.FrmRecord" %>
<%@ page import="ca.openosp.openo.form.FrmRecordFactory" %>
<%@ page import="org.owasp.encoder.Encode" %>
<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.title"/></title>
        <link rel="stylesheet" type="text/css" href="annualStyle.css">
        <link rel="stylesheet" type="text/css" media="print" href="print.css">
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
        </style>
    </head>

    <script type="text/javascript" language="Javascript">
        function onPrint() {
            var ret = checkAllDates();
            if (ret == true) {
                window.print();
            }
            return ret;
        }

        function onSave() {
            document.forms[0].submit.value = "save";
            var ret = checkAllDates();
            if (ret == true) {
                ret = confirm("<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgWannaSave"/>");
            }
            return ret;
        }

        function onSaveExit() {
            document.forms[0].submit.value = "exit";
            var ret = checkAllDates();
            if (ret == true) {
                ret = confirm("<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgSaveExit"/>");
            }
            return ret;
        }

        /**
         * DHTML date validation script. Courtesy of SmartWebby.com (http://www.smartwebby.com/dhtml/)
         */
// Declaring valid date character, minimum year and maximum year
        var dtCh = "/";
        var minYear = 1900;
        var maxYear = 3100;

        function isInteger(s) {
            var i;
            for (i = 0; i < s.length; i++) {
                // Check that current character is number.
                var c = s.charAt(i);
                if (((c < "0") || (c > "9"))) return false;
            }
            // All characters are numbers.
            return true;
        }

        function stripCharsInBag(s, bag) {
            var i;
            var returnString = "";
            // Search through string's characters one by one.
            // If character is not in bag, append to returnString.
            for (i = 0; i < s.length; i++) {
                var c = s.charAt(i);
                if (bag.indexOf(c) == -1) returnString += c;
            }
            return returnString;
        }

        function daysInFebruary(year) {
            // February has 29 days in any year evenly divisible by four,
            // EXCEPT for centurial years which are not also divisible by 400.
            return (((year % 4 == 0) && ((!(year % 100 == 0)) || (year % 400 == 0))) ? 29 : 28);
        }

        function DaysArray(n) {
            for (var i = 1; i <= n; i++) {
                this[i] = 31
                if (i == 4 || i == 6 || i == 9 || i == 11) {
                    this[i] = 30
                }
                if (i == 2) {
                    this[i] = 29
                }
            }
            return this
        }

        function isDate(dtStr) {
            var daysInMonth = DaysArray(12)
            var pos1 = dtStr.indexOf(dtCh)
            var pos2 = dtStr.indexOf(dtCh, pos1 + 1)
            var strMonth = dtStr.substring(0, pos1)
            var strDay = dtStr.substring(pos1 + 1, pos2)
            var strYear = dtStr.substring(pos2 + 1)
            strYr = strYear
            if (strDay.charAt(0) == "0" && strDay.length > 1) strDay = strDay.substring(1)
            if (strMonth.charAt(0) == "0" && strMonth.length > 1) strMonth = strMonth.substring(1)
            for (var i = 1; i <= 3; i++) {
                if (strYr.charAt(0) == "0" && strYr.length > 1) strYr = strYr.substring(1)
            }
            month = parseInt(strMonth)
            day = parseInt(strDay)
            year = parseInt(strYr)
            if (pos1 == -1 || pos2 == -1) {
                return "format"
            }
            if (month < 1 || month > 12) {
                return "month"
            }
            if (day < 1 || day > 31 || (month == 2 && day > daysInFebruary(year)) || day > daysInMonth[month]) {
                return "day"
            }
            if (strYear.length != 4 || year == 0 || year < minYear || year > maxYear) {
                return "year"
            }
            if (dtStr.indexOf(dtCh, pos2 + 1) != -1 || isInteger(stripCharsInBag(dtStr, dtCh)) == false) {
                return "date"
            }
            return true
        }


        function checkTypeIn(obj) {
            if (!checkTypeNum(obj.value)) {
                alert("<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgTypeANumber"/>");
            }
        }

        function valDate(dateBox) {
            try {
                var dateString = dateBox.value;
                if (dateString == "") {
                    //            alert('dateString'+dateString);
                    return true;
                }
                var dt = dateString.split('/');
                var y = dt[0];
                var m = dt[1];
                var d = dt[2];
                var orderString = m + '/' + d + '/' + y;
                var pass = isDate(orderString);

                if (pass != true) {
                    alert('Invalid ' + pass + ' in field ' + dateBox.name);
                    dateBox.focus();
                    return false;
                }
            } catch (ex) {
                alert('Catch Invalid Date in field ' + dateBox.name);
                dateBox.focus();
                return false;
            }
            return true;
        }

        function checkAllDates() {
            var b = true;
            if (valDate(document.forms[0].formDate) == false) {
                b = false;
            }

            return b;

        }

        function popupPage(vheight, vwidth, varpage) { //open a new popup window
            var page = "" + varpage;
            windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,top=5,left=5";//360,680
            var popup = window.open(page, "aplan", windowprops);
        }


    </script>


    <%
        String formClass = "Annual";
        String formLink = "formannualmale.jsp";

        int demoNo = Integer.parseInt(request.getParameter("demographic_no"));
        int formId = Integer.parseInt(request.getParameter("formId"));
        int provNo = Integer.parseInt((String) session.getAttribute("user"));
        FrmRecord rec = (new FrmRecordFactory()).factory(formClass);
        java.util.Properties props = rec.getFormRecord(LoggedInInfo.getLoggedInInfoFromSession(request), demoNo, formId);


    %>

    <BODY bgproperties="fixed" onLoad="javascript:window.focus()"
          topmargin="0" leftmargin="0" rightmargin="0">
    <form action="${pageContext.request.contextPath}/form/formname.do" method="post">


        <input type="hidden" name="demographic_no"
               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("demographic_no", "0")))%>"/>
        <input type="hidden" name="ID"
               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ID", "0")))%>"/>
        <input type="hidden" name="provider_no"
               value="<%=Encode.forHtml(request.getParameter("provNo"))%>"/>
        <input type="hidden" name="formCreated"
               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("formCreated", "")))%>"/>
        <input type="hidden" name="form_class" value="<%=formClass%>"/>
        <input type="hidden" name="form_link" value="<%=formLink%>"/>
        <input type="hidden" name="provNo"
               value="<%= Encode.forHtmlAttribute(request.getParameter("provNo")) %>"/>
        <input type="hidden" name="submit" value="exit"/>

        <table class="Head" class="hidePrint">
            <tr>
                <td align="left"><input type="submit"
                                        value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnSave"/>"
                                        onclick="javascript:return onSave();"/> <input type="submit"
                                                                                       value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnSaveExit"/>"
                                                                                       onclick="javascript:return onSaveExit();"/>
                    <input type="submit"
                           value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnExit"/>"
                           onclick="javascript:return onExit();"/> <input type="button"
                                                                          value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnPrint"/>"
                                                                          onclick="javascript:return onPrint();"/></td>
                <td align='right'><a
                        href="javascript: popupPage(700,950,'<%= request.getContextPath() %>/decision/annualreview/annualreviewplanner.jsp?demographic_no=<%=Encode.forUriComponent(String.valueOf(demoNo))%>&formId=<%=Encode.forUriComponent(String.valueOf(formId))%>&provNo=<%=Encode.forUriComponent(String.valueOf(provNo))%>');"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnAnnualReview"/></a></td>
            </tr>
        </table>

        <table>
            <tr>
                <td>
                    <table cellspacing="3" cellpadding="0" width="100%">
                        <tr>
                            <td><big><i><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgAnnualMaleReview"/></b></i></big></td>
                            <td><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formName"/>:</b> <input
                                    type="text" class="Input" name="pName" readonly="true" size="30"
                                    value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("pName", "")))%>"/></td>
                            <td><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formAge"/>:</b> <input type="text"
                                                                                              class="Input"
                                                                                              readonly="true" name="age"
                                                                                              size="11"
                                                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("age", "")))%>"
                                                                                              readonly="true"/></td>
                            <td><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formDate"/></b><small>(yyyy/mm/dd)</small>:
                                <input type="text" class="Input" name="formDate" size="11"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("formDate", "")))%>"/></td>
                        </tr>
                    </table>
                    <table width="100%">
                        <tr>
                            <td rowspan="4">
                                <table class="DashedBorder" width="100%">
                                    <tr>
                                        <td><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgCurrentConcerns"/>:</b></td>
                                    </tr>
                                    <tr>
                                        <td><textarea style="height: 480px; width: 400px;"
                                                      name="currentConcerns"><%=Encode.forHtml(String.valueOf(props.getProperty("currentConcerns", "")))%></textarea>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td align="center"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgSeeChart"/>
                                            &nbsp;&nbsp;&nbsp; <input type="checkbox"
                                                                      name="currentConcernsNo"
                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("currentConcernsNo", "")))%> />
                                            &nbsp;<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnNo"/>&nbsp;&nbsp;&nbsp; <input
                                                    type="checkbox" name="currentConcernsYes"
                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("currentConcernsYes", "")))%> />
                                            &nbsp;<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnYes"/></td>
                                    </tr>
                                </table>
                            </td>
                            <td>
                                <table width="100%">
                                    <tr>
                                        <td colspan="3"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgSystemsReview"/>:</b></td>
                                        <td>&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formN"/></b></td>
                                        <td colspan="2"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formAbN"/></b></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="headN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("headN", "")))%> /></td>
                                        <td><input type="checkbox" name="headAbN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("headAbN", "")))%> /></td>
                                        <td align="left" nowrap="true"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formHeadNeck"/>:
                                        </td>
                                        <td align="right"><input type="text" name="head"
                                                                 class="SystemsReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("head", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="respN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("respN", "")))%> /></td>
                                        <td><input type="checkbox" name="respAbN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("respAbN", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formResp"/>:
                                        </td>
                                        <td align="right"><input type="text" name="resp"
                                                                 class="SystemsReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("resp", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="cardioN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("cardioN", "")))%> /></td>
                                        <td><input type="checkbox" name="cardioAbN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("cardioAbN", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formCardio"/>:
                                        </td>
                                        <td align="right"><input type="text" name="cardio"
                                                                 class="SystemsReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cardio", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="giN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("giN", "")))%> /></td>
                                        <td><input type="checkbox" name="giAbN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("giAbN", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formGI"/>:</td>
                                        <td align="right"><input type="text" name="gi"
                                                                 class="SystemsReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("gi", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="guN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("guN", "")))%> /></td>
                                        <td><input type="checkbox" name="guAbN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("guAbN", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formGU"/>:</td>
                                        <td align="right"><input type="text" name="gu"
                                                                 class="SystemsReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("gu", "")))%>"/></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <table width="100%">
                                    <tr>
                                        <td><input type="checkbox" name="skinN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("skinN", "")))%> /></td>
                                        <td><input type="checkbox" name="skinAbN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("skinAbN", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formSkin"/>:
                                        </td>
                                        <td colspan="3" align="right"><input type="text" name="skin"
                                                                             class="SystemsReview"
                                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("skin", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="mskN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("mskN", "")))%> /></td>
                                        <td><input type="checkbox" name="mskAbN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("mskAbN", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formMSK"/>:
                                        </td>
                                        <td colspan="3" align="right"><input type="text" name="msk"
                                                                             class="SystemsReview"
                                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("msk", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="endocrinN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("endocrinN", "")))%> /></td>
                                        <td><input type="checkbox" name="endocrinAbN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("endocrinAbN", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formEndocrin"/>:
                                        </td>
                                        <td colspan="3" align="right"><input type="text"
                                                                             name="endocrin" class="SystemsReview"
                                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("endocrin", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td valign="top"><input type="checkbox" name="otherN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("otherN", "")))%> /></td>
                                        <td valign="top"><input type="checkbox" name="otherAbN"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("otherAbN", "")))%> /></td>
                                        <td valign="top"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formOther"/>:
                                        </td>
                                        <td colspan="3" align="right"><textarea name="other"
                                                                                class="SystemsReview"
                                                                                style="height: 50px;"><%=Encode.forHtml(String.valueOf(props.getProperty("other", "")))%></textarea>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>&nbsp;</td>
                                        <td>&nbsp;</td>
                                        <td colspan="4"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formReview"/>:
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>&nbsp;</td>
                                        <td><input type="checkbox" name="drugs"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("drugs", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formDrugs"/></td>
                                        <td style="width: 130px;">&nbsp;</td>
                                        <td align="right"><input type="checkbox" name="medSheet"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("medSheet", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formMedSheet"/></td>
                                    </tr>
                                    <tr>
                                        <td>&nbsp;</td>
                                        <td><input type="checkbox" name="allergies"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("allergies", "")))%> /></td>
                                        <td colspan="2" nowrap="true"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formAllergies"/></td>
                                        <td align="right"><input type="checkbox" name="frontSheet1"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("frontSheet1", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formFrontSheet"/></td>
                                    </tr>
                                    <tr>
                                        <td>&nbsp;</td>
                                        <td><input type="checkbox" name="familyHistory"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("familyHistory", "")))%> /></td>
                                        <td colspan="2"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formFamilyHist"/></td>
                                        <td align="right"><input type="checkbox" name="frontSheet2"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("frontSheet2", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formFrontSheet"/></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                    <table width="100%">
                        <tr>
                            <td>
                                <table class="DashedBorder">
                                    <tr>
                                        <td colspan="3" nowrap="true"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgLifestyleReview"/>:</b></td>
                                        <td><b><i><small>("<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgAnyConcerns"/>")</small></i></b>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formNo"/></td>
                                        <td colspan="2"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formYes"/></td>
                                        <td>&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="smokingNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("smokingNo", "")))%> /></td>
                                        <td><input type="checkbox" name="smokingYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("smokingYes", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formSmoking"/>:
                                        </td>
                                        <td align="right"><input type="text" name="smoking"
                                                                 class="LifestyleReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("smoking", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="alcoholNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("alcoholNo", "")))%> /></td>
                                        <td><input type="checkbox" name="alcoholYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("alcoholYes", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formAlcohol"/>:
                                        </td>
                                        <td align="right"><input type="text" name="alcohol"
                                                                 class="LifestyleReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("alcohol", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="otcNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("otcNo", "")))%> /></td>
                                        <td><input type="checkbox" name="otcYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("otcYes", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formIllicitDrugs"/>:
                                        </td>
                                        <td align="right"><input type="text" name="otc"
                                                                 class="LifestyleReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("otc", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="exerciseNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("exerciseNo", "")))%> /></td>
                                        <td><input type="checkbox" name="exerciseYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("exerciseYes", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formExcercise"/></td>
                                        <td align="right"><input type="text" name="exercise"
                                                                 class="LifestyleReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("exercise", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="nutritionNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("nutritionNo", "")))%> /></td>
                                        <td><input type="checkbox" name="nutritionYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("nutritionYes", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formNitrition"/>:
                                        </td>
                                        <td align="right"><input type="text" name="nutrition"
                                                                 class="LifestyleReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("nutrition", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="dentalNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("dentalNo", "")))%> /></td>
                                        <td><input type="checkbox" name="dentalYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("dentalYes", "")))%> /></td>
                                        <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formDentalHygiene"/>:
                                        </td>
                                        <td align="right"><input type="text" name="dental"
                                                                 class="LifestyleReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("dental", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td valign="top"><input type="checkbox"
                                                                name="relationshipNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("relationshipNo", "")))%> /></td>
                                        <td valign="top"><input type="checkbox"
                                                                name="relationshipYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("relationshipYes", "")))%> /></td>
                                        <td valign="top"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formRelationship"/>:
                                        </td>
                                        <td align="right"><textarea name="relationship"
                                                                    class="LifestyleReview"
                                                                    rows="2"><%=Encode.forHtml(String.valueOf(props.getProperty("relationship", "")))%></textarea>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="sexualityNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("sexualityNo", "")))%> /></td>
                                        <td><input type="checkbox" name="sexualityYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("sexualityYes", "")))%> /></td>
                                        <td nowrap="true"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formSexualityRisks"/>:
                                        </td>
                                        <td align="right"><input type="text" name="sexuality"
                                                                 class="LifestyleReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("sexuality", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="occupationalNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("occupationalNo", "")))%> /></td>
                                        <td><input type="checkbox" name="occupationalYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("occupationalYes", "")))%> /></td>
                                        <td nowrap="true"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formOccupationalRisks"/>:
                                        </td>
                                        <td align="right"><input type="text" name="occupational"
                                                                 class="LifestyleReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("occupational", "")))%>"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="drivingNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("drivingNo", "")))%> /></td>
                                        <td><input type="checkbox" name="drivingYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("drivingYes", "")))%> /></td>
                                        <td nowrap="true"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formDrivingSafety"/>:
                                        </td>
                                        <td align="right"><input type="text" name="driving"
                                                                 class="LifestyleReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("driving", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" name="travelNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("travelNo", "")))%> /></td>
                                        <td><input type="checkbox" name="travelYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("travelYes", "")))%> /></td>
                                        <td nowrap="true"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formForeignTravel"/>:
                                        </td>
                                        <td align="right"><input type="text" name="travel"
                                                                 class="LifestyleReview"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("travel", "")))%>"/></td>
                                    </tr>
                                    <tr>
                                        <td valign="top"><input type="checkbox" name="otherNo"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("otherNo", "")))%> /></td>
                                        <td valign="top"><input type="checkbox" name="otherYes"
                                                <%=Encode.forHtml(String.valueOf(props.getProperty("otherYes", "")))%> /></td>
                                        <td nowrap="true" valign="top"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formOther"/>:
                                        </td>
                                        <td rowspan="3" align="right"><textarea
                                                name="otherLifestyle" class="LifestyleReview"
                                                rows="6"><%=Encode.forHtml(String.valueOf(props.getProperty("otherLifestyle", "")))%></textarea></td>
                                    </tr>
                                </table>
                            </td>
                            <td width="100%" valign="top" class="DashedBorder">
                                <table width="100%">
                                    <tr>
                                        <td width="50%" colspan="2"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgScreeningReview"/>:</b></td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td><a
                                                href="javascript: popupPage(700,950,'<%= request.getContextPath() %>/decision/annualreview/annualreviewplanner.jsp?demographic_no=<%=Encode.forUriComponent(String.valueOf(demoNo))%>&formId=<%=Encode.forUriComponent(String.valueOf(formId))%>&provN
o=<%=Encode.forHtml(String.valueOf(provNo))%>');"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnRisk"/></a></td>
                                    </tr>
                                    <!--tr>
                    <td><input type="checkbox" name="rectal" <%=Encode.forHtml(String.valueOf(props.getProperty("rectal", "")))%> /></td>
                    <td>Rectal Exam
                </tr>
                <tr>
                    <td><input type="checkbox" name="maleCardiac" <%=Encode.forHtml(String.valueOf(props.getProperty("maleCardiac", "")))%> /></td>
                    <td>Cardiac Risk Factors
                </tr>
                <tr>
                    <td><input type="checkbox" name="maleImmunization" <%=Encode.forHtml(String.valueOf(props.getProperty("maleImmunization", "")))%> /></td>
                    <td>Immunization
                </tr>
                <tr>
                    <td><input type="checkbox" name="maleOther1c" <%=Encode.forHtml(String.valueOf(props.getProperty("maleOther1c", "")))%> /></td>
                    <td><input type="text" name="maleOther1" class="ScreeningReview" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("maleOther1", "")))%>" /></td>
                </tr>
                <tr>
                    <td><input type="checkbox" name="maleOther2c" <%=Encode.forHtml(String.valueOf(props.getProperty("maleOther2c", "")))%> /></td>
                    <td><input type="text" name="maleOther2" class="ScreeningReview" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("maleOther2", "")))%>" /></td>
                </tr>
                <tr style="height:100%;">
                    <td>&nbsp;</td>
                </tr-->
                                </table>
                            </td>
                        </tr>
                    </table>
                    <table width="100%" class="tableWithBorder">
                        <tr>
                            <td colspan="9"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgPhysicalExam"/>:</b></td>
                        </tr>
                        <tr>
                            <td><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgVitals"/>: </b></td>
                            <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formBP"/>:
                                <input type="text" name="bprTop" size="5" maxlength="3"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("bprTop", "")))%>"/>/ <input
                                        type="text" name="bprBottom" size="5" maxlength="3"
                                        value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("bprBottom", "")))%>"/> <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgR"/></td>
                            <td align="right"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formPulse"/>:
                            </td>
                            <td><input type="text" name="pulse" size="10" maxlength="10"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("pulse", "")))%>"/> /<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgMinute"/></td>
                            <td align="right"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formHeight"/>:
                            </td>
                            <td><input type="text" name="height" size="10" maxlength="4"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("height", "")))%>"/> <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgHeightUnit"/>.
                            </td>
                            <td align="right"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formWeight"/>: <input
                                    type="text" name="weight" size="10" maxlength="4"
                                    value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("weight", "")))%>"/> <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgWeightUnit"/>.
                            </td>
                        <tr>
                            <td>&nbsp;</td>
                            <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formBP"/>:
                                <input type="text" name="bplTop" size="5" maxlength="3"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("bplTop", "")))%>"/>/ <input
                                        type="text" name="bplBottom" size="5" maxlength="3"
                                        value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("bplBottom", "")))%>"/> <fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.msgL"/></td>
                            <td align="right"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formRythm"/>:
                            </td>
                            <td><input type="text" name="rhythm" size="10" maxlength="10"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("rhythm", "")))%>"/></td>
                            <td align="right"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formUrineDipstick"/>:
                            </td>
                            <td><input type="text" name="urine" size="20" maxlength="30"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("urine", "")))%>"/></td>
                        </tr>
                    </table>
                    <table style="page-break-before: always;" width="100%">
                        <tr>
                            <td rowspan="3">
                                <table width="100%" class="DashedBorder">
                                    <tr>
                                        <td><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formPhysicalSigns"/>: </b></td>
                                    </tr>
                                    <tr>
                                        <td><textarea name="physicalSigns"
                                                      class="PhysicalSigns"><%=Encode.forHtml(String.valueOf(props.getProperty("physicalSigns", "")))%></textarea>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                    <table width="100%" class="TableWithBorder">
                        <tr>
                            <td>
                                <table width="100%">
                                    <tr>
                                        <td><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formAssessment"/></b></td>
                                    </tr>
                                    <tr>
                                        <td align="center"><textarea name="assessment"
                                                                     class="AssessmentPlan"><%=Encode.forHtml(String.valueOf(props.getProperty("assessment", "")))%></textarea>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                            <td>
                                <table width="100%">
                                    <tr>
                                        <td align="center"><b><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formPlan"/></b></td>
                                    </tr>
                                    <tr>
                                        <td align="center"><textarea name="plan"
                                                                     class="AssessmentPlan"><%=Encode.forHtml(String.valueOf(props.getProperty("plan", "")))%></textarea>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" align="right"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.formSignature"/>: <input
                                    type="text" name="signature" size="30"
                                    value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("signature", "")))%>"/></td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>

        <table class="Head" class="hidePrint">
            <tr>
                <td align="left"><input type="submit"
                                        value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnSave"/>"
                                        onclick="javascript:return onSave();"/> <input type="submit"
                                                                                       value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnSaveExit"/>"
                                                                                       onclick="javascript:return onSaveExit();"/>
                    <input type="submit"
                           value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnExit"/>"
                           onclick="javascript:return onExit();"/> <input type="button"
                                                                          value="<fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnPrint"/>"
                                                                          onclick="javascript:return onPrint();"/></td>
                <td align='right'><a
                        href="javascript: popupPage(700,950,'<%= request.getContextPath() %>/decision/annualreview/annualreviewplanner.jsp?demographic_no=<%=Encode.forUriComponent(String.valueOf(demoNo))%>&formId=<%=Encode.forUriComponent(String.valueOf(formId))%>&provNo=<%=Encode.forUriComponent(String.valueOf(provNo))%>');"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formMaleAnnual.btnAnnualReview"/></a></td>
            </tr>
        </table>

    </form>
    </body>
</html>
