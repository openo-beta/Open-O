//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.CriteriaTypeOption;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface CriteriaTypeOptionDao extends AbstractDao<CriteriaTypeOption> {

    public List<CriteriaTypeOption> findAll();

    public List<CriteriaTypeOption> getCriteriaTypeOptionByTypeId(Integer typeId);

    public CriteriaTypeOption getByValue(String optionValue);

    public CriteriaTypeOption getByValueAndTypeId(String optionValue, Integer typeId);
}
 