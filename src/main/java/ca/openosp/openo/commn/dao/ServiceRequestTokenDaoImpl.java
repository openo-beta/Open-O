//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ServiceRequestToken;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ServiceRequestTokenDaoImpl extends AbstractDaoImpl<ServiceRequestToken> implements ServiceRequestTokenDao {

    public ServiceRequestTokenDaoImpl() {
        super(ServiceRequestToken.class);
    }

    @Override
    public List<ServiceRequestToken> findAll() {
        Query query = this.entityManager.createQuery("SELECT x FROM ServiceRequestToken x", ServiceRequestToken.class);
        return query.getResultList();
    }

    @Override
    public ServiceRequestToken findByTokenId(String token) {
        Query query = this.entityManager.createQuery("SELECT x FROM ServiceRequestToken x WHERE x.tokenId = ?1", ServiceRequestToken.class);
        query.setParameter(1, token);
        return this.getSingleResultOrNull(query);
    }
}