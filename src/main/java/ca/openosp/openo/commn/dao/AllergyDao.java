//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Allergy;

public interface AllergyDao extends AbstractDao<Allergy> {

    public List<Allergy> findAllergies(Integer demographic_no);

    public List<Allergy> findActiveAllergies(Integer demographic_no);

    public List<Allergy> findActiveAllergiesOrderByDescription(Integer demographic_no);

    public List<Allergy> findByDemographicIdUpdatedAfterDate(Integer demographicId, Date updatedAfterThisDate);

    public List<Integer> findDemographicIdsUpdatedAfterDate(Date updatedAfterThisDate);

    public List<Allergy> findByUpdateDate(Date updatedAfterThisDateInclusive, int itemsToReturn);

    public List<Allergy> findByProviderDemographicLastUpdateDate(String providerNo, Integer demographicId,
                                                                 Date updatedAfterThisDateExclusive, int itemsToReturn);

    public List<Allergy> findAllCustomAllergiesWithNullNonDrugFlag(int start, int limit);
}
