//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.OscarJobType;

public interface OscarJobTypeDao extends AbstractDao<OscarJobType> {
    List<OscarJobType> findByClassName(String className);
}
