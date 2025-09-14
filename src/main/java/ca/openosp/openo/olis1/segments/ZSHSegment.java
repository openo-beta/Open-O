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

import ca.openosp.openo.commn.model.Provider;

/**
 * Z-Segment Header (ZSH) for OLIS HL7 messages.
 *
 * The ZSH segment is an OLIS-specific custom segment that provides additional
 * header information not covered by the standard MSH segment. It contains
 * provider-specific information about the healthcare professional making the query.
 *
 * This segment includes:
 * - Provider identification number
 * - Provider name (last and first name)
 *
 * The ZSH segment is mandatory for OLIS queries and helps with:
 * - Provider authentication and authorization
 * - Audit trail maintenance
 * - Result delivery preferences
 * - Provider-specific filtering
 *
 * Format: ZSH|provider_number|provider_name
 *
 * If provider information is unavailable (e.g., system-initiated queries),
 * the segment defaults to system values: ZSH|-1|system
 *
 * @since 2008-01-01
 */
public class ZSHSegment implements Segment {

    /**
     * The healthcare provider making the OLIS query.
     * Contains provider number, name, and other identifying information.
     */
    private Provider provider;

    /**
     * Constructs a ZSH segment for the specified provider.
     *
     * @param provider Provider the healthcare provider making the query
     * @since 2008-01-01
     */
    public ZSHSegment(Provider provider) {
        this.provider = provider;
    }

    /**
     * Generates the ZSH segment HL7 string.
     *
     * Constructs the ZSH segment with provider information:
     * - ZSH-1: Provider number
     * - ZSH-2: Provider name (formatted as "LastName FirstName")
     *
     * If provider information is unavailable or an error occurs,
     * returns default system values for automated/system queries.
     *
     * @return String the complete ZSH segment for the OLIS message
     * @since 2008-01-01
     */
    @Override
    public String getSegmentHL7String() {
        try {
            // Format: ZSH|provider_no|last_name first_name
            return "ZSH|" + provider.getProviderNo() + "|" + provider.getLastName() + " " + provider.getFirstName();
        } catch (Exception e) {
            // Default values for system-initiated queries or missing provider info
            return "ZSH|-1|system";
        }
    }
}
