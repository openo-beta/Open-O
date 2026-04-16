//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;


import ca.openosp.openo.commn.model.ServiceAccessToken;

public interface ServiceAccessTokenDao extends AbstractDao<ServiceAccessToken> {

    public List<ServiceAccessToken> findAll();

    void persist(ServiceAccessToken token);

    void remove(ServiceAccessToken token);

    public ServiceAccessToken findByTokenId(String token);

}
