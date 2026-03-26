//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.DocumentExtraReviewer;

public interface DocumentExtraReviewerDao extends AbstractDao<DocumentExtraReviewer> {
    List<DocumentExtraReviewer> findByDocumentNo(Integer documentNo);
}
