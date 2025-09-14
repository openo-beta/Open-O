//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.olis1.segments;

/**
 * Display Continuation Segment (DSC) for OLIS HL7 message continuation.
 *
 * The DSC segment is used to request additional results from OLIS when the
 * initial query response exceeds the requested limit. When OLIS cannot return
 * all matching results in a single response, it provides a continuation pointer
 * in the DSC segment of the response message.
 *
 * This continuation mechanism enables:
 * - Paginated retrieval of large result sets
 * - Controlled data transfer to prevent timeout issues
 * - Efficient memory management for both client and server
 * - Progressive loading of laboratory results
 *
 * The continuation pointer is opaque to the client and must be used exactly
 * as provided by OLIS. It contains server-side state information that allows
 * OLIS to resume result transmission from the correct position.
 *
 * HL7 DSC Segment Structure:
 * - DSC-1: Continuation Pointer (required)
 * - DSC-2: Continuation Style (optional, not used by OLIS)
 *
 * Usage Pattern:
 * 1. Send initial query with QRD-7 limit parameter
 * 2. If response contains DSC segment, extract continuation pointer
 * 3. Resend identical query with DSC segment containing pointer
 * 4. Repeat until no DSC segment in response (all results retrieved)
 *
 * @since 2008-01-01
 */
public class DSCSegment implements Segment {

    /**
     * The continuation pointer received from OLIS.
     * This opaque value contains server-side state information
     * that enables OLIS to continue result transmission from
     * the correct position in the result set.
     */
    private String continuationPointer;

    /**
     * Constructs a DSC segment with the specified continuation pointer.
     *
     * The continuation pointer must be the exact value received from
     * a previous OLIS response. Modifying or constructing this value
     * manually will result in query failures.
     *
     * @param continuationPointer String the continuation pointer from OLIS response
     * @since 2008-01-01
     */
    public DSCSegment(String continuationPointer) {
        this.continuationPointer = continuationPointer;
    }

    /**
     * Generates the DSC segment HL7 string.
     *
     * Creates the DSC segment with the continuation pointer for inclusion
     * in a continuation query to OLIS. The segment format follows HL7 v2.5
     * specifications.
     *
     * @return String the complete DSC segment for the continuation query
     * @since 2008-01-01
     */
    @Override
    public String getSegmentHL7String() {
        return "DSC|" + continuationPointer;
    }

}
