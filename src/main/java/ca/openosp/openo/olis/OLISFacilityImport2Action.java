package ca.openosp.openo.olis;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.olis.dao.OLISFacilityDao;
import ca.openosp.openo.olis.model.OLISFacility;
import ca.openosp.openo.olis.util.OlisXlsxSheetReader;
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

    /**
     * Receives the multipart upload via the Struts {@code UploadedFilesAware}
     * contract and records the first file as the XLSX extract to import.
     *
     * @param uploadedFiles List&lt;UploadedFile&gt; the uploaded files; only the first
     *        is used. A {@code null} or empty list leaves the action with no file, so
     *        {@link #execute()} returns {@code "form"}.
     * @since 2026-05-20
     */
    @Override
    public void withUploadedFiles(List<UploadedFile> uploadedFiles) {
        if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
            this.uploadedXlsx = uploadedFiles.get(0);
            this.xlsxOnDisk = PathValidationUtils.toFile(uploadedXlsx);
            this.xlsxFileName = uploadedXlsx.getOriginalName();
        }
    }

    /**
     * Imports the uploaded eHealth Lab/SCC extract: parses the XLSX, upserts each
     * facility on its natural key (facility class + licence number), and marks
     * facilities absent from the file {@code INACTIVE}.
     *
     * @return String the Struts result name — {@code "report"} once an import has
     *         run (tallies in {@link #getLabReport()}/{@link #getSccReport()}), or
     *         {@code "form"} when no file was supplied or the upload could not be
     *         parsed (reason in {@link #getErrorMessage()})
     * @throws SecurityException if the caller lacks {@code _admin} write privilege
     * @since 2026-05-20
     */
    public String execute() {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_admin", "w", null)) {
            throw new SecurityException("missing required sec object");
        }

        if (xlsxOnDisk == null) {
            return "form";
        }

        try (ZipFile zip = new ZipFile(xlsxOnDisk)) {
            List<String> sharedStrings = OlisXlsxSheetReader.readSharedStrings(zip);
            String sheetPath = OlisXlsxSheetReader.firstSheetPath(zip);
            if (sheetPath == null) {
                errorMessage = "Uploaded file does not look like an XLSX workbook (no worksheet entries found).";
                return "form";
            }

            OLISFacilityDao dao = SpringUtils.getBean(OLISFacilityDao.class);
            dao.markAllInactive(OLISFacility.CLASS_LAB);
            dao.markAllInactive(OLISFacility.CLASS_SCC);

            labReport = new ImportReport();
            sccReport = new ImportReport();
            OlisXlsxSheetReader.streamRows(zip, sheetPath, sharedStrings, row -> importRow(dao, row));
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

    // ---------- Getters for the JSP ----------

    /**
     * @return ImportReport the add/update tally for the LAB sheet, or {@code null}
     *         if no import has run
     * @since 2026-05-20
     */
    public ImportReport getLabReport() {
        return labReport;
    }

    /**
     * @return ImportReport the add/update tally for the SCC sheet, or {@code null}
     *         if no import has run
     * @since 2026-05-20
     */
    public ImportReport getSccReport() {
        return sccReport;
    }

    /**
     * @return String the user-facing error message when the upload could not be
     *         parsed, or {@code null} if the import succeeded
     * @since 2026-05-20
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return String the original filename of the uploaded XLSX, or {@code null} if
     *         no file was uploaded
     * @since 2026-05-20
     */
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
