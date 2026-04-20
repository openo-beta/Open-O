//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ProfessionalSpecialist;
import ca.openosp.openo.commn.model.ServiceSpecialists;
import org.springframework.stereotype.Repository;

@Repository
public class ServiceSpecialistsDaoImpl extends AbstractDaoImpl<ServiceSpecialists> implements ServiceSpecialistsDao {

    public ServiceSpecialistsDaoImpl() {
        super(ServiceSpecialists.class);
    }

    @Override
    public List<ServiceSpecialists> findByServiceId(int serviceId) {
        Query q = entityManager.createQuery("select x from ServiceSpecialists x where x.id.serviceId = ?1");
        q.setParameter(1, serviceId);

        @SuppressWarnings("unchecked")
        List<ServiceSpecialists> results = q.getResultList();

        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object[]> findSpecialists(Integer servId) {
        String sql = "FROM ServiceSpecialists ser, " + ProfessionalSpecialist.class.getSimpleName() + " pro " +
                "WHERE pro.id = ser.id.specId and ser.id.serviceId = ?1 " +
                "ORDER BY pro.lastName";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, servId);
        return query.getResultList();
    }
}
