//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.util.List;

import ca.openosp.openo.PMmodule.dao.ProgramDao;
import ca.openosp.openo.PMmodule.dao.ProgramProviderDAO;
import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.PMmodule.model.ProgramProvider;
import ca.openosp.openo.commn.dao.ProviderDefaultProgramDao;
import ca.openosp.openo.commn.model.ProviderDefaultProgram;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProgramManager2Impl implements ProgramManager2 {

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private ProgramProviderDAO programProviderDAO;

    @Autowired
    private ProviderDefaultProgramDao providerDefaultProgramDao;

    @Override
    public Program getProgram(LoggedInInfo loggedInInfo, Integer programId) {
        return programDao.getProgram(programId);
    }

    @Override
    public List<Program> getAllPrograms(LoggedInInfo loggedInInfo) {
        return programDao.findAll();
    }

    @Override
    public List<ProgramProvider> getAllProgramProviders(LoggedInInfo loggedInInfo) {
        return programProviderDAO.getAllProgramProviders();
    }

    @Override
    public List<ProgramProvider> getProgramDomain(LoggedInInfo loggedInInfo, String providerNo) {
        return programProviderDAO.getProgramProvidersByProvider(providerNo);
    }

    @Override
    public ProgramProvider getCurrentProgramInDomain(LoggedInInfo loggedInInfo) {
        return getCurrentProgramInDomain(loggedInInfo, loggedInInfo.getLoggedInProviderNo());
    }

    @Override
    public ProgramProvider getCurrentProgramInDomain(LoggedInInfo loggedInInfo, String providerNo) {
        ProgramProvider result = null;
        int defProgramId = 0;
        List<ProviderDefaultProgram> rs = providerDefaultProgramDao.getProgramByProviderNo(providerNo);
        if (!rs.isEmpty()) {
            defProgramId = rs.get(0).getProgramId();
            if (defProgramId > 0) {
                result = programProviderDAO.getProgramProvider(providerNo, Long.valueOf(defProgramId));
            }
        }
        return (result);
    }

    @Override
    public void setCurrentProgramInDomain(String providerNo, Integer programId) {

        if (programProviderDAO.getProgramProvider(providerNo, programId.longValue()) != null) {
            providerDefaultProgramDao.setDefaultProgram(providerNo, programId);
        } else {
            throw new RuntimeException("Program not in user's domain");
        }
    }
}
