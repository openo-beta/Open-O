//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CtlRelationships;
import org.springframework.stereotype.Repository;

@Repository
public class CtlRelationshipsDaoImpl extends AbstractDaoImpl<CtlRelationships> implements CtlRelationshipsDao {

    public CtlRelationshipsDaoImpl() {
        super(CtlRelationships.class);
    }

    public List<CtlRelationships> findAllActive() {
        Query query = entityManager.createQuery("select x from " + this.modelClass.getName() + " x where x.active=true");
        @SuppressWarnings("unchecked")
        List<CtlRelationships> results = query.getResultList();

        return results;
    }

    public CtlRelationships findByValue(String value) {
        Query query = entityManager.createQuery("select x from " + this.modelClass.getName() + " x where x.value=?1 and x.active=true");
        query.setParameter(1, value);

        return this.getSingleResultOrNull(query);
    }
}
