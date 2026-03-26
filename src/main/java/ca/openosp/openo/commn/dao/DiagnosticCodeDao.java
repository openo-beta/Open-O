//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.AbstractCodeSystemModel;
import ca.openosp.openo.commn.model.DiagnosticCode;

public interface DiagnosticCodeDao extends AbstractCodeSystemDao<DiagnosticCode> {
    List<DiagnosticCode> findByDiagnosticCode(String diagnosticCode);

    List<DiagnosticCode> findByDiagnosticCodeAndRegion(String diagnosticCode, String region);

    List<DiagnosticCode> search(String searchString);

    List<DiagnosticCode> newSearch(String a, String b, String c, String d, String e, String f);

    List<DiagnosticCode> searchCode(String code);

    List<DiagnosticCode> searchText(String description);

    List<DiagnosticCode> getByDxCode(String dxCode);

    List<DiagnosticCode> findByRegionAndType(String billRegion, String serviceType);

    List<Object[]> findDiagnosictsAndCtlDiagCodesByServiceType(String serviceType);

    DiagnosticCode findByCode(String code);

    AbstractCodeSystemModel<?> findByCodingSystem(String codingSystem);
}
