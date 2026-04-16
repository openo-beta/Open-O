//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ConsentType;

import java.util.List;
import java.util.Collections;

public interface ConsentTypeDao extends AbstractDao<ConsentType> {

    public ConsentType findConsentType(String type);

    public ConsentType findConsentTypeForProvider(String type, String providerNo);

    public List<ConsentType> findAllActive();
}
