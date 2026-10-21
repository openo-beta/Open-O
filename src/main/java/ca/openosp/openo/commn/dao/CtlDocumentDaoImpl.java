//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CtlDocument;
import org.springframework.stereotype.Repository;

@Repository
public class CtlDocumentDaoImpl extends AbstractDaoImpl<CtlDocument> implements CtlDocumentDao {

    public CtlDocumentDaoImpl() {
        super(CtlDocument.class);
    }

    @Override
    public CtlDocument getCtrlDocument(Integer docId) {
        Query query = entityManager.createQuery("select x from CtlDocument x where x.id.documentNo=?1");
        query.setParameter(1, docId);

        return (getSingleResultOrNull(query));
    }

    @Override
    public List<CtlDocument> findByDocumentNoAndModule(Integer ctlDocNo, String module) {
        Query query = entityManager
                .createQuery("select x from CtlDocument x where x.id.documentNo=?1 and x.id.module = ?2");
        query.setParameter(1, ctlDocNo);
        query.setParameter(2, module);

        @SuppressWarnings("unchecked")
        List<CtlDocument> cList = query.getResultList();
        return cList;
    }

    @Override
    public List<CtlDocument> findByDocumentNos(List<Integer> documentNos) {
        if (documentNos == null || documentNos.isEmpty()) return Collections.emptyList();
        Query query = entityManager.createQuery("select x from CtlDocument x where x.id.documentNo in (?1)");
        query.setParameter(1, documentNos);

        @SuppressWarnings("unchecked")
        List<CtlDocument> cList = query.getResultList();
        return cList;
    }

}
