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

import java.util.Collection;

/**
 * Healthcare condition entity representing patient medical conditions and associated comorbidities.
 * This entity extends ClinicalFactor to represent specific medical conditions that affect patients,
 * including the ability to track related comorbid conditions.
 *
 * A Condition represents a diagnosed medical state, disease, symptom, or health issue that
 * requires clinical attention, monitoring, or treatment. The entity supports complex
 * condition relationships through its collection of comorbid conditions, enabling
 * comprehensive patient health status tracking.
 *
 * @see ClinicalFactor
 * @since November 1, 2004
 */
public class Condition extends ClinicalFactor {

    /**
     * Collection of comorbid conditions associated with this primary condition.
     * Comorbid conditions are additional medical conditions that exist simultaneously
     * with the primary condition and may influence treatment decisions.
     */
    private Collection<Condition> coMorbidConditions;

    /**
     * Default constructor creating an empty Condition instance.
     * All fields will be initialized to their default values.
     */
    public Condition() {
        super();
    }

    /**
     * Gets the collection of comorbid conditions associated with this condition.
     *
     * @return Collection&lt;Condition&gt; the collection of comorbid conditions, may be null or empty
     */
    public Collection<Condition> getCoMorbidConditions() {
        return coMorbidConditions;
    }

    /**
     * Sets the collection of comorbid conditions associated with this condition.
     *
     * @param coMorbidConditions Collection&lt;Condition&gt; the collection of comorbid conditions
     */
    public void setCoMorbidConditions(Collection<Condition> coMorbidConditions) {
        this.coMorbidConditions = coMorbidConditions;
    }

    /**
     * Adds a comorbid condition to this condition's collection of related conditions.
     *
     * @param condition Condition the comorbid condition to add
     */
    public void addCoMorbidCondition(Condition condition) {
        if (coMorbidConditions != null) {
            coMorbidConditions.add(condition);
        }
    }

    /**
     * Removes a comorbid condition from this condition's collection.
     *
     * @param condition Condition the comorbid condition to remove
     * @return boolean true if the condition was removed, false if it was not found
     */
    public boolean removeCoMorbidCondition(Condition condition) {
        if (coMorbidConditions != null) {
            return coMorbidConditions.remove(condition);
        }
        return false;
    }

    /**
     * Checks if this condition has any associated comorbid conditions.
     *
     * @return boolean true if there are comorbid conditions, false otherwise
     */
    public boolean hasCoMorbidConditions() {
        return coMorbidConditions != null && !coMorbidConditions.isEmpty();
    }
}