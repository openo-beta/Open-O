package ca.openosp.openo.PMmodule.caisi_integrator;

import java.util.Date;
import java.io.Serializable;

/**
 * Header metadata for CAISI integrator file transfer operations.
 *
 * <p>This class represents the header section of healthcare data files transferred
 * between CAISI integrator systems. The header contains essential metadata including
 * version information, facility identification, user context, and temporal tracking
 * for healthcare data synchronization and audit compliance.</p>
 *
 * <p><strong>Key Header Information:</strong></p>
 * <ul>
 *   <li>Version control for file format compatibility</li>
 *   <li>Facility identification for multi-facility environments</li>
 *   <li>Temporal markers for incremental synchronization</li>
 *   <li>User context for audit trails and HIPAA/PIPEDA compliance</li>
 *   <li>Dependency tracking for ordered data processing</li>
 * </ul>
 *
 * @see IntegratorFileFooter
 * @see ByteWrapper
 * @see CaisiIntegratorUpdateTask
 * @since 2009
 */
public class IntegratorFileHeader implements Serializable
{
    private Date lastDate;
    private Date date;
    private String dependsOn;
    private Integer cachedFacilityId;
    private String cachedFacilityName;
    private String username;
    public static final int VERSION = 1;

    /**
     * Gets the current date of this file transfer.
     *
     * @return Date the current transfer date
     * @since 2009
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Sets the current date of this file transfer.
     *
     * @param date Date the current transfer date to set
     * @since 2009
     */
    public void setDate(final Date date) {
        this.date = date;
    }

    /**
     * Gets the last synchronization date for incremental updates.
     *
     * @return Date the previous synchronization date
     * @since 2009
     */
    public Date getLastDate() {
        return this.lastDate;
    }

    /**
     * Sets the last synchronization date for incremental updates.
     *
     * @param lastDate Date the previous synchronization date to set
     * @since 2009
     */
    public void setLastDate(final Date lastDate) {
        this.lastDate = lastDate;
    }

    /**
     * Gets the dependency identifier for ordered processing.
     *
     * @return String the dependency identifier
     * @since 2009
     */
    public String getDependsOn() {
        return this.dependsOn;
    }

    /**
     * Sets the dependency identifier for ordered processing.
     *
     * @param dependsOn String the dependency identifier to set
     * @since 2009
     */
    public void setDependsOn(final String dependsOn) {
        this.dependsOn = dependsOn;
    }

    /**
     * Gets the file format version number.
     *
     * @return int the version number (currently 1)
     * @since 2009
     */
    public int getVersion() {
        return 1;
    }

    /**
     * Gets the cached facility ID for multi-facility identification.
     *
     * @return Integer the facility ID
     * @since 2009
     */
    public Integer getCachedFacilityId() {
        return this.cachedFacilityId;
    }

    /**
     * Sets the cached facility ID for multi-facility identification.
     *
     * @param cachedFacilityId Integer the facility ID to set
     * @since 2009
     */
    public void setCachedFacilityId(final Integer cachedFacilityId) {
        this.cachedFacilityId = cachedFacilityId;
    }

    /**
     * Gets the cached facility name for human-readable identification.
     *
     * @return String the facility name
     * @since 2009
     */
    public String getCachedFacilityName() {
        return this.cachedFacilityName;
    }

    /**
     * Sets the cached facility name for human-readable identification.
     *
     * @param cachedFacilityName String the facility name to set
     * @since 2009
     */
    public void setCachedFacilityName(final String cachedFacilityName) {
        this.cachedFacilityName = cachedFacilityName;
    }

    /**
     * Gets the username for audit trail and security context.
     *
     * @return String the username
     * @since 2009
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username for audit trail and security context.
     *
     * @param username String the username to set
     * @since 2009
     */
    public void setUsername(final String username) {
        this.username = username;
    }
}
