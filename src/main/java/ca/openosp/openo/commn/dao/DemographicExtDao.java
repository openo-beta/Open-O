//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.*;

import ca.openosp.openo.commn.model.DemographicExt;
import ca.openosp.openo.commn.model.enumerator.DemographicExtKey;

public interface DemographicExtDao extends AbstractDao<DemographicExt> {

    public DemographicExt getDemographicExt(Integer id);

    public List<DemographicExt> getDemographicExtByDemographicNo(Integer demographicNo);

    public DemographicExt getDemographicExt(Integer demographicNo, DemographicExtKey demographicExtKey);

    public DemographicExt getDemographicExt(Integer demographicNo, String key);

    public List<DemographicExt> getDemographicExtByKeyAndValue(DemographicExtKey demographicExtKey, String value);

    public List<DemographicExt> getDemographicExtByKeyAndValue(String key, String value);

    public DemographicExt getLatestDemographicExt(Integer demographicNo, String key);

    public void updateDemographicExt(DemographicExt de);

    public void saveDemographicExt(Integer demographicNo, String key, String value);

    public void removeDemographicExt(Integer id);

    public void removeDemographicExt(Integer demographicNo, String key);

    public Map<String, String> getAllValuesForDemo(Integer demo);

    public void addKey(String providerNo, Integer demo, String key, String value);

    public void addKey(String providerNo, Integer demo, String key, String newValue, String oldValue);

    public List<String[]> getListOfValuesForDemo(Integer demo);

    public String getValueForDemoKey(Integer demo, String key);

    public List<Integer> findDemographicIdsByKeyVal(DemographicExtKey demographicExtKey, String val);

    public List<Integer> findDemographicIdsByKeyVal(String key, String val);

    public List<DemographicExt> getMultipleDemographicExtKeyForDemographicNumbersByProviderNumber(
            final DemographicExtKey demographicExtKey,
            final Collection<Integer> demographicNumbers,
            final String midwifeNumber);

    public List<Integer> getDemographicNumbersByDemographicExtKeyAndProviderNumberAndDemographicLastNameRegex(
            final DemographicExtKey key,
            final String providerNumber,
            final String lastNameRegex);

}
