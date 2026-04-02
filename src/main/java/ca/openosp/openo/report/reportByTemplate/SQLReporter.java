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


package ca.openosp.openo.report.reportByTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ca.openosp.openo.util.PreparedSQL;
import ca.openosp.openo.utility.MiscUtils;

import ca.openosp.openo.db.DBHandler;
import ca.openosp.openo.report.data.RptResultStruct;
import ca.openosp.openo.util.UtilMisc;

import com.Ostermiller.util.CSVPrinter;


/**
 * @author rjonasz
 */
public class SQLReporter implements Reporter {

    /**
     * Creates a new instance of SQLReporter
     */
    public SQLReporter() {
    }

    public boolean generateReport(HttpServletRequest request) {
        String templateId = request.getParameter("templateId");
        // Validate templateId is numeric to prevent injection into template lookup
        if (templateId == null || !templateId.matches("^[0-9]+$")) {
            request.setAttribute("errormsg", "Error: Invalid template ID.");
            return false;
        }
        ReportObject curReport = (new ReportManager()).getReportTemplateNoParam(templateId);
        Map parameterMap = request.getParameterMap();

        if (curReport.isSequence()) {
            return generateSequencedReport(request);
        }

        PreparedSQL prepared = curReport.getParameterizedSQL(parameterMap);
        if (prepared == null || prepared.isMissingParams()) {
            request.setAttribute("errormsg", "Error: Cannot find all parameters for the query.  Check the template.");
            request.setAttribute("templateid", templateId);
            return false;
        }
        String sql = prepared.getSql();
        if (sql == null || sql.trim().isEmpty()) {
            request.setAttribute("errormsg", "Error: Cannot find all parameters for the query.  Check the template.");
            request.setAttribute("templateid", templateId);
            return false;
        }

        String rsHtml = "An SQL query error has occured ";
        String csv = "";
        try( StringWriter swr = new StringWriter();
             ResultSet rs = DBHandler.GetPreSQL(sql, prepared.getParamsArray()) ) {
            if (!rs.isBeforeFirst()) {
                rsHtml = "The query returned no results.";
            } else {
                rsHtml = RptResultStruct.getStructure2(rs);  //makes html from the result set
                CSVPrinter csvp = new CSVPrinter(swr);
                csvp.writeln(UtilMisc.getArrayFromResultSet(rs));
                csv = swr.toString();
            }
        } catch (SQLException sqe) {
            rsHtml += sqe.getCause() != null ? sqe.getCause() : sqe.getMessage();
            MiscUtils.getLogger().error("Error", sqe);
        } catch (IOException e) {
            rsHtml = "Error: during creation of CSV : " + (e.getCause() != null ? e.getCause() : e.getMessage());
            MiscUtils.getLogger().error("Error", e);
        }

        request.getSession().setAttribute("csv", csv);
        request.setAttribute("csv", csv);
        request.setAttribute("sql", sql);
        request.setAttribute("reportobject", curReport);
        request.setAttribute("resultsethtml", rsHtml);

        return true;
    }

    public boolean generateSequencedReport(HttpServletRequest request) {
        String templateId = request.getParameter("templateId");
        // Validate templateId is numeric to prevent injection into template lookup
        if (templateId == null || !templateId.matches("^[0-9]+$")) {
            request.setAttribute("errormsg", "Error: Invalid template ID.");
            return false;
        }
        ReportObject curReport = (new ReportManager()).getReportTemplateNoParam(templateId);
        Map parameterMap = request.getParameterMap();

        int x = 0;
        PreparedSQL prepared = null;
        while ((prepared = curReport.getParameterizedSQL(x, parameterMap)) != null) {
            if (prepared.isMissingParams()) {
                request.setAttribute("errormsg", "Error: Cannot find all parameters for the query.  Check the template.");
                request.setAttribute("templateid", templateId);
                return false;
            }

            String reportSql = prepared.getSql();
            String rsHtml = "An SQL query error has occured ";
            String csv = "";
            try( StringWriter swr = new StringWriter();
                ResultSet rs = DBHandler.GetPreSQL(reportSql, prepared.getParamsArray()) ) {
                if (!rs.isBeforeFirst()) {
                    rsHtml = "The query returned no results.";
                } else {
                    rsHtml = RptResultStruct.getStructure2(rs);  //makes html from the result set
                    CSVPrinter csvp = new CSVPrinter(swr);
                    csvp.writeln(UtilMisc.getArrayFromResultSet(rs));
                    csv = swr.toString();
                }
            } catch (SQLException sqe) {
                rsHtml += sqe.getCause() != null ? sqe.getCause() : sqe.getMessage();
                MiscUtils.getLogger().error("Error", sqe);
            } catch (IOException e) {
                rsHtml = "Error: during creation of CSV : " + (e.getCause() != null ? e.getCause() : e.getMessage());
                MiscUtils.getLogger().error("Error", e);
            }

            setSequencedAttribute(request, "csv", x, csv);
            setSequencedAttribute(request, "sql", x, reportSql);
            setSequencedAttribute(request, "resultsethtml", x, rsHtml);
            x++;
        }

        request.setAttribute("sequenceLength", x);
        request.setAttribute("reportobject", curReport);

        return true;
    }

    private void setSequencedAttribute(HttpServletRequest request, String prefix, int index, String value) {
        // Attribute key naming: prefix-0, prefix-1, etc.
        request.setAttribute(prefix.concat("-").concat(Integer.toString(index)), value);
    }

}