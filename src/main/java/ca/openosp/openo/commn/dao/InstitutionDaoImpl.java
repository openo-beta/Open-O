//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Institution;
import org.springframework.stereotype.Repository;

@Repository
public class InstitutionDaoImpl extends AbstractDaoImpl<Institution> implements InstitutionDao {

    public InstitutionDaoImpl() {
        super(Institution.class);
    }

    @SuppressWarnings("unchecked")
    public List<Institution> findAll() {
        
        Query query = createQuery("x", null);
        return query.getResultList();
    }
}
