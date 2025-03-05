<%--

   Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
   This software is published under the GPL GNU General Public License.
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.
   <p>
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU General Public License for more details.
   <p>
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
   <p>
   This software was written for
   Centre for Research on Inner City Health, St. Michael's Hospital,
   Toronto, Ontario, Canada

--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<html>
<body>
<p>You are required to configure Multi-Factor authentication before you can continue.</p>
<div class="row p-2">

    <!-- Left Section: Instructions -->
    <div class="col-12 col-md-7 ps-4">
        <p class="h4 text-secondary mt-3">Sync Authenticator App</p>
        <p class="mb-4"><small>Use an app such as Microsoft Authenticator or Google Authenticator to generate a verification code.</small></p>
        <p><small>Once the app is installed, scan the QR shown and enter a verification code to complete the setup.</small></p>
    </div>

    <!-- Right Section: QR Code -->
    <div class="col-12 col-md-5 text-center">
        <div class="row mx-0 mt-2 p-2">
            <img src="${requestScope.qrData}" alt="QR Code for Multi-Factor Authentication Setup" class="img-fluid">
        </div>
        <div class="row">
            <jsp:include page="/mfa/mfa_otp_handler.jsp"/>
        </div>
    </div>
</div>
</body>

