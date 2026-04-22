//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ProviderBillCenter;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderBillCenterDaoImpl extends AbstractDaoImpl<ProviderBillCenter> implements ProviderBillCenterDao {

    public ProviderBillCenterDaoImpl() {
        super(ProviderBillCenter.class);
    }
}
