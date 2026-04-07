//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Groups;

public interface GroupsDao extends AbstractDao<Groups> {

    public List<Groups> findByParentId(int groupId);
}
