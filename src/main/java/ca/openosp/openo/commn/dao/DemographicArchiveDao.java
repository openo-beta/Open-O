//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.DemographicArchive;

public interface DemographicArchiveDao extends AbstractDao<DemographicArchive> {

    public List<DemographicArchive> findByDemographicNo(Integer demographicNo);

    public List<DemographicArchive> findRosterStatusHistoryByDemographicNo(Integer demographicNo);

    public Long archiveRecord(Demographic d);

    public List<DemographicArchive> findByDemographicNoChronologically(Integer demographicNo);

    public List<Object[]> findMetaByDemographicNo(Integer demographicNo);
}
