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

/**
 * Quantity Limited Request parameter (QRD-7) for OLIS queries.
 *
 * This parameter specifies the maximum number of results to return from an OLIS query.
 * It's used to limit result sets for performance optimization and to implement
 * pagination in result retrieval.
 *
 * The QRD-7 field in HL7 contains:
 * - Quantity limit (maximum number of results)
 * - Units (always "RD" for records)
 * - Coding system (HL70126)
 *
 * This parameter is particularly useful for:
 * - Preventing overwhelming response sizes
 * - Implementing paginated result retrieval
 * - Optimizing query performance
 * - Managing network bandwidth
 *
 * HL7 field structure:
 * - QRD.7.1: Quantity (numeric limit)
 * - QRD.7.2.1: Units ("RD" for records)
 * - QRD.7.2.3: Name of Coding System ("HL70126")
 *
 * @since 2008-01-01
 */
public class QRD7 implements Parameter {

    /**
     * The maximum number of results to return.
     * Specifies the quantity limit for the query response.
     */
    private Integer queryNum;

    /**
     * Constructs a QRD7 parameter with a specified result limit.
     *
     * @param queryNum Integer the maximum number of results to return
     * @since 2008-01-01
     */
    public QRD7(Integer queryNum) {
        this.queryNum = queryNum;
    }

    /**
     * Converts the QRD7 parameter to its OLIS HL7 string representation.
     *
     * Formats the quantity limit with units "RD" (records) and coding system "HL70126".
     *
     * @return String the formatted HL7 parameter string
     * @since 2008-01-01
     */
    @Override
    public String toOlisString() {
        return getQueryCode() + ".1^" + queryNum + "~" + getQueryCode() + ".2.1^RD~" + getQueryCode() + ".2.3^HL70126";
    }

    /**
     * Sets the quantity limit value.
     *
     * @param value Object the maximum number of results (must be Integer)
     * @since 2008-01-01
     */
    @Override
    public void setValue(Object value) {
        if (value instanceof Integer) {
            queryNum = (Integer) value;
        }
    }

    /**
     * Sets a component value (not supported for QRD7).
     *
     * QRD7 uses the primary setValue method for the quantity value.
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
     * Sets a subcomponent value (not supported for QRD7).
     *
     * QRD7 does not have subcomponents.
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

    /**
     * Returns the HL7 field identifier for quantity limited request.
     *
     * @return String "@QRD.7" indicating this is the QRD-7 field
     * @since 2008-01-01
     */
    @Override
    public String getQueryCode() {
        return "@QRD.7";
    }

}
