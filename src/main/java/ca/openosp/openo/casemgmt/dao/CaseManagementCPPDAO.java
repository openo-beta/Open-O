//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.casemgmt.model.CaseManagementCPP;

/*
 * Updated by Eugene Petruhin on 09 jan 2009 while fixing #2482832 & #2494061
 */
public interface CaseManagementCPPDAO {

    public CaseManagementCPP getCPP(String demographic_no);

    public void saveCPP(CaseManagementCPP cpp);
}
