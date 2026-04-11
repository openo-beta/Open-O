//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collection;
import java.util.List;

import ca.openosp.openo.commn.model.DemographicCust;

public interface DemographicCustDao extends AbstractDao<DemographicCust> {

    public List<DemographicCust> findMultipleMidwife(Collection<Integer> demographicNos, String oldMidwife);

    public List<DemographicCust> findMultipleResident(Collection<Integer> demographicNos, String oldResident);

    public List<DemographicCust> findMultipleNurse(Collection<Integer> demographicNos, String oldNurse);

    public List<DemographicCust> findByResident(String resident);

    public Integer select_demoname(String resident, String lastNameRegExp);

    public Integer select_demoname1(String nurse, String lastNameRegExp);

    public Integer select_demoname2(String midwife, String lastNameRegExp);

    public List<DemographicCust> findAllByDemographicNumber(int demographic_no);

}
