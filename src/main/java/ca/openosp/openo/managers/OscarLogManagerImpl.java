//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.openosp.openo.log.LogAction;

@Service
public class OscarLogManagerImpl implements OscarLogManager {

    @Autowired
    private OscarLogDao oscarLogDao;

    public List<Object[]> getRecentDemographicsViewedByProvider(LoggedInInfo loggedInInfo, String providerNo, int startPosition, int itemsToReturn) {
        List<Object[]> results = oscarLogDao.getRecentDemographicsViewedByProvider(providerNo, startPosition, itemsToReturn);

        LogAction.addLogSynchronous(loggedInInfo, "OscarLogManager.getRecentDemographicsViewedByProvider", "providerNo" + providerNo);

        return results;

    }

    public List<Object[]> getRecentDemographicsViewedByProviderAfterDateIncluded(LoggedInInfo loggedInInfo, String providerNo, Date date, int startPosition, int itemsToReturn) {
        List<Object[]> results = oscarLogDao.getRecentDemographicsViewedByProviderAfterDateIncluded(providerNo, date, startPosition, itemsToReturn);
        LogAction.addLogSynchronous(loggedInInfo, "OscarLogManager.getRecentDemographicsViewedByProviderAfterDateIncluded", "providerNo" + providerNo);
        return results;
    }

}
