//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.Flowsheet;
import org.springframework.stereotype.Repository;

@Repository
public class FlowsheetDaoImpl extends AbstractDaoImpl<Flowsheet> implements FlowsheetDao {

    public FlowsheetDaoImpl() {
        super(Flowsheet.class);
    }

    @Override
    public List<Flowsheet> findAll() {
        Query query = entityManager.createQuery("select f from Flowsheet f");

        @SuppressWarnings("unchecked")
        List<Flowsheet> results = query.getResultList();

        return results;
    }

    @Override
    public Flowsheet findByName(String name) {
        Query query = entityManager.createQuery("select f from Flowsheet f where f.name=?1");
        query.setParameter(1, name);

        return getSingleResultOrNull(query);
    }
}
