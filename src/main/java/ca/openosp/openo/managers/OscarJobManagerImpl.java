//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import ca.openosp.openo.commn.dao.OscarJobDao;
import ca.openosp.openo.commn.dao.OscarJobTypeDao;
import ca.openosp.openo.commn.jobs.OscarJobUtils;
import ca.openosp.openo.commn.model.OscarJob;
import ca.openosp.openo.commn.model.OscarJobType;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.openosp.openo.log.LogAction;

@Service
public class OscarJobManagerImpl implements OscarJobManager {

    //private Logger logger = MiscUtils.getLogger();

    @Autowired
    private OscarJobDao oscarJobDao;

    @Autowired
    private OscarJobTypeDao oscarJobTypeDao;


    public void saveJob(LoggedInInfo loggedInInfo, OscarJob oscarJob) {
        oscarJobDao.persist(oscarJob);

        //--- log action ---
        LogAction.addLogSynchronous(loggedInInfo, "OscarJobManager.saveJob", "oscarJob.id=" + oscarJob.getId());
    }

    public void updateJob(LoggedInInfo loggedInInfo, OscarJob oscarJob) {
        oscarJobDao.merge(oscarJob);

        //--- log action ---
        LogAction.addLogSynchronous(loggedInInfo, "OscarJobManager.updateJob", "oscarJob.id=" + oscarJob.getId());
    }

    /*
     * Make sure it's a valid class, and that it implements OscarRunnable
     */
    public List<OscarJobType> getCurrentlyAvaliableJobTypes() {

        List<OscarJobType> validTypes = new ArrayList<OscarJobType>();

        for (OscarJobType oscarJobType : oscarJobTypeDao.findAll(0, AbstractDaoImpl.MAX_LIST_RETURN_SIZE)) {
            if (OscarJobUtils.isJobTypeCurrentlyValid(oscarJobType)) {
                validTypes.add(oscarJobType);
            }
        }
        return validTypes;
    }

    public List<OscarJobType> getAllJobTypes() {
        return oscarJobTypeDao.findAll(0, AbstractDaoImpl.MAX_LIST_RETURN_SIZE);
    }


    public List<OscarJob> getAllJobs(LoggedInInfo loggedInInfo) {

        List<OscarJob> jobs = oscarJobDao.findAll(0, OscarJobDao.MAX_LIST_RETURN_SIZE);

        //--- log action ---
        LogAction.addLogSynchronous(loggedInInfo, "OscarJobManager.getAllJobs", "");

        return jobs;
    }

    public List<OscarJob> getJobByName(LoggedInInfo loggedInInfo, String name) {
        List<OscarJob> jobs = oscarJobDao.getJobByName(name);
        //--- log action ---
        LogAction.addLog(loggedInInfo, "OscarJobManager.getJobByName", null, null, null, "name=" + name);
        return jobs;
    }

    public OscarJob getJob(LoggedInInfo loggedInInfo, Integer id) {

        OscarJob job = oscarJobDao.find(id);

        if (job != null) {
            //--- log action ---
            LogAction.addLogSynchronous(loggedInInfo, "OscarJobManager.getJob", "id=" + id);
        }

        return job;
    }

    public OscarJobType addIfNotLoaded(LoggedInInfo loggedInInfo, OscarJobType jobType) {
        List<OscarJobType> allJobs = getAllJobTypes();
        boolean loaded = false;
        for (OscarJobType ojt : allJobs) {
            if (ojt.getName().equals(jobType.getName()) && ojt.getClassName().equals(jobType.getClassName())) {
                return ojt;
            }
        }
        if (!loaded) {
            saveJobType(loggedInInfo, jobType);
            return jobType;
        }
        return null;
    }

    public OscarJobType getJobType(LoggedInInfo loggedInInfo, Integer id) {

        OscarJobType jobType = oscarJobTypeDao.find(id);

        if (jobType != null) {
            //--- log action ---
            LogAction.addLogSynchronous(loggedInInfo, "OscarJobManager.getJobType", "id=" + id);
        }

        return jobType;
    }

    public void saveJobType(LoggedInInfo loggedInInfo, OscarJobType oscarJob) {
        oscarJobTypeDao.persist(oscarJob);

        //--- log action ---
        LogAction.addLogSynchronous(loggedInInfo, "OscarJobManager.saveJobType", "oscarJobType.id=" + oscarJob.getId());
    }

    public void updateJobType(LoggedInInfo loggedInInfo, OscarJobType oscarJob) {
        oscarJobTypeDao.merge(oscarJob);

        //--- log action ---
        LogAction.addLogSynchronous(loggedInInfo, "OscarJobManager.updateJobType", "oscarJobType.id=" + oscarJob.getId());
    }

}
