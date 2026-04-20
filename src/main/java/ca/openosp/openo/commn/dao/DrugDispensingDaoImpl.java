//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.DrugDispensing;
import org.springframework.stereotype.Repository;

@Repository
public class DrugDispensingDaoImpl extends AbstractDaoImpl<DrugDispensing> implements DrugDispensingDao {

    public DrugDispensingDaoImpl() {
        super(DrugDispensing.class);
    }

    @Override
    public List<DrugDispensing> findByDrugId(Integer drugId) {
        Query query = entityManager.createQuery("SELECT x FROM DrugDispensing x where x.drugId = ?1  order by x.dateCreated DESC");
        query.setParameter(1, drugId);

        @SuppressWarnings("unchecked")
        List<DrugDispensing> results = query.getResultList();
        return results;
    }


}
