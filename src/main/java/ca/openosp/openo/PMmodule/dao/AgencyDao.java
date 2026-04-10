//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.Agency;

public interface AgencyDao {

    public Agency getLocalAgency();

    public void saveAgency(Agency agency);

}