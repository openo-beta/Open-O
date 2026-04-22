//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collection;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.DemographicSets;
import org.springframework.stereotype.Repository;

@Repository
public class DemographicSetsDaoImpl extends AbstractDaoImpl<DemographicSets> implements DemographicSetsDao {

    public DemographicSetsDaoImpl() {
        super(DemographicSets.class);
    }

    @Override
    public List<DemographicSets> findBySetName(String setName) {
        String sql = "select x from DemographicSets x where x.archive != ?1 and x.name=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, "1");
        query.setParameter(2, setName);
        @SuppressWarnings("unchecked")
        List<DemographicSets> results = query.getResultList();
        return results;
    }

    @Override
    public List<DemographicSets> findBySetNames(Collection<String> setNameList) {
        String sql = "select x from DemographicSets x where x.archive != ?1 and x.name in (?2)";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, "1");
        query.setParameter(2, setNameList);
        @SuppressWarnings("unchecked")
        List<DemographicSets> results = query.getResultList();
        return results;
    }

    @Override
    public List<DemographicSets> findBySetNameAndEligibility(String setName, String eligibility) {
        String sql = "select x from DemographicSets x where x.name = ?1 and x.eligibility=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, setName);
        query.setParameter(2, eligibility);
        @SuppressWarnings("unchecked")
        List<DemographicSets> results = query.getResultList();
        return results;
    }

    @Override
    public List<String> findSetNamesByDemographicNo(Integer demographicNo) {
        String sql = "select distinct(x.name) from DemographicSets x where x.archive = ?1 and x.demographicNo=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, "1");
        query.setParameter(2, demographicNo);
        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();
        return results;
    }

    @Override
    public List<String> findSetNames() {
        String sql = "select distinct(x.name) from DemographicSets x";
        Query query = entityManager.createQuery(sql);
        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();
        return results;
    }

    @Override
    public List<DemographicSets> findBySetNameAndDemographicNo(String setName, int demographicNo) {
        String sql = "select x from DemographicSets x where x.name = ?1 and x.demographicNo=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, setName);
        query.setParameter(2, demographicNo);
        @SuppressWarnings("unchecked")
        List<DemographicSets> results = query.getResultList();
        return results;
    }
}
