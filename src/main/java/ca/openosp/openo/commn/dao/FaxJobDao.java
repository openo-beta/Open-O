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

    /**
     * Retrieves fax jobs by a list of IDs in a single batch query.
     *
     * @param ids List&lt;Integer&gt; the fax job IDs to retrieve
     * @return List&lt;FaxJob&gt; the matching fax jobs, or an empty list if ids is null or empty
     * @since 2026-02-03
     */
    public List<FaxJob> findByIds(List<Integer> ids);

}
