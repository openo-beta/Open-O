//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import org.springframework.stereotype.Repository;
import ca.openosp.openo.commn.model.BillingONErrorCode;

@Repository
public class BillingONErrorCodeDaoImpl extends AbstractDaoImpl<BillingONErrorCode> implements BillingONErrorCodeDao {

    public BillingONErrorCodeDaoImpl() {
        super(BillingONErrorCode.class);
    }
}
