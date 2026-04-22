//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.SecObjectName;

public interface SecObjectNameDao extends AbstractDao<SecObjectName> {
    List<SecObjectName> findAll();

    List<String> findDistinctObjectNames();
}
