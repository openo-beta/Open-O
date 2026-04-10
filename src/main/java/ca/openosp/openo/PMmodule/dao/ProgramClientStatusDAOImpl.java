//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import ca.openosp.openo.PMmodule.model.ProgramClientStatus;
import ca.openosp.openo.commn.model.Admission;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;

public class ProgramClientStatusDAOImpl extends HibernateDaoSupport implements ProgramClientStatusDAO {

    private Logger log = MiscUtils.getLogger();
    public SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactoryOverride(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    public List<ProgramClientStatus> getProgramClientStatuses(Integer programId) {
        String sSQL = "from ProgramClientStatus pcs where pcs.programId=?0";
        return (List<ProgramClientStatus>) this.getHibernateTemplate().find(sSQL, programId);
    }

    public void saveProgramClientStatus(ProgramClientStatus status) {
        this.getHibernateTemplate().saveOrUpdate(status);
    }

    public ProgramClientStatus getProgramClientStatus(String id) {
        if (id == null || Integer.valueOf(id) < 0) {
            throw new IllegalArgumentException();
        }

        ProgramClientStatus pcs = null;
        pcs = this.getHibernateTemplate().get(ProgramClientStatus.class, Integer.valueOf(id));
        if (pcs != null) return pcs;
        else return null;
    }

    public void deleteProgramClientStatus(String id) {
        this.getHibernateTemplate().delete(getProgramClientStatus(id));
    }

    public boolean clientStatusNameExists(Integer programId, String statusName) {
        if (programId == null || programId.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        if (statusName == null || statusName.length() <= 0) {
            throw new IllegalArgumentException();
        }

        // Session session = getSession();
        Session session = sessionFactory.getCurrentSession();
        List teams = new ArrayList();
        try {
            Query query = session.createQuery("select pt.id from ProgramClientStatus pt where pt.programId = ?1 and pt.name = ?2");
            query.setLong(1, programId.longValue());
            query.setString(2, statusName);

            teams = query.list();

            if (log.isDebugEnabled()) {
                log.debug("teamNameExists: programId = " + programId + ", statusName = " + statusName + ", result = " + !teams.isEmpty());
            }
        } finally {
            //releaseSession(session);
            session.close();
        }
        return !teams.isEmpty();
    }

    public List<Admission> getAllClientsInStatus(Integer programId, Integer statusId) {
        if (programId == null || programId <= 0) {
            throw new IllegalArgumentException();
        }

        if (statusId == null || statusId <= 0) {
            throw new IllegalArgumentException();
        }

        String sSQL = "from Admission a where a.ProgramId = ?0 and a.TeamId = ?1 and a.AdmissionStatus='current'";
        List<Admission> results = (List<Admission>) this.getHibernateTemplate().find(sSQL, new Object[]{programId, statusId});

        if (log.isDebugEnabled()) {
            log.debug("getAdmissionsInTeam: programId= " + programId + ",statusId=" + statusId + ",# results=" + results.size());
        }

        return results;
    }
}
