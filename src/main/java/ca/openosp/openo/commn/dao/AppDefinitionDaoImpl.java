//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.AppDefinition;
import org.springframework.stereotype.Repository;

@Repository
public class AppDefinitionDaoImpl extends AbstractDaoImpl<AppDefinition> implements AppDefinitionDao {

    public AppDefinitionDaoImpl() {
        super(AppDefinition.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AppDefinition> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public AppDefinition findByName(String name) {
        Query query = entityManager.createQuery("select x from AppDefinition x where x.name = ?1");
        query.setParameter(1, name);

        List<AppDefinition> list = query.getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public AppDefinition findByConsumerKey(String consumerKey) {
        Query query = entityManager.createQuery("select x from AppDefinition x where x.config like ?1");
        query.setParameter(1, "%<consumerKey>" + consumerKey + "</consumerKey>%");

        List<AppDefinition> list = query.getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
}
