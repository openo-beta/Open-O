package ca.openosp.openo.olis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.SAXParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.olis.dao.OLISRequestNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISResultNomenclatureDao;
import ca.openosp.openo.olis.model.OLISRequestNomenclature;
import ca.openosp.openo.olis.model.OLISResultNomenclature;
import ca.openosp.openo.util.UtilXML;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PathValidationUtils;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Admin action: imports the official eHealth Ontario OLIS Nomenclatures XLSX
 * distribution into the local {@code OLISResultNomenclature} +
 * {@code OLISRequestNomenclature} tables.
 * <p>
 * Parses the XLSX with stdlib {@code java.util.zip} + SAX (no Apache POI
 * dependency required) and upserts row-by-row, deriving:
 * <ul>
 * <li>{@code nameId}            from "LOINC Code" / "OLIS Test Request Code"</li>
 * <li>{@code name}              from "Result Alternate Name 1" / "Request Alternate Name 1"
 *                               (per OLIS Interface Spec §6.7.1.2 — "Alternate Name 1" is the
 *                               suggested local display name)</li>
 * <li>{@code effectiveDate}     from "Effective Date" (Excel serial date or ISO string)</li>
 * <li>{@code endDate}           from "End Date"</li>
 * <li>{@code status}            from "Validation Status Indicator"
 *                               (ACTIVE/INACTIVE)</li>
 * <li>{@code externalCodeVersion} from "External Code Version"</li>
 * <li>{@code category}          from "Test Request Category" (Request rows only)</li>
 * </ul>
 * <p>
 * Operational context: each eHealth Ontario release notice imposes a ~7-day
 * "review and remap by" window before non-conforming senders begin to have OLIS
 * messages rejected. This action is the primary mechanism to keep OpenO
 * current within that window. See {@code docs/olis/readiness-plan.md} D2a for
 * the design rationale.
 *
 * @since 2026-05-15
 */
public class OLISNomenclatureImport2Action extends ActionSupport implements UploadedFilesAware {

    private static final org.apache.logging.log4j.Logger LOG = MiscUtils.getLogger();

    private HttpServletRequest request = ServletActionContext.getRequest();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    private UploadedFile uploadedXlsx;
    private File xlsxOnDisk;
    private String xlsxFileName;

    private ImportReport resultReport;
    private ImportReport requestReport;
    private String errorMessage;

    @Override
    public void withUploadedFiles(List<UploadedFile> uploadedFiles) {
        if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
            this.uploadedXlsx = uploadedFiles.get(0);
            this.xlsxOnDisk = PathValidationUtils.toFile(uploadedXlsx);
            this.xlsxFileName = uploadedXlsx.getOriginalName();
        }
    }

    public String execute() {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_admin", "w", null)) {
            throw new SecurityException("missing required sec object");
        }

        // Form view
        if (xlsxOnDisk == null) {
            return "form";
        }

        try (ZipFile zip = new ZipFile(xlsxOnDisk)) {
            List<String> sharedStrings = readSharedStrings(zip);
            Map<String, String> sheetNameToPath = readWorkbookSheetMap(zip);

            String resultSheetPath = sheetNameToPath.get("Test Result Nomenclatures");
            String requestSheetPath = sheetNameToPath.get("Test Request Nomenclature");

            if (resultSheetPath == null || requestSheetPath == null) {
                errorMessage = "Uploaded file does not look like an OLIS Nomenclatures distribution "
                        + "(missing the 'Test Result Nomenclatures' or 'Test Request Nomenclature' sheet).";
                return "form";
            }

            OLISResultNomenclatureDao resultDao = SpringUtils.getBean(OLISResultNomenclatureDao.class);
            OLISRequestNomenclatureDao requestDao = SpringUtils.getBean(OLISRequestNomenclatureDao.class);

            resultReport = importResults(zip, resultSheetPath, sharedStrings, resultDao);
            requestReport = importRequests(zip, requestSheetPath, sharedStrings, requestDao);
            LOG.info("OLIS nomenclature import — results: " + resultReport
                    + "; requests: " + requestReport);
            request.setAttribute("resultReport", resultReport);
            request.setAttribute("requestReport", requestReport);
            request.setAttribute("xlsxFileName", xlsxFileName);
            return "report";
        } catch (Exception e) {
            LOG.error("OLIS nomenclature import failed", e);
            errorMessage = "Import failed: " + e.getClass().getSimpleName() + " — " + e.getMessage();
            request.setAttribute("errorMessage", errorMessage);
            return "form";
        }
    }

    private ImportReport importResults(ZipFile zip, String sheetPath, List<String> sharedStrings,
                                       final OLISResultNomenclatureDao dao) throws Exception {
        final ImportReport rep = new ImportReport();
        streamRows(zip, sheetPath, sharedStrings, new RowConsumer() {
            @Override
            public void accept(Map<String, String> row) {
                String nameId = trimToNull(row.get("LOINC Code"));
                if (nameId == null) {
                    return;
                }
                OLISResultNomenclature existing = findByNameId(dao, nameId);
                boolean isNew = (existing == null);
                OLISResultNomenclature entity = isNew ? new OLISResultNomenclature() : existing;
                if (isNew) {
                    entity.setNameId(nameId);
                }
                entity.setName(StringUtils.defaultString(row.get("Result Alternate Name 1")).trim());
                entity.setEffectiveDate(parseDateCell(row.get("Effective Date")));
                entity.setEndDate(parseDateCell(row.get("End Date")));
                String newStatus = deriveStatus(row);
                boolean wasActive = "ACTIVE".equals(entity.getStatus());
                entity.setStatus(newStatus);
                entity.setExternalCodeVersion(trimToNull(row.get("External Code Version")));
                if (isNew) {
                    dao.persist(entity);
                    rep.added++;
                } else {
                    dao.merge(entity);
                    if (wasActive && !"ACTIVE".equals(newStatus)) {
                        rep.deprecated++;
                    } else {
                        rep.updated++;
                    }
                }
            }
        });
        return rep;
    }

    private ImportReport importRequests(ZipFile zip, String sheetPath, List<String> sharedStrings,
                                        final OLISRequestNomenclatureDao dao) throws Exception {
        final ImportReport rep = new ImportReport();
        streamRows(zip, sheetPath, sharedStrings, new RowConsumer() {
            @Override
            public void accept(Map<String, String> row) {
                String nameId = trimToNull(row.get("OLIS Test Request Code"));
                if (nameId == null) {
                    return;
                }
                OLISRequestNomenclature existing = findByNameId(dao, nameId);
                boolean isNew = (existing == null);
                OLISRequestNomenclature entity = isNew ? new OLISRequestNomenclature() : existing;
                if (isNew) {
                    entity.setNameId(nameId);
                }
                entity.setName(StringUtils.defaultString(row.get("Request Alternate Name 1")).trim());
                entity.setCategory(trimToNull(row.get("Test Request Category")));
                entity.setEffectiveDate(parseDateCell(row.get("Effective Date")));
                entity.setEndDate(parseDateCell(row.get("End Date")));
                String newStatus = deriveStatus(row);
                boolean wasActive = "ACTIVE".equals(entity.getStatus());
                entity.setStatus(newStatus);
                entity.setExternalCodeVersion(trimToNull(row.get("External Code Version")));
                if (isNew) {
                    dao.persist(entity);
                    rep.added++;
                } else {
                    dao.merge(entity);
                    if (wasActive && !"ACTIVE".equals(newStatus)) {
                        rep.deprecated++;
                    } else {
                        rep.updated++;
                    }
                }
            }
        });
        return rep;
    }

    private static OLISResultNomenclature findByNameId(OLISResultNomenclatureDao dao, String nameId) {
        try {
            return dao.findByNameId(nameId);
        } catch (Exception e) {
            return null;
        }
    }

    private static OLISRequestNomenclature findByNameId(OLISRequestNomenclatureDao dao, String nameId) {
        try {
            return dao.findByNameId(nameId);
        } catch (Exception e) {
            return null;
        }
    }

    private static String deriveStatus(Map<String, String> row) {
        String validation = trimToNull(row.get("Validation Status Indicator"));
        if (validation != null && validation.equalsIgnoreCase("INACTIVE")) {
            return "INACTIVE";
        }
        String workflow = trimToNull(row.get("Workflow Status Indicator"));
        if (workflow != null && !workflow.equalsIgnoreCase("RELEASED")) {
            return "INACTIVE";
        }
        return "ACTIVE";
    }

    private static Date parseDateCell(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        // Try Excel numeric serial (days since 1899-12-30, with Excel's 1900 leap-year quirk).
        try {
            double serial = Double.parseDouble(s);
            LocalDate epoch = LocalDate.of(1899, 12, 30);
            LocalDate ld = epoch.plusDays((long) serial);
            return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (NumberFormatException ignored) {
            // not numeric — fall through to ISO parse
        }
        // Some sheets store the date as a string. Try the OLIS-observed formats only,
        // in non-lenient mode so an ambiguous "3/9/2023" against yyyy/MM/dd doesn't get
        // normalized into year 9 (it must hit M/d/yyyy as 2023-03-09).
        String[] patterns = new String[]{"yyyy-MM-dd", "M/d/yyyy"};
        for (String p : patterns) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(p, Locale.CANADA);
                fmt.setLenient(false);
                return fmt.parse(s);
            } catch (Exception ignored) {
                // try next
            }
        }
        return null;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // ---------- XLSX (zip + SAX) ----------

    private static List<String> readSharedStrings(ZipFile zip) throws Exception {
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) {
            return new ArrayList<String>();
        }
        final List<String> strings = new ArrayList<String>();
        SAXParser parser = UtilXML.newSecureSAXParser();
        try (InputStream in = zip.getInputStream(entry)) {
            parser.parse(new InputSource(in), new DefaultHandler() {
                private StringBuilder buf = new StringBuilder();
                private boolean inT = false;
                private boolean inSi = false;
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    if ("si".equals(localName) || qName.endsWith(":si") || "si".equals(qName)) {
                        buf.setLength(0);
                        inSi = true;
                    } else if ("t".equals(localName) || qName.endsWith(":t") || "t".equals(qName)) {
                        inT = inSi;
                    }
                }
                @Override
                public void characters(char[] ch, int start, int length) {
                    if (inT) {
                        buf.append(ch, start, length);
                    }
                }
                @Override
                public void endElement(String uri, String localName, String qName) {
                    if ("si".equals(localName) || qName.endsWith(":si") || "si".equals(qName)) {
                        strings.add(buf.toString());
                        inSi = false;
                    } else if ("t".equals(localName) || qName.endsWith(":t") || "t".equals(qName)) {
                        inT = false;
                    }
                }
            });
        }
        return strings;
    }

    private static Map<String, String> readWorkbookSheetMap(ZipFile zip) throws Exception {
        // workbook.xml lists each sheet with name + r:id; workbook.xml.rels maps r:id → target path.
        ZipEntry wbEntry = zip.getEntry("xl/workbook.xml");
        ZipEntry relsEntry = zip.getEntry("xl/_rels/workbook.xml.rels");
        if (wbEntry == null || relsEntry == null) {
            return new HashMap<String, String>();
        }
        final Map<String, String> rIdToTarget = new HashMap<String, String>();
        SAXParser p1 = UtilXML.newSecureSAXParser();
        try (InputStream in = zip.getInputStream(relsEntry)) {
            p1.parse(new InputSource(in), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    if ("Relationship".equals(localName) || "Relationship".equals(qName)) {
                        String id = attrs.getValue("Id");
                        String target = attrs.getValue("Target");
                        if (id != null && target != null) {
                            rIdToTarget.put(id, target.startsWith("/") ? target.substring(1) : "xl/" + target);
                        }
                    }
                }
            });
        }
        final Map<String, String> sheetNameToPath = new HashMap<String, String>();
        SAXParser p2 = UtilXML.newSecureSAXParser();
        try (InputStream in = zip.getInputStream(wbEntry)) {
            p2.parse(new InputSource(in), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    if ("sheet".equals(localName) || "sheet".equals(qName)) {
                        String name = attrs.getValue("name");
                        String rid = attrs.getValue("r:id");
                        if (rid == null) rid = attrs.getValue("id");
                        if (name != null && rid != null) {
                            String target = rIdToTarget.get(rid);
                            if (target != null) {
                                sheetNameToPath.put(name, target);
                            }
                        }
                    }
                }
            });
        }
        return sheetNameToPath;
    }

    private interface RowConsumer {
        void accept(Map<String, String> row);
    }

    private static void streamRows(ZipFile zip, String sheetPath, final List<String> sharedStrings,
                                   final RowConsumer consumer) throws Exception {
        ZipEntry entry = zip.getEntry(sheetPath);
        if (entry == null) {
            throw new IOException("Worksheet entry not found: " + sheetPath);
        }
        SAXParser parser = UtilXML.newSecureSAXParser();
        try (InputStream in = zip.getInputStream(entry)) {
            parser.parse(new InputSource(in), new DefaultHandler() {
                private final Map<String, String> currentRow = new HashMap<String, String>();
                private final Map<String, String> columnLetterToHeader = new HashMap<String, String>();
                private String currentCellRef;
                private String currentCellType;
                private StringBuilder cellBuf = new StringBuilder();
                private boolean inValue = false;
                private boolean inInlineStr = false;
                private int rowIndex = 0;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    String name = localName.isEmpty() ? qName : localName;
                    if ("row".equals(name)) {
                        currentRow.clear();
                        rowIndex++;
                    } else if ("c".equals(name)) {
                        currentCellRef = attrs.getValue("r");
                        currentCellType = attrs.getValue("t");
                        cellBuf.setLength(0);
                    } else if ("v".equals(name)) {
                        inValue = true;
                    } else if ("is".equals(name)) {
                        inInlineStr = true;
                    } else if ("t".equals(name) && inInlineStr) {
                        inValue = true;
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    if (inValue) {
                        cellBuf.append(ch, start, length);
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    String name = localName.isEmpty() ? qName : localName;
                    if ("v".equals(name)) {
                        inValue = false;
                    } else if ("t".equals(name) && inInlineStr) {
                        inValue = false;
                    } else if ("is".equals(name)) {
                        inInlineStr = false;
                    } else if ("c".equals(name)) {
                        String raw = cellBuf.toString();
                        String value;
                        if ("s".equals(currentCellType)) {
                            try {
                                value = sharedStrings.get(Integer.parseInt(raw.trim()));
                            } catch (Exception e) {
                                value = raw;
                            }
                        } else {
                            value = raw;
                        }
                        String column = columnLetterOnly(currentCellRef);
                        if (rowIndex == 1) {
                            columnLetterToHeader.put(column, value);
                        } else {
                            String header = columnLetterToHeader.get(column);
                            if (header != null) {
                                currentRow.put(header, value);
                            }
                        }
                    } else if ("row".equals(name)) {
                        if (rowIndex > 1 && !currentRow.isEmpty()) {
                            consumer.accept(currentRow);
                        }
                    }
                }
            });
        }
    }

    private static String columnLetterOnly(String cellRef) {
        if (cellRef == null) return "";
        int i = 0;
        while (i < cellRef.length() && Character.isLetter(cellRef.charAt(i))) i++;
        return cellRef.substring(0, i);
    }

    // ---------- Getters for the JSP ----------

    public ImportReport getResultReport() {
        return resultReport;
    }

    public ImportReport getRequestReport() {
        return requestReport;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getXlsxFileName() {
        return xlsxFileName;
    }

    public static class ImportReport {
        public int added = 0;
        public int updated = 0;
        public int deprecated = 0;

        public int getAdded() { return added; }
        public int getUpdated() { return updated; }
        public int getDeprecated() { return deprecated; }
        public int getTotal() { return added + updated + deprecated; }

        @Override
        public String toString() {
            return "added=" + added + ", updated=" + updated + ", deprecated=" + deprecated;
        }
    }
}
