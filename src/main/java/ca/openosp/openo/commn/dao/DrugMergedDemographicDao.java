//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Drug;

public interface DrugMergedDemographicDao extends DrugDao {
    List<Drug> findByDemographicId(Integer demographicId);

    List<Drug> findByDemographicId(Integer demographicId, Boolean archived);

    List<Drug> findByDemographicIdOrderByDate(Integer demographicId, Boolean archived);

    List<Drug> findByDemographicIdOrderByPosition(Integer demographicId, Boolean archived);

    List<Drug> findByDemographicIdSimilarDrugOrderByDate(Integer demographicId, String regionalIdentifier, String customName);

    List<Drug> findByDemographicIdSimilarDrugOrderByDate(Integer demographicId, String regionalIdentifier, String customName, String brandName);

    List<Drug> findByDemographicIdSimilarDrugOrderByDate(Integer demographicId, String regionalIdentifier, String customName, String brandName, String atc);

    List<Drug> getUniquePrescriptions(String demographic_no);

    List<Drug> getPrescriptions(String demographic_no);

    List<Drug> getPrescriptions(String demographic_no, boolean all);

    List<Drug> findByDemographicIdUpdatedAfterDate(Integer demographicId, Date updatedAfterThisDate);

    List<Drug> findByDemographicIdAndAtc(int demographicNo, String atc);

    List<Drug> findByDemographicIdAndRegion(int demographicNo, String regionalIdentifier);

    List<Drug> findByDemographicIdAndDrugId(int demographicNo, Integer drugId);

    List<Object[]> findDrugsAndPrescriptions(int demographicNo);

    List<Drug> findByRegionBrandDemographicAndProvider(String regionalIdentifier, String brandName, int demographicNo, String providerNo);

    Drug findByBrandNameDemographicAndProvider(String brandName, int demographicNo, String providerNo);

    Drug findByCustomNameDemographicIdAndProviderNo(String customName, int demographicNo, String providerNo);

    Integer findLastNotArchivedId(String brandName, String genericName, int demographicNo);

    Drug findByDemographicIdRegionalIdentifierAndAtcCode(String atcCode, String regionalIdentifier, int demographicNo);
}
