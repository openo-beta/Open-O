//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.AppDefinition;

public interface AppDefinitionDao extends AbstractDao<AppDefinition> {

    public List<AppDefinition> findAll();

    public AppDefinition findByName(String name);
    
    public AppDefinition findByConsumerKey(String consumerKey);
}
