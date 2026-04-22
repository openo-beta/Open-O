//CHECKSTYLE:OFF



package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ResourceStorage;

public interface ResourceStorageDao extends AbstractDao<ResourceStorage> {
    public ResourceStorage findActive(String resourceType);

    public List<ResourceStorage> findActiveAll(String resourceType);

    public List<ResourceStorage> findAll(String resourceType);

    public List<ResourceStorage> findByUUID(String uuid);
}
