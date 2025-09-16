//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.olis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.olis.dao.OLISSystemPreferencesDao;
import ca.openosp.openo.olis.model.OLISSystemPreferences;
import ca.openosp.openo.utility.DbConnectionFilter;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

/**
 * OLIS Scheduler Job for automated polling of laboratory results from Ontario Laboratories Information System.
 * <p>
 * This class extends TimerTask to provide scheduled execution of OLIS polling operations based on
 * system preferences. The job checks configured time windows, frequency settings, and last run times
 * to determine when to execute OLIS result retrieval.
 * <p>
 * Key features:
 * - Time window validation (start/end times)
 * - Frequency-based polling control
 * - Automatic database connection cleanup
 * - Comprehensive logging of operations
 * - Integration with OLISSystemPreferences for configuration
 *
 * @since 2008
 */
public class OLISSchedulerJob extends TimerTask {

    /** Logger for tracking OLIS polling operations and errors */
    private static final Logger logger = MiscUtils.getLogger();

    /**
     * Executes the OLIS polling task based on system preferences and timing constraints.
     * <p>
     * This method performs the following operations:
     * 1. Retrieves OLIS system preferences from database
     * 2. Validates current time against configured time windows
     * 3. Checks polling frequency to prevent excessive requests
     * 4. Initiates OLIS result polling if conditions are met
     * 5. Updates last run timestamp in preferences
     * <p>
     * The job will skip execution if:
     * - No preferences are configured
     * - Current time is outside configured time window
     * - Insufficient time has passed since last poll based on frequency setting
     *
     * @throws RuntimeException if database operations or OLIS polling fails
     */
    @Override
    public void run() {
        try {
            logger.info("starting OLIS poller job");

            // Retrieve OLIS system preferences to check if polling is enabled and configured
            OLISSystemPreferencesDao olisPrefDao = (OLISSystemPreferencesDao) SpringUtils.getBean(OLISSystemPreferencesDao.class);
            OLISSystemPreferences olisPrefs = olisPrefDao.getPreferences();
            if (olisPrefs == null) {
                // OLIS preferences not configured - skip polling
                logger.info("Don't need to run right now..no prefs");
                return;
            }
            Date now = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmssZ");
            Date startDate = null, endDate = null;

            // Parse configured time window from preferences
            try {
                if (olisPrefs.getStartTime() != null && olisPrefs.getStartTime().trim().length() > 0)
                    startDate = dateFormatter.parse(olisPrefs.getStartTime());

                if (olisPrefs.getEndTime() != null && olisPrefs.getEndTime().trim().length() > 0)
                    endDate = dateFormatter.parse(olisPrefs.getEndTime());
            } catch (ParseException e) {
                logger.error("Error parsing OLIS time window preferences", e);
            }
            logger.info("start date = " + startDate);
            logger.info("end date = " + endDate);

            // Check if current time falls within configured polling window
            if ((startDate != null && now.before(startDate)) || (endDate != null && now.after(endDate))) {
                logger.info("Don't need to run right now - outside time window");
                return;
            }

            // Check polling frequency to prevent excessive OLIS requests
            if (olisPrefs.getLastRun() != null) {
                // Verify sufficient time has passed since last poll based on configured frequency
                int freqMins = (olisPrefs.getPollFrequency() != null) ? olisPrefs.getPollFrequency() : 0;
                Calendar cal = Calendar.getInstance();
                cal.setTime(olisPrefs.getLastRun());
                cal.add(Calendar.MINUTE, freqMins);

                if (cal.getTime().getTime() > now.getTime()) {
                    logger.info("not yet time to run - last run @ " + olisPrefs.getLastRun() + " and freq is " + freqMins + " mins.");
                    return;
                }
            }

            logger.info("===== OLIS JOB RUNNING....");

            // Update last run timestamp before polling to prevent concurrent executions
            olisPrefs.setLastRun(new Date());
            olisPrefDao.merge(olisPrefs);

            // Execute OLIS polling to retrieve new laboratory results
            OLISPollingUtil.requestResults(null);
        } catch (Exception e) {
            logger.error("Error during OLIS polling execution", e);
        } finally {
            // Ensure database connections are properly released after job execution
            DbConnectionFilter.releaseAllThreadDbResources();
        }
    }

}
