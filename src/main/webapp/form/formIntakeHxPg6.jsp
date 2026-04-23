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

        <!--Immunizations-->
        <h2><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.immunizationsTitle"/></h2>
        <hr/>
        <table>
            <tr>
                <td class="title" colspan="4"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.whenImmunized"/>:</td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.hepatitisBSerum"/>:</td>
                <td><input type="text" name="ImmunizationHepatitisB"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationHepatitisB","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="ImmunizationYearHepatitisB"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationYearHepatitisB","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.tetanusLockjaw"/>:</td>
                <td><input type="text" name="ImmunizationHadTetanus"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationHadTetanus","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="ImmunizationYearTetanus"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationYearTetanus","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.polio"/>:</td>
                <td><input type="text" name="ImmunizationHadPolio"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationHadPolio","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="ImmunizationYearPolio"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationYearPolio","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.mmr"/>:</td>
                <td><input type="text" name="ImmunizationHadMMR"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationHadMMR","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="ImmunizationYearMMR"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationYearMMR","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.lastTBTest"/>:</td>
                <td><input type="text" name="ImmunizationHadTB" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationHadTB","")))%>"/>
                </td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="ImmunizationYearTB"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationYearTB","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.rubella"/>:</td>
                <td><input type="text" name="ImmunizationHadRubella"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationHadRubella","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="ImmunizationYearRubella"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationYearRubella","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.varicella"/>:</td>
                <td><input type="text" name="ImmunizationHadVaricella"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationHadVaricella","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="ImmunizationYearVaricella"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationYearVaricella","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.meningitis"/>:</td>
                <td><input type="text" name="ImmunizationHadMeningitis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationHadMeningitis","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="ImmunizationYearMeningitis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationYearMeningitis","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.pneumococcus"/>:</td>
                <td><input type="text" name="ImmunizationHadPneumococcus"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationHadPneumococcus","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="ImmunizationYearPneumococcus"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ImmunizationYearPneumococcus","")))%>"/></td>
            </tr>
            <tr>
                <td class="title" colspan="4"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.hadDisease"/>:</td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.hepatitisBSerum"/>:</td>
                <td><input type="text" name="immunizationDiseaseHepatitisB"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseHepatitisB","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="immunizationDiseaseYearHepatitisB"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseYearHepatitisB","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.tetanusLockjaw"/>:</td>
                <td><input type="text" name="immunizationDiseaseTetanus"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseTetanus","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="immunizationDiseaseYearTetanus"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseYearTetanus","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.polio"/>:</td>
                <td><input type="text" name="immunizationDiseasePolio"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseasePolio","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="immunizationDiseaseYearPolio"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseYearPolio","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.mmr"/>:</td>
                <td><input type="text" name="immunizationDiseaseMMR"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseMMR","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="immunizationDiseaseYearMMR"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseYearMMR","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.lastTBTest"/>:</td>
                <td><input type="text" name="immunizationDiseaseTb"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseTb","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="immunizationDiseaseYearTb"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseYearTb","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.rubella"/>:</td>
                <td><input type="text" name="immunizationDiseaseRubella"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseRubella","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="immunizationDiseaseYearRubella"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseYearRubella","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.varicella"/>:</td>
                <td><input type="text" name="immunizationDiseaseVaricella"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseVaricella","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="immunizationDiseaseYearVaricella"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseYearVaricella","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.meningitis"/>:</td>
                <td><input type="text" name="immunizationDiseaseMeningitis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseMeningitis","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="immunizationDiseaseYearMeningitis"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseYearMeningitis","")))%>"/></td>
            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.pneumococcus"/>:</td>
                <td><input type="text" name="immunizationDiseasePneumococcus"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseasePneumococcus","")))%>"/></td>

                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.year"/>:</td>
                <td><input type="text" name="immunizationDiseaseYearPneumococcus"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("immunizationDiseaseYearPneumococcus","")))%>"/></td>
            </tr>
            <tr>
                <td class="title" colspan="3"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.formIntakeHx.immunizationCardOnPerson"/>:</td>
                <td><input type="text" name="HaveImmunizationCard"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("HaveImmunizationCard","")))%>"/></td>
            </tr>
        </table>

    </form>
    </body>
</html>