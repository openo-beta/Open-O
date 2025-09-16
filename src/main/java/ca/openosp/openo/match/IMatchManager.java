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

package ca.openosp.openo.match;

/**
 * Healthcare patient-provider matching manager interface for OpenO EMR system.
 *
 * <p>This interface defines the core contract for managing the automated matching
 * of patients (clients) to available healthcare provider slots (vacancies) within
 * healthcare programs. The matching system is event-driven and processes different
 * types of healthcare-related events to maintain optimal patient-provider assignments.</p>
 *
 * <p>The matching system supports three main event types:
 * <ul>
 *   <li>Client creation events - triggered when new patients are added to waitlists</li>
 *   <li>Vacancy creation events - triggered when healthcare providers create new appointment slots</li>
 *   <li>Scheduled events - periodic maintenance and optimization of existing matches</li>
 * </ul>
 * </p>
 *
 * <p>Healthcare Context: This interface is specifically designed for Canadian healthcare
 * environments where patients may need to be matched to appropriate healthcare providers
 * based on various criteria including geographic location, medical specialties,
 * appointment preferences, and program-specific requirements.</p>
 *
 * @see MatchManager
 * @see Matcher
 * @see VacancyClientMatch
 * @since 2006-12-16
 */
public interface IMatchManager {

    /**
     * Defines possible event types that trigger healthcare matching processes.
     *
     * <p>These events correspond to key healthcare workflow activities:
     * <ul>
     *   <li>{@code CLIENT_CREATED} - A new patient has been added to a program waitlist</li>
     *   <li>{@code VACANCY_CREATED} - A healthcare provider has created new appointment availability</li>
     *   <li>{@code SCHEDULED_EVENT} - Periodic system maintenance and match optimization</li>
     * </ul>
     * </p>
     */
    public static enum Event {
        /** Triggered when a new patient (client) is registered and needs provider matching */
        CLIENT_CREATED,

        /** Triggered when a healthcare provider creates new appointment vacancy slots */
        VACANCY_CREATED,

        /** Triggered by scheduled system maintenance for match optimization and cleanup */
        SCHEDULED_EVENT;
    }

    /**
     * Processes healthcare matching events to create or update patient-provider matches.
     *
     * <p>This method serves as the central entry point for all healthcare matching activities.
     * It accepts different entity types depending on the event being processed and returns
     * a status message indicating the processing outcome.</p>
     *
     * <p>Event Processing Details:
     * <ul>
     *   <li>{@code CLIENT_CREATED} - entity should be a {@link ca.openosp.openo.commn.model.Demographic} object</li>
     *   <li>{@code VACANCY_CREATED} - entity should be a {@link ca.openosp.openo.PMmodule.model.Vacancy} object</li>
     *   <li>{@code SCHEDULED_EVENT} - entity parameter is ignored (can be null)</li>
     * </ul>
     * </p>
     *
     * @param <E> the type of healthcare entity being processed (Demographic, Vacancy, etc.)
     * @param entity the healthcare entity to process (patient demographic or provider vacancy)
     * @param event the type of healthcare event that triggered this processing
     * @return String status message indicating the processing result ("Done", "Not required", etc.)
     * @throws MatchManagerException if the event type is not supported or processing fails
     */
    public <E> String processEvent(E entity, Event event) throws MatchManagerException;
}
