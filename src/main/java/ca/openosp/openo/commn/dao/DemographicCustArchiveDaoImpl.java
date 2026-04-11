//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.DemographicCust;
import ca.openosp.openo.commn.model.DemographicCustArchive;
import org.springframework.stereotype.Repository;

@Repository
public class DemographicCustArchiveDaoImpl extends AbstractDaoImpl<DemographicCustArchive>
        implements DemographicCustArchiveDao {

    public DemographicCustArchiveDaoImpl() {
        super(DemographicCustArchive.class);
    }

    @Override
    public Integer archiveDemographicCust(DemographicCust dc) {
        DemographicCustArchive dca = new DemographicCustArchive(dc);
        persist(dca);
        return dca.getId();
    }
}
