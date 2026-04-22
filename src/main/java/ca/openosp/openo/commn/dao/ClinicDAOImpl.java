//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.Clinic;
import org.springframework.stereotype.Repository;

/**
 * @author Jason Gallagher
 */
@Repository
public class ClinicDAOImpl extends AbstractDaoImpl<Clinic> implements ClinicDAO {

    public ClinicDAOImpl() {
        super(Clinic.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Clinic> findAll() {
        Query query = entityManager.createQuery("SELECT x FROM " + modelClass.getSimpleName() + " x");
        List<Clinic> results = query.getResultList();
        return results;
    }

    @Override
    public Clinic getClinic() {
        Query query = entityManager.createQuery("select c from Clinic c");
        @SuppressWarnings("unchecked")
        List<Clinic> codeList = query.getResultList();
        if (codeList.size() > 0) {
            return codeList.get(0);
        }
        return null;
    }

    @Override
    public void save(Clinic clinic) {
        if (clinic.getId() != null && clinic.getId().intValue() > 0) {
            merge(clinic);
        } else {
            persist(clinic);
        }
    }

}
