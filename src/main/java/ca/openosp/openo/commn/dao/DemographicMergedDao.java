//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.DemographicMerged;

public interface DemographicMergedDao extends AbstractDao<DemographicMerged> {

    public List<DemographicMerged> findCurrentByMergedTo(int demographicNo);

    public List<DemographicMerged> findCurrentByDemographicNo(int demographicNo);

    public List<DemographicMerged> findByDemographicNo(int demographicNo);

    public List<DemographicMerged> findByParentAndChildIds(Integer parentId, Integer childId);
}
