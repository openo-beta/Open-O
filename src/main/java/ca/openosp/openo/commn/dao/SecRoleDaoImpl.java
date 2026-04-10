//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.SecRole;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SecRoleDaoImpl extends AbstractDaoImpl<SecRole> implements SecRoleDao {

    public SecRoleDaoImpl() {
        super(SecRole.class);
    }

    @Override
    public List<SecRole> findAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("select x from SecRole x");

        sb.append(" order by x.name");

        Query query = entityManager.createQuery(sb.toString());

        @SuppressWarnings("unchecked")
        List<SecRole> results = query.getResultList();

        return results;
    }

    @Override
    public List<String> findAllNames() {
        StringBuilder sb = new StringBuilder();
        sb.append("select x.name from SecRole x");

        sb.append(" order by x.name");

        Query query = entityManager.createQuery(sb.toString());

        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();

        return results;
    }

    @Override
    public SecRole findByName(String name) {
        Query q = entityManager.createQuery("select x from SecRole x where x.name=?1");

        q.setParameter(1, name);

        return this.getSingleResultOrNull(q);
    }

    @Override
    public List<SecRole> findAllOrderByRole() {
        Query query = entityManager.createQuery("select x from SecRole x order by x.name");

        @SuppressWarnings("unchecked")
        List<SecRole> results = query.getResultList();

        return results;
    }
}
