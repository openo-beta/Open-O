//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.LabReportInformation;
import org.springframework.stereotype.Repository;

@Repository
public class LabReportInformationDaoImpl extends AbstractDaoImpl<LabReportInformation> implements LabReportInformationDao {

    public LabReportInformationDaoImpl() {
        super(LabReportInformation.class);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findReportsByPhysicianId(Integer physicianId) {
        String sql = "FROM LabReportInformation lri, LabPatientPhysicianInfo lpp "
                + "WHERE lpp.id = ?1 "
                + "AND lri.id = lpp.labReportInfoId";
        Query q = entityManager.createQuery(sql);
        q.setParameter(1, physicianId);
        return q.getResultList();
    }
}
