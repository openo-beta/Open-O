//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.commn.model.CVCImmunization;

public interface CVCImmunizationDao extends AbstractDao<CVCImmunization> {

    public void removeAll();

    public List<CVCImmunization> findAllGeneric();

    public List<CVCImmunization> findByParent(String conceptCodeId);

    public CVCImmunization findBySnomedConceptId(String conceptCodeId);

    public List<CVCImmunization> query(String term, boolean includeGenerics, boolean includeBrands);
}
