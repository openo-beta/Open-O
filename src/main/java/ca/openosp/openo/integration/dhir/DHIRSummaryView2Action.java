/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * <p>
 * This software was written for the Department of Family Medicine McMaster University Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.integration.dhir;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.dao.LookupListDao;
import ca.openosp.openo.commn.dao.LookupListItemDao;
import ca.openosp.openo.commn.dao.PreventionDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.JSONAction;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.commn.model.Prevention;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.integration.fhir.r4.model.Patient;
import ca.openosp.openo.integration.oneId.TokenExpiredException;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Struts2 action for DHIR (Digital Health Immunization Repository) summary view.
 * Provides JSON endpoints for immunization data from DHIR and from the local EMR.
 *
 * @since 2025-01-01
 */
public class DHIRSummaryView2Action extends JSONAction {

    private static final Logger logger = MiscUtils.getLogger();

    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final DemographicDao demographicDao = SpringUtils.getBean(DemographicDao.class);

    public String execute() throws Exception {
        String method = request.getParameter("method");
        if ("emrData".equals(method)) {
            return emrData();
        }
        if ("hideDisclaimer".equals(method)) {
            return hideDisclaimer();
        }
        return dhirData();
    }

    /**
     * Fetches immunization data from DHIR for the given demographic, date range.
     * Returns a JSON response containing immunizations, recommendations, and patient info.
     */
    private String dhirData() throws Exception {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_prevention", "r", null)) {
            throw new SecurityException("missing required security object (_prevention)");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy-MM-dd");

        String demographicNo = request.getParameter("demographic_no");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        Demographic demographic = demographicDao.getDemographic(demographicNo);

        if (StringUtils.isEmpty(demographic.getHin())) {
            jsonResponse(new JSONObject().put("error", "No Health Card Number"));
            return null;
        }
        if (StringUtils.isEmpty(demographic.getFormattedDob())) {
            jsonResponse(new JSONObject().put("error", "No valid Date of Birth"));
            return null;
        }

        Date startDate = null;
        Date endDate = null;
        try {
            startDate = fmt1.parse(startDateStr);
        } catch (ParseException e) {
            // leave null; DHIRManager handles null dates
        }
        try {
            endDate = fmt1.parse(endDateStr);
        } catch (ParseException e) {
            // leave null; DHIRManager handles null dates
        }

        DHIRManager mgr = new DHIRManager();
        Bundle bundle;
        List<String> searchParamsUsed = new ArrayList<>();
        try {
            bundle = mgr.search(request, demographic, startDate, endDate, searchParamsUsed);
        } catch (TokenExpiredException e) {
            jsonResponse(new JSONObject().put("error", e.getMessage()).put("requireNewToken", true));
            return null;
        } catch (ConsentBlockException | DHIRException e) {
            jsonResponse(new JSONObject().put("error", e.getMessage()));
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving DHIR data", e);
            jsonResponse(new JSONObject().put("error", e.getMessage()));
            return null;
        }

        if (bundle == null) {
            logger.debug("null bundle");
            jsonResponse(new JSONObject().put("error", "An error occured retrieving the data"));
            return null;
        }

        logger.info(FhirContext.forR4().newJsonParser().encodeResourceToString(bundle));

        SearchResultsHandler handler = new SearchResultsHandler(bundle);

        List<ImmunizationHandler> handlers = new ArrayList<>();
        for (Immunization immunization : handler.getImmunizationResources()) {
            handlers.add(new ImmunizationHandler(immunization));
        }

        JSONObject root = new JSONObject();
        JSONArray imms = new JSONArray();

        for (ImmunizationHandler iHandler : handlers) {
            JSONObject imm = new JSONObject();
            imm.put("immunizationDate", emptyIfNull(iHandler.getImmunizationDate()));
            imm.put("validFlag", emptyIfNull(iHandler.getValidFlag()));
            imm.put("agent", emptyIfNull(iHandler.getAgent()));
            imm.put("tradeName", emptyIfNull(iHandler.getTradeName()));
            imm.put("lotNumber", emptyIfNull(iHandler.getLotNumber()));
            imm.put("status", emptyIfNull(iHandler.getStatus()));
            imm.put("PHU", emptyIfNull(iHandler.getPHU()));
            imm.put("performerName", emptyIfNull(iHandler.getPerformerName(handler.getAllResources())));
            imm.put("expirationDate", emptyIfNull(iHandler.getExpirationDate()));
            imms.put(imm);
        }

        root.put("timestamp", "");
        root.put("startDate", startDateStr);
        root.put("endDate", endDateStr);
        root.put("immunizations", imms);
        root.put("searchParams", searchParamsUsed);

        Map<String, Resource> map = handler.getAllResources();
        JSONArray recommendations = new JSONArray();

        for (Resource r : map.values()) {
            if (r.getResourceType() == ResourceType.ImmunizationRecommendation) {
                ImmunizationRecommendationsHandler irHandler = new ImmunizationRecommendationsHandler(
                        (org.hl7.fhir.r4.model.ImmunizationRecommendation) r);

                String dateGenerated = sdf.format(irHandler.getDate());
                root.put("immunizationsRecommendationDateGenerated", dateGenerated);

                Map<String, List<JSONObject>> mapByStatus = new HashMap<>();
                mapByStatus.put("Overdue", new ArrayList<>());
                mapByStatus.put("Up to date", new ArrayList<>());
                mapByStatus.put("Due", new ArrayList<>());
                mapByStatus.put("Eligible but not due", new ArrayList<>());

                for (ImmunizationRecommendation ir : irHandler.getRecs()) {
                    JSONObject rec = new JSONObject();

                    JSONArray vaccineCodes = new JSONArray();
                    for (Coding c : ir.getCodes()) {
                        JSONObject v = new JSONObject();
                        v.put("system", c.getSystem());
                        v.put("code", c.getCode());
                        v.put("display", c.getDisplay());
                        vaccineCodes.put(v);
                    }
                    rec.put("vaccineCodes", vaccineCodes);
                    rec.put("targetDisease", emptyIfNull(ir.getTargetDisease()));
                    rec.put("date", sdf.format(ir.getDate()));

                    Coding c = ir.getForecastStatus();
                    JSONObject fs = new JSONObject();
                    fs.put("system", c.getSystem());
                    fs.put("code", c.getCode());
                    fs.put("display", c.getDisplay());
                    rec.put("forecastStatus", fs);
                    rec.put("dateGenerated", dateGenerated);

                    List<JSONObject> bucket = mapByStatus.computeIfAbsent(c.getDisplay(), k -> new ArrayList<>());
                    bucket.add(rec);
                    recommendations.put(rec);
                }

                root.put("recommendations", recommendations);

                JSONObject rec2 = new JSONObject();
                for (Map.Entry<String, List<JSONObject>> entry : mapByStatus.entrySet()) {
                    JSONArray arr = new JSONArray();
                    for (JSONObject obj : entry.getValue()) {
                        arr.put(obj);
                    }
                    rec2.put(entry.getKey(), arr);
                }
                root.put("recommendationsByStatus", rec2);

            } else if (r.getResourceType() == ResourceType.Patient) {
                root.put("patient", handler.getResourceAsString(r));
                Patient patient = new Patient((org.hl7.fhir.r4.model.Patient) r);
                root.put("dob", patient.getOscarResource().getBirthDayAsString());
                root.put("sex", patient.getOscarResource().getSex());
                root.put("lastname", patient.getOscarResource().getLastName());
                root.put("firstname", patient.getOscarResource().getFirstName());
                root.put("middlename", patient.getOscarResource().getMiddleNames());
            }
        }

        jsonResponse(root);
        return null;
    }

    /**
     * Returns immunization records from the local EMR for the given demographic and date range.
     */
    private String emrData() throws Exception {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_prevention", "r", null)) {
            throw new SecurityException("missing required security object (_prevention)");
        }

        PreventionDao preventionDao = SpringUtils.getBean(PreventionDao.class);
        ProviderDao providerDao = SpringUtils.getBean(ProviderDao.class);
        LookupListDao lookupListDao = SpringUtils.getBean(LookupListDao.class);
        LookupListItemDao lookupListItemDao = SpringUtils.getBean(LookupListItemDao.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy-MM-dd");

        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        Date startDate = null;
        Date endDate = null;
        try {
            startDate = fmt1.parse(startDateStr);
        } catch (ParseException e) {
            // leave null
        }
        try {
            endDate = fmt1.parse(endDateStr);
            Calendar c = Calendar.getInstance();
            c.setTime(endDate);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            endDate = c.getTime();
        } catch (ParseException e) {
            // leave null
        }

        List<Prevention> preventions = preventionDao.findActiveByDemoIdWithDates(
                Integer.parseInt(request.getParameter("demographic_no")), startDate, endDate);

        JSONObject root = new JSONObject();
        JSONArray imms = new JSONArray();

        LookupList ll = lookupListDao.findByName("RouteOfAdmin");
        LookupList llSite = lookupListDao.findByName("AnatomicalSite");

        for (Prevention prevention : preventions) {
            prevention.setPreventionExtendedProperties();

            JSONObject imm = new JSONObject();
            imm.put("name", emptyIfNull(prevention.getName()));
            imm.put("code", emptyIfNull(prevention.getDIN()));
            imm.put("type", emptyIfNull(prevention.getImmunizationType()));
            imm.put("manufacturer", emptyIfNull(prevention.getManufacture()));
            imm.put("lotNumber", emptyIfNull(prevention.getLotNo()));

            String routeForDisplay = prevention.getRouteForDisplay();
            if (ll != null) {
                LookupListItem lli = lookupListItemDao.findByLookupListIdAndValue(ll.getId(), prevention.getRouteForDisplay());
                if (lli != null) {
                    routeForDisplay = lli.getLabel();
                }
            }
            imm.put("route", emptyIfNull(routeForDisplay));

            String siteForDisplay = prevention.getSite();
            if (llSite != null) {
                LookupListItem lliSite = lookupListItemDao.findByLookupListIdAndValue(llSite.getId(), prevention.getSite());
                if (lliSite != null) {
                    siteForDisplay = lliSite.getLabel();
                }
            }
            imm.put("site", emptyIfNull(siteForDisplay));
            imm.put("dose", emptyIfNull(prevention.getDose()));
            imm.put("date", emptyIfNull(sdf.format(prevention.getPreventionDate())));
            imm.put("refused", emptyIfNull(prevention.isRefused() ? "Yes" : "No"));
            imm.put("notes", emptyIfNull(prevention.getComment()));

            if ("-1".equals(prevention.getProviderNo())) {
                imm.put("preformer", emptyIfNull(prevention.getProviderName()));
            } else {
                Provider provider = providerDao.getProvider(prevention.getProviderNo());
                imm.put("preformer", emptyIfNull(provider.getFormattedName()));
            }

            imms.put(imm);
        }

        root.put("immunizations", imms);
        jsonResponse(root);
        return null;
    }

    /**
     * Stores the user's choice to hide the DHIR disclaimer for the current session.
     */
    private String hideDisclaimer() {
        request.getSession().setAttribute("dhir.disclaimer.hide", true);
        return null;
    }

    private Object emptyIfNull(Object o) {
        return o == null ? "" : o;
    }
}
