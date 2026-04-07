//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.EFormDocs;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class EFormDocsDaoImpl extends AbstractDaoImpl<EFormDocs> implements EFormDocsDao {

    public EFormDocsDaoImpl() {
        super(EFormDocs.class);
    }

    public List<EFormDocs> findByFdidIdDocNoDocType(Integer fdid, Integer documentNo, String docType) {
        String sql = "select x from EFormDocs x where x.fdid=?1 and x.documentNo=?2 and x.docType=?3 and x.deleted is NULL";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, fdid);
        query.setParameter(2, documentNo);
        query.setParameter(3, docType);

        List<EFormDocs> results = query.getResultList();
        return results;
    }

    public List<EFormDocs> findByFdidIdDocType(Integer fdid, String docType) {
        String sql = "select x from EFormDocs x where x.fdid=?1 and x.docType=?2 and x.deleted is NULL";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, fdid);
        query.setParameter(2, docType);

        return query.getResultList();
    }

    public List<EFormDocs> findByFdid(Integer fdid) {
        String sql = "select x from EFormDocs x where x.fdid=?1 and x.deleted is NULL";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, fdid);

        List<EFormDocs> results = query.getResultList();
        return results;
    }

    public List<Object[]> findLabs(Integer fdid) {
        String sql = "FROM EFormDocs cd, PatientLabRouting plr " +
                "WHERE plr.labNo = cd.documentNo " +
                "AND cd.fdid = ?1" +
                "AND cd.docType = ?2" +
                "AND cd.deleted IS NULL " +
                "ORDER BY cd.documentNo";
        Query q = entityManager.createQuery(sql);
        q.setParameter(1, fdid);
        q.setParameter(2, EFormDocs.DOCTYPE_LAB);
        return q.getResultList();
    }
}
