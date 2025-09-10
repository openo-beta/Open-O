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
 * Represents an event triggered when the status of an appointment changes.
 * This event is published whenever an appointment's status is updated (e.g., from 'scheduled' to 'confirmed', 'cancelled', etc.).
 * It extends Spring's {@link ApplicationEvent}, making it easy to integrate with Spring's event handling mechanism.
 * Listeners can subscribe to this event to perform actions based on the status change, such as sending notifications,
 * updating UI components, or triggering other business processes.
 */
public class AppointmentStatusChangeEvent extends ApplicationEvent {
    Logger logger = MiscUtils.getLogger();

    /**
     * The unique identifier for the appointment whose status has changed, allowing listeners to identify the correct record.
     */
    private final String appointment_no;
    /**
     * The unique identifier for the provider of the appointment, used for provider-specific logic.
     */
    private final String provider_no;
    /**
     * The new status of the appointment, which is the core information of this event.
     */
    private final String status;

    /**
     * Constructs a new {@code AppointmentStatusChangeEvent}.
     *
     * @param source The object on which the event initially occurred. Typically, this is the service or component
     *               that initiated the status change.
     * @param appointment_no The unique identifier for the appointment that has undergone a status change. Must not be null.
     * @param provider_no The unique identifier for the provider of the appointment. Must not be null.
     * @param status The new status of the appointment. Must not be null.
     */
    public AppointmentStatusChangeEvent(Object source, String appointment_no, String provider_no, String status) {
        super(source);
        this.appointment_no = appointment_no;
        this.provider_no = provider_no;
        this.status = status;
        logger.debug("Object up " + source.getClass().getName());
    }

    /**
     * Compares this event with another object for equality.
     * Two {@code AppointmentStatusChangeEvent} objects are considered equal if they have the same appointment number,
     * provider number, and status. This is useful for testing and for managing events in collections.
     *
     * @param obj The object to compare against.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        AppointmentStatusChangeEvent other = (AppointmentStatusChangeEvent) obj;
        if (this.appointment_no == null) {
            if (other.appointment_no != null) {
                return false;
            }
        } else if (!this.appointment_no.equals(other.appointment_no)) {
            return false;
        }
        if (this.provider_no == null) {
            if (other.provider_no != null) {
                return false;
            }
        } else if (!this.provider_no.equals(other.provider_no)) {
            return false;
        }
        if (this.status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!this.status.equals(other.status)) {
            return false;
        }
        return true;
    }

    /**
     * Retrieves the unique identifier of the appointment whose status has changed.
     * This ID allows listeners to fetch the full appointment details if needed.
     *
     * @return The appointment's unique identifier.
     */
    public final String getAppointment_no() {
        return this.appointment_no;
    }

    /**
     * Retrieves the unique identifier of the provider for the appointment.
     * This can be used to perform provider-specific actions in response to the event.
     *
     * @return The provider's unique identifier.
     */
    public final String getProvider_no() {
        return this.provider_no;
    }

    /**
     * Retrieves the new status of the appointment.
     * This is the central piece of information in the event, indicating the appointment's new state.
     *
     * @return The new appointment status.
     */
    public final String getStatus() {
        return this.status;
    }

    /**
     * Generates a hash code for this event.
     * The hash code is calculated based on the appointment number, provider number, and status,
     * ensuring that equal objects have equal hash codes. This is important for use in hash-based collections.
     *
     * @return The hash code for this event.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((this.appointment_no == null) ? 0 : this.appointment_no
                .hashCode());
        result = (prime * result)
                + ((this.provider_no == null) ? 0 : this.provider_no.hashCode());
        result = (prime * result)
                + ((this.status == null) ? 0 : this.status.hashCode());
        return result;
    }

    /**
     * Provides a string representation of the event.
     * The output includes the appointment number, provider number, and new status, which is useful for
     * logging and debugging purposes.
     *
     * @return A string summary of the event.
     */
    @Override
    public String toString() {
        return "AppointmentChangeEvent [appointment_no=" + this.appointment_no
                + ", provider_no=" + this.provider_no + ", status="
                + this.status + "]";
    }


}
