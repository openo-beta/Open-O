//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Form;
import org.springframework.stereotype.Repository;

@Repository
public class FormDaoImpl extends AbstractDaoImpl<Form> implements FormDao {

    public FormDaoImpl() {
        super(Form.class);
    }

    @SuppressWarnings("unchecked")
    public List<Form> findByDemographicNo(Integer demographicNo) {
        Query q = entityManager.createQuery("select f from Form f where f.demographicNo = ?1 order by f.formDate desc, f.formTime desc, f.id desc");
        q.setParameter(1, demographicNo);

        return q.getResultList();

    }

    @SuppressWarnings("unchecked")
    public Form search_form_no(Integer demographicNo, String formName) {
        Query q = entityManager.createQuery("select f from Form f where f.demographicNo = ?1 and f.formName like ?2 order by f.formDate desc, f.formTime desc, f.id ");
        q.setParameter(1, demographicNo);
        q.setParameter(2, formName);
        q.setMaxResults(1);

        return this.getSingleResultOrNull(q);

    }

    @SuppressWarnings("unchecked")
    public List<Form> findAllGroupByDemographicNo() {
        Query q = entityManager.createQuery("select f from Form f group by f.demographicNo");

        return q.getResultList();
    }
}
