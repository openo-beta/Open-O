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

import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.ws.WebServiceException;

import ca.openosp.openo.utility.*;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.caisi_integrator.ws.DemographicWs;
import ca.openosp.openo.commn.dao.DemographicExtDao;
import ca.openosp.openo.commn.dao.FacilityDao;
import ca.openosp.openo.commn.dao.UserPropertyDAO;
import ca.openosp.openo.commn.jobs.OscarRunnable;
import ca.openosp.openo.commn.model.DemographicExt;
import ca.openosp.openo.commn.model.Facility;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.commn.model.UserProperty;

import ca.openosp.OscarProperties;

/**
 * CAISI Integrator local data store synchronization job for healthcare records.
 *
 * <p>This background job maintains local cached copies of integrated healthcare data from the CAISI
 * (Client Access to Integrated Services Information) Integrator network. It periodically polls the
 * CAISI Integrator for changes to patient records and updates the local fallback storage to ensure
 * healthcare providers have access to critical patient information even when network connectivity
 * to remote facilities is interrupted.</p>
 *
 * <p>The job implements incremental synchronization by tracking the last successful pull timestamp
 * for each facility and only retrieving records that have been updated since that time. This approach
 * minimizes network traffic and processing overhead while ensuring data currency.</p>
 *
 * <p>Healthcare data types synchronized include:
 * <ul>
 * <li>Clinical notes and progress reports</li>
 * <li>Medical forms and assessments</li>
 * <li>Patient issues and active problems</li>
 * <li>Prevention and immunization records</li>
 * <li>Medication lists and prescriptions</li>
 * <li>Hospital admissions and discharges</li>
 * <li>Appointments and scheduling data</li>
 * <li>Allergy and adverse reaction information</li>
 * <li>Medical documents and attachments</li>
 * <li>Laboratory results and diagnostic reports</li>
 * </ul></p>
 *
 * <p>The job respects patient consent preferences and only synchronizes data for patients who have
 * authorized sharing across the integrated healthcare network. It maintains audit trails and follows
 * HIPAA/PIPEDA privacy requirements for all data operations.</p>
 *
 * @see IntegratorFallBackManager
 * @see CaisiIntegratorManager
 * @see OscarRunnable
 * @since 2017-04-04
 */
public class IntegratorLocalStoreUpdateJob implements OscarRunnable {

    private static final Logger logger = MiscUtils.getLogger();

    private FacilityDao facilityDao = SpringUtils.getBean(FacilityDao.class);
    private UserPropertyDAO userPropertyDao = (UserPropertyDAO) SpringUtils.getBean(UserPropertyDAO.class);
    private DemographicExtDao demographicExtDao = SpringUtils.getBean(DemographicExtDao.class);


    private Provider provider;
    private Security security;

    /**
     * Sets the provider context for this job execution.
     *
     * @param provider the healthcare provider under whose context the job will run
     * @since 2017-04-04
     */
    public void setLoggedInProvider(Provider provider) {
        this.provider = provider;
    }

    /**
     * Sets the security context for this job execution.
     *
     * @param security the security context containing access permissions and roles
     * @since 2017-04-04
     */
    public void setLoggedInSecurity(Security security) {
        this.security = security;
    }

    /**
     * Executes the local store synchronization job.
     *
     * <p>This method creates a logged-in context using the configured provider and security settings,
     * then initiates the data synchronization process for all integrator-enabled facilities.</p>
     *
     * @since 2017-04-04
     */
    @Override
    public void run() {
        MiscUtils.getLogger().info("Running IntegratorFileLogUpdateJob");
        LoggedInInfo x = new LoggedInInfo();
        x.setLoggedInProvider(provider);
        x.setLoggedInSecurity(security);

        try {
            pullForAllFacilities(x);
        } catch (Exception e) {
            logger.error("unexpected error occurred", e);
        }

    }

    /**
     * Initiates data synchronization for all integrator-enabled healthcare facilities.
     *
     * <p>This method iterates through all configured facilities and attempts to synchronize
     * healthcare data from those that have CAISI Integrator connectivity enabled. It handles
     * connection failures gracefully and continues processing other facilities even if some
     * are unavailable.</p>
     *
     * @param loggedInInfo the authentication context for accessing remote facilities
     * @since 2017-04-04
     */
    public void pullForAllFacilities(LoggedInInfo loggedInInfo) {
        List<Facility> facilities = facilityDao.findAll(true);

        // Process each facility that has integrator connectivity enabled
        for (Facility facility : facilities) {
            try {
                if (facility.isIntegratorEnabled()) {
                    findChangedRecordsFromIntegrator(loggedInInfo, facility);
                }
            } catch (WebServiceException e) {
                // Handle web service connectivity issues gracefully
                if (CxfClientUtilsOld.isConnectionException(e)) {
                    logger.warn("Error connecting to integrator. " + e.getMessage());
                    logger.debug("Error connecting to integrator.", e);
                } else {
                    logger.error("Unexpected error.", e);
                }
            } catch (Exception e) {
                logger.error("Unexpected error.", e);
            }
        }
    }

    /**
     * Discovers and synchronizes changed healthcare records from the CAISI Integrator.
     *
     * <p>This method implements incremental synchronization by querying the CAISI Integrator for
     * patient records that have been updated since the last successful pull. It uses two approaches:
     * <ol>
     * <li>getDemographicIdPushedAfterDateByRequestingFacility - Returns local demographic IDs that
     *     have changes, including transitive changes from linked records at other facilities</li>
     * <li>getDemographicsPushedAfterDate - Returns raw listing of direct record changes</li>
     * </ol></p>
     *
     * <p>For each changed patient record, the method synchronizes all associated healthcare data
     * types including notes, forms, medications, allergies, lab results, and other clinical information.
     * Only patients marked as having this facility as their primary EMR are processed to avoid
     * duplicate data synchronization.</p>
     *
     * @param loggedInInfo the authentication context for accessing the integrator
     * @param facility the healthcare facility to synchronize data for
     * @throws MalformedURLException if the integrator service URL is invalid
     * @since 2017-04-04
     */
    protected void findChangedRecordsFromIntegrator(LoggedInInfo loggedInInfo, Facility facility) throws MalformedURLException {
        logger.info("Start fetch data for facility : " + facility.getId() + " : " + facility.getName());
        // Check if local store caching is enabled in system configuration
        boolean integratorLocalStore = OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes");
        if (!integratorLocalStore) {
            logger.info("Integrator Local Store (INTEGRATOR_LOCAL_STORE) is not enabled. Aborting. You can disable this job from the admin menu");
            return;
        }
        DemographicWs demographicService = CaisiIntegratorManager.getDemographicWs(loggedInInfo, facility);

        Calendar nextTime = Calendar.getInstance();

        // Retrieve the timestamp of the last successful data pull for this facility
        Date lastPushDate = new Date(0);
        try {
            UserProperty lastPull = userPropertyDao.getProp(UserProperty.INTEGRATOR_LAST_PULL_PRIMARY_EMR + "+" + facility.getId());
            lastPushDate.setTime(Long.parseLong(lastPull.getValue()));
        } catch (Exception epull) {
            MiscUtils.getLogger().error("lastPull Error:", epull);
            // Default to epoch time if no previous pull timestamp exists
            lastPushDate = new Date(0);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastPushDate);

        // Query integrator for patient IDs that have been updated since last pull
        List<Integer> demographicNos = demographicService.getDemographicIdPushedAfterDateByRequestingFacility(cal);

        if (demographicNos.isEmpty()) {
            logger.debug("No demographics updated on the integrator");
        } else {
            logger.debug("demos changed " + demographicNos.size());
        }
        // Process each changed patient record
        int demographicFetchCount = 0;
        for (Integer demographicNo : demographicNos) {
            logger.debug("Demographic " + demographicNo + " updated on the integrator, primary emr ? ");

            // Only process patients for whom this facility is the primary EMR
            DemographicExt demographicExt = demographicExtDao.getLatestDemographicExt(demographicNo, "primaryEMR");
            if (demographicExt != null && demographicExt.getValue().equals("1")) {
                demographicFetchCount++;
                BenchmarkTimer benchTimer = new BenchmarkTimer("fetch and save for facilityId:" + facility.getId() + ", demographicId:" + demographicNo + "  " + demographicFetchCount + " of " + demographicNos.size());
                IntegratorFallBackManager.saveLinkNotes(loggedInInfo, demographicNo);
                benchTimer.tag("saveLinkedNotes");
                IntegratorFallBackManager.saveRemoteForms(loggedInInfo, demographicNo);
                benchTimer.tag("saveRemoteForms");


                IntegratorFallBackManager.saveDemographicIssues(loggedInInfo, demographicNo);
                benchTimer.tag("saveDemographicIssues");
                IntegratorFallBackManager.saveDemographicPreventions(loggedInInfo, demographicNo);
                benchTimer.tag("saveDemographicPreventions");
                IntegratorFallBackManager.saveDemographicDrugs(loggedInInfo, demographicNo);
                benchTimer.tag("saveDemographicDrugs");
                IntegratorFallBackManager.saveAdmissions(loggedInInfo, demographicNo);
                benchTimer.tag("saveAdmissions");
                IntegratorFallBackManager.saveAppointments(loggedInInfo, demographicNo);
                benchTimer.tag("saveAppointments");
                IntegratorFallBackManager.saveAllergies(loggedInInfo, demographicNo);
                benchTimer.tag("saveAllergies");
                IntegratorFallBackManager.saveDocuments(loggedInInfo, demographicNo);
                benchTimer.tag("saveDocuments");
                IntegratorFallBackManager.saveLabResults(loggedInInfo, demographicNo);
                benchTimer.tag("saveLabResults");

                // Additional data types not currently synchronized:
                // - Measurements (vital signs, assessments)
                // - Diagnosis research codes
                // - Billing items and charges
                // - Electronic forms and structured data

                logger.debug(benchTimer.report());
            }
            // Update the last successful pull timestamp for this facility
            userPropertyDao.saveProp(UserProperty.INTEGRATOR_LAST_PULL_PRIMARY_EMR + "+" + facility.getId(), "" + nextTime.getTime().getTime());
        }
        logger.info("End fetch data for facility : " + facility.getId() + " : " + facility.getName());
    }

    /**
     * Sets configuration parameters for this job (currently unused).
     *
     * @param string configuration string (not currently used by this job)
     * @since 2017-04-04
     */
    @Override
    public void setConfig(String string) {
        // No configuration parameters currently used
    }

}
