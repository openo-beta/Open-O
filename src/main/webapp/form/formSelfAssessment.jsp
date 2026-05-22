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

<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="ca.openosp.openo.form.FrmRecord" %>
<%@ page import="ca.openosp.openo.form.FrmRecordFactory" %>
<%@ page import="ca.openosp.openo.utility.LocaleUtils" %>
<%@ page import="ca.openosp.openo.commn.dao.DemographicDao" %>
<%@ page import="ca.openosp.openo.commn.model.Demographic" %>
<%@ page import="ca.openosp.openo.PMmodule.dao.ProviderDao" %>
<%@ page import="ca.openosp.openo.utility.SpringUtils" %>
<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%
    String formClass = "SelfAssessment";
    String formLink = "formSelfAssessment.jsp";
    String projectHome = request.getContextPath().substring(1);

    int formId = Integer.parseInt(request.getParameter("formId"));
    int demoNo = Integer.parseInt(request.getParameter("demographic_no"));

    FrmRecord rec = (new FrmRecordFactory()).factory(formClass);
    java.util.Properties props = rec.getFormRecord(LoggedInInfo.getLoggedInInfoFromSession(request), demoNo, formId);

    DemographicDao demoDao = (DemographicDao) SpringUtils.getBean(DemographicDao.class);
    Demographic demo = demoDao.getDemographic(request.getParameter("demographic_no"));
    String demoName = demo.getFormattedName();

    ProviderDao providerDao = (ProviderDao) SpringUtils.getBean(ProviderDao.class);
    String providerNo = props.getProperty("provider_no", "");
    String providerName = "";
    if (providerNo != null && !providerNo.isEmpty() && !providerNo.equals("999998")) {
        providerName = providerDao.getProviderName(providerNo);
    } else {
        providerName = LocaleUtils.getMessage(request.getLocale(), "oscarEncounter.formCounseling.notValidated");
    }
    props.setProperty("doc_name", providerName);

%>

<html>
    <% response.setHeader("Cache-Control", "no-cache");%>

    <HEAD>
        <META HTTP-EQUIV="CONTENT-TYPE" CONTENT="text/html; charset=iso-8859-1">

        <TITLE>Self-Assessment Intake - v1</TITLE>
    </HEAD>

    <body bgproperties="fixed" topmargin="0" leftmargin="0" rightmargin="0" onload="window.resizeTo(768,768)" bgcolor="#eeeeee">

    <form action="${pageContext.request.contextPath}/form/formname.do" method="post">
        <input type="hidden" name="demographic_no" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("demographic_no", "0")))%>"/>
        <input type="hidden" name="formCreated" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("formCreated", "")))%>"/>
        <input type="hidden" name="form_class" value="<%=formClass%>"/>
        <input type="hidden" name="form_link" value="<%=formLink%>"/>
        <input type="hidden" name="formId" value="<%=Encode.forHtmlAttribute(String.valueOf(formId))%>"/>
        <input type="hidden" name="demographic_no" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("demographic_no", "0")))%>"/>
        <input type="hidden" name="doc_name" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("doc_name", "")))%>"/>
        <input type="hidden" name="submit" value="exit"/>

        <table cellpadding="0" cellspacing="0" border="1" align="center" width="803" bordercolor="#000001">
            <COL WIDTH=433>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP BGCOLOR="#000000">
                    <OL>
                        <LI><P><font color="#ffffff" STYLE="font-size: 14pt" FACE="Arial, serif"><B>ID</B></FONT></P>
                        </LI>
                    </OL>
                </TD>
            </TR>
            <TR VALIGN=TOP>
                <TD WIDTH=335>
                    <p>Name: <input type="text" name="name" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("name", "")))%>" readonly></FONT>
                    </P>
                    <p>Sex: <input type="text" name="sex" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("sex", "")))%>" readonly></FONT>
                    </P>
                    <p>DOB: <input type="text" name="p_birthdate" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("p_birthdate", "")))%>"
                                   readonly></FONT></P>
                </TD>
                <TD WIDTH=433>
                    <p>Faculty: <input type="text" name="faculty" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("faculty", "")))%>"></FONT>
                    </P>
                    <p>Academic Year: <input type="text" name="AcademicYear"
                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("AcademicYear", "")))%>"></FONT></P>
                    <p>Part-time or Full-time: <input type="text" name="PTFT"
                                                      value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PTFT", "")))%>"></FONT></P>
                    <p>Do you have a job: <input type="text" name="Job" size="25" maxlength="200"
                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Job", "")))%>"></FONT></P>
                    <p>Average hours of Work/Week: <input type="text" name="Hours" size="25" maxlength="200"
                                                          value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Hours", "")))%>"></FONT></P>
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP BGCOLOR="#000000"></td>
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP>
                    Living Situation:<br>
                    Residence: <input type="text" name="Residence" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Residence", "")))%>">
                    <br>
                    Off Campus: <input type="text" name="Campus" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Campus", "")))%>"> <br>
                    At Home: <input type="text" name="Home" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Home", "")))%>"> <br>
                    Number of Roommates: <input type="text" name="Roommates" size="5" maxlength="50"
                                                value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Roommates", "")))%>"><br>
                    Other: <input type="text" name="LivingSituationOther" size="100" maxlength="250"
                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("LivingSituationOther", "")))%>">
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP BGCOLOR="#000000">
                    <OL START=3>
                        <LI><P><font color="#ffffff" STYLE="font-size: 14pt" FACE="Arial, serif">Please check the boxes
                            that best describe the reasons you have come to counselling: </FONT></P></LI>
                        <LI><P><font color="#ffffff" STYLE="font-size: 14pt" FACE="Arial, serif">Please complete to your
                            comfort level: </FONT></P></LI>
                    </OL>
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP>
                    Depression/Self-Esteem: <input type="text" name="Depression"
                                                   value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Depression", "")))%>"> <br>
                    Feelings of helplessness/hopelessness: <input type="text" name="helplessness"
                                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("helplessness", "")))%>">
                    <br>
                    ADHD: <input type="text" name="ADHD" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ADHD", "")))%>"> <br>
                    Obsessions/Compulsions: <input type="text" name="Obsessions"
                                                   value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Obsessions", "")))%>"> <br>
                    Bipolar Mood Disorder/Manic Depressive Illness: <input type="text" name="Bipolar"
                                                                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Bipolar", "")))%>">
                    <br>
                    Anxiety: <input type="text" name="Anxiety" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Anxiety", "")))%>"> <br>
                    Self-Esteem: <input type="text" name="Esteem" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Esteem", "")))%>"> <br>
                    Relationship Difficulties: <input type="text" name="Relationship"
                                                      value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Relationship", "")))%>"> <br>

                    Eating Problems: <input type="text" name="Eating" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Eating", "")))%>">
                    <br>
                    Sexual Issues: <input type="text" name="Sexual" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Sexual", "")))%>"> <br>
                    Suicidal Thoughts: <input type="text" name="Suicidal"
                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Suicidal", "")))%>"> <br>
                    Psychosis, hearing voices, hallucinations: <input type="text" name="Psychosis"
                                                                      value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Psychosis", "")))%>">
                    <br>
                    Mania: <input type="text" name="Mania" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Mania", "")))%>"> <br>
                    Grief: <input type="text" name="Grief" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Grief", "")))%>"> <br>
                    Substance Abuse: <input type="text" name="Substance"
                                            value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Substance", "")))%>"> <br>
                    Trauma:<br>
                    <UL>
                        <LI>Emotional: <input type="text" name="TraumaEmotional"
                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("TraumaEmotional", "")))%>"></LI>
                        <LI>Physical: <input type="text" name="TraumaPhysical"
                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("TraumaPhysical", "")))%>"></LI>
                        <LI>Sexual: <input type="text" name="TraumaSexual"
                                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("TraumaSexual", "")))%>"></LI>

                    </UL>
                    Academic Difficulties: <input type="text" name="Academic"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Academic", "")))%>"> <br>
                    Other please explain: <br>
                    <input type="text" name="ReasonsOther" size="100" maxlength="200"
                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ReasonsOther", "")))%>">
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP BGCOLOR="#000000">
                    <OL START=4>
                        <LI><P><FONT COLOR="#ffffff"><B>PAST MEDICAL HISTORY</B></FONT></P></LI>
                    </OL>
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP>
                    Hospitalizations: <br>
                    When (Date YYYY-MM-DD) and why: <br>
                    <textarea name="Hospitalizations" id="Hospitalizations" rows="0"
                              cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("Hospitalizations", "")))%></textarea><br>

                    Surgery: <br>
                    <textarea name="Surgery" id="Surgery" rows="0"
                              cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("Surgery", "")))%></textarea><br>

                    Medical illnesses: <br><textarea name="Medicalillnesses" id="Medicalillnesses" rows="0"
                                                     cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("Medicalillnesses", "")))%></textarea><br>

                    Are you currently taking medication: <input type="text" name="CurrentMedications"
                                                                value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("CurrentMedications", "")))%>">
                    <br>
                    Please list:<br><textarea name="CurrentMedicationsList" id="CurrentMedicationsList" rows="0"
                                              cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("CurrentMedicationsList", "")))%></textarea><br>

                    Have you previously taken psychiatric medication? <input type="text" name="psychiatricMedications"
                                                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("psychiatricMedications", "")))%>">
                    <br>
                    Please list:<br><textarea name="psychiatricMedicationsList" id="psychiatricMedicationsList" rows="0"
                                              cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("psychiatricMedicationsList", "")))%></textarea><br>

                    Please enter additional information:<br><textarea name="HospitalizationsOther"
                                                                      id="HospitalizationsOther" rows="0"
                                                                      cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("HospitalizationsOther", "")))%></textarea><br>
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP BGCOLOR="#000000">
                    <OL START=5>
                        <LI><P><FONT COLOR="#ffffff"><B>PAST PSYCHIATRIC HISTORY</B></FONT></P></LI>
                    </OL>
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP>
                    Substance Abuse: <input type="text" name="PastSubstance"
                                            value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastSubstance", "")))%>"> <br>
                    Alcohol: <input type="text" name="PastAlcohol" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastAlcohol", "")))%>">
                    <br>
                    Prescribed Drugs: <input type="text" name="PastPrescribedDrugs"
                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastPrescribedDrugs", "")))%>"> <br>
                    Over the Counter Medications: <input type="text" name="PastCounterMedications"
                                                         value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastCounterMedications", "")))%>">
                    <br>
                    Street Drugs: <input type="text" name="PastStreetDrugs"
                                         value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastStreetDrugs", "")))%>"> <br>
                    Tobacco: <input type="text" name="PastTobacco" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastTobacco", "")))%>">
                    <br>
                    Trauma:
                    <UL>
                        <LI>Emotional: <input type="text" name="PastPSYCHIATRICTraumaEmotional"
                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastPSYCHIATRICTraumaEmotional", "")))%>">
                        </LI>
                        <LI>Physical: <input type="text" name="PastPSYCHIATRICTraumaPhysical"
                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastPSYCHIATRICTraumaPhysical", "")))%>"></LI>
                        <LI>Sexual: <input type="text" name="PastPSYCHIATRICTraumaSexual"
                                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastPSYCHIATRICTraumaSexual", "")))%>"></LI>
                    </UL>
                    Legal Problems: <input type="text" name="PastLegal"
                                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastLegal", "")))%>"> <br>
                    Gambling Addiction: <input type="text" name="PastGambling"
                                               value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastGambling", "")))%>"> <br>
                    Allergies/Reactions to Psychiatric Medications: <input type="text" name="PastReactionsMedication"
                                                                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastReactionsMedication", "")))%>">
                    <br>

                    Medication Name: <br><textarea name="PastReactionsMedicationList" id="PastReactionsMedicationList"
                                                   rows="0"
                                                   cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("PastReactionsMedicationList", "")))%></textarea><br>

                    Suicide Attempts: <input type="text" name="PastSuicideAttempts"
                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastSuicideAttempts", "")))%>"> <br>
                    How Many?<br><textarea name="PastSuicideMany" id="PastSuicideMany" rows="0"
                                           cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("PastSuicideMany", "")))%></textarea><br>
                    When?<br><textarea name="PastSuicideWhen" id="PastSuicideWhen" rows="0"
                                       cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("PastSuicideWhen", "")))%></textarea><br>

                    Self-Harm/Cutting: <input type="text" name="PastCutting"
                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("PastCutting", "")))%>"> <br>
                    Post traumatic stress disorder?: <input type="text" name="ptsd"
                                                            value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ptsd", "")))%>"> <br>

                    Please enter additional information: <br><textarea name="PastPASTPSYCHIATRICOther"
                                                                       id="PastPASTPSYCHIATRICOther" rows="0"
                                                                       cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("PastPASTPSYCHIATRICOther", "")))%></textarea><br>
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP BGCOLOR="#000000">
                    <OL START=6>
                        <LI><P><FONT FACE="Arial, serif" COLOR="#ffffff" SIZE=2 STYLE="font-size: 9pt"><FONT><B>Immediate
                            Family Members</B></FONT></P></LI>
                    </OL>
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=785 VALIGN=TOP>
                    Ages:
                    Mother: <input type="text" name="AgesMother" size="5" maxlength="50"
                                   value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("AgesMother", "")))%>"><br>
                    Father: <input type="text" name="AgesFather" size="5" maxlength="50"
                                   value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("AgesFather", "")))%>"><br>
                    Siblings: <input type="text" name="AgesSiblings" size="30" maxlength="100"
                                     value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("AgesSiblings", "")))%>"><br>
                    Others: <input type="text" name="AgesOthers" size="100" maxlength="100"
                                   value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("AgesOthers", "")))%>"><br>
                    Are You Adopted?: <input type="text" name="Adopted" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Adopted", "")))%>">
                    <br>
                </TD>
            </TR>
            <TR>
                <TD width="778" colspan="2" valign="TOP" bgcolor="#000000">
                    <OL START=7>
                        <LI>
                            <P><FONT COLOR="#ffffff"><B>Family Psychiatric History (check all that apply to your
                                immediate family)</B></FONT></P>
                        </LI>
                    </OL>
                </TD>
            </TR>
            <TR>
                <TD WIDTH=778 colspan="2" VALIGN=TOP>
                    Depression: <input type="text" name="FamilyDepression"
                                       value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilyDepression", "")))%>"> <br>
                    Anxiety: <input type="text" name="FamilyAnxiety"
                                    value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilyAnxiety", "")))%>"> <br>
                    Substance Abuse: <input type="text" name="FamilySubstance"
                                            value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilySubstance", "")))%>"> <br>
                    Alcohol: <input type="text" name="FamilyAlcohol"
                                    value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilyAlcohol", "")))%>"> <br>
                    Drugs Specify:
                    <br><textarea name="FamilyDrugs" id="FamilyDrugs" rows="0"
                                  cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("FamilyDrugs", "")))%></textarea><br>
                    Trauma:
                    <UL>
                        <LI>Emotional: <input type="text" name="FamilyEmotional"
                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilyEmotional", "")))%>"></LI>
                        <LI>Physical: <input type="text" name="FamilyPhysical"
                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilyPhysical", "")))%>"></LI>
                        <LI>Sexual: <input type="text" name="FamilySexual"
                                           value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilySexual", "")))%>"></LI>
                    </UL>
                    Suicide: <input type="text" name="FamilySuicide"
                                    value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilySuicide", "")))%>"> <br>
                    Eating Disorder: <input type="text" name="FamilyEating"
                                            value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilyEating", "")))%>"> <br>
                    Bipolar Disorder: <input type="text" name="FamilyBipolar"
                                             value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilyBipolar", "")))%>"> <br>
                    Psychosis: <input type="text" name="FamilyPsychosis"
                                      value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilyPsychosis", "")))%>"> <br>
                    Schizophrenia: <input type="text" name="FamilySchizophrenia"
                                          value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilySchizophrenia", "")))%>"> <br>
                    ADHD: <input type="text" name="FamilyADHD" value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("FamilyADHD", "")))%>"> <br>

                    Please enter additional information: <br><textarea name="FamilyPsychiatricOther"
                                                                       id="FamilyPsychiatricOther" rows="0"
                                                                       cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("FamilyPsychiatricOther", "")))%></textarea><br>
                </TD>
            </TR>
            <TR>
                <TD WIDTH=778 colspan="2" VALIGN=TOP BGCOLOR="#000000">
                </TD>
            </TR>
            <TR>
                <TD WIDTH=778 colspan="2" VALIGN=TOP>
                    Are you a smoker at present? <input type="text" name="Smoker"
                                                        value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Smoker", "")))%>"> <br>
                    How much do you smoke? <input type="text" name="SmokeQty" size="100" maxlength="100"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("SmokeQty", "")))%>"> <br>
                    Do you use street drugs of any kind: <input type="text" name="StreetDrugs"
                                                                value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("StreetDrugs", "")))%>"> <br>
                    Do you drink alcohol? <input type="text" name="DrinkAlcohol"
                                                 value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrinkAlcohol", "")))%>"> <br>
                    On average, how many drinks do yo have per occasion? <input type="text" name="DrinkAlcoholMany"
                                                                                size="100" maxlength="100"
                                                                                value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrinkAlcoholMany", "")))%>"><br>
                    On average, how many drinks do yo have per week? <input type="text" name="DrinkAlcoholWeekly"
                                                                            size="100" maxlength="150"
                                                                            value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("DrinkAlcoholWeekly", "")))%>"><br>
                    Do you exercise weekly? <input type="text" name="Exercise"
                                                   value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Exercise", "")))%>"> <br>
                    Do you eat 3 meals a day? <input type="text" name="Meals"
                                                     value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("Meals", "")))%>"> <br>
                    Are you in a relationship? <input type="text" name="InRelationship"
                                                      value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("InRelationship", "")))%>"> <br>
                    Is your academic performance okay? <input type="text" name="AcademicPerformance"
                                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("AcademicPerformance", "")))%>">
                    <br>
                    Sexual Orientation:<input type="text" name="SexualOrientation"
                                              value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("SexualOrientation", "")))%>"> <br>
                    <br>
                    Religious Affiliation: <input type="text" name="ReligiousAffiliation" size="100" maxlength="150"
                                                  value="<%=Encode.forHtmlAttribute(String.valueOf(props.getProperty("ReligiousAffiliation", "")))%>"> <br>
                    Please enter additional information: <br><textarea name="GeneralOther" id="GeneralOther" rows="0"
                                                                       cols="111"><%=Encode.forHtml(String.valueOf(props.getProperty("GeneralOther", "")))%></textarea><br>

                </TD>
            </TR>

            <TR>
                <TD WIDTH=778 colspan="2" VALIGN=TOP>
                    <br>
                    <br>
                </TD>
            </TR>

        </TABLE>


        <div align="center" id="buttons">
            <input id="savebut" type="submit" value="Save" onclick="javascript: return onSave();"/>
            <input id="saveexitbut" type="submit" value="Save and Exit" onclick="javascript: return onSaveExit();"/>
            <input id="exitbut" type="submit" value="Exit" onclick="javascript: return onExit();"/>

        </div>

        </body>

        <script type="text/javascript">

            function onSave() {
                document.forms[0].submit.value = "save";
                ret = confirm("Are you sure you want to save this form?");
                return ret;
            }

            function onSaveExit() {
                document.forms[0].submit.value = "exit";
                ret = confirm("Are you sure you wish to save and close this window?");
                return ret;
            }

            function onExit() {
                if (confirm("Are you sure you wish to exit without saving your changes?") == true) {
                    window.close();
                }
                return (false);
            }

        </script>
    </form>
</html>
