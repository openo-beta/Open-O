//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import ca.openosp.openo.commn.model.RBTGroup;

public interface RBTGroupDao extends AbstractDao<RBTGroup> {

    public int deleteByNameAndTemplateId(String groupName, Integer templateId);

    public int deleteByName(String groupName);

    public List<RBTGroup> getByGroupName(String groupName);

    public List<String> getGroupNames();

}
