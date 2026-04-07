//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.utility.LoggedInInfo;

public interface AllergyManager {

    public Allergy getAllergy(LoggedInInfo loggedInInfo, Integer id);

    public List<Allergy> getActiveAllergies(LoggedInInfo loggedInInfo, Integer demographicNo);

    public List<Allergy> getUpdatedAfterDate(LoggedInInfo loggedInInfo, Date updatedAfterThisDateInclusive,
                                             int itemsToReturn);

    public List<Allergy> getByDemographicIdUpdatedAfterDate(LoggedInInfo loggedInInfo, Integer demographicId,
                                                            Date updatedAfterThisDate);

    public List<Allergy> getAllergiesByProgramProviderDemographicDate(LoggedInInfo loggedInInfo, Integer programId,
                                                                      String providerNo, Integer demographicId, Calendar updatedAfterThisDateInclusive, int itemsToReturn);
}
