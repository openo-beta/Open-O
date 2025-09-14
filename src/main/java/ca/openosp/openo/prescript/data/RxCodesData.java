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


package ca.openosp.openo.prescript.data;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.commn.dao.CtlFrequencyDao;
import ca.openosp.openo.commn.dao.CtlSpecialInstructionsDao;
import ca.openosp.openo.commn.model.CtlFrequency;
import ca.openosp.openo.commn.model.CtlSpecialInstructions;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Data access class for prescription codes including frequency codes and special instructions.
 *
 * This class provides centralized access to prescription-related code tables that are used
 * throughout the prescription module. It retrieves frequency codes (e.g., "BID", "TID", "QID")
 * and special instructions that can be applied to prescriptions.
 *
 * The class acts as a facade over the underlying DAO layer, transforming database entities
 * into domain-specific objects suitable for use in the prescription workflow. This abstraction
 * allows the prescription module to work with simplified data structures while maintaining
 * flexibility in the underlying data storage.
 *
 * @since 2006-03-01
 */
public class RxCodesData {

    /**
     * DAO for accessing frequency code data from the database.
     * Frequency codes define how often a medication should be taken.
     */
    private CtlFrequencyDao ctlFrequencyDao = (CtlFrequencyDao) SpringUtils.getBean(CtlFrequencyDao.class);

    /**
     * DAO for accessing special instruction templates from the database.
     * Special instructions provide standardized directions for medication administration.
     */
    private CtlSpecialInstructionsDao ctlSpecialInstructionsDao = (CtlSpecialInstructionsDao) SpringUtils.getBean(CtlSpecialInstructionsDao.class);

    /**
     * Retrieves all available frequency codes from the database.
     *
     * Frequency codes define standard dosing schedules for medications (e.g., "BID" for twice daily,
     * "TID" for three times daily). Each code includes minimum and maximum daily dose information
     * to help calculate appropriate medication quantities.
     *
     * The method transforms database entities into FrequencyCode objects that encapsulate
     * the frequency information in a format suitable for prescription calculations.
     *
     * @return FrequencyCode[] array containing all available frequency codes, empty array if none found
     */
    public FrequencyCode[] getFrequencyCodes() {
        FrequencyCode[] arr = {};
        ArrayList<FrequencyCode> lst = new ArrayList<FrequencyCode>();

        List<CtlFrequency> ctlFrequencies = ctlFrequencyDao.findAll();
        for (CtlFrequency ctlFrequency : ctlFrequencies) {
            lst.add(new FrequencyCode(ctlFrequency.getId(), ctlFrequency.getFreqCode(), ctlFrequency.getDailyMin(), ctlFrequency.getDailyMax()));
        }
        arr = lst.toArray(arr);

        return arr;
    }

    /**
     * Retrieves all available special instruction templates from the database.
     *
     * Special instructions are pre-defined text templates that can be added to prescriptions
     * to provide additional guidance for medication administration. Examples include
     * "Take with food", "Avoid alcohol", or "Do not crush tablets".
     *
     * These templates help standardize prescription instructions and ensure important
     * administration details are consistently communicated to patients.
     *
     * @return String[] array containing all special instruction descriptions, empty array if none found
     */
    public String[] getSpecialInstructions() {
        List<String> resultList = new ArrayList<String>();
        List<CtlSpecialInstructions> ctlSpecialInstructionsList = ctlSpecialInstructionsDao.findAll();
        for (CtlSpecialInstructions ctlSpecialInstructions : ctlSpecialInstructionsList) {
            resultList.add(ctlSpecialInstructions.getDescription());
        }
        String[] arr = {};
        arr = resultList.toArray(arr);

        return arr;
    }


    /**
     * Inner class representing a medication frequency code with dosing range information.
     *
     * This class encapsulates the essential information about a dosing frequency,
     * including the code itself (e.g., "BID", "TID") and the expected range of
     * daily doses. This information is crucial for calculating prescription quantities
     * and ensuring appropriate medication supplies.
     *
     * The daily minimum and maximum values help prescribers determine appropriate
     * dispensing quantities based on the prescribed frequency.
     */
    public class FrequencyCode {
        /**
         * Unique identifier for the frequency code in the database.
         */
        int freqId;

        /**
         * The frequency code string (e.g., "BID", "TID", "QID").
         * These are standard medical abbreviations for dosing frequencies.
         */
        String freqCode;

        /**
         * Minimum number of doses per day for this frequency.
         * Stored as String to handle special cases and display formatting.
         */
        String dailyMin;

        /**
         * Maximum number of doses per day for this frequency.
         * Stored as String to handle special cases and display formatting.
         */
        String dailyMax;

        /**
         * Constructs a FrequencyCode with integer daily dose values.
         *
         * This constructor accepts integer values for daily doses and converts them
         * to strings internally. This is commonly used when loading data from the
         * database where dose counts are stored as integers.
         *
         * @param freqId int unique identifier for the frequency code
         * @param freqCode String the frequency code (e.g., "BID", "TID")
         * @param dailyMin int minimum doses per day
         * @param dailyMax int maximum doses per day
         */
        public FrequencyCode(int freqId, String freqCode, int dailyMin, int dailyMax) {
            this.freqId = freqId;
            this.freqCode = freqCode;
            this.dailyMin = Integer.toString(dailyMin);
            this.dailyMax = Integer.toString(dailyMax);
        }

        /**
         * Constructs a FrequencyCode with string daily dose values.
         *
         * This constructor accepts string values for daily doses, allowing for
         * special cases where dose counts might include ranges or special notations
         * (e.g., "1-2", "PRN").
         *
         * @param freqId int unique identifier for the frequency code
         * @param freqCode String the frequency code (e.g., "BID", "TID")
         * @param dailyMin String minimum doses per day as string
         * @param dailyMax String maximum doses per day as string
         */
        public FrequencyCode(int freqId, String freqCode, String dailyMin, String dailyMax) {
            this.freqId = freqId;
            this.freqCode = freqCode;
            this.dailyMin = dailyMin;
            this.dailyMax = dailyMax;
        }

        /**
         * Gets the unique identifier for this frequency code.
         *
         * @return int the frequency code ID from the database
         */
        public int getFreqId() {
            return this.freqId;
        }

        /**
         * Gets the frequency code string.
         *
         * @return String the frequency code (e.g., "BID", "TID", "QID")
         */
        public String getFreqCode() {
            return this.freqCode;
        }

        /**
         * Gets the minimum daily dose count for this frequency.
         *
         * @return String minimum doses per day
         */
        public String getDailyMin() {
            return this.dailyMin;
        }

        /**
         * Gets the maximum daily dose count for this frequency.
         *
         * @return String maximum doses per day
         */
        public String getDailyMax() {
            return this.dailyMax;
        }
    }
}
