//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.math.BigDecimal;
import java.util.List;

import ca.openosp.openo.commn.model.BillingOnItemPayment;

public interface BillingOnItemPaymentDao extends AbstractDao<BillingOnItemPayment> {
    BillingOnItemPayment findByPaymentIdAndItemId(int paymentId, int itemId);

    List<BillingOnItemPayment> getAllByItemId(int itemId);

    List<BillingOnItemPayment> getItemsByPaymentId(int paymentId);

    BigDecimal getAmountPaidByItemId(int itemId);

    List<BillingOnItemPayment> getItemPaymentByInvoiceNoItemId(Integer ch1_id, Integer item_id);

    List<BillingOnItemPayment> findByBillingNo(int billingNo);

    public static BigDecimal calculateItemPaymentTotal(List<BillingOnItemPayment> paymentRecords) {

        BigDecimal paidTotal = new BigDecimal("0.00");
        for (BillingOnItemPayment bPay : paymentRecords) {
            BigDecimal amtPaid = bPay.getPaid();
            paidTotal = paidTotal.add(amtPaid);
        }

        return paidTotal;
    }

    public static BigDecimal calculateItemRefundTotal(List<BillingOnItemPayment> paymentRecords) {

        BigDecimal refundTotal = new BigDecimal("0.00");
        for (BillingOnItemPayment bPay : paymentRecords) {
            BigDecimal amtRefunded = bPay.getRefund();
            refundTotal = refundTotal.add(amtRefunded);
        }

        return refundTotal;
    }
}
