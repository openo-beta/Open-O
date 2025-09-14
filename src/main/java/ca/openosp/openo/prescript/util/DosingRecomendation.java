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


package ca.openosp.openo.prescript.util;

import java.util.ArrayList;
import java.util.Hashtable;

import ca.openosp.openo.utility.MiscUtils;

/**
 * Represents a medication dosing recommendation for renal dosing adjustments.
 * This class encapsulates dosing information for medications based on creatinine clearance
 * ranges and provides functionality to evaluate whether a patient's renal function
 * falls within specific dosing ranges.
 *
 * <p>The class is primarily used in the prescription system to provide clinical decision
 * support for medication dosing in patients with varying degrees of renal impairment.
 * Dosing recommendations are typically based on creatinine clearance (ClCr) ranges
 * and include specific dose adjustments or warnings.</p>
 *
 * @since 2007-04-16
 */
public class DosingRecomendation {

    /**
     * The medication name for this dosing recommendation.
     */
    private String name = null;

    /**
     * The ATC (Anatomical Therapeutic Chemical) code for the medication.
     * Used for standardized drug classification.
     */
    private String atccode = null;

    /**
     * Additional clinical information and warnings related to the dosing recommendation.
     * May include contraindications, monitoring requirements, or special instructions.
     */
    private String moreinfo = null;

    /**
     * List of dose adjustments based on creatinine clearance ranges.
     * Each Hashtable contains keys like "clcrrange" and "recommendation"
     * representing the ClCr range and corresponding dosing advice.
     */
    private ArrayList<Hashtable<String, String>> Dose = null;

    /**
     * Returns a string representation of this dosing recommendation.
     *
     * @return String containing the medication name and ATC code
     */
    public String toString() {
        return "name: " + name + " atccode: " + atccode;
    }

    /**
     * Creates a new instance of DosingRecomendation with default null values.
     * All fields must be populated using setter methods after construction.
     */
    public DosingRecomendation() {
    }

    /**
     * Gets the medication name for this dosing recommendation.
     *
     * @return String the medication name, or null if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the medication name for this dosing recommendation.
     *
     * @param name String the medication name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the ATC (Anatomical Therapeutic Chemical) code for the medication.
     *
     * @return String the ATC code, or null if not set
     */
    public String getAtccode() {
        return atccode;
    }

    /**
     * Sets the ATC (Anatomical Therapeutic Chemical) code for the medication.
     *
     * @param atccode String the ATC code to set
     */
    public void setAtccode(String atccode) {
        this.atccode = atccode;
    }

    /**
     * Gets additional clinical information and warnings for this dosing recommendation.
     *
     * @return String additional information, or null if not set
     */
    public String getMoreinfo() {
        return moreinfo;
    }

    /**
     * Sets additional clinical information and warnings for this dosing recommendation.
     *
     * @param moreinfo String additional information to set
     */
    public void setMoreinfo(String moreinfo) {
        this.moreinfo = moreinfo;
    }

    /**
     * Gets the list of dose adjustments based on creatinine clearance ranges.
     *
     * @return ArrayList&lt;Hashtable&lt;String, String&gt;&gt; list of dose recommendations, or null if not set
     */
    public ArrayList<Hashtable<String, String>> getDose() {
        return Dose;
    }

    /**
     * Sets the list of dose adjustments based on creatinine clearance ranges.
     *
     * @param Dose ArrayList&lt;Hashtable&lt;String, String&gt;&gt; list of dose recommendations to set
     */
    public void setDose(ArrayList<Hashtable<String, String>> Dose) {
        this.Dose = Dose;
    }


    /**
     * Evaluates whether a given creatinine clearance value falls within the range
     * specified in a dose recommendation.
     *
     * <p>This method parses various creatinine clearance range formats and determines
     * if the provided value meets the criteria. The ranges are stored in the doseVal
     * hashtable with the key "clcrrange" and can be in the following formats:</p>
     *
     * <ul>
     * <li>"30-50" - between 30 and 50 (inclusive)</li>
     * <li>"&amp;lt;15" or "&lt;15" - less than or equal to 15</li>
     * <li>"&amp;gt;50" or "&gt;50" - greater than or equal to 50</li>
     * <li>"30" - exactly equal to 30</li>
     * </ul>
     *
     * @param val int the creatinine clearance value to evaluate (mL/min)
     * @param doseVal Hashtable&lt;String, String&gt; containing the dose recommendation
     *                with "clcrrange" key specifying the acceptable range
     * @return boolean true if the value falls within the specified range, false otherwise
     */
    public boolean valueInRangeOfDose(int val, Hashtable<String, String> doseVal) {
        boolean valueInRange = false;
        try {
            String toParse = doseVal.get("clcrrange");

            MiscUtils.getLogger().debug("TO PARSE: " + toParse);
            if (toParse == null) {
                return false;
            }

            // Handle range format: "30-50" (between style)
            if (toParse.indexOf("-") != -1) {
                String[] betweenVals = toParse.split("-");
                if (betweenVals.length == 2) {
                    int lower = Integer.parseInt(betweenVals[0]);
                    int upper = Integer.parseInt(betweenVals[1]);

                    if (val >= lower && val <= upper) {
                        valueInRange = true;
                    }
                }

            // Handle greater than format: "&gt;50" or ">50"
            } else if (toParse.indexOf("&gt;") != -1 || toParse.indexOf(">") != -1) {
                toParse = toParse.replaceFirst("&gt;", "");
                toParse = toParse.replaceFirst(">", "");

                int gt = Integer.parseInt(toParse);
                if (val >= gt) {
                    valueInRange = true;
                }
            // Handle less than format: "&lt;15" or "<15"
            } else if (toParse.indexOf("&lt;") != -1 || toParse.indexOf("<") != -1) {
                toParse = toParse.replaceFirst("&lt;", "");
                toParse = toParse.replaceFirst("<", "");

                int lt = Integer.parseInt(toParse);
                if (val <= lt) {
                    valueInRange = true;
                }
            // Handle exact value format: "30"
            } else if (!toParse.equals("")) {
                int eq = Integer.parseInt(toParse);
                if (val == eq) {
                    valueInRange = true;
                }
            }

        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        }
        return valueInRange;
    }


}
