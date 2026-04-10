//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.AccessType;
import ca.openosp.openo.PMmodule.model.ProgramAccess;

public interface ProgramAccessDAO {

    public List<ProgramAccess> getAccessListByProgramId(Long programId);

    public ProgramAccess getProgramAccess(Long id);

    public ProgramAccess getProgramAccess(Long programId, Long accessTypeId);

    public List<ProgramAccess> getProgramAccessListByType(Long programId, String accessType);

    public void saveProgramAccess(ProgramAccess pa);

    public void deleteProgramAccess(Long id);

    public List<AccessType> getAccessTypes();

    public AccessType getAccessType(Long id);
}
