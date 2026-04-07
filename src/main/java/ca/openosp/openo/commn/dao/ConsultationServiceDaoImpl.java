//CHECKSTYLE:OFF



package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ConsultationServices;
import ca.openosp.openo.encounter.oscarConsultationRequest.config.data.ConsultationServiceDto;
import org.springframework.stereotype.Repository;

/**
 * @author rjonasz
 */
@Repository
public class ConsultationServiceDaoImpl extends AbstractDaoImpl<ConsultationServices> implements ConsultationServiceDao {
    public final String REFERRING_DOCTOR = "Referring Doctor";
    public final String ACTIVE = "1";
    public final String INACTIVE = "02";
    public final boolean ACTIVE_ONLY = true;
    public final boolean WITH_INACTIVE = false;

    public ConsultationServiceDaoImpl() {
        super(ConsultationServices.class);
    }

    public List<ConsultationServices> findAll() {
        String sql = "select x from ConsultationServices x order by x.serviceDesc";
        Query query = entityManager.createQuery(sql);

        @SuppressWarnings("unchecked")
        List<ConsultationServices> results = query.getResultList();
        return results;
    }

    public List<ConsultationServices> findActive() {
        String sql = "select x from ConsultationServices x where x.active=?1 order by x.serviceDesc";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, "1");

        @SuppressWarnings("unchecked")
        List<ConsultationServices> results = query.getResultList();
        return results;
    }

    public List<ConsultationServices> findActiveNames() {
        String sql = "select x.serviceId,x.serviceDesc from ConsultationServices x where x.active=?1 order by x.serviceDesc";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, "1");

        @SuppressWarnings("unchecked")
        List<ConsultationServices> results = query.getResultList();
        return results;
    }


    public ConsultationServices findByDescription(String description) {
        String sql = "select x from ConsultationServices x where x.active=?1 and x.serviceDesc = ?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, "1");
        query.setParameter(2, description);

        return this.getSingleResultOrNull(query);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ConsultationServiceDto> findActiveServiceSummaries() {
        Query query = entityManager.createQuery("SELECT NEW ca.openosp.openo.encounter.oscarConsultationRequest.config.data.ConsultationServiceDto(cs.serviceId, cs.serviceDesc) FROM ConsultationServices cs WHERE cs.active = :active ORDER BY cs.serviceDesc");
        query.setParameter("active", ACTIVE);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServiceDescription(Integer serviceId) {
        Query query = entityManager.createQuery("SELECT cs.serviceDesc FROM ConsultationServices cs WHERE cs.serviceId = :id");
        query.setParameter("id", serviceId);
        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public ConsultationServices findReferringDoctorService(boolean activeOnly) {
        String sql = "select x from ConsultationServices x where x.serviceDesc=?1";
        if (activeOnly) sql += " and x.active=?2";

        Query query = entityManager.createQuery(sql);
        query.setParameter(1, REFERRING_DOCTOR);
        if (activeOnly) query.setParameter(2, ACTIVE);

        @SuppressWarnings("unchecked")
        List<ConsultationServices> results = query.getResultList();

        //filter out the first active one if multiple services are found
        if (results != null && results.size() > 1) {
            for (ConsultationServices cs : results) {
                if (cs.getActive().equals(ACTIVE)) {
                    results = new ArrayList<ConsultationServices>();
                    results.add(cs);
                    break;
                }
            }
        }
        return (results == null || results.isEmpty()) ? null : results.get(0);
    }
}
