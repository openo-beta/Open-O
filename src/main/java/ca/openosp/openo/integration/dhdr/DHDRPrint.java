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
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.printing.FontSettings;
import org.oscarehr.common.printing.PdfWriterFactory;
import org.oscarehr.managers.DemographicManager;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.SpringUtils;

import java.awt.*;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DHDRPrint {

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

    PdfWriter writer = PdfWriterFactory.newInstance(document, outputStream,
        FontSettings.HELVETICA_10PT);

    HeaderFooter header = getHeaderFooter(demo, "DHDR Detailed");
    document.setHeader(header);

    document.open();
    contentByte = writer.getDirectContent();

    Paragraph dhrDisclaimerParagraph =
        new Paragraph(
            "Warning: Limited to Drug and Pharmacy Service Information available in the Digital Health Drug Repository (DHDR) EHR Service. To ensure a Best Possible Medication History (BPMH), please review this information with the patient/family and use other available sources of medication information in addition to the DHDR EHR Service.",
            FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, Color.BLACK));
    dhrDisclaimerParagraph.add(Chunk.NEWLINE);
    document.add(dhrDisclaimerParagraph);

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

    JSONObject med = jsonOb.getJSONObject("med");

    if (med != null) {

      table.addCell(getHeaderCell("Dispense Date"));
      table.addCell(getHeaderCell(med.optString("whenPrepared"))); // Dispense Date

      table.setHeaderRows(1);

      table.addCell(getHeaderCell("Generic"));
      table.addCell(getItemCell(med.optString("genericName"))); // Generic

      JSONObject brandObj = med.getJSONObject("brandName");
      table.addCell(getHeaderCell("Brand"));
      table.addCell(getItemCell(brandObj.optString("display"))); // Brand

      table.addCell(getHeaderCell("DIN/PIN"));
      table.addCell(getItemCell(brandObj.optString("code"))); // Brand

      try {
        JSONObject ahfsClassObj = med.getJSONObject("ahfsClass");
        if (!ahfsClassObj.isEmpty()) {
          table.addCell(getHeaderCell("Therapeutic Class"));
          table.addCell(getItemCell(ahfsClassObj.optString("display"))); // Brand
        }
      } catch (JSONException ignored) {
      }

      try {
        JSONObject ahfsSubClassObj = med.getJSONObject("ahfsSubClass");
        if (!ahfsSubClassObj.isEmpty()) {
          table.addCell(getHeaderCell("Therapeutic Sub-Class"));
          table.addCell(getItemCell(ahfsSubClassObj.optString("display"))); // Brand
        }
      } catch (JSONException ignored) {
      }

      table.addCell(getHeaderCell("Rx Number"));
      table.addCell(getItemCell(med.optString("rxNumber"))); // Brand

      table.addCell(getHeaderCell("Medical Condition/Reason for Use"));

      StringBuilder reasonCodesStr = new StringBuilder();
      JSONArray reasonCodes = med.getJSONArray("reasonCode");
      for (int i = 0; i < reasonCodes.size(); i++) {
        JSONObject jsonObject = reasonCodes.getJSONObject(i);

        reasonCodesStr.append(jsonObject.opt("code") + " -- " + jsonObject.opt("display"));
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
      table.addCell(getItemCell(med.optString("dispensedQuantity")));
      table.addCell(getHeaderCell("Est Days Supply"));
      table.addCell(getItemCell(med.optString("estimatedDaysSupply")));

      table.addCell(getHeaderCell("Refills Remaining"));
      table.addCell(getItemCell(med.optString("refillsRemaining")));
      table.addCell(getHeaderCell("Quantity Remaining"));
      table.addCell(getItemCell(med.optString("quantityRemaining")));

      JSONObject prescriberLicenceNumberObj = med.getJSONObject("prescriberLicenceNumber");
      table.addCell(getHeaderCell("Prescriber"));
      table.addCell(
          getItemCell(
              med.optString("prescriberLastname")
                  + ", "
                  + med.optString("prescriberFirstname")
                  + (prescriberLicenceNumberObj.isEmpty()
                      ? ""
                      : (" ("
                          + prescriberLicenceNumberObj.optString("value")
                          + ")"))));

      if (!prescriberLicenceNumberObj.isEmpty()) {
        table.addCell(getHeaderCell("Prescriber ID"));
        table.addCell(
            getItemCell(
                prescriberLicenceNumberObj.optString("system")
                    + " "
                    + prescriberLicenceNumberObj.optString("value")));
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
      JSONObject pharmacistLicenceNumber = med.getJSONObject("pharmacistLicenceNumber");
      table.addCell(
          getItemCell(
              med.optString("pharmacistLastname")
                  + ", "
                  + med.optString("pharmacistFirstname")
                  + (pharmacistLicenceNumber.isEmpty()
                      ? ""
                      : (" ("
                          + pharmacistLicenceNumber.optString("value")
                          + ")"))));

      document.add(table);
    } else {
      Paragraph noResults =
          new Paragraph(
              "No Med Found.",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
      noResults.add(Chunk.NEWLINE);
      document.add(noResults);
    }

    addDocumentFooter(loggedInInfo, document);
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

    PdfWriter writer = PdfWriterFactory.newInstance(document, outputStream, FontSettings.HELVETICA_10PT);

    HeaderFooter header = getHeaderFooter(demo, "DHDR Summary");
    document.setHeader(header);

    document.open();
    contentByte = writer.getDirectContent();

    Paragraph dhrDisclaimerParagraph =
        new Paragraph(
            "Warning: Limited to Drug and Pharmacy Service Information available in the Digital Health Drug Repository (DHDR) EHR Service. To ensure a Best Possible Medication History (BPMH), please review this information with the patient/family and use other available sources of medication information in addition to the DHDR EHR Service.",
            FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, Color.BLACK));
    dhrDisclaimerParagraph.add(Chunk.NEWLINE);
    document.add(dhrDisclaimerParagraph);

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

    JSONArray arr = jsonOb.getJSONArray("meds");
    Paragraph drugProductParagraph =
        new Paragraph(
            "Drug Product", FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
    drugProductParagraph.add(
        new Phrase(
            "(Found " + arr.size() + " Events)",
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
    drugProductParagraph.add(Chunk.NEWLINE);
    drugProductParagraph.setSpacingAfter(5f);
    document.add(drugProductParagraph);

    /////// table
    if (arr.size() > 0) {

      for (var i = 0; i < arr.size(); i++) {
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
    if (jsonOb.containsKey("services")) {
      JSONArray serviceArr = jsonOb.getJSONArray("services");

      document.add(Chunk.NEWLINE);

      Paragraph servicesProductParagraph =
          new Paragraph(
              "Pharma Services",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
      servicesProductParagraph.add(
          new Phrase(
              "(Found " + serviceArr.size() + " Events)",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
      servicesProductParagraph.add(Chunk.NEWLINE);
      servicesProductParagraph.setSpacingAfter(5f);
      document.add(servicesProductParagraph);

      PdfPTable serviceTable = new PdfPTable(8);
      serviceTable.setWidthPercentage(100.0f);

      if (serviceArr.size() > 0) {

        serviceTable.addCell(getHeaderCell("Last Service Date"));
        serviceTable.addCell(getHeaderCell("Pickup Date"));
        serviceTable.addCell(getHeaderCell("Pharmacy Service Type"));
        serviceTable.addCell(getHeaderCell("Pharmacy Service Description"));
        serviceTable.addCell(getHeaderCell("Therapeutic Class/Sub-class"));
        serviceTable.addCell(getHeaderCell("Pharmacy Name"));
        serviceTable.addCell(getHeaderCell("Pharmacist"));
        serviceTable.addCell(getHeaderCell("Pharmacy Fax"));
        serviceTable.setHeaderRows(1);

        for (int i = 0; i < serviceArr.size(); i++) {
          JSONObject med = serviceArr.getJSONObject(i);

          serviceTable.addCell(getItemCell(med.optString("whenPrepared"))); // Dispense Date
          serviceTable.addCell(getItemCell(med.optString("whenHandedOver"))); // Generic
          JSONObject brandObj = med.getJSONObject("brandName");
          serviceTable.addCell(getItemCell(brandObj.optString("display"))); // Brand
          serviceTable.addCell(getItemCell(med.optString("genericName")));
          serviceTable.addCell(
              getItemCell(med.optString("ahfsClass") + "/" + med.optString("ahfsSubClass")));
          serviceTable.addCell(getItemCell(med.optString("dispensingPharmacy")));
          JSONObject pharmacistLicenceNumberObj = med.getJSONObject("pharmacistLicenceNumber");
          serviceTable.addCell(
              getItemCell(
                  med.optString("pharmacistLastname")
                      + ", "
                      + med.optString("pharmacistFirstname")
                      + " ("
                      + pharmacistLicenceNumberObj.optString("value")
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

    addDocumentFooter(loggedInInfo, document);
  }

  private PdfPCell populateSummaryDrugMetaData(PdfPTable table, JSONObject med) {
    PdfPTable headerTable = new PdfPTable(new float[] {1f, 3f, 1.5f, 3f, 1.5f, 2f});
    PdfPCell header = new PdfPCell(headerTable);
    header.setColspan(12);
    table.addCell(header);

    String dispenseDate = med.optString("whenPrepared");
    String pickupDate = med.optString("pickUpDate");
    JSONObject patient = med.getJSONObject("patient");
    String patientDob = patient.optString("birthDate");
    String patientGender = patient.optString("gender");
    String patientHcn = "N/A";
    if (patient.has("identifier")
        && !patient.getJSONArray("identifier").isEmpty()
        && patient.getJSONArray("identifier").getJSONObject(0).has("value")) {
      patientHcn = patient.getJSONArray("identifier").getJSONObject(0).getString("value");
    }
    String patientFirstName = "N/A";
    String patientLastName = "N/A";
    if (patient.has("name") && !patient.getJSONArray("name").isEmpty()) {
      JSONObject name = patient.getJSONArray("name").getJSONObject(0);
      if (name.has("given") && !name.getJSONArray("given").isEmpty()) {
        patientFirstName = name.getJSONArray("given").getString(0);
      }
      patientLastName = name.optString("family");
    }

    JSONObject prescriberLicenceNumberObj = med.getJSONObject("prescriberLicenceNumber");
    String prescriberLicenseNumber =
        prescriberLicenceNumberObj == null || prescriberLicenceNumberObj.isEmpty()
            ? ""
            : " (" + prescriberLicenceNumberObj.optString("value") + ")";

    String prescriberName =
        med.optString("prescriberLastname")
            + (med.optString("prescriberLastname").length() > 0 ? ", " : "")
            + med.optString("prescriberFirstname")
            + prescriberLicenseNumber;
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
        getHeaderCell("Status"));
  }

  private void populateSummaryDrugData(JSONObject med, PdfPTable table) {
    table.addCell(getItemCell(med.optString("genericName")));
    JSONObject brandObj = med.getJSONObject("brandName");
    table.addCell(
        getItemCell(
            brandObj.optString("display")
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

  private Phrase getTitlePhrase(Demographic demo, String title) {
    Phrase titlePhrase =
        new Phrase(
            16, title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD, Color.BLACK));
    titlePhrase.add(Chunk.NEWLINE);
    titlePhrase.add(
        new Chunk(
            demo.getFormattedName(),
            FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, Color.BLACK)));
    titlePhrase.add(Chunk.NEWLINE);
    titlePhrase.add(
        new Chunk(
            getDemoInfo(demo),
            FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, Color.BLACK)));

    return titlePhrase;
  }

  private HeaderFooter getHeaderFooter(Demographic demo, String title) {
    HeaderFooter header = new HeaderFooter(getTitlePhrase(demo, title), false);
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

    PdfWriter writer =
        PdfWriterFactory.newInstance(document, outputStream, FontSettings.HELVETICA_10PT);

    HeaderFooter header = getHeaderFooter(demo, "DHDR Comparative");
    document.setHeader(header);

    document.open();

    Paragraph dhrDisclaimerParagraph =
        new Paragraph(
            "Warning: Limited to Drug and Pharmacy Service Information available in the Digital Health Drug Repository (DHDR) EHR Service. To ensure a Best Possible Medication History (BPMH), please review this information with the patient/family and use other available sources of medication information in addition to the DHDR EHR Service.",
            FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, Color.BLACK));
    dhrDisclaimerParagraph.add(Chunk.NEWLINE);
    document.add(dhrDisclaimerParagraph);

    ///

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

    JSONArray arr = jsonOb.getJSONArray("meds");
    Paragraph drugProductParagraph =
        new Paragraph(
            "DHDR Drugs", FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
    drugProductParagraph.add(
        new Phrase(
            "(Found " + arr.size() + " Events)",
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
    drugProductParagraph.add(Chunk.NEWLINE);
    drugProductParagraph.setSpacingAfter(5f);
    document.add(drugProductParagraph);

    if (arr.size() > 0) {

      for (var i = 0; i < arr.size(); i++) {
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

    if (jsonOb.containsKey("services")) {
      JSONArray serviceArr = jsonOb.getJSONArray("services");

      document.add(Chunk.NEWLINE);

      Paragraph servicesProductParagraph =
          new Paragraph(
              "DHDR PharmaServices",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
      servicesProductParagraph.add(
          new Phrase(
              "(Found " + serviceArr.size() + " Events)",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
      servicesProductParagraph.add(Chunk.NEWLINE);
      servicesProductParagraph.setSpacingAfter(5f);
      document.add(servicesProductParagraph);

      PdfPTable serviceTable = new PdfPTable(8);
      serviceTable.setWidthPercentage(100.0f);

      if (serviceArr.size() > 0) {

        serviceTable.addCell(getHeaderCell("Last Service Date"));
        serviceTable.addCell(getHeaderCell("Pickup Date"));
        serviceTable.addCell(getHeaderCell("Pharmacy Service Type"));
        serviceTable.addCell(getHeaderCell("Pharmacy Service Description"));
        serviceTable.addCell(getHeaderCell("Therapeutic Class/Sub-class"));
        serviceTable.addCell(getHeaderCell("Pharmacy Name"));
        serviceTable.addCell(getHeaderCell("Pharmacist"));
        serviceTable.addCell(getHeaderCell("Pharmacy #"));
        serviceTable.setHeaderRows(1);

        for (int i = 0; i < serviceArr.size(); i++) {
          JSONObject med = serviceArr.getJSONObject(i);

          serviceTable.addCell(getItemCell(med.optString("whenPrepared")));
          serviceTable.addCell(getItemCell(med.optString("whenHandedOver")));
          JSONObject brandObj = med.getJSONObject("brandName");
          serviceTable.addCell(getItemCell(brandObj.optString("display")));
          serviceTable.addCell(getItemCell(med.optString("genericName")));
          serviceTable.addCell(
              getItemCell(med.optString("ahfsClass") + "/" + med.optString("ahfsSubClass")));
          serviceTable.addCell(getItemCell(med.optString("dispensingPharmacy")));
          JSONObject pharmacistLicenceNumberObj = med.getJSONObject("pharmacistLicenceNumber");
          serviceTable.addCell(
              getItemCell(
                  med.optString("pharmacistLastname")
                      + ", "
                      + med.optString("pharmacistFirstname")
                      + " ("
                      + pharmacistLicenceNumberObj.optString("value")
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

    if (jsonOb.containsKey("localData")) {
      JSONArray localArr = jsonOb.getJSONArray("localData");

      document.add(Chunk.NEWLINE);

      Paragraph servicesProductParagraph =
          new Paragraph(
              "EMR Prescriptions",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK));
      servicesProductParagraph.add(
          new Phrase(
              "(Found " + localArr.size() + " Events)",
              FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
      servicesProductParagraph.add(Chunk.NEWLINE);
      servicesProductParagraph.setSpacingAfter(5f);
      document.add(servicesProductParagraph);

      PdfPTable localTable = new PdfPTable(4);
      localTable.setWidthPercentage(100.0f);

      if (localArr.size() > 0) {

        localTable.addCell(getHeaderCell("Start Date"));
        localTable.addCell(getHeaderCell("Medication"));
        localTable.addCell(getHeaderCell("Prescriber"));
        localTable.addCell(getHeaderCell("DIN"));
        localTable.setHeaderRows(1);

        for (int i = 0; i < localArr.size(); i++) {
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

    addDocumentFooter(loggedInInfo, document);
  }

  private void addDocumentFooter(LoggedInInfo loggedInInfo, Document document)
      throws DocumentException {
    String printedByName =
        loggedInInfo.getLoggedInProvider().getLastName()
            + ", "
            + loggedInInfo.getLoggedInProvider().getFirstName();

    Paragraph datePrinted =
        new Paragraph(
            "Printed on " + formatter.format(new Date()),
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
    Paragraph printedBy =
        new Paragraph(
            "Printed by " + printedByName,
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK));
    datePrinted.add(Chunk.NEWLINE);
    document.add(datePrinted);
    document.add(printedBy);
    document.close();
  }
}
