package ca.openosp.openo.olis;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.openosp.openo.olis.OLISNomenclatureImport2Action.ImportReport;
import ca.openosp.openo.olis.dao.OLISRequestNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISResultNomenclatureDao;
import ca.openosp.openo.olis.model.OLISRequestNomenclature;
import ca.openosp.openo.olis.model.OLISResultNomenclature;

/**
 * Transactional service that applies a parsed eHealth Ontario OLIS Nomenclatures
 * distribution to the {@code OLISResultNomenclature} + {@code OLISRequestNomenclature}
 * tables as a single atomic unit.
 * <p>
 * A nomenclature refresh upserts both the Result and Request sheets. The two
 * sheets are a single logical update: a failure partway through must not leave the
 * Result table fully refreshed while the Request table is stale or half-written.
 * The work lives here, in a Spring-managed bean, because {@link OLISNomenclatureImport2Action}
 * is instantiated directly by Struts2 — Spring's transactional AOP proxy never wraps
 * it, so {@code @Transactional} on the action would be a silent no-op and each
 * {@code @Transactional} DAO call would otherwise commit independently. Invoking
 * {@link #importNomenclatures(List, List, ImportReport, ImportReport)} through the
 * Spring proxy opens one transaction the DAO calls join (propagation REQUIRED), so
 * both sheets commit or roll back together.
 *
 * @since 2026-06-02
 */
@Service
public class OLISNomenclatureImportService {

    private final OLISResultNomenclatureDao resultDao;
    private final OLISRequestNomenclatureDao requestDao;

    @Autowired
    public OLISNomenclatureImportService(OLISResultNomenclatureDao resultDao,
                                         OLISRequestNomenclatureDao requestDao) {
        this.resultDao = resultDao;
        this.requestDao = requestDao;
    }

    /**
     * Atomically upserts both nomenclature sheets. Runs in a single transaction:
     * any failure rolls the whole refresh back, leaving both tables in their prior
     * consistent state.
     *
     * @param resultRows    List&lt;Map&lt;String,String&gt;&gt; parsed Test Result Nomenclatures rows
     * @param requestRows   List&lt;Map&lt;String,String&gt;&gt; parsed Test Request Nomenclature rows
     * @param resultReport  ImportReport tally mutated in place for the Result sheet
     * @param requestReport ImportReport tally mutated in place for the Request sheet
     * @since 2026-06-02
     */
    @Transactional
    public void importNomenclatures(List<Map<String, String>> resultRows,
                                    List<Map<String, String>> requestRows,
                                    ImportReport resultReport, ImportReport requestReport) {
        for (Map<String, String> row : resultRows) {
            importResultRow(row, resultReport);
        }
        for (Map<String, String> row : requestRows) {
            importRequestRow(row, requestReport);
        }
    }

    private void importResultRow(Map<String, String> row, ImportReport rep) {
        String nameId = trimToNull(row.get("LOINC Code"));
        if (nameId == null) {
            return;
        }
        // findByNameId returns null when the code is not yet stored. Real persistence
        // errors are intentionally NOT swallowed — they propagate so the transaction
        // rolls back rather than silently treating a failed lookup as "not found".
        OLISResultNomenclature existing = resultDao.findByNameId(nameId);
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
            resultDao.persist(entity);
            rep.added++;
        } else {
            resultDao.merge(entity);
            if (wasActive && !"ACTIVE".equals(newStatus)) {
                rep.deprecated++;
            } else {
                rep.updated++;
            }
        }
    }

    private void importRequestRow(Map<String, String> row, ImportReport rep) {
        String nameId = trimToNull(row.get("OLIS Test Request Code"));
        if (nameId == null) {
            return;
        }
        OLISRequestNomenclature existing = requestDao.findByNameId(nameId);
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
            requestDao.persist(entity);
            rep.added++;
        } else {
            requestDao.merge(entity);
            if (wasActive && !"ACTIVE".equals(newStatus)) {
                rep.deprecated++;
            } else {
                rep.updated++;
            }
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
}
