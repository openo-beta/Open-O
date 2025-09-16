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
package ca.openosp.openo.match.vacancy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Healthcare provider vacancy requirements and matching template data.
 *
 * <p>This class encapsulates healthcare provider appointment slot (vacancy) information
 * and associated matching criteria used by the OpenO EMR matching system to pair
 * patients with appropriate healthcare providers. Each vacancy contains detailed
 * requirements that patients must meet for optimal matching.</p>
 *
 * <p>Healthcare Vacancy Components:
 * <ul>
 *   <li>Provider appointment slot identifier and program association</li>
 *   <li>Detailed matching template with patient requirements</li>
 *   <li>Flexible criteria supporting complex healthcare matching scenarios</li>
 *   <li>Program-specific eligibility and preference rules</li>
 * </ul>
 * </p>
 *
 * <p>Matching Template Structure: The vacancy data contains a comprehensive
 * template system that defines what patient characteristics are required,
 * preferred, or acceptable for this healthcare provider slot. Each template
 * element includes weighting and matching logic for optimal patient selection.</p>
 *
 * <p>Program Integration: Vacancies are associated with specific healthcare
 * programs, enabling program-specific matching rules and ensuring patients
 * are matched to appropriate care providers within their enrolled programs.</p>
 *
 * @see ca.openosp.openo.match.Matcher
 * @see VacancyTemplateData
 * @see ca.openosp.openo.match.client.ClientData
 * @see ca.openosp.openo.match.VacancyClientMatch
 * @since 2012-11-10
 */
public class VacancyData {
    /** Healthcare provider vacancy identifier */
    private int vacancy_id;

    /** Healthcare program identifier for this vacancy */
    private int program_id;

    /** Matching template with patient requirements and criteria */
    private Map<String, VacancyTemplateData> vacancyData = new HashMap<String, VacancyTemplateData>();

    /**
     * Gets the healthcare provider vacancy identifier.
     *
     * @return int the unique appointment slot ID from the healthcare system
     */
    public int getVacancy_id() {
        return vacancy_id;
    }

    /**
     * Sets the healthcare provider vacancy identifier.
     *
     * @param vacancy_id int the appointment slot ID
     */
    public void setVacancy_id(int vacancy_id) {
        this.vacancy_id = vacancy_id;
    }

    /**
     * Gets the healthcare program identifier for this vacancy.
     *
     * @return int the program ID that owns this provider appointment slot
     */
    public int getProgram_id() {
        return program_id;
    }

    /**
     * Sets the healthcare program identifier for this vacancy.
     *
     * @param program_id int the program ID that owns this appointment slot
     */
    public void setProgram_id(int program_id) {
        this.program_id = program_id;
    }

    /**
     * Gets the matching template with patient requirements and criteria.
     *
     * <p>This map contains the detailed matching template that defines what
     * patient characteristics are required for this healthcare provider vacancy.
     * Each key represents a patient characteristic category (demographics, medical
     * conditions, preferences), and the corresponding VacancyTemplateData defines
     * the acceptable values and matching logic.</p>
     *
     * <p>Common Healthcare Template Keys:
     * <ul>
     *   <li>Demographics: age_range, gender, location_proximity</li>
     *   <li>Medical: diagnosis_codes, severity_levels, mobility_requirements</li>
     *   <li>Program: eligibility_criteria, priority_categories</li>
     *   <li>Preferences: language_preference, appointment_timing</li>
     * </ul>
     * </p>
     *
     * @return Map<String, VacancyTemplateData> matching template with requirements
     */
    public Map<String, VacancyTemplateData> getVacancyData() {
        return vacancyData;
    }

    /**
     * Sets the matching template with patient requirements and criteria.
     *
     * @param vacancyData Map<String, VacancyTemplateData> the matching template
     */
    public void setVacancyData(Map<String, VacancyTemplateData> vacancyData) {
        this.vacancyData = vacancyData;
    }

    /**
     * Returns a string representation of this healthcare vacancy for debugging and logging.
     *
     * <p>The string representation includes the vacancy ID, program ID, and a
     * truncated view of the matching template to prevent excessive output while
     * providing useful debugging information.</p>
     *
     * @return String formatted representation of vacancy data (limited to 16 template entries)
     */
    @Override
    public String toString() {
        final int maxLen = 16;
        return "VacancyData [vacancy_id="
                + vacancy_id
                + ", program_id="
                + program_id
                + ", vacancyData="
                + (vacancyData != null ? toString(vacancyData.entrySet(),
                maxLen) : null) + "]";
    }

    /**
     * Helper method to create truncated string representation of vacancy template collection.
     *
     * <p>This method limits the output to prevent excessive logging while still
     * providing useful debugging information about vacancy requirements.</p>
     *
     * @param collection Collection<?> the vacancy template entries to format
     * @param maxLen int maximum number of entries to include in output
     * @return String formatted string representation of the collection
     */
    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext()
                && i < maxLen; i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }

}
