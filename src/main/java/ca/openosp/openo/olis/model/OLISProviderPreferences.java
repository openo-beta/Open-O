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
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ca.openosp.openo.commn.model.AbstractModel;

/**
 * Entity model for OLIS provider-specific preferences and configuration.
 * <p>
 * This entity stores individual healthcare provider preferences for OLIS laboratory
 * result polling operations. Each provider can have customized settings for when
 * and how OLIS queries should be executed, allowing for personalized automation
 * based on provider workflow requirements.
 * <p>
 * Key attributes:
 * - Provider ID: Links preferences to specific healthcare provider
 * - Start time: When automated OLIS polling should begin for this provider
 * - Last run: Timestamp of most recent OLIS operation for frequency control
 * <p>
 * This entity supports provider-level customization of OLIS operations, enabling
 * different providers to have different polling schedules and preferences within
 * the same EMR system.
 *
 * @since 2008
 */
@Entity
public class OLISProviderPreferences extends AbstractModel<String> {
    /** Unique identifier for the healthcare provider */
    @Id
    private String providerId;

    /** Start time configuration for automated OLIS operations */
    private String startTime;

    /** Timestamp of the last OLIS polling operation for this provider */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastRun;

    /**
     * Gets the entity identifier (provider ID).
     *
     * @return String the provider ID serving as the primary key
     */
    @Override
    public String getId() {
        return providerId;
    }

    /**
     * Gets the healthcare provider identifier.
     *
     * @return String the unique provider ID
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * Sets the healthcare provider identifier.
     *
     * @param providerNo String the unique provider ID to set
     */
    public void setProviderId(String providerNo) {
        this.providerId = providerNo;
    }

    /**
     * Gets the start time configuration for OLIS operations.
     *
     * @return String the start time setting
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time configuration for OLIS operations.
     *
     * @param startTime String the start time setting to configure
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
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
     * Default constructor for OLISProviderPreferences.
     * <p>
     * Creates a new provider preferences instance with default values.
     */
    public OLISProviderPreferences() {
        super();
    }
}
