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

package ca.openosp.openo.dao;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Base Data Access Object (DAO) class for legacy JDBC-based database operations.
 *
 * This abstract superclass provides a foundation for JDBC-based DAO implementations
 * in the OpenO EMR system. It was originally created to extract database access code
 * from JSP files and centralize it in proper DAO classes, following the DAO pattern.
 *
 * The class provides two primary query execution strategies:
 * - Map-based results: Automatic mapping of result sets to Map<String, Object>
 * - RowMapper-based results: Custom mapping using Spring RowMapper implementations
 *
 * Key architectural decisions:
 * - Uses Spring's JdbcDaoSupport for JDBC template integration
 * - Query definitions are externalized in subclass arrays for maintainability
 * - RowMappers are defined per query for complex object mapping
 * - Named query approach prevents SQL injection and improves readability
 *
 * This class represents the legacy JDBC approach and is being gradually replaced
 * by Hibernate-based DAOs for better ORM support. New development should prefer
 * Hibernate DAOs unless there's a specific need for raw JDBC performance.
 *
 * Subclasses must implement:
 * - getDbQueries(): Returns array of [queryName, sqlQuery] pairs
 * - getRowMappers(): Returns map of queryName to RowMapper instances
 *
 * Security considerations:
 * - All queries should use parameterized statements (no string concatenation)
 * - PHI data must never be logged
 * - Query parameters should be validated in service layer
 *
 * Usage note: Do not access methods of this class directly from controllers
 * or JSPs. Use the corresponding Manager/Service classes instead to maintain
 * proper layering and transaction boundaries.
 *
 * @since 2005-01-01
 * @deprecated Consider using Hibernate-based DAOs for new development
 */
public abstract class OscarSuperDao extends JdbcDaoSupport {

    /**
     * Logger instance for debugging and error tracking.
     * Uses the MiscUtils logger factory for consistent logging across the application.
     */
    protected static final Logger logger = MiscUtils.getLogger();

    /**
     * Provides the SQL query definitions for this DAO.
     *
     * Subclasses must return a two-dimensional array where each element contains:
     * - [0]: Query name (key) used to identify the query
     * - [1]: SQL query string with parameter placeholders (?)
     *
     * Example:
     * return new String[][] {
     *     {"findByStatus", "SELECT * FROM appointments WHERE status = ?"},
     *     {"countByProvider", "SELECT COUNT(*) FROM appointments WHERE provider_no = ?"}
     * };
     *
     * @return String[][] array of query definitions [queryName, sqlQuery]
     * @since 2005-01-01
     */
    protected abstract String[][] getDbQueries();

    /**
     * Provides the RowMapper implementations for complex query results.
     *
     * Subclasses must return a map where:
     * - Key: Query name (must match a query name from getDbQueries())
     * - Value: RowMapper instance for converting ResultSet rows to objects
     *
     * RowMappers are used when the default Map-based results are insufficient
     * and custom object mapping is required.
     *
     * @return Map mapping query names to their corresponding RowMapper instances
     * @since 2005-01-01
     */
    protected abstract Map<String, RowMapper> getRowMappers();

    /**
     * Executes a parameterized SELECT query with automatic Map-based result mapping.
     *
     * This method provides a simple way to execute queries when custom object mapping
     * is not required. Each row in the result set is converted to a Map where:
     * - Keys are column names (or aliases)
     * - Values are the corresponding column values
     *
     * The method is useful for:
     * - Ad-hoc queries where creating a DTO is overkill
     * - Queries returning dynamic columns
     * - Quick data retrieval for reports or displays
     *
     * Example usage:
     * List<Map<String, Object>> results = dao.executeSelectQuery(
     *     "findActiveAppointments",
     *     new Object[] { providerNo, date }
     * );
     *
     * @param queryName String the key identifying the SQL query in getDbQueries()
     * @param params Object[] array of parameters to bind to the query placeholders
     * @return List<Map<String, Object>> where each Map represents a result row
     * @throws IllegalArgumentException if queryName is not found in getDbQueries()
     * @since 2005-01-01
     */
    public List<Map<String, Object>> executeSelectQuery(String queryName, Object[] params) {
        return getJdbcTemplate().queryForList(getSqlQueryByKey(queryName), params);
    }

    /**
     * Executes a parameterized SELECT query with custom RowMapper-based result mapping.
     *
     * This method uses a RowMapper to convert each result set row into a custom
     * object (DTO, entity, or value object). The RowMapper for the query must be
     * registered in the getRowMappers() map with the same key as the query name.
     *
     * Use this method when:
     * - You need strongly-typed result objects
     * - Complex data transformations are required
     * - Building domain objects from result sets
     * - Type safety is important for the calling code
     *
     * Example usage:
     * List<Appointment> appointments = (List<Appointment>) dao.executeRowMappedSelectQuery(
     *     "findByDateRange",
     *     new Object[] { startDate, endDate }
     * );
     *
     * @param queryName String the key identifying both the SQL query and its RowMapper
     * @param params Object[] array of parameters to bind to the query placeholders
     * @return List<Object> of custom objects created by the RowMapper (cast required)
     * @throws IllegalArgumentException if queryName is not found in queries or mappers
     * @since 2005-01-01
     */
    @SuppressWarnings("unchecked")
    public List<Object> executeRowMappedSelectQuery(String queryName, Object[] params) {
        return getJdbcTemplate().query(getSqlQueryByKey(queryName), params, getRowMapperByKey(queryName));
    }

    /**
     * Retrieves the SQL query string associated with a query name.
     *
     * This internal method looks up the SQL query from the getDbQueries() array
     * using the provided key. It performs a linear search through the query
     * definitions to find a matching query name.
     *
     * The method logs the query name at debug level for troubleshooting.
     * This can help track which queries are being executed during debugging
     * but should not log any PHI or query parameters.
     *
     * @param key String the query name to look up
     * @return String the SQL query associated with the key
     * @throws IllegalArgumentException if no query with the specified name exists
     * @since 2005-01-01
     */
    private String getSqlQueryByKey(String key) {
        // Log query name for debugging (never log parameters or results with PHI)
        logger.debug("Calling query " + key);

        // Linear search through query definitions
        for (String[] query : getDbQueries()) {
            if (query[0].equals(key)) {
                return query[1];
            }
        }

        // Query not found - throw descriptive exception
        throw new IllegalArgumentException("dbQueries array contains no query with specified name: " + key);
    }

    /**
     * Retrieves the RowMapper instance associated with a query name.
     *
     * This internal method looks up the RowMapper from the getRowMappers() map
     * using the provided key. The RowMapper is responsible for converting
     * ResultSet rows into custom objects.
     *
     * RowMappers provide:
     * - Type-safe object creation from ResultSet
     * - Consistent mapping logic across queries
     * - Separation of data access and object creation concerns
     *
     * @param key String the query name whose RowMapper to retrieve
     * @return RowMapper the mapper instance for the specified query
     * @throws IllegalArgumentException if no mapper with the specified name exists
     * @since 2005-01-01
     */
    private RowMapper getRowMapperByKey(String key) {
        // Look up the RowMapper in the map
        RowMapper rowMapper = getRowMappers().get(key);

        // Return mapper if found
        if (rowMapper != null) {
            return rowMapper;
        }

        // Mapper not found - throw descriptive exception
        throw new IllegalArgumentException("rowMappers map contains no row mapper with specified name: " + key);
    }
}
