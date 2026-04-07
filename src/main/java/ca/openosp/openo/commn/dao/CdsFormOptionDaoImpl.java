//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CdsFormOption;
import org.springframework.stereotype.Repository;

@Repository
public class CdsFormOptionDaoImpl extends AbstractDaoImpl<CdsFormOption> implements CdsFormOptionDao {

    public CdsFormOptionDaoImpl() {
        super(CdsFormOption.class);
    }

    public List<CdsFormOption> findByVersionAndCategory(String formVersion, String mainCatgeory) {
        String sqlCommand = "select x from CdsFormOption x where x.cdsFormVersion=?1 and x.cdsDataCategory like ?2 order by x.id";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, formVersion);
        query.setParameter(2, mainCatgeory + '%');
        @SuppressWarnings("unchecked")
        List<CdsFormOption> results = query.getResultList();
        return (results);
    }

    public List<CdsFormOption> findByVersion(String formVersion) {
        String sqlCommand = "select x from CdsFormOption x where x.cdsFormVersion=?1 order by x.id";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, formVersion);
        @SuppressWarnings("unchecked")
        List<CdsFormOption> results = query.getResultList();
        return (results);
    }
}
