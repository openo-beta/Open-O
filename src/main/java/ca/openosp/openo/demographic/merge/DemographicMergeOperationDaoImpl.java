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
package ca.openosp.openo.demographic.merge;

import ca.openosp.openo.utility.MiscUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JPA/JDBC implementation of {@link DemographicMergeOperationDao}.
 * <p>
 * Uses native {@code INSERT INTO … SELECT} for direct-copy tables (one SQL statement per table).
 * Uses {@code JdbcTemplate.batchUpdate()} for derived table loops.
 * Schema metadata is resolved via {@code DatabaseMetaData} (JDBC standard, H2-compatible for tests)
 * and cached in-process to avoid repeated metadata queries across 40+ tables in a single merge.
 * All table and column identifiers from {@link DemographicTableIndex} are validated against
 * an alphanumeric whitelist before being interpolated into SQL to prevent injection.
 *
 * @since 2026-03-19
 */
@Repository
@Transactional
public class DemographicMergeOperationDaoImpl implements DemographicMergeOperationDao {

    private static final Logger logger = MiscUtils.getLogger();
    private static final int BATCH_SIZE = 50;

    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // --- MetadataService: schema lookups with in-process caching ---

    private final ConcurrentHashMap<String, Boolean> tableExistsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> columnCache = new ConcurrentHashMap<>();

    private boolean tableExists(String tableName) {
        return tableExistsCache.computeIfAbsent(tableName, t ->
            Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) con -> {
                DatabaseMetaData meta = con.getMetaData();
                try (ResultSet rs = meta.getTables(null, null, t, new String[]{"TABLE"})) {
                    return rs.next();
                }
            }))
        );
    }

    private List<String> getColumnNames(String tableName) {
        return columnCache.computeIfAbsent(tableName, t ->
            jdbcTemplate.execute((ConnectionCallback<List<String>>) con -> {
                DatabaseMetaData meta = con.getMetaData();
                List<String> columns = new ArrayList<>();
                try (ResultSet rs = meta.getColumns(null, null, t, null)) {
                    while (rs.next()) {
                        columns.add(rs.getString("COLUMN_NAME"));
                    }
                }
                return columns;
            })
        );
    }

    // --- QueryService: data queries ---

    private List<Long> getSourcePks(String tableName, String pkColumn, String demoColumn, Integer demographicNo, String additionalWhere) {
        String sql = "SELECT " + pkColumn + " FROM " + tableName +
                     " WHERE " + demoColumn + " = ?" +
                     (additionalWhere != null && !additionalWhere.isEmpty() ? " AND " + additionalWhere : "") +
                     " ORDER BY " + pkColumn;
        return jdbcTemplate.queryForList(sql, Long.class, demographicNo);
    }

    private long getMaxPk(String tableName, String pkColumn) {
        String sql = "SELECT COALESCE(MAX(" + pkColumn + "), 0) FROM " + tableName;
        Long max = jdbcTemplate.queryForObject(sql, Long.class);
        return max != null ? max : 0L;
    }

    private List<Long> getNewPks(String tableName, String pkColumn, String demoColumn, Integer targetDemographicNo, long maxPkBefore) {
        String sql = "SELECT " + pkColumn + " FROM " + tableName +
                     " WHERE " + pkColumn + " > ? AND " + demoColumn + " = ?" +
                     " ORDER BY " + pkColumn;
        return jdbcTemplate.queryForList(sql, Long.class, maxPkBefore, targetDemographicNo);
    }

    // --- Identifier validation ---

    /**
     * Validates that a SQL identifier (table or column name) contains only safe characters.
     * Prevents SQL injection when identifiers are interpolated directly into query strings.
     *
     * @param name String the identifier to validate
     * @throws IllegalArgumentException if the identifier contains unsafe characters
     */
    private void validateIdentifier(String name) {
        if (name == null || !name.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + name);
        }
    }

    // --- Public API ---

    @Override
    public Map<Long, Long> copyDemographic(Integer sourceDemographicNo, Integer targetDemographicNo, DemographicTableIndex tableIndex) {
        String tableName = tableIndex.getTableName();
        String demoColumn = tableIndex.getDemographicColumn();
        String pkColumn = tableIndex.getPkColumn();
        String additionalWhere = tableIndex.getAdditionalWhereClause();

        validateIdentifier(tableName);
        validateIdentifier(demoColumn);
        validateIdentifier(pkColumn);

        if (!tableExists(tableName)) {
            return Collections.emptyMap();
        }

        List<String> columns = getColumnNames(tableName);
        if (columns.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> sourcePks = getSourcePks(tableName, pkColumn, demoColumn, sourceDemographicNo, additionalWhere);
        if (sourcePks.isEmpty()) {
            return Collections.emptyMap();
        }

        long maxPkBefore = getMaxPk(tableName, pkColumn);

        // Build INSERT INTO ... SELECT, nulling the PK and substituting the target demographic
        StringBuilder insertSql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder selectSql = new StringBuilder(" SELECT ");

        for (int i = 0; i < columns.size(); i++) {
            String col = columns.get(i);
            if (i > 0) {
                insertSql.append(", ");
                selectSql.append(", ");
            }
            insertSql.append(col);
            if (col.equalsIgnoreCase(pkColumn)) {
                selectSql.append("NULL");
            } else if (col.equalsIgnoreCase(demoColumn)) {
                selectSql.append(targetDemographicNo);
            } else {
                selectSql.append(col);
            }
        }

        insertSql.append(")");
        selectSql.append(" FROM ").append(tableName)
                 .append(" WHERE ").append(demoColumn).append(" = ?");

        if (additionalWhere != null && !additionalWhere.isEmpty()) {
            selectSql.append(" AND ").append(additionalWhere);
        }

        jdbcTemplate.update(insertSql.toString() + selectSql.toString(), sourceDemographicNo);

        List<Long> newPks = getNewPks(tableName, pkColumn, demoColumn, targetDemographicNo, maxPkBefore);

        Map<Long, Long> pkMap = new HashMap<>();
        for (int i = 0; i < sourcePks.size() && i < newPks.size(); i++) {
            pkMap.put(sourcePks.get(i), newPks.get(i));
        }

        return pkMap;
    }

    @Override
    public void copyDerivedRows(Map<Long, Long> parentPkMap, String childTableName, String fkColumn, String childPkColumn, boolean isChildPkAutoIncrement, String extraDemoColumn, Integer targetDemographicNo) {
        // Implemented in Task 4b
        throw new UnsupportedOperationException("copyDerivedRows - to be implemented in Task 4b");
    }

    @Override
    public Map<Long, Long> copyDemographicForCaseMgmtIssue(Integer sourceDemographicNo, Integer targetDemographicNo) {
        // Implemented in Task 4c
        throw new UnsupportedOperationException("copyDemographicForCaseMgmtIssue - to be implemented in Task 4c");
    }

    @Override
    public void copyCaseMgmtIssueNotes(Map<Long, Long> issuePkMap, Map<Long, Long> notePkMap) {
        // Implemented in Task 4d
        throw new UnsupportedOperationException("copyCaseMgmtIssueNotes - to be implemented in Task 4d");
    }

    @Override
    public void copyConsultationRequestExtArchive(Map<Long, Long> archivePkMap, Map<Long, Long> requestPkMap) {
        // Implemented in Task 4d
        throw new UnsupportedOperationException("copyConsultationRequestExtArchive - to be implemented in Task 4d");
    }

    @Override
    public void copyErereferAttachmentData(Map<Long, Long> pkMap) {
        // Implemented in Task 4e
        throw new UnsupportedOperationException("copyErereferAttachmentData - to be implemented in Task 4e");
    }

    @Override
    public void copyFormBooleanValues(String formName, Map<Long, Long> pkMap) {
        // Implemented in Task 4e
        throw new UnsupportedOperationException("copyFormBooleanValues - to be implemented in Task 4e");
    }

    @Override
    public void copyIdentityTables(Integer sourceDemographicNo, Integer targetDemographicNo, boolean isSecondary) {
        // Implemented in Task 4f
        throw new UnsupportedOperationException("copyIdentityTables - to be implemented in Task 4f");
    }
}
