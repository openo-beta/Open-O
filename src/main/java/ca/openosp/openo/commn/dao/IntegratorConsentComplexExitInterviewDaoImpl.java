//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.FacilityDemographicPrimaryKey;
import ca.openosp.openo.commn.model.IntegratorConsentComplexExitInterview;
import org.springframework.stereotype.Repository;

@Repository
public class IntegratorConsentComplexExitInterviewDaoImpl extends AbstractDaoImpl<IntegratorConsentComplexExitInterview> implements IntegratorConsentComplexExitInterviewDao {

    public IntegratorConsentComplexExitInterviewDaoImpl() {
        super(IntegratorConsentComplexExitInterview.class);
    }

    public IntegratorConsentComplexExitInterview find(FacilityDemographicPrimaryKey id) {
        return (entityManager.find(IntegratorConsentComplexExitInterview.class, id));
    }

}
