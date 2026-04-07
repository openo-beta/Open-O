//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.BillingONCHeader1;
import ca.openosp.openo.commn.model.BillingONItem;
import ca.openosp.openo.commn.model.BillingONPayment;
import ca.openosp.openo.commn.model.BillingOnTransaction;
import ca.openosp.openo.billings.ca.on.data.BillingClaimHeader1Data;

public interface BillingOnTransactionDao extends AbstractDao<BillingOnTransaction> {
    BillingOnTransaction getTransTemplate(BillingONCHeader1 cheader1, BillingONItem billItem, BillingONPayment billPayment, String curProviderNo, int itempaymentId);

    BillingOnTransaction getUpdateCheader1TransTemplate(BillingClaimHeader1Data cheader1, String curProviderNo);
}
