//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.QuickListUser;
import org.springframework.stereotype.Repository;

@Repository
public class QuickListUserDaoImpl extends AbstractDaoImpl<QuickListUser> implements QuickListUserDao {

    public QuickListUserDaoImpl() {
        super(QuickListUser.class);
    }

    @Override
    public List<QuickListUser> findByNameAndProviderNo(String name, String providerNo) {
        Query q = entityManager.createQuery("SELECT x FROM QuickListUser x WHERE x.quickListName=?1 AND x.providerNo=?2");
        q.setParameter(1, name);
        q.setParameter(2, providerNo);

        @SuppressWarnings("unchecked")
        List<QuickListUser> results = q.getResultList();

        return results;
    }
}
