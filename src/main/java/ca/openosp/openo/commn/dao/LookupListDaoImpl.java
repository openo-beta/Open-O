//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.LookupList;
import org.springframework.stereotype.Repository;

@Repository
public class LookupListDaoImpl extends AbstractDaoImpl<LookupList> implements LookupListDao {

    public LookupListDaoImpl() {
        super(LookupList.class);
    }

    @Override
    public List<LookupList> findAllActive() {
        Query q = entityManager.createQuery("select l from LookupList l where l.active=?1 order by l.name asc");
        q.setParameter(1, true);

        @SuppressWarnings("unchecked")
        List<LookupList> result = q.getResultList();

        return result;
    }

    @Override
    public LookupList findByName(String name) {
        Query q = entityManager.createQuery("select l from LookupList l where l.name=?1");
        q.setParameter(1, name);

        LookupList ll = this.getSingleResultOrNull(q);

        return ll;
    }
}
