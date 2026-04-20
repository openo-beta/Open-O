//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.CriteriaType;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface CriteriaTypeDao extends AbstractDao<CriteriaType> {

    public List<CriteriaType> findAll();

    public CriteriaType findByName(String fieldName);

    public List<CriteriaType> getAllCriteriaTypes();

    public List<CriteriaType> getAllCriteriaTypesByWlProgramId(Integer wlProgramId);
}
 