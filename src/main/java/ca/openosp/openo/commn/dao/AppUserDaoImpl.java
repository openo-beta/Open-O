//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.AppUser;
import org.springframework.stereotype.Repository;

@Repository
public class AppUserDaoImpl extends AbstractDaoImpl<AppUser> implements AppUserDao {

    public AppUserDaoImpl() {
        super(AppUser.class);
    }

    @Override
    public AppUser findForProvider(int appId, String providerNo) {
        Query query = entityManager.createQuery("select x from AppUser x where x.appId = ?1 and x.providerNo = ?2");
        query.setParameter(1, appId);
        query.setParameter(2, providerNo);
        List<AppUser> list = query.getResultList();
        if (list == null || list.size() == 0) {
            return null;
        }

        return list.get(0);
    }
}
