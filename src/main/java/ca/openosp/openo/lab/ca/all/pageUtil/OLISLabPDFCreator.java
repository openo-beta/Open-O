//CHECKSTYLE:OFF
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
package ca.openosp.openo.lab.ca.all.pageUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.Hl7TextMessageDao;
import ca.openosp.openo.commn.model.Hl7TextMessage;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import ca.openosp.OscarProperties;
import ca.openosp.openo.lab.ca.all.Hl7textResultsData;
import ca.openosp.openo.lab.ca.all.parsers.Factory;
import ca.openosp.openo.lab.ca.all.parsers.Hl7FormattedText;
import ca.openosp.openo.lab.ca.all.parsers.OLISHL7Handler;
import ca.openosp.openo.lab.ca.all.util.Utilities;
import ca.openosp.openo.utility.HtmlTextCleaner;
import ca.openosp.openo.utility.PathValidationUtils;

import java.io.File;


public class OLISLabPDFCreator extends PdfPageEventHelper {
    private OutputStream os;

    private final String FINAL_CODE = "F";
    private final String REPORT_FINAL = "Final";
    private final String REPORT_PARTIAL = "Partial";


    private OLISHL7Handler handler;
    private int versionNum;
    private String[] multiID;
    private String id;
    // CT 20.2/20.3: who generated the printed report and when, stamped per page in onEndPage.
    private java.util.Date generationDate = new java.util.Date();
    private String generatedByUser = "";

    private Document document;
    private BaseFont bf;
    private BaseFont cf;
    private Font font;
    private Font boldFont;
    private Font redFont;
    private Font blueFont;
    private Font categoryHeadFont;
    private Font commentFont;
    private Font subscriptFont;

    private String category = "";
    private String newCategory = "";

    private Logger logger = MiscUtils.getLogger();

    /**
     * Convenience constructor for request-driven rendering. Resolves the segment id
     * from the {@code segmentID} request parameter, falling back to the request
     * attribute of the same name, then delegates to
     * {@link #OLISLabPDFCreator(OutputStream, HttpServletRequest, String)}.
     *
     * @param request HttpServletRequest the current request; supplies the segment id
     *                and, for the search-preview path, the {@code uuid} parameter and
     *                logged-in session
     * @param os      OutputStream the stream the generated PDF is written to
     * @since 2026-05-13
     */
    public OLISLabPDFCreator(HttpServletRequest request, OutputStream os) {
        this(os, request, request.getParameter("segmentID") != null ? request.getParameter("segmentID") : (String) request.getAttribute("segmentID"));
    }

    /**
     * Constructor for saved-lab rendering paths that have no {@link HttpServletRequest}
     * (e.g. {@code LabManagerImpl.renderLab} called from the consult attachment
     * manager). Only valid when {@code segmentId} is a real {@code hl7TextMessage} id;
     * the "0" search-preview path requires
     * {@link #OLISLabPDFCreator(OutputStream, HttpServletRequest, String)} to pull the
     * uuid and {@code LoggedInInfo} from the session.
     *
     * @param os        OutputStream the stream the generated PDF is written to
     * @param segmentId String the {@code hl7TextMessage} id of a saved OLIS lab
     * @since 2026-05-13
     */
    public OLISLabPDFCreator(OutputStream os, String segmentId) {
        this(os, null, segmentId);
    }

    /**
     * Renders a saved OLIS lab to a PDF byte array. Mirrors
     * {@link LabPDFCreator#getPdfBytes(String, String)} for OLIS labs; only works for
     * saved labs, not the {@code segmentID=0} search-preview path.
     *
     * @param segmentId String the {@code hl7TextMessage} id of a saved OLIS lab
     * @return byte[] the rendered PDF document
     * @throws IOException       if writing the PDF to the buffer fails
     * @throws DocumentException if PDF generation fails
     * @since 2026-05-13
     */
    public static byte[] getPdfBytes(String segmentId) throws IOException, DocumentException {
        // Delegate, parsing the handler here to preserve the existing self-fetching behavior.
        return getPdfBytes(segmentId, null);
    }

    /**
     * Variant of {@link #getPdfBytes(String)} that reuses an already-parsed
     * {@code handler} for a saved lab instead of re-parsing it.
     *
     * @param segmentId String the {@code hl7TextMessage} id of a saved OLIS lab
     * @param handler   OLISHL7Handler a pre-parsed handler, or {@code null} to fetch it
     * @return byte[] the rendered PDF document
     * @throws IOException       if writing the PDF to the buffer fails
     * @throws DocumentException if PDF generation fails
     * @since 2026-06-01
     */
    public static byte[] getPdfBytes(String segmentId, OLISHL7Handler handler) throws IOException, DocumentException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        new OLISLabPDFCreator(baos, null, segmentId, handler).printPdf();
        return baos.toByteArray();
    }

    /**
     * Primary constructor. Determines the lab version among any matching siblings,
     * then builds the OLIS handler: for a saved lab ({@code segmentId != "0"}) the
     * handler is loaded from the stored {@code hl7TextMessage}; for the search-preview
     * path ({@code segmentId == "0"}) the cached OLIS response is read from the temp
     * file keyed by the request's {@code uuid} parameter and parsed in-place.
     *
     * @param os        OutputStream the stream the generated PDF is written to
     * @param request   HttpServletRequest the current request; required for the
     *                  {@code segmentId == "0"} preview path (supplies {@code uuid} and
     *                  the logged-in session) and may be {@code null} for saved labs
     * @param segmentId String the {@code hl7TextMessage} id, or {@code "0"} for the
     *                  unsaved search-preview path
     * @since 2026-05-13
     */
    public OLISLabPDFCreator(OutputStream os, HttpServletRequest request, String segmentId) {
        this(os, request, segmentId, null);
    }

    /**
     * Variant of {@link #OLISLabPDFCreator(OutputStream, HttpServletRequest, String)}
     * that reuses an already-parsed {@code injected} handler for a saved lab instead
     * of re-parsing it; callers that built the handler to discriminate OLIS vs. generic
     * labs pass it here to avoid a second HL7 parse. {@code null} restores the
     * self-fetching behavior. Applies only to saved labs ({@code segmentId != "0"});
     * the {@code "0"} search-preview path always parses the cached response in-place.
     *
     * @param os        OutputStream the stream the generated PDF is written to
     * @param request   HttpServletRequest required for the {@code segmentId == "0"} path
     * @param segmentId String the {@code hl7TextMessage} id, or {@code "0"} for preview
     * @param injected  OLISHL7Handler a pre-parsed handler for the saved lab, or
     *                  {@code null} to fetch it internally
     * @since 2026-06-01
     */
    public OLISLabPDFCreator(OutputStream os, HttpServletRequest request, String segmentId, OLISHL7Handler injected) {
        this.os = os;
        this.id = segmentId;

        // CT 20.2/20.3: capture the generating user for the per-page "Generated from OLIS
        // on <timestamp> by user <name>" stamp. Best-effort — never block PDF generation.
        if (request != null) {
            try {
                LoggedInInfo lii = LoggedInInfo.getLoggedInInfoFromSession(request);
                if (lii != null && lii.getLoggedInProvider() != null) {
                    String fn = lii.getLoggedInProvider().getFirstName();
                    String ln = lii.getLoggedInProvider().getLastName();
                    this.generatedByUser = ((fn == null ? "" : fn) + " " + (ln == null ? "" : ln)).trim();
                }
            } catch (Exception ignore) {
                // leave generatedByUser blank
            }
        }

        // determine lab version
        String multiLabId = Hl7textResultsData.getMatchingLabs(id);
        this.multiID = multiLabId.split(",");

        int i = 0;
        while (!multiID[i].equals(id)) {
            i++;
        }
        this.versionNum = i + 1;

        if (!segmentId.equals("0")) { // OLIS lab that is stored in chart has a segmentID that is not 0
            //Need date lab was received by OSCAR
            Hl7TextMessageDao hl7TxtMsgDao = (Hl7TextMessageDao) SpringUtils.getBean(Hl7TextMessageDao.class);
            Hl7TextMessage hl7TextMessage = hl7TxtMsgDao.find(Integer.parseInt(segmentId));
            java.util.Date date = hl7TextMessage.getCreated();
            String stringFormat = "yyyy-MM-dd HH:mm";

            // reuse the supplied handler when present, else parse the saved lab
            this.handler = (injected != null) ? injected : (OLISHL7Handler) Factory.getHandler(id);
        } else { // OLIS lab not saved to chart has a segmentId of 0
            LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

            String uuidToAdd = request.getParameter("uuid");
            if (uuidToAdd == null || uuidToAdd.trim().isEmpty()) {
                throw new SecurityException("Invalid uuid parameter");
            }
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            File olisFile = PathValidationUtils.validatePath("olis_" + uuidToAdd + ".response", tmpDir);
            String fileName = olisFile.getPath();
            String hl7Parsed = "";
            try {
                if (Files.exists(Paths.get(fileName))) {
                    ArrayList<String> hl7Body = Utilities.separateMessages(fileName);
                    for (String hl7Text : hl7Body) {
                        hl7Parsed += hl7Text.replace("\\E\\", "\\SLASHHACK\\").replace("µ", "\\MUHACK\\").replace("\\H\\", "\\.H\\").replace("\\N\\", "\\.N\\");
                    }
                    // set
                    java.util.Date date = new java.util.Date();
                    String stringFormat = "yyyy-MM-dd HH:mm";
                    //dateLabReceived = UtilDateUtilities.DateToString(date, stringFormat);
                    //create handler
                    this.handler = (OLISHL7Handler) Factory.getHandler("OLIS_HL7", hl7Parsed);
                }
            } catch (IOException ioe) {
                //Reading file failed
                MiscUtils.getLogger().error("Couldn't print requested OLIS lab.", ioe);
                request.setAttribute("result", "Error");
            } catch (Exception e) {
                // separating message failed
                MiscUtils.getLogger().error("Couldn't print requested OLIS lab.", e);
                request.setAttribute("result", "Error");
            }
        }
    }

    public void printPdf() throws IOException, DocumentException {

        // check that we have data to print
        if (handler == null) {
            throw new DocumentException();
        }

        //Create the document we are going to write to
        document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, os);

        //Set page event, function onEndPage will execute each time a page is finished being created
        writer.setPageEvent(this);

        document.setPageSize(PageSize.LETTER);
        // Leave room in the top margin for the OLIS static header and in the bottom
        // margin for the confidentiality footer added per page in onEndPage
        // (CT Tracker reqs 2.3 and 15.1).
        document.setMargins(36, 36, 56, 48);
        document.addTitle("Title of the Document");
        document.addCreator("OSCAR");
        document.open();

        //Create the fonts that we are going to use
        bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        cf = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        font = new Font(bf, 9, Font.NORMAL);
        boldFont = new Font(bf, 9, Font.BOLD);
        redFont = new Font(bf, 9, Font.NORMAL, BaseColor.RED);
        blueFont = new Font(bf, 9, Font.NORMAL, BaseColor.BLUE);
        categoryHeadFont = new Font(bf, 12, Font.BOLD);
        commentFont = new Font(cf, 9, Font.NORMAL);
        subscriptFont = new Font(bf, 6, Font.NORMAL);


        // add the header table containing the patient and lab info to the document
        createInfoTable();

        // add the tests and test info for each header
        ArrayList<String> headers = handler.getHeaders();
        int obr;
        int lineNum = 0;
        for (int i = 0; i < headers.size(); i++) {
            //Gets the mapped OBR for the current index
            obr = handler.getMappedOBR(i);
            lineNum = obr + 1;
            //If the current lineNum is not a childOBR
            if (!handler.isChildOBR(lineNum)) {
                //Calls on the addOLISLabCategory function passing the header at the current obr, and the obr itself
                addOLISLabCategory(headers.get(obr), obr);
            }
        }

        document.close();

        os.flush();
    }

    /*
     * Given the name of a lab category this method will add the category
     * header, the test result headers and the test results for that category.
     */
    private void addOLISLabCategory(String header, Integer obr) throws DocumentException {
        BaseColor categoryBackground = new BaseColor(255, 204, 0);
        BaseColor separatorColour = new BaseColor(0, 51, 153);

        //Creates a separator cell for separation between results
        PdfPCell separator = new PdfPCell();
        separator.setColspan(2);
        separator.setBorder(0);
        separator.setBackgroundColor(separatorColour);
        separator.setFixedHeight(1f);


        //Category Table Variables
        float[] categoryTableWidths;
        categoryTableWidths = new float[]{2f, 3f};
        PdfPTable categoryTable = new PdfPTable(categoryTableWidths);
        categoryTable.setWidthPercentage(100);
        categoryTable.setKeepTogether(true);

        //Main Table Variables
        float[] mainTableWidths;
        //Unused column is 3f
        mainTableWidths = new float[]{8f, 3f, 1f, 3f, 2f, 2f};
        PdfPTable table = new PdfPTable(mainTableWidths);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        //Sets the current category as a newCategory
        newCategory = handler.getOBRCategory(obr);

        //If it is a different category, then add a new category header to the category table
        if (!category.equals(newCategory)) {
            categoryTable.addCell(separator);
            //Adds the Category name to the table
            cell = new PdfPCell();
            cell.setColspan(2);
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(categoryBackground);
            cell.setPhrase(new Phrase(newCategory, categoryHeadFont));
            categoryTable.addCell(cell);
            //The new category becomes the current category
            category = newCategory;
        }

        //Adds a separator
        categoryTable.addCell(separator);

        //Renews the cell so that it is clean
        cell = new PdfPCell();
        cell.setBorder(0);
        Phrase categoryPhrase = new Phrase();
        categoryPhrase.setFont(boldFont);
        //Replaces
        categoryPhrase.add(HtmlTextCleaner.toPlainText(header));
        // CT 10.2.x: red parenthetical status adjacent to the test request name,
        // e.g. "(test was cancelled)" / "(amended)".
        String obrRedText = handler.getObrStatusRedText(obr);
        if (!stringIsNullOrEmpty(obrRedText)) {
            categoryPhrase.setFont(new Font(bf, 9, Font.NORMAL, BaseColor.RED));
            categoryPhrase.add(" " + obrRedText);
        }
        //Gets the point of care and outputs message if it exists
        String poc = handler.getPointOfCare(obr);
        if (!stringIsNullOrEmpty(poc)) {
            categoryPhrase.setFont(subscriptFont);
            categoryPhrase.add("\n\n(test performed at point of care)");
        }

        //Checks if the OBR is blocked
        Boolean blocked = handler.isOBRBlocked(obr);
        if (blocked) {
            categoryPhrase.setFont(new Font(bf, 7, Font.NORMAL, BaseColor.RED));
            categoryPhrase.add("\n\n(Do Not Disclose Without Explicit Patient Consent");
        }

        cell.setPhrase(categoryPhrase);
        categoryTable.addCell(cell);
        cell.setBorder(0);
        //Sets the specimen source and request status
        float[] specimenTableWidths = {1f, 4f};
        PdfPTable specimenTable = new PdfPTable(specimenTableWidths);
        //If there is a specimen source
        if (!stringIsNullOrEmpty(handler.getObrSpecimenSource(obr))) {
            cell.setPhrase(new Phrase("Specimen Type: ", boldFont));
            specimenTable.addCell(cell);
            cell.setPhrase(new Phrase(handler.getObrSpecimenSource(obr), font));
            specimenTable.addCell(cell);
            cell.setBorder(0);
        }

        // CT 9.5: site modifier under its own label
        if (!stringIsNullOrEmpty(handler.getSiteModifier(obr))) {
            cell.setPhrase(new Phrase("Site Modifier: ", boldFont));
            specimenTable.addCell(cell);
            cell.setPhrase(new Phrase(handler.getSiteModifier(obr), font));
            specimenTable.addCell(cell);
            cell.setBorder(0);
        }

        cell.setBorder(0);
        //Outputs the request status
        cell.setPhrase(new Phrase("Request Status: ", boldFont));
        specimenTable.addCell(cell);
        cell.setPhrase(new Phrase(handler.getObrStatus(obr), font));
        specimenTable.addCell(cell);

        //Adds the specimen table to the category table
        cell = new PdfPCell(specimenTable);
        cell.setBorder(0);
        categoryTable.addCell(cell);

        //Adds a small separator between the top row and the collection table
        cell = new PdfPCell();
        cell.setColspan(2);
        cell.setBorder(0);
        cell.setFixedHeight(1f);
        categoryTable.addCell(cell);

        //Creates the collection table and adds it to the category table
        PdfPTable collectionTable = createCollectionTable(obr);
        cell = new PdfPCell(collectionTable);
        cell.setBorder(0);
        cell.setColspan(2);
        categoryTable.addCell(cell);

        cell = new PdfPCell();
        cell.setBorder(0);
        String primaryFacility = handler.getPerformingFacilityName();
        String performingFacility = handler.getOBRPerformingFacilityName(obr);
        if (!primaryFacility.equals(performingFacility) && !stringIsNullOrEmpty(performingFacility)) {
            cell.setPhrase(new Phrase("Performing Lab: ", boldFont));
            categoryTable.addCell(cell);
            cell.setPhrase(new Phrase(performingFacility, font));
            categoryTable.addCell(cell);
            cell.setPhrase(new Phrase("Address: ", boldFont));
            categoryTable.addCell(cell);
            cell.setPhrase(new Phrase(getFullAddress(handler.getPerformingFacilityAddress(obr)), font));
            categoryTable.addCell(cell);
        }
        String diagnosis = handler.getDiagnosis(obr);
        if (!stringIsNullOrEmpty(diagnosis)) {
            cell.setPhrase(new Phrase("Diagnosis: ", font));
            categoryTable.addCell(cell);
            cell.setPhrase(new Phrase(diagnosis, font));
            categoryTable.addCell(cell);
        }

        cell = new PdfPCell();
        //Column Headers
        cell.setColspan(1);
        cell.setBorder(15);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new BaseColor(210, 212, 255));
        cell.setPhrase(new Phrase("Name", boldFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Result", boldFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Flag", boldFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Reference Range", boldFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Units", boldFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Status", boldFont));
        table.addCell(cell);

        cell.setBorder(12);
        cell.setBorderColor(BaseColor.BLACK); // cell.setBorderColor(Color.WHITE);
        cell.setBackgroundColor(new BaseColor(255, 255, 255));

        boolean obrFlag = false;
        int obxCount = handler.getOBXCount(obr);
        String collectorsComment = handler.getCollectorsComment(obr);
        int obx = 0;

        if (!stringIsNullOrEmpty(collectorsComment)) {
            cell.setColspan(7);
            Phrase collectorsCommentPhrase = new Phrase();
            collectorsCommentPhrase.setFont(font);
            collectorsCommentPhrase.add("Collector's Comment: ");
            // CT 9.9.2: collector's comment in fixed-width font
            collectorsCommentPhrase.setFont(commentFont);
            collectorsCommentPhrase.add(Hl7FormattedText.toPlainText(collectorsComment));

            collectorsCommentPhrase.setFont(subscriptFont);
            collectorsCommentPhrase.add("\t\t" + handler.getCollectorsCommentSourceOrganization(obr));
            cell.setPhrase(collectorsCommentPhrase);
            table.addCell(cell);

            cell.setColspan(1);
        }


        if (handler.getObservationHeader(obr, 0).equals(header)) {
            int commentCount = handler.getOBRCommentCount(obr);
            for (int comment = 0; comment < commentCount; comment++) {
                String obxNN = handler.getOBXName(obr, 0);
                if (!obrFlag && obxNN.equals("")) {
                    cell.setPhrase(new Phrase(handler.getOBRName(comment), font));
                    table.addCell(cell);
                    cell.setPhrase(new Phrase(handler.getObrSpecimenSource(comment), font));
                    table.addCell(cell);
                    cell.setColspan(5);
                    table.addCell(cell);
                    obrFlag = true;
                }

                String obrComment = handler.getOBRComment(obr, comment);
                String sourceOrg = handler.getOBRSourceOrganization(obr, comment);

                cell.setColspan(6);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                Phrase obrCommentPhrase = new Phrase();
                obrCommentPhrase.setFont(font);
                obrCommentPhrase.add(obrComment);
                obrCommentPhrase.setFont(subscriptFont);
                obrCommentPhrase.add("\t\t" + sourceOrg);
                cell.setPhrase(obrCommentPhrase);
                table.addCell(cell);
            }
        }

        for (int count = 0; count < obxCount; count++) {
            obx = handler.getMappedOBX(obr, count);
            String obxName = handler.getOBXName(obr, obx);
            boolean b1 = false;
            boolean b2 = false;
            boolean b3 = false;

            boolean fail = true;

            try {
                b1 = !handler.getOBXResultStatus(obr, obx).equals("DNS");
                b2 = !stringIsNullOrEmpty(obxName);
                String obsHeader = handler.getObservationHeader(obr, obx);
                b3 = obsHeader.equals(header);
                fail = false;

            } catch (Exception e) {
                logger.info("ERROR: " + e);
            }

            if (!fail && b1 && b2 && b3) {
                String obrName = handler.getOBRName(obr);
                b1 = !obrFlag && !stringIsNullOrEmpty(obrName);
                b2 = !(obxName.contains(obrName));
                b3 = obxCount < 2;

                if (b1 && b2 && b3) {
                    obrFlag = true;
                }

                String status = handler.getOBXResultStatus(obr, obx).trim();
                String statusMsg = "";
                try {
                    statusMsg = OLISHL7Handler.getTestResultStatusMessage(handler.getOBXResultStatus(obr, obx).charAt(0));
                } catch (Exception e) {
                    statusMsg = "";
                }

                //Creates a new font used on the line
                Font lineFont = new Font(font);
                String abnormal = handler.getOBXAbnormalFlag(obr, obx);

                //If the abnormal status starts with L then the font color is blue
                if (abnormal != null && abnormal.startsWith("L")) {
                    lineFont = blueFont;
                }
                //If the abnormal status starts with an A, H, or isOBXAbnormal returns true then the font color is blue
                else if (abnormal != null && (abnormal.equals("A") || abnormal.startsWith("H") || handler.isOBXAbnormal(obr, obx))) {
                    lineFont = redFont;
                }

                Font statusMsgFont = new Font(lineFont);

                //Gets the font style to be used in the table according to the status
                if (status != null && status.startsWith("W")) {
                    lineFont.setStyle(Font.STRIKETHRU);
                }
                //Creates a new phrase to hold the display name
                Phrase obxDisplayName = new Phrase();
                //Sets the font and replaces all breaks with a new line
                obxDisplayName.setFont(lineFont);
                obxDisplayName.add(HtmlTextCleaner.toPlainText(obxName));

                //Checks the abnormal nature of the test and adds the necessary portion to the displayName
                String abnormalNature = handler.getNatureOfAbnormalTest(obr, obx);
                if (!stringIsNullOrEmpty(abnormalNature)) {
                    obxDisplayName.setFont(subscriptFont);
                    obxDisplayName.add("\t\t" + abnormalNature);
                }


                String obxValueType = handler.getOBXValueType(obr, obx).trim();
                if (obxValueType.equals("ST") && handler.renderAsFT(obr, obx)) {
                    obxValueType = "FT";
                } else if (obxValueType.equals("TX") && handler.renderAsNM(obr, obx)) {
                    obxValueType = "NM";
                } else if (obxValueType.equals("FT") && handler.renderAsNM(obr, obx)) {
                    obxValueType = "NM";
                }

                //Sets the cell border to 15 so that the cells in the table are completely bordered instead of just left and right borders
                cell.setBorder(15);

                //Checks the obxValueType and populates the table row with the proper data
                if (obxValueType.equals("NM") || obxValueType.equals("ST") || obxValueType.equals("SN")) {
                    //Checks if it is Ancillary and obxValueType is not SN, adds Patient Observation row to table
                    if (handler.isAncillary(obr, obx) && !obxValueType.equals("SN")) {
                        cell.setColspan(6);
                        cell.setPhrase(new Phrase("Patient Observation", font));
                        table.addCell(cell);
                    }

                    cell.setColspan(1);
                    //Adds the columns for the current Value Type
                    cell.setVerticalAlignment(Element.ALIGN_TOP);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setPhrase(obxDisplayName);
                    table.addCell(cell);


                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    //If the type does not equal SN, then outputs normal OBX result, if it is SN then outputs SNResult
                    if (!obxValueType.equals("SN"))
                        cell.setPhrase(new Phrase(Hl7FormattedText.toPlainText(handler.getOBXResult(obr, obx)), lineFont));
                    else
                        cell.setPhrase(new Phrase(Hl7FormattedText.toPlainText(handler.getOBXSNResult(obr, obx)), lineFont));

                    table.addCell(cell);

                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPhrase(new Phrase(handler.getOBXAbnormalFlag(obr, obx), lineFont));
                    table.addCell(cell);

                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setPhrase(new Phrase(handler.getOBXReferenceRange(obr, obx), lineFont));
                    table.addCell(cell);

                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setPhrase(new Phrase(Hl7FormattedText.toPlainText(handler.getOBXUnits(obr, obx)), lineFont));
                    table.addCell(cell);

                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPhrase(new Phrase(statusMsg, statusMsgFont));
                    table.addCell(cell);
                } else if (obxValueType.equals("TX") || obxValueType.equals("FT")) {
                    //Adds the columns for the current Value Type
                    cell.setVerticalAlignment(Element.ALIGN_TOP);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setColspan(6);
                    cell.setPhrase(obxDisplayName);
                    table.addCell(cell);

                    cell.setColspan(5);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPhrase(new Phrase(Hl7FormattedText.toPlainText(handler.getOBXResult(obr, obx)), lineFont));
                    table.addCell(cell);

                    cell.setColspan(1);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPhrase(new Phrase(statusMsg, statusMsgFont));
                    table.addCell(cell);
                }
                //Combines the TM, DT, and TS displays into one to reduce redundant code since the only difference between them is the OBX Results that are retrieved
                else if (obxValueType.equals("TM") || obxValueType.equals("DT") || obxValueType.equals("TS")) {
                    cell.setColspan(1);
                    //Adds the columns for the current Value Type
                    cell.setVerticalAlignment(Element.ALIGN_TOP);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setPhrase(obxDisplayName);
                    table.addCell(cell);

                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    //Gets the OBX result based on the value type
                    if (obxValueType.equals("TM")) {
                        cell.setPhrase(new Phrase(Hl7FormattedText.toPlainText(handler.getOBXTMResult(obr, obx)), lineFont));
                    } else if (obxValueType.equals("DT")) {
                        cell.setPhrase(new Phrase(Hl7FormattedText.toPlainText(handler.getOBXDTResult(obr, obx)), lineFont));
                    } else {
                        cell.setPhrase(new Phrase(Hl7FormattedText.toPlainText(handler.getOBXTSResult(obr, obx)), lineFont));
                    }

                    table.addCell(cell);

                    cell.setColspan(3);
                    cell.setPhrase(new Phrase("", lineFont));
                    table.addCell(cell);

                    cell.setColspan(1);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPhrase(new Phrase(statusMsg, statusMsgFont));
                    table.addCell(cell);
                } else if (obxValueType.equals("ED")) {
                    //Adds the columns for the current row
                    cell.setColspan(1);
                    cell.setVerticalAlignment(Element.ALIGN_TOP);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setPhrase(obxDisplayName);
                    table.addCell(cell);

                    cell.setColspan(3);
                    cell.setPhrase(new Phrase("", lineFont));
                    table.addCell(cell);

                    cell.setColspan(1);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPhrase(new Phrase(handler.getOBXUnits(obr, obx), lineFont));
                    table.addCell(cell);

                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPhrase(new Phrase(statusMsg, statusMsgFont));
                    table.addCell(cell);

                    cell.setColspan(6);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setPhrase(new Phrase("Attachment omitted from printing", lineFont));
                    table.addCell(cell);

                } else if (obxValueType.equals("CE")) {
                    //Adds the columns for the current Value Type
                    cell.setColspan(6);
                    cell.setVerticalAlignment(Element.ALIGN_TOP);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setPhrase(obxDisplayName);
                    table.addCell(cell);

                    cell.setColspan(5);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPhrase(new Phrase(handler.getOBXCEName(obr, obx), font));
                    table.addCell(cell);

                    cell.setColspan(1);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPhrase(new Phrase(statusMsg, statusMsgFont));
                    table.addCell(cell);

                    //If the status is final
                    if (handler.isStatusFinal(handler.getOBXResultStatus(obr, obx).charAt(0))) {
                        String parentId = handler.getOBXCEParentId(obr, obx);
                        //If there is a parent ID then outputs a table for Agent and Sensitivity
                        if (!stringIsNullOrEmpty(parentId)) {
                            float[] ceTableWidths = {2f, 3f};
                            PdfPTable ceTable = new PdfPTable(ceTableWidths);
                            ceTable.setWidthPercentage(10f);


                            //Column Headers
                            cell.setColspan(1);
                            //Enables the borders with the bitwise combination of 7 (1 top, 2 bottom, 4 left)
                            cell.setBorder(7);
                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            cell.setPhrase(new Phrase("Agent", boldFont));
                            ceTable.addCell(cell);
                            //Enables the borders with the bitwise combination of 11 (1 top, 2 bottom, 8 right)
                            cell.setBorder(11);
                            cell.setPhrase(new Phrase("Sensitivity", boldFont));
                            ceTable.addCell(cell);
                            cell.setBorder(12);

                            cell.setColspan(1);
                            int childOBR = handler.getChildOBR(parentId) - 1;
                            //If the childOBR does not equal -1
                            if (childOBR != -1) {
                                //Gets the Gets the childOBR length
                                int childLength = handler.getOBXCount(childOBR);
                                //For each child obr, outputs it
                                for (int ceIndex = 0; ceIndex < childLength; ceIndex++) {
                                    Font strikeoutFont = new Font(bf, 9, Font.STRIKETHRU);
                                    String ceStatus = handler.getOBXResultStatus(childOBR, ceIndex).trim();
                                    boolean ceStrikeout = ceStatus != null && ceStatus.startsWith("W");
                                    Phrase ceName = new Phrase();
                                    Phrase ceSense = new Phrase();
                                    //If the font should be strikethrough
                                    if (ceStrikeout) {
                                        ceName.setFont(strikeoutFont);
                                        ceSense.setFont(strikeoutFont);
                                    }
                                    ceName.add(handler.getOBXCESensitivity(childOBR, ceIndex));
                                    ceSense.add(handler.getOBXName(childOBR, ceIndex));
                                    cell.setPhrase(ceName);
                                    ceTable.addCell(cell);
                                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    cell.setPhrase(ceSense);
                                    ceTable.addCell(cell);

                                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                }
                            }

                            //Adds the ceTable to the main table
                            cell = new PdfPCell(ceTable);
                            cell.setBorder(12);
                            cell.setColspan(6);
                            //For the table, sets the padding to
                            cell.setPaddingLeft(220);
                            cell.setPaddingRight(220);
                            table.addCell(cell);
                            //Sets the padding back to 0
                            cell.setPaddingLeft(0);
                            cell.setPaddingRight(0);

                            if (category.toUpperCase().trim().equals("MICROBIOLOGY")) {
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setPhrase(new Phrase("S=Susceptible  R=Resistant  I=Intermediate  MS=Moderately Susceptible  NI=No Interpretation  NS=Non Susceptible  S-DD=Susceptible Dose Dependent  VS=Very Susceptible", font));
                                table.addCell(cell);
                            }
                            cell.setColspan(1);
                        }
                    }
                } else {
                    //Adds the columns for the current Value Type
                    cell.setColspan(1);
                    cell.setVerticalAlignment(Element.ALIGN_TOP);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setPhrase(obxDisplayName);
                    table.addCell(cell);


                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setPhrase(new Phrase(handler.getOBXResult(obr, obx), lineFont));
                    table.addCell(cell);

                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPhrase(new Phrase(handler.getOBXAbnormalFlag(obr, obx), lineFont));
                    table.addCell(cell);

                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setPhrase(new Phrase(handler.getOBXReferenceRange(obr, obx), lineFont));
                    table.addCell(cell);

                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setPhrase(new Phrase(handler.getOBXUnits(obr, obx), lineFont));
                    table.addCell(cell);

                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPhrase(new Phrase(statusMsg, statusMsgFont));
                    table.addCell(cell);
                }
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                //If there is an obs method, outputs it
                String obsMethod = handler.getOBXObservationMethod(obr, obx);
                if (!stringIsNullOrEmpty(obsMethod)) {
                    cell.setColspan(6);
                    cell.setPhrase(new Phrase("Observation Method: " + obsMethod, font));
                    table.addCell(cell);
                    cell.setColspan(0);
                }
                //If there is an obsDate, outputs it
                String obsDate = handler.getOBXObservationDate(obr, obx);
                if (!stringIsNullOrEmpty(obsDate)) {
                    cell.setColspan(6);
                    cell.setPhrase(new Phrase("Observation Date: " + obsDate, font));
                    table.addCell(cell);
                    cell.setColspan(0);
                }

                cell.setColspan(6);
                cell.setBorder(12);
                //For each comment, outputs it
                for (int commentCount = 0; commentCount < handler.getOBXCommentCount(obr, obx); commentCount++) {
                    Phrase comment = new Phrase();
                    comment.setFont(commentFont);
                    comment.add(handler.getOBXComment(obr, obx, commentCount));
                    comment.setFont(subscriptFont);
                    comment.add("\t\t" + handler.getOBXSourceOrganization(obr, obx, commentCount));
                    cell.setPhrase(comment);
                    table.addCell(cell);
                }
            }
        }

        PdfPTable borderedCategoryTable = new PdfPTable(1);
        borderedCategoryTable.setWidthPercentage(100);
        cell = new PdfPCell(categoryTable);
        cell.setBorder(15);
        borderedCategoryTable.addCell(cell);

        document.add(borderedCategoryTable);
        document.add(table);

    }


    /*
     *  createInfoTable creates and adds the table at the top of the document
     *  which contains the patient and lab information
     */
    private void createInfoTable() throws DocumentException {

        String fullAddress = "";

        //Create patient info table
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        float[] pInfoWidths = {2f, 3f};
        PdfPTable pInfoTable = new PdfPTable(pInfoWidths);
        cell.setPhrase(new Phrase("Health #: ", boldFont));
        pInfoTable.addCell(cell);
        cell.setPhrase(new Phrase(handler.getFormattedHealthNum(), font));
        pInfoTable.addCell(cell);

        cell.setPhrase(new Phrase("Patient Name: ", boldFont));
        pInfoTable.addCell(cell);
        cell.setPhrase(new Phrase(handler.getPatientName(), font));
        pInfoTable.addCell(cell);

        cell.setPhrase(new Phrase("Date of Birth: ", boldFont));
        pInfoTable.addCell(cell);
        cell.setPhrase(new Phrase(handler.getDOB(), font));
        pInfoTable.addCell(cell);

        cell.setPhrase(new Phrase("Age: ", boldFont));
        pInfoTable.addCell(cell);
        cell.setPhrase(new Phrase(handler.getAge(), font));
        pInfoTable.addCell(cell);

        cell.setPhrase(new Phrase("Sex: ", boldFont));
        pInfoTable.addCell(cell);
        cell.setPhrase(new Phrase(handler.getSex(), font));
        pInfoTable.addCell(cell);

        //Patient Address
        for (HashMap<String, String> address : handler.getPatientAddresses()) {
            //Adds the address type to the table
            cell.setPhrase(new Phrase(address.get("Address Type") + ": ", boldFont));
            pInfoTable.addCell(cell);
            //Gets the full address
            fullAddress = getFullAddress(address);
            //Sets the cell's phrase and adds the cell to the table
            cell.setPhrase(new Phrase(fullAddress, font));
            pInfoTable.addCell(cell);
        }
        //Patient Home Phone
        ArrayList<HashMap<String, String>> homePhones = handler.getPatientHomeTelecom();
        for (HashMap<String, String> homePhone : homePhones) {
            Phrase phonePhrase = new Phrase();
            //Adds the phone's use
            cell.setPhrase(new Phrase("Home: ", boldFont));
            pInfoTable.addCell(cell);

            //Adds the phone number and useCode to the phrase
            phonePhrase.setFont(font);
            phonePhrase.add(getPhone(homePhone));
            phonePhrase.setFont(subscriptFont);
            phonePhrase.add(new Phrase(homePhone.get("useCode")));
            //Adds the phrase to the table
            cell.setPhrase(phonePhrase);
            pInfoTable.addCell(cell);
        }
        //Patient Work Telephone
        ArrayList<HashMap<String, String>> workPhones = handler.getPatientWorkTelecom();
        for (HashMap<String, String> workPhone : workPhones) {
            Phrase phonePhrase = new Phrase();
            //Adds the phone's use
            cell.setPhrase(new Phrase("Work: ", boldFont));
            pInfoTable.addCell(cell);
            //Adds the phone number and useCode
            phonePhrase.setFont(font);
            phonePhrase.add(getPhone(workPhone));
            phonePhrase.setFont(subscriptFont);
            phonePhrase.add(workPhone.get("useCode"));
            //Adds the phrase to the table
            cell.setPhrase(phonePhrase);
            pInfoTable.addCell(cell);
        }


        //Create results info table
        PdfPTable rInfoTable = new PdfPTable(2);
        cell.setPhrase(new Phrase("Report Status: ", boldFont));
        rInfoTable.addCell(cell);
        cell.setPhrase(new Phrase(handler.getOrderStatus() == FINAL_CODE ? REPORT_FINAL : REPORT_PARTIAL, font));
        rInfoTable.addCell(cell);

        cell.setPhrase(new Phrase("Order ID: ", boldFont));
        rInfoTable.addCell(cell);
        Phrase orderIdPhrase = new Phrase();
        orderIdPhrase.setFont(font);
        orderIdPhrase.add(handler.getAccessionNum());
        orderIdPhrase.setFont(subscriptFont);
        orderIdPhrase.add("\t\t" + handler.getAccessionNumSourceOrganization());

        cell.setPhrase(orderIdPhrase);
        rInfoTable.addCell(cell);

        cell.setPhrase(new Phrase("Order Received Date: ", boldFont));
        rInfoTable.addCell(cell);
        cell.setPhrase(new Phrase(handler.getOrderDate(), font));
        rInfoTable.addCell(cell);

        if (!stringIsNullOrEmpty(handler.getLastUpdateInOLISUnformated())) {
            cell.setPhrase(new Phrase("Last Updated In OLIS: ", boldFont));
            rInfoTable.addCell(cell);
            cell.setPhrase(new Phrase(handler.getLastUpdateInOLIS(), font));
            rInfoTable.addCell(cell);
        }

        if (!stringIsNullOrEmpty(handler.getSpecimenReceivedDateTime())) {
            cell.setPhrase(new Phrase("Specimen Received: ", boldFont));
            rInfoTable.addCell(cell);
            cell.setPhrase(new Phrase(handler.getSpecimenReceivedDateTime(), font));
            rInfoTable.addCell(cell);
        }

        HashMap<String, String> address;
        address = handler.getOrderingFacilityAddress();
        if (!stringIsNullOrEmpty(handler.getOrderingFacilityName())) {
            cell.setPhrase(new Phrase("Ordering Facility: ", boldFont));
            rInfoTable.addCell(cell);
            cell.setPhrase(new Phrase(handler.getOrderingFacilityName(), font));
            rInfoTable.addCell(cell);

            if (address != null && address.size() > 0) {
                cell.setPhrase(new Phrase("Address: ", boldFont));
                rInfoTable.addCell(cell);
                cell.setPhrase(new Phrase(getFullAddress(handler.getOrderingFacilityAddress()), font));
                rInfoTable.addCell(cell);
            }
        }

        cell.setPhrase(new Phrase("Ordering Provider: ", boldFont));
        rInfoTable.addCell(cell);
        cell.setPhrase(getDoctorNamePhrase(handler.getDocNameStructured()));
        rInfoTable.addCell(cell);

        address = handler.getOrderingProviderAddress();
        if (address != null && address.size() > 0) {
            cell.setPhrase(new Phrase("Address: ", boldFont));
            rInfoTable.addCell(cell);
            fullAddress = getFullAddress(handler.getOrderingProviderAddress());
            cell.setPhrase(new Phrase(fullAddress, font));
            rInfoTable.addCell(cell);
        }

        for (HashMap<String, String> phone : handler.getOrderingProviderPhones()) {
            String phoneNumber = "";
            //Adds the phone's use
            cell.setPhrase(new Phrase(phone.get("useCode") + ": ", boldFont));
            rInfoTable.addCell(cell);
            //Adds the phone number
            cell.setPhrase(getPhone(phone));
            rInfoTable.addCell(cell);
        }

        OLISHL7Handler.DoctorName attending = handler.getAttendingProviderStructured();
        if (!attending.isEmpty()) {
            cell.setPhrase(new Phrase("Attending Provider: ", boldFont));
            rInfoTable.addCell(cell);
            cell.setPhrase(getDoctorNamePhrase(attending));
            rInfoTable.addCell(cell);
        }

        OLISHL7Handler.DoctorName admitting = handler.getAdmittingProviderStructured();
        if (!admitting.isEmpty()) {
            cell.setPhrase(new Phrase("Admitting Provider: ", boldFont));
            rInfoTable.addCell(cell);
            cell.setPhrase(getDoctorNamePhrase(admitting));
            rInfoTable.addCell(cell);
        }

        String primaryFacility = handler.getPerformingFacilityName();
        String reportingFacility = handler.getReportingFacilityName();

        if (!stringIsNullOrEmpty(primaryFacility)) {
            //Determines if the performing facility is also the reporting facility and adds it and the name
            String facilityRole = "Primary " + (primaryFacility.equals(reportingFacility) ? "Reporting and " : "") + "Performing Lab: ";
            cell.setPhrase(new Phrase(facilityRole, boldFont));
            rInfoTable.addCell(cell);
            cell.setPhrase(new Phrase(primaryFacility, font));
            rInfoTable.addCell(cell);
            //Creates the format for the address and adds it
            address = handler.getPerformingFacilityAddress();
            if (address != null && address.size() > 0) {
                cell.setPhrase(new Phrase("Address: ", boldFont));
                rInfoTable.addCell(cell);
                fullAddress = getFullAddress(address);
                cell.setPhrase(new Phrase(fullAddress, font));
                rInfoTable.addCell(cell);
            }
        }

        if (!stringIsNullOrEmpty(reportingFacility) && !reportingFacility.equals(primaryFacility)) {
            //Adds reporting facility name
            cell.setPhrase(new Phrase("Primary Reporting Lab: ", boldFont));
            rInfoTable.addCell(cell);
            cell.setPhrase(new Phrase(reportingFacility, font));
            rInfoTable.addCell(cell);


            //Creates the format for the address and adds it
            address = handler.getReportingFacilityAddress();
            if (address != null && address.size() > 0) {
                cell.setPhrase(new Phrase("Address: ", boldFont));
                rInfoTable.addCell(cell);

                fullAddress = getFullAddress(address);
                cell.setPhrase(new Phrase(fullAddress, font));
                rInfoTable.addCell(cell);
                ;
            }
        }


        //Create client table
        PdfPTable clientTable = new PdfPTable(1);

        cell.setPhrase(getCCDocNamesPhrase(handler.getCCDocsStructured()));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        clientTable.addCell(cell);

        //Create comment table
        Phrase commentPhrase = new Phrase();
        PdfPTable commentTable = new PdfPTable(1);
        commentTable.setWidthPercentage(100);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setColspan(1);
        cell.setPhrase(new Phrase("Comments: ", boldFont));
        commentTable.addCell(cell);
        for (int comment = 0; comment < handler.getReportCommentCount(); comment++) {
            commentPhrase.clear();

            cell.setPaddingLeft(10);
            // CT 5.5.2: order/report-level notes in fixed-width font
            commentPhrase.setFont(commentFont);
            commentPhrase.add(handler.getReportComment(comment));
            commentPhrase.setFont(subscriptFont);
            commentPhrase.add("\t\t" + handler.getReportSourceOrganization(comment));
            cell.setPhrase(commentPhrase);
            commentTable.addCell(cell);
        }


        //Create header info table
        float[] tableWidths = {2f, 3f};
        PdfPTable table = new PdfPTable(tableWidths);
        if (multiID.length > 1) {
            cell = new PdfPCell(new Phrase("Version: " + versionNum + " of " + multiID.length, boldFont));
            cell.setBackgroundColor(new BaseColor(210, 212, 255));
            cell.setPadding(3);
            cell.setColspan(2);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Phrase("Detail Results: Patient Info", boldFont));
        cell.setBackgroundColor(new BaseColor(210, 212, 255));
        cell.setPadding(5);
        table.addCell(cell);
        cell.setPhrase(new Phrase("Results Info", boldFont));
        table.addCell(cell);

        // add the created tables to the document
        table = addTableToTable(table, pInfoTable, 1);
        table = addTableToTable(table, rInfoTable, 1);
        table = addTableToTable(table, clientTable, 2);
        table = addTableToTable(table, commentTable, 2);

        table.setWidthPercentage(100);

        document.add(table);
    }

    /*
     *  addTableToTable(PdfPTable main, PdfPTable add) adds the table 'add' as
     *  a cell spanning 'colspan' columns to the table main.
     */
    private PdfPTable addTableToTable(PdfPTable main, PdfPTable add, int colspan) {
        PdfPCell cell = new PdfPCell(add);
        cell.setPadding(3);
        cell.setColspan(colspan);
        main.addCell(cell);
        return main;
    }

    /*
     *  onEndPage is a page event that occurs when a page has finished being created.
     *  It is used to add header and footer information to each page.
     */
    public void onEndPage(PdfWriter writer, Document document) {
        try {

            Rectangle page = document.getPageSize();
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            int pageNum = document.getPageNumber();
            float width = page.getWidth();
            float height = page.getHeight();

            //add patient name header for every page but the first.
            if (pageNum > 1) {
                cb.beginText();
                cb.setFontAndSize(bf, 8);
                cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, handler.getPatientName(), 575, height - 30, 0);
                cb.endText();

            }

            // OLIS-mandated static report header (CT Tracker req 2.3): bold, top-left, every page.
            BaseFont bfBold = BaseFont.createFont(BaseFont.TIMES_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            cb.beginText();
            cb.setFontAndSize(bfBold, 9);
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Ministry of Health and Long-Term Care", 36, height - 30, 0);
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Ontario Laboratories Information System (OLIS)", 36, height - 41, 0);
            cb.endText();

            // CT 20.2/20.3: who generated the report and when, bottom-left of every page.
            String genStamp = "Generated from OLIS on "
                    + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(generationDate);
            if (!generatedByUser.isEmpty()) {
                genStamp += " by user " + generatedByUser;
            }
            cb.beginText();
            cb.setFontAndSize(bf, 7);
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT, genStamp, 36, 30, 0);
            cb.endText();

            //add footer for every page
            cb.beginText();
            cb.setFontAndSize(bf, 8);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "-" + pageNum + "-", width / 2, 30, 0);
            cb.endText();

            // OLIS-mandated confidentiality footer (CT Tracker req 15.1): italic, centred, every page.
            BaseFont bfItalic = BaseFont.createFont(BaseFont.TIMES_ITALIC, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            cb.beginText();
            cb.setFontAndSize(bfItalic, 8);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "CONFIDENTIAL - report contains Personal Health Information", width / 2, 42, 0);
            cb.endText();


            // add promotext as footer if it is enabled
            if (OscarProperties.getInstance().getProperty("FORMS_PROMOTEXT") != null) {
                cb.beginText();
                cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED), 6);
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, OscarProperties.getInstance().getProperty("FORMS_PROMOTEXT"), width / 2, 19, 0);
                cb.endText();
            }

            // throw any exceptions
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    public String getAddressFieldIfNotNullOrEmpty(HashMap<String, String> address, String key) {
        return getAddressFieldIfNotNullOrEmpty(address, key, true);
    }

    public String getAddressFieldIfNotNullOrEmpty(HashMap<String, String> address, String key, boolean newLine) {
        if (address == null) {
            return "";
        }
        String value = address.get(key);
        if (stringIsNullOrEmpty(value)) {
            return "";
        }
        String result = value + (newLine ? "\n" : "");
        return result;
    }

    public boolean stringIsNullOrEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public String getFullAddress(HashMap<String, String> address) {

        String city = getAddressFieldIfNotNullOrEmpty(address, "City", false);
        String province = getAddressFieldIfNotNullOrEmpty(address, "Province", false);

        String fullAddress = "";
        fullAddress += getAddressFieldIfNotNullOrEmpty(address, "Street Address");
        fullAddress += getAddressFieldIfNotNullOrEmpty(address, "Other Designation");
        fullAddress += getAddressFieldIfNotNullOrEmpty(address, "Postal Code");
        fullAddress += city + ("".equals(city) || "".equals(province) ? "" : ", ") + province + ("".equals(city) && "".equals(province) ? "" : "\n");
        fullAddress += getAddressFieldIfNotNullOrEmpty(address, "Country", false);

        return fullAddress;
    }

    public Phrase getPhone(HashMap<String, String> phone) {

        String phoneNumber = "";
        if (phone.get("email") != null) {
            phoneNumber = phone.get("email");
        } else {
            String countryCode = phone.get("countryCode");
            if (stringIsNullOrEmpty(countryCode)) {
                countryCode = "";
            }

            String localNumber = phone.get("localNumber");
            if (!stringIsNullOrEmpty(localNumber) && localNumber.length() > 4) {
                localNumber = localNumber.substring(0, 3) + "-" + localNumber.substring(3);
            } else {
                localNumber = "";
            }

            String areaCode = phone.get("areaCode");
            if (!stringIsNullOrEmpty(areaCode)) {
                areaCode = " (" + areaCode + ") ";
            } else {
                areaCode = "";
            }

            String extension = phone.get("extension");
            if (!stringIsNullOrEmpty(extension)) {
                extension = " x" + extension;
            } else {
                extension = "";
            }

            phoneNumber = countryCode + areaCode + localNumber + extension;
        }

        return new Phrase(phoneNumber, font);
    }

    public PdfPTable createCollectionTable(Integer obr) {
        PdfPTable collectionTable = new PdfPTable(2);
        //Sets the default cell's border to 0 in case completeRow() needs to add in a cell
        collectionTable.getDefaultCell().setBorder(0);
        //Declares innerTable to keep nice spacing in the cells
        PdfPTable innerTable = new PdfPTable(1);
        //Declares a collectionCell to be used only to add the innerTable to the collection table
        PdfPCell collectionCell;
        //Normal cell to be used for addition to the innerTable
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        //Gets the data from the handler
        String collectionDateTime = handler.getCollectionDateTime(obr);
        String specimenCollectedBy = handler.getSpecimenCollectedBy(obr);
        String collectionVolume = handler.getCollectionVolume(obr);
        String noOfSampleContainers = handler.getNoOfSampleContainers(obr);

        //Checks if the collectionDateTime string is not null
        if (!stringIsNullOrEmpty(collectionDateTime)) {
            //Adds the header and the value of the collection date time
            cell.setPhrase(new Phrase("Collection Date/Time", boldFont));
            innerTable.addCell(cell);
            cell.setPhrase(new Phrase(collectionDateTime, font));
            innerTable.addCell(cell);
            //Adds the inner table to the collectionCell
            collectionCell = new PdfPCell(innerTable);
            collectionCell.setBorder(0);
            //Adds the collectionCell to the collectionTable
            collectionTable.addCell(collectionCell);
        }
        //Checks if the specimen collected by string is not null
        if (!stringIsNullOrEmpty(specimenCollectedBy)) {
            //Resets the inner table
            innerTable = new PdfPTable(1);
            //Adds the header and the value of the specimen collected by
            cell.setPhrase(new Phrase("Specimen Collected By", boldFont));
            innerTable.addCell(cell);
            cell.setPhrase(new Phrase(specimenCollectedBy, font));
            innerTable.addCell(cell);
            //Adds the inner table to the collectionCell
            collectionCell = new PdfPCell(innerTable);
            collectionCell.setBorder(0);
            //Adds the collectionCell to the collectionTable
            collectionTable.addCell(collectionCell);
        }
        //Checks if the collection volume string is not null
        if (!stringIsNullOrEmpty(collectionVolume)) {
            //Resets the inner table
            innerTable = new PdfPTable(1);
            //Adds the header and the value of the collection volume
            cell.setPhrase(new Phrase("Collection Volume", boldFont));
            innerTable.addCell(cell);
            cell.setPhrase(new Phrase(collectionVolume, font));
            innerTable.addCell(cell);
            //Adds the inner table to the collectionCell
            collectionCell = new PdfPCell(innerTable);
            collectionCell.setBorder(0);
            //Adds the collectionCell to the collectionTable
            collectionTable.addCell(collectionCell);
        }
        //Checks if the no. of sample containers string is not null
        if (!stringIsNullOrEmpty(noOfSampleContainers)) {
            //Resets the inner table
            innerTable = new PdfPTable(1);
            //Adds the header and the value of the no. of sample containers
            cell.setPhrase(new Phrase("No. of Sample Containers", boldFont));
            innerTable.addCell(cell);
            cell.setPhrase(new Phrase(noOfSampleContainers, font));
            innerTable.addCell(cell);
            //Adds the inner table to the collectionCell
            collectionCell = new PdfPCell(innerTable);
            collectionCell.setBorder(0);
            //Adds the collectionCell to the collectionTable
            collectionTable.addCell(collectionCell);
        }
        //Completes the current row in case there is only one cell in it
        collectionTable.completeRow();
        //Returns the collection table
        return collectionTable;
    }

    /**
     * Builds the "cc: Client:" phrase from the structured cc-doctor list. Each doctor is
     * rendered via {@link #getDoctorNamePhrase(OLISHL7Handler.DoctorName)} so name and license
     * credential pick up the correct fonts; entries are comma-separated.
     *
     * <p>Replaces the older variant that took a single comma-joined String + re-parsed the
     * embedded {@code <span>} markup back out via Jsoup. With the structured handler API
     * the markup never gets synthesized in the first place.</p>
     */
    private Phrase getCCDocNamesPhrase(List<OLISHL7Handler.DoctorName> ccDocs) {
        Phrase ccDocNames = new Phrase();

        ccDocNames.setFont(boldFont);
        ccDocNames.add("cc: Client:  ");
        ccDocNames.setFont(font);

        for (OLISHL7Handler.DoctorName doc : ccDocs) {
            ccDocNames.addAll(getDoctorNamePhrase(doc).getChunks());
            ccDocNames.add(new Chunk(", ", font));
        }
        if (!ccDocs.isEmpty()) {
            ccDocNames.remove(ccDocNames.size() - 1);
        }

        return ccDocNames;
    }

    /**
     * Builds a {@link Phrase} for a single doctor — name part in the main font, license
     * credential ({@code "MD 109753"} etc.) in {@link #subscriptFont}.
     *
     * <p>Replaces the older variant that took a String + parsed the embedded
     * {@code <span style="margin-left:15px; font-size:8px; color:#333333;">...</span>}
     * back out via Jsoup. The structured form removes that round-trip.</p>
     */
    private Phrase getDoctorNamePhrase(OLISHL7Handler.DoctorName doctor) {
        Phrase doctorPhrase = new Phrase();
        if (doctor == null) return doctorPhrase;

        String namePart = doctor.getNamePart();
        String licensePart = doctor.getLicensePart();

        if (!namePart.isEmpty()) {
            doctorPhrase.add(new Chunk(namePart + " ", font));
        }
        if (!licensePart.isEmpty()) {
            doctorPhrase.add(new Chunk("\t" + licensePart, subscriptFont));
        }
        return doctorPhrase;
    }
}