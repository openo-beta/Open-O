//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.eform.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.eform.EFormLoader;
import ca.openosp.openo.eform.EFormUtil;
import ca.openosp.openo.eform.data.DatabaseAP;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public final class FetchUpdatedData2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private ObjectMapper objectMapper = new ObjectMapper();
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public String execute() throws IOException {
        return ajaxFetchData();
    }

    public String ajaxFetchData() throws IOException {
        String demographic = request.getParameter("demographic");
        String provider = request.getParameter("providers");
        String uuid = request.getParameter("uuid");
        String fields = request.getParameter("fields");

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_eform", "r", null)) {
            throw new SecurityException("missing required sec object (_eform)");
        }

        // Validate inputs to prevent SQL injection via DatabaseAP template substitution.
        // These values are substituted directly into SQL templates; they must be safe.
        if (demographic != null && !demographic.matches("^[0-9]+$")) {
            throw new SecurityException("Invalid demographic parameter");
        }
        if (provider != null && !provider.matches("^[a-zA-Z0-9_,]+$")) {
            throw new SecurityException("Invalid provider parameter");
        }
        if (uuid != null && !uuid.matches("^[a-fA-F0-9\\-]+$")) {
            throw new SecurityException("Invalid uuid parameter");
        }

        HashMap<String, String> outValues = new HashMap<String, String>();

        if (fields != null) {
            List<String> oscarUpdateFields = Arrays.asList(fields.split(","));

            for (String field : oscarUpdateFields) {
                DatabaseAP ap = EFormLoader.getAP(field);
                if (ap != null) {
                    String sql = ap.getApSQL();
                    String output = ap.getApOutput();
                    //replace ${demographic} with demogrpahicNo
                    if (sql != null) {
                        // Parameterize template variables instead of string substitution
                        List<Object> params = new ArrayList<>();
                        sql = parameterizeToken(sql, "demographic", demographic, params);
                        sql = parameterizeToken(sql, "providers", provider, params);
                        sql = parameterizeToken(sql, "uuid", uuid, params);

                        ArrayList<String> names = DatabaseAP.parserGetNames(output); //a list of ${apName} --> apName
                        sql = DatabaseAP.parserClean(sql);  //replaces all other ${apName} expressions with 'apName'
                        Object[] paramArray = params.toArray();

                        if (ap.isJsonOutput()) {
                            ArrayNode values = EFormUtil.getJsonValues(names, sql, paramArray);
                            output = values.toString(); //in case of JsonOutput, return the whole ArrayNode and let the javascript deal with it
                        } else {
                            ArrayList<String> values = EFormUtil.getValues(names, sql, paramArray);
                            if (values.size() != names.size()) {
                                output = "";
                            } else {
                                for (int i = 0; i < names.size(); i++) {
                                    output = DatabaseAP.parserReplace(names.get(i), values.get(i), output);
                                }
                            }
                        }
                        outValues.put(field, output);
                    }
                }
            }
        }

        ObjectNode json = objectMapper.valueToTree(outValues);

        response.getOutputStream().write(json.toString().getBytes());

        return null;
    }

    /**
     * Replaces all occurrences of ${name} in sql with ? and adds value to params list.
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
}