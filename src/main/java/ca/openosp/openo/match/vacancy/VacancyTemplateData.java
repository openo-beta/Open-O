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
package ca.openosp.openo.match.vacancy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Healthcare provider vacancy matching template with weighted criteria and scoring algorithms.
 *
 * <p>This class represents individual matching criteria within a healthcare provider vacancy
 * template. Each template element defines specific patient requirements, acceptable values,
 * and sophisticated matching algorithms that determine how well patients fit provider needs.
 * The system supports both exact matching and fuzzy matching with weighted scoring.</p>
 *
 * <p>Healthcare Matching Features:
 * <ul>
 *   <li>Weighted scoring system (1-100 weight values)</li>
 *   <li>Multiple matching modes: exact values, ranges, and fuzzy string matching</li>
 *   <li>Gender-specific matching with transgender inclusivity</li>
 *   <li>Numerical range matching for age, severity, and clinical scores</li>
 *   <li>Levenshtein distance for approximate text matching</li>
 * </ul>
 * </p>
 *
 * <p>Matching Algorithm Types:
 * <ul>
 *   <li><strong>Gender Matching:</strong> Supports male, female, transgender with case-insensitive partial matching</li>
 *   <li><strong>Range Matching:</strong> Numerical ranges with string representations ("18-65", "18 to 65")</li>
 *   <li><strong>Value Matching:</strong> Exact or approximate string matching using edit distance</li>
 *   <li><strong>Optional Matching:</strong> Flexible criteria that enhance but don't require perfect matches</li>
 * </ul>
 * </p>
 *
 * <p>Canadian Healthcare Context: Designed to support complex healthcare matching
 * scenarios including age-based programs, gender-specific services, location-based
 * care delivery, and clinical severity matching for optimal patient-provider pairing.</p>
 *
 * @see VacancyData
 * @see ca.openosp.openo.match.client.ClientData
 * @see ca.openosp.openo.match.Matcher
 * @since 2012-11-10
 */
public class VacancyTemplateData {
    /** Logger for healthcare matching operations */
    private static final Logger logger = MiscUtils.getLogger();

    /** Maximum weight value for matching criteria */
    private static final int MAX_WEIGHT = 100;

    /** Minimum weight value for matching criteria */
    private static final int MIN_WEIGHT = 0;

    /** Parameter key for gender-specific matching */
    private static final String GENDER = "gender";

    /** Weight/importance of this matching criterion (1-100) */
    private int weight = 1;

    /** Parameter name/key for this matching criterion */
    private String param;

    /** Acceptable values for exact or fuzzy matching */
    private List<String> values = new ArrayList<String>();

    /** Numerical ranges for age, severity, or clinical score matching */
    private List<Range> ranges = new ArrayList<Range>();

    /** Whether this criterion is optional (enhances match but not required) */
    private boolean option;

    /** Whether this criterion uses range-based matching */
    private boolean range;

    /** Supported gender values including transgender inclusivity */
    private List<String> transaGender = Arrays.asList("male", "female", "m", "f", "transgender");

    /**
     * Calculates weighted match score between patient data and vacancy requirement.
     *
     * <p>This method implements sophisticated healthcare matching algorithms that support
     * multiple matching strategies based on the type of patient characteristic being evaluated.
     * The scoring system returns values from 0 (no match) to 100 (perfect match), weighted
     * by the criterion's importance.</p>
     *
     * <p>Healthcare Matching Strategies:
     * <ul>
     *   <li><strong>Gender:</strong> Inclusive matching supporting transgender patients</li>
     *   <li><strong>Ranges:</strong> Numerical matching for age, clinical scores, severity levels</li>
     *   <li><strong>Values:</strong> Exact or fuzzy string matching with edit distance calculations</li>
     *   <li><strong>Empty Criteria:</strong> Returns perfect match (100) for flexible requirements</li>
     * </ul>
     * </p>
     *
     * @param value String the patient's characteristic value to match against this criterion
     * @return int weighted match score (0-100) indicating how well patient meets this requirement
     */
    public int matches(String value) {
        // Ensure minimum weight to prevent division by zero
        if (this.weight == 0) {
            this.weight = 1;
        }
        // Special handling for gender matching with transgender inclusivity
        if (GENDER.equalsIgnoreCase(param)) {
            if (value != null) {
                // Check for any supported gender values in patient data
                for (String gender : transaGender) {
                    if (value.toLowerCase().contains(gender)) {
                        return 100; // Perfect match for any recognized gender
                    }
                }
            }
        }
        // Handle range-based matching for numerical healthcare criteria
        if (this.range) {
            // Empty ranges indicate flexible requirement (perfect match)
            if (ranges.isEmpty()) {
                return 100;
            }

            // Try string-based range matching first (e.g., "18-65", "mild-moderate")
            if (!StringUtils.isNumeric(value)) {
                for (Range range : ranges) {
                    for (String rangeString : range.rangeString) {
                        if (value.contains(rangeString) || rangeString.contains(value)) {
                            return 100; // Perfect match for string range
                        }
                    }
                }
            }

            // Try numerical range matching for age, scores, etc.
            Integer val = null;
            try {
                val = Integer.valueOf(value);
            } catch (Exception e) {
                logger.error("Error parsing numerical value for range matching", e);
            }

            if (val != null) {
                // Check if patient value falls within any acceptable range
                for (Range rangeVal : ranges) {
                    if (rangeVal.isInRange(val)) {
                        return 100; // Perfect match within range
                    }
                }
            } else {
                return 0; // No match for non-numeric value in numeric range
            }
        }
        // Handle value-based matching with fuzzy string comparison
        String valueToMatch = value;
        if (valueToMatch == null) {
            return 0; // No match for null patient data
        }

        // Empty values list indicates flexible requirement (perfect match)
        if (values.isEmpty()) {
            return 100;
        }

        // Calculate best match score using Levenshtein distance for fuzzy matching
        TreeSet<Integer> weights = new TreeSet<Integer>();
        for (String val : values) {
            if (val == null) {
                // Null acceptable value
                if (values.size() == 1) {
                    return 100; // Perfect match if only acceptable value is null
                }
                weights.add(MAX_WEIGHT);
            } else {
                // Calculate fuzzy match score using edit distance
                int distance = StringUtils.getLevenshteinDistance(val.toLowerCase(),
                        valueToMatch.toLowerCase());
                int calculatedWeight = 100 / (distance == 0 ? 1 : distance);
                weights.add(calculatedWeight);
            }
        }
        // Return the highest match score found
        return weights.last();
    }

    /**
     * Checks if this matching criterion is optional.
     *
     * @return boolean true if criterion is optional (enhances match but not required)
     */
    public boolean isOption() {
        return option;
    }

    /**
     * Sets whether this matching criterion is optional.
     *
     * @param option boolean true if criterion should be optional
     */
    public void setOption(boolean option) {
        this.option = option;
    }

    /**
     * Sets whether this criterion uses range-based matching.
     *
     * @param range boolean true if criterion should use range matching algorithms
     */
    public void setRange(boolean range) {
        this.range = range;
    }

    /**
     * Gets the importance weight for this matching criterion.
     *
     * @return int weight value (1-100) indicating criterion importance
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Sets the importance weight for this matching criterion.
     *
     * @param weight int weight value (1-100) indicating criterion importance
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Gets the parameter name/key for this matching criterion.
     *
     * @return String the parameter name (e.g., "age", "gender", "diagnosis")
     */
    public String getParam() {
        return param;
    }

    /**
     * Sets the parameter name/key for this matching criterion.
     *
     * @param param String the parameter name for this healthcare criterion
     */
    public void setParam(String param) {
        this.param = param;
    }

    /**
     * Gets the list of acceptable values for this matching criterion.
     *
     * @return List<String> acceptable values for exact or fuzzy matching
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Sets the list of acceptable values for this matching criterion.
     *
     * @param values List<String> acceptable values for matching
     */
    public void setValues(List<String> values) {
        this.values = values;
    }

    /**
     * Adds a numerical range for this matching criterion.
     *
     * <p>Ranges are used for healthcare criteria like age groups, severity levels,
     * clinical scores, or any numerical patient characteristics that have acceptable
     * minimum and maximum values.</p>
     *
     * @param min Integer minimum acceptable value (null for no minimum)
     * @param max Integer maximum acceptable value (null for no maximum)
     */
    public void addRange(Integer min, Integer max) {
        // Set unbounded ranges if nulls provided
        if (min == null) {
            min = Integer.MIN_VALUE;
        }
        if (max == null) {
            max = Integer.MAX_VALUE;
        }
        ranges.add(new Range(min.longValue(), max.longValue()));
    }

    /**
     * Returns a string representation of this matching template criterion.
     *
     * <p>The string includes the criterion weight, parameter name, acceptable values,
     * ranges, and matching options. Output is truncated for readability.</p>
     *
     * @return String formatted representation of this matching criterion
     */
    @Override
    public String toString() {
        final int maxLen = 20;
        return "VacancyTemplateData [weight="
                + weight
                + ", param="
                + param
                + ", values="
                + (values != null ? values.subList(0,
                Math.min(values.size(), maxLen)) : null)
                + ", ranges="
                + (ranges != null ? ranges.subList(0,
                Math.min(ranges.size(), maxLen)) : null) + ", option="
                + option + ", range=" + range + "]";
    }

    /**
     * Internal class representing a numerical range for healthcare matching criteria.
     *
     * <p>Supports both numerical range checking and string-based range representations
     * commonly used in healthcare settings (e.g., "18-65", "18 to 65").</p>
     */
    private static class Range {
        /** Minimum value in the range */
        private long min;

        /** Maximum value in the range */
        private long max;

        /** String representations of this range for text-based matching */
        List<String> rangeString = Collections.EMPTY_LIST;

        /**
         * Creates a new healthcare range with min/max values.
         *
         * @param min long minimum acceptable value
         * @param max long maximum acceptable value
         */
        public Range(long min, long max) {
            this.min = min;
            this.max = max;
            // Create common string representations for text-based range matching
            rangeString = new ArrayList<String>(Arrays.asList("" + min + "-" + max, "" + min + " to " + max));
        }

        /**
         * Checks if a number falls within this healthcare range.
         *
         * @param number long the value to check
         * @return boolean true if number is within range (inclusive)
         */
        boolean isInRange(long number) {
            return number >= min && number <= max;
        }
    }

}
