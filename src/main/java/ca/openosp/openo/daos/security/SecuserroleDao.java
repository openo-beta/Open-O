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

import ca.openosp.openo.PMmodule.web.formbean.StaffForm;

import ca.openosp.openo.model.security.Secuserrole;

/**
 * Data Access Object interface for managing user-role assignments in the OpenO EMR system.
 * <p>
 * This interface defines operations for assigning roles to users (providers) within
 * specific organizational contexts. Each Secuserrole record represents a many-to-many
 * relationship between providers and roles, with additional organizational scoping.
 * </p>
 * <p>
 * The DAO supports complex queries for multi-organization deployments where users
 * may have different roles in different facilities or departments. All operations
 * maintain audit trails through lastUpdateDate tracking.
 * </p>
 * <p>
 * Transaction control can be managed through Spring's declarative transaction
 * management or programmatically. All methods that modify data should be executed
 * within a transaction context to ensure consistency.
 * </p>
 *
 * @since 2005-01-01
 * @see ca.openosp.openo.model.security.Secuserrole
 * @see ca.openosp.openo.daos.security.SecroleDao
 */
public interface SecuserroleDao {
    /** Property constant for provider number field */
    public static final String PROVIDER_NO = "providerNo";
    /** Property constant for role name field */
    public static final String ROLE_NAME = "roleName";
    /** Property constant for organization code field */
    public static final String ORGCD = "orgcd";
    /** Property constant for active status field */
    public static final String ACTIVEYN = "activeyn";

    /**
     * Batch saves multiple user-role assignments with update-then-insert logic.
     *
     * @param list List list of Secuserrole objects to save
     */
    public void saveAll(List list);

    /**
     * Saves or updates a user-role assignment.
     *
     * @param transientInstance Secuserrole the assignment to save
     */
    public void save(Secuserrole transientInstance);

    /**
     * Updates the role name for an existing assignment.
     *
     * @param id Integer the assignment ID
     * @param roleName String the new role name
     */
    public void updateRoleName(Integer id, String roleName);

    /**
     * Deletes a user-role assignment.
     *
     * @param persistentInstance Secuserrole the assignment to delete
     */
    public void delete(Secuserrole persistentInstance);

    /**
     * Deletes all assignments for an organization.
     *
     * @param orgcd String the organization code
     * @return int number of records deleted
     */
    public int deleteByOrgcd(String orgcd);

    /**
     * Deletes all role assignments for a provider.
     *
     * @param providerNo String the provider number
     * @return int number of records deleted
     */
    public int deleteByProviderNo(String providerNo);

    /**
     * Deletes an assignment by ID.
     *
     * @param id Integer the assignment ID
     * @return int number of records deleted (0 or 1)
     */
    public int deleteById(Integer id);

    /**
     * Updates an existing assignment's active status.
     *
     * @param instance Secuserrole the assignment to update
     * @return int number of rows affected
     */
    public int update(Secuserrole instance);

    /**
     * Finds an assignment by ID.
     *
     * @param id java.lang.Integer the assignment ID
     * @return Secuserrole the assignment, or null if not found
     */
    public Secuserrole findById(java.lang.Integer id);

    /**
     * Finds assignments by example.
     *
     * @param instance Secuserrole example instance
     * @return List list of matching assignments
     */
    public List findByExample(Secuserrole instance);

    /**
     * Generic property search.
     *
     * @param propertyName String the property to search
     * @param value Object the value to match
     * @return List list of matching assignments
     */
    public List findByProperty(String propertyName, Object value);

    /**
     * Finds all assignments for a provider.
     *
     * @param providerNo Object the provider number
     * @return List list of assignments
     */
    public List findByProviderNo(Object providerNo);

    /**
     * Finds all assignments for a role.
     *
     * @param roleName Object the role name
     * @return List list of assignments
     */
    public List findByRoleName(Object roleName);

    /**
     * Finds assignments by organization with optional active-only filter.
     *
     * @param orgcd Object the organization code
     * @param activeOnly boolean true to filter for active providers only
     * @return List list of assignments
     */
    public List findByOrgcd(Object orgcd, boolean activeOnly);

    /**
     * Searches for staff assignments by criteria.
     *
     * @param staffForm StaffForm search criteria
     * @return List list of matching assignments
     */
    public List searchByCriteria(StaffForm staffForm);

    /**
     * Finds assignments by active status.
     *
     * @param activeyn Object the active status
     * @return List list of assignments
     */
    public List findByActiveyn(Object activeyn);

    /**
     * Retrieves all user-role assignments.
     *
     * @return List list of all assignments
     */
    public List findAll();

    /**
     * Merges a detached assignment.
     *
     * @param detachedInstance Secuserrole the detached instance
     * @return Secuserrole the merged instance
     */
    public Secuserrole merge(Secuserrole detachedInstance);

    /**
     * Attaches a modified assignment.
     *
     * @param instance Secuserrole the modified instance
     */
    public void attachDirty(Secuserrole instance);

    /**
     * Attaches an unmodified assignment.
     *
     * @param instance Secuserrole the clean instance
     */
    public void attachClean(Secuserrole instance);
}
