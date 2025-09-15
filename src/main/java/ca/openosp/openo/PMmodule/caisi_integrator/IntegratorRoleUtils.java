//CHECKSTYLE:OFF
/**
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
 */

package ca.openosp.openo.PMmodule.caisi_integrator;

import ca.openosp.openo.caisi_integrator.util.Role;

/**
 * Utility class for mapping OpenO EMR roles to CAISI integrator role enumerations.
 *
 * <p>This utility provides role translation services between the local OpenO EMR
 * role system and the standardized role enumerations used in the CAISI integrator
 * network. Proper role mapping is essential for maintaining appropriate access
 * controls and authorization across integrated healthcare facilities.</p>
 *
 * <p>The role mapping supports both web service-based roles and utility-based roles,
 * with automatic normalization of role names (uppercase, space-to-underscore conversion)
 * and extensible support for custom role mappings where direct translation is not possible.</p>
 *
 * <p><strong>Security Note:</strong> Role mapping is critical for maintaining proper
 * access controls in multi-facility healthcare environments where different facilities
 * may use different role naming conventions but need to respect equivalent access levels.</p>
 *
 * @see ca.openosp.openo.caisi_integrator.ws.Role
 * @see ca.openosp.openo.caisi_integrator.util.Role
 * @since 2008
 */
public final class IntegratorRoleUtils {

    /**
     * Maps an OpenO EMR role string to a CAISI integrator web service role enumeration.
     *
     * <p>Attempts to match the given role string to a web service role enumeration
     * by converting to uppercase and replacing spaces with underscores. This supports
     * standard role name variations while maintaining security boundaries.</p>
     *
     * <p>Null inputs and unmatched roles return null, allowing calling code to
     * handle unmappable roles appropriately.</p>
     *
     * @param oscarRole String the local OpenO EMR role name to map
     * @return ca.openosp.openo.caisi_integrator.ws.Role the mapped integrator role, or null if no match
     * @since 2008
     */
    public static ca.openosp.openo.caisi_integrator.ws.Role getIntegratorRole(String oscarRole) {
        if (oscarRole == null) return (null);

        try {
            return (ca.openosp.openo.caisi_integrator.ws.Role.valueOf(oscarRole.toUpperCase().replaceAll(" ", "_")));
        } catch (Exception e) {
            // Direct matches preferred; null and non-matches are expected and handled gracefully
        }

        // Extension point for custom role mappings where direct translation isn't possible
        // Example: if ("Front Desk Worker".equals(oscarRole)) return(Role.FDW);

        return (null);
    }

    /**
     * Maps an OpenO EMR role string to a CAISI integrator utility role enumeration.
     *
     * <p>Similar to getIntegratorRole but targets the utility role enumeration system
     * rather than the web service role system. This provides flexibility for different
     * role contexts within the CAISI integration framework.</p>
     *
     * @param oscarRole String the local OpenO EMR role name to map
     * @return Role the mapped integrator utility role, or null if no match
     * @since 2008
     */
    public static Role getIntegratorRole2(String oscarRole) {
        if (oscarRole == null) return (null);

        try {
            return (Role.valueOf(oscarRole.toUpperCase().replaceAll(" ", "_")));
        } catch (Exception e) {
            // Direct matches preferred; null and non-matches are expected and handled gracefully
        }

        // Extension point for custom role mappings where direct translation isn't possible
        // Example: if ("Front Desk Worker".equals(oscarRole)) return(Role.FDW);

        return (null);
    }
}
