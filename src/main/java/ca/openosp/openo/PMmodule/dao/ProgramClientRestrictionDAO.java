//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.openosp.openo.PMmodule.model.ProgramClientRestriction;
import ca.openosp.openo.commn.dao.DemographicDao;

/**
 *
 */
public interface ProgramClientRestrictionDAO {

    public Collection<ProgramClientRestriction> find(int programId, int demographicNo);

    public void save(ProgramClientRestriction restriction);

    public ProgramClientRestriction find(int restrictionId);

    public Collection<ProgramClientRestriction> findForProgram(int programId);

    public Collection<ProgramClientRestriction> findDisabledForProgram(int programId);

    public Collection<ProgramClientRestriction> findForClient(int demographicNo);

    public Collection<ProgramClientRestriction> findForClient(int demographicNo, int facilityId);

    public Collection<ProgramClientRestriction> findDisabledForClient(int demographicNo);

    public void setDemographicDao(DemographicDao demographicDao);

    public void setProgramDao(ProgramDao programDao);

    public void setProviderDao(ProviderDao providerDao);

}
 