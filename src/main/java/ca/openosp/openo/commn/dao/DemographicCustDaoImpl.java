//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collection;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.DemographicCust;
import org.springframework.stereotype.Repository;

@Repository
public class DemographicCustDaoImpl extends AbstractDaoImpl<DemographicCust> implements DemographicCustDao {

    public DemographicCustDaoImpl() {
        super(DemographicCust.class);
    }

    @Override
    public List<DemographicCust> findMultipleMidwife(Collection<Integer> demographicNos, String oldMidwife) {
        String sql = "select x from DemographicCust x where x.id IN (?1) and x.midwife=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNos);
        query.setParameter(2, oldMidwife);

        @SuppressWarnings("unchecked")
        List<DemographicCust> results = query.getResultList();
        return results;
    }

    @Override
    public List<DemographicCust> findMultipleResident(Collection<Integer> demographicNos, String oldResident) {
        String sql = "select x from DemographicCust x where x.id IN (?1) and x.resident=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNos);
        query.setParameter(2, oldResident);

        @SuppressWarnings("unchecked")
        List<DemographicCust> results = query.getResultList();
        return results;
    }

    @Override
    public List<DemographicCust> findMultipleNurse(Collection<Integer> demographicNos, String oldNurse) {
        String sql = "select x from DemographicCust x where x.id IN (?1) and x.nurse=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNos);
        query.setParameter(2, oldNurse);

        @SuppressWarnings("unchecked")
        List<DemographicCust> results = query.getResultList();
        return results;
    }

    @Override
    public List<DemographicCust> findByResident(String resident) {
        String sql = "select x from DemographicCust x where x.resident like ?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, resident);

        @SuppressWarnings("unchecked")
        List<DemographicCust> results = query.getResultList();
        return results;
    }

    @Override
    public Integer select_demoname(String resident, String lastNameRegExp) {
        String sql = "select d.demographic_no from demographic d, demographiccust c where c.cust2=?1 and d.demographic_no=c.demographic_no and d.last_name REGEXP ?2";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, resident);
        query.setParameter(2, lastNameRegExp);

        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    @Override
    public Integer select_demoname1(String nurse, String lastNameRegExp) {
        String sql = "select d.demographic_no from demographic d, demographiccust c where c.cust2=?1 and d.demographic_no=c.demographic_no and d.last_name REGEXP ?2";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, nurse);
        query.setParameter(2, lastNameRegExp);

        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    @Override
    public Integer select_demoname2(String midwife, String lastNameRegExp) {
        String sql = "select d.demographic_no from demographic d, demographiccust c where c.cust2=?1 and d.demographic_no=c.demographic_no and d.last_name REGEXP ?2";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, midwife);
        query.setParameter(2, lastNameRegExp);

        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    @Override
    public List<DemographicCust> findAllByDemographicNumber(int demographic_no) {
        String sql = "select x from DemographicCust x where x.id = ?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographic_no);

        @SuppressWarnings("unchecked")
        List<DemographicCust> results = query.getResultList();
        return results;
    }

}
