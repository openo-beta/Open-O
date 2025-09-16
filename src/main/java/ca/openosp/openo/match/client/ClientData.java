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
package ca.openosp.openo.match.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Patient (client) assessment data container for healthcare matching algorithms.
 *
 * <p>This class encapsulates patient information and assessment responses that are
 * used by the OpenO EMR matching system to pair patients with appropriate healthcare
 * providers. The data structure supports flexible patient characteristic storage
 * for complex healthcare matching scenarios.</p>
 *
 * <p>Healthcare Data Elements:
 * <ul>
 *   <li>Patient demographics and identifiers</li>
 *   <li>Assessment form responses and clinical data</li>
 *   <li>Flexible key-value pairs for diverse patient characteristics</li>
 *   <li>Support for multiple assessment forms per patient</li>
 * </ul>
 * </p>
 *
 * <p>Matching Integration: The patient data stored in this class is directly
 * compared against healthcare provider vacancy requirements through the
 * {@link ca.openosp.openo.match.Matcher} algorithms. The flexible data structure
 * allows for complex matching criteria including demographics, medical conditions,
 * location preferences, and program-specific requirements.</p>
 *
 * <p>Data Sources: Patient data typically originates from healthcare assessment
 * forms, demographic records, and clinical evaluations that have been completed
 * as part of the patient's enrollment in healthcare programs.</p>
 *
 * @see ca.openosp.openo.match.Matcher
 * @see ca.openosp.openo.match.vacancy.VacancyData
 * @see ca.openosp.openo.match.VacancyClientMatch
 * @since 2012-11-10
 */
public class ClientData {
    /** Patient identifier (demographic number) from the healthcare system */
    private int clientId;

    /** Assessment form identifier that provided this patient data */
    private int formId;

    /** Patient characteristics and assessment responses as key-value pairs */
    private Map<String, String> clientData = new HashMap<String, String>();

    /**
     * Gets the patient identifier for this assessment data.
     *
     * @return int the patient demographic number from the healthcare system
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Sets the patient identifier for this assessment data.
     *
     * @param clientId int the patient demographic number
     */
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the assessment form identifier that provided this patient data.
     *
     * @return int the form ID that collected these patient characteristics
     */
    public int getFormId() {
        return formId;
    }

    /**
     * Sets the assessment form identifier that provided this patient data.
     *
     * @param formId int the form ID that collected this assessment data
     */
    public void setFormId(int formId) {
        this.formId = formId;
    }

    /**
     * Gets the patient characteristics and assessment data for matching.
     *
     * <p>This map contains key-value pairs representing various patient
     * characteristics that can be matched against healthcare provider vacancy
     * requirements. Keys typically represent assessment questions or demographic
     * categories, while values contain the patient's specific responses or data.</p>
     *
     * <p>Common Healthcare Data Keys:
     * <ul>
     *   <li>Demographics: age, gender, postal_code, location</li>
     *   <li>Medical: conditions, severity, mobility, special_needs</li>
     *   <li>Preferences: language, provider_type, appointment_times</li>
     *   <li>Program-specific: eligibility_criteria, priority_level</li>
     * </ul>
     * </p>
     *
     * @return Map<String, String> patient characteristics as key-value pairs for matching
     */
    public Map<String, String> getClientData() {
        return clientData;
    }

    /**
     * Returns a string representation of this patient data for debugging and logging.
     *
     * <p>The string representation includes the patient ID, form ID, and a
     * truncated view of the patient characteristics to prevent excessive
     * output while maintaining healthcare data privacy.</p>
     *
     * @return String formatted representation of patient data (limited to 20 entries)
     */
    @Override
    public String toString() {
        final int maxLen = 20;
        return "ClientData [clientId="
                + clientId
                + ", formId="
                + formId
                + ", clientData="
                + (clientData != null ? toString(clientData.entrySet(), maxLen)
                : null) + "]";
    }

    /**
     * Helper method to create truncated string representation of patient data collection.
     *
     * <p>This method limits the output to prevent excessive logging while still
     * providing useful debugging information about patient characteristics.</p>
     *
     * @param collection Collection<?> the patient data entries to format
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
