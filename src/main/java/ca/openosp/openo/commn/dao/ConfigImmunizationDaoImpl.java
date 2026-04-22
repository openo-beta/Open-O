//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ConfigImmunization;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigImmunizationDaoImpl extends AbstractDaoImpl<ConfigImmunization> implements ConfigImmunizationDao {

    public ConfigImmunizationDaoImpl() {
        super(ConfigImmunization.class);
    }

    public List<ConfigImmunization> findAll() {
        String sql = "select x from ConfigImmunization x where x.archived=0 AND x.name IS NOT NULL and x.name != '' ORDER BY x.name";
        Query query = entityManager.createQuery(sql);

        @SuppressWarnings("unchecked")
        List<ConfigImmunization> results = query.getResultList();
        return results;
    }

    public List<ConfigImmunization> findByArchived(Integer archived, boolean orderByName) {
        String orderBy = "";
        if (orderByName) {
            orderBy = " ORDER BY x.name";
        }
        String sql = "select x from ConfigImmunization x where x.archived=?1" + orderBy;
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, archived);

        @SuppressWarnings("unchecked")
        List<ConfigImmunization> results = query.getResultList();
        return results;
    }
}
