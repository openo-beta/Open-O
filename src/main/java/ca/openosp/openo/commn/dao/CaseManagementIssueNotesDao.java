//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.casemgmt.model.CaseManagementIssue;

public interface CaseManagementIssueNotesDao {

    public List<CaseManagementIssue> getNoteIssues(Integer noteId);

    public List<Integer> getNoteIdsWhichHaveIssues(String[] issueId);

}
