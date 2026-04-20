//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.EFormReportTool;
import ca.openosp.openo.utility.LoggedInInfo;


/**
 * @author Marc Dumontier
 */
public interface EFormReportToolManager {


    //@PersistenceContext
    //protected EntityManager entityManager = null;


    public List<EFormReportTool> findAll(LoggedInInfo loggedInInfo, Integer offset, Integer limit);

    /*
     * Updates the eft_latest column to 1 for the latest form from each demographic. This is calculated by latest form date/form time, and in the
     * case that there's 2 results, the highest fdid will be marked.
     */
    public void markLatest(LoggedInInfo loggedInInfo, Integer eformReportToolId);

    public void addNew(LoggedInInfo loggedInInfo, EFormReportTool eformReportTool);

    public void populateReportTable(LoggedInInfo loggedInInfo, Integer eformReportToolId);

    public void deleteAllData(LoggedInInfo loggedInInfo, Integer eformReportToolId);

    public void remove(LoggedInInfo loggedInInfo, Integer eformReportToolId);

    public int getNumRecords(LoggedInInfo loggedInInfo, Integer eformReportToolId);

    public Integer getNumRecords(LoggedInInfo loggedInInfo, EFormReportTool eformReportTool);
}
