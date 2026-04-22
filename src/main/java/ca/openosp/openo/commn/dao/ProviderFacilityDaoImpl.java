//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ProviderFacility;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderFacilityDaoImpl extends AbstractDaoImpl<ProviderFacility> implements ProviderFacilityDao {

    public ProviderFacilityDaoImpl() {
        super(ProviderFacility.class);
    }

    @Override
    public List<ProviderFacility> findByProviderNoAndFacilityId(String providerNo, int facilityId) {
        Query q = entityManager.createQuery("SELECT x FROM ProviderFacility x WHERE x.id.providerNo=?1 AND x.id.facilityId=?2");
        q.setParameter(1, providerNo);
        q.setParameter(2, facilityId);

        @SuppressWarnings("unchecked")
        List<ProviderFacility> results = q.getResultList();

        return results;
    }
}
