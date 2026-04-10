//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.DemographicCust;
import ca.openosp.openo.commn.model.DemographicCustArchive;

public interface DemographicCustArchiveDao extends AbstractDao<DemographicCustArchive> {

    public Integer archiveDemographicCust(DemographicCust dc);
}
