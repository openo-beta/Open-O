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
    String formClass = "LateLifeFDIFunction";
    String formLink = "formlatelifeFDIfunction.jsp";

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
        <title>Late Life FDI: Function component</title>
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
        <link rel="stylesheet" type="text/css" media="all" href="<%= request.getContextPath() %>/share/css/extractedFromPages.css"/>
    </head>


    <script type="text/javascript" language="Javascript">
        var choiceFormat = new Array(6, 10, 11, 15, 16, 20, 21, 25, 26, 30, 31, 35, 36, 40, 41, 45,
            46, 50, 51, 55, 56, 60, 61, 65, 66, 70, 71, 75, 76, 80, 81, 85,
            86, 90, 91, 95, 96, 100, 101, 105, 106, 110, 111, 115, 116, 120, 121, 125,
            126, 130, 131, 135, 136, 140, 141, 145, 146, 150, 151, 155, 156, 160, 161, 165,
            167, 171, 172, 176, 177, 181, 182, 186, 187, 191, 192, 196, 197, 201, 202, 206);
        var allNumericField = null;
        var allMatch = null;
        var action = "/<%=Encode.forJavaScript(String.valueOf(project_home))%>/form/formname.do";
        var totalScore = 0;

        function goToInstructions() {
            document.getElementById('instruction').style.display = 'block';
            document.getElementById('page1').style.display = 'none';
            document.getElementById('page2').style.display = 'none';
            document.getElementById('page3').style.display = 'none';
            document.getElementById('page4').style.display = 'none';
            document.getElementById('page5').style.display = 'none';
            document.getElementById('totalScore').style.display = 'none';
            document.getElementById('subject2').style.display = 'none';
            document.getElementById('functionBar').style.display = 'none';
            document.getElementById('copyRight').style.display = 'block';
        }

        function goToVisualAid1() {
            var vheight = 768;
            var vwidth = 640;
            var page = "form/formlatelifefunctionvisualAid1.jsp";
            windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0";
            window.open(page, "", windowprops);
        }

        function goToVisualAid2() {
            var vheight = 768;
            var vwidth = 640;
            var windowprops = "height=768,width=600,location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=600,screenY=200,top=0,left=0";
            window.open("form/formlatelifefunctionvisualAid2.jsp", "", windowprops);
        }

        function goToPage1() {
            document.getElementById('instruction').style.display = 'none';
            document.getElementById('page1').style.display = 'block';
            document.getElementById('page2').style.display = 'none';
            document.getElementById('page3').style.display = 'none';
            document.getElementById('page4').style.display = 'none';
            document.getElementById('page5').style.display = 'none';
            document.getElementById('totalScore').style.display = 'none';
            document.getElementById('subject2').style.display = 'none';
            document.getElementById('functionBar').style.display = 'block';
            document.getElementById('copyRight').style.display = 'none';
        }

        function backToPage1() {
            document.getElementById('instruction').style.display = 'none';
            document.getElementById('page1').style.display = 'block';
            document.getElementById('page2').style.display = 'none';
            document.getElementById('page3').style.display = 'none';
            document.getElementById('page4').style.display = 'none';
            document.getElementById('page5').style.display = 'none';
            document.getElementById('totalScore').style.display = 'none';
            document.getElementById('subject2').style.display = 'none';
            document.getElementById('functionBar').style.display = 'block';
            document.getElementById('copyRight').style.display = 'none';
        }

        function goToPage2() {
            var checkboxes = new Array(6, 10, 11, 15, 16, 20, 21, 25, 26, 30, 31, 35, 36, 40, 41, 45);
            if (is1CheckboxChecked(0, checkboxes) == true && isFormCompleted(6, 45, 8, 0) == true) {
                document.getElementById('instruction').style.display = 'none';
                document.getElementById('page1').style.display = 'none';
                document.getElementById('page2').style.display = 'block';
                document.getElementById('page3').style.display = 'none';
                document.getElementById('page4').style.display = 'none';
                document.getElementById('page5').style.display = 'none';
                document.getElementById('totalScore').style.display = 'none';
                document.getElementById('subject2').style.display = 'none';
                document.getElementById('functionBar').style.display = 'block';
                document.getElementById('copyRight').style.display = 'none';
            }
        }

        function backToPage2() {
            document.getElementById('instruction').style.display = 'none';
            document.getElementById('page1').style.display = 'none';
            document.getElementById('page2').style.display = 'block';
            document.getElementById('page3').style.display = 'none';
            document.getElementById('page4').style.display = 'none';
            document.getElementById('page5').style.display = 'none';
            document.getElementById('totalScore').style.display = 'none';
            document.getElementById('subject2').style.display = 'none';
            document.getElementById('functionBar').style.display = 'block';
            document.getElementById('copyRight').style.display = 'none';
        }

        function goToPage3() {
            var checkboxes = new Array(46, 50, 51, 55, 56, 60, 61, 65, 66, 70, 71, 75, 76, 80, 81, 85);
            if (is1CheckboxChecked(0, checkboxes) == true && isFormCompleted(46, 85, 8, 0) == true) {
                document.getElementById('instruction').style.display = 'none';
                document.getElementById('page1').style.display = 'none';
                document.getElementById('page2').style.display = 'none';
                document.getElementById('page3').style.display = 'block';
                document.getElementById('page4').style.display = 'none';
                document.getElementById('page5').style.display = 'none';
                document.getElementById('totalScore').style.display = 'none';
                document.getElementById('subject2').style.display = 'none';
                document.getElementById('functionBar').style.display = 'block';
                document.getElementById('copyRight').style.display = 'none';
            }
        }

        function backToPage3() {
            document.getElementById('instruction').style.display = 'none';
            document.getElementById('page1').style.display = 'none';
            document.getElementById('page2').style.display = 'none';
            document.getElementById('page3').style.display = 'block';
            document.getElementById('page4').style.display = 'none';
            document.getElementById('page5').style.display = 'none';
            document.getElementById('totalScore').style.display = 'none';
            document.getElementById('subject2').style.display = 'none';
            document.getElementById('functionBar').style.display = 'block';
            document.getElementById('copyRight').style.display = 'none';
        }

        function goToPage4() {
            var checkboxes = new Array(86, 90, 91, 95, 96, 100, 101, 105, 106, 110, 111, 115, 116, 120, 121, 125);
            if (is1CheckboxChecked(0, checkboxes) == true && isFormCompleted(86, 125, 8, 0) == true) {
                document.getElementById('instruction').style.display = 'none';
                //document.getElementById('visualAid1').style.display = 'none';
                //document.getElementById('visualAid2').style.display = 'none';
                document.getElementById('page1').style.display = 'none';
                document.getElementById('page2').style.display = 'none';
                document.getElementById('page3').style.display = 'none';
                document.getElementById('page4').style.display = 'block';
                document.getElementById('page5').style.display = 'none';
                document.getElementById('totalScore').style.display = 'none';
                document.getElementById('subject2').style.display = 'none';
                document.getElementById('functionBar').style.display = 'block';
                document.getElementById('copyRight').style.display = 'none';
            }
        }

        function backToPage4() {
            document.getElementById('instruction').style.display = 'none';
            //document.getElementById('visualAid1').style.display = 'none';
            //document.getElementById('visualAid2').style.display = 'none';
            document.getElementById('page1').style.display = 'none';
            document.getElementById('page2').style.display = 'none';
            document.getElementById('page3').style.display = 'none';
            document.getElementById('page4').style.display = 'block';
            document.getElementById('page5').style.display = 'none';
            document.getElementById('totalScore').style.display = 'none';
            document.getElementById('subject2').style.display = 'none';
            document.getElementById('functionBar').style.display = 'block';
            document.getElementById('copyRight').style.display = 'none';
        }

        function goToPage5() {
            var checkboxes = new Array(126, 130, 131, 135, 136, 140, 141, 145, 146, 150, 151, 155, 156, 160, 161, 165);
            if (is1CheckboxChecked(0, checkboxes) == true) {
                document.getElementById('instruction').style.display = 'none';
                //document.getElementById('visualAid1').style.display = 'none';
                //document.getElementById('visualAid2').style.display = 'none';
                document.getElementById('page1').style.display = 'none';
                document.getElementById('page2').style.display = 'none';
                document.getElementById('page3').style.display = 'none';
                document.getElementById('page4').style.display = 'none';
                document.getElementById('page5').style.display = 'block';
                document.getElementById('totalScore').style.display = 'none';
                //document.getElementById('subject2').style.display = 'block';
                document.getElementById('functionBar').style.display = 'block';
                document.getElementById('copyRight').style.display = 'none';
            }
        }

        function backToPage5() {
            document.getElementById('instruction').style.display = 'none';
            //document.getElementById('visualAid1').style.display = 'none';
            //document.getElementById('visualAid2').style.display = 'none';
            document.getElementById('page1').style.display = 'none';
            document.getElementById('page2').style.display = 'none';
            document.getElementById('page3').style.display = 'none';
            document.getElementById('page4').style.display = 'none';
            document.getElementById('page5').style.display = 'block';
            document.getElementById('totalScore').style.display = 'none';
            //document.getElementById('subject2').style.display = 'block';
            document.getElementById('functionBar').style.display = 'block';
            document.getElementById('copyRight').style.display = 'none';
        }

        function goToScorePage() {
            var checkboxes = new Array(126, 130, 131, 135, 136, 140, 141, 145, 146, 150, 151, 155, 156, 160, 161, 165);
            if (is1CheckboxChecked(0, checkboxes) == true && isFormCompleted(126, 165, 8, 0) == true) {
                document.getElementById('instruction').style.display = 'none';
                //document.getElementById('visualAid1').style.display = 'none';
                //document.getElementById('visualAid2').style.display = 'none';
                document.getElementById('page1').style.display = 'none';
                document.getElementById('page2').style.display = 'none';
                document.getElementById('page3').style.display = 'none';
                document.getElementById('page4').style.display = 'none';
                document.getElementById('page5').style.display = 'none';
                document.getElementById('totalScore').style.display = 'block';
                //document.getElementById('subject2').style.display = 'block';
                document.getElementById('functionBar').style.display = 'block';
                document.getElementById('copyRight').style.display = 'none';
            }
        }

        function checkBeforeSave() {
            if (document.getElementById('page5').style.display == 'block') {
                if (isFormCompleted(166, 205, 8, 0) == true)
                    return true;
            } else {
                if (isFormCompleted(6, 45, 8, 0) == true && isFormCompleted(46, 85, 8, 0) == true && isFormCompleted(86, 125, 8, 0) == true && isFormCompleted(126, 165, 8, 0) == true && isFormCompleted(166, 205, 8, 0) == true)
                    return true;
            }

            return false;
        }

        function showSubtitle() {
            if (document.getElementById('questionnaire').style.display == 'block')
                document.getElementById('questionnaire').style.display = 'none';
            else
                document.getElementById('questionnaire').style.display = 'block';
        }

        function calculateScore() {
            var nbElements = document.forms[0].elements.length - 45;
            var element;
            var score = 0;
            for (var i = 6; i < nbElements; i++) {
                element = document.forms[0].elements[i]
                if (element.checked == true) {
                    if (element.name.match("None") == "None")
                        score = score + 5;
                    else if (element.name.match("ALittle") == "ALittle")
                        score = score + 4;
                    else if (element.name.match("Some") == "Some")
                        score = score + 3;
                    else if (element.name.match("ALot") == "ALot")
                        score = score + 2;
                    else if (element.name.match("Cannot") == "Cannot")
                        score = score + 1;
                }
            }
            document.forms[0].score.value = score;
            goToScorePage();
        }
    </script>
    <script type="text/javascript" src="form/formScripts.js">
    </script>


    <body bgproperties="fixed" topmargin="0" leftmargin="0" rightmargin="0"
          onload="window.resizeTo(768,732)">
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

        <table border="0" cellspacing="1" cellpadding="0" width="735px"
               height="95%">
            <tr>
                <td valign="top" colspan="2">
                    <table border="0" cellspacing="0" cellpadding="0" width="735px"
                           height="10%">
                        <tr>
                            <th class="lefttopCell" width="17%">&nbsp;</th>
                            <th class="subject">Late Life FDI: Function Component <br>
                                <table style="display: none" id="subject2">
                                    <tr>
                                        <th class="subject" style="border: 0px">For those who use
                                            walking devices <br>
                                            <font style="font-size: 60%"> The following are questions
                                                only for people using canes, walkers, or other walking devices </font>.

                </td>
            </tr>
        </table>
        </th>
        </tr>
        </table>
        </td>
        </tr>
        <tr>
            <td bgcolor="#A9A9A9" valign="top" align="center" width="17%"
                style="border-right: 2px solid #A9A9A9;">
                <table>
                    <tr>
                        <td class="leftcol"><a href="javascript: goToInstructions();">Instructions</a></td>
                    </tr>
                    <tr>
                        <td></td>
                    </tr>
                    <tr>
                        <td class="leftcol"><a href="javascript: goToVisualAid1();"
                                               title="for core questions">Visual Aid 1</a></td>
                    </tr>
                    <tr>
                        <td></td>
                    </tr>
                    <tr>
                        <td class="leftcol"><a href="javascript: goToVisualAid2();"
                                               title="for additional device questions">Visual Aid 2</a></td>
                    </tr>
                    <tr>
                        <td></td>
                    </tr>
                    <tr>
                        <td class="leftcol"><a href="javascript: showSubtitle();">Questionnaire</a></td>
                    </tr>
                    <tr>
                        <td>
                            <table width="100%" style="display: none" id="questionnaire">
                                <tr>
                                    <td width="10%"></td>
                                    <td><a href="javascript: goToPage1();"
                                           title="Core questions">&bull; <font style="font-size: 70%">Core</font></a>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="10%"></td>
                                    <td><a href="javascript: goToPage5();"
                                           title="Additional questions for users of assistive devices">&bull;
                                        <font style="font-size: 70%">Additional</font></a></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </td>
            <td width="83%">
                <table border="0" cellspacing="0" cellpadding="0" height="100%"
                       width="100%">
                    <tr>
                        <td valign="top">
                            <table border="0" cellspacing="0" cellpadding="0" height="85%"
                                   width="100%" id="instruction">
                                <tr>
                                    <td valign="top" colspan="2">
                                        <table width="100%" height="590px" border="0" cellspacing="0"
                                               cellpadding="0">
                                            <tr class="title">
                                                <th colspan="6">Instruction for Function Questions</th>
                                            </tr>
                                            <tr>
                                                <td>In this following section, I will ask you about your
                                                    ability to do specific activities as part of your daily
                                                    routines. I am interested in your <font
                                                            style="font-style: italic">sense of your ability</font> to
                                                    do
                                                    it on a typical day. It is not important that you actually do
                                                    the activity on a daily basis. In fact, I may mention some
                                                    activities that you do not do at all. You can still answer
                                                    these questions by assessing how difficult you <font
                                                            style="text-decoration: underline"> think</font> they would
                                                    be
                                                    for you to do on an average day. <br>
                                                    <br>
                                                    Factors that influence the level of difficulty you have may
                                                    include: pain, fatigue, fear, weakness, soreness, ailments,
                                                    health conditions, or disabilities. <br>
                                                    <br>
                                                    I want you to know how difficult the activity would be for you
                                                    to do <font style="text-decoration: underline"> without</font>
                                                    the help of someone else, and <font
                                                            style="text-decoration: underline">without</font> the use of
                                                    a
                                                    cane, walker or any other assistive walking device (or
                                                    wheelchair or scooter). <br>
                                                    <br>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="border: 1px solid #000000"><br>
                                                    <table>
                                                        <tr>
                                                            <td width="3%"></td>
                                                            <td><font style="font-weight: bold">Interviewer
                                                                personal note:</font> <br>
                                                                For the Function items, using fixed support is
                                                                acceptable
                                                                (e.g. holding onto furniture, walls), unless otherwise
                                                                specified in the item. <br>
                                                                <br>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td><br>
                                                    [Show visual aid to interviewee] <br>
                                                    <br>
                                                    <table>
                                                        <tr>
                                                            <td width="10%"></td>
                                                            <td colspan="2">Please choose from these answers:</td>
                                                        </tr>
                                                        <tr>
                                                            <td></td>
                                                            <td width="10%"></td>
                                                            <td><font style="font-weight: bold">None</font>
                                                        </tr>
                                                        <tr>
                                                            <td></td>
                                                            <td width="10%"></td>
                                                            <td><font style="font-weight: bold">A Little</font>
                                                        </tr>
                                                        <tr>
                                                            <td></td>
                                                            <td width="10%"></td>
                                                            <td><font style="font-weight: bold">Some</font>
                                                        </tr>
                                                        <tr>
                                                            <td></td>
                                                            <td width="10%"></td>
                                                            <td><font style="font-weight: bold">Quite a lot</font>
                                                        </tr>
                                                        <tr>
                                                            <td></td>
                                                            <td width="10%"></td>
                                                            <td><font style="font-weight: bold">Cannot do</font>
                                                        </tr>
                                                    </table>
                                                    <br>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>Let's begin...</td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            <table border="0" cellspacing="0" cellpadding="0"
                                   style="display: none" height="85%" width="100%" id="page1">
                                <tr>
                                    <td valign="top" colspan="2">
                                        <table width="100%" height="590px" border="0" cellspacing="1px"
                                               cellpadding="0">
                                            <tr class="title">
                                                <th colspan="7">
                                                    <table border="0" cellspacing="0" cellpadding="0">
                                                        <tr class="title">
                                                            <th>How much difficulty do you have...?</th>
                                                        </tr>
                                                        <tr class="title">
                                                            <th align="left"><font
                                                                    style="font-size: 65%; text-align: left;">(Remember
                                                                this is without the help of someone else and without the
                                                                use
                                                                of any assistive walking device.)</font>
                                                        </tr>
                                                </th>
                                        </table>
                                        </th>
                                </tr>
                                <tr class="title">
                                    <td colspan="2"></td>
                                    <td width="5%"><font style="font-size: 65%;">None</font></td>
                                    <td width="5%"><font style="font-size: 65%;">A
                                        little</font></td>
                                    <td width="5%"><font style="font-size: 65%;">Some</font></td>
                                    <td width="5%"><font style="font-size: 65%;">Quite
                                        a lot</font></td>
                                    <td width="5%"><font style="font-size: 65%;">Cannot
                                        do</font></td>
                                </tr>
                                <tr>
                                    <td class="question" valign="top" width="5%">F1.</td>
                                    <td class="question" valign="top" width="45%">Unscrewing
                                        the lid off a previously unopened jar without using any devices
                                    </td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F1None"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F1None", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F1ALittle"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F1ALittle", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F1Some"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F1Some", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F1ALot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F1ALot", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F1Cannot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F1Cannot", "")))%> /></td>
                                </tr>
                                <tr>
                                    <td class="question" valign="top" width="5%">F2.</td>
                                    <td class="question" valign="top" width="45%">Going up &
                                        down a flight of stairs inside, using a handrail
                                    </td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F2None"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F2None", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F2ALittle"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F2ALittle", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F2Some"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F2Some", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F2ALot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F2ALot", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F2Cannot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F2Cannot", "")))%> /></td>
                                </tr>
                                <tr>
                                    <td class="question" valign="top" width="5%">F3.</td>
                                    <td class="question" valign="top" width="45%">Putting on
                                        and taking off long pants (including managing fasteners)
                                    </td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F3None"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F3None", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F3ALittle"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F3ALittle", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F3Some"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F3Some", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F3ALot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F3ALot", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F3Cannot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F3Cannot", "")))%> /></td>
                                </tr>
                                <tr>
                                    <td class="question" valign="top" width="5%">F4.</td>
                                    <td class="question" valign="top" width="45%">Running 1/2
                                        mile or more
                                    </td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F4None"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F4None", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F4ALittle"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F4ALittle", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F4Some"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F4Some", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F4ALot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F4ALot", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F4Cannot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F4Cannot", "")))%> /></td>
                                </tr>
                                <tr>
                                    <td class="question" valign="top" width="5%">F5.</td>
                                    <td class="question" valign="top">Using common utensils
                                        for preparing meals (e.g., can opener, potato peeler, or sharp
                                        knife)
                                    </td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F5None"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F5None", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F5ALittle"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F5ALittle", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F5Some"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F5Some", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F5ALot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F5ALot", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F5Cannot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F5Cannot", "")))%> /></td>
                                </tr>
                                <tr>
                                    <td class="question" valign="top" width="5%">F6.</td>
                                    <td class="question" valign="top">Holding a full glass of
                                        water in one hand
                                    </td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F6None"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F6None", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F6ALittle"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F6ALittle", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F6Some"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F6Some", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F6ALot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F6ALot", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F6Cannot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F6Cannot", "")))%> /></td>
                                </tr>
                                <tr>
                                    <td class="question" valign="top" width="5%">F7.</td>
                                    <td class="question" valign="top">Walking a mile, taking
                                        rests as necessary
                                    </td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F7None"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F7None", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F7ALittle"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F7ALittle", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F7Some"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F7Some", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F7ALot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F7ALot", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F7Cannot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F7Cannot", "")))%> /></td>
                                </tr>
                                <tr>
                                    <td class="question" valign="top" width="5%">F8.</td>
                                    <td class="question" valign="top">Going up & down a flight
                                        of stairs outside, without using a handrail
                                    </td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F8None"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F8None", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F8ALittle"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F8ALittle", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F8Some"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F8Some", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F8ALot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F8ALot", "")))%> /></td>
                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                              class="checkbox" name="F8Cannot"
                                            <%=Encode.forHtml(String.valueOf(props.getProperty("F8Cannot", "")))%> /></td>
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
                <table border="0" cellspacing="0" cellpadding="0"
                       style="display: none" width="100%" height="85%" id="page2">
                    <tr>
                        <td valign="top" colspan="2">
                            <table width="100%" height="590px" border="0" cellspacing="1px"
                                   cellpadding="0">
                                <tr class="title">
                                    <th colspan="7">
                                        <table border="0" cellspacing="0" cellpadding="0">
                                            <tr class="title">
                                                <th>How much difficulty do you have...?</th>
                                            </tr>
                                            <tr class="title">
                                                <th align="left"><font
                                                        style="font-size: 65%; text-align: left;">(Remember
                                                    this is without the help of someone else and without the use
                                                    of any assistive walking device.)</font>
                                            </tr>
                                    </th>
                            </table>
                            </th>
                    </tr>
                    <tr class="title">
                        <td colspan="2"></td>
                        <td width="5%"><font style="font-size: 65%;">None</font></td>
                        <td width="5%"><font style="font-size: 65%;">A
                            little</font></td>
                        <td width="5%"><font style="font-size: 65%;">Some</font></td>
                        <td width="5%"><font style="font-size: 65%;">Quite
                            a lot</font></td>
                        <td width="5%"><font style="font-size: 65%;">Cannot
                            do</font></td>
                    </tr>
                    <tr>
                        <td class="question" valign="top" width="5%">F9.</td>
                        <td class="question" valign="top" width="45%">Running a
                            short distance, such as to catch a bus
                        </td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F9None"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F9None", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F9ALittle"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F9ALittle", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F9Some"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F9Some", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F9ALot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F9ALot", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F9Cannot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F9Cannot", "")))%> /></td>
                    </tr>
                    <tr>
                        <td class="question" valign="top" width="5%">F10.</td>
                        <td class="question" valign="top">Reaching overhead while
                            standing, as if to pull a light cord
                        </td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F10None"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F10None", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F10ALittle"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F10ALittle", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F10Some"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F10Some", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F10ALot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F10ALot", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F10Cannot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F10Cannot", "")))%> /></td>
                    </tr>
                    <tr>
                        <td class="question" valign="top" width="5%">F11.</td>
                        <td class="question" valign="top">Sitting down in and
                            standing up from a low, soft couch
                        </td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F11None"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F11None", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F11ALittle"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F11ALittle", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F11Some"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F11Some", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F11ALot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F11ALot", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F11Cannot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F11Cannot", "")))%> /></td>
                    </tr>
                    <tr>
                        <td class="question" valign="top" width="5%">F12.</td>
                        <td class="question" valign="top">Putting on and taking
                            off a coat or jacket
                        </td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F12None"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F12None", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F12ALittle"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F12ALittle", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F12Some"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F12Some", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F12ALot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F12ALot", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F12Cannot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F12Cannot", "")))%> /></td>
                    </tr>
                    <tr>
                        <td class="question" valign="top" width="5%">F13.</td>
                        <td class="question" valign="top">Reaching behind your
                            back as if to put a belt through a belt loop
                        </td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F13None"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F13None", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F13ALittle"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F13ALittle", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F13Some"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F13Some", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F13ALot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F13ALot", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F13Cannot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F13Cannot", "")))%> /></td>
                    </tr>
                    <tr>
                        <td class="question" valign="top" width="5%">F14.</td>
                        <td class="question" valign="top">Stepping up and down
                            from a curb
                        </td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F14None"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F14None", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F14ALittle"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F14ALittle", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F14Some"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F14Some", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F14ALot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F14ALot", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F14Cannot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F14Cannot", "")))%> /></td>
                    </tr>
                    <tr>
                        <td class="question" valign="top" width="5%">F15.</td>
                        <td class="question" valign="top">Opening a heavy, outside
                            door
                        </td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F15None"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F15None", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F15ALittle"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F15ALittle", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F15Some"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F15Some", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F15ALot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F15ALot", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F15Cannot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F15Cannot", "")))%> /></td>
                    </tr>
                    <tr>
                        <td class="question" valign="top" width="5%">F16.</td>
                        <td class="question" valign="top">Rip open a package of
                            snack food (e.g. cellophane wrapping on crackers) using only
                            your hands
                        </td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F16None"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F16None", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F16ALittle"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F16ALittle", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F16Some"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F16Some", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F16ALot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F16ALot", "")))%> /></td>
                        <td bgcolor="white" align="center"><input type="checkbox"
                                                                  class="checkbox" name="F16Cannot"
                                <%=Encode.forHtml(String.valueOf(props.getProperty("F16Cannot", "")))%> /></td>
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
        <table border="0" cellspacing="0" cellpadding="0"
               style="display: none" width="100%" height="85%" id="page3">
            <tr>
                <td valign="top" colspan="2">
                    <table width="100%" height="590px" border="0" cellspacing="1px"
                           cellpadding="0">
                        <tr class="title">
                            <th colspan="7">
                                <table border="0" cellspacing="0" cellpadding="0">
                                    <tr class="title">
                                        <th>How much difficulty do you have...?</th>
                                    </tr>
                                    <tr class="title">
                                        <th align="left"><font
                                                style="font-size: 65%; text-align: left;">(Remember
                                            this is without the help of someone else and without the use
                                            of any assistive walking device.)</font>
                                    </tr>
                            </th>
                    </table>
                    </th>
            </tr>
            <tr class="title">
                <td colspan="2"></td>
                <td width="5%"><font style="font-size: 65%;">None</font></td>
                <td width="5%"><font style="font-size: 65%;">A
                    little</font></td>
                <td width="5%"><font style="font-size: 65%;">Some</font></td>
                <td width="5%"><font style="font-size: 65%;">Quite
                    a lot</font></td>
                <td width="5%"><font style="font-size: 65%;">Cannot
                    do</font></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F17.</td>
                <td class="question" valign="top" width="45%">Pouring from
                    a large pitcher
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F17None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F17None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F17ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F17ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F17Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F17Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F17ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F17ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F17Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F17Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F18.</td>
                <td class="question" valign="top">Getting into and out of
                    a car/taxi (sedan)
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F18None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F18None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F18ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F18ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F18Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F18Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F18ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F18ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F18Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F18Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F19.</td>
                <td class="question" valign="top">Hiking a couple of miles
                    on uneven surfaces, including hills
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F19None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F19None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F19ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F19ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F19Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F19Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F19ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F19ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F19Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F19Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F20.</td>
                <td class="question" valign="top">Going up and down 3
                    flights of stairs inside, using a handrail
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F20None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F20None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F20ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F20ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F20Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F20Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F20ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F20ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F20Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F20Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F21.</td>
                <td class="question" valign="top">Picking up a kitchen
                    chair and moving it, in order to clean
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F21None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F21None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F21ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F21ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F21Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F21Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F21ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F21ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F21Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F21Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F22.</td>
                <td class="question" valign="top">Using a step stool to
                    reach into a high cabinet
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F22None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F22None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F22ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F22ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F22Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F22Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F22ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F22ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F22Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F22Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F23.</td>
                <td class="question" valign="top">Making a bed, including
                    spreading and tucking in bed sheets
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F23None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F23None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F23ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F23ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F23Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F23Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F23ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F23ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F23Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F23Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F24.</td>
                <td class="question" valign="top">Carrying something in
                    both arms while climbing a flight of stairs (e.g. laundry
                    basket)
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F24None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F24None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F24ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F24ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F24Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F24Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F24ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F24ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F24Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F24Cannot", "")))%> /></td>
            </tr>
        </table>
        </td>
        </tr>
        <tr class="subject">
            <td align="left"><a href="javascript: backToPage2();"><<
                Previous Page</a></td>
            <td align="right"><a href="javascript: goToPage4();">Next
                Page >></a></td>
        </tr>
        </table>
        <table border="0" cellspacing="0" cellpadding="0"
               style="display: none" width="100%" height="85%" id="page4">
            <tr>
                <td valign="top" colspan="2">
                    <table width="100%" height="590px" border="0" cellspacing="1px"
                           cellpadding="0">
                        <tr class="title">
                            <th colspan="7">
                                <table border="0" cellspacing="0" cellpadding="0">
                                    <tr class="title">
                                        <th>How much difficulty do you have...?</th>
                                    </tr>
                                    <tr class="title">
                                        <th align="left"><font
                                                style="font-size: 65%; text-align: left;">(Remember
                                            this is without the help of someone else and without the use
                                            of any assistive walking device.)</font>
                                    </tr>
                            </th>
                    </table>
                    </th>
            </tr>
            <tr class="title">
                <td colspan="2"></td>
                <td width="5%"><font style="font-size: 65%;">None</font></td>
                <td width="5%"><font style="font-size: 65%;">A
                    little</font></td>
                <td width="5%"><font style="font-size: 65%;">Some</font></td>
                <td width="5%"><font style="font-size: 65%;">Quite
                    a lot</font></td>
                <td width="5%"><font style="font-size: 65%;">Cannot
                    do</font></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F25.</td>
                <td class="question" valign="top" width="45%">Bending over
                    from a standing position to pick up a piece of clothing from
                    the floor
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F25None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F25None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F25ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F25ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F25Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F25Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F25ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F25ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F25Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F25Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F26.</td>
                <td class="question" valign="top">Walking around one floor
                    of your home, taking into consideration thresholds, doors,
                    furniture, and a variety of floor coverings
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F26None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F26None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F26ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F26ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F26Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F26Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F26ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F26ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F26Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F26Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F27.</td>
                <td class="question" valign="top">Getting up from the
                    floor (as if you were laying on the ground)
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F27None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F27None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F27ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F27ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F27Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F27Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F27ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F27ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F27Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F27Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F28.</td>
                <td class="question" valign="top">Washing dishes, pots,
                    and utensils by hand while standing at sink
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F28None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F28None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F28ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F28ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F28Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F28Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F28ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F28ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F28Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F28Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F29.</td>
                <td class="question" valign="top">Walking several blocks</td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F29None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F29None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F29ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F29ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F29Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F29Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F29ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F29ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F29Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F29Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F30.</td>
                <td class="question" valign="top">Taking a 1 mile, brisk
                    walk without stopping to rest
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F30None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F30None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F30ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F30ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F30Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F30Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F30ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F30ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F30Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F30Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F31.</td>
                <td class="question" valign="top">Stepping on and off a
                    bus
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F31None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F31None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F31ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F31ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F31Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F31Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F31ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F31ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F31Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F31Cannot", "")))%> /></td>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">F32.</td>
                <td class="question" valign="top">Walking on a slippery
                    surface outdoors
                </td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F32None"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F32None", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F32ALittle"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F32ALittle", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F32Some"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F32Some", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F32ALot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F32ALot", "")))%> /></td>
                <td bgcolor="white" align="center"><input type="checkbox"
                                                          class="checkbox" name="F32Cannot"
                        <%=Encode.forHtml(String.valueOf(props.getProperty("F32Cannot", "")))%> /></td>
            </tr>
        </table>
        </td>
        </tr>
        <tr class="subject">
            <td align="left"><a href="javascript: backToPage3();"><<
                Previous Page</a></td>
            <td align="right"><a href="javascript: calculateScore();">
                Calculate Total Score</a></td>
        </tr>
        </table>
        <table border="0" cellspacing="0" cellpadding="0"
               style="display: none" width="100%" height="85%" id="totalScore">
            <tr>
                <td valign="top" colspan="2">
                    <table width="100%" height="590px" border="0" cellspacing="1px"
                           cellpadding="0">
                        <tr class="title">
                            <th>
                                <table border="0" cellspacing="0" cellpadding="0">
                                    <tr class="title">
                                        <th>How much difficulty do you have...?</th>
                                    </tr>
                                    <tr class="title">
                                        <th align="left"><font
                                                style="font-size: 65%; text-align: left;">(Remember
                                            this is without the help of someone else and without the use
                                            of any assistive walking device.)</font>
                                    </tr>
                            </th>
                    </table>
                    </th>
            </tr>
            <tr>
                <td class="question" valign="top" width="5%">Your total
                    raw score is: <input class="finalScore" type="text"
                                         readonly="true" name="score"/></td>
            </tr>
            <tr>
                <td>
                    <table height="520px">
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
            <td align="left"><a href="javascript: backToPage4();"><<
                Previous Page</a></td>
            <td align="right">&nbsp;</td>
        </tr>
        </table>
        <table border="0" cellspacing="0" cellpadding="0"
               style="display: none" width="100%" height="85%" id="page5">
            <tr>
                <td valign="top" colspan="2">
                    <table width="100%" height="590px" border="0" cellspacing="1px"
                           cellpadding="0">
                        <tr class="title">
                            <th colspan="7">
                                <table border="0" cellspacing="0" cellpadding="0">
                                    <tr class="title">
                                        <th>When you use your cane, walker, or other walking
                                            device, how much difficulty do you have...?
                                        </th>
                                    </tr>
                                </table>
                            </th>
                        </tr>
                        <tr class="title">
                            <td colspan="2"></td>
                            <td width="5%"><font style="font-size: 65%;">None</font></td>
                            <td width="5%"><font style="font-size: 65%;">A
                                little</font></td>
                            <td width="5%"><font style="font-size: 65%;">Some</font></td>
                            <td width="5%"><font style="font-size: 65%;">Quite
                                a lot</font></td>
                            <td width="5%"><font style="font-size: 65%;">Cannot
                                do</font></td>
                        </tr>
                        <tr>
                            <td class="question" valign="top" width="5%">FD7.</td>
                            <td class="question" valign="top" width="45%">Walking a
                                mile, taking rests as necessary
                            </td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD7None"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD7None", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD7ALittle"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD7ALittle", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD7Some"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD7Some", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD7ALot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD7ALot", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD7Cannot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD7Cannot", "")))%> /></td>
                        </tr>
                        <tr>
                            <td class="question" valign="top" width="5%">FD8.</td>
                            <td class="question" valign="top">Going up & down a
                                flight of stairs outside, without using a handrail
                            </td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD8None"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD8None", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD8ALittle"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD8ALittle", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD8Some"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD8Some", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD8ALot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD8ALot", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD8Cannot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD8Cannot", "")))%> /></td>
                        </tr>
                        <tr>
                            <td class="question" valign="top" width="5%">FD14.</td>
                            <td class="question" valign="top">Stepping up and down
                                from a curb
                            </td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD14None"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD14None", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD14ALittle"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD14ALittle", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD14Some"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD14Some", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD14ALot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD14ALot", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD14Cannot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD14Cannot", "")))%> /></td>
                        </tr>
                        <tr>
                            <td class="question" valign="top" width="5%">FD15.</td>
                            <td class="question" valign="top">Opening a heavy, outside
                                door
                            </td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD15None"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD15None", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD15ALittle"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD15ALittle", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD15Some"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD15Some", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD15ALot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD15ALot", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD15Cannot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD15Cannot", "")))%> /></td>
                        </tr>
                        <tr>
                            <td class="question" valign="top" width="5%">FD26.</td>
                            <td class="question" valign="top">Walking around one floor
                                of your home, taking into consideration thresholds, doors,
                                furniture, and a variety of floor coverings
                            </td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD26None"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD26None", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD26ALittle"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD26ALittle", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD26Some"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD26Some", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD26ALot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD26ALot", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD26Cannot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD26Cannot", "")))%> /></td>
                        </tr>
                        <tr>
                            <td class="question" valign="top" width="5%">FD29.</td>
                            <td class="question" valign="top">Walking several blocks</td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD29None"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD29None", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD29ALittle"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD29ALittle", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD29Some"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD29Some", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD29ALot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD29ALot", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD29Cannot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD29Cannot", "")))%> /></td>
                        </tr>
                        <tr>
                            <td class="question" valign="top" width="5%">FD30.</td>
                            <td class="question" valign="top">Taking a 1 mile, brisk
                                walk without stopping to rest
                            </td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD30None"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD30None", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD30ALittle"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD30ALittle", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD30Some"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD30Some", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD30ALot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD30ALot", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD30Cannot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD30Cannot", "")))%> /></td>
                        </tr>
                        <tr>
                            <td class="question" valign="top" width="5%">FD32.</td>
                            <td class="question" valign="top">Walking on a slippery
                                surface outdoors
                            </td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD32None"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD32None", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD32ALittle"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD32ALittle", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD32Some"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD32Some", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD32ALot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD32ALot", "")))%> /></td>
                            <td bgcolor="white" align="center"><input type="checkbox"
                                                                      class="checkbox" name="FD32Cannot"
                                    <%=Encode.forHtml(String.valueOf(props.getProperty("FD32Cannot", "")))%> /></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr class="subject">
                <td align="left">&nbsp;</td>
                <td align="right">&nbsp;</td>
            </tr>
        </table>
        </td>
        </tr>
        <tr>
            <td valign="top">
                <table class="Head" style="display: none" width="100%" height="15%"
                       id="functionBar">
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
                        <td><font style="font-size: 70%">&copy; Copyright
                            2002 Trustees of Boston University, All Right Reserved</font></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <table class="Head" valign="bottom" width="100%" height="15%"
                       id="copyRight">
                    <tr>
                        <td><font style="font-size: 70%">&copy; Copyright
                            2002 Trustees of Boston University, All Right Reserved</font></td>
                    </tr>
                </table>
            </td>
        </tr>
        </table>
        </td>
        </tr>
        </table>
    </form>
    </body>
</html>
