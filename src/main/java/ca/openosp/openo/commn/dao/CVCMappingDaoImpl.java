//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CVCMapping;
import org.springframework.stereotype.Repository;

@Repository
public class CVCMappingDaoImpl extends AbstractDaoImpl<CVCMapping> implements CVCMappingDao {

    public CVCMappingDaoImpl() {
        super(CVCMapping.class);
    }

    public CVCMapping findByOscarName(String oscarName) {
        Query query = entityManager.createQuery("SELECT x FROM CVCMapping x WHERE x.oscarName = :oscarName");
        query.setParameter("oscarName", oscarName);

        return this.getSingleResultOrNull(query);
    }

    public CVCMapping findBySnomedId(String cvcSnomedId) {
        Query query = entityManager.createQuery("SELECT x FROM CVCMapping x WHERE x.cvcSnomedId = :cvcSnomedId");
        query.setParameter("cvcSnomedId", cvcSnomedId);

        return this.getSingleResultOrNull(query);
    }

    public List<CVCMapping> findMultipleByOscarName(String oscarName) {
        Query query = entityManager.createQuery("SELECT x FROM CVCMapping x WHERE x.oscarName = :oscarName");
        query.setParameter("oscarName", oscarName);

        List<CVCMapping> results = query.getResultList();
        return results;
    }
}
