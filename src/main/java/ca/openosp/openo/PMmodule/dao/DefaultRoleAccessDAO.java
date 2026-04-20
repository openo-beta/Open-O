//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.DefaultRoleAccess;

public interface DefaultRoleAccessDAO {

    public void deleteDefaultRoleAccess(Long id);

    public DefaultRoleAccess getDefaultRoleAccess(Long id);

    public List<DefaultRoleAccess> getDefaultRoleAccesses();

    public List<DefaultRoleAccess> findAll();

    public void saveDefaultRoleAccess(DefaultRoleAccess dra);

    public DefaultRoleAccess find(Long roleId, Long accessTypeId);

    public List<Object[]> findAllRolesAndAccessTypes();
}
 