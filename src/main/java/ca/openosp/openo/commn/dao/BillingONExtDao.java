//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.BillingONCHeader1;
import ca.openosp.openo.commn.model.BillingONExt;
import ca.openosp.openo.commn.model.BillingONPayment;

/**
 * @author mweston4
 */
public interface BillingONExtDao extends AbstractDao<BillingONExt> {

    public final static String KEY_PAYMENT = "payment";
    public final static String KEY_REFUND = "refund";
    public final static String KEY_DISCOUNT = "discount";
    public final static String KEY_CREDIT = "credit";
    public final static String KEY_PAY_DATE = "payDate";
    public final static String KEY_PAY_METHOD = "payMethod";
    public final static String KEY_TOTAL = "total";
    public final static String KEY_GST = "gst";

    public List<BillingONExt> find(String key, String value);

    public List<BillingONExt> findByBillingNoAndKey(Integer billingNo, String key);

    public List<BillingONExt> findByBillingNoAndPaymentIdAndKey(Integer billingNo, Integer paymentId, String key);

    public String getPayMethodDesc(BillingONExt bExt);

    public BigDecimal getPayment(BillingONPayment paymentRecord);

    public BigDecimal getRefund(BillingONPayment paymentRecord);

    public BillingONExt getRemitTo(BillingONCHeader1 bCh1);

    public BillingONExt getBillTo(BillingONCHeader1 bCh1);

    public BillingONExt getBillToInactive(BillingONCHeader1 bCh1);

    public BillingONExt getDueDate(BillingONCHeader1 bCh1);

    public BillingONExt getUseBillTo(BillingONCHeader1 bCh1);

    public List<BillingONExt> find(Integer billingNo, String key, Date start, Date end);

    public List<BillingONExt> findByBillingNoAndPaymentNo(int billingNo, int paymentId);

    public List<BillingONExt> getClaimExtItems(int billingNo);

    public List<BillingONExt> getBillingExtItems(String billingNo);

    public List<BillingONExt> getInactiveBillingExtItems(String billingNo);

    public BigDecimal getAccountVal(int billingNo, String key);

    public BillingONExt getClaimExtItem(Integer billingNo, Integer demographicNo, String keyVal);

    public void setExtItem(int billingNo, int demographicNo, String keyVal, String value, Date dateTime, char status);

    // public static boolean isNumberKey(String key);
    public boolean isNumberKey(String key);
}
