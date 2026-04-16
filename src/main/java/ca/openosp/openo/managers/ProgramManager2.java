//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.util.List;

import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.PMmodule.model.ProgramProvider;
import ca.openosp.openo.utility.LoggedInInfo;

public interface ProgramManager2 {

    public Program getProgram(LoggedInInfo loggedInInfo, Integer programId);

    public List<Program> getAllPrograms(LoggedInInfo loggedInInfo);

    public List<ProgramProvider> getAllProgramProviders(LoggedInInfo loggedInInfo);

    public List<ProgramProvider> getProgramDomain(LoggedInInfo loggedInInfo, String providerNo);

    public ProgramProvider getCurrentProgramInDomain(LoggedInInfo loggedInInfo);

    public ProgramProvider getCurrentProgramInDomain(LoggedInInfo loggedInInfo, String providerNo);

    public void setCurrentProgramInDomain(String providerNo, Integer programId);
}
