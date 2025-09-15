package ca.openosp.openo.PMmodule.caisi_integrator;

import java.io.Serializable;

/**
 * Serializable wrapper for byte array data used in CAISI integrator file transfers.
 *
 * <p>This utility class provides a simple wrapper around byte arrays to enable
 * serialization for healthcare data transfer between CAISI integrator systems.
 * It is commonly used for packaging patient documents, images, and other binary
 * healthcare data for secure transmission between facilities while maintaining
 * data integrity and supporting different serialization frameworks.</p>
 *
 * <p>The wrapper ensures that binary healthcare data can be safely transmitted
 * across network boundaries while maintaining HIPAA/PIPEDA compliance for
 * patient data protection.</p>
 *
 * @see IntegratorFileHeader
 * @see IntegratorFileFooter
 * @since March 24, 2009
 */
public class ByteWrapper implements Serializable
{
    private byte[] data;

    /**
     * Default constructor creating an empty byte wrapper.
     *
     * @since March 24, 2009
     */
    public ByteWrapper() {
    }

    /**
     * Constructs a byte wrapper with the specified data.
     *
     * @param data byte[] the binary data to wrap for serialization
     * @since March 24, 2009
     */
    public ByteWrapper(final byte[] data) {
        this.data = data;
    }

    /**
     * Retrieves the wrapped byte array data.
     *
     * @return byte[] the binary data contained in this wrapper
     * @since March 24, 2009
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * Sets the byte array data to be wrapped.
     *
     * @param data byte[] the binary data to wrap for serialization
     * @since March 24, 2009
     */
    public void setData(final byte[] data) {
        this.data = data;
    }
}
