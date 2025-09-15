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

import java.util.List;

import ca.openosp.openo.caisi_integrator.ws.FacilityWs;
import ca.openosp.openo.caisi_integrator.ws.ImportLog;
import ca.openosp.openo.commn.dao.FacilityDao;
import ca.openosp.openo.commn.dao.IntegratorFileLogDao;
import ca.openosp.openo.commn.jobs.OscarRunnable;
import ca.openosp.openo.commn.model.IntegratorFileLog;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

/**
 * CAISI Integrator file transfer status monitoring and update job.
 *
 * <p>This background job monitors the status of healthcare data files transferred to the CAISI Integrator
 * system and synchronizes local file transfer logs with remote processing status. It ensures that healthcare
 * providers receive timely updates about the status of their data submissions, including successful processing,
 * errors, or ongoing processing states.</p>
 *
 * <p>The job queries the remote CAISI Integrator for import status updates on files that haven't been marked
 * as completed locally. This enables tracking of data integration workflows and provides feedback to healthcare
 * providers about the success or failure of their data submissions to the integrated healthcare network.</p>
 *
 * <p>File status tracking includes:
 * <ul>
 * <li>PROCESSING - File is being processed by the integrator</li>
 * <li>COMPLETED - File was successfully processed and integrated</li>
 * <li>ERROR - File processing failed due to validation or system errors</li>
 * </ul></p>
 *
 * <p>The job implements OscarRunnable to integrate with the OpenO EMR job scheduling system for automated
 * execution at regular intervals.</p>
 *
 * @see IntegratorFileLog
 * @see CaisiIntegratorManager
 * @see OscarRunnable
 * @since 2017-04-04
 */
public class IntegratorFileLogUpdateJob implements OscarRunnable {

    private Provider provider;
    private Security security;


    /**
     * Executes the file log status update job.
     *
     * <p>This method retrieves all local file logs that haven't been marked as completed and queries
     * the remote CAISI Integrator for their current processing status. It updates the local status
     * to reflect the most current state, prioritizing ERROR status over PROCESSING, and COMPLETED
     * status as the final state.</p>
     *
     * @since 2017-04-04
     */
    @Override
    public void run() {

        FacilityDao facilityDao = SpringUtils.getBean(FacilityDao.class);
        IntegratorFileLogDao integratorFileLogDao = SpringUtils.getBean(IntegratorFileLogDao.class);


        MiscUtils.getLogger().info("Running IntegratorFileLogUpdateJob");
        LoggedInInfo x = new LoggedInInfo();
        x.setLoggedInProvider(provider);
        x.setLoggedInSecurity(security);
        try {
            FacilityWs service = CaisiIntegratorManager.getFacilityWs(x, facilityDao.findAll(true).get(0));


            // Get all local file logs that haven't been marked as completed
            List<IntegratorFileLog> ourLogs = integratorFileLogDao.findAllWithNoCompletedIntegratorStatus();
            for (IntegratorFileLog ourLog : ourLogs) {
                // Query remote integrator for status updates on this file
                List<ImportLog> theirLogs = service.getImportLogByFilenameAndChecksum(ourLog.getFilename(), ourLog.getChecksum());
                String bestStatus = null;

                // Determine the most appropriate status from multiple import log entries
                // Priority: COMPLETED > ERROR > PROCESSING
                for (ImportLog theirLog : theirLogs) {
                    if (bestStatus == null) {
                        bestStatus = theirLog.getStatus();
                        continue;
                    }
                    // ERROR status takes priority over PROCESSING
                    if (theirLog.getStatus().equals("ERROR") && bestStatus.equals("PROCESSING")) {
                        bestStatus = theirLog.getStatus();
                    }
                    // COMPLETED status takes highest priority
                    if (theirLog.getStatus().equals("COMPLETED")) {
                        bestStatus = theirLog.getStatus();
                    }
                }
                // Update local log with the latest remote status
                ourLog.setIntegratorStatus(bestStatus);
                integratorFileLogDao.merge(ourLog);
                MiscUtils.getLogger().info("Updated " + ourLog.getFilename() + " with status " + ourLog.getIntegratorStatus());
            }


        } catch (Exception e) {
            MiscUtils.getLogger().error("Error getting file statuses", e);
        }

    }

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
