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

import ca.uhn.fhir.context.FhirContext;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.common.dao.DemographicDao;
import org.oscarehr.common.dao.LookupListDao;
import org.oscarehr.common.dao.LookupListItemDao;
import org.oscarehr.common.dao.PreventionDao;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.LookupList;
import org.oscarehr.common.model.LookupListItem;
import org.oscarehr.common.model.Prevention;
import org.oscarehr.common.model.Provider;
import org.oscarehr.integration.TokenExpiredException;
import org.oscarehr.integration.fhir.r4.model.Patient;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DHIRSummaryViewAction extends DispatchAction {

  Logger logger = MiscUtils.getLogger();
  DemographicDao demographicDao = SpringUtils.getBean(DemographicDao.class);

  public ActionForward unspecified(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    List<ImmunizationHandler> handlers = new ArrayList<ImmunizationHandler>();

    String demographicNo = request.getParameter("demographic_no");
    String startDateStr = request.getParameter("startDate");
    String endDateStr = request.getParameter("endDate");

    Demographic demographic = demographicDao.getDemographic(demographicNo);

    if (StringUtils.isEmpty(demographic.getHin())) {
      JSONObject root = new JSONObject();
      root.put("error", "No Health Card Number");
      root.write(response.getWriter());
      return null;
    }
    if (StringUtils.isEmpty(demographic.getFormattedDob())) {
      JSONObject root = new JSONObject();
      root.put("error", "No valid Date of Birth");
      root.write(response.getWriter());
      return null;
    }

    Date startDate = null;
    Date endDate = null;
    SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy-MM-dd");
    try {
      startDate = fmt1.parse(startDateStr);
    } catch (ParseException e) {
    }
    try {
      endDate = fmt1.parse(endDateStr);
    } catch (ParseException e) {
    }

    DHIRManager mgr = new DHIRManager();
    Bundle bundle = null;
    List<String> searchParamsUsed = new ArrayList<String>();
    try {

      bundle = mgr.search(request, demographic, startDate, endDate, searchParamsUsed);
    } catch (TokenExpiredException e) {
      JSONObject root = new JSONObject();
      root.put("error", e.getMessage());
      root.put("requireNewToken", true);
      root.write(response.getWriter());
      return null;
    } catch (ConsentBlockException e) {
      JSONObject root = new JSONObject();
      root.put("error", e.getMessage());
      root.write(response.getWriter());
      return null;
    } catch (DHIRException e) {
      JSONObject root = new JSONObject();
      root.put("error", e.getMessage());
      root.write(response.getWriter());
      return null;
    } catch (Exception e) {
      logger.error("Error", e);
      JSONObject root = new JSONObject();
      root.put("error", e.getMessage());
      root.write(response.getWriter());
      return null;
    }

    if (bundle == null) {
      logger.debug("null bundle");
      JSONObject root = new JSONObject();
      root.put("error", "An error occured retrieving the data");
      root.write(response.getWriter());
      return null;
    }

    logger.info(FhirContext.forR4().newJsonParser().encodeResourceToString(bundle));

    SearchResultsHandler handler = new SearchResultsHandler(bundle);

    for (Immunization immunization : handler.getImmunizationResources()) {
      handlers.add(new ImmunizationHandler(immunization));
    }
    request.setAttribute("handlers", handlers);

    //convert to JSON
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

      imms.add(imm);
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

        Map<String, List<JSONObject>> mapByStatus = new HashMap<String, List<JSONObject>>();
        mapByStatus.put("Overdue", new ArrayList<JSONObject>());
        mapByStatus.put("Up to date", new ArrayList<JSONObject>());
        mapByStatus.put("Due", new ArrayList<JSONObject>());
        mapByStatus.put("Eligible but not due", new ArrayList<JSONObject>());

        for (ImmunizationRecommendation ir : irHandler.getRecs()) {
          JSONObject rec = new JSONObject();

          JSONArray vaccineCodes = new JSONArray();
          for (Coding c : ir.getCodes()) {
            JSONObject v = new JSONObject();
            v.put("system", c.getSystem());
            v.put("code", c.getCode());
            v.put("display", c.getDisplay());
            vaccineCodes.add(v);
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

          if (mapByStatus.get(c.getDisplay()) == null) {
            List<JSONObject> jList = new ArrayList<JSONObject>();
            jList.add(rec);
            mapByStatus.put(c.getDisplay(), jList);
          } else {
            List<JSONObject> jList = mapByStatus.get(c.getDisplay());
            jList.add(rec);
          }
          recommendations.add(rec);
        }

        root.put("recommendations", recommendations);

        JSONObject rec2 = new JSONObject();
        for (String key : mapByStatus.keySet()) {
          JSONArray arr = new JSONArray();
          arr.addAll(mapByStatus.get(key));
          rec2.put(key, arr);
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

    root.write(response.getWriter());

    return null;

  }

  public ActionForward emrData(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    PreventionDao preventionDao = SpringUtils.getBean(PreventionDao.class);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    ProviderDao providerDao = SpringUtils.getBean(ProviderDao.class);

    String startDateStr = request.getParameter("startDate");
    String endDateStr = request.getParameter("endDate");

    Date startDate = null;
    Date endDate = null;
    SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy-MM-dd");
    try {
      startDate = fmt1.parse(startDateStr);
    } catch (ParseException e) {
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
    }

    List<Prevention> preventions = preventionDao.findActiveByDemoIdWithDates(
        Integer.parseInt(request.getParameter("demographic_no")), startDate, endDate);

    JSONObject root = new JSONObject();
    JSONArray imms = new JSONArray();

    LookupListDao lookupListDao = SpringUtils.getBean(LookupListDao.class);
    LookupListItemDao lookupListItemDao = SpringUtils.getBean(LookupListItemDao.class);
    LookupList ll = lookupListDao.findByName("RouteOfAdmin");
    LookupListItem lli = null;

    for (Prevention prevention : preventions) {
      prevention.setPreventionExtendedProperties();
      //name, code, type, manufacturer, lot#, route, site, dose, date, refused, notes
      JSONObject imm = new JSONObject();
      imm.put("name", emptyIfNull(prevention.getName()));
      imm.put("code", emptyIfNull(prevention.getDIN()));
      imm.put("type", emptyIfNull(prevention.getImmunizationType()));
      imm.put("manufacturer", emptyIfNull(prevention.getManufacture()));
      imm.put("lotNumber", emptyIfNull(prevention.getLotNo()));

      String routeForDisplay = prevention.getRouteForDisplay();
      if (ll != null) {
        lli = lookupListItemDao.findByLookupListIdAndValue(ll.getId(),
            prevention.getRouteForDisplay());
        if (lli != null) {
          routeForDisplay = lli.getLabel();
        }
      }
      imm.put("route", emptyIfNull(routeForDisplay));

      String siteForDisplay = prevention.getSite();
      LookupList llSite = lookupListDao.findByName("AnatomicalSite");
      LookupListItem lliSite = null;
      if (llSite != null) {
        lliSite = lookupListItemDao.findByLookupListIdAndValue(llSite.getId(),
            prevention.getSite());
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

      imms.add(imm);
    }

    root.put("immunizations", imms);
    root.write(response.getWriter());

    return null;
  }


  public ActionForward hideDisclaimer(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) {
    request.getSession().setAttribute("dhir.disclaimer.hide", true);
    return null;
  }

  private Object emptyIfNull(Object o) {
    if (o == null) {
      return "";
    }
    return o;
  }
}
