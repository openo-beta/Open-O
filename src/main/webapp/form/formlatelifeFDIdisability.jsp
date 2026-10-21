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
    String formClass = "LateLifeFDIDisability";
    String formLink = "formlatelifeFDIdisability.jsp";

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
        <title>Late Life FDI: Disability component</title>
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
        <link rel="stylesheet" type="text/css" media="all" href="<%= request.getContextPath() %>/share/css/extractedFromPages.css"/>
    </head>


    <script type="text/javascript" language="Javascript">
        var choiceFormat = new Array(6, 10, 11, 15, 16, 20, 21, 25, 26, 30, 31, 35, 36, 40, 41, 45,
            46, 50, 51, 55, 56, 60, 61, 65, 66, 70, 71, 75, 76, 80, 81, 85,
            86, 90, 91, 95, 96, 100, 101, 105, 106, 110, 111, 115, 116, 120, 121, 125,
            126, 130, 131, 135, 136, 140, 141, 145, 146, 150, 151, 155, 156, 160, 161, 165);
        var allNumericField = null;
        var allMatch = null;
        var action = "/<%=Encode.forJavaScript(String.valueOf(project_home))%>/form/formname.do";

        function goToInstructions() {
            document.getElementById('instruction').style.display = 'block';
            document.getElementById('page1').style.display = 'none';
            document.getElementById('page2').style.display = 'none';
            document.getElementById('page3').style.display = 'none';
            document.getElementById('functionBar').style.display = 'none';
            document.getElementById('copyRight').style.display = 'block';
        }

        function goToVisualAid1() {
            var vheight = 500;
            var vwidth = 640;
            var windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=600,screenY=200,top=0,left=0";
            window.open("form/formlatelifedisabilityvisualAid1.jsp", "", windowprops);
        }

        function goToVisualAid2() {
            var vheight = 768;
            var vwidth = 640;
            var windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=600,screenY=200,top=0,left=0";
            window.open("form/formlatelifedisabilityvisualAid2.jsp", "", windowprops);
        }

        function goToPage1() {
            document.getElementById('instruction').style.display = 'none';
            document.getElementById('page1').style.display = 'block';
            document.getElementById('page2').style.display = 'none';
            document.getElementById('page3').style.display = 'none';
            document.getElementById('functionBar').style.display = 'block';
            document.getElementById('copyRight').style.display = 'none';
        }

        function backToPage1() {
            document.getElementById('instruction').style.display = 'none';
            document.getElementById('page1').style.display = 'block';
            document.getElementById('page2').style.display = 'none';
            document.getElementById('page3').style.display = 'none';
            document.getElementById('functionBar').style.display = 'block';
            document.getElementById('copyRight').style.display = 'none';
        }

        function goToPage2() {
            var checkboxes = new Array(6, 10, 11, 15, 16, 20, 21, 25, 26, 30, 31, 35, 36, 40, 41, 45, 46, 50, 51, 55, 56, 60, 61, 65);
            if (is1CheckboxChecked(0, checkboxes) == true && isFormCompleted(6, 65, 12, 0) == true) {
                document.getElementById('instruction').style.display = 'none';
                document.getElementById('page1').style.display = 'none';
                document.getElementById('page2').style.display = 'block';
                document.getElementById('page3').style.display = 'none';
                document.getElementById('functionBar').style.display = 'block';
                document.getElementById('copyRight').style.display = 'none';
            }
        }

        function backToPage2() {
            document.getElementById('instruction').style.display = 'none';
            document.getElementById('page1').style.display = 'none';
            document.getElementById('page2').style.display = 'block';
            document.getElementById('page3').style.display = 'none';
            document.getElementById('functionBar').style.display = 'block';
            document.getElementById('copyRight').style.display = 'none';
        }

        function goToPage3() {
            var checkboxes = new Array(66, 70, 71, 75, 76, 80, 81, 85, 86, 90, 91, 95, 96, 100, 101, 105, 106, 110, 111, 115);
            if (is1CheckboxChecked(0, checkboxes) == true && isFormCompleted(66, 115, 10, 0) == true) {
                document.getElementById('instruction').style.display = 'none';
                document.getElementById('page1').style.display = 'none';
                document.getElementById('page2').style.display = 'none';
                document.getElementById('page3').style.display = 'block';
                document.getElementById('functionBar').style.display = 'block';
                document.getElementById('copyRight').style.display = 'none';
            }
        }

        function checkBeforeSave() {
            if (document.getElementById('page3').style.display == 'block') {
                if (isFormCompleted(116, 165, 10, 0) == true)
                    return true;
            } else {
                if (isFormCompleted(6, 65, 12, 0) == true && isFormCompleted(66, 115, 10, 0) == true && isFormCompleted(116, 165, 10, 0) == true)
                    return true;
            }

            return false;
        }
    </script>
    <script type="text/javascript" src="formScripts.js">
    </script>


    <body bgproperties="fixed" topmargin="0" leftmargin="0" rightmargin="0"
          onload="window.resizeTo(768,748)">
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
                            <th class="subject">Late Life FDI: Disability Component</th>
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
                            <td class="leftcol"><a href="javascript: goToPage1();">Questionnaire</a></td>
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
                                            <table width="100%" height="650px" border="0" cellspacing="0"
                                                   cellpadding="0">
                                                <tr class="title">
                                                    <th colspan="6">Instruction for Disability Questions</th>
                                                </tr>
                                                <tr>
                                                    <td>In this set of questions, I will ask you about
                                                        everyday things you do at this time in your life. <br>
                                                        There are <font style="text-decoration: underline">two</font>
                                                        parts to each question. <br>
                                                        First, I will ask you <font style="font-style: italic">How
                                                            often</font> you do a certain activity. <br>
                                                        Next, I will ask you <font style="font-style: italic">To
                                                            what extent do you feel limited</font> in doing this
                                                        activity. <br>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <table height="10px">
                                                            <tr>
                                                                <td></td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                <tr>
                                                <tr>
                                                    <td valign="center" align="center">
                                                        <table style="border: 1px solid #000000" cellpadding="2"
                                                               width="90%" height="55%">
                                                            <tr>
                                                                <td width="3%"></td>
                                                                <td class="instruction" colspan="2"><font
                                                                        style="font-weight: bold">Explain each question
                                                                    and
                                                                    subsequent answer options:</font> <br>
                                                                    <br>
                                                                    For the first question (<font
                                                                            style="font-style: italic">How
                                                                        often do you do the activity?</font>), please
                                                                    choose from these
                                                                    answers:
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td width="10%"></td>
                                                                <td class="instruction"><font style="font-weight: bold">Very
                                                                    often</font></td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td width="10%"></td>
                                                                <td class="instruction"><font style="font-weight: bold">Often</font>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td width="10%"></td>
                                                                <td class="instruction"><font style="font-weight: bold">Once
                                                                    in a while</font></td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td width="10%"></td>
                                                                <td class="instruction"><font style="font-weight: bold">Almost
                                                                    never</font></td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td width="10%"></td>
                                                                <td class="instruction"><font style="font-weight: bold">Never</font>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td class="instruction" colspan="2">[Show visual aid to
                                                                    interviewee]
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                            </tr>
                                                            <tr>
                                                                <td width="3%"></td>
                                                                <td class="instruction" colspan="2">For the second
                                                                    question (<font style="font-style: italic">To what
                                                                        extent do you feel limited in doing the
                                                                        activity?</font>), please
                                                                    choose from these answers:
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td width="10%"></td>
                                                                <td class="instruction"><font style="font-weight: bold">Not
                                                                    at all</font></td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td width="10%"></td>
                                                                <td class="instruction"><font style="font-weight: bold">A
                                                                    little</font></td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td width="10%"></td>
                                                                <td class="instruction"><font style="font-weight: bold">Somewhat</font>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td width="10%"></td>
                                                                <td class="instruction"><font style="font-weight: bold">A
                                                                    lot</font></td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td width="10%"></td>
                                                                <td class="instruction"><font style="font-weight: bold">Completely</font>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td class="instruction" colspan="2">[Show visual aid to
                                                                    interviewee]
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                            </tr>
                                                            <tr>
                                                                <td></td>
                                                                <td class="instruction" colspan="2">For example, you
                                                                    might feel limited because of your health, or
                                                                    because it
                                                                    takes a lot of mental and physical energy. Please
                                                                    keep in
                                                                    mind that you can also feel limited by factors
                                                                    outside of
                                                                    yourself. Your environment could restrict you from
                                                                    doing the
                                                                    things; for instance, transportation issues,
                                                                    accessibility,
                                                                    and social or economic circumstances could limit you
                                                                    from
                                                                    doing things you would like to do. Think of all
                                                                    these factors
                                                                    when you answer this section.
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <table height="10px">
                                                            <tr>
                                                                <td></td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                <tr>
                                                <tr>
                                                    <td>For each question, please select the one answer that
                                                        comes closest to the way you have been feeling. <br>
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
                                            <table width="100%" height="612px" border="0" cellspacing="1px"
                                                   cellpadding="0">
                                                <tr class="title">
                                                    <th colspan="2" rowspan="2"></th>
                                                    <th colspan="5" style="border-right: 2px solid #F2F2F2">How
                                                        often Do you...?
                                                    </th>
                                                    <th colspan="5">To what extent do you feel limit in...?</th>
                                                </tr>
                                                <tr class="title">
                                                    <td width="6%"><font style="font-size: 60%;">Very
                                                        Often</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Often</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Once
                                                        in a while</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Almost
                                                        never</font></td>
                                                    <td width="6%" style="border-right: 2px solid #F2F2F2"><font
                                                            style="font-size: 60%;">Never</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Not at
                                                        all</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">A
                                                        little</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Somewhat</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">A lot</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Completely</font></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D1.</td>
                                                    <td class="question" valign="top" width="33%">Keep
                                                        (Keeping) in touch with others through letters, phone, or email
                                                    </td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D1VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D1VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D1Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D1Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D1OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D1OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D1AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D1AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D1Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D1Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D1Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D1Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D1Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D1Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D1Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D1Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D1Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D1Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D1Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D1Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D2.</td>
                                                    <td class="question" valign="top">Visit (Visiting)
                                                        friends and family in their homes.
                                                    </td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D2VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D2VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D2Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D2Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D2OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D2OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D2AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D2AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D2Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D2Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D2Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D2Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D2Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D2Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D2Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D2Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D2Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D2Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D2Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D2Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D3.</td>
                                                    <td class="question" valign="top">Provide (Providing)
                                                        care or assistance to others. <font class="instruction">This
                                                            may include providing personal care, transportation, and
                                                            running errands for family members or friends.</font></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D3VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D3VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D3Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D3Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D3OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D3OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D3AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D3AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D3Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D3Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D3Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D3Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D3Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D3Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D3Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D3Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D3Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D3Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D3Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D3Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D4.</td>
                                                    <td class="question" valign="top">Take (Taking) care of
                                                        the inside of your home. <font class="instruction">This
                                                            includes managing and taking responsibility for home making,
                                                            laundry, housecleaning and minor household repairs.</font>
                                                    </td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D4VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D4VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D4Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D4Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D4OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D4OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D4AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D4AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D4Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D4Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D4Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D4Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D4Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D4Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D4Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D4Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D4Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D4Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D4Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D4Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D5.</td>
                                                    <td class="question" valign="top" width="35%">Work
                                                        (Working) at a volunteer job outside your home.
                                                    </td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D5VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D5VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D5Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D5Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D5OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D5OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D5AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D5AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D5Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D5Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D5Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D5Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D5Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D5Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D5Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D5Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D5Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D5Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D5Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D5Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D6.</td>
                                                    <td class="question" valign="top">Take (Taking) part in
                                                        active recreation. <font class="instruction">This may
                                                            include bowling, golf, tennis, hiking, jogging, or
                                                            swimming.</font></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D6VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D6VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D6Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D6Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D6OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D6OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D6AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D6AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D6Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D6Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D6Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D6Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D6Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D6Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D6Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D6Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D6Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D6Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D6Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D6Completely", "")))%> /></td>
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
                                            <table width="100%" height="612px" border="0" cellspacing="1px"
                                                   cellpadding="0">
                                                <tr class="title">
                                                    <th colspan="2" rowspan="2"></th>
                                                    <th colspan="5" style="border-right: 2px solid #F2F2F2">How
                                                        often Do you...?
                                                    </th>
                                                    <th colspan="5">To what extent do you feel limit in...?</th>
                                                </tr>
                                                <tr class="title">
                                                    <td width="6%"><font style="font-size: 60%;">Very
                                                        Often</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Often</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Once
                                                        in a while</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Almost
                                                        never</font></td>
                                                    <td width="6%" style="border-right: 2px solid #F2F2F2"><font
                                                            style="font-size: 60%;">Never</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Not at
                                                        all</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">A
                                                        little</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Somewhat</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">A lot</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Completely</font></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D7.</td>
                                                    <td class="question" valign="top" width="33%">Take
                                                        (Taking) care of household business and finances. <font
                                                                class="instruction">This may include managing and
                                                            taking responsibility for your money, paying bills, dealing
                                                            with a landlord or tenants, dealing with utility companies
                                                            or
                                                            governmental agencies.</font></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D7VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D7VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D7Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D7Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D7OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D7OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D7AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D7AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D7Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D7Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D7Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D7Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D7Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D7Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D7Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D7Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D7Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D7Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D7Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D7Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D8.</td>
                                                    <td class="question" valign="top">Take (Taking) care of
                                                        your own health. <font class="instruction">This may
                                                            include managing daily medications, following a special
                                                            diet,
                                                            scheduling doctor's appointments.</font></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D8VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D8VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D8Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D8Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D8OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D8OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D8AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D8AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D8Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D8Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D8Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D8Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D8Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D8Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D8Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D8Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D8Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D8Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D8Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D8Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D9.</td>
                                                    <td class="question" valign="top">Travel (Traveling) out
                                                        of town for at least an overnight stay.
                                                    </td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D9VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D9VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D9Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D9Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D9OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D9OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D9AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D9AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D9Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D9Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D9Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D9Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D9Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D9Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D9Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D9Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D9Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D9Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D9Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D9Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D10.</td>
                                                    <td class="question" valign="top">Take (Taking) part in a
                                                        regular fitness program. <font class="instruction">This
                                                            may include walking for exercise, stationary biking, weight
                                                            lifting, or exercise classes.</font></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D10VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D10VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D10Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D10Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D10OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D10OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D10AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D10AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D10Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D10Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D10Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D10Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D10Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D10Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D10Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D10Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D10Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D10Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D10Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D10Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D11.</td>
                                                    <td class="question" valign="top">Invite (Inviting)
                                                        people into your home for a meal or entertainment.
                                                    </td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D11VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D11VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D11Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D11Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D11OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D11OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D11AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D11AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D11Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D11Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D11Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D11Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D11Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D11Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D11Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D11Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D11Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D11Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D11Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D11Completely", "")))%> /></td>
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
                                            <table width="100%" height="612px" border="0" cellspacing="1px"
                                                   cellpadding="0">
                                                <tr class="title">
                                                    <th colspan="2" rowspan="2"></th>
                                                    <th colspan="5" style="border-right: 2px solid #F2F2F2">How
                                                        often Do you...?
                                                    </th>
                                                    <th colspan="5">To what extent do you feel limit in...?</th>
                                                </tr>
                                                <tr class="title">
                                                    <td width="6%"><font style="font-size: 60%;">Very
                                                        Often</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Often</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Once
                                                        in a while</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Almost
                                                        never</font></td>
                                                    <td width="6%" style="border-right: 2px solid #F2F2F2"><font
                                                            style="font-size: 60%;">Never</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Not at
                                                        all</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">A
                                                        little</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Somewhat</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">A lot</font></td>
                                                    <td width="6%"><font style="font-size: 60%;">Completely</font></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D12.</td>
                                                    <td class="question" valign="top" width="33%">Go (Going)
                                                        out with others to public places such as restaurants or movies.
                                                    </td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D12VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D12VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D12Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D12Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D12OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D12OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D12AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D12AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D12Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D12Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D12Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D12Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D12Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D12Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D12Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D12Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D12Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D12Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D12Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D12Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D13.</td>
                                                    <td class="question" valign="top">Take (Taking) care of
                                                        your own personal care needs. <font class="instruction">
                                                            This includes bathing, dressing, and toileting.</font></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D13VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D13VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D13Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D13Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D13OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D13OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D13AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D13AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D13Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D13Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D13Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D13Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D13Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D13Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D13Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D13Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D13Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D13Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D13Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D13Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D14.</td>
                                                    <td class="question" valign="top">Take (Taking) part in
                                                        organized social activities. <font class="instruction">
                                                            This may include clubs, card playing, senior center events,
                                                            community or religious groups.</font></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D14VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D14VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D14Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D14Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D14OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D14OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D14AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D14AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D14Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D14Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D14Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D14Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D14Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D14Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D14Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D14Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D14Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D14Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D14Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D14Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D15.</td>
                                                    <td class="question" valign="top">Take (Taking) care of
                                                        local errands. <font class="instruction"> This may
                                                            include managing and taking responsibility for shopping for
                                                            food and personal items, and going to the bank, library, or
                                                            dry
                                                            cleaner.</font></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D15VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D15VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D15Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D15Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D15OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D15OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D15AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D15AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D15Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D15Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D15Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D15Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D15Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D15Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D15Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D15Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D15Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D15Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D15Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D15Completely", "")))%> /></td>
                                                </tr>
                                                <tr>
                                                    <td class="question" valign="top" width="5%">D16.</td>
                                                    <td class="question" valign="top">Prepare (Preparing)
                                                        meals for yourself. <font class="instruction"> This
                                                            includes planning, cooking, serving, and cleaning up.</font>
                                                    </td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D16VeryOften"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D16VeryOften", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D16Often"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D16Often", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D16OnceInAWhile"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D16OnceInAWhile", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D16AlmostNever"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D16AlmostNever", "")))%> /></td>
                                                    <td bgcolor="white" align="center"
                                                        style="border-right: 2px solid #F2F2F2"><input
                                                            type="checkbox" class="checkbox" name="D16Never"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D16Never", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D16Not"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D16Not", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D16Little"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D16Little", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D16Somewhat"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D16Somewhat", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D16Alot"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D16Alot", "")))%> /></td>
                                                    <td bgcolor="white" align="center"><input type="checkbox"
                                                                                              class="checkbox"
                                                                                              name="D16Completely"
                                                            <%=Encode.forHtml(String.valueOf(props.getProperty("D16Completely", "")))%> /></td>
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
