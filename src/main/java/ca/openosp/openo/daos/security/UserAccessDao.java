//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
 * Contributors:
 * <Quatro Group Software Systems inc.>  <OSCAR Team>
 * <p>
 * Modifications made by Magenta Health in 2024.
 */
package ca.openosp.openo.daos.security;

import java.util.List;

/**
 * Data Access Object interface for retrieving user access permissions in the OpenO EMR system.
 * <p>
 * This interface provides methods to query user access rights based on provider number
 * and optional shelter/facility context. It supports multi-facility installations where
 * access rights may vary by organizational unit.
 * </p>
 * <p>
 * The interface works with UserAccessValue entities which represent computed access
 * permissions derived from the user's roles and the organization's security configuration.
 * </p>
 *
 * @since 2005-01-01
 * @see ca.openosp.openo.model.UserAccessValue
 */
public interface UserAccessDao {
    /**
     * Retrieves the complete access list for a provider, optionally filtered by shelter.
     * <p>
     * Returns UserAccessValue entities ordered by function code, privilege level (descending),
     * and organization code. This ordering ensures higher privileges appear first.
     * </p>
     *
     * @param providerNo String the provider's unique identifier
     * @param shelterId Integer optional shelter ID for filtering (null for all shelters)
     * @return List list of UserAccessValue entities representing access permissions
     */
    public List GetUserAccessList(String providerNo, Integer shelterId);

    /**
     * Retrieves distinct organization access codes for a provider.
     * <p>
     * Returns a list of organization code CSV strings that the provider has
     * at least read access to, useful for determining facility-level access.
     * </p>
     *
     * @param providerNo String the provider's unique identifier
     * @param shelterId Integer optional shelter ID for filtering
     * @return List list of organization code CSV strings
     */
    public List GetUserOrgAccessList(String providerNo, Integer shelterId);

}
