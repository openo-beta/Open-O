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

package ca.openosp.openo.report.reportByTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds a parameterized SQL statement and its bind parameter values.
 * Used by the report template engine to separate SQL structure from user-supplied values,
 * enabling safe execution via PreparedStatement.
 */
public class PreparedSQL {

    /** Sentinel indicating that required template parameters were missing. */
    public static final PreparedSQL MISSING_PARAMS = new PreparedSQL("", Collections.emptyList());

    private final String sql;
    private final List<String> params;

    public PreparedSQL(String sql, List<String> params) {
        this.sql = sql;
        this.params = params != null ? Collections.unmodifiableList(new ArrayList<>(params)) : Collections.emptyList();
    }

    public String getSql() {
        return sql;
    }

    public List<String> getParams() {
        return params;
    }

    public Object[] getParamsArray() {
        return params.toArray();
    }

    public boolean isMissingParams() {
        return this == MISSING_PARAMS;
    }

    public boolean isNullOrEmpty() {
        return sql == null || sql.trim().isEmpty();
    }
}
