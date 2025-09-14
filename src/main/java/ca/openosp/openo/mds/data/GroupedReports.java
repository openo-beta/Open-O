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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Represents a group of laboratory test results organized by observation request (OBR) segment.
 * <p>
 * This class encapsulates laboratory results that are grouped together under a single observation
 * request in the MDS (Medical Data Systems) HL7 message structure. Each GroupedReports instance
 * corresponds to a specific OBR segment and contains all related observation results (OBX segments)
 * along with timing information and result codes.
 * <p>
 * Key functionality includes:
 * <ul>
 * <li>Association with specific OBR (Observation Request) segments</li>
 * <li>HL7 timestamp parsing and formatting for display</li>
 * <li>Collection of related laboratory test results</li>
 * <li>Mapping to specific test codes and categories</li>
 * </ul>
 * This class is primarily used in the laboratory result display and processing workflow
 * for Canadian healthcare providers using the MDS laboratory system.
 *
 * @since February 4, 2004
 */
public class GroupedReports {

    /** The OBR (Observation Request) segment identifier that this group is associated with */
    public String associatedOBR;
    /** Collection of individual test results (OBX segments) belonging to this observation request */
    public ArrayList<Results> resultsArray;
    /** Formatted timestamp string for display (dd-MMM-yy HH:mm format) */
    public String timeStamp;
    /** List of laboratory test codes associated with this grouped report */
    public List<String> codes;

    /**
     * Constructs a GroupedReports instance for organizing laboratory results under an OBR segment.
     * <p>
     * This constructor initializes the grouped report with the associated OBR identifier,
     * parses the HL7 timestamp into a human-readable format, and sets up the test codes
     * collection. The timestamp parsing handles the standard HL7 datetime format (YYYYMMDDHHMMSS)
     * and converts it to a display-friendly format.
     *
     * @param oBR String the OBR (Observation Request) segment identifier
     * @param hL7TimeStamp String HL7 timestamp in YYYYMMDDHHMMSS format
     * @param codes List<String> collection of laboratory test codes for this group
     */
    GroupedReports(String oBR, String hL7TimeStamp, List<String> codes) {
        associatedOBR = oBR;
        this.codes = codes;
        try {
            // Initialize calendar and date formatter for HL7 timestamp conversion
            GregorianCalendar cal = new GregorianCalendar(Locale.ENGLISH);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy HH:mm", Locale.ENGLISH);

            // Parse HL7 timestamp format (YYYYMMDDHHMMSS) into calendar components
            // Note: Calendar months are zero-based, so subtract 1 from parsed month
            cal.set(Integer.parseInt(hL7TimeStamp.substring(0, 4)),
                   Integer.parseInt(hL7TimeStamp.substring(4, 6)) - 1,
                   Integer.parseInt(hL7TimeStamp.substring(6, 8)),
                   Integer.parseInt(hL7TimeStamp.substring(8, 10)),
                   Integer.parseInt(hL7TimeStamp.substring(10, 12)),
                   Integer.parseInt(hL7TimeStamp.substring(12, 14)));

            // Format timestamp for display in Canadian healthcare standard format
            timeStamp = dateFormat.format(cal.getTime());
        } catch (Exception e) {
            // Handle malformed timestamps gracefully with empty string
            timeStamp = "";
        }
    }

}
