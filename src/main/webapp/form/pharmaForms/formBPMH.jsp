<%--

    Copyright (c) 2015-2019. The Pharmacists Clinic, Faculty of Pharmaceutical Sciences, University of British Columbia. All Rights Reserved.
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
    The Pharmacists Clinic
    Faculty of Pharmaceutical Sciences
    University of British Columbia
    Vancouver, British Columbia, Canada

--%>

<%@ page language="java" contentType="text/html" %>

<%-- 
	Author: Dennis Warren 
	Company: Colcamex Resources
	Date: November 2014 
	Comment: The Pharmacists Clinic, Faculty of Pharmaceutical Sciences, University of British Columbia.
--%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    String roleName2$ = session.getAttribute("userrole") + "," + session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName2$%>" objectName="_form" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect("../../securityError.jsp?type=_form");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<html:html>

    <head>
        <title><bean:message key="colcamex.formBPMH.title"/></title>
        <link rel="stylesheet" type="text/css" media="screen"
              href="${ pageContext.request.contextPath }/form/pharmaForms/index.css"/>
        <link rel="stylesheet" type="text/css" media="screen"
              href="${ pageContext.request.contextPath }/css/bootstrap.min.css"/>
        <link rel="stylesheet" type="text/css" href="${ pageContext.request.contextPath }/css/healthCareTeam.css"/>
        <script type="text/javascript" src="${ pageContext.request.contextPath }/js/jquery.js"></script>
        <script type="text/javascript">

            function popUpData(data) {
                if (data) {
                    location.reload();
                }
            }

            jQuery(document).ready(function ($) {
                $("#editFamilyDr").click(function () {
                    var familyDrContactId = $("#familyDrContactId").val();
                    var demographicNo = '${ bpmh.demographic.demographicNo }';
                    var source = "${ pageContext.request.contextPath }/demographic/manageHealthCareTeam.jsp?" +
                        "demographicNo=" + demographicNo +
                        "&view=detached" +
                        "&element=providerId";
                    var windowspecs = "width=700,height=400,left=-1000,top=-1000, scrollbars=yes, resizable=yes";
                    window.open(source, "manageHealthCareTeam", windowspecs);
                })
            })
        </script>

    </head>

    <body id="formBPMH">

    <!--  HEADING  -->
    <!-- FORM BEGIN -->
    <html:form action="/formBPMH.do">
        <html:hidden property="demographicNo"/>
        <html:hidden property="formId"/>
        <header>

            <h1><bean:message key="colcamex.formBPMH.title"/></h1>

            <c:set var="controls" value="on" scope="page"/>

            <div id="bpmhId">

                <c:if test="${empty bpmh.provider}">
                    <span class="red">
                        <fmt:message key="colcamex.formBPMH.preparedby"/>
                        <fmt:message key="colcamex.formBPMH.error.unknown"/>
                    </span>
                </c:if>

                <c:if test="${not empty bpmh.provider}">
                    <span>
                        <fmt:message key="colcamex.formBPMH.preparedby"/>
                        <c:out value="${bpmh.provider.formattedName}"/>
                        <c:choose>
                            <c:when test="${not empty bpmh.provider.ohipNo}">
                                &#40;<c:out value="${bpmh.provider.ohipNo}"/>&#41;
                            </c:when>
                            <c:otherwise>
                                &#40;<fmt:message key="colcamex.formBPMH.error.unknown"/>&#41;
                            </c:otherwise>
                        </c:choose>
                    </span>
                </c:if>

                <c:choose>
                    <c:when test="${empty bpmh.formDateFormatted}">
                        <span class="red">
                    </c:when>
                    <c:otherwise>
                        <span>
                    </c:otherwise>
                </c:choose>

					<bean:message key="colcamex.formBPMH.preparedon"/>
					<bean:write name="bpmh" property="formDateFormatted"/>
				</span>
									
				<span>				
					<bean:message key="colcamex.formBPMH.printedon"/>
					<bean:write name="bpmh" property="editDateFormatted"/>
				</span>

            </div>
        </header>

        <!-- SUB HEADING -->
        <section id="subHeader">

            <!--  PATIENT  -->
            <table id="patientId">
                <tr>
                    <th colspan="6"><bean:message key="colcamex.formBPMH.patient"/></th>
                </tr>
                <tr>
                    <td rowspan="2" class="columnTitle"><bean:message key="colcamex.formBPMH.patient.name"/></td>
                    <td rowspan="2">
                        <bean:write name="bpmh" property="demographic.fullName"/>
                    </td>
                    <td class="columnTitle"><bean:message key="colcamex.formBPMH.patient.insurance"/></td>
                    <td>
                        <bean:write name="bpmh" property="demographic.hin"/>
                    </td>
                    <td class="columnTitle"><bean:message key="colcamex.formBPMH.patient.gender"/></td>
                    <td>
                        <bean:write name="bpmh" property="demographic.sex"/>
                    </td>
                </tr>

                <tr>
                    <td class="columnTitle"><bean:message key="colcamex.formBPMH.patient.dob"/></td>
                    <td>
                        <bean:write name="bpmh" property="demographic.formattedDob"/>
                    </td>
                    <td class="columnTitle"><bean:message key="colcamex.formBPMH.patient.phone"/></td>
                    <td>
                        <bean:write name="bpmh" property="demographic.phone"/>
                    </td>
                </tr>
            </table>
            <table id="allergies">
                <tr>
                    <td class="columnTitle">
                        <bean:message key="colcamex.formBPMH.patient.allergies"/>
                    </td>
                    <td>
                        <c:if test="${not empty bpmh.allergiesString}">
                            <c:out value="${bpmh.allergiesString}"/>
                        </c:if>
                    </td>
                </tr>
            </table>
        </section>

        <!-- FAMILY PHYSICIAN -->
        <section id="familyPhysician">
            <html:hidden name="bpmh" property="familyDrContactId" styleId="familyDrContactId"/>
            <table id="providerId">
                <tr>
                    <th colspan="7"><bean:message key="colcamex.formBPMH.familyDr"/></th>
                </tr>
                <tr>
                    <td class="columnTitle"><fmt:message key="colcamex.formBPMH.familyDr.name"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${empty bpmh.familyDrName}">
                                <span class="red">Unknown</span>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${bpmh.familyDrName}"/>
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td class="columnTitle"><fmt:message key="colcamex.formBPMH.familyDr.phone"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${empty bpmh.familyDrPhone}">
                                <span class="red">Unknown</span>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${bpmh.familyDrPhone}"/>
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td class="columnTitle"><fmt:message key="colcamex.formBPMH.familyDr.fax"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${empty bpmh.familyDrFax}">
                                <span class="red">Unknown</span>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${bpmh.familyDrFax}"/>
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <c:if test="${bpmh.formId == 0 && not empty bpmh.familyDrContactId}">
                        <td class="columnTitle" style="text-align:center;background-color:#CCC;border-top:#ccc thin solid;">
                            <input type="button" id="editFamilyDr" value="edit"/>
                        </td>
                    </c:if>
                </tr>

            </table>
        </section>

        <!-- DRUG DATA -->
        <section id="drugData">
            <table id="drugtable">

                <tr>
                    <th colspan="4"><bean:message key="colcamex.formBPMH.drugs"/></th>
                </tr>
                <tr id="drugtableSubheading">

                    <td class="columnTitle">
                        <bean:message key="colcamex.formBPMH.drugs.what"/><br/>
                        <span class="smallText">
							<bean:message key="colcamex.formBPMH.drugs.what.sub"/>
						</span>
                    </td>
                    <td class="columnTitle">
                        <bean:message key="colcamex.formBPMH.drugs.how"/><br/>
                        <span class="smallText">
							<bean:message key="colcamex.formBPMH.drugs.how.sub"/>
						</span>
                    </td>
                    <td class="columnTitle">
                        <bean:message key="colcamex.formBPMH.drugs.why"/><br/>
                        <span class="smallText">
							<bean:message key="colcamex.formBPMH.drugs.why.sub"/>
						</span>
                    </td>
                    <td class="columnTitle">
                        <bean:message key="colcamex.formBPMH.drugs.instructions"/><br/>
                        <span class="smallText">
							<bean:message key="colcamex.formBPMH.drugs.instructions.sub"/>
						</span>
                    </td>
                </tr>
                <c:set value="missingDrugData" var="false"/>
                <c:if test="${not empty bpmh.drugs}">
                    <c:forEach var="drugs" items="${bpmh.drugs}" varStatus="status">
                        <tr>
                            <html:hidden name="drugs" property="id" indexed="true"/>

                            <td>
                                <!-- WHAT -->
                                <c:out value="${drugs.what}"/>
                            </td>

                            <!-- HOW -->
                            <c:choose>
                                <c:when test="${empty drugs.how}">
                                    <c:set var="missingDrugData" value="true"/>
                                    <td style="border:red medium solid;">&nbsp;</td>
                                </c:when>
                                <c:otherwise>
                                    <td><c:out value="${drugs.how}"/></td>
                                </c:otherwise>
                            </c:choose>

                            <!-- WHY -->
                            <c:choose>
                                <c:when test="${empty drugs.why}">
                                    <c:set var="missingDrugData" value="true"/>
                                    <td style="border:red medium solid;">&nbsp;</td>
                                </c:when>
                                <c:otherwise>
                                    <td><c:out value="${drugs.why}"/></td>
                                </c:otherwise>
                            </c:choose>

                            <!-- INSTRUCTION -->
                            <td>
                                <c:choose>
                                    <c:when test="${not empty drugs.instruction}">
                                        <c:out value="${drugs.instruction}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <html:textarea name="drugs" indexed="true" property="instruction">&nbsp;</html:textarea>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </c:if>
            </table>
        </section>
        <section id="declaration">
            <table>
                <tr>
                    <td>
                        <html:checkbox name="bpmh" property="confirm" value="checked"/>
                        <label for="confirm"><bean:message key="colcamex.formBPMH.confirm"/></label>
                    </td>
                </tr>
            </table>
        </section>
        <section id="note">
            <table>
                <tr>
                    <td>
                        <bean:message key="colcamex.formBPMH.notes"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <c:choose>
                            <c:when test="${not empty bpmh.note}">
                                <c:out value="${bpmh.note}"/>&nbsp;
                            </c:when>
                            <c:otherwise>
                                <html:textarea property="note"> &nbsp;</html:textarea>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </table>
        </section>

        <section id="controls">
            <table>
                <tr>
                    <td>
                        <c:if test="${ missingDrugData }">
                            <c:set var="controls" value="off" scope="page"/>
                            <span style="color:red;">Missing Medication Data</span>
                        </c:if>
                        <c:choose>
                            <c:when test="${empty bpmh.allergiesString}">
                                <c:set var="controls" value="off" scope="page"/>
                                <span style="color:red;">Allergy Notation is Required (ie: NKDA)</span>
                            </c:when>
                        </c:choose>

                        <c:choose>
                            <c:when test="${empty bpmh.provider}">
                                <c:set var="controls" value="off" scope="page"/>
                                <span class="red">
                                    <fmt:message key="colcamex.formBPMH.error.missing.provider"/>
                                </span>
                            </c:when>
                        </c:choose>


                        <c:if test="${ controls eq 'on' }">

                            <html:submit property="method">
                                <bean:message key="colcamex.formBPMH.print"/>
                            </html:submit>

                            <c:if test="${bpmh.formId == 0}">
                                <button type="submit" name="method">
                                    <fmt:message key="colcamex.formBPMH.save"/>
                                </button>
                            </c:if>


                        </c:if>

                        <c:if test="${not empty saved}">
                            <c:forEach var="message" items="${saved}">
                                <div class="messages">
                                    <c:out value="${message}" escapeXml="false"/>
                                </div>
                            </c:forEach>
                        </c:if>

                    </td>
                </tr>
            </table>
        </section>
    </html:form>

    <!-- FOOTER -->
    <footer>
        <span><bean:message key="colcamex.formBPMH.formowner"/></span>
    </footer>

    </body>
</html:html>