//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * <p>
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package org.oscarehr.PMmodule.dao;

import org.oscarehr.PMmodule.model.ProgramProvider;
import org.oscarehr.common.model.Facility;

import java.util.List;

public interface ProgramProviderDAO {

    List<ProgramProvider> getProgramProviderByProviderProgramId(String providerNo, Long programId);

    List<ProgramProvider> getAllProgramProviders();

    List<ProgramProvider> getProgramProviderByProviderNo(String providerNo);

    List<ProgramProvider> getProgramProviders(Long programId);

    List<ProgramProvider> getProgramProvidersByProvider(String providerNo);

    List getProgramProvidersByProviderAndFacility(String providerNo, Integer facilityId);

    ProgramProvider getProgramProvider(Long id);

    ProgramProvider getProgramProvider(String providerNo, Long programId);

    ProgramProvider getProgramProvider(String providerNo, Long programId, Long roleId);

    void saveProgramProvider(ProgramProvider pp);

    void deleteProgramProvider(Long id);

    void deleteProgramProviderByProgramId(Long programId);

    List<ProgramProvider> getProgramProvidersInTeam(Integer programId, Integer teamId);

    List<ProgramProvider> getProgramDomain(String providerNo);

    List<ProgramProvider> getActiveProgramDomain(String providerNo);

    List<ProgramProvider> getProgramDomainByFacility(String providerNo, Integer facilityId);

    boolean isThisProgramInProgramDomain(String providerNo, Integer programId);

    List<Facility> getFacilitiesInProgramDomain(String providerNo);

    void updateProviderRole(ProgramProvider pp, Long roleId);
}
