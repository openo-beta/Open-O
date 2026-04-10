//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.merge.MergedDemographicTemplate;
import ca.openosp.openo.commn.model.Allergy;
import org.springframework.stereotype.Repository;

@Repository("AllergyDao")
public class AllergyMergedDemographicDaoImpl extends AllergyDaoImpl implements AllergyMergedDemographicDao {

    @Override
    public List<Allergy> findAllergies(final Integer demographic_no) {
        List<Allergy> result = super.findAllergies(demographic_no);
        MergedDemographicTemplate<Allergy> template = new MergedDemographicTemplate<Allergy>() {
            @Override
            public List<Allergy> findById(Integer demographic_no) {
                return AllergyMergedDemographicDaoImpl.super.findAllergies(demographic_no);
            }
        };
        return template.findMerged(demographic_no, result);
    }

    @Override
    public List<Allergy> findActiveAllergies(final Integer demographic_no) {
        List<Allergy> result = super.findActiveAllergies(demographic_no);
        MergedDemographicTemplate<Allergy> template = new MergedDemographicTemplate<Allergy>() {
            @Override
            public List<Allergy> findById(Integer demographic_no) {
                return AllergyMergedDemographicDaoImpl.super.findActiveAllergies(demographic_no);
            }
        };
        return template.findMerged(demographic_no, result);

    }

    @Override
    public List<Allergy> findActiveAllergiesOrderByDescription(final Integer demographic_no) {
        List<Allergy> result = super.findActiveAllergiesOrderByDescription(demographic_no);
        MergedDemographicTemplate<Allergy> template = new MergedDemographicTemplate<Allergy>() {
            @Override
            public List<Allergy> findById(Integer demographic_no) {
                return AllergyMergedDemographicDaoImpl.super.findActiveAllergiesOrderByDescription(demographic_no);
            }
        };
        return template.findMerged(demographic_no, result);
    }

    @Override
    public List<Allergy> findByDemographicIdUpdatedAfterDate(final Integer demographicId,
                                                             final Date updatedAfterThisDate) {
        List<Allergy> result = super.findByDemographicIdUpdatedAfterDate(demographicId, updatedAfterThisDate);
        MergedDemographicTemplate<Allergy> template = new MergedDemographicTemplate<Allergy>() {
            @Override
            public List<Allergy> findById(Integer demographic_no) {
                return AllergyMergedDemographicDaoImpl.super.findByDemographicIdUpdatedAfterDate(demographicId,
                        updatedAfterThisDate);
            }
        };
        return template.findMerged(demographicId, result);
    }
}
