//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;


import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ResourceStorage;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceStorageDaoImpl extends AbstractDaoImpl<ResourceStorage> implements ResourceStorageDao {

    public ResourceStorageDaoImpl() {
        super(ResourceStorage.class);
    }

    @Override
    public ResourceStorage findActive(String resourceType) {
        String sqlCommand = "FROM " + modelClass.getSimpleName() + " r WHERE r.resourceType = ?1 AND r.active = true";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, resourceType);
        return getSingleResultOrNull(query);
    }

    @Override
    public List<ResourceStorage> findActiveAll(String resourceType) {
        String sqlCommand = "FROM " + modelClass.getSimpleName() + " r WHERE r.resourceType = ?1 AND r.active = true";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, resourceType);
        return query.getResultList();
    }

    @Override
    public List<ResourceStorage> findAll(String resourceType) {
        String sqlCommand = "FROM " + modelClass.getSimpleName() + " r WHERE r.resourceType = ?1";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, resourceType);
        return query.getResultList();
    }

    @Override
    public List<ResourceStorage> findByUUID(String uuid) {
        String sqlCommand = "FROM " + modelClass.getSimpleName() + " r WHERE r.uuid = ?1";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, uuid);
        return query.getResultList();
    }

}
