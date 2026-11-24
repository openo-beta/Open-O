//CHECKSTYLE:OFF
package ca.openosp.openo.olis.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import ca.openosp.openo.olis.model.OLISSourceNomenclature;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link OLISSourceNomenclature}. Resolves OLIS specimen source codes
 * (OBR-15.1.1) to catalog entries so a test request's specimen type displays a
 * human-readable name (CT 9.4 "Specimen Type", lookup = "Source Nom File").
 *
 * @since 2026-06-18
 */
@Repository
public class OLISSourceNomenclatureDao extends AbstractDaoImpl<OLISSourceNomenclature> {

    public OLISSourceNomenclatureDao() {
        super(OLISSourceNomenclature.class);
    }

    /**
     * Resolves a single specimen source code to its catalog entry.
     *
     * @param value String the specimen source code (OBR-15.1.1)
     * @return OLISSourceNomenclature the matching entry, or {@code null} if none
     */
    public OLISSourceNomenclature findByValue(String value) {
        Query query = entityManager.createQuery(
                "select x from OLISSourceNomenclature x where x.value=?1");
        query.setParameter(1, value);
        return this.getSingleResultOrNull(query);
    }

    /**
     * Bulk-resolves a list of specimen source codes to a {@code value -> entry}
     * map (one query rather than per-request lookups during a render).
     *
     * @param values List&lt;String&gt; the specimen source codes from a report
     * @return Map&lt;String, OLISSourceNomenclature&gt; keyed by source code;
     *         empty when {@code values} is null/empty
     */
    @SuppressWarnings("unchecked")
    public Map<String, OLISSourceNomenclature> findByValues(List<String> values) {
        Map<String, OLISSourceNomenclature> resultsMap = new HashMap<>();
        if (values == null || values.isEmpty()) {
            return resultsMap;
        }
        Query query = entityManager.createQuery(
                "select x from OLISSourceNomenclature x where x.value in (:values)");
        query.setParameter("values", values);
        List<OLISSourceNomenclature> resultsList = query.getResultList();
        for (OLISSourceNomenclature nomenclature : resultsList) {
            resultsMap.put(nomenclature.getValue(), nomenclature);
        }
        return resultsMap;
    }
}
