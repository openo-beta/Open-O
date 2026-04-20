//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.DrugReason;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.stereotype.Repository;

@Repository
public class DrugReasonDaoImpl extends AbstractDaoImpl<DrugReason> implements DrugReasonDao {

    public DrugReasonDaoImpl() {
        super(DrugReason.class);
    }

    public boolean addNewDrugReason(DrugReason d) {
        MiscUtils.getLogger().debug("trying to save a drug reason");

        try {
            entityManager.persist(d);
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
            return false;
        }
        return true;
    }


    public Boolean hasReason(Integer drugId, String codingSystem, String code, boolean onlyActive) {
        boolean hasReason = false;
        String sqlCommand = "select x from DrugReason x where x.drugId=?1 and x.codingSystem = ?2 and x.code = ?3 and x.archivedFlag = ?4";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, drugId);
        query.setParameter(2, codingSystem);
        query.setParameter(3, code);
        query.setParameter(4, !onlyActive);


        @SuppressWarnings("unchecked")
        List<DrugReason> results = query.getResultList();
        if (results != null && results.size() > 0) {
            hasReason = true;
        }
        return hasReason;
    }


    public List<DrugReason> getReasonsForDrugID(Integer drugId, boolean onlyActive) {

        String sqlCommand = "select x from DrugReason x where x.drugId=?1 and x.archivedFlag = ?2";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, drugId);
        query.setParameter(2, !onlyActive);

        @SuppressWarnings("unchecked")
        List<DrugReason> results = query.getResultList();

        return (results);
    }


    public List<DrugReason> getReasonsByIcd9CodeAndDemographicNo(String icd9Code, Integer demographicNo) {
        String sqlCommand = "select x from DrugReason x where x.codingSystem=?1 and x.code= ?2 and x.demographicNo = ?3 and x.archivedFlag = ?4";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, "icd9");
        query.setParameter(2, icd9Code);
        query.setParameter(3, demographicNo);
        query.setParameter(4, false);

        @SuppressWarnings("unchecked")
        List<DrugReason> results = query.getResultList();

        return (results);
    }

}
