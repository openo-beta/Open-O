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


package ca.openosp.openo.PMmodule.caisi_integrator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicPrevention;
import ca.openosp.openo.caisi_integrator.ws.CachedProvider;
import ca.openosp.openo.caisi_integrator.ws.DemographicWs;
import ca.openosp.openo.caisi_integrator.ws.FacilityIdStringCompositePk;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ca.openosp.openo.util.DateUtils;

/**
 * Helper class for managing remote prevention data in CAISI integrator.
 *
 * <p>This utility class provides specialized functionality for retrieving and processing
 * prevention/immunization information from remote CAISI-integrated facilities. It supports
 * comprehensive preventive care tracking by providing access to patient immunization
 * histories, screening records, and preventive care measures across multiple healthcare
 * facilities.</p>
 *
 * <p><strong>Key Healthcare Functions:</strong></p>
 * <ul>
 *   <li>Remote prevention data retrieval with provider context</li>
 *   <li>Prevention data mapping to standardized format for display</li>
 *   <li>XML attribute parsing for complex prevention metadata</li>
 *   <li>Integration with local prevention tracking systems</li>
 *   <li>Offline fallback support for continuity of care</li>
 * </ul>
 *
 * <p>The helper ensures that healthcare providers have access to complete prevention
 * histories regardless of where the patient received care previously, supporting
 * evidence-based preventive care decisions and avoiding unnecessary duplicate procedures.</p>
 *
 * @see IntegratorFallBackManager
 * @see CaisiIntegratorManager
 * @see RemoteDrugAllergyHelper
 * @since 2009
 */
public final class RemotePreventionHelper {
    private static Logger logger = MiscUtils.getLogger();

    /**
     * Retrieves linked prevention data and converts it to standardized data maps.
     *
     * <p>Fetches prevention/immunization records from remote CAISI facilities for the
     * specified patient and converts them to HashMap objects suitable for display
     * and integration with local prevention tracking systems. Each prevention record
     * includes provider information, dates, and refusal status.</p>
     *
     * <p>The method supports both online and offline modes, falling back to cached
     * data when remote integrator services are unavailable to ensure continuity of care.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param localDemographicId Integer the local patient demographic ID
     * @return ArrayList&lt;HashMap&lt;String, Object&gt;&gt; list of prevention data maps
     * @since 2009
     */
    public static ArrayList<HashMap<String, Object>> getLinkedPreventionDataMap(LoggedInInfo loggedInInfo, Integer localDemographicId) {
        ArrayList<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();

        try {


            List<CachedDemographicPrevention> preventions = null;
            try {
                if (!CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                    DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
                    preventions = demographicWs.getLinkedCachedDemographicPreventionsByDemographicId(localDemographicId);
                }
            } catch (Exception e) {
                MiscUtils.getLogger().error("Unexpected error.", e);
                CaisiIntegratorManager.checkForConnectionError(loggedInInfo.getSession(), e);
            }

            if (CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                preventions = IntegratorFallBackManager.getRemotePreventions(loggedInInfo, localDemographicId);
            }

            for (CachedDemographicPrevention prevention : preventions) {
                results.add(getPreventionDataMap(loggedInInfo, prevention));
            }
        } catch (Exception e) {
            logger.error("Error getting remote Preventions", e);
        }

        return (results);
    }

    /**
     * Converts a cached prevention record to a standardized data map.
     *
     * <p>Transforms a remote prevention record into a HashMap containing all relevant
     * prevention information including ID, refusal status, prevention type, provider
     * details, and formatted dates. This standardized format is compatible with
     * local prevention display and tracking systems.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param prevention CachedDemographicPrevention the remote prevention record to convert
     * @return HashMap&lt;String, Object&gt; the standardized prevention data map
     * @throws MalformedURLException if facility URL configuration is invalid
     * @since 2009
     */
    public static HashMap<String, Object> getPreventionDataMap(LoggedInInfo loggedInInfo, CachedDemographicPrevention prevention) throws MalformedURLException {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("id", prevention.getFacilityPreventionPk().getCaisiItemId().toString());
        result.put("refused", booleanTo10String(prevention.isRefused()));
        result.put("type", prevention.getPreventionType());
        result.put("provider_no", prevention.getCaisiProviderId());
        result.put("prevention_date", DateUtils.formatDate(prevention.getPreventionDate(), null));
        if (prevention.getPreventionDate() != null) {
            result.put("prevention_date_asDate", prevention.getPreventionDate().getTime());
        }

        FacilityIdStringCompositePk providerPk = new FacilityIdStringCompositePk();
        providerPk.setIntegratorFacilityId(prevention.getFacilityPreventionPk().getIntegratorFacilityId());
        providerPk.setCaisiItemId(prevention.getCaisiProviderId());
        CachedProvider cachedProvider = CaisiIntegratorManager.getProvider(loggedInInfo, loggedInInfo.getCurrentFacility(), providerPk);
        if (cachedProvider != null) {
            result.put("provider_name", cachedProvider.getLastName() + ", " + cachedProvider.getFirstName());
        }

        return (result);
    }

    /**
     * Converts a boolean value to string representation ("1" or "0").
     *
     * <p>Utility method for converting boolean values to the string format
     * expected by prevention tracking systems.</p>
     *
     * @param b boolean the boolean value to convert
     * @return String "1" if true, "0" if false
     * @since 2009
     */
    private static String booleanTo10String(boolean b) {
        if (b) return ("1");
        else return ("0");
    }

    /**
     * Parses XML attributes from a prevention record into a HashMap.
     *
     * <p>Extracts and parses XML-formatted prevention attributes from a cached
     * prevention record. These attributes may contain additional metadata about
     * the prevention such as lot numbers, manufacturer information, site of
     * administration, or other clinical details.</p>
     *
     * @param prevention CachedDemographicPrevention the prevention record with XML attributes
     * @return HashMap&lt;String, String&gt; the parsed prevention attributes
     * @throws IOException if XML parsing encounters I/O errors
     * @throws SAXException if XML is malformed
     * @throws ParserConfigurationException if XML parser configuration fails
     * @since 2009
     */
    public static HashMap<String, String> getRemotePreventionAttributesAsHashMap(CachedDemographicPrevention prevention) throws IOException, SAXException, ParserConfigurationException {
        Document doc = XmlUtils.toDocument(prevention.getAttributes());
        Node root = doc.getFirstChild();
        HashMap<String, String> result = new HashMap<String, String>();

        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            result.put(node.getNodeName(), node.getTextContent());
        }

        return (result);
    }
}
