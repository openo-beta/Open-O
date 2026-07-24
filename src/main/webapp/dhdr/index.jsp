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
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@page import="ca.openosp.openo.utility.SpringUtils" %>
<%@page import="ca.openosp.openo.commn.dao.SystemPreferencesDao" %>
<%@page import="ca.openosp.openo.commn.model.SystemPreferences" %>
<%@page import="ca.openosp.OscarProperties" %>
<%@page import="org.owasp.encoder.Encode" %>
<fmt:setBundle basename="oscarResources"/>
<%
    String roleName$ = (String)session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
	boolean authed=true;
%><security:oscarSec roleName="<%=roleName$%>" objectName="_rx" rights="r" reverse="<%=true%>">
	<%authed=false; %>
	<%response.sendRedirect("../securityError.jsp?type=_admin&type=_admin.misc");%>
</security:oscarSec><%
	if(!authed) {
		return;
	}

	// DHDR12.01-12.02: the ODB Formulary ("Check Medication Coverage - Drug Formulary") and EAP
	// links. EAP requests are submitted through the SADIE portal, so the EAP link targets SADIE.
	// Both URLs are admin-configurable in oscar_mcmaster.properties (dhdr.odb_formulary_url /
	// dhdr.eap_url); the OMD-published addresses are used when the properties are unset.
	String odbUrl = OscarProperties.getInstance().getProperty("dhdr.odb_formulary_url", "https://www.ontario.ca/check-medication-coverage/");
	String eapUrl = OscarProperties.getInstance().getProperty("dhdr.eap_url", "http://www.health.gov.on.ca/en/pro/programs/sadie/");

	// DHDR-04: PCOI viewlet "not responding" timeout in milliseconds, configurable via the
	// oneid_viewlet_timeout system preference; default 300000 (5 minutes) when unset or invalid.
	int viewletTimeout = 300000;
	SystemPreferencesDao systemPreferencesDao = SpringUtils.getBean(SystemPreferencesDao.class);
	SystemPreferences viewletTimeoutPref = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.oneid_viewlet_timeout);
	if (viewletTimeoutPref != null && viewletTimeoutPref.getValue() != null && !viewletTimeoutPref.getValue().trim().isEmpty()) {
		try {
			viewletTimeout = Integer.parseInt(viewletTimeoutPref.getValue().trim());
		} catch (NumberFormatException e) {
			// keep the 300000 default
		}
	}

	// DHDR02.03: the clinic-level default for the "last N days" search, configurable in
	// oscar_mcmaster.properties (dhdr.default_search_days); the requirement's suggested 120 is used
	// when unset or invalid. Read once here so a user editing the range on screen cannot write back
	// into the default, which DHDR02.03 forbids.
	int defaultSearchDays = 120;
	try {
		int configured = Integer.parseInt(OscarProperties.getInstance()
			.getProperty("dhdr.default_search_days", String.valueOf(defaultSearchDays)).trim());
		if (configured > 0) {
			defaultSearchDays = configured;
		}
	} catch (NumberFormatException e) {
		// keep the default above
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="dhdrView">
<head>
	<title>DHDR Search</title>
	<link href="<%=request.getContextPath() %>/library/bootstrap/3.0.0/css/bootstrap.css" rel="stylesheet">
	<link rel="stylesheet" href="<%=request.getContextPath() %>/css/font-awesome.min.css">
	<script type="text/javascript" src="<%=request.getContextPath() %>/library/angular.min.js"></script>	
	<script type="text/javascript" src="<%=request.getContextPath() %>/library/ui-bootstrap-tpls-0.11.0.js"></script>
	<script src="<%=request.getContextPath() %>/web/common/demographicServices.js"></script>	
	<script src="<%=request.getContextPath() %>/web/common/providerServices.js"></script>	
	<script src="<%=request.getContextPath() %>/web/common/dhdrServices.js"></script>	
	<script src="<%=request.getContextPath() %>/web/common/rxServices.js"></script>	
	<script src="<%=request.getContextPath() %>/web/filters.js"></script>
	<style>
		.modal-lg{
			width:1700px;
		}
		.container {
			max-width: 1750px;
		}
		.pcoi-frame {
			width: -webkit-fill-available;
		}
		.startDate {
			min-width: 90px;
		}
		.table-overflow-x {
			overflow-x: auto;
		}
		.modal-lg {
			width: 95vw;
		}
		.modal-lg iframe {
			height: 80vh;
		}
	</style>
</head>

<body vlink="#0000FF" class="BodyStyle">
	<div ng-controller="dhdrView">
	<div class="page-header container" style="margin-top: 0px; margin-bottom: 0px;">
		<div class="row">
			<div class="col-xs-12">
				<h1 class="patientHeaderName" style="margin-top: 0px;" ng-cloak>
				<b>{{demographic.lastName}}, {{demographic.firstName}}</b>  <span ng-show="demographic.hin">({{demographic.hin}})</span>

				<small class="patientHeaderExt pull-right">
					<i><fmt:message key="demographic.patient.context.born"/>: </i>
					<b>{{demographicDobText()}}</b> (<b>{{demographic.age | age}}</b>) &nbsp;&nbsp; <i><fmt:message key="demographic.patient.context.sex"/>:</i> <b>{{demographic.sex}}</b>
					<i> &nbsp;&nbsp; <fmt:message key="Appointment.msgTelephone"/>:</i> <b>{{demographic.phone}}</b>
					<!-- <span class="glyphicon glyphicon-new-window"></span>-->
				</small>
			</h1>
			</div>
		</div>
	</div>
	<div class="container" ng-show="dhdrPatientResolved" ng-cloak>
		<%-- DHDR03.02: identify the DHDR-side patient whenever events are shown, flagging
		     fields that do not match the EMR demographic. --%>
		<div class="alert" ng-class="dhdrPatientDataUnmatched ? 'alert-warning' : 'alert-info'" role="alert" style="margin-bottom: 8px;">
			<strong>DHDR EHR Service patient:</strong>
			{{dhdrPatient.lastName}}, {{dhdrPatient.firstName}}{{matchNote(dhdrPatient.nameUnmatched, dhdrPatient.nameMissing)}}
			&nbsp;&middot;&nbsp; HIN: {{dhdrPatient.hin}}{{matchNote(dhdrPatient.hinUnmatched, dhdrPatient.hinMissing)}}
			&nbsp;&middot;&nbsp; DOB: {{dhdrPatient.dob | date}}{{matchNote(dhdrPatient.dobUnmatched, dhdrPatient.dobMissing)}}
			&nbsp;&middot;&nbsp; Sex: {{dhdrPatient.gender}}{{matchNote(dhdrPatient.genderUnmatched, dhdrPatient.genderMissing)}}
			<div ng-show="dhdrPatientDataUnmatched"><em>Some DHDR patient details differ from the EMR record, or are not recorded in it.</em></div>
		</div>
	</div>
	<div class="container">
		<h4>More Information:</h4>
		<input type="button" class="btn btn-default" value="Ontario Drug Benefit" ng-click="openWindow('<%=Encode.forJavaScript(odbUrl)%>')"/>
		<input type="button" class="btn btn-default" value="Enhanced Access Program" ng-click="openWindow('<%=Encode.forJavaScript(eapUrl)%>')"/>
		<%-- DHDR13.02: entry point to the cross-patient Temporary Consent Unblock report (opens in a new window). --%>
		<input type="button" class="btn btn-default" value="Consent Override Report" ng-click="openWindow('<%=Encode.forJavaScript(request.getContextPath() + "/dhdr/consentOverrideReport.do")%>')"/>
	</div>
	<hr/>
	<div class="container">
		<div class="row">
		 	<div class="col-xs-12" >
		 		<form class="form-inline">
				  <div class="form-group">
				    <label for="exampleInputName2">Start Date</label>
				    <input type="date" class="form-control" id="exampleInputName2" placeholder="2020-01-01" ng-model="searchConfig.startDate" ng-change="clearSearchDays();">
				  </div>
				  <div class="form-group">
				    <label for="exampleInputEmail2">End Date</label>
				    <input type="date" class="form-control" id="exampleInputEmail2" placeholder="2020-03-31" ng-model="searchConfig.endDate" ng-change="clearSearchDays();">
				  </div>
				  <%-- DHDR02.03(a): search by number of days before today. Fills the two date fields
				       above, so the range stays visible and hand-editable afterwards. --%>
				  <div class="form-group">
				    <label for="dhdrSearchDays">Last</label>
				    <input type="number" min="1" class="form-control" id="dhdrSearchDays" style="width:6em;"
				           ng-model="searchDays" ng-change="applySearchDays();">
				    <label for="dhdrSearchDays">days</label>
				  </div>

				  <button type="submit" class="btn btn-default" ng-click="callSearch();" ng-disabled="buttonDisabled" style="vertical-align: bottom;">Search</button>
				  <button type="submit" class="btn btn-default" ng-click="setSearchDateToAll();" ng-disabled="buttonDisabled" style="vertical-align: bottom;">Search All</button>
				</form>
		 		
		 	</div>
		 </div>
		 <div class="row" style="margin-bottom:2px;">
		 	<div class="col-xs-12" >
		 	<i>DHDR is being searched with HIN: {{demographic.hin}}  DOB: {{demographicDobText()}}</i>
	 	<br/>
	 	<!-- DHDR02.05 (B2 #6): display the search period used alongside the results. -->
	 	<%-- Formatted in searchPeriodText() so the period reads in the same format as the tables
	 	     (DHDR03.06). A cleared start date sends no lower bound, so describe that rather than
	 	     render a dangling "  to <end>" range the search never actually used (BP6). --%>
	 	<i>Search period: {{searchPeriodText()}}</i>
		 	</div>
		 </div>
		 
		 <div class="row" style="margin-bottom:10px;">
		 	<div class="col-xs-12" >
		 		<!-- DHDR03.03: non-intrusive disclaimer - always rendered, no dismiss control (v3.0 retired the DHDR15.03 suppress path). -->
		 		<div class="alert alert-info" role="alert">
		 				<i>Warning: Limited to Drug and Pharmacy Service Information available in the Digital Health Drug Repository (DHDR) EHR Service. 
		 					To ensure a Best Possible Medication History, please review this information with the patient/family and use other available sources of medication 
		 					information in addition to the DHDR EHR Service. For more details on the information available in the DHDR EHR Service, 
		 					please  <a class="alert-link" href="http://www.forms.ssb.gov.on.ca/mbs/ssb/forms/ssbforms.nsf/FormDetail?OpenForm&ACT=RDR&TAB=PROFILE&SRCH=&ENV=WWE&TIT=5056-87E&NO=014-5056-87E" target="_blank">click here</a></i>
		 		</div>
		 		
		 		<div ng-show="searching">
					Searching...
				</div>

		 		<%-- DHDR14.01: a PHI-free notice for every error/warning the EMR could not obtain an
		 		     OperationOutcome for - including a DHDR EHR Service that does not respond. Shows the
		 		     error code, description, severity and the date/time of the incident. No admin role is
		 		     required to see it, and no technical detail is exposed: that stays in the audit log. --%>
		 		<div ng-repeat="serviceError in serviceErrors" class="alert" ng-class="serviceErrorClass(serviceError)" role="alert">
		 			<strong>{{serviceError.httpMessage}}</strong>
		 			<div>{{noticeCodeLabel(serviceError.severity)}} {{serviceError.httpCode}} &middot; Severity: {{serviceError.severity}} &middot; {{serviceError.dateTime}}</div>
		 			<div ng-if="serviceError.moreInformation">{{serviceError.moreInformation}}</div>
		 		</div>

		 		<div ng-repeat="outs in outcomes" >
		 			<div ng-repeat="issue in outs.issues"  class="alert" ng-class="issueClass(issue)" role="alert">
		 				{{issue.details.text}}
		 				<%-- DHDR14.01: an error received from the DHDR EHR Service must present its code,
		 				     description, severity and the date/time of the incident. The 'suppressed' issue is
		 				     the normal consent-block workflow (handled just below), not an error, so it is
		 				     excluded from the error line. --%>
		 				<div class="small" ng-if="issue.code !== 'suppressed'">{{noticeCodeLabel(issue.severity)}} {{dhdrCode(issue)}} &middot; Severity: {{issue.severity}} &middot; {{outs.receivedAt | date:'medium'}}</div>
		 				<span ng-if="issue.code === 'suppressed'">
		 					<!-- DHDR09.03: the EMR renders the mandated consent-block message itself (not reliant on the OperationOutcome text). -->
		 					<div>Access to Drug and Pharmacy Service information has been blocked by the patient.</div>
		 					<button type="button" class="btn btn-danger" ng-click="callConsentBlock();" ng-disabled="buttonDisabled">Temporary Consent Unblock</button>
		 					<button type="button" class="btn btn-default" ng-click="logOverrideStatus(outs.id, null, 'Cancelled');">Cancel</button>
		 					<button type="button" class="btn btn-default" ng-click="logOverrideStatus(outs.id, null, 'Refused');">Refused</button>
		 				</span>
		 			</div>
		 		</div>
				
				<!-- DHDR09.05: refuse/cancel outcome message - shown briefly, then the viewer closes automatically. -->
				<div class="alert alert-warning" role="alert" ng-show="overrideResultMessage">{{overrideResultMessage}}</div>

				<!-- DHDR02.04: a valid search returning zero events must inform the user. Distinct from the
			     PCR patient-not-found / consent-suppressed cases (B2 #15), which surface via outcomes above. -->
			<div class="alert alert-warning" role="alert" ng-show="searchComplete && !searching && meds.length === 0 && services.length === 0 && serviceErrors.length === 0 && !hasBlockingOutcome()">
				No records found for the specified search date period.
			</div>

				<ul class="nav nav-pills nav-justified">
				  <li role="presentation" ng-class="currentView('summary')"><a ng-click="showSummary()">Summary</a></li>
				  <li role="presentation" ng-class="currentView('comp')"><a href="#" ng-click="showComp()">Comparative</a></li>  
				</ul>
			</div>
		 </div>
		<div ng-show="viewWhen('summary')">
			<div class="row">
				<div class="col-xs-12">
					<h6>DHDR Drugs <small><a ng-click="showHideFilter()">Filter</a></small></h6>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12">
					<div ng-show="showFilter()" >
						<form class="form-horizontal">
						  <div class="form-group">
							<label class="col-sm-2 control-label">Generic name</label>
							<div class="col-sm-10">
							  <input ng-model="searchtxt.genericName" type="text" placeholder="type to filter" class="form-control" />
							</div>
						  </div>
						  <div class="form-group">
							<label class="col-sm-2 control-label">Brand name</label>
							<div class="col-sm-10">
							  <input ng-model="searchtxt.brandName.display" type="text" placeholder="type to filter" class="form-control"/>
							</div>
						  </div>
						  <div class="form-group">
							<label class="col-sm-2 control-label">Dispensed date</label>
							<div class="col-sm-10">
							  <input ng-model="searchtxt.whenPrepared" type="text" placeholder="type to filter" class="form-control"/>
							</div>
						  </div>
						  <div class="form-group">
							<label class="col-sm-2 control-label">Pharmacy Name</label>
							<div class="col-sm-10">
							  <input ng-model="searchtxt.dispensingPharmacy" type="text" placeholder="type to filter" class="form-control" />
							</div>
						  </div>
						  <div class="form-group">
							<label class="col-sm-2 control-label">Prescriber Name</label>
							<div class="col-sm-10">
							  <input ng-model="searchtxt.prescriberLastname" type="text" placeholder="type to filter" class="form-control" />
							</div>
						  </div>

						</form>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12">
					<table class="table table-condensed table-striped table-bordered" ng-show="meds.length > 0">
						<thead>
							<tr>
								<td colspan="12">
									{{meds.length}} results returned  <button type="button" class="btn btn-default btn-xs" ng-click="toggleExpandAll()">{{expandAll ? 'Collapse All' : 'Expand All'}}</button>  <button type="button" class="btn btn-default btn-xs" ng-click="printSummary()">Print</button>
								</td>
							</tr>
							<tr>
								<th>
									<a ng-click="orderByField='whenPrepared'; reverseSort = !reverseSort">
										Dispense Date
										<span ng-show="orderByField == 'whenPrepared'">
											<span ng-show="!reverseSort">^</span>
											<span ng-show="reverseSort">v</span>
										</span>
									</a>
								</th>
								<th>
									<a ng-click="orderByField='pickUpDate'; reverseSort = !reverseSort">
										Pickup Date
										<span ng-show="orderByField == 'pickUpDate'">
											<span ng-show="!reverseSort">^</span>
											<span ng-show="reverseSort">v</span>
										</span>
									</a>
								</th>
								<th>
									<a ng-click="orderByField='genericName'; reverseSort = !reverseSort">
										Generic
										<span ng-show="orderByField == 'genericName'">
											<span ng-show="!reverseSort">^</span>
											<span ng-show="reverseSort">v</span>
										</span>
									</a>
								</th>
								<th>
									<a ng-click="orderByField='brandName.display'; reverseSort = !reverseSort">
										Brand<span ng-show="orderByField == 'brandName.display'">
										<span ng-show="!reverseSort">^</span>
										<span ng-show="reverseSort">v</span>
									</span>
									</a>
								</th>
								<th>Therapeutic Class/Sub-class</th>
								<th>Dosage</th>
								<th>Frequency</th>
								<th>Quantity</th>
								<th>Supply / Refills / Qty Remaining</th>
								<th>
									<a ng-click="orderByField='prescriberLastname'; reverseSort = !reverseSort">
										Prescriber
										<span ng-show="orderByField == 'prescriberLastname'">
											<span ng-show="!reverseSort">^</span>
											<span ng-show="reverseSort">v</span>
										</span>
									</a>
								</th>
								<th>
									<a ng-click="orderByField='dispensingPharmacy'; reverseSort = !reverseSort">
										Pharmacy
										<span ng-show="orderByField == 'dispensingPharmacy'">
											<span ng-show="!reverseSort">^</span>
											<span ng-show="reverseSort">v</span>
										</span>
									</a>
								</th>
								<th>Rx Count</th>
							</tr>
						</thead>

						<tbody>
							<tr ng-repeat="med in (expandAll ? meds : uniqMeds) | filter : searchtxt | orderBy:orderByField:reverseSort" ng-hide="!expandAll && med.hide" ng-class="getRowClass(med)">
								<th scope="row">{{med.whenPrepared | date}}</th>
								<th>{{med.pickUpDate | date}}</th>
								<td ng-click="getDetailView(med);">
									<a href="#">
										{{med.genericName}}
									</a>
								</td>
								<td>{{med.brandName.display}} {{med.dispensedDrugStrength}} {{med.drugDosageForm}}</td>
								<td>{{med.ahfsClass}} / {{med.ahfsSubClass}}</td>
								<td>{{med.dose}} {{med.doseUnit}}</td>
								<td>{{med | dhdrFrequency}}</td>
								<td>{{med.dispensedQuantity}} {{med.dispensedQuantityUnit}}</td>
								<td>
									Est Days Supply:{{med.estimatedDaysSupply}}
									Refills Remaining:{{med.refillsRemaining}}
									Quantity Remaining:{{med.quantityRemaining}}
								</td>
								<td>{{med.prescriberLastname}}, {{med.prescriberFirstname}} <span ng-if="med.prescriberPhoneNumber">Tel:{{med.prescriberPhoneNumber}}</span></td>
								<td>{{med.dispensingPharmacy}} <span ng-if="med.dispensingPharmacyFaxNumber">Fax:{{med.dispensingPharmacyFaxNumber}}</span></td>
								<td ng-click="showGroupedMeds2(medsWithGroupedDups[med.getUniqVal()])"><span ng-if="med.headRecord"><a>{{medsWithGroupedDups[med.getUniqVal()].length}}</a></span><!-- {{med | json}}  --></td>

							</tr>
						</tbody>
					</table>
				</div>
			</div>
			<div class="row" >
				<div class="col-xs-12">
					<%-- services  --%>
					<h6>DHDR Pharmacy Service<small> <a ng-click="showHideServiceFilter()">Filter</a></small></h6>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12">
					<div ng-show="showServiceFilter()" >
						<form class="form-horizontal">
						  <div class="form-group">
							<label class="col-sm-2 control-label">Pharmacy Service Description</label>
							<div class="col-sm-10">
							  <input ng-model="searchServicetxt.genericName" type="text" placeholder="type to filter" class="form-control" />
							</div>
						  </div>
						  <div class="form-group">
							<label class="col-sm-2 control-label">Pharmacy Service Type</label>
							<div class="col-sm-10">
							  <input ng-model="searchServicetxt.brandName.display" type="text" placeholder="type to filter" class="form-control" />
							</div>
						  </div>
						  <div class="form-group">
							<label class="col-sm-2 control-label">Therapeutic Class</label>
							<div class="col-sm-10">
							  <input ng-model="searchServicetxt.ahfsClass" type="text" placeholder="type to filter" class="form-control" />
							</div>
						  </div>
						  <div class="form-group">
							<label class="col-sm-2 control-label">Last Service Date</label>
							<div class="col-sm-10">
							  <input ng-model="searchServicetxt.whenPrepared" type="text" placeholder="type to filter" class="form-control" />
							</div>
						  </div>
						  <div class="form-group">
							<label class="col-sm-2 control-label">Pharmacy Name</label>
							<div class="col-sm-10">
							  <input ng-model="searchServicetxt.dispensingPharmacy" type="text" placeholder="type to filter" class="form-control" />
							</div>
						  </div>
						  <div class="form-group">
							<label class="col-sm-2 control-label">Pharmacist Name</label>
							<div class="col-sm-10">
							  <input ng-model="searchServicetxt.pharmacistLastname" type="text" placeholder="type to filter" class="form-control" />
							</div>
						  </div>
						  <div class="form-group">
							<label class="col-sm-2 control-label">Pharmacy Fax</label>
							<div class="col-sm-10">
							  <input ng-model="searchServicetxt.dispensingPharmacyFaxNumber" type="text" placeholder="type to filter" class="form-control" />
							</div>
						  </div>
						</form>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12">
					<table class="table table-condensed table-striped table-bordered" ng-show="services.length > 0">
						<thead>
							<tr>
								<td colspan="10">
									{{services.length}} results returned  <button type="button" class="btn btn-default btn-xs" ng-click="toggleExpandAllServices()">{{expandAllServices ? 'Collapse All' : 'Expand All'}}</button>  <button type="button" class="btn btn-default btn-xs" ng-click="printSummary()">Print</button>
								</td>
							</tr>
							<tr>
								<th>
									<a ng-click="serviceOrderByField='whenPrepared'; serviceReverseSort = !serviceReverseSort">Last Service Date <span ng-show="serviceOrderByField == 'whenPrepared'"><span ng-show="!serviceReverseSort">^</span><span ng-show="serviceReverseSort">v</span></span></a>
								</th>
								<th>
									<a ng-click="serviceOrderByField='whenHandedOver'; serviceReverseSort = !serviceReverseSort">Pickup Date <span ng-show="serviceOrderByField == 'whenHandedOver'"><span ng-show="!serviceReverseSort">^</span><span ng-show="serviceReverseSort">v</span></span></a>
								</th>
								<th>
									<a ng-click="serviceOrderByField='brandName.display'; serviceReverseSort = !serviceReverseSort">Pharmacy Service Type<span ng-show="serviceOrderByField == 'brandName.display'"><span ng-show="!serviceReverseSort">^</span><span ng-show="serviceReverseSort">v</span></span></a>
								</th>
								<th>
									<a ng-click="serviceOrderByField='genericName'; serviceReverseSort = !serviceReverseSort">Pharmacy Service Description<span ng-show="serviceOrderByField == 'genericName'"><span ng-show="!serviceReverseSort">^</span><span ng-show="serviceReverseSort">v</span></span></a>
								</th>
								<th>Rx Number</th>
								<th>Therapeutic Class/Sub-class</th>
								<th>
									<a ng-click="serviceOrderByField='dispensingPharmacy'; serviceReverseSort = !serviceReverseSort">Pharmacy Name<span ng-show="serviceOrderByField == 'dispensingPharmacy'"><span ng-show="!serviceReverseSort">^</span><span ng-show="serviceReverseSort">v</span></span></a>
								</th>
								<th>
									<a ng-click="serviceOrderByField='pharmacistLastname'; serviceReverseSort = !serviceReverseSort">Pharmacist<span ng-show="serviceOrderByField == 'pharmacistLastname'"><span ng-show="!serviceReverseSort">^</span><span ng-show="serviceReverseSort">v</span></span></a>
								</th>
								<th>
									<a ng-click="serviceOrderByField='dispensingPharmacyFaxNumber'; serviceReverseSort = !serviceReverseSort">Pharmacy Fax<span ng-show="serviceOrderByField == 'dispensingPharmacyFaxNumber'"><span ng-show="!serviceReverseSort">^</span><span ng-show="serviceReverseSort">v</span></span></a>
								</th>
								<th>Service Count</th>
							</tr>
						</thead>

						<tbody>
							<tr ng-repeat="med in (expandAllServices ? services : uniqServices) | filter : searchServicetxt | orderBy:serviceOrderByField:serviceReverseSort" ng-hide="!expandAllServices && med.hide" ng-class="getRowClass(med)">
								<th scope="row">{{med.whenPrepared | date}}</th>
								<td scope="row">{{med.whenHandedOver | date}}</td>
								<td>{{med.brandName.display}}</td>
								<td>{{med.genericName}} </td>
								<td>{{med.rxNumber}}</td>
								<td scope="row">{{med.ahfsClass}}/{{med.ahfsSubClass}}</td>
								<td>{{med.dispensingPharmacy}}</td>
								<td>{{med.pharmacistLastname}}, {{med.pharmacistFirstname}} </td>
								<td>{{med.dispensingPharmacyFaxNumber}}</td>
								<td ng-click="showGroupedServices2(servicesWithGroupedDups[med.serviceGroupKey])"><span ng-if="med.headRecord"><a>{{servicesWithGroupedDups[med.serviceGroupKey].length}}</a></span><!-- {{med | json}}  --></td>

							</tr>
						</tbody>
					</table>
				</div>
			</div> <!--  end services -->
		</div>
		<div ng-show="viewWhen('comp')">	<!-- comparative view start -->
	 		<div class="row">
		 		<div class="col-xs-12" >
		 			<button type="button" class="btn btn-default btn-xs" ng-click="hideShowDhirPharma()"><span ng-if="hideShowDhirPharmaVal">Hide</span><span ng-if="!hideShowDhirPharmaVal">Show</span> DHDR PharmaServices</button>
		 			<button type="button" class="btn btn-default btn-xs" ng-click="hideShowDhirDrug()"><span ng-if="hideShowDhirDrugVal">Hide</span><span ng-if="!hideShowDhirDrugVal">Show</span> DHDR Drugs</button>
		 			<button type="button" class="btn btn-default btn-xs" ng-click="printComparative()">Print</button>
		 		</div>
		 	</div>
			<div class="row">
				<div class="col-xs-6">
					<div ng-if="hideShowDhirDrugVal">
						<h4>DHDR Drugs <small><a ng-click="showHideFilter()">Filter</a></small></h4>
						<h6>Medication Dispense</h6>
						<div ng-show="showFilter()">
							<form class="form-horizontal">
								<div class="form-group">
									<label class="col-sm-2 control-label">Brand name</label>
									<div class="col-sm-10">
										<input ng-model="searchtxt.brandName.display" type="text" placeholder="type to filter" class="form-control"/>
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-2 control-label">Generic name</label>
									<div class="col-sm-10">
										<input ng-model="searchtxt.genericName" type="text" placeholder="type to filter" class="form-control"/>
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-2 control-label">Dispensed date</label>
									<div class="col-sm-10">
										<input ng-model="searchtxt.whenPrepared" type="text" placeholder="type to filter" class="form-control"/>
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-2 control-label">Pharmacy Name</label>
									<div class="col-sm-10">
										<input ng-model="searchtxt.dispensingPharmacy" type="text" placeholder="type to filter" class="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-2 control-label">Prescriber Name</label>
									<div class="col-sm-10">
										<input ng-model="searchtxt.prescriberLastname" type="text" placeholder="type to filter" class="form-control" />
									</div>
								</div>

							</form>
						</div>
					</div>
				</div>
				<div class="col-xs-6">
					<h4>EMR Local Data</h4>
				</div>
			</div>
	 		<div class="row">
		 		<div class="col-xs-6 table-overflow-x ">
			 		<div ng-if="hideShowDhirDrugVal">
						<table class="table table-condensed table-striped table-bordered" >
				 			<thead>
								<tr>
									<td colspan="6">
										{{meds.length}} results returned
									</td>
								</tr>
				 				<tr> 
				 					<th>
										<a ng-click="orderByField='whenPrepared'; reverseSort = !reverseSort">
											Dispense Date
											<span ng-show="orderByField == 'whenPrepared'">
												<span ng-show="!reverseSort">^</span>
												<span ng-show="reverseSort">v</span>
											</span>
										</a>
									</th>
									<th>
										<a ng-click="orderByField='pickUpDate'; reverseSort = !reverseSort">
											Pickup Date
											<span ng-show="orderByField == 'pickUpDate'">
												<span ng-show="!reverseSort">^</span>
												<span ng-show="reverseSort">v</span>
											</span>
										</a>
									</th>
									<th>
										<a ng-click="orderByField='brandName.display'; reverseSort = !reverseSort">
											Brand
											<span ng-show="orderByField == 'brandName.display'">
												<span ng-show="!reverseSort">^</span>
												<span ng-show="reverseSort">v</span>
											</span>
										</a>
										/
										<a ng-click="orderByField='genericName'; reverseSort = !reverseSort">
											Generic
											<span ng-show="orderByField == 'genericName'">
												<span ng-show="!reverseSort">^</span>
												<span ng-show="reverseSort">v</span>
											</span>
										</a>
									</th>
									<th>Therapeutic Class/Sub-class</th>
									<th>Quantity</th>
									<th>Dosage</th>
									<th>Frequency</th>
				 					<th>Supply / Refills / Qty Remaining</th>
									<th>
										<a ng-click="orderByField='prescriberLastname'; reverseSort = !reverseSort">
											Prescriber
											<span ng-show="orderByField == 'prescriberLastname'">
												<span ng-show="!reverseSort">^</span>
												<span ng-show="reverseSort">v</span>
											</span>
										</a>
									</th>
									<th>
										<a ng-click="orderByField='dispensingPharmacy'; reverseSort = !reverseSort">
											Pharmacy
											<span ng-show="orderByField == 'dispensingPharmacy'">
												<span ng-show="!reverseSort">^</span>
												<span ng-show="reverseSort">v</span>
											</span>
										</a>
									</th>
								</tr> 
							</thead> 
				 			<tbody> 
				 				<tr ng-repeat="med in meds | filter: searchtxt | orderBy: orderByField:reverseSort" ng-class="getRowClass(med)">
				 					<th scope="row">{{med.whenPrepared | date}}</th> 
				 					<th>{{med.pickUpDate | date}}</th>
									<td ng-click="getDetailView(med);">
										<a href="#">
											{{med.brandName.display}} {{med.dispensedDrugStrength}} {{med.drugDosageForm}} ({{med.genericName}})
										</a>
									</td>
									<td>{{med.ahfsClass}} / {{med.ahfsSubClass}}</td>
				 					<td>{{med.dispensedQuantity}} {{med.dispensedQuantityUnit}}</td>
									<td>{{med.dose}}  {{med.doseUnit}}</td>
									<td>{{med | dhdrFrequency}}</td>
				 					<td>
				 						Est Days Supply:{{med.estimatedDaysSupply}}
				 						Refills Remaining:{{med.refillsRemaining}}
										Quantity Remaining:{{med.quantityRemaining}}
				 					</td>
				 					<td>{{med.prescriberLastname}}, {{med.prescriberFirstname}} <span ng-if="med.prescriberPhoneNumber">Tel:{{med.prescriberPhoneNumber}}</span></td>
				 					<td>{{med.dispensingPharmacy}} <span ng-if="med.dispensingPharmacyFaxNumber">Fax:{{med.dispensingPharmacyFaxNumber}}</span></td>
				 					
				 					
				 				</tr>
				 			</tbody> 
				 		</table>
			 		</div>
		 		</div>
		 		<div class="col-xs-6 table-overflow-x " >
		 			<div ng-include="'emrMedsTable.html'"></div>
		 		</div>
		 	</div>
			<div class="row">
				<div class="col-xs-6 table-overflow-x ">
					<div ng-if="hideShowDhirPharmaVal">
						<h6>DHDR Pharmacy Service <small><a ng-click="showHideServiceFilter()">Filter</a></small></h6>
						<div ng-show="showServiceFilter()" >
							<form class="form-horizontal">
								<div class="form-group">
									<label class="col-sm-2 control-label">Pharmacy Service Description</label>
									<div class="col-sm-10">
										<input ng-model="searchServicetxt.genericName" type="text" placeholder="type to filter" class="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-2 control-label">Pharmacy Service Type</label>
									<div class="col-sm-10">
										<input ng-model="searchServicetxt.brandName.display" type="text" placeholder="type to filter" class="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-2 control-label">Therapeutic Class</label>
									<div class="col-sm-10">
										<input ng-model="searchServicetxt.ahfsClass" type="text" placeholder="type to filter" class="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-2 control-label">Last Service Date</label>
									<div class="col-sm-10">
										<input ng-model="searchServicetxt.whenPrepared" type="text" placeholder="type to filter" class="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-2 control-label">Pharmacy Name</label>
									<div class="col-sm-10">
										<input ng-model="searchServicetxt.dispensingPharmacy" type="text" placeholder="type to filter" class="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-2 control-label">Pharmacist Name</label>
									<div class="col-sm-10">
										<input ng-model="searchServicetxt.pharmacistLastname" type="text" placeholder="type to filter" class="form-control" />
									</div>
								</div>
							</form>
						</div>
						<table class="table table-condensed table-striped table-bordered" ng-show="services.length > 0">
							<thead>
							<tr>
								<td colspan="8">
									{{services.length}} results returned
								</td>
							</tr>
							<tr>
								<th>
									<a ng-click="serviceOrderByField='whenPrepared'; serviceReverseSort = !serviceReverseSort">
										Last Service Date
										<span ng-show="serviceOrderByField == 'whenPrepared'">
												 <span ng-show="!serviceReverseSort">&#9650;</span>
												 <span ng-show="serviceReverseSort">&#9660;</span>
											 </span>
									</a>
								</th>
								<th>
									<a ng-click="serviceOrderByField='whenHandedOver'; serviceReverseSort = !serviceReverseSort">
										Pickup Date
										<span ng-show="serviceOrderByField == 'whenHandedOver'">
												 <span ng-show="!serviceReverseSort">&#9650;</span>
												 <span ng-show="serviceReverseSort">&#9660;</span>
											 </span>
									</a>
								</th>
								<th>
									<a ng-click="serviceOrderByField='brandName.display'; serviceReverseSort = !serviceReverseSort">
										Pharmacy Service Type
										<span ng-show="serviceOrderByField == 'brandName.display'">
												 <span ng-show="!serviceReverseSort">&#9650;</span>
												 <span ng-show="serviceReverseSort">&#9660;</span>
											 </span>
									</a>
								</th>
								<th>
									<a ng-click="serviceOrderByField='genericName'; serviceReverseSort = !serviceReverseSort">
										Pharmacy Service Description
										<span ng-show="serviceOrderByField == 'genericName'">
												 <span ng-show="!serviceReverseSort">&#9650;</span>
												 <span ng-show="serviceReverseSort">&#9660;</span>
											 </span>
									</a>
								</th>
								<th>Rx Number</th>
								<th>Therapeutic Class/Sub-class</th>
								<th>
									<a ng-click="serviceOrderByField='dispensingPharmacy'; serviceReverseSort = !serviceReverseSort">
										Pharmacy Name<span ng-show="serviceOrderByField == 'dispensingPharmacy'">
											 <span ng-show="!serviceReverseSort">&#9650;</span>
											 <span ng-show="serviceReverseSort">&#9660;</span>
										 </span>
									</a>
								</th>
								<th>
									<a ng-click="serviceOrderByField='pharmacistLastname'; serviceReverseSort = !serviceReverseSort">
										Pharmacist<span ng-show="serviceOrderByField == 'pharmacistLastname'">
											 <span ng-show="!serviceReverseSort">&#9650;</span>
											 <span ng-show="serviceReverseSort">&#9660;</span>
										 </span>
									</a>
								</th>
							</tr>
							</thead>

							<tbody>
							<tr ng-repeat="med in services | filter : searchServicetxt | orderBy:serviceOrderByField:serviceReverseSort">
								<th scope="row">{{med.whenPrepared | date}}</th>
								<td scope="row">{{med.whenHandedOver | date}}</td>
								<td>{{med.brandName.display}}</td>
								<td>{{med.genericName}} </td>
								<td>{{med.rxNumber}}</td>
								<td>{{med.ahfsClass}} / {{med.ahfsSubClass}}</td>
								<td>{{med.dispensingPharmacy}} - Fax:{{med.dispensingPharmacyFaxNumber}}</td>
								<td>{{med.pharmacistLastname}}, {{med.pharmacistFirstname}} </td>

							</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div class="col-xs-6 table-overflow-x ">
					<h4>EMR Local Data</h4>
					<%-- DHDR08.01(a): the EMR side carries the EMR's medication records (per DHDR05.02),
					     the same table the drug Comparative shows. --%>
					<div ng-include="'emrMedsTable.html'"></div>
				</div>
			</div>
	 	</div> <!-- comparitive view end -->
	</div>
	<%-- DHDR05.02: the EMR medication records. Rendered in both the drug Comparative and
	     the Pharmacy Service Comparative (DHDR08.01a), so it lives in one template. --%>
	<script type="text/ng-template" id="emrMedsTable.html">
		 			<table class="table table-condensed table-striped table-bordered" ng-show="compLocalMeds.length > 0"> 
		 			   	<thead>
							<tr>
								<td colspan="12">
									{{compLocalMeds.length}} results returned
								</td>
							</tr>
			 				<tr>
			 					<%-- DHDR03.05: sortable by dispensed date and generic name, with the
			 					     active element indicated, as on the DHDR tables. --%>
			 					<th>
			 						<a ng-click="emrSort.field='rxDate'; emrSort.reverse = !emrSort.reverse">
			 							Start Date
			 							<span ng-show="emrSort.field == 'rxDate'">
			 								<span ng-show="!emrSort.reverse">^</span>
			 								<span ng-show="emrSort.reverse">v</span>
			 							</span>
			 						</a>
			 					</th>
			 					<th>
			 						<a ng-click="emrSort.field='medication'; emrSort.reverse = !emrSort.reverse">
			 							Medication
			 							<span ng-show="emrSort.field == 'medication'">
			 								<span ng-show="!emrSort.reverse">^</span>
			 								<span ng-show="emrSort.reverse">v</span>
			 							</span>
			 						</a>
			 					</th>
			 					<th>Strength</th>
			 					<th>Dosage</th>
			 					<th>Frequency</th>
			 					<th>Prescriber</th>
			 					<th>DIN</th>
			 					<th>Qty / Duration</th>
			 					<th>Refills</th>
							</tr> 
						</thead> 
		 				<tbody> 
			 				<tr ng-repeat="med in compLocalMeds | orderBy: emrSortValue : emrSort.reverse">
			 					<th scope="row" class="startDate">{{med.rxDate | date}}</th>
			 					<td>{{med.genericName || med.brandName || med.customName}}</td>
			 					<td>{{med.strength}} {{med.strengthUnit}}</td>
			 					<%-- DHDR05.02(e) value + unit of measure; see emrDoseUnit(). --%>
			 					<td><span ng-if="med.takeMin">{{med.takeMin}}<span ng-if="med.takeMax && med.takeMax !== med.takeMin"> - {{med.takeMax}}</span><span ng-if="emrDoseUnit(med)"> {{emrDoseUnit(med)}}</span></span></td>
			 					<td>{{med.frequency}}</td>
			 					<td>{{med.providerName}}</td>
			 					<td>{{med.regionalIdentifier}}</td>
			 					<td>{{med.quantity}}<span ng-if="med.duration"> / {{med.duration}} {{med.durationUnit}}</span></td>
			 					<%-- DHDR05.02(i): refill duration is always days, per the Rx screen's
			 					     own validation, so the unit is a fixed label. --%>
			 					<td>{{med.repeats}}<span ng-if="med.refillQuantity"> ({{med.refillQuantity}}<span ng-if="med.refillDuration"> / {{med.refillDuration}} days</span>)</span></td>
			 				</tr>
			 			</tbody> 
		 			</table>
	</script>
	<script type="text/ng-template" id="myModalContent.html">
        <div class="modal-header">
			<div class="row">
				<div class="col-md-6">
					<h3 class="modal-title" id="modal-title">{{med.genericName}} - {{med.whenPrepared | date}}</h3>
				</div>
				<div class="col-md-6 text-right">
					<button class="btn btn-primary" type="button" ng-click="printDetail()">Print</button>
					<button class="btn btn-warning" type="button" ng-click="cancel()">Cancel</button>
				</div>
			</div>
        </div>
        <div class="modal-body" id="modal-body">
            <div class="md-dialog-content" id="dialogContentApptProvider">
            
            <div class="row">
                <div class="col-xs-11">



                    <table class="table table-bordered table striped" >
                       
						<tr> 
		 					<th>Dispense Date</th> 
							<th scope="row">{{med.whenPrepared | date}}</th>
						</tr>
						<tr>
							<th>Pickup Date</th>
							<th scope="row">{{med.pickUpDate | date}}</th>
						</tr>
						<tr>
		 					<th>Generic</th> 
							<td>{{med.genericName}}</td>
</tr>
						<tr>
		 					<th>Brand</th>
							<td>{{med.brandName.display}}</td>
 </tr>
						<tr>
		 					<th>DIN/PIN</th>
							<td>{{med.brandName.code}}</td>
 						</tr>
						<tr>
		 					<th>Therapeutic Class</th>
							<td>{{med.ahfsClass}}</td>
 						</tr>
						<tr>
		 					<th>Therapeutic Sub-Class</th>
							<td>{{med.ahfsSubClass}}</td>
 						</tr>
						<tr>
		 					<th>Rx Number</th>
							<td>{{med.rxNumber}}</td>
 						</tr>
						<tr>
							<th>Medical Condition/Reason for Use</th>
							<td>
								<%-- DHDR06.01(c): each reasonCode is a CodeableConcept, so the code and display
							     live on its codings - iterating the concept itself walks its properties
							     instead and yields nothing. Falls back to the concept's plain text when it
							     carries no coding. --%>
								<div ng-repeat="rcode in med.reasonCode">
									<div ng-repeat="reason in rcode.coding">({{reason.code}}) -- {{reason.display}}</div>
									<div ng-if="!rcode.coding.length && rcode.text">{{rcode.text}}</div>
								</div>
							</td>
						</tr>

						<tr>
		 					<th>Strength</th>	
							<td>{{med.dispensedDrugStrength}}</td>
						</tr>
						<tr>
		 					<th>Dosage Form</th>
							<td>{{med.drugDosageForm}}</td>
						</tr>
						<tr>
							<th>Dosage</th>
							<td>{{med.dose}} {{med.doseUnit}}</td>
						</tr>
						<tr>
							<th>Frequency</th>
							<td>{{med | dhdrFrequency}}</td>
						</tr>
						<tr>

							<th>Quantity</th>
							<td>{{med.dispensedQuantity}} {{med.dispensedQuantityUnit}}</td>
						</tr>
						<tr>
		 					<th>Est Days Supply</th>
							<td>{{med.estimatedDaysSupply}}</td>
						</tr>

						<tr>
		 					<th>Refills Remaining</th>
							<td>{{med.refillsRemaining}}</td>
						</tr>
						<tr>
		 					<th>Quantity Remaining</th>
							<td>{{med.quantityRemaining}}</td>
						</tr>
						<tr>
							<th>Patient Name</th>
							<td>
								{{dhdrPatient.lastName}}, {{dhdrPatient.firstName}}
								<span ng-show="dhdrPatient.nameUnmatched"> (UNMATCHED)</span>
							</td>
						</tr>
						<tr>
							<th>Patient Gender</th>
							<td>
								{{dhdrPatient.gender}}
								<span ng-show="dhdrPatient.genderUnmatched"> (UNMATCHED)</span>
							</td>
						</tr>
						<tr>
							<th>Patient DOB</th>
							<td>
								{{dhdrPatient.dob | date}}
								<span ng-show="dhdrPatient.dobUnmatched"> (UNMATCHED)</span>
							</td>
						</tr>
						<tr>
							<th>Patient HIN</th>
							<td>
								{{dhdrPatient.hin}}
								<span ng-show="dhdrPatient.hinUnmatched"> (UNMATCHED)</span>
							</td>
						</tr>
						<tr>
		 					<th>Prescriber</th>
							<td>{{med.prescriberLastname}}, {{med.prescriberFirstname}} ({{med.prescriberLicenceNumber.value}})</td>
						</tr>
						<tr>
						<tr>
		 					<th>Prescriber ID</th>
							<td title="{{getLicence(med.prescriberLicenceNumber.system)}}">{{getLicenceMnemonic(med.prescriberLicenceNumber.system)}} ({{med.prescriberLicenceNumber.value}})</td>
						</tr>
						<tr>
		 					<th>Prescriber Phone</th>
							<td>{{med.prescriberPhoneNumber}}</td>
						</tr>	
						<tr>
		 					<th>Pharmacy</th>
							<td>{{med.dispensingPharmacy}}</td>
						</tr>
						<tr>
		 					<th>Pharmacy Fax</th>
							<td>{{med.dispensingPharmacyFaxNumber}}</td>
						</tr> 
						<tr>
		 					<th>Pharmacy Phone</th>
							<td>{{med.dispensingPharmacyPhoneNumber}}</td> 
						</tr> 
						<tr>
							<th>Pharmacist</th>
							<td>{{med.pharmacistLastname}}, {{med.pharmacistFirstname}} ({{med.pharmacistLicenceNumber.value}})
						</tr>
					
<!-- tr>
<td colspan=2>
<pre>{{med}}</pre>
</td>
</tr -->
                        
                    </table>
                </div>
            </div>
        </div>
        </div>
    </script>
    <script type="text/ng-template" id="pcoi.html">
<div class="modal-body" id="modal-body">
		<a ng-if="showUntilLoaded" ng-click="reload()"> Failed to load? click here</a>	
		<a ng-if="viewletNotResponding" ng-click="cancel()"> Viewlet not responding? click here</a>	
		<%-- allow-modals is load-bearing, not incidental: window.print() is gated by the sandbox's
		     modals flag, so dropping it would stop the viewlet printing to a local printer and
		     break DHDR10.04. --%>
		<iframe id="pcoi-frame" class="pcoi-frame" src="{{pcoiUrl}}" sandbox="allow-forms allow-scripts allow-same-origin allow-modals"
				width="540" height="600" ng-onload="loadingResult(state,message)"></iframe>
		
<div>
	</script>
	<script type="text/ng-template" id="drugDupsContent.html">
        <div class="modal-header">
            <h3 class="modal-title" id="modal-title">{{med.genericName}} - {{med.whenPrepared | date}}</h3>
        </div>
        <div class="modal-body" id="modal-body">
            <div class="md-dialog-content" id="dialogContentApptProvider">
            
            <div class="row">
                <div class="col-xs-12">
					<table class="table table-condensed table-striped table-bordered" ng-show="meds.length > 0"> 		 			
		 			<thead> 
		 				<tr> 
		 					<th>Dispense Date</th>
							<th>Pickup Date</th>
		 					<th>Generic</th> 
		 					<th>Brand</th> 
		 					<th>Strength</th>
		 					<th>Dosage Form</th>
							<th>Dosage</th>
							<th>Frequency</th>
		 					<th>Quantity</th>
		 					<th>Est Days Supply</th>
		 					<th>Refills Remaining</th>
							<th>Quantity Remaining</th>
		 					<th>Prescriber</th>
		 					<th>Pharmacy</th>
						</tr> 
					</thead> 
					
		 			<tbody> 
		 				<%-- Order comes from the group itself, which is sorted most-recent-first when the
		 				     search finishes (DHDR04.02). An orderBy here cannot do it: this modal runs on its
		 				     own scope, so a sort field named from the main controller reads as undefined and
		 				     AngularJS then returns the list untouched. --%>
		 				<tr ng-repeat="med in meds | filter : searchtxt">
		 					<th scope="row">{{med.whenPrepared | date}}</th>
							<th>{{med.pickUpDate | date}}</th>
		 					<td ng-click="getDetailView(med);">{{med.genericName}} </td>
		 					<td>{{med.brandName.display}}</td>
		 					<td>{{med.dispensedDrugStrength}}</td>
		 					<td>{{med.drugDosageForm}}</td>
							<td>{{med.dose}} {{med.doseUnit}}</td>
							<td>{{med.frequency}}</td>
		 					<td>{{med.dispensedQuantity}} {{med.dispensedQuantityUnit}}</td>
		 					<td>{{med.estimatedDaysSupply}}</td>
		 					<td>{{med.refillsRemaining}}</td>
							<td>{{med.quantityRemaining}}</td>
		 					<td>{{med.prescriberLastname}}, {{med.prescriberFirstname}} <span ng-if="med.prescriberPhoneNumber">Tel:{{med.prescriberPhoneNumber}}</span></td>
		 					<td>{{med.dispensingPharmacy}} <span ng-if="med.dispensingPharmacyFaxNumber">Fax:{{med.dispensingPharmacyFaxNumber}}</span></td>
		 				</tr> 
		 			</tbody> 
		 		</table>
                </div>
            </div>
        </div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-warning" type="button" ng-click="cancel()">Cancel</button>
        </div>
    </script>
	<script type="text/ng-template" id="pharmaDupsContent.html">
        <div class="modal-header">
            <h3 class="modal-title" id="modal-title">{{med.genericName}} - {{med.whenPrepared | date}}</h3>
        </div>
        <div class="modal-body" id="modal-body">
            <div class="md-dialog-content" id="dialogContentApptProvider">
            
            <div class="row">
                <div class="col-xs-12">
					<table class="table table-condensed table-striped table-bordered" ng-show="services.length > 0"> 		 			
		 			<thead> 
		 				<tr> 
		 					<th>Last Service Date </th> 
		 					<th>Pickup Date</th> 
		 					<th>Pharmacy Service Type</th> 
		 					<th>Pharmacy Service Description</th> 
							<th>Pharmacy Name</th>
		 					<th>Pharmacist</th>
		 					<th>Pharmacy Fax</th>
						</tr> 
					</thead> 
					 
		 			<tbody> 
		 				<%-- Ordered by the group, as in the drug modal above (DHDR07.02). --%>
		 				<tr ng-repeat="med in services | filter : searchServicetxt" >
		 					<th scope="row">{{med.whenPrepared | date}}</th> 
		 					<td scope="row">{{med.whenHandedOver | date}}</td>
		 					<td>{{med.brandName.display}}</td> 
		 					<td>{{med.genericName}} </td>
		 					<td>{{med.dispensingPharmacy}}</td>
		 					<td>{{med.pharmacistLastname}}, {{med.pharmacistFirstname}} </td>
		 					<td>{{med.dispensingPharmacyFaxNumber}}</td>
		 				</tr> 
		 			</tbody> 
		 		</table>
                </div>
            </div>
        </div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-warning" type="button" ng-click="cancel()">Cancel</button>
        </div>
    </script>
	<script>
		var app = angular.module("dhdrView", ['demographicServices','providerServices','dhdrServices','oscarFilters','ui.bootstrap','rxServices']);
		
		//app.config(['$locationProvider'],function($locationProvider ) {
		//	$locationProvider.html5Mode(true);
		//});

		// DHDR04.01: the frequency cell is built from four parts, none of which the DHDR service
		// is obliged to send - dosageInstruction is optional under the consumer profile, and the
		// IG's own pharmacy-service example carries none. Interpolating the parts directly left a
		// record with no dosage instruction reading "every  -", which looks like a frequency
		// rather than like the absence of one. Composed only when there is something to compose.
		// A filter rather than a scope method because the detail view renders in its own modal
		// scope, where a helper hung off the main controller would silently resolve to nothing.
		app.filter('dhdrFrequency', function(){
			return function(med){
				if(!med){ return ""; }
				var parts = [med.frequency, med.period, med.periodMax, med.periodUnit];
				var present = parts.some(function(p){
					return p !== undefined && p !== null && String(p).trim() !== "";
				});
				if(!present){ return ""; }
				// Same shape as before for a record that does carry the parts, so nothing that
				// already printed changes.
				return (med.frequency || "") + " every " + (med.period || "")
						+ " - " + (med.periodMax || "") + " " + (med.periodUnit || "");
			};
		});


		app.controller("dhdrView", function($scope,demographicService,providerService,dhdrService,rxService,$location,$window,$modal,$http,$filter,$timeout) {

			$scope.demographicNo = $location.search().demographic_no;
			
			//if($scope.demographicNo == undefined){
				var urlParams = new URLSearchParams(window.location.search);
				$scope.demographicNo = urlParams.get("demographic_no");
			//}
			$scope.demographic = {};
			activeProvidersHash = {};
			$scope.meds = [];
			$scope.uniqMeds = [];
			$scope.uniqServices = [];
			$scope.services = [];
			$scope.outcomes = [];
			// DHDR14.01: notices about the DHDR EHR Service itself (unreachable, unresponsive,
			// expired session). Distinct from outcomes, which are the issues the service reported.
			$scope.serviceErrors = [];
			// DHDR02.03: clinic-level default, from the dhdr.default_search_days property.
			defaultDaysToSearch = <%= defaultSearchDays %>;
			// The bounds are yyyy-MM-dd strings, never Dates: a Date is an instant, and converting a
			// zone-less calendar date to one shifts it a day behind UTC. Convert once, here.
			asSearchDate = function(d){ return $filter('date')(d, "yyyy-MM-dd"); };
			// Joins the parts directly - new Date(y+"-"+m+"-"+d) reads as ISO, so UTC midnight.
			// Null when a part is missing, so callers cannot build a "null-null-null" bound.
			partsAsSearchDate = function(y, m, d){
				if (!y || !m || !d) { return null; }
				let pad = function(v){ return (String(v).length < 2 ? "0" : "") + v; };
				return y + "-" + pad(m) + "-" + pad(d);
			};
			// DHDR03.06: dates must read the same across every DHDR view, so the patient header and
			// the "searched with" line render through the same default medium format the tables use
			// rather than the raw yyyy-MM-dd parts. Keeps the placeholder when no date of birth is
			// recorded, so the gap stays visible instead of collapsing to nothing.
			$scope.demographicDobText = function(){
				let iso = partsAsSearchDate($scope.demographic.dobYear,
						$scope.demographic.dobMonth, $scope.demographic.dobDay);
				return iso ? $filter('date')(iso) : "--";
			};
			// DHDR03.02: how a differing field reads in the patient banner. Absent from the EMR and
			// disagreeing with the EMR are both flagged, but they call for different action.
			$scope.matchNote = function(unmatched, missing){
				if (!unmatched) { return ""; }
				return missing ? " (NOT IN EMR)" : " (UNMATCHED)";
			};
			// DHDR02.03(c) searches all available events, normally from the patient's birth. Where
			// there is no date of birth to start from, this floor stands in: dropping the lower
			// bound is not equivalent, because the service then applies its own 120-day default
			// (BP5) and "Search All" would quietly become a 120-day search.
			let earliestSearchDate = "1900-01-01";
			$scope.searchConfig = {};
			// UI-only, deliberately NOT on searchConfig: that object is posted verbatim to
			// searchByDemographicNo2, and an unknown field there fails Jackson deserialization.
			$scope.searchDays = defaultDaysToSearch;
			// DHDR02.03(a): fill the range from a number of days before today. Writes only into the
			// two date fields, never back into defaultDaysToSearch - DHDR02.03 requires that editing
			// the range while searching must not change the configured default.
			$scope.applySearchDays = function(){
				let days = parseInt($scope.searchDays, 10);
				if (isNaN(days) || days < 1) { return; }
				let end = new Date();
				let start = new Date(end);
				start.setDate(end.getDate() - days);
				$scope.searchConfig.endDate = asSearchDate(end);
				$scope.searchConfig.startDate = asSearchDate(start);
			};
			// Hand-editing a date, or searching all events, means the range is no longer the one the
			// days box describes, so clear it rather than leave a number that misstates the period.
			$scope.clearSearchDays = function(){ $scope.searchDays = null; };
			$scope.applySearchDays();

			// DHDR02.05: state the period actually searched. With no start date the query carries no
			// lower bound, so saying "<blank> to <end>" would advertise a period we never asked for.
			$scope.searchPeriodText = function(){
				// DHDR03.06: shown in the tables' format. Safe on a yyyy-MM-dd string - the date
				// filter reads it as local, unlike new Date(), which would land a day earlier.
				let shown = function(d){ return d ? $filter('date')(d) : d; };
				let start = shown($scope.searchConfig.startDate);
				let end = shown($scope.searchConfig.endDate);
				if (!start && !end) { return "all available events"; }
				if (!start) { return "all events up to " + end; }
				if (!end) { return start + " onwards"; }
				return start + " to " + end;
			};
			$scope.searching = false;
			$scope.showSummaryProductFilter = false;
			$scope.showSummaryServiceFilter = false;
			$scope.hideShowDhirPharmaVal = true;
			$scope.hideShowDhirDrugVal = true;
			$scope.dhdrPatient = {
				firstName: String,
				lastName: String,
				gender: String,
				dob: String,
				hin: String,
				nameUnmatched: false,
				genderUnmatched: false,
				dobUnmatched: false,
				hinUnmatched: false
			};
			$scope.dhdrPatientResolved = false;
			$scope.dhdrPatientData = "";
			$scope.patientDataUnmatched = false;
			$scope.dhdrPatientDataUnmatched = false;
			$scope.searchComplete = false;
			$scope.buttonDisabled = false;

			
			$scope.hideShowDhirPharma = function(){
				if($scope.hideShowDhirPharmaVal){
					$scope.hideShowDhirPharmaVal = false;
				}else{
					$scope.hideShowDhirPharmaVal = true;
				}
			}
			
			$scope.hideShowDhirDrug = function(){
				if($scope.hideShowDhirDrugVal){
					$scope.hideShowDhirDrugVal = false;
				}else{
					$scope.hideShowDhirDrugVal = true;
				}
			}
			
			$scope.showHideFilter = function(){
				if($scope.showSummaryProductFilter){
					$scope.searchtxt = {};
					$scope.showSummaryProductFilter =false;
				}else{
					$scope.showSummaryProductFilter = true;
				}
			}
			
			$scope.showFilter = function(){
				return $scope.showSummaryProductFilter;
			}
			
			$scope.showHideServiceFilter = function(){
				if($scope.showSummaryServiceFilter){
					$scope.searchServicetxt = {};
					$scope.showSummaryServiceFilter =false;
				}else{
					$scope.showSummaryServiceFilter = true;
				}
			}
			
			$scope.showServiceFilter = function(){
				return $scope.showSummaryServiceFilter;
			}
			
			
			
			
			getAllActiveProviders = function(){
	    			providerService.getAllActiveProviders().then(function(data){
		    			$scope.activeProviders = data;
		    			angular.forEach($scope.activeProviders, function(provider) {
		    				activeProvidersHash[provider.providerNo] = provider;
		    			});
				});
	    		};
	    		
	    		getAllActiveProviders();
	    		
	    		$scope.getProviderName = function(providerNumber){
	    			provider = activeProvidersHash[providerNumber];
	    			if(provider == null){ return providerNumber+" N/A inactive"}
	    			return provider.lastName+", "+provider.firstName;
	    		}

			// DHDR05.02(e): dose unit for an EMR med. Not med.doseUnit - that is the DHDR
			// side's, off doseQuantity.unit, and can be a mass. Prefer drugs.unit, but it
			// often just repeats the strength unit, so fall back to the drug form where the
			// form names a countable unit (DemographicExportHelper's list). Anything else
			// has no unit to show, and DHDR05.02 asks for these "if available".
			let emrDoseUnitForms = ["capsule","drop","dosing","grobule","granule","patch","pellet","pill","tablet"];
			$scope.emrDoseUnit = function(med){
				if (!med) { return ""; }
				let unit = med.unit ? med.unit.toLowerCase() : "";
				let strengthUnit = med.strengthUnit ? med.strengthUnit.toLowerCase() : "";
				if (unit && unit !== strengthUnit) { return unit; }
				let form = med.form ? med.form.toLowerCase() : "";
				for (let i = 0; i < emrDoseUnitForms.length; i++) {
					if (form.indexOf(emrDoseUnitForms[i]) !== -1) { return emrDoseUnitForms[i]; }
				}
				return "";
			}

			// DHDR03.05: the EMR side must sort by the same two elements as the DHDR side - generic
			// name and dispensed date - and show which one is active. Held on an object because the
			// EMR table is ng-included twice, and a primitive assigned from either include would
			// shadow the parent scope instead of writing through to it. Defaults to date descending,
			// which is what DHDR03.01 asks the view to open on.
			$scope.emrSort = { field: "rxDate", reverse: true };
			// Sorts on the value the Medication column actually displays, which falls back through
			// brand and custom name when the EMR holds no generic name.
			$scope.emrSortValue = function(med){
				if ($scope.emrSort.field === "medication") {
					return (med.genericName || med.brandName || med.customName || "").toUpperCase();
				}
				return med.rxDate;
			};

			$scope.orderByField = 'whenPrepared';
			$scope.reverseSort = true;
			
			$scope.serviceOrderByField = 'whenPrepared';
			$scope.serviceReverseSort = true;
			
			
			////////
			currentViewValue = 'summary'
			
			$scope.showSummary = function(){
				currentViewValue = 'summary';
			}
			
			$scope.printSummary = function(){
				var toPrint = {};
				toPrint.meds = $scope.meds;
				toPrint.services = $scope.services;
				// DHDR13.01.b: the DHDR-side patient demographic printed on each page
				toPrint.dhdrPatient = $scope.dhdrPatient;
				// Already yyyy-MM-dd; re-filtering could shift the day.
				toPrint.startDate = $scope.searchConfig.startDate;
				toPrint.endDate   = $scope.searchConfig.endDate;

				$http.post('../ws/rs/dhdr/'+$scope.demographicNo+'/print/summary',toPrint,{ responseType: 'arraybuffer' }).then(function (response) {
					
				       var file = new Blob([response.data], {type: 'application/pdf'});
				       var fileURL = URL.createObjectURL(file);
				       window.open(fileURL);
				}, function(errorMessage) {
					alert("Error getting printout");
					//rxComp.error = errorMessage;
				});	
				//window.open('../ws/rs/dhdr/'+$scope.demographicNo+'/print/summary','_blank');
			}
			
			$scope.printComparative = function(){
				var toPrint = {};
				toPrint.meds = $scope.meds;
				toPrint.services = $scope.services;
				toPrint.localData = $scope.compLocalMeds;
				// DHDR13.01.b: the DHDR-side patient demographic printed on each page
				toPrint.dhdrPatient = $scope.dhdrPatient;
				// Already yyyy-MM-dd; re-filtering could shift the day.
				toPrint.startDate = $scope.searchConfig.startDate;
				toPrint.endDate   = $scope.searchConfig.endDate;

				$http.post('../ws/rs/dhdr/'+$scope.demographicNo+'/print/comparative',toPrint,{ responseType: 'arraybuffer' }).then(function (response) {
					
				       var file = new Blob([response.data], {type: 'application/pdf'});
				       var fileURL = URL.createObjectURL(file);
				       window.open(fileURL);
				}, function(errorMessage) {
					alert("Error getting printout");
					//rxComp.error = errorMessage;
				});	
				//window.open('../ws/rs/dhdr/'+$scope.demographicNo+'/print/summary','_blank');
			}
			
			$scope.showComp = function() {
				currentViewValue = 'comp';	
					rxService.getMedications($scope.demographicNo, "").then(function(data) {
						// The drug list arrives as `drug`, not `content`: DrugSearchResponse annotates
						// getContent() with @XmlElement(name="drug") inside @XmlElementWrapper("content"),
						// and the wrapper only survives in XML - the JSON this endpoint returns is
						// {"drug":[...]}. Reading `content` left the EMR side of the comparison empty.
						$scope.compLocalMeds = data.data.drug;

						angular.forEach($scope.compLocalMeds,function(med){
							med.providerName = $scope.getProviderName(med.providerNo);
						});
						
					}, function(errorMessage) {
						//rxComp.error = errorMessage;
					});	
				
			}
			
			$scope.currentView = function(view){
				if(currentViewValue === view){
					return "active";
				}
			}
			
			$scope.viewWhen = function(view){
			
				if(currentViewValue === view){
					return true;
				}
			}
			

			
			
			
			$scope.setSearchDateToAll = function(){

				
				$scope.clearSearchDays();
				$scope.searchConfig.startDate = partsAsSearchDate($scope.demographic.dobYear,
					$scope.demographic.dobMonth, $scope.demographic.dobDay) || earliestSearchDate;
				$scope.searchConfig.endDate = asSearchDate(new Date());
				$scope.callSearch();
			}
			
			$scope.medsWithGroupedDups = [];

			// DHDR04.03: groups are collapsed by default (only the most-recent event per
			// group shows via uniqMeds). This single-action toggle expands every group at
			// once by switching the drug table to the full flat event list (meds), then
			// collapses back to the grouped heads. Per-group expand stays on the Rx Count
			// modal (showGroupedMeds2).
			$scope.expandAll = false;
			$scope.toggleExpandAll = function(){
				$scope.expandAll = !$scope.expandAll;
			}

			// DHDR07.03: pharmacy service groups collapse by default (only the group head
			// shows via uniqServices). This single-action toggle expands every pharmacy
			// group at once by switching the service table to the full flat event list
			// (services), then collapses back to the grouped heads. Mirrors the drug
			// expand-all; per-group expand stays on the service-count modal
			// (showGroupedServices2).
			$scope.expandAllServices = false;
			$scope.toggleExpandAllServices = function(){
				$scope.expandAllServices = !$scope.expandAllServices;
			}


			// DHDR14.01: one severity-to-style map for every notice. An informational outcome - a
			// successful temporary unblock (CONSENT_TEMP_UNBLOCK) or COVaxON being unavailable - is
			// not an error and must not read as a red one; only error/fatal are danger.
			$scope.noticeClass = function(severity){
				if(severity === "warning"){ return "alert-warning"; }
				if(severity === "information" || severity === "informational"){ return "alert-info"; }
				return "alert-danger";
			}
			// The leading label on the code line, so an informational notice does not announce an
			// "Error code" for an event that is not an error.
			$scope.noticeCodeLabel = function(severity){
				if(severity === "warning"){ return "Warning code:"; }
				if(severity === "information" || severity === "informational"){ return "Notice code:"; }
				return "Error code:";
			}
			$scope.issueClass = function(issue){
				return $scope.noticeClass(issue.severity);
			}

			// DHDR14.01: the actionable code is in issue.details.coding.code or issue.diagnostics;
			// issue.code is only the FHIR issue-type token.
			$scope.dhdrCode = function(issue){
				if(issue.details && issue.details.coding && issue.details.coding.length > 0 && issue.details.coding[0].code){
					return issue.details.coding[0].code;
				}
				return issue.diagnostics ? issue.diagnostics : issue.code;
			}

			// DHDR02.04: error/fatal and consent-block issues suppress the empty-state; an
			// informational outcome (e.g. COVaxON down) must not.
			$scope.hasBlockingOutcome = function(){
				for(var oi = 0; oi < $scope.outcomes.length; oi++){
					var iss = $scope.outcomes[oi].issues || [];
					for(var ii = 0; ii < iss.length; ii++){
						if(iss[ii].severity === "error" || iss[ii].severity === "fatal" || iss[ii].code === "suppressed"){
							return true;
						}
					}
				}
				return false;
			}

			$scope.serviceErrorClass = function(serviceError){
				return $scope.noticeClass(serviceError.severity);
			}
			
			
			$scope.compDhirMeds = [];
			$scope.compLocalMeds = [];
			
			/*$scope.fillCompView = function(){
				var compsearchConfig = {};
				compsearchConfig.startDate = new Date($scope.demographic.dobYear+"-"+$scope.demographic.dobMonth+"-"+$scope.demographic.dobDay);
				compsearchConfig.endDate = new Date();
			
				dhdrService.searchByDemographicNo2($scope.demographicNo,compsearchConfig).then(function(response){
					$scope.searching = false;
					$scope.compDhirMeds = [];
					$scope.outcomes = [];
					for (x of  response.entry) {
						if(x.resource.resourceType === "OperationOutcome"){
							var o = new OperationOutcome(x);
							$scope.outcomes.push(o);
						}else if(x.resource.resourceType === "MedicationDispense"){
							var d = new MedicationDispense(x);
							$scope.compDhirMeds.push(d);
						}
					}
				},function(reason){
					$scope.searching = false;
					alert(reason);
				});
				
				
				rxService.getMedications($scope.demographicNo, "").then(function(data) {
					// See showComp: the endpoint returns the list as `drug`, not `content`.
					$scope.compLocalMeds = data.data.drug;
				}, function(errorMessage) {
					//rxComp.error = errorMessage;
				});
				
			};
			*/
			
			
			/*
			$scope.$watch('location.search()', function() {
		        $scope.demographicNo = ($location.search()).demographicNo;
		        getDemo();
		    }, true);
*/
			
			
			$scope.showGroupedMeds = function(med) {
				hiddenGroup = $scope.medsWithGroupedDups[med.getUniqVal()];
				//for (x of  hiddenGroup) {
				//	x.hide = false;
				//}
				
				var currentlyHasHiddenItems = false; 
				for (x of  hiddenGroup) {
					if(x.hide){
						currentlyHasHiddenItems = true;
					}
				}
				
				if(currentlyHasHiddenItems){
					for (x of  hiddenGroup) {
						x.hide = false;
					}
				}else{
					for (x of  hiddenGroup) {
						if(x.hiddenRecord){
							x.hide = true;	
						}
					}
				}
				
			}
			
			$scope.showGroupedService = function(med){
				hiddenGroup = $scope.servicesWithGroupedDups[med.serviceGroupKey];
				var currentlyHasHiddenItems = false; 
				for (x of  hiddenGroup) {
					if(x.hide){
						currentlyHasHiddenItems = true;
					}
				}
				if(currentlyHasHiddenItems){
					for (x of  hiddenGroup) {
						x.hide = false;
					}
				}else{
					for (x of  hiddenGroup) {
						if(x.hiddenRecord){
							x.hide = true;	
						}
					}
				}
			}
			
			$scope.getRowClass = function(med){
				// Highlight rows when the DHDR patient does not match the EMR demographic.
				if($scope.dhdrPatientDataUnmatched){
					return "warning";
				}
			}
			
			processEntries = function(entries){
				var skipped = 0;
				for (x of entries) {
				  try {
					if(x.resource.resourceType === "OperationOutcome"){
						var o = new OperationOutcome(x);
						$scope.outcomes.push(o);
					}else if(x.resource.resourceType === "MedicationDispense"){
						var d = new MedicationDispense(x);
						if (d.patient) {
							$scope.dhdrPatient.firstName = getFirstName(d.patient.name);
							$scope.dhdrPatient.lastName = getLastName(d.patient.name);
							$scope.dhdrPatient.gender = d.patient.gender || "";
							$scope.dhdrPatient.dob = d.patient.birthDate || "";
							$scope.dhdrPatient.hin = getPatientIdentifier(d.patient.identifier);
							$scope.dhdrPatientResolved = true;
						}

						function getFirstName(nameArray) {
							if (Array.isArray(nameArray) && nameArray.length > 0) {
								const givenNames = nameArray[0].given || [];
								return givenNames[0] || "";
							}
							return "";
						}

						function getLastName(nameArray) {
							if (Array.isArray(nameArray) && nameArray.length > 0) {
								return nameArray[0].family || "";
							}
							return "";
						}

						function getPatientIdentifier(identifierArray) {
							if (Array.isArray(identifierArray) && identifierArray.length > 0) {
								return identifierArray[0].value || "";
							}
							return "";
						}

						if(d.categoryCode === "service"){
							$scope.services.push(d);

							//if ($scope.medsWithGroupedDups.indexOf(d.getUniqVal()) === -1) {
							// Fall back when the event carries no brand identifier, so the deref cannot throw
							// and abort the loop. Key is stored so the badge and expand handler agree.
							var svcKey = (d.brandName && d.brandName.display) ? d.brandName.display : (d.genericName || 'Unknown Product');
							d.serviceGroupKey = svcKey;
							// Only collect here. Which member heads the group, and which are hidden behind
							// it, is decided once by regroupByMostRecent below - deciding it here too
							// would mean picking whichever event happened to arrive first.
							if($scope.servicesWithGroupedDups[svcKey] === undefined){
								$scope.servicesWithGroupedDups[svcKey] = [];
							}
							$scope.servicesWithGroupedDups[svcKey].push(d);

						}else{

							///
							$scope.meds.push(d);

							//if ($scope.medsWithGroupedDups.indexOf(d.getUniqVal()) === -1) {
							if($scope.medsWithGroupedDups[d.getUniqVal()] === undefined){
								$scope.medsWithGroupedDups[d.getUniqVal()] = [];
							}
							$scope.medsWithGroupedDups[d.getUniqVal()].push(d);
						}
					}
				  } catch (e) {
					// One malformed entry must not truncate the whole list. Without this the loop aborts and
					// every later entry is silently lost, leaving a result set that still looks complete - so
					// count the record and surface it below rather than dropping it quietly. Structural detail
					// only, never the record's contents, so no PHI reaches the console.
					skipped++;
					console.error("DHDR: skipped unreadable entry - " + e.message);
				  }
				}

				// Reported through the same notice list as the consent and search errors, so a record the
				// viewer could not read is raised where the reader already looks for service problems.
				if (skipped > 0) {
					$scope.serviceErrors.push({
						httpMessage: skipped + (skipped === 1 ? " record was" : " records were")
							+ " returned by DHDR but could not be displayed.",
						httpCode: "DHDR02.01",
						severity: "error",
						dateTime: new Date().toLocaleString(),
						moreInformation: "The data for these records was incomplete or in an unexpected format."
							+ " The remaining records are shown. Use other available sources of medication"
							+ " information to confirm this patient's history."
					});
				}

				// Groups are only complete once this page's entries are in, and a later page can carry
				// an event newer than the head chosen so far, so re-derive rather than patch.
				regroupByMostRecent($scope.medsWithGroupedDups, $scope.uniqMeds);
				regroupByMostRecent($scope.servicesWithGroupedDups, $scope.uniqServices);

				//If a block record is found the other warnings are dumped.  Probably a bad idea but OMD's requirement.
				for(outcome of $scope.outcomes) {
					var replaceIssue = null;
					for(issue of outcome.issues){	
						if	(issue.code === 'suppressed'){
							replaceIssue = issue;
						}
					}
					if(replaceIssue != null){
						outcome.issues = [];
						outcome.issues.push(replaceIssue);
					}
				}
				
			};

			// DHDR04.02 / DHDR07.02: a group is represented by its most recent event, and the events
			// inside it are listed most recent first. Both are settled here rather than by the order
			// entries arrived in, because that order belongs to the service: `_sort=-whenprepared` is
			// a hint a FHIR server MAY ignore, and pages arrive one at a time, so a group's newest
			// event can land after its head was already chosen. Sorts through the same orderBy the
			// tables use, so the view has one ordering rule rather than two that could drift.
			// Rewrites uniq in place - the tables bind to that array, so it must keep its identity.
			regroupByMostRecent = function(groups, uniq){
				uniq.length = 0;
				for(var key in groups){
					var members = $filter('orderBy')(groups[key], 'whenPrepared', true);
					if(!members.length){ continue; }
					groups[key] = members;
					for(var i = 0; i < members.length; i++){
						// The head is the row the Summary view shows; the rest stay hidden behind it.
						// hiddenRecord marks a member as non-head for good. Only showGroupedMeds and
						// showGroupedService read it, and no template calls either any more, so it is
						// carried rather than relied on - dropping it would leave those two silently
						// wrong about which rows to re-hide if they are ever wired back up.
						members[i].headRecord = (i === 0);
						members[i].hide = (i > 0);
						members[i].hiddenRecord = (i > 0);
					}
					uniq.push(members[0]);
				}
			};

			$scope.openWindow = function(url) {
				window.open(url, '_blank');
			}
			
			$scope.callSearch = function(){

				$scope.buttonDisabled = true;
				$scope.meds = [];
				$scope.services = [];
				$scope.outcomes = [];
				$scope.serviceErrors = [];
				$scope.uniqMeds = [];
				$scope.uniqServices = [];
				$scope.expandAll = false;
				$scope.expandAllServices = false;
				$scope.overrideResultMessage = '';

				$scope.medsWithGroupedDups = [];
				$scope.servicesWithGroupedDups = [];
				$scope.searchConfig.searchId = null;
				$scope.searchConfig.pageId = null;
				$scope.searchComplete = false;
				$scope.dhdrPatientResolved = false;

				// DHDR02.02: the HCN is mandatory in the request and must not be sent absent. The
				// viewer auto-fires on load, so refuse to dispatch rather than send an empty hcn| query.
				if(!$scope.demographic || !$scope.demographic.hin || !((''+$scope.demographic.hin).trim())){
					$scope.buttonDisabled = false;
					$scope.searching = false;
					$scope.serviceErrors.push({
						httpMessage: "Cannot search the DHDR: this patient has no Health Card Number on file.",
						httpCode: "DHDR02.02",
						severity: "error",
						dateTime: new Date().toLocaleString(),
						moreInformation: "A Health Card Number is required to query the DHDR EHR Service. Add the HCN to the patient record and try again."
					});
					return;
				}

				search($scope.demographicNo,$scope.searchConfig);
			
			}
		
			search = function(demographicNo,searchConfig){
				$scope.searching = true;
				dhdrService.searchByDemographicNo2(demographicNo,searchConfig).then(function(response){

					$scope.buttonDisabled = false;
					$scope.searching = false;

					if(angular.isUndefined(response.entry)){
						if(angular.isDefined(response.resourceType) && response.resourceType === "OperationOutcome"){
							var o = new OperationOutcome(response);
							$scope.outcomes.push(o);
							return;
						} else if (angular.isDefined(response.httpCode)) {
							// DHDR14.01: the service could not be reached. Render the notice rather
							// than alert()ing it, and stop - there is no bundle to process, and the
							// empty-state message (DHDR02.04) would misreport the failure as "no
							// records found".
							$scope.serviceErrors.push(response);
							return;
						}
					}
						
					// A valid search with zero results may omit the entry array; guard so the empty-state
					// (DHDR02.04) renders instead of throwing on an undefined entry list.
					if (response.entry) {
						processEntries(response.entry);
					}

					$scope.searchComplete = true;

					// Only run the EMR<->DHDR patient-match comparison once processEntries has populated
					// dhdrPatient from a returned record; on a zero-result search its fields are still the
					// placeholder String constructor and .toUpperCase() would throw (breaks DHDR02.04).
					if ($scope.dhdrPatient && typeof $scope.dhdrPatient.firstName === "string") {
						// Built the same way as a search bound, so the parts are zero-padded before
						// being compared with the DHDR record's yyyy-MM-dd birthDate.
						let demographicDob = partsAsSearchDate($scope.demographic.dobYear,
								$scope.demographic.dobMonth, $scope.demographic.dobDay);
						let dhdrGenderInitial = angular.isDefined($scope.dhdrPatient.gender)
								? $scope.dhdrPatient.gender.charAt(0).toUpperCase() : "";
						// DHDR03.02 asks that anything not matching be identified, so every difference
						// is flagged - including a field the EMR simply does not hold. That case reads
						// differently in the banner: a missing value needs filling in, a disagreeing
						// one needs reconciling, and they are not the same instruction to the reader.
						let blank = function(v){
							return v === null || v === undefined || String(v).trim() === "";
						};
						// The EMR holds a name as the clinic typed it and the DHDR record as the ministry
						// holds it, so compare them folded: a difference in case or padding is not a
						// difference in identity, and flagging one sends the reader to reconcile two
						// records that already agree.
						let differs = function(emr, dhdr){
							return String(blank(emr) ? "" : emr).trim().toUpperCase()
									!== String(blank(dhdr) ? "" : dhdr).trim().toUpperCase();
						};

						$scope.dhdrPatient.nameMissing = blank($scope.demographic.firstName)
								|| blank($scope.demographic.lastName);
						$scope.dhdrPatient.nameUnmatched =
								differs($scope.demographic.firstName, $scope.dhdrPatient.firstName)
								|| differs($scope.demographic.lastName, $scope.dhdrPatient.lastName);

						$scope.dhdrPatient.genderMissing = blank($scope.demographic.sex);
						$scope.dhdrPatient.genderUnmatched = differs($scope.demographic.sex, dhdrGenderInitial);

						$scope.dhdrPatient.dobMissing = blank(demographicDob);
						$scope.dhdrPatient.dobUnmatched = (demographicDob !== $scope.dhdrPatient.dob);

						$scope.dhdrPatient.hinMissing = blank($scope.demographic.hin);
						$scope.dhdrPatient.hinUnmatched = ($scope.demographic.hin !== $scope.dhdrPatient.hin);

						$scope.dhdrPatientDataUnmatched = ($scope.dhdrPatient.nameUnmatched
								|| $scope.dhdrPatient.hinUnmatched || $scope.dhdrPatient.dobUnmatched
								|| $scope.dhdrPatient.genderUnmatched);
					}

					if(response.link && response.link.some(function(l){ return l.relation === "next"; })){
						$scope.searchConfig.searchId = response.id;
						if($scope.searchConfig.pageId == null){
							$scope.searchConfig.pageId = 2;
						
						}else{
							$scope.searchConfig.pageId = $scope.searchConfig.pageId+1;
						}
						search($scope.demographicNo,$scope.searchConfig);
					}
					
					
					
				},function(reason){
					$scope.searching = false;
					$scope.buttonDisabled = false;
					$scope.serviceErrors.push(reason);
				});
			}

			getDemo = function(){
				demographicService.getDemographic($scope.demographicNo).then(function(response){
					$scope.demographic = response;
					//search($scope.demographicNo,$scope.searchConfig);
					$scope.callSearch();
				},function(reason){
					alert(reason);
				});
			};
			
			getDemo();
			
			$scope.getDetailView = function(med,$event){
			    
	    		    var modalInstance = $modal.open({
	    		      
	    		      templateUrl: 'myModalContent.html',
	    		      controller: 'ModalInstanceCtrl',
	    		      controllerAs: 'mpa',
	    		      parent: angular.element(document.body),
	    		      size: 'lg',
	    		      appendTo: $event,
	    		      resolve: {
	    		    	  	
	    		    	  		med: function () {
	    		          		return med;
	    		        		},
	    		        		demoNo: function () {
	    		          		return $scope.demographicNo;
	    		        		},
						  		dhdrPatient: function() {
								return $scope.dhdrPatient;
								}
	    		      }
	    		    });

	    		    modalInstance.result.then(function (selectedItem) {
	    		      selected = selectedItem;
	    		    }, function () {
	    		      // Dismissal is not an error; the empty handler keeps AngularJS from
	    		      // reporting the dismissed modal as an unhandled rejection.
	    		    });
    		  };
	    	
    		  
    		  $scope.showGroupedMeds2 =function(meds,$event){
				var modalInstance = $modal.open({
	    		      
	    		      templateUrl: 'drugDupsContent.html',
	    		      controller: 'DrugDupsInstanceCtrl',
	    		      controllerAs: 'ddpa',
	    		      parent: angular.element(document.body),
	    		      size: 'lg',
	    		      appendTo: $event,
	    		      resolve: {
	    		    	  	
	    		    	  		meds: function () {
	    		          		return meds;
	    		        		},
	    		        		getDetailView: function () {
	    		          		return $scope.getDetailView;
	    		        		} 
	    		      }
	    		    });

	    		    modalInstance.result.then(function (selectedItem) {
	    		      selected = selectedItem;
	    		    }, function () {
	    		      // Dismissal is not an error; the empty handler keeps AngularJS from
	    		      // reporting the dismissed modal as an unhandled rejection.
	    		    });
    		  };
    		  
    		  
    		  $scope.showGroupedServices2 =function(services,$event){
  				var modalInstance = $modal.open({
  	    		      
  	    		      templateUrl: 'pharmaDupsContent.html',
  	    		      controller: 'PharmaDupsInstanceCtrl',
  	    		      controllerAs: 'pdpa',
  	    		      parent: angular.element(document.body),
  	    		      size: 'lg',
  	    		      appendTo: $event,
  	    		      resolve: {
  	    		    	  	
  	    		    	  		services: function () {
  	    		          		return services;
  	    		        		},
  	    		        		getDetailView: function () {
  	    		          		return $scope.getDetailView;
  	    		        		} 
  	    		      }
  	    		    });

  	    		    modalInstance.result.then(function (selectedItem) {
  	    		      selected = selectedItem;
  	    		    }, function () {
  	    		      // Dismissal is not an error; the empty handler keeps AngularJS from
  	    		      // reporting the dismissed modal as an unhandled rejection.
  	    		    });
      		  };

			  $scope.logOverrideStatus = function(uuid, data, status) {
				  let reason = {};
				  if (status === 'Refused' || status === 'Cancelled') {
					  let reasonPrompt = prompt("Please provide a reason (optional):");
					  if (reasonPrompt != null) {
						  reason.name = "Reason";
						  reason.value = reasonPrompt;
						  data = reason;
					  }
				  }

				  // DHDR09.05: the message reflects the user decision, not the audit-log POST result.
				  var showOverrideResult = function() {
					  if (status === "Refused" || status === "Cancelled") {
						  $scope.overrideResultMessage = (status === "Refused")
								  ? "Access to Drug and Pharmacy Service Information has been refused."
								  : "Access to Drug and Pharmacy Service Information has been cancelled.";
						  $timeout(function() { window.close(); }, 4000);
					  }
				  };
				  dhdrService.logConsentOverride($scope.demographicNo, uuid, data, status)
						  .then(showOverrideResult, showOverrideResult);
			  }
			
    		  $scope.callConsentBlock = function($event){
				  	$scope.buttonDisabled = true;
    				dhdrService.getConsentOveride($scope.demographicNo, "PCOI").then(function(response){
						$scope.buttonDisabled = false;
    					if(response.status == 268){
    						alert("Error check the log for more details :\n"+response.data.summary);// response.data);
    						return;
    					}
    					
    					var med = response.data;

							if (!med || !med.referenceURL) {
								alert("Error retrieving Temporary Consent Override: Viewlet URL is null");
								return;
							}
    					
    					//window.open(med.referenceURL);  only for testing in chrome
    					
    					
    					var modalInstance = $modal.open({
    		    		      
    		    		      templateUrl: 'pcoi.html',
    		    		      controller: 'PcoiInstanceCtrl',
    		    		      controllerAs: 'mpcoi',
    		    		      parent: angular.element(document.body),
    		    		      size: 'lg',
    		    		      appendTo: $event,
    		    		      resolve: {
    		    		    	  	
    		    		    	  		med: function () {
    		    		          		return med;
    		    		        		}
    		    		      }
    		    		    });
    					//pcoi message back 
    					//message { target: Window, isTrusted: true, data: "{\"status\":\"completed\"}", origin: "https://pcoi-pst.apps.dev.ehealthontario.ca", lastEventId: "", source: Restricted https://pcoi-pst.apps.dev.ehealthontario.ca/main, ports: Restricted, srcElement: Window, currentTarget: Window, eventPhase: 2,  }
    					modalInstance.result.then(function (selectedItem) {
    						// DHDR11.01: process the PCOI viewlet response per the ONE Access Viewlet Framework
    						// Appendix A format - errors[] / successes[] / utility.code arrays (multi-LOB), not a
    						// flat status field. Each success/error entry names the microService (LOB) it is for.
    						var pcoiResult = {};
    						try {
    							pcoiResult = (selectedItem && typeof selectedItem.data === 'string')
    								? JSON.parse(selectedItem.data)
    								: ((selectedItem && selectedItem.data) || {});
    						} catch (err) {
    							pcoiResult = {};
    						}
    						var successes = angular.isArray(pcoiResult.successes) ? pcoiResult.successes : [];
    						var errors = angular.isArray(pcoiResult.errors) ? pcoiResult.errors : [];
    						var utilityCodes = (pcoiResult.utility && angular.isArray(pcoiResult.utility.code))
    							? pcoiResult.utility.code : [];

    						// DHDR override confirmed = a successes[] entry for the DHDR LOB (PCOI_CONSENT_SUCCESS_02
    						// / 201). PCOI_CONSENT_SUCCESS_01 alone only confirms the PCOI call, not the DHDR override.
    						var dhdrOverridden = successes.some(function (s) {
    							return s && (s.microService === 'DHDR'
    								|| (angular.isArray(s.code) && s.code.indexOf('PCOI_CONSENT_SUCCESS_02') !== -1));
    						});
    						var cancelled = utilityCodes.indexOf('PCOI_CONSENT_CANCELLED') !== -1;
    						var failed = !dhdrOverridden && errors.length > 0;

    						// DHDR11.01.a/b, DHDR15.02: audit the actual outcome, not a blanket successful override.
    						var auditStatus = dhdrOverridden ? 'Overwrite' : (failed ? 'Failed' : (cancelled ? 'Cancelled' : 'Overwrite'));
    						dhdrService.logConsentOverride($scope.demographicNo, med.uuid, selectedItem.data, auditStatus);

    						if (failed) {
    							// DHDR11.01.b: the override did not complete - leave the existing block shown and inform the user.
    							$scope.overrideResultMessage = "The temporary consent unblock did not complete. "
    								+ "Access to Drug and Pharmacy Service Information remains blocked.";
    							return;
    						}
    						if (cancelled) {
    							// User cancelled inside the PCOI viewlet (OAVF utility PCOI_CONSENT_CANCELLED); the existing block remains shown.
    							return;
    						}
    						// DHDR11.02 + OAVF B.4.2.5 / B.5: on success (or an ambiguous / no-code response) re-load the
    						// DHDR query - it is the source of truth, returning data with the CONSENT_TEMP_UNBLOCK notice.
    						$scope.callSearch();
    		    		    }, function () {
    		    		      // No resolving message (backdrop / esc close): per OAVF B.4.2.5, re-load in case the override succeeded.
    		    		      $scope.callSearch();
    		    		    });
    					
    				},function(reason){
						$scope.buttonDisabled = false;
    					alert(reason);
    				});
    				
    		  }
	
			
			
			
		});
		
		function OperationOutcome(operationOutcome){

			this.outcomme = operationOutcome;
			this.issues = [];
			var resource = angular.isDefined(this.outcomme.resource) ? this.outcomme.resource : this.outcomme;
			if(angular.isDefined(resource.issue)){
				this.issues = resource.issue;
			}
			// DHDR15.02: correlation id for a consent override logged from this notice.
			this.id = angular.isDefined(resource.id) ? resource.id : null;
			// DHDR14.01 incident time: the service's own timestamp when it supplied one.
			this.receivedAt = (resource.meta && resource.meta.lastUpdated)
				? new Date(resource.meta.lastUpdated) : new Date();
				
			
			
		}

		
		function MedicationDispense(medication){
			this.med = medication;
			this.hide = false;
			
			/* uniq value
			 a) Generic name of the dispensed drug [Medication.code.coding[2].display]
			 b) Dispensed drug strength [Medication.extension[1].valueString]
			 c) Drug dosage form (e.g., tablet, capsule, injection) [Medication.form.coding.display]
			*/
			this.getUniqVal = function(){
				 return this.genericName+":"+this.dispensedDrugStrength+":"+this.drugDosageForm;
			 }
			
			this.uniqVal = this.genericName+":"+this.dispensedDrugStrength+":"+this.drugDosageForm;
			 
			 /*
<pre>

Prescriber Information
g) Prescriber ID (e.g., practitioner license or CPSO number) [Practitioner.identifier.value]
h) ID Reference [Practitioner.identifier.system]
Pharmacy Information
i) Pharmacist Name [Practitioner.name.given] [Practitioner.name.family]
j) Pharmacy Phone Number [Organization.telecom[1].value]
</pre>
			 
			 */
			
			if(angular.isDefined(this.med.resource.identifier)){
				for (ident of  this.med.resource.identifier) {
					if(angular.isDefined(ident.value)){
						this.rxNumber = ident.value;			
					}
				}				
			} 
			 
			 /*
			 "extension": [
          {
            "url": "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-ext-refills-remaining",
            "valueInteger": 1
          },
          {
            "url": "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-ext-quantity-remaining",
            "valueQuantity": {
              "value": 120,
              "unit": "tsp",
              "system": "http://snomed.info/sct",
              "code": "SOL"
            }
          }
        ],
        */
			
			if(angular.isDefined(this.med.resource.extension)){
				
				for (ext of  this.med.resource.extension) {
					if(angular.isDefined(ext.url) && ext.url === "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-ext-refills-remaining"){
						
						this.refillsRemaining = ext.valueInteger;
					}else if(angular.isDefined(ext.url) && ext.url === "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-ext-quantity-remaining"){
						this.quantityRemaining = ext.valueQuantity.value+" "+ext.valueQuantity.unit;
					}
				}
			}
        
			this.whenPrepared = this.med.resource.whenPrepared;
			if (angular.isDefined(this.med.resource.whenHandedOver)) {
				// Drug tables bind pickUpDate, pharmacy tables bind whenHandedOver; same source.
				this.pickUpDate = this.med.resource.whenHandedOver;
				this.whenHandedOver = this.med.resource.whenHandedOver;
			}
			if(angular.isDefined(this.med.resource.quantity) && angular.isDefined(this.med.resource.quantity.value)){
				this.dispensedQuantity = this.med.resource.quantity.value;
				this.dispensedQuantityUnit = this.med.resource.quantity.unit;
			}
			if(angular.isDefined(this.med.resource.daysSupply) && angular.isDefined(this.med.resource.daysSupply.value)){
				this.estimatedDaysSupply = this.med.resource.daysSupply.value;
			}
			if(angular.isDefined(this.med.resource.reasonCode)){
				this.reasonCode = this.med.resource.reasonCode;
			}
			if(angular.isDefined(this.med.resource.category) && angular.isDefined(this.med.resource.category.coding)){
				for(coding of this.med.resource.category.coding) {

					// DHDR-02 (B2 #1): route on the OH dispense-category code. The normative code set
					// (device/drug/product/service) is stable, but the v4.0.3 IG emits this category
					// under three different eHealth Ontario system URLs across its own search examples:
					//   .../NamingSystem/ca-on-medication-dispense-category   (legacy)
					//   .../CodeSystem/medication-dispense-category           (transitional)
					//   .../CodeSystem/dispense-category                      (canonical)
					// Accept the code from any of them (tolerant to OH's URL churn and to future code
					// additions), but ignore codings from other categorization systems - e.g. the HL7
					// base medicationdispense-category, whose tokens (inpatient/outpatient/...) differ -
					// so a foreign system's code is never read as ours and mis-routes an event.
					if(angular.isDefined(coding.system)
							&& coding.system.startsWith("http://ehealthontario.ca/fhir/")
							&& coding.system.endsWith("dispense-category")) {
						this.categoryCode = coding.code;
						this.categoryDisplay = coding.display
					}
				}

			}

			if (angular.isDefined(this.med.resource.dosageInstruction)) {
				let dosageInstruction = this.med.resource.dosageInstruction[0];
				if (angular.isDefined(dosageInstruction.doseAndRate)) {
					this.dose = angular.isDefined(dosageInstruction.doseAndRate[0]) ? dosageInstruction.doseAndRate[0].doseQuantity.value : "";
					// Guard first, then prepend the space inside the value: + binds tighter than ?:.
					this.doseUnit = angular.isDefined(dosageInstruction.doseAndRate[0]) ? (" " + dosageInstruction.doseAndRate[0].doseQuantity.unit) : "";
				}
				if (angular.isDefined(dosageInstruction.timing)) {
					this.frequency = dosageInstruction.timing.repeat.frequency;
					this.period = dosageInstruction.timing.repeat.period;
					this.periodMax = dosageInstruction.timing.repeat.periodMax;
					this.periodUnit = dosageInstruction.timing.repeat.periodUnit;
				}
			}

			
			// contained is optional: a dispense can carry no Medication/Practitioner/Patient at all.
			var contained = angular.isDefined(this.med.resource.contained) ? this.med.resource.contained : [];
			for (res of contained) {
				
				if(res.resourceType === "Medication") {
					
					if(res.code != null) {
						
						if(angular.isDefined(res.form) && angular.isDefined(res.form.text)){
							this.drugDosageForm = res.form.text	
						}
						
						if(angular.isDefined(res.extension)){
							for(ext of res.extension) {
								if("http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-ext-medication-strength" === ext.url){
									this.dispensedDrugStrength = ext.valueString;
								}
							}
						}
				
						
						if (angular.isDefined(res.code.coding)) {
							for(coding of res.code.coding) {
								if(!angular.isDefined(coding.system)) {
									continue;
								}
								// Match on the trailing NamingSystem id, not the full URL: the consumer profile
								// fixes these to https:// while the IG examples use http://.
								if(coding.system.endsWith("ca-drug-gen-name")) {
									this.genericName = coding.display;
								}else if(coding.system.endsWith("ca-on-drug-class-ahfs")) {
									this.ahfsClass = coding.display;
								}else if(coding.system.endsWith("ca-on-drug-subclass-ahfs")) {
									this.ahfsSubClass = coding.display;
								}else if(coding.system.endsWith("ca-hc-din")) {
									// Health Canada DIN - the preferred brand identifier.
									this.brandName = coding;
								}else if(angular.isDefined(coding.code) && !angular.isDefined(this.brandName)) {
									// A non-DIN identifier (PIN, or a CCDD product code such as a COVaxON
									// immunization). Only when no DIN was captured, so a DIN always wins.
									this.brandName = coding;
								}

							}
						}
					}else {
						// Never interpolate the resource here - it carries PHI.
						console.warn("DHDR: contained Medication resource has no code element");
					}
				
				}else if(res.resourceType ===  "Organization") {
					this.dispensingPharmacy = res.name;
					if(angular.isDefined(res.telecom)){
						for(tele of res.telecom){
							if("fax" === tele.system){
								this.dispensingPharmacyFaxNumber = tele.value;		
							}
							if("phone" === tele.system){
								this.dispensingPharmacyPhoneNumber = tele.value;		
							}
	
						}
					}
				}else if(res.resourceType ===  "MedicationRequest") {
					this.reasonCode = [];
					if(angular.isDefined(res.reasonCode)){
						for(code of res.reasonCode){	
							// Push, so reasonCode stays an array - the print reads it with getJSONArray.
							this.reasonCode.push(code);
						}
					}	
					
				}else if(res.resourceType ==="Practitioner") {
					
					if (angular.isDefined(res.identifier)) {
						for(identifier of res.identifier) {
							if(!angular.isDefined(identifier.system)) {
								continue;
							}
							if(identifier.system.endsWith("ca-on-license-pharmacist")) {
								this.pharmacistLicenceNumber = identifier;
								if(angular.isDefined(res.name)){
									for( humanName of res.name) {
										this.pharmacistLastname = humanName.family;
										if(angular.isDefined(humanName.given)){
											this.pharmacistFirstname = humanName.given[0];
										}
									}
								}
							}else if(identifier.system.indexOf("ca-on-license") !== -1
									|| identifier.system.indexOf("prescriber") !== -1) {
								// Any prescriber licensing college, not physicians only. Pharmacist handled above.
								this.prescriberLicenceNumber = identifier;
								if(angular.isDefined(res.name)){
									for( humanName of res.name) {
										this.prescriberLastname = humanName.family;
										if(angular.isDefined(humanName.given)){
											this.prescriberFirstname = humanName.given[0];
										}
									}
								}

								if (angular.isDefined(res.telecom)) {
									for(tele of res.telecom){
										if("phone" === tele.system){
											this.prescriberPhoneNumber = tele.value;
										}
									}
								}
							}

						}
					}
				} else if (res.resourceType === "Patient") {
					this.patient = res;
				} else {
				}
				
			}
			
		}
			
		app.controller('ModalInstanceCtrl', function ModalInstanceCtrl($scope, $modal, $modalInstance, med, demoNo,
																	   dhdrPatient, $http){
			$scope.med = med;
			$scope.dhdrPatient = dhdrPatient;
			
			$scope.cancel = function(){
				
				$modalInstance.close(false);	
			}
			
			$scope.getLicence = function(val){
				if(val == null){
					return "N/A";
				}
				
				if(val.endsWith("ca-on-license-physician")){
					return "College of Physicians and Surgeons of Ontario";
				}else if(val.endsWith("ca-on-license-dental-surgeon")){
					return "Royal College of Dental Surgeons of Ontario";
				}else if(val.endsWith("ca-out-of-province -prescriber")){
					return "Out-of-Province Prescriber";
				}else if(val.endsWith("ca-on-license-chiropodist")){
					return "College of Chiropodists of Ontario";
				}else if(val.endsWith("ca-on-license-midwife")){
					return "College of Midwives of Ontario";
				}else if(val.endsWith("ca-on-license-pharmacist")){
					return "Ontario College of Pharmacists";
				}else if(val.endsWith("ca-on-license-optometrist")){
					return "College of Optometrists of Ontario";
				}else if(val.endsWith("ca-on-license-nurse")){
					return "College of Nurses of Ontario";
				}else if(val.endsWith("ca-on-license-naturopath")){
					return "College of Naturopaths of Ontario";
				}else if(val.endsWith("ca-on-unknown-prescriber")){
					return "Unknown Prescriber";
				}
				return "N/A";
			}
			
			// Licensing-body mnemonic for the GUI, with the full name on hover. Only well-established
			// abbreviations; anything else falls back to the full body name.
			$scope.getLicenceMnemonic = function(val){
				if(val == null){ return "N/A"; }
				if(val.endsWith("ca-on-license-physician")){ return "CPSO"; }
				if(val.endsWith("ca-on-license-dental-surgeon")){ return "RCDSO"; }
				if(val.endsWith("ca-on-license-pharmacist")){ return "OCP"; }
				if(val.endsWith("ca-on-license-nurse")){ return "CNO"; }
				if(val.endsWith("ca-on-license-midwife")){ return "CMO"; }
				if(val.endsWith("ca-on-license-optometrist")){ return "COO"; }
				if(val.endsWith("ca-on-license-naturopath")){ return "CONO"; }
				// No widely-used abbreviation - show the full body name instead.
				return $scope.getLicence(val);
			}

			$scope.printDetail = function(){
					var toPrint = {};
					toPrint.med = $scope.med;
					// DHDR13.01.b: the DHDR-side patient demographic printed on each page
					toPrint.dhdrPatient = $scope.dhdrPatient;

					$http.post('../ws/rs/dhdr/'+demoNo+'/print/detail',toPrint,{ responseType: 'arraybuffer' }).then(function (response) {
						
					       var file = new Blob([response.data], {type: 'application/pdf'});
					       var fileURL = URL.createObjectURL(file);
					       window.open(fileURL);
					}, function(errorMessage) {
						alert("Error getting printout");
						//rxComp.error = errorMessage;
					});	
					//window.open('../ws/rs/dhdr/'+$scope.demographicNo+'/print/summary','_blank');
			
			}
			
		});
		
		app.controller('DrugDupsInstanceCtrl', function ModalInstanceCtrl($scope, $modal, $modalInstance,meds,getDetailView,$http){
			$scope.meds = meds;
			$scope.getDetailView = getDetailView;
			
			$scope.cancel = function(){
				
				$modalInstance.close(false);	
			}
			
			
			
		});
		
		app.controller('PharmaDupsInstanceCtrl', function ModalInstanceCtrl($scope, $modal, $modalInstance,services,getDetailView,$http){
			$scope.services = services;
			$scope.getDetailView = getDetailView;
			
			$scope.cancel = function(){
				
				$modalInstance.close(false);	
			}
			
			
			
		});
		
		
		
		
		app.controller('PcoiInstanceCtrl', function ModalInstanceCtrl($scope, $modal, $modalInstance,med,$sce,$window,$http,$timeout){
			// Gets URL without parameters
			const PCOI_ORIGIN_URL = med.referenceURL.split('?')[0];

			$window.addEventListener('message', function(e) {
				if (e.origin === PCOI_ORIGIN_URL) {
					$modalInstance.close(e);
				}
			});
			
			$scope.showUntilLoaded = true;
			$scope.viewletNotResponding = false;
			

			$timeout(function() {
				$scope.viewletNotResponding = true;
			}, <%= viewletTimeout %>); <%-- DHDR-04: PCOI viewlet not-responding timeout, oneid_viewlet_timeout pref (default 300000) --%>
			
			
			
			$scope.med = med;
			$scope.pcoiUrl = $sce.trustAsResourceUrl(med.referenceURL);
		
			$scope.reload = function(){
				
				$scope.pcoiUrl = $sce.trustAsResourceUrl(med.referenceURL);
			}
		
			$scope.loadingResult = function(e){
				$scope.showUntilLoaded = false;
				$scope.$apply();
			}
			
			$scope.cancel = function(){
				
				$modalInstance.close(false);	
			}
		});
		
		app.directive("ngOnload", function elementOnloadDirective() {
	        return {
	            restrict: "A",
	            scope: {
	                callback: "&ngOnload"
	            },
	            link: function link(scope, element, attrs) {
	                // hooking up the onload event - calling the callback on load event
	                element.one("load", function (state,message) {
	                	
	                    var contentLocation = element.length > 0 && element[0].contentWindow ? element[0].contentWindow.location : undefined;
	                    scope.callback({
	                        contentLocation: contentLocation
	                    });
	                });
	            }
	        };
	    });
	
		
	</script>
	</body>
</html>	    			