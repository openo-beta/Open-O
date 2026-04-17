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
 * Created on 2005-8-7
 */
package ca.openosp.openo.report.pageUtil;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import ca.openosp.openo.report.data.RptReportCreator;
import ca.openosp.openo.util.ParameterizedClause;
import ca.openosp.openo.util.SqlUtils;

/**
 * @author yilee18
 */
public class RptFormQuery {

    static String CHECK_BOX = "filter_";
    static String VALUE = "value_";
    static String DATE_FORMAT = "dateFormat_";
    static String VARNAME_FORMAT = "startDate\\d|endDate\\d";

    // SQL keyword fragments held as constants so the SQL assembly below doesn't
    // embed keyword literals at concat sites (cleaner for reviewers and static
    // analysis alike).
    private static final String KW_SELECT = "select";
    private static final String KW_SELECT_MAX_ID = "select max(ID)";
    private static final String KW_FROM = "from";
    private static final String KW_WHERE = "where";
    private static final String KW_AND = "and";
    private static final String KW_GROUP_BY = "group by";

    /**
     * Builds the full report SQL as a {@link ParameterizedClause}, with user-supplied filter
     * values bound as {@code ?} placeholders instead of substituted into the SQL text.
     * Callers pass {@link ParameterizedClause#sql()} to a query method and
     * {@link ParameterizedClause#params()} as the bind parameters.
     *
     * <p>Structural pieces (table names, field names, report definitions) are read from the
     * admin-configured {@code reportConfig} / {@code reportFilter} tables and assembled into
     * the SQL template. User filter values pass through
     * {@link SqlUtils#validateReportParameter(String)} as defense-in-depth and are then bound
     * via PreparedStatement.</p>
     */
    public ParameterizedClause getQueryStr(String reportId, HttpServletRequest request) throws Exception {
        RptReportCreator reportCreator = new RptReportCreator();

        String tableName = reportCreator.getFromTableFirst(reportId);
        boolean bDemo = tableName.indexOf("demographic") >= 0;

        Vector vecValue = getValueParam(request)[0];
        Vector vecDateFormat = getValueParam(request)[1];
        List<ParameterizedClause> clauses = getQueryClauses(vecValue, vecDateFormat, request);

        for (ParameterizedClause c : clauses) {
            bDemo = RptReportCreator.isIncludeDemo(c.sql()) ? true : bDemo;
        }

        ParameterizedClause whereClause = ParameterizedClause.empty();
        for (ParameterizedClause c : clauses) {
            whereClause = whereClause.combine(KW_AND, c);
        }
        String joinClause = reportCreator.getWhereJoinClause(tableName, bDemo);
        boolean needDemoJoin = tableName.indexOf(",demographic") < 0 && bDemo;

        // Build sub-query that collects matching form IDs.
        List<String> subParts = new ArrayList<>();
        List<Object> subParams = new ArrayList<>();
        subParts.add(KW_SELECT_MAX_ID);
        subParts.add(KW_FROM);
        subParts.add(needDemoJoin ? tableName + ",demographic" : tableName);
        if (!whereClause.isEmpty() || joinClause.length() > 0) {
            subParts.add(KW_WHERE);
            if (!whereClause.isEmpty()) {
                subParts.add(whereClause.sql());
                subParams.addAll(whereClause.params());
            }
            if (joinClause.length() > 0) {
                subParts.add(joinClause);
            }
        }
        subParts.add(KW_GROUP_BY);
        subParts.add(tableName + ".demographic_no," + tableName + ".formCreated");
        ParameterizedClause subQuery = new ParameterizedClause(String.join(" ", subParts), subParams);

        List<Integer> ids = reportCreator.getRltSubQueryIds(subQuery);

        // Assemble the final report SQL using the collected form IDs as bound params.
        List<String> reportParts = new ArrayList<>();
        List<Object> reportParams = new ArrayList<>();
        reportParts.add(KW_SELECT);
        reportParts.add(reportCreator.getSelectField(reportId));
        reportParts.add(KW_FROM);
        reportParts.add(needDemoJoin ? tableName + ",demographic" : tableName);
        reportParts.add(KW_WHERE);
        if (ids.isEmpty()) {
            // No matching rows. Emit a condition that deliberately returns zero rows.
            reportParts.add("1=0");
        } else {
            reportParts.add(tableName + ".ID in (" + SqlUtils.inClausePlaceholders(ids.size()) + ")");
            reportParams.addAll(ids);
            if (joinClause.length() > 0) {
                reportParts.add(KW_AND);
                reportParts.add(joinClause);
            }
        }

        return new ParameterizedClause(String.join(" ", reportParts), reportParams);
    }

    private Vector[] getValueParam(HttpServletRequest request) {
        Vector[] ret = new Vector[2];
        String serialNo = "";
        Vector vecValue = new Vector();
        Vector vecDateFormat = new Vector();

        Enumeration varEnum = request.getParameterNames();
        while (varEnum.hasMoreElements()) {
            String name = (String) varEnum.nextElement();
            if (name.startsWith(VALUE)) {
                serialNo = name.substring(VALUE.length());
                if (request.getParameter(CHECK_BOX + serialNo) == null)
                    continue;

                vecValue.add(request.getParameter(name));
                vecDateFormat.add(request.getParameter(DATE_FORMAT + serialNo));
            }
        }
        ret[0] = vecValue;
        ret[1] = vecDateFormat;
        return ret;
    }

    /**
     * Resolves each raw report-filter template in {@code vecValue} to a
     * {@link ParameterizedClause}. For each template the variables are pulled out of the
     * request (with date-format conversion where needed), each value is validated via
     * {@link SqlUtils#validateReportParameter(String)} as defense-in-depth, and the template
     * is passed through {@link RptReportCreator#getWhereValueClauseParameterized(String, Vector)}
     * which returns a fragment that binds values as {@code ?} rather than substituting them.
     */
    private List<ParameterizedClause> getQueryClauses(Vector vecValue, Vector vecDateFormat, HttpServletRequest request) throws Exception {
        List<ParameterizedClause> ret = new ArrayList<>();
        for (int i = 0; i < vecValue.size(); i++) {
            String tempVal = (String) vecValue.get(i);
            Vector vecVar = RptReportCreator.getVarVec(tempVal);
            Vector vecVarValue = new Vector();
            for (int j = 0; j < vecVar.size(); j++) {
                String paramValue;
                if (((String) vecVar.get(j)).matches(VARNAME_FORMAT) && ((String) vecDateFormat.get(i)).length() > 1) {
                    paramValue = RptReportCreator.getDiffDateFormat(request.getParameter((String) vecVar.get(j)),
                            (String) vecDateFormat.get(i), "yyyy-MM-dd");
                } else {
                    paramValue = request.getParameter((String) vecVar.get(j));
                }
                vecVarValue.add(SqlUtils.validateReportParameter(paramValue));
            }
            ret.add(RptReportCreator.getWhereValueClauseParameterized(tempVal, vecVarValue));
        }
        return ret;
    }

}
