package ca.openosp.openo.olis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.SAXParser;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.olis.dao.OLISFacilityDao;
import ca.openosp.openo.olis.model.OLISFacility;
import ca.openosp.openo.util.UtilXML;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PathValidationUtils;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Admin action: imports the official eHealth Ontario Lab/SCC Extract XLSX
 * distribution into the local {@code OLISFacility} table.
 * <p>
 * Parses the XLSX with stdlib {@code java.util.zip} + SAX (no Apache POI
 * dependency required), discriminates each row as a Laboratory or Specimen
 * Collection Centre by its OID column, and upserts on the natural key
 * {@code (facilityClass, licenceNumber)}.
 * <p>
 * Deprecation strategy: the extract contains no validation/workflow status
 * columns (unlike the OLIS Nomenclature distribution). Before the upsert,
 * every existing ACTIVE row is marked INACTIVE; rows present in the new
 * extract are then promoted back to ACTIVE. Rows not present in the new
 * extract therefore remain INACTIVE, so the AJAX picker hides them from
 * the lab-roster dropdowns.
 * <p>
 * Operational context: the canonical roster lives at
 * <a href="https://ehealthontario.on.ca/en/support/lab-results/olis-whats-new/olis-client-support">eHealth
 * Ontario OLIS Client Support</a>. Run this importer after each Lab/SCC Extract
 * refresh to keep the OLIS Search and OLIS Preferences pickers current. See
 * {@code docs/olis/requirements-analysis.md} OLIS04.03 for the design rationale.
 *
 * @since 2026-05-20
 */
public class OLISFacilityImport2Action extends ActionSupport implements UploadedFilesAware {

    private static final org.apache.logging.log4j.Logger LOG = MiscUtils.getLogger();

    private HttpServletRequest request = ServletActionContext.getRequest();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    private UploadedFile uploadedXlsx;
    private File xlsxOnDisk;
    private String xlsxFileName;

    private ImportReport labReport;
    private ImportReport sccReport;
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

        if (xlsxOnDisk == null) {
            return "form";
        }

        try (ZipFile zip = new ZipFile(xlsxOnDisk)) {
            List<String> sharedStrings = readSharedStrings(zip);
            String sheetPath = firstSheetPath(zip);
            if (sheetPath == null) {
                errorMessage = "Uploaded file does not look like an XLSX workbook (no worksheet entries found).";
                return "form";
            }

            OLISFacilityDao dao = SpringUtils.getBean(OLISFacilityDao.class);
            dao.markAllInactive(OLISFacility.CLASS_LAB);
            dao.markAllInactive(OLISFacility.CLASS_SCC);

            labReport = new ImportReport();
            sccReport = new ImportReport();
            streamRows(zip, sheetPath, sharedStrings, dao);
            LOG.info("OLIS Lab/SCC import — labs: " + labReport + "; sccs: " + sccReport);
            request.setAttribute("labReport", labReport);
            request.setAttribute("sccReport", sccReport);
            request.setAttribute("xlsxFileName", xlsxFileName);
            return "report";
        } catch (Exception e) {
            LOG.error("OLIS Lab/SCC import failed", e);
            errorMessage = "Import failed: " + e.getClass().getSimpleName() + " — " + e.getMessage();
            request.setAttribute("errorMessage", errorMessage);
            return "form";
        }
    }

    private void importRow(OLISFacilityDao dao, Map<String, String> row) {
        String licence = trimToNull(row.get("Licence Number"));
        String oid = trimToNull(row.get("OID"));
        String name = trimToNull(row.get("Facility Name"));
        if (licence == null || oid == null || name == null) {
            return;
        }
        String facilityClass = classFromOid(oid);
        if (facilityClass == null) {
            return;
        }
        ImportReport rep = OLISFacility.CLASS_LAB.equals(facilityClass) ? labReport : sccReport;

        OLISFacility existing = dao.findByClassAndLicence(facilityClass, licence);
        boolean isNew = (existing == null);
        OLISFacility entity = isNew ? new OLISFacility() : existing;
        if (isNew) {
            entity.setLicenceNumber(licence);
            entity.setFacilityClass(facilityClass);
        }
        entity.setOid(oid);
        entity.setName(name);
        entity.setAddressLine1(trimToNull(row.get("Facility Address Line One")));
        entity.setAddressLine2(trimToNull(row.get("Facility Address Line Two")));
        entity.setCity(trimToNull(row.get("Facility Address City")));
        entity.setPostalCode(trimToNull(row.get("Facility Address Postal_Code")));
        entity.setStatus("ACTIVE");
        if (isNew) {
            dao.persist(entity);
            rep.added++;
        } else {
            dao.merge(entity);
            rep.updated++;
        }
    }

    private static String classFromOid(String oid) {
        if (OLISFacility.OID_LAB.equals(oid)) {
            return OLISFacility.CLASS_LAB;
        }
        if (OLISFacility.OID_SCC.equals(oid)) {
            return OLISFacility.CLASS_SCC;
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

    private static String firstSheetPath(ZipFile zip) throws Exception {
        ZipEntry wbEntry = zip.getEntry("xl/workbook.xml");
        ZipEntry relsEntry = zip.getEntry("xl/_rels/workbook.xml.rels");
        if (wbEntry == null || relsEntry == null) {
            return null;
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
        final String[] firstTarget = new String[1];
        SAXParser p2 = UtilXML.newSecureSAXParser();
        try (InputStream in = zip.getInputStream(wbEntry)) {
            p2.parse(new InputSource(in), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    if (firstTarget[0] != null) return;
                    if ("sheet".equals(localName) || "sheet".equals(qName)) {
                        String rid = attrs.getValue("r:id");
                        if (rid == null) rid = attrs.getValue("id");
                        if (rid != null) {
                            firstTarget[0] = rIdToTarget.get(rid);
                        }
                    }
                }
            });
        }
        return firstTarget[0];
    }

    private void streamRows(ZipFile zip, String sheetPath, final List<String> sharedStrings,
                            final OLISFacilityDao dao) throws Exception {
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
                            importRow(dao, currentRow);
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

    public ImportReport getLabReport() {
        return labReport;
    }

    public ImportReport getSccReport() {
        return sccReport;
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

        public int getAdded() { return added; }
        public int getUpdated() { return updated; }
        public int getTotal() { return added + updated; }

        @Override
        public String toString() {
            return "added=" + added + ", updated=" + updated;
        }
    }

}
