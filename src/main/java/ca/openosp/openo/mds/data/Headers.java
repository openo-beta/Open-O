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


package ca.openosp.openo.mds.data;

import java.util.ArrayList;

/**
 * Represents laboratory report section headers and their associated grouped results.
 * <p>
 * This class encapsulates the organizational structure of MDS (Medical Data Systems) laboratory
 * reports, where results are grouped into logical sections with descriptive headers. Each Headers
 * instance represents a distinct section of a laboratory report (e.g., "Hematology", "Chemistry",
 * "Microbiology") and contains all the grouped results that belong to that section.
 * <p>
 * Key features include:
 * <ul>
 * <li>Report section identification via flags and sequence numbers</li>
 * <li>Multi-level heading support for complex report structures</li>
 * <li>Collection of grouped results organized by observation requests</li>
 * <li>Integration with HL7 message structure for Canadian laboratory systems</li>
 * </ul>
 * This class is essential for maintaining the hierarchical organization of laboratory data
 * as it appears in printed reports and clinical displays.
 *
 * @since February 4, 2004
 */
public class Headers {

    /**
     * Constructs a Headers instance for organizing laboratory report sections.
     * <p>
     * This constructor initializes a report section header with its identification
     * parameters and prepares the collection for grouped results. The constructor
     * sets up the hierarchical structure needed for proper report organization
     * and display.
     *
     * @param rF String the report flag identifier for this section
     * @param rS String the report sequence number for ordering sections
     * @param rH String[] array of heading text strings for multi-level headings (can be null)
     */
    Headers(String rF, String rS, String[] rH) {
        // Initialize collection for grouped laboratory results
        groupedReportsArray = new ArrayList<GroupedReports>();
        reportFlag = rF;
        reportSequence = rS;
        reportHeading = rH;
    }

    /** Report flag identifier used to categorize this section (e.g., discipline or test type) */
    public String reportFlag;
    /** Sequence number for ordering report sections in proper display order */
    public String reportSequence;
    /** Array of heading text strings supporting multi-level hierarchical headings */
    public String[] reportHeading;
    /** Collection of grouped laboratory results that belong to this report section */
    public ArrayList<GroupedReports> groupedReportsArray;
}
