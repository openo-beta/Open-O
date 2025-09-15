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
 * Entity defining the types of criteria available for matching and eligibility.
 *
 * CriteriaType serves as a metadata definition for different kinds of criteria
 * that can be used in the matching system. Each type defines what attribute
 * is being evaluated and how it should be collected and matched.
 *
 * Key features:
 * - Defines available criteria categories
 * - Specifies field types for data validation
 * - Provides default values for consistency
 * - Controls active/inactive status
 * - Program-specific criteria support
 *
 * Common criteria types:
 * - Demographics: Age, Gender, Language, Ethnicity
 * - Clinical: Diagnosis, Acuity Level, Medication Requirements
 * - Social: Housing Status, Income Level, Employment
 * - Behavioral: Substance Use, Violence Risk, Compliance
 * - Administrative: Insurance, Legal Status, Documentation
 *
 * Field types supported:
 * - Text: Free-form text entry
 * - Number: Numeric values for ranges
 * - Select: Dropdown selection from options
 * - Multi-select: Multiple choice selection
 * - Date: Date-based criteria
 * - Boolean: Yes/No criteria
 *
 * The criteria type system enables:
 * - Dynamic form generation for data collection
 * - Validation rules based on field type
 * - Consistent matching across programs
 * - Program-specific customization
 *
 * Database mapping:
 * - Table: criteria_type
 * - Primary key: CRITERIA_TYPE_ID (auto-generated)
 * - Program scope: WL_PROGRAM_ID for program-specific types
 *
 * @since 2005-01-01
 * @see Criteria
 * @see CriteriaTypeOption
 * @see Program
 */
@Entity
@Table(name = "criteria_type")
public class CriteriaType extends AbstractModel<Integer> implements java.io.Serializable {

    /**
     * Primary key for the criteria type.
     * Auto-generated unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CRITERIA_TYPE_ID", unique = true, nullable = false)
    private Integer id;

    /**
     * Display name of the criteria type.
     * Human-readable label for UI display.
     * Examples: "Age", "Gender", "Primary Diagnosis".
     */
    @Column(name = "FIELD_NAME", nullable = false, length = 128)
    private String fieldName;

    /**
     * Data type of the criteria field.
     * Determines input control and validation.
     * Values: "text", "number", "select", "date", "boolean".
     */
    @Column(name = "FIELD_TYPE", nullable = false, length = 128)
    private String fieldType;

    /**
     * Default value for new criteria instances.
     * Pre-populated value for common scenarios.
     * Improves data entry efficiency.
     */
    @Column(name = "DEFAULT_VALUE")
    private String defaultValue;

    /**
     * Active status flag.
     * True if this criteria type is currently available.
     * False for deprecated or disabled types.
     */
    @Column(name = "ACTIVE", nullable = false)
    private Boolean active;

    /**
     * Associated waitlist program ID.
     * Links criteria type to specific program.
     * Null for system-wide criteria types.
     */
    @Column(name = "WL_PROGRAM_ID")
    private Integer wlProgramId;

    /**
     * Ad-hoc capability flag.
     * True if criteria of this type can be created dynamically.
     * False for predefined criteria only.
     */
    @Column(name = "CAN_BE_ADHOC", nullable = false)
    private Boolean canBeAdhoc;

    /**
     * Gets the unique identifier of this criteria type.
     *
     * Returns the primary key from the criteria_type table.
     * Used to reference this type in criteria definitions.
     *
     * @return Integer the criteria type ID, null if not persisted
     * @since 2005-01-01
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this criteria type.
     *
     * Updates the primary key value. Typically called
     * by persistence framework during save/load operations.
     *
     * @param id Integer the new criteria type ID
     * @since 2005-01-01
     */
    public void setId(Integer id) {
        this.id = id;
    }


    /**
     * Default constructor for creating new CriteriaType.
     *
     * Creates an empty criteria type instance.
     * All fields must be set before persisting.
     *
     * @since 2005-01-01
     */
    public CriteriaType() {
        // Empty constructor for JPA/Hibernate
    }

    /**
     * Minimal constructor with required fields.
     *
     * Creates a criteria type with essential attributes.
     * Default value and program ID can be set later.
     *
     * @param fieldName String the display name
     * @param fieldType String the data type (text, number, etc.)
     * @param active Boolean whether type is active
     * @param canBeAdhoc Boolean whether ad-hoc creation is allowed
     * @since 2005-01-01
     */
    public CriteriaType(String fieldName, String fieldType, Boolean active,
                        Boolean canBeAdhoc) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.active = active;
        this.canBeAdhoc = canBeAdhoc;
    }

    /**
     * Full constructor with all fields.
     *
     * Creates a complete criteria type with all attributes
     * including optional default value and program association.
     *
     * @param fieldName String the display name
     * @param fieldType String the data type
     * @param defaultValue String the default value
     * @param active Boolean whether type is active
     * @param wlProgramId Integer associated program ID
     * @param canBeAdhoc Boolean whether ad-hoc creation is allowed
     * @since 2005-01-01
     */
    public CriteriaType(String fieldName, String fieldType,
                        String defaultValue, Boolean active, Integer wlProgramId,
                        Boolean canBeAdhoc) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.defaultValue = defaultValue;
        this.active = active;
        this.wlProgramId = wlProgramId;
        this.canBeAdhoc = canBeAdhoc;
    }

    /**
     * Gets the field name of this criteria type.
     *
     * Returns the human-readable name displayed in UI.
     * This is the label users see when selecting criteria.
     *
     * @return String the field name
     * @since 2005-01-01
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name of this criteria type.
     *
     * Updates the display label for this criteria type.
     * Should be clear and descriptive for users.
     *
     * @param fieldName String the new field name
     * @since 2005-01-01
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the field type of this criteria.
     *
     * Returns the data type that determines input control
     * and validation rules.
     *
     * Common values:
     * - "text": Free-form text input
     * - "number": Numeric input with validation
     * - "select": Dropdown selection
     * - "date": Date picker
     * - "boolean": Checkbox or yes/no
     *
     * @return String the field type
     * @since 2005-01-01
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * Sets the field type of this criteria.
     *
     * Defines what kind of data this criteria accepts
     * and how it should be presented in the UI.
     *
     * @param fieldType String the data type
     * @since 2005-01-01
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Gets the default value for this criteria type.
     *
     * Returns the pre-populated value used when creating
     * new criteria instances. Helps standardize common values.
     *
     * @return String the default value, null if none
     * @since 2005-01-01
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value for this criteria type.
     *
     * Defines what value should be pre-populated
     * when creating new criteria of this type.
     *
     * @param defaultValue String the default value
     * @since 2005-01-01
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Gets the active status of this criteria type.
     *
     * Returns whether this type is currently available
     * for use in creating new criteria.
     *
     * @return Boolean true if active, false if disabled
     * @since 2005-01-01
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Sets the active status of this criteria type.
     *
     * Enables or disables this type for use.
     * Inactive types are hidden from selection.
     *
     * @param active Boolean true to enable, false to disable
     * @since 2005-01-01
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Gets the waitlist program ID for this criteria type.
     *
     * Returns the program this type is associated with.
     * Null indicates system-wide availability.
     *
     * @return Integer the program ID, null if system-wide
     * @since 2005-01-01
     */
    public Integer getWlProgramId() {
        return wlProgramId;
    }

    /**
     * Sets the waitlist program ID for this criteria type.
     *
     * Associates this type with a specific program.
     * Set to null for system-wide criteria types.
     *
     * @param wlProgramId Integer the program ID
     * @since 2005-01-01
     */
    public void setWlProgramId(Integer wlProgramId) {
        this.wlProgramId = wlProgramId;
    }

    /**
     * Gets whether this criteria type supports ad-hoc creation.
     *
     * Returns true if criteria of this type can be created
     * dynamically during runtime operations.
     *
     * @return Boolean true if ad-hoc capable, false otherwise
     * @since 2005-01-01
     */
    public Boolean getCanBeAdhoc() {
        return canBeAdhoc;
    }

    /**
     * Sets whether this criteria type supports ad-hoc creation.
     *
     * Determines if users can create criteria of this type
     * on-the-fly versus using predefined criteria only.
     *
     * @param canBeAdhoc Boolean true to allow ad-hoc, false to restrict
     * @since 2005-01-01
     */
    public void setCanBeAdhoc(Boolean canBeAdhoc) {
        this.canBeAdhoc = canBeAdhoc;
    }

}
