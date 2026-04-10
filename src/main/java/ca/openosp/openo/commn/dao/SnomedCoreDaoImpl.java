//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.AbstractCodeSystemModel;
import ca.openosp.openo.commn.model.SnomedCore;
import org.springframework.stereotype.Repository;

@Repository
public class SnomedCoreDaoImpl extends AbstractCodeSystemDaoImpl<SnomedCore> implements SnomedCoreDao {

    public SnomedCoreDaoImpl() {
        super(SnomedCore.class);
    }

    @Override
    public List<SnomedCore> getSnomedCoreCode(String snomedCoreCode) {
        Query query = entityManager.createQuery("select i from SnomedCore i where i.snomedCore=?1");
        query.setParameter(1, snomedCoreCode);

        @SuppressWarnings("unchecked")
        List<SnomedCore> results = query.getResultList();

        return results;
    }

    @Override
    public List<SnomedCore> getSnomedCore(String query) {
        Query q = entityManager.createQuery("select i from SnomedCore i where i.snomedCore like ?1 or i.description like ?2 order by i.description");
        q.setParameter(1, "%" + query + "%");
        q.setParameter(2, "%" + query + "%");

        @SuppressWarnings("unchecked")
        List<SnomedCore> results = q.getResultList();

        return results;
    }

    @Override
    public List<SnomedCore> searchCode(String term) {
        return getSnomedCore(term);
    }

    @Override
    public SnomedCore findByCode(String code) {
        List<SnomedCore> results = getSnomedCoreCode(code);
        if (results.isEmpty())
            return null;
        return results.get(0);
    }

    @Override
    public AbstractCodeSystemModel<?> findByCodingSystem(String codingSystem) {
        Query query = entityManager.createQuery("FROM SnomedCore s WHERE s.snomedCore like ?1");
        query.setParameter(1, codingSystem);
        query.setMaxResults(1);
        return getSingleResultOrNull(query);
    }
}
