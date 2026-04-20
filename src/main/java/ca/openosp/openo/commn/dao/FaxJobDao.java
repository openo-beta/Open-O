//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.FaxJob;

public interface FaxJobDao extends AbstractDao<FaxJob> {

    public List<FaxJob> getFaxStatusByDateDemographicProviderStatusTeam(String demographic_no, String provider_no,
                                                                        String status, String team, Date beginDate, Date endDate);

    public List<FaxJob> getReadyToSendFaxes(String number);

    public List<FaxJob> getInprogressFaxesByJobId();

}
