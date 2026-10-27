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
    Adds a signature to the logged-in provider's list, or edits one of theirs.

    Parameters:
      signatureId - the signature to edit; without it the page adds a new one.
    When EnterSignature.do comes back here with an errorKey request attribute, the page shows the
    problem and keeps the text that was typed.

    @since 2002-11-08
--%>

<!-- add by caisi -->
<%@ taglib uri="/WEB-INF/caisi-tag.tld" prefix="caisi" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!-- add by caisi end<style>* {border:1px solid black;}</style> -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="csrf" uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" %>

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="ca.openosp.openo.managers.ProviderSignatureManager" %>
<%@ page import="ca.openosp.openo.managers.model.SignatureChoice" %>
<%@ page import="ca.openosp.openo.utility.SpringUtils" %>

<%
    if (session.getValue("user") == null) {
        response.sendRedirect(request.getContextPath() + "/logout.htm");
        return;
    }
    String curUser_no = (String) session.getAttribute("user");

    String signatureIdParam = request.getParameter("signatureId");
    String errorKey = (String) request.getAttribute("errorKey");
    boolean editing = signatureIdParam != null && !signatureIdParam.isEmpty();
    String text = "";
    if (errorKey != null) {
        // Coming back with a problem: keep what was typed
        text = request.getParameter("signature") == null ? "" : request.getParameter("signature");
    } else if (editing) {
        ProviderSignatureManager providerSignatureManager = SpringUtils.getBean(ProviderSignatureManager.class);
        SignatureChoice choice;
        try {
            choice = providerSignatureManager.findChoice(curUser_no, SignatureChoice.parsePostedId(signatureIdParam)).orElse(null);
        } catch (NumberFormatException e) {
            choice = null;
        }
        if (choice == null || !choice.isSavedSignature()) {
            // Not one of this provider's saved signatures, e.g. deleted in another window
            response.sendRedirect(request.getContextPath() + "/provider/providerSignature.jsp");
            return;
        }
        text = choice.getText();
    }
%>
<fmt:setBundle basename="oscarResources"/>
<html>
    <head>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>

        <base href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" %>">
        <link rel="stylesheet" type="text/css"
              href="<%= request.getContextPath() %>/oscarEncounter/encounterStyles.css">

        <title><fmt:message key="provider.editSignature.title"/></title>
    </head>

    <body class="BodyStyle" vlink="#0000FF">
    <!-- add by caisi -->
    <caisi:isModuleLoad moduleName="caisi">
        <iframe id="hiddenFrame" src="javascript:void(0)" style="display: none"></iframe>
        <script>
            function toggleSig(n) {
                // Function disabled - infirm.do action no longer exists
            }
        </script>
    </caisi:isModuleLoad>
    <!-- add by caisi end-->
    <table class="MainTable" id="scrollNumber1" name="encounterTable">
        <tr class="MainTableTopRow">
            <td class="MainTableTopRowLeftColumn"><fmt:message key="provider.editSignature.msgPrefs"/></td>
            <td class="MainTableTopRowRightColumn">
                <table class="TopStatusBar">
                    <tr>
                        <td><fmt:message key="provider.editSignature.msgProviderSignature"/></td>
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
            <td class="MainTableRightColumn"><form action="<%= request.getContextPath() %>/EnterSignature.do" method="post">
                <input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue/>"/>
                <input type="hidden" name="method" value="save"/>
                <% if (editing) { %>
                <input type="hidden" name="signatureId" value="<%= Encode.forHtmlAttribute(signatureIdParam) %>"/>
                <label for="signature"><fmt:message key="provider.editSignature.msgEdit"/></label>
                <% } else { %>
                <label for="signature"><fmt:message key="provider.editSignature.msgNew"/></label>
                <% } %>
                <br>
                <% if (errorKey != null) { %>
                <span style="color: red;"><fmt:message key="<%= errorKey %>"/></span>
                <br>
                <% } %>
                <input type="text" name="signature" id="signature" size="40" maxlength="<%= ProviderSignatureManager.MAX_LENGTH %>"
                       value="<%= Encode.forHtmlAttribute(text) %>"/>
                <br>
                <!-- add by caisi -->
                <caisi:isModuleLoad moduleName="caisi">
                    <INPUT TYPE="checkbox"
                            <%= ((Boolean)session.getAttribute("signOnNote")).booleanValue()?"checked":""%>
                           onchange="toggleSig('<%= curUser_no %>')">also sign the signature in encounter notes
                </caisi:isModuleLoad>
                <!-- add by caisi end-->
                <% if (editing) { %>
                <input type="submit" value="<fmt:message key="provider.editSignature.btnUpdate"/>"/>
                <% } else { %>
                <input type="submit" value="<fmt:message key="provider.editSignature.btnSubmit"/>"/>
                <% } %>
                <a href="<%= request.getContextPath() %>/provider/providerSignature.jsp"><fmt:message key="global.btnCancel"/></a>
            </form></td>
        </tr>
        <tr>
            <td class="MainTableBottomRowLeftColumn"></td>
            <td class="MainTableBottomRowRightColumn"></td>
        </tr>
    </table>
    </body>
</html>
