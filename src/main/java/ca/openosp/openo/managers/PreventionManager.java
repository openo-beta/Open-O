//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.util.*;

import ca.openosp.openo.commn.model.Prevention;
import ca.openosp.openo.commn.model.PreventionExt;
import ca.openosp.openo.utility.LoggedInInfo;

public interface PreventionManager {

    public List<Prevention> getUpdatedAfterDate(LoggedInInfo loggedInInfo, Date updatedAfterThisDateExclusive,
                                                int itemsToReturn);

    public List<Prevention> getByDemographicIdUpdatedAfterDate(LoggedInInfo loggedInInfo, Integer demographicId,
                                                               Date updatedAfterThisDateExclusive);

    public Prevention getPrevention(LoggedInInfo loggedInInfo, Integer id);

    public List<PreventionExt> getPreventionExtByPrevention(LoggedInInfo loggedInInfo, Integer preventionId);

    public ArrayList<String> getPreventionTypeList();

    public ArrayList<HashMap<String, String>> getPreventionTypeDescList();

    public boolean hideItem(String item);

    public void addCustomPreventionItems(String items);

    public void addPreventionWithExts(Prevention prevention, HashMap<String, String> exts);

    public List<Prevention> getPreventionsByProgramProviderDemographicDate(LoggedInInfo loggedInInfo, Integer programId,
                                                                           String providerNo, Integer demographicId, Calendar updatedAfterThisDateExclusive, int itemsToReturn);

    public List<Prevention> getPreventionsByDemographicNo(LoggedInInfo loggedInInfo, Integer demographicNo);

    public String getWarnings(LoggedInInfo loggedInInfo, String demo);

    public String checkNames(String k);

    public boolean isDisabled();

    public boolean isCreated();

    public Set<String> getPreventionStopSigns();

    public boolean isPrevDisabled(String name);

    public List<String> getDisabledPreventions();

    public boolean isHidePrevItemExist();

    public boolean setDisabledPreventions(List<String> newDisabledPreventions);

    public List<Prevention> getImmunizationsByDemographic(LoggedInInfo loggedInInfo, Integer demographicNo);

    public String getCustomPreventionItems();

}
