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
package ca.openosp.openo.web;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Individual patient appointment item bean for healthcare scheduling systems.
 *
 * This XML-serializable bean encapsulates all essential information for a single
 * patient appointment, providing a structured data model for healthcare scheduling
 * interfaces. It contains comprehensive appointment details including patient
 * demographics, scheduling information, clinical purpose, and administrative notes.
 *
 * <p>The bean supports XML marshaling for integration with web services and
 * AJAX-based scheduling systems, enabling real-time appointment data exchange
 * between different healthcare system components. This is critical for maintaining
 * accurate appointment schedules across multiple clinical interfaces and ensuring
 * coordinated patient care delivery.</p>
 *
 * <p>Appointment data includes essential clinical context such as visit reasons,
 * appointment types, and duration, which help healthcare providers prepare
 * appropriately for patient encounters and allocate clinical resources effectively.
 * Status tracking enables workflow management for appointment lifecycle from
 * scheduling through completion.</p>
 *
 * @since June 2010
 * @see ca.openosp.openo.web.PatientListApptBean
 * @see ca.openosp.openo.commn.model.Appointment
 */
@XmlRootElement
public class PatientListApptItemBean implements Serializable {
    /** Patient demographic identifier for linking to patient records */
    private Integer demographicNo;
    /** Unique appointment identifier for system tracking */
    private Integer appointmentNo;
    /** Patient name for display purposes */
    private String name;
    /** Appointment status (scheduled, confirmed, completed, cancelled) */
    private String status;
    /** Formatted start time for the appointment */
    private String startTime;
    /** Clinical reason or purpose for the appointment */
    private String reason;
    /** Expected duration of the appointment */
    private String duration;
    /** Type of appointment (consultation, follow-up, procedure) */
    private String type;
    /** Additional clinical or administrative notes */
    private String notes;
    /** Appointment date */
    private Date date;

    /**
     * Retrieves the patient demographic identifier.
     *
     * @return Integer patient demographic number for record linking
     */
    public Integer getDemographicNo() {
        return demographicNo;
    }

    /**
     * Sets the patient demographic identifier.
     *
     * @param demographicNo Integer patient demographic number for record linking
     */
    public void setDemographicNo(Integer demographicNo) {
        this.demographicNo = demographicNo;
    }

    /**
     * Retrieves the patient name for display.
     *
     * @return String patient name formatted for user interface display
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the patient name for display.
     *
     * @param name String patient name formatted for user interface display
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the appointment status.
     *
     * @return String current status (scheduled, confirmed, completed, cancelled)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the appointment status for workflow tracking.
     *
     * @param status String appointment status (scheduled, confirmed, completed, cancelled)
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Retrieves the formatted appointment start time.
     *
     * @return String formatted start time for display
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets the appointment start time.
     *
     * @param startTime String formatted start time for scheduling
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * Retrieves the clinical reason for the appointment.
     *
     * @return String clinical purpose or reason for the visit
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the clinical reason for the appointment.
     *
     * @param reason String clinical purpose or reason for the visit
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Retrieves the unique appointment identifier.
     *
     * @return Integer appointment number for system tracking
     */
    public Integer getAppointmentNo() {
        return appointmentNo;
    }

    /**
     * Sets the unique appointment identifier.
     *
     * @param appointmentNo Integer appointment number for system tracking
     */
    public void setAppointmentNo(Integer appointmentNo) {
        this.appointmentNo = appointmentNo;
    }

    /**
     * Retrieves the appointment date.
     *
     * @return Date appointment date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the appointment date.
     *
     * @param date Date appointment date for scheduling
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Retrieves the expected appointment duration.
     *
     * @return String formatted duration for scheduling purposes
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Sets the expected appointment duration.
     *
     * @param duration String formatted duration for resource planning
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * Retrieves the appointment type.
     *
     * @return String type of appointment (consultation, follow-up, procedure)
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the appointment type for clinical categorization.
     *
     * @param type String type of appointment (consultation, follow-up, procedure)
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Retrieves additional appointment notes.
     *
     * @return String clinical or administrative notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets additional appointment notes.
     *
     * @param notes String clinical or administrative notes for reference
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

}