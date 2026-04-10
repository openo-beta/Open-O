//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ClinicLocation;
import org.springframework.stereotype.Repository;

@Repository
public class ClinicLocationDaoImpl extends AbstractDaoImpl<ClinicLocation> implements ClinicLocationDao {

    public ClinicLocationDaoImpl() {
        super(ClinicLocation.class);
    }

    @SuppressWarnings("unchecked")
    public List<ClinicLocation> findAll() {
        Query query = entityManager.createQuery("SELECT x FROM " + modelClass.getSimpleName() + " x");
        List<ClinicLocation> results = query.getResultList();
        return results;
    }

    public List<ClinicLocation> findByClinicNo(Integer clinicNo) {
        String sql = "select c from ClinicLocation c where c.clinicNo=?1 order by c.clinicLocationNo";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, clinicNo);

        @SuppressWarnings("unchecked")
        List<ClinicLocation> results = query.getResultList();

        return results;
    }

    public String searchVisitLocation(String clinicLocationNo) {
        String sql = "select c from ClinicLocation c where c.clinicLocationNo=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, clinicLocationNo);

        @SuppressWarnings("unchecked")
        List<ClinicLocation> results = query.getResultList();
        if (!results.isEmpty()) {
            return results.get(0).getClinicLocationName();
        }
        return null;
    }

    public ClinicLocation searchBillLocation(Integer clinicNo, String clinicLocationNo) {
        String sql = "select c from ClinicLocation c where c.clinicNo=?1 and c.clinicLocationNo=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, clinicNo);
        query.setParameter(2, clinicLocationNo);

        return this.getSingleResultOrNull(query);
    }

    public void removeByClinicLocationNo(String clinicLocationNo) {
        String sql = "select c from ClinicLocation c where c.clinicLocationNo=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, clinicLocationNo);

        @SuppressWarnings("unchecked")
        List<ClinicLocation> results = query.getResultList();
        for (ClinicLocation clinicLocation : results) {
            this.remove(clinicLocation);
        }
    }
}
