//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.LabTestResults;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class LabTestResultsDaoImpl extends AbstractDaoImpl<LabTestResults> implements LabTestResultsDao {

    public LabTestResultsDaoImpl() {
        super(LabTestResults.class);
    }

    @Override
    public List<LabTestResults> findByTitleAndLabInfoId(Integer labId) {
        Query query = createQuery("r", "r.title IS NOT EMPTY and r.labPatientPhysicianInfoId = :labId");
        query.setParameter("labId", labId);
        return query.getResultList();
    }

    @Override
    public List<LabTestResults> findByLabInfoId(Integer labId) {
        Query query = createQuery("r", "r.labPatientPhysicianInfoId = :labId");
        query.setParameter("labId", labId);
        return query.getResultList();
    }

    @Override
    public List<LabTestResults> findByAbnAndLabInfoId(String abn, Integer labId) {
        Query query = createQuery("r", "r.abn = :abn and r.labPatientPhysicianInfoId = :labId");
        query.setParameter("labId", labId);
        query.setParameter("abn", abn);
        return query.getResultList();
    }

    /**
     * Finds unique test names for the specified patient and lab type
     *
     * @param demoNo  Demographic id of the patient
     * @param labType Type of the lab to find results for
     * @return Returns a list of triples containing lab type, test title and test name.
     */
    @Override
    public List<Object[]> findUniqueTestNames(Integer demoNo, String labType) {
        String jpql = "SELECT DISTINCT p.labType, ltr.title, ltr.testName " +
                "FROM " +
                "PatientLabRouting p, " +
                "LabTestResults ltr, " +
                "LabPatientPhysicianInfo lpp " +
                "WHERE p.labType = :labType " +
                "AND p.demographicNo = :demoNo " +
                "AND p.labNo = ltr.labPatientPhysicianInfoId " +
                "AND ltr.labPatientPhysicianInfoId = lpp.id " +
                "AND ltr.testName IS NOT NULL " +
                "AND ltr.testName IS NOT EMPTY " +
                "ORDER BY ltr.title";

        Query query = entityManager.createQuery(jpql);
        query.setParameter("labType", labType);
        query.setParameter("demoNo", demoNo);
        return query.getResultList();
    }

    @Override
    public List<LabTestResults> findByAbnAndPhysicianId(String abn, Integer lppii) {
        Query q = createQuery("ltr", "ltr.abn = :abn and ltr.labPatientPhysicianInfoId = :lppii");
        q.setParameter("abn", abn);
        q.setParameter("lppii", lppii);
        return q.getResultList();
    }

    @Override
    public List<LabTestResults> findByLabPatientPhysicialInfoId(Integer labid) {
        Query query = createQuery("r", "r.labPatientPhysicianInfoId = :labid");
        query.setParameter("labid", labid);
        return query.getResultList();
    }
}
