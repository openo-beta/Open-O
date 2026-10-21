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

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.title"/></title>
        <link rel="stylesheet" type="text/css" href="westernuStyle.css">
    </head>
    <body bgproperties="fixed" topmargin="0" leftmargin="0" rightmargin="0" onload="window.resizeTo(768,768)"
          bgcolor="#eeeeee">
    <form action="${pageContext.request.contextPath}/form/formname.do" method="post">
        <h1><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.title"/></h1>

        <%@include file="formIntakeHxTitleBar.jsp" %>

        <!--Medical Conditions-->
        <h2><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.medicalConditionsTitle"/></h2>
        <hr/>
        <table>
            <tr>
                <td class="title" colspan="4"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.hadConditions"/>:</td>
            </tr>

            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.brokenBones"/>:</td>
                <td><input type="text" name="Conditionsbrokenbones"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsbrokenbones","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescbrokenbones"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescbrokenbones","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.migraine"/>:</td>
                <td><input type="text" name="Conditionsmigraine"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsmigraine","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescmigraine"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescmigraine","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.neuroDisorder"/>:</td>
                <td><input type="text" name="Conditionsneurologicdisorder"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsneurologicdisorder","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescneurologicdisorder"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescneurologicdisorder","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.asthma"/>:</td>
                <td><input type="text" name="Conditionsasthma" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsasthma","")))%>"/>
                </td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescasthma"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescasthma","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.pneumonia"/>:</td>
                <td><input type="text" name="Conditionspneumonia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionspneumonia","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescpneumonia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescpneumonia","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.lungDisease"/>:</td>
                <td><input type="text" name="Conditionslungdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionslungdisease","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDesclungdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDesclungdisease","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.heartDisease"/>:</td>
                <td><input type="text" name="Conditionsheartdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsheartdisease","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescheartdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescheartdisease","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.ulcer"/>:</td>
                <td><input type="text" name="Conditionsulcer" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsulcer","")))%>"/>
                </td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDesculcer"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDesculcer","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.bowelDisease"/>:</td>
                <td><input type="text" name="Conditionsboweldisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsboweldisease","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescboweldisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescboweldisease","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.hepatitis"/>:</td>
                <td><input type="text" name="Conditionshepatitis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionshepatitis","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDeschepatitis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDeschepatitis","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.hivPositive"/>:</td>
                <td><input type="text" name="ConditionsHIV" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsHIV","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescHIV" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescHIV","")))%>"/>
                </td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.thyroidProblem"/>:</td>
                <td><input type="text" name="Conditionsthyroid" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsthyroid","")))%>"/>
                </td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescthyroid"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescthyroid","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.bloodDisorder"/>:</td>
                <td><input type="text" name="Conditionsblooddisorder"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsblooddisorder","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescblooddisorder"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescblooddisorder","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.diabetes"/>:</td>
                <td><input type="text" name="Conditionsdiabetes"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsdiabetes","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescdiabetes"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescdiabetes","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.bloodTransfusion"/>:</td>
                <td><input type="text" name="Conditionsbloodtransfusion"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsbloodtransfusion","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescbloodtransfusion"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescbloodtransfusion","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.cancer"/>:</td>
                <td><input type="text" name="Conditionscancerorleukemia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionscancerorleukemia","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDesccancerorleukemia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDesccancerorleukemia","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.sexualDisease"/>:</td>
                <td><input type="text" name="Conditionssexualdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionssexualdisease","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescsexualdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescsexualdisease","")))%>"/></td>
            </tr>

            <tr>
                <td class="title" colspan="4"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.hadConditions"/>:</td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.urinary"/>:</td>
                <td><input type="text" name="ConditionsURI" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsURI","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescURI" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescURI","")))%>"/>
                </td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.emotional"/>:</td>
                <td><input type="text" name="Conditionsemotional"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsemotional","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescemotional"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescemotional","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.arthritis"/>:</td>
                <td><input type="text" name="Conditionsarthritis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsarthritis","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescarthritis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescarthritis","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.eatingDisorder"/>:</td>
                <td><input type="text" name="Conditionseatingdisorder"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionseatingdisorder","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDesceatingdisorder"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDesceatingdisorder","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.osteoporosis"/>:</td>
                <td><input type="text" name="Conditionsosteoporosis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsosteoporosis","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescosteoporosis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescosteoporosis","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.skinProblems"/>:</td>
                <td><input type="text" name="Conditionsskin" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsskin","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescskin"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescskin","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.highBP"/>:</td>
                <td><input type="text" name="ConditionsHighbloodpressure"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsHighbloodpressure","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescHighbloodpressure"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescHighbloodpressure","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.learningDisability"/>:</td>
                <td><input type="text" name="Conditionslearningdisability"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionslearningdisability","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDesclearningdisability"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDesclearningdisability","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.schizophrenia"/>:</td>
                <td><input type="text" name="Conditionsschizophrenia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsschizophrenia","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescschizophrenia"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescschizophrenia","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.alcoholDependency"/>:</td>
                <td><input type="text" name="Conditionsalcohol" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsalcohol","")))%>"/>
                </td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescalcohol"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescalcohol","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.multipleSclerosis"/>:</td>
                <td><input type="text" name="ConditionsMS" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsMS","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescMS" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescMS","")))%>"/>
                </td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.stroke"/>:</td>
                <td><input type="text" name="Conditionsstroke" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsstroke","")))%>"/>
                </td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescstroke"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescstroke","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.highCholesterol"/>:</td>
                <td><input type="text" name="ConditionsHighcholesterol"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsHighcholesterol","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescHighcholesterol"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescHighcholesterol","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.depression"/>:</td>
                <td><input type="text" name="Conditionsdepression"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Conditionsdepression","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescdepression"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescdepression","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.drugDependancy"/>:</td>
                <td><input type="text" name="ConditionsDrugdependency"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDrugdependency","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescDrugdependency"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescDrugdependency","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.otherDisease"/>:</td>
                <td><input type="text" name="ConditionsOtherdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsOtherdisease","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.describe"/>:</td>
                <td><input type="text" name="ConditionsDescOtherdisease"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ConditionsDescOtherdisease","")))%>"/></td>
            </tr>
        </table>
    </form>
    </body>
</html>
