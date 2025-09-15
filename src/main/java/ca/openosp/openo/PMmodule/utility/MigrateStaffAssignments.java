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

import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.DbConnectionFilter;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Database migration utility for converting legacy staff assignments to program-provider model.
 * <p>
 * This class is a one-time migration tool designed to convert data from an old
 * provider_role_program table structure to the newer program_provider table structure.
 * It maps legacy group-based assignments to modern role-based assignments using
 * predefined role mappings.
 * </p>
 * <p>
 * Migration Logic:
 * <ul>
 *   <li>Reads legacy provider_role_program table entries</li>
 *   <li>Maps group_id=9 to "Nurse" role, all others to "Doctor" role</li>
 *   <li>Validates that both program and provider exist before migration</li>
 *   <li>Inserts new program_provider records with proper role assignments</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Security Warning:</strong> This class uses direct SQL concatenation
 * which could be vulnerable to SQL injection. However, since it's a migration
 * utility with controlled input, this may be acceptable.
 * </p>
 * <p>
 * <strong>Usage:</strong> This is a standalone utility meant to be run once
 * during system migration. Uses direct JDBC rather than ORM for performance.
 * </p>
 *
 * @since 2006-12-16
 * @see ca.openosp.openo.PMmodule.model.ProgramProvider
 */
    /** Logger for migration process tracking and error reporting */
    private static final Logger log = MiscUtils.getLogger();
    /**
     * Database ID for the Nurse role.
     * Populated during initialization by querying caisi_role table.
     */
    protected int nurseRoleId = 0;
    /**
     * Database ID for the Doctor role.
     * Populated during initialization by querying caisi_role table.
     */
    protected int doctorRoleId = 0;

    /**
     * Default constructor.
     * Role IDs are initialized in the run() method.
     */
    public MigrateStaffAssignments() {
    }

    /**
     * Main migration execution method.
     * <p>
     * Performs the complete migration process:
     * <ol>
     *   <li>Initializes role IDs by querying the caisi_role table</li>
     *   <li>Reads all records from the legacy provider_role_program table</li>
     *   <li>For each record, validates that both program and provider exist</li>
     *   <li>Creates corresponding program_provider records with role mappings</li>
     * </ol>
     * </p>
     * <p>
     * The migration uses direct JDBC for performance, processing potentially
     * large numbers of legacy assignments. Only valid program-provider combinations
     * are migrated to ensure referential integrity.
     * </p>
     *
     * @throws Exception if database operations fail or required roles are not found
     */
    public void run() throws Exception {
        // Initialize role IDs from database
        nurseRoleId = (int) this.getRoleId("Nurse");
        doctorRoleId = (int) this.getRoleId("Doctor");

        // Query all legacy staff assignments
        Statement stmt = DbConnectionFilter.getThreadLocalDbConnection().createStatement();
        stmt.execute("SELECT * FROM provider_role_program");
        ResultSet rs = stmt.getResultSet();

        // Process each legacy assignment
        while (rs.next()) {
            long providerNo = rs.getInt("provider_no");
            long programId = rs.getInt("program_id");
            long group_id = rs.getInt("group_id");

            // Validate references exist before migration
            if (programExists(programId) && providerExists(providerNo)) {
                this.addProgramProvider(programId, providerNo, group_id);
            }
        }
        rs.close();
    }

    /**
     * Validates that a program exists in the database.
     * <p>
     * Checks the program table to ensure the specified program ID exists
     * before attempting to create program-provider relationships. This
     * prevents foreign key constraint violations during migration.
     * </p>
     *
     * @param programId long the program ID to validate
     * @return boolean true if program exists, false otherwise
     * @throws Exception if database query fails
     */
    public boolean programExists(long programId) throws Exception {
        Statement stmt = DbConnectionFilter.getThreadLocalDbConnection().createStatement();
        stmt.execute("SELECT count(*) as num FROM program where program_id =" + programId);
        ResultSet rs = stmt.getResultSet();
        rs.next();
        long num = rs.getInt("num");
        return num > 0;
    }

    /**
     * Validates that a provider exists in the database.
     * <p>
     * Checks the provider table to ensure the specified provider number exists
     * before attempting to create program-provider relationships. This
     * prevents foreign key constraint violations during migration.
     * </p>
     *
     * @param providerNo long the provider number to validate
     * @return boolean true if provider exists, false otherwise
     * @throws Exception if database query fails
     */
    public boolean providerExists(long providerNo) throws Exception {
        Statement stmt = DbConnectionFilter.getThreadLocalDbConnection().createStatement();
        stmt.execute("SELECT count(*) as num FROM provider where provider_no =" + providerNo);
        ResultSet rs = stmt.getResultSet();
        rs.next();
        long num = rs.getInt("num");
        return num > 0;
    }

    /**
     * Retrieves the database ID for a role by name.
     * <p>
     * Queries the caisi_role table to find the role_id for the specified
     * role name. This is used during initialization to map role names
     * to their database identifiers for efficient lookups during migration.
     * </p>
     *
     * @param name String the role name to look up (e.g., "Nurse", "Doctor")
     * @return long the database ID for the role
     * @throws Exception if the role is not found or database query fails
     */
    public long getRoleId(String name) throws Exception {
        Statement stmt = DbConnectionFilter.getThreadLocalDbConnection().createStatement();
        stmt.execute("SELECT * FROM caisi_role where name = '" + name + "'");
        ResultSet rs = stmt.getResultSet();
        if (rs.next()) {
            return rs.getInt("role_id");
        } else {
            throw new Exception("no " + name + " role defined");
        }
    }

    /**
     * Creates a new program_provider record with role mapping.
     * <p>
     * Inserts a new record into the program_provider table, mapping the
     * legacy group-based assignment to the modern role-based system.
     * The role mapping follows this logic:
     * <ul>
     *   <li>group_id = 9 maps to Nurse role</li>
     *   <li>All other group_ids map to Doctor role</li>
     * </ul>
     * </p>
     * <p>
     * The team_id is set to 0, indicating no specific team assignment.
     * This may need to be updated in future migrations if team assignments
     * become relevant.
     * </p>
     *
     * @param programId long the program identifier
     * @param providerNo long the provider number
     * @param groupNo long the legacy group number for role mapping
     * @throws Exception if database insert operation fails
     */
    public void addProgramProvider(long programId, long providerNo, long groupNo) throws Exception {
        Statement stmt = DbConnectionFilter.getThreadLocalDbConnection().createStatement();

        // Map legacy group ID to modern role ID
        long roleId = doctorRoleId;
        if (groupNo == 9) {
            roleId = nurseRoleId;
        }

        // Insert new program_provider record
        stmt.executeUpdate("insert into program_provider (program_id,provider_no,role_id,team_id) values (" +
                          programId + "," + providerNo + "," + roleId + "," + "0)");
    }

    /**
     * Command-line entry point for running the staff assignment migration.
     * <p>
     * Creates a new instance of the migration utility and executes the
     * migration process. This is designed to be run once during system
     * upgrade to convert legacy data structures.
     * </p>
     *
     * @param args String[] command-line arguments (not used)
     * @throws Exception if migration fails
     */
    public static void main(String args[]) throws Exception {
        new MigrateStaffAssignments().run();
    }
}
