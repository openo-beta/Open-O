//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package org.oscarehr.PMmodule.web;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.oscarehr.PMmodule.caisi_integrator.CaisiIntegratorManager;
import org.oscarehr.PMmodule.exception.AdmissionException;
import org.oscarehr.PMmodule.exception.ProgramFullException;
import org.oscarehr.PMmodule.exception.ServiceRestrictionException;
import org.oscarehr.PMmodule.model.*;
import org.oscarehr.PMmodule.service.*;
import org.oscarehr.PMmodule.web.formbean.GenericIntakeConstants;
import org.oscarehr.caisi_integrator.ws.*;
import org.oscarehr.casemgmt.dao.ClientImageDAO;
import org.oscarehr.casemgmt.model.ClientImage;
import org.oscarehr.casemgmt.service.CaseManagementManager;
import org.oscarehr.common.dao.OscarAppointmentDao;
import org.oscarehr.common.model.Admission;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.Demographic.PatientStatus;
import org.oscarehr.common.model.JointAdmission;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SessionConstants;
import org.oscarehr.util.SpringUtils;
import oscar.OscarProperties;
import oscar.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;

public class GenericIntakeEdit2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();


    private static Logger LOG = MiscUtils.getLogger();
    // Forwards
    private static final String EDIT = "edit";
    private static final String PRINT = "print";
    private static final String CLIENT_EDIT = "clientEdit";
    private static final String EFORM_ADD = "eformAdd";
    private static final String APPT = "appointment";
    protected static final String CLIENT_EDIT_ID = "id";

    private ClientImageDAO clientImageDAO = SpringUtils.getBean(ClientImageDAO.class);
    private SurveyManager surveyManager = (SurveyManager) SpringUtils.getBean("surveyManager2");
    //private IMatchManager matchManager = new MatchManager();

    protected static final String PROGRAM_ID = "programId";
    protected static final String TYPE = "type";
    protected static final String CLIENT = "client";
    protected static final String APPOINTMENT = "appointment";
    protected static final String DEMOGRAPHIC_NO = "demographic_no";
    protected static final String FORM_ID = "fid";
    protected static final String CLIENT_ID = "clientId";
    protected static final String INTAKE_ID = "intakeId";


    private GenericIntakeManager genericIntakeManager = SpringUtils.getBean(GenericIntakeManager.class);
    protected ClientManager clientManager = SpringUtils.getBean(ClientManager.class);
    protected ProgramManager programManager = SpringUtils.getBean(ProgramManager.class);
    protected AdmissionManager admissionManager = SpringUtils.getBean(AdmissionManager.class);
    protected CaseManagementManager caseManagementManager = SpringUtils.getBean(CaseManagementManager.class);


    public void setGenericIntakeManager(GenericIntakeManager genericIntakeManager) {
        this.genericIntakeManager = genericIntakeManager;
    }

    protected Integer getProgramId(HttpServletRequest request) {
        Integer programId = null;

        String programIdParam = request.getParameter(PROGRAM_ID);

        if (programIdParam != null) {
            try {
                programId = Integer.valueOf(programIdParam);
            } catch (NumberFormatException e) {
                LOG.error("Error", e);
            }
        }

        return programId;
    }

    public String execute() {
        String method = request.getParameter("method");
        if ("create".equals(method)) {
            return create();
        } else if ("blank".equals(method)) {
            return blank();
        } else if ("update".equals(method)) {
            return update();
        } else if ("print".equals(method)) {
            return print();
        } else if ("save".equals(method)) {
            return save();
        } else if ("save_draft".equals(method)) {
            return save_draft();
        } else if ("save_temp".equals(method)) {
            return save_temp();
        } else if ("save_admit".equals(method)) {
            return save_admit();
        } else if ("save_notAdmit".equals(method)) {
            return save_notAdmit();
        } else if ("clientEdit".equals(method)) {
            return clientEdit();
        } else if ("save_proxy".equals(method)) {
            return save_proxy();
        } 
        return SUCCESS;
    }

    public String create() {

        // [ 1842774 ] RFQ Feature: link reg intake gender to list editor table;
        List genders = GenericIntakeSearch2Action.getGenders();
        this.setGenders(genders);
        // end of change

        String intakeType = request.getParameter(TYPE);
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();

        Intake intake = null;
        MiscUtils.getLogger().debug("INTAKE TYPE " + intakeType);
        if (Intake.QUICK.equalsIgnoreCase(intakeType)) {
            intake = genericIntakeManager.createQuickIntake(providerNo);
        } else if (Intake.INDEPTH.equalsIgnoreCase(intakeType)) {
            intake = genericIntakeManager.createIndepthIntake(providerNo);
        } else if (Intake.PROGRAM.equalsIgnoreCase(intakeType)) {
            intake = genericIntakeManager.createProgramIntake(getProgramId(request), providerNo);
        }

        MiscUtils.getLogger().debug("INTAKE IS NULL " + String.valueOf(intake == null));
        List<IntakeNodeJavascript> jsLocation = genericIntakeManager.getIntakeNodeJavascriptLocation(intake.getNode().getQuestionId());

        Integer defaultCommunityProgramId = null;

        if (org.oscarehr.common.IsPropertiesOn.propertiesOn("oscarClinic")) {
            defaultCommunityProgramId = getOscarClinicDefaultCommunityProgramId(oscar.OscarProperties.getInstance().getProperty("oscarClinicDefaultProgram"));
        }

        setBeanProperties(loggedInInfo, intake, getClient(request), providerNo, 
                Agency.getLocalAgency().areServiceProgramsVisible(intakeType), 
                Agency.getLocalAgency().areExternalProgramsVisible(intakeType), 
                null, null, loggedInInfo.getCurrentFacility().getId(), null, jsLocation, 
                Agency.getLocalAgency().areCommunityProgramsVisible(intakeType), defaultCommunityProgramId);

        request.getSession().setAttribute(SessionConstants.INTAKE_CLIENT_IS_DEPENDENT_OF_FAMILY, false);

        request.getSession().setAttribute("intakeCurrentBedCommunityId", null);
        request.getSession().setAttribute("intakeCurrentCommunityId", null);

        //set up appointment's attributes here: request.getSession().getAttribute("appointment_date");

        ProgramUtils.addProgramRestrictions(request);


        StringBuilder path = new StringBuilder("/PMmodule/GenericIntake/Edit.jsp");
        String fromAppt = request.getParameter("fromAppt");

        if (fromAppt != null && "1".equals(fromAppt)) {

            //  String originalPage2 = request.getParameter("originalPage");
            String provider_no2 = request.getParameter("provider_no");
            String bFirstDisp2 = request.getParameter("bFirstDisp");
            String year2 = request.getParameter("year");
            String month2 = request.getParameter("month");
            String day2 = request.getParameter("day");
            String start_time2 = request.getParameter("start_time");
            String end_time2 = request.getParameter("end_time");
            String duration2 = request.getParameter("duration");
            String bufName = "";
            String demographicNo = "";
            String bufDoctorNo = providerNo;
            String bufChart = "";
            String addAppt = "?user_id=" + providerNo + "&provider_no=" + provider_no2 + "&bFirstDisp=" + bFirstDisp2 + "&appointment_date=" + request.getParameter("appointment_date") + "&year=" + year2 + "&month=" + month2 + "&day=" + day2 + "&start_time=" + start_time2 + "&end_time=" + end_time2 + "&duration=" + duration2 + "&name=" + URLEncoder.encode(bufName.toString()) + "&chart_no=" + URLEncoder.encode(bufChart.toString()) + "&bFirstDisp=false&demographic_no=" + demographicNo + "&messageID=" + request.getParameter("messageId") + "&doctor_no=" + bufDoctorNo.toString() + "&notes=" + request.getParameter("notes") + "&reason=" + request.getParameter("reason") + "&location=" + request.getParameter("location") + "&resources=" + request.getParameter("resources") + "&apptType=" + request.getParameter("apptType") + "&style=" + request.getParameter("style") + "&billing=" + request.getParameter("billing") + "&status=" + request.getParameter("status") + "&createdatetime=" + request.getParameter("createdatetime") + "&creator=" + request.getParameter("creator") + "&remarks=" + request.getParameter("remarks");

            path.append(addAppt);
        }

        try {
            response.sendRedirect(path.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
        //return EDIT;
    }

    public String blank() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        Integer facilityId = loggedInInfo.getCurrentFacility().getId();

        // [ 1842774 ] RFQ Feature: link reg intake gender to list editor table;
        List genders = GenericIntakeSearch2Action.getGenders();
        this.setGenders(genders);
        // end of change

        String intakeType = request.getParameter(TYPE);

        Integer nodeId = null;
        try {
            String strNodeId = request.getParameter("nodeId");
            if (strNodeId != null)
                nodeId = Integer.valueOf(strNodeId);
        } catch (NumberFormatException e) {
            MiscUtils.getLogger().warn("error", e);
        }


        Intake intake = null;

        if (Intake.QUICK.equalsIgnoreCase(intakeType)) {
            if (intake == null) {
                intake = genericIntakeManager.createQuickIntake(providerNo);
                //intake.setClientId(clientId);
                intake.setFacilityId(facilityId);
            }
        } else if (Intake.INDEPTH.equalsIgnoreCase(intakeType)) {
            if (intake == null) {
                intake = genericIntakeManager.createIndepthIntake(providerNo);
                //intake.setClientId(clientId);
                intake.setFacilityId(facilityId);
            }
        } else if (Intake.PROGRAM.equalsIgnoreCase(intakeType)) {
            if (intake == null) {
                intake = genericIntakeManager.createProgramIntake(getProgramId(request), providerNo);
                //intake.setClientId(clientId);
                intake.setFacilityId(facilityId);
            }
        } else {
            IntakeNode in = genericIntakeManager.getIntakeNode(nodeId);
            if (intake == null) {
                intake = genericIntakeManager.createIntake(in, providerNo);
                //intake.setClientId(clientId);
                intake.setFacilityId(facilityId);
            }
        }

        List<IntakeNodeJavascript> jsLocation = genericIntakeManager.getIntakeNodeJavascriptLocation(intake.getNode().getQuestionId());

        Demographic client = new Demographic();


        setBeanProperties(loggedInInfo, intake, client, providerNo, 
                Agency.getLocalAgency().areServiceProgramsVisible(intakeType), 
                Agency.getLocalAgency().areExternalProgramsVisible(intakeType), 
                null, null, facilityId, nodeId, jsLocation, 
                Agency.getLocalAgency().areCommunityProgramsVisible(intakeType), null);

        // UCF -- intake accessment : please don't remove the following lines
        List allForms = surveyManager.getAllFormsForCurrentProviderAndCurrentFacility(loggedInInfo);
        request.getSession().setAttribute("survey_list", allForms);

        request.getSession().setAttribute("intakeCurrentBedId", null);
        request.getSession().setAttribute("intakeCurrentCommunityId", null);

        request.getSession().setAttribute(SessionConstants.INTAKE_CLIENT_IS_DEPENDENT_OF_FAMILY, false);


        ProgramUtils.addProgramRestrictions(request);

        return EDIT;

    }

    public String update() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        Integer facilityId = loggedInInfo.getCurrentFacility().getId();

        // [ 1842774 ] RFQ Feature: link reg intake gender to list editor table;
        List genders = GenericIntakeSearch2Action.getGenders();
        this.setGenders(genders);
        // end of change

        String intakeType = request.getParameter(TYPE);
        Integer clientId = getClientIdAsInteger(request);

        Integer nodeId = null;
        try {
            String strNodeId = request.getParameter("nodeId");
            if (strNodeId != null)
                nodeId = Integer.valueOf(strNodeId);
        } catch (NumberFormatException e) {
            MiscUtils.getLogger().warn("error", e);
        }


        Intake intake = null;

        if (Intake.QUICK.equalsIgnoreCase(intakeType)) {
            intake = genericIntakeManager.copyRegIntake(clientId, providerNo, facilityId);
            if (intake == null) {
                intake = genericIntakeManager.createQuickIntake(providerNo);
                intake.setClientId(clientId);
                intake.setFacilityId(facilityId);
            }
        } else if (Intake.INDEPTH.equalsIgnoreCase(intakeType)) {
            intake = genericIntakeManager.copyIndepthIntake(clientId, providerNo, facilityId);
            if (intake == null) {
                intake = genericIntakeManager.createIndepthIntake(providerNo);
                intake.setClientId(clientId);
                intake.setFacilityId(facilityId);
            }
        } else if (Intake.PROGRAM.equalsIgnoreCase(intakeType)) {
            intake = genericIntakeManager.copyProgramIntake(clientId, getProgramId(request), providerNo, facilityId);
            if (intake == null) {
                intake = genericIntakeManager.createProgramIntake(getProgramId(request), providerNo);
                intake.setClientId(clientId);
                intake.setFacilityId(facilityId);
            }
        } else {
            IntakeNode in = genericIntakeManager.getIntakeNode(nodeId);
            intake = genericIntakeManager.copyIntakeWithId(in, clientId, null, providerNo, facilityId);
            if (intake == null) {
                intake = genericIntakeManager.createIntake(in, providerNo);
                intake.setClientId(clientId);
                intake.setFacilityId(facilityId);
            }
        }

        List<IntakeNodeJavascript> jsLocation = genericIntakeManager.getIntakeNodeJavascriptLocation(intake.getNode().getQuestionId());


        setBeanProperties(loggedInInfo, intake, getClient(clientId), providerNo, Agency.getLocalAgency()
                        .areServiceProgramsVisible(intakeType), Agency.getLocalAgency().areExternalProgramsVisible(intakeType),
                getCurrentServiceProgramIds(clientId), getCurrentExternalProgramId(clientId), facilityId, nodeId, jsLocation, Agency.getLocalAgency().areCommunityProgramsVisible(intakeType), getCurrentCommunityProgramId(clientId));

        // UCF -- intake accessment : please don't remove the following lines
        List allForms = surveyManager.getAllFormsForCurrentProviderAndCurrentFacility(loggedInInfo);
        request.getSession().setAttribute("survey_list", allForms);


        String oldCommunityProgramId = String.valueOf(getCurrentCommunityProgramId(clientId));
        request.getSession().setAttribute("intakeCurrentCommunityId", oldCommunityProgramId);

        if (clientManager.isClientFamilyHead(clientId)) {
            request.getSession().setAttribute(SessionConstants.INTAKE_CLIENT_IS_DEPENDENT_OF_FAMILY, false);
        } else {
            if (clientManager.isClientDependentOfFamily(clientId))
                request.getSession().setAttribute(SessionConstants.INTAKE_CLIENT_IS_DEPENDENT_OF_FAMILY, true);
            else request.getSession().setAttribute(SessionConstants.INTAKE_CLIENT_IS_DEPENDENT_OF_FAMILY, false);
        }

        ProgramUtils.addProgramRestrictions(request);

        return EDIT;
    }

    public String print() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        Integer facilityId = loggedInInfo.getCurrentFacility().getId();

        String intakeType = request.getParameter(TYPE);
        Integer clientId = getClientIdAsInteger(request);
        Integer intakeId = getIntakeId(request);

        Intake intake = null;

        if (Intake.QUICK.equalsIgnoreCase(intakeType)) {
            if (intakeId > -1) {
                intake = genericIntakeManager.getRegIntakeById(intakeId, facilityId);
            } else {
                intake = genericIntakeManager.getMostRecentQuickIntake(clientId, facilityId);
            }
        } else if (Intake.INDEPTH.equalsIgnoreCase(intakeType)) {
            intake = genericIntakeManager.getMostRecentIndepthIntake(clientId, facilityId);
        } else if (Intake.PROGRAM.equalsIgnoreCase(intakeType)) {
            intake = genericIntakeManager.getMostRecentProgramIntake(clientId, getProgramId(request), facilityId);
        }

        List<IntakeNodeJavascript> jsLocation = genericIntakeManager.getIntakeNodeJavascriptLocation(intake.getNode().getQuestionId());


        setBeanProperties(loggedInInfo, intake, getClient(clientId), providerNo, false, false, null, null, facilityId, null, jsLocation, false, null);

        return PRINT;
    }

    public String save_all(String saveWhich) {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        Intake intake = this.getIntake();
        String intakeType = intake.getType();
        Demographic client = this.getClient();
        Integer nodeId = this.getNodeId();
        Integer oldId = null;
        String formattedAdmissionDate = request.getParameter("admissionDate");
        Date admissionDate;
        if (StringUtils.isBlank(formattedAdmissionDate)) {
            admissionDate = new Date();
        } else {
            admissionDate = oscar.util.DateUtils.toDate(formattedAdmissionDate);
        }

        /* for repeating elements */
        String[] repeatSizes = request.getParameterValues("repeat_size");
        if (repeatSizes != null) {
            for (String rs : repeatSizes) {
                String[] vals = rs.split("-");
                String rNodeId = vals[0];
                String rSize = vals[1];
                intake.cleanRepeatingAnswers(Integer.parseInt(rNodeId), Integer.parseInt(rSize));
            }
        }
        try {
            try {
                GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(request.getParameter("eff_year")), Integer.parseInt(request.getParameter("eff_month")), Integer.parseInt(request.getParameter("eff_day")));
                client.setEffDate(cal.getTime());
            } catch (Exception e) {
                LOG.debug("date parse exception on eff date", e);
                // that's fine ignore it, probably an invalid date or no date set.
            }

            String anonymous = StringUtils.trimToNull(request.getParameter("anonymous"));
            client.setAnonymous(anonymous);

            //check to see if HIN already exists in the system
            if (client.getDemographicNo() == null) {
                String hin = client.getHin();
                String hcType = client.getHcType();
                if (hin.length() > 0 && clientManager.checkHealthCardExists(hin, hcType)) {
                    addActionMessage(getText("hin.duplicate"));
                    return EDIT;
                }
            }

            if (StringUtils.isBlank(client.getPatientStatus())) {
                client.setPatientStatus(PatientStatus.AC.name());
            }

            // save client information.
            saveClient(client, providerNo);


            // for RFQ:
            if (OscarProperties.getInstance().isTorontoRFQ()) {
                Integer clientId = client.getDemographicNo();
                if (clientId != null && !"".equals(clientId)) {
                    // Get current community program
                    Admission communityProgramAdmission = admissionManager.getCurrentCommunityProgramAdmission(client.getDemographicNo());
                    oldId = communityProgramAdmission != null ? communityProgramAdmission.getProgramId() : null;

                    // Save 'external' program for RFQ.
                    admitExternalProgram(client.getDemographicNo(), providerNo, this.getSelectedExternalProgramId());
                }
                // get and set intake location
                // client.setChildren(this.getProgramInDomainId());
                Integer intakeLocationId = 0;
                String intakeLocationStr = this.getProgramInDomainId();
                if (intakeLocationStr == null || "".equals(intakeLocationStr)) {
                    if ("RFQ_admit".equals(saveWhich)) {
                        if (this.getProgramInDomainId() != null && this.getProgramInDomainId().trim().length() > 0)
                            intakeLocationId = Integer.valueOf(this.getProgramInDomainId());
                    }
                } else {
                    intakeLocationId = Integer.valueOf(intakeLocationStr);
                }

                intake.setIntakeLocation(intakeLocationId);
            }

            intake.setFacilityId(loggedInInfo.getCurrentFacility().getId());

            String admissionText = null;
            String remoteReferralId = StringUtils.trimToNull(request.getParameter("remoteReferralId"));
            if (remoteReferralId != null) {
                admissionText = getAdmissionText(loggedInInfo, admissionText, remoteReferralId);
            }

            admitBedCommunityProgram(client.getDemographicNo(), providerNo, this.getSelectedCommunityProgramId(), saveWhich, admissionText, admissionDate);

            if (remoteReferralId != null) {
                // doing this after the admit is about as transactional as this is going to get for now.
                ReferralWs referralWs;
                try {
                    referralWs = CaisiIntegratorManager.getReferralWs(loggedInInfo, loggedInInfo.getCurrentFacility());
                    referralWs.removeReferral(Integer.parseInt(remoteReferralId));
                } catch (Exception e) {
                    LOG.error("Unexpected error", e);
                }
            }

            // if (!this.getSelectedServiceProgramIds().isEmpty() && "RFQ_admit".endsWith(saveWhich)) {
            //if (!this.getSelectedServiceProgramIds().isEmpty()) { //should be able to discharge from all service programs.
            admitServicePrograms(client.getDemographicNo(), providerNo, this.getSelectedServiceProgramIds(), null, admissionDate);
            //}

            if ("normal".equals(saveWhich) || "appointment".equals(saveWhich)) {
                // normal intake saving . eg. seaton house
                intake.setIntakeStatus("Signed");
                intake.setId(null);
                saveIntake(intake, client.getDemographicNo());
            } else if ("draft".equals(saveWhich)) {
                intake.setIntakeStatus("Draft");
                intake.setId(null);
                saveIntake(intake, client.getDemographicNo());
            } else {
                // RFQ intake saving...
                if ("RFQ_temp".equals(saveWhich)) {
                    intake.setIntakeStatus("Unsigned");
                    saveUpdateIntake(intake, client.getDemographicNo());
                } else if ("RFQ_admit".equals(saveWhich)) {
                    intake.setIntakeStatus("Signed");
                    intake.setId(null);
                    saveIntake(intake, client.getDemographicNo());
                } else if ("RFQ_notAdmit".equals(saveWhich)) {
                    intake.setIntakeStatus("Signed");
                    intake.setId(null);
                    saveIntake(intake, client.getDemographicNo());
                }
            }
        } catch (ProgramFullException e) {
            addActionMessage(getText("admit.full"));
        } catch (AdmissionException e) {
            MiscUtils.getLogger().error("Error", e);
            LOG.error("Error", e);
            addActionMessage(getText("message", e.getMessage()));
        } catch (ServiceRestrictionException e) {
            addActionMessage(getText("admit.service_restricted", e.getRestriction().getComments(), e.getRestriction().getProvider()
                    .getFormattedName()));
        }

        List<IntakeNodeJavascript> jsLocation = genericIntakeManager.getIntakeNodeJavascriptLocation(intake.getNode().getQuestionId());


        setBeanProperties(loggedInInfo, intake, client, providerNo, Agency.getLocalAgency().areServiceProgramsVisible(
                        intakeType), Agency.getLocalAgency().areExternalProgramsVisible(intakeType),
                getCurrentServiceProgramIds(client.getDemographicNo()), getCurrentExternalProgramId(client.getDemographicNo()), loggedInInfo.getCurrentFacility().getId(), nodeId, jsLocation, Agency.getLocalAgency().areCommunityProgramsVisible(intakeType), getCurrentCommunityProgramId(client.getDemographicNo()));


        String oldCommunityProgramId = String.valueOf(getCurrentCommunityProgramId(client.getDemographicNo()));
        request.getSession().setAttribute("intakeCurrentCommunityId", oldCommunityProgramId);

        String remoteFacilityIdStr = StringUtils.trimToNull(request.getParameter("remoteFacilityId"));
        String remoteDemographicIdStr = StringUtils.trimToNull(request.getParameter("remoteDemographicId"));
        if (remoteFacilityIdStr != null && remoteDemographicIdStr != null) {
            try {
                int remoteFacilityId = Integer.parseInt(remoteFacilityIdStr);
                int remoteDemographicId = Integer.parseInt(remoteDemographicIdStr);
                DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());

                // link the clients
                demographicWs.linkDemographics(providerNo, client.getDemographicNo(), remoteFacilityId, remoteDemographicId);

                // copy image if exists
                {
                    DemographicTransfer demographicTransfer = demographicWs.getDemographicByFacilityIdAndDemographicId(remoteFacilityId, remoteDemographicId);

                    if (demographicTransfer.getPhoto() != null) {
                        ClientImage clientImage = new ClientImage();
                        clientImage.setDemographic_no(client.getDemographicNo());
                        clientImage.setImage_data(demographicTransfer.getPhoto());
                        clientImage.setImage_type("jpg");
                        clientImageDAO.saveClientImage(clientImage);
                    }
                }
            } catch (MalformedURLException e) {
                LOG.error("Error", e);
            } catch (WebServiceException e) {
                LOG.error("Error", e);
            }
        }
		/*
				GenericIntakeEditFormBean formBean2 = new GenericIntakeEditFormBean();
				request.getSession().setAttribute("genericIntakeEditForm", formBean2);
		*/
        if (("RFQ_admit".equals(saveWhich) || "RFQ_notAdmit".equals(saveWhich)) && oldId != null) {
            return clientEdit();
        } else if (request.getAttribute("ERROR_KEY") != null) {
            return EDIT;
        } else if ("appointment".equals(saveWhich)) {
            StringBuilder path = new StringBuilder("/appointment/addappointment.jsp");
            //String fromAppt = request.getParameter("fromAppt");
            //String originalPage2 = request.getParameter("originalPage");
            String provider_no2 = request.getParameter("provider_no");
            String bFirstDisp2 = request.getParameter("bFirstDisp");
            String year2 = request.getParameter("year");
            String month2 = request.getParameter("month");
            String day2 = request.getParameter("day");
            String start_time2 = request.getParameter("start_time");
            String end_time2 = request.getParameter("end_time");
            String duration2 = request.getParameter("duration");
            String bufName = client.getDisplayName();
            Integer dem = client.getDemographicNo();
            String bufDoctorNo = client.getProviderNo();
            String bufChart = client.getChartNo();
            String addAppt = "?user_id=" + request.getParameter("creator") + "&provider_no=" + provider_no2 + "&bFirstDisp=" + bFirstDisp2 + "&appointment_date=" + request.getParameter("appointment_date") + "&year=" + year2 + "&month=" + month2 + "&day=" + day2 + "&start_time=" + start_time2 + "&end_time=" + end_time2 + "&duration=" + duration2 + "&name=" + URLEncoder.encode(bufName.toString()) + "&chart_no=" + URLEncoder.encode(bufChart.toString()) + "&bFirstDisp=false&demographic_no=" + dem.toString() + "&messageID=" + request.getParameter("messageId") + "&doctor_no=" + bufDoctorNo.toString() + "&notes=" + request.getParameter("notes") + "&reason=" + request.getParameter("reason") + "&location=" + request.getParameter("location") + "&resources=" + request.getParameter("resources") + "&type=" + request.getParameter("apptType") + "&style=" + request.getParameter("style") + "&billing=" + request.getParameter("billing") + "&status=" + request.getParameter("status") + "&createdatetime=" + request.getParameter("createdatetime") + "&creator=" + request.getParameter("creator") + "&remarks=" + request.getParameter("remarks");

            path.append(addAppt);
            try {
                response.sendRedirect(path.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        } else {
            return clientEdit();
        }
    }

    private String getAdmissionText(LoggedInInfo loggedInInfo, String admissionText, String remoteReferralId) {
        try {
            ReferralWs referralWs = CaisiIntegratorManager.getReferralWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            Referral remoteReferral = referralWs.getReferral(Integer.parseInt(remoteReferralId));
            StringBuilder sb = new StringBuilder();

            CachedFacility cachedFacility = CaisiIntegratorManager.getRemoteFacility(loggedInInfo, loggedInInfo.getCurrentFacility(), remoteReferral.getSourceIntegratorFacilityId());
            sb.append("Referred from : ");
            sb.append(cachedFacility.getName());

            FacilityIdStringCompositePk facilityProviderPrimaryKey = new FacilityIdStringCompositePk();
            facilityProviderPrimaryKey.setIntegratorFacilityId(remoteReferral.getSourceIntegratorFacilityId());
            facilityProviderPrimaryKey.setCaisiItemId(remoteReferral.getSourceCaisiProviderId());
            CachedProvider cachedProvider = CaisiIntegratorManager.getProvider(loggedInInfo, loggedInInfo.getCurrentFacility(), facilityProviderPrimaryKey);
            sb.append(" by ");
            sb.append(cachedProvider.getLastName());
            sb.append(", ");
            sb.append(cachedProvider.getFirstName());
            if (cachedProvider.getWorkPhone() != null) {
                sb.append("(");
                sb.append(cachedProvider.getWorkPhone());
                sb.append(")");
            }

            sb.append(". ");
            sb.append("Reason for Referral : ");
            sb.append(remoteReferral.getReasonForReferral());

            sb.append(". ");
            sb.append("Presenting Problem : ");
            sb.append(remoteReferral.getPresentingProblem());

            admissionText = sb.toString();
        } catch (Exception e) {
            LOG.error("Unexpected error.", e);
        }
        return admissionText;
    }

    public String save() {

        String fromAppt = request.getParameter("fromAppt");
		/*
        String originalPage2 = request.getParameter("originalPage");
        String provider_no2 = request.getParameter("provider_no");
        String bFirstDisp2 = request.getParameter("bFirstDisp");
        String year2 = request.getParameter("year");
        String month2 = request.getParameter("month");
        String day2 = request.getParameter("day");
        String start_time2 = request.getParameter("start_time");
        String end_time2 = request.getParameter("end_time");
        String duration2 = request.getParameter("duration");
        */
        if (fromAppt != null && fromAppt.equals("1")) {
            return save_all("appointment");
        } else {
            return save_all("normal");
        }
    }

    public String save_draft() {
        return save_all("draft");
    }

    public String save_temp() {

        return save_all("RFQ_temp");
    }

    public String save_admit() {
        return save_all("RFQ_admit");
    }

    public String save_notAdmit() {
        return save_all("RFQ_notAdmit");
    }

    public String clientEdit() {
        String forward = null;
        Integer clientEditId = this.getClient().getDemographicNo();

        StringBuilder parameters = new StringBuilder("?");

        Set<Integer> serviceProgramIds = this.getSelectedServiceProgramIds();
        if (!serviceProgramIds.isEmpty()) {
            Integer serviceProgramId = serviceProgramIds.iterator().next(); //assumption: there is only one item in this set at a time. Take the 1st one.
            if (serviceProgramId == Integer.parseInt(OscarProperties.getIntakeProgramAccessServiceId())) {
                parameters.append(FORM_ID).append("=").append(OscarProperties.getIntakeProgramAccessFId());
                forward = EFORM_ADD;
            } else if (serviceProgramId == Integer.parseInt(OscarProperties.getIntakeProgramCashServiceId())) {
                parameters.append(FORM_ID).append("=").append(OscarProperties.getIntakeProgramCashFId());
                forward = EFORM_ADD;
            }
        }
        if (EFORM_ADD.equals(forward)) {
            parameters.append("&");
            parameters.append(DEMOGRAPHIC_NO).append("=").append(clientEditId);
            parameters.append("&");
            parameters.append(APPOINTMENT).append("=").append(0); // appointment code is always 0
        } else {
            parameters.append(CLIENT_EDIT_ID).append("=").append(clientEditId);
            forward = CLIENT_EDIT;
        }

        return createRedirectForward(forward, parameters);
    }

    public String save_proxy() {
        try {
            save();
            int clientEditId = this.getClient().getDemographicNo();
            OutputStream os = response.getOutputStream();
            os.write(("" + clientEditId).getBytes());
        } catch (IOException e) {
            //ignore
            LOG.error(e.getMessage(), e);
        }

        return null;
    }

    // Adapt

    private Demographic getClient(Integer clientId) {
        return clientManager.getClientByDemographicNo(clientId.toString());
    }

    private Set<Program> getActiveProviderPrograms(String providerNo) {
        Set<Program> activeProviderPrograms = new HashSet<Program>();

        for (Program providerProgram : programManager.getProgramDomain(providerNo)) {
            if (providerProgram != null && providerProgram.isActive()) {
                activeProviderPrograms.add(providerProgram);
            }
        }

        return activeProviderPrograms;
    }


    public List<Program> getServicePrograms(Set<Program> providerPrograms, String providerNo) {
        List<Program> servicePrograms = new ArrayList<Program>();

        for (Object o : programManager.getServicePrograms()) {
            Program program = (Program) o;

            if (providerPrograms.contains(program)) {
                servicePrograms.add(program);
            }
        }

        return servicePrograms;
    }


    private List<Program> getExternalPrograms(Set<Program> providerPrograms) {
        List<Program> externalPrograms = new ArrayList<Program>();

        for (Program program : programManager.getExternalPrograms()) {
            externalPrograms.add(program);
        }
        return externalPrograms;
    }

    private List<Program> getProgramsInDomain(Set<Program> providerPrograms) {
        List<Program> programsInDomain = new ArrayList<Program>();

        for (Program program : providerPrograms) {
            programsInDomain.add(program);
        }
        return programsInDomain;
    }

    private Integer getOscarClinicDefaultCommunityProgramId(String communityProgram) {
        Integer communityProgramId = null;
        communityProgramId = programManager.getProgramIdByProgramName(communityProgram);
        return communityProgramId;
    }


    private Integer getCurrentCommunityProgramId(Integer clientId) {
        Integer currentProgramId = null;

        Admission communityProgramAdmission = admissionManager.getCurrentCommunityProgramAdmission(clientId);

        if (communityProgramAdmission != null) {
            currentProgramId = communityProgramAdmission.getProgramId();
        }

        return currentProgramId;
    }

    private Integer getCurrentExternalProgramId(Integer clientId) {
        Integer currentProgramId = null;

        Admission externalProgramAdmission = admissionManager.getCurrentExternalProgramAdmission(clientId);

        if (externalProgramAdmission != null) {
            currentProgramId = externalProgramAdmission.getProgramId();
        }

        return currentProgramId;
    }

    private SortedSet<Integer> getCurrentServiceProgramIds(Integer clientId) {
        SortedSet<Integer> currentProgramIds = new TreeSet<Integer>();

        List<?> admissions = admissionManager.getCurrentServiceProgramAdmission(clientId);
        if (admissions != null) {
            for (Object o : admissions) {
                Admission serviceProgramAdmission = (Admission) o;
                currentProgramIds.add(serviceProgramAdmission.getProgramId());
            }
        }

        return currentProgramIds;
    }

    private void saveClient(Demographic client, String providerNo) {

        String strSaveMrp = OscarProperties.getInstance().getProperty("caisi.registration_intake.updateMRPOnSave", "true");
        if ("true".equals(strSaveMrp)) {
            client.setProviderNo(providerNo);
        }

        clientManager.saveClient(client);
        //this is slowing things down, and AFAIK waitlist isn't being used anywhere
		/*
		try {
			log.info("Processing client creation event with MatchManager..." + 
					matchManager.<Demographic>processEvent(client, IMatchManager.Event.CLIENT_CREATED));
		} catch (Exception e) {
			MiscUtils.getLogger().error("Error while processing MatchManager.processEvent(Client)",e);
		}
		*/
    }

    private void admitExternalProgram(Integer clientId, String providerNo, Integer externalProgramId) throws ProgramFullException, AdmissionException, ServiceRestrictionException {
        Program externalProgram = null;
        Integer currentExternalProgramId = getCurrentExternalProgramId(clientId);

        if (externalProgramId != null) {
            externalProgram = programManager.getProgram(externalProgramId);
        }

        if (externalProgram != null) {
            if (currentExternalProgramId == null) {
                admissionManager.processAdmission(clientId, providerNo, externalProgram, "intake external discharge", "intake external admit");
            } else if (!currentExternalProgramId.equals(externalProgramId)) {
                /*
                 * if (programManager.getProgram(externalProgramId).isExternal()) { if (externalProgram.isExternal()) { admissionManager.processAdmission(clientId, providerNo,
                 * externalProgram, "intake external discharge", "intake external admit"); } }
                 */
                admissionManager.processDischarge(currentExternalProgramId, clientId, "intake external discharge", "0");
                admissionManager.processAdmission(clientId, providerNo, externalProgram, "intake external discharge", "intake external admit");
            }
        }
    }

    public void admitBedCommunityProgram(Integer clientId, String providerNo, Integer bedCommunityProgramId, String saveWhich, String admissionText, Date admissionDate) throws ProgramFullException,
            AdmissionException, ServiceRestrictionException {
        Program bedCommunityProgram = null;
        // Get current community program  
        Admission communityProgramAdmission = admissionManager.getCurrentCommunityProgramAdmission(clientId);
        Integer currentBedCommunityProgramId = communityProgramAdmission != null ? communityProgramAdmission.getProgramId() : null;

        if (admissionText == null) admissionText = "intake admit";

        if ("RFQ_notAdmit".equals(saveWhich) && bedCommunityProgramId == null && currentBedCommunityProgramId == null) {
            return;
        }
        if (bedCommunityProgramId == null && currentBedCommunityProgramId == null) {
            bedCommunityProgram = programManager.getHoldingTankProgram();
        } else if (bedCommunityProgramId != null) {
            bedCommunityProgram = programManager.getProgram(bedCommunityProgramId);
        }

        boolean isFamilyHead = false;
        boolean isFamilyDependent = false;
        JointAdmission clientsJadm = null;
        List<JointAdmission> dependentList = null;
        Integer[] dependentIds = null;

        if (clientManager != null && clientId != null) {
            dependentList = clientManager.getDependents(Integer.valueOf(clientId));
            clientsJadm = clientManager.getJointAdmission(Integer.valueOf(clientId));
        }
        if (clientsJadm != null && clientsJadm.getHeadClientId() != null) {
            isFamilyDependent = true;
        }
        if (dependentList != null && dependentList.size() > 0) {
            isFamilyHead = true;
        }
        if (dependentList != null) {
            dependentIds = new Integer[dependentList.size()];
            for (int i = 0; i < dependentList.size(); i++) {
                dependentIds[i] = new Integer(dependentList.get(i).getClientId().intValue());
            }
        }

        if (isFamilyDependent) {
            throw new AdmissionException("you cannot admit a dependent family/group member, you must remove the dependent status or admit the family head");

        } else if (isFamilyHead && dependentIds != null && dependentIds.length >= 1) {
            Integer[] familyIds = new Integer[dependentIds.length + 1];
            familyIds[0] = clientId;
            for (int i = 0; i < dependentIds.length; i++) {
                familyIds[i + 1] = dependentIds[i];
            }
            for (int i = 0; i < familyIds.length; i++) {
                Integer familyId = familyIds[i];
                if (bedCommunityProgram != null) {
                    if (currentBedCommunityProgramId == null) {
                        admissionManager.processAdmission(familyId, providerNo, bedCommunityProgram, "intake discharge", admissionText, admissionDate);
                    } else if (!currentBedCommunityProgramId.equals(bedCommunityProgramId)) {
                        if (bedCommunityProgram.isCommunity()) {
                            admissionManager.processDischargeToCommunity(bedCommunityProgramId, familyId, providerNo, "intake discharge", "0", admissionDate);
                        } else {
                            admissionManager.processDischarge(currentBedCommunityProgramId, familyId, "intake discharge", "0", admissionDate);
                            admissionManager.processAdmission(familyId, providerNo, bedCommunityProgram, "intake discharge", admissionText, admissionDate);
                        }
                    }
                }
            }

            // throw new AdmissionException(
            // "If you admit the family head, all dependents will also be admitted to this program and discharged from their current programs. Are you sure you wish to proceed?");

        } else {

            if (bedCommunityProgram != null) {
                if (currentBedCommunityProgramId == null) {
                    admissionManager.processAdmission(clientId, providerNo, bedCommunityProgram, "intake discharge", admissionText, admissionDate);
                } else if (!currentBedCommunityProgramId.equals(bedCommunityProgramId)) {
                    if (bedCommunityProgram.isCommunity()) {
                        admissionManager.processDischargeToCommunity(bedCommunityProgramId, clientId, providerNo, "intake discharge", "0", admissionDate);
                    } else {
                        admissionManager.processDischarge(currentBedCommunityProgramId, clientId, "intake discharge", "0", admissionDate);
                        admissionManager.processAdmission(clientId, providerNo, bedCommunityProgram, "intake discharge", admissionText, admissionDate);
                    }
                }
            }
        }
    }

    public void admitServicePrograms(Integer clientId, String providerNo, Set<Integer> serviceProgramIds, String admissionText, Date admissionDate) throws ProgramFullException, AdmissionException,
            ServiceRestrictionException {
        SortedSet<Integer> currentServicePrograms = getCurrentServiceProgramIds(clientId);

        if (admissionText == null) admissionText = "intake admit";

        //only allow to discharge the programs for which you are staff of.
        Set<Program> programsInDomain = getActiveProviderPrograms(providerNo);
        List<Integer> programDomainIds = new ArrayList<Integer>();
        for (Program p : programsInDomain) {
            programDomainIds.add(p.getId());
        }

        //discharge from all
        if (serviceProgramIds.isEmpty()) {
            for (Object programId : currentServicePrograms) {
                if (programDomainIds.contains(programId)) {
                    admissionManager.processDischarge((Integer) programId, clientId, "intake discharge", "0", admissionDate);
                }
            }
            return;
        }

        //remove the ones selected, and discharge the ones not selected
        Collection<?> discharge = CollectionUtils.subtract(currentServicePrograms, serviceProgramIds);

        for (Object programId : discharge) {
            if (programDomainIds.contains(programId)) {
                admissionManager.processDischarge((Integer) programId, clientId, "intake discharge", "0", admissionDate);
            }
        }


        Collection<?> admit = CollectionUtils.subtract(serviceProgramIds, currentServicePrograms);

        for (Object programId : admit) {
            Program program = programManager.getProgram((Integer) programId);
            admissionManager.processAdmission(clientId, providerNo, program, "intake discharge", admissionText, admissionDate);
        }
    }

    private void saveIntake(Intake intake, Integer clientId) {
        intake.setClientId(clientId);
        genericIntakeManager.saveIntake(intake);
    }

    private void saveUpdateIntake(Intake intake, Integer clientId) {
        intake.setClientId(clientId);

        genericIntakeManager.saveUpdateIntake(intake);
    }

    public Set<Program> getActiveProviderProgramsInFacility(LoggedInInfo loggedInInfo, String providerNo, Integer facilityId) {
        Set<Program> programs = new HashSet<Program>();
        Set<Program> programsInDomain = getActiveProviderPrograms(providerNo);
        if (facilityId == null) return programs;

        for (Program p : programManager.getProgramDomainInCurrentFacilityForCurrentProvider(loggedInInfo, false)) {
            if (programsInDomain.contains(p)) {
                programs.add(p);
            }
        }

        return programs;
    }

    // Bean

    private void setBeanProperties(LoggedInInfo loggedInInfo, Intake intake, Demographic client, String providerNo,
                                   boolean serviceProgramsVisible, boolean externalProgramsVisible, SortedSet<Integer> currentServiceProgramIds,
                                   Integer currentExternalProgramId, Integer facilityId, Integer nodeId, List<IntakeNodeJavascript> javascriptLocation, boolean communityProgramsVisible, Integer currentCommunityProgramId) {
        this.setIntake(intake);
        this.setClient(client);
        this.setNodeId(nodeId);
        this.setJsLocation(javascriptLocation);

        if (communityProgramsVisible || serviceProgramsVisible || externalProgramsVisible) {
            Set<Program> providerPrograms = getActiveProviderProgramsInFacility(loggedInInfo, providerNo, facilityId);

            if (communityProgramsVisible) {
                //this.setCommunityPrograms(getCommunityPrograms());
                this.setSelectedCommunityProgramId(currentCommunityProgramId);
            }

            if (serviceProgramsVisible) {
                this.setServicePrograms(getServicePrograms(providerPrograms, providerNo));
                this.setSelectedServiceProgramIds(currentServiceProgramIds);
            }

            if (externalProgramsVisible) {
                this.setExternalPrograms(getExternalPrograms(providerPrograms));
                this.setSelectedExternalProgramId(currentExternalProgramId);
            }

            this.setProgramsInDomain(getProgramsInDomain(providerPrograms));

            String intakeLocation = "";
            if (intake != null) {
                intakeLocation = String.valueOf(intake.getIntakeLocation());
            }
            if (intakeLocation == null || "".equals(intakeLocation) || "null".equals(intakeLocation)) {
                this.setSelectedProgramInDomainId(0);
            } else {
                this.setSelectedProgramInDomainId(Integer.valueOf(intakeLocation));
            }
        }

    }

    protected String createRedirectForward(String forwardName, StringBuilder parameters) {
        return forwardName;
    }

    protected Demographic getClient(HttpServletRequest request) {
        Demographic client = (Demographic) getSessionAttribute(request, CLIENT);
        return (client != null) ? client : new Demographic();
    }

    public void setClientManager(ClientManager mgr) {
        this.clientManager = mgr;
    }

    public void setProgramManager(ProgramManager mgr) {
        this.programManager = mgr;
    }

    public void setAdmissionManager(AdmissionManager mgr) {
        this.admissionManager = mgr;
    }

    public void setCaseManagementManager(CaseManagementManager caseManagementManager) {
        this.caseManagementManager = caseManagementManager;
    }

    protected Integer getClientIdAsInteger(HttpServletRequest request) {
        Integer clientId = null;
        String clientId_str = request.getParameter(CLIENT_ID);
        if (clientId_str != null) {
            try {
                clientId = Integer.valueOf(clientId_str);
            } catch (NumberFormatException e) {
                LOG.error("Error", e);
            }
        }
        return clientId;

    }

    protected Integer getIntakeId(HttpServletRequest request) {
        return Integer.valueOf(request.getParameter(INTAKE_ID));
    }

    protected Object getSessionAttribute(HttpServletRequest request, String attributeName) {
        Object attribute = request.getSession().getAttribute(attributeName);

        if (attribute != null) {
            request.getSession().removeAttribute(attributeName);
        }

        return attribute;
    }

    private static final String EXTERNAL_PROGRAM_LABEL = "External Agency Client Referred From";
    private static final String COMMUNITY_PROGRAM_LABEL = "Residential Status";
    private static final String PROGRAM_IN_DOMAIN_LABEL = "Select where the intake is being performed if different from Admission Program";

    private String method;

    private Demographic client;
    private List genders;
    private LabelValueBean[] months;
    private LabelValueBean[] days;
    private LabelValueBean[] provinces;


    private List<LabelValueBean> communityPrograms;
    private String communityProgramId;
    private String communityProgramLabel;

    private List<LabelValueBean> externalPrograms;
    private String externalProgramId;
    private String externalProgramLabel;

    private List<LabelValueBean> servicePrograms;
    private String[] serviceProgramIds;

    private List<LabelValueBean> programsInDomain;
    private String programInDomainId;
    private String programInDomainLabel;

    private Intake intake;

    private Integer nodeId;

    private List<IntakeNodeJavascript> jsLocation;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Demographic getClient() {
        return client;
    }

    public void setClient(Demographic client) {
        this.client = client;
    }

    public List getGenders() {
        return genders;
    }

    public void setGenders(List genders) {
        this.genders = genders;
    }

    public LabelValueBean[] getMonths() {
        return months;
    }

    public LabelValueBean[] getDays() {
        return days;
    }

    public LabelValueBean[] getProvinces() {
        return provinces;
    }

    public void setProvinces(LabelValueBean[] provinces) {
        this.provinces = provinces;
    }

//	 programs in provider's domain --------------------

    public List<LabelValueBean> getProgramsInDomain() {
        return programsInDomain;
    }

    public void setProgramsInDomain(List<Program> programs_inDomain) {
        setProgramInDomainLabel(!programs_inDomain.isEmpty());

        List<LabelValueBean> labelValues = new ArrayList<LabelValueBean>();
        labelValues.add(GenericIntakeConstants.EMPTY);
        labelValues.addAll(convertToLabelValues(programs_inDomain));
        programsInDomain = labelValues;
    }

    public String getProgramInDomainLabel() {
        return programInDomainLabel;
    }

    public void setProgramInDomainLabel(boolean hasProgramsInDomain) {
        StringBuilder buffer = new StringBuilder();

        if (hasProgramsInDomain) {
            buffer.append(PROGRAM_IN_DOMAIN_LABEL);
        }

        programInDomainLabel = buffer.toString();
    }

    // Selected program id

    public Integer getSelectedProgramInDomainId() {
        return convertToInteger(programInDomainId);
    }

    public void setSelectedProgramInDomainId(Integer selectedId) {
        programInDomainId = convertToString(selectedId);
    }

    public String getProgramInDomainId() {
        return programInDomainId;
    }

    public void setProgramInDomainId(String programInDomainId) {
        this.programInDomainId = programInDomainId;
    }
    //---------------------


    //	 external programs --------------------

    public List<LabelValueBean> getExternalPrograms() {
        return externalPrograms;
    }

    public void setExternalPrograms(List<Program> externalPrograms_s) {
        setExternalProgramLabel(!externalPrograms_s.isEmpty());

        List<LabelValueBean> labelValues = new ArrayList<LabelValueBean>();
        labelValues.add(GenericIntakeConstants.EMPTY);
        labelValues.addAll(convertToLabelValues(externalPrograms_s));
        externalPrograms = labelValues;
    }

    public String getExternalProgramLabel() {
        return externalProgramLabel;
    }

    public void setExternalProgramLabel(boolean hasExternalPrograms) {
        StringBuilder buffer = new StringBuilder();

        if (hasExternalPrograms) {
            buffer.append(EXTERNAL_PROGRAM_LABEL);
        }

        externalProgramLabel = buffer.toString();
    }

    // Selected External program id

    public Integer getSelectedExternalProgramId() {
        return convertToInteger(externalProgramId);
    }

    public void setSelectedExternalProgramId(Integer selectedId) {
        externalProgramId = convertToString(selectedId);
    }

    public String getExternalProgramId() {
        return externalProgramId;
    }

    public void setExternalProgramId(String externalProgramId) {
        this.externalProgramId = externalProgramId;
    }
    //---------------------





    //////////////////////////////////
    //	 Community programs

    public List<LabelValueBean> getCommunityPrograms() {
        return communityPrograms;
    }

    public void setCommunityPrograms(List<Program> communityPrograms2) {
        setCommunityProgramLabel(!communityPrograms2.isEmpty());
        communityPrograms = convertToLabelValues2(communityPrograms2);
    }

    public String getCommunityProgramLabel() {
        return communityProgramLabel;
    }

    public void setCommunityProgramLabel(boolean hasCommunityPrograms) {
        StringBuilder buffer = new StringBuilder();

        if (hasCommunityPrograms) {
            buffer.append(COMMUNITY_PROGRAM_LABEL);
        }

        communityProgramLabel = buffer.toString();
    }

    // Selected Community program id

    public Integer getSelectedCommunityProgramId() {
        return convertToInteger(communityProgramId);
    }

    public void setSelectedCommunityProgramId(Integer selectedId) {
        communityProgramId = convertToString(selectedId);
    }

    public String getCommunityProgramId() {
        return communityProgramId;
    }

    public void setCommunityProgramId(String communityProgramId) {
        this.communityProgramId = communityProgramId;
    }

    // Service programs

    public List<LabelValueBean> getServicePrograms() {
        return servicePrograms;
    }

    public void setServicePrograms(List<Program> programs) {
        servicePrograms = convertToLabelValues(programs);
    }

    // Selected service program id

    public Set<Integer> getSelectedServiceProgramIds() {
        return convertToIntegers(serviceProgramIds);
    }

    public void setSelectedServiceProgramIds(Set<Integer> selectedIds) {
        serviceProgramIds = convertToStrings(selectedIds);
    }

    public String[] getServiceProgramIds() {
        return serviceProgramIds;
    }

    public void setServiceProgramIds(String[] serviceProgramIds) {
        this.serviceProgramIds = serviceProgramIds;
    }

    public Intake getIntake() {
        return intake;
    }

    public void setIntake(Intake intake) {
        this.intake = intake;
    }

    public String getTitle() {
        return intake.getNode().getLabelStr();
    }

    // Private

    private List<LabelValueBean> convertToLabelValues2(List<Program> primary) {
        List<LabelValueBean> labelValues = new ArrayList<LabelValueBean>();

        labelValues.add(GenericIntakeConstants.EMPTY);
        labelValues.addAll(convertToLabelValues(primary));

        return labelValues;
    }

    private List<LabelValueBean> convertToLabelValues(List<Program> programs) {
        List<LabelValueBean> labelValues = new ArrayList<LabelValueBean>();

        if (programs != null) {
            for (Program program : programs) {
                labelValues.add(new LabelValueBean(program.getName(), program.getId().toString()));
            }
        }

        return labelValues;
    }

    private Set<Integer> convertToIntegers(String[] sources) {
        Set<Integer> result = new HashSet<Integer>();

        if (sources != null) {
            for (String source : sources) {
                result.add(convertToInteger(source));
            }
        }

        return result;
    }

    private Integer convertToInteger(String source) {
        Integer result = null;

        if (source != null && source.length() > 0) {
            result = Integer.valueOf(source);
        }

        return result;
    }

    private String[] convertToStrings(Set<Integer> sources) {
        List<String> result = new ArrayList<String>();

        if (sources != null) {
            for (Integer source : sources) {
                result.add(convertToString(source));
            }
        }

        return result.toArray(new String[result.size()]);
    }

    private String convertToString(Integer source) {
        String result = null;

        if (source != null) {
            result = source.toString();
        }

        return result;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public List<IntakeNodeJavascript> getJsLocation() {
        return jsLocation;
    }

    public void setJsLocation(List<IntakeNodeJavascript> jsLocation) {
        this.jsLocation = jsLocation;
    }
}
