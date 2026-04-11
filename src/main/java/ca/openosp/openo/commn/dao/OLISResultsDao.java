//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.OLISResults;

public interface OLISResultsDao extends AbstractDao<OLISResults> {
    boolean hasExistingResult(String requestingHICProviderNo, String queryType, String hash);

    List<OLISResults> getResultList(String requestingHICProviderNo, String queryType);

    OLISResults findByUUID(String uuid);
}
