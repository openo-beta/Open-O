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
    String roleName3$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed2 = true;
%>
<security:oscarSec roleName="<%=roleName3$%>" objectName="_form" rights="r" reverse="<%=true%>">
    <%authed2 = false; %>
    <%response.sendRedirect(request.getContextPath() + "/securityError.jsp?type=_form");%>
</security:oscarSec>
<%
    if (!authed2) {
        return;
    }
%>

<!DOCTYPE html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="oscarResources"/>

<%@ page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@ page import="org.owasp.encoder.Encode" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:message key="oscarEncounter.formIntakeHx.title"/></title>
        <link rel="stylesheet" type="text/css" href="westernuStyle.css">
    </head>
    <body bgproperties="fixed" topmargin="0" leftmargin="0" rightmargin="0" onload="window.resizeTo(768,768)"
          bgcolor="#eeeeee">
    <form action="${pageContext.request.contextPath}/form/formname.do" method="post">
        <h1><fmt:message key="oscarEncounter.formIntakeHx.title"/></h1>

        <%@include file="formIntakeHxTitleBar.jsp" %>

        <!--Allergies-->
        <h2><fmt:message key="oscarEncounter.formIntakeHx.sectionAllergiesTitle"/></h2>
        <hr/>
        <table>
            <tr>
                <td colspan="2" class="title"><fmt:message key="oscarEncounter.formIntakeHx.allergicToDrugsYN"/>?</td>
                <td><input type="text" name="AllergicYesNo" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("AllergicYesNo","")))%>"/></td>
            </tr>
            <tr>
                <td colspan="4" class="title"><fmt:message key="oscarEncounter.formIntakeHx.allergicDrugNames"/>:</td>
            </tr>
            <tr>
                <td colspan="4">
                    <table>
                        <tr>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.drug1"/>:</td>
                            <td><input type="text" name="DrugAllergy1"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergy1","")))%>"/></td>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.reactionSymptoms"/>:</td>
                            <td><input type="text" name="DrugAllergyRS1"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergyRS1","")))%>"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.drug2"/>:</td>
                            <td><input type="text" name="DrugAllergy2"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergy2","")))%>"/></td>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.reactionSymptoms"/>:</td>
                            <td><input type="text" name="DrugAllergyRS2"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergyRS2","")))%>"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.drug3"/>:</td>
                            <td><input type="text" name="DrugAllergy3"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergy3","")))%>"/></td>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.reactionSymptoms"/>:</td>
                            <td><input type="text" name="DrugAllergyRS3"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergyRS3","")))%>"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.drug4"/>:</td>
                            <td><input type="text" name="DrugAllergy4"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergy4","")))%>"/></td>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.reactionSymptoms"/>:</td>
                            <td><input type="text" name="DrugAllergyRS4"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergyRS4","")))%>"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.drug5"/>:</td>
                            <td><input type="text" name="DrugAllergy5"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergy5","")))%>"/></td>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.reactionSymptoms"/>:</td>
                            <td><input type="text" name="DrugAllergyRS5"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergyRS5","")))%>"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.drug6"/>:</td>
                            <td><input type="text" name="DrugAllergy6"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergy6","")))%>"/></td>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.reactionSymptoms"/>:</td>
                            <td><input type="text" name="DrugAllergyRS6"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergyRS6","")))%>"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.drug7"/>:</td>
                            <td><input type="text" name="DrugAllergy7"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergy7","")))%>"/></td>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.reactionSymptoms"/>:</td>
                            <td><input type="text" name="DrugAllergyRS7"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergyRS7","")))%>"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.drug8"/>:</td>
                            <td><input type="text" name="DrugAllergy8"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergy8","")))%>"/></td>
                            <td><fmt:message key="oscarEncounter.formIntakeHx.reactionSymptoms"/>:</td>
                            <td><input type="text" name="DrugAllergyRS8"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrugAllergyRS8","")))%>"/></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td colspan="3" class="title"><fmt:message key="oscarEncounter.formIntakeHx.allergyShotYN"/>?</td>
                <td><input type="text" name="AllergicShotsYesNo"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("AllergicShotsYesNo","")))%>"/></td>
            </tr>
            <tr>
                <td colspan="3" class="title"><fmt:message key="oscarEncounter.formIntakeHx.allergyNonDrugYN"/>?</td>
                <td><input type="text" name="AllergicNonDrugYesNo"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("AllergicNonDrugYesNo","")))%>"/></td>
            </tr>
            <tr>
                <td colspan="4" class="title"><fmt:message key="oscarEncounter.formIntakeHx.allergicNonDrug"/>:</td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.allergicNonDrugBeeSting"/></td>
                <td><input type="checkbox" name="allergicbee" <%=Encode.forHtml(String.valueOf(props.getProperty("allergicbee", "")))%>/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.allergicNonDrugRagweedPollen"/></td>
                <td><input type="checkbox" name="allergicragweed" <%=Encode.forHtml(String.valueOf(props.getProperty("allergicragweed", "")))%>/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.allergicNonDrugOtherPollen"/></td>
                <td><input type="checkbox"
                           name="allergicOtherPollens" <%=Encode.forHtml(String.valueOf(props.getProperty("allergicOtherPollens", "")))%>/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.allergicNonDrugGrasses"/></td>
                <td><input type="checkbox" name="allergicgrasses" <%=Encode.forHtml(String.valueOf(props.getProperty("allergicgrasses", "")))%>/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.allergicNonDrugDust"/></td>
                <td><input type="checkbox" name="allergicdust" <%=Encode.forHtml(String.valueOf(props.getProperty("allergicdust", "")))%>/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.allergicNonDrugAnimalFur"/></td>
                <td><input type="checkbox" name="allergicanimal" <%=Encode.forHtml(String.valueOf(props.getProperty("allergicanimal", "")))%>/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.allergicNonDrugOtherAgent"/></td>
                <td><input type="checkbox" name="allergicother" <%=Encode.forHtml(String.valueOf(props.getProperty("allergicother", "")))%>/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.allergicNonDrugFood"/></td>
                <td><input type="checkbox" name="allergicfood" <%=Encode.forHtml(String.valueOf(props.getProperty("allergicfood", "")))%>/></td>
            </tr>
        </table>
    </form>
    </body>
</html>