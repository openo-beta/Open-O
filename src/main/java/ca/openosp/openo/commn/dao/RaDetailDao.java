//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.RaDetail;

public interface RaDetailDao extends AbstractDao<RaDetail> {
    List<RaDetail> findByBillingNo(Integer billingNo);

    List<RaDetail> findByRaHeaderNo(Integer raHeaderNo);

    List<Integer> findUniqueBillingNoByRaHeaderNoAndProviderAndNotErrorCode(Integer raHeaderNo, String providerOhipNo, String codes);

    List<RaDetail> getRaDetailByDate(Date startDate, Date endDate, Locale locale);

    List<RaDetail> getRaDetailByDate(Provider p, Date startDate, Date endDate, Locale locale);

    List<RaDetail> getRaDetailByClaimNo(String claimNo);

    List<RaDetail> search_raerror35(Integer raHeaderNo, String error1, String error2, String providerOhipNo);

    List<Integer> search_ranoerror35(Integer raHeaderNo, String error1, String error2, String providerOhipNo);

    List<Integer> search_raob(Integer raHeaderNo);

    List<Integer> search_racolposcopy(Integer raHeaderNo);

    List<Object[]> search_raprovider(Integer raHeaderNo);

    List<RaDetail> search_rasummary_dt(Integer raHeaderNo, String providerOhipNo);

    List<Integer> search_ranoerrorQ(Integer raHeaderNo, String providerOhipNo);

    List<String> getBillingExplanatoryList(Integer billingNo);

    List<RaDetail> findByBillingNoServiceDateAndProviderNo(Integer billingNo, String serviceDate, String providerNo);

    List<RaDetail> findByBillingNoAndErrorCode(Integer billingNo, String errorCode);

    List<Integer> findDistinctIdOhipWithError(Integer raHeaderNo, String providerOhipNo, List<String> codes);

    List<RaDetail> findByHeaderAndBillingNos(Integer raHeaderNo, Integer billingNo);

    List<RaDetail> findByRaHeaderNoAndServiceCodes(Integer raHeaderNo, List<String> serviceCodes);

    List<RaDetail> findByRaHeaderNoAndProviderOhipNo(Integer raHeaderNo, String providerOhipNo);
}
