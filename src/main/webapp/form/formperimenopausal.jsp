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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@ page import="ca.openosp.openo.form.FrmRecord" %>
<%@ page import="ca.openosp.openo.form.FrmRecordFactory" %>
<%@ page import="org.owasp.encoder.Encode" %>

<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <title>Peri Menopausal</title>
        <link rel="stylesheet" type="text/css" href="periMenopausalStyle.css">
        <link rel="stylesheet" type="text/css" media="print" href="print.css">
        <style type="text/css" media="print">
            BODY {
                font-size: 85%;
            }

            TABLE {
                font-size: 85%;
            }
        </style>
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
    </head>

    <%
        String formClass = "PeriMenopausal";
        String formLink = "formperimenopausal.jsp";

        int demoNo = Integer.parseInt(request.getParameter("demographic_no"));
        int formId = Integer.parseInt(request.getParameter("formId"));
        int provNo = Integer.parseInt((String) session.getAttribute("user"));
        FrmRecord rec = (new FrmRecordFactory()).factory(formClass);
        java.util.Properties props = rec.getFormRecord(LoggedInInfo.getLoggedInInfoFromSession(request), demoNo, formId);
    %>

    <script type="text/javascript" language="Javascript">

        function onPrint() {
//        document.forms[0].submit.value="print";
//        var ret = checkAllDates();
//        if(ret==true)
//        {
//            ret = confirm("Do you wish to save this form and view the print preview?");
//        }
//        return ret;
            window.print();
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

        function popupPage(varpage) { //open a new popup window
            var page = "" + varpage;
            windowprops = "height=600,width=700,location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=50,screenY=50,top=20,left=20";
            var popup = window.open(page, "printlocation", windowprops);
            if (popup != null) {
                if (popup.opener == null) {
                    popup.opener = self;
                }
            }
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
            if (valDate(document.forms[0].date1) == false) {
                b = false;
            } else if (valDate(document.forms[0].date2) == false) {
                b = false;
            } else if (valDate(document.forms[0].date3) == false) {
                b = false;
            } else if (valDate(document.forms[0].date4) == false) {
                b = false;
            } else if (valDate(document.forms[0].date5) == false) {
                b = false;
            } else if (valDate(document.forms[0].date6) == false) {
                b = false;
            } else if (valDate(document.forms[0].date7) == false) {
                b = false;
            } else if (valDate(document.forms[0].date8) == false) {
                b = false;
            } else if (valDate(document.forms[0].pneumovaxDate) == false) {
                b = false;
            }

            return b;
        }
    </script>


    <body>

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

        <table class="Head" class="hidePrint">
            <tr>
                <td align="left"><input type="submit" value="Save"
                                        onclick="javascript:return onSave();"/> <input type="submit"
                                                                                       value="Save and Exit"
                                                                                       onclick="javascript:return onSaveExit();"/>
                    <input
                            type="submit" value="Exit" onclick="javascript:return onExit();"/>
                    <input type="button" value="Print"
                           onclick="javascript:return onPrint();"/></td>
            </tr>
        </table>

        <table width="100%">
            <tr>
                <td class="title" colspan="2" align="center">PBSG CHART AID -
                    WOMEN'S HEALTH
                </td>
            </tr>
            <tr>
                <td class="title" colspan="2" align="center">PERI - POST -
                    MENOPAUSAL YEARS
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <table class="TableWithBorder" width="100%">
                        <tr>
                            <td width="50%">Name: <input type="hidden" name="pName"
                                                         value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("pName", "")))%>"/> <%=Encode.forHtml(String.valueOf(props.getProperty("pName", "")))%>
                            </td>
                            <td width="25%">Age at Menopause: <input type="text"
                                                                     name="ageMenopause" size="3"
                                                                     value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ageMenopause", "")))%>"/>
                            </td>
                            <td>Present Age: <input type="hidden" name="age"
                                                    value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("age", "")))%>"/> <%=Encode.forHtml(String.valueOf(props.getProperty("age", "")))%>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr class="TableWithBorder">
                <td colspan="2">Please complete side one and bring form to your
                    next appointment
                </td>
            </tr>
            <tr>
                <td width="50%">
                    <table class="history" cellspacing="0" width="100%" border="1">
                        <tr>
                            <td><a>OSTEOPOROSIS RISK FACTORS</a></td>
                            <td width="10%"><a>YES</a></td>
                            <td width="10%"><a>NO</a></td>
                        </tr>
                        <tr>
                            <td>Early Menopause (under 45)</td>
                            <td><input type="checkbox" name="orf_emYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_emYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_emNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_emNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Family History of Osteoporosis</td>
                            <td><input type="checkbox" name="orf_fhoYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_fhoYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_fhoNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_fhoNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Family History of Osteoporotic Fractures</td>
                            <td><input type="checkbox" name="orf_fhofYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_fhofYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_fhofNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_fhofNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Loss of Height</td>
                            <td><input type="checkbox" name="orf_lhYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_lhYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_lhNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_lhNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Personal History of Fractures</td>
                            <td><input type="checkbox" name="orf_phfYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_phfYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_phfNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_phfNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>White or Asian Race</td>
                            <td><input type="checkbox" name="orf_warYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_warYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_warNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_warNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Thin and Small Boned</td>
                            <td><input type="checkbox" name="orf_tsbYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_tsbYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_tsbNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_tsbNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>History of Irregular Periods or Infrequent Periods or
                                Eating Disorders
                            </td>
                            <td><input type="checkbox" name="orf_hipYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_hipYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_hipNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_hipNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Infrequent Exercise (less than 30 min/day)</td>
                            <td><input type="checkbox" name="orf_ieYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_ieYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_ieNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_ieNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Lifelong Low Calcium Intake (less than 400 mg/day) = (2
                                8oz glasses of milk per day)
                            </td>
                            <td><input type="checkbox" name="orf_llciYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_llciYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_llciNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_llciNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Cigarette Smoking</td>
                            <td><input type="checkbox" name="orf_csYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_csYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_csNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_csNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Caffeine (more than 3 cups of coffee/day)</td>
                            <td><input type="checkbox" name="orf_cYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_cYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_cNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_cNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Alcohol (consistently more than 2 drinks/day)</td>
                            <td><input type="checkbox" name="orf_aYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_aYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_aNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_aNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Current or Previous Thyroid Disorder</td>
                            <td><input type="checkbox" name="orf_cptdYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_cptdYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_cptdNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_cptdNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Drugs: Corticosteroids</td>
                            <td><input type="checkbox" name="orf_dcYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_dcYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_dcNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_dcNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Anti-seizure
                                Drugs
                            </td>
                            <td><input type="checkbox" name="orf_adYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_adYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_adNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_adNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Thyroid
                                Medications
                            </td>
                            <td><input type="checkbox" name="orf_tmYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_tmYes", "")))%> /></td>
                            <td><input type="checkbox" name="orf_tmNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("orf_tmNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td colspan="3"><textarea name="orf_comments"
                                                      style="height: 72px;"><%=Encode.forHtml(String.valueOf(props.getProperty("orf_comments", "")))%></textarea>
                            </td>
                        </tr>
                        <tr>
                            <td><a>CURRENT SYMPTOMS</a></td>
                            <td><a>YES</a></td>
                            <td><a>NO</a></td>
                        </tr>
                        <tr>
                            <td>Mood Changes</td>
                            <td><input type="checkbox" name="cs_mcYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_mcYes", "")))%> /></td>
                            <td><input type="checkbox" name="cs_mcNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_mcNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Memory Problems</td>
                            <td><input type="checkbox" name="cs_mpYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_mpYes", "")))%> /></td>
                            <td><input type="checkbox" name="cs_mpNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_mpNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Hot Flashes</td>
                            <td><input type="checkbox" name="cs_hfYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_hfYes", "")))%> /></td>
                            <td><input type="checkbox" name="cs_hfNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_hfNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Vaginal Dryness</td>
                            <td><input type="checkbox" name="cs_vdYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_vdYes", "")))%> /></td>
                            <td><input type="checkbox" name="cs_vdNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_vdNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Discomfort with Sexual Intercourse</td>
                            <td><input type="checkbox" name="cs_dsiYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_dsiYes", "")))%> /></td>
                            <td><input type="checkbox" name="cs_dsiNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_dsiNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Loss of Interest in Sexual Activity</td>
                            <td><input type="checkbox" name="cs_lisaYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_lisaYes", "")))%> /></td>
                            <td><input type="checkbox" name="cs_lisaNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_lisaNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Loss of Bladder Control</td>
                            <td><input type="checkbox" name="cs_lbcYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_lbcYes", "")))%> /></td>
                            <td><input type="checkbox" name="cs_lbcNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_lbcNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>History of Bladder Infections</td>
                            <td><input type="checkbox" name="cs_hbiYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_hbiYes", "")))%> /></td>
                            <td><input type="checkbox" name="cs_hbiNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("cs_hbiNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td colspan="3"><textarea name="cs_comments"
                                                      style="height: 73px;"><%=Encode.forHtml(String.valueOf(props.getProperty("cs_comments", "")))%></textarea>
                            </td>
                        </tr>
                    </table>
                </td>
                <td width="50%">
                    <table class="history" width="100%" cellspacing="0" border="1">
                        <tr>
                            <td><a>CARDIOVASCULAR RISK FACTORS</a></td>
                            <td width="10%"><a>YES</a></td>
                            <td width="10%"><a>NO</a></td>
                        </tr>
                        <tr>
                            <td>Family History of Heart Disease</td>
                            <td><input type="checkbox" name="crf_fhhdYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_fhhdYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_fhhdNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_fhhdNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>History of Angina</td>
                            <td><input type="checkbox" name="crf_haYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_haYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_haNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_haNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>History of Congestive Heart Failure</td>
                            <td><input type="checkbox" name="crf_hchfYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_hchfYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_hchfNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_hchfNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>History of Heart Attack</td>
                            <td><input type="checkbox" name="crf_hhaYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_hhaYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_hhaNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_hhaNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>History of Diabetes</td>
                            <td><input type="checkbox" name="crf_hdYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_hdYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_hdNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_hdNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Cigarette Smoking</td>
                            <td><input type="checkbox" name="crf_csYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_csYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_csNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_csNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>High Blood Pressure</td>
                            <td><input type="checkbox" name="crf_hbpYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_hbpYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_hbpNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_hbpNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Low HDL Cholesterol (if known)</td>
                            <td><input type="checkbox" name="crf_lhcYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_lhcYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_lhcNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_lhcNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>High Triglycerides (if known)</td>
                            <td><input type="checkbox" name="crf_htYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_htYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_htNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_htNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>High LDL Cholesterol (if known)</td>
                            <td><input type="checkbox" name="crf_hlcYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_hlcYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_hlcNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_hlcNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Overweight</td>
                            <td><input type="checkbox" name="crf_oYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_oYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_oNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_oNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Sedentary Lifestyle</td>
                            <td><input type="checkbox" name="crf_slYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_slYes", "")))%> /></td>
                            <td><input type="checkbox" name="crf_slNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("crf_slNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td colspan="3"><textarea
                                    name="crf_comments"><%=Encode.forHtml(String.valueOf(props.getProperty("crf_comments", "")))%></textarea>
                            </td>
                        </tr>
                        <tr>
                            <td><a>RELEVANT HISTORY</a></td>
                            <td><a>YES</a></td>
                            <td><a>NO</a></td>
                        </tr>
                        <tr>
                            <td>Family History of Breast Cancer</td>
                            <td><input type="checkbox" name="rh_fhbcYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_fhbcYes", "")))%> /></td>
                            <td><input type="checkbox" name="rh_fhbcNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_fhbcNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Personal History of Breast Cancer</td>
                            <td><input type="checkbox" name="rh_phbcYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_phbcYes", "")))%> /></td>
                            <td><input type="checkbox" name="rh_phbcNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_phbcNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Personal History of Other Cancer</td>
                            <td><input type="checkbox" name="rh_phocYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_phocYes", "")))%> /></td>
                            <td><input type="checkbox" name="rh_phocNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_phocNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Hysterectomy &nbsp;&nbsp;&nbsp;Age: <input type="text"
                                                                           name="ageHysterectomy" size="2"
                                                                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ageHysterectomy", "")))%>"/>
                            </td>
                            <td><input type="checkbox" name="rh_hYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_hYes", "")))%> /></td>
                            <td><input type="checkbox" name="rh_hNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_hNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; With Removal of
                                Ovaries
                            </td>
                            <td><input type="checkbox" name="rh_hwroYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_hwroYes", "")))%> /></td>
                            <td><input type="checkbox" name="rh_hwroNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_hwroNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>History of Pain or Cysts in Breasts</td>
                            <td><input type="checkbox" name="rh_hpcbYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_hpcbYes", "")))%> /></td>
                            <td><input type="checkbox" name="rh_hpcbNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_hpcbNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Family History of Alzheimers Disease</td>
                            <td><input type="checkbox" name="rh_fhadYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_fhadYes", "")))%> /></td>
                            <td><input type="checkbox" name="rh_fhadNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_fhadNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Family History of Colon Cancer</td>
                            <td><input type="checkbox" name="rh_fhccYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_fhccYes", "")))%> /></td>
                            <td><input type="checkbox" name="rh_fhccNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_fhccNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Other <input type="text" name="rh_other"
                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("rh_other", "")))%>"/></td>
                            <td><input type="checkbox" name="rh_oYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_oYes", "")))%> /></td>
                            <td><input type="checkbox" name="rh_oNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("rh_oNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td colspan="3"><textarea
                                    name="rh_comments"><%=Encode.forHtml(String.valueOf(props.getProperty("rh_comments", "")))%></textarea>
                            </td>
                        </tr>
                        <tr>
                            <td><a>CURRENT MEDICATIONS</a></td>
                            <td colspan="2"><a>DOSAGE</a></td>
                        </tr>
                        <tr>
                            <td>Calcium Supplement</td>
                            <td colspan="2"><input type="text" style="width: 100%;"
                                                   name="cm_cs" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cm_cs", "")))%>"/></td>
                        </tr>
                        <tr>
                            <td>Vitamin D Supplement</td>
                            <td colspan="2"><input type="text" style="width: 100%;"
                                                   name="cm_vds" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cm_vds", "")))%>"/></td>
                        </tr>
                        <tr>
                            <td><input type="text" name="cm_other1" size="50"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cm_other1", "")))%>"/></td>
                            <td colspan="2"><input type="text" style="width: 100%;"
                                                   name="cm_o1" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cm_o1", "")))%>"/></td>
                        </tr>
                        <tr>
                            <td><input type="text" name="cm_other2" size="50"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cm_other2", "")))%>"/></td>
                            <td colspan="2"><input type="text" style="width: 100%;"
                                                   name="cm_o2" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("cm_o2", "")))%>"/></td>
                        </tr>
                        <tr>
                            <td colspan="3"><textarea
                                    name="cm_comments"><%=Encode.forHtml(String.valueOf(props.getProperty("cm_comments", "")))%></textarea>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <table class="history" width="100%" cellspacing="0" border="1">
                        <tr>
                            <td width="40%">&nbsp;</td>
                            <td width="5%"><a>YES</a></td>
                            <td width="5%"><a>NO</a></td>
                            <td width="15%">&nbsp;</td>
                            <td width="5%"><a>YES</a></td>
                            <td width="5%"><a>NO</a></td>
                            <td width="15%">&nbsp;</td>
                            <td width="5%"><a>YES</a></td>
                            <td width="5%"><a>NO</a></td>
                        </tr>
                        <tr>
                            <td>Are you presently taking Hormone Replacement Therapy?</td>
                            <td><input type="checkbox" name="phrtYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("phrtYes", "")))%> /></td>
                            <td><input type="checkbox" name="phrtNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("phrtNo", "")))%> /></td>
                            <td>Estrogen</td>
                            <td><input type="checkbox" name="estrogenYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("estrogenYes", "")))%> /></td>
                            <td><input type="checkbox" name="estrogenNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("estrogenNo", "")))%> /></td>
                            <td>Progesterone</td>
                            <td><input type="checkbox" name="progesteroneYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("progesteroneYes", "")))%> /></td>
                            <td><input type="checkbox" name="progesteroneNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("progesteroneNo", "")))%> /></td>
                        </tr>
                        <tr>
                            <td>Have you ever taken Hormone Replacement Therapy?</td>
                            <td><input type="checkbox" name="hrtYes"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("hrtYes", "")))%> /></td>
                            <td><input type="checkbox" name="hrtNo"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("hrtNo", "")))%> /></td>
                            <td colspan="6">If yes, when? <input type="text"
                                                                 style="width: 75%;" name="whenHrt"
                                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("whenHrt", "")))%>"/></td>
                        </tr>
                        <tr>
                            <td colspan="9">Reason Discontinued<br>
                                <textarea name="reasonDiscontinued"
                                          style="width: 100%; height: 75px;"><%=Encode.forHtml(String.valueOf(props.getProperty("reasonDiscontinued", "")))%></textarea>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>

            <tr>
                <td class="title" colspan="2" align="center">HEALTH MAINTENANCE
                    FLOW SHEET
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <table class="flowchart" width="100%" cellspacing="0" border="1"
                           cellspacing="0">
                        <tr>
                            <td width="14%">Date</td>
                            <td width="9%"><input type="text" name="date1"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("date1", "")))%>"/></td>
                            <td width="9%"><input type="text" name="date2"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("date2", "")))%>"/></td>
                            <td width="9%"><input type="text" name="date3"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("date3", "")))%>"/></td>
                            <td width="9%"><input type="text" name="date4"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("date4", "")))%>"/></td>
                            <td width="14%">&nbsp;</td>
                            <td width="9%"><input type="text" name="date5"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("date5", "")))%>"/></td>
                            <td width="9%"><input type="text" name="date6"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("date6", "")))%>"/></td>
                            <td width="9%"><input type="text" name="date7"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("date7", "")))%>"/></td>
                            <td width="9%"><input type="text" name="date8"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("date8", "")))%>"/></td>
                        </tr>
                        <tr>
                            <td>ETOH USE - # drinks/day</td>
                            <td><textarea name="etohUse1"><%=Encode.forHtml(String.valueOf(props.getProperty("etohUse1", "")))%></textarea></td>
                            <td><textarea name="etohUse2"><%=Encode.forHtml(String.valueOf(props.getProperty("etohUse2", "")))%></textarea></td>
                            <td><textarea name="etohUse3"><%=Encode.forHtml(String.valueOf(props.getProperty("etohUse3", "")))%></textarea></td>
                            <td><textarea name="etohUse4"><%=Encode.forHtml(String.valueOf(props.getProperty("etohUse4", "")))%></textarea></td>
                            <td>Smoking Cessation - # pack/yrs</td>
                            <td><textarea
                                    name="smokingCessation1"><%=Encode.forHtml(String.valueOf(props.getProperty("smokingCessation1", "")))%></textarea>
                            </td>
                            <td><textarea
                                    name="smokingCessation2"><%=Encode.forHtml(String.valueOf(props.getProperty("smokingCessation2", "")))%></textarea>
                            </td>
                            <td><textarea
                                    name="smokingCessation3"><%=Encode.forHtml(String.valueOf(props.getProperty("smokingCessation3", "")))%></textarea>
                            </td>
                            <td><textarea
                                    name="smokingCessation4"><%=Encode.forHtml(String.valueOf(props.getProperty("smokingCessation4", "")))%></textarea>
                            </td>
                        </tr>
                        <tr>
                            <td>Exercise - Type/Frequency</td>
                            <td><textarea name="exercise1"><%=Encode.forHtml(String.valueOf(props.getProperty("exercise1", "")))%></textarea></td>
                            <td><textarea name="exercise2"><%=Encode.forHtml(String.valueOf(props.getProperty("exercise2", "")))%></textarea></td>
                            <td><textarea name="exercise3"><%=Encode.forHtml(String.valueOf(props.getProperty("exercise3", "")))%></textarea></td>
                            <td><textarea name="exercise4"><%=Encode.forHtml(String.valueOf(props.getProperty("exercise4", "")))%></textarea></td>
                            <td>Vision 65+</td>
                            <td><textarea name="vision1"><%=Encode.forHtml(String.valueOf(props.getProperty("vision1", "")))%></textarea></td>
                            <td><textarea name="vision2"><%=Encode.forHtml(String.valueOf(props.getProperty("vision2", "")))%></textarea></td>
                            <td><textarea name="vision3"><%=Encode.forHtml(String.valueOf(props.getProperty("vision3", "")))%></textarea></td>
                            <td><textarea name="vision4"><%=Encode.forHtml(String.valueOf(props.getProperty("vision4", "")))%></textarea></td>
                        </tr>
                        <tr>
                            <td>Low Fat / High Fibre Diet</td>
                            <td><textarea name="lowFat1"><%=Encode.forHtml(String.valueOf(props.getProperty("lowFat1", "")))%></textarea></td>
                            <td><textarea name="lowFat2"><%=Encode.forHtml(String.valueOf(props.getProperty("lowFat2", "")))%></textarea></td>
                            <td><textarea name="lowFat3"><%=Encode.forHtml(String.valueOf(props.getProperty("lowFat3", "")))%></textarea></td>
                            <td><textarea name="lowFat4"><%=Encode.forHtml(String.valueOf(props.getProperty("lowFat4", "")))%></textarea></td>
                            <td>TD Last</td>
                            <td><textarea name="tdLast1"><%=Encode.forHtml(String.valueOf(props.getProperty("tdLast1", "")))%></textarea></td>
                            <td><textarea name="tdLast2"><%=Encode.forHtml(String.valueOf(props.getProperty("tdLast2", "")))%></textarea></td>
                            <td><textarea name="tdLast3"><%=Encode.forHtml(String.valueOf(props.getProperty("tdLast3", "")))%></textarea></td>
                            <td><textarea name="tdLast4"><%=Encode.forHtml(String.valueOf(props.getProperty("tdLast4", "")))%></textarea></td>
                        </tr>
                        <tr>
                            <td>Calcium 1000 - 1500 mg/day</td>
                            <td><textarea name="calcium1"><%=Encode.forHtml(String.valueOf(props.getProperty("calcium1", "")))%></textarea></td>
                            <td><textarea name="calcium2"><%=Encode.forHtml(String.valueOf(props.getProperty("calcium2", "")))%></textarea></td>
                            <td><textarea name="calcium3"><%=Encode.forHtml(String.valueOf(props.getProperty("calcium3", "")))%></textarea></td>
                            <td><textarea name="calcium4"><%=Encode.forHtml(String.valueOf(props.getProperty("calcium4", "")))%></textarea></td>
                            <td>Flu 65+</td>
                            <td><textarea name="flu1"><%=Encode.forHtml(String.valueOf(props.getProperty("flu1", "")))%></textarea></td>
                            <td><textarea name="flu2"><%=Encode.forHtml(String.valueOf(props.getProperty("flu2", "")))%></textarea></td>
                            <td><textarea name="flu3"><%=Encode.forHtml(String.valueOf(props.getProperty("flu3", "")))%></textarea></td>
                            <td><textarea name="flu4"><%=Encode.forHtml(String.valueOf(props.getProperty("flu4", "")))%></textarea></td>
                        </tr>
                        <tr>
                            <td>Vitamin D - 400 mg/day</td>
                            <td><textarea name="vitaminD1"><%=Encode.forHtml(String.valueOf(props.getProperty("vitaminD1", "")))%></textarea></td>
                            <td><textarea name="vitaminD2"><%=Encode.forHtml(String.valueOf(props.getProperty("vitaminD2", "")))%></textarea></td>
                            <td><textarea name="vitaminD3"><%=Encode.forHtml(String.valueOf(props.getProperty("vitaminD3", "")))%></textarea></td>
                            <td><textarea name="vitaminD4"><%=Encode.forHtml(String.valueOf(props.getProperty("vitaminD4", "")))%></textarea></td>
                            <td nowrap="true">Pneumovax - 65+<br>
                                Date: <input type="text" style="width: 100%;" name="pneumovaxDate"
                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("pneumovaxDate", "")))%>"/></td>
                            <td><textarea name="pneumovax1"><%=Encode.forHtml(String.valueOf(props.getProperty("pneumovax1", "")))%></textarea></td>
                            <td><textarea name="pneumovax2"><%=Encode.forHtml(String.valueOf(props.getProperty("pneumovax2", "")))%></textarea></td>
                            <td><textarea name="pneumovax3"><%=Encode.forHtml(String.valueOf(props.getProperty("pneumovax3", "")))%></textarea></td>
                            <td><textarea name="pneumovax4"><%=Encode.forHtml(String.valueOf(props.getProperty("pneumovax4", "")))%></textarea></td>
                        </tr>
                        <tr>
                            <td>Pap Smear</td>
                            <td><textarea name="papSmear1"><%=Encode.forHtml(String.valueOf(props.getProperty("papSmear1", "")))%></textarea></td>
                            <td><textarea name="papSmear2"><%=Encode.forHtml(String.valueOf(props.getProperty("papSmear2", "")))%></textarea></td>
                            <td><textarea name="papSmear3"><%=Encode.forHtml(String.valueOf(props.getProperty("papSmear3", "")))%></textarea></td>
                            <td><textarea name="papSmear4"><%=Encode.forHtml(String.valueOf(props.getProperty("papSmear4", "")))%></textarea></td>
                            <td>Height</td>
                            <td><textarea name="height1"><%=Encode.forHtml(String.valueOf(props.getProperty("height1", "")))%></textarea></td>
                            <td><textarea name="height2"><%=Encode.forHtml(String.valueOf(props.getProperty("height2", "")))%></textarea></td>
                            <td><textarea name="height3"><%=Encode.forHtml(String.valueOf(props.getProperty("height3", "")))%></textarea></td>
                            <td><textarea name="height4"><%=Encode.forHtml(String.valueOf(props.getProperty("height4", "")))%></textarea></td>
                        </tr>
                        <tr>
                            <td>Blood Pressure</td>
                            <td><textarea name="bloodPressure1"><%=Encode.forHtml(String.valueOf(props.getProperty("bloodPressure1", "")))%></textarea>
                            </td>
                            <td><textarea name="bloodPressure2"><%=Encode.forHtml(String.valueOf(props.getProperty("bloodPressure2", "")))%></textarea>
                            </td>
                            <td><textarea name="bloodPressure3"><%=Encode.forHtml(String.valueOf(props.getProperty("bloodPressure3", "")))%></textarea>
                            </td>
                            <td><textarea name="bloodPressure4"><%=Encode.forHtml(String.valueOf(props.getProperty("bloodPressure4", "")))%></textarea>
                            </td>
                            <td>Weight</td>
                            <td><textarea name="weight1"><%=Encode.forHtml(String.valueOf(props.getProperty("weight1", "")))%></textarea></td>
                            <td><textarea name="weight2"><%=Encode.forHtml(String.valueOf(props.getProperty("weight2", "")))%></textarea></td>
                            <td><textarea name="weight3"><%=Encode.forHtml(String.valueOf(props.getProperty("weight3", "")))%></textarea></td>
                            <td><textarea name="weight4"><%=Encode.forHtml(String.valueOf(props.getProperty("weight4", "")))%></textarea></td>
                        </tr>
                        <tr>
                            <td>Clinical Breast Exam</td>
                            <td><textarea name="cbe1"><%=Encode.forHtml(String.valueOf(props.getProperty("cbe1", "")))%></textarea></td>
                            <td><textarea name="cbe2"><%=Encode.forHtml(String.valueOf(props.getProperty("cbe2", "")))%></textarea></td>
                            <td><textarea name="cbe3"><%=Encode.forHtml(String.valueOf(props.getProperty("cbe3", "")))%></textarea></td>
                            <td><textarea name="cbe4"><%=Encode.forHtml(String.valueOf(props.getProperty("cbe4", "")))%></textarea></td>
                            <td>Bone Mineral Density (see guidelines)</td>
                            <td><textarea name="bmd1"><%=Encode.forHtml(String.valueOf(props.getProperty("bmd1", "")))%></textarea></td>
                            <td><textarea name="bmd2"><%=Encode.forHtml(String.valueOf(props.getProperty("bmd2", "")))%></textarea></td>
                            <td><textarea name="bmd3"><%=Encode.forHtml(String.valueOf(props.getProperty("bmd3", "")))%></textarea></td>
                            <td><textarea name="bmd4"><%=Encode.forHtml(String.valueOf(props.getProperty("bmd4", "")))%></textarea></td>
                        </tr>
                        <tr>
                            <td>Mammography (50 - 69)</td>
                            <td><textarea name="mammography1"><%=Encode.forHtml(String.valueOf(props.getProperty("mammography1", "")))%></textarea></td>
                            <td><textarea name="mammography2"><%=Encode.forHtml(String.valueOf(props.getProperty("mammography2", "")))%></textarea></td>
                            <td><textarea name="mammography3"><%=Encode.forHtml(String.valueOf(props.getProperty("mammography3", "")))%></textarea></td>
                            <td><textarea name="mammography4"><%=Encode.forHtml(String.valueOf(props.getProperty("mammography4", "")))%></textarea></td>
                            <td><input type="text" name="other1" style="width: 100%;"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("other1", "")))%>"/></td>
                            <td><textarea name="other11"><%=Encode.forHtml(String.valueOf(props.getProperty("other11", "")))%></textarea></td>
                            <td><textarea name="other12"><%=Encode.forHtml(String.valueOf(props.getProperty("other12", "")))%></textarea></td>
                            <td><textarea name="other13"><%=Encode.forHtml(String.valueOf(props.getProperty("other13", "")))%></textarea></td>
                            <td><textarea name="other14"><%=Encode.forHtml(String.valueOf(props.getProperty("other14", "")))%></textarea></td>
                        </tr>
                        <tr>
                            <td><input type="text" name="other2" style="width: 100%;"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("other2", "")))%>"/></td>
                            <td><textarea name="other21"><%=Encode.forHtml(String.valueOf(props.getProperty("other21", "")))%></textarea></td>
                            <td><textarea name="other22"><%=Encode.forHtml(String.valueOf(props.getProperty("other22", "")))%></textarea></td>
                            <td><textarea name="other23"><%=Encode.forHtml(String.valueOf(props.getProperty("other23", "")))%></textarea></td>
                            <td><textarea name="other24"><%=Encode.forHtml(String.valueOf(props.getProperty("other24", "")))%></textarea></td>
                            <td><input type="text" name="other3" style="width: 100%;"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("other3", "")))%>"/></td>
                            <td><textarea name="other31"><%=Encode.forHtml(String.valueOf(props.getProperty("other31", "")))%></textarea></td>
                            <td><textarea name="other32"><%=Encode.forHtml(String.valueOf(props.getProperty("other32", "")))%></textarea></td>
                            <td><textarea name="other33"><%=Encode.forHtml(String.valueOf(props.getProperty("other33", "")))%></textarea></td>
                            <td><textarea name="other34"><%=Encode.forHtml(String.valueOf(props.getProperty("other34", "")))%></textarea></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td align="center" colspan="2"><br>
                    <br>
                    <a class="title">Post Menopausal Health Maintenance - Selected
                        Guidelines</a><br>
                    (If not otherwise noted, all information from the Canadian Task Force
                    on the Periodic Health Exam)
                </td>
            </tr>
            <tr>
                <td style="border-right: 0;" width="40%">
                    <table width="100%" cellspacing="0" border="1" cellpadding="5">
                        <tr>
                            <td align="center"><b><i>SCREENING for BREAST CANCER</i></b></td>
                        </tr>
                        <tr>
                            <td>Age 50 - 69 mammography and clinical exam yearly</td>
                        </tr>
                        <tr>
                            <td>Age 40 - 49 screening not recommended</td>
                        </tr>
                        <tr>
                            <td>BSE value unclear (c)</td>
                        </tr>
                    </table>
                </td>
                <td style="border-left: 0;">
                    <table width="100%" cellspacing="0" border="1" cellpadding="5">
                        <tr>
                            <td align="center"><b><i>SCREENING for CERVICAL CANCER</i></b></td>
                        </tr>
                        <tr>
                            <td>
                                <li>Women of all ages who are, or have been, sexually active
                                    should be screened
                                </li>
                                <li>after 3 normal paps at 1 year intervals, screening can be
                                    every 2 years
                                </li>
                                <li>if there have been 4 normal paps in the previous 10 years,
                                    screening may be discontinued after age 70
                                </li>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <table width="100%" cellspacing="0" border="1">
                        <tr>
                            <td colspan="2">
                                <table width="100%">
                                    <tr>
                                        <td><b><i>OSTEOPOROSIS</i></b></td>
                                        <td>Definition:</td>
                                        <td>Osteopenic &gt; 1.0 - 2.5 Sds}<br>
                                            Osteopenic &gt; 2.5 Sds}
                                        </td>
                                        <td>Below the mean for young adults (Tscore) on bone mineral
                                            density (BMD)
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <li>Accurate measurements of height after age 35</li>
                                <li>BMD (bone mass density) NOT recommended as a mass
                                    screening tool
                                </li>
                                <br>
                                <b>CLINICAL INDICATIONS FOR BMD</b>: (CMAL 1996: 155(8):P1113)<br>
                                <br>
                                <li>if required for decision-making on HRT</li>
                                <li>fractures or evidence of osteopenia</li>
                                <li>long-term oral steroid use</li>
                                <li>hyperparathyroidism</li>
                                <li>strong family history</li>
                                <li>to monitor ongoing osteoporosis therapy</li>
                                <li>multiple risk factors</li>
                            </td>
                            <td><b>COUNSEL ON:</b><br>
                                <br>
                                1. Calcium: 1000 - 1500 mg/day<br>
                                <br>
                                2. Vitamin D:<br>
                                <span style="padding-left: 30px;">&gt;50yrs: 200 IU/day</span><br>
                                <span style="padding-left: 30px;">&gt;65yrs or Osteoporosis:
					400 - 800 IU/day</span><br>
                                <br>
                                3. Fall avoidance techniques<br>
                                <br>
                                4. Physical activity/weight bearing exercises
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <table width="100%" cellspacing="0" border="1">
                        <tr>
                            <td colspan="2"><b><i>HORMONE REPLACEMENT THERAPY
                                (CMAL 1996; 155(8):1130)</i></b></td>
                        </tr>
                        <tr>
                            <td><b>INDICATIONS</b><br>
                                <br>
                                1. Relief of symptoms<br>
                                2. Prevention of osteoporosis - (bone loss 3 - 5 times greater
                                after menopause if not on HRT)<br>
                                3. Cardiovascular protection - (may reduce risk of MI and stroke by
                                up to 50%)
                            </td>
                            <td><b>CONTRAINDICATIONS</b><br>
                                <br>
                                1. Undiagnosed vaginal bleeding<br>
                                2. Known or suspected breast or uterus cancer<br>
                                3. Acute liver disease, chronic impairment of liver
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <table width="100%" cellspacing="0" border="1">
                        <tr>
                            <td colspan="2"><b><i>CHOLESTEROL SCREENING</i></b></td>
                        </tr>
                        <tr>
                            <td>
                                <li>insufficient evidence to include or exclude in women</li>
                                <li>screen based on clinical judgement and other risk factors</li>
                            </td>
                            <td>
                                <li>average of 3 or more readings required to accurately
                                    reflect "true level"
                                <li>
                                <li>HRT can decrease LDL and increase HDL</li>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>

        <table class="Head" class="hidePrint">
            <tr>
                <td align="left"><input type="submit" value="Save"
                                        onclick="javascript:return onSave();"/> <input type="submit"
                                                                                       value="Save and Exit"
                                                                                       onclick="javascript:return onSaveExit();"/>
                    <input
                            type="submit" value="Exit" onclick="javascript:return onExit();"/>
                    <input type="button" value="Print"
                           onclick="javascript:return onPrint();"/></td>
            </tr>
        </table>

    </form>
    </body>
</html>
