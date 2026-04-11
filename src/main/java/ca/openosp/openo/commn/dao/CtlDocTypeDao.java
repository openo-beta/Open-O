//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlDocType;

public interface CtlDocTypeDao extends AbstractDao<CtlDocType> {
    void changeDocType(String docType, String module, String status);

    List<CtlDocType> findByStatusAndModule(String[] status, String module);

    List<CtlDocType> findByStatusAndModule(List<String> status, String module);

    List<CtlDocType> findByDocTypeAndModule(String docType, String module);

    void addDocType(String docType, String module);

    List<String> findModules();
}
