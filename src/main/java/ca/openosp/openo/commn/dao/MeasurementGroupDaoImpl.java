//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.MeasurementGroup;
import org.springframework.stereotype.Repository;
import ca.openosp.OscarProperties;

@Repository
@SuppressWarnings("unchecked")
public class MeasurementGroupDaoImpl extends AbstractDaoImpl<MeasurementGroup> implements MeasurementGroupDao {

    public MeasurementGroupDaoImpl() {
        super(MeasurementGroup.class);
    }

    @Override
    public List<MeasurementGroup> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @Override
    public List<MeasurementGroup> findByNameAndTypeDisplayName(String name, String typeDisplayName) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.name=?1 AND x.typeDisplayName=?2";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, name);
        query.setParameter(2, typeDisplayName);


        List<MeasurementGroup> results = query.getResultList();

        return (results);
    }

    @Override
    public List<MeasurementGroup> findByTypeDisplayName(String typeDisplayName) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.typeDisplayName=?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, typeDisplayName);


        List<MeasurementGroup> results = query.getResultList();

        return (results);
    }

    @Override
    public List<MeasurementGroup> findByName(String name) {
        boolean orderById = "true".equals(OscarProperties.getInstance().getProperty("oscarMeasurements.orderGroupById", "false"));
        String orderBy = "";
        if (orderById) {
            orderBy = " ORDER BY x.id ASC";
        }
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.name=?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, name);

        List<MeasurementGroup> results = query.getResultList();

        return (results);
    }

    @Override
    public List<Object> findUniqueTypeDisplayNamesByGroupName(String groupName) {
        String sql = "SELECT DISTINCT mg.typeDisplayName FROM MeasurementGroup mg WHERE mg.name = ?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, groupName);
        return query.getResultList();
    }
}
