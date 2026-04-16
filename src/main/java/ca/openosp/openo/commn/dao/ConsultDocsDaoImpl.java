//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ConsultDocs;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class ConsultDocsDaoImpl extends AbstractDaoImpl<ConsultDocs> implements ConsultDocsDao {

    public ConsultDocsDaoImpl() {
        super(ConsultDocs.class);
    }

    public List<ConsultDocs> findByRequestIdDocNoDocType(Integer requestId, Integer documentNo, String docType) {
        String sql = "select x from ConsultDocs x where x.requestId=?1 and x.documentNo=?2 and x.docType=?3 and x.deleted is NULL";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, requestId);
        query.setParameter(2, documentNo);
        query.setParameter(3, docType);

        List<ConsultDocs> results = query.getResultList();
        return results;
    }

    public List<ConsultDocs> findByRequestIdDocType(Integer requestId, String docType) {
        String sql = "select x from ConsultDocs x where x.requestId=?1 and x.docType=?2 and x.deleted is NULL";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, requestId);
        query.setParameter(2, docType);

        List<ConsultDocs> results = query.getResultList();
        if (results == null) {
            return Collections.emptyList();
        }
        return results;
    }

    public List<ConsultDocs> findByRequestId(Integer requestId) {
        String sql = "select x from ConsultDocs x where x.requestId=?1 and x.deleted is NULL";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, requestId);

        List<ConsultDocs> results = query.getResultList();
        return results;
    }

    public List<Object[]> findLabs(Integer consultationId) {
        String sql = "FROM ConsultDocs cd, PatientLabRouting plr " +
                "WHERE plr.labNo = cd.documentNo " +
                "AND cd.requestId = :consultationId " +
                "AND cd.docType = :docType " +
                "AND cd.deleted IS NULL " +
                "ORDER BY cd.documentNo";
        Query q = entityManager.createQuery(sql);
        q.setParameter("consultationId", consultationId);
        q.setParameter("docType", ConsultDocs.DOCTYPE_LAB);
        return q.getResultList();
    }
}
