//CHECKSTYLE:OFF


package ca.openosp.openo.daos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import ca.openosp.openo.model.DefaultIssue;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;

public class DefaultIssueDaoImpl extends AbstractDaoImpl<DefaultIssue> implements DefaultIssueDao {

    public DefaultIssueDaoImpl() {
        super(DefaultIssue.class);
    }

    public DefaultIssue findDefaultIssue(Integer id) {
        Query query = entityManager.createQuery("select x from DefaultIssue x where x.id = ?1");
        query.setParameter(1, id);
        return getSingleResultOrNull(query);
    }

    @SuppressWarnings("unchecked")
    public DefaultIssue getLastestDefaultIssue() {
        Query query = entityManager.createQuery("select x from DefaultIssue x order by x.assignedtime desc");
        query.setMaxResults(1);
        List<DefaultIssue> issueList = query.getResultList();
        if (issueList == null || issueList.size() == 0) {
            return null;
        }
        return issueList.get(0);
    }

    @SuppressWarnings("unchecked")
    public List<DefaultIssue> findAll() {
        Query query = entityManager.createQuery("select x from DefaultIssue x order by x.assignedtime desc");
        return query.getResultList();
    }

    public void saveDefaultIssue(DefaultIssue issue) {
        if (issue.getId() != null && issue.getId() > 0) {
            merge(issue);
        } else {
            persist(issue);
        }
    }

    @SuppressWarnings("unchecked")
    public String[] findAllDefaultIssueIds() {
        Query query = entityManager.createQuery("select x.issueIds from DefaultIssue x order by x.assignedtime");
        query.setMaxResults(1);
        List<String> issueIdsList = query.getResultList();
        if (issueIdsList.size() == 0) {
            return new String[0];
        }
        Set<String> issueIdsSet = new HashSet<String>();
        for (String ids : issueIdsList) {
            String[] idsArr = ids.split(",");
            for (String id : idsArr) {
                if (id.length() > 0) {
                    issueIdsSet.add(id);
                }
            }
        }

        return issueIdsSet.toArray(new String[issueIdsSet.size()]);
    }
}
