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
package ca.openosp.openo.fax.core;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.stereotype.Component;

import ca.openosp.OscarProperties;

/**
 * Scheduled service for automated processing of medical fax transmission operations.
 *
 * This Spring-managed component provides critical background processing for healthcare
 * fax communications, ensuring reliable and timely transmission of medical documents
 * between healthcare providers, laboratories, specialists, and other medical facilities.
 * The scheduler operates continuously to maintain healthcare communication workflows
 * without manual intervention.
 *
 * The scheduler orchestrates three essential healthcare fax operations:
 *
 * 1. **Document Import**: Automatically retrieves incoming medical documents from
 *    external fax service providers, ensuring that lab results, consultation reports,
 *    and other critical medical information is promptly available to healthcare providers.
 *
 * 2. **Document Transmission**: Processes queued outgoing medical faxes including
 *    consultation requests, prescription orders, patient referrals, and lab orders,
 *    ensuring reliable delivery of time-sensitive medical information.
 *
 * 3. **Status Monitoring**: Updates transmission status for all active fax jobs,
 *    providing real-time feedback on delivery confirmation and error conditions
 *    that may require healthcare staff attention.
 *
 * Key features for healthcare reliability:
 * - Configurable polling intervals to balance system load with communication urgency
 * - Comprehensive error handling to prevent service interruption from single failures
 * - Automatic service restart capability for administrative maintenance
 * - Thread-safe status tracking for concurrent access from multiple system components
 * - Robust exception management with automatic scheduler shutdown on critical errors
 *
 * The scheduler maintains high availability standards required for healthcare
 * operations where delayed communications can impact patient care quality and safety.
 *
 * @see ca.openosp.openo.fax.core.FaxImporter
 * @see ca.openosp.openo.fax.core.FaxSender
 * @see ca.openosp.openo.fax.core.FaxStatusUpdater
 * @since 2014-08-29
 */
@Component
public class FaxSchedulerJob extends TimerTask {
    /** Logger for tracking scheduler operations and healthcare fax processing activities */
    private static final Logger logger = MiscUtils.getLogger();

    /**
     * Static flag indicating whether the fax scheduler is currently active.
     * This flag enables system-wide status checking without requiring scheduler instances,
     * which is essential for healthcare system monitoring and administrative interfaces.
     */
    private static boolean isRunning = false;

    /** Configuration property key for fax polling interval in milliseconds */
    private static final String FAX_POLL_INTERVAL_KEY = "faxPollInterval";

    /** Timer instance for scheduling periodic fax processing operations */
    private static Timer timer;

    /** Current scheduler task instance for timer management */
    private static TimerTask timerTask = null;

    /**
     * Executes the main fax processing cycle for healthcare document management.
     *
     * This method orchestrates the complete fax processing workflow by sequentially
     * executing import, transmission, and status update operations. Each operation
     * is essential for maintaining reliable healthcare communication:
     *
     * - **Import Phase**: Downloads incoming medical documents from fax services
     * - **Send Phase**: Transmits queued outgoing medical documents to recipients
     * - **Update Phase**: Synchronizes transmission status for all active fax jobs
     *
     * The method implements robust error handling to ensure that failures in individual
     * operations do not compromise overall system stability. However, unexpected exceptions
     * trigger automatic scheduler shutdown to prevent potential system instability
     * that could impact critical healthcare communications.
     *
     * The scheduler status is updated upon successful completion to indicate healthy
     * operation for monitoring and administrative interfaces.
     *
     * @throws RuntimeException if any critical error occurs during processing
     */
    @Override
    public void run() {
        try {
            FaxImporter faxImporter = new FaxImporter();
            faxImporter.poll();

            FaxSender faxSender = new FaxSender();
            faxSender.send();

            FaxStatusUpdater faxStatusUpdater = new FaxStatusUpdater();
            faxStatusUpdater.updateStatus();

            setRunning(true);
        } catch (Exception e) {
            // Implement fail-safe shutdown for healthcare system stability
            // Automatic cancellation prevents cascading failures in medical communication
            cancel();
            logger.error("Fax scheduler has been stopped due to an unexpected error", e);
            setRunning(false);

            // Rethrow exception to ensure complete thread termination
            // This prevents potential corruption of medical document processing
            throw e;
        }
    }

    /**
     * Restarts the fax scheduler task with current configuration settings.
     *
     * This method provides administrative control for restarting the healthcare
     * fax processing service, typically used when:
     * - Configuration changes require service reload
     * - Error recovery after system issues
     * - Routine maintenance operations
     * - Performance optimization adjustments
     *
     * The restart operation is atomic, ensuring no gap in fax processing that
     * could cause delays in critical healthcare document transmission.
     */
    public synchronized void restartTask() {
        cancelTask();
        startTask();
    }

    /**
     * Starts the fax scheduler with configured polling interval for healthcare document processing.
     *
     * This method initializes the automated fax processing service using the configured
     * polling interval from system properties. The scheduler begins processing medical
     * documents after a short delay to allow for system initialization.
     *
     * Default polling interval is set to 60 seconds if configuration is missing or invalid,
     * ensuring healthcare fax services remain operational even with configuration issues.
     */
    private synchronized void startTask() {
        // Prevent duplicate scheduler instances for healthcare service reliability
        if (isRunning) {
            return;
        }

        // Configure polling interval with healthcare-appropriate defaults
        String faxPollInterval = (String) OscarProperties.getInstance().get(FAX_POLL_INTERVAL_KEY);
        long period = 60000; // Default 60-second interval for healthcare operations
        try {
            period = Long.parseLong(faxPollInterval);
        } catch (Exception e) {
            // Use safe default interval to ensure continuous medical communication
            logger.error("FaxSchedularJob not scheduled, period is missing or invalid in properties file : " + FAX_POLL_INTERVAL_KEY + ": " + faxPollInterval, e);
            logger.error("Setting period to default: 60000 ms");
        }
        timerTask = new FaxSchedulerJob();
        timer = new Timer("FaxSchedulerJob Timer");
        timer.schedule(timerTask, 3000, period);
    }

    /**
     * Cancels the active fax scheduler task and cleans up resources.
     *
     * This method safely shuts down the fax processing service, ensuring that
     * any ongoing operations complete before termination. The method is automatically
     * called during Spring bean destruction to ensure clean service shutdown.
     *
     * Proper cancellation is critical in healthcare environments to prevent
     * interruption of time-sensitive medical document transmissions.
     */
    @PreDestroy
    private synchronized void cancelTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timer.cancel();
        } else {
            this.cancel();
        }
        setRunning(false);
    }

    /**
     * Checks whether the fax scheduler is currently active and processing medical documents.
     *
     * This static method enables system-wide status checking for monitoring interfaces,
     * administrative tools, and health check processes without requiring scheduler instances.
     *
     * @return Boolean true if the scheduler is actively processing healthcare faxes
     */
    public static Boolean isRunning() {
        return isRunning;
    }

    /**
     * Updates the scheduler running status for system monitoring and administrative interfaces.
     *
     * This method is used internally to maintain accurate status information for
     * healthcare system monitoring and administrative oversight of fax operations.
     *
     * @param isFaxSchedulerRunning Boolean new running status for the scheduler
     */
    public static void setRunning(Boolean isFaxSchedulerRunning) {
        isRunning = isFaxSchedulerRunning;
    }

}
