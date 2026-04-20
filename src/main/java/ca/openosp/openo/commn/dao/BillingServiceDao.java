//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.BillingService;

public interface BillingServiceDao extends AbstractDao<BillingService> {
    static public final String BC = "BC";

    List<BillingService> getBillingCodeAttr(String serviceCode);

    boolean codeRequiresSLI(String code);

    List<BillingService> findBillingCodesByCode(String code, String region);

    List<BillingService> findByServiceCode(String code);

    List<BillingService> findByServiceCodeAndDate(String code, Date date);

    List<BillingService> findByServiceCodes(List<String> codes);

    List<BillingService> finAllPrivateCodes();

    List<BillingService> findBillingCodesByCode(String code, String region, int order);

    List<BillingService> findBillingCodesByCode(String code, String region, Date billingDate, int order);

    String searchDescBillingCode(String code, String region);

    List<BillingService> search(String str, String region, Date billingDate);

    BillingService searchBillingCode(String str, String region);

    BillingService searchBillingCode(String str, String region, Date billingDate);

    BillingService searchPrivateBillingCode(String privateCode, Date billingDate);

    boolean editBillingCodeDesc(String desc, String val, Integer codeId);

    boolean editBillingCode(String val, Integer codeId);

    boolean insertBillingCode(String code, String date, String description, String termDate, String region);

    Date getLatestServiceDate(Date endDate, String serviceCode);

    Object[] getUnitPrice(String bcode, Date date);

    String getUnitPercentage(String bcode, Date date);

    List<BillingService> findBillingCodesByFontStyle(Integer styleId);

    List<BillingService> findByRegionGroupAndType(String billRegion, String serviceGroup, String serviceType);

    List<BillingService> findByServiceCodeOrDescription(String serviceCode);

    List<BillingService> findMostRecentByServiceCode(String serviceCode);

    List<BillingService> findAll();

    List<Object[]> findSomethingByBillingId(Integer billingNo);

    List<BillingService> findGst(String code, Date date);

    List<BillingService> search_service_code(String code, String code1, String code2, String desc, String desc1, String desc2);

    List<BillingService> findByServiceCodeAndLatestDate(String serviceCode, Date date);

    List<Object[]> findBillingServiceAndCtlBillingServiceByMagic(String serviceType, String serviceGroup, Date billReferenceDate);

    List<Object> findBillingCodesByCodeAndTerminationDate(String serviceCode, Date terminationDate);

    String getCodeDescription(String val, String billReferalDate);
}
