//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlDocClass;

public interface CtlDocClassDao extends AbstractDao<CtlDocClass> {
    List<String> findUniqueReportClasses();

    List<String> findSubClassesByReportClass(String reportClass);
}
