//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.SpecialistsJavascript;
import org.springframework.stereotype.Repository;

@Repository
public class SpecialistsJavascriptDaoImpl extends AbstractDaoImpl<SpecialistsJavascript> implements SpecialistsJavascriptDao {

    public SpecialistsJavascriptDaoImpl() {
        super(SpecialistsJavascript.class);
    }

    @Override
    public List<SpecialistsJavascript> findBySetId(String setId) {
        Query q = entityManager.createQuery("select x from SpecialistsJavascript x where x.setId=?1");
        q.setParameter(1, setId);

        @SuppressWarnings("unchecked")
        List<SpecialistsJavascript> results = q.getResultList();

        return results;
    }
}
