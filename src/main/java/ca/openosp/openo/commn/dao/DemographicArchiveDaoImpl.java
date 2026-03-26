//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.DemographicArchive;
import org.springframework.stereotype.Repository;

import ca.openosp.openo.util.StringUtils;
import ca.openosp.openo.util.UtilDateUtilities;

@Repository
public class DemographicArchiveDaoImpl extends AbstractDaoImpl<DemographicArchive> implements DemographicArchiveDao {

    public DemographicArchiveDaoImpl() {
        super(DemographicArchive.class);
    }

    @Override
    public List<DemographicArchive> findByDemographicNo(Integer demographicNo) {

        String sqlCommand = "select x from DemographicArchive x where x.demographicNo=?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<DemographicArchive> results = query.getResultList();

        return (results);
    }

    @Override
    public List<DemographicArchive> findRosterStatusHistoryByDemographicNo(Integer demographicNo) {

        String sqlCommand = "select x from DemographicArchive x where x.demographicNo=?1 order by x.id desc";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<DemographicArchive> results = query.getResultList();

        // Remove entries with identical rosterStatus
        String this_rs, next_rs;
        Date this_rd, next_rd, this_td, next_td;
        for (int i = 0; i < results.size() - 1; i++) {
            this_rs = StringUtils.noNull(results.get(i).getRosterStatus());
            next_rs = StringUtils.noNull(results.get(i + 1).getRosterStatus());
            this_rd = results.get(i).getRosterDate();
            next_rd = results.get(i + 1).getRosterDate();
            this_td = results.get(i).getRosterTerminationDate();
            next_td = results.get(i + 1).getRosterTerminationDate();

            if (this_rs.equalsIgnoreCase(next_rs) &&
                    UtilDateUtilities.nullSafeCompare(this_rd, next_rd) == 0 &&
                    UtilDateUtilities.nullSafeCompare(this_td, next_td) == 0) {
                results.remove(i);
                i--;
            }
        }
        return (results);
    }

    @Override
    public Long archiveRecord(Demographic d) {
        DemographicArchive da = new DemographicArchive(d);
        persist(da);
        return da.getId();
    }

    @Override
    public List<DemographicArchive> findByDemographicNoChronologically(Integer demographicNo) {

        String sqlCommand = "select x from DemographicArchive x where x.demographicNo=?1 order by x.lastUpdateDate ASC";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<DemographicArchive> results = query.getResultList();

        return (results);
    }

    @Override
    public List<Object[]> findMetaByDemographicNo(Integer demographicNo) {

        String sqlCommand = "select x.id,x.demographicNo,x.lastUpdateUser,x.lastUpdateDate from DemographicArchive x where x.demographicNo=?1 order by x.lastUpdateDate DESC, x.id DESC";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return (results);
    }
}
