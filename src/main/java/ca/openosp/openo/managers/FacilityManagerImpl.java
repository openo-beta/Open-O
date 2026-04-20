//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.util.List;

import ca.openosp.openo.commn.dao.FacilityDao;
import ca.openosp.openo.commn.model.Facility;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.openosp.openo.log.LogAction;

@Service
public class FacilityManagerImpl implements FacilityManager {
    @Autowired
    private FacilityDao facilityDao;

    public Facility getDefaultFacility(LoggedInInfo loggedInInfo) {
        List<Facility> results = facilityDao.findAll(true);
        if (results.size() == 0) {
            return (null);
        } else {
            return (results.get(0));
        }
    }

    public List<Facility> getAllFacilities(LoggedInInfo loggedInInfo, Boolean active) {
        List<Facility> results = facilityDao.findAll(active);

        //--- log action ---
        LogAction.addLogSynchronous(loggedInInfo, "FacilityManager.getAllFacilities", null);

        return (results);
    }
}
