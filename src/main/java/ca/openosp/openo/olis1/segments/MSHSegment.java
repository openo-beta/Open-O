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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import ca.openosp.OscarProperties;

import ca.openosp.openo.olis1.queries.QueryType;

/**
 * Message Header Segment (MSH) for OLIS HL7 messages.
 *
 * The MSH segment is the first segment in every HL7 message and contains
 * critical metadata about the message including:
 * - Message delimiters and encoding characters
 * - Sending and receiving application/facility identifiers
 * - Message timestamp
 * - Message type and trigger event
 * - Unique message control ID
 * - Processing ID (production vs test)
 * - HL7 version
 *
 * For OLIS queries, the MSH segment specifically identifies:
 * - The EMR system sending the query (via X.500 distinguished name)
 * - The OLIS system as the receiver
 * - The query type (SPQ - Stored Procedure Query)
 * - Processing mode (P=Production, T=Test)
 *
 * The segment follows HL7 v2.3.1 specifications with OLIS-specific
 * requirements for authentication and message routing.
 *
 * @since 2008-01-01
 */
public class MSHSegment implements Segment {

    /**
     * The type of OLIS query being performed.
     * Determines the message structure and query parameters.
     */
    private QueryType queryType;

    /**
     * Date formatter for HL7 timestamp format.
     * Formats dates as YYYYMMDDHHMMSS±ZZZZ with timezone.
     */
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmssZZZZZ");

    /**
     * Unique message control ID.
     * Generated UUID ensures each message can be uniquely tracked.
     */
    private String uuidString = UUID.randomUUID().toString();

    /**
     * Constructs an MSH segment for the specified query type.
     *
     * @param queryType QueryType the type of OLIS query (Z01, Z02, etc.)
     * @since 2008-01-01
     */
    public MSHSegment(QueryType queryType) {
        this.queryType = queryType;
    }

    /**
     * Gets the unique message control ID.
     *
     * This UUID is used to track the message through its lifecycle
     * and correlate requests with responses.
     *
     * @return String the UUID message control ID
     * @since 2008-01-01
     */
    public String getUuidString() {
        return uuidString;
    }

    /**
     * Generates the MSH segment HL7 string.
     *
     * Constructs the complete MSH segment with all required fields:
     * - MSH-1: Field separator (|)
     * - MSH-2: Encoding characters (^~\&)
     * - MSH-3: Sending application (X.500 distinguished name)
     * - MSH-4: Sending facility (MCMUN2)
     * - MSH-5: Receiving application (OLIS)
     * - MSH-6: Receiving facility (empty)
     * - MSH-7: Message timestamp
     * - MSH-8: Security (empty)
     * - MSH-9: Message type (SPQ^Zxx^SPQ_Q08)
     * - MSH-10: Message control ID (UUID)
     * - MSH-11: Processing ID (P=Production, T=Test)
     * - MSH-12: Version ID (2.3.1)
     * - MSH-18: Character set (8859/1 - Latin-1)
     *
     * @return String the complete MSH segment for the OLIS message
     * @since 2008-01-01
     */
    @Override
    public String getSegmentHL7String() {

        // X.500 distinguished name for EMR authentication
        String sendingApplication = OscarProperties.getInstance().getProperty("OLIS_SENDING_APPLICATION", "^CN=EMR.MCMUN2.CST,OU=Applications,OU=eHealthUsers,OU=Subscribers,DC=subscribers,DC=ssh^X500");
        // Processing mode: P=Production, T=Test
        String processingId = OscarProperties.getInstance().getProperty("OLIS_PROCESSING_ID", "P");

        return "MSH|^~\\&|" + sendingApplication + "|MCMUN2|" +
                "^OLIS^X500||" + dateFormatter.format(new Date()) + "||SPQ^" + queryType.toString() + "^SPQ_Q08|" + uuidString + "|" + processingId + "|2.3.1||||||8859/1";
    }

}
