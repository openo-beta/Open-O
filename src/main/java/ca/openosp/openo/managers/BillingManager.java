//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.managers.model.ServiceType;
import ca.openosp.openo.utility.LoggedInInfo;

public interface BillingManager {

    public List<ServiceType> getUniqueServiceTypes(LoggedInInfo loggedInInfo);

    public List<ServiceType> getUniqueServiceTypes(LoggedInInfo loggedInInfo, String type);
}


