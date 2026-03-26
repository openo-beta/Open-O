//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.dao.AllergyDao;
import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.commn.model.ConsentType;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.openosp.openo.log.LogAction;

@Service
public class AllergyManagerImpl implements AllergyManager {
    @Autowired
    private AllergyDao allergyDao;

    @Autowired
    private PatientConsentManager patientConsentManager;

    @Override
    public Allergy getAllergy(LoggedInInfo loggedInInfo, Integer id) {
        Allergy result = allergyDao.find(id);

        // --- log action ---
        if (result != null) {
            LogAction.addLogSynchronous(loggedInInfo, "AllergyManager.getAllergy", "id=" + id);
        }

        return (result);
    }

    @Override
    public List<Allergy> getActiveAllergies(LoggedInInfo loggedInInfo, Integer demographicNo) {
        List<Allergy> results = allergyDao.findActiveAllergiesOrderByDescription(demographicNo);

        // --- log action ---
        if (results != null && results.size() > 0) {
            LogAction.addLogSynchronous(loggedInInfo, "AllergyManager.getActiveAllergies",
                    "demographicNo=" + demographicNo);
        }

        return (results);
    }

    @Override
    public List<Allergy> getUpdatedAfterDate(LoggedInInfo loggedInInfo, Date updatedAfterThisDateInclusive,
                                             int itemsToReturn) {
        List<Allergy> results = allergyDao.findByUpdateDate(updatedAfterThisDateInclusive, itemsToReturn);
        patientConsentManager.filterProviderSpecificConsent(loggedInInfo, results);
        LogAction.addLogSynchronous(loggedInInfo, "AllergyManager.getUpdatedAfterDate",
                "updatedAfterThisDateInclusive=" + updatedAfterThisDateInclusive);

        return (results);
    }

    @Override
    public List<Allergy> getByDemographicIdUpdatedAfterDate(LoggedInInfo loggedInInfo, Integer demographicId,
                                                            Date updatedAfterThisDate) {
        List<Allergy> results = new ArrayList<Allergy>();
        ConsentType consentType = patientConsentManager.getProviderSpecificConsent(loggedInInfo);
        if (patientConsentManager.hasPatientConsented(demographicId, consentType)) {
            results = allergyDao.findByDemographicIdUpdatedAfterDate(demographicId, updatedAfterThisDate);
            LogAction.addLogSynchronous(loggedInInfo, "AllergyManager.getByDemographicIdUpdatedAfterDate",
                    "demographicId=" + demographicId + " updatedAfterThisDate=" + updatedAfterThisDate);
        }
        return (results);
    }

    /**
     * At this time, not all criteria maybe available in oscar but the method
     * signature is what is "should" be
     * and hopefully can be refactored as data becomes available.
     */
    @Override
    public List<Allergy> getAllergiesByProgramProviderDemographicDate(LoggedInInfo loggedInInfo, Integer programId,
                                                                      String providerNo, Integer demographicId, Calendar updatedAfterThisDateInclusive, int itemsToReturn) {
        List<Allergy> results = allergyDao.findByProviderDemographicLastUpdateDate(providerNo, demographicId,
                updatedAfterThisDateInclusive.getTime(), itemsToReturn);

        LogAction.addLogSynchronous(loggedInInfo, "AllergyManager.getUpdatedAfterDate",
                "programId=" + programId + ", providerNo=" + providerNo + ", demographicId=" + demographicId
                        + ", updatedAfterThisDateInclusive=" + updatedAfterThisDateInclusive.getTime());

        return (results);
    }
}
