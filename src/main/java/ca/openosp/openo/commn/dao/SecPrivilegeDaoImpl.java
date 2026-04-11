//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.SecPrivilege;
import org.springframework.stereotype.Repository;

@Repository
public class SecPrivilegeDaoImpl extends AbstractDaoImpl<SecPrivilege> implements SecPrivilegeDao {

    public SecPrivilegeDaoImpl() {
        super(SecPrivilege.class);
    }

    @Override
    public List<SecPrivilege> findAll() {
        String sql = "SELECT s FROM SecPrivilege s order by s.id";

        Query query = entityManager.createQuery(sql);

        @SuppressWarnings("unchecked")
        List<SecPrivilege> result = query.getResultList();

        return result;
    }
}
