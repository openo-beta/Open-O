package ca.openosp.openo.olis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.olis.util.OlisXlsxSheetReader;
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

        List<Map<String, String>> resultRows = new ArrayList<>();
        List<Map<String, String>> requestRows = new ArrayList<>();
        try (ZipFile zip = new ZipFile(xlsxOnDisk)) {
            List<String> sharedStrings = OlisXlsxSheetReader.readSharedStrings(zip);
            Map<String, String> sheetNameToPath = OlisXlsxSheetReader.workbookSheetMap(zip);

            String resultSheetPath = sheetNameToPath.get("Test Result Nomenclatures");
            String requestSheetPath = sheetNameToPath.get("Test Request Nomenclature");

            if (resultSheetPath == null || requestSheetPath == null) {
                errorMessage = "Uploaded file does not look like an OLIS Nomenclatures distribution "
                        + "(missing the 'Test Result Nomenclatures' or 'Test Request Nomenclature' sheet).";
                return "form";
            }

            // Parse both sheets into memory first so a malformed file fails before any DB
            // mutation. streamRows reuses a single mutable Map per row, so each row must be
            // copied to be retained.
            OlisXlsxSheetReader.streamRows(zip, resultSheetPath, sharedStrings,
                    row -> resultRows.add(new HashMap<>(row)));
            OlisXlsxSheetReader.streamRows(zip, requestSheetPath, sharedStrings,
                    row -> requestRows.add(new HashMap<>(row)));
        } catch (Exception e) {
            LOG.error("OLIS nomenclature import failed", e);
            // Detail is logged above; the user gets a generic message so internal
            // exception text (e.g. DB/schema fragments) is not surfaced to the screen.
            errorMessage = "Import failed. Please verify the file is a valid OLIS Nomenclatures XLSX and try again.";
            request.setAttribute("errorMessage", errorMessage);
            return "form";
        }

        try {
            resultReport = new ImportReport();
            requestReport = new ImportReport();
            // Both sheets are one logical refresh; delegate to the transactional service so a
            // mid-import failure rolls back and both tables keep their prior consistent state.
            OLISNomenclatureImportService importService = SpringUtils.getBean(OLISNomenclatureImportService.class);
            importService.importNomenclatures(resultRows, requestRows, resultReport, requestReport);
            LOG.info("OLIS nomenclature import — results: " + resultReport
                    + "; requests: " + requestReport);
            request.setAttribute("resultReport", resultReport);
            request.setAttribute("requestReport", requestReport);
            request.setAttribute("xlsxFileName", xlsxFileName);
            return "report";
        } catch (Exception e) {
            LOG.error("OLIS nomenclature import failed", e);
            // Detail is logged above; the user gets a generic message so internal
            // exception text (e.g. DB/schema fragments) is not surfaced to the screen.
            errorMessage = "Import failed. Please verify the file is a valid OLIS Nomenclatures XLSX and try again.";
            request.setAttribute("errorMessage", errorMessage);
            return "form";
        }
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
