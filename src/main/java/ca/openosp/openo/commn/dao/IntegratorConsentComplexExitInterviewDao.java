//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.FacilityDemographicPrimaryKey;
import ca.openosp.openo.commn.model.IntegratorConsentComplexExitInterview;

public interface IntegratorConsentComplexExitInterviewDao extends AbstractDao<IntegratorConsentComplexExitInterview> {
    IntegratorConsentComplexExitInterview find(FacilityDemographicPrimaryKey id);
}
