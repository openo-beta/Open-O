//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import ca.openosp.openo.casemgmt.model.CaseManagementIssue;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class CaseManagementIssueNotesDaoImpl implements CaseManagementIssueNotesDao {

    @PersistenceContext(unitName = "entityManagerFactory")
    protected EntityManager entityManager = null;

    @Override
    public List<CaseManagementIssue> getNoteIssues(Integer noteId) {
        Query query = entityManager.createNativeQuery(
                "select casemgmt_issue.* from casemgmt_issue_notes, casemgmt_issue where note_id=?1 and casemgmt_issue_notes.id=casemgmt_issue.id",
                CaseManagementIssue.class);
        query.setParameter(1, noteId);

        @SuppressWarnings("unchecked")
        List<CaseManagementIssue> results = query.getResultList();
        return (results);
    }

    @Override
    public List<Integer> getNoteIdsWhichHaveIssues(String[] issueId) {
        if (issueId == null || issueId.length == 0)
            return null;
        if (issueId.length == 1 && issueId[0].equals(""))
            return null;

        // Build parameterized IN clause
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < issueId.length; i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?").append(i + 1);
        }

        String sql = "select note_id from casemgmt_issue_notes where id in (" + placeholders.toString() + ")";

        Query query = entityManager.createNativeQuery(sql);

        for (int i = 0; i < issueId.length; i++) {
            query.setParameter(i + 1, issueId[i]);
        }

        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();
        return (results);
    }

}
