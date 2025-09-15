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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import ca.openosp.openo.commn.model.AbstractModel;

/**
 * Entity defining predefined options for criteria types.
 *
 * CriteriaTypeOption provides the available choices for criteria types
 * that use selection-based input (dropdowns, radio buttons, checkboxes).
 * Each option represents one possible value that can be selected.
 *
 * Key features:
 * - Predefined options for consistent data entry
 * - Display ordering for UI presentation
 * - Support for both value and range options
 * - Label/value separation for internationalization
 *
 * Option types:
 * - Simple value: Single selectable value
 * - Range option: Represents a numeric range
 * - Display label: What users see in UI
 * - Storage value: What's stored in database
 *
 * Common examples:
 * - Gender: Male, Female, Other, Prefer not to say
 * - Language: English, French, Spanish, Mandarin
 * - Age ranges: 0-17, 18-25, 26-40, 41-65, 65+
 * - Service level: Emergency, Urgent, Routine, Scheduled
 * - Housing status: Housed, Homeless, At risk, Transitional
 *
 * Display ordering:
 * - Options are sorted by displayOrderNumber
 * - Allows custom ordering independent of alphabetical
 * - Supports reordering without data changes
 *
 * Label vs Value:
 * - Label: User-friendly display text
 * - Value: System value for matching/storage
 * - Enables internationalization of labels
 * - Maintains consistent values across languages
 *
 * Database mapping:
 * - Table: criteria_type_option
 * - Primary key: OPTION_ID (auto-generated)
 * - Foreign key: CRITERIA_TYPE_ID links to criteria_type
 *
 * @since 2005-01-01
 * @see CriteriaType
 * @see Criteria
 */
@Entity
@Table(name = "criteria_type_option")
public class CriteriaTypeOption extends AbstractModel<Integer> implements java.io.Serializable {

    /**
     * Primary key for the criteria type option.
     * Auto-generated unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPTION_ID", unique = true, nullable = false)
    private Integer id;

    /**
     * Reference to the parent criteria type.
     * Links this option to its criteria type definition.
     * Multiple options can belong to one type.
     */
    @Column(name = "CRITERIA_TYPE_ID", nullable = false)
    private Integer criteriaTypeId;

    /**
     * Display order for UI presentation.
     * Lower numbers appear first in lists.
     * Allows custom ordering of options.
     */
    @Column(name = "DISPLAY_ORDER_NUMBER", nullable = false)
    private Integer displayOrderNumber;

    /**
     * User-visible label for this option.
     * Displayed in dropdowns and selection lists.
     * Can be localized for different languages.
     */
    @Column(name = "OPTION_LABEL", nullable = false, length = 128)
    private String optionLabel;

    /**
     * System value for this option.
     * Stored in database when option is selected.
     * Used for matching and business logic.
     */
    @Column(name = "OPTION_VALUE")
    private String optionValue;

    /**
     * Start value for range-based options.
     * Defines minimum value in a numeric range.
     * Example: Age range 18-25 would have rangeStartValue=18.
     */
    @Column(name = "RANGE_START_VALUE")
    private Integer rangeStartValue;

    /**
     * End value for range-based options.
     * Defines maximum value in a numeric range.
     * Example: Age range 18-25 would have rangeEndValue=25.
     */
    @Column(name = "RANGE_END_VALUE")
    private Integer rangeEndValue;

    /**
     * Gets the unique identifier of this option.
     *
     * Returns the primary key from the criteria_type_option table.
     * Used to uniquely reference this option.
     *
     * @return Integer the option ID, null if not persisted
     * @since 2005-01-01
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this option.
     *
     * Updates the primary key value. Typically called
     * by persistence framework during save/load operations.
     *
     * @param id Integer the new option ID
     * @since 2005-01-01
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Default constructor for creating new CriteriaTypeOption.
     *
     * Creates an empty option instance. All fields
     * must be set before persisting.
     *
     * @since 2005-01-01
     */
    public CriteriaTypeOption() {
        // Empty constructor for JPA/Hibernate
    }

    /**
     * Minimal constructor with required fields.
     *
     * Creates an option with essential attributes.
     * Value and range fields can be set later if needed.
     *
     * @param criteriaTypeId Integer the parent criteria type ID
     * @param displayOrderNumber Integer the display order
     * @param optionLabel String the user-visible label
     * @since 2005-01-01
     */
    public CriteriaTypeOption(Integer criteriaTypeId,
                              Integer displayOrderNumber, String optionLabel) {
        this.criteriaTypeId = criteriaTypeId;
        this.displayOrderNumber = displayOrderNumber;
        this.optionLabel = optionLabel;
    }

    /**
     * Full constructor with all fields.
     *
     * Creates a complete option with all attributes including
     * optional value and range specifications.
     *
     * @param criteriaTypeId Integer the parent criteria type ID
     * @param displayOrderNumber Integer the display order
     * @param optionLabel String the user-visible label
     * @param optionValue String the system value
     * @param rangeStartValue Integer range minimum (if applicable)
     * @param rangeEndValue Integer range maximum (if applicable)
     * @since 2005-01-01
     */
    public CriteriaTypeOption(Integer criteriaTypeId,
                              Integer displayOrderNumber, String optionLabel, String optionValue,
                              Integer rangeStartValue, Integer rangeEndValue) {
        this.criteriaTypeId = criteriaTypeId;
        this.displayOrderNumber = displayOrderNumber;
        this.optionLabel = optionLabel;
        this.optionValue = optionValue;
        this.rangeStartValue = rangeStartValue;
        this.rangeEndValue = rangeEndValue;
    }

    /**
     * Gets the parent criteria type identifier.
     *
     * Returns the ID of the criteria type this option
     * belongs to. Multiple options share the same type ID.
     *
     * @return Integer the criteria type ID
     * @since 2005-01-01
     */
    public Integer getCriteriaTypeId() {
        return criteriaTypeId;
    }

    /**
     * Sets the parent criteria type identifier.
     *
     * Links this option to a specific criteria type.
     * Must reference a valid criteria_type record.
     *
     * @param criteriaTypeId Integer the criteria type ID
     * @since 2005-01-01
     */
    public void setCriteriaTypeId(Integer criteriaTypeId) {
        this.criteriaTypeId = criteriaTypeId;
    }

    /**
     * Gets the display order number.
     *
     * Returns the position of this option in UI lists.
     * Options are sorted by this value in ascending order.
     *
     * @return Integer the display order number
     * @since 2005-01-01
     */
    public Integer getDisplayOrderNumber() {
        return displayOrderNumber;
    }

    /**
     * Sets the display order number.
     *
     * Defines where this option appears in lists.
     * Use consistent numbering (e.g., 10, 20, 30) to
     * allow easy insertion of new options.
     *
     * @param displayOrderNumber Integer the display order
     * @since 2005-01-01
     */
    public void setDisplayOrderNumber(Integer displayOrderNumber) {
        this.displayOrderNumber = displayOrderNumber;
    }

    /**
     * Gets the option label.
     *
     * Returns the user-friendly text displayed in UI.
     * This is what users see in dropdowns and lists.
     *
     * @return String the display label
     * @since 2005-01-01
     */
    public String getOptionLabel() {
        return optionLabel;
    }

    /**
     * Sets the option label.
     *
     * Defines the text users see for this option.
     * Should be clear and descriptive.
     *
     * @param optionLabel String the display label
     * @since 2005-01-01
     */
    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    /**
     * Gets the option value.
     *
     * Returns the system value stored when this option
     * is selected. May differ from the display label.
     *
     * @return String the system value
     * @since 2005-01-01
     */
    public String getOptionValue() {
        return optionValue;
    }

    /**
     * Sets the option value.
     *
     * Defines the value stored in the database.
     * Can be a code or abbreviation different from label.
     *
     * @param optionValue String the system value
     * @since 2005-01-01
     */
    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    /**
     * Gets the range start value.
     *
     * Returns the minimum value for range-based options.
     * Null if this is not a range option.
     *
     * @return Integer the range minimum
     * @since 2005-01-01
     */
    public Integer getRangeStartValue() {
        return rangeStartValue;
    }

    /**
     * Sets the range start value.
     *
     * Defines the minimum for range options.
     * Set to null for non-range options.
     *
     * @param rangeStartValue Integer the range minimum
     * @since 2005-01-01
     */
    public void setRangeStartValue(Integer rangeStartValue) {
        this.rangeStartValue = rangeStartValue;
    }

    /**
     * Gets the range end value.
     *
     * Returns the maximum value for range-based options.
     * Null if this is not a range option.
     *
     * @return Integer the range maximum
     * @since 2005-01-01
     */
    public Integer getRangeEndValue() {
        return rangeEndValue;
    }

    /**
     * Sets the range end value.
     *
     * Defines the maximum for range options.
     * Set to null for non-range options.
     *
     * @param rangeEndValue Integer the range maximum
     * @since 2005-01-01
     */
    public void setRangeEndValue(Integer rangeEndValue) {
        this.rangeEndValue = rangeEndValue;
    }

}
