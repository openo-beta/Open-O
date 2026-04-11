//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MdsZMN;

public interface MdsZMNDao extends AbstractDao<MdsZMN> {
    MdsZMN findBySegmentIdAndReportName(Integer id, String reportName);

    MdsZMN findBySegmentIdAndResultMnemonic(Integer id, String rm);

    List<String> findResultCodes(Integer id, String reportSequence);
}
