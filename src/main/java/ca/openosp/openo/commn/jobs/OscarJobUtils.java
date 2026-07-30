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
package ca.openosp.openo.commn.jobs;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.OscarJobDao;
import ca.openosp.openo.commn.model.OscarJob;
import ca.openosp.openo.commn.model.OscarJobType;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.utility.SpringUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

public class OscarJobUtils {

    /** A cron expression of the form "every N minutes", as written by the HRM polling configuration. */
    private static final Pattern EVERY_N_MINUTES = Pattern.compile("^0\\s+0/(\\d{1,4})\\s+\\*\\s+1/1\\s+\\*\\s+\\?$");

    /**
     * Builds the trigger used to schedule a job.
     * <p>
     * A step in the cron minute field restarts at the top of each hour, so an interval that does not
     * divide 60 leaves a short gap there: "every 25 minutes" fires at :00, :25 and :50, then at :00
     * again, which is 10 minutes later. Intervals that do not divide 60 are scheduled at a fixed rate
     * so the spacing between runs is exact.
     *
     * @param cronExpression String the cron expression stored on the job
     * @return Trigger a fixed-rate PeriodicTrigger when the interval does not divide 60, otherwise a CronTrigger
     */
    static Trigger buildTrigger(String cronExpression) {
        Matcher matcher = EVERY_N_MINUTES.matcher(cronExpression.trim());
        if (matcher.matches()) {
            int minutes = Integer.parseInt(matcher.group(1));
            if (minutes > 0 && 60 % minutes != 0) {
                PeriodicTrigger trigger = new PeriodicTrigger(minutes, TimeUnit.MINUTES);
                trigger.setFixedRate(true);
                return trigger;
            }
        }
        return new CronTrigger(cronExpression);
    }


    public static boolean isJobTypeCurrentlyValid(OscarJobType oscarJobType) {

        if (oscarJobType.getClassName() == null) {
            return false;
        }

        try {
            Class clazz = Class.forName(oscarJobType.getClassName());
            for (Class i : clazz.getInterfaces()) {
                if (i.getName().equals("ca.openosp.openo.commn.jobs.OscarRunnable")) {
                    return true;
                }
            }
        } catch (Exception e) {
            //ignore
        }

        return false;
    }


    public static void initializeJobExecutionFramework() throws Exception {
        //SpringTaskScheduler
        OscarJobDao oscarJobDao = SpringUtils.getBean(OscarJobDao.class);


        for (OscarJob job : oscarJobDao.findAll(0, OscarJobDao.MAX_LIST_RETURN_SIZE)) {
            scheduleJob(job);
        }

    }

    public static void resetJobExecutionFramework() throws Exception {
        //SpringTaskScheduler
        OscarJobDao oscarJobDao = SpringUtils.getBean(OscarJobDao.class);


        for (Integer jobId : OscarJobExecutingManager.getFutures().keySet()) {
            ScheduledFuture<Object> future = OscarJobExecutingManager.getFutures().get(jobId);
            if (future != null) {
                future.cancel(false);
            }
        }
        OscarJobExecutingManager.getFutures().clear();


        for (OscarJob job : oscarJobDao.findAll(0, OscarJobDao.MAX_LIST_RETURN_SIZE)) {
            scheduleJob(job);
        }

    }


    public static boolean scheduleJob(OscarJob job) throws Exception {
        //SpringTaskScheduler
        TaskScheduler taskScheduler = (TaskScheduler) SpringUtils.getBean(TaskScheduler.class);
        OscarJobDao oscarJobDao = SpringUtils.getBean(OscarJobDao.class);
        ProviderDao providerDao = SpringUtils.getBean(ProviderDao.class);

        ScheduledFuture<Object> future = OscarJobExecutingManager.getFutures().get(job.getId());
        if (future != null) {
            future.cancel(false);
        }

        if (!job.isEnabled()) {
            return false;
        }
        if (job.getCronExpression() == null) {
            return false;
        }
        if (job.getOscarJobType() == null || !job.getOscarJobType().isEnabled() || !OscarJobUtils.isJobTypeCurrentlyValid(job.getOscarJobType())) {
            return false;
        }

        Trigger trigger = buildTrigger(job.getCronExpression());

        OscarRunnable oscarRunnableInstance = (OscarRunnable) Class.forName(job.getOscarJobType().getClassName()).newInstance();

        Security security = new Security();
        security.setSecurityNo(0);
        oscarRunnableInstance.setLoggedInSecurity(security);

        Provider provider = providerDao.getProvider(job.getProviderNo());
        if (provider == null) {
            return false;
        }
        oscarRunnableInstance.setLoggedInProvider(provider);
        oscarRunnableInstance.setConfig(job.getConfig());

        ScheduledFuture<Object> schedulefuture = (ScheduledFuture<Object>) taskScheduler.schedule(oscarRunnableInstance, trigger);
        //cancel,isCancelled, isDone

        OscarJobExecutingManager.getFutures().put(job.getId(), schedulefuture);

        return true;
    }

}
