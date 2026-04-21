package ca.openosp.openo.documentManager.dao;

import ca.openosp.openo.commn.dao.DocumentDao;
import ca.openosp.openo.commn.model.ConsultDocs;
import ca.openosp.openo.commn.model.CtlDocument;
import ca.openosp.openo.commn.model.CtlDocumentPK;
import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.test.base.OpenOTestBase;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;

/**
 * Base class for DocumentDao integration tests.
 *
 * Provides helpers to persist Document, CtlDocument, and ConsultDocs rows in
 * the combinations needed to exercise attached-document queries (especially
 * the multi-ctl-row case that triggers the first-tuple-wins classification
 * ambiguity in EDocUtil.listDocs).
 */
@Tag("integration")
@Tag("database")
@Tag("slow")
@Tag("dao")
@Tag("document")
@Transactional
public abstract class DocumentDaoBaseIntegrationTest extends OpenOTestBase {

    @Autowired
    protected DocumentDao documentDao;

    @PersistenceContext(unitName = "entityManagerFactory")
    protected EntityManager entityManager;

    /** Persist a Document with sensible defaults. Returns the generated documentNo. */
    protected Integer persistDocument(String description, int public1, char status) {
        Document d = new Document();
        d.setDocdesc(description);
        d.setDocfilename(description.replace(' ', '_') + ".pdf");
        d.setDoccreator("999998");
        d.setResponsible("999998");
        d.setStatus(status);
        d.setContenttype("application/pdf");
        d.setPublic1(public1);
        d.setObservationdate(new Date());
        d.setUpdatedatetime(new Date());
        entityManager.persist(d);
        entityManager.flush();
        return d.getDocumentNo();
    }

    /** Create a ctl_document row binding documentNo to (module, moduleId). */
    protected void persistCtlDocument(Integer documentNo, String module, Integer moduleId) {
        CtlDocument ctl = new CtlDocument();
        CtlDocumentPK pk = new CtlDocumentPK(module, moduleId, documentNo);
        ctl.setId(pk);
        ctl.setStatus("A");
        entityManager.persist(ctl);
        entityManager.flush();
    }

    /** Attach a document to a consult request. */
    protected void persistConsultDocAttachment(Integer requestId, Integer documentNo) {
        ConsultDocs cd = new ConsultDocs(requestId, documentNo, ConsultDocs.DOCTYPE_DOC, "999998");
        cd.setAttachDate(new Date());
        entityManager.persist(cd);
        entityManager.flush();
    }
}
