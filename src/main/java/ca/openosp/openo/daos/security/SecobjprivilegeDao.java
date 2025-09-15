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

import ca.openosp.openo.model.security.Secobjprivilege;

/**
 * Data Access Object interface for managing security object privileges in the OpenO EMR system.
 * <p>
 * This interface defines the contract for managing the relationship between security objects,
 * roles, and privileges. It represents the core of the role-based access control (RBAC) system,
 * where each record defines what privilege (read, write, delete, etc.) a specific role has
 * on a particular security object (function, module, or resource).
 * </p>
 * <p>
 * The Secobjprivilege entity creates a many-to-many relationship between roles and security
 * objects with an additional privilege level attribute. This allows fine-grained control
 * over what actions users with specific roles can perform on protected resources.
 * </p>
 * <p>
 * Implementation classes should ensure transactional integrity when modifying privilege
 * assignments, as incorrect configurations could lead to security vulnerabilities or
 * access denial for legitimate users.
 * </p>
 *
 * @since 2005-01-01
 * @see ca.openosp.openo.model.security.Secobjprivilege
 * @see ca.openosp.openo.daos.security.SecroleDao
 * @see ca.openosp.openo.daos.security.SecObjectNameDao
 */
public interface SecobjprivilegeDao {

    /**
     * Persists or updates a single security object privilege assignment.
     *
     * @param secobjprivilege Secobjprivilege the privilege assignment to save
     * @throws IllegalArgumentException if the parameter is null
     */
    public void save(Secobjprivilege secobjprivilege);

    /**
     * Batch saves or updates multiple privilege assignments.
     * <p>
     * This method attempts to update existing records first, and only
     * inserts new records if the update affects no rows. This ensures
     * no duplicate privilege assignments are created.
     * </p>
     *
     * @param list List list of Secobjprivilege objects to save
     * @throws RuntimeException if the batch operation fails
     */
    public void saveAll(List list);

    /**
     * Updates an existing privilege assignment.
     * <p>
     * Updates the provider number for a specific combination of
     * object name, privilege code, and role/user group.
     * </p>
     *
     * @param instance Secobjprivilege the privilege assignment to update
     * @return int number of rows affected (0 if no matching record found)
     */
    public int update(Secobjprivilege instance);

    /**
     * Deletes all privilege assignments for a specific role.
     * <p>
     * This is typically used when removing a role from the system
     * or resetting a role's permissions before reassignment.
     * </p>
     *
     * @param roleName String the name of the role whose privileges should be deleted
     * @return int number of records deleted
     */
    public int deleteByRoleName(String roleName);

    /**
     * Deletes a specific privilege assignment.
     *
     * @param persistentInstance Secobjprivilege the privilege assignment to delete
     * @throws RuntimeException if deletion fails
     */
    public void delete(Secobjprivilege persistentInstance);

    /**
     * Retrieves the description of a security function/object.
     *
     * @param function_code String the function code to look up
     * @return String the function description, or empty string if not found
     */
    public String getFunctionDesc(String function_code);

    /**
     * Retrieves the description of an access type/privilege level.
     *
     * @param accessType_code String the access type code to look up
     * @return String the access type description, or empty string if not found
     */
    public String getAccessDesc(String accessType_code);

    /**
     * Retrieves all privilege assignments for a specific role.
     * <p>
     * Results are ordered by object name code for consistent display.
     * </p>
     *
     * @param roleName String the role name to query
     * @return List list of Secobjprivilege objects for the role
     * @throws IllegalArgumentException if roleName is null
     */
    public List getFunctions(String roleName);

    /**
     * Generic property-based search for privilege assignments.
     *
     * @param propertyName String the property name to search by
     * @param value Object the value to match
     * @return List list of matching Secobjprivilege objects
     */
    public List findByProperty(String propertyName, Object value);

    /**
     * Retrieves privileges for a specific object filtered by multiple roles.
     * <p>
     * This method is useful for determining what privileges a user with
     * multiple roles has on a specific security object.
     * </p>
     *
     * @param o String the object name code
     * @param roles List<String> list of role names to filter by
     * @return List<Secobjprivilege> list of matching privilege assignments
     */
    public List<Secobjprivilege> getByObjectNameAndRoles(String o, List<String> roles);

    /**
     * Retrieves all privilege assignments for multiple roles.
     * <p>
     * This method efficiently fetches all privileges for a set of roles
     * in a single query, useful for building a complete permission matrix
     * for a user with multiple roles.
     * </p>
     *
     * @param roles List<String> list of role names
     * @return List<Secobjprivilege> list of all privilege assignments for the specified roles
     */
    public List<Secobjprivilege> getByRoles(List<String> roles);
}
