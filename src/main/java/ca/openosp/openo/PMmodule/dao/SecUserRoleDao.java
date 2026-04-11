//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.PMmodule.model.SecUserRole;

public interface SecUserRoleDao {

    public List<SecUserRole> getUserRoles(String providerNo);

    public List<SecUserRole> getSecUserRolesByRoleName(String roleName);

    public List<SecUserRole> findByRoleNameAndProviderNo(String roleName, String providerNo);

    public boolean hasAdminRole(String providerNo);

    public SecUserRole find(Long id);

    public void save(SecUserRole sur);

    public List<String> getRecordsAddedAndUpdatedSinceTime(Date date);

}
