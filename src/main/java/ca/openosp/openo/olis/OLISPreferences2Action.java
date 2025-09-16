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

import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.Misc;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.olis.dao.OLISSystemPreferencesDao;
import ca.openosp.openo.olis.model.OLISSystemPreferences;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
//import org.springframework.scheduling.timer.ScheduledTimerTask;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for managing OLIS system-wide preferences and polling configuration.
 *
 * <p>This administrative action allows system administrators to configure the global settings
 * for OLIS polling operations. It handles the configuration of polling time ranges, frequencies,
 * and patient filtering options. Changes to polling frequency trigger automatic restart of the
 * scheduler to apply new settings immediately.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Configure start and end times for polling ranges</li>
 *   <li>Set polling frequency (interval between polls)</li>
 *   <li>Enable/disable patient filtering</li>
 *   <li>Automatic scheduler restart when frequency changes</li>
 *   <li>Date/time format conversion for OLIS compatibility</li>
 * </ul>
 * </p>
 *
 * <p>This action requires administrative privileges (_admin) to execute. Input times are
 * converted from user-friendly format (YYYY-MM-dd HH:mm:ss Z) to OLIS format (YYYYMMddHHmmssZ)
 * before storage.</p>
 *
 * @since 2012-01-01
 * @see OLISSystemPreferences
 * @see OLISSystemPreferencesDao
 * @see OLISSchedulerJob
 */
public class OLISPreferences2Action extends ActionSupport {
    /**
     * HTTP request object for retrieving preference parameters.
     */
    HttpServletRequest request = ServletActionContext.getRequest();

    /**
     * HTTP response object for sending responses.
     * Currently unused but maintained for potential future enhancements.
     */
    HttpServletResponse response = ServletActionContext.getResponse();

    /**
     * Security manager for validating administrative privileges.
     * Ensures only administrators can modify OLIS system preferences.
     */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Executes the preference update action.
     *
     * <p>This method processes the OLIS system preference updates including:
     * <ol>
     *   <li>Validating administrative privileges</li>
     *   <li>Converting date/time formats from user input to OLIS format</li>
     *   <li>Updating polling frequency and patient filter settings</li>
     *   <li>Persisting changes to the database</li>
     *   <li>Restarting the scheduler if polling frequency changed</li>
     * </ol>
     * </p>
     *
     * <p>Request parameters:
     * <ul>
     *   <li><b>startTime</b> - Start time in format "YYYY-MM-dd HH:mm:ss Z"</li>
     *   <li><b>endTime</b> - End time in format "YYYY-MM-dd HH:mm:ss Z"</li>
     *   <li><b>pollFrequency</b> - Polling interval in minutes (default: 30)</li>
     *   <li><b>filter_patients</b> - Checkbox to enable patient filtering</li>
     * </ul>
     * </p>
     *
     * <p>Request attributes set:
     * <ul>
     *   <li><b>success</b> - Boolean indicating if update was successful</li>
     * </ul>
     * </p>
     *
     * @return String SUCCESS constant indicating successful execution
     * @throws Exception if date parsing fails or database update fails
     * @throws SecurityException if user lacks administrative privileges
     */
    @Override
    public String execute() throws Exception {
        // Verify administrative privileges
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_admin", "r", null)) {
            throw new SecurityException("missing required sec object (_admin)");
        }

        // Configure date formatters for converting between user and OLIS formats
        DateTimeFormatter input = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss Z");
        DateTimeFormatter output = DateTimeFormat.forPattern("YYYYMMddHHmmssZ");
        DateTime date;

        // Convert start time from user format to OLIS format
        String startTime = Misc.getStr(request.getParameter("startTime"), "").trim();
        if (!startTime.equals("")) {
            date = input.parseDateTime(startTime);
            startTime = date.toString(output);
        }

        // Convert end time from user format to OLIS format
        String endTime = Misc.getStr(request.getParameter("endTime"), "").trim();
        if (!endTime.equals("")) {
            date = input.parseDateTime(endTime);
            endTime = date.toString(output);
        }

        // Extract polling frequency (default to 30 minutes if not specified)
        Integer pollFrequency = Misc.getInt(request.getParameter("pollFrequency"), 30);

        // Check if patient filtering is enabled
        String filterPatients = request.getParameter("filter_patients");

        // Load current system preferences
        OLISSystemPreferencesDao olisPrefDao = (OLISSystemPreferencesDao) SpringUtils.getBean(OLISSystemPreferencesDao.class);
        OLISSystemPreferences olisPrefs = olisPrefDao.getPreferences();

        try {
            // Update preference values
            olisPrefs.setStartTime(startTime);
            olisPrefs.setEndTime(endTime);
            olisPrefs.setFilterPatients((filterPatients != null) ? true : false);

            // Check if polling frequency changed (requires scheduler restart)
            boolean restartTimer = !pollFrequency.equals(olisPrefs.getPollFrequency());
            olisPrefs.setPollFrequency(pollFrequency);

            // Persist changes to database
            olisPrefDao.save(olisPrefs);
            request.setAttribute("success", true);

            // Restart scheduler if polling frequency changed
            if (restartTimer) {
                // Get the scheduled task from Spring context
                ScheduledExecutorTask task = (ScheduledExecutorTask) SpringUtils.getBean(ScheduledExecutorTask.class);
                Runnable tt = task.getRunnable();

                // Start new thread with updated frequency
                Thread t = new Thread(tt);
                t.start();
            }

        } catch (Exception e) {
            // Log error and indicate failure to UI
            MiscUtils.getLogger().error("Changing Preferences failed", e);
            request.setAttribute("success", false);
        }
        return SUCCESS;

    }
}
