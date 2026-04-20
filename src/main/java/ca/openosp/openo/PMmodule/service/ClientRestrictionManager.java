//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.service;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.PMmodule.model.ProgramClientRestriction;
import ca.openosp.openo.PMmodule.exception.ClientAlreadyRestrictedException;

public interface ClientRestrictionManager {

    List<ProgramClientRestriction> getActiveRestrictionsForProgram(int programId, Date asOfDate);

    List<ProgramClientRestriction> getDisabledRestrictionsForProgram(Integer programId, Date asOfDate);

    List<ProgramClientRestriction> getActiveRestrictionsForClient(int demographicNo, Date asOfDate);

    List<ProgramClientRestriction> getActiveRestrictionsForClient(int demographicNo, int facilityId, Date asOfDate);

    List<ProgramClientRestriction> getDisabledRestrictionsForClient(int demographicNo, Date asOfDate);

    ProgramClientRestriction checkClientRestriction(int programId, int demographicNo, Date asOfDate);

    void saveClientRestriction(ProgramClientRestriction restriction) throws ClientAlreadyRestrictedException;

    void terminateEarly(int programClientRestrictionId, String providerNo);

    void disableClientRestriction(int restrictionId);

    void enableClientRestriction(Integer restrictionId);
}
