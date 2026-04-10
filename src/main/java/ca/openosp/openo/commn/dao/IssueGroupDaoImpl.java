//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.IssueGroup;
import org.springframework.stereotype.Repository;

@Repository
public class IssueGroupDaoImpl extends AbstractDaoImpl<IssueGroup> implements IssueGroupDao {

    public IssueGroupDaoImpl() {
        super(IssueGroup.class);
    }

    @Override
    public List<IssueGroup> findAll() {
        Query query = entityManager.createQuery("select x from IssueGroup x");

        @SuppressWarnings("unchecked")
        List<IssueGroup> results = query.getResultList();

        return (results);
    }

}
