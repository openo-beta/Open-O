//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ConsentType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collections;

@Repository
public class ConsentTypeDaoImpl extends AbstractDaoImpl<ConsentType> implements ConsentTypeDao {

    protected ConsentTypeDaoImpl() {
        super(ConsentType.class);
    }

    @Override
    public ConsentType findConsentType(String type) {
        String sql = "select x from " + modelClass.getSimpleName() + " x where x.type=?1 and x.active=1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, type);

        ConsentType consentType = getSingleResultOrNull(query);

        return consentType;
    }

    @Override
    public ConsentType findConsentTypeForProvider(String type, String providerNo) {
        String sql = "select x from " + modelClass.getSimpleName()
                + " x where x.type=?1 and x.active=1 and x.providerNo = ?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, type);
        query.setParameter(2, providerNo);
        ConsentType consentType = getSingleResultOrNull(query);

        return consentType;
    }

    @Override
    public List<ConsentType> findAllActive() {
        Query q = entityManager.createQuery("select ct from ConsentType ct where ct.active=?1  order by ct.type asc");
        q.setParameter(1, true);

        @SuppressWarnings("unchecked")
        List<ConsentType> result = q.getResultList();

        if (result == null) {
            result = Collections.emptyList();
        }

        return result;
    }
}
