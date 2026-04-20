//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.IntegratorConsent;
import org.springframework.stereotype.Repository;

@Repository
public class IntegratorConsentDaoImpl extends AbstractDaoImpl<IntegratorConsent> implements IntegratorConsentDao {

    public IntegratorConsentDaoImpl() {
        super(IntegratorConsent.class);
    }

    public IntegratorConsent findLatestByFacilityDemographic(int facilityId, int demographicId) {
        String sqlCommand = "select * from IntegratorConsent where facilityId=?1 and demographicId=?2 order by createdDate desc";
        Query query = entityManager.createNativeQuery(sqlCommand, modelClass);
        query.setParameter(1, facilityId);
        query.setParameter(2, demographicId);
        return (getSingleResultOrNull(query));
    }

    public List<IntegratorConsent> findByFacilityAndDemographic(int facilityId, int demographicId) {
        Query query = entityManager.createQuery("select x from IntegratorConsent x where x.facilityId=?1 and x.demographicId=?2 order by x.createdDate desc");
        query.setParameter(1, facilityId);
        query.setParameter(2, demographicId);
        @SuppressWarnings("unchecked")
        List<IntegratorConsent> results = query.getResultList();
        return (results);
    }

    public List<IntegratorConsent> findByFacilityAndDemographicSince(int facilityId, int demographicId, Date lastDataUpdated) {
        Query query = entityManager.createQuery("select x from IntegratorConsent x where x.facilityId=?1 and x.demographicId=?2 and x.createdDate > ?3 order by x.createdDate desc");
        query.setParameter(1, facilityId);
        query.setParameter(2, demographicId);
        query.setParameter(3, lastDataUpdated);
        @SuppressWarnings("unchecked")
        List<IntegratorConsent> results = query.getResultList();
        return (results);
    }

    public List<Integer> findDemographicIdsByFacilitySince(int facilityId, Date lastDataUpdated) {
        Query query = entityManager.createQuery("select distinct x.demographicId from IntegratorConsent x where x.facilityId=?1 and x.createdDate > ?2 order by x.createdDate desc");
        query.setParameter(1, facilityId);
        query.setParameter(2, lastDataUpdated);
        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();
        return (results);
    }
}
