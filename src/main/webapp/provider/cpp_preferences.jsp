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

<%@ include file="/casemgmt/taglibs.jsp" %>
<fmt:setBundle basename="oscarResources"/>
<%@ page import="ca.openosp.openo.provider.web.CppPreferencesUIBean" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%
    String curUser_no;
    curUser_no = (String) session.getAttribute("user");

    boolean bFirstLoad = request.getAttribute("status") == null;

    CppPreferencesUIBean bean = (CppPreferencesUIBean) request.getAttribute("bean");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>
<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
        <title><fmt:message key="provider.cppPrefs"/></title>

        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/oscarEncounter/encounterStyles.css">
        <link rel="stylesheet" type="text/css" media="all" href="<c:out value="${ctx}"/>/share/calendar/calendar.css"
              title="win2k-cold-1">
        <script src="<c:out value="${ctx}"/>/share/javascript/prototype.js" type="text/javascript"></script>
        <script src="<c:out value="${ctx}"/>/share/javascript/scriptaculous.js" type="text/javascript"></script>
        <script src="<c:out value="${ctx}"/>/js/jquery.js"></script>
        <script>
            jQuery.noConflict();
        </script>

        <script type="text/javascript">

            function validate() {
                //make sure none of the positions are duplicates
                if (getTotalPos("R1I1") > 1) {
                    alert("You have a duplicate for Row 1, Column 1..Please fix.");
                    return false;
                }
                if (getTotalPos("R1I2") > 1) {
                    alert("You have a duplicate for Row 1, Column 2..Please fix.");
                    return false;
                }
                if (getTotalPos("R2I1") > 1) {
                    alert("You have a duplicate for Row 2, Column 1..Please fix.");
                    return false;
                }
                if (getTotalPos("R2I2") > 1) {
                    alert("You have a duplicate for Row 2, Column 2..Please fix.");
                    return false;
                }
                return true;
            }

            function getTotalPos(value) {
                var total = 0;
                jQuery("select").each(function () {
                    if (jQuery(this).val() == value) {
                        total++;
                    }
                });
                return total;
            }
        </script>

    </head>

    <body class="BodyStyle" vlink="#0000FF">

    <table class="MainTable" id="scrollNumber1" name="encounterTable">
        <tr class="MainTableTopRow">
            <td class="MainTableTopRowLeftColumn"><fmt:message key="provider.setNoteStaleDate.msgPrefs"/></td>
            <td style="color: white" class="MainTableTopRowRightColumn"><fmt:message key="provider.cppPrefs"/></td>
        </tr>
        <tr>
            <td class="MainTableLeftColumn">&nbsp;</td>
            <td class="MainTableRightColumn">
                <!-- form starts here -->
                <form action="<c:out value="${ctx}"/>/provider/CppPreferences.do?method=save" method="post"
                      onSubmit="return validate();">
                    <table width="100%" border="1">
                        <tr>
                            <td colspan="2">
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Enable Custom EChart", CppPreferencesUIBean.ENABLE, bean.getEnable())))%>
                            </td>

                        </tr>
                        <tr>
                            <td>Social History</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.SOCIAL_HISTORY_POS))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getPositionSelect(bean.getSocialHxPosition())))%>
                                </select>
                                <br/>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Start Date", CppPreferencesUIBean.SOC_HX_START_DATE, bean.getSocialHxStartDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Resolution Date", CppPreferencesUIBean.SOC_HX_RES_DATE, bean.getSocialHxResDate())))%>
                            </td>
                        </tr>

                        <tr>
                            <td>Medical History</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.MEDICAL_HISTORY_POS))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getPositionSelect(bean.getMedicalHxPosition())))%>
                                </select>
                                <br/>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Start Date", CppPreferencesUIBean.MED_HX_START_DATE, bean.getMedHxStartDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Resolution Date", CppPreferencesUIBean.MED_HX_RES_DATE, bean.getMedHxResDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Treatment", CppPreferencesUIBean.MED_HX_TREATMENT, bean.getMedHxTreatment())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Procedure Date", CppPreferencesUIBean.MED_HX_PROCEDURE_DATE, bean.getMedHxProcedureDate())))%>
                            </td>
                        </tr>

                        <tr>
                            <td>Ongoing Concerns</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.ONGOING_CONCERNS_POS))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getPositionSelect(bean.getOngoingConcernsPosition())))%>
                                </select>
                                <br/>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Start Date", CppPreferencesUIBean.ONGOING_START_DATE, bean.getOngoingConcernsStartDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Resolution Date", CppPreferencesUIBean.ONGOING_RES_DATE, bean.getOngoingConcernsResDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Problem Status", CppPreferencesUIBean.ONGOING_PROBLEM_STATUS, bean.getOngoingConcernsProblemStatus())))%>
                            </td>
                        </tr>

                        <tr>
                            <td>Reminders</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.REMINDERS_POS))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getPositionSelect(bean.getRemindersPosition())))%>
                                </select>
                                <br/>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Start Date", CppPreferencesUIBean.REMINDERS_START_DATE, bean.getRemindersStartDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Resolution Date", CppPreferencesUIBean.REMINDERS_RES_DATE, bean.getRemindersResDate())))%>
                            </td>
                        </tr>

                        <tr>
                            <td>Preventions</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.PREVENTIONS_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getPreventionsDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>Disease Registry</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.DX_REGISTRY_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getDxRegistryDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>Forms</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.FORMS_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getFormsDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>eForms</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.EFORMS_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getEformsDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>Documents</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.DOCUMENTS_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getDocumentsDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>Lab Result</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.LABS_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getLabsDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>Measurements</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.MEASUREMENTS_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getMeasurementsDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>Consultations</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.CONSULTATIONS_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getConsultationsDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>HRM Documents</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.HRM_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getHrmDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>Allergies</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.ALLERGIES_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getAllergiesDisplay())))%>
                                </select>
                                <br/>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Start Date", CppPreferencesUIBean.ALLERGY_START_DATE, bean.getAllergyStartDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Severity", CppPreferencesUIBean.ALLERGY_SEVERITY, bean.getAllergySeverity())))%>

                            </td>
                        </tr>

                        <tr>
                            <td>Medications</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.MEDICATIONS_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getMedicationsDisplay())))%>
                                </select>
                                <br/>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Start Date", CppPreferencesUIBean.MEDICATION_START_DATE, bean.getMedicationStartDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show End Date", CppPreferencesUIBean.MEDICATION_END_DATE, bean.getMedicationEndDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Qty", CppPreferencesUIBean.MEDICATION_QTY, bean.getMedicationQty())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Repeats", CppPreferencesUIBean.MEDICATION_REPEATS, bean.getMedicationRepeats())))%>

                            </td>
                        </tr>

                        <tr>
                            <td>Other Meds</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.OTHER_MEDS_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getOtherMedsDisplay())))%>
                                </select>
                                <br/>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Start Date", CppPreferencesUIBean.OTHER_MEDS_START_DATE, bean.getOtherMedsStartDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Resolution Date", CppPreferencesUIBean.OTHER_MEDS_RES_DATE, bean.getOtherMedsResDate())))%>
                            </td>
                        </tr>

                        <tr>
                            <td>Risk Factors</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.RISK_FACTORS_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getRiskFactorsDisplay())))%>
                                </select>
                                <br/>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Start Date", CppPreferencesUIBean.RISK_FACTORS_START_DATE, bean.getRiskFactorsStartDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Resolution Date", CppPreferencesUIBean.RISK_FACTORS_RES_DATE, bean.getRiskFactorsResDate())))%>
                            </td>

                        </tr>

                        <tr>
                            <td>Family History</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.FAMILY_HISTORY_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getFamilyHxDisplay())))%>
                                </select>
                                <br/>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Start Date", CppPreferencesUIBean.FAMILY_HISTORY_START_DATE, bean.getFamilyHistoryStartDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Resolution Date", CppPreferencesUIBean.FAMILY_HISTORY_RES_DATE, bean.getFamilyHistoryResDate())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Treatment", CppPreferencesUIBean.FAMILY_HISTORY_TREATMENT, bean.getFamilyHistoryTreatment())))%>
                                <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getCheckbox("Show Relationship", CppPreferencesUIBean.FAMILY_HISTORY_RELATIONSHIP, bean.getFamilyHistoryRelationship())))%>

                            </td>
                        </tr>

                        <tr>
                            <td>Unresolved Issues</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.UNRESOLVED_ISSUES_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getUnresolvedIssuesDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>Resolved Issues</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.RESOLVED_ISSUES_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getResolvedIssuesDisplay())))%>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>Episodes</td>
                            <td>
                                <select name="<%=Encode.forHtmlAttribute(String.valueOf(CppPreferencesUIBean.EPISODES_DSP))%>">
                                    <%=Encode.forHtml(String.valueOf(CppPreferencesUIBean.getDisplaySelect(bean.getEpisodesDisplay())))%>
                                </select>
                            </td>
                        </tr>
                    </table>
                    <input type="submit" value="Save Changes"/>
                </form>
                <!-- end of form -->
            </td>
        </tr>
        <tr>
            <td class="MainTableBottomRowLeftColumn"></td>
            <td class="MainTableBottomRowRightColumn"></td>
        </tr>
    </table>
    </body>
</html>
