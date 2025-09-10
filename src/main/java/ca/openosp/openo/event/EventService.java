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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * A service class responsible for publishing application-wide events.
 * This service acts as a central point for event publication, decoupling event producers from event consumers.
 * It uses the Spring Framework's {@link ApplicationEventPublisher} to broadcast events to registered listeners.
 * <p>
 * This class implements {@link ApplicationEventPublisherAware}, which allows the Spring container to inject
 * an {@code ApplicationEventPublisher} instance into it. This is the standard way to get a handle to the
 * event publishing mechanism within a Spring-managed bean.
 * <p>
 * The service provides specific methods for publishing different types of business events, such as
 * appointment status changes and new appointment creations. This makes the act of publishing an event
 * more explicit and descriptive in the calling code.
 */
public class EventService implements ApplicationEventPublisherAware {
    Logger logger = MiscUtils.getLogger();
    /**
     * The Spring {@link ApplicationEventPublisher} used to publish events.
     * This publisher is injected by the Spring container because this class implements
     * {@link ApplicationEventPublisherAware}. It is the core component that handles the
     * broadcasting of events to all registered listeners.
     */
    protected ApplicationEventPublisher applicationEventPublisher;

    /**
     * Sets the {@link ApplicationEventPublisher} that this object is to use.
     * This method is part of the {@link ApplicationEventPublisherAware} interface and is called by the
     * Spring container upon bean initialization to inject the event publisher.
     *
     * @param arg0 The {@code ApplicationEventPublisher} to be used by this service.
     */
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher arg0) {
        this.applicationEventPublisher = arg0;

    }


    /**
     * Publishes an {@link AppointmentStatusChangeEvent}.
     * This method is called when the status of an appointment is changed.
     * <p>
     * Event is fired from:
     * <ul>
     *     <li>{@code src/main/webapp/appointment/appointmentupdatearecord.jsp} -- edit appt screen</li>
     *     <li>{@code src/main/webapp/providers/provideraddstatus.jsp}  -- in appt screen when user clicks on the icon to change the appt status</li>
     * </ul>
     *
     * @param source The object that is publishing the event.
     * @param appointment_no The ID of the appointment whose status has changed.
     * @param provider_no The ID of the provider associated with the appointment.
     * @param status The new status of the appointment.
     */
    public void appointmentStatusChanged(Object source, String appointment_no, String provider_no, String status) {
        logger.debug("appointmentStatusChanged thrown by " + source.getClass().getName() + " appt# " + appointment_no + " status " + status);

        applicationEventPublisher.publishEvent(new AppointmentStatusChangeEvent(source, appointment_no, provider_no, status));
    }

    /**
     * Publishes an {@link AppointmentCreatedEvent}.
     * This method is called when a new appointment is created.
     * <p>
     * Event is fired from:
     * <ul>
     *     <li>{@code src/main/webapp/appointment/appointmentaddarecord.jsp}</li>
     *     <li>{@code src/main/webapp/appointment/appointmentaddrecordcard.jsp}</li>
     *     <li>{@code src/main/webapp/appointment/appointmentaddrecordprint.jsp}</li>
     * </ul>
     *
     * @param source The object that is publishing the event.
     * @param appointment_no The ID of the newly created appointment.
     * @param provider_no The ID of the provider associated with the appointment.
     */
    public void appointmentCreated(Object source, String appointment_no, String provider_no) {
        applicationEventPublisher.publishEvent(new AppointmentCreatedEvent(source, appointment_no, provider_no));
    }
}
