//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Encounter;
import org.springframework.stereotype.Repository;

@Repository
public class EncounterDaoImpl extends AbstractDaoImpl<Encounter> implements EncounterDao {

    public EncounterDaoImpl() {
        super(Encounter.class);
    }

    public List<Encounter> findByDemographicNo(Integer demographicNo) {
        Query q = entityManager.createQuery("select e from Encounter e where e.demographicNo = ?1 order by e.encounterDate desc, e.encounterTime desc");
        q.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<Encounter> results = q.getResultList();

        return results;
    }
}
