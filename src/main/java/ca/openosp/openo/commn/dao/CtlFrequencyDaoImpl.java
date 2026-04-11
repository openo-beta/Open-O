//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CtlFrequency;
import org.springframework.stereotype.Repository;

@Repository
public class CtlFrequencyDaoImpl extends AbstractDaoImpl<CtlFrequency> implements CtlFrequencyDao {

    public CtlFrequencyDaoImpl() {
        super(CtlFrequency.class);
    }

    @SuppressWarnings("unchecked")
    public List<CtlFrequency> findAll() {
        Query query = entityManager.createQuery("SELECT x FROM " + modelClass.getSimpleName() + " x");
        List<CtlFrequency> results = query.getResultList();
        return results;
    }
}
