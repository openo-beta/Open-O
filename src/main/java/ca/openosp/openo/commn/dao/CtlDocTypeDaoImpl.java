//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CtlDocType;
import ca.openosp.openo.documentManager.EDocUtil;
import org.springframework.stereotype.Repository;

@Repository
public class CtlDocTypeDaoImpl extends AbstractDaoImpl<CtlDocType> implements CtlDocTypeDao {

    public CtlDocTypeDaoImpl() {
        super(CtlDocType.class);
    }

    public void changeDocType(String docType, String module, String status) {
        List<String> modules = EDocUtil.getModulesForQuery(module);
        String sql = "UPDATE CtlDocType SET status =?1 WHERE module in (?2) AND doctype =?3";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, status);
        query.setParameter(2, modules);
        query.setParameter(3, docType);

        query.executeUpdate();
    }

    public List<CtlDocType> findByStatusAndModule(String[] status, String module) {
        List<String> result = new ArrayList<String>();
        for (int x = 0; x < status.length; x++) {
            result.add(status[x]);
        }
        return this.findByStatusAndModule(result, module);
    }

    public List<CtlDocType> findByStatusAndModule(List<String> status, String module) {
        List<String> modules = EDocUtil.getModulesForQuery(module);
        Query query = entityManager.createQuery("select c from CtlDocType c where c.status in (?1) and c.module in (?2)");
        query.setParameter(1, status);
        query.setParameter(2, modules);
        @SuppressWarnings("unchecked")
        List<CtlDocType> results = query.getResultList();
        return results;
    }

    public List<CtlDocType> findByDocTypeAndModule(String docType, String module) {
        List<String> modules = EDocUtil.getModulesForQuery(module);
        Query query = entityManager.createQuery("select c from CtlDocType c where c.docType=?1 and c.module in (?2)");
        query.setParameter(1, docType);
        query.setParameter(2, modules);
        @SuppressWarnings("unchecked")
        List<CtlDocType> results = query.getResultList();
        return results;
    }

    public void addDocType(String docType, String module) {
        if (module == null) {
            throw new IllegalArgumentException("module cannot be null");
        }
        CtlDocType d = new CtlDocType();
        d.setDocType(docType);
        d.setModule(module.toLowerCase(Locale.ROOT));
        d.setStatus("A");
        entityManager.persist(d);
    }

    public List<String> findModules() {
        Query query = createQuery("SELECT DISTINCT d.module", "d", "");
        List<String> result = new ArrayList<String>();
        for (Object o : query.getResultList()) {
            result.add(String.valueOf(o));
        }
        return result;
    }
}
