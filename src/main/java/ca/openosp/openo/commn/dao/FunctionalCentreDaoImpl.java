//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.FunctionalCentre;
import org.springframework.stereotype.Repository;

@Repository
public class FunctionalCentreDaoImpl extends AbstractDaoImpl<FunctionalCentre> implements FunctionalCentreDao {

    public FunctionalCentreDaoImpl() {
        super(FunctionalCentre.class);
    }

    @Override
    public List<FunctionalCentre> findAll() {
        Query query = entityManager.createQuery("select x from " + modelClass.getSimpleName() + " x");

        @SuppressWarnings("unchecked")
        List<FunctionalCentre> results = query.getResultList();

        return (results);
    }

    @Override
    public List<FunctionalCentre> findInUseByFacility(Integer facilityId) {
        Query query = entityManager.createNativeQuery("select distinct FunctionalCentre.* from program,FunctionalCentre where facilityId=?1 and program.functionalCentreId=FunctionalCentre.accountId", modelClass);
        query.setParameter(1, facilityId);

        @SuppressWarnings("unchecked")
        List<FunctionalCentre> results = query.getResultList();

        return (results);
    }
}
