//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.Drug;

import java.util.Date;
import java.util.List;

public interface DrugDao extends AbstractDao<Drug> {

    public boolean addNewDrug(Drug d);

    public List<Drug> findByPrescriptionId(Integer prescriptionId);

    public List<Drug> findByDemographicId(Integer demographicId);

    public List<Drug> findByDemographicId(Integer demographicId, Boolean archived);

    public List<Drug> findByScriptNo(Integer scriptNo, Boolean archived);

    public List<Drug> findByDemographicIdOrderByDate(Integer demographicId, Boolean archived);

    public List<Drug> findByDemographicIdOrderByPositionForExport(Integer demographicId, Boolean archived);

    public List<Drug> findByDemographicIdOrderByPosition(Integer demographicId, Boolean archived);

    public List<Drug> findByDemographicIdSimilarDrugOrderByDate(Integer demographicId, String regionalIdentifier,
                                                                String customName);

    public List<Drug> findByDemographicIdSimilarDrugOrderByDate(Integer demographicId, String regionalIdentifier,
                                                                String customName, String brandName);

    public List<Drug> findByDemographicIdSimilarDrugOrderByDate(Integer demographicId, String regionalIdentifier,
                                                                String customName, String brandName, String atc);

    public List<Drug> getUniquePrescriptions(String demographic_no);

    public List<Drug> getPrescriptions(String demographic_no);

    public List<Drug> getPrescriptions(String demographic_no, boolean all);

    public int getNumberOfDemographicsWithRxForProvider(String providerNo, Date startDate, Date endDate,
                                                        boolean distinct);

    public List<Drug> findByDemographicIdUpdatedAfterDate(Integer demographicId, Date updatedAfterThisDate);

    public List<Drug> findByAtc(String atc);

    public List<Drug> findByAtc(List<String> atc);

    public List<Drug> findByDemographicIdAndAtc(int demographicNo, String atc);

    public List<Drug> findByDemographicIdAndRegion(int demographicNo, String regionalIdentifier);

    public List<Drug> findByDemographicIdAndDrugId(int demographicNo, Integer drugId);

    public List<Object[]> findDrugsAndPrescriptions(int demographicNo);

    public List<Object[]> findDrugsAndPrescriptionsByScriptNumber(int scriptNumber);

    public int getMaxPosition(int demographicNo);

    public Drug findByEverything(String providerNo, int demographicNo, Date rxDate, Date endDate, Date writtenDate,
                                 String brandName, String gcn_SEQNO, String customName, float takeMin, float takeMax, String frequencyCode,
                                 String duration, String durationUnit, String quantity, String unitName, int repeat, Date lastRefillDate,
                                 boolean nosubs, boolean prn, String escapedSpecial, String outsideProviderName, String outsideProviderOhip,
                                 boolean customInstr, Boolean longTerm, boolean customNote, Boolean pastMed,
                                 Boolean patientCompliance, String specialInstruction, String comment, boolean startDateUnknown);

    public List<Object[]> findByParameter(String parameter, String value);

    public List<Drug> findByRegionBrandDemographicAndProvider(String regionalIdentifier, String brandName,
                                                              int demographicNo, String providerNo);

    public Drug findByBrandNameDemographicAndProvider(String brandName, int demographicNo, String providerNo);

    public Drug findByCustomNameDemographicIdAndProviderNo(String customName, int demographicNo, String providerNo);

    public Integer findLastNotArchivedId(String brandName, String genericName, int demographicNo);

    public Drug findByDemographicIdRegionalIdentifierAndAtcCode(String atcCode, String regionalIdentifier,
                                                                int demographicNo);

    public List<String> findSpecialInstructions();

    public List<String> findSpecialInstructionsMatching(String spInstructQuery);

    public List<Integer> findDemographicIdsUpdatedAfterDate(Date updatedAfterThisDate);

    public List<Integer> findNewDrugsSinceDemoKey(String keyName);

    public List<Drug> findLongTermDrugsByDemographic(Integer demographicId);

    /**
     * Retrieves a list of Drug objects based on the provided script number and demographic number.
     *
     * @param scriptNo the script number associated with the drugs to be retrieved
     * @param demographicNo the demographic number associated with the drugs to be retrieved
     * @return a list of Drug objects matching the provided script number and demographic number
     */
    List<Drug> findBy(int scriptNo, int demographicNo);
}
