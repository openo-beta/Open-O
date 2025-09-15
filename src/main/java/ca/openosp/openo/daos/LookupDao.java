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

package ca.openosp.openo.daos;

import java.sql.SQLException;
import java.util.List;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.commn.model.Facility;

import ca.openosp.openo.model.LookupCodeValue;
import ca.openosp.openo.model.LookupTableDefValue;

/**
 * Data Access Object interface for lookup table management.
 *
 * This interface provides operations for managing lookup tables and codes
 * throughout the EMR system. Lookup tables are used to maintain standardized
 * lists of values for various fields such as diagnoses, procedures, service
 * codes, and organizational codes. They ensure data consistency and provide
 * dropdown options for user interfaces.
 *
 * The DAO handles:
 * - Loading and filtering lookup codes by table ID
 * - Managing hierarchical code relationships with parent codes
 * - Saving and updating lookup values with field definitions
 * - Organization code management for multi-facility operations
 * - Executing stored procedures for complex lookup operations
 * - Active client counting for organizational reporting
 *
 * This is a critical component for maintaining referential integrity
 * and standardization across the healthcare system's data.
 *
 * @since 2009-01-01
 */
public interface LookupDao {

    /**
     * Loads a list of lookup codes for a specific table.
     *
     * Retrieves lookup codes filtered by table ID with optional
     * filtering by active status, code value, and description.
     *
     * @param tableId String identifier of the lookup table
     * @param activeOnly boolean true to retrieve only active codes
     * @param code String optional code filter (partial match)
     * @param codeDesc String optional description filter (partial match)
     * @return List list of matching LookupCodeValue objects
     */
    public List LoadCodeList(String tableId, boolean activeOnly, String code, String codeDesc);

    /**
     * Retrieves a specific lookup code value.
     *
     * @param tableId String identifier of the lookup table
     * @param code String the specific code to retrieve
     * @return LookupCodeValue the lookup code object, or null if not found
     */
    public LookupCodeValue GetCode(String tableId, String code);

    /**
     * Loads a list of lookup codes with hierarchical filtering.
     *
     * Retrieves lookup codes that can be filtered by parent code
     * for hierarchical code structures.
     *
     * @param tableId String identifier of the lookup table
     * @param activeOnly boolean true to retrieve only active codes
     * @param parentCode String parent code for hierarchical filtering
     * @param code String optional code filter (partial match)
     * @param codeDesc String optional description filter (partial match)
     * @return List list of matching LookupCodeValue objects
     */
    public List LoadCodeList(String tableId, boolean activeOnly, String parentCode, String code, String codeDesc);

    /**
     * Retrieves the table definition for a lookup table.
     *
     * Gets metadata about the lookup table structure including
     * field definitions and table properties.
     *
     * @param tableId String identifier of the lookup table
     * @return LookupTableDefValue table definition object
     */
    public LookupTableDefValue GetLookupTableDef(String tableId);

    /**
     * Loads field definitions for a lookup table.
     *
     * Retrieves the list of field definitions that describe
     * the structure and validation rules for the lookup table.
     *
     * @param tableId String identifier of the lookup table
     * @return List list of field definition objects
     */
    public List LoadFieldDefList(String tableId);

    /**
     * Gets field values for a specific code.
     *
     * Retrieves all field values associated with a specific
     * code in the lookup table.
     *
     * @param tableDef LookupTableDefValue table definition
     * @param code String the code to retrieve values for
     * @return List list of field values for the code
     */
    public List GetCodeFieldValues(LookupTableDefValue tableDef, String code);

    /**
     * Gets all field values for a lookup table.
     *
     * Retrieves field values for all codes in the lookup table,
     * returned as a list of lists for tabular representation.
     *
     * @param tableDef LookupTableDefValue table definition
     * @return List<List> nested list of all field values
     */
    public List<List> GetCodeFieldValues(LookupTableDefValue tableDef);

    /**
     * Saves a lookup code value with field definitions.
     *
     * Creates or updates a lookup code value using the provided
     * table definition and field values.
     *
     * @param isNew boolean true for insert, false for update
     * @param tableDef LookupTableDefValue table definition
     * @param fieldDefList List list of field definitions with values
     * @return String result message or identifier
     * @throws SQLException if database operation fails
     */
    public String SaveCodeValue(boolean isNew, LookupTableDefValue tableDef, List fieldDefList) throws SQLException;

    /**
     * Saves a lookup code value.
     *
     * Creates or updates a lookup code value directly.
     *
     * @param isNew boolean true for insert, false for update
     * @param codeValue LookupCodeValue the code value to save
     * @return String result message or identifier
     * @throws SQLException if database operation fails
     */
    public String SaveCodeValue(boolean isNew, LookupCodeValue codeValue) throws SQLException;

    /**
     * Saves a program as an organization code.
     *
     * Creates or updates an organization code entry based on
     * a program entity for multi-facility integration.
     *
     * @param program Program the program to save as org code
     * @throws SQLException if database operation fails
     */
    public void SaveAsOrgCode(Program program) throws SQLException;

    /**
     * Checks if one organization is within another.
     *
     * Determines hierarchical relationship between organizations
     * for access control and data filtering.
     *
     * @param org1 String first organization code
     * @param org2 String second organization code
     * @return boolean true if org1 is within org2's hierarchy
     */
    public boolean inOrg(String org1, String org2);

    /**
     * Saves a facility as an organization code.
     *
     * Creates or updates an organization code entry based on
     * a facility entity for multi-facility operations.
     *
     * @param facility Facility the facility to save as org code
     * @throws SQLException if database operation fails
     */
    public void SaveAsOrgCode(Facility facility) throws SQLException;

    /**
     * Saves a lookup code value as an organization code.
     *
     * Generic method to save any lookup code value as an
     * organization code in the specified table.
     *
     * @param orgVal LookupCodeValue the organization value to save
     * @param tableId String target table identifier
     * @throws SQLException if database operation fails
     */
    public void SaveAsOrgCode(LookupCodeValue orgVal, String tableId) throws SQLException;

    /**
     * Executes a stored procedure.
     *
     * Runs a database stored procedure with the provided parameters
     * for complex lookup operations.
     *
     * @param procName String name of the stored procedure
     * @param params String[] array of parameters for the procedure
     * @throws SQLException if procedure execution fails
     */
    public void runProcedure(String procName, String[] params) throws SQLException;

    /**
     * Gets the count of active clients for an organization.
     *
     * Returns the number of active clients associated with
     * the specified organization code for reporting purposes.
     *
     * @param orgCd String organization code
     * @return int count of active clients
     * @throws SQLException if database query fails
     */
    public int getCountOfActiveClient(String orgCd) throws SQLException;

    /**
     * Sets the provider DAO dependency.
     *
     * Injects the provider DAO for operations requiring
     * provider-related lookups.
     *
     * @param providerDao ProviderDao the provider DAO instance
     */
    public void setProviderDao(ProviderDao providerDao);

}
