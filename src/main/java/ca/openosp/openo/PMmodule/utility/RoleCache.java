//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.PMmodule.utility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import ca.openosp.openo.PMmodule.dao.DefaultRoleAccessDAO;
import ca.openosp.openo.PMmodule.model.DefaultRoleAccess;
import ca.openosp.openo.commn.dao.CaisiAccessTypeDao;
import ca.openosp.openo.commn.model.CaisiAccessType;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.daos.security.SecroleDao;
import ca.openosp.openo.model.security.Secrole;

/**
 * Cache manager for security roles and access type mappings in the PMmodule.
 * <p>
 * This class provides a static caching mechanism for roles, access types, and
 * default role-access mappings. It reduces database queries for authorization
 * checks by maintaining in-memory copies of frequently accessed security data.
 * The cache supports the CAISI (Client Access to Integrated Services and Information)
 * access control model.
 * </p>
 * <p>
 * The cache maintains three data structures:
 * <ul>
 *   <li>roleMap - Maps role IDs to Secrole objects</li>
 *   <li>accessTypeMap - Maps access type names to their IDs</li>
 *   <li>defaultRoleAccessMap - Multi-value map of access types to authorized roles</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Thread Safety Warning:</strong> The static maps are not synchronized.
 * Concurrent access during reload() could lead to inconsistent state. Consider
 * using ConcurrentHashMap and atomic operations for thread safety.
 * </p>
 * <p>
 * <strong>Cache Consistency:</strong> The reload() method must be called to
 * refresh the cache after database changes. There's no automatic invalidation,
 * which could lead to stale authorization data.
 * </p>
 * <p>
 * <strong>Memory Consideration:</strong> The cache loads all roles and access
 * types into memory. For systems with many roles, this could consume significant
 * memory.
 * </p>
 *
 * @since 2001-08-01
 * @see ca.openosp.openo.model.security.Secrole
 * @see ca.openosp.openo.commn.model.CaisiAccessType
 */
public class RoleCache {

    /** DAO for retrieving security roles from database */
    static SecroleDao secroleDao = (SecroleDao) SpringUtils.getBean(SecroleDao.class);
    /** DAO for retrieving default role-access mappings */
    static DefaultRoleAccessDAO defaultRoleAccessDAO = (DefaultRoleAccessDAO) SpringUtils.getBean(DefaultRoleAccessDAO.class);
    /** DAO for retrieving CAISI access types */
    static CaisiAccessTypeDao accessTypeDao = (CaisiAccessTypeDao) SpringUtils.getBean(CaisiAccessTypeDao.class);

    /**
     * Cache of role objects keyed by role ID.
     * WARNING: Not thread-safe - should use ConcurrentHashMap
     */
    static Map<Long, Secrole> roleMap = new HashMap<Long, Secrole>();

    /**
     * Maps access type names to their database IDs for quick lookup.
     * WARNING: Not thread-safe - should use ConcurrentHashMap
     */
    static Map<String, Integer> accessTypeMap = new HashMap<String, Integer>();

    /**
     * Multi-value map storing which roles have access to each access type.
     * Key: access type ID, Value: collection of role IDs with that access
     */
    static MultiValueMap defaultRoleAccessMap = new MultiValueMap();

    /**
     * Reloads all cached data from the database.
     * <p>
     * This method refreshes all three caches by querying the database for
     * current roles, access types, and role-access mappings. It should be
     * called after any changes to security configuration in the database.
     * </p>
     * <p>
     * WARNING: This method is not thread-safe. Concurrent access during reload
     * could result in partial updates being visible to other threads, leading
     * to authorization errors. Consider implementing double-buffering or
     * synchronization.
     * </p>
     * <p>
     * The reload process:
     * <ol>
     *   <li>Clears and rebuilds access type name-to-ID mappings</li>
     *   <li>Clears and rebuilds role ID-to-object mappings</li>
     *   <li>Clears and rebuilds default role-access associations</li>
     * </ol>
     * </p>
     */
    public static void reload() {
        // Rebuild access type mappings
        accessTypeMap.clear();
        for (CaisiAccessType at : accessTypeDao.findAll()) {
            accessTypeMap.put(at.getName(), at.getId());
        }

        // Rebuild role cache
        roleMap.clear();
        for (Secrole role : secroleDao.getRoles()) {
            setRole(role.getId(), role);
        }

        // Rebuild role-access mappings
        // MultiValueMap allows multiple roles per access type
        defaultRoleAccessMap.clear();
        for (DefaultRoleAccess dra : defaultRoleAccessDAO.findAll()) {
            long roleId = dra.getRoleId();
            long accessTypeId = dra.getAccessTypeId();
            // Map access type to authorized role
            defaultRoleAccessMap.put(accessTypeId, roleId);
        }
    }

    /**
     * Checks if a role has access to a specific access type.
     * <p>
     * This method performs authorization checks by looking up whether
     * the specified role is authorized for the given access type.
     * Uses cached data for performance, avoiding database queries.
     * </p>
     * <p>
     * If the access type name is not found in the cache, a warning is
     * logged and access is denied (fail-closed security model).
     * </p>
     *
     * @param accessTypeName String the name of the access type to check
     * @param roleId long the role ID to check authorization for
     * @return boolean true if the role has access, false otherwise
     */
    public static boolean hasAccess(String accessTypeName, long roleId) {
        // Look up access type ID from name
        Integer accessTypeId = accessTypeMap.get(accessTypeName);
        if (accessTypeId == null) {
            // Unknown access type - log and deny access (fail-closed)
            MiscUtils.getLogger().warn("Access Type not found:" + accessTypeName);
            return false;
        }

        // Get all roles authorized for this access type
        Collection<Long> roles = defaultRoleAccessMap.getCollection(accessTypeId.longValue());
        // Check if the specified role is in the authorized set
        if (roles != null && roles.contains(roleId))
            return true;
        return false;
    }

    /**
     * Retrieves a cached role by its ID.
     *
     * @param id Long the role ID to look up
     * @return Secrole the role object, or null if not found
     */
    public static Secrole getRole(Long id) {
        return roleMap.get(id);
    }

    /**
     * Adds or updates a role in the cache.
     * <p>
     * Typically called during reload() but can be used to update
     * individual roles without a full cache refresh.
     * </p>
     *
     * @param roleId Long the role's unique identifier
     * @param role Secrole the role object to cache
     */
    public static void setRole(Long roleId, Secrole role) {
        roleMap.put(roleId, role);
    }
}
