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


package ca.openosp.openo.eform;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ca.openosp.openo.utility.MiscUtils;

import ca.openosp.openo.eform.data.DatabaseAP;
import ca.openosp.openo.util.PreparedSQL;

/**
 * @author jay
 */
public class APExecute {

    /**
     * Creates a new instance of APExecute
     */
    public APExecute() {
    }

    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String execute(String ap, String demographicNo) {
        EFormLoader.getInstance();
        DatabaseAP dap = EFormLoader.getAP(ap);

        if (dap == null) {
            MiscUtils.getLogger().error("DatabaseAP not found for ap: " + ap);
            return "";
        }

        PreparedSQL prepared = parameterizeTemplate(dap.getApSQL(), "demographic", demographicNo);
        String sql = prepared.getSql();
        String output = dap.getApOutput();
        MiscUtils.getLogger().debug("SQL----" + sql);
        ArrayList<String> names = DatabaseAP.parserGetNames(output);
        sql = DatabaseAP.parserClean(sql);

        if (dap.isJsonOutput()) {
            ArrayNode values = EFormUtil.getJsonValues(names, sql, prepared.getParamsArray());
            output = values.toString();
        } else {
            ArrayList<String> values = EFormUtil.getValues(names, sql, prepared.getParamsArray());
            if (values.size() != names.size()) {
                output = "";
            } else {
                for (int i = 0; i < names.size(); i++) {
                    output = DatabaseAP.parserReplace(names.get(i), values.get(i), output);
                }
            }
        }
        return output;
    }

    public String execute(String ap, String demographicNo, Integer invoiceNo) {
        EFormLoader.getInstance();
        DatabaseAP dap = EFormLoader.getAP(ap);

        if (dap == null) {
            MiscUtils.getLogger().error("DatabaseAP not found for ap: " + ap);
            return "";
        }

        MiscUtils.getLogger().debug("AP:" + ap);
        List<Object> params = new ArrayList<>();
        String sql = dap.getApSQL();
        sql = parameterizeToken(sql, "invoiceNo", String.valueOf(invoiceNo), params);
        sql = parameterizeToken(sql, "demographic", demographicNo, params);

        String output = dap.getApOutput();
        MiscUtils.getLogger().debug("SQL----" + sql);

        ArrayList<String> names = DatabaseAP.parserGetNames(output);
        sql = DatabaseAP.parserClean(sql);

        ArrayList<String> values = EFormUtil.getValues(names, sql, params.toArray());
        if (values.size() != names.size()) {
            output = "";
        } else {
            for (int i = 0; i < names.size(); i++) {
                output = DatabaseAP.parserReplace(names.get(i), values.get(i), output);
            }
        }

        return output;
    }

    /**
     * Parameterizes a single ${name} token in a SQL template.
     * Replaces all occurrences of ${name} with ? and collects values for PreparedStatement binding.
     */
    private static String parameterizeToken(String sql, String name, String value, List<Object> params) {
        String token = "${" + name + "}";
        int idx;
        while ((idx = sql.indexOf(token)) >= 0) {
            sql = sql.substring(0, idx) + "?" + sql.substring(idx + token.length());
            params.add(value);
        }
        return sql;
    }

    /**
     * Convenience method to parameterize a template with a single named variable.
     */
    private static PreparedSQL parameterizeTemplate(String sql, String name, String value) {
        List<Object> params = new ArrayList<>();
        sql = parameterizeToken(sql, name, value, params);
        return new PreparedSQL(sql, params);
    }
}
