//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.commn.model.ProviderDefaultProgram;

public interface ProviderDefaultProgramDao extends AbstractDao<ProviderDefaultProgram> {

    public List<ProviderDefaultProgram> getProgramByProviderNo(String providerNo);

    public void setDefaultProgram(String providerNo, int programId);

    public List<ProviderDefaultProgram> getProviderSig(String providerNo);

    public void saveProviderDefaultProgram(ProviderDefaultProgram pdp);

    public void toggleSig(String providerNo);

    public List<Program> findProgramsByProvider(String providerNo);

    public List<Program> findProgramsByFacilityId(Integer facilityId);
}
