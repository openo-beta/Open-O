//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.casemgmt.model.ProviderExt;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderExtDaoImpl extends AbstractDaoImpl<ProviderExt> implements ProviderExtDao {

    public ProviderExtDaoImpl() {
        super(ProviderExt.class);
    }
}
