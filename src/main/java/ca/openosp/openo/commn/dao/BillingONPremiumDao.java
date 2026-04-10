//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import ca.openosp.openo.commn.model.BillingONPremium;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.utility.LoggedInInfo;

public interface BillingONPremiumDao extends AbstractDao<BillingONPremium> {
    List<BillingONPremium> getActiveRAPremiumsByPayDate(Date startDate, Date endDate, Locale locale);

    List<BillingONPremium> getActiveRAPremiumsByProvider(Provider p, Date startDate, Date endDate, Locale locale);

    List<BillingONPremium> getRAPremiumsByRaHeaderNo(Integer raHeaderNo);

    void parseAndSaveRAPremiums(LoggedInInfo loggedInInfo, Integer raHeaderNo, Locale locale);
}
