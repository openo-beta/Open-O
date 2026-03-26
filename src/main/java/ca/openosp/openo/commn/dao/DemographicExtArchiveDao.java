//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.DemographicExt;
import ca.openosp.openo.commn.model.DemographicExtArchive;

public interface DemographicExtArchiveDao extends AbstractDao<DemographicExtArchive> {

    public List<DemographicExtArchive> getDemographicExtArchiveByDemoAndKey(Integer demographicNo, String key);

    public DemographicExtArchive getDemographicExtArchiveByArchiveIdAndKey(Long archiveId, String key);

    public List<DemographicExtArchive> getDemographicExtArchiveByArchiveId(Long archiveId);

    public List<DemographicExtArchive> getDemographicExtArchiveByDemoReverseCronological(Integer demographicNo);

    public Integer archiveDemographicExt(DemographicExt de);
}
