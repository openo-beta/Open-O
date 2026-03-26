//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ReportByExamplesFavorite;

public interface ReportByExamplesFavoriteDao extends AbstractDao<ReportByExamplesFavorite> {
    List<ReportByExamplesFavorite> findByQuery(String query);

    List<ReportByExamplesFavorite> findByEverything(String providerNo, String favoriteName, String queryString);

    List<ReportByExamplesFavorite> findByProvider(String providerNo);
}
