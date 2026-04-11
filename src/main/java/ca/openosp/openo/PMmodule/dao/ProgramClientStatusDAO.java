//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.PMmodule.model.ProgramClientStatus;
import ca.openosp.openo.commn.model.Admission;

public interface ProgramClientStatusDAO {
    public List<ProgramClientStatus> getProgramClientStatuses(Integer programId);

    public void saveProgramClientStatus(ProgramClientStatus status);

    public ProgramClientStatus getProgramClientStatus(String id);

    public void deleteProgramClientStatus(String id);

    public boolean clientStatusNameExists(Integer programId, String statusName);

    public List<Admission> getAllClientsInStatus(Integer programId, Integer statusId);
}
