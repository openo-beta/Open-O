//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.DrugDispensingMapping;
import org.springframework.stereotype.Repository;

@Repository
public class DrugDispensingMappingDaoImpl extends AbstractDaoImpl<DrugDispensingMapping> implements DrugDispensingMappingDao {

    public DrugDispensingMappingDaoImpl() {
        super(DrugDispensingMapping.class);
    }

    public DrugDispensingMapping findMappingByDin(String din) {

        Query query = entityManager.createQuery("SELECT x FROM DrugDispensingMapping x where x.din = ?1");
        query.setParameter(1, din);

        return this.getSingleResultOrNull(query);
    }

    public DrugDispensingMapping findMapping(String din, String duration, String durUnit, String freqCode, String quantity, Float takeMin, Float takeMax) {

        StringBuilder queryStr = new StringBuilder("SELECT x FROM DrugDispensingMapping x where x.din = :din and x.duration = :duration and x.quantity = :quantity");

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        if (durUnit != null) {
            queryStr.append(" and x.durUnit = :durUnit ");
            params.put("durUnit", durUnit);
        }
        if (freqCode != null) {
            queryStr.append(" and x.freqCode = :freqCode ");
            params.put("freqCode", freqCode);
        }
        if (takeMin != null) {
            queryStr.append(" and x.takeMin = :takeMin ");
            params.put("takeMin", takeMin);
        }
        if (takeMax != null) {
            queryStr.append(" and x.takeMax = :takeMax ");
            params.put("takeMax", takeMax);
        }

        Query query = entityManager.createQuery(queryStr.toString());

        query.setParameter("din", din);
        query.setParameter("duration", duration);
        query.setParameter("quantity", quantity);

        for (String key : params.keySet()) {
            query.setParameter(key, params.get(key));
        }


        return this.getSingleResultOrNull(query);
    }
}
