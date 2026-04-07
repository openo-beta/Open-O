//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.OscarJob;
import ca.openosp.openo.commn.model.OscarJobType;

public interface OscarJobDao extends AbstractDao<OscarJob> {
    List<OscarJob> findByType(OscarJobType oscarJobType);

    List<OscarJob> getJobByName(String name);
}
