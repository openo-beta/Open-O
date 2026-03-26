//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.util.List;

import ca.openosp.openo.commn.model.Facility;
import ca.openosp.openo.utility.LoggedInInfo;

public interface FacilityManager {

    public Facility getDefaultFacility(LoggedInInfo loggedInInfo);

    public List<Facility> getAllFacilities(LoggedInInfo loggedInInfo, Boolean active);
}
 