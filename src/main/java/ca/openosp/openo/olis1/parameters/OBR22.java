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
 * Results Report/Status Change Date/Time parameter (OBR-22) for OLIS queries.
 *
 * This parameter represents the date/time when results were reported or when
 * the status of results changed in the laboratory system. It's used to filter
 * queries based on when results became available rather than when tests were performed.
 *
 * The parameter supports:
 * - Single timestamp: Specific report date/time
 * - Timestamp range: Start and end times separated by "&"
 *
 * This differs from OBR-7 (observation date) as it tracks:
 * - When results were released/reported
 * - When result status changed (e.g., preliminary to final)
 * - Update timestamps for amended results
 *
 * Date format follows HL7 v2.5 timestamp specification:
 * - Format: YYYYMMDDHHMMSS±ZZZZ (with timezone offset)
 * - Example: 20240115143000-0500 (Jan 15, 2024, 2:30 PM EST)
 *
 * Common uses:
 * - Retrieving newly reported results
 * - Finding results updated within a time window
 * - Polling for recent result changes
 *
 * @since 2008-01-01
 */
public class OBR22 implements Parameter {

    /**
     * The formatted timestamp value or timestamp range.
     * Single timestamp or range in format: timestamp1[&timestamp2]
     */
    private String value;

    /**
     * Date formatter for converting Date objects to HL7 timestamp format.
     * Uses pattern: yyyyMMddHHmmssZZZZZ (includes timezone).
     */
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmssZZZZZ");

    /**
     * Constructs an OBR22 parameter with a pre-formatted timestamp value.
     *
     * @param value String the formatted timestamp or timestamp range
     * @since 2008-01-01
     */
    public OBR22(String value) {
        this.value = value;
    }

    /**
     * Default constructor for OBR22 parameter.
     *
     * Creates an empty OBR22 parameter. Timestamp values can be set
     * using the setValue method.
     *
     * @since 2008-01-01
     */
    public OBR22() {
        // Empty constructor for dynamic value setting
    }

    /**
     * Sets the report/status change timestamp value.
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
                // Single timestamp value
                this.value = dateFormatter.format(value);
            } else if (value instanceof List) {
                // Timestamp range: start & end
                @SuppressWarnings("unchecked")
                List<Date> dates = (List<Date>) value;
                this.value = dateFormatter.format(dates.get(0));
                this.value += "&" + dateFormatter.format(dates.get(1));
            }
        }
    }

    /**
     * Sets a component value (not supported for OBR22).
     *
     * OBR22 uses the primary setValue method for timestamp values.
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
     * Converts the OBR22 parameter to its OLIS HL7 string representation.
     *
     * Formats the timestamp value with the appropriate field identifier.
     *
     * @return String the formatted HL7 parameter string
     * @since 2008-01-01
     */
    @Override
    public String toOlisString() {
        return getQueryCode() + "^" + value;
    }

    /**
     * Returns the HL7 field identifier for results report/status change date/time.
     *
     * @return String "@OBR.22" indicating this is the OBR-22 field
     * @since 2008-01-01
     */
    @Override
    public String getQueryCode() {
        return "@OBR.22";
    }

    /**
     * Sets a subcomponent value (not supported for OBR22).
     *
     * OBR22 does not have subcomponents.
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
