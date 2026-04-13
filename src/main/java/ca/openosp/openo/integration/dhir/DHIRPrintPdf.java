package ca.openosp.openo.integration.dhir;

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

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.oscarehr.common.dao.PreventionDao;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.Prevention;
import org.oscarehr.common.printing.FontSettings;
import org.oscarehr.common.printing.PdfWriterFactory;
import org.oscarehr.integration.TokenExpiredException;
import org.oscarehr.managers.DemographicManager;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DHIRPrintPdf {

  PreventionDao preventionDao = SpringUtils.getBean(PreventionDao.class);
  DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
  Logger logger = MiscUtils.getLogger();
  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

  private Document document;
  private final float LEADING = 12;

  private final Map<String, String> readableStatuses = new HashMap<String, String>();
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


  /** Creates a new instance of PreventionPrintPdf */
  public DHIRPrintPdf() {
    readableStatuses.put("0", "Completed or Normal");
    readableStatuses.put("1", "Refused");
    readableStatuses.put("2", "Ineligible");
  }

  public void printPdf(HttpServletRequest request, HttpServletResponse response)
      throws IOException, DocumentException {
    response.setContentType("application/pdf");  //octet-stream
    response.setHeader("Content-Disposition", "attachment; filename=\"DHIR.pdf\"");
    printPdf(request, response.getOutputStream());
  }

  public void printPdf(HttpServletRequest request, OutputStream outputStream)
      throws IOException, DocumentException {

    String demoNo = request.getParameter("demographicNo");
    Demographic demo = demographicManager.getDemographic(
        LoggedInInfo.getLoggedInInfoFromSession(request), demoNo);

    if (demo == null) {
      throw new DocumentException();
    }

    Date startDate, endDate, emrStartDate, emrEndDate;

    boolean includeEMR, includeDHIR, includeForecast;

    try {
      startDate = parseDate(request, "startDate");
      endDate = parseDate(request, "endDate");
      emrStartDate = parseDate(request, "emrStartDate");
      emrEndDate = parseDate(request, "emrEndDate");
    } catch (Exception e) {
      return;
    }

    includeEMR = getBoolean(request, "includeEMR");
    includeDHIR = getBoolean(request, "includeDHIR");
    includeForecast = getBoolean(request, "includeForecast");

    List<Prevention> preventions = null;

    if (includeEMR) {
      preventions = preventionDao.findActiveByDemoIdWithDates(demo.getDemographicNo(), emrStartDate,
          emrEndDate);
      for (Prevention p : preventions) {
        p.setPreventionExtendedProperties();
      }
    }

    Bundle bundle;
    List<ImmunizationHandler> handlers = new ArrayList<ImmunizationHandler>();
    SearchResultsHandler handler = null;
    List<String> searchParams = new ArrayList<String>();
    if (includeDHIR || includeForecast) {

      DHIRManager mgr = new DHIRManager();

      try {
        bundle = mgr.search(request, demo, startDate, endDate, searchParams);
      } catch (TokenExpiredException e) {
        logger.error("Error", e);
        return;
      } catch (ConsentBlockException e) {
        logger.error("Error", e);
        return;
      } catch (DHIRException e) {
        logger.error("Error", e);
        return;
      } catch (Exception e) {
        logger.error("Error", e);
        return;
      }
      handler = new SearchResultsHandler(bundle);
      for (Immunization immunization : handler.getImmunizationResources()) {
        handlers.add(new ImmunizationHandler(immunization));
      }
    }

    Collections.sort(handlers, ImmunizationHandler.DATE_SORT);
    Collections.reverse(handlers);
    document = new Document();
    document.setPageSize(PageSize.LETTER);

    PdfWriter writer = PdfWriterFactory.newInstance(document, outputStream,
        FontSettings.HELVETICA_10PT);

    HeaderFooter header = getHeaderFooter(demo);
    document.setHeader(header);

    document.open();

    Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    Font boldFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.BOLD, Color.BLACK);

    Paragraph disclaimerParagraph = new Paragraph();
    disclaimerParagraph.add(new Phrase("Warning:", boldFont));
    disclaimerParagraph.add(new Phrase(
        "Limited to Immunization Information available in the Digital Health Immunization Repository (DHIR) EHR service. To ensure a Best Possible Immunization History, please review this information with the patient/family and use other available sources of Immunization information in addition to the DHIR EHR service.",
        font));

    document.add(disclaimerParagraph);

    if (includeEMR) {

      Paragraph emrHeaderParagraph = new Paragraph("Immunization Event(s) in EMR",
          FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD | Font.UNDERLINE, Color.BLACK));
      emrHeaderParagraph.add(Chunk.NEWLINE);
      document.add(emrHeaderParagraph);

      Paragraph emrDateRangeParagraph = new Paragraph("Date Range: ",
          FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
      emrDateRangeParagraph.add(
          new Phrase(formatter.format(emrStartDate) + " to " + formatter.format(emrEndDate),
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
      emrDateRangeParagraph.add(Chunk.NEWLINE);

      document.add(emrDateRangeParagraph);

      document.add(Chunk.NEWLINE);

      if (preventions.size() > 0) {

        PdfPTable table = new PdfPTable(11);
        table.setWidthPercentage(100.0f);

        table.addCell(getHeaderCell("Name"));
        table.addCell(getHeaderCell("Code"));
        table.addCell(getHeaderCell("Type"));
        table.addCell(getHeaderCell("Manufacturer"));
        table.addCell(getHeaderCell("Lot #"));
        table.addCell(getHeaderCell("Route"));
        table.addCell(getHeaderCell("Site"));
        table.addCell(getHeaderCell("Dose"));
        table.addCell(getHeaderCell("Date"));
        table.addCell(getHeaderCell("Refused"));
        table.addCell(getHeaderCell("Notes"));

        table.setHeaderRows(1);

        for (Prevention prevention : preventions) {
          table.addCell(getItemCell(emptyIfNull(prevention.getName())));
          table.addCell(getItemCell(emptyIfNull(prevention.getDIN())));
          table.addCell(getItemCell(emptyIfNull(prevention.getImmunizationType())));
          table.addCell(getItemCell(emptyIfNull(prevention.getManufacture())));
          table.addCell(getItemCell(emptyIfNull(prevention.getLotNo())));
          table.addCell(getItemCell(emptyIfNull(prevention.getRouteForDisplay())));
          table.addCell(getItemCell(emptyIfNull(prevention.getSite())));
          table.addCell(getItemCell(emptyIfNull(prevention.getDose())));
          table.addCell(getItemCell(emptyIfNull(sdf.format(prevention.getPreventionDate()))));
          table.addCell(getItemCell(emptyIfNull((prevention.isRefused() ? "Yes" : "No"))));
          table.addCell(getItemCell(emptyIfNull(prevention.getComment())));

        }

        document.add(table);

        Paragraph numResults = new Paragraph("Found " + preventions.size() + " Events.",
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
        numResults.add(Chunk.NEWLINE);
        document.add(numResults);

      } else {
        Paragraph noResults = new Paragraph("No events found for the search time period.",
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
        noResults.add(Chunk.NEWLINE);
        document.add(noResults);
      }
    }

    if (includeDHIR) {
      document.add(new Phrase(Chunk.NEWLINE));
      document.add(new Phrase(Chunk.NEWLINE));

      Paragraph dhirHeaderParagraph = new Paragraph("Immunization Event(s) from DHIR",
          FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD | Font.UNDERLINE, Color.BLACK));
      dhirHeaderParagraph.add(Chunk.NEWLINE);
      document.add(dhirHeaderParagraph);

      Paragraph dhirDateRangeParagraph = new Paragraph("Date Range: ",
          FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
      dhirDateRangeParagraph.add(
          new Phrase(formatter.format(startDate) + " to " + formatter.format(endDate),
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
      dhirDateRangeParagraph.add(Chunk.NEWLINE);
      document.add(dhirDateRangeParagraph);

      document.add(Chunk.NEWLINE);

      if (handlers.size() > 0) {
        PdfPTable dhirTable = new PdfPTable(9);
        dhirTable.setWidthPercentage(100.0f);

        dhirTable.addCell(getHeaderCell("Date"));
        dhirTable.addCell(getHeaderCell("Valid Flag"));
        dhirTable.addCell(getHeaderCell("Agent"));
        dhirTable.addCell(getHeaderCell("Trade Name"));
        dhirTable.addCell(getHeaderCell("Lot #"));
        dhirTable.addCell(getHeaderCell("Expiration Date"));
        dhirTable.addCell(getHeaderCell("Status"));
        dhirTable.addCell(getHeaderCell("PHU"));
        dhirTable.addCell(getHeaderCell("Performer"));

        dhirTable.setHeaderRows(1);

        for (ImmunizationHandler iHandler : handlers) {
          dhirTable.addCell(getItemCell(emptyIfNull(iHandler.getImmunizationDate())));
          dhirTable.addCell(getItemCell(emptyIfNull(iHandler.getValidFlag())));
          dhirTable.addCell(getItemCell(emptyIfNull(iHandler.getAgent())));
          dhirTable.addCell(getItemCell(emptyIfNull(iHandler.getTradeName())));
          dhirTable.addCell(getItemCell(emptyIfNull(iHandler.getLotNumber())));
          dhirTable.addCell(getItemCell(emptyIfNull(iHandler.getExpirationDate())));
          dhirTable.addCell(getItemCell(emptyIfNull(iHandler.getStatus())));
          dhirTable.addCell(getItemCell(emptyIfNull(iHandler.getPHU())));
          dhirTable.addCell(
              getItemCell(emptyIfNull(iHandler.getPerformerName(handler.getAllResources()))));

        }
        document.add(dhirTable);

        Paragraph numResults = new Paragraph("Found " + handlers.size() + " Events.",
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
        numResults.add(Chunk.NEWLINE);
        document.add(numResults);

      } else {
        Paragraph noResults = new Paragraph("No events found for the search time period.",
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
        noResults.add(Chunk.NEWLINE);
        document.add(noResults);
      }
    }

    if (includeForecast) {
      //TODO: only if EMR or DHIR
      document.add(Chunk.NEWLINE);
      document.add(Chunk.NEWLINE);
      Paragraph forecastHeaderParagraph = new Paragraph(LEADING, "Immunization Forecast",
          FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD | Font.UNDERLINE, Color.BLACK));
      forecastHeaderParagraph.add(Chunk.NEWLINE);
      document.add(forecastHeaderParagraph);
      document.add(Chunk.NEWLINE);

      Map<String, Resource> map = handler.getAllResources();
      JSONArray recommendationsVaccine = new JSONArray();
      JSONArray recommendationsDisease = new JSONArray();
      JSONObject rec2 = new JSONObject();
      JSONObject rec3 = new JSONObject();

      for (Resource r : map.values()) {
        if (r.getResourceType() == ResourceType.ImmunizationRecommendation) {
          ImmunizationRecommendationsHandler irHandler = new ImmunizationRecommendationsHandler(
              (org.hl7.fhir.r4.model.ImmunizationRecommendation) r);

          String dateGenerated = sdf.format(irHandler.getDate());

          Map<String, List<JSONObject>> mapByStatusVaccine = new HashMap<String, List<JSONObject>>();
          mapByStatusVaccine.put("Overdue", new ArrayList<JSONObject>());
          mapByStatusVaccine.put("Up to date", new ArrayList<JSONObject>());
          mapByStatusVaccine.put("Due", new ArrayList<JSONObject>());
          mapByStatusVaccine.put("Eligible but not due", new ArrayList<JSONObject>());

          Map<String, List<JSONObject>> mapByStatusDisease = new HashMap<String, List<JSONObject>>();
          mapByStatusDisease.put("Overdue", new ArrayList<JSONObject>());
          mapByStatusDisease.put("Up to date", new ArrayList<JSONObject>());
          mapByStatusDisease.put("Due", new ArrayList<JSONObject>());
          mapByStatusDisease.put("Eligible but not due", new ArrayList<JSONObject>());

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

            if (vaccineCodes.size() > 0) {

              if (mapByStatusVaccine.get(c.getDisplay()) == null) {
                List<JSONObject> jList = new ArrayList<JSONObject>();
                jList.add(rec);
                mapByStatusVaccine.put(c.getDisplay(), jList);
              } else {
                List<JSONObject> jList = mapByStatusVaccine.get(c.getDisplay());
                jList.add(rec);
              }
              recommendationsVaccine.add(rec);
            } else {
              if (mapByStatusDisease.get(c.getDisplay()) == null) {
                List<JSONObject> jList = new ArrayList<JSONObject>();
                jList.add(rec);
                mapByStatusDisease.put(c.getDisplay(), jList);
              } else {
                List<JSONObject> jList = mapByStatusDisease.get(c.getDisplay());
                jList.add(rec);
              }
              recommendationsDisease.add(rec);
            }
          }

          for (String key : mapByStatusVaccine.keySet()) {
            JSONArray arr = new JSONArray();
            arr.addAll(mapByStatusVaccine.get(key));
            rec2.put(key, arr);
          }

          for (String key : mapByStatusDisease.keySet()) {
            JSONArray arr = new JSONArray();
            arr.addAll(mapByStatusDisease.get(key));
            rec3.put(key, arr);
          }

        }

      }

      Paragraph byVaccineHeaderParagraph = new Paragraph(LEADING, "By Vaccine",
          FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD | Font.ITALIC, Color.BLACK));
      byVaccineHeaderParagraph.add(Chunk.NEWLINE);
      document.add(byVaccineHeaderParagraph);
      document.add(Chunk.NEWLINE);

      PdfPTable forecastTableVaccine = new PdfPTable(4);
      forecastTableVaccine.setWidthPercentage(100.0f);

      forecastTableVaccine.addCell(getHeaderCell("Overdue"));
      forecastTableVaccine.addCell(getHeaderCell("Due"));
      forecastTableVaccine.addCell(getHeaderCell("Eligible but not due"));
      forecastTableVaccine.addCell(getHeaderCell("Up to date"));

      forecastTableVaccine.setHeaderRows(1);

      JSONArray a1 = rec2.getJSONArray("Overdue");
      JSONArray a2 = rec2.getJSONArray("Due");
      JSONArray a3 = rec2.getJSONArray("Eligible but not due");
      JSONArray a4 = rec2.getJSONArray("Up to date");

      int maxSize = NumberUtils.max(a1.size(), a2.size(), a3.size(), a4.size());

      for (int x = 0; x < maxSize; x++) {
        JSONObject j1, j2, j3, j4;

        j1 = getJSONObjectOrNull(a1, x);
        j2 = getJSONObjectOrNull(a2, x);
        j3 = getJSONObjectOrNull(a3, x);
        j4 = getJSONObjectOrNull(a4, x);

        forecastTableVaccine.addCell(getForecastItemCell(j1));
        forecastTableVaccine.addCell(getForecastItemCell(j2));
        forecastTableVaccine.addCell(getForecastItemCell(j3));
        forecastTableVaccine.addCell(getForecastItemCell(j4));

      }

      document.add(forecastTableVaccine);

      Paragraph byDiseaseHeaderParagraph = new Paragraph(LEADING, "By Disease",
          FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD | Font.ITALIC, Color.BLACK));
      byDiseaseHeaderParagraph.add(Chunk.NEWLINE);
      document.add(byDiseaseHeaderParagraph);
      document.add(Chunk.NEWLINE);

      PdfPTable forecastTableDisease = new PdfPTable(4);
      forecastTableDisease.setWidthPercentage(100.0f);

      forecastTableDisease.addCell(getHeaderCell("Overdue"));
      forecastTableDisease.addCell(getHeaderCell("Due"));
      forecastTableDisease.addCell(getHeaderCell("Eligible but not due"));
      forecastTableDisease.addCell(getHeaderCell("Up to date"));

      forecastTableDisease.setHeaderRows(1);

      JSONArray b1 = rec2.getJSONArray("Overdue");
      JSONArray b2 = rec2.getJSONArray("Due");
      JSONArray b3 = rec2.getJSONArray("Eligible but not due");
      JSONArray b4 = rec2.getJSONArray("Up to date");

      int maxSizeD = NumberUtils.max(a1.size(), a2.size(), a3.size(), a4.size());

      for (int x = 0; x < maxSizeD; x++) {
        JSONObject j1, j2, j3, j4;

        j1 = getJSONObjectOrNull(b1, x);
        j2 = getJSONObjectOrNull(b2, x);
        j3 = getJSONObjectOrNull(b3, x);
        j4 = getJSONObjectOrNull(b4, x);

        forecastTableVaccine.addCell(getForecastItemCell(j1));
        forecastTableVaccine.addCell(getForecastItemCell(j2));
        forecastTableVaccine.addCell(getForecastItemCell(j3));
        forecastTableVaccine.addCell(getForecastItemCell(j4));

      }

      document.add(forecastTableVaccine);

    }
    document.close();
  }


  private JSONObject getJSONObjectOrNull(JSONArray o, int idx) {
    JSONObject result = null;
    try {
      result = o.getJSONObject(idx);
    } catch (Exception e) {

    }
    return result;
  }

  private String emptyIfNull(String o) {
    if (o == null) {
      return "";
    }
    return o;
  }

  private String emptyIfNull(Boolean o) {
    if (o == null) {
      return "";
    }
    return String.valueOf(o);
  }

  private PdfPCell getHeaderCell(String name) {
    Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, Color.BLACK);

    PdfPCell cell = new PdfPCell(new Phrase(name, font));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

    return cell;
  }

  private PdfPCell getItemCell(String name) {
    Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.BLACK);

    PdfPCell cell = new PdfPCell(new Phrase(name, font));

    return cell;
  }

  private PdfPCell getForecastItemCell(JSONObject data) {
    Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    Font boldFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.BOLD, Color.BLACK);

    if (data == null) {
      return new PdfPCell();
    }

    PdfPCell cell = new PdfPCell();

    if (data.getString("targetDisease") != null && data.getString("targetDisease").length() > 0) {
      cell.addElement(new Phrase(data.getString("targetDisease"), boldFont));
    }
    if (data.getJSONArray("vaccineCodes") != null && data.getJSONArray("vaccineCodes").size() > 0) {
      cell.addElement(
          new Phrase(((JSONObject) data.getJSONArray("vaccineCodes").get(0)).getString("display"),
              boldFont));
    }
    if (data.getJSONArray("vaccineCodes") != null && data.getJSONArray("vaccineCodes").size() > 1) {
      cell.addElement(
          new Phrase(((JSONObject) data.getJSONArray("vaccineCodes").get(1)).getString("display"),
              boldFont));
    }

    if (data.getString("date") != null) {
      cell.addElement(new Phrase("\n" + data.getString("date"), font));

    }

    return cell;
  }

  private Date parseDate(HttpServletRequest request, String fieldName) throws Exception {
    Date result = null;
    String d = request.getParameter(fieldName);
    if (StringUtils.isEmpty(d)) {
      return null;
    }
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    try {
      result = formatter.parse(d);
    } catch (ParseException e) {
      throw new Exception("invalid parameter sent : " + fieldName + " = " + d);
    }
    return result;
  }

  private String getDemoInfo(Demographic demo) {
    StringBuilder demoInfo = new StringBuilder(demo.getSexDesc()).append(" Age: ")
        .append(demo.getAge()).append(" (").append(demo.getBirthDayAsString()).append(")")
        .append(" HIN: (").append(demo.getHcType()).append(") ").append(demo.getHin()).append(" ")
        .append(demo.getVer());
    return demoInfo.toString();

  }

  private Phrase getTitlePhrase(Demographic demo) {
    Phrase titlePhrase = new Phrase(16, "EMR / DHIR Summary",
        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD, Color.BLACK));
    titlePhrase.add(Chunk.NEWLINE);
    titlePhrase.add(new Chunk(demo.getFormattedName(),
        FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, Color.BLACK)));
    titlePhrase.add(Chunk.NEWLINE);
    titlePhrase.add(new Chunk(getDemoInfo(demo),
        FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.BLACK)));

    return titlePhrase;
  }

  private HeaderFooter getHeaderFooter(Demographic demo) {
    HeaderFooter header = new HeaderFooter(getTitlePhrase(demo), false);
    header.setAlignment(HeaderFooter.ALIGN_RIGHT);
    header.setBorder(Rectangle.BOTTOM);

    return header;
  }

  private boolean getBoolean(HttpServletRequest request, String name) {
    String str = request.getParameter(name);

    if (StringUtils.isEmpty(str)) {
      return false;
    }

    try {
      return Boolean.valueOf(str);
    } catch (Exception e) {
    }

    return false;

  }
}
