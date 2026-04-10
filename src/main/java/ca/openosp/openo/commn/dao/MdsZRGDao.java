//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MdsZRG;

public interface MdsZRGDao extends AbstractDao<MdsZRG> {
    List<Object[]> findById(Integer id);

    List<Object> findReportGroupHeadingsById(Integer id, String reportGroupId);
}
