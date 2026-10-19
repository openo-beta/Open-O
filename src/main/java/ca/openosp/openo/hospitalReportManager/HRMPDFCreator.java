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
package ca.openosp.openo.hospitalReportManager;


import com.itextpdf.text.Element;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.codec.TiffImage;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.hospitalReportManager.dao.HRMDocumentDao;

import ca.openosp.openo.hospitalReportManager.dao.HRMProviderConfidentialityStatementDao;
import ca.openosp.openo.hospitalReportManager.dao.HRMSendingFacilityDao;

import ca.openosp.openo.hospitalReportManager.model.HRMDocument;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import ca.openosp.openo.documentManager.ConvertToEdoc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;

public class HRMPDFCreator extends PdfPageEventHelper {
    private Logger logger = MiscUtils.getLogger();
    private OutputStream outputStream;
    private HRMDocument hrmDocument;
    private HRMReport hrmReport;
    private Document document;
    private LoggedInInfo loggedInInfo;


    public HRMPDFCreator(OutputStream outputStream, Integer hrmId, LoggedInInfo loggedInInfo) {

        //Gets the HRMDocumentDao
        HRMDocumentDao hrmDocumentDao = SpringUtils.getBean(HRMDocumentDao.class);
        //Stores the output stream and hrmId
        this.outputStream = outputStream;
        this.loggedInInfo = loggedInInfo;

        try {
            //Gets the HRMDocument by the provided Id

            List<HRMDocument> hrmDocuments = hrmDocumentDao.findById(hrmId);

            //If the list is not null and it has items in it
            if (hrmDocuments != null && hrmDocuments.size() > 0) {
                hrmDocument = hrmDocuments.get(0);

                logger.info("Parsing the HRM Document into a report for printing");
                //Gets and parses the HRMReport storing it in the class variable
                hrmReport = HRMReportParser.parseReport(loggedInInfo, hrmDocument.getReportFile());
            }
        } catch (NumberFormatException e) {
            logger.error("HRM Id is not a valid integer", e);
        }
    }

    public void printPdf() {
        try {
            if (!hrmReport.isBinary()) {
                document = new Document();

                PdfWriter writer = PdfWriter.getInstance(document, outputStream);
                writer.setPageEvent(this);

                document.setPageSize(PageSize.LETTER);

                document.open();

                if (hrmReport != null) {
                    generateHRMReport(hrmReport);
                } else {
                    logger.error("There is no HRM Report");
                }

                document.close();
            } else {
                outputStream.write(renderAttachment());
            }

            outputStream.flush();
        } catch (IOException e) {
            logger.error("An I/O Exception has occurred while either getting a PDFWriter Instance or creating the BaseFont", e);
        } catch (DocumentException e) {
            logger.error("A Document Exception occurred while either getting a PDFWriter or creating the BaseFont", e);
        }
    }

    /**
     * Tells whether the attachment is an image iText can read directly.
     *
     * @param extension String the FileExtensionAndVersion value from the report, for example ".jpeg"
     * @return boolean true for gif, jpg, jpeg and png
     */
    private static boolean isImageExtension(String extension) {
        return ".gif".equals(extension) || ".jpg".equals(extension) || ".jpeg".equals(extension) || ".png".equals(extension);
    }

    /**
     * Opens a letter-size PDF on the given stream with this class as page event handler.
     *
     * @param target   OutputStream where the PDF bytes go
     * @param fullPage boolean true removes the margins so an image can use the whole page
     * @throws DocumentException if the writer cannot be created
     */
    private void openDocument(OutputStream target, boolean fullPage) throws DocumentException {
        document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, target);
        writer.setPageEvent(this);
        document.setPageSize(PageSize.LETTER);
        if (fullPage) {
            document.setMargins(0, 0, 0, 0);
        }
        document.open();
    }

    /**
     * Builds the confidentiality statement block printed under an image page.
     *
     * @return PdfPTable the statement block, or null when the printing provider has no statement
     * @throws IOException       if the font cannot be created
     * @throws DocumentException if the font cannot be created
     */
    private PdfPTable buildConfidentialityStatementTable() throws IOException, DocumentException {
        HRMProviderConfidentialityStatementDao hrmProviderConfidentialityStatementDao = SpringUtils.getBean(HRMProviderConfidentialityStatementDao.class);
        String confidentialityStatement = hrmProviderConfidentialityStatementDao.getConfidentialityStatementForProvider(loggedInInfo.getLoggedInProviderNo());
        if (confidentialityStatement == null || confidentialityStatement.trim().length() == 0) {
            return null;
        }
        BaseFont baseFont = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        Font font = new Font(baseFont, 10, Font.NORMAL);
        Font italicFont = new Font(baseFont, 10, Font.ITALIC);
        PdfPCell separator = new PdfPCell();
        separator.setBorder(PdfPCell.BOTTOM);
        separator.setFixedHeight(0.1f);
        PdfPCell space = new PdfPCell();
        space.setBorder(0);
        space.setFixedHeight(2f);
        PdfPTable table = new PdfPTable(1);
        table.setTotalWidth(PageSize.LETTER.getWidth());
        table.setLockedWidth(true);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        table.addCell(separator);
        cell.setPhrase(new Phrase("Confidentiality Statement:", italicFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase(confidentialityStatement, font));
        table.addCell(cell);
        table.addCell(space);
        return table;
    }

    /**
     * Adds one image to the current page, scaled to the page and rotated when it is wider than tall,
     * with the confidentiality statement under it.
     *
     * @param image                        Image the page content
     * @param confidentialityStatementTable PdfPTable the statement block, or null to print none
     * @throws DocumentException if the image cannot be added
     */
    private void addImagePage(Image image, PdfPTable confidentialityStatementTable) throws DocumentException {
        float footerHeight = confidentialityStatementTable != null ? confidentialityStatementTable.getTotalHeight() + 5 : 0f;
        image.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight() - footerHeight);
        if (image.getScaledWidth() >= document.getPageSize().getWidth() && image.getScaledHeight() <= document.getPageSize().getWidth()) {
            image.setRotationDegrees(90);
            image.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight() - footerHeight);
        }
        document.add(image);
        if (confidentialityStatementTable != null) {
            document.add(confidentialityStatementTable);
        }
    }

    /**
     * Reads the plain text out of an RTF attachment.
     *
     * @param rtf byte[] the decoded attachment
     * @return String the text without RTF markup
     * @throws IOException if the RTF cannot be parsed
     */
    private static String extractRtfText(byte[] rtf) throws IOException {
        RTFEditorKit kit = new RTFEditorKit();
        javax.swing.text.Document textDocument = kit.createDefaultDocument();
        try (InputStream in = new ByteArrayInputStream(rtf)) {
            kit.read(in, textDocument, 0);
            return textDocument.getText(0, textDocument.getLength());
        } catch (BadLocationException e) {
            throw new IOException("Could not read the RTF text", e);
        }
    }

    /**
     * Builds a one-page PDF saying the attachment cannot be printed, so the merge still gets a PDF.
     *
     * @param extension String the attachment extension named in the notice
     * @return byte[] the notice page
     * @throws DocumentException if the page cannot be written
     */
    private byte[] unsupportedAttachmentPage(String extension) throws DocumentException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        openDocument(buffer, false);
        document.add(new Paragraph("This report has a " + extension + " attachment that cannot be printed. Open the report and use the download link."));
        document.close();
        return buffer.toByteArray();
    }

    /**
     * Turns the binary attachment into PDF bytes: pdf as received, gif/jpg/jpeg/png and tiff as image pages,
     * rtf as a text report, html through the eForm converter. Anything else, or an attachment that fails to
     * render, becomes a one-page notice so the caller always gets a PDF.
     *
     * @return byte[] a complete PDF
     * @throws IOException       if the notice page cannot be written
     * @throws DocumentException if the notice page cannot be written
     */
    private byte[] renderAttachment() throws IOException, DocumentException {
        String extension = hrmReport.getFileExtension();
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            if (".pdf".equals(extension)) {
                // The PDF is printed as it was received, once it is known to open as a PDF
                byte[] pdf = hrmReport.getBinaryContent();
                new PdfReader(pdf).close();
                return pdf;
            } else if (isImageExtension(extension)) {
                openDocument(buffer, true);
                addImagePage(Image.getInstance(hrmReport.getBinaryContent()), buildConfidentialityStatementTable());
                document.close();
            } else if (".tiff".equals(extension)) {
                openDocument(buffer, true);
                PdfPTable confidentialityStatementTable = buildConfidentialityStatementTable();
                // A TIFF file can hold several pages; each one becomes a page of the PDF
                RandomAccessFileOrArray tiff = new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(hrmReport.getBinaryContent()));
                int pageCount = TiffImage.getNumberOfPages(tiff);
                for (int page = 1; page <= pageCount; page++) {
                    if (page > 1) {
                        document.newPage();
                    }
                    addImagePage(TiffImage.getTiffImage(tiff, page), confidentialityStatementTable);
                }
                document.close();
            } else if (".rtf".equals(extension)) {
                // The RTF text is laid out like a text report, with the statement and the metadata box
                openDocument(buffer, false);
                generateHRMReport(hrmReport, extractRtfText(hrmReport.getBinaryContent()));
                document.close();
            } else if (".html".equals(extension)) {
                EFormData eFormData = new EFormData();
                eFormData.setFormData(new String(hrmReport.getBinaryContent(), StandardCharsets.UTF_8));
                return Files.readAllBytes(ConvertToEdoc.saveAsTempPDF(eFormData));
            } else {
                // Not a PDF; a raw copy would break the PDF merge in PrintHRMReport2Action
                return unsupportedAttachmentPage(extension);
            }
            return buffer.toByteArray();
        } catch (Exception e) {
            logger.error("Could not render the " + extension + " attachment of HRM document " + hrmDocument.getId() + " for printing", e);
            return unsupportedAttachmentPage(extension);
        }
    }

    private void generateHRMReport(HRMReport hrmReport) throws IOException, DocumentException {
        generateHRMReport(hrmReport, hrmReport.getFirstReportTextContent());
    }

    /**
     * Lays out the report header, the given body text, the confidentiality statement and the metadata box.
     *
     * @param hrmReport  HRMReport the report being printed
     * @param reportText String the text to print as the report body
     * @throws IOException       if the font cannot be created
     * @throws DocumentException if the table cannot be added to the PDF
     */
    private void generateHRMReport(HRMReport hrmReport, String reportText) throws IOException, DocumentException {
        BaseFont baseFont = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        Font font = new Font(baseFont, 10, Font.NORMAL);
        Font italicFont = new Font(baseFont, 10, Font.ITALIC);
        Font boldFont = new Font(baseFont, 10, Font.BOLD);

        //Creates a cell to be used to insert a small amount of space
        PdfPCell space = new PdfPCell();
        space.setBorder(0);
        space.setFixedHeight(2f);

        //Creates a cell to be used as a separator line
        PdfPCell separator = new PdfPCell();
        separator.setBorder(PdfPCell.BOTTOM);
        separator.setFixedHeight(0.1f);

        //Creates the main page table
        PdfPTable mainPage = new PdfPTable(1);
        mainPage.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);

        //Creates the Demographic Info area
        cell.setPhrase(new Phrase("Demographic Info:", boldFont));
        mainPage.addCell(cell);

        cell.setPhrase(new Phrase(hrmReport.getLegalName(), font));
        mainPage.addCell(cell);

        cell.setPhrase(new Phrase(hrmReport.getHCN() + " " + hrmReport.getHCNVersion() + " " + hrmReport.getGender(), font));
        mainPage.addCell(cell);

        cell.setPhrase(new Phrase("DOB: " + hrmReport.getDateOfBirthAsString(), font));
        mainPage.addCell(cell);

        //Adds a spacing cell
        mainPage.addCell(space);

        //Outputs the when the HRM report was received
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPhrase(new Phrase("This report was received from the Hospital Report Manager (HRM) at " + hrmDocument.getTimeReceived(), italicFont));
        mainPage.addCell(cell);

        //Adds a separator and spacing
        mainPage.addCell(separator);
        mainPage.addCell(space);

        //If the report is binary, outputs a message letting the user know that there was an attachment
        if (hrmReport.isBinary()) {
            cell.setPhrase(new Phrase("This report contains an attachment", font));
            mainPage.addCell(cell);
        }

        //Outputs the main content from the report
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPhrase(new Phrase(reportText, font));
        mainPage.addCell(cell);
        mainPage.addCell(space);

        //Add confidentiality statement to PDF
        HRMProviderConfidentialityStatementDao hrmProviderConfidentialityStatementDao = SpringUtils.getBean(HRMProviderConfidentialityStatementDao.class);
        String confidentialityStatement = hrmProviderConfidentialityStatementDao.getConfidentialityStatementForProvider(loggedInInfo.getLoggedInProviderNo());
        if (confidentialityStatement != null && confidentialityStatement.trim().length() > 0) {
            mainPage.addCell(separator);
            cell.setPhrase(new Phrase("Confidentiality Statement:", italicFont));
            mainPage.addCell(cell);
            cell.setPhrase(new Phrase(confidentialityStatement, font));
            mainPage.addCell(cell);
            mainPage.addCell(space);
        }

        //Creates a box at the bottom of the report that contains the metadata
        float[] metaDataBoxWidths = {1f, 2f};
        PdfPTable metaDataBox = new PdfPTable(metaDataBoxWidths);

        cell.setPhrase(new Phrase("Message Unique ID: ", boldFont));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaDataBox.addCell(cell);

        cell.setPhrase(new Phrase(hrmReport.getMessageUniqueId(), font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        metaDataBox.addCell(cell);

        cell.setPhrase(new Phrase("Sending Author: ", boldFont));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaDataBox.addCell(cell);

        cell.setPhrase(new Phrase(hrmReport.getSendingAuthor(), font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        metaDataBox.addCell(cell);

        cell.setPhrase(new Phrase("Sending Facility: ", boldFont));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaDataBox.addCell(cell);

        HRMSendingFacilityDao hrmSendingFacilityDao = SpringUtils.getBean(HRMSendingFacilityDao.class);
        cell.setPhrase(new Phrase(hrmSendingFacilityDao.getDisplayName(hrmReport.getSendingFacilityId()), font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        metaDataBox.addCell(cell);

        cell.setPhrase(new Phrase("Sending Facility Report No.: ", boldFont));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaDataBox.addCell(cell);

        cell.setPhrase(new Phrase(hrmReport.getSendingFacilityReportNo(), font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        metaDataBox.addCell(cell);

        cell.setPhrase(new Phrase("Date and Time of Report: ", boldFont));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaDataBox.addCell(cell);

        cell.setPhrase(new Phrase(HRMReportParser.getAppropriateDateStringFromReport(hrmReport), font));

        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        metaDataBox.addCell(cell);

        cell.setPhrase(new Phrase("Result Status: ", boldFont));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaDataBox.addCell(cell);

        String resultStatus = hrmReport.getResultStatus() != null && hrmReport.getResultStatus().equalsIgnoreCase("C") ? "Cancelled" : "Signed by the responsible author and Released by health records";
        cell.setPhrase(new Phrase(resultStatus, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        metaDataBox.addCell(cell);

        cell = new PdfPCell(metaDataBox);
        cell.setBorder(15);

        //Adds the metaDataBox to the main page
        mainPage.addCell(cell);

        //Adds the table to the document
        document.add(mainPage);
    }
}
