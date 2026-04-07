//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.commn.dao.CtlBillingServiceDao;
import ca.openosp.openo.commn.dao.CtlBillingServiceDaoImpl;
import ca.openosp.openo.managers.model.ServiceType;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillingManagerImpl implements BillingManager {

    @Autowired
    CtlBillingServiceDao ctlBillingServiceDao;

    /*
     * I'm only doing that conversion in the manager because I don't have time to
     * refactor the DAO method..but given more time..that's where I would do it.
     * Regardless those other calls should be moved over to calling this one.
     */
    @Override
    public List<ServiceType> getUniqueServiceTypes(LoggedInInfo loggedInInfo) {
        return getUniqueServiceTypes(loggedInInfo, CtlBillingServiceDaoImpl.DEFAULT_STATUS);
    }

    /*
     * I'm only doing that conversion in the manager because I don't have time to
     * refactor the DAO method..but given more time..that's where I would do it.
     * Regardless those other calls should be moved over to calling this one.
     */
    @Override
    public List<ServiceType> getUniqueServiceTypes(LoggedInInfo loggedInInfo, String type) {
        List<ServiceType> result = new ArrayList<ServiceType>();

        for (Object[] r : ctlBillingServiceDao.getUniqueServiceTypes(type)) {
            result.add(new ServiceType((String) r[0], (String) r[1]));
        }

        return result;
    }
}
