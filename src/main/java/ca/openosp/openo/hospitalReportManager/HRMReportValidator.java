package ca.openosp.openo.hospitalReportManager;

import java.time.DateTimeException;
import java.time.LocalDate;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.xml.sax.SAXException;

import ca.openosp.openo.commn.model.enumerator.BinaryFileExtension;
import omd.hrm.DateFullOrPartial;
import omd.hrm.OmdCds;
import omd.hrm.ReportContent;
import omd.hrm.ReportFormat;
import omd.hrm.ReportsReceived;

/**
 * Pure, stateless validation of parsed HRM XML reports. Separated from {@link HRMReportParser}
 * so that the main parsing flow stays linear and these domain checks can evolve independently.
 *
 * @since 2026-06-05
 */
public final class HRMReportValidator {

    private HRMReportValidator() {
    }

    /**
     * Runs all post-unmarshal validations against the parsed OmdCds root.
     *
     * @param root OmdCds the unmarshalled report root
     * @throws SAXException if any validation rule is violated
     */
    public static void validate(OmdCds root) throws SAXException {
        validateReportContent(root);
        validateDateOfBirth(root);
    }

    private static void validateReportContent(OmdCds root) throws SAXException {
        if (root == null || root.getPatientRecord() == null) return;
        for (ReportsReceived report : root.getPatientRecord().getReportsReceived()) {
            if (ReportFormat.BINARY.equals(report.getFormat())) {
                validateBinaryReport(report);
            } else if (ReportFormat.TEXT.equals(report.getFormat())) {
                validateTextReport(report);
            }
        }
    }

    private static void validateBinaryReport(ReportsReceived report) throws SAXException {
        String ext = report.getFileExtensionAndVersion();
        BinaryFileExtension type = BinaryFileExtension.fromExtension(ext);
        if (type == null) {
            throw new SAXException("Invalid content found: <FileExtensionAndVersion> is '" + ext + "'"
                + " which is not a valid binary extension; supported: " + BinaryFileExtension.allValues());
        }
        ReportContent content = report.getContent();
        if (content != null && content.getMedia() != null && !type.matchesContent(content.getMedia())) {
            throw new SAXException("Invalid content found: <FileExtensionAndVersion> is '" + ext + "'"
                + " but <Content> does not match the expected " + ext + " format");
        }
    }

    private static void validateTextReport(ReportsReceived report) throws SAXException {
        String ext = report.getFileExtensionAndVersion();
        if (ext != null && !"From OMD Report Manager".equals(ext)) {
            throw new SAXException("Invalid content found: <FileExtensionAndVersion> is '" + ext + "'"
                + " for Format=Text; expected: 'From OMD Report Manager'");
        }
    }

    private static void validateDateOfBirth(OmdCds root) throws SAXException {
        if (root == null || root.getPatientRecord() == null
                || root.getPatientRecord().getDemographics() == null) return;

        DateFullOrPartial dob = root.getPatientRecord().getDemographics().getDateOfBirth();
        if (dob == null) return;

        XMLGregorianCalendar dobCal;
        if (dob.getDateTime() != null) dobCal = dob.getDateTime();
        else if (dob.getFullDate() != null) dobCal = dob.getFullDate();
        else if (dob.getYearMonth() != null) dobCal = dob.getYearMonth();
        else if (dob.getYearOnly() != null) dobCal = dob.getYearOnly();
        else return;

        int year = dobCal.getYear();
        if (year < 1800) {
            throw new SAXException("Invalid content found: <DateOfBirth> year " + year + " is before 1800");
        }

        int month = (dobCal.getMonth() == DatatypeConstants.FIELD_UNDEFINED) ? 1 : dobCal.getMonth();
        int day   = (dobCal.getDay()   == DatatypeConstants.FIELD_UNDEFINED) ? 1 : dobCal.getDay();
        LocalDate dobDate;
        try {
            dobDate = LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new SAXException("Invalid content found: <DateOfBirth> contains invalid date: "
                    + year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day));
        }
        if (dobDate.isAfter(LocalDate.now())) {
            throw new SAXException("Invalid content found: <DateOfBirth> " + dobDate + " is in the future");
        }
    }
}
