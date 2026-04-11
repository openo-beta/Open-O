//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.LookupList;

public interface LookupListDao extends AbstractDao<LookupList> {

    public List<LookupList> findAllActive();

    public LookupList findByName(String name);
}
