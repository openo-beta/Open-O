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

package ca.openosp.openo.PMmodule.web.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Utility class for managing user role-based access control in the PMmodule web interface.
 * <p>
 * This class provides static methods to extract and verify user roles from the HTTP session.
 * It implements a simple role-checking mechanism where roles are stored as a comma-separated
 * string in the session. This approach allows for multiple role assignments per user,
 * supporting complex authorization scenarios in healthcare settings where staff may have
 * multiple responsibilities.
 * </p>
 * <p>
 * The class uses a session-based approach for role storage, which means roles are determined
 * at login time and persist throughout the user's session. This provides good performance
 * but requires session invalidation on role changes to take effect.
 * </p>
 * <p>
 * Performance Note: The current implementation uses linear search for role checking,
 * which is noted as inefficient in the code comments. For systems with many roles or
 * frequent authorization checks, consider caching roles in a Set for O(1) lookup.
 * </p>
 * <p>
 * Security Consideration: This utility assumes roles have already been authenticated
 * and authorized during the login process. It does not perform any validation of the
 * role data itself.
 * </p>
 *
 * @since 2005-01-01
 */
public class UserRoleUtils {

    /** Session attribute key where the comma-separated roles string is stored */
    public static final String USER_ROLE_SESSION_KEY = "userrole";
    /** Delimiter used to separate multiple roles in the session string */
    public static final String USER_SEPARATOR = ",";

    /**
     * Enumeration of standard system roles.
     * <p>
     * These represent the primary healthcare roles in the PMmodule system.
     * Each role typically corresponds to different access levels and functionality:
     * <ul>
     *   <li>doctor - Full clinical access, can modify patient records</li>
     *   <li>admin - Administrative functions, user management, system configuration</li>
     *   <li>receptionist - Front desk operations, scheduling, basic patient data</li>
     *   <li>nurse - Clinical support functions, vital signs, basic charting</li>
     *   <li>external - Limited access for external consultants or contractors</li>
     *   <li>er_clerk - Emergency room specific functions, triage support</li>
     * </ul>
     * </p>
     * <p>
     * Note: The system also supports custom roles beyond this enumeration
     * through the string-based hasRole() method.
     * </p>
     */
    public enum Roles {
        doctor, admin, receptionist, nurse, external, er_clerk
    }

    /**
     * Retrieves all roles assigned to the current user from the session.
     * <p>
     * This method will return an array of strings representing
     * the roles as extracted from the session. If there are no
     * roles then an empty array is returned, it should never
     * return null.
     * </p>
     * <p>
     * The roles are stored as a comma-separated string in the session
     * and are split into an array for processing. This format allows
     * for easy storage while supporting multiple role assignments.
     * </p>
     * <p>
     * Example session value: "doctor,admin" would return ["doctor", "admin"]
     * </p>
     *
     * @param request HttpServletRequest the HTTP request containing the session
     * @return String[] array of role names, empty array if no roles found (never null)
     */
    public static String[] getUserRoles(HttpServletRequest request) {

        HttpSession session = request.getSession();

        // Retrieve the comma-separated roles string from session
        String temp = (String) session.getAttribute(USER_ROLE_SESSION_KEY);

        // Return empty array if no roles found to avoid null pointer issues
        if (temp == null) return (new String[0]);
        // Split the comma-separated string into individual roles
        else return (temp.split(USER_SEPARATOR));
    }

    /**
     * Checks if the current user has a specific role using the Roles enum.
     * <p>
     * This method checks to see if the currently logged in user has the role.
     * This method is inefficient and just iterates through all the roles right now.
     * </p>
     * <p>
     * This is a convenience method that accepts the type-safe Roles enum
     * and delegates to the string-based hasRole method. Using the enum
     * helps prevent typos and provides compile-time checking of role names.
     * </p>
     * <p>
     * Performance Note: Uses linear search O(n) where n is the number of roles.
     * For better performance with many roles, consider caching roles in a HashSet.
     * </p>
     *
     * @param request HttpServletRequest the HTTP request containing the session
     * @param role Roles the role enum value to check for
     * @return boolean true if the user has the specified role, false otherwise
     */
    public static boolean hasRole(HttpServletRequest request, Roles role) {
        // Convert enum to string name and delegate to string-based method
        return (hasRole(request, role.name()));
    }

    /**
     * Checks if the current user has a specific role using a string identifier.
     * <p>
     * This method checks to see if the currently logged in user has the role.
     * This method is inefficient and just iterates through all the roles right now.
     * </p>
     * <p>
     * This method supports both standard roles (from the Roles enum) and
     * custom roles that may be defined in the system. It performs a
     * case-sensitive comparison of role names.
     * </p>
     * <p>
     * Performance Warning: This implementation uses linear search through
     * all user roles for each check. For applications with many roles or
     * frequent authorization checks, consider:
     * <ul>
     *   <li>Caching roles in a HashSet in the session for O(1) lookup</li>
     *   <li>Using a bitmap for standard roles if the set is fixed</li>
     *   <li>Implementing a role hierarchy to reduce the number of checks</li>
     * </ul>
     * </p>
     *
     * @param request HttpServletRequest the HTTP request containing the session
     * @param role String the role name to check for (case-sensitive)
     * @return boolean true if the user has the specified role, false otherwise
     */
    public static boolean hasRole(HttpServletRequest request, String role) {

        // Linear search through all user roles
        // This is inefficient for users with many roles
        for (String temp : getUserRoles(request)) {
            // Case-sensitive comparison
            if (temp.equals(role)) return (true);
        }

        return (false);
    }

}
