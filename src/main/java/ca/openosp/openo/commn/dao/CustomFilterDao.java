//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CustomFilter;

public interface CustomFilterDao extends AbstractDao<CustomFilter> {

    public CustomFilter findByName(String name);

    public CustomFilter findByNameAndProviderNo(String name, String providerNo);

    public List<CustomFilter> getCustomFilters();

    public List<CustomFilter> findByProviderNo(String providerNo);

    public List<CustomFilter> getCustomFilterWithShortCut(String providerNo);

    public void deleteCustomFilter(String name);
}