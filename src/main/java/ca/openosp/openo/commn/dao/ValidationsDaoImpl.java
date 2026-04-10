//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.Validations;
import org.springframework.stereotype.Repository;

@Repository
public class ValidationsDaoImpl extends AbstractDaoImpl<Validations> implements ValidationsDao {

    public ValidationsDaoImpl() {
        super(Validations.class);
    }

    @SuppressWarnings("unchecked")
    public List<Validations> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Validations> findByAll(String regularExpParam, Double minValueParam, Double maxValueParam,
                                       Integer minLengthParam, Integer maxLengthParam, Boolean isNumericParam,
                                       Boolean isDateParam) {

        StringBuilder buf = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        for (Object[] i : new Object[][]{{"regularExp", regularExpParam}, {"minValue", minValueParam}, {"maxValue", maxValueParam}, {"minLength", minLengthParam}, {"maxLength", maxLengthParam}, {"isNumeric", isNumericParam}, {"isDate", isDateParam}}) {
            String name = (String) i[0];
            Object value = i[1];
            if (buf.length() > 0) {
                buf.append(" AND ");
            }
            buf.append("v.").append(name).append(" = :").append(name);
            params.put(name, value);
        }

        if (buf.length() > 0) {
            buf.insert(0, " WHERE ");
        }
        buf.insert(0, "FROM Validations v");

        Query query = entityManager.createQuery(buf.toString());
        for (Entry<String, Object> e : params.entrySet()) {
            query.setParameter(e.getKey(), e.getValue());
        }
        return query.getResultList();

    }

    @SuppressWarnings("unchecked")
    public List<Validations> findByName(String name) {
        Query query = createQuery("v", "v.name = ?1");
        query.setParameter(1, name);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findValidationsBy(Integer demo, String type, Integer validationId) {
        String sql = "FROM Validations v, Measurement m " +
                "WHERE " +
                "m.demographicId = ?1 " +
                "AND m.type = ?2 " +
                "AND v.id = ?3 " +
                "GROUP BY m.id " +
                "ORDER BY m.dateObserved DESC, m.createDate DESC";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demo);
        query.setParameter(2, type);
        query.setParameter(3, validationId);
        return query.getResultList();

    }
}
