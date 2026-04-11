//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.Desaprisk;
import org.springframework.stereotype.Repository;

@Repository
public class DesapriskDaoImpl extends AbstractDaoImpl<Desaprisk> implements DesapriskDao {

    public DesapriskDaoImpl() {
        super(Desaprisk.class);
    }

    @Override
    public Desaprisk search(Integer formNo, Integer demographicNo) {

        String sqlCommand = "select x from Desaprisk x where x.formNo <= ?1 and x.demographicNo=?2 order by x.formNo DESC, x.desapriskDate DESC, x.desapriskTime DESC";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, formNo);
        query.setParameter(2, demographicNo);

        @SuppressWarnings("unchecked")
        List<Desaprisk> results = query.getResultList();

        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }
}
