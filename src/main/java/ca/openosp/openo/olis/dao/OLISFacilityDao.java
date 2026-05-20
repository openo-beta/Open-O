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
     * Active rows for one class, used by the AJAX picker to provide name+licence
     * suggestions. Case-insensitive substring match against the facility name; ties
     * broken alphabetically.
     *
     * @param facilityClass String "LAB", "SCC", or null/"ANY" for both
     * @param term          String substring to match
     * @param limit         int max rows to return
     * @return List of matching active facilities, at most {@code limit} entries
     */
    @SuppressWarnings("unchecked")
    public List<OLISFacility> findByClassAndNameLike(String facilityClass, String term, int limit) {
        StringBuilder sql = new StringBuilder("select x from ")
                .append(this.modelClass.getName())
                .append(" x where x.status = 'ACTIVE' and lower(x.name) like :term");
        boolean filterClass = (facilityClass != null
                && !facilityClass.isEmpty()
                && !"ANY".equalsIgnoreCase(facilityClass));
        if (filterClass) {
            sql.append(" and x.facilityClass = :facilityClass");
        }
        sql.append(" order by x.name, x.licenceNumber");
        Query query = entityManager.createQuery(sql.toString());
        query.setParameter("term", "%" + term.toLowerCase() + "%");
        if (filterClass) {
            query.setParameter("facilityClass", facilityClass);
        }
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
