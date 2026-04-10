//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.EFormDocs;

public interface EFormDocsDao extends AbstractDao<EFormDocs> {
    List<EFormDocs> findByFdidIdDocNoDocType(Integer fdid, Integer documentNo, String docType);

    List<EFormDocs> findByFdidIdDocType(Integer fdid, String docType);

    List<EFormDocs> findByFdid(Integer fdid);

    List<Object[]> findLabs(Integer fdid);
}
