//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.List;

import ca.openosp.openo.commn.model.DemographicSets;
import ca.openosp.openo.utility.LoggedInInfo;

public interface DemographicSetsManager {


    public List<DemographicSets> getAllDemographicSets(LoggedInInfo loggedInInfo, int offset, int itemsToReturn);

    public List<String> getNames(LoggedInInfo loggedInInfo);

    public List<DemographicSets> getByName(LoggedInInfo loggedInInfo, String setName);
}
