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

package ca.openosp.openo.entities;

/**
 * Test entity representing laboratory test definitions and results within OpenO EMR.
 * This entity serves as a data transfer object for laboratory test information,
 * supporting test ordering, result management, and clinical decision-making processes
 * in healthcare settings.
 *
 * <p>Laboratory tests are fundamental components of medical diagnosis and patient
 * monitoring. This entity provides a flexible framework for representing various
 * types of laboratory tests, their descriptions, result values, and frequency
 * recommendations for ongoing patient care.
 *
 * <p>Key laboratory test features include:
 * <ul>
 * <li>Test identification with unique IDs for laboratory information systems
 * <li>Descriptive test names for clinical recognition and ordering
 * <li>Result value storage with default "NO DATA" status for unreported tests
 * <li>Frequency recommendations for routine monitoring and follow-up testing
 * <li>Integration with clinical workflows and diagnostic protocols
 * </ul>
 *
 * <p>Common laboratory test categories supported include:
 * <ul>
 * <li>Blood chemistry panels (glucose, electrolytes, liver function)
 * <li>Hematology tests (CBC, differential counts)
 * <li>Microbiology cultures and sensitivity testing
 * <li>Immunology and serology tests
 * <li>Therapeutic drug monitoring levels
 * <li>Hormone and endocrine function tests
 * </ul>
 *
 * <p>The Test entity supports clinical decision-making by providing structured
 * test information that can be integrated with clinical protocols, reminder systems,
 * and quality improvement initiatives for optimal patient care management.
 *
 * @see ca.openosp.openo.entities.Patient
 * @since November 1, 2004
 */
public class Test {
    private String id;
    private String description;
    private String value = "NO DATA";
    private String frequency;

    /**
     * Default constructor for Test entity.
     * Creates a new Test instance with value field initialized to "NO DATA" as default,
     * and all other fields initialized to null.
     */
    public Test() {
    }

    /**
     * Gets the unique identifier for this laboratory test.
     * This ID is used for linking test orders, results, and clinical protocols
     * within the laboratory information system.
     *
     * @return String the test ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this laboratory test.
     *
     * @param id String the test ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the descriptive name of the laboratory test.
     * This human-readable description is used for test identification,
     * clinical ordering interfaces, and result reporting.
     *
     * @return String the test description/name
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the descriptive name of the laboratory test.
     * Should be a clear, clinically meaningful test name that healthcare
     * providers can easily recognize and interpret.
     *
     * @param description String the test description/name
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the test result value.
     * Returns the actual laboratory result if available, or "NO DATA" if no
     * result has been recorded. The value format depends on the test type
     * (numeric, qualitative, or descriptive).
     *
     * @return String the test result value, defaulting to "NO DATA"
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the test result value.
     * This field can contain numeric results, qualitative results (positive/negative),
     * or descriptive findings depending on the test type.
     *
     * @param value String the test result value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the recommended frequency for this laboratory test.
     * This field indicates how often the test should be repeated for
     * routine monitoring, chronic disease management, or medication monitoring.
     *
     * @return String the recommended test frequency (e.g., "annually", "every 3 months", "as needed")
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * Sets the recommended frequency for this laboratory test.
     * Common frequencies include periodic intervals for monitoring chronic conditions,
     * medication levels, or routine health maintenance protocols.
     *
     * @param frequency String the recommended test frequency
     */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

}
