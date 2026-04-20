//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Security;
import org.springframework.stereotype.Repository;

@Repository
public class SecurityDaoImpl extends AbstractDaoImpl<Security> implements SecurityDao {

    public SecurityDaoImpl() {
        super(Security.class);
    }

    @Override
    public List<Security> findAllOrderBy(String columnName) {
        Query query = entityManager.createQuery("SELECT s FROM Security s ORDER BY " + columnName);
        @SuppressWarnings("unchecked")
        List<Security> secList = query.getResultList();
        return secList;
    }

    @Override
    public List<Security> findByProviderNo(String providerNo) {
        Query query = entityManager.createQuery("select x from Security x where x.providerNo=?1");
        query.setParameter(1, providerNo);
        @SuppressWarnings("unchecked")
        List<Security> secList = query.getResultList();
        return secList;
    }

    @Override
    public List<Security> findByLikeProviderNo(String providerNo) {
        Query query = entityManager.createQuery("select x from Security x where x.providerNo like ?1");
        query.setParameter(1, providerNo);
        @SuppressWarnings("unchecked")
        List<Security> secList = query.getResultList();
        return secList;
    }

    @Override
    public List<Security> findByUserName(String userName) {
        Query query = entityManager.createQuery("select x from Security x where x.userName=?1");
        query.setParameter(1, userName);
        @SuppressWarnings("unchecked")
        List<Security> secList = query.getResultList();
        return secList;
    }

    @Override
    public List<Security> findByOneIdKey(String ssoKey) {
        Query query = entityManager.createQuery("SELECT x FROM Security x WHERE x.oneIdKey = ?1");
        query.setParameter(1, ssoKey);
        @SuppressWarnings("unchecked")
        List<Security> securityList = query.getResultList();
        return securityList;
    }

    @Override
    public void updateOneIdKey(Security securityRecord) {
        merge(securityRecord);
    }

    @Override
    public List<Security> findByLikeUserName(String userName) {
        Query query = entityManager.createQuery("select x from Security x where x.userName like ?1");
        query.setParameter(1, userName);
        @SuppressWarnings("unchecked")
        List<Security> secList = query.getResultList();
        return secList;
    }

    @Override
    public Security getByProviderNo(String providerNo) {
        List<Security> secList = this.findByProviderNo(providerNo);
        if (secList.isEmpty())
            return null;
        return secList.get(0);
    }

    @Override
    public List<Object[]> findProviders() {
        String sql = "FROM Security s, Provider p WHERE p.providerNo = s.providerNo ORDER BY p.lastName";
        Query query = entityManager.createQuery(sql);
        return query.getResultList();
    }

    @Override
    public List<Security> findByProviderSite(String providerNo) {
        String queryStr = "select * from security s inner join providersite p on s.provider_no = p.provider_no " +
                "where p.site_id in(select site_id from providersite where provider_no=?1)";
        Query query = entityManager.createNativeQuery(queryStr, modelClass);
        query.setParameter(1, providerNo);
        @SuppressWarnings("unchecked")
        List<Security> secList = query.getResultList();
        return secList;
    }
}
