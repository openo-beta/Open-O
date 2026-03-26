//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.DefaultRoleAccess;

public interface RoleProgramAccessDAO {

    public List<DefaultRoleAccess> getDefaultAccessRightByRole(Long roleId);

    public List<DefaultRoleAccess> getDefaultSpecificAccessRightByRole(Long roleId, String accessType);

    public boolean hasAccess(String accessName, Long roleId);
}
