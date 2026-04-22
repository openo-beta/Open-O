//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.Prescription;
import org.springframework.stereotype.Repository;

@Repository
public class PrescriptionDaoImpl extends AbstractDaoImpl<Prescription> implements PrescriptionDao {

    public PrescriptionDaoImpl() {
        super(Prescription.class);
    }

    @Override
    public List<Prescription> findByDemographicId(Integer demographicId) {

        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.demographicId=?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, demographicId);

        @SuppressWarnings("unchecked")
        List<Prescription> results = query.getResultList();
        return (results);
    }

    @Override
    public List<Prescription> findByDemographicIdUpdatedAfterDate(Integer demographicId, Date afterThisDate) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.demographicId=?1 and x.lastUpdateDate>=?2";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, demographicId);
        query.setParameter(2, afterThisDate);

        @SuppressWarnings("unchecked")
        List<Prescription> results = query.getResultList();
        return (results);
    }

    @Override
    public List<Prescription> findByDemographicIdUpdatedAfterDateExclusive(Integer demographicId, Date afterThisDate) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.demographicId=?1 and x.lastUpdateDate>?2";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, demographicId);
        query.setParameter(2, afterThisDate);

        @SuppressWarnings("unchecked")
        List<Prescription> results = query.getResultList();
        return (results);
    }

    @Override
    public int updatePrescriptionsByScriptNo(Integer scriptNo, String comment) {
        Query query = entityManager.createQuery("UPDATE Prescription p SET p.comments = ?1 WHERE p.id = ?2");
        query.setParameter(1, comment);
        query.setParameter(2, scriptNo);
        return query.executeUpdate();
    }

    /**
     * @return results ordered by lastUpdateDate
     */
    @Override
    public List<Prescription> findByUpdateDate(Date updatedAfterThisDateExclusive, int itemsToReturn) {
        String sqlCommand = "select x from " + modelClass.getSimpleName()
                + " x where x.lastUpdateDate>?1 order by x.lastUpdateDate";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, updatedAfterThisDateExclusive);
        setLimit(query, itemsToReturn);

        @SuppressWarnings("unchecked")
        List<Prescription> results = query.getResultList();
        return (results);
    }

    /**
     * @return results ordered by lastUpdateDate asc
     */
    @Override
    public List<Prescription> findByProviderDemographicLastUpdateDate(String providerNo, Integer demographicId,
                                                                      Date updatedAfterThisDateExclusive, int itemsToReturn) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.demographicId=?1 and x.providerNo=?2 and x.lastUpdateDate>?3 order by x.lastUpdateDate";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, demographicId);
        query.setParameter(2, providerNo);
        query.setParameter(3, updatedAfterThisDateExclusive);
        setLimit(query, itemsToReturn);

        @SuppressWarnings("unchecked")
        List<Prescription> results = query.getResultList();
        return (results);
    }

}
