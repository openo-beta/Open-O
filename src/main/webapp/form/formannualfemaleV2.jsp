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
<%@ page import="ca.openosp.openo.form.*" %>
<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@ page import="ca.openosp.openo.form.FrmRecord" %>
<%@ page import="ca.openosp.openo.form.FrmRecordFactory" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="oscarResources"/>



<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <title>Annual Health Review</title>
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

        function popupOscarCon(vheight, vwidth, varpage) {
            var page = varpage;
            windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes, screenX=0,screenY=0,top=0,left=0";
            var popup = window.open(varpage, "<fmt:message key="oscarEncounter.Index.msgOscarConsultation"/>", windowprops);
            popup.focus();
        }


        function onSave() {
            document.forms[0].submit.value = "save";
            var ret = checkAllDates();
            if (ret == true) {
                ret = confirm("Are you sure you want to save this form?");
            }
            return ret;
        }

        function onSaveExit() {
            document.forms[0].submit.value = "exit";
            var ret = checkAllDates();
            if (ret == true) {
                ret = confirm("Are you sure you wish to save and close this window?");
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
                alert("You must type in a number in the field.");
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
            if (valDate(document.forms[0].lmp) == false) {
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
        String formClass = "AnnualV2";
//        String formClass = "Annual";
        String formLink = "formannualfemaleV2.jsp";

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
           value=<%=Encode.forHtml(request.getParameter("provNo"))%>/>
    <input type="hidden" name="formCreated"
           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("formCreated", "")))%>"/>
    <input type="hidden" name="form_class" value="<%=formClass%>"/>
    <input type="hidden" name="form_link" value="<%=formLink%>"/>
    <input type="hidden" name="provNo"
           value="<%= Encode.forHtmlAttribute(request.getParameter("provNo")) %>"/>
    <input type="hidden" name="submit" value="exit"/>

    <table class="Head">
        <!--class="hidePrint"-->
        <tr>
            <td align="left"><input type="submit" value="Save"
                                    onclick="javascript:return onSave();"/> <input type="submit"
                                                                                   value="Save and Exit"
                                                                                   onclick="javascript:return onSaveExit();"/>
                <input
                        type="submit" value="Exit" onclick="javascript:return onExit();"/>
                <input type="button" value="Print"
                       onclick="javascript:return onPrint();"/> <input type="button"
                                                                       value="Consult"
                                                                       onclick="javascript:popupOscarCon(700,960,'<%= request.getContextPath() %>/oscarEncounter/oscarConsultationRequest/ConsultationFormRequest.jsp?de=<%=Encode.forJavaScript(String.valueOf(demoNo))%>');"/>

            </td>
            <td align='right'><a
                    href="javascript: popupPage(700,950,'<%= request.getContextPath() %>/decision/annualreview/annualreviewplanner.jsp?demographic_no=<%=Encode.forUriComponent(String.valueOf(demoNo))%>&formId=<%=Encode.forUriComponent(String.valueOf(formId))%>&provNo=<%=Encode.forUriComponent(String.valueOf(provNo))%>');">Annual
                Review Planner</a></td>
        </tr>
    </table>

    <table>
        <tr>
            <td>
                <table cellspacing="3" cellpadding="0" width="100%">
                    <tr>
                        <td><big><i><b>ANNUAL FEMALE HEALTH REVIEW</b></i></big></td>
                        <td><b>Name:</b> <input type="text" class="Input" name="pName"
                                                readonly="true" size="30"
                                                value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("pName", "")))%>"/></td>
                        <td><b>Age:</b> <input type="text" class="Input"
                                               readonly="true" name="age" size="11"
                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("age", "")))%>"/></td>
                        <td><b>Date</b><small>(yyyy/mm/dd)</small>: <input type="text"
                                                                           class="Input" name="formDate" size="11"
                                                                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("formDate", "")))%>"/>
                        </td>
                    </tr>
                </table>
                <table width="100%" class="FixedTableWithBorder">
                    <tr>
                        <td align="center" valign=top>
                            <table width="100%">
                                <tr>
                                    <td class="HeadingsReqOhip">PMHX/PSHX:*</td>
                                    <td class="HeadingNotOhip">Updated<input type="checkbox"
                                                                             name="pmhxPshxUpdated"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("pmhxPshxUpdated", "")))%> /></td>
                                </tr>
                                <tr>
                                    <td class="HeadingsReqOhip">FamHx:*</td>
                                    <td class="HeadingNotOhip">Updated<input type="checkbox"
                                                                             name="famHxUpdated" <%=Encode.forHtml(String.valueOf(props.getProperty("famHxUpdated", "")))%> />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="HeadingsReqOhip">SocHx:*</td>
                                    <td class="HeadingNotOhip">Updated<input type="checkbox"
                                                                             name="socHxUpdated" <%=Encode.forHtml(String.valueOf(props.getProperty("socHxUpdated", "")))%> />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="HeadingNotOhip">Allergies:</td>
                                    <td class="HeadingNotOhip">Updated<input type="checkbox"
                                                                             name="allergiesUpdated"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("allergiesUpdated", "")))%> /></td>
                                </tr>
                                <tr>
                                    <td class="HeadingNotOhip">Medications:</td>
                                    <td class="HeadingNotOhip">Updated<input type="checkbox"
                                                                             name="medicationsUpdated"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("medicationsUpdated", "")))%> /></td>
                                </tr>

                            </table>
                        </td>
                        <td align="center">
                            <table cellspacing=0>
                                <tr>
                                    <td class="HeadingsReqOhip">Wt:*</td>
                                    <td><input type="text" name="weight"
                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("weight", "")))%>"/></td>
                                <tr>
                                <tr>
                                    <td class="HeadingsReqOhip">Ht:*</td>
                                    <td><input type="text" name="height"
                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("height", "")))%>"/></td>
                                <tr>
                                <tr>
                                    <td class="HeadingNotOhip">Waist (90cm):</td>
                                    <td><input type="text" name="waist"
                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("waist", "")))%>"/></td>
                                <tr>
                                <tr>
                                    <td class="HeadingNotOhip">LMP:</td>
                                    <td><input type="text" name="lmp"
                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("lmp", "")))%>"/></td>
                                <tr>
                                <tr>
                                    <td class="HeadingsReqOhip">BP:*</td>
                                    <td><input type="text" name="BP"
                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("BP", "")))%>"/></td>
                                <tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <br>
                <table class="FixedTableWithBorder">
                    <tr>
                        <td valign="top">
                            <table>
                                <tr>
                                    <td colspan="3" nowrap="true" align=center
                                        class="HeadingsReqOhip">Lifestyle Review:
                                    </td>
                                    <td>&nbsp;</td>
                                </tr>
                                <tr>
                                    <td>&nbsp;</td>
                                    <td class="Headings">No</td>
                                    <td class="Headings">Yes</td>
                                    <td>&nbsp;</td>
                                </tr>
                                <tr>

                                    <td class="listItemReqOhip">Smoking:*</td>
                                    <td><input type="checkbox" name="smokingNo"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("smokingNo", "")))%> /></td>
                                    <td><input type="checkbox" name="smokingYes"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("smokingYes", "")))%> /></td>
                                    <td align="right"><input type="text" name="smoking"
                                                             class="LifestyleReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("smoking", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItemReqOhip">ETOH:*</td>
                                    <td><input type="checkbox" name="etohNo"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("etohNo", "")))%> /></td>
                                    <td><input type="checkbox" name="etohYes"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("etohYes", "")))%> /></td>
                                    <td align="right"><input type="text" name="etoh"
                                                             class="LifestyleReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("etoh", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItem">Caffeine:</td>
                                    <td><input type="checkbox" name="caffineNo"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("caffineNo", "")))%> /></td>
                                    <td><input type="checkbox" name="caffineYes"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("caffineYes", "")))%> /></td>
                                    <td align="right"><input type="text" name="caffine"
                                                             class="LifestyleReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("caffine", "")))%>"/></td>
                                </tr>

                                <tr>
                                    <td class="listItemReqOhip">OTC/Illicit Drugs:*</td>
                                    <td><input type="checkbox" name="otcNo"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("otcNo", "")))%> /></td>
                                    <td><input type="checkbox" name="otcYes"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("otcYes", "")))%> /></td>
                                    <td align="right"><input type="text" name="otc"
                                                             class="LifestyleReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("otc", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItem">Exercise/Sports:</td>
                                    <td><input type="checkbox" name="exerciseNo"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("exerciseNo", "")))%> /></td>
                                    <td><input type="checkbox" name="exerciseYes"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("exerciseYes", "")))%> /></td>
                                    <td align="right"><input type="text" name="exercise"
                                                             class="LifestyleReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("exercise", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItemReqOhip">Nutrition:</td>
                                    <td><input type="checkbox" name="nutritionNo"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("nutritionNo", "")))%> /></td>
                                    <td><input type="checkbox" name="nutritionYes"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("nutritionYes", "")))%> /></td>
                                    <td align="right"><input type="text" name="nutrition"
                                                             class="LifestyleReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("nutrition", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItem">Dental Hygiene:</td>
                                    <td><input type="checkbox" name="dentalNo"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("dentalNo", "")))%> /></td>
                                    <td><input type="checkbox" name="dentalYes"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("dentalYes", "")))%> /></td>
                                    <td align="right"><input type="text" name="dental"
                                                             class="LifestyleReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("dental", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td nowrap="true" class="listItem">Occupational Risks:</td>
                                    <td><input type="checkbox" name="occupationalNo"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("occupationalNo", "")))%> /></td>
                                    <td><input type="checkbox" name="occupationalYes"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("occupationalYes", "")))%> /></td>
                                    <td align="right"><input type="text" name="occupational"
                                                             class="LifestyleReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("occupational", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td nowrap="true" class="listItem">Foreign Travel (in last
                                        yr.):
                                    </td>
                                    <td><input type="checkbox" name="travelNo"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("travelNo", "")))%> /></td>
                                    <td><input type="checkbox" name="travelYes"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("travelYes", "")))%> /></td>
                                    <td align="right"><input type="text" name="travel"
                                                             class="LifestyleReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("travel", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td nowrap="true" class="listItem">Sexual
                                        Health/Relationships:
                                    </td>
                                    <td><input type="checkbox" name="sexualityNo"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("sexualityNo", "")))%> /></td>
                                    <td><input type="checkbox" name="sexualityYes"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("sexualityYes", "")))%> /></td>
                                    <td align="right"><input type="text" name="sexuality"
                                                             class="LifestyleReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("sexuality",
"")))%>"/></td>
                                </tr>
                            </table>
                        </td>
                        <td>
                            <table width="100%">
                                <tr>
                                    <td colspan="4" align="center" class="HeadingsReqOhip">Functional
                                        Inquiry*/Current Concerns:
                                    </td>

                                </tr>
                                <tr>
                                    <td>&nbsp;</td>
                                    <td class="Headings">N</td>
                                    <td class="Headings">AbN</td>
                                    <td>&nbsp;</td>
                                </tr>
                                <tr>
                                    <td align="left" nowrap="true"
                                        title="(sleep, energy, wt. loss, appetite, etc.)"
                                        class="listItemReqOhip">General*:
                                    </td>
                                    <td><input type="checkbox" name="generalN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("generalN", "")))%> /></td>
                                    <td><input type="checkbox" name="generalAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("generalAbN", "")))%> /></td>
                                    <td align="right"><input type="text" name="general"
                                                             class="SystemsReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("general", "")))%>"/></td>
                                </tr>

                                <tr>
                                    <td align="left" nowrap="true" class="listItemReqOhip">H/N:*</td>
                                    <td><input type="checkbox" name="headN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("headN", "")))%> /></td>
                                    <td><input type="checkbox" name="headAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("headAbN", "")))%> /></td>
                                    <td align="right"><input type="text" name="head"
                                                             class="SystemsReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("head", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItemReqOhip">Chest:*</td>
                                    <td><input type="checkbox" name="chestN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("chestN", "")))%> /></td>
                                    <td><input type="checkbox" name="chestAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("chestAbN", "")))%> /></td>
                                    <td align="right"><input type="text" name="chest"
                                                             class="SystemsReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("chest", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItemReqOhip">CVS:*</td>
                                    <td><input type="checkbox" name="cvsN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("cvsN", "")))%> /></td>
                                    <td><input type="checkbox" name="cvsAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("cvsAbN", "")))%> /></td>
                                    <td align="right"><input type="text" name="cvs"
                                                             class="SystemsReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cvs", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItemReqOhip">G.I.:*</td>
                                    <td><input type="checkbox" name="giN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("giN", "")))%> /></td>
                                    <td><input type="checkbox" name="giAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("giAbN", "")))%> /></td>
                                    <td align="right"><input type="text" name="gi"
                                                             class="SystemsReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("gi", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItemReqOhip">G.U.:*</td>
                                    <td><input type="checkbox" name="guN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("guN", "")))%> /></td>
                                    <td><input type="checkbox" name="guAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("guAbN", "")))%> /></td>
                                    <td align="right"><input type="text" name="gu"
                                                             class="SystemsReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("gu", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItemReqOhip">CNS:*</td>
                                    <td><input type="checkbox" name="cnsN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("cnsN", "")))%> /></td>
                                    <td><input type="checkbox" name="cnsAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("cnsAbN", "")))%> /></td>
                                    <td align="right"><input type="text" name="cns"
                                                             class="SystemsReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cns", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItemReqOhip">MSK:*</td>
                                    <td><input type="checkbox" name="mskN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("mskN", "")))%> /></td>
                                    <td><input type="checkbox" name="mskAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("mskAbN", "")))%> /></td>
                                    <td align="right"><input type="text" name="msk"
                                                             class="SystemsReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("msk", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItemReqOhip">Skin:*</td>
                                    <td><input type="checkbox" name="skinN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("skinN", "")))%> /></td>
                                    <td><input type="checkbox" name="skinAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("skinAbN", "")))%> /></td>
                                    <td align="right"><input type="text" name="skin"
                                                             class="SystemsReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("skin", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td class="listItemReqOhip">Mood:*</td>
                                    <td><input type="checkbox" name="moodN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("moodN", "")))%> /></td>
                                    <td><input type="checkbox" name="moodAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("moodAbN", "")))%> /></td>
                                    <td align="right"><input type="text" name="mood"
                                                             class="SystemsReview"
                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("mood", "")))%>"/></td>
                                </tr>
                                <tr>
                                    <td valign="top" class="listItem">Other:</td>
                                    <td valign="top"><input type="checkbox" name="otherN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("otherN", "")))%> /></td>
                                    <td valign="top"><input type="checkbox" name="otherAbN"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("otherAbN", "")))%> /></td>
                                    <td align="right"><textarea name="other"
                                                                class="SystemsReview"
                                                                style="height: 50px;"><%=Encode.forHtml(String.valueOf(props.getProperty("other", "")))%></textarea>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <br>
                <table class="FixedTableWithBorder">
                    <tr>
                        <td>
                            <table>
                                <tr>
                                    <td>
                                        <table>
                                            <tr>
                                                <td>
                                                    <table>
                                                        <tr>
                                                            <td class="HeadingsReqOhip">H/N:*</td>
                                                            <td class="Headings">N</td>
                                                            <td class="Headings">AbN</td>
                                                            <td>&nbsp;</td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Eyes:</td>
                                                            <td><input type="checkbox" name="eyesN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("eyesN", "")))%> /></td>
                                                            <td><input type="checkbox" name="eyesAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("eyesAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="eyes"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("eyes", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Ears:</td>
                                                            <td><input type="checkbox" name="earsN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("earsN", "")))%> /></td>
                                                            <td><input type="checkbox" name="earsAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("earsAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="ears"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ears", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Oropharynx:</td>
                                                            <td><input type="checkbox" name="oropharynxN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("oropharynxN", "")))%> /></td>
                                                            <td><input type="checkbox" name="oropharynxAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("oropharynxAbN", "")))%> />
                                                            </td>
                                                            <td><input type="text" class="OnExam" name="oropharynx"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("oropharynx", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Thyroid:</td>
                                                            <td><input type="checkbox" name="thyroidN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("thyroidN", "")))%> /></td>
                                                            <td><input type="checkbox" name="thyroidAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("thyroidAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="thyroid"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("thyroid", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">L. nodes:</td>
                                                            <td><input type="checkbox" name="lnodesN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("lnodesN", "")))%> /></td>
                                                            <td><input type="checkbox" name="lnodesAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("lnodesAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="lnodes"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("lnodes", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td colspan=4>&nbsp;</td>
                                                        </tr>
                                                        <tr>
                                                            <td class="HeadingsReqOhip">CHEST:*</td>
                                                            <td class="Headings">N</td>
                                                            <td class="Headings">AbN</td>
                                                            <td>&nbsp;</td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Clear:</td>
                                                            <td><input type="checkbox" name="clearN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("clearN", "")))%> /></td>
                                                            <td><input type="checkbox" name="clearAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("clearAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="clear"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("clear", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">A/E = bilat:</td>
                                                            <td><input type="checkbox" name="bilatN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("bilatN", "")))%> /></td>
                                                            <td><input type="checkbox" name="bilatAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("bilatAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="bilat"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("bilat", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Wheezes:</td>
                                                            <td><input type="checkbox" name="wheezesN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("wheezesN", "")))%> /></td>
                                                            <td><input type="checkbox" name="wheezesAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("wheezesAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="wheezes"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("wheezes", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Crackles:</td>
                                                            <td><input type="checkbox" name="cracklesN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cracklesN", "")))%> /></td>
                                                            <td><input type="checkbox" name="cracklesAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cracklesAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="crackles"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("crackles", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Other:</td>
                                                            <td colspan=2>&nbsp;</td>
                                                            <td><input type="text" class="OnExam" name="chestOther"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("chestOther", "")))%>"/>
                                                            </td>

                                                        </tr>
                                                        <tr>
                                                            <td colspan=4>&nbsp;</td>
                                                        </tr>
                                                        <tr>
                                                            <td class="HeadingsReqOhip">CVS*</td>
                                                            <td class="Headings">N</td>
                                                            <td class="Headings">AbN</td>
                                                            <td>&nbsp;</td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItemReqOhip">S1,S2*:</td>
                                                            <td><input type="checkbox" name="s1s2N"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("s1s2N", "")))%> /></td>
                                                            <td><input type="checkbox" name="s1s2AbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("s1s2AbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="s1s2"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("s1s2", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Murmur:</td>
                                                            <td><input type="checkbox" name="murmurN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("murmurN", "")))%> /></td>
                                                            <td><input type="checkbox" name="murmurAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("murmurAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="murmur"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("murmur", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Periph pulse:</td>
                                                            <td><input type="checkbox" name="periphPulseN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("periphPulseN", "")))%> /></td>
                                                            <td><input type="checkbox" name="periphPulseAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("periphPulseAbN", "")))%> />
                                                            </td>
                                                            <td><input type="text" class="OnExam" name="periphPulse"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("periphPulse", "")))%>"
                                                                / />
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Edema:</td>
                                                            <td><input type="checkbox" name="edemaN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("edemaN", "")))%> /></td>
                                                            <td><input type="checkbox"
                                                                       name="edemaAbN"<%=Encode.forHtml(String.valueOf(props.getProperty("edemaAbN", "")))%>
                                                                / >
                                                            </td>
                                                            <td><input type="text" class="OnExam" name="edema"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("edema", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">JVP:</td>
                                                            <td><input type="checkbox" name="jvpN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("jvpN", "")))%> /></td>
                                                            <td><input type="checkbox"
                                                                       name="jvpAbN" <%=Encode.forHtml(String.valueOf(props.getProperty("jvpAbN", "")))%>
                                                                //>
                                                            </td>
                                                            <td><input type="text" class="OnExam" name="jvp"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("jvp", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItemReqOhip">HR/rhythm:*</td>
                                                            <td><input type="checkbox" name="rhythmN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rhythmN", "")))%> /></td>
                                                            <td><input type="checkbox" name="rhythmAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rhythmAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="rhythm"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("rhythm", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItemReqOhip">BP:*</td>
                                                            <td><input type="checkbox" name="chestbpN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("chestbpN", "")))%> /></td>
                                                            <td><input type="checkbox" name="chestbpAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("chestbpAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="chestbp"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("chestbp", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Other:</td>
                                                            <td colspan=2>&nbsp;</td>
                                                            <td><input type="text" class="OnExam" name="cvsOther"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cvsOther", "")))%>"/>
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
                        <td>
                            <table>
                                <tr>
                                    <td>
                                        <table>
                                            <tr>
                                                <td>
                                                    <table>
                                                        <tr>
                                                            <td class="HeadingsReqOhip">BREASTS:*</td>
                                                            <td class="Headings">N</td>
                                                            <td class="Headings">AbN</td>
                                                            <td>&nbsp;</td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Left:</td>
                                                            <td><input type="checkbox" name="breastLeftN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("breastLeftN", "")))%> /></td>
                                                            <td><input type="checkbox" name="breastLeftAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("breastLeftAbN", "")))%> />
                                                            </td>
                                                            <td><input type="text" class="OnExam" name="breastLeft"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("breastLeft", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Right:</td>
                                                            <td><input type="checkbox" name="breastRightN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("breastRightN", "")))%> /></td>
                                                            <td><input type="checkbox" name="breastRightAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("breastRightAbN", "")))%> />
                                                            </td>
                                                            <td><input type="text" class="OnExam" name="breastRight"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("breastRight", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td colspan=4>&nbsp;</td>
                                                        </tr>

                                                        <tr>
                                                            <td class="HeadingsReqOhip">ABD:*</td>
                                                            <td class="Headings">N</td>
                                                            <td class="Headings">AbN</td>
                                                            <td>&nbsp;</td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Soft:</td>
                                                            <td><input type="checkbox" name="softN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("softN", "")))%> /></td>
                                                            <td><input type="checkbox" name="softAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("softAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="soft"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("soft", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Tender:</td>
                                                            <td><input type="checkbox" name="tenderN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("tenderN", "")))%> /></td>
                                                            <td><input type="checkbox" name="tenderAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("tenderAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="tender"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("tender", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">b.s.:</td>
                                                            <td><input type="checkbox" name="bsN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("bsN", "")))%> /></td>
                                                            <td><input type="checkbox" name="bsAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("bsAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="bs"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("bs", "")))%>"/></td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Hepatomeg:</td>
                                                            <td><input type="checkbox" name="hepatomegN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("hepatomegN", "")))%> /></td>
                                                            <td><input type="checkbox" name="hepatomegAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("hepatomegAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="hepatomeg"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("hepatomeg", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Splenomeg:</td>
                                                            <td><input type="checkbox" name="splenomegN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("splenomegN", "")))%> /></td>
                                                            <td><input type="checkbox" name="splenomegAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("splenomegAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="splenomeg"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("splenomeg", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Masses:</td>
                                                            <td><input type="checkbox" name="massesN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("massesN", "")))%> /></td>
                                                            <td><input type="checkbox" name="massesAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("massesAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="masses"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("masses", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Rectal:</td>
                                                            <td><input type="checkbox" name="rectalN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rectalN", "")))%> /></td>
                                                            <td><input type="checkbox" name="rectalAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rectalAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="rectal"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("rectal", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td colspan=4>&nbsp;</td>
                                                        </tr>

                                                        <tr>
                                                            <td class="HeadingsReqOhip">GENITALIA:*</td>
                                                            <td class="Headings">N</td>
                                                            <td class="Headings">AbN</td>
                                                            <td>&nbsp;</td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Cx:</td>
                                                            <td><input type="checkbox" name="cxN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cxN", "")))%> /></td>
                                                            <td><input type="checkbox" name="cxAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cxAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="cx"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cx", "")))%>"/></td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Bimanual:</td>
                                                            <td><input type="checkbox" name="bimanualN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("bimanualN", "")))%> /></td>
                                                            <td><input type="checkbox" name="bimanualAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("bimanualAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="bimanual"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("bimanual", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Adnexa:</td>
                                                            <td><input type="checkbox" name="adnexaN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("adnexaN", "")))%> /></td>
                                                            <td><input type="checkbox" name="adnexaAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("adnexaAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="adnexa"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("adnexa", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="listItem">Pap:</td>
                                                            <td><input type="checkbox" name="papN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("papN", "")))%> /></td>
                                                            <td><input type="checkbox" name="papAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("papAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="pap"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("pap", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td colspan=4>&nbsp;</td>
                                                        </tr>

                                                        <tr>
                                                            <td class="HeadingsReqOhip">MSK:*</td>
                                                            <td><input type="checkbox" name="exammskN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("exammskN", "")))%> /></td>
                                                            <td><input type="checkbox" name="exammskAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("exammskAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="exammsk"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("exammsk", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td colspan=4>&nbsp;</td>
                                                        </tr>

                                                        <tr>
                                                            <td class="HeadingsReqOhip">Skin:*</td>
                                                            <td><input type="checkbox" name="examskinN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("examskinN", "")))%> /></td>
                                                            <td><input type="checkbox" name="examskinAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("examskinAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="examskin"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("examskin", "")))%>"/>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td colspan=4>&nbsp;</td>
                                                        </tr>

                                                        <tr>
                                                            <td class="HeadingsReqOhip">CNS:*</td>
                                                            <td><input type="checkbox" name="examcnsN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("examcnsN", "")))%> /></td>
                                                            <td><input type="checkbox" name="examcnsAbN"
                                                                    <%=Encode.forHtml(String.valueOf(props.getProperty("examcnsAbN", "")))%> /></td>
                                                            <td><input type="text" class="OnExam" name="examcns"
                                                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("examcns", "")))%>"/>
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
                    </tr>
                </table>
                <br>
                <table class="FixedTableWithBorder">
                    <tr>
                        <td>
                            <table width="100%">
                                <tr>
                                    <td><b>IMPRESSION & PLAN</b></td>
                                </tr>
                                <tr>
                                    <td align="center"><textarea name="impressionPlan"
                                                                 class="ImpressionPlan"><%=Encode.forHtml(String.valueOf(props.getProperty("impressionPlan", "")))%></textarea>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <br>


                <table class="FixedTableWithBorder">
                    <tr>
                        <td class="bottomBorder">
                            <table border=0 width="100%" cellspacing="0" cellpadding="0">
                                <tr>
                                    <td colspan=3>
                                        <table cellspacing="0" cellpadding="0" width="100%" border=0>
                                            <tr>
                                                <td class="HeadingsReqOhip" width="200">1. Sexual Health:</td>
                                                <td class="HeadingsReqOhip">G</td>
                                                <td class="HeadingsReqOhip">T</td>
                                                <td class="HeadingsReqOhip">P</td>
                                                <td class="HeadingsReqOhip">A</td>
                                                <td class="HeadingsReqOhip">L</td>
                                                <td class="HeadingsReqOhip">PAP</td>
                                            </tr>
                                        </table>
                                    </td>

                                </tr>
                                <tr>
                                    <td colspan=3 class="listItem">Previous Hx Sti's:</td>
                                </tr>
                                <tr>
                                    <td colspan=3 class="listItem">Contraception:</td>
                                </tr>
                                <tr>
                                    <td class="listItem">Preconceptive Counselling (A):</td>
                                    <td class="listItem">Folate 0.4mg or 4 mg +ve Hx</td>
                                    <td class="listItem">Rubella</td>
                                </tr>
                                <tr>
                                    <td class="listItem">&nbsp;</td>
                                    <td class="listItem">Varicella Toxo</td>
                                    <td class="listItem">&nbsp;</td>
                                </tr>
                                <tr>
                                    <td colspan=3 class="listItem">Sexual Dysfunction:</td>
                                </tr>
                                <tr>
                                    <td colspan=3 class="listItem">Safe Sex:</td>
                                </tr>
                            </table>
                        </td>
                        <td class="bottomBorder"><textarea class="ToDos" rows=6
                                                           name="toDoSexualHealth"><%=Encode.forHtml(String.valueOf(props.getProperty("toDoSexualHealth", "")))%></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="bottomBorder" valign="top">
                            <table cellspacing=0 cellpadding=0 width="100%">
                                <tr>
                                    <td class="HeadingsReqOhip">2. Obesity:</td>
                                    <td class="listItem">BMI(>27)</td>
                                    <td align=right class="listItem">Level B</td>
                                </tr>
                            </table>
                        </td>
                        <td class="bottomBorder"><textarea class="ToDos" rows=2
                                                           name="toDoObesity"><%=Encode.forHtml(String.valueOf(props.getProperty("toDoObesity", "")))%></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="bottomBorder">
                            <table width="100%" cellspacing=0 cellpadding=0>
                                <tr>
                                    <td valign=top class="HeadingsReqOhip">3.Cholesterol:</td>
                                    <td>
                                        <table>
                                            <tr>
                                                <td colspan=2 class="listItem">F> 50 years or 2 +ve Risk
                                                    Factors:
                                                </td>
                                            </tr>
                                            <tr>
                                                <td colspan=2 class="HeadingsReqOhip">Risk Factors</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">CAD</td>
                                                <td class="listItem">DM</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">PVD</td>
                                                <td class="listItem">Stig of inc lipids</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">CVD</td>
                                                <td class="listItem">Fam hx CAD</td>
                                            </tr>
                                            <tr>
                                                <td colspan=2 class="listItem">Carotid disease</td>
                                            </tr>
                                            <tr>
                                                <td colspan=2 class="listItem">Fam hx hypertension</td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td align=right valign=top class="listItem">Level C</td>
                                </tr>


                            </table>
                        </td>
                        <td class="bottomBorder"><textarea class="ToDos" rows=8
                                                           name="toDoCholesterol"><%=Encode.forHtml(String.valueOf(props.getProperty("toDoCholesterol", "")))%></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="bottomBorder">
                            <table width="100%" cellspacing=0 cellpadding=0>
                                <tr>
                                    <td class="HeadingsReqOhip">4. Osteoporosis:</td>
                                    <td class="listItem">BMD age > 65 yes or 1 major or 2 minor
                                        RF's
                                    </td>
                                    <td align="right" class="listItem">Level A</td>
                                </tr>
                            </table>
                            <table>
                                <tr>
                                    <td valign=top class="listItem">
                                        <table>
                                            <tr>
                                                <td class="HeadingsReqOhip">Major RF's</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Compression #</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Fragility # > 40 years</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Fam Hx (hip#)</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Glucocorticoids > 3 months</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Malabsorption</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Primary hyperthyroidism</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Osteopenia on x-ray</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Menopause < 45 years</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Inc. falls risk</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Hypogonadism</td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td valign=top>
                                        <table>
                                            <tr>
                                                <td class="HeadingsReqOhip">Minor RF's</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">RA</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Clinical hyperthyroidism</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Anticonvulsants</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Wt < 57kg</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Smoking</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">ETOH</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Excessive caffine</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Dec. dietary Ca<sup>2+</sup></td>
                                            </tr>
                                        </table>

                                    </td>
                                </tr>
                            </table>
                            <table>
                                <tr>
                                    <td colspan=2 class="listItem">Screening Frequencies:</td>
                                </tr>
                                <td class="listItem">&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                <td class="listItem">Annually if at risk for rapid bone loss
                                    (steroids, immobility)<br>
                                    q2 to 3 years for TC that increases bone mineral density slowly
                                    (calcitonin)<br>
                                    Ca<sup>2+</sup> / Vit D req: Age > 50 yes Ca<sup>2+</sup> 1500mg /
                                    Vit D 800 IU OD
                                </td>
                            </table>
                        </td>
                        <td class="bottomBorder"><textarea class="ToDos" rows=19
                                                           name="toDoOsteoporosis"><%=Encode.forHtml(String.valueOf(props.getProperty("toDoOsteoporosis", "")))%></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="bottomBorder">
                            <table width=100%>
                                <tr>
                                    <td rowspan=3 valign=top class="HeadingsReqOhip">5. PAPs:</td>
                                    <td class="listItem">annual after becoming sexually active</td>
                                    <td align=right class="listItem">Level B</td>
                                </tr>
                                <tr>
                                    <td colspan=2 class="listItem">if N X3 then q2 yr (B)</td>
                                </tr>
                                <tr>
                                    <td colspan=2 class="listItem">if 4 N in 10 yrs and no hx
                                        AbN, d/c screening age 70
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td class="bottomBorder"><textarea class="ToDos" rows=3
                                                           name="toDoPAPs"><%=Encode.forHtml(String.valueOf(props.getProperty("toDoPAPs", "")))%></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="bottomBorder" valign=top width="100%">
                            <table width="100%" valign="top">
                                <tr>
                                    <td class="HeadingsReqOhip">6.Mammogram:</td>
                                    <td class="listItem">age 50 - 69 yrs: q2 yrs</td>
                                    <td align=right class="listItem">Level A</td>
                                </tr>
                            </table>
                        </td>
                        <td class="bottomBorder"><textarea class="ToDos" rows=3
                                                           name="toDoMammogram"><%=Encode.forHtml(String.valueOf(props.getProperty("toDoMammogram", "")))%></textarea>
                        </td>

                    </tr>
                    <tr>
                        <td class="bottomBorder">
                            <table>
                                <tr>
                                    <td class="HeadingsReqOhip">7. Colorectal CA:</td>
                                </tr>
                                <tr>
                                    <td valign=top>
                                        <table>
                                            <tr>
                                                <td class="listItem">annual FOB age 50 yrs (A)</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">sig or colonoscopy for high risk (B)</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">genetic screening (B)</td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td>
                                        <table>
                                            <tr>
                                                <td class="HeadingsReqOhip">RF's</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">FPS</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">polyps</td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Fam Hx colon Ca
                                                <td>
                                            </tr>
                                            <tr>
                                                <td class="listItem">Endometrial/Breast/Ovarian Ca</td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td class="bottomBorder"><textarea class="ToDos" rows=7
                                                           name="toDoColorectal"><%=Encode.forHtml(String.valueOf(props.getProperty("toDoColorectal", "")))%></textarea>
                        </td>

                    </tr>
                    <tr>
                        <td class="bottomBorder">
                            <table width="100%">
                                <tr>
                                    <td rowspan=4 class="HeadingsReqOhip" valign="top">8.
                                        Elderly:
                                    </td>
                                    <td class="listItem">Falls (A)</td>
                                    <td class="listItem">Home Safety (C)</td>
                                </tr>
                                <tr>
                                    <td class="listItem">Vision (B)</td>
                                    <td class="listItem">Driving</td>
                                </tr>
                                <tr>
                                    <td class="listItem">Hearing (B)</td>
                                    <td class="listItem">A-fib</td>
                                </tr>
                                <tr>
                                    <td class="listItem">Cognition (A)</td>
                                    <td class="listItem">Abuse (C)</td>
                                </tr>
                            </table>
                        </td>
                        <td class="bottomBorder"><textarea class="ToDos" rows=5
                                                           name="toDoElderly"><%=Encode.forHtml(String.valueOf(props.getProperty("toDoElderly", "")))%></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="bottomBorder">
                            <table>
                                <tr>
                                    <td width=200 class="HeadingsReqOhip">9.Immunization</td>
                                    <td class="listItem">Td<input type="checkbox"
                                                                  name="immunizationtd"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("immunizationtd", "")))%> /></td>
                                    <td class="listItem">Pneumovax<input type="checkbox"
                                                                         name="immunizationPneumovax"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("immunizationPneumovax", "")))%> /></td>
                                    <td class="listItem">Flu<input type="checkbox"
                                                                   name="immunizationFlu"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("immunizationFlu", "")))%> /></td>
                                    <td class="listItem">Menjugate<input type="checkbox"
                                                                         name="immunizationMenjugate"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("immunizationMenjugate", "")))%> /></td>
                                </tr>
                            </table>
                        </td>
                        <td class="bottomBorder"><textarea class="ToDos" rows=3
                                                           name="toDoImmunization"><%=Encode.forHtml(String.valueOf(props.getProperty("toDoImmunization", "")))%></textarea>
                        </td>
                    </tr>

                </table>

                <br>
                <table class="FixedTableWithBorder">
                    <tr>
                        <td colspan="2" align="right">Signature: <input type="text"
                                                                        name="signature" size="30"
                                                                        value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("signature", "")))%>"/>
                        </td>
                    </tr>
                </table>

                <table class="Head" class="hidePrint">
                    <tr>
                        <td align="left"><input type="submit" value="Save"
                                                onclick="javascript:return onSave();"/> <input type="submit"
                                                                                               value="Save and Exit"
                                                                                               onclick="javascript:return onSaveExit();"/>
                            <input type="submit" value="Exit"
                                   onclick="javascript:return onExit();"/> <input type="button"
                                                                                  value="Print"
                                                                                  onclick="javascript:return onPrint();"/>
                        </td>
                        <td align='right'><a
                                href="javascript: popupPage(700,950,'<%= request.getContextPath() %>/decision/annualreview/annualreviewplanner.jsp?demographic_no=<%=Encode.forUriComponent(String.valueOf(demoNo))%>&formId=<%=Encode.forUriComponent(String.valueOf(formId))%>&provNo=<%=Encode.forUriComponent(String.valueOf(provNo))%>');">Annual
                            Review Planner</a></td>
                    </tr>
                </table>

                </form>

    </body>
</html>
