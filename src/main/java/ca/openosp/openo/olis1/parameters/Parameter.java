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
 * Interface defining the contract for OLIS query parameters.
 *
 * This interface represents a parameter that can be included in an OLIS HL7 query message.
 * Parameters are the building blocks of query segments and contain the actual search criteria
 * and data values used to query the OLIS system for laboratory results.
 *
 * OLIS parameters follow HL7 v2.5 conventions where complex data types can have multiple
 * components and subcomponents. The interface provides methods to set values at different
 * levels of the parameter hierarchy:
 * - Simple value: Direct parameter value (e.g., patient ID)
 * - Component value: Part of a composite field (e.g., name parts)
 * - Subcomponent value: Part of a component (e.g., identifier type within an ID)
 *
 * Each parameter implementation corresponds to a specific HL7 field used in OLIS queries,
 * such as patient demographics, date ranges, test codes, or provider information.
 *
 * @since 2008-01-01
 */
public interface Parameter {

    /**
     * Converts the parameter to its OLIS HL7 string representation.
     *
     * Formats the parameter value(s) according to HL7 v2.5 specifications and
     * OLIS-specific requirements. This includes proper escaping of special characters,
     * component separation using "^" delimiters, and handling of null or empty values.
     *
     * The output format depends on the specific parameter type and may include:
     * - Simple values: Direct string representation
     * - Composite values: Components separated by "^"
     * - Repeating values: Repetitions separated by "~"
     *
     * @return String the HL7-formatted parameter value ready for inclusion in an OLIS message
     * @since 2008-01-01
     */
    public String toOlisString();

    /**
     * Sets the primary value of the parameter.
     *
     * For simple parameters, this sets the entire parameter value.
     * For complex parameters, this typically sets the first or primary component.
     * The implementation should handle appropriate type conversion and validation
     * based on the specific parameter requirements.
     *
     * @param value Object the value to set (type depends on the specific parameter)
     * @since 2008-01-01
     */
    public void setValue(Object value);

    /**
     * Sets a specific component value within a composite parameter.
     *
     * Used for parameters that have multiple components according to HL7 specifications.
     * Components are typically numbered starting from 1 (following HL7 conventions).
     * For example, in a patient name parameter, component 1 might be family name,
     * component 2 given name, etc.
     *
     * @param part Integer the component number (1-based index)
     * @param value Object the value to set for the specified component
     * @since 2008-01-01
     */
    public void setValue(Integer part, Object value);

    /**
     * Sets a specific subcomponent value within a component of a composite parameter.
     *
     * Used for parameters with nested structure where components themselves have
     * subcomponents. This is common in complex HL7 data types like identifiers
     * that have both a value and a type indicator.
     *
     * @param part Integer the component number (1-based index)
     * @param part2 Integer the subcomponent number within the component (1-based index)
     * @param value Object the value to set for the specified subcomponent
     * @since 2008-01-01
     */
    public void setValue(Integer part, Integer part2, Object value);

    /**
     * Returns the query code identifier for this parameter.
     *
     * The query code identifies the parameter's role in the OLIS query structure.
     * This is typically the HL7 field identifier (e.g., "PID-3" for patient identifier,
     * "OBR-7" for observation date/time). The code is used by the query builder to
     * properly position the parameter within the HL7 message segments.
     *
     * @return String the HL7 field identifier or OLIS-specific parameter code
     * @since 2008-01-01
     */
    public String getQueryCode();
}
