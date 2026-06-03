//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.hospitalReportManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.URL;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;


//Replaced old CXF FileUtils and DOM parser imports with Java NIO for simpler UTF-8 file reads
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.DemographicCustDao;
import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.dao.IncomingLabRulesDao;
import ca.openosp.openo.commn.dao.PropertyDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.DemographicCust;
import ca.openosp.openo.commn.model.IncomingLabRules;
import ca.openosp.openo.commn.model.Property;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.hospitalReportManager.dao.HRMDocumentDao;
import ca.openosp.openo.hospitalReportManager.dao.HRMDocumentSubClassDao;
import ca.openosp.openo.hospitalReportManager.dao.HRMSubClassDao;
import ca.openosp.openo.hospitalReportManager.dao.HRMDocumentToDemographicDao;
import ca.openosp.openo.hospitalReportManager.dao.HRMDocumentToProviderDao;
import ca.openosp.openo.hospitalReportManager.dao.HRMSendingFacilityDao;
import ca.openosp.openo.hospitalReportManager.model.HRMDocument;
import ca.openosp.openo.hospitalReportManager.model.HRMDocumentSubClass;
import ca.openosp.openo.hospitalReportManager.model.HRMDocumentToDemographic;
import ca.openosp.openo.hospitalReportManager.model.HRMDocumentToProvider;
import ca.openosp.openo.hospitalReportManager.model.HRMSubClass;
import org.owasp.encoder.Encode;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import org.springframework.core.io.ClassPathResource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ca.openosp.openo.commn.model.enumerator.BinaryFileExtension;

import omd.hrm.DateFullOrPartial;
import omd.hrm.OmdCds;
import omd.hrm.ReportContent;
import omd.hrm.ReportFormat;
import omd.hrm.ReportsReceived;

import ca.openosp.OscarProperties;


public class HRMReportParser {

    private static Logger logger = MiscUtils.getLogger();

    private HRMReportParser() {
    }

    public static HRMReport parseReport(LoggedInInfo loggedInInfo, Integer hrmDocumentId) {
        HRMDocumentDao hrmDocumentDao = SpringUtils.getBean(HRMDocumentDao.class);
        HRMDocument hrmDocument = hrmDocumentDao.find(hrmDocumentId);
        if (hrmDocument != null) {
            return parseReport(loggedInInfo, hrmDocument.getReportFile());
        }
        return null;
    }

    public static HRMReport parseReport(LoggedInInfo loggedInInfo, String hrmReportFileLocation) {
        return parseReport(loggedInInfo, hrmReportFileLocation, null);
    }

    /*
     * Called when a report is added to system
     */
    public static HRMReport parseReport(LoggedInInfo loggedInInfo, String hrmReportFileLocation, List<Throwable> errors) {
        OmdCds root = null;

        logger.info("Parsing the Report in the location:" + hrmReportFileLocation);

        String fileData = null;
        // Non-fatal warnings raised during parsing (e.g. an invalid placeholder date substituted
        // with today's date) — carried on the returned HRMReport and surfaced in the upload UI.
        List<String> parseWarnings = new ArrayList<>();
        if (hrmReportFileLocation != null) {
            try {
                // a lot of the parsers need to refer to a file and even when they provide
                // parse(String text) it treats the text as a URL, so we load from disk
                File tmpXMLholder = new File(hrmReportFileLocation);

                // check DOCUMENT_DIR if not found
                if (!tmpXMLholder.exists()) {
                    String place = OscarProperties.getInstance().getDocumentDirectory();
                    tmpXMLholder = new File(place + File.separator + hrmReportFileLocation);
                }

                if (!tmpXMLholder.exists()) {
                    logger.warn("unable to find the HRM report. checked "
                        + hrmReportFileLocation + ", and in the document_dir");
                }

                // read file into UTF-8 String using NIO
                if (tmpXMLholder.exists()) {
                    fileData = Files.readString(
                        tmpXMLholder.toPath(),
                        StandardCharsets.UTF_8
                    );
                    validateNoEmptyElements(tmpXMLholder);
                }

                // Load and compile the XSD schema
                SchemaFactory factory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                URL schemaUrl = new ClassPathResource("/xsd/hrm/1.1.2/ontariomd_hrm.xsd").getURL();
                Schema schema = factory.newSchema(schemaUrl);

                // Replace invalid placeholder dates (e.g. 0-00-00T00:00:00) with today's date so the
                // report parses instead of being rejected; each substitution records a warning.
                Document normalizedDoc = HRMXmlValidator.normalizeInvalidDates(tmpXMLholder, parseWarnings);

                // Unmarshal into JAXB model from the normalized DOM
                JAXBContext jc = JAXBContext.newInstance("omd.hrm");
                Unmarshaller u = jc.createUnmarshaller();
                u.setSchema(schema);
                OmdCds parsed = (OmdCds) u.unmarshal(normalizedDoc);

                validateReportContent(parsed);
                validateDateOfBirth(parsed);
                root = parsed;  // only assign after validation passes

                tmpXMLholder = null;
            } catch (FileNotFoundException e) {
                logger.error("File Not Found " + e);
                if (errors != null) errors.add(e);
            } catch (SAXException e) {
                logger.error("SAX ERROR PARSING XML " + e);
                if (errors != null) errors.add(e);
            } catch (JAXBException e) {
                logger.error("error", e);
                Throwable cause = e.getLinkedException() != null ? e.getLinkedException() : e;
                String msg = cause.getMessage();
                SFTPConnector.notifyHrmError(loggedInInfo, msg);
                if (errors != null) errors.add(cause);
            } catch (IOException e) {
                logger.error("IO error during HRM report parsing: " + e.getMessage(), e);
                if (errors != null) errors.add(e);
            }

            if (root != null && hrmReportFileLocation != null && fileData != null) {
                HRMReport hrmReport = new HRMReport(root, hrmReportFileLocation, fileData);
                hrmReport.addUploadWarnings(parseWarnings);
                return hrmReport;
            }
        }

        return null;
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

    private static void validateNoEmptyElements(File xmlFile) throws SAXException, IOException {
        HRMXmlValidator.validateNoRequiredElementsEmpty(xmlFile);
    }


    public static void addReportToInbox(LoggedInInfo loggedInInfo, HRMReport report) {
        addReportToInbox(loggedInInfo, report, new ArrayList<String>());
    }

    public static void addReportToInbox(LoggedInInfo loggedInInfo, HRMReport report, List<String> warnings) {

        if (report == null) {
            logger.info("addReportToInbox cannot continue, report parameter is null");
            return;
        }

        logger.info("Adding Report to Inbox, for file:" + report.getFileLocation());

        // Surface warnings raised during parsing (e.g. invalid placeholder dates substituted earlier)
        if (warnings != null) {
            warnings.addAll(report.getUploadWarnings());
        }

        addUnknownSendingFacilityWarning(report, warnings);
        addUnknownSubClassWarning(report, warnings);

        HRMDocument document = new HRMDocument();

        File fileLocation = new File(report.getFileLocation());

        document.setReportFile(fileLocation.getName());
        document.setReportStatus(report.getResultStatus());
        document.setReportType(report.getFirstReportClass());
        document.setTimeReceived(new Date());
        document.setSourceFacility(report.getSendingFacilityId());
        document.setSourceFacilityReportNo(report.getSendingFacilityReportNo());

        warnIfSendingFacilityNotRegistered(loggedInInfo, report.getSendingFacilityId());

        String reportFileData = report.getFileData();

        String noMessageIdFileData = reportFileData.replaceAll("<MessageUniqueID>.*?</MessageUniqueID>", "<MessageUniqueID></MessageUniqueID>");
        String noTransactionInfoFileData = reportFileData.replaceAll("<TransactionInformation>.*?</TransactionInformation>", "<TransactionInformation></TransactionInformation>");
        String noDemograhpicInfoFileData = reportFileData.replaceAll("<Demographics>.*?</Demographics>", "<Demographics></Demographics").replaceAll("<MessageUniqueID>.*?</MessageUniqueID>", "<MessageUniqueID></MessageUniqueID>");

        String noMessageIdHash = DigestUtils.md5Hex(noMessageIdFileData);
        String noTransactionInfoHash = DigestUtils.md5Hex(noTransactionInfoFileData);
        String noDemographicInfoHash = DigestUtils.md5Hex(noDemograhpicInfoFileData);

        document.setReportHash(noMessageIdHash);
        document.setReportLessTransactionInfoHash(noTransactionInfoHash);
        document.setReportLessDemographicInfoHash(noDemographicInfoHash);

        document.setReportDate(HRMReportParser.getAppropriateDateFromReport(report));

        document.setDescription("");

        String name = report.getLegalLastName() + ", " + report.getLegalFirstName();
        for (String iName : report.getLegalOtherNames()) {
            name = name + " " + iName;
        }
        document.setFormattedName(name);
        document.setDob(report.getDateOfBirthAsString());
        document.setGender(report.getGender());
        document.setHcn(report.getHCN());

        document.setClassName(report.getFirstReportClass());
        document.setSubClassName(report.getFirstReportSubClass());

        // Auto-categorize at import using the (sending facility, class, sub-class) mapping (HRM02.08).
        // Persisting it here means a null hrmCategoryId reliably identifies a report that did not match
        // any configured category, which the inbox "unmatched category" filter relies on (HRM02.09).
        document.setHrmCategoryId(resolveCategoryId(report));

        document.setRecipientId(report.getDeliverToUserId());
        document.setRecipientName(report.getDeliveryToUserIdFormattedName());

        // We're going to check to see if there's a match in the database already for either of these
        // report hash matches = duplicate report for same recipient
        // no transaction info hash matches = duplicate report, but different recipient
        HRMDocumentDao hrmDocumentDao = (HRMDocumentDao) SpringUtils.getBean(HRMDocumentDao.class);
        List<Integer> exactMatchList = hrmDocumentDao.findByHash(noMessageIdHash);

        if (exactMatchList == null || exactMatchList.size() == 0) {
            List<HRMDocument> sameReportDifferentRecipientReportList = hrmDocumentDao.findByNoTransactionInfoHash(noTransactionInfoHash);

            if (sameReportDifferentRecipientReportList != null && sameReportDifferentRecipientReportList.size() > 0) {
                logger.info("Same Report Different Recipient, for file:" + report.getFileLocation());
                HRMReportParser.routeReportToProvider(sameReportDifferentRecipientReportList.get(0), report);
            } else {
                // New report or changed report
                hrmDocumentDao.persist(document);
                logger.debug("MERGED DOCUMENTS ID" + document.getId());


                String demProviderNo = HRMReportParser.routeReportToDemographic(report, document, warnings);
                HRMReportParser.doSimilarReportCheck(loggedInInfo, report, document);

                PropertyDao propertyDao = SpringUtils.getBean(PropertyDao.class);

                // Link the HRM to the MRP
                boolean providerLinkingRules = propertyDao.isActiveBooleanProperty(Property.PROPERTY_KEY.provider_linking_rules);
                if (providerLinkingRules && demProviderNo != null && !demProviderNo.equals("0")) {
                    routeReportToProvider(document.getId(), demProviderNo);
                }

				// Attempt a route to the provider listed in the report -- if they don't exist, note that in the record
				Boolean routeSuccess = HRMReportParser.routeReportToProvider(report, document.getId(), warnings);
				if (!routeSuccess) {

					logger.info("Adding the provider name to the list of unidentified providers, for file:"+report.getFileLocation());

					// Add the provider name to the list of unidentified providers for this report
					document.setUnmatchedProviders((document.getUnmatchedProviders() != null ? document.getUnmatchedProviders() : "") + "|" + ((report.getDeliverToUserIdLastName()!=null)?report.getDeliverToUserIdLastName() + ", " + report.getDeliverToUserIdFirstName():report.getDeliverToUserId()) + " (" + report.getDeliverToUserId() + ")");
					hrmDocumentDao.merge(document);
					// Route this report to the "system" user so that a search for "all" in the inbox will come up with them
					HRMReportParser.routeReportToProvider(document.getId(), "-1");
				}

                HRMReportParser.routeReportToSubClass(report, document.getId());
            }
        } else if (exactMatchList != null && exactMatchList.size() > 0) {
            // We've seen this one before.  Increment the counter on how many times we've seen it before
            //TODO: do we need to save more info about when we saw the duplicates!
            logger.debug("We've seen this report before. Increment the counter on how many times we've seen it before, for file:" + report.getFileLocation());

            HRMDocument existingDocument = hrmDocumentDao.findById(exactMatchList.get(0)).get(0);
            existingDocument.setNumDuplicatesReceived((existingDocument.getNumDuplicatesReceived() != null ? existingDocument.getNumDuplicatesReceived() : 0) + 1);

            hrmDocumentDao.merge(existingDocument);

            if (warnings != null) {
                warnings.add("This report has already been received and has been flagged as a duplicate.");
            }
        }
    }

    private static String routeReportToDemographic(HRMReport report, HRMDocument mergedDocument, List<String> warnings) {

        if (report == null) {
            logger.info("routeReportToDemographic cannot continue, report parameter is null");
            return null;
        }


        logger.info("Routing Report To Demographic, for file:" + report.getFileLocation());

        // Search the demographics on the system for a likely match and route it to them automatically
        DemographicDao demographicDao = (DemographicDao) SpringUtils.getBean(DemographicDao.class);

        List<Demographic> matchingDemographicListByHin = demographicDao.searchDemographicByHIN(report.getHCN());

        String demProviderNo = null;
        if (matchingDemographicListByHin.size() > 0) {
            if (OscarProperties.getInstance().isPropertyActive("omd_hrm_demo_matching_criteria")) {
                for (Demographic d : matchingDemographicListByHin) {
                    if (report.getGender().equalsIgnoreCase(d.getSex())
                            && report.getDateOfBirthAsString().equalsIgnoreCase(d.getBirthDayAsString())
                            && report.getLegalLastName().equalsIgnoreCase(d.getLastName())) {
                        HRMReportParser.routeReportToDemographic(mergedDocument.getId(), d.getDemographicNo());
                        demProviderNo = d.getProviderNo();
                        break;
                    }
                }
                if (demProviderNo == null) {
                    addStrictMatchMismatchWarnings(report, matchingDemographicListByHin, warnings);
                }
            } else {
                // if there is a matching record assign to variable
                Demographic demographic = matchingDemographicListByHin.get(0); // searchDemographicByHIN typically returns only one result where there is a match
                // if not empty and DOB matches as well, route report to Demographic
                if (report.getDateOfBirthAsString().equalsIgnoreCase(demographic.getBirthDayAsString())) {
                    HRMReportParser.routeReportToDemographic(mergedDocument.getId(), demographic.getDemographicNo());
                    demProviderNo = demographic.getProviderNo();
                }
            }
        }

        return demProviderNo;
    }

    /**
     * UC69/UC70: HCN found a candidate patient but the strict match failed.
     * Emit one warning per field (DateOfBirth, LastName) that disagrees with the
     * report so the uploader can reconcile the mismatch.
     */
    private static void addStrictMatchMismatchWarnings(HRMReport report,
                                                       List<Demographic> candidates,
                                                       List<String> warnings) {
        if (warnings == null || candidates == null || candidates.isEmpty()) return;

        for (Demographic d : candidates) {
            String reportDob = report.getDateOfBirthAsString();
            String reportLastName = report.getLegalLastName();

            boolean dobMatches = reportDob != null && reportDob.equalsIgnoreCase(d.getBirthDayAsString());
            boolean lastNameMatches = reportLastName != null && reportLastName.equalsIgnoreCase(d.getLastName());

            if (!dobMatches) {
                warnings.add("Patient unmatched: DateOfBirth in the report (" + reportDob
                        + ") does not match the patient's DateOfBirth.");
            }
            if (!lastNameMatches) {
                warnings.add("Patient unmatched: LastName in the report (" + reportLastName
                        + ") does not match the patient's LastName.");
            }
            if (!dobMatches || !lastNameMatches) return;
        }
    }


    /**
     * UC65: warn when an HRM report arrives from a Sending Facility that has not
     * been configured on this clinic. "Configured" means at least one HRMSubClass
     * mapping row references the SF ID. Wildcard rows (SF = "*") are ignored.
     */
    private static void addUnknownSendingFacilityWarning(HRMReport report, List<String> warnings) {
        if (warnings == null) return;
        String sf = report.getSendingFacilityId();
        if (sf == null || sf.isEmpty() || "*".equals(sf)) return;

        HRMSubClassDao hrmSubClassDao = SpringUtils.getBean(HRMSubClassDao.class);
        if (!hrmSubClassDao.findBySendingFacilityId(sf).isEmpty()) return;

        warnings.add("Invalid Sending Facility: '" + sf + "' is not configured for this clinic.");
    }

    /**
     * UC66: warn when an HRM report's Class/SubClass combination has no
     * matching HRMSubClass mapping configured on this clinic. For Medical
     * Records reports the SubClass element is stored verbatim as
     * subClassName; for Diagnostic Imaging / Cardio Respiratory reports the
     * accompanying subclasses carry both a name and mnemonic.
     */
    private static void addUnknownSubClassWarning(HRMReport report, List<String> warnings) {
        if (warnings == null) return;
        String className = report.getFirstReportClass();
        if (className == null || className.isEmpty()) return;

        HRMSubClassDao hrmSubClassDao = SpringUtils.getBean(HRMSubClassDao.class);
        String sf = report.getSendingFacilityId();

        boolean isAccompanying = className.equalsIgnoreCase("Diagnostic Imaging Report")
                || className.equalsIgnoreCase("Cardio Respiratory Report");

        if (isAccompanying) {
            List<List<Object>> accompanying = report.getAccompanyingSubclassList();
            if (accompanying == null || accompanying.isEmpty()) return;
            List<Object> first = accompanying.get(0);
            String subClassName = first.size() > 0 ? (String) first.get(0) : null;
            String subClassMnemonic = first.size() > 1 ? (String) first.get(1) : null;
            if (subClassName == null || subClassName.isEmpty()) return;
            if (hrmSubClassDao.findApplicableSubClassMapping(className, subClassName, subClassMnemonic, sf) == null) {
                warnings.add("Unmatched Report SubClass: '" + subClassName
                        + (subClassMnemonic != null && !subClassMnemonic.isEmpty() ? "^" + subClassMnemonic : "")
                        + "' is not configured for this clinic.");
            }
        } else {
            String subClass = report.getFirstReportSubClass();
            if (subClass == null || subClass.isEmpty()) return;
            if (hrmSubClassDao.findApplicableSubClassMapping(className, subClass, null, sf) == null) {
                warnings.add("Unmatched Report SubClass: '" + subClass + "' is not configured for this clinic.");
            }
        }
    }

    /**
     * Resolves the EMR category for a report from its (sending facility, class, sub-class) using the
     * configured HRM category mappings. Mirrors the matching used by {@link #addUnknownSubClassWarning}
     * so the category persisted at import and the "unmatched" determination always agree.
     *
     * @param report HRMReport the parsed HRM report
     * @return Integer the matched HRMCategory id, or null if no configured mapping applies
     */
    private static Integer resolveCategoryId(HRMReport report) {
        String className = report.getFirstReportClass();
        if (className == null || className.isEmpty()) return null;

        HRMSubClassDao hrmSubClassDao = SpringUtils.getBean(HRMSubClassDao.class);
        String sf = report.getSendingFacilityId();

        boolean isAccompanying = className.equalsIgnoreCase("Diagnostic Imaging Report")
                || className.equalsIgnoreCase("Cardio Respiratory Report");

        HRMSubClass subClass;
        if (isAccompanying) {
            List<List<Object>> accompanying = report.getAccompanyingSubclassList();
            if (accompanying == null || accompanying.isEmpty()) return null;
            List<Object> first = accompanying.get(0);
            if (first == null) return null;
            String subClassName = first.size() > 0 ? (String) first.get(0) : null;
            String subClassMnemonic = first.size() > 1 ? (String) first.get(1) : null;
            if (subClassName == null || subClassName.isEmpty()) return null;
            subClass = hrmSubClassDao.findApplicableSubClassMapping(className, subClassName, subClassMnemonic, sf);
        } else {
            String subClassName = report.getFirstReportSubClass();
            if (subClassName == null || subClassName.isEmpty()) return null;
            subClass = hrmSubClassDao.findApplicableSubClassMapping(className, subClassName, null, sf);
        }

        return (subClass != null && subClass.getHrmCategory() != null) ? subClass.getHrmCategory().getId() : null;
    }


    private static boolean hasSameStatus(HRMReport report, HRMReport loadedReport) {
        if (report.getResultStatus() != null) {
            return report.getResultStatus().equalsIgnoreCase(loadedReport.getResultStatus());
        }

        return true;
    }

    /*
     * this only gets called for new or changed reports being added to DB. We already know this isn't
     * an exact duplicate report.
     *
     * 1) If this report was sent to another patient before, then we set the parentId of this report to that one
     *
     */
    private static void doSimilarReportCheck(LoggedInInfo loggedInInfo, HRMReport report, HRMDocument mergedDocument) {

        if (report == null) {
            logger.info("doSimilarReportCheck cannot continue, report parameter is null");
            return;
        }
        logger.info("Identifying if this is a report that we received before, but was sent to the wrong demographic, for file:" + report.getFileLocation());

        HRMDocumentDao hrmDocumentDao = (HRMDocumentDao) SpringUtils.getBean(HRMDocumentDao.class);

        // Check #1: Identify if this is a report that we received before, but was sent to the wrong demographic.
        // we set the parent on those other reports to this one. this way we can display the other versions when viewing.
        List<Integer> parentReportList = hrmDocumentDao.findAllWithSameNoDemographicInfoHash(mergedDocument.getReportLessDemographicInfoHash());
        if (parentReportList != null && parentReportList.size() > 0) {
            for (Integer id : parentReportList) {
                if (id != null && id.intValue() != mergedDocument.getId().intValue()) {
                    mergedDocument.setParentReport(id);
                    hrmDocumentDao.merge(mergedDocument);
                    return;
                }
            }
        }

        // Load all the reports for this demographic into memory -- check by name only
        List<HRMReport> thisDemoHrmReportList = HRMReportParser.loadAllReportsRoutedToDemographic(loggedInInfo, report.getLegalName());

        for (HRMReport loadedReport : thisDemoHrmReportList) {
            boolean hasSameReportContent = report.getFirstReportTextContent().equalsIgnoreCase(loadedReport.getFirstReportTextContent());
            boolean hasSameStatus = hasSameStatus(report, loadedReport);
            boolean hasSameClass = report.getFirstReportClass().equalsIgnoreCase(loadedReport.getFirstReportClass());
            boolean hasSameDate = false;

            hasSameDate = HRMReportParser.getAppropriateDateFromReport(report).equals(HRMReportParser.getAppropriateDateFromReport(loadedReport));

            Integer threshold = 0;

            if (hasSameReportContent)
                threshold += 100;
            else
                threshold += 10;

            if (hasSameStatus)
                threshold += 5;
            else
                threshold += 10;

            if (hasSameClass)
                threshold += 10;
            else
                threshold += 10;

            if (hasSameDate)
                threshold += 20;
            else
                threshold += 5;

            if (threshold >= 45) {
                // This is probably a changed report addressed to the same demographic, so set the parent id (as long as this isn't the same report) and we're done!
                if (loadedReport.getHrmParentDocumentId() != null && loadedReport.getHrmDocumentId().intValue() != mergedDocument.getId().intValue()) {
                    mergedDocument.setParentReport(loadedReport.getHrmParentDocumentId());
                    hrmDocumentDao.merge(mergedDocument);
                    return;
                } else if (loadedReport.getHrmParentDocumentId() == null) {
                    mergedDocument.setParentReport(loadedReport.getHrmDocumentId());
                    hrmDocumentDao.merge(mergedDocument);
                    return;
                }
            }
        }
    }


    private static List<HRMReport> loadAllReportsRoutedToDemographic(LoggedInInfo loggedInInfo, String legalName) {
        DemographicDao demographicDao = (DemographicDao) SpringUtils.getBean(DemographicDao.class);
        HRMDocumentToDemographicDao hrmDocumentToDemographicDao = (HRMDocumentToDemographicDao) SpringUtils.getBean(HRMDocumentToDemographicDao.class);
        HRMDocumentDao hrmDocumentDao = (HRMDocumentDao) SpringUtils.getBean(HRMDocumentDao.class);

        List<Demographic> matchingDemographicListByName = demographicDao.searchDemographic(legalName);

        List<HRMReport> allRoutedReports = new LinkedList<HRMReport>();

        for (Demographic d : matchingDemographicListByName) {
            List<HRMDocumentToDemographic> matchingHrmDocumentList = hrmDocumentToDemographicDao.findByDemographicNo(d.getDemographicNo().toString());
            for (HRMDocumentToDemographic matchingHrmDocument : matchingHrmDocumentList) {
                HRMDocument hrmDocument = hrmDocumentDao.find(matchingHrmDocument.getHrmDocumentId());

                HRMReport hrmReport = HRMReportParser.parseReport(loggedInInfo, hrmDocument.getReportFile());
                if (hrmReport != null) {
                    hrmReport.setHrmDocumentId(hrmDocument.getId());
                    hrmReport.setHrmParentDocumentId(hrmDocument.getParentReport());
                    allRoutedReports.add(hrmReport);
                }
            }
        }

        return allRoutedReports;

    }


    public static void routeReportToSubClass(HRMReport report, Integer reportId) {
        if (report == null) {
            logger.info("routeReportToSubClass cannot continue, report parameter is null");
            return;
        }

        logger.info("Routing Report To SubClass, for file:" + report.getFileLocation());

        HRMDocumentSubClassDao hrmDocumentSubClassDao = (HRMDocumentSubClassDao) SpringUtils.getBean(HRMDocumentSubClassDao.class);

        if (report.getFirstReportClass().equalsIgnoreCase("Diagnostic Imaging Report") || report.getFirstReportClass().equalsIgnoreCase("Cardio Respiratory Report")) {
            List<List<Object>> subClassList = report.getAccompanyingSubclassList();

            boolean firstSubClass = true;

            for (List<Object> subClass : subClassList) {
                HRMDocumentSubClass newSubClass = new HRMDocumentSubClass();

                newSubClass.setSubClass((String) subClass.get(0));
                newSubClass.setSubClassMnemonic((String) subClass.get(1));
                newSubClass.setSubClassDescription((String) subClass.get(2));
                newSubClass.setSubClassDateTime((Date) subClass.get(3));
                newSubClass.setSendingFacilityId(report.getSendingFacilityId());
                if (firstSubClass) {
                    newSubClass.setActive(true);
                    firstSubClass = false;
                }
                newSubClass.setHrmDocumentId(reportId);

                hrmDocumentSubClassDao.merge(newSubClass);
            }
        } else {
            // There aren't subclasses on a Medical Records Report
        }
    }

    public static String getAppropriateDateStringFromReport(HRMReport report) {
        if (report.getFirstReportClass().equalsIgnoreCase("Diagnostic Imaging Report") || report.getFirstReportClass().equalsIgnoreCase("Cardio Respiratory Report")) {
            return (String) report.getAccompanyingSubclassList().get(0).get(4);
        }

        Calendar calendar = report.getFirstReportEventTime();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        sdf.setTimeZone(calendar.getTimeZone());

        return sdf.format(calendar.getTime());
    }

    public static Date getAppropriateDateFromReport(HRMReport report) {
        if (report.getFirstReportClass().equalsIgnoreCase("Diagnostic Imaging Report") || report.getFirstReportClass().equalsIgnoreCase("Cardio Respiratory Report")) {
            return ((Date) (report.getAccompanyingSubclassList().get(0).get(3)));
        }

        // Medical Records Report
        return report.getFirstReportEventTime().getTime();
    }

    public static boolean routeReportToProvider(HRMReport report, Integer reportId) {
        return routeReportToProvider(report, reportId, new ArrayList<String>());
    }

    public static boolean routeReportToProvider(HRMReport report, Integer reportId, List<String> warnings) {
        if (report == null) {
            logger.info("routeReportToProvider cannot continue, report parameter is null");
            return false;
        }

        logger.info("Routing Report to Provider, for file:" + report.getFileLocation());

        HRMDocumentToProviderDao hrmDocumentToProviderDao = SpringUtils.getBean(HRMDocumentToProviderDao.class);
        ProviderDao providerDao = SpringUtils.getBean(ProviderDao.class);
        IncomingLabRulesDao incomingLabRulesDao = SpringUtils.getBean(IncomingLabRulesDao.class);

        String practitionerNo = report.getDeliverToUserId();

        Provider sendToProvider = null;
        if (OscarProperties.getInstance().isPropertyActive("OMD_match_using_OLIS_identifier_type")) {
            if (practitionerNo.startsWith("D")) {
                sendToProvider = providerDao.getProviderByPractitionerNoAndOlisType(practitionerNo.substring(1), "MDL");
            } else if (practitionerNo.startsWith("N")) {
                sendToProvider = providerDao.getProviderByPractitionerNoAndOlisType(practitionerNo.substring(1), "NPL");
            }
        } else {
            sendToProvider = providerDao.getProviderByPractitionerNo(practitionerNo.substring(1));
        }

        // UC73: the practitioner number matched a provider, but the report's <Provider>
        // LastName disagrees with that provider's LastName. The CPSO/CNO likely points
        // at the wrong person — refuse the link and surface a warning instead.
        if (sendToProvider != null) {
            String reportLastName = report.getDeliverToUserIdLastName();
            if (reportLastName != null && !reportLastName.isEmpty()
                    && !reportLastName.equalsIgnoreCase(sendToProvider.getLastName())) {
                if (warnings != null) {
                    warnings.add("Provider unmatched: DeliverToUserID '" + practitionerNo
                            + "' matches a provider whose LastName does not match the report's "
                            + "Provider LastName ('" + reportLastName + "') — verify the DeliverToUserID.");
                }
                sendToProvider = null;
            }
        }

        // UC44: DeliverToUserID prefix (D = physician/CPSO, N = nurse/CNO) must match the
        // matched provider's role. A "D" prefix on a nurse's CNO — or vice versa — is the
        // "switched CPSO with CNO" case: still link the report, but surface a warning.
        if (sendToProvider != null && practitionerNo != null && !practitionerNo.isEmpty() && warnings != null) {
            char prefix = practitionerNo.charAt(0);
            @SuppressWarnings("deprecation")
            String providerType = sendToProvider.getProviderType();
            if (prefix == 'D' && "nurse".equalsIgnoreCase(providerType)) {
                warnings.add("DeliverToUserID '" + practitionerNo + "': practitioner '"
                        + practitionerNo.substring(1) + "' is registered as a nurse (CNO); "
                        + "expected prefix 'N' but got 'D'.");
            } else if (prefix == 'N' && "doctor".equalsIgnoreCase(providerType)) {
                warnings.add("DeliverToUserID '" + practitionerNo + "': practitioner '"
                        + practitionerNo.substring(1) + "' is registered as a physician (CPSO); "
                        + "expected prefix 'D' but got 'N'.");
            }
        }

        // UC45: CPSO and CNO numbers conventionally start with "0"; billing IDs do not.
        // If the DeliverToUserID has a D/N prefix but the remaining ID does not start with "0"
        if (practitionerNo != null && practitionerNo.length() > 1 && warnings != null) {
            char prefix = practitionerNo.charAt(0);
            String idPart = practitionerNo.substring(1);
            if ((prefix == 'D' || prefix == 'N') && !idPart.startsWith("0")) {
                String expected = (prefix == 'D') ? "CPSO" : "CNO";
                warnings.add("DeliverToUserID '" + practitionerNo + "': '" + idPart
                        + "' does not look like a " + expected + " number (expected to start with '0').");
            }
        }

        List<Provider> sendToProviderList = new LinkedList<Provider>();
        if (sendToProvider != null) {
            sendToProviderList.add(sendToProvider);
        }

        if (OscarProperties.getInstance().isPropertyActive("queens_resident_tagging")) {
            DemographicDao demographicDao = (DemographicDao) SpringUtils.getBean(DemographicDao.class);
            List<Demographic> matchingDemographicListByHin = demographicDao.searchDemographicByHIN(report.getHCN());
            if (!matchingDemographicListByHin.isEmpty()) {
                Demographic demographic = demographicDao.searchDemographicByHIN(report.getHCN()).get(0);
                DemographicCustDao demographicCustDao = SpringUtils.getBean(DemographicCustDao.class);
                //add mrp if not already in list
                if (sendToProvider != null && !sendToProvider.getProviderNo().equals(demographic.getProviderNo()) && demographic.getProvider() != null) {
                    sendToProviderList.add(demographic.getProvider());
                }
                //get and add alt providers
                List<DemographicCust> demographicCust = demographicCustDao.findAllByDemographicNumber(demographic.getDemographicNo());
                if (demographicCust.size() > 0) {
                    ArrayList<String> residentIds = new ArrayList<String>();
                    residentIds.add(demographicCust.get(0).getMidwife());
                    residentIds.add(demographicCust.get(0).getNurse());
                    residentIds.add(demographicCust.get(0).getResident());
                    for (String residentId : residentIds) {
                        if (residentId != null && !residentId.equals("")) {
                            Provider p = providerDao.getProvider(residentId);
                            if (p != null) {
                                sendToProviderList.add(p);
                            }
                        }
                    }
                }
            }
        }

        for (Provider p : sendToProviderList) {

            List<HRMDocumentToProvider> existingHRMDocumentToProviders = hrmDocumentToProviderDao.findByHrmDocumentIdAndProviderNoList(reportId, p.getProviderNo());

            if (existingHRMDocumentToProviders == null || existingHRMDocumentToProviders.size() == 0) {
                HRMDocumentToProvider providerRouting = new HRMDocumentToProvider();
                providerRouting.setHrmDocumentId(reportId);

                providerRouting.setProviderNo(p.getProviderNo());
                providerRouting.setSignedOff(0);

                hrmDocumentToProviderDao.merge(providerRouting);
            }

            //Gets the list of IncomingLabRules pertaining to the current providers
            List<IncomingLabRules> incomingLabRules = incomingLabRulesDao.findCurrentByProviderNo(p.getProviderNo());
            //If the list is not null
            if (incomingLabRules != null) {
                //For each labRule in the list
                for (IncomingLabRules labRule : incomingLabRules) {
                    if (labRule.getForwardTypeStrings().contains("HRM")) {
                        //Creates a string of the providers number that the lab will be forwarded to
                        String forwardProviderNumber = labRule.getFrwdProviderNo();
                        //Checks to see if this providers is already linked to this lab
                        HRMDocumentToProvider hrmDocumentToProvider = hrmDocumentToProviderDao.findByHrmDocumentIdAndProviderNo(reportId, forwardProviderNumber);
                        //If a record was not found
                        if (hrmDocumentToProvider == null) {
                            //Puts the information into the HRMDocumentToProvider object
                            hrmDocumentToProvider = new HRMDocumentToProvider();
                            hrmDocumentToProvider.setHrmDocumentId(reportId);
                            hrmDocumentToProvider.setProviderNo(forwardProviderNumber);
                            hrmDocumentToProvider.setSignedOff(0);
                            //Stores it in the table
                            hrmDocumentToProviderDao.persist(hrmDocumentToProvider);
                        }
                    }
                }
            }
        }

        return sendToProviderList.size() > 0;

    }

    public static void setDocumentParent(String reportId, String childReportId) {
        HRMDocumentDao hrmDocumentDao = (HRMDocumentDao) SpringUtils.getBean(HRMDocumentDao.class);
        try {
            HRMDocument childDocument = hrmDocumentDao.find(childReportId);
            childDocument.setParentReport(Integer.parseInt(reportId));

            hrmDocumentDao.merge(childDocument);
        } catch (Exception e) {
            MiscUtils.getLogger().error("Can't set HRM document parent", e);
        }
    }

    public static void routeReportToProvider(HRMDocument originalDocument, HRMReport newReport) {
        routeReportToProvider(newReport, originalDocument.getId());
    }

    public static void routeReportToProvider(Integer reportId, String providerNo) {
        HRMDocumentToProviderDao hrmDocumentToProviderDao = (HRMDocumentToProviderDao) SpringUtils.getBean(HRMDocumentToProviderDao.class);

        // Check if routing already exists
        HRMDocumentToProvider existing = hrmDocumentToProviderDao.findByHrmDocumentIdAndProviderNo(reportId, providerNo);
        if (existing != null) {
            return; // Don't create duplicate
        }

        HRMDocumentToProvider providerRouting = new HRMDocumentToProvider();
        providerRouting.setHrmDocumentId(reportId);
        providerRouting.setProviderNo(providerNo);

        hrmDocumentToProviderDao.merge(providerRouting);

    }

    public static void signOffOnReport(String providerRoutingId, Integer signOffStatus) {
        HRMDocumentToProviderDao hrmDocumentToProviderDao = (HRMDocumentToProviderDao) SpringUtils.getBean(HRMDocumentToProviderDao.class);
        HRMDocumentToProvider providerRouting = hrmDocumentToProviderDao.find(providerRoutingId);

        if (providerRouting != null) {
            providerRouting.setSignedOff(signOffStatus);
            providerRouting.setSignedOffTimestamp(new Date());
            hrmDocumentToProviderDao.merge(providerRouting);
        }
    }

    public static void routeReportToDemographic(Integer reportId, Integer demographicNo) {
        HRMDocumentToDemographicDao hrmDocumentToDemographicDao = (HRMDocumentToDemographicDao) SpringUtils.getBean(HRMDocumentToDemographicDao.class);

        HRMDocumentToDemographic demographicRouting = new HRMDocumentToDemographic();
        demographicRouting.setDemographicNo(demographicNo);
        demographicRouting.setHrmDocumentId(reportId);
        demographicRouting.setTimeAssigned(new Date());

        hrmDocumentToDemographicDao.merge(demographicRouting);

    }

    private static void warnIfSendingFacilityNotRegistered(LoggedInInfo loggedInInfo, String sendingFacilityId) {
        if (sendingFacilityId == null || sendingFacilityId.trim().isEmpty()) {
            return;
        }
        try {
            HRMSendingFacilityDao dao = SpringUtils.getBean(HRMSendingFacilityDao.class);
            if (dao.findBySendingFacilityId(sendingFacilityId) == null) {
                // Encode the facility ID (sourced from the HRM XML) before logging/notifying to prevent log injection.
                String safeSf = Encode.forJava(sendingFacilityId);
                logger.warn("HRM report received from unregistered Sending Facility '"
                        + safeSf + "'. Add it via Admin → Integration → Hospital Report Manager (HRM) Sending Facilities"
                        + " to enable facility-name display on reports.");
                SFTPConnector.notifyHrmAdmin(loggedInInfo, "Unregistered HRM Sending Facility",
                        "OpenO received an HRM report from an unregistered Sending Facility: " + safeSf
                                + ".\n\nThe report was processed normally. Register this facility via Admin → Integration → Hospital Report Manager (HRM) Sending Facilities to enable name resolution on display.");
            }
        } catch (Exception e) {
            logger.warn("Could not check HRMSendingFacility registry for '" + Encode.forJava(sendingFacilityId) + "'", e);
        }
    }
}
