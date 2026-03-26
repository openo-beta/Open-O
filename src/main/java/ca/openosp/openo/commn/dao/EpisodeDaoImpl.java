//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collection;
import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Episode;
import org.springframework.stereotype.Repository;

@Repository
public class EpisodeDaoImpl extends AbstractDaoImpl<Episode> implements EpisodeDao {

    public EpisodeDaoImpl() {
        super(Episode.class);
    }

    public List<Episode> findAll(Integer demographicNo) {
        Query query = entityManager.createQuery("SELECT e FROM Episode e WHERE e.demographicNo=?1 ORDER BY e.startDate DESC");
        query.setParameter(1, demographicNo);
        @SuppressWarnings("unchecked")
        List<Episode> results = query.getResultList();
        return results;
    }

    public List<Episode> findAllCurrent(Integer demographicNo) {
        Query query = entityManager.createQuery("SELECT e FROM Episode e WHERE e.status='Current' AND e.demographicNo=?1 ORDER BY e.startDate DESC");
        query.setParameter(1, demographicNo);
        @SuppressWarnings("unchecked")
        List<Episode> results = query.getResultList();
        return results;
    }

    public List<Episode> findCurrentByCodeTypeAndCodes(Integer demographicNo, String codeType, Collection<String> codes) {
        Query query = entityManager.createQuery("SELECT e FROM Episode e WHERE e.status='Current' AND e.demographicNo=?1 AND e.codingSystem=?2 AND e.code IN (?3) ORDER BY e.startDate DESC");
        query.setParameter(1, demographicNo);
        query.setParameter(2, codeType);
        query.setParameter(3, codes);
        @SuppressWarnings("unchecked")
        List<Episode> results = query.getResultList();
        return results;
    }

    public List<Episode> findCompletedByCodeTypeAndCodes(Integer demographicNo, String codeType, Collection<String> codes) {
        Query query = entityManager.createQuery("SELECT e FROM Episode e WHERE e.status='Complete' AND e.demographicNo=?1 AND e.codingSystem=?2 AND e.code IN (?3) ORDER BY e.startDate DESC");
        query.setParameter(1, demographicNo);
        query.setParameter(2, codeType);
        query.setParameter(3, codes);
        @SuppressWarnings("unchecked")
        List<Episode> results = query.getResultList();
        return results;
    }
}
