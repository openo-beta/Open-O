//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ConsultResponseDoc;

public interface ConsultResponseDocDao extends AbstractDao<ConsultResponseDoc> {
    ConsultResponseDoc findByResponseIdDocNoDocType(Integer responseId, Integer documentNo, String docType);

    List<ConsultResponseDoc> findByResponseId(Integer responseId);

    List<Object[]> findLabs(Integer consultResponseId);
}
