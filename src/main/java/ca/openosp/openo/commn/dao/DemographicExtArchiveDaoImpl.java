//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.DemographicExt;
import ca.openosp.openo.commn.model.DemographicExtArchive;
import org.springframework.stereotype.Repository;

@Repository
public class DemographicExtArchiveDaoImpl extends AbstractDaoImpl<DemographicExtArchive>
        implements DemographicExtArchiveDao {

    public DemographicExtArchiveDaoImpl() {
        super(DemographicExtArchive.class);
    }

    @Override
    public List<DemographicExtArchive> getDemographicExtArchiveByDemoAndKey(Integer demographicNo, String key) {
        Query query = entityManager.createQuery(
                "SELECT d from DemographicExtArchive d where d.demographicNo=?1 and d.key = ?2 order by d.dateCreated DESC");
        query.setParameter(1, demographicNo);
        query.setParameter(2, key);

        @SuppressWarnings("unchecked")
        List<DemographicExtArchive> results = query.getResultList();
        return results;
    }

    @Override
    public DemographicExtArchive getDemographicExtArchiveByArchiveIdAndKey(Long archiveId, String key) {
        Query query = entityManager.createQuery(
                "SELECT d from DemographicExtArchive d where d.archiveId=?1 and d.key = ?2 order by d.dateCreated DESC");
        query.setParameter(1, archiveId);
        query.setParameter(2, key);

        return this.getSingleResultOrNull(query);
    }

    @Override
    public List<DemographicExtArchive> getDemographicExtArchiveByArchiveId(Long archiveId) {
        Query query = entityManager.createQuery("SELECT d from DemographicExtArchive d where d.archiveId=?1");
        query.setParameter(1, archiveId);

        @SuppressWarnings("unchecked")
        List<DemographicExtArchive> results = query.getResultList();
        return results;
    }

    @Override
    public List<DemographicExtArchive> getDemographicExtArchiveByDemoReverseCronological(Integer demographicNo) {
        Query query = entityManager.createQuery(
                "SELECT d from DemographicExtArchive d where d.demographicNo=?1 order by d.dateCreated ASC");
        query.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<DemographicExtArchive> results = query.getResultList();
        return results;
    }

    @Override
    public Integer archiveDemographicExt(DemographicExt de) {
        DemographicExtArchive dea = new DemographicExtArchive(de);
        persist(dea);
        return dea.getId();
    }
}
