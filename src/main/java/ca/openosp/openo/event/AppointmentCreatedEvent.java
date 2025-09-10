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
 * Represents an event that is triggered when a new appointment is created within the application.
 * This event extends Spring's {@link ApplicationEvent}, allowing it to be published and consumed
 * by any Spring-managed bean that implements {@link org.springframework.context.ApplicationListener}.
 * <p>
 * This event encapsulates the essential information about the created appointment, namely the
 * appointment number and the provider number associated with it. This information can then be
 * used by listeners to perform actions such as sending notifications, updating other parts of
 * the system, or triggering workflows.
 * <p>
 * For example, a listener could be implemented to send an email confirmation to the patient and
 * the provider whenever a new appointment is scheduled.
 */
public class AppointmentCreatedEvent extends ApplicationEvent {
    Logger logger = MiscUtils.getLogger();

    /**
     * The unique identifier for the newly created appointment, used to fetch detailed information and uniquely identify the record.
     */
    private final String appointment_no;
    /**
     * The unique identifier for the provider of the new appointment, essential for scheduling and notifications.
     */
    private final String provider_no;

    /**
     * Constructs a new {@code AppointmentCreatedEvent}. This is the primary and only way to
     * create an instance of this event.
     *
     * @param source The object on which the event initially occurred. In many cases, this will be
     *               the service or component that is responsible for creating the appointment.
     *               It is a standard part of the Spring event mechanism.
     * @param appointment_no The unique identifier for the newly created appointment. This value
     *                       is essential for listeners to identify and process the appointment.
     *                       It must not be null.
     * @param provider_no The unique identifier for the provider associated with the appointment.
     *                    This value is also critical for listeners to correctly handle the event.
     *                    It must not be null.
     */
    public AppointmentCreatedEvent(Object source, String appointment_no, String provider_no) {
        super(source);
        this.appointment_no = appointment_no;
        this.provider_no = provider_no;
    }

    /**
     * Compares this {@code AppointmentCreatedEvent} with another object for equality. The result is
     * {@code true} if and only if the argument is not {@code null} and is an
     * {@code AppointmentCreatedEvent} object that has the same appointment number and provider number
     * as this object.
     * <p>
     * This method is essential for comparing two event instances, for example in tests or in
     * collections. It ensures that two events are considered the same if they refer to the
     * same appointment and provider.
     *
     * @param obj The object to compare this {@code AppointmentCreatedEvent} against.
     * @return {@code true} if the given object represents an {@code AppointmentCreatedEvent}
     *         equivalent to this one, {@code false} otherwise.
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
        AppointmentCreatedEvent other = (AppointmentCreatedEvent) obj;
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

        return true;
    }

    /**
     * Retrieves the unique identifier of the appointment associated with this event. This ID
     * is a key piece of information that listeners will use to fetch more details about the
     * appointment.
     *
     * @return The unique identifier for the appointment. This is the value that was provided
     *         at the time of the event's construction.
     */
    public final String getAppointment_no() {
        return this.appointment_no;
    }

    /**
     * Retrieves the unique identifier of the provider associated with this appointment event.
     * This allows listeners to know which provider the appointment is for, which can be
     * critical for logic such as checking a provider's schedule or sending provider-specific
     * notifications.
     *
     * @return The unique identifier for the provider. This is the value that was provided
     *         at the time of the event's construction.
     */
    public final String getProvider_no() {
        return this.provider_no;
    }


    /**
     * Returns a hash code value for the object. This method is supported for the benefit of
     * hash tables such as those provided by {@link java.util.HashMap}.
     * <p>
     * The hash code is generated based on the appointment number and provider number, which are
     * the core components of this event's identity. This ensures that two equal
     * {@code AppointmentCreatedEvent} objects will have the same hash code.
     *
     * @return A hash code value for this object.
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
        return result;
    }

    /**
     * Returns a string representation of the {@code AppointmentCreatedEvent}. This is primarily
     * used for logging and debugging purposes. The string includes the class name, the
     * appointment number, and the provider number, providing a concise summary of the event's
     * state.
     *
     * @return A string representation of the object, useful for logging and debugging.
     */
    @Override
    public String toString() {
        return "AppointmentChangeEvent [appointment_no=" + this.appointment_no
                + ", provider_no=" + this.provider_no + "]";
    }


}
