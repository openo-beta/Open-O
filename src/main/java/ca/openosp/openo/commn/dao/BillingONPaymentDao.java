//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ca.openosp.openo.commn.model.BillingONCHeader1;
import ca.openosp.openo.commn.model.BillingONPayment;

public interface BillingONPaymentDao extends AbstractDao<BillingONPayment> {
    void setBillingONExtDao(BillingONExtDao billingONExtDao);

    void setBillingONCHeader1Dao(BillingONCHeader1Dao billingONCHeader1Dao);

    BillingONExtDao getBillingONExtDao();

    BillingONCHeader1Dao getBillingONCHeader1Dao();

    List<BillingONPayment> listPaymentsByBillingNo(Integer billingNo);

    List<BillingONPayment> listPaymentsByBillingNoDesc(Integer billingNo);

    BigDecimal getPaymentsSumByBillingNo(Integer billingNo);

    BigDecimal getPaymentsRefundByBillingNo(Integer billingNo);

    BigDecimal getPaymentsDiscountByBillingNo(Integer billingNo);

    String getTotalSumByBillingNoWeb(String billingNo);

    String getPaymentsRefundByBillingNoWeb(String billingNo);

    int getPaymentIdByBillingNo(int billingNo);

    int getCountOfPaymentByPaymentTypeId(int paymentTypeId);

    String getPaymentTypeById(int paymentTypeId);

    List<BillingONPayment> find3rdPartyPayRecordsByBill(BillingONCHeader1 bCh1);

    List<Integer> find3rdPartyPayments(Integer billingNo);

    List<BillingONPayment> find3rdPartyPaymentsByBillingNo(Integer billingNo);

    List<BillingONPayment> find3rdPartyPayRecordsByBill(BillingONCHeader1 bCh1, Date startDate, Date endDate);

    void createPayment(BillingONCHeader1 bCh1, Locale locale, String payType, BigDecimal paidAmt, String payMethod, String providerNo);

    static BigDecimal calculatePaymentTotal(List<BillingONPayment> paymentRecords) {
        BigDecimal paidTotal = new BigDecimal("0.00");
        for (BillingONPayment bPay : paymentRecords) {
            BigDecimal amtPaid = bPay.getTotal_payment();
            paidTotal = paidTotal.add(amtPaid);
        }
        return paidTotal;
    }

    static BigDecimal calculateRefundTotal(List<BillingONPayment> paymentRecords) {
        BigDecimal refundTotal = new BigDecimal("0.00");
        for (BillingONPayment bPay : paymentRecords) {
            BigDecimal amtRefunded = bPay.getTotal_refund();
            refundTotal = refundTotal.add(amtRefunded);
        }
        return refundTotal;
    }
}
