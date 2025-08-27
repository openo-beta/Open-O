<%--


    Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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

    This software was written for
    Centre for Research on Inner City Health, St. Michael's Hospital,
    Toronto, Ontario, Canada

--%>


<%@page import="oscar.Misc" %>
<%@page import="ca.openosp.openo.util.UtilMisc" %>
<%@include file="/casemgmt/taglibs.jsp" %>
<%@taglib uri="/WEB-INF/caisi-tag.tld" prefix="caisi" %>
<%@page import="java.util.Enumeration" %>
<%@page import="ca.openosp.openo.encounter.pageUtil.NavBarDisplayDAO" %>
<%@page import="java.util.Arrays,java.util.Properties,java.util.List,java.util.Set,java.util.ArrayList,java.util.Enumeration,java.util.HashSet,java.util.Iterator,java.text.SimpleDateFormat,java.util.Calendar,java.util.Date,java.text.ParseException" %>
<%@page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@page import="org.oscarehr.common.model.UserProperty,org.oscarehr.casemgmt.model.*,org.oscarehr.casemgmt.service.* " %>
<%@page import="org.oscarehr.casemgmt.web.formbeans.*" %>
<%@page import="org.oscarehr.PMmodule.model.*" %>
<%@page import="org.oscarehr.common.model.*" %>
<%@page import="ca.openosp.openo.util.DateUtils" %>
<%@page import="ca.openosp.openo.documentManager.EDocUtil" %>
<%@page import="org.springframework.web.context.WebApplicationContext" %>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@page import="ca.openosp.openo.casemgmt.common.Colour" %>
<%@page import="ca.openosp.openo.documentManager.EDoc" %>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@page import="com.quatro.dao.security.*,ca.openosp.openo.model.security.Secrole" %>
<%@page import="org.oscarehr.utility.EncounterUtil" %>
<%@page import="org.apache.cxf.common.i18n.UncheckedException" %>
<%@page import="ca.openosp.openo.casemgmt.web.NoteDisplay" %>
<%@page import="ca.openosp.openo.casemgmt.web.CaseManagementViewAction" %>
<%@page import="org.oscarehr.utility.SpringUtils" %>
<%@page import="ca.openosp.openo.rx.data.RxPrescriptionData" %>
<%@page import="ca.openosp.openo.casemgmt.dao.CaseManagementNoteLinkDAO" %>
<%@page import="org.oscarehr.common.dao.ProfessionalSpecialistDao" %>
<%@page import="oscar.OscarProperties" %>
<%@page import="org.oscarehr.utility.MiscUtils" %>
<%@page import="ca.openosp.openo.util.UtilDateUtilities" %>
<%@page import="ca.openosp.openo.casemgmt.web.NoteDisplayNonNote" %>
<%@page import="org.oscarehr.common.dao.EncounterTemplateDao" %>
<%@page import="ca.openosp.openo.casemgmt.web.CheckBoxBean" %>

<% java.util.Properties oscarVariables = OscarProperties.getInstance(); %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>

<div id="cppBoxes">
    <div id="divR1">
        <!-- social history -->
        <div id="divR1I1" class="topBox">
        </div>

        <div id="divR1I2" class="topBox">

        </div>
    </div>

    <div id="divR2">
        <!--Ongoing Concerns cell -->
        <div id="divR2I1" class="topBox">
        </div>
        <!--Reminders cell -->
        <div id="divR2I2" class="topBox">
        </div>
    </div>

</div>

<div id="notCPP">

</div>