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
 * Interface defining the contract for OLIS HL7 message segments.
 *
 * This interface represents a segment in an HL7 message used for OLIS communication.
 * Segments are the primary structural units of HL7 messages, each serving a specific
 * purpose in conveying healthcare information between systems.
 *
 * In the OLIS context, segments include:
 * - Standard HL7 segments (MSH, PID, OBR, etc.)
 * - OLIS-specific custom segments (ZSH, ZBR, ZPD, etc.)
 * - Query-specific segments (SPR for stored procedure requests)
 * - Control segments (DSC for continuation)
 *
 * Each segment implementation is responsible for:
 * - Managing its field values and structure
 * - Formatting output according to HL7 v2.5 specifications
 * - Applying OLIS-specific validation and formatting rules
 * - Handling special characters and delimiters properly
 *
 * Segments are assembled together to form complete OLIS messages that can be
 * transmitted to the Ontario Laboratories Information System for querying
 * laboratory results.
 *
 * @since 2008-01-01
 */
public interface Segment {

    /**
     * Generates the HL7 string representation of this segment.
     *
     * Constructs a properly formatted HL7 segment string according to v2.5
     * specifications and OLIS requirements. The output includes:
     * - Segment identifier (3-character code)
     * - Field separator (|)
     * - All segment fields in proper sequence
     * - Appropriate handling of empty/null fields
     * - Proper escaping of special characters
     *
     * The segment string does not include segment terminators (carriage return
     * or line feed) as these are added by the message assembler.
     *
     * Example output format: "MSH|^~\\&|SENDING_APP|..."
     *
     * @return String the complete HL7 segment string ready for message assembly
     * @since 2008-01-01
     */
    public String getSegmentHL7String();

}
