//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.IntegratorProgressItem;

public interface IntegratorProgressItemDao extends AbstractDao<IntegratorProgressItem> {
    IntegratorProgressItem find(int integratorProgressId, int demographicNo);

    List<Integer> findOutstandingDemographicNos(int integratorProgressId);

    Integer findTotalOutstandingDemographicNos(int integratorProgressId);

    Integer findTotalDemographicNos(int integratorProgressId);
}
