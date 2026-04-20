//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.BillingONEAReport;
import ca.openosp.openo.billings.ca.on.data.BillingProviderData;

public interface BillingONEAReportDao extends AbstractDao<BillingONEAReport> {
    List<BillingONEAReport> findByProviderOhipNoAndGroupNoAndSpecialtyAndProcessDate(String providerOhipNo, String groupNo, String specialty, Date processDate);

    List<BillingONEAReport> findByProviderOhipNoAndGroupNoAndSpecialtyAndProcessDateAndBillingNo(String providerOhipNo, String groupNo, String specialty, Date processDate, Integer billingNo);

    List<BillingONEAReport> findByBillingNo(Integer billingNo);

    List<String> getBillingErrorList(Integer billingNo);

    List<BillingONEAReport> findByMagic(String ohipNo, String billingGroupNo, String specialtyCode, Date fromDate, Date toDate, String reportName);

    List<BillingONEAReport> findByMagic(List<BillingProviderData> list, Date fromDate, Date toDate, String reportName);
}
