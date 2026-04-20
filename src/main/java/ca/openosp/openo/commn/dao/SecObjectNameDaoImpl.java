//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.SecObjectName;
import org.springframework.stereotype.Repository;

@Repository(value = "secObjectNameDaoJpa")
public class SecObjectNameDaoImpl extends AbstractDaoImpl<SecObjectName> implements SecObjectNameDao {

    public SecObjectNameDaoImpl() {
        super(SecObjectName.class);
    }

    @Override
    public List<SecObjectName> findAll() {
        String sql = "SELECT s FROM SecObjectName s order by s.id";
        Query query = entityManager.createQuery(sql);
        @SuppressWarnings("unchecked")
        List<SecObjectName> result = query.getResultList();
        return result;
    }

    @Override
    public List<String> findDistinctObjectNames() {
        String sql = "SELECT distinct(s.id) FROM SecObjectName s order by s.id";
        Query query = entityManager.createQuery(sql);
        @SuppressWarnings("unchecked")
        List<String> result = query.getResultList();
        return result;
    }
}
