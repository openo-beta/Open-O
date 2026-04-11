//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.decisionSupport.model.DSGuideline;
import org.springframework.stereotype.Repository;

@Repository
public class DSGuidelineDaoImpl extends AbstractDaoImpl<DSGuideline> implements DSGuidelineDao {

    public DSGuidelineDaoImpl() {
        super(DSGuideline.class);
    }

    @Override
    public DSGuideline findByUUID(String uuid) {
        String sql = "select c from DSGuideline c where c.uuid = ?1 and c.status = 'A' order by c.dateStart desc";

        Query query = entityManager.createQuery(sql);
        query.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<DSGuideline> list = query.getResultList();

        if (list == null || list.size() == 0) {
            return null;
        }

        return list.get(0);
    }

    @Override
    public List<DSGuideline> getDSGuidelinesByProvider(String providerNo) {
        String sql = "select c from DSGuideline c, DSGuidelineProviderMapping m where c.uuid = m.guidelineUUID and m.providerNo = ?1 and c.status = 'A'";

        Query query = entityManager.createQuery(sql);
        query.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<DSGuideline> list = query.getResultList();

        return list;
    }
}
