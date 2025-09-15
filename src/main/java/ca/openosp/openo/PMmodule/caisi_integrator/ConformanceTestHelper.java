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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.caisi_integrator.ws.CachedProvider;
import ca.openosp.openo.caisi_integrator.ws.DemographicTransfer;
import ca.openosp.openo.caisi_integrator.ws.DemographicWs;
import ca.openosp.openo.caisi_integrator.ws.FacilityIdStringCompositePk;
import ca.openosp.openo.caisi_integrator.ws.ProviderCommunicationTransfer;
import ca.openosp.openo.caisi_integrator.ws.ProviderWs;
import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import ca.openosp.openo.utility.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ca.openosp.OscarProperties;
import ca.openosp.openo.tickler.TicklerCreator;
import ca.openosp.openo.util.DateUtils;

/**
 * Utility class for CAISI integrator conformance testing and data synchronization features.
 *
 * <p>This helper class provides specialized functionality for healthcare system integration
 * testing and conformance validation. It includes methods for processing remote provider
 * communications, synchronizing demographic data, and validating data consistency across
 * integrated healthcare facilities.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Remote provider message follow-up processing via tickler system</li>
 *   <li>Cross-facility demographic data synchronization</li>
 *   <li>Data consistency validation between local and remote records</li>
 *   <li>Conformance testing support for healthcare integration standards</li>
 * </ul>
 *
 * <p>The class is designed to support regulatory compliance testing and ensure
 * that healthcare data integration meets clinical workflow requirements while
 * maintaining HIPAA/PIPEDA privacy standards.</p>
 *
 * @see CaisiIntegratorManager
 * @see TicklerCreator
 * @since June 19, 2011
 */
public final class ConformanceTestHelper {
    private static Logger logger = MiscUtils.getLogger();
    public static boolean enableConformanceOnlyTestFeatures = Boolean.parseBoolean(OscarProperties.getInstance().getProperty("ENABLE_CONFORMANCE_ONLY_FEATURES"));

    /**
     * Retrieves remote provider follow-up messages and creates local tickler entries.
     *
     * <p>Processes provider communication messages of type "FOLLOWUP" from remote CAISI
     * facilities and converts them into local tickler reminders. This enables cross-facility
     * care coordination by ensuring that follow-up tasks from remote providers are visible
     * in the local provider's task list.</p>
     *
     * <p>The method extracts demographic ID, provider information, and message content
     * from XML-formatted provider communications and creates appropriately formatted
     * tickler entries with proper attribution to the original sender.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @since June 19, 2011
     */
    public static void populateLocalTicklerWithRemoteProviderMessageFollowUps(LoggedInInfo loggedInInfo) {
        try {
            ProviderWs providerWs = CaisiIntegratorManager.getProviderWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            List<ProviderCommunicationTransfer> followUps = providerWs.getProviderCommunications(loggedInInfo.getLoggedInProviderNo(), "FOLLOWUP", true);

            if (followUps == null) return;

            logger.debug("Folowups found : " + followUps.size());

            for (ProviderCommunicationTransfer providerCommunication : followUps) {
                Document doc = XmlUtils.toDocument(providerCommunication.getData());
                Node root = doc.getFirstChild();
                String demographicId = XmlUtils.getChildNodeTextContents(root, "destinationDemographicId");
                String note = XmlUtils.getChildNodeTextContents(root, "note");

                TicklerCreator t = new TicklerCreator();

                logger.debug("Create tickler : " + demographicId + ", " + providerCommunication.getDestinationProviderId() + ", " + note);

                FacilityIdStringCompositePk senderProviderId = new FacilityIdStringCompositePk();
                senderProviderId.setIntegratorFacilityId(providerCommunication.getSourceIntegratorFacilityId());
                senderProviderId.setCaisiItemId(providerCommunication.getSourceProviderId());
                CachedProvider senderProvider = CaisiIntegratorManager.getProvider(loggedInInfo, loggedInInfo.getCurrentFacility(), senderProviderId);
                if (senderProvider != null) {
                    note = "Sent by remote providers : " + senderProvider.getLastName() + ", " + senderProvider.getFirstName() + "<br />--------------------<br />" + note;
                }

                t.createTickler(loggedInInfo, demographicId, providerCommunication.getDestinationProviderId(), note);

                providerWs.deactivateProviderCommunication(providerCommunication.getId());
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    /**
     * Copies demographic properties from remote linked records to local demographic.
     *
     * <p>Synchronizes demographic information from the first linked remote demographic
     * record to the local demographic record. This ensures data consistency across
     * integrated facilities when patient information is updated remotely.</p>
     *
     * <p>The method retrieves directly linked demographics and applies field-level
     * updates while preserving roster date information for audit purposes.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param localDemographicId Integer the local demographic ID to update
     * @since June 19, 2011
     */
    public static void copyLinkedDemographicsPropertiesToLocal(LoggedInInfo loggedInInfo, Integer localDemographicId) {
        try {
            DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            List<DemographicTransfer> directLinks = demographicWs.getDirectlyLinkedDemographicsByDemographicId(localDemographicId);

            logger.debug("found linked demographics size:" + directLinks.size());

            if (directLinks.size() > 0) {
                DemographicTransfer demographicTransfer = directLinks.get(0);

                logger.debug("remoteDemographic:" + ReflectionToStringBuilder.toString(demographicTransfer));

                DemographicDao demographicDao = (DemographicDao) SpringUtils.getBean(DemographicDao.class);
                Demographic demographic = demographicDao.getDemographicById(localDemographicId);

                CaisiIntegratorManager.copyDemographicFieldsIfNotNull(demographicTransfer, demographic);

                demographic.setRosterDate(new Date());

                demographicDao.save(demographic);
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    /**
     * Checks if remote demographic data differs from local demographic data.
     *
     * <p>Performs field-by-field comparison between the local demographic record
     * and the first linked remote demographic to detect data inconsistencies.
     * This is used to identify when synchronization is needed or when conflicts
     * exist between facilities.</p>
     *
     * <p>Compares core demographic fields including birth date, contact information,
     * health insurance details, and address information using appropriate null-safe
     * comparison methods.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param localDemographicId Integer the local demographic ID to compare
     * @return boolean true if differences are detected, false if data matches
     * @since June 19, 2011
     */
    public static boolean hasDifferentRemoteDemographics(LoggedInInfo loggedInInfo, Integer localDemographicId) {
        boolean ret = false;
        try {
            DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            List<DemographicTransfer> directLinks = demographicWs.getDirectlyLinkedDemographicsByDemographicId(localDemographicId);

            logger.debug("found linked demographics size:" + directLinks.size());

            if (directLinks.size() > 0) {
                DemographicTransfer demographicTransfer = directLinks.get(0);

                logger.debug("remoteDemographic:" + ReflectionToStringBuilder.toString(demographicTransfer));

                DemographicDao demographicDao = (DemographicDao) SpringUtils.getBean(DemographicDao.class);
                Demographic demographic = demographicDao.getDemographicById(localDemographicId);

                if (demographicTransfer.getBirthDate() != null && !(DateUtils.getNumberOfDaysBetweenTwoDates(demographicTransfer.getBirthDate(), demographic.getBirthDay()) == 0))
                    ret = true;
                if (demographicTransfer.getCity() != null && !demographicTransfer.getCity().equalsIgnoreCase(demographic.getCity()))
                    ret = true;
                if (demographicTransfer.getFirstName() != null && !demographicTransfer.getFirstName().equals(demographic.getFirstName()))
                    ret = true;
                if (demographicTransfer.getGender() != null && !demographicTransfer.getGender().toString().equals(demographic.getSex()))
                    ret = true;
                if (demographicTransfer.getHin() != null && !demographicTransfer.getHin().equals(demographic.getHin()))
                    ret = true;
                if (demographicTransfer.getHinType() != null && !demographicTransfer.getHinType().equals(demographic.getHcType()))
                    ret = true;
                if (demographicTransfer.getHinVersion() != null && !demographicTransfer.getHinVersion().equals(demographic.getVer()))
                    ret = true;
                if (isRemoteDateDifferent(DateUtils.toGregorianCalendar(demographic.getEffDate()), demographicTransfer.getHinValidStart()))
                    ret = true;
                if (isRemoteDateDifferent(DateUtils.toGregorianCalendar(demographic.getHcRenewDate()), demographicTransfer.getHinValidEnd()))
                    ret = true;
                if (demographicTransfer.getLastName() != null && !demographicTransfer.getLastName().equals(demographic.getLastName()))
                    ret = true;
                if (demographicTransfer.getProvince() != null && !demographicTransfer.getProvince().equalsIgnoreCase(demographic.getProvince()))
                    ret = true;
                if (demographicTransfer.getSin() != null && !demographicTransfer.getSin().equals(demographic.getSin()))
                    ret = true;
                if (demographicTransfer.getStreetAddress() != null && !demographicTransfer.getStreetAddress().equals(demographic.getAddress()))
                    ret = true;
                if (demographicTransfer.getPhone1() != null && !demographicTransfer.getPhone1().equals(demographic.getPhone()))
                    ret = true;
                if (demographicTransfer.getPhone2() != null && !demographicTransfer.getPhone2().equals(demographic.getPhone2()))
                    ret = true;
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return ret;
    }


    /**
     * Determines if a remote date is different from a local date.
     *
     * <p>Compares two Calendar objects with special null handling logic:
     * - Both null: no difference (false)
     * - Local has value, remote is null: no difference (false)
     * - Local is null, remote has value: difference detected (true)
     * - Both have values: compare dates for differences (true if different)</p>
     *
     * <p>This logic prioritizes remote data when present while avoiding
     * overwrites when remote data is missing.</p>
     *
     * @param local Calendar the local date value
     * @param remote Calendar the remote date value
     * @return boolean true if remote date should override local, false otherwise
     * @since June 19, 2011
     */
    public static boolean isRemoteDateDifferent(Calendar local, Calendar remote) {
        boolean isRemoteDateDifferent = false;
        if (remote != null) {
            if (local == null) {
                isRemoteDateDifferent = true;
            } else if (!(DateUtils.getNumberOfDaysBetweenTwoDates(local, remote) == 0)) {
                isRemoteDateDifferent = true;
            }
        }
        return isRemoteDateDifferent;
    }


}
