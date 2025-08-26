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
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.oscarehr.PMmodule.service.AdmissionManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.PMmodule.model.Program;
import org.oscarehr.common.model.Admission;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SessionConstants;
import org.oscarehr.util.SpringUtils;
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
    private ProgramManager pm = SpringUtils.getBean(ProgramManager.class);
    private AdmissionManager mgr = SpringUtils.getBean(AdmissionManager.class);


    public static void updateCurrentProgram(String programId, String providerNo) {
        if (programId == null) {
            return;
        }
        try {
            Integer.parseInt(programId);
        } catch (NumberFormatException e) {
            logger.error("error parsing programId to set a users default program", e);
        }
        logger.debug("updating the current program to " + programId);
    }

    public String execute() throws Exception {
        String mtd = request.getParameter("action");
        if ("getProgramList".equals(mtd)) {
            return getProgramList();
        } else if ("getSig".equals(mtd)) {
            return getSig();
        } else if ("toggleSig".equals(mtd)) {
            return toggleSig();
        }
        return showProgram();
    }

    public String showProgram() throws Exception {
        logger.debug("====> inside showProgram action.");

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        HttpSession se = request.getSession();
        se.setAttribute("infirmaryView_initflag", "true");
        String providerNo = (String) se.getAttribute("user");
        String archiveView = (String) request.getSession().getAttribute("archiveView");


        Integer facilityId = null;

        // facility filtering
        if (OscarProperties.getInstance().getBooleanProperty("FILTER_ON_FACILITY", "true")) {
            facilityId = loggedInInfo.getCurrentFacility().getId();
        }

        // Get programs for the provider
        List<LabelValueBean> programBeans = new ArrayList<LabelValueBean>();
        
        // Get the programs for this provider
        List<Program> programs = pm.getProgramDomain(providerNo);
        if (facilityId != null) {
            // Filter by facility if needed
            for (Program p : programs) {
                if (p.getFacilityId() == facilityId.intValue() && p.isActive()) {
                    programBeans.add(new LabelValueBean(p.getName(), String.valueOf(p.getId())));
                }
            }
        } else {
            // No facility filtering
            for (Program p : programs) {
                if (p.isActive()) {
                    programBeans.add(new LabelValueBean(p.getName(), String.valueOf(p.getId())));
                }
            }
        }

        //this one seems to be for the caisi case management page, and caseload screens
        se.setAttribute("infirmaryView_programBeans", programBeans);


        //set default program
        int defaultprogramId = 0;
        boolean defaultInList = false;

        for (LabelValueBean programBean : programBeans) {
            int id = Integer.valueOf(programBean.getValue()).intValue();
            if (defaultprogramId == id) {
                defaultInList = true;
            }
        }
        if (!defaultInList) {
            defaultprogramId = 0;
        }
        int OriprogramId = 0;
        if (!programBeans.isEmpty()) {
            OriprogramId = Integer.valueOf(programBeans.get(0).getValue()).intValue();
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
            se.setAttribute("program_client_statuses", pm.getProgramClientStatuses(Integer.valueOf(programId)));
        }

        // Set program information from the selected program
        if (programId != 0) {
            org.oscarehr.PMmodule.model.Program selectedProgram = pm.getProgram(programId);
            if (selectedProgram != null) {
                se.setAttribute("infirmaryView_programAddress", selectedProgram.getAddress() != null ? selectedProgram.getAddress().replaceAll("\\n", "<br>") : "");
                se.setAttribute("infirmaryView_programTel", selectedProgram.getPhone() != null ? selectedProgram.getPhone() : "");
                se.setAttribute("infirmaryView_programFax", selectedProgram.getFax() != null ? selectedProgram.getFax() : "");
            } else {
                se.setAttribute("infirmaryView_programAddress", "");
                se.setAttribute("infirmaryView_programTel", "");
                se.setAttribute("infirmaryView_programFax", "");
            }
        } else {
            se.setAttribute("infirmaryView_programAddress", "");
            se.setAttribute("infirmaryView_programTel", "");
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

        List<LabelValueBean> demographicBeans = new ArrayList<LabelValueBean>();
        List<LabelValueBean> filteredDemographicBeans = new ArrayList<LabelValueBean>();
        if (request.getParameter("infirmaryView_clientStatusId") != null) {
            String statusId = request.getParameter("infirmaryView_clientStatusId");
            if (statusId.equals("0")) {
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
                        admissions = mgr.getAdmissions_archiveView(String.valueOf(programId), Integer.valueOf(demographicNo));
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
                        admission = mgr.getCurrentAdmission(String.valueOf(programId), Integer.valueOf(demographicNo));
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

            LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
            HttpSession se = request.getSession();
            se.setAttribute("infirmaryView_initflag", "true");
            String providerNo = (String) se.getAttribute("user");
            String archiveView = (String) request.getSession().getAttribute("archiveView");


            Integer facilityId = null;

            // facility filtering
            if (OscarProperties.getInstance().getBooleanProperty("FILTER_ON_FACILITY", "true")) {
                facilityId = loggedInInfo.getCurrentFacility().getId();
            }
            // Get programs for the provider
        List<LabelValueBean> programBeans = new ArrayList<LabelValueBean>();


            //set default program
            int defaultprogramId = 0;
            boolean defaultInList = false;

            for (LabelValueBean programBean : programBeans) {
                int id = Integer.valueOf(programBean.getValue()).intValue();
                if (defaultprogramId == id) {
                    defaultInList = true;
                }
            }
            if (!defaultInList) {
                defaultprogramId = 0;
            }
            int OriprogramId = 0;
            if (!programBeans.isEmpty()) {
                OriprogramId = Integer.valueOf(programBeans.get(0).getValue()).intValue();
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
            
            // Get the programs for this provider
            List<Program> programs = pm.getProgramDomain(providerNo);
            if (facilityId != null) {
                // Filter by facility if needed
                for (Program p : programs) {
                    if (p.getFacilityId() == facilityId.intValue() && p.isActive()) {
                        programBeans.add(new LabelValueBean(p.getName(), String.valueOf(p.getId())));
                    }
                }
            } else {
                // No facility filtering
                for (Program p : programs) {
                    if (p.isActive()) {
                        programBeans.add(new LabelValueBean(p.getName(), String.valueOf(p.getId())));
                    }
                }
            }

            //this one seems to be for the caisi case management page, and caseload screens
            se.setAttribute("infirmaryView_programBeans", programBeans);

            if (programId != 0) {
                se.setAttribute("case_program_id", String.valueOf(programId));
            }

            if (programId != 0) {
                se.setAttribute("program_client_statuses", pm.getProgramClientStatuses(Integer.valueOf(programId)));
            }

            // Set program information from the selected program
            if (programId != 0) {
                org.oscarehr.PMmodule.model.Program selectedProgram = pm.getProgram(programId);
                if (selectedProgram != null) {
                    se.setAttribute("infirmaryView_programAddress", selectedProgram.getAddress() != null ? selectedProgram.getAddress().replaceAll("\\n", "<br>") : "");
                    se.setAttribute("infirmaryView_programTel", selectedProgram.getPhone() != null ? selectedProgram.getPhone() : "");
                    se.setAttribute("infirmaryView_programFax", selectedProgram.getFax() != null ? selectedProgram.getFax() : "");
                } else {
                    se.setAttribute("infirmaryView_programAddress", "");
                    se.setAttribute("infirmaryView_programTel", "");
                    se.setAttribute("infirmaryView_programFax", "");
                }
            } else {
                se.setAttribute("infirmaryView_programAddress", "");
                se.setAttribute("infirmaryView_programTel", "");
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

            List<LabelValueBean> demographicBeans = new ArrayList<LabelValueBean>();
            List<LabelValueBean> filteredDemographicBeans = new ArrayList<LabelValueBean>();
            if (request.getParameter("infirmaryView_clientStatusId") != null) {
                String statusId = request.getParameter("infirmaryView_clientStatusId");
                if (statusId.equals("0")) {
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
                            admissions = mgr.getAdmissions_archiveView(String.valueOf(programId), Integer.valueOf(demographicNo));
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
                            admission = mgr.getCurrentAdmission(String.valueOf(programId), Integer.valueOf(demographicNo));
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
        String providerNo = request.getParameter("providerNo");
        if (providerNo == null) providerNo = (String) request.getSession().getAttribute("user");
        request.getSession().setAttribute("signOnNote", false);
        int pid = 0;

        request.getSession().setAttribute("programId_oscarView", "0");

        String ppid = (String) request.getSession().getAttribute("case_program_id");
        if (ppid == null) {
            request.getSession().setAttribute("case_program_id", String.valueOf(pid));
        } else {
            request.getSession().setAttribute("case_program_id", ppid);

        }

        return null;
    }

    public String toggleSig() {
        logger.debug("====> inside toggleSig action.");
        String providerNo = request.getParameter("providerNo");
        if (providerNo == null) providerNo = (String) request.getSession().getAttribute("user");
        request.getSession().setAttribute("signOnNote", false);
        return null;
    }

    public String getProgramList() {
        String providerNo = request.getParameter("providerNo");
        String facilityId = request.getParameter("facilityId");

        String outTxt = "<option value='all'>All Programs</option>";
        PrintWriter out;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            MiscUtils.getLogger().error(e.getMessage(), e);
            return null;
        }
        out.print(outTxt);
        if (providerNo == null || facilityId == null || providerNo.isEmpty() || facilityId.isEmpty()) {
            return null;
        }
        int facility_id = 1;
        try {
            facility_id = Integer.parseInt(facilityId);
        } catch (NumberFormatException e) {
            MiscUtils.getLogger().error(e.getMessage(), e);
            return null;
        }
        return null;
    }

}
