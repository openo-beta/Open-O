//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.MeasurementCSSLocation;
import org.springframework.stereotype.Repository;

@Repository
public class MeasurementCSSLocationDaoImpl extends AbstractDaoImpl<MeasurementCSSLocation> implements MeasurementCSSLocationDao {

    public MeasurementCSSLocationDaoImpl() {
        super(MeasurementCSSLocation.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<MeasurementCSSLocation> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @Override
    public List<MeasurementCSSLocation> findByLocation(String location) {
        Query q = entityManager.createQuery("select m from MeasurementCSSLocation m where m.location=?1");
        q.setParameter(1, location);

        @SuppressWarnings("unchecked")
        List<MeasurementCSSLocation> results = q.getResultList();

        return results;
    }
}
