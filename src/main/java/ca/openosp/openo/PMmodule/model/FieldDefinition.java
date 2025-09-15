//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package ca.openosp.openo.PMmodule.model;

import java.io.Serializable;

/**
 * Metadata definition for data fields in structured import/export operations.
 *
 * FieldDefinition provides schema information for parsing and formatting
 * data fields in fixed-width or delimited file formats. This class is used
 * to define the structure of data fields for import/export operations,
 * particularly for integration with external systems.
 *
 * Key features:
 * - Field positioning for fixed-width formats
 * - Data type specification for validation
 * - Date format patterns for temporal data
 * - Length constraints for data integrity
 *
 * Common use cases:
 * - Healthcare data exchange (HL7, EDI)
 * - Government reporting formats
 * - Legacy system integration
 * - Batch processing definitions
 * - Fixed-width file parsing
 *
 * Field types supported:
 * - String: Character data with length limits
 * - Integer: Whole numbers with range validation
 * - Decimal: Floating point with precision
 * - Date: Temporal data with format patterns
 * - Boolean: True/false indicators
 *
 * Position-based parsing:
 * - Start index defines field position in record
 * - Field length determines characters to read
 * - Supports both fixed-width and delimited formats
 *
 * Format patterns:
 * - Date formats: yyyy-MM-dd, yyyyMMdd, etc.
 * - Number formats: Leading zeros, decimal places
 * - String formats: Padding, truncation rules
 *
 * @since 2005-01-01
 * @see FormInfo
 */
public class FieldDefinition implements Serializable {

    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Name identifier for the field.
     * Used as reference key in field mappings.
     * Examples: "patientId", "admissionDate", "diagnosisCode".
     */
    private String fieldName;

    /**
     * Maximum length of the field in characters.
     * Enforces data truncation or padding rules.
     * Critical for fixed-width format compliance.
     */
    private Integer fieldLength;

    /**
     * Data type of the field.
     * Determines parsing and validation rules.
     * Common values: "String", "Integer", "Date", "Decimal".
     */
    private String fieldType;

    /**
     * Starting position index in the record.
     * Zero-based index for field extraction.
     * Used in fixed-width format parsing.
     */
    private Integer fieldStartIndex;

    /**
     * Date format pattern string.
     * Defines parsing pattern for date fields.
     * Examples: "yyyy-MM-dd", "yyyyMMdd", "MM/dd/yyyy".
     */
    private String dateFormatStr;

    /**
     * Gets the maximum field length.
     *
     * Returns the character limit for this field.
     * Used for validation and formatting operations.
     *
     * @return Integer the field length in characters
     * @since 2005-01-01
     */
    public Integer getFieldLength() {
        return fieldLength;
    }

    /**
     * Sets the maximum field length.
     *
     * Defines the character limit for this field.
     * Values exceeding this length will be truncated.
     *
     * @param fieldLength Integer the maximum characters allowed
     * @since 2005-01-01
     */
    public void setFieldLength(Integer fieldLength) {
        this.fieldLength = fieldLength;
    }

    /**
     * Gets the field name identifier.
     *
     * Returns the unique name used to reference this field
     * in mappings and transformations.
     *
     * @return String the field name
     * @since 2005-01-01
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name identifier.
     *
     * Defines the unique name for field reference.
     * Should be descriptive and follow naming conventions.
     *
     * @param fieldName String the field identifier
     * @since 2005-01-01
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the field data type.
     *
     * Returns the type specification for parsing and validation.
     * Common values: "String", "Integer", "Date", "Decimal".
     *
     * @return String the field type
     * @since 2005-01-01
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * Sets the field data type.
     *
     * Specifies how the field should be parsed and validated.
     * Type determines applicable format patterns.
     *
     * @param fieldType String the data type
     * @since 2005-01-01
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Gets the field starting position.
     *
     * Returns the zero-based index where this field begins
     * in a fixed-width record format.
     *
     * @return Integer the starting character position
     * @since 2005-01-01
     */
    public Integer getFieldStartIndex() {
        return fieldStartIndex;
    }

    /**
     * Sets the field starting position.
     *
     * Defines where to begin reading this field in the record.
     * Critical for accurate fixed-width parsing.
     *
     * @param fieldStartIndex Integer the zero-based start position
     * @since 2005-01-01
     */
    public void setFieldStartIndex(Integer fieldStartIndex) {
        this.fieldStartIndex = fieldStartIndex;
    }

    /**
     * Gets the date format pattern.
     *
     * Returns the SimpleDateFormat pattern for parsing date fields.
     * Only applicable when fieldType is "Date".
     *
     * @return String the date format pattern
     * @since 2005-01-01
     */
    public String getDateFormatStr() {
        return dateFormatStr;
    }

    /**
     * Sets the date format pattern.
     *
     * Defines the pattern for parsing and formatting date values.
     * Uses SimpleDateFormat pattern syntax.
     *
     * @param dateFormatStr String the format pattern (e.g., "yyyy-MM-dd")
     * @since 2005-01-01
     */
    public void setDateFormatStr(String dateFormatStr) {
        this.dateFormatStr = dateFormatStr;
    }
}
