//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.BatchEligibility;
import org.springframework.stereotype.Repository;

@Repository
public class BatchEligibilityDaoImpl extends AbstractDaoImpl<BatchEligibility> implements BatchEligibilityDao {

    public BatchEligibilityDaoImpl() {
        super(BatchEligibility.class);
    }

}
