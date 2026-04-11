//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.OtherId;

public interface OtherIdDAO extends AbstractDao<OtherId> {
    OtherId getOtherId(Integer tableName, Integer tableId, String otherKey);

    OtherId getOtherId(Integer tableName, String tableId, String otherKey);

    OtherId searchTable(Integer tableName, String otherKey, String otherValue);

    void save(OtherId otherId);
}
