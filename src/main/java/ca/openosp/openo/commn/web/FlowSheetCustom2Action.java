/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


package ca.openosp.openo.commn.web;

import org.apache.logging.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import ca.openosp.openo.commn.dao.FlowSheetCustomizationDao;
import ca.openosp.openo.commn.dao.FlowSheetUserCreatedDao;
import ca.openosp.openo.commn.model.FlowSheetCustomization;
import ca.openosp.openo.commn.model.FlowSheetUserCreated;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import ca.openosp.openo.encounter.oscarMeasurements.FlowSheetItem;
import ca.openosp.openo.encounter.oscarMeasurements.MeasurementFlowSheet;
import ca.openosp.openo.encounter.oscarMeasurements.MeasurementTemplateFlowSheetConfig;
import ca.openosp.openo.encounter.oscarMeasurements.util.Recommendation;
import ca.openosp.openo.encounter.oscarMeasurements.util.RecommendationCondition;
import ca.openosp.openo.encounter.oscarMeasurements.util.TargetColour;
import ca.openosp.openo.encounter.oscarMeasurements.util.TargetCondition;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class FlowSheetCustom2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static final Logger logger = MiscUtils.getLogger();

    private FlowSheetCustomizationDao flowSheetCustomizationDao = SpringUtils.getBean(FlowSheetCustomizationDao.class);
    private FlowSheetUserCreatedDao flowSheetUserCreatedDao = SpringUtils.getBean(FlowSheetUserCreatedDao.class);
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public String execute() throws Exception {
        String method = request.getParameter("method");
        if ("save".equals(method)) {
            return save();
        } else if ("update".equals(method)) {
            return update();
        } else if ("delete".equals(method)) {
            return delete();
        } else if ("restore".equals(method)) {
            return restore();
        } else if ("archiveMod".equals(method)) {
            return archiveMod();
        } else if ("createNewFlowSheet".equals(method)) {
            return createNewFlowSheet();
        }
        return SUCCESS;
    }

    public String save() throws Exception {
        String flowsheet = request.getParameter("flowsheet");
        String demographicNo = "0";
        if (request.getParameter("demographic") != null) {
            demographicNo = request.getParameter("demographic");
        }
        String scope = request.getParameter("scope");

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_demographic", "w", demographicNo)) {
            throw new SecurityException("missing required sec object (_demographic)");
        }

        MeasurementTemplateFlowSheetConfig templateConfig = MeasurementTemplateFlowSheetConfig.getInstance();
        MeasurementFlowSheet mFlowsheet = templateConfig.getFlowSheet(flowsheet);

        if (request.getParameter("measurement") != null) {

            Hashtable<String, String> h = new Hashtable<String, String>();

            h.put("measurement_type", request.getParameter("measurement"));
            h.put("display_name", request.getParameter("display_name"));
            h.put("guideline", request.getParameter("guideline"));
            h.put("graphable", request.getParameter("graphable"));
            h.put("value_name", request.getParameter("value_name"));
            String prevItem = null;
            if (request.getParameter("count") != null) {
                int cou = Integer.parseInt(request.getParameter("count"));
                if (cou != 0) {
                    prevItem = mFlowsheet.getMeasurementList().get(cou);
                }
            }

            @SuppressWarnings("unchecked")
            Enumeration<String> en = request.getParameterNames();

            List<Recommendation> ds = new ArrayList<Recommendation>();
            while (en.hasMoreElements()) {
                String s = en.nextElement();
                if (s.startsWith("monthrange")) {
                    String extrachar = s.replaceAll("monthrange", "").trim();
                    logger.debug("EXTRA CAH " + extrachar);

                    if (request.getParameter("monthrange" + extrachar) != null) {
                        String mRange = request.getParameter("monthrange" + extrachar);
                        String strn = request.getParameter("strength" + extrachar);
                        String dsText = request.getParameter("text" + extrachar);

                        if (!mRange.trim().equals("")) {
                            ds.add(new Recommendation("" + h.get("measurement_type"), mRange, strn, dsText));
                        }
                    }
                }
            }

            if (h.get("measurement_type") != null) {
                FlowSheetItem item = new FlowSheetItem(h);
                item.setRecommendations(ds);
                Element va = templateConfig.getItemFromObject(item);

                XMLOutputter outp = new XMLOutputter();
                outp.setFormat(Format.getPrettyFormat());

                FlowSheetCustomization cust = new FlowSheetCustomization();
                cust.setAction(FlowSheetCustomization.ADD);
                cust.setPayload(outp.outputString(va));
                cust.setFlowsheet(flowsheet);
                cust.setMeasurement(prevItem);//THIS THE MEASUREMENT TO SET THIS AFTER!
                cust.setProviderNo("clinic".equals(scope) ? "" : LoggedInInfo.getLoggedInInfoFromSession(request).getLoggedInProviderNo());
                cust.setDemographicNo(demographicNo);
                cust.setCreateDate(new Date());

                logger.debug("SAVE " + cust);

                flowSheetCustomizationDao.persist(cust);

            }
        }
        request.setAttribute("demographic", demographicNo);
        request.setAttribute("flowsheet", flowsheet);
        return SUCCESS;
    }

    public String update() {
        MeasurementTemplateFlowSheetConfig templateConfig = MeasurementTemplateFlowSheetConfig.getInstance();

        String flowsheet = request.getParameter("flowsheet");
        String demographicNo = "0";
        if (request.getParameter("demographic") != null) {
            demographicNo = request.getParameter("demographic");
        }
        String scope = request.getParameter("scope");

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_demographic", "w", demographicNo)) {
            throw new SecurityException("missing required sec object (_demographic)");
        }

        logger.debug("UPDATING FOR demographic " + demographicNo);

        if (request.getParameter("updater") != null) {
            Hashtable<String, String> h = new Hashtable<String, String>();
            h.put("measurement_type", request.getParameter("measurement_type"));
            h.put("display_name", request.getParameter("display_name"));
            h.put("guideline", request.getParameter("guideline"));
            h.put("graphable", request.getParameter("graphable"));
            h.put("value_name", request.getParameter("value_name"));

            FlowSheetItem item = new FlowSheetItem(h);


            @SuppressWarnings("unchecked")
            Enumeration<String> en = request.getParameterNames();

            List<TargetColour> targets = new ArrayList<TargetColour>();
            List<Recommendation> recommendations = new ArrayList<Recommendation>();
            while (en.hasMoreElements()) {
                String s = en.nextElement();
                if (s.startsWith("strength")) {
                    String extrachar = s.replaceAll("strength", "").trim();
                    logger.debug("EXTRA CAH " + extrachar);
                    boolean go = true;
                    Recommendation rec = new Recommendation();
                    rec.setStrength(request.getParameter(s));
                    int targetCount = 1;
                    rec.setText(request.getParameter("text" + extrachar));
                    List<RecommendationCondition> conds = new ArrayList<RecommendationCondition>();
                    while (go) {
                        String type = request.getParameter("type" + extrachar + "c" + targetCount);
                        if (type != null) {
                            if (!type.equals("-1")) {
                                String param = request.getParameter("param" + extrachar + "c" + targetCount);
                                String value = request.getParameter("value" + extrachar + "c" + targetCount);
                                RecommendationCondition cond = new RecommendationCondition();
                                cond.setType(type);
                                cond.setParam(param);
                                cond.setValue(value);
                                if (value != null && !value.trim().equals("")) {
                                    conds.add(cond);
                                }
                            }
                        } else {
                            go = false;
                        }
                        targetCount++;
                    }
                    if (conds.size() > 0) {
                        rec.setRecommendationCondition(conds);
                        recommendations.add(rec);
                    }
                    //////
                    /*  Strength:   <select name="strength<%=count%>">
                        Text: <input type="text" name="text<%=count%>" length="100"  value="<%=e.getText()%>" />
                        <select name="type<%=count%>c<%=condCount%>" >
                        Param: <input type="text" name="param<%=count%>c<%=condCount%>" value="<%=s(cond.getParam())%>" />
                        Value: <input type="text" name="value<%=count%>c<%=condCount%>" value="<%=cond.getValue()%>" />
                    */
                    //////


//                    String mRange = request.getParameter("monthrange" + extrachar);
//                    String strn = request.getParameter("strength" + extrachar);
//                    String dsText = request.getParameter("text" + extrachar);
//                    if (!mRange.trim().equals("")){
//                       ds.add(new Recommendation("" + h.get("measurement_type"), mRange, strn, dsText));
//                    }
                } else if (s.startsWith("col")) {
                    String extrachar = s.replaceAll("col", "").trim();
                    logger.debug("EXTRA CHA " + extrachar);
                    boolean go = true;
                    int targetCount = 1;
                    TargetColour tcolour = new TargetColour();
                    tcolour.setIndicationColor(request.getParameter(s));
                    List<TargetCondition> conds = new ArrayList<TargetCondition>();
                    while (go) {
                        String type = request.getParameter("targettype" + extrachar + "c" + targetCount);
                        if (type != null) {
                            if (!type.equals("-1")) {
                                String param = request.getParameter("targetparam" + extrachar + "c" + targetCount);
                                String value = request.getParameter("targetvalue" + extrachar + "c" + targetCount);
                                TargetCondition cond = new TargetCondition();
                                cond.setType(type);
                                cond.setParam(param);
                                cond.setValue(value);
                                if (value != null && !value.trim().equals("")) {
                                    conds.add(cond);
                                }
                            }
                        } else {
                            go = false;
                        }
                        targetCount++;
                    }
                    if (conds.size() > 0) {
                        tcolour.setTargetConditions(conds);
                        targets.add(tcolour);
                    }
                }
            }
            item.setTargetColour(targets);
            item.setRecommendations(recommendations);

            Element va = templateConfig.getItemFromObject(item);

            XMLOutputter outp = new XMLOutputter();
            outp.setFormat(Format.getPrettyFormat());

            FlowSheetCustomization cust = new FlowSheetCustomization();
            cust.setAction(FlowSheetCustomization.UPDATE);
            cust.setPayload(outp.outputString(va));
            cust.setFlowsheet(flowsheet);
            if (demographicNo != null) {
                cust.setDemographicNo(demographicNo);
            }
            cust.setMeasurement(item.getItemName());//THIS THE MEASUREMENT TO SET THIS AFTER!
            cust.setProviderNo("clinic".equals(scope) ? "" : LoggedInInfo.getLoggedInInfoFromSession(request).getLoggedInProviderNo());

            logger.debug("UPDATE " + cust);

            flowSheetCustomizationDao.persist(cust);

        }
        request.setAttribute("demographic", demographicNo);
        request.setAttribute("flowsheet", flowsheet);
        return SUCCESS;
    }

    public String delete() {
        logger.debug("IN DELETE");
        String flowsheet = request.getParameter("flowsheet");
        String measurement = request.getParameter("measurement");
        String demographicNo = "0";
        if (request.getParameter("demographic") != null) {
            demographicNo = request.getParameter("demographic");
        }
        String scope = request.getParameter("scope");

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_demographic", "w", demographicNo)) {
            throw new SecurityException("missing required sec object (_demographic)");
        }

        FlowSheetCustomization cust = new FlowSheetCustomization();
        cust.setAction(FlowSheetCustomization.DELETE);
        cust.setFlowsheet(flowsheet);
        cust.setMeasurement(measurement);
        cust.setProviderNo("clinic".equals(scope) || demographicNo != null ? "" : LoggedInInfo.getLoggedInInfoFromSession(request).getLoggedInProviderNo());
        cust.setDemographicNo(demographicNo);

        flowSheetCustomizationDao.persist(cust);
        logger.debug("DELETE " + cust);

        request.setAttribute("demographic", demographicNo);
        request.setAttribute("flowsheet", flowsheet);
        return SUCCESS;
    }

    public String restore() {
        logger.debug("IN RESTORE");
        String flowsheet = request.getParameter("flowsheet");
        String measurement = request.getParameter("measurement");
        String demographicNo = "0";
        if (request.getParameter("demographic") != null) {
            demographicNo = request.getParameter("demographic");
        }
        String scope = request.getParameter("scope");

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_demographic", "w", demographicNo)) {
            throw new SecurityException("missing required sec object (_demographic)");
        }

        if ("clinic".equals(scope)) {
            //clinic level
            for (FlowSheetCustomization cust : flowSheetCustomizationDao.getFlowSheetCustomizations(flowsheet)) {
                if ("delete".equals(cust.getAction()) && cust.getMeasurement().equals(measurement)) {
                    flowSheetCustomizationDao.remove(cust.getId());
                }
            }

        } else {
            if (demographicNo == null) {
                //providers level
                for (FlowSheetCustomization cust : flowSheetCustomizationDao.getFlowSheetCustomizations(flowsheet, LoggedInInfo.getLoggedInInfoFromSession(request).getLoggedInProviderNo())) {
                    if ("delete".equals(cust.getAction()) && cust.getMeasurement().equals(measurement)) {
                        flowSheetCustomizationDao.remove(cust.getId());
                    }
                }
            } else {
                //patient level
                for (FlowSheetCustomization cust : flowSheetCustomizationDao.getFlowSheetCustomizationsForPatient(flowsheet, demographicNo)) {
                    if ("delete".equals(cust.getAction()) && cust.getMeasurement().equals(measurement)) {
                        flowSheetCustomizationDao.remove(cust.getId());
                    }
                }
            }
        }

        request.setAttribute("demographic", demographicNo);
        request.setAttribute("flowsheet", flowsheet);
        return SUCCESS;
    }

    public String archiveMod() {
        logger.debug("IN MOD");
        String id = request.getParameter("id");

        String flowsheet = request.getParameter("flowsheet");
        String demographicNo = request.getParameter("demographic");

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_demographic", "w", demographicNo)) {
            throw new SecurityException("missing required sec object (_demographic)");
        }

        FlowSheetCustomization cust = flowSheetCustomizationDao.getFlowSheetCustomization(Integer.parseInt(id));
        if (cust != null) {
            cust.setArchived(true);
            cust.setArchivedDate(new Date());
            flowSheetCustomizationDao.merge(cust);
        }
        logger.debug("archiveMod " + cust);

        request.setAttribute("demographic", demographicNo);
        request.setAttribute("flowsheet", flowsheet);
        return SUCCESS;
    }

    /*first add it as a flowsheet into the current system.  The save it to the database so that it will be there on reboot */
    public String createNewFlowSheet() {
        logger.debug("IN create new flowsheet");
        //String name let oscar create the name
        String dxcodeTriggers = request.getParameter("dxcodeTriggers");
        String displayName = request.getParameter("displayName");
        String warningColour = request.getParameter("warningColour");
        String recommendationColour = request.getParameter("recommendationColour");
        //String topHTML 				= request.getParameter("topHTML");  // Not supported yet


        /// NEW FLOWSHEET CODE
        MeasurementFlowSheet m = new MeasurementFlowSheet();
        m.parseDxTriggers(dxcodeTriggers);
        m.setDisplayName(displayName);
        m.setWarningColour(warningColour);
        m.setRecommendationColour(recommendationColour);

        MeasurementTemplateFlowSheetConfig templateConfig = MeasurementTemplateFlowSheetConfig.getInstance();
        String name = templateConfig.addFlowsheet(m);
        m.loadRuleBase();
        /// END FLOWSHEET CODE

        FlowSheetUserCreated fsuc = new FlowSheetUserCreated();
        fsuc.setName(name);
        fsuc.setDisplayName(displayName);
        fsuc.setDxcodeTriggers(dxcodeTriggers);
        fsuc.setWarningColour(warningColour);
        fsuc.setRecommendationColour(recommendationColour);
        fsuc.setArchived(false);
        fsuc.setCreatedDate(new Date());
        flowSheetUserCreatedDao.persist(fsuc);

        request.setAttribute("flowsheet", fsuc.getName());
        request.setAttribute("displayName", fsuc.getDisplayName());
        return SUCCESS;
    }
}
