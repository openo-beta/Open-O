//CHECKSTYLE:OFF



package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.OtherId;
import org.springframework.stereotype.Repository;

/**
 * @author Jason Gallagher
 */
@Repository
public class OtherIdDAOImpl extends AbstractDaoImpl<OtherId> implements OtherIdDAO {

    /**
     * Creates a new instance of UserPropertyDAO
     */
    public OtherIdDAOImpl() {
        super(OtherId.class);
    }

    @Override
    public OtherId getOtherId(Integer tableName, Integer tableId, String otherKey) {
        return getOtherId(tableName, String.valueOf(tableId), otherKey);
    }

    @Override
    public OtherId getOtherId(Integer tableName, String tableId, String otherKey) {
        Query query = entityManager.createQuery("select o from OtherId o where o.tableName=?1 and o.tableId=?2 and o.otherKey=?3 and o.deleted=?4 order by o.id desc");
        query.setParameter(1, tableName);
        query.setParameter(2, tableId);
        query.setParameter(3, otherKey);
        query.setParameter(4, false);

        @SuppressWarnings("unchecked")
        List<OtherId> otherIdList = query.getResultList();

        return otherIdList.size() > 0 ? otherIdList.get(0) : null;
    }

    @Override
    public OtherId searchTable(Integer tableName, String otherKey, String otherValue) {
        Query query = entityManager.createQuery("select o from OtherId o where o.tableName=?1 and o.otherKey=?2 and o.otherId=?3 and o.deleted=?4 order by o.id desc");
        query.setParameter(1, tableName);
        query.setParameter(2, otherKey);
        query.setParameter(3, otherValue);
        query.setParameter(4, false);

        @SuppressWarnings("unchecked")
        List<OtherId> otherIdList = query.getResultList();

        return otherIdList.size() > 0 ? otherIdList.get(0) : null;
    }

    @Override
    public void save(OtherId otherId) {
        if (otherId.getId() != null && otherId.getId().intValue() > 0) {
            merge(otherId);
        } else {
            persist(otherId);
        }
    }
}
 