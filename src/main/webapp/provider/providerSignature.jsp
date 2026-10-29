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
<%--
    The logged-in provider's signature list, opened from Provider Preferences.

    Lists the provider's prescriber name and their saved signatures, marks the default (pre-selected
    when they print a prescription), and links to add, edit, delete and make default. Changes post to
    EnterSignature.do, which comes back here.

    @since 2002-11-08
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="csrf" uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" %>

<%@ page import="java.util.List" %>
<%@ page import="ca.openosp.openo.managers.ProviderSignatureManager" %>
<%@ page import="ca.openosp.openo.managers.model.SignatureChoice" %>
<%@ page import="ca.openosp.openo.utility.SpringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%
    if (session.getValue("user") == null) {
        response.sendRedirect(request.getContextPath() + "/logout.htm");
        return;
    }
    String curUser_no = (String) session.getAttribute("user");
    ProviderSignatureManager providerSignatureManager = SpringUtils.getBean(ProviderSignatureManager.class);
    List<SignatureChoice> choices = providerSignatureManager.getChoices(curUser_no);
%>

<fmt:setBundle basename="oscarResources"/>
<fmt:message key="provider.providerSignature.msgDeleteConfirm" var="deleteConfirm"/>

<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>

        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
        <link rel="stylesheet" type="text/css"
              href="<%= request.getContextPath() %>/oscarEncounter/encounterStyles.css">

        <title><fmt:message key="provider.providerSignature.title"/></title>

        <style>
            .signatureList {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 6px;
                /* encounterStyles.css shrinks the text of every table; keep the page's size */
                font-size: inherit;
                /* Set widths, so the links stay put when Make default moves between rows */
                table-layout: fixed;
            }

            .signatureList .defaultColumn {
                width: 10em;
            }

            .signatureList .actionsColumn {
                width: 19em;
            }

            .signatureList th {
                text-align: left;
                border-bottom: 1px solid #999999;
                padding: 3px 6px;
            }

            .signatureList td {
                border-bottom: 1px solid #dddddd;
                padding: 3px 6px;
                overflow-wrap: break-word;
            }

            .signatureList form {
                display: inline;
            }

            /* Delete and Make default post a form but look like the Edit link */
            .linkButton {
                background: none;
                border: none;
                padding: 0;
                color: #0000FF;
                text-decoration: underline;
                cursor: pointer;
                font: inherit;
            }
        </style>
    </head>

    <body class="BodyStyle" vlink="#0000FF">
    <table class="MainTable" id="scrollNumber1" name="encounterTable">
        <tr class="MainTableTopRow">
            <td class="MainTableTopRowLeftColumn"><fmt:message key="provider.providerSignature.msgPrefs"/></td>
            <td class="MainTableTopRowRightColumn">
                <table class="TopStatusBar">
                    <tr>
                        <td><fmt:message key="provider.providerSignature.msgTitle"/>
                        </td>
                        <td>&nbsp;</td>
                        <td style="text-align: right"><a
                                href="javascript:popupStart(300,400,'About.jsp')"><fmt:message key="global.about"/></a> | <a
                                href="javascript:popupStart(300,400,'License.jsp')"><fmt:message key="global.license"/></a></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="MainTableLeftColumn">&nbsp;</td>
            <td class="MainTableRightColumn">
                <table class="signatureList">
                    <tr>
                        <th><fmt:message key="provider.providerSignature.colSignature"/></th>
                        <th class="defaultColumn"></th>
                        <th class="actionsColumn"><fmt:message key="provider.providerSignature.colActions"/></th>
                    </tr>
                    <% for (SignatureChoice choice : choices) {
                        String signatureId = choice.getPostedId(); %>
                    <tr>
                        <td>
                            <%= Encode.forHtml(choice.getText()) %>
                            <% if (choice.isPrescriberName()) { %>
                            <i>(<fmt:message key="provider.providerSignature.msgYourName"/>)</i>
                            <% } %>
                        </td>
                        <td>
                            <% if (choice.isDefaultChoice()) { %>
                            &#10003; <fmt:message key="provider.providerSignature.msgDefault"/>
                            <% } %>
                        </td>
                        <td>
                            <% if (choice.isSavedSignature()) { %>
                            <a href="<%= request.getContextPath() %>/provider/editSignature.jsp?signatureId=<%= Encode.forUriComponent(signatureId) %>"><fmt:message key="provider.providerSignature.btnEdit"/></a>
                            |
                            <form action="<%= request.getContextPath() %>/EnterSignature.do" method="post"
                                  onsubmit="return confirm('<%= Encode.forJavaScriptAttribute(String.valueOf(pageContext.getAttribute("deleteConfirm"))) %>');">
                                <input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue/>"/>
                                <input type="hidden" name="method" value="delete"/>
                                <input type="hidden" name="signatureId" value="<%= Encode.forHtmlAttribute(signatureId) %>"/>
                                <button type="submit" class="linkButton"><fmt:message key="provider.providerSignature.btnDelete"/></button>
                            </form>
                            <% } %>
                            <% if (!choice.isDefaultChoice()) { %>
                            <% if (choice.isSavedSignature()) { %>|<% } %>
                            <form action="<%= request.getContextPath() %>/EnterSignature.do" method="post">
                                <input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue/>"/>
                                <input type="hidden" name="method" value="makeDefault"/>
                                <input type="hidden" name="signatureId" value="<%= Encode.forHtmlAttribute(signatureId) %>"/>
                                <button type="submit" class="linkButton"
                                        title="<fmt:message key="provider.providerSignature.msgMakeDefaultTitle"/>"><fmt:message key="provider.providerSignature.btnMakeDefault"/></button>
                            </form>
                            <% } %>
                        </td>
                    </tr>
                    <% } %>
                </table>
                <a href="<%= request.getContextPath() %>/provider/editSignature.jsp"><fmt:message key="provider.providerSignature.btnClickHere"/></a>
                <fmt:message key="provider.providerSignature.msgAdd"/>
            </td>
        </tr>
        <tr>
            <td class="MainTableBottomRowLeftColumn"></td>
            <td class="MainTableBottomRowRightColumn"></td>
        </tr>
    </table>
    </body>
</html>
