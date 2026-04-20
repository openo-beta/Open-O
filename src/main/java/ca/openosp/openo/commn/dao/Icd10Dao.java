//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.AbstractCodeSystemModel;
import ca.openosp.openo.commn.model.Icd10;

import java.util.List;

public interface Icd10Dao extends AbstractCodeSystemDao<Icd10> {
    List<Icd10> searchCode(String term);
    Icd10 findByCode(String code);
    AbstractCodeSystemModel<?> findByCodingSystem(String codingSystem);
    List<Icd10> searchText(String description);
}
