//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Facility;
import org.springframework.stereotype.Repository;

@Repository
public class FacilityDaoImpl extends AbstractDaoImpl<Facility> implements FacilityDao {

    public FacilityDaoImpl() {
        super(Facility.class);
    }

    /**
     * Find all ordered by name.
     *
     * @param active null is find all, true is find only active, false is find only inactive.
     */
    public List<Facility> findAll(Boolean active) {
        StringBuilder sb = new StringBuilder();
        sb.append("select x from Facility x");
        if (active != null) sb.append(" where x.disabled=?1");
        sb.append(" order by x.name");

        Query query = entityManager.createQuery(sb.toString());
        if (active != null) query.setParameter(1, !active);

        @SuppressWarnings("unchecked")
        List<Facility> results = query.getResultList();

        return (results);
    }

}
