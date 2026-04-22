//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MyGroupAccessRestriction;

public interface MyGroupAccessRestrictionDao extends AbstractDao<MyGroupAccessRestriction> {
    List<MyGroupAccessRestriction> findByGroupId(String myGroupNo);

    List<MyGroupAccessRestriction> findByProviderNo(String providerNo);

    MyGroupAccessRestriction findByGroupNoAndProvider(String myGroupNo, String providerNo);
}
