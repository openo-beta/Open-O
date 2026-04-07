//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.Groups;
import org.springframework.stereotype.Repository;

@Repository
public class GroupsDaoImpl extends AbstractDaoImpl<Groups> implements GroupsDao {

    public GroupsDaoImpl() {
        super(Groups.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Groups> findByParentId(int groupId) {
        Query query = createQuery("g", "g.parentId = ?1");
        query.setParameter(1, groupId);
        return query.getResultList();
    }

}
