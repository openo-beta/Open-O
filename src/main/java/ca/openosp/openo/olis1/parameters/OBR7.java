//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.olis1.parameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Observation Date/Time parameter (OBR-7) for OLIS queries.
 *
 * This parameter represents the observation date/time field in HL7 messages,
 * used to specify when a laboratory test was performed or specimen was collected.
 * It supports both single date values and date ranges for query filtering.
 *
 * The parameter accepts:
 * - Single date: Specific observation date/time
 * - Date range: Earliest and latest dates separated by "&"
 *
 * Date format follows HL7 v2.5 timestamp specification:
 * - Format: YYYYMMDDHHMMSS±ZZZZ (with timezone offset)
 * - Example: 20240115143000-0500 (Jan 15, 2024, 2:30 PM EST)
 *
 * This parameter is commonly used to:
 * - Filter results by observation date
 * - Specify time windows for result retrieval
 * - Limit queries to recent observations
 *
 * @since 2008-01-01
 */
public class OBR7 implements Parameter {
    /**
     * The formatted date/time value or date range.
     * Single date or range in format: date1[&date2]
     */
    private String value;

    /**
     * Date formatter for converting Date objects to HL7 timestamp format.
     * Uses pattern: yyyyMMddHHmmssZZZZZ (includes timezone).
     */
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmssZZZZZ");

    /**
     * Constructs an OBR7 parameter with a pre-formatted date value.
     *
     * @param value String the formatted date/time or date range
     * @since 2008-01-01
     */
    public OBR7(String value) {
        this.value = value;
    }

    /**
     * Default constructor for OBR7 parameter.
     *
     * Creates an empty OBR7 parameter. Date values can be set
     * using the setValue method.
     *
     * @since 2008-01-01
     */
    public OBR7() {
        // Empty constructor for dynamic value setting
    }

    /**
     * Sets the observation date/time value.
     *
     * Accepts either a single Date or a List of two Dates for a range.
     * Dates are automatically formatted to HL7 timestamp format.
     *
     * @param value Object either a Date or List<Date> with 2 elements for range
     * @since 2008-01-01
     */
    @Override
    public void setValue(Object value) {
        if (value != null) {
            if (value instanceof Date) {
                // Single date value
                this.value = dateFormatter.format(value);
            } else if (value instanceof List) {
                // Date range: earliest & latest
                @SuppressWarnings("unchecked")
                List<Date> dates = (List<Date>) value;
                this.value = dateFormatter.format(dates.get(0));
                this.value += "&" + dateFormatter.format(dates.get(1));
            }
        }
    }

    /**
     * Sets a component value (not supported for OBR7).
     *
     * OBR7 uses the primary setValue method for date values.
     *
     * @param part Integer the component number (ignored)
     * @param value Object the value to set (ignored)
     * @throws UnsupportedOperationException always, as components are not supported
     * @since 2008-01-01
     */
    @Override
    public void setValue(Integer part, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts the OBR7 parameter to its OLIS HL7 string representation.
     *
     * Formats the date/time value with the appropriate field identifier.
     *
     * @return String the formatted HL7 parameter string
     * @since 2008-01-01
     */
    @Override
    public String toOlisString() {
        return getQueryCode() + "^" + value;
    }

    /**
     * Returns the HL7 field identifier for observation date/time.
     *
     * @return String "@OBR.7" indicating this is the OBR-7 field
     * @since 2008-01-01
     */
    @Override
    public String getQueryCode() {
        return "@OBR.7";
    }

    /**
     * Sets a subcomponent value (not supported for OBR7).
     *
     * OBR7 does not have subcomponents.
     *
     * @param part Integer the component number (ignored)
     * @param part2 Integer the subcomponent number (ignored)
     * @param value Object the value to set (ignored)
     * @throws UnsupportedOperationException always, as subcomponents are not supported
     * @since 2008-01-01
     */
    @Override
    public void setValue(Integer part, Integer part2, Object value) {
        throw new UnsupportedOperationException();
    }

}
