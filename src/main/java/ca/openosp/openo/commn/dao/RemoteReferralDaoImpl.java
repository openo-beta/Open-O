//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.RemoteReferral;
import org.springframework.stereotype.Repository;

@Repository
public class RemoteReferralDaoImpl extends AbstractDaoImpl<RemoteReferral> implements RemoteReferralDao {

    public RemoteReferralDaoImpl() {
        super(RemoteReferral.class);
    }

    @Override
    public List<RemoteReferral> findByFacilityIdDemogprahicId(Integer facilityId, Integer demographicId) {
        String sql = "select x from " + modelClass.getSimpleName() + " x where x.facilityId=?1 and x.demographicId=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, facilityId);
        query.setParameter(2, demographicId);

        @SuppressWarnings("unchecked")
        List<RemoteReferral> results = query.getResultList();
        return results;
    }
}
