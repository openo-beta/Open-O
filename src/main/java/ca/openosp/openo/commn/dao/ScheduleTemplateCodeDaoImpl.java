//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ScheduleTemplateCode;
import org.springframework.stereotype.Repository;

@Repository
public class ScheduleTemplateCodeDaoImpl extends AbstractDaoImpl<ScheduleTemplateCode> implements ScheduleTemplateCodeDao {

    public ScheduleTemplateCodeDaoImpl() {
        super(ScheduleTemplateCode.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ScheduleTemplateCode> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @Override
    public ScheduleTemplateCode getByCode(char code) {
        //Query query = entityManager.createQuery("FROM " + modelClass.getSimpleName() + " bst WHERE bst.id IN (:typeCodes)");
        Query query = entityManager.createQuery("select s from ScheduleTemplateCode s where s.code=?1");
        query.setParameter(1, code);

        @SuppressWarnings("unchecked")
        List<ScheduleTemplateCode> results = query.getResultList();
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

    @Override
    public List<ScheduleTemplateCode> findTemplateCodes() {
        Query query = entityManager.createQuery("select s from ScheduleTemplateCode s where s.bookinglimit > 0 and s.duration <>''");

        @SuppressWarnings("unchecked")
        List<ScheduleTemplateCode> results = query.getResultList();

        return results;
    }

    @Override
    public ScheduleTemplateCode findByCode(String code) {
        char codeChar = code.charAt(0);
        Query query = entityManager.createQuery("select s from ScheduleTemplateCode s where s.code like ?1");
        query.setParameter(1, codeChar);

        @SuppressWarnings("unchecked")
        List<ScheduleTemplateCode> results = query.getResultList();
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }
}
