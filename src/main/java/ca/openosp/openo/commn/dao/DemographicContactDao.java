//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import ca.openosp.openo.commn.model.DemographicContact;

public interface DemographicContactDao extends AbstractDao<DemographicContact> {

    public List<DemographicContact> findByDemographicNo(int demographicNo);

    public List<DemographicContact> findActiveByDemographicNo(int demographicNo);

    public List<DemographicContact> findByDemographicNoAndCategory(int demographicNo, String category);

    public List<DemographicContact> find(int demographicNo, int contactId);

    public List<DemographicContact> findAllByContactIdAndCategoryAndType(int contactId, String category, int type);

    public List<DemographicContact> findAllByDemographicNoAndCategoryAndType(int demographicNo, String category,
                                                                             int type);

    public List<DemographicContact> findSDMByDemographicNo(int demographicNo);
}
