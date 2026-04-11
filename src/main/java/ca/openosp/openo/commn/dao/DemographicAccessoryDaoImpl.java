//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.DemographicAccessory;
import org.springframework.stereotype.Repository;

@Repository
public class DemographicAccessoryDaoImpl extends AbstractDaoImpl<DemographicAccessory> implements DemographicAccessoryDao {

    public DemographicAccessoryDaoImpl() {
        super(DemographicAccessory.class);
    }

    @Override
    public long findCount(Integer demographicNo) {
        String sql = "select count(x) from DemographicAccessory x where x.demographicNo=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNo);

        Long count = (Long) query.getSingleResult();

        return count;
    }
}
