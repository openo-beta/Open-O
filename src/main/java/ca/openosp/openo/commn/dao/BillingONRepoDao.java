//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.BillingONItem;
import ca.openosp.openo.commn.model.BillingONCHeader1;

import java.util.Locale;

import ca.openosp.openo.commn.model.BillingONRepo;

public interface BillingONRepoDao extends AbstractDao<BillingONRepo> {
    void createBillingONItemEntry(BillingONItem bItem, Locale locale);

    void createBillingONCHeader1Entry(BillingONCHeader1 bCh1, Locale locale);
}
