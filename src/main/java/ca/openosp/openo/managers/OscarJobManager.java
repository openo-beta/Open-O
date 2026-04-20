//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.List;

import ca.openosp.openo.commn.model.OscarJob;
import ca.openosp.openo.commn.model.OscarJobType;
import ca.openosp.openo.utility.LoggedInInfo;

public interface OscarJobManager {


    public void saveJob(LoggedInInfo loggedInInfo, OscarJob oscarJob);

    public void updateJob(LoggedInInfo loggedInInfo, OscarJob oscarJob);

    /*
     * Make sure it's a valid class, and that it implements OscarRunnable
     */
    public List<OscarJobType> getCurrentlyAvaliableJobTypes();

    public List<OscarJobType> getAllJobTypes();

    public List<OscarJob> getAllJobs(LoggedInInfo loggedInInfo);

    public List<OscarJob> getJobByName(LoggedInInfo loggedInInfo, String name);

    public OscarJob getJob(LoggedInInfo loggedInInfo, Integer id);

    public OscarJobType addIfNotLoaded(LoggedInInfo loggedInInfo, OscarJobType jobType);

    public OscarJobType getJobType(LoggedInInfo loggedInInfo, Integer id);

    public void saveJobType(LoggedInInfo loggedInInfo, OscarJobType oscarJob);

    public void updateJobType(LoggedInInfo loggedInInfo, OscarJobType oscarJob);
}
