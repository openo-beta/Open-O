//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.service;

import ca.openosp.openo.PMmodule.dao.*;
import ca.openosp.openo.PMmodule.model.*;
import ca.openosp.openo.PMmodule.dao.*;
import ca.openosp.openo.commn.dao.AdmissionDao;
import ca.openosp.openo.commn.model.Admission;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.util.LabelValueBean;

import java.util.List;

public interface ProgramManager {

    boolean getEnabled();

    void setEnabled(boolean enabled);

    ProgramSignatureDao getProgramSignatureDao();

    void setProgramSignatureDao(ProgramSignatureDao programSignatureDao);

    void setProgramDao(ProgramDao dao);

    void setProgramProviderDAO(ProgramProviderDAO dao);

    void setProgramFunctionalUserDAO(ProgramFunctionalUserDAO dao);

    void setProgramTeamDAO(ProgramTeamDAO dao);

    void setProgramAccessDAO(ProgramAccessDAO dao);

    void setAdmissionDao(AdmissionDao dao);

    void setDefaultRoleAccessDAO(DefaultRoleAccessDAO dao);

    void setProgramClientStatusDAO(ProgramClientStatusDAO dao);

    Program getProgram(String programId);

    Program getProgram(Integer programId);

    Program getProgram(Long programId);

    List<Program> getActiveProgramByFacility(String providerNo, Integer facilityId);

    String getProgramName(String programId);

    Integer getProgramIdByProgramName(String programName);

    List<Program> getAllPrograms();

    List<Program> getAllPrograms(String programStatus, String type, int facilityId);

    List<Program> getCommunityPrograms(Integer facilityId);

    List<Program> getPrograms(Integer facilityId);

    List<Program> getPrograms();


    List<Program> getServicePrograms();

    Program[] getExternalPrograms();


    boolean isServiceProgram(String programId);

    boolean isCommunityProgram(String programId);

    void saveProgram(Program program);

    void removeProgram(String programId);

    List<ProgramProvider> getProgramProviders(String programId);

    List<ProgramProvider> getProgramProvidersByProvider(String providerNo);

    ProgramProvider getProgramProvider(String id);

    ProgramProvider getProgramProvider(String providerNo, String programId);

    void saveProgramProvider(ProgramProvider pp);

    void deleteProgramProvider(String id);

    void deleteProgramProviderByProgramId(Long programId);

    List<FunctionalUserType> getFunctionalUserTypes();

    FunctionalUserType getFunctionalUserType(String id);

    void saveFunctionalUserType(FunctionalUserType fut);

    void deleteFunctionalUserType(String id);

    List<FunctionalUserType> getFunctionalUsers(String programId);

    ProgramFunctionalUser getFunctionalUser(String id);

    void saveFunctionalUser(ProgramFunctionalUser pfu);

    void deleteFunctionalUser(String id);

    Long getFunctionalUserByUserType(Long programId, Long userTypeId);

    List<ProgramTeam> getProgramTeams(String programId);

    ProgramTeam getProgramTeam(String id);

    void saveProgramTeam(ProgramTeam team);

    void deleteProgramTeam(String id);

    boolean teamNameExists(Integer programId, String teamName);

    List<ProgramAccess> getProgramAccesses(String programId);

    ProgramAccess getProgramAccess(String id);

    void saveProgramAccess(ProgramAccess pa);

    void deleteProgramAccess(String id);

    List<AccessType> getAccessTypes();

    AccessType getAccessType(Long id);

    List<ProgramProvider> getAllProvidersInTeam(Integer programId, Integer teamId);

    List<Admission> getAllClientsInTeam(Integer programId, Integer teamId);

    List<Program> search(Program criteria);

    List<Program> searchByFacility(Program criteria, Integer facilityId);

    Program getHoldingTankProgram();

    ProgramAccess getProgramAccess(String programId, String accessTypeId);

    List<Program> getProgramDomain(String providerNo);

    List<Program> getActiveProgramDomain(String providerNo);

    List<Program> getProgramDomainInCurrentFacilityForCurrentProvider(LoggedInInfo loggedInInfo, boolean activeOnly);

    Program[] getCommunityPrograms();

    List<LabelValueBean> getProgramBeans(String providerNo);

    List<DefaultRoleAccess> getDefaultRoleAccesses();

    DefaultRoleAccess getDefaultRoleAccess(String id);

    void saveDefaultRoleAccess(DefaultRoleAccess dra);

    void deleteDefaultRoleAccess(String id);

    DefaultRoleAccess findDefaultRoleAccess(long roleId, long accessTypeId);

    List<ProgramClientStatus> getProgramClientStatuses(Integer programId);

    void saveProgramClientStatus(ProgramClientStatus status);

    void deleteProgramClientStatus(String id);

    boolean clientStatusNameExists(Integer programId, String statusName);

    List<Admission> getAllClientsInStatus(Integer programId, Integer statusId);

    ProgramClientStatus getProgramClientStatus(String statusId);

    ProgramSignature getProgramFirstSignature(Integer programId);

    List<ProgramSignature> getProgramSignatures(Integer programId);

    void saveProgramSignature(ProgramSignature programSignature);

    VacancyTemplate getVacancyTemplate(Integer templateId);

    void setVacancyTemplateDao(VacancyTemplateDao vacancyTemplateDao);

    boolean hasAccessBasedOnCurrentFacility(LoggedInInfo loggedInInfo, Integer programId);

    List<Program> getAllProgramsByRole(String providerNo, int roleId);
}
