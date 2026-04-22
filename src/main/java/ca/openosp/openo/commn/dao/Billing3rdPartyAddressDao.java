//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.billing.CA.ON.model.Billing3rdPartyAddress;

import java.util.List;

public interface Billing3rdPartyAddressDao extends AbstractDao<Billing3rdPartyAddress> {
    List<Billing3rdPartyAddress> findAll();

    List<Billing3rdPartyAddress> findByCompanyName(String companyName);

    List<Billing3rdPartyAddress> findAddresses(String searchModeParam, String orderByParam, String keyword, String limit1, String limit2);
}
