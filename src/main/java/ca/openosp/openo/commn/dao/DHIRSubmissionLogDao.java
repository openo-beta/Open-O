//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.DHIRSubmissionLog;

public interface DHIRSubmissionLogDao extends AbstractDao<DHIRSubmissionLog> {

    public List<DHIRSubmissionLog> findAll();

    public DHIRSubmissionLog findLatestPendingByPreventionId(Integer preventionId);

    public List<DHIRSubmissionLog> findByPreventionId(Integer preventionId);
}
