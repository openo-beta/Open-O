//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ConsultDocs;

public interface ConsultDocsDao extends AbstractDao<ConsultDocs> {
    List<ConsultDocs> findByRequestIdDocNoDocType(Integer requestId, Integer documentNo, String docType);

    List<ConsultDocs> findByRequestIdDocType(Integer requestId, String docType);

    List<ConsultDocs> findByRequestId(Integer requestId);

    List<Object[]> findLabs(Integer consultationId);
}
