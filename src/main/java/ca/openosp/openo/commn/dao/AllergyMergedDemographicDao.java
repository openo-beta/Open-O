//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Allergy;

public interface AllergyMergedDemographicDao extends AbstractDao<Allergy> {


    public List<Allergy> findAllergies(final Integer demographic_no);


    public List<Allergy> findActiveAllergies(final Integer demographic_no);


    public List<Allergy> findActiveAllergiesOrderByDescription(final Integer demographic_no);

    public List<Allergy> findByDemographicIdUpdatedAfterDate(final Integer demographicId,
                                                             final Date updatedAfterThisDate);

    public List<Allergy> findAllCustomAllergiesWithNullNonDrugFlag(int start, int limit);
}
