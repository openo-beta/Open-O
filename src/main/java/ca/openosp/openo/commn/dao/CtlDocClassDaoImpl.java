//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CtlDocClass;
import org.springframework.stereotype.Repository;

@Repository
public class CtlDocClassDaoImpl extends AbstractDaoImpl<CtlDocClass> implements CtlDocClassDao {

    public CtlDocClassDaoImpl() {
        super(CtlDocClass.class);
    }

    @Override
    public List<String> findUniqueReportClasses() {

        String sqlCommand = "select distinct x.reportClass from CtlDocClass x order by lower(x.reportClass)";

        Query query = entityManager.createQuery(sqlCommand);

        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();

        return (results);
    }

    @Override
    public List<String> findSubClassesByReportClass(String reportClass) {

        String sqlCommand = "select x.subClass from CtlDocClass x where x.reportClass=?1 order by x.subClass";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, reportClass);

        @SuppressWarnings("unchecked")
        List<String> results = query.getResultList();

        return (results);
    }
}
