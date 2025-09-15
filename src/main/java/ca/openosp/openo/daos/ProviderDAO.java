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

package ca.openosp.openo.daos;

import java.util.List;

import ca.openosp.openo.commn.model.Provider;

/**
 * Data Access Object interface for provider management.
 *
 * This interface defines operations for accessing and managing healthcare
 * provider information in the EMR system. Providers include physicians,
 * nurses, and other healthcare professionals who deliver care and have
 * access to the system.
 *
 * The DAO provides methods for:
 * - Retrieving all providers in the system
 * - Looking up providers by their unique identifier
 * - Searching providers by name
 *
 * Note: Historical implementation note indicates this interface may have
 * configuration issues with Spring bean management. The interface is
 * maintained for compatibility with existing code that depends on it.
 *
 * @since 2012-01-01
 * @deprecated Consider using ca.openosp.openo.PMmodule.dao.ProviderDao instead
 */
public interface ProviderDAO {

    /**
     * Retrieves all providers in the system.
     *
     * Returns a complete list of all healthcare providers
     * registered in the EMR system.
     *
     * @return List<Provider> list of all providers, empty list if none exist
     */
    public List<Provider> getProviders();

    /**
     * Retrieves a provider by their unique identifier.
     *
     * @param provider_no String the unique provider number
     * @return Provider the provider entity, or null if not found
     */
    public Provider getProvider(String provider_no);

    /**
     * Searches for a provider by name.
     *
     * Finds a provider matching the specified last name and first name.
     * If multiple providers match, returns the first match.
     *
     * @param lastName String the provider's last name
     * @param firstName String the provider's first name
     * @return Provider the matching provider, or null if not found
     */
    public Provider getProviderByName(String lastName, String firstName);

}
 