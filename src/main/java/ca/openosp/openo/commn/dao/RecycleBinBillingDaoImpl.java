//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import org.springframework.stereotype.Repository;
import ca.openosp.openo.commn.model.RecycleBinBilling;

@Repository
public class RecycleBinBillingDaoImpl extends AbstractDaoImpl<RecycleBinBilling> implements RecycleBinBillingDao {

    public RecycleBinBillingDaoImpl() {
        super(RecycleBinBilling.class);
    }
}
