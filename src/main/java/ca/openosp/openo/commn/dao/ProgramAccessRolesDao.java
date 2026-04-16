//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ProgramAccessRoles;

public interface ProgramAccessRolesDao extends AbstractDao<ProgramAccessRoles> {
    int removeAll();
}
