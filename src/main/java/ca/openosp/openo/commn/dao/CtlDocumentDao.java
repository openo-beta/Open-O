//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlDocument;

public interface CtlDocumentDao extends AbstractDao<CtlDocument> {

    public CtlDocument getCtrlDocument(Integer docId);

    public List<CtlDocument> findByDocumentNoAndModule(Integer ctlDocNo, String module);

}
