//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.SecPrivilege;

public interface SecPrivilegeDao extends AbstractDao<SecPrivilege> {
    List<SecPrivilege> findAll();
}
