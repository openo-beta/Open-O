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
package org.caisi.core.web;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.caisi.service.InfirmBedProgramManager;
import org.oscarehr.PMmodule.service.AdmissionManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.common.model.Admission;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SecurityUtils;
import org.oscarehr.util.SessionConstants;
import org.oscarehr.util.SpringUtils;
import org.oscarehr.util.WebUtils;
import oscar.OscarProperties;
import oscar.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Infirm2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static final Logger logger = MiscUtils.getLogger();
    private static InfirmBedProgramManager bpm = (InfirmBedProgramManager) SpringUtils.getBean(InfirmBedProgramManager.class);
    private ProgramManager pm = (ProgramManager) SpringUtils.getBean(ProgramManager.class);
    private AdmissionManager mgr = (AdmissionManager) SpringUtils.getBean(AdmissionManager.class);


    public static void updateCurrentProgram(String programId, String providerNo) {
        if (programId == null || providerNo == null) {
            logger.warn("Cannot update current program: null programId or providerNo");
            return;
        }
        
        int programIdInt;
        try {
            programIdInt = Integer.parseInt(programId);
        } catch (NumberFormatException e) {
            logger.error("Error parsing programId to set a user's default program", e);
            return;
        }
        
        if (programIdInt <= 0) {
            logger.warn("Invalid program ID: " + programId);
            return;
        }
        
        logger.debug("Updating the current program to " + programId);
        bpm.setDefaultProgramId(providerNo, programIdInt);
    }

    public String execute() throws Exception {
        // Verify user is logged in
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (loggedInInfo == null) {
            logger.error("Unauthorized access attempt to execute method");
            return "login";
        }
        
        // Validate action parameter
        String mtd = request.getParameter("action");
        if (mtd == null) {
            return showProgram();
        }
        
        // Use a whitelist approach for action parameter
        switch (mtd) {
            case "getProgramList":
                return getProgramList();
            case "getSig":
                return getSig();
            case "toggleSig":
                return toggleSig();
            default:
                return showProgram();
        }
    }

    public String showProgram() throws Exception {
        logger.debug("====> inside showProgram action.");

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (loggedInInfo == null) {
            logger.error("Unauthorized access attempt to showProgram");
            return "login";
        }
        
        HttpSession se = request.getSession();
        if (se == null) {
            logger.error("Session not found in showProgram");
            return "login";
        }
        
        se.setAttribute("infirmaryView_initflag", "true");
        String providerNo = (String) se.getAttribute("user");
        if (providerNo == null) {
            logger.error("Provider number not found in session");
            return "login";
        }
        
        String archiveView = (String) request.getSession().getAttribute("archiveView");


        Integer facilityId = null;

        // facility filtering
        if (OscarProperties.getInstance().getBooleanProperty("FILTER_ON_FACILITY", "true")) {
            facilityId = loggedInInfo.getCurrentFacility().getId();
        }

        List<LabelValueBean> programBeans = bpm.getProgramBeans(providerNo, facilityId);

        //this one seems to be for the caisi case management page, and caseload screens
        se.setAttribute("infirmaryView_programBeans", programBeans);


        //set default program
        int defaultprogramId = bpm.getDefaultProgramId(providerNo);
        boolean defaultInList = false;

        for (LabelValueBean programBean : programBeans) {
            int id = new Integer(programBean.getValue()).intValue();
            if (defaultprogramId == id) {
                defaultInList = true;
            }
        }
        if (!defaultInList) {
            defaultprogramId = 0;
        }
        int OriprogramId = 0;
        if (!programBeans.isEmpty()) {
            OriprogramId = new Integer(programBeans.get(0).getValue()).intValue();
        }
        int programId = 0;
        if (defaultprogramId != 0 && OriprogramId != 0) {
            programId = defaultprogramId;
        } else {
            if (OriprogramId == 0) {
                programId = 0;
            }
            if (defaultprogramId == 0 && OriprogramId != 0) {
                programId = OriprogramId;
            }
        }

        //I guess this overrides everything we've done??
        if (se.getAttribute(SessionConstants.CURRENT_PROGRAM_ID) != null) {
            programId = Integer.valueOf((String) se.getAttribute(SessionConstants.CURRENT_PROGRAM_ID)).intValue();
        }

        se.setAttribute(SessionConstants.CURRENT_PROGRAM_ID, String.valueOf(programId));

        org.caisi.core.web.Infirm2Action.updateCurrentProgram(String.valueOf(programId), loggedInInfo.getLoggedInProviderNo());

        if (programId != 0) {
            se.setAttribute("case_program_id", String.valueOf(programId));
        }

        if (programId != 0) {
            se.setAttribute("program_client_statuses", pm.getProgramClientStatuses(new Integer(programId)));
        }

        String[] programInfo = bpm.getProgramInformation(programId);
        if (programInfo[0] != null) {
            se.setAttribute("infirmaryView_programAddress", StringEscapeUtils.escapeHtml(programInfo[0]).replaceAll("\\n", "<br>"));
        } else {
            se.setAttribute("infirmaryView_programAddress", "");
        }
        if (programInfo[1] != null) {
            se.setAttribute("infirmaryView_programTel", StringEscapeUtils.escapeHtml(programInfo[1]));
        } else {
            se.setAttribute("infirmaryView_programTel", "");
        }
        if (programInfo[2] != null) {
            se.setAttribute("infirmaryView_programFax", StringEscapeUtils.escapeHtml(programInfo[2]));
        } else {
            se.setAttribute("infirmaryView_programFax", "");
        }

        Date dt;

        if (se.getAttribute("infirmaryView_date") != null) {
            dt = (Date) se.getAttribute("infirmaryView_date"); //new Date(year,month-1,day);
        } else {
            dt = new Date();
        }

        //lets not load the demographic beans when in appointment view..no point
        org.oscarehr.PMmodule.model.Program p = this.pm.getProgram(programId);
        if (p != null) {
            String exclusiveView = p.getExclusiveView();

            if (exclusiveView != null && exclusiveView.equals("appointment"))
                return null;
        }

        //release memory

        List<LabelValueBean> demographicBeans = bpm.getDemographicByBedProgramIdBeans(programId, dt, archiveView);
        List<LabelValueBean> filteredDemographicBeans = new ArrayList<LabelValueBean>();
        if (request.getParameter("infirmaryView_clientStatusId") != null) {
            String statusId = request.getParameter("infirmaryView_clientStatusId");
            // Validate statusId is numeric
            if (!StringUtils.isNumeric(statusId)) {
                logger.warn("Invalid non-numeric client status ID: " + statusId);
                filteredDemographicBeans = demographicBeans;
            }
            else if (statusId.equals("0")) {
                filteredDemographicBeans = demographicBeans;
            } else {
                Admission admission = new Admission();
                List<Admission> admissions = new ArrayList<Admission>();
                Integer csi;
                for (Iterator<LabelValueBean> iter = demographicBeans.iterator(); iter.hasNext(); ) {
                    LabelValueBean bean = iter.next();
                    String demographicNo = bean.getValue();
                    admission = null;
                    admissions = null;
                    if (archiveView != null && archiveView.equals("true")) {
                        admissions = mgr.getAdmissions_archiveView(String.valueOf(programId), new Integer(demographicNo));
                        for (Iterator<Admission> i1 = admissions.iterator(); i1.hasNext(); ) {
                            admission = i1.next();
                            csi = admission.getClientStatusId();
                            if (csi == null)
                                csi = 0;
                            if (statusId != null && statusId.equals(csi.toString())) {
                                filteredDemographicBeans.add(bean);
                                break;
                            }
                        }

                    } else {
                        admission = mgr.getCurrentAdmission(String.valueOf(programId), new Integer(demographicNo));
                        if (admission != null) {
                            csi = admission.getClientStatusId();
                            if (csi == null)
                                csi = 0;
                            if (statusId != null && statusId.equals(csi.toString())) {
                                filteredDemographicBeans.add(bean);
                            }
                        }
                    }
                }
                request.setAttribute("infirmaryView_clientStatusId", statusId);
            }
        } else {
            filteredDemographicBeans = demographicBeans;
        }
        se.setAttribute("infirmaryView_demographicBeans", filteredDemographicBeans);
        response.getOutputStream().print(" ");

        return null;
    }

    public static void loadProgram(HttpServletRequest request, HttpServletResponse response) {
        try {
            logger.debug("====> inside loadProgram action.");

            // Verify user is logged in
            LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
            if (loggedInInfo == null) {
                logger.error("Unauthorized access attempt to loadProgram");
                return;
            }
            
            HttpSession se = request.getSession();
            se.setAttribute("infirmaryView_initflag", "true");
            String providerNo = (String) se.getAttribute("user");
            if (providerNo == null) {
                logger.error("Provider number not found in session");
                return;
            }
            
            String archiveView = (String) request.getSession().getAttribute("archiveView");


            Integer facilityId = null;

            // facility filtering
            if (OscarProperties.getInstance().getBooleanProperty("FILTER_ON_FACILITY", "true")) {
                facilityId = loggedInInfo.getCurrentFacility().getId();
            }
            List<LabelValueBean> programBeans = bpm.getProgramBeans(providerNo, facilityId);

            //this one seems to be for the caisi case management page, and caseload screens
            se.setAttribute("infirmaryView_programBeans", programBeans);


            //set default program
            int defaultprogramId = bpm.getDefaultProgramId(providerNo);
            boolean defaultInList = false;

            for (LabelValueBean programBean : programBeans) {
                int id = new Integer(programBean.getValue()).intValue();
                if (defaultprogramId == id) {
                    defaultInList = true;
                }
            }
            if (!defaultInList) {
                defaultprogramId = 0;
            }
            int OriprogramId = 0;
            if (!programBeans.isEmpty()) {
                OriprogramId = new Integer(programBeans.get(0).getValue()).intValue();
            }
            int programId = 0;
            if (defaultprogramId != 0 && OriprogramId != 0) {
                programId = defaultprogramId;
            } else {
                if (OriprogramId == 0) {
                    programId = 0;
                }
                if (defaultprogramId == 0 && OriprogramId != 0) {
                    programId = OriprogramId;
                }
            }

            //I guess this overrides everything we've done??
            if (se.getAttribute(SessionConstants.CURRENT_PROGRAM_ID) != null) {
                programId = Integer.valueOf((String) se.getAttribute(SessionConstants.CURRENT_PROGRAM_ID)).intValue();
            }

            se.setAttribute(SessionConstants.CURRENT_PROGRAM_ID, String.valueOf(programId));

            org.caisi.core.web.Infirm2Action.updateCurrentProgram(String.valueOf(programId), loggedInInfo.getLoggedInProviderNo());


            ProgramManager pm = SpringUtils.getBean(ProgramManager.class);
            AdmissionManager mgr = SpringUtils.getBean(AdmissionManager.class);

            if (programId != 0) {
                se.setAttribute("case_program_id", String.valueOf(programId));
            }

            if (programId != 0) {
                se.setAttribute("program_client_statuses", pm.getProgramClientStatuses(new Integer(programId)));
            }

            String[] programInfo = bpm.getProgramInformation(programId);
            if (programInfo[0] != null) {
                se.setAttribute("infirmaryView_programAddress", StringEscapeUtils.escapeHtml(programInfo[0]).replaceAll("\\n", "<br>"));
            } else {
                se.setAttribute("infirmaryView_programAddress", "");
            }
            if (programInfo[1] != null) {
                se.setAttribute("infirmaryView_programTel", StringEscapeUtils.escapeHtml(programInfo[1]));
            } else {
                se.setAttribute("infirmaryView_programTel", "");
            }
            if (programInfo[2] != null) {
                se.setAttribute("infirmaryView_programFax", StringEscapeUtils.escapeHtml(programInfo[2]));
            } else {
                se.setAttribute("infirmaryView_programFax", "");
            }

            Date dt;

            if (se.getAttribute("infirmaryView_date") != null) {
                dt = (Date) se.getAttribute("infirmaryView_date"); //new Date(year,month-1,day);
            } else {
                dt = new Date();
            }

            //lets not load the demographic beans when in appointment view..no point
            org.oscarehr.PMmodule.model.Program p = pm.getProgram(programId);
            if (p != null) {
                String exclusiveView = p.getExclusiveView();

                if (exclusiveView != null && exclusiveView.equals("appointment"))
                    return;
            }

            //release memory

            List<LabelValueBean> demographicBeans = bpm.getDemographicByBedProgramIdBeans(programId, dt, archiveView);
            List<LabelValueBean> filteredDemographicBeans = new ArrayList<LabelValueBean>();
            if (request.getParameter("infirmaryView_clientStatusId") != null) {
                String statusId = request.getParameter("infirmaryView_clientStatusId");
                // Validate statusId is numeric
                if (!StringUtils.isNumeric(statusId)) {
                    logger.warn("Invalid non-numeric client status ID: " + statusId);
                    filteredDemographicBeans = demographicBeans;
                }
                else if (statusId.equals("0")) {
                    filteredDemographicBeans = demographicBeans;
                } else {
                    Admission admission = new Admission();
                    List<Admission> admissions = new ArrayList<Admission>();
                    Integer csi;
                    for (Iterator<LabelValueBean> iter = demographicBeans.iterator(); iter.hasNext(); ) {
                        LabelValueBean bean = iter.next();
                        String demographicNo = bean.getValue();
                        admission = null;
                        admissions = null;
                        if (archiveView != null && archiveView.equals("true")) {
                            admissions = mgr.getAdmissions_archiveView(String.valueOf(programId), new Integer(demographicNo));
                            for (Iterator<Admission> i1 = admissions.iterator(); i1.hasNext(); ) {
                                admission = i1.next();
                                csi = admission.getClientStatusId();
                                if (csi == null)
                                    csi = 0;
                                if (statusId != null && statusId.equals(csi.toString())) {
                                    filteredDemographicBeans.add(bean);
                                    break;
                                }
                            }

                        } else {
                            admission = mgr.getCurrentAdmission(String.valueOf(programId), new Integer(demographicNo));
                            if (admission != null) {
                                csi = admission.getClientStatusId();
                                if (csi == null)
                                    csi = 0;
                                if (statusId != null && statusId.equals(csi.toString())) {
                                    filteredDemographicBeans.add(bean);
                                }
                            }
                        }
                    }
                    request.setAttribute("infirmaryView_clientStatusId", statusId);
                }
            } else {
                filteredDemographicBeans = demographicBeans;
            }
            se.setAttribute("infirmaryView_demographicBeans", filteredDemographicBeans);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }

    public String getSig() {
        logger.debug("====> inside getSig action.");
        
        // Verify user is logged in
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (loggedInInfo == null) {
            logger.error("Unauthorized access attempt to getSig");
            return "login";
        }
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            logger.error("Session not found in getSig");
            return "login";
        }
        
        // Get provider number with validation
        String providerNo = request.getParameter("providerNo");
        if (providerNo == null) {
            providerNo = (String) session.getAttribute("user");
            if (providerNo == null) {
                logger.error("Provider number not found in session");
                return "login";
            }
        } else {
            // Validate that the requested providerNo matches the logged-in user
            // or the user has admin privileges
            String loggedInProviderNo = loggedInInfo.getLoggedInProviderNo();
            if (!providerNo.equals(loggedInProviderNo) && !loggedInInfo.getCurrentProvider().hasAdminRole()) {
                logger.warn("Attempt to access provider signature for different provider: " + providerNo);
                return "accessDenied";
            }
        }
        
        Boolean onsig = bpm.getProviderSig(providerNo);
        session.setAttribute("signOnNote", onsig);
        int pid = bpm.getDefaultProgramId(providerNo);

        session.setAttribute("programId_oscarView", "0");

        String ppid = (String) session.getAttribute("case_program_id");
        if (ppid == null) {
            session.setAttribute("case_program_id", String.valueOf(pid));
        } else {
            session.setAttribute("case_program_id", ppid);
        }

        return null;
    }

    public String toggleSig() {
        logger.debug("====> inside toggleSig action.");
        
        // Verify user is logged in
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (loggedInInfo == null) {
            logger.error("Unauthorized access attempt to toggleSig");
            return "login";
        }
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            logger.error("Session not found in toggleSig");
            return "login";
        }
        
        // Get provider number with validation
        String providerNo = request.getParameter("providerNo");
        if (providerNo == null) {
            providerNo = (String) session.getAttribute("user");
            if (providerNo == null) {
                logger.error("Provider number not found in session");
                return "login";
            }
        } else {
            // Validate that the requested providerNo matches the logged-in user
            // or the user has admin privileges
            String loggedInProviderNo = loggedInInfo.getLoggedInProviderNo();
            if (!providerNo.equals(loggedInProviderNo) && !loggedInInfo.getCurrentProvider().hasAdminRole()) {
                logger.warn("Attempt to toggle signature for different provider: " + providerNo);
                return "accessDenied";
            }
        }
        
        Boolean onsig = bpm.getProviderSig(providerNo);
        session.setAttribute("signOnNote", onsig);
        
        // Add CSRF token validation if this is a POST request
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (!WebUtils.isValidCsrfToken(request)) {
                logger.warn("Invalid CSRF token in toggleSig request");
                return "accessDenied";
            }
        }
        
        return null;
    }

    public String getProgramList() {
        // Verify CSRF token or other security check
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (loggedInInfo == null) {
            logger.error("Unauthorized access attempt to getProgramList");
            return null;
        }
        
        // Add CSRF token validation if this is a POST request
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (!WebUtils.isValidCsrfToken(request)) {
                logger.warn("Invalid CSRF token in getProgramList request");
                return "accessDenied";
            }
        }
        
        String providerNo = request.getParameter("providerNo");
        String facilityId = request.getParameter("facilityId");

        // Set content type and prevent caching
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        String outTxt = "<option value='all'>All Programs</option>";
        PrintWriter out;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            MiscUtils.getLogger().error("Error getting response writer", e);
            return null;
        }
        
        out.print(outTxt);
        
        // Validate input parameters
        if (providerNo == null || facilityId == null || providerNo.isEmpty() || facilityId.isEmpty()) {
            logger.warn("Missing required parameters for getProgramList");
            try {
                response.getWriter().write("<option value=''>Error: Missing parameters</option>");
            } catch (IOException e) {
                logger.error("Error writing response", e);
            }
            return null;
        }
        
        // Validate facilityId is numeric
        if (!StringUtils.isNumeric(facilityId)) {
            logger.warn("Invalid non-numeric facility ID: " + facilityId);
            return null;
        }
        
        int facility_id;
        try {
            facility_id = Integer.parseInt(facilityId);
            if (facility_id <= 0) {
                logger.warn("Invalid facility ID value: " + facility_id);
                return null;
            }
        } catch (NumberFormatException e) {
            MiscUtils.getLogger().error("Error parsing facility ID", e);
            return null;
        }
        
        InfirmBedProgramManager manager = bpm;
        List<LabelValueBean> programBeans = null;
        
        if (providerNo.equalsIgnoreCase("all")) {
            // Check if user has permission to view all programs
            if (!loggedInInfo.getCurrentProvider().hasAdminRole()) {
                logger.warn("Non-admin user attempting to access all programs: " + loggedInInfo.getLoggedInProviderNo());
                try {
                    response.getWriter().write("<option value=''>Access Denied</option>");
                    return null;
                } catch (IOException e) {
                    logger.error("Error writing response", e);
                    return null;
                }
            }
            programBeans = manager.getProgramBeansByFacilityId(facility_id);
        } else {
            // Validate providerNo format and permissions
            if (!providerNo.equals(loggedInInfo.getLoggedInProviderNo()) && 
                !loggedInInfo.getCurrentProvider().hasAdminRole()) {
                logger.warn("User attempting to access programs for different provider: " + providerNo);
                try {
                    response.getWriter().write("<option value=''>Access Denied</option>");
                    return null;
                } catch (IOException e) {
                    logger.error("Error writing response", e);
                    return null;
                }
            }
            programBeans = manager.getProgramBeans(providerNo, facility_id);
        }
        
        // Output program options with proper HTML escaping
        for (LabelValueBean pb : programBeans) {
            String value = StringEscapeUtils.escapeHtml(pb.getValue());
            String label = StringEscapeUtils.escapeHtml(pb.getLabel());
            out.print(String.format("<option value='%s'>%s</option>", value, label));
        }

        return null;
    }

}
