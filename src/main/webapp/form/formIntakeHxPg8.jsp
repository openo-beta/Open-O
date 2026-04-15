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
<%@ page import="org.owasp.encoder.Encode" %>
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

        <!--Family History-->
        <h2><fmt:message key="oscarEncounter.formIntakeHx.familyHistoryTitle"/></h2>
        <hr/>
        <table>
            <tr>
                <td class="title" colspan="4"><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.familyConditions"/>:
                </td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.migraine"/>:</td>
                <td><input type="text" name="biologicalmigraine"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalmigraine","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescmigraine"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescmigraine","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.neuroDisorder"/>:</td>
                <td><input type="text" name="biologicalneurologic"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalneurologic","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescneurologic"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescneurologic","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.asthma"/>:</td>
                <td><input type="text" name="biologicalasthma" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalasthma","")))%>"/>
                </td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescasthma"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescasthma","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.pheumonia"/>:</td>
                <td><input type="text" name="biologicalpneumonia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalpneumonia","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescpneumonia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescpneumonia","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.lungDisease"/>:</td>
                <td><input type="text" name="biologicallungdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicallungdisease","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDesclungdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDesclungdisease","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.heartDisease"/>:</td>
                <td><input type="text" name="biologicalheartdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalheartdisease","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescheartdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescheartdisease","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.ulcer"/>:</td>
                <td><input type="text" name="biologicalulcer" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalulcer","")))%>"/>
                </td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDesculcer"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDesculcer","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.bowelDisease"/>:</td>
                <td><input type="text" name="biologicalboweldisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalboweldisease","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescboweldisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescboweldisease","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.hepatitis"/>:</td>
                <td><input type="text" name="biologicalhepatitis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalhepatitis","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDeschepatitis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDeschepatitis","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.thyroid"/>:</td>
                <td><input type="text" name="biologicalthyroid" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalthyroid","")))%>"/>
                </td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescthyroid"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescthyroid","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.bloodDisorder"/>:</td>
                <td><input type="text" name="biologicalblooddisorder"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalblooddisorder","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescblooddisorder"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescblooddisorder","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.diabetes"/>:</td>
                <td><input type="text" name="biologicaldiabetes"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicaldiabetes","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescdiabetes"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescdiabetes","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.bloodTransfusion"/>:</td>
                <td><input type="text" name="biologicalbloodtransfusion"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalbloodtransfusion","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescbloodtransfusion"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescbloodtransfusion","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.cancer"/>:</td>
                <td><input type="text" name="biologicalcancerorleukemia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalcancerorleukemia","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDesccancerorleukemia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDesccancerorleukemia","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.uri"/>:</td>
                <td><input type="text" name="biologicalURI" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalURI","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescURI" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescURI","")))%>"/>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.emotional"/>:</td>
                <td><input type="text" name="biologicalemotional"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalemotional","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescemotional"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescemotional","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.arthritis"/>:</td>
                <td><input type="text" name="biologicalarthritis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalarthritis","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescarthritis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescarthritis","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.osteoporosis"/>:</td>
                <td><input type="text" name="biologicalosteoporosis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalosteoporosis","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescosteoporosis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescosteoporosis","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.skinProblems"/>:</td>
                <td><input type="text" name="biologicalskin" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalskin","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescskin"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescskin","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.highBP"/>:</td>
                <td><input type="text" name="biologicalHBP" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalHBP","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescHBP" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescHBP","")))%>"/>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.learningDisability"/>:</td>
                <td><input type="text" name="biologicallearningdisability"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicallearningdisability","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDesclearningdisability"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDesclearningdisability","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.schizophrenia"/>:</td>
                <td><input type="text" name="biologicalschizophrenia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalschizophrenia","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescschizophrenia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescschizophrenia","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.alcoholDependency"/>:</td>
                <td><input type="text" name="biologicalalcohol" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalalcohol","")))%>"/>
                </td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescalcohol"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescalcohol","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.multipleSclerosis"/>:</td>
                <td><input type="text" name="biologicalMS" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalMS","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescMS" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescMS","")))%>"/>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.stroke"/>:</td>
                <td><input type="text" name="biologicalstroke" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalstroke","")))%>"/>
                </td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescstroke"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescstroke","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.cholesterol"/>:</td>
                <td><input type="text" name="biologicalhighcholesterol"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalhighcholesterol","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDeschighcholesterol"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDeschighcholesterol","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.depression"/>:</td>
                <td><input type="text" name="biologicaldepression"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicaldepression","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescdepression"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescdepression","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="oscarEncounter.formIntakeHx.familyHistory.drugDependency"/>:</td>
                <td><input type="text" name="biologicaldrug" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicaldrug","")))%>"/></td>

                <td><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="biologicalDescdrug"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("biologicalDescdrug","")))%>"/></td>
            </tr>
        </table>
    </form>
    </body>
</html>