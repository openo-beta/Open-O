//CHECKSTYLE:OFF
package ca.openosp.openo.olis.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import ca.openosp.openo.olis.model.OLISMicroorganismNomenclature;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link OLISMicroorganismNomenclature}. Resolves OLIS microorganism
 * codes (OBX-5.1, coding system HL79905) to catalog entries so coded
 * microbiology results display a human-readable organism name (CV06).
 *
 * <p>Derived from the oscarpro {@code OlisMicroorganismNomenclatureDao} (GPLv2),
 * namespace-migrated to OpenO's DAO conventions.</p>
 *
 * @since 2026-06-17
 */
@Repository
public class OLISMicroorganismNomenclatureDao extends AbstractDaoImpl<OLISMicroorganismNomenclature> {

    /**
     * Constructs the DAO bound to the {@link OLISMicroorganismNomenclature} entity.
     */
    public OLISMicroorganismNomenclatureDao() {
        super(OLISMicroorganismNomenclature.class);
    }

    /**
     * Resolves a single OLIS microorganism code to its catalog entry.
     *
     * @param code String the microorganism code (OBX-5.1, coding system HL79905)
     * @return OLISMicroorganismNomenclature the matching catalog entry, or {@code null}
     *         when no entry exists for the code
     * @since 2026-06-17
     */
    public OLISMicroorganismNomenclature findByMicroorganismCode(String code) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.microorganismCode=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, code);
        return this.getSingleResultOrNull(query);
    }

    /**
     * Bulk-resolves a list of microorganism codes to a {@code code -> entry} map
     * (one query rather than per-result lookups during a render).
     *
     * @param codes List&lt;String&gt; the microorganism codes from a report's results
     * @return Map&lt;String, OLISMicroorganismNomenclature&gt; keyed by microorganism code;
     *         empty when {@code codes} is null/empty
     */
    @SuppressWarnings("unchecked")
    public Map<String, OLISMicroorganismNomenclature> findByMicroorganismCodes(List<String> codes) {
        Map<String, OLISMicroorganismNomenclature> resultsMap = new HashMap<>();
        // An empty IN (:codes) is invalid JPQL on some providers; skip the query.
        if (codes == null || codes.isEmpty()) {
            return resultsMap;
        }
        String sql = "select x from " + this.modelClass.getName() + " x where x.microorganismCode in (:codes)";
        Query query = entityManager.createQuery(sql);
        query.setParameter("codes", codes);
        List<OLISMicroorganismNomenclature> resultsList = query.getResultList();
        for (OLISMicroorganismNomenclature nomenclature : resultsList) {
            resultsMap.put(nomenclature.getMicroorganismCode(), nomenclature);
        }
        return resultsMap;
    }
}
