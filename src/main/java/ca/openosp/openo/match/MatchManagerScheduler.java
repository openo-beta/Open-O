//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package ca.openosp.openo.match;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Scheduled task manager for automated healthcare patient-provider matching maintenance.
 *
 * <p>This class provides automated scheduling capabilities for the OpenO EMR matching system,
 * running periodic maintenance tasks to optimize patient-provider matches, clean up expired
 * assignments, and ensure the matching system operates efficiently over time.</p>
 *
 * <p>Scheduling Configuration:
 * <ul>
 *   <li>Execution Interval: Every 30 minutes (1,800,000 milliseconds)</li>
 *   <li>Initial Delay: Immediate start (0 milliseconds)</li>
 *   <li>Thread Model: Single dedicated scheduler thread</li>
 *   <li>Task Type: Fixed delay scheduling to prevent overlap</li>
 * </ul>
 * </p>
 *
 * <p>Healthcare Operations: The scheduler performs background maintenance that
 * supports optimal healthcare delivery by ensuring patient-provider matches
 * remain current and efficient. This includes handling changes in patient
 * status, provider availability, and system-wide optimization activities.</p>
 *
 * <p>System Integration: Uses a dedicated thread pool executor with custom
 * thread naming for easy identification in healthcare system monitoring
 * and logging systems.</p>
 *
 * @see MatchManager
 * @see IMatchManager
 * @since 2005-12-16
 */
public class MatchManagerScheduler {
    private Logger logger = MiscUtils.getLogger();
    private IMatchManager matchManager = new MatchManager();

    /** Initial delay before first scheduled task execution (immediate start) */
    private static final int DELAY = 0;

    /** Period between scheduled task executions (30 minutes in milliseconds) */
    private static final int PERIOD = 30 * 60 * 1000;

    /** Dedicated scheduler service for healthcare matching maintenance tasks */
    private ScheduledExecutorService MATCH_MANAGER_SCHEDULER =
            new ScheduledThreadPoolExecutor(1, new ScheduledMatchManagerThreadFactory());

    /**
     * Constructs and starts the healthcare matching system scheduler.
     *
     * <p>Upon instantiation, this constructor immediately begins the scheduled
     * execution of healthcare matching maintenance tasks. The scheduler runs
     * continuously to ensure optimal patient-provider matching performance.</p>
     *
     * <p>The scheduler uses fixed delay execution to prevent task overlap,
     * ensuring each maintenance cycle completes before the next begins.</p>
     */
    public MatchManagerScheduler() {
        // Start scheduled healthcare matching maintenance tasks
        MATCH_MANAGER_SCHEDULER.scheduleWithFixedDelay(new MatchMgrSheduledTask(),
                DELAY, PERIOD, TimeUnit.MILLISECONDS);
    }

    /**
     * Custom thread factory for healthcare matching scheduler threads.
     *
     * <p>Creates dedicated threads for healthcare matching maintenance with
     * descriptive naming for easy identification in system monitoring and logging.</p>
     */
    private static class ScheduledMatchManagerThreadFactory implements ThreadFactory {

        /**
         * Creates a new thread for healthcare matching scheduled tasks.
         *
         * @param r Runnable the task to be executed by the new thread
         * @return Thread configured thread for healthcare matching maintenance
         */
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "SCHEDULED_MATCH_MANAGER_THREAD-1");
        }
    }

    /**
     * Scheduled task implementation for healthcare matching system maintenance.
     *
     * <p>This runnable task executes periodic maintenance operations for the
     * healthcare patient-provider matching system. It handles system optimization,
     * cleanup activities, and ensures matching performance remains optimal.</p>
     */
    private class MatchMgrSheduledTask implements Runnable {

        /**
         * Executes healthcare matching system maintenance tasks.
         *
         * <p>This method runs every 30 minutes to perform scheduled maintenance
         * on the healthcare matching system. All exceptions are caught and logged
         * to ensure the scheduler continues operating despite individual task failures.</p>
         */
        @Override
        public void run() {
            try {
                // Execute scheduled healthcare matching maintenance
                logger.info("Processing scheduled MatchManager maintenance task..."
                    + matchManager.processEvent(null, IMatchManager.Event.SCHEDULED_EVENT));
            } catch (MatchManagerException e) {
                // Log errors but continue scheduler operation
                logger.error("Error while processing scheduled MatchManager maintenance task");
                logger.error("Scheduler error details:", e);
            }
        }
    }
}
