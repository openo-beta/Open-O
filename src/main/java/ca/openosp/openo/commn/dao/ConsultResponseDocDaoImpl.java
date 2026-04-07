//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ConsultResponseDoc;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class ConsultResponseDocDaoImpl extends AbstractDaoImpl<ConsultResponseDoc> implements ConsultResponseDocDao {
    public ConsultResponseDocDaoImpl() {
        super(ConsultResponseDoc.class);
    }

    public ConsultResponseDoc findByResponseIdDocNoDocType(Integer responseId, Integer documentNo, String docType) {
        String sql = "select x from ConsultResponseDoc x where x.responseId=?1 and x.documentNo=?2 and x.docType=?3 and x.deleted IS NULL";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, responseId);
        query.setParameter(2, documentNo);
        query.setParameter(3, docType);

        List<ConsultResponseDoc> results = query.getResultList();
        if (results != null && results.size() > 0) return results.get(0);
        else return null;
    }

    public List<ConsultResponseDoc> findByResponseId(Integer responseId) {
        String sql = "select x from ConsultResponseDoc x where x.responseId=?1 and x.deleted IS NULL";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, responseId);

        List<ConsultResponseDoc> results = query.getResultList();
        return results;
    }

    public List<Object[]> findLabs(Integer consultResponseId) {
        String sql = "FROM ConsultResponseDoc crd, PatientLabRouting plr " +
                "WHERE plr.labNo = crd.documentNo " +
                "AND crd.responseId = ?1" +
                "AND crd.docType = ?2" +
                "AND crd.deleted IS NULL " +
                "ORDER BY crd.documentNo";
        Query q = entityManager.createQuery(sql);
        q.setParameter(1, consultResponseId);
        q.setParameter(2, ConsultResponseDoc.DOCTYPE_LAB);
        return q.getResultList();
    }
}
