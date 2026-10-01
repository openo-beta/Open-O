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
<%
    LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
    String providerNo = loggedInInfo.getLoggedInProviderNo();


    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%@ taglib uri="/WEB-INF/caisi-tag.tld" prefix="caisi" %>
<%@ taglib uri="/WEB-INF/oscar-tag.tld" prefix="oscar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.util.*" %>
<%@ page import="ca.openosp.OscarProperties" %>
<%@ page import="ca.openosp.openo.utility.SpringUtils" %>

<%@page import="ca.openosp.openo.commn.model.ProviderPreference" %>
<%@page import="ca.openosp.openo.web.admin.ProviderPreferencesUIBean" %>
<%@page import="ca.openosp.openo.utility.LoggedInInfo" %>
<%@page import="ca.openosp.openo.web.PrescriptionQrCodeUIBean" %>
<%@page import="ca.openosp.openo.commn.model.EForm" %>
<%@page import="ca.openosp.openo.commn.model.EncounterForm" %>
<%@page import="ca.openosp.openo.commn.dao.CtlBillingServiceDao" %>
<%@page import="java.util.List" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="ca.openosp.openo.managers.UserPropertyManager" %>
<%@ page import="ca.openosp.openo.commn.model.enumerator.UserPropertyKey" %>

<%!
    CtlBillingServiceDao ctlBillingServiceDao = SpringUtils.getBean(CtlBillingServiceDao.class);
    UserPropertyManager userPropertyManager = SpringUtils.getBean(UserPropertyManager.class);
%>
<%
    /*
     * Settings come from two tables:
     *  - property (UserProperty), read here as a map for the EL below
     *  - ProviderPreference
     */
    pageContext.setAttribute("userProperty", userPropertyManager.getAllUserProperties(loggedInInfo));
%>

<html>

    <head>
        <c:set var="ctx" value="${pageContext.request.contextPath}"/>
        <link rel="stylesheet" href="<%= request.getContextPath() %>/library/bootstrap/5.0.2/css/bootstrap.min.css"/>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <title><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.title"/></title>
        <script src="<%=request.getContextPath()%>/csrfguard" type="text/javascript"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/library/jquery/jquery-3.6.4.min.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/library/bootstrap/5.0.2/js/bootstrap.bundle.min.js"></script>
        <script language="JavaScript">

            $(document).ready(function () {
                $('#rxInteractionWarningLevel').change(function () {
                    $.post('<c:out value="${ctx}"/>/provider/rxInteractionWarningLevel.do', {method: "update", value: this.value});
                });
            });

            function setfocus() {
                this.focus();
                document.UPDATEPRE.mygroup_no.focus();
                document.UPDATEPRE.mygroup_no.select();
            }

            function upCaseCtrl(ctrl) {
                ctrl.value = ctrl.value.toUpperCase();
            }

            function checkTypeNum(typeIn) {
                var typeInOK = true;
                var i = 0;
                var length = typeIn.length;
                var ch;
                // walk through a string and find a number
                if (length >= 1) {
                    while (i < length) {
                        ch = typeIn.substring(i, i + 1);
                        if (ch == ".") {
                            i++;
                            continue;
                        }
                        if ((ch < "0") || (ch > "9")) {
                            typeInOK = false;
                            break;
                        }
                        i++;
                    }
                } else typeInOK = false;
                return typeInOK;
            }

            function checkTypeIn(obj) {
                if (!checkTypeNum(obj.value)) {
                    alert("<fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.msgMustBeNumber"/>");
                }
            }

            function checkTypeInAll() {
                var checkin = false;
                var s = 0;
                var e = 0;
                var i = 0;
                if (isNumeric(document.UPDATEPRE.start_hour.value) && isNumeric(document.UPDATEPRE.end_hour.value) && isNumeric(document.UPDATEPRE.every_min.value)) {
                    s = eval(document.UPDATEPRE.start_hour.value);
                    e = eval(document.UPDATEPRE.end_hour.value);
                    i = eval(document.UPDATEPRE.every_min.value);
                    if (e < 24) {
                        if (s < e) {
                            if (i <= (e - s) * 60 && i > 0) {
                                checkin = true;
                            } else {
                                alert("<fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.msgPositivePeriod"/>");
                                this.focus();
                                document.UPDATEPRE.every_min.focus();
                            }
                        } else {
                            alert("<fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.msgStartHourErlierEndHour"/>");
                            this.focus();
                            document.UPDATEPRE.start_hour.focus();
                        }
                    } else {
                        alert("<fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.msgHourLess24"/>");
                        this.focus();
                        document.UPDATEPRE.end_hour.focus();
                    }
                } else {
                    alert("<fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.msgTypeNumbers"/>");
                }
                return checkin;
            }

            function popupPage(vheight, vwidth, varpage) { //open a new popup window
                var page = "" + varpage;
                windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,top=5,left=5";//360,680
                var popup = window.open(page, "<fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.titlePopup"/>", windowprops);
                if (popup != null) {
                    if (popup.opener == null) {
                        popup.opener = self;
                    }
                }
            }

            function isNumeric(strString) {
                var validNums = "0123456789";
                var strChar;
                var retval = true;
                if (strString.length == 0) {
                    retval = false;
                }
                for (i = 0; i < strString.length && retval == true; i++) {
                    strChar = strString.charAt(i);
                    if (validNums.indexOf(strChar) == -1) {
                        retval = false;
                    }
                }
                return retval;
            }

            function showHideBillPref() {
                $("#billingONpref").toggle();
            }

            function showHideERxPref() {
                //$("eRxPref").toggle();
            }
        </script>
        <style>
            :root *:not(h2) {
                font-family: Arial, "Helvetica Neue", Helvetica, sans-serif !important;
                font-size: 12px;
                overscroll-behavior: none;
                -webkit-font-smoothing: antialiased;
                -moz-osx-font-smoothing: grayscale;
            }

            :root a {
                color: blue;
                text-decoration: none;
            }

            .preferenceLabel {
                text-align: right;
                width: 25%;
                font-weight: bold;
                vertical-align: top;
            }

            .preferenceUnits {
                font-weight: normal;
            }

            .preferenceTable label {
                font-weight: bold;
            }

            /* centre the label against a single-line input or checkbox, also when the label wraps */
            .preferenceTable tr:has(> td > .form-control, > td > .form-select, > td > .input-group, > td > input[type=checkbox]) > td {
                vertical-align: middle;
            }

            /* quick link labels: push down by the input's padding and border to line up with the text in the input */
            .preferenceTable table td:has(> label) {
                padding-top: calc(.5rem + .375rem + 1px);
            }

            table.eRxTableCenter {
                width: 50%;
                margin-left: 25%;
                margin-right: 25%;
            }

        </style>
    </head>

    <%
        ProviderPreference providerPreference = ProviderPreferencesUIBean.getProviderPreference(providerNo);

        if (providerPreference == null) {
            providerPreference = new ProviderPreference();
        }

        String startHour = request.getParameter("start_hour") != null ? request.getParameter("start_hour") : providerPreference.getStartHour().toString();
        String endHour = request.getParameter("end_hour") != null ? request.getParameter("end_hour") : providerPreference.getEndHour().toString();
        String everyMin = request.getParameter("every_min") != null ? request.getParameter("every_min") : providerPreference.getEveryMin().toString();
        String myGroupNo = request.getParameter("mygroup_no") != null ? request.getParameter("mygroup_no") : providerPreference.getMyGroupNo();
        String newTicklerWarningWindow = request.getParameter("new_tickler_warning_window") != null ? request.getParameter("new_tickler_warning_window") : providerPreference.getNewTicklerWarningWindow();
        String ticklerProviderNo = request.getParameter("tklerproviderno");
        String defaultPMM = request.getParameter("default_pmm") != null ? request.getParameter("default_pmm") : providerPreference.getDefaultCaisiPmm();
        String caisiBillingNotDelete = request.getParameter("caisiBillingPreferenceNotDelete") != null ? request.getParameter("caisiBillingPreferenceNotDelete") : String.valueOf(providerPreference.getDefaultDoNotDeleteBilling());

        //TODO add proper user interface for this billing setting in Ontario?
        // String defaultBillingLocation = providerPreference.getDefaultBillingLocation()!=null?providerPreference.getDefaultBillingLocation():"no";
    %>

    <body onLoad="setfocus();showHideBillPref();showHideERxPref();">
    <div class="container-md">
    <FORM NAME="UPDATEPRE" METHOD="post" ACTION="providerupdatepreference.jsp" onSubmit="return(checkTypeInAll())">

        <h2 style="margin:auto 15px;">
            <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.description"/>
        </h2>

        <table class="table table-striped preferenceTable">
            <tr>
                <td class="preferenceLabel">
                    <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.preference.formStartHour"/>
                    <span class="preferenceUnits">(0-23)</span>
                </td>
                <td class="preferenceValue">
                    <INPUT class="form-control" TYPE="TEXT" NAME="start_hour" VALUE='<%=Encode.forHtmlAttribute(String.valueOf(startHour))%>' size="2" maxlength="2">
                </td>
            </tr>
            <tr>
                <td class="preferenceLabel">
                    <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.preference.formEndHour"/>
                    <span class="preferenceUnits">(0-23)</span>
                </td>
                <td class="preferenceValue">
                    <INPUT class="form-control" TYPE="TEXT" NAME="end_hour" VALUE='<%=Encode.forHtmlAttribute(String.valueOf(endHour))%>' size="2" maxlength="2">
                </td>
            </tr>
            <tr>
                <td class="preferenceLabel">
                    <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.preference.formPeriod"/>
                    <span class="preferenceUnits"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.preference.min"/></span>
                </td>
                <td class="preferenceValue">
                    <INPUT class="form-control" TYPE="TEXT" NAME="every_min" VALUE='<%=Encode.forHtmlAttribute(String.valueOf(everyMin))%>' size="2" maxlength="2">
                </td>
            </tr>
            <tr>
                <td class="preferenceLabel">
                    <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.preference.formGroupNo"/>
                </td>
                <td class="preferenceValue">
                    <div class="input-group">
                        <INPUT class="form-control" TYPE="TEXT" NAME="mygroup_no" VALUE='<%=Encode.forHtmlAttribute(String.valueOf(myGroupNo))%>' size="12" maxlength="10">
                        <input type="button" class="btn btn-light border" value="<fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.viewedit"/>"
                               onClick="popupPage(360,680,'providerdisplaymygroup.jsp' );return false;"/>
                    </div>
                </td>
            </tr>
            <!-- ticklerPlus removed -->

            <!-- QR Code on prescriptions setting -->
            <tr>
                <td class="preferenceLabel">
                    <label for="prescriptionQrCodes"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.qrCodeOnPrescriptions"/></label>
                </td>
                <td class="preferenceValue">
                    <%
                        boolean checked = PrescriptionQrCodeUIBean.isPrescriptionQrCodeEnabledForProvider(providerNo);
                    %>
                    <input type="checkbox" id="prescriptionQrCodes" name="prescriptionQrCodes" <%=checked ? "checked=\"checked\"" : ""%> />
                </td>
            </tr>

                <%-- links to display on the appointment screen --%>
            <tr>
                <td class="preferenceLabel">
                    <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.appointmentScreenLinkNameDisplayLength"/>
                </td>
                <td class="preferenceValue">
                    <input type="text" class="form-control" name="appointmentScreenFormsNameDisplayLength"
                           value='<%=Encode.forHtmlAttribute(String.valueOf(providerPreference.getAppointmentScreenLinkNameDisplayLength()))%>' size="2">
                </td>
            </tr>
            <tr>
                <td class="preferenceLabel">
                    <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.formsToDisplayOnAppointmentScreen"/>
                </td>
                <td class="preferenceValue">
                    <div style="height:10em;overflow-y:auto;">
                        <%
                            List<EncounterForm> encounterForms = ProviderPreferencesUIBean.getAllEncounterForms();
                            Collection<String> checkedEncounterFormNames = ProviderPreferencesUIBean.getCheckedEncounterFormNames(providerNo);
                            int formIndex = 0;
                            for (EncounterForm encounterForm : encounterForms) {
                                String nameEscaped = Encode.forHtml(encounterForm.getFormName());
                                String checkedString = (checkedEncounterFormNames.contains(encounterForm.getFormName()) ? "checked=\"checked\"" : "");
                                formIndex++;
                        %>
                        <div>
                            <input type="checkbox" id="encounterFormName<%=formIndex%>" name="encounterFormName"
                                   value="<%=nameEscaped%>" <%=checkedString%> />
                            <label for="encounterFormName<%=formIndex%>"><%=nameEscaped%></label>
                        </div>
                        <%
                            }
                        %>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="preferenceLabel">
                    <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.eFormsToDisplayOnAppointmentScreen"/>
                </td>
                <td class="preferenceValue">
                    <div style="height:10em;overflow-y:auto;">
                        <%
                            List<EForm> eforms = ProviderPreferencesUIBean.getAllEForms();
                            Collection<ProviderPreference.EformLink> checkedEFormIds = ProviderPreferencesUIBean.getCheckedEFormIds(providerNo);
                            for (EForm eform : eforms) {
                                String checkedString = "";
                                inner:
                                for (ProviderPreference.EformLink eformLink : checkedEFormIds) {
                                    if (eform.getId().equals(eformLink.getAppointmentScreenEForm())) {
                                        checkedString = "checked";
                                        break inner;
                                    }
                                }

                        %>
                        <div>
                            <input type="checkbox" id="eformId<%=Encode.forHtmlAttribute(String.valueOf(eform.getId()))%>" name="eformId"
                                   value="<%=Encode.forHtmlAttribute(String.valueOf(eform.getId()))%>" <%=checkedString%> />
                            <label for="eformId<%=Encode.forHtmlAttribute(String.valueOf(eform.getId()))%>"><%=Encode.forHtml(eform.getFormName())%></label>
                        </div>
                        <%
                            }
                        %>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="preferenceLabel">
                    <fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.quickLinksToDisplayOnAppointmentScreen"/>
                </td>
                <td class="preferenceValue">
                    <div style="max-height:10em;overflow-y:auto;">
                        <%
                            Collection<ProviderPreference.QuickLink> quickLinks = ProviderPreferencesUIBean.getQuickLinks(providerNo);
                            for (ProviderPreference.QuickLink quickLink : quickLinks) {
                        %>
                        <div>
                            <input type="button" class="btn btn-light border" value="<fmt:setBundle basename="oscarResources"/><fmt:message key="REMOVE"/>"
                                   onclick="document.location='providerPreferenceQuickLinksAction.jsp?action=remove&name='+escape('<%=Encode.forJavaScript(quickLink.getName())%>')"/>
                            <%=Encode.forHtml(quickLink.getName())%>
                            : <%=Encode.forHtml(quickLink.getUrl())%>
                        </div>
                        <%
                            }
                        %>
                    </div>
                    <table class="table">
                        <tr>
                            <td style="border:none;text-align:right">
                                <label for="quickLinkName"><fmt:setBundle basename="oscarResources"/><fmt:message key="NAME"/></label></td>
                            <td style="border:none"><input type="text" class="form-control" id="quickLinkName" name="quickLinkName"/></td>
                        </tr>
                        <tr>
                            <td style="border:none;text-align:right">
                                <label for="quickLinkUrl"><fmt:setBundle basename="oscarResources"/><fmt:message key="URL"/></label></td>
                            <td style="border:none">
                                <input type="text" class="form-control" id="quickLinkUrl" name="quickLinkUrl"/>
                                <div>(expanded tokens in the url are ${contextPath}
                                    and ${demographicId})
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td style="border:none"></td>
                            <td style="border:none">
                                <script type="text/javascript">
                                    function addQuickLink() {
                                        name = escape(document.UPDATEPRE.quickLinkName.value);
                                        url = escape(document.UPDATEPRE.quickLinkUrl.value);
                                        document.location = "providerPreferenceQuickLinksAction.jsp?action=add&name=" + name + "&url=" + url;
                                    }
                                </script>
                                <input type="button" class="btn btn-light border" value="<fmt:setBundle basename="oscarResources"/><fmt:message key="ADD"/>" onclick="addQuickLink()"/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td class="preferenceLabel">
                    <label for="schedule.week_view_weekends">Show Weekends in Week View</label>
                </td>
                <td class="preferenceValue">
                    <c:set var="weekends" value="${userProperty[UserPropertyKey.SCHEDULE_WEEK_VIEW_WEEKENDS.name]}"/>
                    <input type="checkbox" id="schedule.week_view_weekends" name="schedule.week_view_weekends"
                           value="true" ${empty weekends or weekends ? 'checked' : ''} />
                </td>
            </tr>
            <tr>
                <td class="preferenceLabel">
                    <label for="rxInteractionWarningLevel"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.rxInteractionWarningLevel"/></label>
                </td>
                <td class="preferenceValue">
                    <select id="rxInteractionWarningLevel" class="form-select">
                        <c:set var="rxLevel" value="${userProperty[UserPropertyKey.RX_INTERACTION_WARNING_LEVEL.name]}"/>
                        <option value="0" ${rxLevel eq '0' ? 'selected' : ''}>Not Specified</option>
                        <option value="1" ${rxLevel eq '1' ? 'selected' : ''}>Low</option>
                        <option value="2" ${rxLevel eq '2' ? 'selected' : ''}>Medium</option>
                        <option value="3" ${rxLevel eq '3' ? 'selected' : ''}>High</option>
                        <option value="4" ${rxLevel eq '4' ? 'selected' : ''}>None</option>
                    </select>
                </td>
            </tr>

        </table>

        <div style="text-align:right;font-weight:bold;padding-bottom:10px;">
            <input type="submit" class="btn btn-primary" value='<fmt:setBundle basename="oscarResources"/><fmt:message key="provider.providerpreference.btnSubmit"/>'>
            <input type="reset" class="btn btn-danger" value='<fmt:setBundle basename="oscarResources"/><fmt:message key="global.btnClose"/>' onClick="window.close();">
        </div>

        <INPUT TYPE="hidden" NAME="color_template" VALUE='deepblue'>


        <table class="table table-striped table-sm">

            <caisi:isModuleLoad moduleName="NEW_CME_SWITCH">
                <oscar:oscarPropertiesCheck property="TORONTO_RFQ" value="no">
                    <tr>
                        <td><a href=#
                                              onClick="popupPage(230,600,'<%= request.getContextPath() %>/casemgmt/newCaseManagementEnable.jsp');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEnableCmeUi"/></a>
                        </td>
                    </tr>
                </oscar:oscarPropertiesCheck>
            </caisi:isModuleLoad>

            <tr>
                <td><a href=#
                                      onClick="popupPage(230,600,'providerDefaultDxCode.jsp?provider_no=<%=Encode.forUriComponent(request.getParameter("provider_no"))%>');return false;">Edit
                    Default Billing Diagnostic Code</a>
                </td>
            </tr>
            <tr>

                <td><a href=#
                                      onClick="popupPage(370,700,'providerchangepassword.jsp');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnChangePassword"/></a>
                </td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewDefaultSex');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetDefaultSex"/></a></td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'providerSignature.jsp');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEditSignature"/></a>
                </td>
            </tr>
            <oscar:oscarPropertiesCheck property="TORONTO_RFQ" value="no" defaultVal="true">
                <security:oscarSec roleName="<%=roleName$%>" objectName="_billing" rights="r">
                    <tr>
                        <td>
                            <% String br = OscarProperties.getInstance().getProperty("billregion");
                                if (br.equals("BC")) { %>
                            <a href=#
                               onClick="popupPage(900,500,'<%=request.getContextPath()%>/billing/CA/BC/viewBillingPreferencesAction.do?providerNo=<%=Encode.forUriComponent(String.valueOf(providerNo))%>');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnBillPreference"/></a>
                            <% } else { %>
                            <a href=# onClick="showHideBillPref();return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnBillPreference"/></a>
                            <% } %>
                        </td>
                    </tr>
                    <tr id="billingONpref">
                        <td>
                            <div>
                                <label for="default_servicetype"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.labelDefaultBillForm"/>:</label>
                                <select id="default_servicetype" name="default_servicetype">
                                    <option value="no">-- no --</option>
                                    <%
                                        if (providerPreference != null) {
                                            String def = providerPreference.getDefaultServiceType();
                                            for (Object[] result : ctlBillingServiceDao.getUniqueServiceTypes("A")) {

                                    %>
                                    <option value="<%=Encode.forHtmlAttribute(String.valueOf((String)result[0]))%>"
                                            <%=((String) result[0]).equals(def) ? "selected" : ""%>>
                                        <%=Encode.forHtml(String.valueOf((String) result[1]))%>
                                    </option>
                                    <%
                                        }
                                    } else {
                                        for (Object[] result : ctlBillingServiceDao.getUniqueServiceTypes("A")) {
                                    %>
                                    <option value="<%=Encode.forHtmlAttribute(String.valueOf((String)result[0]))%>"><%=Encode.forHtml(String.valueOf((String) result[1]))%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </div>
                        </td>
                    </tr>
                </security:oscarSec>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(400,860,'providerAddress.jsp');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEditAddress"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(400,860,'providerPhone.jsp');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEditPhoneNumber"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(400,860,'providerFax.jsp');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEditFaxNumber"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(500,860,'providerPrinter.jsp');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetDefaultPrinter"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewRxPageSize');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetRxPageSize"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewCppSingleLine');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetCppSingleLine"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewShowPatientDOB');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetShowPatientDOB"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewDefaultQuantity');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.SetDefaultPrescriptionQuantity"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=view&provider_no=<%=Encode.forUriComponent(String.valueOf(providerNo))%>');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEditStaleDate"/></a></td>
                </tr>


                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewConsultationRequestCuffOffDate');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetConsultationCutoffTimePeriod"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewConsultationRequestTeamWarning');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetConsultationTeam"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewWorkLoadManagement');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetWorkLoadManagement"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewConsultPasteFmt');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetConsultPasteFmt"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewFavouriteEformGroup');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetEformGroup"/></a></td>
                </tr>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewHCType');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetHCType"/></a></td>
                </tr>
                <% if (OscarProperties.getInstance().hasProperty("ONTARIO_MD_INCOMINGREQUESTOR")) {%>
                <tr>
                    <td><a href=#
                                          onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewOntarioMDId');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetmyOntarioMD"/></a></td>
                </tr>
                <%}%>
            </oscar:oscarPropertiesCheck>

            <tr>
                <td><a href=# onClick="popupPage(400,860,'<%=request.getContextPath()%>/provider/CppPreferences.do');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.cppPrefs"/></a></td>
            </tr>

            <tr>
                <td><a href=#
                                      onClick="popupPage(400,860,'<%=request.getContextPath()%>/provider/OlisPreferences.do');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.olisPrefs"/></a></td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(500,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewCommentLab');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnDisableAckCommentLab"/></a></td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewLabRecall');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnLabRecallSettings"/></a></td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewEncounterWindowSize');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEditDefaultEncounterWindowSize"/></a></td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewQuickChartSize');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEditDefaultQuickChartSize"/></a></td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewEDocBrowserInDocumentReport');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetEDocBrowserInDocumentReport"/></a></td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewEDocBrowserInMasterFile');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetEDocBrowserInMasterFile"/></a></td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewPatientNameLength');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEditSetPatientNameLength"/></a></td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'<%= request.getContextPath() %>/admin/displayDocumentDescriptionTemplate.jsp');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetDocumentDescriptionTemplate"/></a></td>
            </tr>
            <tr>
                <td><a href=# onClick="popupPage(500,900,'clients.jsp');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEditClients"/></a></td>
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewDisplayDocumentAs');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnSetDisplayDocumentAs"/></a></td>
            </tr>
            <tr>
                <td><a href=#
            </tr>
            <tr>
                <td><a href=#
                                      onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewAppointmentCardPrefs');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnEditSetAppointmentCardPrefs"/></a></td>
            </tr>

            <oscar:oscarPropertiesCheck property="util.erx.enabled" value="true">
            <security:oscarSec roleName="<%=roleName$%>" objectName="_rx" rights="r">
            <tr>
                <td>
                    <a href=# onClick="showHideERxPref();return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.eRx.btnPrefLink"/></a>
                </td>
            </tr>
            <tr>
                <td>
                    <div id="eRxPref">
                                <%
            	String eRxEnabledChecked="unchecked";
                String eRxTrainingModeChecked="unchecked";
                                        
				boolean eRxEnabled = false;
                String eRx_SSO_URL = "";
                String eRxUsername = "";
                String eRxPassword = "";
                String eRxFacility = "";
                boolean eRxTrainingMode = false;
                                                        
                if (providerPreference != null){                                       
                	eRxEnabled = providerPreference.isERxEnabled();
                    if(eRxEnabled) eRxEnabledChecked = "checked";
                                
                    eRx_SSO_URL = providerPreference.getERx_SSO_URL();
                    eRxUsername = providerPreference.getERxUsername();
                    eRxPassword = providerPreference.getERxPassword();
                    eRxFacility = providerPreference.getERxFacility();
                                
                    eRxTrainingMode = providerPreference.isERxTrainingMode();
                    if(eRxTrainingMode) eRxTrainingModeChecked = "checked";
                                
                    if(eRx_SSO_URL==null || "null".equalsIgnoreCase(eRx_SSO_URL)) eRx_SSO_URL=OscarProperties.getInstance().getProperty("util.erx.oscarerx_sso_url");
                    if(eRxUsername==null || "null".equalsIgnoreCase(eRxUsername)) eRxUsername="";
                    if(eRxPassword==null || "null".equalsIgnoreCase(eRxPassword)) eRxPassword="";
                    if(eRxFacility==null || "null".equalsIgnoreCase(eRxFacility)) eRxFacility="";
                }
                %>
                        <table class="table eRxTableCenter">
                            <tr>
                                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.eRx.labelEnable"/>:</td>
                                <td><input name="erx_enable" title="Enable the External Prescriber"
                                           type="checkbox" <%=Encode.forHtml(String.valueOf(eRxEnabledChecked))%> /></td>
                            </tr>
                            <tr>
                                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.eRx.labelUser"/>:</td>
                                <td><input name="erx_username" class="form-control" type="text" value="<%=Encode.forHtmlAttribute(String.valueOf(eRxUsername))%>"
                                           title="Username to access the External Prescriber"/></td>
                            </tr>
                            <tr>
                                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.eRx.labelPassword"/>:</td>
                                <td><input name="erx_password" class="form-control" type="password" value="<%=Encode.forHtmlAttribute(String.valueOf(eRxPassword))%>"
                                           title="Password to access the External Prescriber"/></td>
                            </tr>
                            <tr>
                                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.eRx.labelFacility"/>:</td>
                                <td><input name="erx_facility" class="form-control" type="text" value="<%=Encode.forHtmlAttribute(String.valueOf(eRxFacility))%>"
                                           title="The Facility ID assigned to you by the External Prescriber"/></td>
                            </tr>
                            <tr>
                                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.eRx.labelTrainingMode"/>:</td>
                                <td><input name="erx_training_mode" type="checkbox"
                                           title="Enable Training Mode" <%=Encode.forHtml(String.valueOf(eRxTrainingModeChecked))%> /></td>
                            </tr>
            <tr>
                <td><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.eRx.labelURL"/>:</td>
                <td><input name="erx_sso_url" class="form-control" type="text" value="<%=Encode.forHtmlAttribute(String.valueOf(eRx_SSO_URL))%>"
                           title="The URL to access the Web Interface from OSCAR Rx"/></td>
            </tr>

        </table>
        </div>
        </td>
        </tr>
        </security:oscarSec>
        </oscar:oscarPropertiesCheck>
        <oscar:oscarPropertiesCheck property="billregion" value="ON" defaultVal="BC">
        <tr>
            <td><a href=#
                                  onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewDashboardPrefs');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnViewDashboardPrefs"/></a></td>
        </tr>
        <tr>
            <td><a href=#
                                  onClick="popupPage(230,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewPreventionPrefs');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnViewPreventionPrefs"/></a></td>
        </tr>

        <tr>
            <td><a href=#
                                  onClick="popupPage(700,860,'<%=request.getContextPath()%>/setProviderStaleDate.do?method=viewLabMacroPrefs');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnViewLabMacroPrefs"/></a></td>
        </tr>
        </oscar:oscarPropertiesCheck>
        <tr>
            <td><a href=#
                                  onClick="popupPage(280,730,'<%=request.getContextPath()%>/setTicklerPreferences.do?method=viewTicklerTaskAssignee');return false;"><fmt:setBundle basename="oscarResources"/><fmt:message key="provider.btnViewTicklerPreferences"/></a></td>
        </tr>
        </table>
    </FORM>
    </div>
    </body>
</html>
