//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.SecurityToken;
import org.springframework.stereotype.Repository;

@Repository
public class SecurityTokenDaoImpl extends AbstractDaoImpl<SecurityToken> implements SecurityTokenDao {

    public SecurityTokenDaoImpl() {
        super(SecurityToken.class);
    }

    @Override
    public SecurityToken getByTokenAndExpiry(String token, Date expiry) {
        Query query = entityManager.createQuery("select t from SecurityToken t where t.token=?1 and t.expiry >= ?2 ");
        query.setParameter(1, token);
        query.setParameter(2, expiry);

        @SuppressWarnings("unchecked")
        List<SecurityToken> results = query.getResultList();

        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }
}
