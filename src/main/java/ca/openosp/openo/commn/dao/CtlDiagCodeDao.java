//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlDiagCode;

public interface CtlDiagCodeDao extends AbstractDao<CtlDiagCode> {
    List<Object[]> getDiagnostics(String billRegion, String serviceType);

    List<CtlDiagCode> findByServiceType(String serviceType);
}
