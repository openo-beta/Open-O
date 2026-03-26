//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CaisiAccessType;
import org.springframework.stereotype.Repository;

@Repository
public class CaisiAccessTypeDaoImpl extends AbstractDaoImpl<CaisiAccessType> implements CaisiAccessTypeDao {

    public CaisiAccessTypeDaoImpl() {
        super(CaisiAccessType.class);
    }

    public List<CaisiAccessType> findAll() {
        String sql = "select x from CaisiAccessType x";
        Query query = entityManager.createQuery(sql);

        @SuppressWarnings("unchecked")
        List<CaisiAccessType> results = query.getResultList();
        return results;
    }

    public CaisiAccessType findByName(String name) {
        String sql = "select x from CaisiAccessType x WHERE x.name = ?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, name);

        @SuppressWarnings("unchecked")
        List<CaisiAccessType> results = query.getResultList();

        if (results.size() == 0) {
            return null;
        }

        return results.get(0);
    }
}
