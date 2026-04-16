//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.FaxJob;
import org.springframework.stereotype.Repository;

@Repository
public class FaxJobDaoImpl extends AbstractDaoImpl<FaxJob> implements FaxJobDao {

    public FaxJobDaoImpl() {
        super(FaxJob.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FaxJob> getFaxStatusByDateDemographicProviderStatusTeam(String demographic_no, String provider_no,
                                                                        String status, String team, Date beginDate, Date endDate) {

        StringBuilder sql = new StringBuilder("select job from FaxJob job ");

        if (demographic_no != null || status != null || team != null || beginDate != null || endDate != null
                || provider_no != null) {
            sql.append(" where ");
        }

        boolean firstClause = true;

        if (demographic_no != null) {
            if (!firstClause) sql.append(" and ");
            sql.append("job.demographicNo = :demographic");
            firstClause = false;
        }
        if (status != null) {
            if (!firstClause) sql.append(" and ");
            sql.append("job.status = :status");
            firstClause = false;
        }
        if (team != null) {
            if (!firstClause) sql.append(" and ");
            sql.append("job.user = :team");
            firstClause = false;
        }
        if (beginDate != null) {
            if (!firstClause) sql.append(" and ");
            sql.append("job.stamp >= :beginDate");
            firstClause = false;
        }
        if (endDate != null) {
            if (!firstClause) sql.append(" and ");
            sql.append("job.stamp <= :endDate");
            firstClause = false;
        }
        if (provider_no != null) {
            if (!firstClause) sql.append(" and ");
            sql.append("job.oscarUser = :provider");
            firstClause = false;
        }
        
        // Create query and set parameters
        Query query = entityManager.createQuery(sql.toString());
        
        if (demographic_no != null) {
            query.setParameter("demographic", Integer.parseInt(demographic_no));
        }
        if (status != null) {
            query.setParameter("status", FaxJob.STATUS.valueOf(status));
        }
        if (team != null) {
            query.setParameter("team", team);
        }
        if (beginDate != null) {
            query.setParameter("beginDate", beginDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        if (provider_no != null) {
            query.setParameter("provider", provider_no);
        }

        List<FaxJob> faxJobList = query.getResultList();

        Collections.sort(faxJobList);

        return faxJobList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FaxJob> getReadyToSendFaxes(String number) {
        Query query = entityManager.createQuery(
                "select job from FaxJob job where job.status = ?1 and job.fax_line = ?2 and job.jobId is null");

        // these faxes are "waiting" to be sent
        // they become "sent" after they clear the api
        query.setParameter(1, FaxJob.STATUS.WAITING);
        query.setParameter(2, number);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FaxJob> getInprogressFaxesByJobId() {
        Query query = entityManager.createQuery(
                "select job from FaxJob job where (job.status = ?1 or job.status = ?2) and job.jobId is not null");

        query.setParameter(1, FaxJob.STATUS.SENT);
        query.setParameter(2, FaxJob.STATUS.WAITING);

        List<FaxJob> faxJobList = query.getResultList();

        Collections.sort(faxJobList);

        return faxJobList;
    }

}
