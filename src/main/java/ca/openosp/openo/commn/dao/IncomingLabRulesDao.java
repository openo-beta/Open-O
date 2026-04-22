//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.IncomingLabRules;

public interface IncomingLabRulesDao extends AbstractDao<IncomingLabRules> {
    List<IncomingLabRules> findCurrentByProviderNoAndFrwdProvider(String providerNo, String frwdProvider);

    List<IncomingLabRules> findByProviderNoAndFrwdProvider(String providerNo, String frwdProvider);

    List<IncomingLabRules> findCurrentByProviderNo(String providerNo);

    List<IncomingLabRules> findByProviderNo(String providerNo);

    List<Object[]> findRules(String providerNo);
}
