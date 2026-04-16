//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collection;
import java.util.List;

import ca.openosp.openo.commn.model.SecObjPrivilege;

public interface SecObjPrivilegeDao extends AbstractDao<SecObjPrivilege> {
    List<SecObjPrivilege> findByRoleUserGroupAndObjectName(String roleUserGroup, String objectName);

    List<SecObjPrivilege> findByObjectNames(Collection<String> objectNames);

    List<SecObjPrivilege> findByRoleUserGroup(String roleUserGroup);

    List<SecObjPrivilege> findByObjectName(String objectName);

    int countObjectsByName(String objName);

    List<Object[]> findByFormNamePrivilegeAndProviderNo(String formName, String privilege, String providerNo);
}
