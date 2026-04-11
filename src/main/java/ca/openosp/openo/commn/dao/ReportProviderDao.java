//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ReportProvider;

public interface ReportProviderDao extends AbstractDao<ReportProvider> {
    List<ReportProvider> findAll();

    List<ReportProvider> findByAction(String action);

    List<ReportProvider> findByProviderNoTeamAndAction(String providerNo, String team, String action);

    List<Object[]> search_reportprovider(String action);

    List<Object[]> search_reportprovider(String action, String providerNo);
}
