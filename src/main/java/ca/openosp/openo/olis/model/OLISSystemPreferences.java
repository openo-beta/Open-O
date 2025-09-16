//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.olis.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ca.openosp.openo.commn.model.AbstractModel;

/**
 * Entity model for OLIS system-wide preferences and configuration settings.
 * <p>
 * This entity stores global configuration settings that control the automated OLIS
 * (Ontario Laboratories Information System) polling operations. These preferences
 * define when, how often, and under what conditions the system should automatically
 * retrieve laboratory results from OLIS.
 * <p>
 * Key configuration attributes:
 * - Start/End Time: Daily time window for automated operations
 * - Poll Frequency: Minutes between polling attempts
 * - Last Run: Timestamp of most recent polling operation
 * - Filter Patients: Whether to apply patient filtering during polling
 * <p>
 * This entity follows a singleton pattern where typically only one system preferences
 * record exists, controlling all automated OLIS operations across the EMR system.
 *
 * @since 2008
 */
@Entity
public class OLISSystemPreferences extends AbstractModel<Integer> {

    /** Primary key identifier for the system preferences record */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /** Start time for the daily OLIS polling window */
    private String startTime;
    /** End time for the daily OLIS polling window */
    private String endTime;
    /** Frequency of OLIS polling operations in minutes */
    private Integer pollFrequency;

    /** Timestamp of the last OLIS polling operation */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastRun;

    /** Whether to apply patient filtering during OLIS operations */
    private boolean filterPatients;


    /**
     * Gets the primary key identifier.
     *
     * @return Integer the auto-generated primary key
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Gets the start time for the daily OLIS polling window.
     *
     * @return String the start time configuration
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time for the daily OLIS polling window.
     *
     * @param startTime String the start time to configure
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end time for the daily OLIS polling window.
     *
     * @return String the end time configuration
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time for the daily OLIS polling window.
     *
     * @param endTime String the end time to configure
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the polling frequency in minutes.
     *
     * @return Integer the number of minutes between polling attempts
     */
    public Integer getPollFrequency() {
        return pollFrequency;
    }

    /**
     * Sets the polling frequency in minutes.
     *
     * @param pollFrequency Integer the number of minutes between polling attempts
     */
    public void setPollFrequency(Integer pollFrequency) {
        this.pollFrequency = pollFrequency;
    }

    /**
     * Gets the timestamp of the last OLIS polling operation.
     *
     * @return Date the last run timestamp
     */
    public Date getLastRun() {
        return lastRun;
    }

    /**
     * Sets the timestamp of the last OLIS polling operation.
     *
     * @param lastRun Date the last run timestamp to record
     */
    public void setLastRun(Date lastRun) {
        this.lastRun = lastRun;
    }


    /**
     * Gets the patient filtering flag for OLIS operations.
     *
     * @return boolean true if patient filtering should be applied
     */
    public boolean isFilterPatients() {
        return filterPatients;
    }

    /**
     * Sets the patient filtering flag for OLIS operations.
     *
     * @param filterPatients boolean true to enable patient filtering
     */
    public void setFilterPatients(boolean filterPatients) {
        this.filterPatients = filterPatients;
    }

    /**
     * Default constructor for OLISSystemPreferences.
     * <p>
     * Creates a new system preferences instance with default values.
     */
    public OLISSystemPreferences() {
        super();
    }
}
