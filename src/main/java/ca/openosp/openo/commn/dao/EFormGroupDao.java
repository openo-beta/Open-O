//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.EFormGroup;

public interface EFormGroupDao extends AbstractDao<EFormGroup> {

    public int deleteByNameAndFormId(String groupName, Integer formId);

    public int deleteByName(String groupName);

    public List<EFormGroup> getByGroupName(String groupName);

    public List<String> getGroupNames();
}
