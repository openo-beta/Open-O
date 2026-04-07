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


package ca.openosp.openo.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import ca.openosp.Misc;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import ca.openosp.openo.utility.MiscUtils;

import ca.openosp.openo.db.DBHandler;

public class SqlUtils {
    private static Logger logger = MiscUtils.getLogger();

    /** Matches safe SQL identifiers: alphanumeric and underscores only. */
    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z0-9_]+$");

    /** Matches numeric-only strings (one or more digits). */
    private static final Pattern NUMERIC_ID = Pattern.compile("^[0-9]+$");

    /**
     * Validates that a value contains only digits (one or more).
     * Use for request parameters that represent numeric IDs before use in SQL.
     *
     * @param value the value to validate
     * @param label a descriptive label for error messages (e.g. "demographic_no")
     * @return the validated value
     * @throws SecurityException if the value is null, empty, or non-numeric
     */
    public static String validateNumericId(String value, String label) {
        if (value == null || value.isEmpty() || !NUMERIC_ID.matcher(value).matches()) {
            throw new SecurityException("Invalid numeric identifier for " + label);
        }
        return value;
    }

    /**
     * Checks whether a value contains only digits (one or more).
     * Unlike {@link #validateNumericId}, this does not throw — use it when
     * custom error handling (e.g. return ERROR, setAttribute) is needed.
     *
     * @param value the value to check
     * @return true if the value is non-null, non-empty, and numeric
     */
    public static boolean isNumericId(String value) {
        return value != null && !value.isEmpty() && NUMERIC_ID.matcher(value).matches();
    }

    /**
     * Validates that a value is either null, empty, or contains only digits.
     * Use for optional numeric fields that may legitimately be blank.
     *
     * @param value the value to validate (null and empty are allowed)
     * @param label a descriptive label for error messages
     * @return the validated value
     * @throws SecurityException if the value is non-empty and non-numeric
     */
    public static String validateOptionalNumericId(String value, String label) {
        if (value != null && !value.isEmpty() && !NUMERIC_ID.matcher(value).matches()) {
            throw new SecurityException("Invalid numeric identifier for " + label);
        }
        return value;
    }

    /**
     * Validates that a table name is safe for direct inclusion in SQL.
     * Table names cannot be parameterized in PreparedStatement, so this
     * method ensures the name contains only alphanumeric characters and underscores.
     *
     * @param tableName the table name to validate
     * @return the validated table name
     * @throws SecurityException if the name is null, empty, or contains unsafe characters
     */
    public static String validateTableName(String tableName) {
        if (tableName == null || tableName.isEmpty() || !VALID_IDENTIFIER.matcher(tableName).matches()) {
            throw new SecurityException("Invalid table name: " + tableName);
        }
        return tableName;
    }

    /**
     * Validates that a column name is safe for direct inclusion in SQL.
     * Column names cannot be parameterized in PreparedStatement, so this
     * method ensures the name contains only alphanumeric characters and underscores.
     *
     * @param columnName the column name to validate
     * @return the validated column name
     * @throws SecurityException if the name is null, empty, or contains unsafe characters
     */
    public static String validateColumnName(String columnName) {
        if (columnName == null || columnName.isEmpty() || !VALID_IDENTIFIER.matcher(columnName).matches()) {
            throw new SecurityException("Invalid column name: " + columnName);
        }
        return columnName;
    }

    /**
     * Validates a sort column against an explicit whitelist of allowed column names.
     * Use this when the column value comes from user input (e.g. request parameters).
     *
     * @param column the column name to validate
     * @param allowed the set of allowed column names
     * @return the validated column name
     * @throws SecurityException if the column is not in the allowed set
     */
    public static String validateSortColumn(String column, Set<String> allowed) {
        if (column == null || !allowed.contains(column)) {
            throw new SecurityException("Invalid sort column: " + column);
        }
        return column;
    }

    /**
     * Generates a comma-separated string of ? placeholders for use in SQL IN clauses.
     * Example: {@code inClausePlaceholders(3)} returns {@code "?,?,?"}.
     *
     * @param count the number of placeholders (must be &gt; 0)
     * @return a string of comma-separated ? placeholders
     * @throws IllegalArgumentException if count is less than 1
     */
    public static String inClausePlaceholders(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("IN clause placeholder count must be >= 1, got: " + count);
        }
        return String.join(",", Collections.nCopies(count, "?"));
    }


    /**
     * Replaces all occurrences of ${name} in a SQL template with ? and adds the value to the
     * params list for PreparedStatement binding. Strips surrounding single or double quotes
     * if present (e.g., '${provider}' becomes ?), since PreparedStatement handles string
     * quoting automatically. Legacy SQL templates (apconfig.xml, ClinicalReports.xml) include
     * quotes for the old string-substitution path, but parameterized queries must not have
     * the placeholder inside quotes or JDBC treats it as a literal '?'.
     *
     * @param sql    the SQL template containing ${name} placeholders
     * @param name   the placeholder name (without ${ } wrapper)
     * @param value  the value to bind
     * @param params the list to collect bind parameter values into
     * @return the SQL with ${name} replaced by ?
     */
    public static String parameterizeToken(String sql, String name, String value, List<Object> params) {
        String token = "${" + name + "}";
        int idx;
        while ((idx = sql.indexOf(token)) >= 0) {
            int start = idx;
            int end = idx + token.length();
            // Strip surrounding single or double quotes: '${token}' or "${token}" → ?
            if (start > 0 && end < sql.length()) {
                char before = sql.charAt(start - 1);
                char after = sql.charAt(end);
                if ((before == '\'' && after == '\'') || (before == '"' && after == '"')) {
                    start--;
                    end++;
                }
            }
            sql = sql.substring(0, start) + "?" + sql.substring(end);
            params.add(value);
        }
        return sql;
    }

    /**
     * Returns a List of String[] which contain the results of the specified arbitrary query.
     *
     * @param qry String - The String SQL Query
     * @return List - The List of String[] results or null if no results were yielded
     */
    public static List<String[]> getQueryResultsList(String qry) {
        ArrayList<String[]> records = null;
        ResultSet rs = null;

        try {
            records = new ArrayList<String[]>();

            rs = DBHandler.GetSQL(qry);
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                String[] record = new String[cols];
                for (int i = 0; i < cols; i++) {
                    record[i] = Misc.getString(rs, i + 1);
                }
                records.add(record);
            }
        } catch (SQLException e) {
            records = null;
            MiscUtils.getLogger().error("Error", e);
        }

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex1) {
                MiscUtils.getLogger().error("Error", ex1);
            }
        }
        if (records != null) {
            records = records.isEmpty() ? null : records;
        }
        return records;
    }

    /**
     * Returns a single row(the first row) from a quesry result Generally should only be used with queries that return a single result Returns null if there is no result
     *
     * @param qry String
     * @return String[]
     */
    public static String[] getRow(String qry) {
        String ret[] = null;
        List<String[]> list = getQueryResultsList(qry);
        if (list != null) {
            ret = list.get(0);
        }
        return ret;
    }

    /**
     * Returns a List of String[] which contain the results of the specified parameterized query.
     */
    public static List<String[]> getQueryResultsList(String qry, Object... params) {
        ArrayList<String[]> records = null;
        ResultSet rs = null;
        try {
            records = new ArrayList<String[]>();
            rs = DBHandler.GetPreSQL(qry, params);
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                String[] record = new String[cols];
                for (int i = 0; i < cols; i++) {
                    record[i] = Misc.getString(rs, i + 1);
                }
                records.add(record);
            }
        } catch (SQLException e) {
            records = null;
            MiscUtils.getLogger().error("Error", e);
        }
        if (rs != null) {
            try { rs.close(); } catch (SQLException ex1) { MiscUtils.getLogger().error("Error", ex1); }
        }
        if (records != null) {
            records = records.isEmpty() ? null : records;
        }
        return records;
    }

    /**
     * Returns a single row from a parameterized query result.
     */
    public static String[] getRow(String qry, Object... params) {
        String ret[] = null;
        List<String[]> list = getQueryResultsList(qry, params);
        if (list != null) {
            ret = list.get(0);
        }
        return ret;
    }

    /**
     * Returns a List of Map objects which contain the results of the specified arbitrary query. The key contains the field names of the table and the value, the field value of the
     * record
     *
     * @param qry String - The String SQL Query
     * @return List - The List of String Map results or null if no results were yielded
     */
    public static List<Properties> getQueryResultsMapList(String qry) {
        List<Properties> records = null;
        ResultSet rs = null;

        try {
            records = new ArrayList<Properties>();

            rs = DBHandler.GetSQL(qry);
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Properties record = new Properties();
                for (int i = 0; i < cols; i++) {
                    String columnName = rs.getMetaData().getColumnName(i + 1);
                    String cellValue = Misc.getString(rs, i + 1);
                    if (columnName != null && !"".equals(columnName)) {

                        cellValue = cellValue == null ? "" : cellValue;
                        record.setProperty(columnName, cellValue);
                    } else {
                        MiscUtils.getLogger().debug("Empty key for value: " + cellValue);
                    }
                }
                records.add(record);
            }
        } catch (SQLException e) {
            records = null;
            MiscUtils.getLogger().error("Error", e);
        }

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex1) {
                MiscUtils.getLogger().error("Error", ex1);
            }
        }

        return records;
    }

    /**
     * Creates an 'in' clause segment of an sql query. This is handy in cases where the criteria of a query is dynamic/unknown
     *
     * @param criteria  String[] - he string array of criteria used to construct the query segment
     * @param useQuotes boolean - a value of true indicates that the clause components are enclosed in quotes
     * @return String - The constructed sql 'in' clause String
     * @deprecated Vulnerable to SQL injection. Use {@link #inClausePlaceholders(int)} with parameterized queries instead.
     */
    @Deprecated
    public static String constructInClauseString(String[] criteria, boolean useQuotes) {
        StringBuilder ret = new StringBuilder();
        String quote = useQuotes == true ? "'" : "";
        if (criteria.length != 0) {
            ret.append("in (");
            for (int i = 0; i < criteria.length; i++) {
                ret.append(quote + criteria[i] + quote);
                if (i < criteria.length - 1) {
                    ret.append(",");
                }
            }
        }
        ret.append(")");
        return ret.toString();
    }

    /**
     * This method will return a string similar to "(1,3,5,7)". The intent is that this method will be used to build "in clauses" like select * from foo where x in (1,3,5,7) for
     * statements. This only works for primitives unless you pre-quote strings.
     * @deprecated Vulnerable to SQL injection. Use {@link #inClausePlaceholders(int)} with parameterized queries instead.
     */
    @Deprecated
    public static String constructInClauseForStatements(Object[] items) {
        return (constructInClauseForStatements(items, false));
    }

    /**
     * This method will return a string similar to "(1,3,5,7)". The intent is that this method will be used to build "in clauses" like select * from foo where x in (1,3,5,7) for
     * statements. This only works for primitives unless you pre-quote strings.
     * @deprecated Vulnerable to SQL injection. Use {@link #inClausePlaceholders(int)} with parameterized queries instead.
     */
    @Deprecated
    public static String constructInClauseForStatements(Object[] items, boolean quoteItems) {
        if (items.length <= 0)
            throw (new IllegalArgumentException("Don't call this method if the items for the in clause is <1 it doesn't make sense."));

        StringBuilder sb = new StringBuilder();
        sb.append('(');

        for (Object item : items) {
            if (sb.length() > 1) sb.append(',');
            if (quoteItems) sb.append('\'');
            sb.append(item);
            if (quoteItems) sb.append('\'');
        }

        sb.append(')');
        return (sb.toString());
    }


    /**
     * This method will close the resources passed in. Pass in null for anything you don't want closed. All exceptions will be logged at WARN level but not rethrown. Note that if
     * you retrieved the connection from something like hibernate/jpa you should not close the connection, let the entityManager / sessionManager do that, just close the statement
     * and resultset.
     */
    public static void closeResources(Connection c, Statement s, ResultSet rs) {
        closeResources(s, rs);

        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                if (e.getMessage().toLowerCase().indexOf("closed") >= 0) {
                    // I don't care.
                } else {
                    logger.warn("Error closing Connection.", e);
                }
            }
        }
    }

    public static void closeResources(Session session, Statement s, ResultSet rs) {
        closeResources(s, rs);

        if (session != null) {
            session.close();
        }
    }

    public static void closeResources(Statement s, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.warn("Error closing ResultSet.", e);
            }
        }

        if (s != null) {
            try {
                s.close();
            } catch (SQLException e) {
                logger.warn("Error closing Statement.", e);
            }
        }
    }


}
