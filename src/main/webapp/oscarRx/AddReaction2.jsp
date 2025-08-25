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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%
    String roleName2$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed = true;
%>
<security:oscarSec roleName="<%=roleName2$%>" objectName="_allergy" rights="r" reverse="<%=true%>">
    <%authed = false; %>
    <%response.sendRedirect("../securityError.jsp?type=_allergy");%>
</security:oscarSec>
<%
    if (!authed) {
        return;
    }
%>

<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="AddReaction.title"/></title>
        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">

        <c:if test="${empty RxSessionBean}">
            <% response.sendRedirect("error.html"); %>
        </c:if>
        <c:if test="${not empty RxSessionBean}">
            <c:set var="bean" value="${RxSessionBean}" scope="page"/>
            <c:if test="${bean.valid == false}">
                <% response.sendRedirect("error.html"); %>
            </c:if>
        </c:if>

        <%
            oscar.oscarRx.pageUtil.RxSessionBean bean = (oscar.oscarRx.pageUtil.RxSessionBean) pageContext.findAttribute("bean");
            RxPatientData.Patient patient = (RxPatientData.Patient) request.getSession().getAttribute("Patient");
            String name = (String) request.getAttribute("name");
            String type = (String) request.getAttribute("type");
            String drugrefId = (String) request.getAttribute("drugrefId");
            String allergyToArchive = (String) request.getAttribute("allergyToArchive");
            String nkdaId = (String) request.getAttribute("nkdaId");

            String reaction = new String();
            String startDate = new String();
            String ageOfOnset = new String();
            String lifeStage = new String();
            String severity = new String();
            String onsetOfReaction = new String();
            Boolean nonDrug = null;

            if (allergyToArchive != null && !allergyToArchive.isEmpty()) {
                org.oscarehr.common.model.Allergy a = patient.getAllergy(Integer.parseInt(allergyToArchive));
                if (a != null) {
                    reaction = a.getReaction();
                    startDate = a.getStartDateFormatted();
                    ageOfOnset = a.getAgeOfOnset();
                    lifeStage = a.getLifeStage();
                    severity = a.getSeverityOfReaction();
                    onsetOfReaction = a.getOnsetOfReaction();
                    nonDrug = a.isNonDrug();
                }
                if (a.getArchived() && nkdaId != null) allergyToArchive = nkdaId;
            } else {
                if (nkdaId != null) allergyToArchive = nkdaId;
            }

            boolean isNKDA = "No Known Drug Allergies".equals(name);
        %>

        <link rel="stylesheet" type="text/css" href="oscarRx/styles.css">

    </head>
    <body topmargin="0" leftmargin="0" vlink="#0000FF">

    <table border="0" cellpadding="0" cellspacing="0"
           style="border-collapse: collapse" bordercolor="#111111" width="100%"
           id="AutoNumber1" height="100%">
        <%@ include file="TopLinks.jsp"%><!-- Row One included here-->
        <tr>
            <%@ include file="SideLinksNoEditFavorites2.jsp"%><!-- <td></td>Side Bar File --->
            <td width="100%" style="border-left: 2px solid #A9A9A9;" height="100%"
                valign="top">
                <table cellpadding="0" cellspacing="2"
                       style="border-collapse: collapse" bordercolor="#111111" width="100%"
                       height="100%">
                    <tr>
                        <td width="0%" valign="top">
                            <div class="DivCCBreadCrumbs"><a href="SearchDrug3.jsp"> <fmt:setBundle basename="oscarResources"/><fmt:message key="SearchDrug.title"/></a>&nbsp;&gt;&nbsp; <a
                                    href="ShowAllergies2.jsp"> <fmt:setBundle basename="oscarResources"/><fmt:message key="EditAllergies.title"/></a>&nbsp;&gt;&nbsp; <b><fmt:setBundle basename="oscarResources"/><fmt:message key="AddReaction.title"/></b></div>
                        </td>
                    </tr>
                    <!----Start new rows here-->

                    <tr>
                        <td>
                            <div class="DivContentSectionHead"><%=name%>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td id="addAllergyDialogue"><form action="<%=request.getContextPath()%>/oscarRx/addAllergy2.do"
                                                               focus="reactionDescription">

                            <script type="text/javascript">
                                function checkStartDate() {
                                    var field = document.forms.RxAddAllergyForm.startDate;
                                    if (field.value.trim() != "") {
                                        var t = /^\d{4}(-[0-1]*\d{1}(-[0-3]*\d{1})*)*$/;
                                        var startDate = new Date(field.value);

                                        if (!t.test(field.value) || startDate == "Invalid Date") {
                                            alert("Invalid Start Date");
                                            setTimeout(function () {
                                                field.focus();
                                            }, 100);
                                        }
                                    }
                                }

                                function checkAgeOfOnset() {
                                    var field = document.forms.RxAddAllergyForm.ageOfOnset;
                                    if (field.value.trim() != "") {
                                        var t = /^\d{1,3}$/;
                                        if (!t.test(field.value)) {
                                            alert("Invalid Age of Onset (3-digit integer only)");
                                            setTimeout(function () {
                                                field.focus();
                                            }, 100);
                                        }
                                    }
                                }

                                function doSubmit() {

                                    if (document.forms.RxAddAllergyForm.nonDrug.value == '') {
                                        alert("Please choose value for non-drug");
                                        return false;
                                    }

                                    confirmRemoveNKDA();

                                    return true;
                                }

                                function confirmRemoveNKDA() {
                                    <% if (nkdaId!=null && !nkdaId.isEmpty()) { %>
                                    if (<%=nkdaId%>>
                                    0
                                )
                                    {
                                        var yes = confirm("Remove \"No Known Drug Allergies\" from list?");
                                        if (!yes) document.forms.RxAddAllergyForm.allergyToArchive.value = "";
                                    }
                                    <% } %>
                                }
                            </script>

                            <table>
                                <tr id="addReactionSubheading">
                                    <td>
                                        Adding Allergy: <%=name%>
                                    </td>
                                </tr>
                                <tr valign="center">
                                    <td>
                                        <span class="label">Comment: </span>
                                        <textarea name="reactionDescription" cols="40" rows="3">
                                                       <%=reaction%>
                                        </textarea>
                                        <input type="hidden" name="ID" value="<%=drugrefId%>"/>
                                        <input type="hidden" name="name" id="name" value="<%=name%>"/>
                                        <input type="hidden" name="allergyToArchive" id="allergyToArchive" value="<%=allergyToArchive%>"/>
                                    </td>
                                </tr>

                                <input type="hidden" name="type" id="type" value="<%=type%>"/>

                                <tr valign="center">
                                    <td>
                                        <link rel="stylesheet" type="text/css" media="all"
                                              href="<%= request.getContextPath() %>/share/calendar/calendar.css"
                                              title="win2k-cold-1"/>
                                        <script type="text/javascript"
                                                src="<%= request.getContextPath() %>/share/calendar/calendar.js"></script>
                                        <script type="text/javascript"
                                                src="<%= request.getContextPath() %>/share/calendar/lang/<fmt:setBundle basename="oscarResources"/><fmt:message key="global.javascript.calendar"/>"></script>
                                        <script type="text/javascript"
                                                src="<%= request.getContextPath() %>/share/calendar/calendar-setup.js"></script>
                                        <script type="text/javascript">
                                            Calendar.setup({
                                                inputField: "startDate",
                                                ifFormat: "%Y-%m-%d",
                                                showsTime: false,
                                                button: "startDateCal",
                                                singleClick: true,
                                                step: 1
                                            });
                                        </script>

                                        <span class="label">Start Date:</span>
                                        <input type="text" name="startDate" id="startDate" size="10" maxlength="10"
                                               value="<%=startDate%>" onblur="checkStartDate();"/>
                                        <img src="<%= request.getContextPath() %>/images/cal.gif" id="startDateCal">(yyyy-mm-dd OR yyyy-mm OR yyyy)
                                    </td>
                                </tr>

                                <tr valign="center">
                                    <td><span class="label">Age Of Onset:</span> <input type="text"
                                            name="ageOfOnset" size="4" maxlength="4" value="<%=ageOfOnset%>"
                                            onblur="checkAgeOfOnset();"/></td>

                                </tr>


                                <tr valign="center">
                                    <td><span class="label"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.lifestage.title"/>:</span>
                                        <select name="lifeStage" value="<%=lifeStage%>">
                                            <option value=""><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.lifestage.opt.notset"/></option>
                                            <option value="N"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.lifestage.opt.newborn"/></option>
                                            <option value="I"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.lifestage.opt.infant"/></option>
                                            <option value="C"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.lifestage.opt.child"/></option>
                                            <option value="T"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.lifestage.opt.adolescent"/></option>
                                            <option value="A"><fmt:setBundle basename="oscarResources"/><fmt:message key="oscarEncounter.lifestage.opt.adult"/></option>
                                        </select>
                                    </td>
                                </tr>

                                <% if (isNKDA) { %>
                                <input type="hidden" name="severityOfReaction" id="severityOfReaction" value="4"/>
                                <input type="hidden" name="onSetOfReaction" id="onSetOfReaction" value="4"/>
                                <% } else { %>
                                <tr valign="center">
                                    <td><span class="label">Severity Of Reaction:</span> <select
                                            name="severityOfReaction" value="<%=severity%>">
                                        <option value="4">Unknown</option>
                                        <option value="1">Mild</option>
                                        <option value="2">Moderate</option>
                                        <option value="3">Severe</option>
                                        <option value="5">No Reaction</option>
                                    </select></td>
                                </tr>

                                <tr valign="center">
                                    <td><span class="label">Onset Of Reaction:</span> <select
                                            name="onSetOfReaction" value="<%=onsetOfReaction%>">
                                        <option value="4">Unknown</option>
                                        <option value="1">Immediate</option>
                                        <option value="2">Gradual</option>
                                        <option value="3">Slow</option>
                                    </select></td>
                                </tr>

                                <%if (drugrefId == null || "0".equals(drugrefId) || "null".equals(drugrefId)) { %>
                                <tr valign="center">
                                    <td><span class="label">Non Drug Indicator:</span>
                                        <select name="nonDrug" id="nonDrug">
                                            <option value="">Select Below</option>
                                            <option value="on" <%=nonDrug != null && nonDrug.booleanValue() == true ? " selected=\"selected\" " : ""%>>
                                                Allergy to non-drug substance
                                            </option>
                                            <option value="off" <%=nonDrug != null && nonDrug.booleanValue() == false ? " selected=\"selected\" " : ""%>>
                                                Adverse reaction to drug
                                            </option>

                                        </select>
                                    </td>
                                </tr>


                                <%} %>


                                <% } %>

                                <tr>
                                    <td>
                                        <input type="submit" name="submit" value="Add Allergy" class="ControlPushButton" onclick="return doSubmit()"/>
                                        <input type=button class="ControlPushButton" id="cancelAddReactionButton"
                                               onclick="window.location='ShowAllergies2.jsp?demographicNo=<%=bean.getDemographicNo() %>'"
                                               value="Cancel"/>
                                    </td>
                                </tr>
                            </table>

                        </form></td>
                    </tr>

                    <tr>
                        <td>
                            <%
                                String sBack = "ShowAllergies2.jsp";
                            %> <input type=button class="ControlPushButton"
                                      onclick="window.location.href='<%=sBack%>';"
                                      value="Back to View Allergies"/></td>
                    </tr>
                    <!----End new rows here-->
                    <tr height="100%">
                        <td></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td height="0%"
                style="border-bottom: 2px solid #A9A9A9; border-top: 2px solid #A9A9A9;"></td>
            <td height="0%"
                style="border-bottom: 2px solid #A9A9A9; border-top: 2px solid #A9A9A9;"></td>
        </tr>
        <tr>
            <td width="100%" height="0%" colspan="2">&nbsp;</td>
        </tr>
        <tr>
            <td width="100%" height="0%" style="padding: 5" bgcolor="#DCDCDC"
                colspan="2"></td>
        </tr>
    </table>

    </body>

</html>
