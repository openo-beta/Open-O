//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.PMmodule.model.FunctionalUserType;
import ca.openosp.openo.PMmodule.model.ProgramFunctionalUser;

public interface ProgramFunctionalUserDAO {

    public List<FunctionalUserType> getFunctionalUserTypes();

    public FunctionalUserType getFunctionalUserType(Long id);

    public void saveFunctionalUserType(FunctionalUserType fut);

    public void deleteFunctionalUserType(Long id);

    public List<FunctionalUserType> getFunctionalUsers(Long programId);

    public ProgramFunctionalUser getFunctionalUser(Long id);

    public void saveFunctionalUser(ProgramFunctionalUser pfu);

    public void deleteFunctionalUser(Long id);

    public Long getFunctionalUserByUserType(Long programId, Long userTypeId);
}
