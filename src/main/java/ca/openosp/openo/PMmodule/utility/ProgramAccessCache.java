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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ca.openosp.openo.PMmodule.dao.ProgramAccessDAO;
import ca.openosp.openo.PMmodule.model.ProgramAccess;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Cache manager for program access permissions in the PMmodule.
 * <p>
 * This class provides a static caching mechanism for program access permissions,
 * reducing database queries for frequently accessed authorization data. It stores
 * access maps keyed by program ID, where each map contains access types and their
 * corresponding ProgramAccess objects.
 * </p>
 * <p>
 * The cache is implemented as a simple in-memory HashMap without expiration or
 * size limits. In production environments with many programs, this could lead to
 * memory issues. Consider implementing an LRU cache or using a proper caching
 * framework like Ehcache.
 * </p>
 * <p>
 * <strong>Thread Safety Warning:</strong> The static HashMap is not synchronized.
 * Concurrent modifications could lead to race conditions or corrupted data.
 * Consider using ConcurrentHashMap or synchronizing access methods.
 * </p>
 * <p>
 * <strong>Cache Invalidation:</strong> There is no automatic cache invalidation.
 * Changes to program access in the database won't be reflected until setAccessMap()
 * is called again. This could lead to stale authorization data and security issues.
 * </p>
 *
 * @since 2001-08-01
 * @see ca.openosp.openo.PMmodule.model.ProgramAccess
 * @see ca.openosp.openo.PMmodule.dao.ProgramAccessDAO
 */
public class ProgramAccessCache {

    /** DAO for retrieving program access data from the database */
    static ProgramAccessDAO programAccessDAO = (ProgramAccessDAO) SpringUtils.getBean(ProgramAccessDAO.class);

    /**
     * Cache storage for program access maps.
     * Key format: "{programId}:AccessMap"
     * Value: Map of access type names to ProgramAccess objects
     * WARNING: Not thread-safe - should use ConcurrentHashMap
     */
    static Map<String, Map> accessMaps = new HashMap<String, Map>();


    /**
     * Retrieves the cached access map for a specific program.
     * <p>
     * Returns null if the program's access map hasn't been cached yet.
     * Callers should check for null and call setAccessMap() if needed.
     * </p>
     *
     * @param programId long the program's unique identifier
     * @return Map access map for the program, or null if not cached
     */
    public static Map getAccessMap(long programId) {
        // Construct cache key using program ID
        return accessMaps.get(programId + ":AccessMap");
    }

    /**
     * Loads and caches the access map for a specific program.
     * <p>
     * Fetches the program's access list from the database and converts it
     * to a map for efficient lookup by access type name. This method will
     * overwrite any existing cached data for the program.
     * </p>
     * <p>
     * Should be called when:
     * <ul>
     *   <li>Program access is modified in the database</li>
     *   <li>Initial cache population is needed</li>
     *   <li>Cache refresh is required due to suspected stale data</li>
     * </ul>
     * </p>
     *
     * @param programId long the program's unique identifier
     */
    public static void setAccessMap(long programId) {
        // Fetch current access list from database
        List programAccessList = programAccessDAO.getAccessListByProgramId(programId);
        // Convert list to map for O(1) lookup by access type
        @SuppressWarnings("unchecked")
        Map programAccessMap = convertProgramAccessListToMap(programAccessList);
        // Store in cache, overwriting any existing entry
        accessMaps.put(programId + ":AccessMap", programAccessMap);
    }

    /**
     * Converts a list of ProgramAccess objects to a map keyed by access type name.
     * <p>
     * This transformation enables O(1) lookup of access permissions by type name
     * rather than O(n) list iteration. Access type names are converted to lowercase
     * for case-insensitive lookups.
     * </p>
     * <p>
     * Note: If multiple ProgramAccess objects have the same access type name
     * (after lowercase conversion), later entries will overwrite earlier ones.
     * This could be a bug if duplicate access types are possible.
     * </p>
     *
     * @param paList List<ProgramAccess> list of program access objects
     * @return Map<String, ProgramAccess> map keyed by lowercase access type names
     */
    private static Map<String, ProgramAccess> convertProgramAccessListToMap(List<ProgramAccess> paList) {
        Map<String, ProgramAccess> map = new HashMap<String, ProgramAccess>();
        if (paList == null) {
            // Return empty map rather than null for safer client code
            return map;
        }
        // Convert list to map for efficient lookup
        for (Iterator<ProgramAccess> iter = paList.iterator(); iter.hasNext(); ) {
            ProgramAccess pa = iter.next();
            // Use lowercase key for case-insensitive lookup
            // WARNING: Duplicates will be overwritten
            map.put(pa.getAccessType().getName().toLowerCase(), pa);
        }
        return map;
    }

}
