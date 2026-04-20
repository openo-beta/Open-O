//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.LabPatientPhysicianInfo;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class LabPatientPhysicianInfoDaoImpl extends AbstractDaoImpl<LabPatientPhysicianInfo> implements LabPatientPhysicianInfoDao {

    public LabPatientPhysicianInfoDaoImpl() {
        super(LabPatientPhysicianInfo.class);
    }

    @Override
    public List<Object[]> findRoutings(Integer demographicNo, String labType) {
        String sql = "FROM LabPatientPhysicianInfo lpp, PatientLabRouting r "
                + "WHERE r.labType = :labType " +
                "AND lpp.id = r.labNo " +
                "AND r.demographicNo = :demoNo";
        Query query = entityManager.createQuery(sql);
        query.setParameter("labType", labType);
        query.setParameter("demoNo", demographicNo);
        return query.getResultList();
    }

    @Override
    public List<Object[]> findByPatientName(String status, String labType, String providerNo, String patientLastName, String patientFirstName, String patientHealthNumber) {
        String sql = "FROM LabPatientPhysicianInfo lpp, ProviderLabRoutingModel plr " +
                "WHERE plr.status like :status " +
                "AND plr.providerNo like :providerNo " +
                "AND plr.labType = :labType " +
                "AND lpp.patientLastName LIKE :lastName " +
                "AND lpp.patientFirstName LIKE :firstName " +
                "AND lpp.patientHin LIKE :hin " +
                "AND plr.labNo = lpp.id";
        Query q = entityManager.createQuery(sql);
        q.setParameter("status", "%" + status + "%");
        q.setParameter("providerNo", "".equals(providerNo) ? "%" : providerNo);
        q.setParameter("labType", labType);
        q.setParameter("lastName", patientLastName + "%");
        q.setParameter("firstName", patientFirstName + "%");
        q.setParameter("hin", patientHealthNumber);
        return q.getResultList();
    }

    @Override
    public List<Object[]> findByDemographic(Integer demographicNo, String labType) {
        String sql = "FROM LabPatientPhysicianInfo lpp, PatientLabRouting plr " +
                "WHERE plr.labType = :labType " +
                "AND lpp.id = plr.labNo " +
                "AND plr.demographicNo = :demoNo";
        Query q = entityManager.createQuery(sql);
        q.setParameter("labType", labType);
        q.setParameter("demoNo", demographicNo);
        return q.getResultList();
    }

    @Override
    public List<Object[]> findLabServiceDatesByLabId(Integer labId) {
        String sql = "SELECT DISTINCT lpp.id, lpp.serviceDate, lpp2.serviceDate " +
                "FROM LabPatientPhysicianInfo lpp, LabPatientPhysicianInfo lpp2, LabReportInformation tr " +
                "WHERE lpp.accessionNum = lpp2.accessionNum " +
                "AND lpp2.id = :labId " +
                "AND tr.id = lpp.labReportInfoId " +
                "ORDER BY tr.printDate, tr.printTime";
        Query query = entityManager.createQuery(sql);
        query.setParameter("labId", labId);
        return query.getResultList();
    }

    @Override
    public List<Integer> getLabResultsSince(Integer demographicNo, Date updateDate) {
        String sql = "select lpp.id from LabPatientPhysicianInfo lpp, PatientLabRouting plr WHERE plr.labNo = lpp.id and plr.demographicNo = ?1 and (lpp.lastUpdateDate > ?2 or plr.dateModified > ?3) AND plr.labType IN ('Epsilon','CML')";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNo);
        query.setParameter(2, updateDate);
        query.setParameter(3, updateDate);

        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();
        return results;
    }
}
