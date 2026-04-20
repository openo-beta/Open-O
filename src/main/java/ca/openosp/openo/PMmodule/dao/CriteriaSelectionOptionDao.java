//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.CriteriaSelectionOption;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface CriteriaSelectionOptionDao extends AbstractDao<CriteriaSelectionOption> {

    public List<CriteriaSelectionOption> getCriteriaSelectedOptionsByCriteriaId(Integer criteriaId);

}
 
 