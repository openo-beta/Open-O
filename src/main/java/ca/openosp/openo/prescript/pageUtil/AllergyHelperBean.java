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


package ca.openosp.openo.prescript.pageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.caisi_integrator.CaisiIntegratorManager;
import ca.openosp.openo.PMmodule.caisi_integrator.IntegratorFallBackManager;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicAllergy;
import ca.openosp.openo.commn.dao.PartialDateDao;
import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.commn.model.PartialDate;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.prescript.data.RxPatientData;
import ca.openosp.openo.prescript.data.RxPatientData.Patient;
import ca.openosp.openo.util.DateUtils;

/**
 * Helper utility for aggregating allergy data from multiple sources.
 *
 * This utility class provides methods to retrieve and display allergy
 * information from both local and remote facilities through the CAISI
 * Integrator system. It handles the complexity of multi-facility allergy
 * data aggregation, including partial date formatting and fallback
 * mechanisms when the integrator is offline.
 *
 * The class transforms raw allergy data into AllergyDisplay objects
 * suitable for presentation in the prescription module's user interface.
 * It ensures comprehensive allergy checking across all integrated facilities
 * to support safe prescribing decisions.
 *
 * @since 2008-03-10
 */
public final class AllergyHelperBean {
    /**
     * Logger for error and debug messages.
     */
    private static Logger logger = MiscUtils.getLogger();

    /**
     * DAO for handling partial date formatting.
     */
    private static final PartialDateDao partialDateDao = (PartialDateDao) SpringUtils.getBean(PartialDateDao.class);

    /**
     * Retrieves all allergies for display from local and remote sources.
     *
     * Aggregates allergy information from the local database and any
     * integrated remote facilities. This ensures comprehensive allergy
     * checking across all facilities where the patient has records.
     *
     * @param loggedInInfo LoggedInInfo current user session information
     * @param demographicId Integer patient demographic identifier
     * @param locale Locale for date formatting
     * @return List<AllergyDisplay> combined list of local and remote allergies
     */
    public static List<AllergyDisplay> getAllergiesToDisplay(LoggedInInfo loggedInInfo, Integer demographicId, Locale locale) {
        ArrayList<AllergyDisplay> results = new ArrayList<AllergyDisplay>();

        addLocalAllergies(loggedInInfo, demographicId, results, locale);

        if (loggedInInfo.getCurrentFacility().isIntegratorEnabled()) {
            addIntegratorAllergies(loggedInInfo, demographicId, results, locale);
        }

        return (results);
    }

    /**
     * Adds local facility allergies to the display list.
     *
     * Retrieves active allergies from the local database and transforms
     * them into display objects. Handles partial date formatting for
     * entry and start dates. Only active (non-archived) allergies are
     * included.
     *
     * @param loggedInInfo LoggedInInfo current user session
     * @param demographicId Integer patient identifier
     * @param results ArrayList<AllergyDisplay> list to add allergies to
     * @param locale Locale for date formatting
     */
    private static void addLocalAllergies(LoggedInInfo loggedInInfo, Integer demographicId, ArrayList<AllergyDisplay> results, Locale locale) {
        Patient pt = RxPatientData.getPatient(loggedInInfo, demographicId);
        if (pt == null) {
            return;
        }
        Allergy[] allergies = pt.getActiveAllergies();

        if (allergies == null) return;

        for (Allergy allergy : allergies) {
            AllergyDisplay allergyDisplay = new AllergyDisplay();

            allergyDisplay.setId(allergy.getAllergyId());

            allergyDisplay.setDescription(allergy.getDescription());
            allergyDisplay.setOnSetCode(allergy.getOnsetOfReaction());
            allergyDisplay.setReaction(allergy.getReaction());
            allergyDisplay.setSeverityCode(allergy.getSeverityOfReaction());
            allergyDisplay.setTypeCode(allergy.getTypeCode());
            // Convert boolean archive status to string representation
            allergyDisplay.setArchived(allergy.getArchived() ? "1" : "0");

            // Format dates with partial date support (year only, year-month, or full date)
            String entryDate = partialDateDao.getDatePartial(allergy.getEntryDate(), PartialDate.ALLERGIES, allergy.getAllergyId(), PartialDate.ALLERGIES_ENTRYDATE);
            String startDate = partialDateDao.getDatePartial(allergy.getStartDate(), PartialDate.ALLERGIES, allergy.getAllergyId(), PartialDate.ALLERGIES_STARTDATE);
            allergyDisplay.setEntryDate(entryDate);
            allergyDisplay.setStartDate(startDate);

            results.add(allergyDisplay);
        }
    }

    /**
     * Adds remote facility allergies via CAISI Integrator.
     *
     * Retrieves allergy information from integrated remote facilities.
     * Includes fallback mechanism to use cached data if the integrator
     * is offline. Each remote allergy is tagged with its source facility
     * for proper identification.
     *
     * @param loggedInInfo LoggedInInfo current user session
     * @param demographicId Integer patient identifier
     * @param results ArrayList<AllergyDisplay> list to add allergies to
     * @param locale Locale for date formatting
     */
    private static void addIntegratorAllergies(LoggedInInfo loggedInInfo, Integer demographicId, ArrayList<AllergyDisplay> results, Locale locale) {
        try {
            List<CachedDemographicAllergy> remoteAllergies = null;
            try {
                // Attempt to retrieve allergies from integrator if online
                if (!CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                    remoteAllergies = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility()).getLinkedCachedDemographicAllergies(demographicId);
                }
            } catch (Exception e) {
                MiscUtils.getLogger().error("Unexpected error.", e);
                CaisiIntegratorManager.checkForConnectionError(loggedInInfo.getSession(), e);
            }

            // Use cached data if integrator is offline
            if (CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                remoteAllergies = IntegratorFallBackManager.getRemoteAllergies(loggedInInfo, demographicId);
            }


            // Convert each remote allergy to display format
            for (CachedDemographicAllergy remoteAllergy : remoteAllergies) {
                AllergyDisplay allergyDisplay = new AllergyDisplay();

                // Set composite key identifying remote facility and allergy
                allergyDisplay.setRemoteFacilityId(remoteAllergy.getFacilityIdIntegerCompositePk().getIntegratorFacilityId());
                allergyDisplay.setId(remoteAllergy.getFacilityIdIntegerCompositePk().getCaisiItemId());

                allergyDisplay.setDescription(remoteAllergy.getDescription());
                allergyDisplay.setEntryDate(DateUtils.formatDate(remoteAllergy.getEntryDate(), locale));
                allergyDisplay.setOnSetCode(remoteAllergy.getOnSetCode());
                allergyDisplay.setReaction(remoteAllergy.getReaction());
                allergyDisplay.setSeverityCode(remoteAllergy.getSeverityCode());
                allergyDisplay.setStartDate(DateUtils.formatDate(remoteAllergy.getStartDate(), locale));
                allergyDisplay.setTypeCode(remoteAllergy.getTypeCode());

                results.add(allergyDisplay);
            }
        } catch (Exception e) {
            logger.error("error getting remote allergies", e);
        }
    }
}
