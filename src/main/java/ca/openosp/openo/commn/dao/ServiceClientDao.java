//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ServiceClient;

import java.util.List;

public interface ServiceClientDao extends AbstractDao<ServiceClient> {
    List<ServiceClient> findAll();

    ServiceClient findByName(String name);

    ServiceClient findByKey(String key);

    ServiceClient find(Integer id);
}
