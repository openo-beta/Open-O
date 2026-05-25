package ca.openosp.openo.hospitalReportManager.dao;

import java.util.List;

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
        String sql = "select x from " + modelClass.getSimpleName() + " x where x.sendingFacilityId = ?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, sendingFacilityId);
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
