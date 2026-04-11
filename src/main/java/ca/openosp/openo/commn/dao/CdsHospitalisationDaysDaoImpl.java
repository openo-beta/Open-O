//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CdsHospitalisationDays;
import org.springframework.stereotype.Repository;

@Repository
public class CdsHospitalisationDaysDaoImpl extends AbstractDaoImpl<CdsHospitalisationDays> implements CdsHospitalisationDaysDao {

    public CdsHospitalisationDaysDaoImpl() {
        super(CdsHospitalisationDays.class);
    }

    public List<CdsHospitalisationDays> findByClientId(Integer clientId) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.clientId=?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, clientId);

        @SuppressWarnings("unchecked")
        List<CdsHospitalisationDays> results = query.getResultList();

        return (results);
    }
}
