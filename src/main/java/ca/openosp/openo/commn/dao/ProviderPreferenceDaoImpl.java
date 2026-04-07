//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ProviderPreference;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderPreferenceDaoImpl extends AbstractDaoImpl<ProviderPreference> implements ProviderPreferenceDao {

    public ProviderPreferenceDaoImpl() {
        super(ProviderPreference.class);
    }
}
