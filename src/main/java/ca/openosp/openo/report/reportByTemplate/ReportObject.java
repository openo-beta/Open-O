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
//This is an interface for classes representing each template 


package ca.openosp.openo.report.reportByTemplate;

import java.util.ArrayList;
import java.util.Map;

public interface ReportObject {
    public String getTemplateId();

    public String getTitle();

    public String getDescription();

    public String getType();

    public ArrayList getParameters();

    public String getPreparedSQL(Map parameters);

    /**
     * Builds a parameterized SQL statement from the report template, replacing {param} markers
     * with ? placeholders and collecting user-supplied values as bind parameters.
     *
     * @param parameters request parameter map from the user
     * @return PreparedSQL with the parameterized query and bind values, or
     *         PreparedSQL.MISSING_PARAMS if a required template parameter is absent
     */
    public PreparedSQL getParameterizedSQL(Map parameters);

    public int getActive();

    public boolean isSequence();

    public String getPreparedSQL(int sequenceNo, Map parameters);

    /**
     * Sequenced variant of {@link #getParameterizedSQL(Map)}.
     *
     * @param sequenceNo zero-based index of the SQL statement in a semicolon-delimited template
     * @param parameters request parameter map
     * @return PreparedSQL for the given sequence, or null if sequenceNo is past the end
     */
    public PreparedSQL getParameterizedSQL(int sequenceNo, Map parameters);

    public String getUuid();

    public void setUuid(String uuid);
}
