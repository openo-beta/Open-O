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

package ca.openosp.openo.PMmodule.utility;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ca.openosp.openo.PMmodule.model.ProgramProvider;
import ca.openosp.openo.PMmodule.service.ProgramManager;
import ca.openosp.openo.PMmodule.service.ProviderManager;
import ca.openosp.openo.commn.model.Provider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ca.openosp.openo.model.security.Secrole;

/**
 * Utility class for retrieving and analyzing provider roles within programs.
 * <p>
 * This class is a command-line tool designed to extract and report on provider
 * role assignments across all programs in the system. It analyzes provider-program
 * relationships and outputs role information for providers with single-role assignments.
 * The tool was likely used for data migration or system analysis purposes.
 * </p>
 * <p>
 * The class operates by:
 * <ul>
 *   <li>Loading all providers from the database</li>
 *   <li>For each provider, collecting their roles across all programs</li>
 *   <li>Filtering providers with single role assignments</li>
 *   <li>Building formatted role reports</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Usage Pattern:</strong> This appears to be a standalone utility class
 * meant to be run from command line using the main() method. It uses Spring
 * application context for dependency injection.
 * </p>
 * <p>
 * <strong>Design Note:</strong> The class uses raw types for collections and
 * lacks proper exception handling. The output logic builds strings but doesn't
 * actually output them, suggesting incomplete implementation.
 * </p>
 *
 * @since 2006-12-16
 * @see ca.openosp.openo.PMmodule.model.ProgramProvider
 * @see ca.openosp.openo.model.security.Secrole
 */
public class GetProviderRoles {

    /**
     * Spring application context for dependency injection.
     * Loaded from test configuration to access manager beans.
     */
    protected ApplicationContext ctx = null;

    /**
     * Constructor that initializes the Spring application context.
     * <p>
     * Loads the test application context configuration to access
     * the required manager beans for database operations. The test
     * context is used rather than the main context, suggesting this
     * was designed as a utility tool.
     * </p>
     */
    public GetProviderRoles() {
        String[] paths = {"/WEB-INF/applicationContext-test.xml"};
        ctx = new ClassPathXmlApplicationContext(paths);
    }

    /**
     * Main execution method that processes all providers and analyzes their roles.
     * <p>
     * This method retrieves all providers from the database and examines their
     * role assignments across all programs. It focuses on providers with single
     * role assignments, building formatted strings for reporting purposes.
     * </p>
     * <p>
     * The method iterates through all providers, gets their associated roles,
     * and skips providers with multiple roles. For single-role providers,
     * it builds a comma-separated string of role names.
     * </p>
     * <p>
     * Note: The built strings are not actually output or returned, suggesting
     * this may be incomplete implementation or the output was removed.
     * </p>
     *
     * @throws Exception if database access or Spring context operations fail
     */
    @SuppressWarnings("unchecked")
    public void run() throws Exception {
        // Get all providers from the database
        ProviderManager providerManager = (ProviderManager) ctx.getBean(ProviderManager.class);
        List providers = providerManager.getProviders();

        // Process each provider
        for (Iterator iter = providers.iterator(); iter.hasNext(); ) {
            Provider provider = (Provider) iter.next();

            // Get all roles for this provider across programs
            Set roles = getRoles(provider.getProviderNo());

            // Skip providers with multiple roles
            if (roles.size() > 1) {
                continue;
            }

            // Build comma-separated role names for single-role providers
            int x = 0;
            StringBuilder buf = new StringBuilder();
            for (Iterator iter2 = roles.iterator(); iter2.hasNext(); ) {
                String roleName = (String) iter2.next();
                if (x != 0) {
                    buf.append(",");
                }
                buf.append(roleName);
                x++;
            }
            buf.append("\n");
            // Note: The built string is not used or output
        }
    }

    /**
     * Retrieves all unique role names for a specific provider.
     * <p>
     * This method queries all program-provider relationships for the given
     * provider and collects the unique role names associated with their
     * assignments. Returns a Set to automatically eliminate duplicate roles
     * across multiple programs.
     * </p>
     * <p>
     * The method handles cases where a ProgramProvider might have a null role
     * by silently skipping such entries. This could indicate incomplete data
     * or referential integrity issues in the database.
     * </p>
     *
     * @param providerNo String the provider's unique identifier
     * @return Set<String> collection of unique role names for this provider
     */
    @SuppressWarnings("unchecked")
    public Set getRoles(String providerNo) {
        ProgramManager programManager = (ProgramManager) ctx.getBean(ProgramManager.class);
        // Get all program-provider relationships for this provider
        List ppList = programManager.getProgramProvidersByProvider(providerNo);
        Set roles = new HashSet();

        // Extract unique role names from program-provider relationships
        for (Iterator iter = ppList.iterator(); iter.hasNext(); ) {
            ProgramProvider pp = (ProgramProvider) iter.next();
            Secrole role = pp.getRole();
            if (role != null) {
                // Add role name to set (duplicates automatically eliminated)
                roles.add(role.getRoleName());
                // Alternative: roles.add(String.valueOf(role.getId()));
            } else {
                // Role is null - could indicate data integrity issue
                // Originally: log.error("ROLE IS NULL" + pp.getId());
            }
        }
        return roles;
    }

    /**
     * Command-line entry point for running the provider role analysis.
     * <p>
     * Creates an instance of GetProviderRoles and executes the analysis.
     * This suggests the class was designed to be run as a standalone utility
     * for data analysis or migration purposes.
     * </p>
     *
     * @param args String[] command-line arguments (not used)
     * @throws Exception if the analysis encounters any errors
     */
    public static void main(String args[]) throws Exception {
        GetProviderRoles prog = new GetProviderRoles();
        prog.run();
    }

}
