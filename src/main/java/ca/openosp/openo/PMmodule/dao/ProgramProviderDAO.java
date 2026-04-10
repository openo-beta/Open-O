//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import ca.openosp.openo.PMmodule.model.ProgramProvider;
import ca.openosp.openo.commn.model.Facility;

import java.util.List;

public interface ProgramProviderDAO {

    public List<ProgramProvider> getProgramProviderByProviderProgramId(String providerNo, Long programId);

    public List<ProgramProvider> getAllProgramProviders();

    public List<ProgramProvider> getProgramProviderByProviderNo(String providerNo);

    public List<ProgramProvider> getProgramProviders(Long programId);

    public List<ProgramProvider> getProgramProvidersByProvider(String providerNo);

    public List getProgramProvidersByProviderAndFacility(String providerNo, Integer facilityId);

    public ProgramProvider getProgramProvider(Long id);

    public ProgramProvider getProgramProvider(String providerNo, Long programId);

    public ProgramProvider getProgramProvider(String providerNo, Long programId, Long roleId);

    public void saveProgramProvider(ProgramProvider pp);

    public void deleteProgramProvider(Long id);

    public void deleteProgramProviderByProgramId(Long programId);

    public List<ProgramProvider> getProgramProvidersInTeam(Integer programId, Integer teamId);

    public List<ProgramProvider> getProgramDomain(String providerNo);

    public List<ProgramProvider> getActiveProgramDomain(String providerNo);

    public List<ProgramProvider> getProgramDomainByFacility(String providerNo, Integer facilityId);

    public boolean isThisProgramInProgramDomain(String providerNo, Integer programId);

    public List<Facility> getFacilitiesInProgramDomain(String providerNo);

    public void updateProviderRole(ProgramProvider pp, Long roleId);
}
