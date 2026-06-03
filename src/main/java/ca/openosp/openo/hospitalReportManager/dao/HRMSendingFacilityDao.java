package ca.openosp.openo.hospitalReportManager.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import ca.openosp.openo.hospitalReportManager.model.HRMSendingFacility;
import org.springframework.stereotype.Repository;

@Repository
public class HRMSendingFacilityDao extends AbstractDaoImpl<HRMSendingFacility> {

    public HRMSendingFacilityDao() {
        super(HRMSendingFacility.class);
    }

    public List<HRMSendingFacility> findAll() {
        String sql = "select x from " + modelClass.getName() + " x order by x.sendingFacilityId";
        Query query = entityManager.createQuery(sql);
        @SuppressWarnings("unchecked")
        List<HRMSendingFacility> facilities = query.getResultList();
        return facilities;
    }

    public HRMSendingFacility findBySendingFacilityId(String sendingFacilityId) {
        if (sendingFacilityId == null) {
            return null;
        }
        // Trim to match the registration action, which trims the ID before saving.
        String sql = "select x from " + modelClass.getSimpleName() + " x where x.sendingFacilityId = ?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, sendingFacilityId.trim());
        return getSingleResultOrNull(query);
    }

    public String getDisplayName(String sendingFacilityId) {
        if (sendingFacilityId == null || sendingFacilityId.trim().isEmpty()) {
            return "";
        }
        HRMSendingFacility sf = findBySendingFacilityId(sendingFacilityId);
        return sf != null
                ? sf.getFacilityName() + " (" + sendingFacilityId + ")"
                : sendingFacilityId;
    }

    /**
     * Loads the whole (small) registry once as a map keyed by trimmed sendingFacilityId.
     * Use with {@link #getDisplayName(String, Map)} to resolve many display names without
     * a per-row query (avoids N+1 in list views).
     *
     * @return Map&lt;String, HRMSendingFacility&gt; registry keyed by sendingFacilityId
     */
    public Map<String, HRMSendingFacility> getRegistryById() {
        Map<String, HRMSendingFacility> registry = new HashMap<String, HRMSendingFacility>();
        for (HRMSendingFacility sf : findAll()) {
            if (sf.getSendingFacilityId() != null) {
                registry.put(sf.getSendingFacilityId().trim(), sf);
            }
        }
        return registry;
    }

    /**
     * Map-backed variant of {@link #getDisplayName(String)} that resolves against a prefetched
     * registry instead of querying. Produces an identical result to the single-argument version.
     *
     * @param sendingFacilityId String the facility ID from the report
     * @param registry Map&lt;String, HRMSendingFacility&gt; prefetched via {@link #getRegistryById()}
     * @return String the display name, the raw ID if unregistered, or "" if blank
     */
    public String getDisplayName(String sendingFacilityId, Map<String, HRMSendingFacility> registry) {
        if (sendingFacilityId == null || sendingFacilityId.trim().isEmpty()) {
            return "";
        }
        HRMSendingFacility sf = registry.get(sendingFacilityId.trim());
        return sf != null
                ? sf.getFacilityName() + " (" + sendingFacilityId + ")"
                : sendingFacilityId;
    }

    /**
     * Returns SF IDs present on HRMDocument.sourceFacility but not in this registry,
     * paired with the count of documents from each. Result rows are Object[]{String, Long}
     * ordered by count desc.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findUnregisteredFacilityCounts() {
        String jpql = "select d.sourceFacility, count(d) from HRMDocument d "
                + "where d.sourceFacility is not null and d.sourceFacility <> '' "
                + "and d.sourceFacility not in (select s.sendingFacilityId from HRMSendingFacility s) "
                + "group by d.sourceFacility "
                + "order by count(d) desc";
        return entityManager.createQuery(jpql).getResultList();
    }
}
