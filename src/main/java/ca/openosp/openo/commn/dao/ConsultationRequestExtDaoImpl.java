//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ConsultationRequestExt;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultationRequestExtDaoImpl extends AbstractDaoImpl<ConsultationRequestExt> implements ConsultationRequestExtDao {

    public ConsultationRequestExtDaoImpl() {
        super(ConsultationRequestExt.class);
    }

    public List<ConsultationRequestExt> getConsultationRequestExts(int requestId) {
        Query query = entityManager.createQuery("select cre from ConsultationRequestExt cre where cre.requestId=?1");
        query.setParameter(1, requestId);
        return query.getResultList();
    }

    public String getConsultationRequestExtsByKey(int requestId, String key) {
        Query query = entityManager.createQuery("select cre.value from ConsultationRequestExt cre where cre.requestId=?1 and cre.key=?2");
        query.setParameter(1, requestId);
        query.setParameter(2, key);
        List<String> results = query.getResultList();
        if (results.size() > 0)
            return results.get(0);
        return null;
    }

    public void clear(int requestId) {
        Query query = entityManager.createQuery("delete from ConsultationRequestExt cre where cre.requestId = ?1");
        query.setParameter(1, requestId);
        query.executeUpdate();
    }
}
