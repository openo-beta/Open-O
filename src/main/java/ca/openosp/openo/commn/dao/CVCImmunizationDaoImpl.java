//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CVCImmunization;
import org.springframework.stereotype.Repository;

@Repository
public class CVCImmunizationDaoImpl extends AbstractDaoImpl<CVCImmunization> implements CVCImmunizationDao {

    public CVCImmunizationDaoImpl() {
        super(CVCImmunization.class);
    }

    @Override
    public void removeAll() {
        Query query = entityManager.createQuery("DELETE FROM CVCImmunization");
        query.executeUpdate();
    }

    @Override
    public List<CVCImmunization> findAllGeneric() {
        Query query = entityManager.createQuery("SELECT x FROM CVCImmunization x WHERE x.generic = :generic");
        query.setParameter("generic", true);
        List<CVCImmunization> result = query.getResultList();
        return result;
    }

    @Override
    public List<CVCImmunization> findByParent(String conceptCodeId) {
        Query query = entityManager
                .createQuery("SELECT x FROM CVCImmunization x WHERE x.parentConceptId = :parentConceptId");
        query.setParameter("parentConceptId", conceptCodeId);
        List<CVCImmunization> result = query.getResultList();
        return result;
    }

    @Override
    public CVCImmunization findBySnomedConceptId(String conceptCodeId) {
        Query query = entityManager
                .createQuery("SELECT x FROM CVCImmunization x WHERE x.snomedConceptId = :snomedConceptId");
        query.setParameter("snomedConceptId", conceptCodeId);
        query.setMaxResults(1);
        List<CVCImmunization> result = query.getResultList();
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @Override
    public List<CVCImmunization> query(String term, boolean includeGenerics, boolean includeBrands) {
        if (!includeBrands && !includeGenerics) {
            return new ArrayList<CVCImmunization>();
        }

        String segment = "";

        if (includeBrands && !includeGenerics) {
            segment = " AND generic=0 ";
        }
        if (!includeBrands && includeGenerics) {
            segment = " AND generic=1 ";
        }

        Query query = entityManager.createQuery(
                "SELECT x FROM CVCImmunization x WHERE x.displayName like :term OR x.picklistName like :term"
                        + segment);
        query.setParameter("term", "%" + term + "%");
        List<CVCImmunization> results = query.getResultList();
        return results;
    }
}
