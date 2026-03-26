//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.MyGroupAccessRestriction;
import org.springframework.stereotype.Repository;

@Repository
public class MyGroupAccessRestrictionDaoImpl extends AbstractDaoImpl<MyGroupAccessRestriction> implements MyGroupAccessRestrictionDao {

    public MyGroupAccessRestrictionDaoImpl() {
        super(MyGroupAccessRestriction.class);
    }

    @Override
    public List<MyGroupAccessRestriction> findByGroupId(String myGroupNo) {
        Query query = entityManager.createQuery("select x from MyGroupAccessRestriction x where x.myGroupNo=?1");
        query.setParameter(1, myGroupNo);

        @SuppressWarnings("unchecked")
        List<MyGroupAccessRestriction> results = query.getResultList();

        return results;
    }

    @Override
    public List<MyGroupAccessRestriction> findByProviderNo(String providerNo) {
        Query query = entityManager.createQuery("select x from MyGroupAccessRestriction x where x.providerNo=?1");
        query.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<MyGroupAccessRestriction> results = query.getResultList();

        return results;
    }

    @Override
    public MyGroupAccessRestriction findByGroupNoAndProvider(String myGroupNo, String providerNo) {
        Query query = entityManager.createQuery("select x from MyGroupAccessRestriction x where x.myGroupNo=?1 and x.providerNo=?2");
        query.setParameter(1, myGroupNo);
        query.setParameter(2, providerNo);

        @SuppressWarnings("unchecked")
        List<MyGroupAccessRestriction> results = query.getResultList();

        if (results.size() > 0) {
            return results.get(0);
        }

        return null;
    }
}
