package ca.openosp.openo.integration.dhdr;
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * <p>This software was written for the Department of Family Medicine McMaster University Hamilton
 * Ontario, Canada
 */

import ca.openosp.OscarProperties;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.printing.FontSettings;
import ca.openosp.openo.commn.printing.PdfWriterFactory;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;
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
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.awt.*;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DHDRPrint {

  /** The DHDR disclaimer (DHDR03.03), printed on each page per DHDR13.01.g. */
  private static final String DHDR_DISCLAIMER =
      "Warning: Limited to Drug and Pharmacy Service Information available in the Digital Health Drug"
          + " Repository (DHDR) EHR Service. To ensure a Best Possible Medication History (BPMH),"
          + " please review this information with the patient/family and use other available sources"
          + " of medication information in addition to the DHDR EHR Service.";

  DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd  'at' HH:mm:ss z");

  public void printDetail(
      LoggedInInfo loggedInInfo,
      Integer demographicNo,
      OutputStream outputStream,
      JSONObject jsonOb)
      throws Exception {
    Document document;
    PdfContentByte contentByte;

    Demographic demo = demographicManager.getDemographic(loggedInInfo, demographicNo);

    if (demo == null) throw new DocumentException();

    document = new Document();
    document.setPageSize(PageSize.LETTER);
    document.setMargins(36, 36, 90, 140);

    PdfWriter writer = PdfWriterFactory.newInstance(document, outputStream,
        FontSettings.HELVETICA_10PT);
    writer.setPageEvent(new DhdrFooterEvent(DHDR_DISCLAIMER, resolveConfidentialityStatement(), buildPrintedInfo(loggedInInfo)));

    String dhdrDemoLine = buildDhdrDemoLine(jsonOb.optJSONObject("dhdrPatient"));
    HeaderFooter header = getHeaderFooter(demo, "DHDR Detailed", dhdrDemoLine);
    document.setHeader(header);

    document.open();
    contentByte = writer.getDirectContent();

    Paragraph emrHeaderParagraph =
        new Paragraph(
            "DHDR Detailed",
            FontFactory.getFont(
                FontFactory.HELVETICA, 12, Font.BOLD | Font.UNDERLINE, Color.BLACK));
    emrHeaderParagraph.add(Chunk.NEWLINE);
    document.add(emrHeaderParagraph);

    document.add(Chunk.NEWLINE);

    /////// table
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100.0f);

    JSONObject med = jsonOb.optJSONObject("med");

    if (med != null) {

      table.addCell(getHeaderCell("Dispense Date"));
      table.addCell(getHeaderCell(med.optString("whenPrepared"))); // Dispense Date

      table.setHeaderRows(1);

      table.addCell(getHeaderCell("Generic"));
      table.addCell(getItemCell(med.optString("genericName"))); // Generic

      JSONObject brandObj = med.optJSONObject("brandName");
      table.addCell(getHeaderCell("Brand"));
      table.addCell(getItemCell(brandObj != null ? brandObj.optString("display") : "")); // Brand

      table.addCell(getHeaderCell("DIN/PIN"));
      table.addCell(getItemCell(brandObj != null ? brandObj.optString("code") : "")); // DIN/PIN

      // The parse stores these as plain display strings, not objects - read them as strings, as
      // populateSummaryDrugData does.
      String ahfsClass = med.optString("ahfsClass");
      if (!ahfsClass.isEmpty()) {
        table.addCell(getHeaderCell("Therapeutic Class"));
        table.addCell(getItemCell(ahfsClass));
      }

      String ahfsSubClass = med.optString("ahfsSubClass");
      if (!ahfsSubClass.isEmpty()) {
        table.addCell(getHeaderCell("Therapeutic Sub-Class"));
        table.addCell(getItemCell(ahfsSubClass));
      }

      table.addCell(getHeaderCell("Rx Number"));
      table.addCell(getItemCell(med.optString("rxNumber"))); // Brand

      table.addCell(getHeaderCell("Medical Condition/Reason for Use"));

      StringBuilder reasonCodesStr = new StringBuilder();
      JSONArray reasonCodes = med.optJSONArray("reasonCode");
      if (reasonCodes != null) {
        for (int i = 0; i < reasonCodes.length(); i++) {
          JSONObject jsonObject = reasonCodes.optJSONObject(i);
          if (jsonObject == null) {
            continue;
          }
          if (reasonCodesStr.length() > 0) {
            reasonCodesStr.append("; ");
          }
          reasonCodesStr.append(jsonObject.opt("code") + " -- " + jsonObject.opt("display"));
        }
      }
      table.addCell(getItemCell(reasonCodesStr.toString()));

      table.addCell(getHeaderCell("Strength"));
      table.addCell(getItemCell(med.optString("dispensedDrugStrength")));
      table.addCell(getHeaderCell("Dosage Form"));
      table.addCell(getItemCell(med.optString("drugDosageForm")));
      table.addCell(getHeaderCell("Dosage"));
      table.addCell(getItemCell(med.optString("dose")));
      table.addCell(getHeaderCell("Frequency"));
      table.addCell(getItemCell(med.optString("frequency")));
      table.addCell(getHeaderCell("Quantity"));
      table.addCell(
          getItemCell(
              med.optString("dispensedQuantity") + " " + med.optString("dispensedQuantityUnit")));
      table.addCell(getHeaderCell("Est Days Supply"));
      table.addCell(getItemCell(med.optString("estimatedDaysSupply")));

      table.addCell(getHeaderCell("Refills Remaining"));
      table.addCell(getItemCell(med.optString("refillsRemaining")));
      table.addCell(getHeaderCell("Quantity Remaining"));
      table.addCell(getItemCell(med.optString("quantityRemaining")));

      JSONObject prescriberLicenceNumberObj = med.optJSONObject("prescriberLicenceNumber");
      table.addCell(getHeaderCell("Prescriber"));
      table.addCell(
          getItemCell(
              med.optString("prescriberLastname")
                  + ", "
                  + med.optString("prescriberFirstname")));

      if (prescriberLicenceNumberObj != null && prescriberLicenceNumberObj.length() > 0) {
        // Licensing body by name, not the raw identifier system URI. The body is empty for an
        // unrecognised system, so join conditionally rather than leaving a leading space.
        String licenceBody = licenceBody(prescriberLicenceNumberObj.optString("system"));
        String licenceValue = prescriberLicenceNumberObj.optString("value");
        table.addCell(getHeaderCell("Prescriber ID"));
        table.addCell(
            getItemCell(licenceBody.isEmpty() ? licenceValue : (licenceBody + " " + licenceValue)));
      }

      table.addCell(getHeaderCell("Prescriber #"));
      table.addCell(getItemCell(med.optString("prescriberPhoneNumber")));
      table.addCell(getHeaderCell("Pharmacy"));
      table.addCell(getItemCell(med.optString("dispensingPharmacy")));
      table.addCell(getHeaderCell("Pharmacy Fax"));
      table.addCell(getItemCell(med.optString("dispensingPharmacyFaxNumber")));

      table.addCell(getHeaderCell("Pharmacy Phone"));
      table.addCell(getItemCell(med.optString("dispensingPharmacyPhoneNumber")));

      table.addCell(getHeaderCell("Pharmacist"));
      JSONObject pharmacistLicenceNumber = med.optJSONObject("pharmacistLicenceNumber");
      String pharmacistLicenceValue =
          pharmacistLicenceNumber != null ? pharmacistLicenceNumber.optString("value") : "";
      // Show the licence when there is one; the test was inverted, so it printed " ()" for a
      // missing licence and hid a real one.
      table.addCell(
          getItemCell(
              med.optString("pharmacistLastname")
                  + ", "
                  + med.optString("pharmacistFirstname")
                  + (pharmacistLicenceValue.isEmpty() ? "" : (" (" + pharmacistLicenceValue + ")"))));

      document.add(table);
    } else {
      Paragraph noResults =
          new Paragraph(
              "No Med Found.",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
      noResults.add(Chunk.NEWLINE);
      document.add(noResults);
    }

    document.close();
  }

  public void printSummary(
      LoggedInInfo loggedInInfo,
      Integer demographicNo,
      OutputStream outputStream,
      JSONObject jsonOb)
      throws Exception {

    Document document;
    PdfContentByte contentByte;

    Demographic demo = demographicManager.getDemographic(loggedInInfo, demographicNo);

    if (demo == null) throw new DocumentException();

    document = new Document();
    document.setPageSize(PageSize.LETTER.rotate());
    document.setMargins(36, 36, 90, 140);

    PdfWriter writer = PdfWriterFactory.newInstance(document, outputStream, FontSettings.HELVETICA_10PT);
    writer.setPageEvent(new DhdrFooterEvent(DHDR_DISCLAIMER, resolveConfidentialityStatement(), buildPrintedInfo(loggedInInfo)));

    String dhdrDemoLine = buildDhdrDemoLine(jsonOb.optJSONObject("dhdrPatient"));
    HeaderFooter header = getHeaderFooter(demo, "DHDR Summary", dhdrDemoLine);
    document.setHeader(header);

    document.open();
    contentByte = writer.getDirectContent();

    Paragraph emrHeaderParagraph =
        new Paragraph(
            "DHDR Summary",
            FontFactory.getFont(
                FontFactory.HELVETICA, 12, Font.BOLD | Font.UNDERLINE, Color.BLACK));
    emrHeaderParagraph.add(Chunk.NEWLINE);
    document.add(emrHeaderParagraph);
    // formatter.format(
    Paragraph emrDateRangeParagraph =
        new Paragraph(
            "Date Range: ", FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
    emrDateRangeParagraph.add(
        new Phrase(
            jsonOb.get("startDate") + " to " + jsonOb.get("endDate"),
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
    emrDateRangeParagraph.add(Chunk.NEWLINE);

    document.add(emrDateRangeParagraph);

    document.add(Chunk.NEWLINE);

    JSONArray arr = sortByWhenPreparedDesc(jsonOb.getJSONArray("meds"));
    Paragraph drugProductParagraph =
        new Paragraph(
            "Drug Product", FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
    drugProductParagraph.add(
        new Phrase(
            "(Found " + arr.length() + " Events)",
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
    drugProductParagraph.add(Chunk.NEWLINE);
    drugProductParagraph.setSpacingAfter(5f);
    document.add(drugProductParagraph);

    /////// table
    if (arr.length() > 0) {

      for (var i = 0; i < arr.length(); i++) {
        JSONObject med = arr.getJSONObject(i);
        PdfPTable table = new PdfPTable(new float[] {3, 3, 3, 1.5f, 1.5f, 1, 1});
        table.setSpacingAfter(10f);
        table.setWidthPercentage(100.0f);
        populateSummaryDrugMetaData(table, med);
        populateSummaryDrugHeader(table);
        populateSummaryDrugData(med, table);
        document.add(table);
      }
    } else {
      Paragraph noResults =
          new Paragraph(
              "No events found for the search time period.",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
      noResults.add(Chunk.NEWLINE);
      document.add(noResults);
    }
    if (jsonOb.has("services")) {
      JSONArray serviceArr = sortByWhenPreparedDesc(jsonOb.getJSONArray("services"));

      document.add(Chunk.NEWLINE);

      Paragraph servicesProductParagraph =
          new Paragraph(
              "Pharma Services",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
      servicesProductParagraph.add(
          new Phrase(
              "(Found " + serviceArr.length() + " Events)",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
      servicesProductParagraph.add(Chunk.NEWLINE);
      servicesProductParagraph.setSpacingAfter(5f);
      document.add(servicesProductParagraph);

      PdfPTable serviceTable = new PdfPTable(9);
      serviceTable.setWidthPercentage(100.0f);

      if (serviceArr.length() > 0) {

        serviceTable.addCell(getHeaderCell("Last Service Date"));
        serviceTable.addCell(getHeaderCell("Pickup Date"));
        serviceTable.addCell(getHeaderCell("Pharmacy Service Type"));
        serviceTable.addCell(getHeaderCell("Pharmacy Service Description"));
        serviceTable.addCell(getHeaderCell("Rx Number"));
        serviceTable.addCell(getHeaderCell("Therapeutic Class/Sub-class"));
        serviceTable.addCell(getHeaderCell("Pharmacy Name"));
        serviceTable.addCell(getHeaderCell("Pharmacist"));
        serviceTable.addCell(getHeaderCell("Pharmacy Fax"));
        serviceTable.setHeaderRows(1);

        for (int i = 0; i < serviceArr.length(); i++) {
          JSONObject med = serviceArr.getJSONObject(i);

          serviceTable.addCell(getItemCell(med.optString("whenPrepared"))); // Last Service Date
          serviceTable.addCell(getItemCell(med.optString("whenHandedOver"))); // Pickup Date
          JSONObject brandObj = med.optJSONObject("brandName");
          serviceTable.addCell(getItemCell(serviceTypeWithPin(brandObj))); // Service Type (+ PIN)
          serviceTable.addCell(getItemCell(med.optString("genericName")));
          serviceTable.addCell(getItemCell(med.optString("rxNumber")));
          serviceTable.addCell(
              getItemCell(med.optString("ahfsClass") + "/" + med.optString("ahfsSubClass")));
          serviceTable.addCell(getItemCell(med.optString("dispensingPharmacy")));
          JSONObject pharmacistLicenceNumberObj = med.optJSONObject("pharmacistLicenceNumber");
          String pharmacistLicenceValue =
              pharmacistLicenceNumberObj != null
                  ? pharmacistLicenceNumberObj.optString("value")
                  : "";
          serviceTable.addCell(
              getItemCell(
                  med.optString("pharmacistLastname")
                      + ", "
                      + med.optString("pharmacistFirstname")
                      + " ("
                      + pharmacistLicenceValue
                      + ")"));
          serviceTable.addCell(
              getItemCell(med.optString("dispensingPharmacyFaxNumber")));
        }

        document.add(serviceTable);
      } else {
        Paragraph noResults =
            new Paragraph(
                "No events found for the search time period.",
                FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
        noResults.add(Chunk.NEWLINE);
        document.add(noResults);
      }
    }

    document.close();
  }

  private PdfPCell populateSummaryDrugMetaData(PdfPTable table, JSONObject med) throws JSONException {
    PdfPTable headerTable = new PdfPTable(new float[] {1f, 3f, 1.5f, 3f, 1.5f, 2f});
    PdfPCell header = new PdfPCell(headerTable);
    header.setColspan(12);
    table.addCell(header);

    String dispenseDate = med.optString("whenPrepared");
    String pickupDate = med.optString("pickUpDate");
    JSONObject patient = med.optJSONObject("patient");
    String patientDob = patient != null ? patient.optString("birthDate") : "N/A";
    String patientGender = patient != null ? patient.optString("gender") : "N/A";
    String patientHcn = "N/A";
    if (patient != null
        && patient.has("identifier")
        && patient.getJSONArray("identifier").length() > 0
        && patient.getJSONArray("identifier").getJSONObject(0).has("value")) {
      patientHcn = patient.getJSONArray("identifier").getJSONObject(0).getString("value");
    }
    String patientFirstName = "N/A";
    String patientLastName = "N/A";
    if (patient != null && patient.has("name") && patient.getJSONArray("name").length() > 0) {
      JSONObject name = patient.getJSONArray("name").getJSONObject(0);
      if (name.has("given") && name.getJSONArray("given").length() > 0) {
        patientFirstName = name.getJSONArray("given").getString(0);
      }
      patientLastName = name.optString("family");
    }

    JSONObject prescriberLicenceNumberObj = med.optJSONObject("prescriberLicenceNumber");
    String prescriberLicenceValue =
        prescriberLicenceNumberObj != null ? prescriberLicenceNumberObj.optString("value") : "";
    // Show the licence when there is one; the test was inverted, so it printed " ()" for a
    // missing licence and hid a real one.
    String prescriberLicenseNumber =
        prescriberLicenceValue.isEmpty() ? "" : " (" + prescriberLicenceValue + ")";

    String prescriberName =
        med.optString("prescriberLastname")
            + (med.optString("prescriberLastname").length() > 0 ? ", " : "")
            + med.optString("prescriberFirstname")
            + prescriberLicenseNumber;
    // Only when no prescriber name resolved at all - this guard must not be inverted.
    if (prescriberName.trim().isEmpty()) {
      prescriberName = "N/A";
    }

    // Row 1
    getSummaryItemHeaderCell(headerTable, "First Name", patientFirstName);
    getSummaryItemHeaderCell(headerTable, "Prescriber", prescriberName);
    getSummaryItemHeaderCell(headerTable, "Dispense Date", dispenseDate);

    // Row 2
    getSummaryItemHeaderCell(headerTable, "Last Name", patientLastName);
    getSummaryItemHeaderCell(
        headerTable, "Prescriber #", med.optString("prescriberPhoneNumber", "N/A"));
    getSummaryItemHeaderCell(headerTable, "Pickup Date", pickupDate);

    // Row 3
    getSummaryItemHeaderCell(headerTable, "Gender", patientGender);
    getSummaryItemHeaderCell(headerTable, "Pharmacy", med.optString("dispensingPharmacy", "N/A"));
    headerTable.addCell(getSpacerCell(2));

    // Row 4
    getSummaryItemHeaderCell(headerTable, "DOB", patientDob);
    getSummaryItemHeaderCell(
        headerTable, "Pharmacy Fax", med.optString("dispensingPharmacyFaxNumber", "N/A"));
    headerTable.addCell(getSpacerCell(2));

    // Row 5
    getSummaryItemHeaderCell(headerTable, "HIN", patientHcn);
    headerTable.addCell(getSpacerCell(4));

    return header;
  }

  private void populateSummaryDrugHeader(PdfPTable table) {
    table.addCell(getHeaderCell("Generic"));
    table.addCell(getHeaderCell("Brand"));
    table.addCell(getHeaderCell("Therapeutic Class/Sub-class"));
    table.addCell(getHeaderCell("Dosage"));
    table.addCell(getHeaderCell("Frequency"));
    table.addCell(getHeaderCell("Quantity"));
    table.addCell(
        getHeaderCell("Supply / Refills / Qty Remaining"));
  }

  private void populateSummaryDrugData(JSONObject med, PdfPTable table) throws JSONException {
    table.addCell(getItemCell(med.optString("genericName")));
    JSONObject brandObj = med.optJSONObject("brandName");
    String brandDisplay = brandObj != null ? brandObj.optString("display") : "";
    table.addCell(
        getItemCell(
            brandDisplay
                + " "
                + med.optString("drugDosageForm")
                + " "
                + med.optString("dispensedDrugStrength")));
    table.addCell(getItemCell(med.optString("ahfsClass") + "/" + med.optString("ahfsSubClass")));
    table.addCell(getItemCell(med.optString("dose") + " " + med.optString("doseUnit")));
    table.addCell(
        getItemCell(
            med.optString("frequency")
                + " every "
                + med.optString("period")
                + " - "
                + med.optString("periodMax")
                + " "
                + med.optString("periodUnit")));
    table.addCell(
        getItemCell(
            med.optString("dispensedQuantity")
                + " "
                + med.optString("dispensedQuantityUnit")));
    table.addCell(
        getItemCell(
            "Est Days Supply:"
                + med.optString("estimatedDaysSupply")
                + " Refills Remaining: "
                + med.optString("refillsRemaining")
                + " Quantity Remaining: "
                + med.optString("quantityRemaining")));
  }

  private PdfPCell getSpacerCell(int i) {
    PdfPCell cell = new PdfPCell();
    cell.setBorderWidth(0f);
    cell.setColspan(i);
    return cell;
  }

  private void getSummaryItemHeaderCell(PdfPTable table, String header, String value) {
    PdfPCell headerCell = getHeaderCell(header);
    headerCell.setBorderWidth(0f);
    headerCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
    table.addCell(headerCell);
    PdfPCell dispenseDateValue = getItemCell(value);
    dispenseDateValue.setBorderWidth(0f);
    table.addCell(dispenseDateValue);
  }

  private PdfPCell getHeaderCell(String name) {
    Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.BOLD, Color.BLACK);

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

  /**
   * Maps a prescriber/pharmacist licence identifier {@code system} URI to the full licensing-body
   * name, mirroring the viewer's {@code getLicence} mapping.
   *
   * @param system String the FHIR identifier system URI, or null/empty
   * @return String the licensing-body name, or an empty string when the system is unknown/absent
   */
  private String licenceBody(String system) {
    if (system == null || system.isEmpty()) {
      return "";
    }
    if (system.endsWith("ca-on-license-physician"))
      return "College of Physicians and Surgeons of Ontario";
    if (system.endsWith("ca-on-license-dental-surgeon"))
      return "Royal College of Dental Surgeons of Ontario";
    if (system.endsWith("ca-out-of-province -prescriber")) return "Out-of-Province Prescriber";
    if (system.endsWith("ca-on-license-chiropodist")) return "College of Chiropodists of Ontario";
    if (system.endsWith("ca-on-license-midwife")) return "College of Midwives of Ontario";
    if (system.endsWith("ca-on-license-pharmacist")) return "Ontario College of Pharmacists";
    if (system.endsWith("ca-on-license-optometrist")) return "College of Optometrists of Ontario";
    if (system.endsWith("ca-on-license-nurse")) return "College of Nurses of Ontario";
    if (system.endsWith("ca-on-license-naturopath")) return "College of Naturopaths of Ontario";
    if (system.endsWith("ca-on-unknown-prescriber")) return "Unknown Prescriber";
    return "";
  }

  /**
   * Renders a pharmacy-service type cell, appending the product identifier (PIN) when present.
   * Null-safe on an absent {@code brandName}.
   *
   * @param brandObj JSONObject the parsed brandName object, or null
   * @return String the service-type display with the PIN appended when available
   */
  private String serviceTypeWithPin(JSONObject brandObj) {
    if (brandObj == null) {
      return "";
    }
    String display = brandObj.optString("display");
    String pin = brandObj.optString("code");
    if (pin.isEmpty()) {
      return display;
    }
    return display.isEmpty() ? ("PIN: " + pin) : (display + " (PIN: " + pin + ")");
  }

  private String getDemoInfo(Demographic demo) {
    StringBuilder demoInfo =
        new StringBuilder(demo.getSexDesc())
            .append(" Age: ")
            .append(demo.getAge())
            .append(" (")
            .append(demo.getBirthDayAsString())
            .append(")")
            .append(" HIN: (")
            .append(demo.getHcType())
            .append(") ")
            .append(demo.getHin())
            .append(" ")
            .append(demo.getVer());
    return demoInfo.toString();
  }

  private Phrase getTitlePhrase(Demographic demo, String title, String dhdrDemoLine) {
    Phrase titlePhrase =
        new Phrase(
            16, title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD, Color.BLACK));
    titlePhrase.add(Chunk.NEWLINE);
    titlePhrase.add(
        new Chunk(
            demo.getFormattedName(),
            FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, Color.BLACK)));
    titlePhrase.add(Chunk.NEWLINE);
    // The EMR-side demographic (DHDR13.01.a).
    titlePhrase.add(
        new Chunk(
            getDemoInfo(demo),
            FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.BLACK)));

    // The DHDR-side demographic (DHDR13.01.b); omitted when no DHDR patient was resolved.
    if (dhdrDemoLine != null && !dhdrDemoLine.isEmpty()) {
      titlePhrase.add(Chunk.NEWLINE);
      titlePhrase.add(
          new Chunk(
              dhdrDemoLine, FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.BLACK)));
    }

    return titlePhrase;
  }

  private HeaderFooter getHeaderFooter(Demographic demo, String title, String dhdrDemoLine) {
    HeaderFooter header = new HeaderFooter(getTitlePhrase(demo, title, dhdrDemoLine), false);
    header.setAlignment(HeaderFooter.ALIGN_RIGHT);
    header.setBorder(Rectangle.BOTTOM);

    return header;
  }

  public void printComparative(
      LoggedInInfo loggedInInfo,
      Integer demographicNo,
      OutputStream outputStream,
      JSONObject jsonOb)
      throws Exception {

    Document document;

    Demographic demo = demographicManager.getDemographic(loggedInInfo, demographicNo);

    if (demo == null) throw new DocumentException();

    document = new Document();
    document.setPageSize(PageSize.LETTER.rotate());
    document.setMargins(36, 36, 90, 140);

    PdfWriter writer =
        PdfWriterFactory.newInstance(document, outputStream, FontSettings.HELVETICA_10PT);
    writer.setPageEvent(new DhdrFooterEvent(DHDR_DISCLAIMER, resolveConfidentialityStatement(), buildPrintedInfo(loggedInInfo)));

    String dhdrDemoLine = buildDhdrDemoLine(jsonOb.optJSONObject("dhdrPatient"));
    HeaderFooter header = getHeaderFooter(demo, "DHDR Comparative", dhdrDemoLine);
    document.setHeader(header);

    document.open();

    Paragraph emrHeaderParagraph =
        new Paragraph(
            "DHDR Comparative",
            FontFactory.getFont(
                FontFactory.HELVETICA, 12, Font.BOLD | Font.UNDERLINE, Color.BLACK));
    emrHeaderParagraph.add(Chunk.NEWLINE);
    document.add(emrHeaderParagraph);
    // formatter.format(
    Paragraph emrDateRangeParagraph =
        new Paragraph(
            "Date Range: ", FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
    emrDateRangeParagraph.add(
        new Phrase(
            jsonOb.get("startDate") + " to " + jsonOb.get("endDate"),
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
    emrDateRangeParagraph.add(Chunk.NEWLINE);

    document.add(emrDateRangeParagraph);

    document.add(Chunk.NEWLINE);

    JSONArray arr = sortByWhenPreparedDesc(jsonOb.getJSONArray("meds"));
    Paragraph drugProductParagraph =
        new Paragraph(
            "DHDR Drugs", FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
    drugProductParagraph.add(
        new Phrase(
            "(Found " + arr.length() + " Events)",
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
    drugProductParagraph.add(Chunk.NEWLINE);
    drugProductParagraph.setSpacingAfter(5f);
    document.add(drugProductParagraph);

    if (arr.length() > 0) {

      for (var i = 0; i < arr.length(); i++) {
        JSONObject med = arr.getJSONObject(i);
        PdfPTable table = new PdfPTable(new float[] {3, 3, 3, 1.5f, 1.5f, 1, 1});
        table.setSpacingAfter(10f);
        table.setWidthPercentage(100.0f);
        populateSummaryDrugMetaData(table, med);
        populateSummaryDrugHeader(table);
        populateSummaryDrugData(med, table);
        document.add(table);
      }

    } else {
      Paragraph noResults =
          new Paragraph(
              "No events found for the search time period.",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
      noResults.add(Chunk.NEWLINE);
      document.add(noResults);
    }

    if (jsonOb.has("services")) {
      JSONArray serviceArr = sortByWhenPreparedDesc(jsonOb.getJSONArray("services"));

      document.add(Chunk.NEWLINE);

      Paragraph servicesProductParagraph =
          new Paragraph(
              "DHDR PharmaServices",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
      servicesProductParagraph.add(
          new Phrase(
              "(Found " + serviceArr.length() + " Events)",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
      servicesProductParagraph.add(Chunk.NEWLINE);
      servicesProductParagraph.setSpacingAfter(5f);
      document.add(servicesProductParagraph);

      PdfPTable serviceTable = new PdfPTable(9);
      serviceTable.setWidthPercentage(100.0f);

      if (serviceArr.length() > 0) {

        serviceTable.addCell(getHeaderCell("Last Service Date"));
        serviceTable.addCell(getHeaderCell("Pickup Date"));
        serviceTable.addCell(getHeaderCell("Pharmacy Service Type"));
        serviceTable.addCell(getHeaderCell("Pharmacy Service Description"));
        serviceTable.addCell(getHeaderCell("Rx Number"));
        serviceTable.addCell(getHeaderCell("Therapeutic Class/Sub-class"));
        serviceTable.addCell(getHeaderCell("Pharmacy Name"));
        serviceTable.addCell(getHeaderCell("Pharmacist"));
        serviceTable.addCell(getHeaderCell("Pharmacy #"));
        serviceTable.setHeaderRows(1);

        for (int i = 0; i < serviceArr.length(); i++) {
          JSONObject med = serviceArr.getJSONObject(i);

          serviceTable.addCell(getItemCell(med.optString("whenPrepared")));
          serviceTable.addCell(getItemCell(med.optString("whenHandedOver")));
          JSONObject brandObj = med.optJSONObject("brandName");
          serviceTable.addCell(getItemCell(serviceTypeWithPin(brandObj)));
          serviceTable.addCell(getItemCell(med.optString("genericName")));
          serviceTable.addCell(getItemCell(med.optString("rxNumber")));
          serviceTable.addCell(
              getItemCell(med.optString("ahfsClass") + "/" + med.optString("ahfsSubClass")));
          serviceTable.addCell(getItemCell(med.optString("dispensingPharmacy")));
          JSONObject pharmacistLicenceNumberObj = med.optJSONObject("pharmacistLicenceNumber");
          String pharmacistLicenceValue =
              pharmacistLicenceNumberObj != null
                  ? pharmacistLicenceNumberObj.optString("value")
                  : "";
          serviceTable.addCell(
              getItemCell(
                  med.optString("pharmacistLastname")
                      + ", "
                      + med.optString("pharmacistFirstname")
                      + " ("
                      + pharmacistLicenceValue
                      + ")"));
          serviceTable.addCell(
              getItemCell(med.optString("dispensingPharmacyPhoneNumber")));
        }

        document.add(serviceTable);
      } else {
        Paragraph noResults =
            new Paragraph(
                "No events found for the search time period.",
                FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
        noResults.add(Chunk.NEWLINE);
        document.add(noResults);
      }
    }

    if (jsonOb.has("localData")) {
      JSONArray localArr = jsonOb.getJSONArray("localData");

      document.add(Chunk.NEWLINE);

      Paragraph servicesProductParagraph =
          new Paragraph(
              "EMR Prescriptions",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
      servicesProductParagraph.add(
          new Phrase(
              "(Found " + localArr.length() + " Events)",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
      servicesProductParagraph.add(Chunk.NEWLINE);
      servicesProductParagraph.setSpacingAfter(5f);
      document.add(servicesProductParagraph);

      PdfPTable localTable = new PdfPTable(4);
      localTable.setWidthPercentage(100.0f);

      if (localArr.length() > 0) {

        localTable.addCell(getHeaderCell("Start Date"));
        localTable.addCell(getHeaderCell("Medication"));
        localTable.addCell(getHeaderCell("Prescriber"));
        localTable.addCell(getHeaderCell("DIN"));
        localTable.setHeaderRows(1);

        for (int i = 0; i < localArr.length(); i++) {
          JSONObject med = localArr.getJSONObject(i);
          localTable.addCell(getItemCell(med.optString("rxDate")));
          localTable.addCell(getItemCell(med.optString("instructions")));
          localTable.addCell(getItemCell(med.optString("providerName")));
          localTable.addCell(getItemCell(med.optString("regionalIdentifier")));
        }

        document.add(localTable);

      } else {
        Paragraph noResults =
            new Paragraph(
                "No events found for the search time period.",
                FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
        noResults.add(Chunk.NEWLINE);
        document.add(noResults);
      }
    }

    document.close();
  }

  /**
   * Builds the "Printed on &lt;date&gt; by &lt;name&gt;" line rendered in the per-page footer
   * (DHDR13.01.c).
   *
   * @param loggedInInfo LoggedInInfo the current session, used for the printing provider's name
   * @return String the printed-on/by footer line
   */
  private String buildPrintedInfo(LoggedInInfo loggedInInfo) {
    String printedByName =
        loggedInInfo.getLoggedInProvider().getLastName()
            + ", "
            + loggedInInfo.getLoggedInProvider().getFirstName();
    return "Printed on " + formatter.format(new Date()) + " by " + printedByName;
  }

  /**
   * Resolves the EMR confidentiality statement printed on each page (DHDR13.01.h). Uses the
   * clinic-configured statement (oscar_mcmaster.properties {@code confidentiality_statement.vN})
   * when set, otherwise a generic personal-health-information confidentiality notice.
   *
   * @return String the confidentiality statement to print
   */
  private String resolveConfidentialityStatement() {
    String configured = OscarProperties.getConfidentialityStatement();
    if (configured != null && !configured.trim().isEmpty()) {
      return configured.trim();
    }
    return "CONFIDENTIALITY NOTICE: This document contains personal health information intended only"
        + " for the authorized recipient. Any unauthorized review, use, disclosure, or distribution"
        + " is prohibited.";
  }

  /**
   * Builds the DHDR-side patient demographic line (DHDR13.01.b) from the front-end {@code
   * dhdrPatient} object (first/last name, gender, DOB, HIN as maintained by the DHDR EHR Service).
   *
   * @param dhdrPatient JSONObject the DHDR-side patient, or null when none was resolved
   * @return String the demographic line, or an empty string when no DHDR patient is available
   */
  private String buildDhdrDemoLine(JSONObject dhdrPatient) {
    if (dhdrPatient == null) {
      return "";
    }
    String first = dhdrPatient.optString("firstName", "").trim();
    String last = dhdrPatient.optString("lastName", "").trim();
    String gender = dhdrPatient.optString("gender", "").trim();
    String dob = dhdrPatient.optString("dob", "").trim();
    String hin = dhdrPatient.optString("hin", "").trim();
    if (first.isEmpty() && last.isEmpty() && dob.isEmpty() && hin.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder("DHDR EHR Service - ");
    sb.append(last);
    if (!first.isEmpty()) {
      sb.append(", ").append(first);
    }
    if (!gender.isEmpty()) {
      sb.append("   Gender: ").append(gender);
    }
    if (!dob.isEmpty()) {
      sb.append("   DOB: ").append(dob);
      String age = computeAge(dob);
      if (!age.isEmpty()) {
        sb.append("   Age: ").append(age);
      }
    }
    if (!hin.isEmpty()) {
      sb.append("   HIN: ").append(hin);
    }
    return sb.toString();
  }

  /**
   * Computes the age in whole years from a FHIR birthDate string (yyyy, yyyy-MM, or yyyy-MM-dd).
   *
   * @param dob String the FHIR birthDate value
   * @return String the age in years, or an empty string when the value cannot be parsed
   */
  private String computeAge(String dob) {
    try {
      String[] parts = dob.split("-");
      int year = Integer.parseInt(parts[0]);
      int month = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
      int day = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;
      LocalDate birth = LocalDate.of(year, month, day);
      return String.valueOf(Period.between(birth, LocalDate.now()).getYears());
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * Returns a copy of the given events ordered by dispense date ({@code whenPrepared}) descending -
   * most recent first - as the printed history requires (DHDR13.01). Events with no whenPrepared
   * value sort last. The source array is left unmodified.
   *
   * @param arr JSONArray the DHDR dispense / pharmacy-service events
   * @return JSONArray the events ordered by whenPrepared descending
   */
  private JSONArray sortByWhenPreparedDesc(JSONArray arr) throws JSONException {
    List<JSONObject> list = new ArrayList<>();
    for (int i = 0; i < arr.length(); i++) {
      list.add(arr.getJSONObject(i));
    }
    list.sort((a, b) -> b.optString("whenPrepared", "").compareTo(a.optString("whenPrepared", "")));
    JSONArray sorted = new JSONArray();
    for (JSONObject event : list) {
      sorted.put(event);
    }
    return sorted;
  }

  /**
   * Page event that stamps the DHDR disclaimer (DHDR13.01.g), the EMR confidentiality statement
   * (DHDR13.01.h), the printed-on/by line (DHDR13.01.c) and the "Page X of Y" counter (DHDR13.01.e)
   * into the bottom margin of every page.
   */
  private static class DhdrFooterEvent extends PdfPageEventHelper {
    private final String disclaimer;
    private final String confidentiality;
    private final String printedInfo;
    private PdfTemplate totalPages;
    private final Font footerFont =
        FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.BLACK);
    private final Font legalFont =
        FontFactory.getFont(FontFactory.HELVETICA, 7, Font.ITALIC, new Color(80, 80, 80));

    DhdrFooterEvent(String disclaimer, String confidentiality, String printedInfo) {
      this.disclaimer = disclaimer;
      this.confidentiality = confidentiality;
      this.printedInfo = printedInfo;
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
      totalPages = writer.getDirectContent().createTemplate(40, 12);
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      PdfContentByte cb = writer.getDirectContent();
      float left = document.left();
      float right = document.right();
      float bottom = document.bottom();

      // DHDR disclaimer (13.01.g) then the EMR confidentiality statement (13.01.h), wrapped in one
      // legal band that fills downward from just below the content area to above the page line.
      ColumnText ct = new ColumnText(cb);
      ct.setLeading(8f);
      ct.setSimpleColumn(left, bottom - 100, right, bottom - 6);
      ct.addText(new Phrase(disclaimer, legalFont));
      ct.addText(new Phrase(Chunk.NEWLINE));
      ct.addText(new Phrase(confidentiality, legalFont));
      try {
        ct.go();
      } catch (DocumentException e) {
        // The footer is best-effort chrome; never let it abort the print.
      }

      // Printed-on/by (left) and Page X of Y (right) share the lowest baseline.
      float lineY = bottom - 118;
      ColumnText.showTextAligned(
          cb, Element.ALIGN_LEFT, new Phrase(printedInfo, footerFont), left, lineY, 0);
      ColumnText.showTextAligned(
          cb,
          Element.ALIGN_RIGHT,
          new Phrase("Page " + writer.getPageNumber() + " of ", footerFont),
          right - 40,
          lineY,
          0);
      cb.addTemplate(totalPages, right - 38, lineY);
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
      ColumnText.showTextAligned(
          totalPages,
          Element.ALIGN_LEFT,
          new Phrase(String.valueOf(writer.getPageNumber() - 1), footerFont),
          0,
          2,
          0);
    }
  }
}
