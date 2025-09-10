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
 * Represents an event that is published when a new demographic record is created in the system.
 * This event is fired to signal that a new patient or user demographic profile has been successfully added.
 * <p>
 * As a subclass of {@link ApplicationEvent}, it integrates with the Spring Framework's event publishing
 * mechanism. Listeners can subscribe to this event to perform actions such as initializing related data,
 * sending welcome notifications, or auditing the creation of new records.
 * <p>
 * The event contains the unique identifier for the newly created demographic record, which allows
 * listeners to easily retrieve the full details of the record.
 */
public class DemographicCreateEvent extends ApplicationEvent {
    Logger logger = MiscUtils.getLogger();

    /**
     * The unique identifier for the newly created demographic record, used by listeners to identify and process the record.
     */
    private Integer demographicNo;

    /**
     * Constructs a new {@code DemographicCreateEvent}.
     *
     * @param source The object on which the event initially occurred. This is typically the service
     *               or component responsible for creating the demographic record.
     * @param demographicNo The unique identifier for the newly created demographic record. This is a
     *                      mandatory field and should not be null.
     */
    public DemographicCreateEvent(Object source, Integer demographicNo) {
        super(source);
        this.demographicNo = demographicNo;
    }

    /**
     * Retrieves the unique identifier of the newly created demographic record.
     * This number can be used by event listeners to fetch the complete demographic data
     * for further processing.
     *
     * @return The unique identifier of the demographic record.
     */
    public Integer getDemographicNo() {
        return demographicNo;
    }

    /**
     * Sets the unique identifier for the demographic record.
     * While this method is provided, in a typical event-driven immutable-state pattern,
     * the demographic number should be set at construction and not changed thereafter.
     * This setter is available for flexibility in scenarios where the event might be
     * constructed and then populated, but this is less common.
     *
     * @param demographicNo The unique identifier to be set for the demographic record.
     */
    public void setDemographicNo(Integer demographicNo) {
        this.demographicNo = demographicNo;
    }


}
