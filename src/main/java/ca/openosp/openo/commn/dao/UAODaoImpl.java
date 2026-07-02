//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.UAO;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository
public class UAODaoImpl extends AbstractDaoImpl<UAO> implements UAODao {

    public UAODaoImpl() {
        super(UAO.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UAO> findByProvider(String providerNo) {
        Query query = entityManager.createQuery(
                "select u from UAO u where u.providerNo = :providerNo and u.active = true order by u.defaultUAO desc");
        query.setParameter("providerNo", providerNo);
        return (List<UAO>) query.getResultList();
    }

    @Override
    public void setAsDefault(UAO uao, String providerNo) {
        for (UAO current : findByProvider(providerNo)) {
            boolean isDefault = Boolean.TRUE.equals(current.getDefaultUAO());
            if (current.getId().equals(uao.getId())) {
                if (!isDefault) {
                    current.setDefaultUAO(true);
                    merge(current);
                }
            } else if (isDefault) {
                current.setDefaultUAO(false);
                merge(current);
            }
        }
    }
}
