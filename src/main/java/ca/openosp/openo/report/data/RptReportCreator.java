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


/*
 * Created on 2005-8-1
 */
package ca.openosp.openo.report.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang3.time.DateFormatUtils;
import ca.openosp.openo.utility.MiscUtils;

import ca.openosp.Misc;
import ca.openosp.openo.db.DBHandler;
import ca.openosp.openo.util.ParameterizedClause;
import ca.openosp.openo.util.SqlUtils;

/**
 * @author yilee18
 */
public final class RptReportCreator {

    // select formBCAR.pg1_ethOrig as Ethnic Origin, ...
    public String getSelectField(String recordId) throws SQLException {
        StringBuilder ret = new StringBuilder();
        String sql = "select * from reportConfig where report_id = ? order by order_no";
        ResultSet rs = DBHandler.GetPreSQL(sql, Integer.parseInt(recordId));
        while (rs.next()) {
            String tableName = SqlUtils.validateTableName(Misc.getString(rs, "table_name"));
            String fieldName = SqlUtils.validateColumnName(Misc.getString(rs, "name"));
            String caption = Misc.getString(rs, "caption");
            ret.append((ret.length() < 8 ? " " : ", ") + tableName + "." + fieldName);
            if (caption != null && caption.length() > 0) {
                ret.append(" as '" + caption.replace("'", "''") + "'");
            }
        }
        rs.close();
        return ret.toString();
    }

    // from formBCAR
    public String getFromTableFirst(String recordId) throws SQLException {
        String ret = "  ";
        String sql = "select distinct table_name from reportConfig where report_id = ? order by table_name desc";
        ResultSet rs = DBHandler.GetPreSQL(sql, Integer.parseInt(recordId));
        if (rs.next()) {
            ret = SqlUtils.validateTableName(Misc.getString(rs, "table_name"));
        }
        rs.close();
        return ret;
    }

    // from formBCAR, demographic
    public String getFromTable(String recordId) throws SQLException {
        String ret = "  ";
        Vector vec = new Vector();
        String sql = "select distinct table_name from reportConfig where report_id = ? order by table_name desc";
        ResultSet rs = DBHandler.GetPreSQL(sql, Integer.parseInt(recordId));
        while (rs.next()) {
            vec.add(SqlUtils.validateTableName(Misc.getString(rs, "table_name")));
        }
        rs.close();
        for (int i = 0; i < vec.size(); i++) {
            ret += (i == 0 ? "" : ",") + vec.get(i);
        }
        return ret;
    }

    // tableName: formBCAR,formBCNewBorn... how to handle??
    public String getWhereJoinClause(String tableName, boolean bDemo) {
        String ret = "";
        if (bDemo)
            ret = tableName + ".demographic_no=demographic.demographic_no";
        return ret;
    }

    /**
     * Walks the {@code ${var}} tokens in {@code template} positionally, replacing each token
     * with a {@code ?} placeholder and adding the corresponding value from {@code values} to
     * the returned clause's params list.
     *
     * <p>If a token is enclosed in matching single or double quotes — optionally with
     * {@code %} LIKE-wildcard characters between the quotes and the token — the quotes
     * and wildcards are pulled out of the SQL and folded into the bound value so the
     * result uses PreparedStatement binding correctly:</p>
     * <pre>
     *   'name = ''${x}'''         value "O'Brien"  →  "name = ?"          bound "O'Brien"
     *   "name like '${x}%'"       value "John"     →  "name like ?"       bound "John%"
     *   "name like '%${x}%'"      value "ohn"      →  "name like ?"       bound "%ohn%"
     * </pre>
     *
     * <p>A null value in {@code values} is bound as an empty string to preserve the behaviour
     * of the legacy substitution-based method ({@code field = ''} rather than {@code field = NULL}).</p>
     *
     * @param template the WHERE-clause fragment template containing {@code ${var}} tokens
     * @param values   positional values to bind; the i-th {@code ${var}} occurrence uses the
     *                 i-th entry in this vector
     * @return a {@link ParameterizedClause} carrying the rewritten SQL and ordered params
     */
    public static ParameterizedClause getWhereValueClauseParameterized(String template, Vector values) {
        if (template == null || template.isEmpty()) {
            return ParameterizedClause.empty();
        }
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        int pos = 0;
        int valueIndex = 0;

        while (pos < template.length()) {
            int startIdx = template.indexOf("${", pos);
            if (startIdx < 0) {
                sql.append(template.substring(pos));
                break;
            }
            int endIdx = template.indexOf("}", startIdx);
            if (endIdx <= startIdx + 2) {
                // malformed or empty ${}: copy remainder verbatim, do not consume a value
                sql.append(template.substring(pos));
                break;
            }

            int replaceStart = startIdx;
            int replaceEnd = endIdx + 1;
            String likePrefix = "";
            String likeSuffix = "";

            // Look for a quoted region enclosing the token, possibly with % wildcards between
            // the quotes and the token. Walk left from ${ past any %, then look for ' or ";
            // walk right from } past any %, then look for a matching quote.
            int leftScan = replaceStart - 1;
            int leftPctCount = 0;
            while (leftScan >= pos && template.charAt(leftScan) == '%') {
                leftPctCount++;
                leftScan--;
            }
            if (leftScan >= pos) {
                char quoteChar = template.charAt(leftScan);
                if (quoteChar == '\'' || quoteChar == '"') {
                    int rightScan = replaceEnd;
                    int rightPctCount = 0;
                    while (rightScan < template.length() && template.charAt(rightScan) == '%') {
                        rightPctCount++;
                        rightScan++;
                    }
                    if (rightScan < template.length() && template.charAt(rightScan) == quoteChar) {
                        // matched quoted region: pull the quotes and any % into the bound value
                        replaceStart = leftScan;
                        replaceEnd = rightScan + 1;
                        if (leftPctCount > 0) {
                            likePrefix = repeat('%', leftPctCount);
                        }
                        if (rightPctCount > 0) {
                            likeSuffix = repeat('%', rightPctCount);
                        }
                    } else {
                        // Left quote with no matching right quote — emitting `?` here would place
                        // the placeholder inside a quoted literal, which JDBC treats as text rather
                        // than a bind slot. Fail loudly at parse time instead of producing SQL that
                        // blows up later with a confusing bind-count mismatch.
                        throw new IllegalArgumentException(
                                "Malformed template: ${...} preceded by "
                                        + quoteChar + " has no matching closing quote: " + template);
                    }
                }
            }

            sql.append(template, pos, replaceStart);
            sql.append("?");

            Object raw = (valueIndex < values.size()) ? values.get(valueIndex) : null;
            String value = (raw == null) ? "" : raw.toString();
            params.add(likePrefix + value + likeSuffix);
            valueIndex++;
            pos = replaceEnd;
        }

        return new ParameterizedClause(sql.toString(), params);
    }

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    public static boolean isIncludeDemo(String value) {
        boolean ret = false;
        if (value.indexOf("demographic.") >= 0)
            ret = true;
        return ret;
    }

    // get ${var} vars inside the string
    public static Vector getVarVec(String value) {
        Vector ret = new Vector();
        
        // Quick exit - no templates possible
        if (!value.contains("${")) {
            return ret;
        }
        
        int pos = 0;
        while (pos < value.length()) {
            int startIdx = value.indexOf("${", pos);
            if (startIdx == -1) break;
            
            int endIdx = value.indexOf("}", startIdx + 2);
            if (endIdx == -1) break;
            
            String varName = value.substring(startIdx + 2, endIdx);
            if (!varName.isEmpty()) {
                ret.add(varName);
            }
            
            pos = endIdx + 1;
        }

        return ret;
    }

    // change date string
    public static String getDiffDateFormat(String strDate, String oDate, String nDate) throws Exception {
        String ret = strDate;
        if (strDate.length() >= oDate.length()) {
            Date a = (new SimpleDateFormat(oDate)).parse(strDate);
            ret = DateFormatUtils.format(a, nDate);
            //ret = DateFormatUtils.format(DateUtils.parseDate(strDate, new String[] { oDate }),
            // nDate);
        } else {
            MiscUtils.getLogger().debug(" getDate wrong!!!");
        }
        return ret;
    }

    /**
     * Runs {@code subQuery} with its bound params and collects the integer values from the
     * first column into a list.
     *
     * <p>Callers should use {@code SqlUtils.inClausePlaceholders(list.size())} + bound params
     * when composing the returned IDs into an outer {@code IN} clause, rather than
     * concatenating them as a comma-separated string.</p>
     */
    public List<Integer> getRltSubQueryIds(ParameterizedClause subQuery) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (ResultSet rs = DBHandler.GetPreSQL(subQuery.sql(), subQuery.params().toArray())) {
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    // from formBCAR, demographic
    public Vector query(String sql, Vector vecFieldName, Object... params) throws SQLException {
        Vector ret = new Vector();
        Properties prop = null;

        ResultSet rs = DBHandler.GetPreSQL(sql, params);
        while (rs.next()) {
            prop = new Properties();
            for (int i = 0; i < vecFieldName.size(); i++) {
                try {
                    // Misc.getString returns "" for SQL NULL — don't re-add a null check.
                    prop.setProperty((String) vecFieldName.get(i),
                            Misc.getString(rs, (String) vecFieldName.get(i)));
                } catch (SQLException e) {
                    prop.setProperty((String) vecFieldName.get(i), "" + rs.getInt((String) vecFieldName.get(i)));
                }
            }
            ret.add(prop);
        }
        rs.close();
        return ret;
    }

}
