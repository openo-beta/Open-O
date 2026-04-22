//CHECKSTYLE:OFF
/**
 * Copyright (c) 2006-. OSCARservice, OpenSoft System. All Rights Reserved.
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
 */

package ca.openosp.openo.report.pageUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

import ca.openosp.OscarProperties;
import ca.openosp.openo.db.DBHandler;
import ca.openosp.openo.util.ParameterizedClause;
import ca.openosp.openo.util.SqlUtils;
import ca.openosp.openo.report.data.RptReportConfigData;
import ca.openosp.openo.report.data.RptReportCreator;
import ca.openosp.openo.report.data.RptReportItem;

import com.Ostermiller.util.CSVPrinter;

public class RptDownloadCSVServlet extends HttpServlet {

    private static final Logger _logger = MiscUtils.getLogger();
    String reportName = "";
    String DELIMETER = "\t";

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null)
            return;
        String in = "";
        try {
            in = request.getParameter("demoReport") != null ? demoReport(request) : formReport(request);
        } catch (ServletException e1) {
            _logger.error("RptDownloadCSVServlet service() - form report");
        } catch (Exception e1) {
            _logger.error("RptDownloadCSVServlet service() - form report");
        }


        // Sanitize reportName for use in Content-Disposition header to prevent HTTP response splitting
        String sanitizedReportName = (reportName == null ? "report" : reportName).replaceAll("[\\r\\n]", "").replaceAll("[\\p{Cntrl}]", "");
        String filename = sanitizedReportName + ".csv"; // request.getParameter("filename");
        OutputStream out = null;
        try {
            if (in != null) {
                out = new BufferedOutputStream(response.getOutputStream());
                byte[] b = in.getBytes();
                int len = b.length;
                int n = 0;
                int FIXED_LEN = 2048;
                String contentType = "application/unknow";
                MiscUtils.getLogger().debug("contentType: " + contentType);
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                while (n <= len - FIXED_LEN) {
                    out.write(b, n, FIXED_LEN); // b.flush();
                    n += FIXED_LEN;
                }
                if (n > len - FIXED_LEN) {
                    out.write(b, n, len - n);
                }
            }
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (Exception e) {
                }
        }

    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    private String formReport(HttpServletRequest request) {


        String SAVE_AS = "default";
        String reportId = request.getParameter("id") != null ? request.getParameter("id") : "0";
        // Validate reportId is numeric to prevent SQL injection
        if (!SqlUtils.isNumericId(reportId)) {
            return "";
        }
        // get form name
        //String reportName = "";
        String in = "";
        try {
            reportName = (new RptReportItem()).getReportName(reportId);
            RptFormQuery formQuery = new RptFormQuery();
            ParameterizedClause reportQuery = formQuery.getQueryStr(reportId, request);

            RptReportConfigData formConfig = new RptReportConfigData();
            Vector[] vecField = formConfig.getAllFieldNameValue(SAVE_AS, reportId);
            Vector vecFieldCaption = vecField[1];

            // reportQuery carries user-supplied filter values as bound ? parameters; the SQL
            // text contains only admin-configured identifiers plus the fixed query structure.
            Vector vecFieldValue = (new RptReportCreator()).query(
                reportQuery.sql(), vecFieldCaption, reportQuery.params().toArray());

            StringWriter swr = new StringWriter();
            CSVPrinter csvp = new CSVPrinter(swr);
            csvp.changeDelimiter('\t');

            for (int i = 0; i < vecFieldCaption.size(); i++) {
                csvp.write((String) vecFieldCaption.get(i));
            }

            for (int i = 0; i < vecFieldValue.size(); i++) {
                Properties prop = (Properties) vecFieldValue.get(i);
                csvp.writeln();
                for (int j = 0; j < vecFieldCaption.size(); j++) {
                    csvp.write(prop.getProperty((String) vecFieldCaption.get(j), ""));
                }
            }
            in = swr.toString();


        } catch (Exception e1) {
            _logger.error("service() - form report");
        }
        return in;
    }

    private String demoReport(HttpServletRequest request) throws Exception {
        reportName = "clientDatabaseReport";
        String in = "";


        String ARTYPE = "formBCAR";
        if (request.getParameter("bcartype") != null && request.getParameter("bcartype").equals("BCAR2007")) {
            ARTYPE = "formBCAR2007";
        }

        MiscUtils.getLogger().debug("AR TYPE " + ARTYPE);


        Properties propDemoSelect = new Properties();
        Properties propSpecSelect = new Properties();
        Properties propARSelect = new Properties();
        propDemoSelect.setProperty("last_name", "Last Name");
        propDemoSelect.setProperty("first_name", "First Name");
        propDemoSelect.setProperty("date_joined", "Date Joined");
        propDemoSelect.setProperty("hin", "Health Ins.");
        propDemoSelect.setProperty("hc_type", "HC Type");
        propDemoSelect.setProperty("address", "Address");
        propDemoSelect.setProperty("city", "City");
        propDemoSelect.setProperty("postal", "Postal Code");
        propDemoSelect.setProperty("phone", "Phone (H)");
        propDemoSelect.setProperty("phone2", "Phone (W)");
        propDemoSelect.setProperty("email", "Email");
        Vector vecSeqDemoSelect = new Vector();
        vecSeqDemoSelect.add("last_name");
        vecSeqDemoSelect.add("first_name");
        vecSeqDemoSelect.add("date_joined");
        vecSeqDemoSelect.add("hin");
        vecSeqDemoSelect.add("hc_type");
        vecSeqDemoSelect.add("address");
        vecSeqDemoSelect.add("city");
        vecSeqDemoSelect.add("postal");
        vecSeqDemoSelect.add("phone");
        vecSeqDemoSelect.add("phone2");
        vecSeqDemoSelect.add("email");

        Vector vecSeqSpecSelect = new Vector();
        propSpecSelect.setProperty("prefer_language", "Preferred Language");
        OscarProperties oscarProps = OscarProperties.getInstance();
        if (oscarProps.getProperty("demographicExt") != null) {
            String[] propDemoExt = oscarProps.getProperty("demographicExt", "").split("\\|");
            for (int i = 0; i < propDemoExt.length; i++) {
                propSpecSelect.setProperty(propDemoExt[i].replace(' ', '_'), propDemoExt[i]);
                vecSeqSpecSelect.add(propDemoExt[i].replace(' ', '_'));
            }
        }

        propARSelect.setProperty("c_EDD", "EDD");
        propARSelect.setProperty("pg1_famPhy", "Family Physician");
        propARSelect.setProperty("pg1_partnerName", "Partner Name");
        Vector vecSeqARSelect = new Vector();
        vecSeqARSelect.add("c_EDD");
        vecSeqARSelect.add("ga");
        vecSeqARSelect.add("pg1_famPhy");
        vecSeqARSelect.add("pg1_partnerName");

        propARSelect.setProperty("ga", "GA Today");
        propARSelect.setProperty("b_primiparous", "Primiparous");

//        get selection
        boolean bDemoSelect = false;
        boolean bARSelect = false;
        boolean bSpecSelect = false;
        String sDemoSelect = "";
        String sSpecSelect = "";
        String sARSelect = "";


        String CHECK_BOX = "filter_";
        String VALUE = "value_";
        String DATE_FORMAT = "dateFormat_";
        String VARNAME_FORMAT = "startDate\\d|endDate\\d";
        Vector vecValue = new Vector();
        Vector vecDateFormat = new Vector();
        Properties propTempDemoSelect = new Properties();
        Properties propTempARSelect = new Properties();
        Properties propTempSpecSelect = new Properties();

        Enumeration varEnum = request.getParameterNames();
        while (varEnum.hasMoreElements()) {
            String name = (String) varEnum.nextElement();
            if (propDemoSelect.containsKey(name)) {
                bDemoSelect = true;
                propTempDemoSelect.setProperty(name, "");
            }
            if (propARSelect.containsKey(name)) {
                bARSelect = true;


                if (!name.equals("ga") && !name.equals("b_primiparous"))
                    sARSelect += (sARSelect.length() < 1 ? "" : ",") + ARTYPE + "." + name;
            }
            if (propSpecSelect.containsKey(name)) {
                bSpecSelect = true;
                sSpecSelect += (sSpecSelect.length() < 1 ? "" : ",") + "demographicExt." + name;
            }

            if (name.startsWith(VALUE)) {
                String serialNo = name.substring(VALUE.length());
                if (request.getParameter(CHECK_BOX + serialNo) == null)
                    continue;

                vecValue.add(request.getParameter(name));
                vecDateFormat.add(request.getParameter(DATE_FORMAT + serialNo));

            }
        }
//         get seq. select string
        for (int i = 0; i < vecSeqDemoSelect.size(); i++) {
            if (propTempDemoSelect.getProperty((String) vecSeqDemoSelect.get(i)) != null) {
                sDemoSelect += (sDemoSelect.length() < 1 ? "" : ",") + "demographic." + vecSeqDemoSelect.get(i);
            }
        }
        for (int i = 0; i < vecSeqARSelect.size(); i++) {
            if (propTempARSelect.getProperty((String) vecSeqARSelect.get(i)) != null) {
                sARSelect += (sARSelect.length() < 1 ? "" : ",") + ARTYPE + "." + vecSeqARSelect.get(i);
            }
        }
        for (int i = 0; i < vecSeqSpecSelect.size(); i++) {
            if (propTempSpecSelect.getProperty((String) vecSeqSpecSelect.get(i)) != null) {
                sSpecSelect += (sSpecSelect.length() < 1 ? "" : ",") + "demographicExt." + vecSeqSpecSelect.get(i);
            }
        }

        MiscUtils.getLogger().debug(":" + bDemoSelect + bSpecSelect + bARSelect);
        MiscUtils.getLogger().debug(":" + sDemoSelect + sSpecSelect + sARSelect);

//        get replaced filter
//         filling the var with the real date value
        boolean bDemoFilter = false;
        boolean bARFilter = false;
        boolean bSpecFilter = false;
        ParameterizedClause demoFilter = ParameterizedClause.empty();
        ParameterizedClause specFilter = ParameterizedClause.empty();
        ParameterizedClause arFilter = ParameterizedClause.empty();
        for (int i = 0; i < vecValue.size(); i++) {
            String tempVal = (String) vecValue.get(i);
            Vector vecVar = RptReportCreator.getVarVec(tempVal);
            Vector vecVarValue = new Vector();
            for (int j = 0; j < vecVar.size(); j++) {
                String paramValue;
                // conver date format if needed
                if (((String) vecVar.get(j)).matches(VARNAME_FORMAT) && ((String) vecDateFormat.get(i)).length() > 1) {
                    paramValue = RptReportCreator.getDiffDateFormat(request.getParameter((String) vecVar.get(j)),
                            (String) vecDateFormat.get(i), "yyyy-MM-dd");
                } else {
                    paramValue = request.getParameter((String) vecVar.get(j));
                }
                vecVarValue.add(paramValue);
            }
            ParameterizedClause strFilter = RptReportCreator.getWhereValueClauseParameterized(tempVal, vecVarValue);
            String filterSql = strFilter.sql();
            if (filterSql.indexOf("demographic.") >= 0) {
                bDemoFilter = true;
                demoFilter = demoFilter.and(strFilter);
            }
            if (filterSql.indexOf("demographicExt.") >= 0) {
                bSpecFilter = true;
                specFilter = specFilter.and(strFilter);
            }
            if (filterSql.indexOf(ARTYPE + ".") >= 0) {
                bARFilter = true;
                //"formBCAR.demographic_no in (select distinct demographic_no from formBCBirthSumMo)"
                if (filterSql.indexOf("formBCBirthSumMo") > 0) {
                    List<Integer> birthSumDemos = new ArrayList<>();
                    try (ResultSet rs = DBHandler.GetPreSQL("select distinct demographic_no from formBCBirthSumMo")) {
                        while (rs.next()) {
                            birthSumDemos.add(rs.getInt("demographic_no"));
                        }
                    }
                    if (birthSumDemos.isEmpty()) {
                        // No matching rows; emit a condition that always evaluates false.
                        strFilter = new ParameterizedClause("1=0", new ArrayList<>());
                    } else {
                        String inSql = ARTYPE + ".demographic_no in ("
                            + SqlUtils.inClausePlaceholders(birthSumDemos.size()) + ")";
                        strFilter = new ParameterizedClause(inSql, new ArrayList<>(birthSumDemos));
                    }
                }

                arFilter = arFilter.and(strFilter);
            }
            MiscUtils.getLogger().debug(i + tempVal + " tempVal: " + vecVarValue);
        }

//        query sub
//        todo: filt out Delivered Clients
//         one table: demographic
        Vector vecFieldCaption = new Vector();
        Vector vecFieldName = new Vector();
        Vector vecFieldValue = new Vector();
        String ORDER_BY = " order by demographic.last_name, demographic.first_name";
        if (bDemoSelect && !bARSelect && !bSpecSelect && bDemoFilter && !bARFilter && !bSpecFilter) {
            String sql = String.join(" ", "select", sDemoSelect, "from demographic where",
                demoFilter.sql(), ORDER_BY);
            MiscUtils.getLogger().debug(" one table: demographic (parameterized)");
            String[] temp = sDemoSelect.replaceAll("demographic.", "").split(",");
            for (int i = 0; i < temp.length; i++) {
                vecFieldCaption.add(propDemoSelect.getProperty(temp[i].trim()));
                vecFieldName.add(temp[i].trim());
                MiscUtils.getLogger().debug(" vecFieldCaption: " + propDemoSelect.getProperty(temp[i].trim()));
            }
            vecFieldValue = (new RptReportCreator()).query(sql, vecFieldName, demoFilter.params().toArray());
        }

//         table: demographic and demographicExt
        Vector vecSpecCaption = new Vector();
        Properties propSpecValue = new Properties();
        if ((bDemoSelect && !bARSelect && bSpecSelect && !bARFilter) || (!bARFilter && bSpecFilter)) {
            if (bDemoSelect && !bARSelect && bSpecSelect && !bSpecFilter) {
                vecFieldName.add("demographic_no");
                // bDemoFilter is not required to enter this branch, so demoFilter may be empty;
                // only emit WHERE when there is an actual filter clause to emit.
                List<String> sqlParts = new ArrayList<>();
                sqlParts.add("select demographic_no,");
                sqlParts.add(sDemoSelect);
                sqlParts.add("from demographic");
                if (!demoFilter.isEmpty()) {
                    sqlParts.add("where");
                    sqlParts.add(demoFilter.sql());
                }
                sqlParts.add(ORDER_BY);
                String sql = String.join(" ", sqlParts);
                MiscUtils.getLogger().debug(" demographic and demographicExt (parameterized)");
                String[] temp = sDemoSelect.replaceAll("demographic.", "").split(",");
                for (int i = 0; i < temp.length; i++) {
                    vecFieldCaption.add(propDemoSelect.getProperty(temp[i].trim()));
                    vecFieldName.add(temp[i].trim());
                    MiscUtils.getLogger().debug(" vecFieldCaption: " + propDemoSelect.getProperty(temp[i].trim()));
                }
                vecFieldValue = (new RptReportCreator()).query(sql, vecFieldName, demoFilter.params().toArray());
                vecFieldName.remove(0); // remove "demographic_no"

                //get demographic_no
                List<Integer> demoNos = new ArrayList<>();
                for (int j = 0; j < vecFieldValue.size(); j++) {
                    Properties prop = (Properties) vecFieldValue.get(j);
                    demoNos.add(Integer.parseInt(prop.getProperty("demographic_no")));
                }
                temp = sSpecSelect.replaceAll("demographicExt.", "").split(",");
                for (int i = 0; i < temp.length; i++) {
                    vecSpecCaption.add(propSpecSelect.getProperty(temp[i].trim()));
                    if (!demoNos.isEmpty()) {
                        sql = "select demographic_no,value from demographicExt where key_val = ? and demographic_no in ("
                            .concat(SqlUtils.inClausePlaceholders(demoNos.size()))
                            .concat(") order by date_time desc limit 1");
                        List<Object> params = new ArrayList<>();
                        params.add(temp[i].trim());
                        params.addAll(demoNos);
                        ResultSet rs = DBHandler.GetPreSQL(sql, params.toArray());
                        while (rs.next()) {
                            propSpecValue.setProperty(rs.getString("demographic_no") + temp[i], rs.getString("value"));
                        }
                        rs.close();
                    }
                }
                MiscUtils.getLogger().debug(" demographic and demographicExt (parameterized)");
            }
            if (bSpecFilter) {
                vecFieldName.add("demographic_no");
                // get demoNo
                String sql = null;
                ResultSet rs = null;
                ParameterizedClause subWhere = demoFilter.and(specFilter);
                String subQuery = String.join(" ",
                    "select distinct(demographic.demographic_no) from demographicExt, demographic where demographic.demographic_no=demographicExt.demographic_no and",
                    subWhere.sql());
                MiscUtils.getLogger().debug(" demographic and demographicExt subQuery (parameterized)");
                List<Integer> subFormDemoNos = new ArrayList<>();
                rs = DBHandler.GetPreSQL(subQuery, subWhere.params().toArray());
                while (rs.next()) {
                    subFormDemoNos.add(rs.getInt("demographic.demographic_no"));
                }
                rs.close();
                // get value for spec
                String[] temp = sSpecSelect.replaceAll("demographicExt.", "").split(",");
                for (int i = 0; i < temp.length; i++) {
                    vecSpecCaption.add(propSpecSelect.getProperty(temp[i].trim()));
                    if (!subFormDemoNos.isEmpty()) {
                        sql = "select demographic_no,value from demographicExt where key_val = ? and demographic_no in ("
                            .concat(SqlUtils.inClausePlaceholders(subFormDemoNos.size()))
                            .concat(") order by date_time desc limit 1");
                        List<Object> params = new ArrayList<>();
                        params.add(temp[i].trim());
                        params.addAll(subFormDemoNos);
                        MiscUtils.getLogger().debug(" demographic and demographicExt (parameterized)");
                        rs = DBHandler.GetPreSQL(sql, params.toArray());
                        while (rs.next()) {
                            propSpecValue.setProperty(rs.getString("demographic_no") + temp[i], rs.getString("value"));
                        }
                        rs.close();
                    }
                }

                temp = sDemoSelect.replaceAll("demographic.", "").split(",");
                for (int i = 0; i < temp.length; i++) {
                    vecFieldCaption.add(propDemoSelect.getProperty(temp[i].trim()));
                    vecFieldName.add(temp[i].trim());
                    MiscUtils.getLogger().debug(" vecFieldCaption: " + propDemoSelect.getProperty(temp[i].trim()));
                }
                if (!subFormDemoNos.isEmpty()) {
                    sql = "select demographic.demographic_no,"
                        .concat(sDemoSelect)
                        .concat(" from demographic where demographic.demographic_no in (")
                        .concat(SqlUtils.inClausePlaceholders(subFormDemoNos.size()))
                        .concat(") ")
                        .concat(ORDER_BY);
                    MiscUtils.getLogger().debug(" demographic and demographicExt: (parameterized)");
                    vecFieldValue = (new RptReportCreator()).query(sql, vecFieldName, subFormDemoNos.toArray());
                }
                vecFieldName.remove(0); // remove "demographic_no"
            }
        }

//         table: demographic and formBCAR


        if ((bDemoSelect && bARSelect && !bSpecSelect && !bSpecFilter) || (!bSpecSelect && bARFilter && !bSpecFilter)) {
            ParameterizedClause subWhere = demoFilter.and(arFilter);
            List<String> subParts = new ArrayList<>();
            subParts.add("select max(ID) from");
            subParts.add(ARTYPE + ", demographic where demographic.demographic_no=" + ARTYPE + ".demographic_no");
            if (!subWhere.isEmpty()) {
                subParts.add("and");
                subParts.add(subWhere.sql());
            }
            subParts.add("group by");
            subParts.add(ARTYPE + ".demographic_no," + ARTYPE + ".formCreated");
            String subQuery = String.join(" ", subParts);
            MiscUtils.getLogger().debug(" demographic and " + ARTYPE + " subQuery (parameterized)");
            List<Integer> subFormIds = new ArrayList<>();
            ResultSet rs = DBHandler.GetPreSQL(subQuery, subWhere.params().toArray());
            while (rs.next()) {
                subFormIds.add(rs.getInt("max(ID)"));
            }
            rs.close();

            String arSelectSuffix = sARSelect.length() > 0 ? ("," + sARSelect) : "";

            String[] temp = sDemoSelect.replaceAll("demographic.", "").split(",");
            for (int i = 0; i < temp.length; i++) {
                vecFieldCaption.add(propDemoSelect.getProperty(temp[i].trim()));
                vecFieldName.add(temp[i].trim());
                MiscUtils.getLogger().debug(" vecFieldCaption: " + propDemoSelect.getProperty(temp[i].trim()));
            }
            if (bARSelect) {
                temp = sARSelect.replaceAll(ARTYPE + ".", "").split(",");
                for (int i = 0; i < temp.length; i++) {
                    vecFieldCaption.add(propARSelect.getProperty(temp[i].trim()));
                    vecFieldName.add(temp[i].trim());
                    MiscUtils.getLogger().debug(" vecFieldCaption: " + propARSelect.getProperty(temp[i].trim()));
                }
            }
            if (!subFormIds.isEmpty()) {
                String sql = "select demographic.demographic_no,"
                    .concat(sDemoSelect).concat(arSelectSuffix)
                    .concat(" from demographic,").concat(ARTYPE)
                    .concat(" where ").concat(ARTYPE).concat(".ID in (")
                    .concat(SqlUtils.inClausePlaceholders(subFormIds.size()))
                    .concat(") and demographic.demographic_no=").concat(ARTYPE).concat(".demographic_no ")
                    .concat(ORDER_BY);
                MiscUtils.getLogger().debug(" demographic and " + ARTYPE + " (parameterized)");
                vecFieldValue = (new RptReportCreator()).query(sql, vecFieldName, subFormIds.toArray());
            }

            //vecFieldName.remove(0); // remove "demographic_no"
        }

//         table: all
        if ((bDemoSelect && bARSelect && bSpecSelect) || (bARFilter && bSpecFilter)) {
            if (bDemoSelect && bARSelect && bSpecSelect && !bSpecFilter) {
                vecFieldName.add("demographic_no");
                ParameterizedClause subWhere = demoFilter.and(arFilter);
                List<String> subParts = new ArrayList<>();
                subParts.add("select max(ID) from");
                subParts.add(ARTYPE + ", demographic where demographic.demographic_no=" + ARTYPE + ".demographic_no");
                if (!subWhere.isEmpty()) {
                    subParts.add("and");
                    subParts.add(subWhere.sql());
                }
                subParts.add("group by");
                subParts.add(ARTYPE + ".demographic_no," + ARTYPE + ".formCreated");
                String subQuery = String.join(" ", subParts);
                MiscUtils.getLogger().debug(" demographic and " + ARTYPE + " subQuery (parameterized)");
                List<Integer> subFormIds = new ArrayList<>();
                ResultSet rs = DBHandler.GetPreSQL(subQuery, subWhere.params().toArray());
                while (rs.next()) {
                    subFormIds.add(rs.getInt("max(ID)"));
                }
                rs.close();

                String arSelectSuffix = sARSelect.length() > 0 ? ("," + sARSelect) : "";

                String[] temp = sDemoSelect.replaceAll("demographic.", "").split(",");
                for (int i = 0; i < temp.length; i++) {
                    vecFieldCaption.add(propDemoSelect.getProperty(temp[i].trim()));
                    vecFieldName.add(temp[i].trim());
                    MiscUtils.getLogger().debug(" vecFieldCaption: " + propDemoSelect.getProperty(temp[i].trim()));
                }
                if (bARSelect) {
                    temp = sARSelect.replaceAll(ARTYPE + ".", "").split(",");
                    for (int i = 0; i < temp.length; i++) {
                        vecFieldCaption.add(propARSelect.getProperty(temp[i].trim()));
                        vecFieldName.add(temp[i].trim());
                        MiscUtils.getLogger().debug(" vecFieldCaption: " + propARSelect.getProperty(temp[i].trim()));
                    }
                }
                if (!subFormIds.isEmpty()) {
                    String sql = "select demographic.demographic_no,"
                        .concat(sDemoSelect).concat(arSelectSuffix)
                        .concat(" from demographic,").concat(ARTYPE)
                        .concat(" where ").concat(ARTYPE).concat(".ID in (")
                        .concat(SqlUtils.inClausePlaceholders(subFormIds.size()))
                        .concat(") and demographic.demographic_no=").concat(ARTYPE).concat(".demographic_no ")
                        .concat(ORDER_BY);
                    MiscUtils.getLogger().debug(" demographic and " + ARTYPE + " (parameterized)");
                    vecFieldValue = (new RptReportCreator()).query(sql, vecFieldName, subFormIds.toArray());
                }
                vecFieldName.remove(0); // remove "demographic_no"

                //get demographic_no
                List<Integer> demoNos = new ArrayList<>();
                for (int j = 0; j < vecFieldValue.size(); j++) {
                    Properties prop = (Properties) vecFieldValue.get(j);
                    demoNos.add(Integer.parseInt(prop.getProperty("demographic_no")));
                }
                temp = sSpecSelect.replaceAll("demographicExt.", "").split(",");
                for (int i = 0; i < temp.length; i++) {
                    vecSpecCaption.add(propSpecSelect.getProperty(temp[i].trim()));
                    if (!demoNos.isEmpty()) {
                        String sql = "select demographic_no,value from demographicExt where key_val = ? and demographic_no in ("
                            .concat(SqlUtils.inClausePlaceholders(demoNos.size()))
                            .concat(") order by date_time");
                        List<Object> params = new ArrayList<>();
                        params.add(temp[i].trim());
                        params.addAll(demoNos);
                        rs = DBHandler.GetPreSQL(sql, params.toArray());
                        while (rs.next()) {
                            propSpecValue.setProperty(rs.getString("demographic_no") + temp[i], rs.getString("value"));
                        }
                        rs.close();
                    }
                }
            }
            MiscUtils.getLogger().debug(" table: all: ");

            if (bARFilter && bSpecFilter) {
                // spec first
                vecFieldName.add("demographic_no");
                // get demoNo
                String sql = null;
                ResultSet rs = null;
                ParameterizedClause specSubWhere = demoFilter.and(specFilter);
                String subQuery = String.join(" ",
                    "select distinct(demographic.demographic_no) from demographicExt, demographic where demographic.demographic_no=demographicExt.demographic_no and",
                    specSubWhere.sql());
                MiscUtils.getLogger().debug(" demographic and demographicExt subQuery (parameterized)");
                List<Integer> subFormDemoNos = new ArrayList<>();
                rs = DBHandler.GetPreSQL(subQuery, specSubWhere.params().toArray());
                while (rs.next()) {
                    subFormDemoNos.add(rs.getInt("demographic.demographic_no"));
                }
                rs.close();
                // get value for spec
                String[] temp = sSpecSelect.replaceAll("demographicExt.", "").split(",");
                for (int i = 0; i < temp.length; i++) {
                    vecSpecCaption.add(propSpecSelect.getProperty(temp[i].trim()));
                    if (!subFormDemoNos.isEmpty()) {
                        sql = "select demographic_no,value from demographicExt where key_val = ? and demographic_no in ("
                            .concat(SqlUtils.inClausePlaceholders(subFormDemoNos.size()))
                            .concat(") order by date_time desc limit 1");
                        List<Object> params = new ArrayList<>();
                        params.add(temp[i].trim());
                        params.addAll(subFormDemoNos);
                        rs = DBHandler.GetPreSQL(sql, params.toArray());
                        while (rs.next()) {
                            propSpecValue.setProperty(rs.getString("demographic_no") + temp[i], rs.getString("value"));
                        }
                        rs.close();
                    }
                }

                // formAR second
                ParameterizedClause arSubWhere = demoFilter.and(arFilter);
                List<String> arSubParts = new ArrayList<>();
                arSubParts.add("select max(ID) from");
                arSubParts.add(ARTYPE + ", demographic where demographic.demographic_no=" + ARTYPE + ".demographic_no");
                if (!arSubWhere.isEmpty()) {
                    arSubParts.add("and");
                    arSubParts.add(arSubWhere.sql());
                }
                arSubParts.add("group by");
                arSubParts.add(ARTYPE + ".demographic_no," + ARTYPE + ".formCreated");
                subQuery = String.join(" ", arSubParts);
                MiscUtils.getLogger().debug(" demographic and " + ARTYPE + " subQuery (parameterized)");
                List<Integer> subFormIds = new ArrayList<>();
                rs = DBHandler.GetPreSQL(subQuery, arSubWhere.params().toArray());
                while (rs.next()) {
                    subFormIds.add(rs.getInt("max(ID)"));
                }
                rs.close();

                // total
                String arSelectSuffix = sARSelect.length() > 0 ? ("," + sARSelect) : "";

                temp = sDemoSelect.replaceAll("demographic.", "").split(",");
                for (int i = 0; i < temp.length; i++) {
                    vecFieldCaption.add(propDemoSelect.getProperty(temp[i].trim()));
                    vecFieldName.add(temp[i].trim());
                    MiscUtils.getLogger().debug(" vecFieldCaption: " + propDemoSelect.getProperty(temp[i].trim()));
                }
                if (bARSelect) {
                    temp = sARSelect.replaceAll(ARTYPE + ".", "").split(",");
                    for (int i = 0; i < temp.length; i++) {
                        vecFieldCaption.add(propARSelect.getProperty(temp[i].trim()));
                        vecFieldName.add(temp[i].trim());
                        MiscUtils.getLogger().debug(" vecFieldCaption: " + propARSelect.getProperty(temp[i].trim()));
                    }
                }
                if (!subFormDemoNos.isEmpty() && !subFormIds.isEmpty()) {
                    sql = "select demographic.demographic_no,"
                        .concat(sDemoSelect).concat(arSelectSuffix)
                        .concat(" from demographic,").concat(ARTYPE)
                        .concat(" where demographic.demographic_no in (")
                        .concat(SqlUtils.inClausePlaceholders(subFormDemoNos.size()))
                        .concat(") and ").concat(ARTYPE).concat(".ID in (")
                        .concat(SqlUtils.inClausePlaceholders(subFormIds.size()))
                        .concat(") and demographic.demographic_no=").concat(ARTYPE).concat(".demographic_no ")
                        .concat(ORDER_BY);
                    MiscUtils.getLogger().debug(" total (parameterized)");
                    List<Object> totalParams = new ArrayList<>();
                    totalParams.addAll(subFormDemoNos);
                    totalParams.addAll(subFormIds);
                    vecFieldValue = (new RptReportCreator()).query(sql, vecFieldName, totalParams.toArray());
                }
                vecFieldName.remove(0); // remove "demographic_no"

            }
        }

        StringWriter swr = new StringWriter();
        CSVPrinter csvp = new CSVPrinter(swr);
        csvp.changeDelimiter('\t');

        csvp.write("id");
        for (int i = 0; i < vecFieldCaption.size(); i++) {
            csvp.write((String) vecFieldCaption.get(i));
        }
        if (bSpecSelect) {
            for (int i = 0; i < vecSpecCaption.size(); i++) {
                csvp.write((String) vecSpecCaption.get(i));
            }
        }

        for (int i = 0; i < vecFieldValue.size(); i++) {
            Properties prop = (Properties) vecFieldValue.get(i);
            csvp.writeln();
            csvp.write("" + (i + 1));

            for (int j = 0; j < vecFieldName.size(); j++) {
                csvp.write(prop.getProperty((String) vecFieldName.get(j), ""));
            }
            if (bSpecSelect) {
                String demoNo = prop.getProperty("demographic_no");
                for (int j = 0; j < vecSpecCaption.size(); j++) {
                    csvp.write(propSpecValue.getProperty(demoNo + ((String) vecSpecCaption.get(j)).replaceAll(" ", "_"), ""));
                }
            }
        }

        in = swr.toString();
        return in;
    }
}