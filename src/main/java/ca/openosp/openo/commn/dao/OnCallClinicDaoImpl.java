//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.OnCallClinic;
import org.springframework.stereotype.Repository;

@Repository
public class OnCallClinicDaoImpl extends AbstractDaoImpl<OnCallClinic> implements OnCallClinicDao {

    public OnCallClinicDaoImpl() {
        super(OnCallClinic.class);
    }

    @Override
    public OnCallClinic findByDate(Date date) {
        Query query = entityManager.createQuery("select x from OnCallClinic x where x.startDate = ?1");
        query.setParameter(1, date);

        return this.getSingleResultOrNull(query);
    }

}
