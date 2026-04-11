//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ServiceAccessToken;
import org.springframework.stereotype.Repository;

@Repository
public class ServiceAccessTokenDaoImpl extends AbstractDaoImpl<ServiceAccessToken> implements ServiceAccessTokenDao {


    public ServiceAccessTokenDaoImpl() {
        super(ServiceAccessToken.class);
    }

    @Override
    public void persist(ServiceAccessToken token) {
        this.entityManager.persist(token);
    }

    @Override
    public void remove(ServiceAccessToken token) {
        this.entityManager.remove(token);
    }

    // @Override
// public ServiceRequestToken merge(ServiceAccessTokenDao token) {
//     return this.entityManager.merge(token);
// }
    @Override
    @SuppressWarnings("unchecked")
    public List<ServiceAccessToken> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @Override
    public ServiceAccessToken findByTokenId(String token) {
        Query query = entityManager.createQuery("SELECT x FROM ServiceAccessToken x WHERE x.tokenId=?1");
        query.setParameter(1, token);

        return this.getSingleResultOrNull(query);
    }

}
