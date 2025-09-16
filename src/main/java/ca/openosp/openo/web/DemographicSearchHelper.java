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

import java.net.MalformedURLException;
import java.util.List;

import ca.openosp.openo.PMmodule.caisi_integrator.CaisiIntegratorManager;
import ca.openosp.openo.caisi_integrator.ws.DemographicWs;
import ca.openosp.openo.caisi_integrator.ws.MatchingDemographicParameters;
import ca.openosp.openo.caisi_integrator.ws.MatchingDemographicTransferScore;
import ca.openosp.openo.utility.LoggedInInfo;

/**
 * Helper utility for integrated demographic searches across multiple OpenO EMR installations.
 * This class provides functionality for searching patient demographics using the CAISI Integrator
 * system, which enables healthcare providers to find patient records across different
 * healthcare facilities and EMR installations within an integrated network.
 *
 * <p>Key integration features:</p>
 * <ul>
 *   <li>Cross-facility patient record searches using demographic matching</li>
 *   <li>Fuzzy matching algorithms with confidence scoring</li>
 *   <li>Support for complex demographic matching parameters</li>
 *   <li>Integration with CAISI (Client Access to Integrated Services and Information) system</li>
 * </ul>
 *
 * <p>The search functionality helps healthcare providers:</p>
 * <ul>
 *   <li>Locate existing patient records before creating duplicates</li>
 *   <li>Find patients who may have received care at other facilities</li>
 *   <li>Support care coordination across integrated healthcare networks</li>
 *   <li>Maintain continuity of care through shared patient information</li>
 * </ul>
 *
 * <p>This utility is typically used in patient registration workflows where providers
 * need to search for existing patients before creating new demographic records.</p>
 *
 * @since June 10, 2011
 * @see MatchingDemographicParameters
 * @see MatchingDemographicTransferScore
 * @see CaisiIntegratorManager
 */
public final class DemographicSearchHelper {

    /**
     * Searches for matching demographic records across integrated healthcare facilities.
     * This method performs a cross-facility patient search using the CAISI Integrator system,
     * returning a list of potential patient matches with confidence scores to help providers
     * identify existing patient records.
     *
     * <p>The search process includes:</p>
     * <ul>
     *   <li>Verification that the current facility has integrator services enabled</li>
     *   <li>Connection to the CAISI Integrator web service</li>
     *   <li>Execution of demographic matching across connected facilities</li>
     *   <li>Return of scored matches for provider review and selection</li>
     * </ul>
     *
     * <p>Matching is performed using configurable demographic parameters that may include:</p>
     * <ul>
     *   <li>Patient name (with fuzzy matching for variations)</li>
     *   <li>Date of birth</li>
     *   <li>Health insurance numbers</li>
     *   <li>Address information</li>
     *   <li>Phone numbers and contact information</li>
     * </ul>
     *
     * @param loggedInInfo LoggedInInfo current user session and facility context
     * @param matchingDemographicParameters MatchingDemographicParameters search criteria for patient matching
     * @return List<MatchingDemographicTransferScore> list of potential matches with confidence scores, or null if integrator is disabled
     * @throws MalformedURLException if the integrator service URL is malformed or inaccessible
     */
    public static List<MatchingDemographicTransferScore> getIntegratedSearchResults(LoggedInInfo loggedInInfo, MatchingDemographicParameters matchingDemographicParameters) throws MalformedURLException {
        // Check if the current facility has CAISI Integrator services enabled
        if (!loggedInInfo.getCurrentFacility().isIntegratorEnabled()) return (null);

        // Obtain demographic web service connection for cross-facility searches
        DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());

        // Execute the demographic search and return scored matches
        List<MatchingDemographicTransferScore> integratedMatches = demographicWs.getMatchingDemographics(matchingDemographicParameters);
        return (integratedMatches);
    }
}
