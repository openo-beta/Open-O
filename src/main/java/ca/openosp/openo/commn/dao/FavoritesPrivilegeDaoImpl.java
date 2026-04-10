//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.FavoritesPrivilege;
import org.springframework.stereotype.Repository;

@Repository
public class FavoritesPrivilegeDaoImpl extends AbstractDaoImpl<FavoritesPrivilege> implements FavoritesPrivilegeDao {

    public FavoritesPrivilegeDaoImpl() {
        super(FavoritesPrivilege.class);
    }

    public List<String> getProviders() {
        Query query = entityManager.createQuery("select x.providerNo from FavoritesPrivilege x where x.openToPublic=?1");
        query.setParameter(1, true);

        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();

        return results;
    }

    public FavoritesPrivilege findByProviderNo(String providerNo) {
        Query query = entityManager.createQuery("select x from FavoritesPrivilege x where x.providerNo=?1");
        query.setParameter(1, providerNo);

        FavoritesPrivilege result = this.getSingleResultOrNull(query);

        return result;
    }

    public void setFavoritesPrivilege(String providerNo, boolean openpublic, boolean writeable) {

        FavoritesPrivilege fp = findByProviderNo(providerNo);
        if (fp != null) {
            fp.setOpenToPublic(openpublic);
            fp.setWriteable(writeable);
        } else {
            fp = new FavoritesPrivilege();
            fp.setProviderNo(providerNo);
            fp.setOpenToPublic(openpublic);
            fp.setWriteable(writeable);
            persist(fp);
        }
    }
}
