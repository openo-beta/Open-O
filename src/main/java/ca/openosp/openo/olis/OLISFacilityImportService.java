package ca.openosp.openo.olis;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.openosp.openo.olis.OLISFacilityImport2Action.ImportReport;
import ca.openosp.openo.olis.dao.OLISFacilityDao;
import ca.openosp.openo.olis.model.OLISFacility;

/**
 * Transactional service that applies a parsed eHealth Ontario Lab/SCC extract to
 * the {@code OLISFacility} table as a single atomic unit.
 * <p>
 * The roster refresh is a destroy-then-rebuild operation: every existing ACTIVE
 * row is first marked INACTIVE, then rows present in the new extract are promoted
 * back to ACTIVE. If any step fails, the whole operation must roll back — otherwise
 * the table is left with rows stranded INACTIVE and the OLIS Search / Preferences
 * pickers return an empty roster until someone re-runs the import.
 * <p>
 * The work lives here, in a Spring-managed bean, rather than in
 * {@link OLISFacilityImport2Action}: Struts2 instantiates the action directly, so
 * Spring's transactional AOP proxy never wraps it and {@code @Transactional} on the
 * action would be a silent no-op. Each individual {@code @Transactional} DAO call
 * invoked from the (non-transactional) action would otherwise commit on its own,
 * leaving no rollback boundary around the multi-step refresh. Calling
 * {@link #importFacilities(List, ImportReport, ImportReport)} through the Spring
 * proxy opens one transaction that the DAO calls join (propagation REQUIRED), so
 * the entire refresh commits or rolls back together.
 *
 * @since 2026-06-02
 */
@Service
public class OLISFacilityImportService {

    private final OLISFacilityDao dao;

    @Autowired
    public OLISFacilityImportService(OLISFacilityDao dao) {
        this.dao = dao;
    }

    /**
     * Atomically replaces the active Lab/SCC roster with the supplied rows.
     * Marks all existing rows of both classes INACTIVE, then upserts each row on
     * its natural key {@code (facilityClass, licenceNumber)} and promotes it back
     * to ACTIVE. Runs in a single transaction: any failure rolls the whole refresh
     * back, leaving the previously ACTIVE roster intact.
     *
     * @param rows      List&lt;Map&lt;String,String&gt;&gt; the parsed extract rows, keyed by
     *                  header name (already read fully into memory by the caller so a
     *                  malformed file never reaches the DB)
     * @param labReport ImportReport tally mutated in place for the LAB class
     * @param sccReport ImportReport tally mutated in place for the SCC class
     * @since 2026-06-02
     */
    @Transactional
    public void importFacilities(List<Map<String, String>> rows, ImportReport labReport, ImportReport sccReport) {
        dao.markAllInactive(OLISFacility.CLASS_LAB);
        dao.markAllInactive(OLISFacility.CLASS_SCC);
        for (Map<String, String> row : rows) {
            importRow(row, labReport, sccReport);
        }
    }

    private void importRow(Map<String, String> row, ImportReport labReport, ImportReport sccReport) {
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
}
