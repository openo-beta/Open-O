//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.MyGroup;
import ca.openosp.openo.commn.model.WaitingListName;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class WaitingListNameDaoImpl extends AbstractDaoImpl<WaitingListName> implements WaitingListNameDao {

    public WaitingListNameDaoImpl() {
        super(WaitingListName.class);
    }

    public long countActiveWatingListNames() {
        Query query = entityManager.createQuery("SELECT COUNT(*) FROM WaitingListName n WHERE n.isHistory = 'N'");
        query.setMaxResults(1);
        return (Long) query.getSingleResult();
    }

    public List<WaitingListName> findCurrentByNameAndGroup(String name, String group) {
        String sql = "select x from WaitingListName x where x.name = ?1 AND x.groupNo = ?2 and x.isHistory='N'";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, name);
        query.setParameter(2, group);

        @SuppressWarnings("unchecked")
        List<WaitingListName> results = query.getResultList();
        return results;
    }

    public List<WaitingListName> findByMyGroups(String providerNo, List<MyGroup> myGroups) {
        List<String> groupIds = new ArrayList<String>();
        for (MyGroup mg : myGroups) {
            groupIds.add(mg.getId().getMyGroupNo());
        }
        if (!groupIds.contains(providerNo)) {
            groupIds.add(providerNo);
        }
        groupIds.add(".default");

        String sql = "select x from WaitingListName x where x.groupNo IN (?1) and x.isHistory='N' order by x.name ASC";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, groupIds);

        @SuppressWarnings("unchecked")
        List<WaitingListName> results = query.getResultList();
        return results;
    }

    public List<WaitingListName> findCurrentByGroup(String group) {
        String sql = "select x from WaitingListName x where x.groupNo = ?1 and x.isHistory='N' order by x.name";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, group);

        @SuppressWarnings("unchecked")
        List<WaitingListName> results = query.getResultList();
        return results;
    }
}
