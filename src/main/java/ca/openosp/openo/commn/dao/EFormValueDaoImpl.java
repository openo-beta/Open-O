//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.EFormValue;
import org.springframework.stereotype.Repository;

@Repository
public class EFormValueDaoImpl extends AbstractDaoImpl<EFormValue> implements EFormValueDao {

    public EFormValueDaoImpl() {
        super(EFormValue.class);
    }

    public List<EFormValue> findByDemographicId(Integer demographicId) {
        Query query = entityManager.createQuery("select x from " + modelClass.getSimpleName() + " x where x.demographicId=?1");
        query.setParameter(1, demographicId);

        @SuppressWarnings("unchecked")
        List<EFormValue> results = query.getResultList();

        return (results);
    }

    public List<EFormValue> findByApptNo(int apptNo) {
        Query query = entityManager.createQuery("select x from " + modelClass.getSimpleName() + " x where x.varName='appt_no' and x.varValue=?1");
        query.setParameter(1, String.valueOf(apptNo));

        @SuppressWarnings("unchecked")
        List<EFormValue> results = query.getResultList();

        return (results);
    }

    public List<EFormValue> findByFormDataId(int fdid) {
        Query query = entityManager.createQuery("select x from " + modelClass.getSimpleName() + " x where x.formDataId=?1");
        query.setParameter(1, fdid);

        @SuppressWarnings("unchecked")
        List<EFormValue> results = query.getResultList();

        return (results);
    }

    public EFormValue findByFormDataIdAndKey(int fdid, String varName) {
        Query query = entityManager.createQuery("select x from " + modelClass.getSimpleName() + " x where x.formDataId=?1 and x.varName=?2");
        query.setParameter(1, fdid);
        query.setParameter(2, varName);

        @SuppressWarnings("unchecked")
        List<EFormValue> results = query.getResultList();

        if (results == null || results.isEmpty()) return null;
        return (results.get(0));
    }

    public List<EFormValue> findByFormDataIdList(List<Integer> fdids) {
        if (fdids == null || fdids.isEmpty()) return new ArrayList<EFormValue>();

        Query query = entityManager.createQuery("select x from " + modelClass.getSimpleName() + " x where x.formDataId in (?1)");
        query.setParameter(1, fdids);

        @SuppressWarnings("unchecked")
        List<EFormValue> results = query.getResultList();

        return (results);
    }

    public List<String> findAllVarNamesForEForm(Integer fid) {
        Query query = entityManager.createQuery("select DISTINCT(x.varName) from " + modelClass.getSimpleName() + " x where x.formId in (?1)");
        query.setParameter(1, fid);

        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();

        return (results);
    }
}
