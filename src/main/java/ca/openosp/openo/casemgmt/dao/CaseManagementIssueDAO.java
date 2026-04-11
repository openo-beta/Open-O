//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.caisi_integrator.ws.FacilityIdDemographicIssueCompositePk;
import ca.openosp.openo.casemgmt.model.CaseManagementIssue;
import ca.openosp.openo.casemgmt.model.Issue;

public interface CaseManagementIssueDAO {

    public List<CaseManagementIssue> getIssuesByDemographic(String demographic_no);

    public List<CaseManagementIssue> getIssuesByDemographicOrderActive(Integer demographic_no, Boolean resolved);

    public List<CaseManagementIssue> getIssuesByNote(Integer noteId, Boolean resolved);

    public Issue getIssueByCmnId(Integer cmnIssueId);

    public CaseManagementIssue getIssuebyId(String demo, String id);

    public CaseManagementIssue getIssuebyIssueCode(String demo, String issueCode);

    public void deleteIssueById(CaseManagementIssue issue);

    public void saveAndUpdateCaseIssues(List<CaseManagementIssue> issuelist);

    public void saveIssue(CaseManagementIssue issue);

    public List<CaseManagementIssue> getAllCertainIssues();

    public List<Integer> getIssuesByProgramsSince(Date date, List<Program> programs);

    public List<CaseManagementIssue> getIssuesByDemographicSince(String demographic_no, Date date);

    public List<FacilityIdDemographicIssueCompositePk> getIssueIdsForIntegrator(Integer facilityId,
                                                                                Integer demographicNo);
}
