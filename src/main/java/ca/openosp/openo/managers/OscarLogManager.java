//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.utility.LoggedInInfo;

public interface OscarLogManager {
    public List<Object[]> getRecentDemographicsViewedByProvider(LoggedInInfo loggedInInfo, String providerNo, int startPosition, int itemsToReturn);

    public List<Object[]> getRecentDemographicsViewedByProviderAfterDateIncluded(LoggedInInfo loggedInInfo, String providerNo, Date date, int startPosition, int itemsToReturn);
}
