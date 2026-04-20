//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.OLISResults;
import org.springframework.stereotype.Repository;

@Repository
public class OLISResultsDaoImpl extends AbstractDaoImpl<OLISResults> implements OLISResultsDao {

    public OLISResultsDaoImpl() {
        super(OLISResults.class);
    }

    @Override
    public boolean hasExistingResult(String requestingHICProviderNo, String queryType, String hash) {
        Query query = entityManager.createQuery("select x from OLISResults x where x.requestingHICProviderNo=?1 and x.queryType=?2 and x.hash = ?3");
        query.setParameter(1, requestingHICProviderNo);
        query.setParameter(2, queryType);
        query.setParameter(3, hash);

        if (!query.getResultList().isEmpty()) {
            return true;
        }

        return false;
    }

    @Override
    public List<OLISResults> getResultList(String requestingHICProviderNo, String queryType) {
        Query query = entityManager.createQuery("select x from OLISResults x where x.requestingHICProviderNo=?1 and x.queryType=?2 and status IS NULL");
        query.setParameter(1, requestingHICProviderNo);
        query.setParameter(2, queryType);

        @SuppressWarnings("unchecked")
        List<OLISResults> results = query.getResultList();

        return results;
    }

    @Override
    public OLISResults findByUUID(String uuid) {
        Query query = entityManager.createQuery("select x from OLISResults x where x.uuid=?1");
        query.setParameter(1, uuid);

        return this.getSingleResultOrNull(query);
    }
}
