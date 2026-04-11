//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.service;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.PMmodule.dao.AgencyDao;
import ca.openosp.openo.PMmodule.dao.ProgramProviderDAO;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.PMmodule.dao.SecUserRoleDao;
import ca.openosp.openo.PMmodule.model.Agency;
import ca.openosp.openo.PMmodule.model.ProgramProvider;
import ca.openosp.openo.PMmodule.model.SecUserRole;
import ca.openosp.openo.commn.model.Facility;
import ca.openosp.openo.commn.model.Provider;

public interface ProviderManager {
    void setProviderDao(ProviderDao providerDao);

    void setAgencyDao(AgencyDao agencyDao);

    void setProgramProviderDAO(ProgramProviderDAO dao);

    void setSecUserRoleDao(SecUserRoleDao secUserRoleDao);

    Provider getProvider(String providerNo);

    String getProviderName(String providerNo);

    List<Provider> getProviders();

    List<Provider> getActiveProviders();

    List<Provider> getActiveProviders(String facilityId, String programId);

    List<Provider> getActiveProviders(String providerNo, Integer shelterId);

    List<Provider> search(String name);

    List<ProgramProvider> getProgramDomain(String providerNo);

    List<ProgramProvider> getProgramDomainByFacility(String providerNo, Integer facilityId);

    List<Facility> getFacilitiesInProgramDomain(String providerNo);


    List<Agency> getAgencyDomain(String providerNo);

    List<Provider> getProvidersByType(String type);

    List<SecUserRole> getSecUserRoles(String providerNo);

    void saveUserRole(SecUserRole sur);
}
