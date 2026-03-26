//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.HrmLogEntry;

public interface HrmLogEntryDao extends AbstractDao<HrmLogEntry> {
    List<HrmLogEntry> findByHrmLogId(int hrmLogId);
}
