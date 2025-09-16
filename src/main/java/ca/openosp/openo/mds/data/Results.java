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
 * Represents individual laboratory test results with associated metadata and interpretation information.
 * <p>
 * This class encapsulates a single laboratory test result from the MDS (Medical Data Systems)
 * laboratory provider, including the test value, reference ranges, abnormal flags, and any
 * associated notes or comments. Each Results instance corresponds to an OBX (Observation Result)
 * segment in the HL7 laboratory message structure.
 * <p>
 * Key features include:
 * <ul>
 * <li>Test result values with units and reference ranges</li>
 * <li>Abnormal flag interpretation for clinical significance</li>
 * <li>Result status interpretation (Final, Preliminary, etc.)</li>
 * <li>Associated notes and comments from laboratory</li>
 * <li>Laboratory identification and traceability</li>
 * </ul>
 *
 * @since February 4, 2004
 */
public class Results {

    /**
     * Constructs a Results instance with complete laboratory test information.
     *
     * @param n String the test name
     * @param rR String the reference range
     * @param u String the units of measurement
     * @param oV String the observation value (test result)
     * @param aF String the abnormal flags
     * @param oI String the observation identifier
     * @param rS String the result status code
     * @param newnotes ArrayList the associated notes
     * @param lID String the laboratory ID
     */
    Results(String n, String rR, String u, String oV, String aF, String oI, String rS, ArrayList newnotes, String lID) {
        name = n;
        referenceRange = rR;
        units = u;
        observationValue = oV;
        abnormalFlags = aF;
        observationIden = oI;
        resultStatus = resultInterpret(rS);
        notes = newnotes;
        labID = lID;
    }

    /** Human-readable test name (e.g., "Hemoglobin", "Glucose") */
    public String name;
    /** Units of measurement for the test result */
    public String units;
    /** Reference range for normal values */
    public String referenceRange;
    /** The actual test result value */
    public String observationValue;
    /** Abnormal flags indicating clinical significance */
    public String abnormalFlags;
    /** Observation identifier from laboratory system */
    public String observationIden;
    /** Interpreted result status (Final, Preliminary, etc.) */
    public String resultStatus;
    /** Collection of notes and comments associated with this result */
    public ArrayList notes;
    /** Laboratory identifier for traceability */
    public String labID;

    private String resultInterpret(String rS) {
        switch (rS.toUpperCase().charAt(0)) {
            case 'C':
                return "Corrected";
            case 'D':
                return "Deleted";
            case 'F':
                return "Final";
            case 'I':
                return "Pending";
            case 'P':
                return "Preliminary";
            case 'R':
                return "Unverified";
            case 'S':
                return "Partial";
            case 'X':
                return "DNR";
            case 'U':
                return "Final";
            case 'W':
                return "Deleted";
            default:
                return "Invalid";
        }
    }


    /**
     * Retrieves a specific note by index from the notes collection.
     * <p>
     * Note: The notes ArrayList should only contain String objects.
     *
     * @param i int the index of the note to retrieve
     * @return String the note at the specified index, or empty string if not found
     */
    public String getLabNotes(int i) {
        String ret = "";
        if (notes != null && notes.size() > i) {
            ret = (String) notes.get(i);
        }
        return ret;
    }
}
