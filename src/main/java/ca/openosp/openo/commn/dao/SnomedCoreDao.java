//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.AbstractCodeSystemModel;
import ca.openosp.openo.commn.model.SnomedCore;

public interface SnomedCoreDao extends AbstractCodeSystemDao<SnomedCore> {
    List<SnomedCore> getSnomedCoreCode(String snomedCoreCode);

    List<SnomedCore> getSnomedCore(String query);

    List<SnomedCore> searchCode(String term);

    SnomedCore findByCode(String code);

    AbstractCodeSystemModel<?> findByCodingSystem(String codingSystem);
}
