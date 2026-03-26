//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Relationships;
import org.springframework.stereotype.Repository;
import ca.openosp.openo.util.ConversionUtils;

@Repository
public class RelationshipsDaoImpl extends AbstractDaoImpl<Relationships> implements RelationshipsDao {

    public RelationshipsDaoImpl() {
        super(Relationships.class);
    }

    @Override
    public List<Relationships> findAll() {
        String sql = "select x from Relationships x order by x.demographicNo";
        Query query = entityManager.createQuery(sql);
        @SuppressWarnings("unchecked")
        List<Relationships> results = query.getResultList();
        return results;
    }

    @Override
    public Relationships findActive(Integer id) {
        Query query = entityManager.createQuery("FROM " + modelClass.getSimpleName() + " r WHERE r.id = ?1 AND (r.deleted IS NULL OR r.deleted = '0')");
        query.setParameter(1, id);
        return getSingleResultOrNull(query);
    }

    @Override
    public List<Relationships> findByDemographicNumber(Integer demographicNumber) {
        Query query = entityManager.createQuery("FROM " + modelClass.getSimpleName() + " r WHERE r.demographicNo = ?1 AND (r.deleted IS NULL OR r.deleted = '0')");
        query.setParameter(1, demographicNumber);
        return query.getResultList();
    }

    @Override
    public List<Relationships> findActiveSubDecisionMaker(Integer demographicNumber) {
        Query query = entityManager.createQuery("FROM " + modelClass.getSimpleName() + " r WHERE r.demographicNo = ?1 AND r.subDecisionMaker = ?2 AND (r.deleted IS NULL OR r.deleted = '0')");
        query.setParameter(1, demographicNumber);
        query.setParameter(2, ConversionUtils.toBoolString(Boolean.TRUE));
        return query.getResultList();
    }

    @Override
    public List<Relationships> findActiveByDemographicNumberAndFacility(Integer demographicNumber, Integer facilityId) {
        Query query = entityManager.createQuery("FROM " + modelClass.getSimpleName() + " r WHERE r.demographicNo = ?1 AND r.facilityId = ?2 AND (r.deleted IS NULL OR r.deleted = '0')");
        query.setParameter(1, demographicNumber);
        query.setParameter(2, facilityId);
        return query.getResultList();
    }
}
