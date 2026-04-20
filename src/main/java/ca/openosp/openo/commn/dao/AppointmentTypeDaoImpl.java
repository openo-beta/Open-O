//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import ca.openosp.openo.commn.model.AppointmentType;

@Repository
public class AppointmentTypeDaoImpl extends AbstractDaoImpl<AppointmentType> implements AppointmentTypeDao {

    public AppointmentTypeDaoImpl() {
        super(AppointmentType.class);
    }

    @Override
    public List<AppointmentType> listAll() {
        String sqlCommand = "select x from AppointmentType x order by x.name";
        Query query = entityManager.createQuery(sqlCommand);

        @SuppressWarnings("unchecked")
        List<AppointmentType> results = query.getResultList();

        return (results);

    }

    @Override
    public AppointmentType findByAppointmentTypeByName(String name) {
        Query query = entityManager.createQuery("from AppointmentType atype where atype.name = ?1").setParameter(1, name);
        return this.getSingleResultOrNull(query);
    }

}
 
