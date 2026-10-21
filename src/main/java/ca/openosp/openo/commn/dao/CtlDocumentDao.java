//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlDocument;

public interface CtlDocumentDao extends AbstractDao<CtlDocument> {

    public CtlDocument getCtrlDocument(Integer docId);

    public List<CtlDocument> findByDocumentNoAndModule(Integer ctlDocNo, String module);

    /**
     * Batch lookup of ctl_document rows for a set of document IDs. Returns all
     * bindings for each doc (including multiple ctl rows per doc when present).
     * Used by attached-doc enrichment to classify many attachments in one query.
     *
     * @param documentNos List&lt;Integer&gt; document IDs to fetch ctl rows for
     * @return List&lt;CtlDocument&gt; all ctl rows matching any of the supplied doc IDs;
     *         empty list if {@code documentNos} is null or empty
     */
    public List<CtlDocument> findByDocumentNos(List<Integer> documentNos);

}
