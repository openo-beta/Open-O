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
 * Entity representing selectable options for multi-choice criteria.
 *
 * CriteriaSelectionOption provides the available choices for criteria
 * that allow selection from predefined options. This enables structured
 * data entry and consistent matching for categorical criteria.
 *
 * Key features:
 * - Defines valid options for selection criteria
 * - Supports multiple options per criteria
 * - Enables dropdown/checkbox UI generation
 * - Ensures data consistency in matching
 *
 * Common use cases:
 * - Language preferences (English, French, Spanish, etc.)
 * - Gender options (Male, Female, Other, etc.)
 * - Program types (Residential, Outpatient, Day Program)
 * - Diagnosis categories (Mental Health, Substance Use, etc.)
 * - Service levels (Emergency, Urgent, Routine)
 *
 * The option values are used in:
 * - User interface generation for selection controls
 * - Validation of criteria values
 * - Matching algorithms for client-program fit
 * - Reporting and analytics
 *
 * Relationship to Criteria:
 * - Many options can belong to one criteria
 * - Criteria references selected option via criteriaValue
 * - Options provide the valid domain of values
 *
 * Database mapping:
 * - Table: criteria_selection_option
 * - Primary key: SELECT_OPTION_ID (auto-generated)
 * - Foreign key: CRITERIA_ID links to criteria table
 *
 * @since 2005-01-01
 * @see Criteria
 * @see CriteriaType
 */
@Entity
@Table(name = "criteria_selection_option")
public class CriteriaSelectionOption extends AbstractModel<Integer> implements java.io.Serializable {

    /**
     * Primary key for the selection option.
     * Auto-generated unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SELECT_OPTION_ID", unique = true, nullable = false)
    private Integer id;

    /**
     * Reference to the parent criteria.
     * Links this option to its criteria definition.
     * Required field for maintaining relationship.
     */
    @Column(name = "CRITERIA_ID", nullable = false)
    private Integer criteriaId;

    /**
     * The selectable option value.
     * Represents one choice in the criteria's domain.
     * Examples: "Male", "English", "Employed".
     */
    @Column(name = "OPTION_VALUE")
    private String optionValue;

    /**
     * Gets the unique identifier of this selection option.
     *
     * Returns the primary key from the criteria_selection_option table.
     * Used to uniquely reference this option.
     *
     * @return Integer the option ID, null if not persisted
     * @since 2005-01-01
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this selection option.
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
     * Default constructor for creating new CriteriaSelectionOption.
     *
     * Creates an empty option instance. The criteriaId and
     * optionValue must be set before persisting.
     *
     * @since 2005-01-01
     */
    public CriteriaSelectionOption() {
        // Empty constructor for JPA/Hibernate
    }

    /**
     * Minimal constructor with required criteria reference.
     *
     * Creates an option linked to a specific criteria.
     * The option value should be set before use.
     *
     * @param criteriaId Integer the parent criteria ID
     * @since 2005-01-01
     */
    public CriteriaSelectionOption(Integer criteriaId) {
        this.criteriaId = criteriaId;
    }

    /**
     * Full constructor with all fields.
     *
     * Creates a complete selection option with criteria
     * reference and the actual option value.
     *
     * @param criteriaId Integer the parent criteria ID
     * @param optionValue String the selectable option text
     * @since 2005-01-01
     */
    public CriteriaSelectionOption(Integer criteriaId, String optionValue) {
        this.criteriaId = criteriaId;
        this.optionValue = optionValue;
    }


    /**
     * Gets the parent criteria identifier.
     *
     * Returns the ID of the criteria this option belongs to.
     * Multiple options can share the same criteria ID.
     *
     * @return Integer the criteria ID
     * @since 2005-01-01
     */
    public Integer getCriteriaId() {
        return criteriaId;
    }

    /**
     * Sets the parent criteria identifier.
     *
     * Links this option to a specific criteria.
     * Must reference a valid criteria record.
     *
     * @param criteriaId Integer the criteria ID
     * @since 2005-01-01
     */
    public void setCriteriaId(Integer criteriaId) {
        this.criteriaId = criteriaId;
    }

    /**
     * Gets the option value text.
     *
     * Returns the actual selectable value for this option.
     * This is what users see and select in the interface.
     *
     * Examples:
     * - "Male" for gender criteria
     * - "English" for language criteria
     * - "Full-time" for employment criteria
     *
     * @return String the option value text
     * @since 2005-01-01
     */
    public String getOptionValue() {
        return optionValue;
    }

    /**
     * Sets the option value text.
     *
     * Defines what this option represents.
     * Should be meaningful to end users.
     *
     * @param optionValue String the option text
     * @since 2005-01-01
     */
    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

}
