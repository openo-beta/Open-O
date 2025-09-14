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

package ca.openosp.openo.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Data model for manual CDS (Clinical Data Store) report line entries in Ontario mental health reporting.
 * This class represents a single row of CDS data that can be manually entered by users when automated
 * data collection is not available or needs supplementation. It supports both data capture from HTTP
 * requests and HTML form generation for data entry interfaces.
 *
 * <p>The data structure follows Ontario's CDS 4.0 reporting format:</p>
 * <ul>
 *   <li>Multiple admissions column for clients with multiple program episodes</li>
 *   <li>11 cohort buckets (0-10) representing years of service duration</li>
 *   <li>Integration with automated reporting for data validation</li>
 * </ul>
 *
 * <p>Manual entry is typically used for:</p>
 * <ul>
 *   <li>Data corrections and adjustments to automated reports</li>
 *   <li>Supplementing missing data not captured by electronic systems</li>
 *   <li>Override values for specific reporting requirements</li>
 *   <li>Historical data entry for periods before electronic data collection</li>
 * </ul>
 *
 * @since December 9, 2009
 * @see Cds4ReportUIBean
 */
public class CdsManualLineEntry {
    private static final Logger logger = MiscUtils.getLogger();

    /** Count for clients with multiple program admissions */
    public int multipleAdmissions;

    /** Count for cohort 0 (less than 1 year of service) */
    public int cohort0;

    /** Count for cohort 1 (1-2 years of service) */
    public int cohort1;

    /** Count for cohort 2 (2-3 years of service) */
    public int cohort2;

    /** Count for cohort 3 (3-4 years of service) */
    public int cohort3;

    /** Count for cohort 4 (4-5 years of service) */
    public int cohort4;

    /** Count for cohort 5 (5-6 years of service) */
    public int cohort5;

    /** Count for cohort 6 (6-7 years of service) */
    public int cohort6;

    /** Count for cohort 7 (7-8 years of service) */
    public int cohort7;

    /** Count for cohort 8 (8-9 years of service) */
    public int cohort8;

    /** Count for cohort 9 (9-10 years of service) */
    public int cohort9;

    /** Count for cohort 10 (10+ years of service) */
    public int cohort10;

    /**
     * Creates a CdsManualLineEntry from HTTP request parameters following CDS naming conventions.
     * This method parses form data submitted from manual CDS entry interfaces, extracting values
     * for multiple admissions and all cohort buckets for a specific CDS section.
     *
     * <p>Expected parameter naming convention:</p>
     * <ul>
     *   <li>{section}.ma - multiple admissions count</li>
     *   <li>{section}.c0 through {section}.c10 - cohort bucket counts</li>
     * </ul>
     *
     * <p>Example: For section "007-02", expects parameters:</p>
     * <ul>
     *   <li>007-02.ma (multiple admissions)</li>
     *   <li>007-02.c0, 007-02.c1, ..., 007-02.c10 (cohort buckets)</li>
     * </ul>
     *
     * @param request HttpServletRequest containing the CDS manual entry form data
     * @param section String the CDS section identifier (e.g., "007-02", "008-01")
     * @return CdsManualLineEntry populated with values from the request, defaulting to 0 for invalid/missing values
     */
    public static CdsManualLineEntry getCdsManualLineEntry(HttpServletRequest request, String section) {
        CdsManualLineEntry cdsManualLineEntry = new CdsManualLineEntry();

        cdsManualLineEntry.multipleAdmissions = getParameterDefault0(request, section + ".ma");
        cdsManualLineEntry.cohort0 = getParameterDefault0(request, section + ".c0");
        cdsManualLineEntry.cohort1 = getParameterDefault0(request, section + ".c1");
        cdsManualLineEntry.cohort2 = getParameterDefault0(request, section + ".c2");
        cdsManualLineEntry.cohort3 = getParameterDefault0(request, section + ".c3");
        cdsManualLineEntry.cohort4 = getParameterDefault0(request, section + ".c4");
        cdsManualLineEntry.cohort5 = getParameterDefault0(request, section + ".c5");
        cdsManualLineEntry.cohort6 = getParameterDefault0(request, section + ".c6");
        cdsManualLineEntry.cohort7 = getParameterDefault0(request, section + ".c7");
        cdsManualLineEntry.cohort8 = getParameterDefault0(request, section + ".c8");
        cdsManualLineEntry.cohort9 = getParameterDefault0(request, section + ".c9");
        cdsManualLineEntry.cohort10 = getParameterDefault0(request, section + ".c10");

        return (cdsManualLineEntry);
    }

    /**
     * Safely extracts an integer parameter from the HTTP request, defaulting to 0 for invalid values.
     * This method provides robust parameter parsing for CDS manual entry forms, handling
     * missing, empty, or non-numeric input gracefully.
     *
     * @param request HttpServletRequest containing the parameter
     * @param parameterName String the name of the parameter to extract
     * @return int the parsed integer value, or 0 if parsing fails or parameter is missing
     */
    private static int getParameterDefault0(HttpServletRequest request, String parameterName) {
        String temp = request.getParameter(parameterName);
        temp = StringUtils.trimToNull(temp);

        try {
            return (Integer.parseInt(temp));
        } catch (Exception e) {
            logger.warn("Error in cds parameter entry, defaulting to 0", e);
            return (0);
        }
    }

    /**
     * Generates an HTML table for manual CDS data entry with input fields for all data columns.
     * This method creates a standardized data entry interface that matches Ontario's CDS reporting
     * format, providing input fields for multiple admissions and all cohort buckets.
     *
     * <p>The generated table structure includes:</p>
     * <ul>
     *   <li>Header row with column labels (Multiple Admissions, Cohort 0-10)</li>
     *   <li>Input row with text fields following CDS parameter naming conventions</li>
     *   <li>Proper field naming for integration with getCdsManualLineEntry()</li>
     * </ul>
     *
     * <p>Generated HTML structure:</p>
     * <pre>
     * &lt;table&gt;
     *   &lt;tr&gt;
     *     &lt;td&gt;Multiple Admissions&lt;/td&gt;
     *     &lt;td&gt;Cohort 0&lt;/td&gt;...&lt;td&gt;Cohort 10&lt;/td&gt;
     *   &lt;/tr&gt;
     *   &lt;tr&gt;
     *     &lt;td&gt;&lt;input name="{section}.ma"/&gt;&lt;/td&gt;
     *     &lt;td&gt;&lt;input name="{section}.c0"/&gt;&lt;/td&gt;...&lt;td&gt;&lt;input name="{section}.c10"/&gt;&lt;/td&gt;
     *   &lt;/tr&gt;
     * &lt;/table&gt;
     * </pre>
     *
     * @param section String the CDS section identifier to use for field naming (e.g., "007-02")
     * @return String HTML table markup for CDS manual data entry
     */
    public static String outputCdsManualLineEntryTable(String section) {

        StringBuilder sb = new StringBuilder();

        // Build table structure with header row
        sb.append("<table>");
        sb.append("<tr>");

        // Add column headers
        sb.append("<td>Multiple Admissions</td>");
        for (int i = 0; i <= 10; i++) {
            sb.append("<td>Cohort " + i + "</td>");
        }

        // Add input row
        sb.append("</tr>");
        sb.append("<tr>");

        // Multiple admissions input field
        sb.append("<td><input type=\"text\" name=\"" + section + ".ma\" /></td>");

        // Cohort bucket input fields (c0 through c10)
        for (int i = 0; i <= 10; i++) {
            sb.append("<td><input type=\"text\" name=\"" + section + ".c" + i + "\" /></td>");
        }

        sb.append("</tr>");
        sb.append("</table>");

        return (sb.toString());
    }
}
