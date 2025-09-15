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
package ca.openosp.openo.daos.security;

import java.util.List;

import ca.openosp.openo.model.security.Secrole;

/**
 * Data Access Object interface for managing security roles in the OpenO EMR system.
 * <p>
 * This interface defines operations for managing roles which are collections of
 * privileges that can be assigned to users. Roles simplify permission management
 * by grouping related privileges together (e.g., doctor role, nurse role, admin role).
 * </p>
 * <p>
 * The system supports both default (system-defined) roles and user-defined custom roles.
 * Default roles cannot be modified by users and provide baseline security configurations.
 * </p>
 *
 * @since 2005-01-01
 * @see ca.openosp.openo.model.security.Secrole
 * @see ca.openosp.openo.daos.security.SecuserroleDao
 */
public interface SecroleDao {

    /**
     * Retrieves all roles from the database, ordered by role name.
     *
     * @return List<Secrole> list of all security roles
     */
    public List<Secrole> getRoles();

    /**
     * Retrieves a role by its unique identifier.
     *
     * @param id Integer the role's database ID
     * @return Secrole the role entity, or null if not found
     * @throws IllegalArgumentException if id is null or <= 0
     */
    public Secrole getRole(Integer id);

    /**
     * Retrieves a role by its unique name.
     *
     * @param roleName String the role name to search for
     * @return Secrole the matching role, or null if not found
     * @throws IllegalArgumentException if roleName is null or empty
     */
    public Secrole getRoleByName(String roleName);

    /**
     * Retrieves all system-defined default roles.
     * <p>
     * Default roles have userDefined=0 and cannot be modified by users.
     * </p>
     *
     * @return List list of default Secrole entities
     */
    public List getDefaultRoles();

    /**
     * Persists or updates a role definition.
     *
     * @param secrole Secrole the role to save
     * @throws IllegalArgumentException if secrole is null
     */
    public void save(Secrole secrole);

}
