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
 * Entity representing matching criteria for vacancy and program eligibility.
 *
 * Criteria define the requirements and preferences used to match clients
 * with appropriate programs, services, or bed vacancies. They support both
 * exact matching and range-based matching for various client attributes.
 *
 * Key features:
 * - Flexible criteria types through CriteriaType association
 * - Support for single values and ranges
 * - Weighted scoring for match optimization
 * - Template and vacancy-specific criteria
 * - Ad-hoc criteria capability
 *
 * Criteria types include:
 * - Demographics (age, gender, language)
 * - Clinical needs (diagnosis, acuity level)
 * - Program requirements (sobriety, medication compliance)
 * - Social factors (housing status, income)
 * - Behavioral factors (violence risk, substance use)
 *
 * Matching mechanisms:
 * - Exact value matching for categorical criteria
 * - Range matching for numerical criteria (age, scores)
 * - Weighted scoring for best-fit matching
 * - Required vs preferred criteria
 *
 * The match score weight allows prioritization:
 * - Weight > 1.0: Higher importance criteria
 * - Weight = 1.0: Standard importance (default)
 * - Weight < 1.0: Lower importance criteria
 * - Weight = 0: Optional/informational only
 *
 * Associations:
 * - CriteriaType: Defines what is being evaluated
 * - VacancyTemplate: Template-level requirements
 * - Vacancy: Specific vacancy requirements
 *
 * Database mapping:
 * - Table: criteria
 * - Primary key: CRITERIA_ID (auto-generated)
 * - Foreign keys: CRITERIA_TYPE_ID, TEMPLATE_ID, VACANCY_ID
 *
 * @since 2005-01-01
 * @see CriteriaType
 * @see Vacancy
 * @see VacancyTemplate
 */
@Entity
@Table(name = "criteria")
public class Criteria extends AbstractModel<Integer> implements java.io.Serializable {

    /**
     * Primary key for the criteria record.
     * Auto-generated unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CRITERIA_ID", unique = true, nullable = false)
    private Integer id;

    /**
     * Reference to the criteria type.
     * Defines what attribute is being evaluated.
     * Required field linking to criteria_type table.
     */
    @Column(name = "CRITERIA_TYPE_ID", nullable = false)
    private Integer criteriaTypeId;

    /**
     * Single value for exact match criteria.
     * Used when matching requires a specific value.
     * Examples: "Male", "English", "Employed".
     */
    @Column(name = "CRITERIA_VALUE")
    private String criteriaValue;

    /**
     * Start value for range-based criteria.
     * Defines minimum acceptable value.
     * Example: Minimum age of 18.
     */
    @Column(name = "RANGE_START_VALUE")
    private Integer rangeStartValue;

    /**
     * End value for range-based criteria.
     * Defines maximum acceptable value.
     * Example: Maximum age of 65.
     */
    @Column(name = "RANGE_END_VALUE")
    private Integer rangeEndValue;

    /**
     * Reference to vacancy template.
     * Links criteria to a template for reusable requirements.
     * Null if criteria is vacancy-specific.
     */
    @Column(name = "TEMPLATE_ID")
    private Integer templateId;

    /**
     * Reference to specific vacancy.
     * Links criteria to a particular bed/slot.
     * Null if criteria is template-level.
     */
    @Column(name = "VACANCY_ID")
    private Integer vacancyId;

    /**
     * Weight factor for match scoring.
     * Determines importance in matching algorithm.
     * Default 1.0, higher values increase importance.
     */
    @Column(name = "MATCH_SCORE_WEIGHT", nullable = false, precision = 22, scale = 0)
    private Double matchScoreWeight = 1.0;

    /**
     * Flag indicating if criteria can be ad-hoc.
     * 0 = Standard criteria only.
     * 1 = Can be created on-the-fly.
     */
    @Column(name = "CAN_BE_ADHOC", nullable = false)
    private Integer canBeAdhoc = 0;

    /**
     * Gets the unique identifier of this criteria.
     *
     * Returns the primary key from the criteria table.
     * Used to uniquely reference this criteria record.
     *
     * @return Integer the criteria ID, null if not persisted
     * @since 2005-01-01
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this criteria.
     *
     * Updates the primary key value. Typically called
     * by persistence framework during save/load operations.
     *
     * @param id Integer the new criteria ID
     * @since 2005-01-01
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Default constructor for creating new Criteria.
     *
     * Creates an empty criteria instance with default values.
     * Match score weight defaults to 1.0 and canBeAdhoc to 0.
     *
     * @since 2005-01-01
     */
    public Criteria() {
        // Default values set in field declarations
    }

    /**
     * Minimal constructor with required fields.
     *
     * Creates criteria with essential fields only.
     * Used when specific values or ranges will be set later.
     *
     * @param criteriaTypeId Integer the type of criteria
     * @param matchScoreWeight Double the importance weight
     * @param canBeAdhoc Integer whether ad-hoc creation is allowed
     * @since 2005-01-01
     */
    public Criteria(Integer criteriaTypeId, Double matchScoreWeight, Integer canBeAdhoc) {
        this.criteriaTypeId = criteriaTypeId;
        this.matchScoreWeight = matchScoreWeight;
        this.canBeAdhoc = canBeAdhoc;
    }

    /**
     * Full constructor with all fields.
     *
     * Creates a complete criteria instance with all attributes.
     * Use for exact value criteria (with criteriaValue) or
     * range criteria (with rangeStartValue and rangeEndValue).
     *
     * @param criteriaTypeId Integer the type of criteria
     * @param criteriaValue String exact match value
     * @param rangeStartValue Integer minimum for range
     * @param rangeEndValue Integer maximum for range
     * @param templateId Integer associated template
     * @param vacancyId Integer associated vacancy
     * @param matchScoreWeight Double importance weight
     * @param canBeAdhoc Integer ad-hoc capability flag
     * @since 2005-01-01
     */
    public Criteria(Integer criteriaTypeId, String criteriaValue, Integer rangeStartValue, Integer rangeEndValue, Integer templateId, Integer vacancyId, Double matchScoreWeight, Integer canBeAdhoc) {
        this.criteriaTypeId = criteriaTypeId;
        this.criteriaValue = criteriaValue;
        this.rangeStartValue = rangeStartValue;
        this.rangeEndValue = rangeEndValue;
        this.templateId = templateId;
        this.vacancyId = vacancyId;
        this.matchScoreWeight = matchScoreWeight;
        this.canBeAdhoc = canBeAdhoc;
    }

    /**
     * Gets the criteria type identifier.
     *
     * Returns the ID that defines what attribute this
     * criteria evaluates (age, gender, diagnosis, etc.).
     *
     * @return Integer the criteria type ID
     * @since 2005-01-01
     */
    public Integer getCriteriaTypeId() {
        return criteriaTypeId;
    }

    /**
     * Sets the criteria type identifier.
     *
     * Specifies what attribute this criteria evaluates.
     * Must reference a valid criteria_type record.
     *
     * @param criteriaTypeId Integer the criteria type ID
     * @since 2005-01-01
     */
    public void setCriteriaTypeId(Integer criteriaTypeId) {
        this.criteriaTypeId = criteriaTypeId;
    }

    /**
     * Gets the exact match value for this criteria.
     *
     * Returns the specific value required for matching.
     * Used for categorical criteria like gender or language.
     * Null if this is a range-based criteria.
     *
     * @return String the exact match value
     * @since 2005-01-01
     */
    public String getCriteriaValue() {
        return criteriaValue;
    }

    /**
     * Sets the exact match value for this criteria.
     *
     * Specifies the required value for categorical matching.
     * Set to null for range-based criteria.
     *
     * @param criteriaValue String the exact match value
     * @since 2005-01-01
     */
    public void setCriteriaValue(String criteriaValue) {
        this.criteriaValue = criteriaValue;
    }

    /**
     * Gets the minimum value for range criteria.
     *
     * Returns the lower bound for range-based matching.
     * Example: Minimum age requirement.
     * Null if this is an exact value criteria.
     *
     * @return Integer the minimum acceptable value
     * @since 2005-01-01
     */
    public Integer getRangeStartValue() {
        return rangeStartValue;
    }

    /**
     * Sets the minimum value for range criteria.
     *
     * Defines the lower bound for range matching.
     * Set to null for exact value criteria.
     *
     * @param rangeStartValue Integer the minimum value
     * @since 2005-01-01
     */
    public void setRangeStartValue(Integer rangeStartValue) {
        this.rangeStartValue = rangeStartValue;
    }

    /**
     * Gets the maximum value for range criteria.
     *
     * Returns the upper bound for range-based matching.
     * Example: Maximum age limit.
     * Null if this is an exact value criteria.
     *
     * @return Integer the maximum acceptable value
     * @since 2005-01-01
     */
    public Integer getRangeEndValue() {
        return rangeEndValue;
    }

    /**
     * Sets the maximum value for range criteria.
     *
     * Defines the upper bound for range matching.
     * Set to null for exact value criteria.
     *
     * @param rangeEndValue Integer the maximum value
     * @since 2005-01-01
     */
    public void setRangeEndValue(Integer rangeEndValue) {
        this.rangeEndValue = rangeEndValue;
    }

    /**
     * Gets the vacancy template identifier.
     *
     * Returns the template this criteria belongs to.
     * Template criteria apply to all vacancies using that template.
     * Null if this is vacancy-specific criteria.
     *
     * @return Integer the template ID
     * @since 2005-01-01
     */
    public Integer getTemplateId() {
        return templateId;
    }

    /**
     * Sets the vacancy template identifier.
     *
     * Associates this criteria with a template.
     * Set to null for vacancy-specific criteria.
     *
     * @param templateId Integer the template ID
     * @since 2005-01-01
     */
    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    /**
     * Gets the vacancy identifier.
     *
     * Returns the specific vacancy this criteria applies to.
     * Vacancy criteria override template criteria.
     * Null if this is template-level criteria.
     *
     * @return Integer the vacancy ID
     * @since 2005-01-01
     */
    public Integer getVacancyId() {
        return vacancyId;
    }

    /**
     * Sets the vacancy identifier.
     *
     * Associates this criteria with a specific vacancy.
     * Set to null for template-level criteria.
     *
     * @param vacancyId Integer the vacancy ID
     * @since 2005-01-01
     */
    public void setVacancyId(Integer vacancyId) {
        this.vacancyId = vacancyId;
    }

    /**
     * Gets the match score weight.
     *
     * Returns the importance factor for this criteria
     * in the matching algorithm. Higher weights indicate
     * more important criteria.
     *
     * Weight scale:
     * - > 1.0: High importance
     * - = 1.0: Standard importance (default)
     * - < 1.0: Low importance
     * - = 0.0: Informational only
     *
     * @return Double the weight factor
     * @since 2005-01-01
     */
    public Double getMatchScoreWeight() {
        return matchScoreWeight;
    }

    /**
     * Sets the match score weight.
     *
     * Defines how important this criteria is in matching.
     * Use higher values for critical requirements,
     * lower values for preferences.
     *
     * @param matchScoreWeight Double the weight factor
     * @since 2005-01-01
     */
    public void setMatchScoreWeight(Double matchScoreWeight) {
        this.matchScoreWeight = matchScoreWeight;
    }

    /**
     * Gets the ad-hoc capability flag.
     *
     * Returns whether this criteria can be created
     * on-the-fly during matching processes.
     *
     * Values:
     * - 0: Standard criteria only
     * - 1: Can be created ad-hoc
     *
     * @return Integer the ad-hoc flag (0 or 1)
     * @since 2005-01-01
     */
    public Integer getCanBeAdhoc() {
        return canBeAdhoc;
    }

    /**
     * Sets the ad-hoc capability flag.
     *
     * Specifies whether this criteria can be created
     * dynamically during runtime operations.
     *
     * @param canBeAdhoc Integer 0 for standard, 1 for ad-hoc capable
     * @since 2005-01-01
     */
    public void setCanBeAdhoc(Integer canBeAdhoc) {
        this.canBeAdhoc = canBeAdhoc;
    }

}
