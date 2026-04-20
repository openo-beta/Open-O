//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.FormLabReq07;
import org.springframework.stereotype.Repository;

@Repository
public class FormLabReq07DaoImpl extends AbstractDaoImpl<FormLabReq07> implements FormLabReq07Dao {

    public FormLabReq07DaoImpl() {
        super(FormLabReq07.class);
    }

    public List<FormLabReq07> findCreatinine() {
        Query q = entityManager.createQuery("select f from FormLabReq07 f where f.creatinine=?1");
        q.setParameter(1, true);

        @SuppressWarnings("unchecked")
        List<FormLabReq07> results = q.getResultList();

        return results;
    }

    public List<FormLabReq07> findAcr() {
        Query q = entityManager.createQuery("select f from FormLabReq07 f where f.acr=?1");
        q.setParameter(1, true);

        @SuppressWarnings("unchecked")
        List<FormLabReq07> results = q.getResultList();

        return results;
    }
}
