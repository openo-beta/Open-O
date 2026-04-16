//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.MeasurementsDeleted;
import org.springframework.stereotype.Repository;

@Repository
public class MeasurementsDeletedDaoImpl extends AbstractDaoImpl<MeasurementsDeleted> implements MeasurementsDeletedDao {

    public MeasurementsDeletedDaoImpl() {
        super(MeasurementsDeleted.class);
    }

    @Override
    public List<MeasurementsDeleted> findDeletedAfterDatetime(Date dateTime) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.dateDeleted>?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, dateTime);

        @SuppressWarnings("unchecked")
        List<MeasurementsDeleted> results = query.getResultList();

        return (results);
    }

}
