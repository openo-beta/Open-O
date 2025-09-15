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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicAllergy;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicDrug;
import ca.openosp.openo.caisi_integrator.ws.DemographicWs;
import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Helper class for managing remote drug and allergy data in CAISI integrator.
 *
 * <p>This utility class provides specialized functionality for retrieving and processing
 * drug and allergy information from remote CAISI-integrated facilities. It supports
 * clinical decision support by providing access to comprehensive patient drug and allergy
 * histories across multiple healthcare facilities.</p>
 *
 * <p><strong>Key Healthcare Functions:</strong></p>
 * <ul>
 *   <li>ATC (Anatomical Therapeutic Chemical) code extraction from remote prescriptions</li>
 *   <li>Regional drug identifier code retrieval for drug interaction checking</li>
 *   <li>Remote allergy data conversion to local allergy objects</li>
 *   <li>Integration with drug reference systems for interaction checking</li>
 *   <li>Offline fallback support for continuity of care</li>
 * </ul>
 *
 * <p>The helper ensures that clinical decision support systems have access to complete
 * drug and allergy information regardless of where the patient received care previously,
 * supporting safer prescribing practices across the integrated healthcare network.</p>
 *
 * @see IntegratorFallBackManager
 * @see CaisiIntegratorManager
 * @see RemotePreventionHelper
 * @since 2009
 */
public class RemoteDrugAllergyHelper {
    private static Logger logger = MiscUtils.getLogger();

    /**
     * Retrieves ATC codes from remote drug prescriptions for a patient.
     *
     * <p>Extracts Anatomical Therapeutic Chemical (ATC) codes from all remote
     * drug prescriptions for the specified patient. ATC codes are essential for
     * drug interaction checking, therapeutic equivalence analysis, and clinical
     * decision support systems.</p>
     *
     * <p>The method supports both online and offline modes, falling back to
     * cached data when remote integrator services are unavailable.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param localDemographicId Integer the local patient demographic ID
     * @return ArrayList&lt;String&gt; list of ATC codes from remote prescriptions
     * @since 2009
     */
    public static ArrayList<String> getAtcCodesFromRemoteDrugs(LoggedInInfo loggedInInfo, Integer localDemographicId) {
        ArrayList<String> atcCodes = new ArrayList<String>();

        try {
            List<CachedDemographicDrug> remoteDrugs = null;
            try {
                if (!CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                    remoteDrugs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility()).getLinkedCachedDemographicDrugsByDemographicId(localDemographicId);
                }
            } catch (Exception e) {
                MiscUtils.getLogger().error("Unexpected error.", e);
                CaisiIntegratorManager.checkForConnectionError(loggedInInfo.getSession(), e);
            }

            if (CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                remoteDrugs = IntegratorFallBackManager.getRemoteDrugs(loggedInInfo, localDemographicId);
            }

            for (CachedDemographicDrug remoteDrug : remoteDrugs) {
                if (remoteDrug.getAtc() != null) atcCodes.add(remoteDrug.getAtc());
            }

        } catch (Exception e) {
            logger.error("Error ", e);
        }

        return (atcCodes);
    }

    /**
     * Retrieves regional identifier codes from remote drug prescriptions for a patient.
     *
     * <p>Extracts regional drug identifier codes from all remote drug prescriptions
     * for the specified patient. Regional identifiers are jurisdiction-specific
     * drug codes that may be required for local formulary checking, billing,
     * or regulatory compliance in different healthcare regions.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param localDemographicId Integer the local patient demographic ID
     * @return ArrayList&lt;String&gt; list of regional identifier codes from remote prescriptions
     * @since 2009
     */
    public static ArrayList<String> getRegionalIdentiferCodesFromRemoteDrugs(LoggedInInfo loggedInInfo, Integer localDemographicId) {
        ArrayList<String> regionalIdentifierCodes = new ArrayList<String>();

        try {
            List<CachedDemographicDrug> remoteDrugs = null;
            try {
                if (!CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                    remoteDrugs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility()).getLinkedCachedDemographicDrugsByDemographicId(localDemographicId);
                }
            } catch (Exception e) {
                MiscUtils.getLogger().error("Unexpected error.", e);
                CaisiIntegratorManager.checkForConnectionError(loggedInInfo.getSession(), e);
            }

            if (CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                remoteDrugs = IntegratorFallBackManager.getRemoteDrugs(loggedInInfo, localDemographicId);
            }

            for (CachedDemographicDrug remoteDrug : remoteDrugs) {
                if (remoteDrug.getAtc() != null) regionalIdentifierCodes.add(remoteDrug.getRegionalIdentifier());
            }

        } catch (Exception e) {
            logger.error("Error ", e);
        }

        return (regionalIdentifierCodes);
    }

    /**
     * Retrieves remote allergy data and converts it to local Allergy objects.
     *
     * <p>Fetches allergy information from remote CAISI facilities for the specified
     * patient and converts it to local Allergy objects suitable for drug interaction
     * checking and clinical decision support. This enables comprehensive allergy
     * awareness across the integrated healthcare network.</p>
     *
     * <p><strong>Important:</strong> The returned allergy objects are specifically
     * formatted for drug reference systems and should not be used for general
     * clinical documentation or storage. They have a demographic number of -1
     * to indicate their remote origin.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param localDemographicId Integer the local patient demographic ID
     * @return ArrayList&lt;Allergy&gt; list of allergy objects for drug interaction checking
     * @since 2009
     */
    public static ArrayList<Allergy> getRemoteAllergiesAsAllergyItems(LoggedInInfo loggedInInfo, Integer localDemographicId) {
        ArrayList<Allergy> results = new ArrayList<Allergy>();

        try {

            List<CachedDemographicAllergy> remoteAllergies = null;
            try {
                if (!CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                    DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
                    remoteAllergies = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility()).getLinkedCachedDemographicAllergies(localDemographicId);
                }
            } catch (Exception e) {
                MiscUtils.getLogger().error("Unexpected error.", e);
                CaisiIntegratorManager.checkForConnectionError(loggedInInfo.getSession(), e);
            }

            if (CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                remoteAllergies = IntegratorFallBackManager.getRemoteAllergies(loggedInInfo, localDemographicId);
            }

            for (CachedDemographicAllergy remoteAllergy : remoteAllergies) {
                results.add(convertRemoteAllergyToLocal(remoteAllergy));
            }

        } catch (Exception e) {
            logger.error("Error retriving remote drugs", e);
        }

        return (results);
    }

    /**
     * Converts a remote cached allergy to a local Allergy object for drug reference checking.
     *
     * <p>Transforms remote allergy data into a local Allergy object with appropriate
     * field mapping for drug interaction and clinical decision support systems. Sets
     * demographic number to -1 to indicate remote origin and prevent confusion with
     * local allergy records.</p>
     *
     * <p><strong>Usage Warning:</strong> The returned allergy objects are specifically
     * designed for drug reference system integration and should not be used for clinical
     * documentation, storage, or display purposes.</p>
     *
     * @param remoteAllergy CachedDemographicAllergy the remote allergy data to convert
     * @return Allergy the converted local allergy object for drug reference use
     * @since 2009
     */
    private static Allergy convertRemoteAllergyToLocal(CachedDemographicAllergy remoteAllergy) {

        Date entryDate = null;
        if (remoteAllergy.getEntryDate() != null) entryDate = remoteAllergy.getEntryDate().getTime();


        Allergy allergy = new Allergy();
        allergy.setDemographicNo(-1);
        allergy.setId(remoteAllergy.getFacilityIdIntegerCompositePk().getCaisiItemId());
        allergy.setDescription(remoteAllergy.getDescription());
        allergy.setHiclSeqno(remoteAllergy.getHiclSeqNo());
        allergy.setHicSeqno(remoteAllergy.getHicSeqNo());
        allergy.setAgcsp(remoteAllergy.getAgcsp());
        allergy.setAgccs(remoteAllergy.getAgccs());
        allergy.setTypeCode(remoteAllergy.getTypeCode());

        return (allergy);
    }
}
