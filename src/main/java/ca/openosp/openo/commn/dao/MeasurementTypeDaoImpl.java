//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.MeasurementType;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class MeasurementTypeDaoImpl extends AbstractDaoImpl<MeasurementType> implements MeasurementTypeDao {

    public MeasurementTypeDaoImpl() {
        super(MeasurementType.class);
    }

    @Override
    public List<MeasurementType> findAll() {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x order by x.type";
        Query query = entityManager.createQuery(sqlCommand);
        List<MeasurementType> results = query.getResultList();
        return (results);
    }

    @Override
    public List<MeasurementType> findAllOrderByName() {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x order by x.typeDisplayName";
        Query query = entityManager.createQuery(sqlCommand);
        List<MeasurementType> results = query.getResultList();
        return (results);
    }

    @Override
    public List<MeasurementType> findAllOrderById() {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x order by x.id";
        Query query = entityManager.createQuery(sqlCommand);
        List<MeasurementType> results = query.getResultList();
        return (results);
    }

    @Override
    public List<MeasurementType> findByType(String type) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.type=?1";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, type);
        List<MeasurementType> results = query.getResultList();
        return (results);
    }

    @Override
    public List<MeasurementType> findByMeasuringInstructionAndTypeDisplayName(String measuringInstruction, String typeDisplayName) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.measuringInstruction=?1 AND x.typeDisplayName=?2";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, measuringInstruction);
        query.setParameter(2, typeDisplayName);
        List<MeasurementType> results = query.getResultList();
        return (results);
    }

    @Override
    public List<MeasurementType> findByTypeDisplayName(String typeDisplayName) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.typeDisplayName=?1";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, typeDisplayName);
        List<MeasurementType> results = query.getResultList();
        return (results);
    }

    @Override
    public List<MeasurementType> findByTypeAndMeasuringInstruction(String type, String measuringInstruction) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.type=?1 AND x.measuringInstruction=?2 ";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, type);
        query.setParameter(2, measuringInstruction);
        List<MeasurementType> results = query.getResultList();
        return (results);
    }

    @Override
    public List<Object> findUniqueTypeDisplayNames() {
        String sql = "SELECT DISTINCT m.typeDisplayName FROM MeasurementType m order by m.typeDisplayName";
        Query query = entityManager.createQuery(sql);
        return query.getResultList();
    }
}
