//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.ProgramQueue;

public interface ProgramQueueDao {
    public ProgramQueue getProgramQueue(Long queueId);

    public List<ProgramQueue> getProgramQueuesByProgramId(Long programId);

    public List<ProgramQueue> getActiveProgramQueuesByProgramId(Long programId);

    public void saveProgramQueue(ProgramQueue programQueue);

    public ProgramQueue getQueue(Long programId, Long clientId);

    public ProgramQueue getActiveProgramQueue(Long programId, Long demographicNo);
}
