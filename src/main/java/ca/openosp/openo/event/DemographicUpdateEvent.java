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
package ca.openosp.openo.event;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.context.ApplicationEvent;

/**
 * Represents an event that is published when a demographic record is updated in the system.
 * This event signals that an existing patient or user demographic profile has been modified.
 * <p>
 * As a subclass of {@link ApplicationEvent}, it leverages the Spring Framework's event infrastructure.
 * Event listeners can subscribe to this event to perform actions in response to demographic updates,
 * such as synchronizing data with external systems, auditing changes, or refreshing cached information.
 * <p>
 * The event carries the unique identifier of the updated demographic record, which is essential for
 * listeners to identify the record that was changed.
 */
public class DemographicUpdateEvent extends ApplicationEvent {

    Logger logger = MiscUtils.getLogger();

    /**
     * The unique identifier for the demographic record that has been updated.
     * This number is the core data of the event, enabling listeners to know which record
     * was modified.
     */
    private Integer demographicNo;

    /**
     * Constructs a new {@code DemographicUpdateEvent}.
     *
     * @param source The object on which the event initially occurred. This is typically the service
     *               or component that is responsible for the update operation.
     * @param demographicNo The unique identifier for the demographic record that was updated. This
     *                      is a required field and should not be null.
     */
    public DemographicUpdateEvent(Object source, Integer demographicNo) {
        super(source);
        this.demographicNo = demographicNo;
    }

    /**
     * Retrieves the unique identifier of the updated demographic record.
     * Listeners can use this number to fetch the latest version of the demographic data
     * for processing.
     *
     * @return The unique identifier of the demographic record.
     */
    public Integer getDemographicNo() {
        return demographicNo;
    }

    /**
     * Sets the unique identifier for the demographic record.
     * This method allows for changing the demographic number after the event has been constructed.
     * However, in a typical immutable event design, this property would be final and set only
     * through the constructor. This setter is provided for flexibility but should be used with
     * caution.
     *
     * @param demographicNo The unique identifier to be set for the demographic record.
     */
    public void setDemographicNo(Integer demographicNo) {
        this.demographicNo = demographicNo;
    }

}
