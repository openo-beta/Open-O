//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ReportTemplates;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class ReportTemplatesDaoImpl extends AbstractDaoImpl<ReportTemplates> implements ReportTemplatesDao {

    public ReportTemplatesDaoImpl() {
        super(ReportTemplates.class);
    }

    @Override
    public List<ReportTemplates> findAll() {
        Query q = createQuery("t", null);
        return q.getResultList();
    }

    @Override
    public List<ReportTemplates> findActive() {
        Query q = createQuery("t", "t.active = 1");
        return q.getResultList();
    }

    @Override
    public ReportTemplates findByUuid(String uuid) {
        Query query = entityManager.createQuery("SELECT r from ReportTemplates r where r.uuid = ?1 and r.active = 1");
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<ReportTemplates> results = query.getResultList();
        if (!results.isEmpty())
            return results.get(0);
        return null;
    }
}
