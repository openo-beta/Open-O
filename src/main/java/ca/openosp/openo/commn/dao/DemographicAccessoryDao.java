//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.DemographicAccessory;

public interface DemographicAccessoryDao extends AbstractDao<DemographicAccessory> {
    long findCount(Integer demographicNo);
}
