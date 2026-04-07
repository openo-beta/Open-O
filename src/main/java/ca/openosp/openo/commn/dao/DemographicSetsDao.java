//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collection;
import java.util.List;

import ca.openosp.openo.commn.model.DemographicSets;

public interface DemographicSetsDao extends AbstractDao<DemographicSets> {

    public List<DemographicSets> findBySetName(String setName);

    public List<DemographicSets> findBySetNames(Collection<String> setNameList);

    public List<DemographicSets> findBySetNameAndEligibility(String setName, String eligibility);

    public List<String> findSetNamesByDemographicNo(Integer demographicNo);

    public List<String> findSetNames();

    public List<DemographicSets> findBySetNameAndDemographicNo(String setName, int demographicNo);
}
