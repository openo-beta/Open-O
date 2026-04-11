//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.PMmodule.model.ProgramTeam;

public interface ProgramTeamDAO {

    public boolean teamExists(Integer teamId);

    public boolean teamNameExists(Integer programId, String teamName);

    public ProgramTeam getProgramTeam(Integer id);

    public List<ProgramTeam> getProgramTeams(Integer programId);

    public void saveProgramTeam(ProgramTeam team);

    public void deleteProgramTeam(Integer id);
}
