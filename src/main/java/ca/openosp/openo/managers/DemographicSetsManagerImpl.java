//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.List;

import ca.openosp.openo.commn.dao.DemographicSetsDao;
import ca.openosp.openo.commn.model.DemographicSets;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DemographicSetsManagerImpl implements DemographicSetsManager {

    @Autowired
    DemographicSetsDao demographicSetsDao;


    public List<DemographicSets> getAllDemographicSets(LoggedInInfo loggedInInfo, int offset, int itemsToReturn) {

        List<DemographicSets> results = demographicSetsDao.findAll(offset, itemsToReturn);

        return (results);
    }

    public List<String> getNames(LoggedInInfo loggedInInfo) {

        return (demographicSetsDao.findSetNames());
    }

    public List<DemographicSets> getByName(LoggedInInfo loggedInInfo, String setName) {
        return (demographicSetsDao.findBySetName(setName));
    }
}
