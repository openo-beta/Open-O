package ca.openosp.openo.olis.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import ca.openosp.openo.olis.model.OLISFacility;

@Repository
public class OLISFacilityDao extends AbstractDaoImpl<OLISFacility> {

    public OLISFacilityDao() {
        super(OLISFacility.class);
    }

    /**
     * Lookup a single facility by its class + licence number (the natural key).
     * Returns null if no row exists; the importer uses this for upsert.
     *
     * @param facilityClass String "LAB" or "SCC"
     * @param licenceNumber String the licence number from the extract
     * @return OLISFacility the row, or null
     */
    public OLISFacility findByClassAndLicence(String facilityClass, String licenceNumber) {
        String sql = "select x from " + this.modelClass.getName() + " x"
                + " where x.facilityClass = :facilityClass"
                + "   and x.licenceNumber = :licenceNumber";
        Query query = entityManager.createQuery(sql);
        query.setParameter("facilityClass", facilityClass);
        query.setParameter("licenceNumber", licenceNumber);
        return this.getSingleResultOrNull(query);
    }

    /**
     * Active rows for one class, used by the AJAX picker to provide name+address
     * +city+licence suggestions. Splits the user query on whitespace and requires
     * EVERY token to appear (in any order) within the haystack
     * {@code lower(name + ' ' + addressLine1 + ' ' + city)}. So a single-token
     * query like {@code "lifelabs"} matches anywhere; a multi-token query like
     * {@code "lifelabs toronto"} narrows to rows containing both tokens
     * regardless of adjacency (the address text between name and city would
     * otherwise break a naive contiguous-substring LIKE). Capped at 4 tokens
     * (extras ignored) so the SQL stays one literal with fixed parameter slots.
     * Unused slots are bound to {@code "%"} which matches every row.
     * <p>
     * The address span is what disambiguates the ~23 LifeLabs SCCs all in
     * city=Toronto — each has a distinct addressLine1.
     *
     * @param facilityClass String "LAB", "SCC", or null/"ANY" for both
     * @param term          String whitespace-separated tokens; all must match
     * @param limit         int max rows to return
     * @return List of matching active facilities, at most {@code limit} entries
     */
    @SuppressWarnings("unchecked")
    public List<OLISFacility> findByClassAndNameLike(String facilityClass, String term, int limit) {
        String[] split = term.toLowerCase().trim().split("\\s+");
        java.util.List<String> tokens = new java.util.ArrayList<String>();
        for (String t : split) {
            if (!t.isEmpty()) {
                tokens.add("%" + t + "%");
            }
        }
        if (tokens.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        // Pad to exactly 4 token slots so the SQL stays a fixed literal; cap extras.
        while (tokens.size() < 4) tokens.add("%");
        if (tokens.size() > 4) tokens = tokens.subList(0, 4);

        boolean filterClass = (facilityClass != null && !facilityClass.isEmpty() && !"ANY".equalsIgnoreCase(facilityClass));
        Query query;
        if (filterClass) {
            query = entityManager.createQuery("select x from ca.openosp.openo.olis.model.OLISFacility x where x.status = 'ACTIVE' and lower(concat(x.name, ' ', coalesce(x.addressLine1, ''), ' ', coalesce(x.city, ''))) like :term0 and lower(concat(x.name, ' ', coalesce(x.addressLine1, ''), ' ', coalesce(x.city, ''))) like :term1 and lower(concat(x.name, ' ', coalesce(x.addressLine1, ''), ' ', coalesce(x.city, ''))) like :term2 and lower(concat(x.name, ' ', coalesce(x.addressLine1, ''), ' ', coalesce(x.city, ''))) like :term3 and x.facilityClass = :facilityClass order by x.name, x.city, x.licenceNumber");
            query.setParameter("facilityClass", facilityClass);
        } else {
            query = entityManager.createQuery("select x from ca.openosp.openo.olis.model.OLISFacility x where x.status = 'ACTIVE' and lower(concat(x.name, ' ', coalesce(x.addressLine1, ''), ' ', coalesce(x.city, ''))) like :term0 and lower(concat(x.name, ' ', coalesce(x.addressLine1, ''), ' ', coalesce(x.city, ''))) like :term1 and lower(concat(x.name, ' ', coalesce(x.addressLine1, ''), ' ', coalesce(x.city, ''))) like :term2 and lower(concat(x.name, ' ', coalesce(x.addressLine1, ''), ' ', coalesce(x.city, ''))) like :term3 order by x.name, x.city, x.licenceNumber");
        }
        query.setParameter("term0", tokens.get(0));
        query.setParameter("term1", tokens.get(1));
        query.setParameter("term2", tokens.get(2));
        query.setParameter("term3", tokens.get(3));
        query.setMaxResults(limit);
        return query.getResultList();
    }

    /**
     * Mark every row of one class INACTIVE. Used by the importer to pre-deprecate
     * before upserting present rows back to ACTIVE — anything not in the new
     * extract therefore ends INACTIVE.
     *
     * @param facilityClass String "LAB" or "SCC"
     * @return int the number of rows marked INACTIVE
     */
    public int markAllInactive(String facilityClass) {
        String sql = "update " + this.modelClass.getName() + " x"
                + " set x.status = 'INACTIVE'"
                + " where x.facilityClass = :facilityClass"
                + "   and x.status = 'ACTIVE'";
        Query query = entityManager.createQuery(sql);
        query.setParameter("facilityClass", facilityClass);
        return query.executeUpdate();
    }
}
