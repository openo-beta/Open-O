//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.model.ProgramQueue;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class ProgramQueueDaoImpl extends HibernateDaoSupport implements ProgramQueueDao {

    private Logger log = MiscUtils.getLogger();

    @Override
    public ProgramQueue getProgramQueue(Long queueId) {
        if (queueId == null || queueId.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        ProgramQueue result = getHibernateTemplate().get(ProgramQueue.class, queueId);

        if (log.isDebugEnabled()) {
            log.debug("getProgramQueue: queueId=" + queueId + ",found=" + (result != null));
        }

        return result;
    }

    @Override
    public List<ProgramQueue> getProgramQueuesByProgramId(Long programId) {
        if (programId == null) {
            throw new IllegalArgumentException();
        }

        String queryStr = " FROM ProgramQueue q WHERE q.ProgramId=?0 ORDER BY  q.Id  ";
        List results = getHibernateTemplate().find(queryStr, programId);

        if (log.isDebugEnabled()) {
            log.debug("getProgramQueue: programId=" + programId + ",# of results=" + results.size());
        }

        return results;
    }

    @Override
    public List<ProgramQueue> getActiveProgramQueuesByProgramId(Long programId) {
        if (programId == null) {
            throw new IllegalArgumentException();
        }

        List results = this.getHibernateTemplate().find(
                "from ProgramQueue pq where pq.ProgramId = ?0 and pq.Status = 'active' order by pq.ReferralDate",
                Long.valueOf(programId));

        if (log.isDebugEnabled()) {
            log.debug("getActiveProgramQueuesByProgramId: programId=" + programId + ",# of results=" + results.size());
        }

        return results;
    }

    @Override
    public void saveProgramQueue(ProgramQueue programQueue) {
        if (programQueue == null) {
            return;
        }

        getHibernateTemplate().saveOrUpdate(programQueue);

        if (log.isDebugEnabled()) {
            log.debug("saveProgramQueue: id=" + programQueue.getId());
        }

    }

    @Override
    public ProgramQueue getQueue(Long programId, Long clientId) {
        if (programId == null) {
            throw new IllegalArgumentException();
        }
        if (clientId == null) {
            throw new IllegalArgumentException();
        }

        ProgramQueue result = null;
        String sSQL = "from ProgramQueue pq where pq.ProgramId = ?0 and pq.ClientId = ?1";
        Object[] params = new Object[]{Long.valueOf(programId), Long.valueOf(clientId)};
        List results = this.getHibernateTemplate().find(sSQL,params);

        if (!results.isEmpty()) {
            result = (ProgramQueue) results.get(0);
        }

        if (log.isDebugEnabled()) {
            log.debug("getQueue: programId=" + programId + ",clientId=" + clientId + ",found=" + (result != null));
        }

        return result;
    }

    @Override
    public ProgramQueue getActiveProgramQueue(Long programId, Long demographicNo) {
        if (programId == null || programId.intValue() <= 0) {
            throw new IllegalArgumentException();
        }
        if (demographicNo == null || demographicNo.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        ProgramQueue result = null;

        String sSQL = "from ProgramQueue pq where pq.ProgramId = ?0 and pq.ClientId = ?1 and pq.Status='active'";
        Object[] params = new Object[]{programId, demographicNo};
        List results = this.getHibernateTemplate().find(sSQL, params);
        if (!results.isEmpty()) {
            result = (ProgramQueue) results.get(0);
        }

        if (log.isDebugEnabled()) {
            log.debug("getActiveProgramQueue: programId=" + programId + ",demogaphicNo=" + demographicNo + ",found="
                    + (result != null));
        }

        return result;
    }
}
