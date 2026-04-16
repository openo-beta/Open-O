//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.OscarLog;

public interface OscarLogDao extends AbstractDao<OscarLog> {

    public List<OscarLog> findByDemographicId(Integer demographicId);

    public List<OscarLog> findByProviderNo(String providerNo);

    public boolean hasRead(String providerNo, String content, String contentId);

    public List<OscarLog> findByActionAndData(String action, String data);

    public List<OscarLog> findByAction(String action, int start, int length, String orderBy, String orderByDirection);

    public List<OscarLog> findByActionContentAndDemographicId(String action, String content, Integer demographicId);

    public List<Integer> getDemographicIdsOpenedSinceTime(Date value);

    public List<Integer> getRecentDemographicsAccessedByProvider(String providerNo, int startPosition,
                                                                 int itemsToReturn);

    public List<Object[]> getRecentDemographicsViewedByProvider(String providerNo, int startPosition,
                                                                int itemsToReturn);

    public List<Object[]> getRecentDemographicsViewedByProviderAfterDateIncluded(String providerNo, Date date,
                                                                                 int startPosition, int itemsToReturn);

    public int purgeLogEntries(Date maxDateToRemove);

}
