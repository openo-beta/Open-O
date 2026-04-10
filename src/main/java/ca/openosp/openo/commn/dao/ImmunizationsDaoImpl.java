//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Immunizations;
import org.springframework.stereotype.Repository;

@Repository
public class ImmunizationsDaoImpl extends AbstractDaoImpl<Immunizations> implements ImmunizationsDao {

    public ImmunizationsDaoImpl() {
        super(Immunizations.class);
    }

    public List<Immunizations> findCurrentByDemographicNo(Integer demographicNo) {
        Query q = entityManager.createQuery("SELECT i FROM Immunizations i WHERE i.demographicNo=?1 AND i.archived=0");
        q.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<Immunizations> results = q.getResultList();

        return results;
    }
}
