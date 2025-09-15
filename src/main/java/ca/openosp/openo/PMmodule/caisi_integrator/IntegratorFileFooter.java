package ca.openosp.openo.PMmodule.caisi_integrator;

import java.io.Serializable;

/**
 * Footer metadata for CAISI integrator file transfer operations.
 *
 * <p>This class represents the footer section of healthcare data files transferred
 * between CAISI integrator systems. The footer contains integrity verification
 * information, primarily a checksum, to ensure data integrity during transmission
 * and detect any corruption or tampering of patient health information.</p>
 *
 * <p>The checksum validation is critical for maintaining HIPAA/PIPEDA compliance
 * by ensuring that patient data transmitted between healthcare facilities remains
 * accurate and untampered during the integration process.</p>
 *
 * @see IntegratorFileHeader
 * @see ByteWrapper
 * @see CaisiIntegratorUpdateTask
 * @since 2009
 */
public class IntegratorFileFooter implements Serializable
{
    private int checksum;

    /**
     * Default constructor initializing checksum to -1 (invalid state).
     *
     * @since 2009
     */
    public IntegratorFileFooter() {
        this.checksum = -1;
    }

    /**
     * Constructs footer with the specified checksum value.
     *
     * @param checksum int the checksum value for data integrity verification
     * @since 2009
     */
    public IntegratorFileFooter(final int checksum) {
        this.checksum = checksum;
    }

    /**
     * Gets the checksum value for data integrity verification.
     *
     * @return int the checksum value, or -1 if not set
     * @since 2009
     */
    public int getChecksum() {
        return this.checksum;
    }

    /**
     * Sets the checksum value for data integrity verification.
     *
     * @param checksum int the checksum value to set
     * @since 2009
     */
    public void setChecksum(final int checksum) {
        this.checksum = checksum;
    }
}
